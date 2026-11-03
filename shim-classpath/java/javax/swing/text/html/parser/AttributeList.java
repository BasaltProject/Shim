/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */
package javax.swing.text.html.parser;

import java.io.Serializable;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

public final class AttributeList implements DTDConstants, Serializable {
  public String name;
  public int type;
  public Vector<?> values;
  public int modifier;
  public String value;
  public AttributeList next;

  AttributeList() { }

  public AttributeList(String name) { this.name = name; }

  public AttributeList(String name, int type, int modifier, String value, Vector<?> values, AttributeList next) {
    this.name = name;
    this.type = type;
    this.modifier = modifier;
    this.value = value;
    this.values = values;
    this.next = next;
  }

  public String getName() { return name; }

  public int getType() { return type; }

  public int getModifier() { return modifier; }

  public Enumeration<?> getValues() { return (values != null) ? values.elements() : null; }

  public String getValue() { return value; }

  public AttributeList getNext() { return next; }

  public String toString() { return name; }

  static Hashtable<Object, Object> attributeTypes = new Hashtable<Object, Object>();

  static void defineAttributeType(String nm, int val) {
    Integer num = Integer.valueOf(val);
    attributeTypes.put(nm, num);
    attributeTypes.put(num, nm);
  }

  static {
    defineAttributeType("CDATA", CDATA);
    defineAttributeType("ENTITY", ENTITY);
    defineAttributeType("ENTITIES", ENTITIES);
    defineAttributeType("ID", ID);
    defineAttributeType("IDREF", IDREF);
    defineAttributeType("IDREFS", IDREFS);
    defineAttributeType("NAME", NAME);
    defineAttributeType("NAMES", NAMES);
    defineAttributeType("NMTOKEN", NMTOKEN);
    defineAttributeType("NMTOKENS", NMTOKENS);
    defineAttributeType("NOTATION", NOTATION);
    defineAttributeType("NUMBER", NUMBER);
    defineAttributeType("NUMBERS", NUMBERS);
    defineAttributeType("NUTOKEN", NUTOKEN);
    defineAttributeType("NUTOKENS", NUTOKENS);
    attributeTypes.put("fixed", Integer.valueOf(FIXED));
    attributeTypes.put("required", Integer.valueOf(REQUIRED));
    attributeTypes.put("current", Integer.valueOf(CURRENT));
    attributeTypes.put("conref", Integer.valueOf(CONREF));
    attributeTypes.put("implied", Integer.valueOf(IMPLIED));
  }

  public static int name2type(String nm) {
    Integer i = (Integer) attributeTypes.get(nm);
    return (i == null) ? CDATA : i.intValue();
  }

  public static String type2name(int tp) { return (String) attributeTypes.get(Integer.valueOf(tp)); }
}
