/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.awt;

import java.awt.geom.Path2D;

final class CffOutlines {
  private final byte[] d;
  private final int[] charStrings;
  private final int[] globalSubrs;
  private final int[][] localSubrs;
  private final byte[] fdSelect;
  private final float scale;

  private float[] stack = new float[96];
  private int sp;
  private int stems;
  private boolean widthDone;
  private double x;
  private double y;
  private boolean open;
  private Path2D.Float path;
  private int[] subrs;
  private boolean ended;

  CffOutlines(byte[] data, int offset, int length, int unitsPerEm) {
    d = data;
    scale = 1f / unitsPerEm;
    int hdrSize = u8(offset + 2);
    int[] nameIndex = index(offset + hdrSize);
    int[] topIndex = index(nameIndex[nameIndex.length - 1]);
    int[] stringIndex = index(topIndex[topIndex.length - 1]);
    globalSubrs = index(stringIndex[stringIndex.length - 1]);
    float[][] top = dict(topIndex[0], topIndex[1]);
    int csOffset = (int) top[17][0];
    charStrings = index(offset + csOffset);
    if (top[1230] != null) {
      int fdArray = offset + (int) top[1236][0];
      int[] fds = index(fdArray);
      localSubrs = new int[fds.length - 1][];
      for (int i = 0; i + 1 < fds.length; i++) {
        float[][] fd = dict(fds[i], fds[i + 1]);
        localSubrs[i] = privateSubrs(offset, fd[18]);
      }
      fdSelect = readFdSelect(offset + (int) top[1237][0], charStrings.length - 1);
    } else {
      localSubrs = new int[][] {privateSubrs(offset, top[18])};
      fdSelect = null;
    }
  }

  int glyphCount() {
    return charStrings.length - 1;
  }

  private int[] privateSubrs(int base, float[] priv) {
    if (priv == null || priv.length < 2) {
      return new int[0];
    }
    int size = (int) priv[0];
    int off = base + (int) priv[1];
    float[][] p = dict(off, off + size);
    return p[19] == null ? new int[0] : index(off + (int) p[19][0]);
  }

  private byte[] readFdSelect(int o, int glyphs) {
    byte[] out = new byte[glyphs];
    int format = u8(o);
    if (format == 0) {
      for (int i = 0; i < glyphs; i++) {
        out[i] = d[o + 1 + i];
      }
    } else if (format == 3) {
      int n = u16(o + 1);
      for (int r = 0; r < n; r++) {
        int first = u16(o + 3 + r * 3);
        int fd = u8(o + 5 + r * 3);
        int next = u16(o + 6 + r * 3);
        for (int g = first; g < next && g < glyphs; g++) {
          out[g] = (byte) fd;
        }
      }
    }
    return out;
  }

  private int u8(int o) {
    return d[o] & 0xff;
  }

  private int u16(int o) {
    return u8(o) << 8 | u8(o + 1);
  }

  private int[] index(int o) {
    int count = u16(o);
    if (count == 0) {
      return new int[] {o + 2};
    }
    int offSize = u8(o + 2);
    int[] out = new int[count + 1];
    int base = o + 3 + (count + 1) * offSize - 1;
    for (int i = 0; i <= count; i++) {
      int v = 0;
      for (int k = 0; k < offSize; k++) {
        v = v << 8 | u8(o + 3 + i * offSize + k);
      }
      out[i] = base + v;
    }
    return out;
  }

  private float[][] dict(int start, int end) {
    float[][] out = new float[1300][];
    float[] operands = new float[48];
    int n = 0;
    int p = start;
    while (p < end) {
      int b = u8(p++);
      if (b <= 21) {
        int op = b;
        if (b == 12) {
          op = 1200 + u8(p++);
        }
        out[op] = java.util.Arrays.copyOf(operands, n);
        n = 0;
      } else if (b == 28) {
        operands[n++ % 48] = (short) u16(p);
        p += 2;
      } else if (b == 29) {
        operands[n++ % 48] = u16(p) << 16 | u16(p + 2);
        p += 4;
      } else if (b == 30) {
        StringBuilder sb = new StringBuilder();
        boolean done = false;
        while (!done) {
          int v = u8(p++);
          for (int nib : new int[] {v >> 4, v & 15}) {
            if (nib == 15) {
              done = true;
              break;
            }
            sb.append(nib < 10 ? String.valueOf(nib) : nib == 10 ? "." : nib == 11 ? "E" : nib == 12 ? "E-" : nib == 14 ? "-" : "");
          }
        }
        float f = 0;
        try {
          f = Float.parseFloat(sb.toString());
        } catch (NumberFormatException e) {
          f = 0;
        }
        operands[n++ % 48] = f;
      } else if (b >= 32 && b <= 246) {
        operands[n++ % 48] = b - 139;
      } else if (b >= 247 && b <= 250) {
        operands[n++ % 48] = (b - 247) * 256 + u8(p++) + 108;
      } else if (b >= 251 && b <= 254) {
        operands[n++ % 48] = -(b - 251) * 256 - u8(p++) - 108;
      }
    }
    return out;
  }

