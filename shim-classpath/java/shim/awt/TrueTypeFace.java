/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.awt;

import java.awt.FontFormatException;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.util.HashMap;
import java.util.Map;

public final class TrueTypeFace extends FontFace {
  private final byte[] d;
  private final Map<String, int[]> tables = new HashMap<>();
  private int unitsPerEm;
  private int locaFormat;
  private int numGlyphs;
  private int numHMetrics;
  private float ascent;
  private float descent;
  private float lineGap;
  private int maxAdvanceUnits = 1;
  private String family = "Unknown";
  private String fullName;
  private String psName;
  private int macStyle;
  private CffOutlines cff;
  private int[] cmapSegments;
  private int cmapFormat;
  private int cmapOffset;
  private final Map<Integer, Shape> cache = new HashMap<>();

  private TrueTypeFace(byte[] data) {
    this.d = data;
  }

  public static TrueTypeFace parse(byte[] data, int fontIndex) throws FontFormatException {
    TrueTypeFace f = new TrueTypeFace(data);
    f.read(fontIndex);
    return f;
  }

  private int u16(int o) {
    return ((d[o] & 0xff) << 8) | (d[o + 1] & 0xff);
  }

  private int s16(int o) {
    return (short) u16(o);
  }

  private long u32(int o) {
    return ((long) u16(o) << 16) | u16(o + 2);
  }

  private void read(int fontIndex) throws FontFormatException {
    try {
      int base = 0;
      if (d.length < 12) {
        throw new FontFormatException("Truncated font");
      }
      long tag = u32(0);
      if (tag == 0x74746366L) {
        base = (int) u32(12 + fontIndex * 4);
        tag = u32(base);
      }
      if (tag != 0x00010000L && tag != 0x74727565L && tag != 0x4F54544FL) {
        throw new FontFormatException("Unsupported font format");
      }
      int n = u16(base + 4);
      for (int i = 0; i < n; i++) {
        int rec = base + 12 + i * 16;
        String name = new String(d, rec, 4, java.nio.charset.StandardCharsets.ISO_8859_1);
        tables.put(name, new int[] {(int) u32(rec + 8), (int) u32(rec + 12)});
      }
      int[] head = need("head");
      unitsPerEm = u16(head[0] + 18);
      locaFormat = s16(head[0] + 50);
      macStyle = u16(head[0] + 44);
      numGlyphs = u16(need("maxp")[0] + 4);
      int[] hhea = need("hhea");
      ascent = s16(hhea[0] + 4) / (float) unitsPerEm;
      descent = -s16(hhea[0] + 6) / (float) unitsPerEm;
      lineGap = s16(hhea[0] + 8) / (float) unitsPerEm;
      numHMetrics = u16(hhea[0] + 34);
      maxAdvanceUnits = u16(hhea[0] + 10);
      int[] cff = tables.get("CFF ");
      if (cff != null) {
        this.cff = new CffOutlines(d, cff[0], cff[1], unitsPerEm);
      } else {
        need("loca");
        need("glyf");
      }
      need("hmtx");
      readCmap();
      readName();
    } catch (ArrayIndexOutOfBoundsException e) {
      throw new FontFormatException("Corrupt font data");
    }
  }

  private int[] need(String name) throws FontFormatException {
    int[] t = tables.get(name);
    if (t == null) {
      throw new FontFormatException("Missing " + name + " table");
    }
    return t;
  }

  private void readName() {
    int[] t = tables.get("name");
    if (t == null) {
      return;
    }
    int count = u16(t[0] + 2);
    int strings = t[0] + u16(t[0] + 4);
    String[] best = new String[7];
    int[] rank = new int[7];
    for (int i = 0; i < count; i++) {
      int rec = t[0] + 6 + i * 12;
      int platform = u16(rec);
      int lang = u16(rec + 4);
      int nameId = u16(rec + 6);
      if (nameId != 1 && nameId != 4 && nameId != 6 && nameId != 16) {
        continue;
      }
      int slot = nameId == 16 ? 0 : nameId;
      int len = u16(rec + 8);
      int off = strings + u16(rec + 10);
      int score = platform == 3 && lang == 0x409 ? 3 : platform == 3 || platform == 0 ? 2 : 1;
      if (score <= rank[slot]) {
        continue;
      }
      rank[slot] = score;
      best[slot] = platform == 3 || platform == 0 ? new String(d, off, len, java.nio.charset.StandardCharsets.UTF_16BE)
          : new String(d, off, len, java.nio.charset.StandardCharsets.ISO_8859_1);
    }
    if (best[1] != null) {
      family = best[1];
    } else if (best[0] != null) {
      family = best[0];
    }
    fullName = best[4];
    psName = best[6];
  }

