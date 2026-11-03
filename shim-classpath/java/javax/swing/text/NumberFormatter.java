/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing.text;

import java.text.Format;
import java.text.NumberFormat;
import java.text.ParseException;

public class NumberFormatter extends InternationalFormatter {
  public NumberFormatter() {
    this(NumberFormat.getNumberInstance());
  }

  public NumberFormatter(NumberFormat format) {
    super(format);
    setFormat(format);
    setAllowsInvalid(true);
    setCommitsOnValidEdit(false);
    setOverwriteMode(false);
  }

  public void setFormat(Format format) {
    super.setFormat(format);
    if (format instanceof java.text.DecimalFormat) {
      setAllowsInvalid(true);
    }
  }

  Object stringToValue(String text, Format f) throws ParseException {
    if (f == null) {
      return text;
    }
    Object value = f.parseObject(text);
    return convertValueToValueClass(value, getValueClass());
  }

  private Object convertValueToValueClass(Object value, Class<?> valueClass) {
    if (valueClass != null && value instanceof Number) {
      Number n = (Number) value;
      if (valueClass == Integer.class) {
        return Integer.valueOf(n.intValue());
      } else if (valueClass == Long.class) {
        return Long.valueOf(n.longValue());
      } else if (valueClass == Float.class) {
        return Float.valueOf(n.floatValue());
      } else if (valueClass == Double.class) {
        return Double.valueOf(n.doubleValue());
      } else if (valueClass == Byte.class) {
        return Byte.valueOf(n.byteValue());
      } else if (valueClass == Short.class) {
        return Short.valueOf(n.shortValue());
      }
    }
    return value;
  }
}
