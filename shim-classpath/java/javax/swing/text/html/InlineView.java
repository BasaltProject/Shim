/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */
package javax.swing.text.html;

import java.awt.Color;
import java.awt.Shape;
import javax.swing.event.DocumentEvent;
import javax.swing.text.AttributeSet;
import javax.swing.text.Element;
import javax.swing.text.LabelView;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;

public class InlineView extends LabelView {
  private boolean nowrap;
  private AttributeSet attr;

  public InlineView(Element elem) {
    super(elem);
    StyleSheet sheet = getStyleSheet();
    attr = sheet.getViewAttributes(this);
  }

  public void insertUpdate(DocumentEvent e, Shape a, ViewFactory f) {
    super.insertUpdate(e, a, f);
  }

  public void removeUpdate(DocumentEvent e, Shape a, ViewFactory f) {
    super.removeUpdate(e, a, f);
  }

  public void changedUpdate(DocumentEvent e, Shape a, ViewFactory f) {
    super.changedUpdate(e, a, f);
    StyleSheet sheet = getStyleSheet();
    attr = sheet.getViewAttributes(this);
    preferenceChanged(null, true, true);
  }

  public AttributeSet getAttributes() {
    if (attr == null) {
      attr = getStyleSheet().getViewAttributes(this);
    }
    return attr;
  }

  public float getMinimumSpan(int axis) {
    if (axis == View.X_AXIS && nowrap) {
      return super.getPreferredSpan(axis);
    }
    return super.getMinimumSpan(axis);
  }

  public int getBreakWeight(int axis, float pos, float len) {
    if (nowrap) {
      return View.BadBreakWeight;
    }
    return super.getBreakWeight(axis, pos, len);
  }

  public View breakView(int axis, int offset, float pos, float len) {
    return super.breakView(axis, offset, pos, len);
  }

  protected void setPropertiesFromAttributes() {
    super.setPropertiesFromAttributes();
    AttributeSet a = getAttributes();
    Object decor = a.getAttribute(CSS.Attribute.TEXT_DECORATION);
    boolean u = (decor != null) ? (decor.toString().indexOf("underline") >= 0) : false;
    setUnderline(u);
    boolean s = (decor != null) ? (decor.toString().indexOf("line-through") >= 0) : false;
    setStrikeThrough(s);
    Object vAlign = a.getAttribute(CSS.Attribute.VERTICAL_ALIGN);
    s = (vAlign != null) ? (vAlign.toString().indexOf("sup") >= 0) : false;
    setSuperscript(s);
    s = (vAlign != null) ? (vAlign.toString().indexOf("sub") >= 0) : false;
    setSubscript(s);
    Object whitespace = a.getAttribute(CSS.Attribute.WHITE_SPACE);
    nowrap = whitespace != null && whitespace.toString().equals("nowrap");
    HTMLDocument doc = (HTMLDocument) getDocument();
    Color bg = doc.getBackground(a);
    if (bg != null) {
      setBackground(bg);
    }
  }

  protected StyleSheet getStyleSheet() {
    HTMLDocument doc = (HTMLDocument) getDocument();
    return doc.getStyleSheet();
  }
}
