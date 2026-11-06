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
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class Type1Face extends FontFace {
  private static final String[] kLatin1 = {"space", "exclamdown", "cent", "sterling", "currency", "yen", "brokenbar",
      "section", "dieresis", "copyright", "ordfeminine", "guillemotleft", "logicalnot", "sfthyphen", "registered",
      "macron", "degree", "plusminus", "twosuperior", "threesuperior", "acute", "mu", "paragraph", "periodcentered",
      "cedilla", "onesuperior", "ordmasculine", "guillemotright", "onequarter", "onehalf", "threequarters",
      "questiondown", "Agrave", "Aacute", "Acircumflex", "Atilde", "Adieresis", "Aring", "AE", "Ccedilla", "Egrave",
      "Eacute", "Ecircumflex", "Edieresis", "Igrave", "Iacute", "Icircumflex", "Idieresis", "Eth", "Ntilde", "Ograve",
      "Oacute", "Ocircumflex", "Otilde", "Odieresis", "multiply", "Oslash", "Ugrave", "Uacute", "Ucircumflex",
      "Udieresis", "Yacute", "Thorn", "germandbls", "agrave", "aacute", "acircumflex", "atilde", "adieresis", "aring",
      "ae", "ccedilla", "egrave", "eacute", "ecircumflex", "edieresis", "igrave", "iacute", "icircumflex",
      "idieresis", "eth", "ntilde", "ograve", "oacute", "ocircumflex", "otilde", "odieresis", "divide", "oslash",
      "ugrave", "uacute", "ucircumflex", "udieresis", "yacute", "thorn", "ydieresis"};
  private static final String[] kAscii = {"space", "exclam", "quotedbl", "numbersign", "dollar", "percent",
      "ampersand", "quotesingle", "parenleft", "parenright", "asterisk", "plus", "comma", "hyphen", "period", "slash",
      "zero", "one", "two", "three", "four", "five", "six", "seven", "eight", "nine", "colon", "semicolon", "less",
      "equal", "greater", "question", "at"};
  private static final String[] kAsciiTail = {"bracketleft", "backslash", "bracketright", "asciicircum", "underscore",
      "grave"};
  private static final String[] kAsciiEnd = {"braceleft", "bar", "braceright", "asciitilde"};
  private static final Object[][] kExtra = {{0x20AC, "Euro"}, {0x201A, "quotesinglbase"}, {0x0192, "florin"},
      {0x201E, "quotedblbase"}, {0x2026, "ellipsis"}, {0x2020, "dagger"}, {0x2021, "daggerdbl"}, {0x02C6, "circumflex"},
      {0x2030, "perthousand"}, {0x0160, "Scaron"}, {0x2039, "guilsinglleft"}, {0x0152, "OE"}, {0x017D, "Zcaron"},
      {0x2018, "quoteleft"}, {0x2019, "quoteright"}, {0x201C, "quotedblleft"}, {0x201D, "quotedblright"},
      {0x2022, "bullet"}, {0x2013, "endash"}, {0x2014, "emdash"}, {0x02DC, "tilde"}, {0x2122, "trademark"},
      {0x0161, "scaron"}, {0x203A, "guilsinglright"}, {0x0153, "oe"}, {0x017E, "zcaron"}, {0x0178, "Ydieresis"},
      {0x0131, "dotlessi"}, {0x0141, "Lslash"}, {0x0142, "lslash"}, {0x02D8, "breve"}, {0x02D9, "dotaccent"},
      {0x02DA, "ring"}, {0x02DB, "ogonek"}, {0x02DD, "hungarumlaut"}, {0x02C7, "caron"}, {0x2044, "fraction"},
      {0xFB01, "fi"}, {0xFB02, "fl"}, {0x2212, "minus"}, {0x2215, "divisionslash"}, {0x00A0, "nbspace"},
      {0x03A9, "Omega"}, {0x0394, "Delta"}, {0x03BC, "mu"}, {0x2202, "partialdiff"}, {0x2211, "summation"},
      {0x220F, "product"}, {0x03C0, "pi"}, {0x222B, "integral"}, {0x221A, "radical"}, {0x221E, "infinity"},
      {0x2248, "approxequal"}, {0x2260, "notequal"}, {0x2264, "lessequal"}, {0x2265, "greaterequal"},
      {0x25CA, "lozenge"}, {0x2206, "increment"}, {0x2126, "Ohm"}, {0x2116, "numero"}, {0x20A3, "franc"},
      {0x2190, "arrowleft"}, {0x2191, "arrowup"}, {0x2192, "arrowright"}, {0x2193, "arrowdown"}, {0x2195, "arrowupdn"}};

  private final String psName;
  private final String fullName;
  private final String family;
  private final String weight;
  private final float ascent;
  private final float descent;
  private final float leading;
  private final float height;
  private final float upem;
  private final Map<String, Integer> glyphByName = new HashMap<>();
  private final List<String> names = new ArrayList<>();
  private final List<byte[]> charstrings = new ArrayList<>();
  private final List<byte[]> subrs = new ArrayList<>();
  private final Map<Integer, Integer> cmap = new HashMap<>();
  private final Map<Integer, Integer> reverse = new HashMap<>();
  private final Map<Integer, Shape> shapes = new HashMap<>();
  private final Map<Integer, Float> widths = new HashMap<>();
  private final int lenIV;
  private final boolean italic;
  private final boolean bold;

  private Type1Face(byte[] clear, byte[] priv) throws FontFormatException {
    String head = new String(clear, StandardCharsets.ISO_8859_1);
    psName = nameToken(head, "/FontName");
    fullName = parenString(head, "/FullName");
    String fam = parenString(head, "/FamilyName");
    weight = parenString(head, "/Weight");
    float[] bbox = bbox(head);
    float[] matrix = matrix(head);
    upem = Math.abs(matrix[0]) > 0 ? Math.round(1f / matrix[0]) : 1000f;
    String full = fullName != null ? fullName : psName;
    family = fam != null ? fam : full;
    float asc = bbox[3];
    float desc = -bbox[1];
    ascent = asc / upem;
    descent = desc / upem;
    height = Math.max(upem * 1.2f, asc + desc);
    leading = Math.max(0f, (height - asc - desc) / upem);
    italic = floatAfter(head, "/ItalicAngle") != 0f;
    bold = weight != null && weight.toLowerCase().contains("bold");
    int liv = 4;
    String ps = new String(priv, StandardCharsets.ISO_8859_1);
    int li = ps.indexOf("/lenIV");
    if (li >= 0) {
      liv = (int) Float.parseFloat(ps.substring(li + 6, ps.indexOf("def", li)).trim());
    }
    lenIV = liv;
    readPrivate(priv, ps);
    if (charstrings.isEmpty()) {
      throw new FontFormatException("Unsupported font type");
    }
    int notdef = names.indexOf(".notdef");
    if (notdef > 0) {
      String n = names.get(0);
      byte[] c = charstrings.get(0);
      names.set(0, ".notdef");
      charstrings.set(0, charstrings.get(notdef));
      names.set(notdef, n);
      charstrings.set(notdef, c);
    }
    for (int i = 0; i < names.size(); i++) {
      glyphByName.put(names.get(i), i);
    }
    buildCmap();
  }

  public static Type1Face parse(byte[] data) throws FontFormatException {
    if (data.length < 2) {
      throw new FontFormatException("short file");
    }
    boolean pfb = (data[0] & 0xff) == 0x80;
    if (!pfb && !(data[0] == '%' && data[1] == '!')) {
      throw new FontFormatException("bad pfa font");
    }
    try {
      byte[] clear;
      byte[] enc;
      if (data.length > 6 && (data[0] & 0xff) == 0x80) {
        java.io.ByteArrayOutputStream a = new java.io.ByteArrayOutputStream();
        java.io.ByteArrayOutputStream b = new java.io.ByteArrayOutputStream();
        int i = 0;
        int phase = 0;
        while (i + 6 <= data.length && (data[i] & 0xff) == 0x80) {
          int t = data[i + 1];
          if (t == 3) {
            break;
          }
          int n = (data[i + 2] & 0xff) | (data[i + 3] & 0xff) << 8 | (data[i + 4] & 0xff) << 16
              | (data[i + 5] & 0xff) << 24;
          if (n < 0 || i + 6 + n > data.length) {
            throw new FontFormatException("java.lang.IndexOutOfBoundsException");
          }
          if (t == 1 && phase == 0) {
            a.write(data, i + 6, n);
          } else if (t == 2) {
            b.write(data, i + 6, n);
            phase = 1;
          }
          i += 6 + n;
        }
        clear = a.toByteArray();
        enc = b.toByteArray();
      } else if (data.length > 2 && data[0] == '%' && data[1] == '!') {
        String s = new String(data, StandardCharsets.ISO_8859_1);
        int e = s.indexOf("eexec");
        if (e < 0) {
          throw new FontFormatException("Unsupported font type");
        }
        int p = e + 5;
        while (p < data.length && (data[p] == ' ' || data[p] == '\r' || data[p] == '\n' || data[p] == '\t')) {
          p++;
        }
        clear = java.util.Arrays.copyOf(data, e);
        java.io.ByteArrayOutputStream b = new java.io.ByteArrayOutputStream();
        int hi = -1;
        for (; p < data.length; p++) {
          int c = Character.digit(data[p], 16);
          if (c < 0) {
            if (data[p] == ' ' || data[p] == '\r' || data[p] == '\n' || data[p] == '\t') {
              continue;
            }
            break;
          }
          if (hi < 0) {
            hi = c;
          } else {
            b.write(hi << 4 | c);
            hi = -1;
          }
        }
        enc = b.toByteArray();
      } else {
        throw new FontFormatException("Unsupported font type");
      }
      byte[] priv = decrypt(enc, 55665, 4);
      return new Type1Face(clear, priv);
    } catch (RuntimeException e) {
      throw new FontFormatException("Unsupported font type");
    }
  }

  private static byte[] decrypt(byte[] in, int key, int skip) {
    int r = key;
    byte[] out = new byte[Math.max(0, in.length - skip)];
    for (int i = 0; i < in.length; i++) {
      int c = in[i] & 0xff;
      int p = c ^ (r >> 8);
      r = ((c + r) * 52845 + 22719) & 0xffff;
      if (i >= skip) {
        out[i - skip] = (byte) p;
      }
    }
    return out;
  }

  private static String nameToken(String s, String key) {
    int i = s.indexOf(key);
    if (i < 0) {
      return null;
    }
    int p = s.indexOf('/', i + key.length());
    int e = p + 1;
    while (e < s.length() && !Character.isWhitespace(s.charAt(e))) {
      e++;
    }
    return s.substring(p + 1, e);
  }

  private static String parenString(String s, String key) {
    int i = s.indexOf(key);
    if (i < 0) {
      return null;
    }
    int p = s.indexOf('(', i);
    int depth = 0;
    int e = p;
    for (; e < s.length(); e++) {
      char c = s.charAt(e);
      if (c == '(') {
        depth++;
      } else if (c == ')') {
        depth--;
        if (depth == 0) {
          break;
        }
      }
    }
    return s.substring(p + 1, e);
  }

  private static float floatAfter(String s, String key) {
    int i = s.indexOf(key);
    if (i < 0) {
      return 0f;
    }
    String[] t = s.substring(i + key.length()).trim().split("\\s+");
    try {
      return Float.parseFloat(t[0]);
    } catch (NumberFormatException e) {
      return 0f;
    }
  }

  private static float[] numbers(String s, String key, char open, char close, int n, float[] def) {
    int i = s.indexOf(key);
    if (i < 0) {
      return def;
    }
    int a = s.indexOf(open, i);
    int b = s.indexOf(close, a);
    String[] t = s.substring(a + 1, b).trim().split("\\s+");
    float[] r = new float[n];
    for (int k = 0; k < n && k < t.length; k++) {
      r[k] = Float.parseFloat(t[k]);
    }
    return r;
  }

  private static float[] bbox(String s) {
    return numbers(s, "/FontBBox", '{', '}', 4, new float[] {0, -200, 1000, 800});
  }

  private static float[] matrix(String s) {
    return numbers(s, "/FontMatrix", '[', ']', 6, new float[] {0.001f, 0, 0, 0.001f, 0, 0});
  }

  private void readPrivate(byte[] priv, String ps) {
    int sp = ps.indexOf("/Subrs");
    int cs = ps.indexOf("/CharStrings");
    if (sp >= 0) {
      int count = (int) Float.parseFloat(ps.substring(sp + 6, ps.indexOf("array", sp)).trim());
      for (int i = 0; i < count; i++) {
        subrs.add(null);
      }
      int p = ps.indexOf("dup", sp);
      while (p >= 0 && (cs < 0 || p < cs)) {
        int[] pos = {p + 3};
        int idx = nextInt(ps, pos);
        int len = nextInt(ps, pos);
        int rd = pos[0];
        while (ps.charAt(rd) == ' ') {
          rd++;
        }
        int dataStart = ps.indexOf(' ', rd) + 1;
        if (idx >= 0 && idx < count) {
          subrs.set(idx, decryptCs(priv, dataStart, len));
        }
        p = ps.indexOf("dup", dataStart + len);
      }
    }
    if (cs >= 0) {
      int p = ps.indexOf("begin", cs);
      p = ps.indexOf('/', p);
      while (p >= 0 && p < ps.length()) {
        int e = p + 1;
        while (e < ps.length() && !Character.isWhitespace(ps.charAt(e))) {
          e++;
        }
        String name = ps.substring(p + 1, e);
        int[] pos = {e};
        int len = nextInt(ps, pos);
        if (len < 0) {
          break;
        }
        int rd = pos[0];
        while (ps.charAt(rd) == ' ') {
          rd++;
        }
        int dataStart = ps.indexOf(' ', rd) + 1;
        names.add(name);
        charstrings.add(decryptCs(priv, dataStart, len));
        int next = dataStart + len;
        p = ps.indexOf('/', next);
        int endIdx = ps.indexOf("end", next);
        if (endIdx >= 0 && (p < 0 || endIdx < p)) {
          int nl = ps.indexOf("ND", next);
          int nl2 = ps.indexOf("|-", next);
          int stop = Math.min(nl < 0 ? Integer.MAX_VALUE : nl, nl2 < 0 ? Integer.MAX_VALUE : nl2);
          if (stop > endIdx) {
            break;
          }
        }
      }
    }
  }

  private static int nextInt(String s, int[] pos) {
    int p = pos[0];
    while (p < s.length() && Character.isWhitespace(s.charAt(p))) {
      p++;
    }
    int st = p;
    while (p < s.length() && (Character.isDigit(s.charAt(p)) || s.charAt(p) == '-')) {
      p++;
    }
    pos[0] = p;
    if (st == p) {
      return -1;
    }
    return Integer.parseInt(s.substring(st, p));
  }

  private byte[] decryptCs(byte[] priv, int off, int len) {
    byte[] raw = java.util.Arrays.copyOfRange(priv, off, off + len);
    return decrypt(raw, 4330, Math.max(lenIV, 0));
  }

  private void buildCmap() {
    Map<String, Integer> unicodeByName = new HashMap<>();
    for (int i = 0; i < kAscii.length; i++) {
      unicodeByName.put(kAscii[i], 0x20 + i);
    }
    for (int i = 0; i < 26; i++) {
      unicodeByName.put(String.valueOf((char) ('A' + i)), 'A' + i);
      unicodeByName.put(String.valueOf((char) ('a' + i)), 'a' + i);
    }
    for (int i = 0; i < kAsciiTail.length; i++) {
      unicodeByName.put(kAsciiTail[i], 0x5B + i);
    }
    for (int i = 0; i < kAsciiEnd.length; i++) {
      unicodeByName.put(kAsciiEnd[i], 0x7B + i);
    }
    for (int i = 0; i < kLatin1.length; i++) {
      unicodeByName.put(kLatin1[i], 0xA0 + i);
    }
    unicodeByName.put("hyphen", 0x2D);
    unicodeByName.put("sfthyphen", 0xAD);
    unicodeByName.put("macron", 0xAF);
    unicodeByName.put("periodcentered", 0xB7);
    unicodeByName.put("middot", 0xB7);
    for (Object[] e : kExtra) {
      unicodeByName.put((String) e[1], (Integer) e[0]);
    }
    for (int i = 0; i < names.size(); i++) {
      String n = names.get(i);
      Integer u = unicodeByName.get(n);
      if (u == null && n.startsWith("uni") && n.length() == 7) {
        try {
          u = Integer.parseInt(n.substring(3), 16);
        } catch (NumberFormatException e) {
          u = null;
        }
      }
      if (u == null && n.startsWith("u") && n.length() >= 5 && n.length() <= 7) {
        try {
          u = Integer.parseInt(n.substring(1), 16);
        } catch (NumberFormatException e) {
          u = null;
        }
      }
      if (u != null && !n.equals(".notdef") && !cmap.containsKey(u)) {
        cmap.put(u, i);
      }
    }
    for (Map.Entry<Integer, Integer> e : cmap.entrySet()) {
      reverse.putIfAbsent(e.getValue(), e.getKey());
    }
  }

  private int glyphFor(int cp) {
    Integer g = cmap.get(cp);
    return g == null ? 0 : g;
  }

  public String family() { return family; }

  public String fullName() { return fullName; }

  public String postScriptName() { return psName; }

  public int fontStyle() { return 0; }

  public int unitsPerEm() { return (int) upem; }

  public boolean canDisplay(int cp) { return cmap.containsKey(cp); }

  public int numGlyphs() { return names.size(); }

  public int glyphCode(int cp) { return glyphFor(cp); }

  public int codePointOf(int glyph) {
    Integer cp = reverse.get(glyph);
    return cp == null ? 0 : cp;
  }

  public float ascent() { return ascent; }

  public float descent() { return descent; }

  public float leading() { return leading; }

  public float advance(int cp) {
    return width(glyphFor(cp)) / upem;
  }

  public float pixels(float emFraction, float size) {
    long scale = TrueTypeHinter.divFix(Math.round(size * 64f), (int) upem);
    float units = Math.round(emFraction * upem * 64f) / 64f;
    return (units * (float) scale) / 4194304f;
  }

  public float advancePixels(float emFraction, float size) {
    long scale = TrueTypeHinter.divFix(Math.round(size * 64f), (int) upem);
    long units = Math.round(emFraction * upem);
    return ((units * scale + 32) >> 6) / 65536f;
  }

  public float lineHeight() { return height / upem; }

  public boolean leadingFromHeight() { return true; }

  private float width(int g) {
    synchronized (shapes) {
      Float w = widths.get(g);
      if (w == null) {
        shape(g);
        w = widths.get(g);
      }
      return w == null ? 0f : w;
    }
  }

  public Shape outline(int cp, boolean bold, boolean italic) {
    Shape base = shape(glyphFor(cp));
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

  private Shape shape(int g) {
    synchronized (shapes) {
      Shape s = shapes.get(g);
      if (s != null) {
        return s;
      }
      Path2D.Float path = new Path2D.Float(Path2D.WIND_NON_ZERO);
      Interp in = new Interp(path);
      try {
        in.run(charstrings.get(g), 0);
        in.finish();
      } catch (RuntimeException e) {
        path.reset();
      }
      widths.put(g, in.width);
      shapes.put(g, path);
      return path;
    }
  }

  private final class Interp {
    final Path2D.Float path;
    final double[] stack = new double[48];
    int sp;
    final double[] ps = new double[32];
    int psp;
    double x;
    double y;
    double sbx;
    float width;
    boolean open;
    boolean flex;
    final double[] flexPts = new double[16];
    int flexCount;
    final double scale = 1.0 / upem;

    Interp(Path2D.Float path) {
      this.path = path;
    }

    void finish() {
      if (open) {
        path.closePath();
        open = false;
      }
    }

    private void moveTo(double dx, double dy) {
      x += dx;
      y += dy;
      if (flex) {
        if (flexCount < 7) {
          flexPts[flexCount * 2] = x;
          flexPts[flexCount * 2 + 1] = y;
          flexCount++;
        }
        return;
      }
      if (open) {
        path.closePath();
      }
      path.moveTo(px(x), py(y));
      open = true;
    }

    private void lineTo(double dx, double dy) {
      x += dx;
      y += dy;
      ensureOpen();
      path.lineTo(px(x), py(y));
    }

    private double px(double v) {
      return (v + offsetX) * scale;
    }

    private double py(double v) {
      return -(v + offsetY) * scale;
    }

    private void ensureOpen() {
      if (!open) {
        path.moveTo(px(x), py(y));
        open = true;
      }
    }

    private void curveTo(double dx1, double dy1, double dx2, double dy2, double dx3, double dy3) {
      ensureOpen();
      double x1 = x + dx1;
      double y1 = y + dy1;
      double x2 = x1 + dx2;
      double y2 = y1 + dy2;
      x = x2 + dx3;
      y = y2 + dy3;
      path.curveTo(px(x1), py(y1), px(x2), py(y2), px(x), py(y));
    }

    boolean run(byte[] cs, int depth) {
      if (depth > 10 || cs == null) {
        return false;
      }
      int i = 0;
      while (i < cs.length) {
        int v = cs[i++] & 0xff;
        if (v >= 32) {
          double n;
          if (v <= 246) {
            n = v - 139;
          } else if (v <= 250) {
            n = (v - 247) * 256 + (cs[i++] & 0xff) + 108;
          } else if (v <= 254) {
            n = -(v - 251) * 256 - (cs[i++] & 0xff) - 108;
          } else {
            n = (cs[i] << 24) | ((cs[i + 1] & 0xff) << 16) | ((cs[i + 2] & 0xff) << 8) | (cs[i + 3] & 0xff);
            i += 4;
          }
          stack[sp++] = n;
          continue;
        }
        switch (v) {
        case 1:
        case 3:
          sp = 0;
          break;
        case 4:
          moveTo(0, stack[sp - 1]);
          sp = 0;
          break;
        case 5:
          lineTo(stack[0], stack[1]);
          sp = 0;
          break;
        case 6:
          lineTo(stack[0], 0);
          sp = 0;
          break;
        case 7:
          lineTo(0, stack[0]);
          sp = 0;
          break;
        case 8:
          curveTo(stack[0], stack[1], stack[2], stack[3], stack[4], stack[5]);
          sp = 0;
          break;
        case 9:
          if (open) {
            path.closePath();
            open = false;
          }
          sp = 0;
          break;
        case 10: {
          int n = (int) stack[--sp];
          if (n >= 0 && n < subrs.size()) {
            if (run(subrs.get(n), depth + 1)) {
              return true;
            }
          }
          break;
        }
        case 11:
          return false;
        case 13:
          sbx = stack[0];
          width = (float) stack[1];
          x = sbx;
          y = 0;
          sp = 0;
          break;
        case 14:
          finish();
          return true;
        case 21:
          moveTo(stack[0], stack[1]);
          sp = 0;
          break;
        case 22:
          moveTo(stack[0], 0);
          sp = 0;
          break;
        case 30:
          curveTo(0, stack[0], stack[1], stack[2], stack[3], 0);
          sp = 0;
          break;
        case 31:
          curveTo(stack[0], 0, stack[1], stack[2], 0, stack[3]);
          sp = 0;
          break;
        case 12: {
          int e = cs[i++] & 0xff;
          switch (e) {
          case 0:
          case 1:
          case 2:
            sp = 0;
            break;
          case 6:
            seac(stack[0], stack[1], stack[2], (int) stack[3], (int) stack[4], depth);
            sp = 0;
            return true;
          case 7:
            sbx = stack[0];
            width = (float) stack[2];
            x = sbx;
            y = stack[1];
            sp = 0;
            break;
          case 12: {
            double b = stack[--sp];
            double a = stack[--sp];
            stack[sp++] = a / b;
            break;
          }
          case 16: {
            int othersubr = (int) stack[--sp];
            int n = (int) stack[--sp];
            if (othersubr == 1) {
              flex = true;
              flexCount = 0;
            } else if (othersubr == 2) {
              // flex point already recorded by rmoveto
            } else if (othersubr == 0) {
              flex = false;
              double endY = stack[--sp];
              double endX = stack[--sp];
              sp--;
              if (flexCount >= 7) {
                ensureOpen();
                path.curveTo(px(flexPts[2]), py(flexPts[3]), px(flexPts[4]), py(flexPts[5]), px(flexPts[6]),
                    py(flexPts[7]));
                path.curveTo(px(flexPts[8]), py(flexPts[9]), px(flexPts[10]), py(flexPts[11]), px(flexPts[12]),
                    py(flexPts[13]));
                x = flexPts[12];
                y = flexPts[13];
              }
              psp = 0;
              ps[psp++] = endY;
              ps[psp++] = endX;
              break;
            } else if (othersubr == 3) {
              psp = 0;
              ps[psp++] = stack[--sp];
              break;
            } else {
              psp = 0;
              for (int k = 0; k < n; k++) {
                ps[psp++] = stack[--sp];
              }
              break;
            }
            for (int k = 0; k < n; k++) {
              sp--;
            }
            break;
          }
          case 17:
            stack[sp++] = psp > 0 ? ps[--psp] : 0;
            break;
          case 33:
            x = stack[0];
            y = stack[1];
            sp = 0;
            break;
          default:
            sp = 0;
          }
          break;
        }
        default:
          sp = 0;
        }
      }
      return false;
    }

    private void seac(double asb, double adx, double ady, int bchar, int achar, int depth) {
      String bn = standardName(bchar);
      String an = standardName(achar);
      Integer bg = glyphByName.get(bn);
      Integer ag = glyphByName.get(an);
      double baseSbx = sbx;
      float w = width;
      if (bg != null) {
        Interp b = new Interp(path);
        b.run(charstrings.get(bg), depth + 1);
        b.finish();
      }
      if (ag != null) {
        Interp a = new Interp(path);
        a.offsetX = baseSbx - asb + adx;
        a.offsetY = ady;
        a.run(charstrings.get(ag), depth + 1);
        a.finish();
      }
      width = w;
    }

    double offsetX;
    double offsetY;
  }

  private static String standardName(int code) {
    if (code >= 32 && code < 127) {
      if (code <= 0x40) {
        return kAscii[code - 32];
      }
      if (code <= 'Z') {
        return String.valueOf((char) code);
      }
      if (code <= 0x60) {
        return kAsciiTail[code - 0x5B];
      }
      if (code <= 'z') {
        return String.valueOf((char) code);
      }
      return kAsciiEnd[code - 0x7B];
    }
    switch (code) {
    case 161: return "exclamdown";
    case 162: return "cent";
    case 163: return "sterling";
    case 164: return "fraction";
    case 165: return "yen";
    case 166: return "florin";
    case 167: return "section";
    case 168: return "currency";
    case 169: return "quotesingle";
    case 170: return "quotedblleft";
    case 171: return "guillemotleft";
    case 172: return "guilsinglleft";
    case 173: return "guilsinglright";
    case 174: return "fi";
    case 175: return "fl";
    case 177: return "endash";
    case 178: return "dagger";
    case 179: return "daggerdbl";
    case 180: return "periodcentered";
    case 182: return "paragraph";
    case 183: return "bullet";
    case 184: return "quotesinglbase";
    case 185: return "quotedblbase";
    case 186: return "quotedblright";
    case 187: return "guillemotright";
    case 188: return "ellipsis";
    case 189: return "perthousand";
    case 191: return "questiondown";
    case 193: return "grave";
    case 194: return "acute";
    case 195: return "circumflex";
    case 196: return "tilde";
    case 197: return "macron";
    case 198: return "breve";
    case 199: return "dotaccent";
    case 200: return "dieresis";
    case 202: return "ring";
    case 203: return "cedilla";
    case 205: return "hungarumlaut";
    case 206: return "ogonek";
    case 207: return "caron";
    case 208: return "emdash";
    case 225: return "AE";
    case 227: return "ordfeminine";
    case 232: return "Lslash";
    case 233: return "Oslash";
    case 234: return "OE";
    case 235: return "ordmasculine";
    case 241: return "ae";
    case 245: return "dotlessi";
    case 248: return "lslash";
    case 249: return "oslash";
    case 250: return "oe";
    case 251: return "germandbls";
    default: return ".notdef";
    }
  }
}
