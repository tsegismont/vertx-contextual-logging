package test.logging;

import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import io.vertx.core.Vertx;
import io.vertx.core.impl.ContextInternal;

public class VertxContextConverter extends ClassicConverter {

  private static final String DEFAULT_VALUE = "";

  @Override
  public String convert(ILoggingEvent event) {
    ContextInternal context = (ContextInternal) Vertx.currentContext();
    return context != null ? (String) context.localContextData().getOrDefault("requestId", DEFAULT_VALUE) : DEFAULT_VALUE;
  }
}