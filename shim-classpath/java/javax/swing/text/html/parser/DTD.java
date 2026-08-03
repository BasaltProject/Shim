/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */
package javax.swing.text.html.parser;

import java.io.IOException;
import java.util.BitSet;
import java.util.Hashtable;
import java.util.Vector;

public class DTD implements DTDConstants {
  public String name;
  public Vector<Element> elements = new Vector<Element>();
  public Hashtable<String, Element> elementHash = new Hashtable<String, Element>();
  public Hashtable<Object, Entity> entityHash = new Hashtable<Object, Entity>();
  public final Element pcdata;
  public final Element html;
  public final Element meta;
  public final Element base;
  public final Element isindex;
  public final Element head;
  public final Element body;
  public final Element applet;
  public final Element param;
  public final Element p;
  public final Element title;
  public static final int FILE_VERSION = 1;
  private static final Hashtable<String, DTD> dtdHash = new Hashtable<String, DTD>();

  protected DTD(String name) {
    this.name = name;
    pcdata = getElement("#pcdata");
    pcdata.type = CDATA;
    html = getElement("html");
    meta = getElement("meta");
    base = getElement("base");
    isindex = getElement("isindex");
    head = getElement("head");
    body = getElement("body");
    applet = getElement("applet");
    param = getElement("param");
    p = getElement("p");
    title = getElement("title");
  }

  public String getName() { return name; }

  public Element getElement(String name) {
    Element e = elementHash.get(name);
    if (e == null) {
      e = new Element(name, elements.size());
      elements.addElement(e);
      elementHash.put(name, e);
    }
    return e;
  }

  public Element getElement(int index) { return elements.elementAt(index); }

  public Entity getEntity(String name) { return entityHash.get(name); }

  public Entity getEntity(int ch) { return entityHash.get(Integer.valueOf(ch)); }

  boolean elementExists(String name) { return !"all".equals(name) && elementHash.get(name) != null; }

  public Entity defineEntity(String name, int type, char[] data) {
    Entity ent = entityHash.get(name);
    if (ent == null) {
      ent = new Entity(name, type, data);
      entityHash.put(name, ent);
      if (((type & GENERAL) != 0) && (data.length == 1)) {
        switch (type & ~GENERAL) {
          case CDATA:
          case SDATA:
            entityHash.put(Integer.valueOf(data[0]), ent);
            break;
          default:
            break;
        }
      }
    }
    return ent;
  }

  public Element defineElement(String name, int type, boolean omitStart, boolean omitEnd, ContentModel content,
      BitSet exclusions, BitSet inclusions, AttributeList atts) {
    Element e = getElement(name);
    e.type = type;
    e.oStart = omitStart;
    e.oEnd = omitEnd;
    e.content = content;
    e.exclusions = exclusions;
    e.inclusions = inclusions;
    e.atts = atts;
    return e;
  }

  public void defineAttributes(String name, AttributeList atts) {
    Element e = getElement(name);
    e.atts = atts;
  }

  protected Entity defEntity(String name, int type, String str) {
    char[] data = str.toCharArray();
    return defineEntity(name, type, data);
  }

  public Entity defEntity(String name, int type, int ch) {
    char[] data = {(char) ch};
    return defineEntity(name, type, data);
  }

  protected Element defElement(String name, int type, boolean omitStart, boolean omitEnd, ContentModel content,
      String[] exclusions, String[] inclusions, AttributeList atts) {
    BitSet excl = null;
    if (exclusions != null && exclusions.length > 0) {
      excl = new BitSet();
      for (String str : exclusions) {
        if (str.length() > 0) {
          excl.set(getElement(str).getIndex());
        }
      }
    }
    BitSet incl = null;
    if (inclusions != null && inclusions.length > 0) {
      incl = new BitSet();
      for (String str : inclusions) {
        if (str.length() > 0) {
          incl.set(getElement(str).getIndex());
        }
      }
    }
    return defineElement(name, type, omitStart, omitEnd, content, excl, incl, atts);
  }

  protected AttributeList defAttributeList(String name, int type, int modifier, String value, String values,
      AttributeList atts) {
    Vector<String> vals = null;
    if (values != null) {
      vals = new Vector<String>();
      for (java.util.StringTokenizer s = new java.util.StringTokenizer(values, "|"); s.hasMoreTokens();) {
        String str = s.nextToken();
        if (str.length() > 0) {
          vals.addElement(str);
        }
      }
    }
    return new AttributeList(name, type, modifier, value, vals, atts);
  }

  protected ContentModel defContentModel(int type, Object obj, ContentModel next) {
    return new ContentModel(type, obj, next);
  }

  public String toString() { return name; }

  public static void putDTDHash(String name, DTD dtd) { dtdHash.put(name, dtd); }

  public static DTD getDTD(String name) throws IOException {
    name = name.toLowerCase();
    DTD dtd = dtdHash.get(name);
    if (dtd == null) {
      dtd = new DTD(name);
      HtmlDtdData.populate(dtd);
      dtdHash.put(name, dtd);
    }
    return dtd;
  }

  public void read(java.io.DataInputStream in) throws IOException {
    throw new IOException("binary DTD files are not supported");
  }
}
