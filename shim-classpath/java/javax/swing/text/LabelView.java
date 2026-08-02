/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */
package javax.swing.text;

import java.awt.Color;
import java.awt.Font;

public class LabelView extends GlyphView implements TabableView {
  private Font font;
  private Color fg;
  private Color bg;
  private boolean underline;
  private boolean strike;
  private boolean superscript;
  private boolean subscript;

  public LabelView(Element elem) {
    super(elem);
  }

  final void sync() {
    if (font == null) {
      setPropertiesFromAttributes();
    }
  }

  protected void setUnderline(boolean u) { underline = u; }

  protected void setStrikeThrough(boolean s) { strike = s; }

  protected void setSuperscript(boolean s) { superscript = s; }

  protected void setSubscript(boolean s) { subscript = s; }

  protected void setBackground(Color bg) { this.bg = bg; }

  protected void setPropertiesFromAttributes() {
    AttributeSet attr = getAttributes();
    if (attr != null) {
      Document d = getDocument();
      if (d instanceof StyledDocument) {
        StyledDocument doc = (StyledDocument) d;
        font = doc.getFont(attr);
        fg = doc.getForeground(attr);
        if (attr.isDefined(StyleConstants.Background)) {
          bg = doc.getBackground(attr);
        } else {
          bg = null;
        }
        setUnderline(StyleConstants.isUnderline(attr));
        setStrikeThrough(StyleConstants.isStrikeThrough(attr));
        setSuperscript(StyleConstants.isSuperscript(attr));
        setSubscript(StyleConstants.isSubscript(attr));
      } else {
        throw new Error("LabelView needs StyledDocument");
      }
    }
  }

  public Color getBackground() {
    sync();
    return bg;
  }

  public Color getForeground() {
    sync();
    return fg;
  }

  public Font getFont() {
    sync();
    return font;
  }

  public boolean isUnderline() {
    sync();
    return underline;
  }

  public boolean isStrikeThrough() {
    sync();
    return strike;
  }

  public boolean isSubscript() {
    sync();
    return subscript;
  }

  public boolean isSuperscript() {
    sync();
    return superscript;
  }

  public void changedUpdate(javax.swing.event.DocumentEvent e, java.awt.Shape a, ViewFactory f) {
    font = null;
    super.changedUpdate(e, a, f);
  }
}
