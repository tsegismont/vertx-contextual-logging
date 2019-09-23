package io.github.tsegismont.vertx.contextual.logging;

import io.vertx.core.Vertx;
import io.vertx.core.impl.ContextInternal;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;
import java.util.function.BiFunction;
import java.util.logging.Formatter;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;

public final class JULContextualDataFormatter extends Formatter {

  private static final String placeholderPrefix = "%{";
  private static final String placeholderSuffix = "}";

  private final String template;

  private final Date dat = new Date();

  private static final List<String> RESERVED = Arrays.asList("date", "source", "logger", "level", "message", "thrown");

  private final List<BiFunction<LogRecord, ContextInternal, Object>> resolvers = new ArrayList<>();

  public JULContextualDataFormatter() {
    this(LogManager.getLogManager().getProperty("format"));
  }

  public JULContextualDataFormatter(String template) {
    // add the default resolvers
    // 1. date
    resolvers.add((record, ctx) -> {
      // with java 11 this should be replaced with the new time APIs
      dat.setTime(record.getMillis());
      return dat;
    });
    // 2. source
    resolvers.add((record, ctx) -> {
      String source;
      if (record.getSourceClassName() != null) {
        source = record.getSourceClassName();
        if (record.getSourceMethodName() != null) {
          source = source + " " + record.getSourceMethodName();
        }
      } else {
        source = record.getLoggerName();
      }
      return source;
    });
    // 3. logger
    resolvers.add((record, ctx) -> record.getLoggerName());
    // 4. level
    resolvers.add((record, ctx) -> record.getLevel().getLocalizedName());
    // 5. message
    resolvers.add((record, ctx) -> formatMessage(record));
    // 6. thrown
    resolvers.add((record, ctx) -> {
      String throwable = "";
      if (record.getThrown() != null) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        pw.println();
        record.getThrown().printStackTrace(pw);
        pw.close();
        throwable = sw.toString();
      }
      return throwable;
    });

    this.template = parseStringValue(template);
  }

  private String parseStringValue(String template) {

    StringBuilder buf = new StringBuilder(template);

    int startIndex = template.indexOf(placeholderPrefix);
    while (startIndex != -1) {
      int endIndex = findPlaceholderEndIndex(buf, startIndex);
      if (endIndex != -1) {
        String placeholder = buf.substring(startIndex + placeholderPrefix.length(), endIndex);

        int index = RESERVED.indexOf(placeholder);

        if (index == -1) {
          // lookup default value
          int defIndex = placeholder.indexOf(":-");

          // need to lock to use inside lambda
          final String defValue;
          final String ctxKey;

          if (defIndex != -1) {
            ctxKey = placeholder.substring(0, defIndex);
            defValue = placeholder.substring(defIndex + 2);
          } else {
            defValue = "-";
            ctxKey = placeholder;
          }

          // placeholder is not present so we need to compute it at runtime
          resolvers.add((record, ctx) -> {
            if (ctx != null) {
              return ContextualData.getOrDefault(ctxKey, defValue);
            } else {
              return defValue;
            }
          });
          index = resolvers.size();
        }

        String sIndex = "%" + index;
        buf.replace(startIndex, endIndex + placeholderSuffix.length(), sIndex);
        startIndex = buf.indexOf(placeholderPrefix, startIndex + sIndex.length());
      } else {
        startIndex = -1;
      }
    }

    return buf.toString();
  }

  private static int findPlaceholderEndIndex(CharSequence buf, int startIndex) {
    int index = startIndex + placeholderPrefix.length();
    int withinNestedPlaceholder = 0;
    while (index < buf.length()) {
      if (substringMatch(buf, index, placeholderSuffix)) {
        if (withinNestedPlaceholder > 0) {
          withinNestedPlaceholder--;
          index = index + placeholderPrefix.length() - 1;
        } else {
          return index;
        }
      } else if (substringMatch(buf, index, placeholderPrefix)) {
        withinNestedPlaceholder++;
        index = index + placeholderPrefix.length();
      } else {
        index++;
      }
    }
    return -1;
  }

  private static boolean substringMatch(CharSequence str, int index, CharSequence substring) {
    for (int j = 0; j < substring.length(); j++) {
      int i = index + j;
      if (i >= str.length() || str.charAt(i) != substring.charAt(j)) {
        return false;
      }
    }
    return true;
  }

  @Override
  public String format(LogRecord record) {
    final Object[] args = new Object[resolvers.size()];
    final ContextInternal context = (ContextInternal) Vertx.currentContext();
    // process the placeholder values
    for (int i = 0; i < args.length; i++) {
      args[i] = resolvers.get(i).apply(record, context);
    }
    // format
    return String.format(template, args);
  }
}
