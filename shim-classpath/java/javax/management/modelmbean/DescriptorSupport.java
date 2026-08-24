/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.management.modelmbean;

import java.lang.reflect.Constructor;
import java.util.Map;
import java.util.TreeMap;
import javax.management.Descriptor;
import javax.management.MBeanException;
import javax.management.RuntimeOperationsException;

public class DescriptorSupport implements Descriptor {
  private static final long serialVersionUID = -6292969195866300415L;

  private transient Map<String, Object> descriptorMap;

  private static final String[] kDescriptorTypes = {"mbean", "attribute", "operation", "constructor", "notification"};
  private static final String[] kPersistPolicies = {"OnUpdate", "OnTimer", "NoMoreOftenThan", "Always", "Never", "OnUnregister"};

  public DescriptorSupport() {
    descriptorMap = new TreeMap<String, Object>(String.CASE_INSENSITIVE_ORDER);
  }

  public DescriptorSupport(int initNumFields) throws MBeanException, RuntimeOperationsException {
    if (initNumFields <= 0) {
      throw new RuntimeOperationsException(new IllegalArgumentException("Descriptor field limit invalid: " + initNumFields),
          "Descriptor field limit invalid: " + initNumFields);
    }
    descriptorMap = new TreeMap<String, Object>(String.CASE_INSENSITIVE_ORDER);
  }

  public DescriptorSupport(DescriptorSupport inDescr) {
    if (inDescr == null) {
      descriptorMap = new TreeMap<String, Object>(String.CASE_INSENSITIVE_ORDER);
    } else {
      descriptorMap = new TreeMap<String, Object>(String.CASE_INSENSITIVE_ORDER);
      descriptorMap.putAll(inDescr.descriptorMap);
    }
  }

  public DescriptorSupport(String inStr) throws MBeanException, RuntimeOperationsException, XMLParseException {
    if (inStr == null) {
      throw new RuntimeOperationsException(new IllegalArgumentException("String in parameter is null"),
          "String in parameter is null");
    }
    descriptorMap = new TreeMap<String, Object>(String.CASE_INSENSITIVE_ORDER);
    parseXml(inStr);
  }

  public DescriptorSupport(String[] fieldNames, Object[] fieldValues) throws RuntimeOperationsException {
    if (fieldNames == null || fieldValues == null || fieldNames.length != fieldValues.length) {
      throw new RuntimeOperationsException(new IllegalArgumentException("Null or invalid fieldNames or fieldValues"),
          "Null or invalid fieldNames or fieldValues");
    }
    descriptorMap = new TreeMap<String, Object>(String.CASE_INSENSITIVE_ORDER);
    for (int i = 0; i < fieldNames.length; i++) {
      setField(fieldNames[i], fieldValues[i]);
    }
  }

  public DescriptorSupport(String... fields) {
    descriptorMap = new TreeMap<String, Object>(String.CASE_INSENSITIVE_ORDER);
    if (fields == null) {
      return;
    }
    for (String field : fields) {
      if (field == null || field.equals("")) {
        continue;
      }
      int eq = field.indexOf('=');
      if (eq < 0) {
        throw new RuntimeOperationsException(new IllegalArgumentException("Illegal arguments: field does not have '=' as a name and value separator"),
            "Illegal arguments: field does not have '=' as a name and value separator");
      }
      if (eq == 0) {
        throw new RuntimeOperationsException(new IllegalArgumentException("Illegal arguments: field name is empty"),
            "Illegal arguments: field name is empty");
      }
      String name = field.substring(0, eq);
      String value = field.substring(eq + 1);
      setField(name, value.equals("") ? null : value);
    }
  }

  public synchronized Object getFieldValue(String fieldName) throws RuntimeOperationsException {
    if (fieldName == null || fieldName.equals("")) {
      throw new RuntimeOperationsException(new IllegalArgumentException("Illegal arguments: null field name"),
          "Illegal arguments: null field name");
    }
    return descriptorMap.get(fieldName);
  }

  public synchronized void setField(String fieldName, Object fieldValue) throws RuntimeOperationsException {
    if (fieldName == null || fieldName.equals("")) {
      throw new RuntimeOperationsException(new IllegalArgumentException("Illegal arguments: null field name"),
          "Illegal arguments: null field name");
    }
    if (validateField(fieldName, fieldValue)) {
      descriptorMap.put(fieldName, fieldValue);
    } else {
      throw new RuntimeOperationsException(new IllegalArgumentException("Field value invalid: " + fieldName + "=" + fieldValue),
          "Field value invalid: " + fieldName + "=" + fieldValue);
    }
  }

