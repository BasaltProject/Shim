/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing.text;

import java.io.Serializable;
import javax.swing.JFormattedTextField;
import javax.swing.JFormattedTextField.AbstractFormatter;

public class DefaultFormatterFactory extends JFormattedTextField.AbstractFormatterFactory implements Serializable {
  private AbstractFormatter defaultFormat;
  private AbstractFormatter displayFormat;
  private AbstractFormatter editFormat;
  private AbstractFormatter nullFormat;

  public DefaultFormatterFactory() { }

  public DefaultFormatterFactory(AbstractFormatter defaultFormat) { this(defaultFormat, null); }

  public DefaultFormatterFactory(AbstractFormatter defaultFormat, AbstractFormatter displayFormat) {
    this(defaultFormat, displayFormat, null);
  }

  public DefaultFormatterFactory(AbstractFormatter defaultFormat, AbstractFormatter displayFormat,
      AbstractFormatter editFormat) {
    this(defaultFormat, displayFormat, editFormat, null);
  }

  public DefaultFormatterFactory(AbstractFormatter defaultFormat, AbstractFormatter displayFormat,
      AbstractFormatter editFormat, AbstractFormatter nullFormat) {
    this.defaultFormat = defaultFormat;
    this.displayFormat = displayFormat;
    this.editFormat = editFormat;
    this.nullFormat = nullFormat;
  }

  public void setDefaultFormatter(AbstractFormatter atf) { defaultFormat = atf; }

  public AbstractFormatter getDefaultFormatter() { return defaultFormat; }

  public void setDisplayFormatter(AbstractFormatter atf) { displayFormat = atf; }

  public AbstractFormatter getDisplayFormatter() { return displayFormat; }

  public void setEditFormatter(AbstractFormatter atf) { editFormat = atf; }

  public AbstractFormatter getEditFormatter() { return editFormat; }

  public void setNullFormatter(AbstractFormatter atf) { nullFormat = atf; }

  public AbstractFormatter getNullFormatter() { return nullFormat; }

  public AbstractFormatter getFormatter(JFormattedTextField source) {
    AbstractFormatter format = null;
    if (source == null) {
      return null;
    }
    Object value = source.getValue();
    if (value == null) {
      format = getNullFormatter();
    }
    if (format == null) {
      if (source.hasFocus()) {
        format = getEditFormatter();
      }
      if (format == null) {
        format = getDisplayFormatter();
        if (format == null) {
          format = getDefaultFormatter();
        }
      }
    }
    return format;
  }
}
