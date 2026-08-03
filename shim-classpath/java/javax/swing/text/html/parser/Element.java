/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */
package javax.swing.text.html.parser;

import java.io.Serializable;
import java.util.BitSet;

public final class Element implements DTDConstants, Serializable {
  public int index;
  public String name;
  public boolean oStart;
  public boolean oEnd;
  public BitSet inclusions;
  public BitSet exclusions;
  public int type = ANY;
  public ContentModel content;
  public AttributeList atts;
  public Object data;

  Element() { }

  Element(String name, int index) {
    this.name = name;
    this.index = index;
  }

  public String getName() { return name; }

  public boolean omitStart() { return oStart; }

  public boolean omitEnd() { return oEnd; }

  public int getType() { return type; }

  public ContentModel getContent() { return content; }

  public AttributeList getAttributes() { return atts; }

  public int getIndex() { return index; }

  public boolean isEmpty() { return type == EMPTY; }

  public String toString() { return name; }

  public AttributeList getAttribute(String name) {
    for (AttributeList a = atts; a != null; a = a.next) {
      if (a.name.equals(name)) {
        return a;
      }
    }
    return null;
  }

  public AttributeList getAttributeByValue(String name) {
    if (name != null) {
      for (AttributeList a = atts; a != null; a = a.next) {
        if ((a.values != null) && a.values.contains(name)) {
          return a;
        }
      }
    }
    return null;
  }

  public static int name2type(String nm) {
    Integer val = typeMap.get(nm);
    return (val != null) ? val.intValue() : 0;
  }

  private static final java.util.Hashtable<String, Integer> typeMap = new java.util.Hashtable<String, Integer>();

  static {
    typeMap.put("CDATA", Integer.valueOf(CDATA));
    typeMap.put("RCDATA", Integer.valueOf(RCDATA));
    typeMap.put("EMPTY", Integer.valueOf(EMPTY));
    typeMap.put("ANY", Integer.valueOf(ANY));
  }
}