  private static int bias(int count) {
    return count < 1240 ? 107 : count < 33900 ? 1131 : 32768;
  }

  synchronized void glyph(int gid, Path2D.Float out) {
    if (gid < 0 || gid >= glyphCount()) {
      return;
    }
    path = out;
    sp = 0;
    stems = 0;
    widthDone = false;
    x = 0;
    y = 0;
    open = false;
    ended = false;
    int fd = fdSelect == null ? 0 : fdSelect[gid] & 0xff;
    subrs = fd < localSubrs.length ? localSubrs[fd] : new int[0];
    run(charStrings[gid], charStrings[gid + 1], 0);
    if (open) {
      path.closePath();
    }
  }

  private void push(float v) {
    if (sp == stack.length) {
      stack = java.util.Arrays.copyOf(stack, sp * 2);
    }
    stack[sp++] = v;
  }

  private void moveTo(double dx, double dy) {
    if (open) {
      path.closePath();
    }
    x += dx;
    y += dy;
    path.moveTo(x * scale, -y * scale);
    open = true;
  }

  private void lineTo(double dx, double dy) {
    x += dx;
    y += dy;
    path.lineTo(x * scale, -y * scale);
  }

  private void curveTo(double dx1, double dy1, double dx2, double dy2, double dx3, double dy3) {
    double x1 = x + dx1;
    double y1 = y + dy1;
    double x2 = x1 + dx2;
    double y2 = y1 + dy2;
    x = x2 + dx3;
    y = y2 + dy3;
    path.curveTo(x1 * scale, -y1 * scale, x2 * scale, -y2 * scale, x * scale, -y * scale);
  }

  private void takeWidth(boolean has) {
    if (!widthDone) {
      widthDone = true;
      if (has) {
        System.arraycopy(stack, 1, stack, 0, sp - 1);
        sp--;
      }
    }
  }

