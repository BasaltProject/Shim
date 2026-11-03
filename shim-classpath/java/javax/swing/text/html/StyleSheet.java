/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */
package javax.swing.text.html;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Rectangle;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.Element;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.View;

public class StyleSheet extends StyleContext {
  static final int DEFAULT_FONT_SIZE = 3;
  private URL base;
  private final List<StyleSheet> linked = new ArrayList<StyleSheet>();
  private final List<Rule> rules = new ArrayList<Rule>();
  private int baseFontSize = DEFAULT_FONT_SIZE;
  private int sysFontSize = 12;

  public StyleSheet() {
    super();
  }

  private static final class Part {
    String tag;
    String cls;
    String id;
  }

  private static final class Rule {
    Part[] parts;
    int specificity;
    int order;
    Style style;
    String selector;
  }

  public Style getRule(HTML.Tag t, Element e) {
    List<Part> chain = new ArrayList<Part>();
    buildChain(e, chain);
    return resolve(chain);
  }

  private Style resolve(List<Part> chain) {
    List<Rule> matched = new ArrayList<Rule>();
    collect(matched, chain);
    Collections.sort(matched, new Comparator<Rule>() {
      public int compare(Rule a, Rule b) {
        return a.specificity - b.specificity;
      }
    });
    NamedStyle result = new NamedStyle();
    for (Rule r : matched) {
      result.addAttributes(r.style);
    }
    return result;
  }

  private void collect(List<Rule> out, List<Part> chain) {
    for (StyleSheet s : linked) {
      s.collect(out, chain);
    }
    for (Rule r : rules) {
      if (matches(r, chain)) {
        out.add(r);
      }
    }
  }

  private static boolean matches(Rule r, List<Part> chain) {
    int pi = r.parts.length - 1;
    int ci = chain.size() - 1;
    if (pi < 0 || ci < 0 || !partMatches(r.parts[pi], chain.get(ci))) {
      return false;
    }
    pi--;
    ci--;
    while (pi >= 0) {
      boolean found = false;
      while (ci >= 0) {
        if (partMatches(r.parts[pi], chain.get(ci))) {
          found = true;
          ci--;
          break;
        }
        ci--;
      }
      if (!found) {
        return false;
      }
      pi--;
    }
    return true;
  }

  private static boolean partMatches(Part sel, Part node) {
    if (sel.tag != null && !sel.tag.equals(node.tag)) {
      return false;
    }
    if (sel.id != null && !sel.id.equals(node.id)) {
      return false;
    }
    if (sel.cls != null) {
      if (node.cls == null) {
        return false;
      }
      boolean ok = false;
      for (String c : node.cls.split("\\s+")) {
        if (c.equals(sel.cls)) {
          ok = true;
        }
      }
      if (!ok) {
        return false;
      }
    }
    return true;
  }

  private static Part nodeFor(Object tag, AttributeSet attrs) {
    Part p = new Part();
    p.tag = tag == null ? null : tag.toString();
    if (attrs != null) {
      Object c = attrs.getAttribute(HTML.Attribute.CLASS);
      Object i = attrs.getAttribute(HTML.Attribute.ID);
      p.cls = c == null ? null : c.toString();
      p.id = i == null ? null : i.toString();
    }
    return p;
  }

  private static void buildChain(Element e, List<Part> chain) {
    if (e == null) {
      return;
    }
    buildChain(e.getParentElement(), chain);
    AttributeSet a = e.getAttributes();
    Object name = a.getAttribute(StyleConstants.NameAttribute);
    if (name instanceof HTML.Tag && name != HTML.Tag.CONTENT) {
      chain.add(nodeFor(name, a));
    } else if (name == HTML.Tag.CONTENT) {
      for (Enumeration<?> en = a.getAttributeNames(); en.hasMoreElements();) {
        Object k = en.nextElement();
        if (k instanceof HTML.Tag) {
          Object v = a.getAttribute(k);
          chain.add(nodeFor(k, v instanceof AttributeSet ? (AttributeSet) v : null));
        }
      }
    }
  }

  public Style getRule(String selector) {
    selector = cleanSelector(selector);
    return getStyle(selector);
  }

