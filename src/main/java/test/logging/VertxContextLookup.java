package test.logging;

import io.vertx.core.Vertx;
import io.vertx.core.impl.ContextInternal;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.pattern.ConverterKeys;
import org.apache.logging.log4j.core.pattern.LogEventPatternConverter;
import org.apache.logging.log4j.core.pattern.PatternConverter;

@Plugin(name = "VertxContextLookup", category = PatternConverter.CATEGORY)
@ConverterKeys("vertxCtx")
public class VertxContextLookup extends LogEventPatternConverter {

  private static final String DEFAULT_VALUE = "";

  private VertxContextLookup() {
    super("vertxCtx", "vertxCtx");
  }

  @Override
  public void format(LogEvent event, StringBuilder toAppendTo) {
    ContextInternal context = (ContextInternal) Vertx.currentContext();
    String val;
    if (context != null) {
      val = (String) context.localContextData().getOrDefault("requestId", DEFAULT_VALUE);
    } else {
      val = DEFAULT_VALUE;
    }
    toAppendTo.append(val);
  }

  public static VertxContextLookup newInstance(final String[] options) {
    return new VertxContextLookup();
  }
}