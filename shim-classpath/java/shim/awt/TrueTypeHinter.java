/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.awt;

import java.util.HashMap;
import java.util.Map;

/** Grid-fitted glyph advances from the TrueType bytecode of a font, computed with full hinting. */
final class TrueTypeHinter {
  private static final int kOn = 1;
  private static final int kTouchX = 2;
  private static final int kTouchY = 4;
  private static final int kMaxSteps = 2000000;

  private static final class Zone {
    int n;
    int[] curX;
    int[] curY;
    int[] orgX;
    int[] orgY;
    int[] orusX;
    int[] orusY;
    byte[] tags;
    int[] contours = new int[0];
    int nContours;

    Zone(int n) {
      this.n = n;
      curX = new int[n];
      curY = new int[n];
      orgX = new int[n];
      orgY = new int[n];
      orusX = new int[n];
      orusY = new int[n];
      tags = new byte[n];
    }
  }

  private static final class Def {
    byte[] code;
    int start;
    int end;
    boolean active;
  }

  private static final class State {
    int rp0;
    int rp1;
    int rp2;
    int dualX = 0x4000;
    int dualY;
    int projX = 0x4000;
    int projY;
    int freeX = 0x4000;
    int freeY;
    int loop = 1;
    int minDist = 64;
    int round = 1;
    boolean autoFlip = true;
    int cvtCutIn = 68;
    int swCutIn;
    int swValue;
    int deltaBase = 9;
    int deltaShift = 3;
    int instructControl;
    int gep0 = 1;
    int gep1 = 1;
    int gep2 = 1;

    State copy() {
      State s = new State();
      s.rp0 = rp0;
      s.rp1 = rp1;
      s.rp2 = rp2;
      s.dualX = dualX;
      s.dualY = dualY;
      s.projX = projX;
      s.projY = projY;
      s.freeX = freeX;
      s.freeY = freeY;
      s.loop = loop;
      s.minDist = minDist;
      s.round = round;
      s.autoFlip = autoFlip;
      s.cvtCutIn = cvtCutIn;
      s.swCutIn = swCutIn;
      s.swValue = swValue;
      s.deltaBase = deltaBase;
      s.deltaShift = deltaShift;
      s.instructControl = instructControl;
      s.gep0 = gep0;
      s.gep1 = gep1;
      s.gep2 = gep2;
      return s;
    }
  }

  private static final class Sized {
    int ppem;
    int scale;
    int[] cvt;
    int[] storage;
    Zone twilight;
    State gs;
    Map<Integer, Integer> advances = new HashMap<>();
  }

  private static final class Loaded {
    int[] x;
    int[] y;
    byte[] tags;
    int[] contours;
    int n;
    int pp1;
    int pp2;
  }

  private final byte[] d;
  private final int glyf;
  private final int loca;
  private final int hmtx;
  private final int unitsPerEm;
  private final int locaFormat;
  private final int numGlyphs;
  private final int numHMetrics;
  private final int maxStack;
  private final int maxStorage;
  private final int maxTwilight;
  private final int maxFuncs;
  private final int[] cvtFu;
  private final byte[] fpgm;
  private final byte[] prep;
  private final boolean integerPpem;
  private final Map<Integer, Sized> sizes = new HashMap<>();
  private Def[] funcs;
  private boolean valid = true;
  private boolean fpgmDone;

  private Sized sz;
  private State gs;
  private Zone pts;
  private Zone twilight;
  private Zone zp0;
  private Zone zp1;
  private Zone zp2;
  private int[] cvt;
  private int[] stack;
  private int top;
  private boolean composite;
  private boolean inPrep;
  private int xScale;
  private int fDotP = 0x4000;
  private int period;
  private int phase;
  private int threshold;
  private int steps;

  private byte[][] frameCode = new byte[64][];
  private int[] frameIp = new int[64];
  private int[] frameCount = new int[64];
  private Def[] frameDef = new Def[64];
  private int frames;

  TrueTypeHinter(byte[] data, Map<String, int[]> tables, int unitsPerEm, int locaFormat, int numGlyphs,
      int numHMetrics) {
    this.d = data;
    this.unitsPerEm = unitsPerEm;
    this.locaFormat = locaFormat;
    this.numGlyphs = numGlyphs;
    this.numHMetrics = numHMetrics;
    this.glyf = tables.get("glyf")[0];
    this.loca = tables.get("loca")[0];
    this.hmtx = tables.get("hmtx")[0];
    int[] maxp = tables.get("maxp");
    boolean full = maxp[1] >= 32;
    this.maxTwilight = full ? u16(maxp[0] + 16) : 0;
    this.maxStorage = full ? u16(maxp[0] + 18) : 0;
    this.maxFuncs = full ? u16(maxp[0] + 20) : 0;
    this.maxStack = full ? u16(maxp[0] + 24) : 256;
    int[] head = tables.get("head");
    this.integerPpem = (u16(head[0] + 16) & 8) != 0;
    int[] c = tables.get("cvt ");
    if (c != null) {
      cvtFu = new int[c[1] / 2];
      for (int i = 0; i < cvtFu.length; i++) {
        cvtFu[i] = (short) u16(c[0] + i * 2);
      }
    } else {
      cvtFu = new int[0];
    }
    int[] f = tables.get("fpgm");
    int[] p = tables.get("prep");
    fpgm = f == null ? new byte[0] : java.util.Arrays.copyOfRange(d, f[0], f[0] + f[1]);
    prep = p == null ? new byte[0] : java.util.Arrays.copyOfRange(d, p[0], p[0] + p[1]);
    funcs = new Def[Math.max(maxFuncs, 1) + 1];
    stack = new int[maxStack + 64];
    if (f == null || p == null) {
      valid = false;
    }
  }

