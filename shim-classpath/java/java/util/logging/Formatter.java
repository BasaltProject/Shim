/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.util.logging;

public abstract class Formatter {
  protected Formatter() {
  }

  public abstract String format(LogRecord record);

  public String getHead(Handler h) {
    return "";
  }

  public String getTail(Handler h) {
    return "";
  }

  public String formatMessage(LogRecord record) {
    String format = record.getMessage();
    java.util.ResourceBundle catalog = record.getResourceBundle();
    if (catalog != null) {
      try {
        format = catalog.getString(format);
      } catch (java.util.MissingResourceException ex) {
        format = record.getMessage();
      }
    }
    try {
      Object[] parameters = record.getParameters();
      if (parameters == null || parameters.length == 0) {
        return format;
      }
      int index = -1;
      int fence = format.length() - 1;
      while ((index = format.indexOf('{', index + 1)) > -1) {
        if (index >= fence) {
          break;
        }
        char digit = format.charAt(index + 1);
        if (digit >= '0' && digit <= '9') {
          return java.text.MessageFormat.format(format, parameters);
        }
      }
      return format;
    } catch (Exception ex) {
      return format;
    }
  }
}
