package test.logging;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class SimpleServer extends AbstractVerticle {

  private static final Logger log = LoggerFactory.getLogger(SimpleServer.class);

  private MongoClient mongoClient;

  @Override
  public void start() {
    mongoClient = MongoClient.createShared(vertx, new JsonObject());

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
            log.info("End of request: {}", param);
            req.response().end("OK!\r\n");
          });

        });

      }).listen(8080);
  }

}