  boolean usable() {
    return valid;
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

  static long mulFix(long a, long b) {
    long s = 1;
    if (a < 0) {
      a = -a;
      s = -s;
    }
    if (b < 0) {
      b = -b;
      s = -s;
    }
    long r = (a * b + 0x8000) >> 16;
    return s < 0 ? -r : r;
  }

  static long mulDiv(long a, long b, long c) {
    long s = 1;
    if (a < 0) {
      a = -a;
      s = -s;
    }
    if (b < 0) {
      b = -b;
      s = -s;
    }
    if (c < 0) {
      c = -c;
      s = -s;
    }
    long r = c > 0 ? (a * b + (c >> 1)) / c : 0x7FFFFFFFL;
    return s < 0 ? -r : r;
  }

  static long divFix(long a, long b) {
    long s = 1;
    if (a < 0) {
      a = -a;
      s = -s;
    }
    if (b < 0) {
      b = -b;
      s = -s;
    }
    long r = b > 0 ? ((a << 16) + (b >> 1)) / b : 0x7FFFFFFFL;
    return s < 0 ? -r : r;
  }

  private static int dot14(long ax, long ay, long bx, long by) {
    long t = ax * bx + ay * by;
    return (int) ((t + 0x2000 + (t >> 63)) >> 14);
  }

  private static int mulFix14(long a, long b) {
    long t = a * b;
    return (int) ((t + 0x2000 + (t >> 63)) >> 14);
  }

  /** Returns the hinted advance in whole pixels, or -1 when the font cannot be hinted at this size. */
  synchronized int advance(int glyph, float size) {
    if (!valid || size < 1f || size > 400f) {
      return -1;
    }
    try {
      Sized s = sized(Math.round(size), size);
      if (s == null) {
        return -1;
      }
      Integer cached = s.advances.get(glyph);
      if (cached != null) {
        return cached;
      }
      int adv = computeAdvance(s, glyph);
      s.advances.put(glyph, adv);
      return adv;
    } catch (RuntimeException e) {
      return -1;
    }
  }

  synchronized java.awt.geom.Path2D.Float hintedOutline(int glyph, float size) {
    if (!valid || size < 1f || size > 400f) {
      return null;
    }
    try {
      Sized s = sized(Math.round(size), size);
      if (s == null) {
        return null;
      }
      sz = s;
      cvt = s.cvt;
      twilight = s.twilight;
      xScale = s.scale;
      inPrep = false;
      Loaded l = load(glyph, 0, (s.gs.instructControl & 1) == 0);
      java.awt.geom.Path2D.Float path = new java.awt.geom.Path2D.Float(java.awt.geom.Path2D.WIND_NON_ZERO);
      int start = 0;
      for (int c : l.contours) {
        contour(path, l, start, c);
        start = c + 1;
      }
      return path;
    } catch (RuntimeException e) {
      return null;
    }
  }

  private static void contour(java.awt.geom.Path2D.Float path, Loaded l, int first, int last) {
    int n = last - first + 1;
    if (n < 1) {
      return;
    }
    int[] px = new int[n];
    int[] py = new int[n];
    boolean[] on = new boolean[n];
    for (int i = 0; i < n; i++) {
      px[i] = l.x[first + i];
      py[i] = -l.y[first + i];
      on[i] = (l.tags[first + i] & kOn) != 0;
    }
    int s = 0;
    int sx;
    int sy;
    if (on[0]) {
      sx = px[0];
      sy = py[0];
      s = 1;
    } else if (on[n - 1]) {
      sx = px[n - 1];
      sy = py[n - 1];
      n--;
    } else {
      sx = (px[0] + px[n - 1]) / 2;
      sy = (py[0] + py[n - 1]) / 2;
    }
    path.moveTo(sx / 64f, sy / 64f);
    float[] cur = {sx / 64f, sy / 64f};
    boolean haveCtl = false;
    int cx = 0;
    int cy = 0;
    for (int i = s; i < n; i++) {
      if (on[i]) {
        if (haveCtl) {
          conic(path, cur, cx / 64f, cy / 64f, px[i] / 64f, py[i] / 64f);
          haveCtl = false;
        } else {
          path.lineTo(px[i] / 64f, py[i] / 64f);
          cur[0] = px[i] / 64f;
          cur[1] = py[i] / 64f;
        }
      } else {
        if (haveCtl) {
          conic(path, cur, cx / 64f, cy / 64f, ((cx + px[i]) / 2) / 64f, ((cy + py[i]) / 2) / 64f);
        }
        cx = px[i];
        cy = py[i];
        haveCtl = true;
      }
    }
    if (haveCtl) {
      conic(path, cur, cx / 64f, cy / 64f, sx / 64f, sy / 64f);
    }
    path.closePath();
  }

  private static void conic(java.awt.geom.Path2D.Float path, float[] cur, float cx, float cy, float ex, float ey) {
    int x0 = Math.round(cur[0] * 256f);
    int y0 = Math.round(cur[1] * 256f);
    int x1 = Math.round(cx * 256f);
    int y1 = Math.round(cy * 256f);
    int x2 = Math.round(ex * 256f);
    int y2 = Math.round(ey * 256f);
    int dx = Math.max(Math.abs(x0 + x2 - 2 * x1), Math.abs(y0 + y2 - 2 * y1));
    int level = 0;
    if (dx >= 64) {
      do {
        dx >>= 2;
        level++;
      } while (dx > 64);
    }
    splitConic(path, x0, y0, x1, y1, x2, y2, level);
    cur[0] = ex;
    cur[1] = ey;
  }

  private static void splitConic(java.awt.geom.Path2D.Float path, int x0, int y0, int x1, int y1, int x2, int y2,
      int level) {
    if (level == 0) {
      path.lineTo(x2 / 256f, y2 / 256f);
      return;
    }
    int ax = x0 + x1;
    int bx = x1 + x2;
    int ay = y0 + y1;
    int by = y1 + y2;
    int mx = (ax + bx) >> 2;
    int my = (ay + by) >> 2;
    splitConic(path, x0, y0, ax >> 1, ay >> 1, mx, my, level - 1);
    splitConic(path, mx, my, bx >> 1, by >> 1, x2, y2, level - 1);
  }

  private Sized sized(int ppem, float size) {
    int key = ppem * 4096 + (integerPpem ? 0 : Math.round(size * 64) & 4095);
    Sized s = sizes.get(key);
    if (s != null) {
      return s.ppem < 0 ? null : s;
    }
    if (!fpgmDone && !runFontProgram()) {
      valid = false;
      return null;
    }
    s = new Sized();
    s.ppem = ppem;
    long pixels = integerPpem ? ppem * 64L : Math.round(size * 64f);
    s.scale = (int) divFix(pixels, unitsPerEm);
    s.cvt = new int[cvtFu.length];
    for (int i = 0; i < cvtFu.length; i++) {
      s.cvt[i] = (int) mulFix(cvtFu[i], s.scale);
    }
    s.storage = new int[Math.max(maxStorage, 1)];
    s.twilight = new Zone(maxTwilight + 4);
    s.gs = new State();
    sizes.put(key, s);
    sz = s;
    gs = new State();
    twilight = s.twilight;
    pts = new Zone(0);
    cvt = s.cvt;
    xScale = s.scale;
    composite = false;
    inPrep = true;
    zp0 = zp1 = zp2 = pts;
    computeFuncs();
    resetStack();
    if (prep.length > 0) {
      try {
        execute(prep);
      } catch (RuntimeException e) {
        // errors in the control value program are tolerated
      }
    }
    inPrep = false;
    gs.dualX = 0x4000;
    gs.dualY = 0;
    gs.projX = 0x4000;
    gs.projY = 0;
    gs.freeX = 0x4000;
    gs.freeY = 0;
    gs.rp0 = gs.rp1 = gs.rp2 = 0;
    gs.gep0 = gs.gep1 = gs.gep2 = 1;
    gs.loop = 1;
    s.gs = gs.copy();
    return s;
  }

  private boolean runFontProgram() {
    sz = new Sized();
    sz.scale = 0;
    sz.cvt = new int[0];
    sz.storage = new int[Math.max(maxStorage, 1)];
    sz.twilight = new Zone(maxTwilight + 4);
    gs = new State();
    twilight = sz.twilight;
    pts = new Zone(0);
    cvt = sz.cvt;
    xScale = 0;
    zp0 = zp1 = zp2 = pts;
    funcs = new Def[Math.max(maxFuncs, 1) + 256];
    resetStack();
    fpgmDone = true;
    try {
      execute(fpgm);
    } catch (RuntimeException e) {
      return false;
    }
    return true;
  }

  private void resetStack() {
    top = 0;
    frames = 0;
    steps = 0;
  }

  private int computeAdvance(Sized s, int glyph) {
    sz = s;
    cvt = s.cvt;
    twilight = s.twilight;
    xScale = s.scale;
    inPrep = false;
    if ((s.gs.instructControl & 1) != 0) {
      Loaded l = load(glyph, 0, false);
      return (int) (((long) l.pp2 - l.pp1 + 32) >> 6);
    }
    Loaded l = load(glyph, 0, true);
    long adv = (long) l.pp2 - l.pp1;
    return (int) ((adv + 32) >> 6);
  }

  private int glyphOffset(int g, int[] len) {
    int a;
    int b;
    if (locaFormat == 0) {
      a = u16(loca + g * 2) * 2;
      b = u16(loca + g * 2 + 2) * 2;
    } else {
      a = (int) u32(loca + g * 4);
      b = (int) u32(loca + g * 4 + 4);
    }
    len[0] = b - a;
    return glyf + a;
  }

  private Loaded load(int g, int depth, boolean hint) {
    if (g < 0 || g >= numGlyphs || depth > 8) {
      throw new IllegalStateException();
    }
    int[] len = new int[1];
    int o = glyphOffset(g, len);
    int advFu = u16(hmtx + Math.min(g, numHMetrics - 1) * 4);
    int lsbFu = g < numHMetrics ? s16(hmtx + g * 4 + 2) : s16(hmtx + numHMetrics * 4 + (g - numHMetrics) * 2);
    int xMin = len[0] > 0 ? s16(o + 2) : 0;
    int pp1Fu = xMin - lsbFu;
    int pp2Fu = pp1Fu + advFu;
    if (len[0] <= 0) {
      Loaded l = new Loaded();
      l.n = 0;
      l.x = new int[0];
      l.y = new int[0];
      l.tags = new byte[0];
      l.contours = new int[0];
      l.pp1 = (int) mulFix(pp1Fu, xScale);
      l.pp2 = (int) mulFix(pp2Fu, xScale);
      if (hint) {
        l.pp1 = (l.pp1 + 32) & ~63;
        l.pp2 = (l.pp2 + 32) & ~63;
      }
      return l;
    }
    int contours = s16(o);
    return contours >= 0 ? loadSimple(o, contours, pp1Fu, pp2Fu, hint) : loadComposite(o, pp1Fu, pp2Fu, depth, hint);
  }

  private Loaded loadSimple(int o, int contours, int pp1Fu, int pp2Fu, boolean hint) {
    int p = o + 10;
    int[] ends = new int[contours];
    for (int i = 0; i < contours; i++) {
      ends[i] = u16(p);
      p += 2;
    }
    int npts = contours == 0 ? 0 : ends[contours - 1] + 1;
    int insLen = u16(p);
    int insAt = p + 2;
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
    Zone z = new Zone(npts + 4);
    z.contours = ends;
    z.nContours = contours;
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
      z.orusX[i] = v;
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
      z.orusY[i] = v;
      z.tags[i] = (byte) (flags[i] & 1);
    }
    z.orusX[npts] = pp1Fu;
    z.orusX[npts + 1] = pp2Fu;
    for (int i = 0; i < npts + 4; i++) {
      z.curX[i] = (int) mulFix(z.orusX[i], xScale);
      z.curY[i] = (int) mulFix(z.orusY[i], xScale);
    }
    if (hint) {
      byte[] ins = java.util.Arrays.copyOfRange(d, insAt, insAt + insLen);
      hintZone(z, ins, false);
    }
    return finish(z, npts);
  }

