/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */
package javax.swing.colorchooser;

import java.awt.Color;

abstract class ChannelModel {
  static final ChannelModel kHsv = new Hsv();
  static final ChannelModel kHsl = new Hsl();
  static final ChannelModel kRgb = new Rgb();
  static final ChannelModel kCmyk = new Cmyk();

  final String key;
  final String[] rowKeys;
  final int[] max;

  ChannelModel(String key, String[] rowKeys, int[] max) {
    this.key = key;
    this.rowKeys = rowKeys;
    this.max = max;
  }

  abstract int[] fromColor(Color c);

  abstract Color toColor(int[] v);

  boolean hasHex() {
    return false;
  }

  static int transparencyOf(int alpha) {
    return (int) ((255 - alpha) * 100 / 255f);
  }

  static int alphaOf(int transparency) {
    return 255 - (int) Math.round(transparency * 255 / 100.0);
  }

  private static Color rgba(float r, float g, float b, int alpha) {
    return new Color(clamp(Math.round(r * 255)), clamp(Math.round(g * 255)), clamp(Math.round(b * 255)), alpha);
  }

  private static int clamp(int v) {
    return Math.max(0, Math.min(255, v));
  }

  private static final class Hsv extends ChannelModel {
    Hsv() {
      super("hsv", new String[] {"HueText", "SaturationText", "ValueText", "TransparencyText"},
          new int[] {360, 100, 100, 100});
    }

    int[] fromColor(Color c) {
      float[] f = Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), null);
      return new int[] {(int) (f[0] * 360), (int) (f[1] * 100), (int) (f[2] * 100), transparencyOf(c.getAlpha())};
    }

    Color toColor(int[] v) {
      int rgb = Color.HSBtoRGB(v[0] / 360f, v[1] / 100f, v[2] / 100f);
      return new Color((rgb & 0xFFFFFF) | (alphaOf(v[3]) << 24), true);
    }
  }

  private static final class Hsl extends ChannelModel {
    Hsl() {
      super("hsl", new String[] {"HueText", "SaturationText", "LightnessText", "TransparencyText"},
          new int[] {360, 100, 100, 100});
    }

    int[] fromColor(Color c) {
      float r = c.getRed() / 255f;
      float g = c.getGreen() / 255f;
      float b = c.getBlue() / 255f;
      float mx = Math.max(r, Math.max(g, b));
      float mn = Math.min(r, Math.min(g, b));
      float l = (mx + mn) / 2;
      float d = mx - mn;
      float h = 0;
      float s = 0;
      if (d != 0) {
        s = d / (1 - Math.abs(2 * l - 1));
        if (mx == r) {
          h = ((g - b) / d) % 6;
        } else if (mx == g) {
          h = (b - r) / d + 2;
        } else {
          h = (r - g) / d + 4;
        }
        h *= 60;
        if (h < 0) {
          h += 360;
        }
      }
      return new int[] {(int) h, (int) (s * 100), (int) (l * 100), transparencyOf(c.getAlpha())};
    }

    Color toColor(int[] v) {
      float h = v[0] / 60f;
      float s = v[1] / 100f;
      float l = v[2] / 100f;
      float c = (1 - Math.abs(2 * l - 1)) * s;
      float x = c * (1 - Math.abs(h % 2 - 1));
      float m = l - c / 2;
      float r;
      float g;
      float b;
      int sector = (int) h;
      if (sector == 0) {
        r = c;
        g = x;
        b = 0;
      } else if (sector == 1) {
        r = x;
        g = c;
        b = 0;
      } else if (sector == 2) {
        r = 0;
        g = c;
        b = x;
      } else if (sector == 3) {
        r = 0;
        g = x;
        b = c;
      } else if (sector == 4) {
        r = x;
        g = 0;
        b = c;
      } else {
        r = c;
        g = 0;
        b = x;
      }
      return rgba(r + m, g + m, b + m, alphaOf(v[3]));
    }
  }

  private static final class Rgb extends ChannelModel {
    Rgb() {
      super("rgb", new String[] {"RedText", "GreenText", "BlueText", "AlphaText"}, new int[] {255, 255, 255, 255});
    }

    int[] fromColor(Color c) {
      return new int[] {c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha()};
    }

    Color toColor(int[] v) {
      return new Color(v[0], v[1], v[2], v[3]);
    }

    boolean hasHex() {
      return true;
    }
  }

  private static final class Cmyk extends ChannelModel {
    Cmyk() {
      super("cmyk", new String[] {"CyanText", "MagentaText", "YellowText", "BlackText", "AlphaText"},
          new int[] {255, 255, 255, 255, 255});
    }

    int[] fromColor(Color c) {
      float r = c.getRed() / 255f;
      float g = c.getGreen() / 255f;
      float b = c.getBlue() / 255f;
      float k = 1 - Math.max(r, Math.max(g, b));
      float d = 1 - k;
      float cy = d == 0 ? 0 : (1 - r - k) / d;
      float ma = d == 0 ? 0 : (1 - g - k) / d;
      float ye = d == 0 ? 0 : (1 - b - k) / d;
      return new int[] {(int) (cy * 255), (int) (ma * 255), (int) (ye * 255), (int) (k * 255), c.getAlpha()};
    }

    Color toColor(int[] v) {
      float k = v[3] / 255f;
      return rgba((1 - v[0] / 255f) * (1 - k), (1 - v[1] / 255f) * (1 - k), (1 - v[2] / 255f) * (1 - k), v[4]);
    }
  }
}
