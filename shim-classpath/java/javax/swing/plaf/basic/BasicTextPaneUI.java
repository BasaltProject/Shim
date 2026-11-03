/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */
package javax.swing.plaf.basic;

import java.awt.Color;
import java.awt.Font;
import java.beans.PropertyChangeEvent;
import javax.swing.JComponent;
import javax.swing.JTextPane;
import javax.swing.plaf.ComponentUI;
import javax.swing.text.EditorKit;
import javax.swing.text.JTextComponent;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;

public class BasicTextPaneUI extends BasicEditorPaneUI {
  public BasicTextPaneUI() { }

  public static ComponentUI createUI(JComponent c) { return new BasicTextPaneUI(); }

  protected String getPropertyPrefix() { return "TextPane"; }

  public void installUI(JComponent c) {
    super.installUI(c);
    JTextComponent comp = getComponent();
    updateFont(comp.getFont());
    updateForeground(comp.getForeground());
  }

  public EditorKit getEditorKit(JTextComponent tc) {
    return ((JTextPane) tc).getEditorKit();
  }

  protected void propertyChange(PropertyChangeEvent evt) {
    super.propertyChange(evt);
    String name = evt.getPropertyName();
    if ("font".equals(name)) {
      updateFont((Font) evt.getNewValue());
    } else if ("foreground".equals(name)) {
      updateForeground((Color) evt.getNewValue());
    } else if ("document".equals(name)) {
      JTextComponent comp = getComponent();
      updateFont(comp.getFont());
      updateForeground(comp.getForeground());
    }
  }

  private void updateFont(Font font) {
    Style style = ((StyledDocument) getComponent().getDocument()).getStyle(StyleContext.DEFAULT_STYLE);
    if (font != null && style != null) {
      StyleConstants.setFontFamily(style, font.getName());
      StyleConstants.setFontSize(style, font.getSize());
      StyleConstants.setBold(style, font.isBold());
      StyleConstants.setItalic(style, font.isItalic());
    }
  }

  private void updateForeground(Color color) {
    Style style = ((StyledDocument) getComponent().getDocument()).getStyle(StyleContext.DEFAULT_STYLE);
    if (style != null) {
      if (color == null) {
        style.removeAttribute(StyleConstants.Foreground);
      } else {
        StyleConstants.setForeground(style, color);
      }
    }
  }
}
