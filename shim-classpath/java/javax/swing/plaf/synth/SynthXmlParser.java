/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */
package javax.swing.plaf.synth;

import java.awt.Color;
import java.awt.Font;
import java.awt.Insets;
import java.io.InputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.FontUIResource;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

final class SynthXmlParser extends SynthStyleFactory {
  private static final class Binding {
    final SynthXmlStyle style;
    final boolean byName;
    final Pattern key;

    Binding(SynthXmlStyle style, boolean byName, Pattern key) {
      this.style = style;
      this.byName = byName;
      this.key = key;
    }
  }

  private final Class<?> resourceBase;
  private final Map<String, Object> named = new HashMap<String, Object>();
  private final Map<String, SynthXmlStyle> styles = new HashMap<String, SynthXmlStyle>();
  private final List<Binding> bindings = new ArrayList<Binding>();
  private final Map<String, Object> defaultsProperties = new HashMap<String, Object>();
  private final SynthStyle fallback;

  SynthXmlParser(Class<?> resourceBase, SynthStyle fallback) {
    this.resourceBase = resourceBase;
    this.fallback = fallback;
  }

  Map<String, Object> defaultsProperties() {
    return defaultsProperties;
  }

  void parse(InputStream in) throws ParseException {
    Document doc;
    try {
      doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(in);
    } catch (Exception e) {
      throw new ParseException("Invalid Synth XML: " + e, 0);
    }
    Element root = doc.getDocumentElement();
    if (!"synth".equals(root.getTagName())) {
      throw new ParseException("Root element must be synth", 0);
    }
    for (Element e : children(root)) {
      String tag = e.getTagName();
      if (tag.equals("style")) {
        parseStyle(e);
      } else if (tag.equals("bind")) {
        parseBind(e);
      } else if (tag.equals("defaultsProperty")) {
        defaultsProperties.put(e.getAttribute("key"), value(e.getAttribute("type"), e.getAttribute("value")));
      } else if (e.hasAttribute("id")) {
        named.put(e.getAttribute("id"), parseValue(e));
      }
    }
  }

  private static List<Element> children(Element parent) {
    List<Element> out = new ArrayList<Element>();
    NodeList nl = parent.getChildNodes();
    for (int i = 0; i < nl.getLength(); i++) {
      Node n = nl.item(i);
      if (n instanceof Element) {
        out.add((Element) n);
      }
    }
    return out;
  }

  private Object parseValue(Element e) throws ParseException {
    String tag = e.getTagName();
    if (tag.equals("color")) {
      return color(e);
    } else if (tag.equals("font")) {
      return font(e);
    } else if (tag.equals("insets")) {
      return insets(e);
    } else if (tag.equals("imageIcon")) {
      java.net.URL url = resourceBase == null ? null : resourceBase.getResource(e.getAttribute("path"));
      return url == null ? null : new ImageIcon(url);
    } else if (tag.equals("object")) {
      try {
        return Class.forName(e.getAttribute("class")).getDeclaredConstructor().newInstance();
      } catch (Exception ex) {
        throw new ParseException("Cannot create " + e.getAttribute("class"), 0);
      }
    }
    return null;
  }

  private Color color(Element e) throws ParseException {
    if (e.hasAttribute("idref")) {
      return (Color) named.get(e.getAttribute("idref"));
    }
    String v = e.getAttribute("value");
    if (v.length() == 0) {
      v = e.getAttribute("name");
    }
    try {
      if (v.startsWith("#")) {
        return new ColorUIResource(new Color((int) Long.parseLong(v.substring(1), 16), v.length() > 7));
      }
      java.lang.reflect.Field f = Color.class.getField(v.toLowerCase());
      return new ColorUIResource((Color) f.get(null));
    } catch (Exception ex) {
      throw new ParseException("Invalid color " + v, 0);
    }
  }

  private Font font(Element e) throws ParseException {
    if (e.hasAttribute("idref")) {
      return (Font) named.get(e.getAttribute("idref"));
    }
    int style = Font.PLAIN;
    String s = e.getAttribute("style");
    if (s.contains("BOLD")) {
      style |= Font.BOLD;
    }
    if (s.contains("ITALIC")) {
      style |= Font.ITALIC;
    }
    try {
      return new FontUIResource(e.getAttribute("name"), style, Integer.parseInt(e.getAttribute("size")));
    } catch (NumberFormatException ex) {
      throw new ParseException("Invalid font size", 0);
    }
  }

  private Insets insets(Element e) throws ParseException {
    if (e.hasAttribute("idref")) {
      return (Insets) named.get(e.getAttribute("idref"));
    }
    try {
      return new javax.swing.plaf.InsetsUIResource(Integer.parseInt(e.getAttribute("top")),
          Integer.parseInt(e.getAttribute("left")), Integer.parseInt(e.getAttribute("bottom")),
          Integer.parseInt(e.getAttribute("right")));
    } catch (NumberFormatException ex) {
      throw new ParseException("Invalid insets", 0);
    }
  }

