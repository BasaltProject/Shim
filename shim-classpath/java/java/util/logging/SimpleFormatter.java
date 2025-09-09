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
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class SimpleFormatter extends Formatter {
  private static final DateTimeFormatter formatter =
      DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss.SSS");

  public synchronized String format(LogRecord record) {
    LocalDateTime ldt = LocalDateTime.ofInstant(
        record.getInstant() != null ? record.getInstant() : Instant.now(),
        ZoneId.systemDefault());
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
    return String.format("%s %s %s: %s%s%n",
        ldt.format(formatter),
        source,
        record.getLevel().getLocalizedName(),
        message,
        throwable);
  }
}
