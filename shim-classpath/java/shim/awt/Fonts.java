/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.awt;

import java.awt.Font;
import java.util.HashMap;
import java.util.Map;

public final class Fonts {
  private static final Map<String, FontFace> kFaces = new HashMap<>();
  private static final String[] kLogical = {"Dialog", "DialogInput", "Monospaced", "SansSerif", "Serif"};

  private Fonts() { }

  public static String[] logicalFamilies() {
    return kLogical.clone();
  }

  public static String logicalFamily(String name) {
    if (name == null) {
      return "Dialog";
    }
    for (String s : kLogical) {
      if (s.equalsIgnoreCase(name)) {
        return s;
      }
    }
    String lower = name.toLowerCase(java.util.Locale.ROOT);
    if (lower.equals("default")) {
      return "Dialog";
    }
    int dot = lower.lastIndexOf('.');
    if (dot > 0 && suffixStyle(lower.substring(dot + 1)) >= 0) {
      return logicalFamily(name.substring(0, dot));
    }
    return null;
  }

  private static int suffixStyle(String s) {
    switch (s) {
    case "plain":
      return 0;
    case "bold":
      return 1;
    case "italic":
      return 2;
    case "bolditalic":
      return 3;
    default:
      return -1;
    }
  }

  public static int logicalSuffixStyle(String name) {
    if (name == null) {
      return 0;
    }
    int dot = name.lastIndexOf('.');
    if (dot <= 0 || logicalFamily(name) == null || logicalFamily(name.substring(0, dot)) == null) {
      return 0;
    }
    return Math.max(0, suffixStyle(name.substring(dot + 1).toLowerCase(java.util.Locale.ROOT)));
  }

  public static FontFace face(String name) {
    return face(name, 0);
  }

  public static synchronized FontFace face(String name, int style) {
    style = (style | logicalSuffixStyle(name)) & 3;
    String fam = logicalFamily(name);
    if (fam == null) {
      FontFace registered = kFaces.get(name.toLowerCase(java.util.Locale.ROOT));
      if (registered != null) {
        return registered;
      }
      FontFace sys = installedFace(name, style);
      if (sys != null) {
        return sys;
      }
      fam = "Dialog";
    }
    final String family = fam;
    final int st = style;
    return kFaces.computeIfAbsent("logical:" + family + ":" + style, k -> systemFace(family, st));
  }

  private static final String[] kFallbackNames = {"Droid Sans Fallback", "Noto Sans CJK SC", "Noto Sans CJK JP",
      "Noto Sans CJK KR", "WenQuanYi Micro Hei", "WenQuanYi Zen Hei", "Noto Sans", "Noto Sans Symbols",
      "Noto Sans Symbols2", "FreeSans", "FreeSerif", "Unifont", "Symbola"};
  private static java.util.List<FontFace> kFallbackFaces;
  private static final Map<Integer, FontFace> kFallbackByCp = new HashMap<>();

  static synchronized FontFace fallbackFace(int cp) {
    if (kFallbackFaces == null) {
      kFallbackFaces = new java.util.ArrayList<>();
      for (String n : kFallbackNames) {
        FontFace f = installedFace(n, 0);
        if (f != null) {
          kFallbackFaces.add(f);
        }
      }
    }
    if (!kFallbackByCp.containsKey(cp)) {
      FontFace found = null;
      for (FontFace f : kFallbackFaces) {
        if (f.canDisplay(cp)) {
          found = f;
          break;
        }
      }
      kFallbackByCp.put(cp, found);
    }
    return kFallbackByCp.get(cp);
  }

  private static FontFace installedFace(String name, int style) {
    SystemFonts.Entry e = SystemFonts.find(name, style);
    if (e == null) {
      return null;
    }
    String key = "sys:" + e.file.getPath();
    FontFace f = kFaces.get(key);
    if (f == null) {
      try {
        byte[] bytes = java.nio.file.Files.readAllBytes(e.file.toPath());
        String fn = e.file.getName().toLowerCase(java.util.Locale.ROOT);
        FontFace parsed = fn.endsWith(".pfb") || fn.endsWith(".pfa") ? Type1Face.parse(bytes)
            : TrueTypeFace.parse(bytes, 0);
        f = new NamedFace(e.family, parsed, e.style != 0, false);
      } catch (Exception | LinkageError ex) {
        return null;
      }
      kFaces.put(key, f);
    }
    return f;
  }

  public static synchronized String[] allFaceNames() {
    java.util.TreeSet<String> names = new java.util.TreeSet<>();
    for (String l : kLogical) {
      for (String s : new String[] {"plain", "bold", "italic", "bolditalic"}) {
        names.add(l + "." + s);
      }
    }
    for (SystemFonts.Entry e : SystemFonts.entries()) {
      names.add(e.full);
    }
    return names.toArray(new String[0]);
  }

  public static synchronized String fullName(String name, int style) {
    if (logicalFamily(name) != null) {
      return null;
    }
    FontFace registered = name == null ? null : kFaces.get(name.toLowerCase(java.util.Locale.ROOT));
    if (registered != null && registered.fullName() != null) {
      return registered.fullName();
    }
    SystemFonts.Entry e = SystemFonts.find(name, style);
    return e == null ? null : e.full;
  }

  public static synchronized String postScriptName(String name, int style) {
    if (logicalFamily(name) != null) {
      return null;
    }
    FontFace registered = name == null ? null : kFaces.get(name.toLowerCase(java.util.Locale.ROOT));
    if (registered != null && registered.postScriptName() != null) {
      return registered.postScriptName();
    }
    SystemFonts.Entry e = SystemFonts.find(name, style);
    return e == null ? null : e.ps;
  }

