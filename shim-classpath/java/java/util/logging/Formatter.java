/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.util.logging;

import java.text.MessageFormat;
import java.util.ResourceBundle;

public abstract class Formatter {
  protected Formatter() {}

  public abstract String format(LogRecord record);

  public String getHead(Handler h) {
    return "";
  }

  public String getTail(Handler h) {
    return "";
  }

  public String formatMessage(LogRecord record) {
    String format = record.getMessage();
    ResourceBundle bundle = record.getResourceBundle();
    if (bundle != null && format != null) {
      try {
        format = bundle.getString(format);
      } catch (Exception ignored) {
      }
    }
    if (format == null) {
      return "";
    }
    Object[] params = record.getParameters();
    if (params == null || params.length == 0) {
      return format;
    }
    if (format.indexOf("{0") >= 0 || format.indexOf("{1") >= 0 ||
        format.indexOf("{2") >= 0 || format.indexOf("{3") >= 0) {
      try {
        return MessageFormat.format(format, params);
      } catch (Exception e) {
        return format;
      }
    }
    return format;
  }
}
