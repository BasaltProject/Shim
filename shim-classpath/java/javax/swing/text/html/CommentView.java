/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */
package javax.swing.text.html;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Insets;
import javax.swing.JTextArea;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.text.AttributeSet;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.JTextComponent;
import javax.swing.text.StyledDocument;

class CommentView extends HiddenTagView {
  CommentView(Element e) {
    super(e);
  }

  protected Component createComponent() {
    Container host = getContainer();
    if (host != null && !((JTextComponent) host).isEditable()) {
      return null;
    }
    JTextArea ta = new JTextArea(getRepresentedText());
    Document doc = getDocument();
    Font font;
    if (doc instanceof StyledDocument) {
      font = ((StyledDocument) doc).getFont(getAttributes());
      ta.setFont(font);
    } else {
      font = ta.getFont();
    }
    updateYAlign(font);
    ta.setBorder(CBorder);
    ta.getDocument().addDocumentListener(this);
    ta.setFocusable(isVisible());
    return ta;
  }

  void resetBorder() { }

  String getRepresentedText() {
    AttributeSet attrs = getElement().getAttributes();
    Object comment = attrs.getAttribute(HTML.Attribute.COMMENT);
    return comment == null ? "" : comment.toString();
  }

  static final Border CBorder = new CommentBorder();
  static final int commentPadding = 3;
  static final int commentPaddingD = commentPadding * 3;

  static class CommentBorder extends LineBorder {
    CommentBorder() {
      super(Color.black, 1);
    }

    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
      super.paintBorder(c, g, x + commentPadding, y, width - commentPaddingD, height);
    }

    public Insets getBorderInsets(Component c, Insets insets) {
      Insets retI = super.getBorderInsets(c, insets);
      retI.left += commentPadding;
      retI.right += commentPadding;
      return retI;
    }

    public boolean isBorderOpaque() { return false; }
  }
}