  private static String cleanSelector(String selector) {
    String s = selector.trim().replaceAll("\\s+", " ");
    return s.toLowerCase(Locale.ROOT);
  }

  public void addRule(String rule) {
    if (rule == null) {
      return;
    }
    String css = rule.replaceAll("(?s)/\\*.*?\\*/", " ");
    int i = 0;
    int n = css.length();
    while (i < n) {
      while (i < n && Character.isWhitespace(css.charAt(i))) {
        i++;
      }
      if (i >= n) {
        break;
      }
      if (css.charAt(i) == '@') {
        int semi = css.indexOf(';', i);
        int brace = css.indexOf('{', i);
        if (brace >= 0 && (semi < 0 || brace < semi)) {
          int depth = 0;
          int j = brace;
          for (; j < n; j++) {
            if (css.charAt(j) == '{') {
              depth++;
            } else if (css.charAt(j) == '}' && --depth == 0) {
              break;
            }
          }
          i = j + 1;
        } else {
          i = semi < 0 ? n : semi + 1;
        }
        continue;
      }
      int open = css.indexOf('{', i);
      if (open < 0) {
        break;
      }
      int close = css.indexOf('}', open);
      if (close < 0) {
        close = n;
      }
      String sels = css.substring(i, open);
      String decls = css.substring(open + 1, close);
      i = close + 1;
      SimpleAttributeSet props = new SimpleAttributeSet();
      CSS.parseDeclarations(decls, props);
      for (String sel : sels.split(",")) {
        addSelector(sel, props);
      }
    }
  }

  private void addSelector(String sel, AttributeSet props) {
    String clean = cleanSelector(sel.replace(">", " "));
    if (clean.length() == 0) {
      return;
    }
    Style style = getStyle(clean);
    Rule existing = null;
    for (Rule r : rules) {
      if (r.selector.equals(clean)) {
        existing = r;
      }
    }
    if (style == null) {
      style = addStyle(clean, null);
    }
    style.addAttributes(props);
    if (existing != null) {
      return;
    }
    Rule r = new Rule();
    r.selector = clean;
    r.style = style;
    r.order = rules.size();
    String[] toks = clean.split(" ");
    r.parts = new Part[toks.length];
    int spec = 0;
    for (int k = 0; k < toks.length; k++) {
      Part p = new Part();
      String t = toks[k];
      int hash = t.indexOf('#');
      int dot = t.indexOf('.');
      int end = t.length();
      if (hash >= 0) {
        p.id = t.substring(hash + 1);
        end = hash;
        spec += 100;
      }
      if (dot >= 0 && (hash < 0 || dot < hash)) {
        int de = hash >= 0 ? hash : t.length();
        p.cls = t.substring(dot + 1, de);
        end = dot;
        spec += 10;
      } else if (dot >= 0) {
        p.cls = t.substring(dot + 1);
        end = Math.min(end, dot);
        spec += 10;
      }
      String tag = t.substring(0, end);
      if (tag.length() > 0 && !tag.equals("*")) {
        p.tag = tag;
        spec += 1;
      }
      r.parts[k] = p;
    }
    r.specificity = spec;
    rules.add(r);
  }

  public void loadRules(Reader in, URL ref) throws IOException {
    StringBuilder sb = new StringBuilder();
    char[] buf = new char[4096];
    int len;
    while ((len = in.read(buf)) > 0) {
      sb.append(buf, 0, len);
    }
    addRule(sb.toString());
  }

  public void importStyleSheet(URL url) {
    try {
      Reader r = new BufferedReader(new InputStreamReader(url.openStream()));
      loadRules(r, url);
      r.close();
    } catch (Throwable e) {
      // unreadable sheets are skipped
    }
  }

  public void addStyleSheet(StyleSheet ss) {
    if (!linked.contains(ss) && ss != this) {
      linked.add(ss);
    }
  }

  public void removeStyleSheet(StyleSheet ss) { linked.remove(ss); }

  public StyleSheet[] getStyleSheets() {
    if (linked.isEmpty()) {
      return null;
    }
    return linked.toArray(new StyleSheet[0]);
  }

  public URL getBase() { return base; }

  public void setBase(URL base) { this.base = base; }

