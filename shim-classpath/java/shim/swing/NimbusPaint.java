/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */
package shim.swing;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JComponent;
import javax.swing.Painter;
import javax.swing.plaf.synth.SynthConstants;

public final class NimbusPaint {
  private NimbusPaint() { }

  private static final Map<String, Painter<JComponent>> kPainters = new HashMap<String, Painter<JComponent>>();

  static {
    for (String row : NimbusButtonData.kRows) {
      int bar = row.indexOf('|');
      kPainters.put(row.substring(0, bar), new Shaped(row.substring(bar + 1), 2, 9f, 7f));
    }
  }

  public static Painter<JComponent> get(String key) {
    return kPainters.get(key);
  }

  public static Painter<JComponent> get(String prefix, int state) {
    boolean disabled = (state & SynthConstants.DISABLED) != 0;
    boolean pressed = (state & SynthConstants.PRESSED) != 0;
    boolean over = (state & SynthConstants.MOUSE_OVER) != 0;
    boolean selected = (state & SynthConstants.SELECTED) != 0;
    boolean def = (state & SynthConstants.DEFAULT) != 0;
    if ("Button".equals(prefix) || "ToggleButton".equals(prefix)) {
      String k;
      if (selected) {
        k = disabled ? "seldis" : pressed ? "selpress" : over ? "selover" : "selected";
      } else if (disabled) {
        k = "dis";
      } else {
        String b = def ? "def" : "";
        k = pressed ? b + "pressed" : over ? b + "over" : def ? "def" : "enabled";
      }
      return kPainters.get("Button." + k);
    }
    if ("TextField".equals(prefix) || "FormattedTextField".equals(prefix) || "PasswordField".equals(prefix)
        || "TextArea".equals(prefix) || "TextPane".equals(prefix) || "EditorPane".equals(prefix)) {
      return disabled ? kFieldDisabled : kField;
    }
    if ("ScrollPane".equals(prefix)) {
      return kScrollPane;
    }
    return null;
  }

  private static final Painter<JComponent> kField = new Field(new int[] {0x8d8e8f, 0x8d8e8f, 0x979899, 0xcbcbcc,
      0xa6a6a7, 0xe5e5e6}, 0xb8b8b9, 0xb8b8b9, 0xffffff, 0xc0c0c1, false);
  private static final Painter<JComponent> kFieldDisabled = new Field(new int[] {0xc7cacf, 0xc7cacf, 0xcbcdd2,
      0xd4d6db, 0xcfd1d6, 0xd9dce0}, 0xd0d2d7, 0xd2d4d9, 0xdee1e5, 0xdee1e5, true);
  private static final Painter<JComponent> kScrollPane = new Field(new int[] {0x9297a1, 0x9297a1, 0x9297a1, 0xd6d9df,
      0x9297a1, 0xd6d9df}, 0x9297a1, 0x9297a1, 0xd6d9df, 0x9297a1, true);

  private static final class Field implements Painter<JComponent> {
    private final int[] top;
    private final int left;
    private final int right;
    private final int fill;
    private final int bottom;
    private final boolean fixedFill;

    Field(int[] top, int left, int right, int fill, int bottom, boolean fixedFill) {
      this.top = top;
      this.left = left;
      this.right = right;
      this.fill = fill;
      this.bottom = bottom;
      this.fixedFill = fixedFill;
    }

    public void paint(Graphics2D g, JComponent c, int w, int h) {
      int x1 = w - 3;
      int y1 = h - 3;
      if (x1 < 3 || y1 < 5) {
        return;
      }
      Color fillColor = new Color(fill);
      java.awt.Color bg = c == null ? null : c.getBackground();
      if (!fixedFill && bg != null && !(bg instanceof javax.swing.plaf.UIResource)) {
        fillColor = bg;
      }
      g.setColor(fillColor);
      g.fillRect(2, 2, w - 4, h - 4);
      for (int i = 0; i < 3; i++) {
        g.setColor(new Color(top[i * 2 + 1]));
        g.fillRect(3, 2 + i, w - 6, 1);
        g.setColor(new Color(top[i * 2]));
        g.fillRect(2, 2 + i, 1, 1);
        g.fillRect(x1, 2 + i, 1, 1);
      }
      g.setColor(new Color(left));
      g.fillRect(2, 5, 1, h - 7);
      g.setColor(new Color(right));
      g.fillRect(x1, 5, 1, h - 7);
      g.setColor(new Color(bottom));
      g.fillRect(3, h - 3, w - 6, 1);
    }
  }