  private Loaded finish(Zone z, int npts) {
    Loaded l = new Loaded();
    l.n = npts;
    l.x = z.curX;
    l.y = z.curY;
    l.tags = z.tags;
    l.contours = z.contours;
    l.pp1 = z.curX[npts];
    l.pp2 = z.curX[npts + 1];
    return l;
  }

  private void hintZone(Zone z, byte[] ins, boolean isComposite) {
    int n = z.n;
    System.arraycopy(z.curX, 0, z.orgX, 0, n);
    System.arraycopy(z.curY, 0, z.orgY, 0, n);
    if (isComposite) {
      System.arraycopy(z.curX, 0, z.orusX, 0, n);
      System.arraycopy(z.curY, 0, z.orusY, 0, n);
    }
    z.curX[n - 4] = (z.curX[n - 4] + 32) & ~63;
    z.curX[n - 3] = (z.curX[n - 3] + 32) & ~63;
    z.curY[n - 2] = (z.curY[n - 2] + 32) & ~63;
    z.curY[n - 1] = (z.curY[n - 1] + 32) & ~63;
    if (ins.length == 0) {
      return;
    }
    gs = sz.gs.copy();
    if ((gs.instructControl & 2) != 0) {
      int ic = gs.instructControl;
      gs = new State();
      gs.instructControl = ic;
    }
    pts = z;
    zp0 = zp1 = zp2 = z;
    computeFuncs();
    composite = isComposite;
    int savedScale = xScale;
    if (isComposite) {
      xScale = 1 << 16;
    }
    int[] savedCvt = cvt;
    cvt = cvt.clone();
    resetStack();
    try {
      execute(ins);
    } catch (RuntimeException e) {
      // hinting errors keep the points as they were
    }
    cvt = savedCvt;
    xScale = savedScale;
    composite = false;
  }

  private Loaded loadComposite(int o, int pp1Fu, int pp2Fu, int depth, boolean hint) {
    int p = o + 10;
    int[] xs = new int[0];
    int[] ys = new int[0];
    byte[] tags = new byte[0];
    int[] ends = new int[0];
    int total = 0;
    int pp1 = (int) mulFix(pp1Fu, xScale);
    int pp2 = (int) mulFix(pp2Fu, xScale);
    int insAt = -1;
    int insLen = 0;
    while (true) {
      int fl = u16(p);
      int gi = u16(p + 2);
      p += 4;
      int a1;
      int a2;
      if ((fl & 1) != 0) {
        if ((fl & 2) != 0) {
          a1 = s16(p);
          a2 = s16(p + 2);
        } else {
          a1 = u16(p);
          a2 = u16(p + 2);
        }
        p += 4;
      } else {
        if ((fl & 2) != 0) {
          a1 = d[p];
          a2 = d[p + 1];
        } else {
          a1 = d[p] & 0xff;
          a2 = d[p + 1] & 0xff;
        }
        p += 2;
      }
      long xx = 0x10000;
      long xy = 0;
      long yx = 0;
      long yy = 0x10000;
      boolean transformed = false;
      if ((fl & 8) != 0) {
        xx = yy = s16(p) * 4L;
        p += 2;
        transformed = true;
      } else if ((fl & 0x40) != 0) {
        xx = s16(p) * 4L;
        yy = s16(p + 2) * 4L;
        p += 4;
        transformed = true;
      } else if ((fl & 0x80) != 0) {
        xx = s16(p) * 4L;
        yx = s16(p + 2) * 4L;
        xy = s16(p + 4) * 4L;
        yy = s16(p + 6) * 4L;
        p += 8;
        transformed = true;
      }
      Loaded c = load(gi, depth + 1, hint);
      int n = c.n;
      int[] cx = c.x.clone();
      int[] cy = c.y.clone();
      if (transformed) {
        for (int i = 0; i < n; i++) {
          long nx = mulFix(cx[i], xx) + mulFix(cy[i], xy);
          long ny = mulFix(cx[i], yx) + mulFix(cy[i], yy);
          cx[i] = (int) nx;
          cy[i] = (int) ny;
        }
      }
      long dx;
      long dy;
      if ((fl & 2) != 0) {
        dx = mulFix(a1, xScale);
        dy = mulFix(a2, xScale);
        if (transformed && (fl & 0x800) != 0 && (fl & 0x1000) == 0) {
          dx = mulFix(dx, (long) Math.hypot(xx, xy));
          dy = mulFix(dy, (long) Math.hypot(yy, yx));
        }
        if (hint && (fl & 4) != 0) {
          dx = (dx + 32) & ~63;
          dy = (dy + 32) & ~63;
        }
      } else if (a1 >= 0 && a1 < total && a2 >= 0 && a2 < n) {
        dx = xs[a1] - cx[a2];
        dy = ys[a1] - cy[a2];
      } else {
        dx = 0;
        dy = 0;
      }
      int base = total;
      int[] nxs = java.util.Arrays.copyOf(xs, total + n);
      int[] nys = java.util.Arrays.copyOf(ys, total + n);
      byte[] ntags = java.util.Arrays.copyOf(tags, total + n);
      for (int i = 0; i < n; i++) {
        nxs[base + i] = (int) (cx[i] + dx);
        nys[base + i] = (int) (cy[i] + dy);
        ntags[base + i] = (byte) (c.tags[i] & kOn);
      }
      int cc = c.contours.length;
      int[] nends = java.util.Arrays.copyOf(ends, ends.length + cc);
      for (int i = 0; i < cc; i++) {
        nends[ends.length + i] = c.contours[i] + base;
      }
      xs = nxs;
      ys = nys;
      tags = ntags;
      ends = nends;
      total += n;
      if ((fl & 0x200) != 0) {
        pp1 = c.pp1;
        pp2 = c.pp2;
      }
      if ((fl & 0x20) == 0) {
        if ((fl & 0x100) != 0) {
          insLen = u16(p);
          insAt = p + 2;
        }
        break;
      }
    }
    Zone z = new Zone(total + 4);
    System.arraycopy(xs, 0, z.curX, 0, total);
    System.arraycopy(ys, 0, z.curY, 0, total);
    System.arraycopy(tags, 0, z.tags, 0, total);
    z.contours = ends;
    z.nContours = ends.length;
    z.curX[total] = pp1;
    z.curX[total + 1] = pp2;
    if (hint) {
      byte[] ins = insAt >= 0 ? java.util.Arrays.copyOfRange(d, insAt, insAt + insLen) : new byte[0];
      hintZone(z, ins, true);
    }
    return finish(z, total);
  }

  // ---- interpreter ----

