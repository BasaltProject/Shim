/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */
package javax.swing.text.html;

import java.awt.Color;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.swing.text.MutableAttributeSet;

public class CSS implements Serializable {
  public static final class Attribute {
    private final String name;
    private final String defaultValue;
    private final boolean inherited;

    private Attribute(String name, String defaultValue, boolean inherited) {
      this.name = name;
      this.defaultValue = defaultValue;
      this.inherited = inherited;
    }

    public String toString() { return name; }

    public String getDefaultValue() { return defaultValue; }

    public boolean isInherited() { return inherited; }

    public static final Attribute BACKGROUND = new Attribute("background", null, false);
    public static final Attribute BACKGROUND_ATTACHMENT = new Attribute("background-attachment", "scroll", false);
    public static final Attribute BACKGROUND_COLOR = new Attribute("background-color", "transparent", false);
    public static final Attribute BACKGROUND_IMAGE = new Attribute("background-image", "none", false);
    public static final Attribute BACKGROUND_POSITION = new Attribute("background-position", null, false);
    public static final Attribute BACKGROUND_REPEAT = new Attribute("background-repeat", "repeat", false);
    public static final Attribute BORDER = new Attribute("border", null, false);
    public static final Attribute BORDER_BOTTOM = new Attribute("border-bottom", null, false);
    public static final Attribute BORDER_BOTTOM_COLOR = new Attribute("border-bottom-color", null, false);
    public static final Attribute BORDER_BOTTOM_STYLE = new Attribute("border-bottom-style", "none", false);
    public static final Attribute BORDER_BOTTOM_WIDTH = new Attribute("border-bottom-width", "medium", false);
    public static final Attribute BORDER_COLOR = new Attribute("border-color", null, false);
    public static final Attribute BORDER_LEFT = new Attribute("border-left", null, false);
    public static final Attribute BORDER_LEFT_COLOR = new Attribute("border-left-color", null, false);
    public static final Attribute BORDER_LEFT_STYLE = new Attribute("border-left-style", "none", false);
    public static final Attribute BORDER_LEFT_WIDTH = new Attribute("border-left-width", "medium", false);
    public static final Attribute BORDER_RIGHT = new Attribute("border-right", null, false);
    public static final Attribute BORDER_RIGHT_COLOR = new Attribute("border-right-color", null, false);
    public static final Attribute BORDER_RIGHT_STYLE = new Attribute("border-right-style", "none", false);
    public static final Attribute BORDER_RIGHT_WIDTH = new Attribute("border-right-width", "medium", false);
    public static final Attribute BORDER_STYLE = new Attribute("border-style", "none", false);
    public static final Attribute BORDER_TOP = new Attribute("border-top", null, false);
    public static final Attribute BORDER_TOP_COLOR = new Attribute("border-top-color", null, false);
    public static final Attribute BORDER_TOP_STYLE = new Attribute("border-top-style", "none", false);
    public static final Attribute BORDER_TOP_WIDTH = new Attribute("border-top-width", "medium", false);
    public static final Attribute BORDER_WIDTH = new Attribute("border-width", "medium", false);
    public static final Attribute CLEAR = new Attribute("clear", "none", false);
    public static final Attribute COLOR = new Attribute("color", "black", true);
    public static final Attribute DISPLAY = new Attribute("display", "block", false);
    public static final Attribute FLOAT = new Attribute("float", "none", false);
    public static final Attribute FONT = new Attribute("font", null, true);
    public static final Attribute FONT_FAMILY = new Attribute("font-family", null, true);
    public static final Attribute FONT_SIZE = new Attribute("font-size", "medium", true);
    public static final Attribute FONT_STYLE = new Attribute("font-style", "normal", true);
    public static final Attribute FONT_VARIANT = new Attribute("font-variant", "normal", true);
    public static final Attribute FONT_WEIGHT = new Attribute("font-weight", "normal", true);
    public static final Attribute HEIGHT = new Attribute("height", "auto", false);
    public static final Attribute LETTER_SPACING = new Attribute("letter-spacing", "normal", true);
    public static final Attribute LINE_HEIGHT = new Attribute("line-height", "normal", true);
    public static final Attribute LIST_STYLE = new Attribute("list-style", "disc", true);
    public static final Attribute LIST_STYLE_IMAGE = new Attribute("list-style-image", "none", true);
    public static final Attribute LIST_STYLE_POSITION = new Attribute("list-style-position", "outside", true);
    public static final Attribute LIST_STYLE_TYPE = new Attribute("list-style-type", "disc", true);
    public static final Attribute MARGIN = new Attribute("margin", null, false);
    public static final Attribute MARGIN_BOTTOM = new Attribute("margin-bottom", "0", false);
    public static final Attribute MARGIN_LEFT = new Attribute("margin-left", "0", false);
    public static final Attribute MARGIN_RIGHT = new Attribute("margin-right", "0", false);
    public static final Attribute MARGIN_TOP = new Attribute("margin-top", "0", false);
    public static final Attribute PADDING = new Attribute("padding", null, false);
    public static final Attribute PADDING_BOTTOM = new Attribute("padding-bottom", "0", false);
    public static final Attribute PADDING_LEFT = new Attribute("padding-left", "0", false);
    public static final Attribute PADDING_RIGHT = new Attribute("padding-right", "0", false);
    public static final Attribute PADDING_TOP = new Attribute("padding-top", "0", false);
    public static final Attribute TEXT_ALIGN = new Attribute("text-align", null, true);
    public static final Attribute TEXT_DECORATION = new Attribute("text-decoration", "none", true);
    public static final Attribute TEXT_INDENT = new Attribute("text-indent", "0", true);
    public static final Attribute TEXT_TRANSFORM = new Attribute("text-transform", "none", true);
    public static final Attribute VERTICAL_ALIGN = new Attribute("vertical-align", "baseline", false);
    public static final Attribute WORD_SPACING = new Attribute("word-spacing", "normal", true);
    public static final Attribute WHITE_SPACE = new Attribute("white-space", "normal", true);
    public static final Attribute WIDTH = new Attribute("width", "auto", false);
    static final Attribute MARGIN_LEFT_LTR = new Attribute("margin-left-ltr", "-2147483648", false);
    static final Attribute MARGIN_LEFT_RTL = new Attribute("margin-left-rtl", "-2147483648", false);
    static final Attribute MARGIN_RIGHT_LTR = new Attribute("margin-right-ltr", "-2147483648", false);
    static final Attribute MARGIN_RIGHT_RTL = new Attribute("margin-right-rtl", "-2147483648", false);
    static final Attribute CAPTION_SIDE = new Attribute("caption-side", "top", true);
    static final Attribute[] allAttributes = {BACKGROUND, BACKGROUND_ATTACHMENT, BACKGROUND_COLOR, BACKGROUND_IMAGE, BACKGROUND_POSITION, BACKGROUND_REPEAT, BORDER, BORDER_BOTTOM, BORDER_BOTTOM_COLOR, BORDER_BOTTOM_STYLE, BORDER_BOTTOM_WIDTH, BORDER_COLOR, BORDER_LEFT, BORDER_LEFT_COLOR, BORDER_LEFT_STYLE, BORDER_LEFT_WIDTH, BORDER_RIGHT, BORDER_RIGHT_COLOR, BORDER_RIGHT_STYLE, BORDER_RIGHT_WIDTH, BORDER_STYLE, BORDER_TOP, BORDER_TOP_COLOR, BORDER_TOP_STYLE, BORDER_TOP_WIDTH, BORDER_WIDTH, CLEAR, COLOR, DISPLAY, FLOAT, FONT, FONT_FAMILY, FONT_SIZE, FONT_STYLE, FONT_VARIANT, FONT_WEIGHT, HEIGHT, LETTER_SPACING, LINE_HEIGHT, LIST_STYLE, LIST_STYLE_IMAGE, LIST_STYLE_POSITION, LIST_STYLE_TYPE, MARGIN, MARGIN_BOTTOM, MARGIN_LEFT, MARGIN_RIGHT, MARGIN_TOP, PADDING, PADDING_BOTTOM, PADDING_LEFT, PADDING_RIGHT, PADDING_TOP, TEXT_ALIGN, TEXT_DECORATION, TEXT_INDENT, TEXT_TRANSFORM, VERTICAL_ALIGN, WORD_SPACING, WHITE_SPACE, WIDTH};
    static final Attribute[] internalAttributes = {MARGIN_LEFT_LTR, MARGIN_LEFT_RTL, MARGIN_RIGHT_LTR, MARGIN_RIGHT_RTL, CAPTION_SIDE};
  }