  public AttributeSet getDeclaration(String decl) {
    if (decl == null) {
      return SimpleAttributeSet.EMPTY;
    }
    SimpleAttributeSet out = new SimpleAttributeSet();
    CSS.parseDeclarations(decl, out);
    return out;
  }

  public void addCSSAttribute(MutableAttributeSet attr, CSS.Attribute key, String value) {
    CSS.addProperty(attr, key.toString(), value);
  }

  public boolean addCSSAttributeFromHTML(MutableAttributeSet attr, CSS.Attribute key, String value) {
    CSS.addProperty(attr, key.toString(), value);
    return true;
  }

  public AttributeSet translateHTMLToCSS(AttributeSet htmlAttrSet) {
    return translate(htmlAttrSet);
  }

  AttributeSet translate(AttributeSet htmlAttr) {
    SimpleAttributeSet out = new SimpleAttributeSet();
    Object name = htmlAttr.getAttribute(StyleConstants.NameAttribute);
    HTML.Tag tag = name instanceof HTML.Tag ? (HTML.Tag) name : null;
    for (Enumeration<?> en = htmlAttr.getAttributeNames(); en.hasMoreElements();) {
      Object k = en.nextElement();
      if (k instanceof CSS.Attribute) {
        out.addAttribute(k, htmlAttr.getAttribute(k));
      }
    }
    String v;
    if ((v = str(htmlAttr, HTML.Attribute.WIDTH)) != null && tag != HTML.Tag.TD && tag != HTML.Tag.TH) {
      out.addAttribute(CSS.Attribute.WIDTH, new CSS.Value(lenOrPx(v)));
    }
    if ((v = str(htmlAttr, HTML.Attribute.HEIGHT)) != null) {
      out.addAttribute(CSS.Attribute.HEIGHT, new CSS.Value(lenOrPx(v)));
    }
    if ((v = str(htmlAttr, HTML.Attribute.BGCOLOR)) != null) {
      out.addAttribute(CSS.Attribute.BACKGROUND_COLOR, new CSS.Value(v));
    }
    if ((v = str(htmlAttr, HTML.Attribute.ALIGN)) != null) {
      if (tag == HTML.Tag.IMG) {
        if (v.equals("left") || v.equals("right")) {
          out.addAttribute(CSS.Attribute.FLOAT, new CSS.Value(v));
        } else {
          out.addAttribute(CSS.Attribute.VERTICAL_ALIGN, new CSS.Value(v));
        }
      } else if (tag != HTML.Tag.TABLE) {
        out.addAttribute(CSS.Attribute.TEXT_ALIGN, new CSS.Value(v));
      }
    }
    if ((v = str(htmlAttr, HTML.Attribute.NOWRAP)) != null) {
      out.addAttribute(CSS.Attribute.WHITE_SPACE, new CSS.Value("nowrap"));
    }
    if ((v = str(htmlAttr, HTML.Attribute.VALIGN)) != null) {
      out.addAttribute(CSS.Attribute.VERTICAL_ALIGN, new CSS.Value(v));
    }
    if ((v = str(htmlAttr, HTML.Attribute.TYPE)) != null && (tag == HTML.Tag.UL || tag == HTML.Tag.OL
        || tag == HTML.Tag.LI)) {
      String t = v;
      if (tag != HTML.Tag.UL) {
        t = v.equals("1") ? "decimal" : v.equals("a") ? "lower-alpha" : v.equals("A") ? "upper-alpha"
            : v.equals("i") ? "lower-roman" : v.equals("I") ? "upper-roman" : v;
      }
      out.addAttribute(CSS.Attribute.LIST_STYLE_TYPE, new CSS.Value(t.toLowerCase(Locale.ROOT)));
    }
    if ((v = str(htmlAttr, HTML.Attribute.BORDER)) != null && (tag == HTML.Tag.TABLE || tag == HTML.Tag.IMG)) {
      String w = lenOrPx(v);
      if (tag == HTML.Tag.TABLE) {
        CSS.Len bl = CSS.Len.parse(w);
        if (bl != null && bl.value > 0 && bl.value < 2) {
          w = "2";
        }
      }
      String style = tag == HTML.Tag.TABLE ? "outset" : "solid";
      CSS.Attribute[] ws = {CSS.Attribute.BORDER_TOP_WIDTH, CSS.Attribute.BORDER_RIGHT_WIDTH,
          CSS.Attribute.BORDER_BOTTOM_WIDTH, CSS.Attribute.BORDER_LEFT_WIDTH};
      CSS.Attribute[] ss = {CSS.Attribute.BORDER_TOP_STYLE, CSS.Attribute.BORDER_RIGHT_STYLE,
          CSS.Attribute.BORDER_BOTTOM_STYLE, CSS.Attribute.BORDER_LEFT_STYLE};
      for (int i = 0; i < 4; i++) {
        out.addAttribute(ws[i], new CSS.Value(w));
        out.addAttribute(ss[i], new CSS.Value(style));
      }
    }
    if ((v = str(htmlAttr, HTML.Attribute.HSPACE)) != null) {
      String w = lenOrPx(v);
      out.addAttribute(CSS.Attribute.MARGIN_LEFT, new CSS.Value(w));
      out.addAttribute(CSS.Attribute.MARGIN_RIGHT, new CSS.Value(w));
    }
    if ((v = str(htmlAttr, HTML.Attribute.VSPACE)) != null) {
      String w = lenOrPx(v);
      out.addAttribute(CSS.Attribute.MARGIN_TOP, new CSS.Value(w));
      out.addAttribute(CSS.Attribute.MARGIN_BOTTOM, new CSS.Value(w));
    }
    Object styleAttr = htmlAttr.getAttribute(HTML.Attribute.STYLE);
    if (styleAttr != null) {
      CSS.parseDeclarations(styleAttr.toString(), out);
    }
    return out;
  }

