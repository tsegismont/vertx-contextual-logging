package test.logging;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.codec.BodyCodec;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.UUID;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class SimpleServer extends AbstractVerticle {

  private static final Logger log = LogManager.getLogger(SimpleServer.class);

  private MongoClient mongoClient;
  private HttpRequest<JsonObject> request;

  public static void main(String[] args) {
    Vertx.vertx().deployVerticle(new SimpleServer());
  }

  @Override
  public void start() {
    mongoClient = MongoClient.createShared(vertx, new JsonObject());

    WebClient webClient = WebClient.create(vertx);
    request = webClient.getAbs("http://worldclockapi.com/api/json/utc/now")
      .as(BodyCodec.jsonObject());

    vertx.createHttpServer()
      .requestHandler(req -> {

        Util.putRequestId();

        String param = req.getParam("param");
        log.info("Server got new request: {}", param);

        mongoClient.insert("test", new JsonObject().put("user", UUID.randomUUID().toString()), ar -> {

          log.info("After inserting in Mongo: {}", param);

          vertx.executeBlocking(fut -> {
            log.info("Before waiting: {}", param);
            try {
              MILLISECONDS.sleep(50);
            } catch (InterruptedException e) {
              Thread.currentThread().interrupt();
            }
            fut.complete();
          }, false, bar -> {
            log.info("End of waiting: {}", param);

            request.send(rar -> {
              log.info("Received webClient response for ({}): {}", param, rar.result().body().getString("currentDateTime"));

              req.response().end("OK!\r\n");
            });
          });

        });

      }).listen(8080);
  }

}