  static final class Value {
    final String text;

    Value(String text) {
      this.text = text;
    }

    public String toString() { return text; }

    public boolean equals(Object o) { return o instanceof Value && ((Value) o).text.equals(text); }

    public int hashCode() { return text.hashCode(); }
  }

  private static final Map<String, Attribute> attributeMap = new HashMap<String, Attribute>();

  static {
    for (Attribute a : Attribute.allAttributes) {
      attributeMap.put(a.toString(), a);
    }
    for (Attribute a : Attribute.internalAttributes) {
      attributeMap.put(a.toString(), a);
    }
  }

  public CSS() { }

  public static Attribute[] getAllAttributeKeys() {
    Attribute[] keys = new Attribute[Attribute.allAttributes.length];
    System.arraycopy(Attribute.allAttributes, 0, keys, 0, keys.length);
    return keys;
  }

  public static final Attribute getAttribute(String name) { return attributeMap.get(name); }

  static boolean isLength(String v) {
    return Len.parse(v) != null;
  }

  static final class Len {
    final float value;
    final String units;
    final boolean percent;

    Len(float value, String units) {
      this.value = value;
      this.units = units;
      this.percent = "%".equals(units);
    }

    static Len parse(String s) {
      if (s == null) {
        return null;
      }
      s = s.trim().toLowerCase(Locale.ROOT);
      int i = 0;
      while (i < s.length() && (Character.isDigit(s.charAt(i)) || s.charAt(i) == '.' || (i == 0 && (s.charAt(i) == '-'
          || s.charAt(i) == '+')))) {
        i++;
      }
      if (i == 0) {
        return null;
      }
      float v;
      try {
        v = Float.parseFloat(s.substring(0, i));
      } catch (NumberFormatException e) {
        return null;
      }
      String u = s.substring(i).trim();
      if (u.length() > 0 && !u.equals("%") && !u.equals("pt") && !u.equals("px") && !u.equals("em") && !u.equals("ex")
          && !u.equals("in") && !u.equals("cm") && !u.equals("mm") && !u.equals("pc")) {
        return null;
      }
      return new Len(v, u);
    }