  private static String str(AttributeSet a, Object key) {
    Object v = a.getAttribute(key);
    return v == null ? null : v.toString();
  }

  private static String lenOrPx(String v) {
    return CSS.Len.parse(v) != null ? v.trim() : "0";
  }

  public AttributeSet getViewAttributes(View v) { return new ViewAttributeSet(v); }

  final class ViewAttributeSet implements AttributeSet {
    private final View host;
    private AttributeSet translated;
    private AttributeSet ruleSet;
    private final AttributeSet htmlAttr;

    ViewAttributeSet(View v) {
      host = v;
      Element elem = v.getElement();
      htmlAttr = elem.getAttributes();
      List<Part> chain = new ArrayList<Part>();
      buildChain(elem, chain);
      ruleSet = resolve(chain);
      translated = translate(htmlAttr);
      Object nm = htmlAttr.getAttribute(StyleConstants.NameAttribute);
      if (nm == HTML.Tag.TD || nm == HTML.Tag.TH) {
        applyTableCell(elem);
      }
    }

    private void applyTableCell(Element elem) {
      Element t = elem.getParentElement();
      while (t != null && t.getAttributes().getAttribute(StyleConstants.NameAttribute) != HTML.Tag.TABLE) {
        t = t.getParentElement();
      }
      if (t == null) {
        return;
      }
      SimpleAttributeSet extra = new SimpleAttributeSet(translated);
      Object b = t.getAttributes().getAttribute(HTML.Attribute.BORDER);
      CSS.Len bl = b == null ? null : CSS.Len.parse(b.toString());
      if (bl != null && bl.value > 0) {
        CSS.Attribute[] ws = {CSS.Attribute.BORDER_TOP_WIDTH, CSS.Attribute.BORDER_RIGHT_WIDTH,
            CSS.Attribute.BORDER_BOTTOM_WIDTH, CSS.Attribute.BORDER_LEFT_WIDTH};
        CSS.Attribute[] ss = {CSS.Attribute.BORDER_TOP_STYLE, CSS.Attribute.BORDER_RIGHT_STYLE,
            CSS.Attribute.BORDER_BOTTOM_STYLE, CSS.Attribute.BORDER_LEFT_STYLE};
        for (int i = 0; i < 4; i++) {
          extra.addAttribute(ws[i], new CSS.Value("1"));
          extra.addAttribute(ss[i], new CSS.Value("inset"));
        }
      }
      Object cp = t.getAttributes().getAttribute(HTML.Attribute.CELLPADDING);
      CSS.Len pl = cp == null ? null : CSS.Len.parse(cp.toString());
      if (pl != null) {
        CSS.Attribute[] ps = {CSS.Attribute.PADDING_TOP, CSS.Attribute.PADDING_RIGHT, CSS.Attribute.PADDING_BOTTOM,
            CSS.Attribute.PADDING_LEFT};
        for (CSS.Attribute p : ps) {
          extra.addAttribute(p, new CSS.Value(cp.toString().trim()));
        }
      }
      translated = extra;
    }