  public synchronized String[] getFields() {
    String[] result = new String[descriptorMap.size()];
    int i = 0;
    for (Map.Entry<String, Object> e : descriptorMap.entrySet()) {
      Object value = e.getValue();
      if (value == null) {
        value = "";
      } else if (!(value instanceof String)) {
        value = "(" + value.toString() + ")";
      }
      result[i++] = e.getKey() + "=" + value;
    }
    return result;
  }

  public synchronized String[] getFieldNames() {
    return descriptorMap.keySet().toArray(new String[0]);
  }

  public synchronized Object[] getFieldValues(String... fieldNames) {
    if (fieldNames == null) {
      return descriptorMap.values().toArray();
    }
    Object[] result = new Object[fieldNames.length];
    for (int i = 0; i < fieldNames.length; i++) {
      result[i] = fieldNames[i] == null || fieldNames[i].equals("") ? null : descriptorMap.get(fieldNames[i]);
    }
    return result;
  }

  public synchronized void setFields(String[] fieldNames, Object[] fieldValues) throws RuntimeOperationsException {
    if (fieldNames == null || fieldValues == null || fieldNames.length != fieldValues.length) {
      throw new RuntimeOperationsException(new IllegalArgumentException("Null or invalid fieldNames or fieldValues"),
          "Null or invalid fieldNames or fieldValues");
    }
    for (int i = 0; i < fieldNames.length; i++) {
      setField(fieldNames[i], fieldValues[i]);
    }
  }

  public synchronized Object clone() throws RuntimeOperationsException {
    return new DescriptorSupport(this);
  }

  public synchronized void removeField(String fieldName) {
    if (fieldName == null || fieldName.equals("")) {
      return;
    }
    descriptorMap.remove(fieldName);
  }