    float get(boolean w3c) {
      float conv = 1f;
      if (units.equals("px")) {
        conv = w3c ? 0.75f : 1.3f;
      } else if (units.equals("pc")) {
        conv = 12f;
      } else if (units.equals("in")) {
        conv = 72f;
      } else if (units.equals("cm")) {
        conv = 28.3464f;
      } else if (units.equals("mm")) {
        conv = 2.83464f;
      }
      return value * conv;
    }

    float get(float base) {
      if (percent) {
        return base * value / 100f;
      }
      if (units.equals("em") || units.equals("ex")) {
        return base * value;
      }
      return get(false);
    }
  }

  static final String[] SIZE_NAMES = {"xx-small", "x-small", "small", "medium", "large", "x-large", "xx-large"};
  static final int[] SIZE_MAP = {8, 10, 12, 14, 18, 24, 36};

  static int sizeIndex(String v) {
    for (int i = 0; i < SIZE_NAMES.length; i++) {
      if (SIZE_NAMES[i].equals(v)) {
        return i;
      }
    }
    return -1;
  }

  static final Map<String, Color> colors = new HashMap<String, Color>();

  static {
    Object[][] c = {{"black", 0x000000}, {"silver", 0xc0c0c0}, {"gray", 0x808080}, {"grey", 0x808080}, {"white", 0xffffff},
        {"maroon", 0x800000}, {"red", 0xff0000}, {"purple", 0x800080}, {"fuchsia", 0xff00ff}, {"green", 0x008000},
        {"lime", 0x00ff00}, {"olive", 0x808000}, {"yellow", 0xffff00}, {"navy", 0x000080}, {"blue", 0x0000ff},
        {"teal", 0x008080}, {"aqua", 0x00ffff}, {"cyan", 0x00ffff}, {"magenta", 0xff00ff}, {"orange", 0xffa500},
        {"pink", 0xffc0cb}, {"brown", 0xa52a2a}, {"gold", 0xffd700}, {"violet", 0xee82ee}, {"darkgray", 0xa9a9a9},
        {"lightgray", 0xd3d3d3}, {"darkblue", 0x00008b}, {"darkgreen", 0x006400}, {"darkred", 0x8b0000}};
    for (Object[] e : c) {
      colors.put((String) e[0], new Color((Integer) e[1]));
    }
  }