  private static Object value(String type, String v) {
    if ("boolean".equals(type) || "BOOLEAN".equals(type)) {
      return Boolean.valueOf(v);
    } else if ("integer".equals(type) || "INTEGER".equals(type)) {
      return Integer.valueOf(v);
    } else if ("float".equals(type) || "FLOAT".equals(type)) {
      return Float.valueOf(v);
    } else if ("double".equals(type) || "DOUBLE".equals(type)) {
      return Double.valueOf(v);
    } else if ("long".equals(type) || "LONG".equals(type)) {
      return Long.valueOf(v);
    }
    return v;
  }

  private static int stateMask(String value) throws ParseException {
    if (value == null || value.length() == 0) {
      return 0;
    }
    int mask = 0;
    for (String part : value.split("\\s+AND\\s+")) {
      String p = part.trim();
      if (p.equals("ENABLED")) {
        mask |= SynthConstants.ENABLED;
      } else if (p.equals("MOUSE_OVER")) {
        mask |= SynthConstants.MOUSE_OVER;
      } else if (p.equals("PRESSED")) {
        mask |= SynthConstants.PRESSED;
      } else if (p.equals("DISABLED")) {
        mask |= SynthConstants.DISABLED;
      } else if (p.equals("FOCUSED")) {
        mask |= SynthConstants.FOCUSED;
      } else if (p.equals("SELECTED")) {
        mask |= SynthConstants.SELECTED;
      } else if (p.equals("DEFAULT")) {
        mask |= SynthConstants.DEFAULT;
      } else {
        throw new ParseException("Unknown state " + p, 0);
      }
    }
    return mask;
  }

  private void parseStyle(Element e) throws ParseException {
    SynthXmlStyle style = new SynthXmlStyle();
    for (Element c : children(e)) {
      String tag = c.getTagName();
      if (tag.equals("state")) {
        SynthXmlStyle.StateData d = style.stateData(stateMask(c.getAttribute("value")));
        for (Element s : children(c)) {
          if (s.getTagName().equals("font")) {
            d.font = font(s);
          } else if (s.getTagName().equals("color")) {
            fillColor(d, s);
          }
        }
      } else if (tag.equals("font")) {
        style.stateData(0).font = font(c);
      } else if (tag.equals("color")) {
        fillColor(style.stateData(0), c);
      } else if (tag.equals("insets")) {
        style.insets = insets(c);
      } else if (tag.equals("opaque")) {
        style.opaque = Boolean.valueOf(c.getAttribute("value"));
      } else if (tag.equals("property")) {
        Object v = value(c.getAttribute("type"), c.getAttribute("value"));
        style.properties.put(c.getAttribute("key"), v);
      }
    }
    if (e.hasAttribute("id")) {
      styles.put(e.getAttribute("id"), style);
    }
  }

  private void fillColor(SynthXmlStyle.StateData d, Element c) throws ParseException {
    Color color = color(c);
    String type = c.getAttribute("type");
    if (type.length() == 0) {
      for (int i = 0; i < d.colors.length; i++) {
        d.colors[i] = color;
      }
      return;
    }
    ColorType ct;
    if (type.equals("BACKGROUND")) {
      ct = ColorType.BACKGROUND;
    } else if (type.equals("FOREGROUND")) {
      ct = ColorType.FOREGROUND;
    } else if (type.equals("FOCUS")) {
      ct = ColorType.FOCUS;
    } else if (type.equals("TEXT_BACKGROUND")) {
      ct = ColorType.TEXT_BACKGROUND;
    } else if (type.equals("TEXT_FOREGROUND")) {
      ct = ColorType.TEXT_FOREGROUND;
    } else {
      throw new ParseException("Unknown color type " + type, 0);
    }
    d.colors[ct.getID()] = color;
  }

  private void parseBind(Element e) throws ParseException {
    SynthXmlStyle style = styles.get(e.getAttribute("style"));
    if (style == null) {
      throw new ParseException("Unknown style " + e.getAttribute("style"), 0);
    }
    String type = e.getAttribute("type");
    bindings.add(new Binding(style, type.equalsIgnoreCase("name"),
        Pattern.compile(e.getAttribute("key"), Pattern.CASE_INSENSITIVE)));
  }

  public SynthStyle getStyle(JComponent c, Region id) {
    SynthXmlStyle merged = null;
    for (Binding b : bindings) {
      String subject = b.byName ? c.getName() : id.getName();
      if (subject != null && b.key.matcher(subject).matches()) {
        if (merged == null) {
          merged = new SynthXmlStyle();
        }
        merged.mergeFrom(b.style);
      }
    }
    return merged != null ? merged : fallback;
  }
}