  private void push(int v) {
    stack[top++] = v;
  }

  private int pop() {
    if (top <= 0) {
      throw new IllegalStateException("stack underflow");
    }
    return stack[--top];
  }

  private Zone zone(int i) {
    if (i == 0) {
      return twilight;
    }
    if (i == 1) {
      return pts;
    }
    throw new IllegalStateException("bad zone");
  }

  private int project(long dx, long dy) {
    if (gs.projX == 0x4000) {
      return (int) dx;
    }
    if (gs.projY == 0x4000) {
      return (int) dy;
    }
    return dot14(dx, dy, gs.projX, gs.projY);
  }

  private int dualProject(long dx, long dy) {
    if (gs.dualX == 0x4000) {
      return (int) dx;
    }
    if (gs.dualY == 0x4000) {
      return (int) dy;
    }
    return dot14(dx, dy, gs.dualX, gs.dualY);
  }

  private void computeFuncs() {
    if (gs.freeX == 0x4000) {
      fDotP = gs.projX;
    } else if (gs.freeY == 0x4000) {
      fDotP = gs.projY;
    } else {
      fDotP = (gs.projX * gs.freeX + gs.projY * gs.freeY) >> 14;
    }
    if (Math.abs(fDotP) < 0x400) {
      fDotP = 0x4000;
    }
  }

  private void move(Zone z, int p, long dist) {
    if (gs.freeX != 0) {
      z.curX[p] += (int) mulDiv(dist, gs.freeX, fDotP);
      z.tags[p] |= kTouchX;
    }
    if (gs.freeY != 0) {
      z.curY[p] += (int) mulDiv(dist, gs.freeY, fDotP);
      z.tags[p] |= kTouchY;
    }
  }

  private void moveOrg(Zone z, int p, long dist) {
    if (gs.freeX != 0) {
      z.orgX[p] += (int) mulDiv(dist, gs.freeX, fDotP);
    }
    if (gs.freeY != 0) {
      z.orgY[p] += (int) mulDiv(dist, gs.freeY, fDotP);
    }
  }

  private void moveZp2(int p, long dx, long dy, boolean touch) {
    if (gs.freeX != 0) {
      zp2.curX[p] += (int) dx;
      if (touch) {
        zp2.tags[p] |= kTouchX;
      }
    }
    if (gs.freeY != 0) {
      zp2.curY[p] += (int) dy;
      if (touch) {
        zp2.tags[p] |= kTouchY;
      }
    }
  }

  private int round(int dist, int comp) {
    int val;
    switch (gs.round) {
    case 1:
      if (dist >= 0) {
        val = (dist + comp + 32) & ~63;
        return val < 0 ? 0 : val;
      }
      val = -((comp - dist + 32) & ~63);
      return val > 0 ? 0 : val;
    case 2:
      if (dist >= 0) {
        val = ((dist + comp) & ~63) + 32;
        return val < 0 ? 32 : val;
      }
      val = -(((comp - dist) & ~63) + 32);
      return val > 0 ? -32 : val;
    case 3:
      if (dist >= 0) {
        val = (dist + comp + 16) & ~31;
        return val < 0 ? 0 : val;
      }
      val = -((comp - dist + 16) & ~31);
      return val > 0 ? 0 : val;
    case 4:
      if (dist >= 0) {
        val = (dist + comp) & ~63;
        return val < 0 ? 0 : val;
      }
      val = -((comp - dist) & ~63);
      return val > 0 ? 0 : val;
    case 5:
      if (dist >= 0) {
        val = (dist + comp + 63) & ~63;
        return val < 0 ? 0 : val;
      }
      val = -((comp - dist + 63) & ~63);
      return val > 0 ? 0 : val;
    case 6:
      return roundNone(dist, comp);
    case 7:
      if (dist >= 0) {
        val = ((dist + threshold - phase + comp) & -period) + phase;
        return val < 0 ? phase : val;
      }
      val = -((threshold - phase + comp - dist) & -period) - phase;
      return val > 0 ? -phase : val;
    default:
      if (dist >= 0) {
        val = ((dist + threshold - phase + comp) / period) * period + phase;
        return val < 0 ? phase : val;
      }
      val = -(((threshold - phase + comp - dist) / period) * period) - phase;
      return val > 0 ? -phase : val;
    }
  }

  private static int roundNone(int dist, int comp) {
    if (dist >= 0) {
      int v = dist + comp;
      return v < 0 ? 0 : v;
    }
    int v = dist - comp;
    return v > 0 ? 0 : v;
  }

  private void setSuperRound(int gridPeriod, int sel) {
    switch (sel & 0xC0) {
    case 0:
      period = gridPeriod / 2;
      break;
    case 0x40:
      period = gridPeriod;
      break;
    case 0x80:
      period = gridPeriod * 2;
      break;
    default:
      period = gridPeriod;
      break;
    }
    switch (sel & 0x30) {
    case 0:
      phase = 0;
      break;
    case 0x10:
      phase = period / 4;
      break;
    case 0x20:
      phase = period / 2;
      break;
    default:
      phase = period * 3 / 4;
      break;
    }
    if ((sel & 0x0F) == 0) {
      threshold = period - 1;
    } else {
      threshold = ((sel & 0x0F) - 4) * period / 8;
    }
    period >>= 8;
    phase >>= 8;
    threshold >>= 8;
  }

  private static void normalize(long x, long y, int[] out) {
    if (x == 0 && y == 0) {
      return;
    }
    double len = Math.sqrt((double) x * x + (double) y * y);
    long vx = Math.round(x / len * 65536.0);
    long vy = Math.round(y / len * 65536.0);
    out[0] = (int) (vx / 4);
    out[1] = (int) (vy / 4);
  }

  private int ppem() {
    return sz.ppem;
  }

  private int readCvt(int i) {
    return cvt[i];
  }

  private static int opLength(byte[] c, int ip) {
    int op = c[ip] & 0xff;
    if (op == 0x40) {
      return 2 + (c[ip + 1] & 0xff);
    }
    if (op == 0x41) {
      return 2 + 2 * (c[ip + 1] & 0xff);
    }
    if (op >= 0xB0 && op <= 0xB7) {
      return 2 + (op - 0xB0);
    }
    if (op >= 0xB8 && op <= 0xBF) {
      return 1 + 2 * (op - 0xB8 + 1);
    }
    return 1;
  }

  private int skipTo(byte[] code, int ip, boolean stopAtElse) {
    int nest = 1;
    while (true) {
      ip += opLength(code, ip);
      if (ip >= code.length) {
        throw new IllegalStateException("unterminated if");
      }
      int op = code[ip] & 0xff;
      if (op == 0x58) {
        nest++;
      } else if (op == 0x1B) {
        if (nest == 1 && stopAtElse) {
          return ip;
        }
      } else if (op == 0x59) {
        nest--;
        if (nest == 0) {
          return ip;
        }
      }
    }
  }

