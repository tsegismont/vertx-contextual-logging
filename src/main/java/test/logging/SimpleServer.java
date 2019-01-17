package test.logging;

import io.vertx.core.AbstractVerticle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class SimpleServer extends AbstractVerticle {

  private Logger log = LoggerFactory.getLogger(SimpleServer.class);

  @Override
  public void start() {
    vertx.createHttpServer()
      .requestHandler(req -> {

        Util.putRequestId();

        String param = req.getParam("param");
        log.info("Server got new request: {}", param);

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

      }).listen(8080);
  }

}