    public int getAttributeCount() { return htmlAttr.getAttributeCount(); }

    public boolean isDefined(Object key) { return getAttribute(key) != null; }

    public boolean isEqual(AttributeSet attr) { return this == attr; }

    public AttributeSet copyAttributes() {
      SimpleAttributeSet s = new SimpleAttributeSet();
      s.addAttributes(ruleSet);
      s.addAttributes(translated);
      return s;
    }

    public Enumeration<?> getAttributeNames() { return copyAttributes().getAttributeNames(); }

    public boolean containsAttribute(Object name, Object value) { return value.equals(getAttribute(name)); }

    public boolean containsAttributes(AttributeSet attributes) {
      for (Enumeration<?> en = attributes.getAttributeNames(); en.hasMoreElements();) {
        Object k = en.nextElement();
        if (!containsAttribute(k, attributes.getAttribute(k))) {
          return false;
        }
      }
      return true;
    }

    public AttributeSet getResolveParent() {
      if (host == null) {
        return null;
      }
      View parent = host.getParent();
      return (parent != null) ? parent.getAttributes() : null;
    }

    public Object getAttribute(Object key) {
      if (key instanceof StyleConstants) {
        Object v = fromConstants(key);
        if (v != null) {
          return v;
        }
      }
      return doGetAttribute(key);
    }

    private Object doGetAttribute(Object key) {
      Object v = translated.getAttribute(key);
      if (v == null && key instanceof CSS.Attribute) {
        v = ruleSet.getAttribute(key);
      }
      if (v == null) {
        v = htmlAttr.getAttribute(key);
      }
      if (v != null) {
        return v;
      }
      if (key instanceof CSS.Attribute) {
        CSS.Attribute css = (CSS.Attribute) key;
        if (css.isInherited()) {
          AttributeSet parent = getResolveParent();
          if (parent != null) {
            return parent.getAttribute(key);
          }
        }
      }
      return null;
    }

    private Object fromConstants(Object key) {
      Object css = null;
      if (key == StyleConstants.Alignment) {
        css = doGetAttribute(CSS.Attribute.TEXT_ALIGN);
        if (css != null) {
          String s = css.toString();
          return Integer.valueOf(s.equals("center") ? StyleConstants.ALIGN_CENTER : s.equals("right")
              ? StyleConstants.ALIGN_RIGHT : s.equals("justify") ? StyleConstants.ALIGN_JUSTIFIED
              : StyleConstants.ALIGN_LEFT);
        }
      } else if (key == StyleConstants.FirstLineIndent) {
        css = doGetAttribute(CSS.Attribute.TEXT_INDENT);
        if (css != null) {
          CSS.Len l = CSS.Len.parse(css.toString());
          return l == null ? null : Float.valueOf(l.get(false));
        }
      }
      return null;
    }
  }

  float parentFontSize(AttributeSet a) {
    AttributeSet p = a == null ? null : a.getResolveParent();
    if (p == null) {
      return sysFontSize;
    }
    return fontPoints(p);
  }

  float fontPoints(AttributeSet a) {
    Object v = a.getAttribute(CSS.Attribute.FONT_SIZE);
    if (v == null) {
      return sysFontSize;
    }
    String s = v.toString().toLowerCase(Locale.ROOT);
    int idx = CSS.sizeIndex(s);
    if (idx >= 0) {
      return CSS.SIZE_MAP[idx];
    }
    if (s.equals("larger") || s.equals("smaller")) {
      float pv = parentFontSize(a);
      int pi = getIndexOfSize(pv);
      int ni = Math.max(0, Math.min(6, pi + (s.equals("larger") ? 1 : -1)));
      return CSS.SIZE_MAP[ni];
    }
    CSS.Len l = CSS.Len.parse(s);
    if (l == null) {
      return sysFontSize;
    }
    if (l.units.length() == 0 && l.value == Math.floor(l.value)) {
      int i = (int) l.value - 1;
      return CSS.SIZE_MAP[Math.max(0, Math.min(6, i))];
    }
    if (l.percent) {
      return l.value / 100f * parentFontSize(a);
    }
    if (l.units.equals("em") || l.units.equals("ex")) {
      return l.value * parentFontSize(a);
    }
    return l.get(false);
  }

