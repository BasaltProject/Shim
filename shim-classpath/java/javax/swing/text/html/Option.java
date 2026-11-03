/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */
package javax.swing.text.html;

import java.io.Serializable;
import javax.swing.text.AttributeSet;

public class Option implements Serializable {
  private String label;
  private boolean selected;
  private final String value;
  private final AttributeSet attr;

  public Option(AttributeSet attr) {
    this.attr = attr.copyAttributes();
    selected = (attr.getAttribute(HTML.Attribute.SELECTED) != null);
    Object v = attr.getAttribute(HTML.Attribute.VALUE);
    value = v == null ? null : v.toString();
  }

  public void setLabel(String label) { this.label = label; }

  public String getLabel() { return label; }

  public AttributeSet getAttributes() { return attr; }

  public String toString() { return label; }

  protected void setSelection(boolean state) { selected = state; }

  public boolean isSelected() { return selected; }

  public String getValue() {
    if (value == null) {
      return label;
    }
    return value;
  }
}