  private void readCmap() throws FontFormatException {
    int[] t = need("cmap");
    int n = u16(t[0] + 2);
    int chosen = -1;
    int chosenScore = -1;
    for (int i = 0; i < n; i++) {
      int rec = t[0] + 4 + i * 8;
      int platform = u16(rec);
      int enc = u16(rec + 2);
      int off = t[0] + (int) u32(rec + 4);
      int fmt = u16(off);
      int score = -1;
      if (platform == 3 && enc == 10 && fmt == 12) {
        score = 4;
      } else if (platform == 0 && fmt == 12) {
        score = 3;
      } else if (platform == 3 && enc == 1 && fmt == 4) {
        score = 2;
      } else if (platform == 0 && fmt == 4) {
        score = 1;
      } else if (fmt == 4 || fmt == 12 || fmt == 0 || fmt == 6) {
        score = 0;
      }
      if (score > chosenScore) {
        chosenScore = score;
        chosen = off;
      }
    }
    if (chosen < 0) {
      throw new FontFormatException("No usable cmap");
    }
    cmapOffset = chosen;
    cmapFormat = u16(chosen);
  }

  private int glyphFor(int cp) {
    int o = cmapOffset;
    switch (cmapFormat) {
    case 0:
      return cp < 256 ? d[o + 6 + cp] & 0xff : 0;
    case 6: {
      int first = u16(o + 6);
      int count = u16(o + 8);
      return cp >= first && cp < first + count ? u16(o + 10 + (cp - first) * 2) : 0;
    }
    case 4: {
      if (cp > 0xffff) {
        return 0;
      }
      int segX2 = u16(o + 6);
      int endCodes = o + 14;
      int startCodes = endCodes + segX2 + 2;
      int deltas = startCodes + segX2;
      int rangeOffsets = deltas + segX2;
      for (int i = 0; i < segX2; i += 2) {
        int end = u16(endCodes + i);
        if (cp <= end) {
          int start = u16(startCodes + i);
          if (cp < start) {
            return 0;
          }
          int delta = u16(deltas + i);
          int ro = u16(rangeOffsets + i);
          if (ro == 0) {
            return (cp + delta) & 0xffff;
          }
          int g = u16(rangeOffsets + i + ro + (cp - start) * 2);
          return g == 0 ? 0 : (g + delta) & 0xffff;
        }
      }
      return 0;
    }
    case 12: {
      long groups = u32(o + 12);
      for (int i = 0; i < groups; i++) {
        int g = o + 16 + i * 12;
        long start = u32(g);
        long end = u32(g + 4);
        if (cp >= start && cp <= end) {
          return (int) (u32(g + 8) + (cp - start));
        }
      }
      return 0;
    }
    default:
      return 0;
    }
  }

  public String family() { return family; }

  public String fullName() { return fullName; }

  public String postScriptName() { return psName; }

  public int fontStyle() { return (macStyle & 1) | (macStyle >> 1 & 1) << 1; }

  public boolean styled() { return (macStyle & 3) != 0; }

  public boolean canDisplay(int cp) { return glyphFor(cp) != 0; }

  public float[] styleMetrics(float size, float ascentPixels) {
    float[] m = super.styleMetrics(size, ascentPixels);
    int[] os2 = tables.get("OS/2");
    int[] post = tables.get("post");
    if (os2 != null && os2[1] >= 30) {
      int ySize = s16(os2[0] + 26);
      int yPos = s16(os2[0] + 28);
      if (ySize != 0 || yPos != 0) {
        m[0] = -yPos * size / unitsPerEm;
        m[1] = ySize * size / unitsPerEm;
      }
    }
    if (post != null && post[1] >= 12) {
      int pos = s16(post[0] + 8);
      int thick = s16(post[0] + 10);
      if (pos != 0 || thick != 0) {
        m[2] = -pos * size / unitsPerEm;
        m[3] = thick * size / unitsPerEm;
      }
    }
    return m;
  }

  public int unitsPerEm() { return unitsPerEm; }

  public int numGlyphs() { return numGlyphs; }

  public int glyphCode(int cp) { return glyphFor(cp); }

  public synchronized Shape hintedGlyph(int cp, float size) {
    if (hintedAdvance(cp, size) < 0 || hinter == null) {
      return null;
    }
    return hinter.hintedOutline(glyphFor(cp), size);
  }

  private java.util.HashMap<Long, Integer> kernPairs;