  private void execute(byte[] initial) {
    byte[] code = initial;
    int ip = 0;
    int frameBase = frames;
    int[] v2 = new int[2];
    while (true) {
      if (ip >= code.length) {
        if (frames > frameBase) {
          throw new IllegalStateException("fell off function");
        }
        return;
      }
      if (++steps > kMaxSteps) {
        throw new IllegalStateException("too many steps");
      }
      int op = code[ip] & 0xff;
      int len = 1;
      switch (op) {
      case 0x00:
      case 0x01:
        gs.projX = gs.dualX = gs.freeX = op == 0 ? 0 : 0x4000;
        gs.projY = gs.dualY = gs.freeY = op == 0 ? 0x4000 : 0;
        computeFuncs();
        break;
      case 0x02:
      case 0x03:
        gs.projX = gs.dualX = op == 2 ? 0 : 0x4000;
        gs.projY = gs.dualY = op == 2 ? 0x4000 : 0;
        computeFuncs();
        break;
      case 0x04:
      case 0x05:
        gs.freeX = op == 4 ? 0 : 0x4000;
        gs.freeY = op == 4 ? 0x4000 : 0;
        computeFuncs();
        break;
      case 0x06:
      case 0x07:
      case 0x08:
      case 0x09: {
        int a1 = pop();
        int a0 = pop();
        // a0 is the deeper value (point in zp2 is a1)
        long ax = zp1.curX[a0] - zp2.curX[a1];
        long ay = zp1.curY[a0] - zp2.curY[a1];
        // FreeType: aIdx1 = args[1], aIdx2 = args[0]; p1 = zp1[aIdx2], p2 = zp2[aIdx1]
        boolean perp = (op & 1) != 0;
        if (ax == 0 && ay == 0) {
          ax = 0x4000;
          perp = false;
        }
        if (perp) {
          long c = ay;
          ay = ax;
          ax = -c;
        }
        v2[0] = 0;
        v2[1] = 0;
        normalize(ax, ay, v2);
        if (op < 0x08) {
          gs.projX = gs.dualX = v2[0];
          gs.projY = gs.dualY = v2[1];
        } else {
          gs.freeX = v2[0];
          gs.freeY = v2[1];
        }
        computeFuncs();
        break;
      }
      case 0x0A: {
        int y = (short) pop();
        int x = (short) pop();
        v2[0] = gs.projX;
        v2[1] = gs.projY;
        normalize(x, y, v2);
        gs.projX = gs.dualX = v2[0];
        gs.projY = gs.dualY = v2[1];
        computeFuncs();
        break;
      }
      case 0x0B: {
        int y = (short) pop();
        int x = (short) pop();
        v2[0] = gs.freeX;
        v2[1] = gs.freeY;
        normalize(x, y, v2);
        gs.freeX = v2[0];
        gs.freeY = v2[1];
        computeFuncs();
        break;
      }
      case 0x0C:
        push(gs.projX);
        push(gs.projY);
        break;
      case 0x0D:
        push(gs.freeX);
        push(gs.freeY);
        break;
      case 0x0E:
        gs.freeX = gs.projX;
        gs.freeY = gs.projY;
        computeFuncs();
        break;
      case 0x0F: {
        int b1 = pop();
        int b0 = pop();
        int a1 = pop();
        int a0 = pop();
        int point = pop();
        long dbx = zp0.curX[b1] - zp0.curX[b0];
        long dby = zp0.curY[b1] - zp0.curY[b0];
        long dax = zp1.curX[a1] - zp1.curX[a0];
        long day = zp1.curY[a1] - zp1.curY[a0];
        long dx = zp0.curX[b0] - zp1.curX[a0];
        long dy = zp0.curY[b0] - zp1.curY[a0];
        zp2.tags[point] |= kTouchX | kTouchY;
        long disc = mulDiv(dax, -dby, 0x40) + mulDiv(day, dbx, 0x40);
        long dotp = mulDiv(dax, dbx, 0x40) + mulDiv(day, dby, 0x40);
        if (19 * Math.abs(disc) > Math.abs(dotp)) {
          long val = mulDiv(dx, -dby, 0x40) + mulDiv(dy, dbx, 0x40);
          zp2.curX[point] = (int) (zp1.curX[a0] + mulDiv(val, dax, disc));
          zp2.curY[point] = (int) (zp1.curY[a0] + mulDiv(val, day, disc));
        } else {
          zp2.curX[point] = (zp1.curX[a0] + zp1.curX[a1] + zp0.curX[b0] + zp0.curX[b1]) / 4;
          zp2.curY[point] = (zp1.curY[a0] + zp1.curY[a1] + zp0.curY[b0] + zp0.curY[b1]) / 4;
        }
        break;
      }
      case 0x10:
        gs.rp0 = pop();
        break;
      case 0x11:
        gs.rp1 = pop();
        break;
      case 0x12:
        gs.rp2 = pop();
        break;
      case 0x13: {
        int z = pop();
        zp0 = zone(z);
        gs.gep0 = z;
        break;
      }
      case 0x14: {
        int z = pop();
        zp1 = zone(z);
        gs.gep1 = z;
        break;
      }
      case 0x15: {
        int z = pop();
        zp2 = zone(z);
        gs.gep2 = z;
        break;
      }
      case 0x16: {
        int z = pop();
        zp0 = zp1 = zp2 = zone(z);
        gs.gep0 = gs.gep1 = gs.gep2 = z;
        break;
      }
      case 0x17: {
        int n = pop();
        if (n < 0) {
          throw new IllegalStateException();
        }
        gs.loop = Math.min(n, 0xFFFF);
        break;
      }
      case 0x18:
        gs.round = 1;
        break;
      case 0x19:
        gs.round = 2;
        break;
      case 0x1A:
        gs.minDist = pop();
        break;
      case 0x1B:
        ip = skipTo(code, ip, false);
        len = 1;
        break;
      case 0x1C: {
        int off = pop();
        if (off == 0 && top == 0) {
          throw new IllegalStateException();
        }
        ip += off;
        len = 0;
        if (ip < 0) {
          throw new IllegalStateException();
        }
        break;
      }
      case 0x1D:
        gs.cvtCutIn = pop();
        break;
      case 0x1E:
        gs.swCutIn = pop();
        break;
      case 0x1F:
        gs.swValue = (int) mulFix(pop(), sz.scale);
        break;
      case 0x20: {
        int a = pop();
        push(a);
        push(a);
        break;
      }
      case 0x21:
        pop();
        break;
      case 0x22:
        top = 0;
        break;
      case 0x23: {
        int a = pop();
        int b = pop();
        push(a);
        push(b);
        break;
      }
      case 0x24:
        push(top);
        break;
      case 0x25: {
        int k = pop();
        if (k <= 0 || k > top) {
          throw new IllegalStateException();
        }
        push(stack[top - k]);
        break;
      }
      case 0x26: {
        int k = pop();
        if (k <= 0 || k > top) {
          throw new IllegalStateException();
        }
        int val = stack[top - k];
        System.arraycopy(stack, top - k + 1, stack, top - k, k - 1);
        stack[top - 1] = val;
        break;
      }
      case 0x27: {
        int p2 = pop();
        int p1 = pop();
        long dist = project((long) zp0.curX[p2] - zp1.curX[p1], (long) zp0.curY[p2] - zp1.curY[p1]) / 2;
        move(zp1, p1, dist);
        move(zp0, p2, -dist);
        break;
      }
      case 0x29: {
        int p = pop();
        int mask = 0xFF;
        if (gs.freeX != 0) {
          mask &= ~kTouchX;
        }
        if (gs.freeY != 0) {
          mask &= ~kTouchY;
        }
        zp0.tags[p] &= (byte) mask;
        break;
      }
      case 0x2A:
      case 0x2B: {
        int f = pop();
        int count = op == 0x2A ? pop() : 1;
        if (f < 0 || f >= funcs.length || funcs[f] == null || !funcs[f].active) {
          throw new IllegalStateException("undefined function");
        }
        if (op == 0x2A && count <= 0) {
          break;
        }
        if (frames >= frameCode.length) {
          throw new IllegalStateException("call stack overflow");
        }
        frameCode[frames] = code;
        frameIp[frames] = ip + 1;
        frameCount[frames] = count;
        frameDef[frames] = funcs[f];
        frames++;
        code = funcs[f].code;
        ip = funcs[f].start;
        len = 0;
        break;
      }
      case 0x2C: {
        int f = pop();
        if (f < 0 || f >= funcs.length) {
          throw new IllegalStateException();
        }
        Def def = new Def();
        def.code = code;
        def.start = ip + 1;
        int scan = ip;
        while (true) {
          scan += opLength(code, scan);
          if (scan >= code.length) {
            throw new IllegalStateException();
          }
          int o2 = code[scan] & 0xff;
          if (o2 == 0x2C || o2 == 0x89) {
            throw new IllegalStateException("nested definition");
          }
          if (o2 == 0x2D) {
            break;
          }
        }
        def.end = scan;
        def.active = true;
        funcs[f] = def;
        ip = scan;
        break;
      }
      case 0x2D: {
        if (frames <= frameBase) {
          throw new IllegalStateException("ENDF outside function");
        }
        int fi = frames - 1;
        frameCount[fi]--;
        if (frameCount[fi] > 0) {
          ip = frameDef[fi].start;
          len = 0;
        } else {
          code = frameCode[fi];
          ip = frameIp[fi];
          frames--;
          len = 0;
        }
        break;
      }
      case 0x2E:
      case 0x2F: {
        int p = pop();
        long dist = 0;
        if ((op & 1) != 0) {
          int cur = project(zp0.curX[p], zp0.curY[p]);
          dist = round(cur, 0) - cur;
        }
        move(zp0, p, dist);
        gs.rp0 = gs.rp1 = p;
        break;
      }
      case 0x30:
      case 0x31:
        iup((op & 1) != 0);
        break;
      case 0x32:
      case 0x33: {
        long[] disp = displacement(op);
        for (; gs.loop > 0; gs.loop--) {
          int p = pop();
          moveZp2(p, disp[0], disp[1], true);
        }
        gs.loop = 1;
        break;
      }
      case 0x34:
      case 0x35: {
        int contour = pop();
        long[] disp = displacement(op);
        int start = contour == 0 ? 0 : zp2.contours[contour - 1] + 1;
        int limit = gs.gep2 == 0 ? zp2.n : zp2.contours[contour] + 1;
        Zone refZone = (op & 1) != 0 ? zp0 : zp1;
        int refp = (op & 1) != 0 ? gs.rp1 : gs.rp2;
        for (int i = start; i < limit; i++) {
          if (refZone != zp2 || refp != i) {
            moveZp2(i, disp[0], disp[1], true);
          }
        }
        break;
      }
      case 0x36:
      case 0x37: {
        int z = pop();
        zone(z);
        long[] disp = displacement(op);
        int limit;
        if (gs.gep2 == 0) {
          limit = zp2.n;
        } else if (zp2.nContours > 0) {
          limit = zp2.contours[zp2.nContours - 1] + 1;
        } else {
          limit = 0;
        }
        Zone refZone = (op & 1) != 0 ? zp0 : zp1;
        int refp = (op & 1) != 0 ? gs.rp1 : gs.rp2;
        for (int i = 0; i < limit; i++) {
          if (refZone != zp2 || refp != i) {
            moveZp2(i, disp[0], disp[1], false);
          }
        }
        break;
      }
      case 0x38: {
        long amount = pop();
        long dx = mulFix14(amount, gs.freeX);
        long dy = mulFix14(amount, gs.freeY);
        for (; gs.loop > 0; gs.loop--) {
          int p = pop();
          moveZp2(p, dx, dy, true);
        }
        gs.loop = 1;
        break;
      }
      case 0x39:
        ip(code);
        break;
      case 0x3A:
      case 0x3B: {
        long dist = pop();
        int point = pop();
        if (gs.gep1 == 0) {
          zp1.orgX[point] = zp0.orgX[gs.rp0];
          zp1.orgY[point] = zp0.orgY[gs.rp0];
          moveOrg(zp1, point, dist);
          zp1.curX[point] = zp1.orgX[point];
          zp1.curY[point] = zp1.orgY[point];
        }
        int cur = project((long) zp1.curX[point] - zp0.curX[gs.rp0], (long) zp1.curY[point] - zp0.curY[gs.rp0]);
        move(zp1, point, dist - cur);
        gs.rp1 = gs.rp0;
        gs.rp2 = point;
        if ((op & 1) != 0) {
          gs.rp0 = point;
        }
        break;
      }
      case 0x3C: {
        for (; gs.loop > 0; gs.loop--) {
          int p = pop();
          int dist = project((long) zp1.curX[p] - zp0.curX[gs.rp0], (long) zp1.curY[p] - zp0.curY[gs.rp0]);
          move(zp1, p, -dist);
        }
        gs.loop = 1;
        break;
      }
      case 0x3D:
        gs.round = 3;
        break;
      case 0x3E:
      case 0x3F: {
        int cvtEntry = pop();
        int point = pop();
        int distance = readCvt(cvtEntry);
        if (gs.gep0 == 0) {
          zp0.orgX[point] = mulFix14(distance, gs.freeX);
          zp0.orgY[point] = mulFix14(distance, gs.freeY);
          zp0.curX[point] = zp0.orgX[point];
          zp0.curY[point] = zp0.orgY[point];
        }
        int orgDist = project(zp0.curX[point], zp0.curY[point]);
        if ((op & 1) != 0) {
          if (Math.abs(distance - orgDist) > gs.cvtCutIn) {
            distance = orgDist;
          }
          distance = round(distance, 0);
        }
        move(zp0, point, distance - orgDist);
        gs.rp0 = gs.rp1 = point;
        break;
      }
      case 0x40: {
        int n = code[ip + 1] & 0xff;
        for (int i = 0; i < n; i++) {
          push(code[ip + 2 + i] & 0xff);
        }
        len = 2 + n;
        break;
      }
      case 0x41: {
        int n = code[ip + 1] & 0xff;
        for (int i = 0; i < n; i++) {
          push((short) (((code[ip + 2 + 2 * i] & 0xff) << 8) | (code[ip + 3 + 2 * i] & 0xff)));
        }
        len = 2 + 2 * n;
        break;
      }
      case 0x42: {
        int val = pop();
        int idx = pop();
        sz.storage[idx] = val;
        break;
      }
      case 0x43:
        push(sz.storage[pop()]);
        break;
      case 0x44: {
        int val = pop();
        int idx = pop();
        cvt[idx] = val;
        break;
      }
      case 0x45:
        push(readCvt(pop()));
        break;
      case 0x46:
      case 0x47: {
        int p = pop();
        int r;
        if ((op & 1) != 0) {
          r = dualProject(zp2.orgX[p], zp2.orgY[p]);
        } else {
          r = project(zp2.curX[p], zp2.curY[p]);
        }
        push(r);
        break;
      }
      case 0x48: {
        int val = pop();
        int p = pop();
        int cur = project(zp2.curX[p], zp2.curY[p]);
        move(zp2, p, val - cur);
        if (gs.gep2 == 0) {
          zp2.orgX[p] = zp2.curX[p];
          zp2.orgY[p] = zp2.curY[p];
        }
        break;
      }
      case 0x49:
      case 0x4A: {
        int k = pop();
        int l = pop();
        int dist;
        if ((op & 1) != 0) {
          dist = project((long) zp0.curX[l] - zp1.curX[k], (long) zp0.curY[l] - zp1.curY[k]);
        } else if (gs.gep0 == 0 || gs.gep1 == 0) {
          dist = dualProject((long) zp0.orgX[l] - zp1.orgX[k], (long) zp0.orgY[l] - zp1.orgY[k]);
        } else {
          dist = dualProject((long) zp0.orusX[l] - zp1.orusX[k], (long) zp0.orusY[l] - zp1.orusY[k]);
          dist = (int) mulFix(dist, xScale);
        }
        push(dist);
        break;
      }
      case 0x4B:
      case 0x4C:
        push(ppem());
        break;
      case 0x4D:
        gs.autoFlip = true;
        break;
      case 0x4E:
        gs.autoFlip = false;
        break;
      case 0x4F:
        pop();
        break;
      case 0x50: {
        int b = pop();
        int a = pop();
        push(a < b ? 1 : 0);
        break;
      }
      case 0x51: {
        int b = pop();
        int a = pop();
        push(a <= b ? 1 : 0);
        break;
      }
      case 0x52: {
        int b = pop();
        int a = pop();
        push(a > b ? 1 : 0);
        break;
      }
      case 0x53: {
        int b = pop();
        int a = pop();
        push(a >= b ? 1 : 0);
        break;
      }
      case 0x54: {
        int b = pop();
        int a = pop();
        push(a == b ? 1 : 0);
        break;
      }
      case 0x55: {
        int b = pop();
        int a = pop();
        push(a != b ? 1 : 0);
        break;
      }
      case 0x56:
        push((round(pop(), 0) & 127) == 64 ? 1 : 0);
        break;
      case 0x57:
        push((round(pop(), 0) & 127) == 0 ? 1 : 0);
        break;
      case 0x58: {
        int c = pop();
        if (c == 0) {
          ip = skipTo(code, ip, true);
          len = 1;
        }
        break;
      }
      case 0x59:
        break;
      case 0x5A: {
        int b = pop();
        int a = pop();
        push(a != 0 && b != 0 ? 1 : 0);
        break;
      }
      case 0x5B: {
        int b = pop();
        int a = pop();
        push(a != 0 || b != 0 ? 1 : 0);
        break;
      }
      case 0x5C:
        push(pop() == 0 ? 1 : 0);
        break;
      case 0x5D:
      case 0x71:
      case 0x72: {
        int n = pop();
        for (int k = 1; k <= n; k++) {
          int a = pop();
          int b = pop();
          if (a >= 0 && a < zp0.n) {
            int c = (b & 0xF0) >> 4;
            if (op == 0x71) {
              c += 16;
            } else if (op == 0x72) {
              c += 32;
            }
            c += gs.deltaBase;
            if (c == ppem()) {
              int bb = (b & 0xF) - 8;
              if (bb >= 0) {
                bb++;
              }
              bb *= 1 << (6 - gs.deltaShift);
              move(zp0, a, bb);
            }
          }
        }
        break;
      }
      case 0x5E:
        gs.deltaBase = pop() & 0xFFFF;
        break;
      case 0x5F: {
        int v = pop();
        if (v < 0 || v > 6) {
          throw new IllegalStateException();
        }
        gs.deltaShift = v;
        break;
      }
      case 0x60: {
        int b = pop();
        int a = pop();
        push(a + b);
        break;
      }
      case 0x61: {
        int b = pop();
        int a = pop();
        push(a - b);
        break;
      }
      case 0x62: {
        int b = pop();
        int a = pop();
        if (b == 0) {
          throw new IllegalStateException();
        }
        push((int) mulDiv(a, 64, b));
        break;
      }
      case 0x63: {
        int b = pop();
        int a = pop();
        push((int) mulDiv(a, b, 64));
        break;
      }
      case 0x64:
        push(Math.abs(pop()));
        break;
      case 0x65:
        push(-pop());
        break;
      case 0x66:
        push(pop() & ~63);
        break;
      case 0x67:
        push((pop() + 63) & ~63);
        break;
      case 0x68:
      case 0x69:
      case 0x6A:
      case 0x6B:
        push(round(pop(), 0));
        break;
      case 0x6C:
      case 0x6D:
      case 0x6E:
      case 0x6F:
        push(roundNone(pop(), 0));
        break;
      case 0x70: {
        int val = pop();
        int idx = pop();
        cvt[idx] = (int) mulFix(val, sz.scale);
        break;
      }
      case 0x73:
      case 0x74:
      case 0x75: {
        int n = pop();
        for (int k = 1; k <= n; k++) {
          int a = pop();
          int b = pop();
          int c = (b & 0xF0) >> 4;
          if (op == 0x74) {
            c += 16;
          } else if (op == 0x75) {
            c += 32;
          }
          c += gs.deltaBase;
          if (c == ppem() && a >= 0 && a < cvt.length) {
            int bb = (b & 0xF) - 8;
            if (bb >= 0) {
              bb++;
            }
            bb *= 1 << (6 - gs.deltaShift);
            cvt[a] += bb;
          }
        }
        break;
      }
      case 0x76:
        setSuperRound(0x4000, pop());
        gs.round = 7;
        break;
      case 0x77:
        setSuperRound(0x2D41, pop());
        gs.round = 8;
        break;
      case 0x78: {
        int c = pop();
        int off = pop();
        if (c != 0) {
          ip += off;
          len = 0;
        }
        break;
      }
      case 0x79: {
        int c = pop();
        int off = pop();
        if (c == 0) {
          ip += off;
          len = 0;
        }
        break;
      }
      case 0x7A:
        gs.round = 6;
        break;
      case 0x7C:
        gs.round = 5;
        break;
      case 0x7D:
        gs.round = 4;
        break;
      case 0x7E:
      case 0x7F:
        pop();
        break;
      case 0x80: {
        for (; gs.loop > 0; gs.loop--) {
          int p = pop();
          pts.tags[p] ^= kOn;
        }
        gs.loop = 1;
        break;
      }
      case 0x81:
      case 0x82: {
        int k = pop();
        int l = pop();
        for (int i = l; i <= k; i++) {
          if (op == 0x81) {
            pts.tags[i] |= kOn;
          } else {
            pts.tags[i] &= ~kOn;
          }
        }
        break;
      }
      case 0x85:
      case 0x8D:
        pop();
        break;
      case 0x86:
      case 0x87: {
        int p1 = pop();
        int p2 = pop();
        long ax = zp1.orgX[p2] - zp2.orgX[p1];
        long ay = zp1.orgY[p2] - zp2.orgY[p1];
        boolean perp = (op & 1) != 0;
        if (ax == 0 && ay == 0) {
          ax = 0x4000;
          perp = false;
        }
        if (perp) {
          long c = ay;
          ay = ax;
          ax = -c;
        }
        v2[0] = gs.dualX;
        v2[1] = gs.dualY;
        normalize(ax, ay, v2);
        gs.dualX = v2[0];
        gs.dualY = v2[1];
        ax = zp1.curX[p2] - zp2.curX[p1];
        ay = zp1.curY[p2] - zp2.curY[p1];
        perp = (op & 1) != 0;
        if (ax == 0 && ay == 0) {
          ax = 0x4000;
          perp = false;
        }
        if (perp) {
          long c = ay;
          ay = ax;
          ax = -c;
        }
        v2[0] = gs.projX;
        v2[1] = gs.projY;
        normalize(ax, ay, v2);
        gs.projX = v2[0];
        gs.projY = v2[1];
        computeFuncs();
        break;
      }
      case 0x88: {
        int sel = pop();
        int k = 0;
        if ((sel & 1) != 0) {
          k = 40;
        }
        push(k);
        break;
      }
      case 0x89:
        throw new IllegalStateException("IDEF unsupported");
      case 0x8A: {
        int c = pop();
        int b = pop();
        int a = pop();
        push(b);
        push(c);
        push(a);
        break;
      }
      case 0x8B: {
        int b = pop();
        int a = pop();
        push(Math.max(a, b));
        break;
      }
      case 0x8C: {
        int b = pop();
        int a = pop();
        push(Math.min(a, b));
        break;
      }
      case 0x8E: {
        int selector = pop();
        int value = pop();
        if (selector >= 1 && selector <= 3) {
          int k = 1 << (selector - 1);
          if (inPrep && (value == 0 || value == k)) {
            gs.instructControl = (gs.instructControl & ~k) | value;
          }
        }
        break;
      }
      default:
        if (op >= 0xB0 && op <= 0xB7) {
          int n = op - 0xB0 + 1;
          for (int i = 0; i < n; i++) {
            push(code[ip + 1 + i] & 0xff);
          }
          len = 1 + n;
        } else if (op >= 0xB8 && op <= 0xBF) {
          int n = op - 0xB8 + 1;
          for (int i = 0; i < n; i++) {
            push((short) (((code[ip + 1 + 2 * i] & 0xff) << 8) | (code[ip + 2 + 2 * i] & 0xff)));
          }
          len = 1 + 2 * n;
        } else if (op >= 0xC0 && op <= 0xDF) {
          mdrp(op);
        } else if (op >= 0xE0) {
          mirp(op);
        } else {
          throw new IllegalStateException("bad opcode " + op);
        }
        break;
      }
      ip += len;
    }
  }

