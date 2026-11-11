/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing.border;

import java.awt.*;
import javax.swing.UIManager;

public class TitledBorder extends AbstractBorder {
  protected String title;
  protected Border border;
  protected int titlePosition;
  protected int titleJustification;
  protected Font titleFont;
  protected Color titleColor;

  public static final int DEFAULT_POSITION = 0;
  public static final int ABOVE_TOP = 1;
  public static final int TOP = 2;
  public static final int BELOW_TOP = 3;
  public static final int ABOVE_BOTTOM = 4;
  public static final int BOTTOM = 5;
  public static final int BELOW_BOTTOM = 6;
  public static final int DEFAULT_JUSTIFICATION = 0;
  public static final int LEFT = 1;
  public static final int CENTER = 2;
  public static final int RIGHT = 3;
  public static final int LEADING = 4;
  public static final int TRAILING = 5;
  protected static final int EDGE_SPACING = 2;
  protected static final int TEXT_SPACING = 2;
  protected static final int TEXT_INSET_H = 5;

  public TitledBorder(String title) { this(null, title, LEADING, DEFAULT_POSITION, null, null); }

  public TitledBorder(Border border) { this(border, "", LEADING, DEFAULT_POSITION, null, null); }

  public TitledBorder(Border border, String title) { this(border, title, LEADING, DEFAULT_POSITION, null, null); }

  public TitledBorder(Border border, String title, int titleJustification, int titlePosition) {
    this(border, title, titleJustification, titlePosition, null, null);
  }

  public TitledBorder(Border border, String title, int titleJustification, int titlePosition, Font titleFont) {
    this(border, title, titleJustification, titlePosition, titleFont, null);
  }

  @java.beans.ConstructorProperties({"border", "title", "titleJustification", "titlePosition", "titleFont", "titleColor"})
  public TitledBorder(Border border, String title, int titleJustification, int titlePosition, Font titleFont,
      Color titleColor) {
    this.title = title;
    this.border = border;
    this.titleFont = titleFont;
    this.titleColor = titleColor;
    setTitleJustification(titleJustification);
    setTitlePosition(titlePosition);
  }

  private int textHeight(Component c) {
    Font f = getFont(c);
    return f == null || c == null ? 0 : c.getFontMetrics(f).getHeight();
  }

  private int textWidth(Component c) {
    Font f = getFont(c);
    return f == null || c == null || title == null ? 0 : c.getFontMetrics(f).stringWidth(title);
  }