  public float kerning(int a, int b) {
    if (kernPairs == null) {
      kernPairs = new java.util.HashMap<>();
      int[] kern = tables.get("kern");
      if (kern != null && kern[1] >= 4 && u16(kern[0]) == 0) {
        int o = kern[0] + 4;
        int n = u16(kern[0] + 2);
        for (int t = 0; t < n && o + 6 <= kern[0] + kern[1]; t++) {
          int len = u16(o + 2);
          int cov = u16(o + 4);
          if ((cov >> 8) == 0 && (cov & 1) != 0) {
            int pairs = u16(o + 6);
            for (int i = 0; i < pairs; i++) {
              int p = o + 14 + i * 6;
              kernPairs.putIfAbsent(((long) u16(p) << 16) | u16(p + 2), s16(p + 4));
            }
          }
          o += len;
        }
      }
    }
    Integer v = kernPairs.get(((long) glyphFor(a) << 16) | glyphFor(b));
    return v == null ? 0f : (float) v / unitsPerEm;
  }

  public int codePointOf(int glyph) {
    synchronized (cache) {
      if (reverse == null) {
        reverse = new HashMap<>();
        for (int cp = 0; cp < 0x10000; cp++) {
          int g = glyphFor(cp);
          if (g != 0) {
            reverse.putIfAbsent(g, cp);
          }
        }
      }
      Integer cp = reverse.get(glyph);
      return cp == null ? 0 : cp;
    }
  }

  private Map<Integer, Integer> reverse;

  public float advance(int cp) {
    int g = glyphFor(cp);
    int hm = tables.get("hmtx")[0];
    int idx = Math.min(g, numHMetrics - 1);
    return u16(hm + idx * 4) / (float) unitsPerEm;
  }

  private TrueTypeHinter hinter;
  private boolean hinterTried;

  public synchronized int hintedAdvance(int cp, float size) {
    if (cff != null) {
      return -1;
    }
    if (!hinterTried) {
      hinterTried = true;
      try {
        TrueTypeHinter h = new TrueTypeHinter(d, tables, unitsPerEm, locaFormat, numGlyphs, numHMetrics);
        hinter = h.usable() ? h : null;
      } catch (RuntimeException e) {
        hinter = null;
      }
    }
    return hinter == null ? -1 : hinter.advance(glyphFor(cp), size);
  }

  public int maxAdvancePixels(float size) {
    long scale = TrueTypeHinter.divFix(Math.round(size * 64f), unitsPerEm);
    long v = TrueTypeHinter.mulFix(maxAdvanceUnits, scale);
    return (int) ((v + 32) >> 6);
  }

  public float ascent() { return ascent; }

  public float descent() { return descent; }

  public float leading() { return lineGap; }

  public Shape outline(int cp, boolean bold, boolean italic) {
    int g = glyphFor(cp);
    Shape base;
    synchronized (cache) {
      base = cache.get(g);
      if (base == null) {
        Path2D.Float path = new Path2D.Float(Path2D.WIND_NON_ZERO);
        try {
          glyph(g, path, new AffineTransform(), 0);
        } catch (RuntimeException e) {
          path.reset();
        }
        base = path;
        cache.put(g, base);
      }
    }
    if (!bold && !italic) {
      return base;
    }
    Path2D.Float copy = new Path2D.Float(base);
    if (italic) {
      copy.transform(AffineTransform.getShearInstance(-0.2, 0));
    }
    if (bold) {
      Path2D.Float shifted = new Path2D.Float(copy);
      shifted.transform(AffineTransform.getTranslateInstance(0.03, 0));
      copy.append(shifted, false);
    }
    return copy;
  }

  private int glyphOffset(int g, int[] lengthOut) {
    int loca = tables.get("loca")[0];
    int glyf = tables.get("glyf")[0];
    int a;
    int b;
    if (locaFormat == 0) {
      a = u16(loca + g * 2) * 2;
      b = u16(loca + g * 2 + 2) * 2;
    } else {
      a = (int) u32(loca + g * 4);
      b = (int) u32(loca + g * 4 + 4);
    }
    lengthOut[0] = b - a;
    return glyf + a;
  }