  private long[] displacement(int op) {
    Zone z;
    int p;
    if ((op & 1) != 0) {
      z = zp0;
      p = gs.rp1;
    } else {
      z = zp1;
      p = gs.rp2;
    }
    int dist = project((long) z.curX[p] - z.orgX[p], (long) z.curY[p] - z.orgY[p]);
    return new long[] {mulDiv(dist, gs.freeX, fDotP), mulDiv(dist, gs.freeY, fDotP)};
  }

  private void ip(byte[] code) {
    boolean twilightMode = gs.gep0 == 0 || gs.gep1 == 0 || gs.gep2 == 0;
    int[] baseX = twilightMode ? zp0.orgX : zp0.orusX;
    int[] baseY = twilightMode ? zp0.orgY : zp0.orusY;
    int rp1 = gs.rp1;
    int rp2 = gs.rp2;
    long oldRange;
    long curRange;
    if (rp1 < 0 || rp1 >= zp0.n || rp2 < 0 || rp2 >= zp1.n) {
      oldRange = 0;
      curRange = 0;
    } else {
      int[] oX = twilightMode ? zp1.orgX : zp1.orusX;
      int[] oY = twilightMode ? zp1.orgY : zp1.orusY;
      oldRange = dualProject((long) oX[rp2] - baseX[rp1], (long) oY[rp2] - baseY[rp1]);
      curRange = project((long) zp1.curX[rp2] - zp0.curX[rp1], (long) zp1.curY[rp2] - zp0.curY[rp1]);
    }
    int[] pX = twilightMode ? zp2.orgX : zp2.orusX;
    int[] pY = twilightMode ? zp2.orgY : zp2.orusY;
    for (; gs.loop > 0; gs.loop--) {
      int point = pop();
      if (point < 0 || point >= zp2.n) {
        throw new IllegalStateException();
      }
      long orgDist = dualProject((long) pX[point] - baseX[rp1], (long) pY[point] - baseY[rp1]);
      long curDist = project((long) zp2.curX[point] - zp0.curX[rp1], (long) zp2.curY[point] - zp0.curY[rp1]);
      long newDist;
      if (orgDist != 0) {
        newDist = oldRange != 0 ? mulDiv(orgDist, curRange, oldRange) : orgDist;
      } else {
        newDist = 0;
      }
      move(zp2, point, newDist - curDist);
    }
    gs.loop = 1;
  }