  private static int[] parse(String hex) {
    int[] out = new int[hex.length() / 6];
    for (int i = 0; i < out.length; i++) {
      out[i] = 0xFF000000 | Integer.parseInt(hex.substring(i * 6, i * 6 + 6), 16);
    }
    return out;
  }

  private static LinearGradientPaint vertical(float y, float h, int[] ref, int top, int bottom) {
    int n = Math.max(2, Math.round(h));
    float[] f = new float[n + 1];
    Color[] c = new Color[n + 1];
    int m = ref.length;
    for (int i = 0; i < n; i++) {
      f[i] = i / (float) n;
      c[i] = new Color(sample(ref, i, n, top, bottom, m));
    }
    f[n] = 1f;
    c[n] = c[n - 1];
    return new LinearGradientPaint(0, y, 0, y + n, f, c);
  }

  private static int sample(int[] ref, int i, int n, int top, int bottom, int m) {
    if (n == m) {
      return ref[i];
    }
    if (i < top) {
      return ref[Math.min(i, m - 1)];
    }
    if (i >= n - bottom) {
      return ref[Math.max(0, m - (n - i))];
    }
    int span = m - top - bottom;
    int nspan = n - top - bottom;
    if (span <= 1 || nspan <= 1) {
      return ref[Math.min(m - 1, top)];
    }
    double p = top + (i - top) * (double) (span - 1) / (nspan - 1);
    int lo = (int) p;
    int hi = Math.min(lo + 1, m - 1);
    return mix(ref[lo], ref[hi], p - lo);
  }

  private static int mix(int a, int b, double u) {
    int r = (int) Math.round(((a >> 16) & 255) + (((b >> 16) & 255) - ((a >> 16) & 255)) * u);
    int g = (int) Math.round(((a >> 8) & 255) + (((b >> 8) & 255) - ((a >> 8) & 255)) * u);
    int bl = (int) Math.round((a & 255) + ((b & 255) - (a & 255)) * u);
    return 0xFF000000 | (r << 16) | (g << 8) | bl;
  }

  private static final class Shaped implements Painter<JComponent> {
    private final int inset;
    private final float arcOuter;
    private final float arcInner;
    private final Color shadow;
    private final int[] border;
    private final int[] fill;

    Shaped(String data, int inset, float arcOuter, float arcInner) {
      this.inset = inset;
      this.arcOuter = arcOuter;
      this.arcInner = arcInner;
      String[] p = data.split(",");
      shadow = new Color((int) Long.parseLong(p[0], 16), true);
      int[] left = parse(p[2]);
      int refH = left.length + 10;
      border = new int[refH];
      int top = 0xFF000000 | Integer.parseInt(p[1], 16);
      for (int i = 0; i < 5; i++) {
        border[i] = mix(top, left[0], i / 5.0);
      }
      System.arraycopy(left, 0, border, 5, left.length);
      int bottom = 0xFF000000 | Integer.parseInt(p[3], 16);
      for (int i = 0; i < 5; i++) {
        border[refH - 5 + i] = mix(left[left.length - 1], bottom, (i + 1) / 5.0);
      }
      fill = parse(p[4]);
    }

    public void paint(Graphics2D g, JComponent c, int w, int h) {
      Graphics2D g2 = (Graphics2D) g.create();
      try {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        float x = inset;
        float y = inset;
        float ow = w - 2 * inset;
        float oh = h - 2 * inset;
        if (ow <= 2 || oh <= 2) {
          return;
        }
        g2.setColor(shadow);
        g2.fill(new RoundRectangle2D.Float(x, y + 1, ow, oh, arcOuter, arcOuter));
        g2.setPaint(vertical(y, oh, border, 2, 2));
        g2.fill(new RoundRectangle2D.Float(x, y, ow, oh, arcOuter, arcOuter));
        g2.setPaint(vertical(y + 1, oh - 2, fill, 6, 5));
        g2.fill(new RoundRectangle2D.Float(x + 1, y + 1, ow - 2, oh - 2, arcInner, arcInner));
      } finally {
        g2.dispose();
      }
    }
  }
}
