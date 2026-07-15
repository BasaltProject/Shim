/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.awt;

import java.awt.AlphaComposite;

public final class Blend {
  private Blend() { }

  public static int compose(int rule, int src, int dst, float extra) {
    float sa = ((src >>> 24) & 0xff) / 255f * extra;
    float da = ((dst >>> 24) & 0xff) / 255f;
    float sr = ((src >> 16) & 0xff) / 255f * sa;
    float sg = ((src >> 8) & 0xff) / 255f * sa;
    float sb = (src & 0xff) / 255f * sa;
    float dr = ((dst >> 16) & 0xff) / 255f * da;
    float dg = ((dst >> 8) & 0xff) / 255f * da;
    float db = (dst & 0xff) / 255f * da;
    float fs;
    float fd;
    switch (rule) {
    case AlphaComposite.CLEAR:
      fs = 0;
      fd = 0;
      break;
    case AlphaComposite.SRC:
      fs = 1;
      fd = 0;
      break;
    case AlphaComposite.DST:
      fs = 0;
      fd = 1;
      break;
    case AlphaComposite.SRC_OVER:
      fs = 1;
      fd = 1 - sa;
      break;
    case AlphaComposite.DST_OVER:
      fs = 1 - da;
      fd = 1;
      break;
    case AlphaComposite.SRC_IN:
      fs = da;
      fd = 0;
      break;
    case AlphaComposite.DST_IN:
      fs = 0;
      fd = sa;
      break;
    case AlphaComposite.SRC_OUT:
      fs = 1 - da;
      fd = 0;
      break;
    case AlphaComposite.DST_OUT:
      fs = 0;
      fd = 1 - sa;
      break;
    case AlphaComposite.SRC_ATOP:
      fs = da;
      fd = 1 - sa;
      break;
    case AlphaComposite.DST_ATOP:
      fs = 1 - da;
      fd = sa;
      break;
    default:
      fs = 1 - da;
      fd = 1 - sa;
      break;
    }
    float ra = sa * fs + da * fd;
    float rr = sr * fs + dr * fd;
    float rg = sg * fs + dg * fd;
    float rb = sb * fs + db * fd;
    if (ra <= 0) {
      return 0;
    }
    int a = clamp(ra * 255f);
    int r = clamp(rr / ra * 255f);
    int g = clamp(rg / ra * 255f);
    int b = clamp(rb / ra * 255f);
    return (a << 24) | (r << 16) | (g << 8) | b;
  }

  private static int clamp(float v) {
    int i = (int) (v + 0.5f);
    return i < 0 ? 0 : i > 255 ? 255 : i;
  }

  public static int srcOver(int src, int dst, int coverage255) {
    int sa = ((src >>> 24) * coverage255 + 127) / 255;
    if (sa == 255) {
      return src;
    }
    if (sa == 0) {
      return dst;
    }
    int da = dst >>> 24;
    if (da == 255) {
      int inv = 255 - sa;
      int r = (((src >> 16) & 0xff) * sa + ((dst >> 16) & 0xff) * inv + 127) / 255;
      int g = (((src >> 8) & 0xff) * sa + ((dst >> 8) & 0xff) * inv + 127) / 255;
      int b = ((src & 0xff) * sa + (dst & 0xff) * inv + 127) / 255;
      return 0xff000000 | (r << 16) | (g << 8) | b;
    }
    return compose(AlphaComposite.SRC_OVER, (src & 0x00ffffff) | (sa << 24), dst, 1f);
  }
}