  public static int getIndexOfSize(float pt) {
    int[] sizes = CSS.SIZE_MAP;
    for (int i = 0; i < sizes.length; i++) {
      if (pt <= sizes[i]) {
        return i;
      }
    }
    return sizes.length - 1;
  }

  public float getPointSize(int index) {
    return CSS.SIZE_MAP[Math.max(0, Math.min(6, index))];
  }

  public float getPointSize(String size) {
    int idx = CSS.sizeIndex(size);
    return idx >= 0 ? CSS.SIZE_MAP[idx] : sysFontSize;
  }

  public void setBaseFontSize(int sz) {
    baseFontSize = Math.max(0, Math.min(7, sz));
  }

  public void setBaseFontSize(String size) {
    int idx = CSS.sizeIndex(size);
    if (idx >= 0) {
      baseFontSize = idx;
    }
  }

  public Color stringToColor(String string) {
    Color c = CSS.parseColor(string);
    return c;
  }

  public Font getFont(AttributeSet a) {
    int size = (int) fontPoints(a);
    Object vAlign = a.getAttribute(CSS.Attribute.VERTICAL_ALIGN);
    if (vAlign != null) {
      String v = vAlign.toString();
      if ((v.indexOf("sup") >= 0) || (v.indexOf("sub") >= 0)) {
        size -= 2;
      }
    }
    Object fam = a.getAttribute(CSS.Attribute.FONT_FAMILY);
    String family = fam != null ? firstFamily(fam.toString()) : Font.SANS_SERIF;
    int style = Font.PLAIN;
    Object w = a.getAttribute(CSS.Attribute.FONT_WEIGHT);
    if (w != null) {
      String ws = w.toString();
      if (ws.equals("bold") || ws.equals("bolder")) {
        style |= Font.BOLD;
      } else if (ws.matches("[0-9]+") && Integer.parseInt(ws) > 400) {
        style |= Font.BOLD;
      }
    }
    Object fs = a.getAttribute(CSS.Attribute.FONT_STYLE);
    if (fs != null && fs.toString().indexOf("italic") >= 0) {
      style |= Font.ITALIC;
    }
    if (family.equalsIgnoreCase("monospace")) {
      family = Font.MONOSPACED;
    }
    Font f = getFont(family, style, size);
    if (f == null || (f.getFamily().equals(Font.DIALOG) && !family.equalsIgnoreCase(Font.DIALOG))) {
      f = getFont(Font.SANS_SERIF, style, size);
    }
    return f;
  }

  private static String firstFamily(String list) {
    String f = list.split(",")[0].trim();
    if (f.length() > 1 && (f.charAt(0) == '"' || f.charAt(0) == '\'')) {
      f = f.substring(1, f.length() - 1);
    }
    return f;
  }

  public Color getForeground(AttributeSet a) {
    Object c = a.getAttribute(CSS.Attribute.COLOR);
    Color col = c == null ? null : CSS.parseColor(c.toString());
    return col == null ? Color.black : col;
  }

  public Color getBackground(AttributeSet a) {
    Object c = a.getAttribute(CSS.Attribute.BACKGROUND_COLOR);
    return c == null ? null : CSS.parseColor(c.toString());
  }

  public BoxPainter getBoxPainter(AttributeSet a) { return new BoxPainter(a, this); }

  public ListPainter getListPainter(AttributeSet a) { return new ListPainter(a, this); }

  public static final class BoxPainter implements Serializable {
    float topMargin;
    float bottomMargin;
    float leftMargin;
    float rightMargin;
    short topPadding;
    short bottomPadding;
    short leftPadding;
    short rightPadding;
    Insets border = new Insets(0, 0, 0, 0);
    Color[] borderColors = new Color[4];
    String[] borderStyles = new String[4];
    Color bg;
    final boolean w3c = false;
    AttributeSet attrs;