  public synchronized boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (!(o instanceof Descriptor)) {
      return false;
    }
    Descriptor other = (Descriptor) o;
    String[] otherNames = other.getFieldNames();
    if (otherNames.length != descriptorMap.size()) {
      return false;
    }
    for (String n : otherNames) {
      if (!descriptorMap.containsKey(n)) {
        return false;
      }
      Object a = descriptorMap.get(n);
      Object b = other.getFieldValue(n);
      if (a == null ? b != null : !java.util.Objects.deepEquals(a, b)) {
        return false;
      }
    }
    return true;
  }

  public synchronized int hashCode() {
    int hash = 0;
    for (Map.Entry<String, Object> e : descriptorMap.entrySet()) {
      Object v = e.getValue();
      int vh = v == null ? 0 : (v.getClass().isArray() ? java.util.Arrays.deepHashCode(new Object[] {v}) - 31 : v.hashCode());
      hash += e.getKey().toLowerCase(java.util.Locale.ENGLISH).hashCode() ^ vh;
    }
    return hash;
  }

  public synchronized boolean isValid() throws RuntimeOperationsException {
    if (descriptorMap.get("name") == null || descriptorMap.get("descriptorType") == null) {
      return false;
    }
    for (Map.Entry<String, Object> e : descriptorMap.entrySet()) {
      if (!validateField(e.getKey(), e.getValue())) {
        return false;
      }
    }
    String type = String.valueOf(descriptorMap.get("descriptorType"));
    boolean knownType = false;
    for (String t : kDescriptorTypes) {
      knownType |= t.equalsIgnoreCase(type);
    }
    if (!knownType) {
      return false;
    }
    if (type.equalsIgnoreCase("operation") || type.equalsIgnoreCase("constructor")) {
      return descriptorMap.get("role") != null;
    }
    return true;
  }

  private static boolean validateField(String name, Object value) {
    if (name == null || name.equals("")) {
      return false;
    }
    String n = name.toLowerCase(java.util.Locale.ENGLISH);
    if (n.equals("name") || n.equals("displayname") || n.equals("class") || n.equals("role")
        || n.equals("export") || n.equals("persistlocation") || n.equals("persistname")
        || n.equals("getmethod") || n.equals("setmethod")) {
      if (value == null) {
        return true;
      }
      return value instanceof String && (!n.equals("name") || ((String) value).length() > 0);
    }
    if (n.equals("descriptortype")) {
      return value instanceof String;
    }
    if (n.equals("persistpolicy")) {
      if (!(value instanceof String)) {
        return false;
      }
      for (String t : kPersistPolicies) {
        if (t.equalsIgnoreCase((String) value)) {
          return true;
        }
      }
      return false;
    }
    if (n.equals("currencytimelimit") || n.equals("lastupdatedtimestamp") || n.equals("lastreturnedtimestamp")
        || n.equals("persistperiod") || n.equals("visibility") || n.equals("severity")) {
      long v;
      try {
        if (value instanceof Number) {
          v = ((Number) value).longValue();
        } else if (value instanceof String) {
          v = Long.parseLong((String) value);
        } else {
          return false;
        }
      } catch (NumberFormatException e) {
        return false;
      }
      if (n.equals("currencytimelimit")) {
        return v >= -1;
      }
      if (n.equals("visibility")) {
        return v >= 1 && v <= 4;
      }
      if (n.equals("severity")) {
        return v >= 0 && v <= 6;
      }
      return v >= 0;
    }
    if (n.equals("log")) {
      if (value instanceof Boolean) {
        return true;
      }
      if (!(value instanceof String)) {
        return false;
      }
      String s = (String) value;
      return s.equalsIgnoreCase("t") || s.equalsIgnoreCase("f") || s.equalsIgnoreCase("true") || s.equalsIgnoreCase("false");
    }
    return true;
  }

  public synchronized String toString() {
    String[] fields = getFields();
    StringBuilder b = new StringBuilder();
    for (int i = 0; i < fields.length; i++) {
      if (i > 0) {
        b.append(", ");
      }
      b.append(fields[i]);
    }
    return b.toString();
  }

  public synchronized String toXMLString() {
    StringBuilder b = new StringBuilder("<Descriptor>");
    for (Map.Entry<String, Object> e : descriptorMap.entrySet()) {
      Object v = e.getValue();
      String text;
      if (v == null) {
        text = "(null)";
      } else if (v instanceof String) {
        text = escape((String) v);
      } else {
        text = "(" + v.getClass().getName() + "/" + escape(v.toString()) + ")";
      }
      b.append("<field name=\"").append(escape(e.getKey())).append("\" value=\"").append(text).append("\"></field>");
    }
    return b.append("</Descriptor>").toString();
  }

  private static String escape(String s) {
    StringBuilder b = new StringBuilder();
    for (int i = 0; i < s.length(); i++) {
      char c = s.charAt(i);
      switch (c) {
        case '<':
          b.append("&lt;");
          break;
        case '>':
          b.append("&gt;");
          break;
        case '&':
          b.append("&amp;");
          break;
        case '"':
          b.append("&quot;");
          break;
        case '\'':
          b.append("&apos;");
          break;
        default:
          b.append(c);
      }
    }
    return b.toString();
  }

  private static String unescape(String s) {
    return s.replace("&lt;", "<").replace("&gt;", ">").replace("&quot;", "\"").replace("&apos;", "'").replace("&amp;", "&");
  }

  private void parseXml(String xml) throws XMLParseException {
    String s = xml.trim();
    if (!s.toLowerCase(java.util.Locale.ENGLISH).startsWith("<descriptor>") || !s.toLowerCase(java.util.Locale.ENGLISH).endsWith("</descriptor>")) {
      throw new XMLParseException("No <descriptor>, </descriptor> pair");
    }
    int pos = "<Descriptor>".length();
    int end = s.length() - "</Descriptor>".length();
    while (pos < end) {
      while (pos < end && Character.isWhitespace(s.charAt(pos))) {
        pos++;
      }
      if (pos >= end) {
        break;
      }
      if (!s.startsWith("<field", pos)) {
        throw new XMLParseException("Unexpected content in Descriptor XML");
      }
      int close = s.indexOf('>', pos);
      if (close < 0) {
        throw new XMLParseException("Unterminated field element");
      }
      String tag = s.substring(pos, close);
      String name = attr(tag, "name");
      String value = attr(tag, "value");
      if (name == null) {
        throw new XMLParseException("field without name");
      }
      pos = close + 1;
      if (s.startsWith("</field>", pos)) {
        pos += "</field>".length();
      }
      Object v;
      if (value == null || value.equals("(null)")) {
        v = null;
      } else if (value.startsWith("(") && value.endsWith(")") && value.indexOf('/') > 0) {
        String cn = value.substring(1, value.indexOf('/'));
        String val = unescape(value.substring(value.indexOf('/') + 1, value.length() - 1));
        try {
          Constructor<?> c = Class.forName(cn).getConstructor(String.class);
          v = c.newInstance(val);
        } catch (Exception e) {
          throw new XMLParseException(e, "Cannot instantiate " + cn);
        }
      } else {
        v = unescape(value);
      }
      setField(unescape(name), v);
    }
  }

  private static String attr(String tag, String name) {
    int i = tag.indexOf(name + "=\"");
    if (i < 0) {
      return null;
    }
    int start = i + name.length() + 2;
    int end = tag.indexOf('"', start);
    if (end < 0) {
      return null;
    }
    return tag.substring(start, end);
  }

  private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
    out.defaultWriteObject();
    out.writeObject(toXMLString());
  }

  private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {
    in.defaultReadObject();
    descriptorMap = new TreeMap<String, Object>(String.CASE_INSENSITIVE_ORDER);
    String xml = (String) in.readObject();
    try {
      parseXml(xml);
    } catch (XMLParseException e) {
      throw new java.io.InvalidObjectException(e.getMessage());
    }
  }
}