  private void run(int start, int end, int depth) {
    if (depth > 10) {
      return;
    }
    int p = start;
    while (p < end && !ended) {
      int b = u8(p++);
      if (b >= 32 || b == 28) {
        if (b == 28) {
          push((short) u16(p));
          p += 2;
        } else if (b <= 246) {
          push(b - 139);
        } else if (b <= 250) {
          push((b - 247) * 256 + u8(p++) + 108);
        } else if (b <= 254) {
          push(-(b - 251) * 256 - u8(p++) - 108);
        } else {
          push((u16(p) << 16 | u16(p + 2)) / 65536f);
          p += 4;
        }
        continue;
      }
      switch (b) {
      case 1:
      case 3:
      case 18:
      case 23:
        takeWidth((sp & 1) != 0);
        stems += sp / 2;
        sp = 0;
        break;
      case 19:
      case 20:
        takeWidth((sp & 1) != 0);
        stems += sp / 2;
        sp = 0;
        p += (stems + 7) / 8;
        break;
      case 21:
        takeWidth(sp > 2);
        if (sp >= 2) {
          moveTo(stack[sp - 2], stack[sp - 1]);
        }
        sp = 0;
        break;
      case 22:
        takeWidth(sp > 1);
        if (sp >= 1) {
          moveTo(stack[sp - 1], 0);
        }
        sp = 0;
        break;
      case 4:
        takeWidth(sp > 1);
        if (sp >= 1) {
          moveTo(0, stack[sp - 1]);
        }
        sp = 0;
        break;
      case 5:
        for (int i = 0; i + 1 < sp; i += 2) {
          lineTo(stack[i], stack[i + 1]);
        }
        sp = 0;
        break;
      case 6:
      case 7: {
        boolean horizontal = b == 6;
        for (int i = 0; i < sp; i++) {
          if (horizontal) {
            lineTo(stack[i], 0);
          } else {
            lineTo(0, stack[i]);
          }
          horizontal = !horizontal;
        }
        sp = 0;
        break;
      }
      case 8:
        for (int i = 0; i + 5 < sp; i += 6) {
          curveTo(stack[i], stack[i + 1], stack[i + 2], stack[i + 3], stack[i + 4], stack[i + 5]);
        }
        sp = 0;
        break;
      case 24: {
        int i = 0;
        for (; i + 7 < sp; i += 6) {
          curveTo(stack[i], stack[i + 1], stack[i + 2], stack[i + 3], stack[i + 4], stack[i + 5]);
        }
        if (i + 1 < sp) {
          lineTo(stack[i], stack[i + 1]);
        }
        sp = 0;
        break;
      }
      case 25: {
        int i = 0;
        for (; i + 7 < sp; i += 2) {
          lineTo(stack[i], stack[i + 1]);
        }
        if (i + 5 < sp) {
          curveTo(stack[i], stack[i + 1], stack[i + 2], stack[i + 3], stack[i + 4], stack[i + 5]);
        }
        sp = 0;
        break;
      }
      case 26: {
        int i = 0;
        double dx1 = 0;
        if ((sp & 1) != 0) {
          dx1 = stack[i++];
        }
        for (; i + 3 < sp; i += 4) {
          curveTo(dx1, stack[i], stack[i + 1], stack[i + 2], 0, stack[i + 3]);
          dx1 = 0;
        }
        sp = 0;
        break;
      }
      case 27: {
        int i = 0;
        double dy1 = 0;
        if ((sp & 1) != 0) {
          dy1 = stack[i++];
        }
        for (; i + 3 < sp; i += 4) {
          curveTo(stack[i], dy1, stack[i + 1], stack[i + 2], stack[i + 3], 0);
          dy1 = 0;
        }
        sp = 0;
        break;
      }
      case 30:
      case 31: {
        boolean horizontal = b == 31;
        int i = 0;
        while (i + 3 < sp) {
          double last = sp - i == 5 ? stack[i + 4] : 0;
          if (horizontal) {
            curveTo(stack[i], 0, stack[i + 1], stack[i + 2], last, stack[i + 3]);
          } else {
            curveTo(0, stack[i], stack[i + 1], stack[i + 2], stack[i + 3], last);
          }
          horizontal = !horizontal;
          i += 4;
        }
        sp = 0;
        break;
      }
      case 10: {
        if (sp > 0) {
          int n = (int) stack[--sp] + bias(subrs.length - 1);
          if (n >= 0 && n + 1 < subrs.length) {
            run(subrs[n], subrs[n + 1], depth + 1);
          }
        }
        break;
      }
      case 29: {
        if (sp > 0) {
          int n = (int) stack[--sp] + bias(globalSubrs.length - 1);
          if (n >= 0 && n + 1 < globalSubrs.length) {
            run(globalSubrs[n], globalSubrs[n + 1], depth + 1);
          }
        }
        break;
      }
      case 11:
        return;
      case 14:
        takeWidth(sp == 1 || sp == 5);
        ended = true;
        break;
      case 12: {
        int op = u8(p++);
        flex(op);
        break;
      }
      default:
        sp = 0;
        break;
      }
    }
  }

  private void flex(int op) {
    switch (op) {
    case 34:
      if (sp >= 7) {
        curveTo(stack[0], 0, stack[1], stack[2], stack[3], 0);
        curveTo(stack[4], 0, stack[5], -stack[2], stack[6], 0);
      }
      break;
    case 35:
      if (sp >= 12) {
        curveTo(stack[0], stack[1], stack[2], stack[3], stack[4], stack[5]);
        curveTo(stack[6], stack[7], stack[8], stack[9], stack[10], stack[11]);
      }
      break;
    case 36:
      if (sp >= 9) {
        curveTo(stack[0], stack[1], stack[2], stack[3], stack[4], 0);
        curveTo(stack[5], 0, stack[6], stack[7], stack[8], -(stack[1] + stack[3] + stack[7]));
      }
      break;
    case 37:
      if (sp >= 11) {
        double dx = stack[0] + stack[2] + stack[4] + stack[6] + stack[8];
        double dy = stack[1] + stack[3] + stack[5] + stack[7] + stack[9];
        curveTo(stack[0], stack[1], stack[2], stack[3], stack[4], stack[5]);
        if (Math.abs(dx) > Math.abs(dy)) {
          curveTo(stack[6], stack[7], stack[8], stack[9], stack[10], -dy);
        } else {
          curveTo(stack[6], stack[7], stack[8], stack[9], -dx, stack[10]);
        }
      }
      break;
    default:
      break;
    }
    sp = 0;
  }
}