    BoxPainter(AttributeSet a, StyleSheet ss) {
      attrs = a;
      topMargin = len(a, CSS.Attribute.MARGIN_TOP, 0);
      bottomMargin = len(a, CSS.Attribute.MARGIN_BOTTOM, 0);
      leftMargin = len(a, CSS.Attribute.MARGIN_LEFT, Integer.MIN_VALUE);
      rightMargin = len(a, CSS.Attribute.MARGIN_RIGHT, Integer.MIN_VALUE);
      topPadding = (short) len(a, CSS.Attribute.PADDING_TOP, 0);
      bottomPadding = (short) len(a, CSS.Attribute.PADDING_BOTTOM, 0);
      leftPadding = (short) len(a, CSS.Attribute.PADDING_LEFT, 0);
      rightPadding = (short) len(a, CSS.Attribute.PADDING_RIGHT, 0);
      CSS.Attribute[] w = {CSS.Attribute.BORDER_TOP_WIDTH, CSS.Attribute.BORDER_LEFT_WIDTH,
          CSS.Attribute.BORDER_BOTTOM_WIDTH, CSS.Attribute.BORDER_RIGHT_WIDTH};
      CSS.Attribute[] s = {CSS.Attribute.BORDER_TOP_STYLE, CSS.Attribute.BORDER_LEFT_STYLE,
          CSS.Attribute.BORDER_BOTTOM_STYLE, CSS.Attribute.BORDER_RIGHT_STYLE};
      CSS.Attribute[] c = {CSS.Attribute.BORDER_TOP_COLOR, CSS.Attribute.BORDER_LEFT_COLOR,
          CSS.Attribute.BORDER_BOTTOM_COLOR, CSS.Attribute.BORDER_RIGHT_COLOR};
      int[] px = new int[4];
      for (int i = 0; i < 4; i++) {
        Object st = a.getAttribute(s[i]);
        borderStyles[i] = st == null ? "none" : st.toString();
        Object cv = a.getAttribute(c[i]);
        borderColors[i] = cv == null ? null : CSS.parseColor(cv.toString());
        if (borderStyles[i].equals("none") || borderStyles[i].equals("hidden")) {
          px[i] = 0;
        } else {
          Object wv = a.getAttribute(w[i]);
          String ws = wv == null ? "medium" : wv.toString();
          px[i] = ws.equals("thin") ? 1 : ws.equals("medium") ? 2 : ws.equals("thick") ? 4 : (int) len(a, w[i], 2);
        }
      }
      border = new Insets(px[0], px[1], px[2], px[3]);
      bg = ss.getBackground(a);
    }

    private static float len(AttributeSet a, CSS.Attribute key, float def) {
      Object v = a.getAttribute(key);
      if (v == null) {
        return def;
      }
      CSS.Len l = CSS.Len.parse(v.toString());
      if (l == null) {
        return def;
      }
      return l.percent ? 0 : l.get(false);
    }

    float orientationMargin(boolean left, View v) {
      float m = left ? leftMargin : rightMargin;
      if (m != Integer.MIN_VALUE) {
        return m;
      }
      AttributeSet a = v.getAttributes();
      boolean ltr = true;
      java.awt.Container c = v.getContainer();
      if (c != null) {
        ltr = c.getComponentOrientation().isLeftToRight();
      }
      CSS.Attribute key = left ? (ltr ? CSS.Attribute.MARGIN_LEFT_LTR : CSS.Attribute.MARGIN_LEFT_RTL)
          : (ltr ? CSS.Attribute.MARGIN_RIGHT_LTR : CSS.Attribute.MARGIN_RIGHT_RTL);
      float o = len(a, key, Integer.MIN_VALUE);
      return o == Integer.MIN_VALUE ? 0 : o;
    }

