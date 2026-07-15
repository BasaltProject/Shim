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
    return null;
  }

  public static FontFace face(String name) {
    return face(name, 0);
  }

  public static synchronized FontFace face(String name, int style) {
    style &= 3;
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

  private static FontFace installedFace(String name, int style) {
    SystemFonts.Entry e = SystemFonts.find(name, style);
    if (e == null) {
      return null;
    }
    String key = "sys:" + e.file.getPath();
    FontFace f = kFaces.get(key);
    if (f == null) {
      try {
        f = new NamedFace(e.family, TrueTypeFace.parse(java.nio.file.Files.readAllBytes(e.file.toPath()), 0),
            e.style != 0);
      } catch (Exception | LinkageError ex) {
        return null;
      }
      kFaces.put(key, f);
    }
    return f;
  }

  public static synchronized String fullName(String name, int style) {
    if (logicalFamily(name) != null) {
      return null;
    }
    SystemFonts.Entry e = SystemFonts.find(name, style);
    return e == null ? null : e.full;
  }

  public static synchronized String postScriptName(String name, int style) {
    if (logicalFamily(name) != null) {
      return null;
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
              return new NamedFace(family, TrueTypeFace.parse(java.nio.file.Files.readAllBytes(file.toPath()), 0), true);
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
            return new NamedFace(family, TrueTypeFace.parse(java.nio.file.Files.readAllBytes(file.toPath()), 0), false);
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

    NamedFace(String name, FontFace delegate, boolean styled) {
      this.name = name;
      this.delegate = delegate;
      this.styled = styled;
    }

    public String family() { return name; }

    public boolean canDisplay(int cp) { return delegate.canDisplay(cp); }

    public float advance(int cp) { return delegate.advance(cp); }

    public java.awt.Shape outline(int cp, boolean bold, boolean italic) { return delegate.outline(cp, bold, italic); }

    public float ascent() { return delegate.ascent(); }

    public float descent() { return delegate.descent(); }

    public float leading() { return delegate.leading(); }

    public int numGlyphs() { return delegate.numGlyphs(); }

    public int missingGlyph() { return delegate.missingGlyph(); }

    public float kerning(int a, int b) { return delegate.kerning(a, b); }

    public int hintedAdvance(int cp, float size) { return delegate.hintedAdvance(cp, size); }

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