  private static final String[] kDirs = {"/usr/share/fonts/truetype/dejavu/", "/usr/share/fonts/dejavu/",
      "/usr/share/fonts/TTF/", "/usr/share/fonts/truetype/liberation/"};

  private static String[] styledFiles(String family, int style) {
    String base;
    String italic = "Oblique";
    switch (family) {
      case "Serif":
        base = "DejaVuSerif";
        italic = "Italic";
        break;
      case "Monospaced":
      case "DialogInput":
        base = "DejaVuSansMono";
        break;
      default:
        base = "DejaVuSans";
    }
    String suffix;
    switch (style) {
      case 1:
        suffix = "-Bold";
        break;
      case 2:
        suffix = "-" + italic;
        break;
      case 3:
        suffix = "-Bold" + italic;
        break;
      default:
        suffix = "";
    }
    if (style == 3 && family.equals("Serif")) {
      suffix = "-BoldItalic";
    }
    return new String[] {base + suffix + ".ttf"};
  }

  private static FontFace systemFace(String family, int style) {
    String[] files;
    boolean styled = style != 0;
    if (styled) {
      files = styledFiles(family, style);
      for (String dir : kDirs) {
        for (String f : files) {
          java.io.File file = new java.io.File(dir + f);
          if (file.isFile()) {
            try {
              return new NamedFace(family, TrueTypeFace.parse(java.nio.file.Files.readAllBytes(file.toPath()), 0), true, true);
            } catch (Exception | LinkageError e) {
              continue;
            }
          }
        }
      }
    }
    switch (family) {
      case "Serif":
        files = new String[] {"DejaVuSerif.ttf", "LiberationSerif-Regular.ttf"};
        break;
      case "Monospaced":
      case "DialogInput":
        files = new String[] {"DejaVuSansMono.ttf", "LiberationMono-Regular.ttf"};
        break;
      default:
        files = new String[] {"DejaVuSans.ttf", "LiberationSans-Regular.ttf"};
    }
    for (String dir : kDirs) {
      for (String f : files) {
        java.io.File file = new java.io.File(dir + f);
        if (file.isFile()) {
          try {
            return new NamedFace(family, TrueTypeFace.parse(java.nio.file.Files.readAllBytes(file.toPath()), 0), false, true);
          } catch (Exception | LinkageError e) {
            continue;
          }
        }
      }
    }
    return new BitmapFace(family);
  }

  private static final class NamedFace extends FontFace {
    private final String name;
    private final FontFace delegate;
    private final boolean styled;
    private final boolean composite;

    NamedFace(String name, FontFace delegate, boolean styled, boolean composite) {
      this.name = name;
      this.delegate = delegate;
      this.styled = styled;
      this.composite = composite;
    }

    private FontFace fallback(int cp) {
      return composite && cp >= 0x80 && !delegate.canDisplay(cp) ? Fonts.fallbackFace(cp) : null;
    }

    public String family() { return name; }

    public boolean canDisplay(int cp) { return delegate.canDisplay(cp) || fallback(cp) != null; }

    public float advance(int cp) {
      FontFace f = fallback(cp);
      return (f != null ? f : delegate).advance(cp);
    }

    public java.awt.Shape outline(int cp, boolean bold, boolean italic) {
      FontFace f = fallback(cp);
      return (f != null ? f : delegate).outline(cp, bold, italic);
    }

    public float ascent() { return delegate.ascent(); }

    public float descent() { return delegate.descent(); }

    public float leading() { return delegate.leading(); }

    public int numGlyphs() { return delegate.numGlyphs(); }

    public int missingGlyph() { return delegate.missingGlyph(); }

    public int glyphCode(int cp) { return delegate.glyphCode(cp); }

    public int codePointOf(int glyph) { return delegate.codePointOf(glyph); }

    public float pixels(float em, float size) { return delegate.pixels(em, size); }

    public float advancePixels(float em, float size) { return delegate.advancePixels(em, size); }

    public float[] styleMetrics(float size, float ascent) { return delegate.styleMetrics(size, ascent); }

    public java.awt.Shape hintedGlyph(int cp, float size) {
      FontFace f = fallback(cp);
      return (f != null ? f : delegate).hintedGlyph(cp, size);
    }

    public int unitsPerEm() { return delegate.unitsPerEm(); }

    public java.awt.Shape scaledOutline(int cp, float size) {
      FontFace f = fallback(cp);
      return (f != null ? f : delegate).scaledOutline(cp, size);
    }

    public float lineHeight() { return delegate.lineHeight(); }

    public boolean leadingFromHeight() { return delegate.leadingFromHeight(); }

    public float kerning(int a, int b) { return delegate.kerning(a, b); }

    public int hintedAdvance(int cp, float size) {
      return fallback(cp) != null ? -1 : delegate.hintedAdvance(cp, size);
    }

    public boolean styled() { return styled; }

    public int maxAdvancePixels(float size) { return delegate.maxAdvancePixels(size); }
  }

  public static synchronized boolean isKnown(String name) {
    return logicalFamily(name) != null || kFaces.containsKey(name.toLowerCase(java.util.Locale.ROOT))
        || SystemFonts.find(name, 0) != null;
  }

  public static synchronized void register(String family, FontFace face) {
    kFaces.put(family.toLowerCase(java.util.Locale.ROOT), face);
  }

  public static synchronized String[] families() {
    java.util.TreeSet<String> out = new java.util.TreeSet<>();
    for (String s : kLogical) {
      out.add(s);
    }
    for (FontFace f : kFaces.values()) {
      if (!(f instanceof BitmapFace) && !(f instanceof NamedFace)) {
        out.add(f.family());
      }
    }
    for (String s : SystemFonts.families()) {
      out.add(s);
    }
    return out.toArray(new String[0]);
  }
}
