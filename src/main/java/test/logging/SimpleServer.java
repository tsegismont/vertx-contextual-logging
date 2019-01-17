package test.logging;

import io.vertx.core.AbstractVerticle;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class SimpleServer extends AbstractVerticle {

  @Override
  public void start() {
    vertx.createHttpServer()
      .requestHandler(req -> {

        Util.putRequestId();

        String param = req.getParam("param");
        System.out.printf("[%s] [%s] Server got new request: %s%n", Util.getThreadName(), Util.getRequestId(), param);

        vertx.executeBlocking(fut -> {
          System.out.printf("[%s] [%s] Before waiting: %s%n", Util.getThreadName(), Util.getRequestId(), param);
          try {
            MILLISECONDS.sleep(50);
          } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
          }
          fut.complete();
        }, false, bar -> {
          System.out.printf("[%s] [%s] End of request: %s%n", Util.getThreadName(), Util.getRequestId(), param);
          req.response().end("OK!\r\n");
        });

      }).listen(8080);
  }

}
