package test.logging;

import io.vertx.core.AbstractVerticle;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class SimpleConsumer extends AbstractVerticle {

  private static final Logger log = LogManager.getLogger(SimpleConsumer.class);

  @Override
  public void start() throws Exception {
    vertx.eventBus().consumer("test", msg -> {

      log.info("Receiver got message");

      vertx.executeBlocking(fut -> {
        log.info("Before recevier waiting");
        try {
          MILLISECONDS.sleep(50);
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
        }
        fut.complete();
      }, false, ar -> {
        log.info("End of receiver waiting");
        msg.reply("ok");
      });

    });
  }
}
