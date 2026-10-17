/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.management;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class ObjectName implements Comparable<ObjectName>, QueryExp {
  private static final long serialVersionUID = 1081892073854801359L;

  public static final ObjectName WILDCARD = newWildcard();

  private String domain;
  private LinkedHashMap<String, String> properties = new LinkedHashMap<String, String>();
  private TreeMap<String, String> sorted = new TreeMap<String, String>();
  private boolean domainPattern;
  private boolean listPattern;
  private boolean valuePattern;
  private String keyString = "";
  private String canonicalName;

  private static ObjectName newWildcard() {
    try {
      return new ObjectName("*:*");
    } catch (MalformedObjectNameException e) {
      throw new Error(e);
    }
  }

  public static ObjectName getInstance(String name) throws MalformedObjectNameException, NullPointerException {
    return new ObjectName(name);
  }

  public static ObjectName getInstance(String domain, String key, String value) throws MalformedObjectNameException {
    return new ObjectName(domain, key, value);
  }

  public static ObjectName getInstance(String domain, Hashtable<String, String> table)
      throws MalformedObjectNameException {
    return new ObjectName(domain, table);
  }

  public static ObjectName getInstance(ObjectName name) {
    if (name.getClass().equals(ObjectName.class)) {
      return name;
    }
    try {
      return new ObjectName(name.getSerializedNameString());
    } catch (MalformedObjectNameException e) {
      throw new IllegalArgumentException(e.getMessage());
    }
  }

  public ObjectName(String name) throws MalformedObjectNameException {
    if (name == null) {
      throw new NullPointerException("name cannot be null");
    }
    parse(name);
  }

  public ObjectName(String domain, String key, String value) throws MalformedObjectNameException {
    Hashtable<String, String> table = new Hashtable<String, String>(1);
    table.put(key, value);
    construct(domain, table);
  }

  public ObjectName(String domain, Hashtable<String, String> table) throws MalformedObjectNameException {
    construct(domain, table);
  }

  private static String show(char c) {
    return c == '\n' ? "\\n" : String.valueOf(c);
  }

  private void parse(String name) throws MalformedObjectNameException {
    if (name.length() == 0) {
      domain = "*";
      domainPattern = true;
      listPattern = true;
      finish();
      return;
    }
    int len = name.length();
    int colon = -1;
    for (int i = 0; i < len; i++) {
      char c = name.charAt(i);
      if (c == ':') {
        colon = i;
        break;
      }
      if (c == '\n') {
        throw new MalformedObjectNameException("Invalid character '\\n' in domain name");
      }
      if (c == '*' || c == '?') {
        domainPattern = true;
      }
    }
    if (colon < 0) {
      if (name.indexOf('=') >= 0) {
        throw new MalformedObjectNameException("Domain part must be specified");
      }
      throw new MalformedObjectNameException("Key properties cannot be empty");
    }
    domain = name.substring(0, colon);
    int idx = colon + 1;
    if (idx == len) {
      throw new MalformedObjectNameException("Key properties cannot be empty");
    }
    while (idx < len) {
      char c = name.charAt(idx);
      if (c == '*') {
        if (listPattern) {
          throw new MalformedObjectNameException("Cannot have several '*' characters in pattern property list");
        }
        listPattern = true;
        idx++;
        if (idx < len) {
          if (name.charAt(idx) != ',') {
            throw new MalformedObjectNameException(
                "Invalid character found after '*': end of name or ',' expected");
          }
          idx++;
        }
        continue;
      }
      int keyStart = idx;
      while (idx < len && name.charAt(idx) != '=') {
        char k = name.charAt(idx);
        if (k == ',' || k == ':' || k == '*' || k == '?' || k == '\n') {
          throw new MalformedObjectNameException("Invalid character '" + show(k) + "' in key part of property");
        }
        idx++;
      }
      if (idx == len) {
        throw new MalformedObjectNameException("Unterminated key property part");
      }
      String key = name.substring(keyStart, idx);
      if (key.isEmpty()) {
        throw new MalformedObjectNameException("Invalid key (empty)");
      }
      idx++;
      String value;
      if (idx < len && name.charAt(idx) == '"') {
        int j = idx + 1;
        int close = -1;
        while (j < len) {
          char q = name.charAt(j);
          if (q == '"') {
            close = j;
            break;
          }
          if (q == '\\') {
            j++;
            if (j >= len) {
              throw new MalformedObjectNameException("Unterminated quoted value");
            }
            char n = name.charAt(j);
            if (n != '\\' && n != '"' && n != '*' && n != '?' && n != 'n') {
              throw new MalformedObjectNameException("Invalid escape sequence '\\" + n + "' in quoted value");
            }
          } else if (q == '*' || q == '?') {
            valuePattern = true;
          } else if (q == '\n') {
            throw new MalformedObjectNameException("Newline in quoted value");
          }
          j++;
        }
        if (close < 0) {
          throw new MalformedObjectNameException("Unterminated quoted value");
        }
        value = name.substring(idx, close + 1);
        idx = close + 1;
        if (idx < len) {
          if (idx + 1 == len) {
            throw new MalformedObjectNameException("Invalid ending character `" + name.charAt(idx) + "'");
          }
          idx++;
        }
      } else {
        int valStart = idx;
        while (idx < len && name.charAt(idx) != ',') {
          char v = name.charAt(idx);
          if (v == '=' || v == ':' || v == '"' || v == '\n') {
            throw new MalformedObjectNameException("Invalid character '" + show(v) + "' in value part of property");
          }
          if (v == '*' || v == '?') {
            valuePattern = true;
          }
          idx++;
        }
        value = name.substring(valStart, idx);
        if (idx < len) {
          if (idx + 1 == len) {
            throw new MalformedObjectNameException("Invalid ending comma");
          }
          idx++;
        }
      }
      if (properties.containsKey(key)) {
        throw new MalformedObjectNameException("key `" + key + "' already defined");
      }
      properties.put(key, value);
    }
    finish();
  }

  private void construct(String domain, Hashtable<String, String> table) throws MalformedObjectNameException {
    if (domain == null) {
      throw new NullPointerException("domain cannot be null");
    }
    if (table == null) {
      throw new NullPointerException("key property list cannot be null");
    }
    if (table.isEmpty()) {
      throw new MalformedObjectNameException("key property list cannot be empty");
    }
    if (domain.indexOf(':') >= 0 || domain.indexOf('\n') >= 0) {
      throw new MalformedObjectNameException("Invalid domain: " + domain);
    }
    for (int i = 0; i < domain.length(); i++) {
      char c = domain.charAt(i);
      if (c == '*' || c == '?') {
        domainPattern = true;
      }
    }
    this.domain = domain;
    for (Map.Entry<String, String> e : table.entrySet()) {
      String key = e.getKey();
      String value = e.getValue();
      if (key == null) {
        throw new NullPointerException("Invalid key (null)");
      }
      if (value == null) {
        throw new NullPointerException("Invalid value (null)");
      }
      checkKey(key);
      checkValue(value);
      properties.put(key, value);
    }
    finish();
  }

  private static void checkKey(String key) throws MalformedObjectNameException {
    if (key.isEmpty()) {
      throw new MalformedObjectNameException("Invalid key (empty)");
    }
    for (int i = 0; i < key.length(); i++) {
      char c = key.charAt(i);
      if (c == '=') {
        throw new MalformedObjectNameException("Invalid character in value: `='");
      }
      if (c == ',' || c == ':' || c == '*' || c == '?' || c == '\n') {
        throw new MalformedObjectNameException("Invalid character in key: `" + show(c) + "'");
      }
    }
  }

  private void checkValue(String value) throws MalformedObjectNameException {
    int len = value.length();
    if (len > 0 && value.charAt(0) == '"') {
      if (len < 2 || value.charAt(len - 1) != '"') {
        throw new MalformedObjectNameException("Missing termination quote");
      }
      for (int i = 1; i < len - 1; i++) {
        char c = value.charAt(i);
        if (c == '"') {
          throw new MalformedObjectNameException("Invalid quote");
        }
        if (c == '\\') {
          i++;
          char n = i < len - 1 ? value.charAt(i) : '\\';
          if (n != '\\' && n != '"' && n != '*' && n != '?' && n != 'n') {
            throw new MalformedObjectNameException("Invalid quoted character sequence '\\" + n + "'");
          }
        } else if (c == '*' || c == '?') {
          valuePattern = true;
        }
      }
      return;
    }
    for (int i = 0; i < len; i++) {
      char c = value.charAt(i);
      if (c == ',') {
        throw new MalformedObjectNameException("Invalid character in value: `,'");
      }
      if (c == '=' || c == ':' || c == '\n') {
        throw new MalformedObjectNameException("Invalid character `" + show(c) + "' in value");
      }
      if (c == '*' || c == '?') {
        valuePattern = true;
      }
    }
  }

  private void finish() {
    StringBuilder k = new StringBuilder();
    for (Map.Entry<String, String> e : properties.entrySet()) {
      if (k.length() > 0) {
        k.append(',');
      }
      k.append(e.getKey()).append('=').append(e.getValue());
    }
    keyString = k.toString();
    sorted.putAll(properties);
    canonicalName = domain + ":" + canonicalKeys(true);
  }

  private String canonicalKeys(boolean withPattern) {
    StringBuilder b = new StringBuilder();
    for (Map.Entry<String, String> e : sorted.entrySet()) {
      if (b.length() > 0) {
        b.append(',');
      }
      b.append(e.getKey()).append('=').append(e.getValue());
    }
    if (withPattern && listPattern) {
      if (b.length() > 0) {
        b.append(',');
      }
      b.append('*');
    }
    return b.toString();
  }

  private String getSerializedNameString() {
    if (!listPattern) {
      return domain + ":" + keyString;
    }
    return domain + ":" + keyString + (keyString.isEmpty() ? "*" : ",*");
  }

  public boolean isPattern() {
    return domainPattern || listPattern || valuePattern;
  }

  public boolean isDomainPattern() {
    return domainPattern;
  }

  public boolean isPropertyPattern() {
    return listPattern || valuePattern;
  }

  public boolean isPropertyListPattern() {
    return listPattern;
  }

  public boolean isPropertyValuePattern() {
    return valuePattern;
  }

  public boolean isPropertyValuePattern(String property) {
    if (property == null) {
      throw new NullPointerException("key property can't be null");
    }
    String v = properties.get(property);
    if (v == null) {
      throw new IllegalArgumentException("key property not found");
    }
    return isValuePattern(v);
  }

  private static boolean isValuePattern(String v) {
    boolean quoted = v.length() > 0 && v.charAt(0) == '"';
    for (int i = 0; i < v.length(); i++) {
      char c = v.charAt(i);
      if (quoted && c == '\\') {
        i++;
      } else if (c == '*' || c == '?') {
        return true;
      }
    }
    return false;
  }

  public String getCanonicalName() {
    return canonicalName;
  }

  public String getDomain() {
    return domain;
  }

  public String getKeyProperty(String property) {
    if (property == null) {
      throw new NullPointerException("key property can't be null");
    }
    return properties.get(property);
  }

  public Hashtable<String, String> getKeyPropertyList() {
    return new Hashtable<String, String>(properties);
  }

  public String getKeyPropertyListString() {
    return keyString;
  }

  public String getCanonicalKeyPropertyListString() {
    return canonicalKeys(false);
  }

  @Override
  public String toString() {
    return getSerializedNameString();
  }

  @Override
  public boolean equals(Object object) {
    if (this == object) {
      return true;
    }
    if (!(object instanceof ObjectName)) {
      return false;
    }
    return canonicalName.equals(((ObjectName) object).getCanonicalName());
  }

  @Override
  public int hashCode() {
    return canonicalName.hashCode();
  }

  public static String quote(String s) {
    StringBuilder buf = new StringBuilder("\"");
    int len = s.length();
    for (int i = 0; i < len; i++) {
      char c = s.charAt(i);
      switch (c) {
      case '\n':
        c = 'n';
        buf.append('\\');
        break;
      case '\\':
      case '"':
      case '*':
      case '?':
        buf.append('\\');
        break;
      default:
        break;
      }
      buf.append(c);
    }
    buf.append('"');
    return buf.toString();
  }

  public static String unquote(String q) {
    StringBuilder buf = new StringBuilder();
    int len = q.length();
    if (len < 2 || q.charAt(0) != '"' || q.charAt(len - 1) != '"') {
      throw new IllegalArgumentException("Argument not quoted");
    }
    for (int i = 1; i < len - 1; i++) {
      char c = q.charAt(i);
      if (c == '\\') {
        if (i == len - 2) {
          throw new IllegalArgumentException("Trailing backslash");
        }
        c = q.charAt(++i);
        switch (c) {
        case 'n':
          c = '\n';
          break;
        case '\\':
        case '"':
        case '*':
        case '?':
          break;
        default:
          throw new IllegalArgumentException("Bad character '" + c + "' after backslash");
        }
      } else {
        switch (c) {
        case '*':
        case '?':
        case '"':
        case '\n':
          throw new IllegalArgumentException("Invalid unescaped character '" + c + "' in the string to unquote");
        default:
          break;
        }
      }
      buf.append(c);
    }
    return buf.toString();
  }

  private static boolean wildmatch(String s, String p) {
    int si = 0;
    int pi = 0;
    int star = -1;
    int mark = 0;
    while (si < s.length()) {
      if (pi < p.length() && p.charAt(pi) == '*') {
        star = pi++;
        mark = si;
      } else if (pi < p.length() && (p.charAt(pi) == '?' || p.charAt(pi) == s.charAt(si))) {
        pi++;
        si++;
      } else if (star >= 0) {
        pi = star + 1;
        si = ++mark;
      } else {
        return false;
      }
    }
    while (pi < p.length() && p.charAt(pi) == '*') {
      pi++;
    }
    return pi == p.length();
  }

  @Override
  public boolean apply(ObjectName name) {
    if (name == null) {
      throw new NullPointerException();
    }
    if (name.isPattern()) {
      return false;
    }
    if (!domainPattern && !isPropertyPattern()) {
      return canonicalName.equals(name.canonicalName);
    }
    if (!(domainPattern ? wildmatch(name.domain, domain) : domain.equals(name.domain))) {
      return false;
    }
    if (!listPattern && properties.size() != name.properties.size()) {
      return false;
    }
    for (Map.Entry<String, String> e : properties.entrySet()) {
      String other = name.properties.get(e.getKey());
      if (other == null) {
        return false;
      }
      if (isValuePattern(e.getValue()) ? !wildmatch(other, e.getValue()) : !e.getValue().equals(other)) {
        return false;
      }
    }
    return true;
  }

  @Override
  public void setMBeanServer(MBeanServer server) {
  }

  @Override
  public int compareTo(ObjectName name) {
    if (name == this) {
      return 0;
    }
    int domainValue = domain.compareTo(name.getDomain());
    if (domainValue != 0) {
      return domainValue;
    }
    String thisType = getKeyProperty("type");
    String anotherType = name.getKeyProperty("type");
    if (thisType == null) {
      thisType = "";
    }
    if (anotherType == null) {
      anotherType = "";
    }
    int typeValue = thisType.compareTo(anotherType);
    if (typeValue != 0) {
      return typeValue;
    }
    return canonicalKeys(true).compareTo(name.canonicalKeys(true));
  }
}