  static Color parseColor(String s) {
    if (s == null) {
      return null;
    }
    s = s.trim().toLowerCase(Locale.ROOT);
    Color named = colors.get(s);
    if (named != null) {
      return named;
    }
    try {
      if (s.startsWith("#")) {
        String h = s.substring(1);
        if (h.length() == 3) {
          h = "" + h.charAt(0) + h.charAt(0) + h.charAt(1) + h.charAt(1) + h.charAt(2) + h.charAt(2);
        }
        if (h.length() == 6) {
          return new Color(Integer.parseInt(h, 16));
        }
        return null;
      }
      if (s.startsWith("rgb(") && s.endsWith(")")) {
        String[] p = s.substring(4, s.length() - 1).split(",");
        int[] v = new int[3];
        for (int i = 0; i < 3; i++) {
          String t = p[i].trim();
          v[i] = t.endsWith("%") ? Math.round(Float.parseFloat(t.substring(0, t.length() - 1)) * 2.55f)
              : Integer.parseInt(t);
          v[i] = Math.max(0, Math.min(255, v[i]));
        }
        return new Color(v[0], v[1], v[2]);
      }
      if (s.length() == 6) {
        return new Color(Integer.parseInt(s, 16));
      }
    } catch (RuntimeException e) {
      return null;
    }
    return null;
  }

  static List<String> splitTop(String v, char sep) {
    List<String> out = new ArrayList<String>();
    int depth = 0;
    boolean q = false;
    char qc = 0;
    int st = 0;
    for (int i = 0; i < v.length(); i++) {
      char c = v.charAt(i);
      if (q) {
        if (c == qc) {
          q = false;
        }
      } else if (c == '"' || c == '\'') {
        q = true;
        qc = c;
      } else if (c == '(') {
        depth++;
      } else if (c == ')') {
        depth--;
      } else if (c == sep && depth == 0) {
        out.add(v.substring(st, i));
        st = i + 1;
      }
    }
    out.add(v.substring(st));
    return out;
  }

