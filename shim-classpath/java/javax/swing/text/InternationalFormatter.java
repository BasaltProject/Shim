/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing.text;

import java.text.Format;
import java.text.ParseException;
import javax.swing.Action;
import javax.swing.JFormattedTextField;

public class InternationalFormatter extends DefaultFormatter {
  private Format format;
  private Comparable<?> max;
  private Comparable<?> min;

  public InternationalFormatter() {
    setOverwriteMode(false);
  }

  public InternationalFormatter(Format format) {
    this();
    setFormat(format);
  }

  public void setFormat(Format format) { this.format = format; }

  public Format getFormat() { return format; }

  public void setMinimum(Comparable<?> minimum) {
    if (getValueClass() == null && minimum != null) {
      setValueClass(minimum.getClass());
    }
    min = minimum;
  }

  public Comparable<?> getMinimum() { return min; }

  public void setMaximum(Comparable<?> max) {
    if (getValueClass() == null && max != null) {
      setValueClass(max.getClass());
    }
    this.max = max;
  }

  public Comparable<?> getMaximum() { return max; }

  public void install(JFormattedTextField ftf) { super.install(ftf); }

  public String valueToString(Object value) throws ParseException {
    if (value == null) {
      return "";
    }
    Format f = getFormat();
    if (f == null) {
      return value.toString();
    }
    return f.format(value);
  }

  public Object stringToValue(String text) throws ParseException {
    Object value = stringToValue(text, getFormat());
    if (value != null && getValueClass() != null && !getValueClass().isInstance(value)) {
      value = super.stringToValue(value.toString());
    }
    try {
      if (!isValidValue(value, true)) {
        throw new ParseException("Value not within min/max range", 0);
      }
    } catch (ClassCastException cce) {
      throw new ParseException("Class cast exception comparing values: " + cce, 0);
    }
    return value;
  }

  public java.text.Format.Field[] getFields(int offset) { return new java.text.Format.Field[0]; }

  public Object clone() throws CloneNotSupportedException { return super.clone(); }

  protected Action[] getActions() { return null; }

  Object stringToValue(String text, Format f) throws ParseException {
    if (f == null) {
      return text;
    }
    return f.parseObject(text);
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  boolean isValidValue(Object value, boolean wantsCCE) {
    Comparable<?> min = getMinimum();
    try {
      if (min != null && ((Comparable) min).compareTo(value) > 0) {
        return false;
      }
    } catch (ClassCastException cce) {
      if (wantsCCE) {
        throw cce;
      }
      return false;
    }
    Comparable<?> max = getMaximum();
    try {
      if (max != null && ((Comparable) max).compareTo(value) < 0) {
        return false;
      }
    } catch (ClassCastException cce) {
      if (wantsCCE) {
        throw cce;
      }
      return false;
    }
    return true;
  }
}
