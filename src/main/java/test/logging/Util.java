package test.logging;

import io.vertx.core.Vertx;
import io.vertx.core.impl.ContextInternal;

import java.util.UUID;

public class Util {

  public static void putRequestId() {
    ContextInternal context = (ContextInternal) Vertx.currentContext();
    context.localContextData().put("requestId", UUID.randomUUID().toString());
  }

  public static String getRequestId() {
    ContextInternal context = (ContextInternal) Vertx.currentContext();
    return (String) context.localContextData().get("requestId");
  }

  public static String getThreadName() {
    return Thread.currentThread().getName();
  }
}
