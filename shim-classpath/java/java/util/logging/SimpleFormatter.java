/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.util.logging;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class SimpleFormatter extends Formatter {
  private static final String kDefaultFormat = "%1$tb %1$td, %1$tY %1$tl:%1$tM:%1$tS %1$Tp %2$s%n%4$s: %5$s%6$s%n";

  private final String format;

  static String getLoggingProperty(String name) {
    return LogManager.getLogManager().getProperty(name);
  }

  public SimpleFormatter() {
    String fmt = getLoggingProperty("java.util.logging.SimpleFormatter.format");
    if (fmt == null) {
      fmt = System.getProperty("java.util.logging.SimpleFormatter.format");
    }
    format = checkFormat(fmt);
  }

  private static String checkFormat(String fmt) {
    if (fmt == null) {
      return kDefaultFormat;
    }
    try {
      String.format(fmt, ZonedDateTime.now(), "", "", "", "", "");
    } catch (IllegalArgumentException e) {
      return kDefaultFormat;
    }
    return fmt;
  }

  @Override
  public String format(LogRecord record) {
    ZonedDateTime zdt = ZonedDateTime.ofInstant(record.getInstant(), ZoneId.systemDefault());
    String source;
    if (record.getSourceClassName() != null) {
      source = record.getSourceClassName();
      if (record.getSourceMethodName() != null) {
        source += " " + record.getSourceMethodName();
      }
    } else {
      source = record.getLoggerName();
    }
    String message = formatMessage(record);
    String throwable = "";
    if (record.getThrown() != null) {
      StringWriter sw = new StringWriter();
      PrintWriter pw = new PrintWriter(sw);
      pw.println();
      record.getThrown().printStackTrace(pw);
      pw.close();
      throwable = sw.toString();
    }
    return String.format(format, zdt, source, record.getLoggerName(), record.getLevel().getLocalizedLevelName(),
        message, throwable);
  }
}