  private void mdrp(int op) {
    int point = pop();
    int orgDist;
    if (gs.gep0 == 0 || gs.gep1 == 0) {
      orgDist = dualProject((long) zp1.orgX[point] - zp0.orgX[gs.rp0], (long) zp1.orgY[point] - zp0.orgY[gs.rp0]);
    } else {
      orgDist = dualProject((long) zp1.orusX[point] - zp0.orusX[gs.rp0],
          (long) zp1.orusY[point] - zp0.orusY[gs.rp0]);
      orgDist = (int) mulFix(orgDist, xScale);
    }
    if (gs.swCutIn > 0 && orgDist < gs.swValue + gs.swCutIn && orgDist > gs.swValue - gs.swCutIn) {
      orgDist = orgDist >= 0 ? gs.swValue : -gs.swValue;
    }
    int distance;
    if ((op & 4) != 0) {
      distance = round(orgDist, 0);
    } else {
      distance = roundNone(orgDist, 0);
    }
    if ((op & 8) != 0) {
      if (orgDist >= 0) {
        if (distance < gs.minDist) {
          distance = gs.minDist;
        }
      } else if (distance > -gs.minDist) {
        distance = -gs.minDist;
      }
    }
    int cur = project((long) zp1.curX[point] - zp0.curX[gs.rp0], (long) zp1.curY[point] - zp0.curY[gs.rp0]);
    move(zp1, point, distance - cur);
    gs.rp1 = gs.rp0;
    gs.rp2 = point;
    if ((op & 16) != 0) {
      gs.rp0 = point;
    }
  }