  private void glyph(int g, Path2D.Float out, AffineTransform at, int depth) {
    if (g < 0 || g >= numGlyphs || depth > 6) {
      return;
    }
    if (cff != null) {
      Path2D.Float raw = new Path2D.Float(Path2D.WIND_NON_ZERO);
      cff.glyph(g, raw);
      out.append(raw.getPathIterator(at), false);
      return;
    }
    int[] len = new int[1];
    int o = glyphOffset(g, len);
    if (len[0] <= 0) {
      return;
    }
    int contours = s16(o);
    float scale = 1f / unitsPerEm;
    if (contours >= 0) {
      int p = o + 10;
      int[] ends = new int[contours];
      for (int i = 0; i < contours; i++) {
        ends[i] = u16(p);
        p += 2;
      }
      int npts = contours == 0 ? 0 : ends[contours - 1] + 1;
      int insLen = u16(p);
      p += 2 + insLen;
      int[] flags = new int[npts];
      for (int i = 0; i < npts;) {
        int f = d[p++] & 0xff;
        flags[i++] = f;
        if ((f & 8) != 0) {
          int rep = d[p++] & 0xff;
          while (rep-- > 0 && i < npts) {
            flags[i++] = f;
          }
        }
      }
      int[] xs = new int[npts];
      int[] ys = new int[npts];
      int v = 0;
      for (int i = 0; i < npts; i++) {
        int f = flags[i];
        if ((f & 2) != 0) {
          int dx = d[p++] & 0xff;
          v += (f & 16) != 0 ? dx : -dx;
        } else if ((f & 16) == 0) {
          v += s16(p);
          p += 2;
        }
        xs[i] = v;
      }
      v = 0;
      for (int i = 0; i < npts; i++) {
        int f = flags[i];
        if ((f & 4) != 0) {
          int dy = d[p++] & 0xff;
          v += (f & 32) != 0 ? dy : -dy;
        } else if ((f & 32) == 0) {
          v += s16(p);
          p += 2;
        }
        ys[i] = v;
      }
      int start = 0;
      double[] pt = new double[2];
      for (int c = 0; c < contours; c++) {
        int end = ends[c];
        contour(out, at, xs, ys, flags, start, end, scale);
        start = end + 1;
      }
    } else {
      int p = o + 10;
      while (true) {
        int flags = u16(p);
        int gi = u16(p + 2);
        p += 4;
        double dx;
        double dy;
        if ((flags & 1) != 0) {
          dx = s16(p);
          dy = s16(p + 2);
          p += 4;
        } else {
          dx = d[p];
          dy = d[p + 1];
          p += 2;
        }
        double a = 1;
        double b = 0;
        double c = 0;
        double dd = 1;
        if ((flags & 8) != 0) {
          a = dd = s16(p) / 16384.0;
          p += 2;
        } else if ((flags & 0x40) != 0) {
          a = s16(p) / 16384.0;
          dd = s16(p + 2) / 16384.0;
          p += 4;
        } else if ((flags & 0x80) != 0) {
          a = s16(p) / 16384.0;
          b = s16(p + 2) / 16384.0;
          c = s16(p + 4) / 16384.0;
          dd = s16(p + 6) / 16384.0;
          p += 8;
        }
        if ((flags & 2) == 0) {
          dx = 0;
          dy = 0;
        }
        AffineTransform child = new AffineTransform(at);
        child.concatenate(new AffineTransform(a, b, c, dd, dx * scale, -dy * scale));
        glyph(gi, out, child, depth + 1);
        if ((flags & 0x20) == 0) {
          break;
        }
      }
    }
  }

  private static void contour(Path2D.Float out, AffineTransform at, int[] xs, int[] ys, int[] flags, int s, int e,
      float scale) {
    int n = e - s + 1;
    if (n < 2) {
      return;
    }
    double[] px = new double[n];
    double[] py = new double[n];
    boolean[] on = new boolean[n];
    double[] tmp = new double[2];
    for (int i = 0; i < n; i++) {
      tmp[0] = xs[s + i] * scale;
      tmp[1] = -ys[s + i] * scale;
      at.transform(tmp, 0, tmp, 0, 1);
      px[i] = tmp[0];
      py[i] = tmp[1];
      on[i] = (flags[s + i] & 1) != 0;
    }
    int first = -1;
    for (int i = 0; i < n; i++) {
      if (on[i]) {
        first = i;
        break;
      }
    }
    double sx;
    double sy;
    int startIdx;
    if (first >= 0) {
      sx = px[first];
      sy = py[first];
      startIdx = first + 1;
    } else {
      sx = (px[0] + px[n - 1]) / 2;
      sy = (py[0] + py[n - 1]) / 2;
      startIdx = 0;
    }
    out.moveTo(sx, sy);
    double cx = 0;
    double cy = 0;
    boolean haveCtrl = false;
    int count = first >= 0 ? n - 1 : n;
    for (int k = 0; k < count; k++) {
      int i = (startIdx + k) % n;
      if (on[i]) {
        if (haveCtrl) {
          out.quadTo(cx, cy, px[i], py[i]);
          haveCtrl = false;
        } else {
          out.lineTo(px[i], py[i]);
        }
      } else {
        if (haveCtrl) {
          double mx = (cx + px[i]) / 2;
          double my = (cy + py[i]) / 2;
          out.quadTo(cx, cy, mx, my);
        }
        cx = px[i];
        cy = py[i];
        haveCtrl = true;
      }
    }
    if (haveCtrl) {
      out.quadTo(cx, cy, sx, sy);
    }
    out.closePath();
  }
}
