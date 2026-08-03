/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */
package javax.swing.text.html;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Toolkit;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.AbstractBorder;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.JTextComponent;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.text.View;

class HiddenTagView extends EditableView implements DocumentListener {
  private float yAlign = 1;
  private boolean isSettingAttributes;

  HiddenTagView(Element e) {
    super(e);
  }

  protected Component createComponent() {
    Container host = getContainer();
    if (host != null && !((JTextComponent) host).isEditable()) {
      return null;
    }
    JTextField tf = new JTextField(getRepresentedText());
    Document doc = getDocument();
    Font font;
    if (doc instanceof StyledDocument) {
      font = ((StyledDocument) doc).getFont(getAttributes());
      tf.setFont(font);
    } else {
      font = tf.getFont();
    }
    tf.getDocument().addDocumentListener(this);
    updateYAlign(font);
    JPanel panel = new JPanel(new BorderLayout());
    panel.setOpaque(false);
    tf.setForeground(Color.BLACK);
    panel.add(tf);
    panel.setBorder(isEndTag() ? EndBorder : StartBorder);
    panel.setAlignmentY(0.5f);
    return panel;
  }

  public float getAlignment(int axis) { return axis == View.Y_AXIS ? yAlign : 0.5f; }

  public float getMinimumSpan(int axis) {
    return isVisible() && axis == View.X_AXIS ? Math.max(30, super.getPreferredSpan(axis))
        : super.getPreferredSpan(axis);
  }

  public float getPreferredSpan(int axis) {
    return isVisible() && axis == View.X_AXIS ? Math.max(30, super.getPreferredSpan(axis))
        : super.getPreferredSpan(axis);
  }

  public float getMaximumSpan(int axis) {
    return isVisible() && axis == View.X_AXIS ? Math.max(30, super.getMaximumSpan(axis))
        : super.getMaximumSpan(axis);
  }

  public void insertUpdate(DocumentEvent e) { updateModelFromText(); }

  public void removeUpdate(DocumentEvent e) { updateModelFromText(); }

  public void changedUpdate(DocumentEvent e) { updateModelFromText(); }

  void updateYAlign(Font font) {
    Container c = getContainer();
    FontMetrics fm = c != null ? c.getFontMetrics(font) : Toolkit.getDefaultToolkit().getFontMetrics(font);
    float h = fm.getHeight();
    float d = fm.getDescent();
    yAlign = h > 0 ? (h - d) / h : 0;
  }

  void resetBorder() { }

  void _updateModelFromText() { }

  void updateModelFromText() {
    if (!isSettingAttributes) {
      _updateModelFromText();
    }
  }

  boolean isEndTag() {
    return getElement().getAttributes().getAttribute(HTML.Attribute.ENDTAG) != null;
  }

  String getRepresentedText() {
    AttributeSet attrs = getElement().getAttributes();
    Object name = attrs.getAttribute(StyleConstants.NameAttribute);
    return name == null ? "" : name.toString();
  }

  static final Border StartBorder = new StartTagBorder();
  static final Border EndBorder = new EndTagBorder();
  static final int circleR = 3;
  static final int circleD = circleR * 2;
  static final int tagSize = 6;
  static final int padding = 3;
  static final Color color = Color.blue;

  static class StartTagBorder extends AbstractBorder {
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
      g.setColor(color);
      x += padding;
      width -= padding + padding;
      g.drawLine(x, y + circleR, x, y + height - circleR);
      g.drawArc(x, y + height - circleD - 1, circleD, circleD, 180, 90);
      g.drawArc(x, y, circleD, circleD, 90, 90);
      g.drawLine(x + circleR, y, x + width - tagSize, y);
      g.drawLine(x + circleR, y + height - 1, x + width - tagSize, y + height - 1);
      g.drawLine(x + width - tagSize, y, x + width - 1, y + height / 2);
      g.drawLine(x + width - tagSize, y + height - 1, x + width - 1, y + height / 2);
    }

    public Insets getBorderInsets(Component c, Insets insets) {
      insets.set(2, 2 + circleR, 2, 2 + tagSize + padding);
      return insets;
    }

    public boolean isBorderOpaque() { return false; }
  }

  static class EndTagBorder extends AbstractBorder {
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
      g.setColor(color);
      x += padding;
      width -= padding + padding;
      g.drawLine(x + width - 1, y + circleR, x + width - 1, y + height - circleR);
      g.drawArc(x + width - circleD - 1, y + height - circleD - 1, circleD, circleD, 270, 90);
      g.drawArc(x + width - circleD - 1, y, circleD, circleD, 0, 90);
      g.drawLine(x + tagSize, y, x + width - circleR, y);
      g.drawLine(x + tagSize, y + height - 1, x + width - circleR, y + height - 1);
      g.drawLine(x + tagSize, y, x, y + height / 2);
      g.drawLine(x + tagSize, y + height - 1, x, y + height / 2);
    }

    public Insets getBorderInsets(Component c, Insets insets) {
      insets.set(2, 2 + tagSize + padding, 2, 2 + circleR);
      return insets;
    }

    public boolean isBorderOpaque() { return false; }
  }
}
