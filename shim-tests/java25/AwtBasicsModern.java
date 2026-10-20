/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 * Minimum release: 21
 */

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Point;
import java.awt.geom.Dimension2D;
import java.awt.geom.Point2D;
import java.util.Arrays;
import java.util.concurrent.Callable;

public class AwtBasicsModern {
  static void t(String name, Callable<Object> c) {
    try {
      System.out.println(name + " = " + c.call());
    } catch (Throwable e) {
      System.out.println(name + " ! " + e.getClass().getName() + ": " + e.getMessage());
    }
  }

  public static void main(String[] args) {
    t("point-ctor", () -> new Point() + " " + new Point(3, 4) + " " + new Point(new Point(5, 6)));
    t("point-set", () -> {
      Point p = new Point();
      p.setLocation(2.5, -2.5);
      Point q = new Point();
      q.setLocation(1.4999, 1.5);
      Point r = new Point(1, 1);
      r.translate(2, -3);
      r.move(7, 8);
      return p + " " + q + " " + r + " " + r.getLocation().equals(r) + " " + (r.getLocation() != r);
    });
    t("point-eq", () -> {
      Point a = new Point(1, 2);
      Point2D.Double b = new Point2D.Double(1, 2);
      Point2D.Float c = new Point2D.Float(1, 2);
      return a.equals(b) + " " + b.equals(a) + " " + c.equals(a) + " " + a.hashCode() + " " + b.hashCode() + " "
          + c.hashCode() + " " + a.equals(null) + " " + a.equals("x");
    });
    t("point-distance", () -> {
      Point a = new Point(0, 0);
      return a.distance(3, 4) + " " + a.distanceSq(3, 4) + " " + a.distance(new Point(6, 8)) + " "
          + Point2D.distance(1, 1, 4, 5) + " " + Point2D.distanceSq(1, 1, 4, 5);
    });
    t("point2d-tostring", () -> new Point2D.Double(1.5, -2) + " " + new Point2D.Float(0.1f, 3) + " "
        + new Point2D.Double());
    t("point2d-set", () -> {
      Point2D.Float f = new Point2D.Float();
      f.setLocation(0.1, 0.2);
      Point2D.Double d = new Point2D.Double();
      d.setLocation(f);
      return f + " " + d + " " + f.getX() + " " + d.getY();
    });
    t("point2d-clone", () -> {
      Point2D.Double d = new Point2D.Double(1, 2);
      Object c = d.clone();
      return c + " " + (c != d) + " " + c.equals(d) + " " + c.getClass().getName();
    });
    t("point2d-hash-negzero", () -> new Point2D.Double(-0.0, 0.0).hashCode() + " "
        + new Point2D.Double(-0.0, 0.0).equals(new Point2D.Double(0.0, 0.0)));
    t("point-getters", () -> new Point(3, 4).getX() + " " + new Point(3, 4).getY());

    t("dim-ctor", () -> new Dimension() + " " + new Dimension(3, 4) + " " + new Dimension(new Dimension(5, 6)));
    t("dim-set", () -> {
      Dimension d = new Dimension();
      d.setSize(2.1, 2.9);
      Dimension e = new Dimension();
      e.setSize(-2.1, -0.5);
      Dimension f = new Dimension(1, 1);
      f.setSize(new Dimension(8, 9));
      Dimension g = new Dimension();
      g.setSize(7, 6);
      return d + " " + e + " " + f + " " + g + " " + g.getSize().equals(g) + " " + d.getWidth() + " " + d.getHeight();
    });
    t("dim-hash", () -> new Dimension(3, 4).hashCode() + " " + new Dimension(0, 0).hashCode() + " "
        + new Dimension(-5, 9).hashCode() + " " + new Dimension(3, 4).equals(new Dimension(3, 4)) + " "
        + new Dimension(3, 4).equals(new Dimension(4, 3)) + " " + new Dimension(3, 4).equals(null));
    t("dim2d", () -> {
      Dimension2D d = new Dimension(1, 2);
      d.setSize(new Dimension(5, 7));
      return d + " " + d.clone();
    });

    t("insets", () -> {
      Insets i = new Insets(1, 2, 3, 4);
      Insets j = (Insets) i.clone();
      i.set(5, 6, 7, 8);
      return i + " " + j + " " + i.hashCode() + " " + j.hashCode() + " " + i.equals(j) + " " + j.equals(new Insets(1, 2,
          3, 4)) + " " + (j != null);
    });

    t("color-consts", () -> Color.red + " " + Color.RED.equals(Color.red) + " " + Color.orange + " " + Color.pink + " "
        + Color.gray + " " + Color.LIGHT_GRAY + " " + Color.DARK_GRAY + " " + Color.cyan);
    t("color-int", () -> {
      Color c = new Color(0x123456);
      return c.getRed() + " " + c.getGreen() + " " + c.getBlue() + " " + c.getAlpha() + " " + c.getRGB() + " "
          + c.hashCode() + " " + c;
    });
    t("color-rgba", () -> {
      Color c = new Color(0x80123456, true);
      Color d = new Color(0x80123456, false);
      Color e = new Color(10, 20, 30, 40);
      return c.getAlpha() + " " + d.getAlpha() + " " + e.getAlpha() + " " + c.getRGB() + " " + d.getRGB() + " "
          + e.getRGB() + " " + c.equals(d) + " " + c.getTransparency() + " " + d.getTransparency() + " "
          + new Color(1, 2, 3, 0).getTransparency();
    });
    t("color-range-int", () -> new Color(256, 0, 0));
    t("color-range-int-all", () -> new Color(-1, 300, -5, 1000));
    t("color-range-alpha", () -> new Color(1, 2, 3, 256));
    t("color-float", () -> {
      Color c = new Color(0.5f, 0.25f, 1.0f);
      Color d = new Color(0.5f, 0.25f, 1.0f, 0.5f);
      return c + " " + d.getAlpha() + " " + Arrays.toString(c.getRGBComponents(null)) + " "
          + Arrays.toString(d.getRGBColorComponents(null)) + " " + Arrays.toString(new Color(10, 20, 30)
          .getRGBComponents(new float[4]));
    });
    t("color-float-range", () -> new Color(1.5f, -0.1f, 0f));
    t("color-float-range-alpha", () -> new Color(0f, 0f, 0f, 2f));
    t("color-brighter-darker", () -> {
      StringBuilder sb = new StringBuilder();
      for (Color c : new Color[] {Color.black, new Color(1, 2, 3), new Color(100, 150, 200, 77), Color.white,
          new Color(0, 0, 5)}) {
        sb.append(c.brighter().getRGB()).append('/').append(c.darker().getRGB()).append(' ');
      }
      return sb;
    });
    t("color-decode", () -> Color.decode("#ff8000") + " " + Color.decode("0x10") + " " + Color.decode("255") + " "
        + Color.decode("-1"));
    t("color-decode-bad", () -> Color.decode("zz"));
    t("color-getcolor", () -> {
      System.setProperty("shim.test.color", "0xff00ff");
      return Color.getColor("shim.test.color") + " " + Color.getColor("shim.nope") + " "
          + Color.getColor("shim.nope", Color.blue) + " " + Color.getColor("shim.nope", 0x102030) + " "
          + Color.getColor("shim.test.color", 0);
    });
    t("color-hsb", () -> {
      StringBuilder sb = new StringBuilder();
      for (float h = 0; h <= 1.0f; h += 0.125f) {
        sb.append(Integer.toHexString(Color.HSBtoRGB(h, 0.75f, 0.5f))).append(' ');
      }
      sb.append(Integer.toHexString(Color.HSBtoRGB(-0.25f, 1f, 1f))).append(' ');
      sb.append(Integer.toHexString(Color.HSBtoRGB(3.5f, 0.3f, 0.9f))).append(' ');
      sb.append(Integer.toHexString(Color.HSBtoRGB(0.2f, 0f, 0.4f)));
      return sb;
    });
    t("color-rgbtohsb", () -> {
      StringBuilder sb = new StringBuilder();
      int[][] in = {{255, 0, 0}, {0, 255, 0}, {0, 0, 255}, {10, 20, 30}, {0, 0, 0}, {255, 255, 255}, {200, 100, 50},
          {50, 100, 200}, {128, 128, 0}};
      for (int[] c : in) {
        sb.append(Arrays.toString(Color.RGBtoHSB(c[0], c[1], c[2], null))).append(' ');
      }
      float[] reuse = new float[3];
      Color.RGBtoHSB(1, 2, 3, reuse);
      sb.append(Arrays.toString(reuse));
      return sb;
    });
    t("color-hsbcolor", () -> Color.getHSBColor(0.6f, 0.7f, 0.8f).getRGB());
    t("color-eq", () -> new Color(1, 2, 3).equals(new Color(1, 2, 3)) + " " + new Color(1, 2, 3).equals(new Color(1, 2,
        3, 254)) + " " + new Color(1, 2, 3).equals("x") + " " + new Color(1, 2, 3).equals(null));
  }
}
