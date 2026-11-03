/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing.text;

import java.text.DateFormat;
import java.text.Format;

public class DateFormatter extends InternationalFormatter {
  public DateFormatter() {
    this(DateFormat.getDateInstance());
  }

  public DateFormatter(DateFormat format) {
    super(format);
    setFormat(format);
  }

  public void setFormat(DateFormat format) { super.setFormat(format); }

  public void setFormat(Format format) { super.setFormat(format); }
}