  private void mirp(int op) {
    int cvtArg = pop();
    int point = pop();
    int cvtEntry = cvtArg + 1;
    if (cvtEntry < 0 || cvtEntry > cvt.length) {
      throw new IllegalStateException();
    }
    int cvtDist = cvtEntry == 0 ? 0 : readCvt(cvtEntry - 1);
    int delta = Math.abs(cvtDist - gs.swValue);
    if (delta < gs.swCutIn) {
      cvtDist = cvtDist >= 0 ? gs.swValue : -gs.swValue;
    }
    if (gs.gep1 == 0) {
      zp1.orgX[point] = zp0.orgX[gs.rp0] + mulFix14(cvtDist, gs.freeX);
      zp1.orgY[point] = zp0.orgY[gs.rp0] + mulFix14(cvtDist, gs.freeY);
      zp1.curX[point] = zp1.orgX[point];
      zp1.curY[point] = zp1.orgY[point];
    }
    int orgDist = dualProject((long) zp1.orgX[point] - zp0.orgX[gs.rp0], (long) zp1.orgY[point] - zp0.orgY[gs.rp0]);
    int curDist = project((long) zp1.curX[point] - zp0.curX[gs.rp0], (long) zp1.curY[point] - zp0.curY[gs.rp0]);
    if (gs.autoFlip && (orgDist ^ cvtDist) < 0) {
      cvtDist = -cvtDist;
    }
    int distance;
    if ((op & 4) != 0) {
      if (gs.gep0 == gs.gep1) {
        if (Math.abs(cvtDist - orgDist) > gs.cvtCutIn) {
          cvtDist = orgDist;
        }
      }
      distance = round(cvtDist, 0);
    } else {
      distance = roundNone(cvtDist, 0);
    }
    if ((op & 8) != 0) {
      if (orgDist >= 0) {
        if (distance < gs.minDist) {
          distance = gs.minDist;
        }
      } else if (distance > -gs.minDist) {
        distance = -gs.minDist;
      }
    }
    move(zp1, point, distance - curDist);
    gs.rp1 = gs.rp0;
    if ((op & 16) != 0) {
      gs.rp0 = point;
    }
    gs.rp2 = point;
  }

  private void iup(boolean xAxis) {
    if (pts.nContours == 0) {
      return;
    }
    int mask = xAxis ? kTouchX : kTouchY;
    int[] cur = xAxis ? pts.curX : pts.curY;
    int[] org = xAxis ? pts.orgX : pts.orgY;
    int[] orus = xAxis ? pts.orusX : pts.orusY;
    int point = 0;
    for (int contour = 0; contour < pts.nContours; contour++) {
      int end = pts.contours[contour];
      int first = point;
      if (end >= pts.n) {
        end = pts.n - 1;
      }
      while (point <= end && (pts.tags[point] & mask) == 0) {
        point++;
      }
      if (point <= end) {
        int firstTouched = point;
        int curTouched = point;
        point++;
        while (point <= end) {
          if ((pts.tags[point] & mask) != 0) {
            interpolate(cur, org, orus, curTouched + 1, point - 1, curTouched, point);
            curTouched = point;
          }
          point++;
        }
        if (curTouched == firstTouched) {
          int dx = cur[curTouched] - org[curTouched];
          if (dx != 0) {
            for (int i = first; i < curTouched; i++) {
              cur[i] += dx;
            }
            for (int i = curTouched + 1; i <= end; i++) {
              cur[i] += dx;
            }
          }
        } else {
          interpolate(cur, org, orus, curTouched + 1, end, curTouched, firstTouched);
          if (firstTouched > 0) {
            interpolate(cur, org, orus, first, firstTouched - 1, curTouched, firstTouched);
          }
        }
      }
      point = end + 1;
    }
  }

  private static void interpolate(int[] cur, int[] org, int[] orus, int p1, int p2, int ref1, int ref2) {
    if (p1 > p2) {
      return;
    }
    int orus1 = orus[ref1];
    int orus2 = orus[ref2];
    if (orus1 > orus2) {
      int t = orus1;
      orus1 = orus2;
      orus2 = t;
      t = ref1;
      ref1 = ref2;
      ref2 = t;
    }
    int org1 = org[ref1];
    int org2 = org[ref2];
    int cur1 = cur[ref1];
    int cur2 = cur[ref2];
    int delta1 = cur1 - org1;
    int delta2 = cur2 - org2;
    if (cur1 == cur2 || orus1 == orus2) {
      for (int i = p1; i <= p2; i++) {
        int x = org[i];
        if (x <= org1) {
          x += delta1;
        } else if (x >= org2) {
          x += delta2;
        } else {
          x = cur1;
        }
        cur[i] = x;
      }
    } else {
      long scale = 0;
      boolean valid = false;
      for (int i = p1; i <= p2; i++) {
        int x = org[i];
        if (x <= org1) {
          x += delta1;
        } else if (x >= org2) {
          x += delta2;
        } else {
          if (!valid) {
            valid = true;
            scale = divFix(cur2 - cur1, orus2 - orus1);
          }
          x = (int) (cur1 + mulFix(orus[i] - orus1, scale));
        }
        cur[i] = x;
      }
    }
  }
}