  public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
    Border b = getBorder();
    String t = getTitle();
    if (t == null || t.isEmpty() || c == null) {
      if (b != null) {
        b.paintBorder(c, g, x, y, width, height);
      }
      return;
    }
    int edge = b instanceof TitledBorder ? 0 : EDGE_SPACING;
    Font f = getFont(c);
    FontMetrics fm = c.getFontMetrics(f);
    int labelH = fm.getHeight();
    int labelW = fm.stringWidth(t);
    Insets own = new Insets(0, 0, 0, 0);
    if (b != null) {
      if (b instanceof AbstractBorder) {
        ((AbstractBorder) b).getBorderInsets(c, own);
      } else {
        Insets i = b.getBorderInsets(c);
        own.set(i.top, i.left, i.bottom, i.right);
      }
    }
    int borderX = x + edge;
    int borderY = y + edge;
    int borderW = width - edge - edge;
    int borderH = height - edge - edge;
    int labelY = y;
    int pos = titlePosition == DEFAULT_POSITION ? TOP : titlePosition;
    if (pos == ABOVE_TOP) {
      own.left = 0;
      own.right = 0;
      borderY += labelH - edge;
      borderH -= labelH - edge;
    } else if (pos == TOP) {
      own.top = edge + own.top / 2 - labelH / 2;
      if (own.top < edge) {
        borderY -= own.top;
        borderH += own.top;
      } else {
        labelY += own.top;
      }
    } else if (pos == BELOW_TOP) {
      labelY += own.top + edge;
    } else if (pos == ABOVE_BOTTOM) {
      labelY += height - labelH - own.bottom - edge;
    } else if (pos == BOTTOM) {
      labelY += height - labelH;
      own.bottom = edge + (own.bottom - labelH) / 2;
      if (own.bottom < edge) {
        borderH += own.bottom;
      } else {
        labelY -= own.bottom;
      }
    } else {
      own.left = 0;
      own.right = 0;
      labelY += height - labelH;
      borderH -= labelH - edge;
    }
    own.left += edge + TEXT_INSET_H;
    own.right += edge + TEXT_INSET_H;
    int available = width - own.left - own.right;
    if (available < labelW) {
      labelW = available;
    }
    int j = titleJustification;
    boolean ltr = c.getComponentOrientation().isLeftToRight();
    if (j == LEADING || j == DEFAULT_JUSTIFICATION) {
      j = ltr ? LEFT : RIGHT;
    } else if (j == TRAILING) {
      j = ltr ? RIGHT : LEFT;
    }
    int labelX = x;
    if (j == LEFT) {
      labelX += own.left;
    } else if (j == RIGHT) {
      labelX += width - own.right - labelW;
    } else {
      labelX += (width - labelW) / 2;
    }
    if (b != null) {
      if (pos != TOP && pos != BOTTOM) {
        b.paintBorder(c, g, borderX, borderY, borderW, borderH);
      } else {
        Graphics g2 = g.create();
        java.awt.geom.Path2D path = new java.awt.geom.Path2D.Float();
        path.append(new Rectangle(borderX, borderY, borderW, labelY - borderY), false);
        path.append(new Rectangle(borderX, labelY, labelX - borderX - TEXT_SPACING, labelH), false);
        path.append(new Rectangle(labelX + labelW + TEXT_SPACING, labelY,
            borderX - labelX + borderW - labelW - TEXT_SPACING, labelH), false);
        path.append(new Rectangle(borderX, labelY + labelH, borderW, borderY - labelY + borderH - labelH), false);
        ((Graphics2D) g2).clip(path);
        b.paintBorder(c, g2, borderX, borderY, borderW, borderH);
        g2.dispose();
      }
    }
    Color old = g.getColor();
    Font oldFont = g.getFont();
    Shape oldClip = g.getClip();
    g.clipRect(labelX, labelY, labelW, labelH);
    g.setColor(getTitleColor());
    g.setFont(f);
    g.drawString(t, labelX, labelY + fm.getAscent());
    g.setClip(oldClip);
    g.setFont(oldFont);
    g.setColor(old);
  }

  public Insets getBorderInsets(Component c, Insets insets) {
    Border b = getBorder();
    if (b != null) {
      if (b instanceof AbstractBorder) {
        ((AbstractBorder) b).getBorderInsets(c, insets);
      } else {
        Insets i = b.getBorderInsets(c);
        insets.set(i.top, i.left, i.bottom, i.right);
      }
    } else {
      insets.left = insets.top = insets.right = insets.bottom = 0;
    }
    if (title == null || title.isEmpty()) {
      return insets;
    }
    int space = EDGE_SPACING + TEXT_SPACING;
    insets.left += space;
    insets.right += space;
    insets.top += space;
    insets.bottom += space;
    int th = textHeight(c);
    int pos = titlePosition == DEFAULT_POSITION ? TOP : titlePosition;
    if (pos == ABOVE_TOP) {
      insets.top += th - 2;
    } else if (pos == TOP) {
      insets.top = Math.max(insets.top, th + EDGE_SPACING);
    } else if (pos == BELOW_TOP) {
      insets.top += th;
    } else if (pos == ABOVE_BOTTOM) {
      insets.bottom += th;
    } else if (pos == BOTTOM) {
      insets.bottom = Math.max(insets.bottom, th + EDGE_SPACING);
    } else {
      insets.bottom += th - 2;
    }
    return insets;
  }

  public boolean isBorderOpaque() { return false; }

  public String getTitle() { return title; }

  public Border getBorder() { return border != null ? border : UIManager.getBorder("TitledBorder.border"); }

  public int getTitlePosition() { return titlePosition; }

  public int getTitleJustification() { return titleJustification; }

  public Font getTitleFont() { return titleFont == null ? UIManager.getFont("TitledBorder.font") : titleFont; }

  public Color getTitleColor() { return titleColor == null ? UIManager.getColor("TitledBorder.titleColor") : titleColor; }

  public void setTitle(String title) { this.title = title; }

  public void setBorder(Border border) { this.border = border; }

  public void setTitlePosition(int titlePosition) {
    if (titlePosition < DEFAULT_POSITION || titlePosition > BELOW_BOTTOM) {
      throw new IllegalArgumentException(titlePosition + " is not a valid title position.");
    }
    this.titlePosition = titlePosition;
  }

  public void setTitleJustification(int titleJustification) {
    if (titleJustification < DEFAULT_JUSTIFICATION || titleJustification > TRAILING) {
      throw new IllegalArgumentException(titleJustification + " is not a valid title justification.");
    }
    this.titleJustification = titleJustification;
  }

  public void setTitleFont(Font titleFont) { this.titleFont = titleFont; }

  public void setTitleColor(Color titleColor) { this.titleColor = titleColor; }

  public Dimension getMinimumSize(Component c) {
    Insets insets = getBorderInsets(c);
    Dimension minSize = new Dimension(insets.right + insets.left, insets.top + insets.bottom);
    minSize.width = Math.max(minSize.width, textWidth(c) + 2 * TEXT_INSET_H);
    return minSize;
  }

  public int getBaseline(Component c, int width, int height) {
    super.getBaseline(c, width, height);
    Font f = getFont(c);
    if (f == null || c == null) {
      return -1;
    }
    return c.getFontMetrics(f).getAscent();
  }

  public Component.BaselineResizeBehavior getBaselineResizeBehavior(Component c) {
    super.getBaselineResizeBehavior(c);
    int pos = titlePosition;
    return pos == ABOVE_BOTTOM || pos == BOTTOM || pos == BELOW_BOTTOM ? Component.BaselineResizeBehavior.CONSTANT_DESCENT
        : Component.BaselineResizeBehavior.CONSTANT_ASCENT;
  }

  protected Font getFont(Component c) {
    Font font = getTitleFont();
    if (font != null) {
      return font;
    }
    if (c != null) {
      font = c.getFont();
      if (font != null) {
        return font;
      }
    }
    return new Font(Font.DIALOG, Font.PLAIN, 12);
  }
}