    public float getInset(int side, View v) {
      float inset = 0;
      switch (side) {
        case View.LEFT:
          inset += orientationMargin(true, v) + border.left + leftPadding;
          break;
        case View.RIGHT:
          inset += orientationMargin(false, v) + border.right + rightPadding;
          break;
        case View.TOP:
          inset += topMargin + border.top + topPadding;
          break;
        case View.BOTTOM:
          inset += bottomMargin + border.bottom + bottomPadding;
          break;
        default:
          throw new IllegalArgumentException("Invalid side: " + side);
      }
      return inset;
    }

    public void paint(Graphics g, float x, float y, float w, float h, View v) {
      float dx = orientationMargin(true, v);
      float dy = topMargin;
      float dw = -(dx + orientationMargin(false, v));
      float dh = -(topMargin + bottomMargin);
      Rectangle r = new Rectangle((int) (x + dx), (int) (y + dy), (int) (w + dw), (int) (h + dh));
      if (bg != null) {
        g.setColor(bg);
        g.fillRect(r.x, r.y, r.width, r.height);
      }
      Color fg = v.getContainer() != null ? v.getContainer().getForeground() : Color.black;
      for (int i = 0; i < 4; i++) {
        int bw = i == 0 ? border.top : i == 1 ? border.left : i == 2 ? border.bottom : border.right;
        if (bw <= 0) {
          continue;
        }
        Color c = borderColors[i] != null ? borderColors[i] : fg;
        g.setColor(c);
        for (int k = 0; k < bw; k++) {
          switch (i) {
            case 0:
              g.drawLine(r.x, r.y + k, r.x + r.width - 1, r.y + k);
              break;
            case 1:
              g.drawLine(r.x + k, r.y, r.x + k, r.y + r.height - 1);
              break;
            case 2:
              g.drawLine(r.x, r.y + r.height - 1 - k, r.x + r.width - 1, r.y + r.height - 1 - k);
              break;
            default:
              g.drawLine(r.x + r.width - 1 - k, r.y, r.x + r.width - 1 - k, r.y + r.height - 1);
              break;
          }
        }
      }
    }
  }

  public static final class ListPainter implements Serializable {
    final String type;
    final String position;
    private final StyleSheet ss;

    ListPainter(AttributeSet attr, StyleSheet ss) {
      this.ss = ss;
      Object t = attr.getAttribute(CSS.Attribute.LIST_STYLE_TYPE);
      type = t == null ? "disc" : t.toString();
      Object p = attr.getAttribute(CSS.Attribute.LIST_STYLE_POSITION);
      position = p == null ? "outside" : p.toString();
    }

    public void paint(Graphics g, float x, float y, float w, float h, View v, int item) {
      View cv = v.getView(item);
      Object name = cv.getElement().getAttributes().getAttribute(StyleConstants.NameAttribute);
      if (name != HTML.Tag.LI || type.equals("none")) {
        return;
      }
      Rectangle r = new Rectangle((int) x, (int) y, (int) w, (int) h);
      g.setColor(v.getContainer() == null ? Color.black : v.getContainer().getForeground());
      java.awt.FontMetrics fm = g.getFontMetrics();
      int size = Math.max(4, fm.getAscent() / 3);
      int cy = r.y + fm.getAscent() / 2 + 2;
      int bx = r.x - 10 - size;
      if (type.equals("disc")) {
        g.fillOval(bx, cy - size / 2, size, size);
      } else if (type.equals("circle")) {
        g.drawOval(bx, cy - size / 2, size, size);
      } else if (type.equals("square")) {
        g.fillRect(bx, cy - size / 2, size, size);
      } else {
        String label = marker(type, item + 1) + ".";
        g.drawString(label, r.x - 8 - fm.stringWidth(label), r.y + fm.getAscent());
      }
    }

    private static String marker(String t, int n) {
      if (t.equals("lower-alpha") || t.equals("upper-alpha")) {
        String s = String.valueOf((char) ('a' + (n - 1) % 26));
        return t.startsWith("upper") ? s.toUpperCase() : s;
      }
      if (t.equals("lower-roman") || t.equals("upper-roman")) {
        String[] r = {"i", "ii", "iii", "iv", "v", "vi", "vii", "viii", "ix", "x"};
        String s = n <= 10 ? r[n - 1] : String.valueOf(n);
        return t.startsWith("upper") ? s.toUpperCase() : s;
      }
      return String.valueOf(n);
    }
  }
}