  static List<String> splitSpaces(String v) {
    List<String> out = new ArrayList<String>();
    int depth = 0;
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < v.length(); i++) {
      char c = v.charAt(i);
      if (c == '(') {
        depth++;
      } else if (c == ')') {
        depth--;
      }
      if (Character.isWhitespace(c) && depth == 0) {
        if (sb.length() > 0) {
          out.add(sb.toString());
          sb.setLength(0);
        }
      } else {
        sb.append(c);
      }
    }
    if (sb.length() > 0) {
      out.add(sb.toString());
    }
    return out;
  }

  static void put(MutableAttributeSet out, Attribute key, String value) {
    out.addAttribute(key, new Value(value.trim()));
  }

  private static void box(MutableAttributeSet out, String value, Attribute t, Attribute r, Attribute b, Attribute l) {
    List<String> p = splitSpaces(value);
    if (p.isEmpty()) {
      return;
    }
    String top = p.get(0);
    String right = p.size() > 1 ? p.get(1) : top;
    String bottom = p.size() > 2 ? p.get(2) : top;
    String left = p.size() > 3 ? p.get(3) : right;
    put(out, t, top);
    put(out, r, right);
    put(out, b, bottom);
    put(out, l, left);
  }

  private static boolean isBorderStyle(String s) {
    return s.equals("none") || s.equals("solid") || s.equals("dashed") || s.equals("dotted") || s.equals("double")
        || s.equals("groove") || s.equals("ridge") || s.equals("inset") || s.equals("outset") || s.equals("hidden");
  }

  private static void borderSide(MutableAttributeSet out, String value, Attribute w, Attribute s, Attribute c) {
    for (String tok : splitSpaces(value)) {
      String t = tok.toLowerCase(Locale.ROOT);
      if (isBorderStyle(t)) {
        put(out, s, t);
      } else if (t.equals("thin") || t.equals("medium") || t.equals("thick") || Len.parse(t) != null) {
        put(out, w, t);
      } else {
        put(out, c, tok);
      }
    }
  }

  static void addProperty(MutableAttributeSet out, String prop, String value) {
    prop = prop.trim().toLowerCase(Locale.ROOT);
    value = value.trim();
    int imp = value.indexOf("!important");
    if (imp >= 0) {
      value = value.substring(0, imp).trim();
    }
    if (value.length() == 0) {
      return;
    }
    if (prop.equals("margin")) {
      box(out, value, Attribute.MARGIN_TOP, Attribute.MARGIN_RIGHT, Attribute.MARGIN_BOTTOM, Attribute.MARGIN_LEFT);
    } else if (prop.equals("padding")) {
      box(out, value, Attribute.PADDING_TOP, Attribute.PADDING_RIGHT, Attribute.PADDING_BOTTOM, Attribute.PADDING_LEFT);
    } else if (prop.equals("border-width")) {
      box(out, value, Attribute.BORDER_TOP_WIDTH, Attribute.BORDER_RIGHT_WIDTH, Attribute.BORDER_BOTTOM_WIDTH,
          Attribute.BORDER_LEFT_WIDTH);
    } else if (prop.equals("border-style")) {
      box(out, value, Attribute.BORDER_TOP_STYLE, Attribute.BORDER_RIGHT_STYLE, Attribute.BORDER_BOTTOM_STYLE,
          Attribute.BORDER_LEFT_STYLE);
    } else if (prop.equals("border-color")) {
      box(out, value, Attribute.BORDER_TOP_COLOR, Attribute.BORDER_RIGHT_COLOR, Attribute.BORDER_BOTTOM_COLOR,
          Attribute.BORDER_LEFT_COLOR);
    } else if (prop.equals("border")) {
      borderSide(out, value, Attribute.BORDER_TOP_WIDTH, Attribute.BORDER_TOP_STYLE, Attribute.BORDER_TOP_COLOR);
      borderSide(out, value, Attribute.BORDER_RIGHT_WIDTH, Attribute.BORDER_RIGHT_STYLE, Attribute.BORDER_RIGHT_COLOR);
      borderSide(out, value, Attribute.BORDER_BOTTOM_WIDTH, Attribute.BORDER_BOTTOM_STYLE, Attribute.BORDER_BOTTOM_COLOR);
      borderSide(out, value, Attribute.BORDER_LEFT_WIDTH, Attribute.BORDER_LEFT_STYLE, Attribute.BORDER_LEFT_COLOR);
    } else if (prop.equals("border-top")) {
      borderSide(out, value, Attribute.BORDER_TOP_WIDTH, Attribute.BORDER_TOP_STYLE, Attribute.BORDER_TOP_COLOR);
    } else if (prop.equals("border-right")) {
      borderSide(out, value, Attribute.BORDER_RIGHT_WIDTH, Attribute.BORDER_RIGHT_STYLE, Attribute.BORDER_RIGHT_COLOR);
    } else if (prop.equals("border-bottom")) {
      borderSide(out, value, Attribute.BORDER_BOTTOM_WIDTH, Attribute.BORDER_BOTTOM_STYLE, Attribute.BORDER_BOTTOM_COLOR);
    } else if (prop.equals("border-left")) {
      borderSide(out, value, Attribute.BORDER_LEFT_WIDTH, Attribute.BORDER_LEFT_STYLE, Attribute.BORDER_LEFT_COLOR);
    } else if (prop.equals("font")) {
      parseFont(out, value);
    } else if (prop.equals("list-style")) {
      for (String tok : splitSpaces(value)) {
        String t = tok.toLowerCase(Locale.ROOT);
        if (t.equals("inside") || t.equals("outside")) {
          put(out, Attribute.LIST_STYLE_POSITION, t);
        } else if (t.startsWith("url(")) {
          put(out, Attribute.LIST_STYLE_IMAGE, tok);
        } else {
          put(out, Attribute.LIST_STYLE_TYPE, t);
        }
      }
    } else if (prop.equals("background")) {
      for (String tok : splitSpaces(value)) {
        String t = tok.toLowerCase(Locale.ROOT);
        if (t.startsWith("url(")) {
          put(out, Attribute.BACKGROUND_IMAGE, tok);
        } else if (t.startsWith("repeat") || t.equals("no-repeat")) {
          put(out, Attribute.BACKGROUND_REPEAT, t);
        } else if (parseColor(tok) != null || t.equals("transparent")) {
          put(out, Attribute.BACKGROUND_COLOR, tok);
        }
      }
    } else {
      Attribute a = attributeMap.get(prop);
      if (a != null) {
        put(out, a, a == Attribute.FONT_FAMILY ? value : lowerIfKeyword(a, value));
      }
    }
  }

  private static String lowerIfKeyword(Attribute a, String value) {
    if (a == Attribute.COLOR || a == Attribute.BACKGROUND_COLOR || a == Attribute.BACKGROUND_IMAGE
        || a == Attribute.LIST_STYLE_IMAGE) {
      return value;
    }
    return value.toLowerCase(Locale.ROOT);
  }

  private static void parseFont(MutableAttributeSet out, String value) {
    List<String> toks = splitSpaces(value);
    int i = 0;
    for (; i < toks.size(); i++) {
      String t = toks.get(i).toLowerCase(Locale.ROOT);
      if (t.equals("italic") || t.equals("oblique")) {
        put(out, Attribute.FONT_STYLE, t);
      } else if (t.equals("small-caps")) {
        put(out, Attribute.FONT_VARIANT, t);
      } else if (t.equals("bold") || t.equals("bolder") || t.equals("lighter") || t.matches("[1-9]00")) {
        put(out, Attribute.FONT_WEIGHT, t);
      } else if (!t.equals("normal")) {
        break;
      }
    }
    if (i < toks.size()) {
      String sz = toks.get(i);
      int slash = sz.indexOf('/');
      if (slash > 0) {
        put(out, Attribute.LINE_HEIGHT, sz.substring(slash + 1));
        sz = sz.substring(0, slash);
      }
      put(out, Attribute.FONT_SIZE, sz.toLowerCase(Locale.ROOT));
      i++;
    }
    if (i < toks.size()) {
      StringBuilder fam = new StringBuilder();
      for (; i < toks.size(); i++) {
        if (fam.length() > 0) {
          fam.append(' ');
        }
        fam.append(toks.get(i));
      }
      put(out, Attribute.FONT_FAMILY, fam.toString());
    }
  }

  static void parseDeclarations(String decl, MutableAttributeSet out) {
    for (String d : splitTop(decl, ';')) {
      int c = d.indexOf(':');
      if (c > 0) {
        addProperty(out, d.substring(0, c), d.substring(c + 1));
      }
    }
  }
}
