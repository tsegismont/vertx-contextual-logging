package test.logging;

import io.vertx.core.Vertx;
import io.vertx.core.impl.ContextInternal;

import java.util.UUID;

public class Util {

  public static void putRequestId() {
    putRequestId(UUID.randomUUID().toString());
  }

  public static void putRequestId(String requestId) {
    ContextInternal context = (ContextInternal) Vertx.currentContext();
    context.localContextData().put("requestId", requestId);
  }

  public static String getRequestId() {
    ContextInternal context = (ContextInternal) Vertx.currentContext();
    return (String) context.localContextData().get("requestId");
  }

  public static String getThreadName() {
    return Thread.currentThread().getName();
  }
}
