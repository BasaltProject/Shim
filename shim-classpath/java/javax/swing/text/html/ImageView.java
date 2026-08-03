/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */
package javax.swing.text.html;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Shape;
import java.net.URL;
import javax.swing.Icon;
import javax.swing.event.DocumentEvent;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.Position;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;

public class ImageView extends View {
  private AttributeSet attr;
  private Image image;
  private URL imageURL;
  private int width;
  private int height;
  private boolean haveWidth;
  private boolean haveHeight;
  private boolean synch;
  private float vAlign;
  private int border;
  private boolean loaded;

  public ImageView(Element elem) {
    super(elem);
  }

  public String getAltText() { return (String) getElement().getAttributes().getAttribute(HTML.Attribute.ALT); }

  public URL getImageURL() {
    String src = (String) getElement().getAttributes().getAttribute(HTML.Attribute.SRC);
    if (src == null) {
      return null;
    }
    URL reference = ((HTMLDocument) getDocument()).getBase();
    try {
      return reference == null ? new URL(src) : new URL(reference, src);
    } catch (java.net.MalformedURLException e) {
      return null;
    }
  }

  public Icon getNoImageIcon() { return null; }

  public Icon getLoadingImageIcon() { return null; }

  public Image getImage() {
    if (!loaded) {
      loaded = true;
      imageURL = getImageURL();
      if (imageURL != null) {
        try {
          image = java.awt.Toolkit.getDefaultToolkit().createImage(imageURL);
        } catch (RuntimeException e) {
          image = null;
        }
      }
    }
    return image;
  }

  public void setLoadsSynchronously(boolean newValue) { synch = newValue; }

  public boolean getLoadsSynchronously() { return synch; }

  protected StyleSheet getStyleSheet() { return ((HTMLDocument) getDocument()).getStyleSheet(); }

  public AttributeSet getAttributes() {
    if (attr == null) {
      attr = getStyleSheet().getViewAttributes(this);
    }
    return attr;
  }

  public String getToolTipText(float x, float y, Shape allocation) { return getAltText(); }

  protected void setPropertiesFromAttributes() {
    AttributeSet a = getElement().getAttributes();
    haveWidth = false;
    haveHeight = false;
    width = intAttr(a, HTML.Attribute.WIDTH);
    height = intAttr(a, HTML.Attribute.HEIGHT);
    haveWidth = width >= 0;
    haveHeight = height >= 0;
    border = Math.max(0, intAttr(a, HTML.Attribute.BORDER));
  }

  private static int intAttr(AttributeSet a, HTML.Attribute key) {
    Object v = a.getAttribute(key);
    if (v == null) {
      return -1;
    }
    try {
      return Integer.parseInt(v.toString().trim());
    } catch (NumberFormatException e) {
      return -1;
    }
  }

  public void setParent(View parent) {
    super.setParent(parent);
    attr = null;
    if (parent != null) {
      setPropertiesFromAttributes();
    }
  }

  public void changedUpdate(DocumentEvent e, Shape a, ViewFactory f) {
    super.changedUpdate(e, a, f);
    attr = null;
    setPropertiesFromAttributes();
    preferenceChanged(null, true, true);
  }

  private int imageSpan(boolean horizontal) {
    Image img = getImage();
    if (img != null) {
      int s = horizontal ? img.getWidth(null) : img.getHeight(null);
      if (s > 0) {
        return s;
      }
    }
    return 38;
  }

  public float getPreferredSpan(int axis) {
    int extra = 2 * border;
    boolean have = getImage() != null && getImage().getWidth(null) > 0;
    switch (axis) {
      case View.X_AXIS:
        return (have && haveWidth ? width : imageSpan(true)) + extra;
      case View.Y_AXIS:
        return (have && haveHeight ? height : imageSpan(false)) + extra;
      default:
        throw new IllegalArgumentException("Invalid axis: " + axis);
    }
  }

  public float getAlignment(int axis) {
    switch (axis) {
      case View.Y_AXIS:
        Object va = getAttributes().getAttribute(CSS.Attribute.VERTICAL_ALIGN);
        if (va != null) {
          String s = va.toString();
          if (s.equals("middle")) {
            return 0.5f;
          } else if (s.equals("top")) {
            return 0f;
          }
        }
        return 1;
      default:
        return super.getAlignment(axis);
    }
  }

  public void paint(Graphics g, Shape a) {
    Rectangle rect = (a instanceof Rectangle) ? (Rectangle) a : a.getBounds();
    Image img = getImage();
    int x = rect.x + border;
    int y = rect.y + border;
    int w = rect.width - 2 * border;
    int h = rect.height - 2 * border;
    if (img != null && img.getWidth(null) > 0) {
      g.drawImage(img, x, y, w, h, null);
    } else {
      g.setColor(Color.lightGray);
      g.drawRect(x, y, Math.max(0, w - 1), Math.max(0, h - 1));
      String alt = getAltText();
      if (alt != null) {
        g.drawString(alt, x + 2, y + g.getFontMetrics().getAscent() + 2);
      }
    }
    if (border > 0) {
      Color c = g.getColor();
      g.setColor(getContainer() == null ? Color.black : getContainer().getForeground());
      for (int i = 0; i < border; i++) {
        g.drawRect(rect.x + i, rect.y + i, rect.width - 2 * i - 1, rect.height - 2 * i - 1);
      }
      g.setColor(c);
    }
  }

  public Shape modelToView(int pos, Shape a, Position.Bias b) throws BadLocationException {
    int p0 = getStartOffset();
    int p1 = getEndOffset();
    if ((pos >= p0) && (pos <= p1)) {
      Rectangle r = a.getBounds();
      if (pos == p1) {
        r.x += r.width;
      }
      r.width = 0;
      return r;
    }
    return null;
  }

  public int viewToModel(float x, float y, Shape a, Position.Bias[] bias) {
    Rectangle alloc = (Rectangle) a;
    if (x < alloc.x + alloc.width) {
      bias[0] = Position.Bias.Forward;
      return getStartOffset();
    }
    bias[0] = Position.Bias.Backward;
    return getEndOffset();
  }

  public void setSize(float width, float height) { }
}
