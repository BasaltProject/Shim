/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.naming.ldap;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import javax.naming.InvalidNameException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttributes;

public class Rdn implements java.io.Serializable, Comparable<Object> {
  private static final long serialVersionUID = -5994465067210009656L;

  private transient List<Entry> entries;
  private transient String rdnString;

  private static final class Entry {
    final String type;
    final Object value;

    Entry(String type, Object value) {
      this.type = type;
      this.value = value;
    }

    String valueString() {
      return escapeValue(value);
    }

    String canonical() {
      String v = valueString();
      if (value instanceof String) {
        v = ((String) value).trim().toLowerCase(Locale.ENGLISH);
        v = escapeValue(v);
      }
      return type.trim().toLowerCase(Locale.ENGLISH) + "=" + v;
    }

    @Override
    public String toString() {
      return type + "=" + valueString();
    }
  }

  public Rdn(Attributes attrSet) throws InvalidNameException {
    if (attrSet.size() == 0) {
      throw new InvalidNameException("Attributes cannot be empty");
    }
    entries = new ArrayList<Entry>();
    try {
      NamingEnumeration<? extends Attribute> attrs = attrSet.getAll();
      while (attrs.hasMore()) {
        Attribute attr = attrs.next();
        entries.add(new Entry(attr.getID(), attr.get()));
      }
    } catch (NamingException e) {
      InvalidNameException ine = new InvalidNameException(e.getMessage());
      ine.initCause(e);
      throw ine;
    }
    sortEntries();
    rdnString = buildString();
  }

  public Rdn(String rdnString) throws InvalidNameException {
    entries = new ArrayList<Entry>();
    parse(rdnString);
    this.rdnString = buildString();
  }

  public Rdn(Rdn rdn) {
    entries = new ArrayList<Entry>(rdn.entries);
    rdnString = rdn.rdnString;
  }

  public Rdn(String type, Object value) throws InvalidNameException {
    if (value == null) {
      throw new NullPointerException("Cannot have null value");
    }
    if (type == null) {
      throw new NullPointerException("Cannot have null type");
    }
    if (type.length() == 0 || (value instanceof String && ((String) value).length() == 0)) {
      throw new InvalidNameException("type or value cannot be empty");
    }
    entries = new ArrayList<Entry>();
    entries.add(new Entry(type, value));
    rdnString = buildString();
  }

  private void sortEntries() {
    Collections.sort(entries, new Comparator<Entry>() {
      public int compare(Entry a, Entry b) {
        return a.canonical().compareTo(b.canonical());
      }
    });
  }

  private String buildString() {
    StringBuilder b = new StringBuilder();
    for (int i = 0; i < entries.size(); i++) {
      if (i > 0) {
        b.append('+');
      }
      b.append(entries.get(i));
    }
    return b.toString();
  }

  private void parse(String s) throws InvalidNameException {
    if (s.trim().length() == 0) {
      throw new InvalidNameException("Invalid RDN: " + s);
    }
    int i = 0;
    int n = s.length();
    while (i < n) {
      int eq = -1;
      int j = i;
      while (j < n) {
        char c = s.charAt(j);
        if (c == '=') {
          eq = j;
          break;
        }
        if (c == '+' || c == ',' || c == ';') {
          break;
        }
        j++;
      }
      if (eq < 0) {
        throw new InvalidNameException("Invalid RDN: " + s);
      }
      String type = s.substring(i, eq).trim();
      if (type.length() == 0) {
        throw new InvalidNameException("Invalid RDN: " + s);
      }
      int k = eq + 1;
      boolean quoted = false;
      StringBuilder raw = new StringBuilder();
      while (k < n) {
        char c = s.charAt(k);
        if (c == '\\' && k + 1 < n) {
          raw.append(c).append(s.charAt(k + 1));
          k += 2;
          continue;
        }
        if (c == '"') {
          quoted = !quoted;
          raw.append(c);
          k++;
          continue;
        }
        if (!quoted && (c == '+' || c == ',' || c == ';')) {
          break;
        }
        raw.append(c);
        k++;
      }
      if (quoted) {
        throw new InvalidNameException("Invalid RDN: " + s);
      }
      if (k < n && s.charAt(k) != '+') {
        throw new InvalidNameException("Invalid RDN: " + s);
      }
      String rawValue = raw.toString();
      String stripped = rawValue;
      if (!stripped.endsWith("\\ ")) {
        int e = stripped.length();
        while (e > 0 && stripped.charAt(e - 1) == ' ' && !(e > 1 && stripped.charAt(e - 2) == '\\')) {
          e--;
        }
        stripped = stripped.substring(0, e);
      }
      stripped = stripped.replaceAll("^ +", "");
      entries.add(new Entry(type, unescapeValue(stripped)));
      i = k + 1;
    }
    if (entries.size() > 1) {
      sortEntries();
    }
  }

  public String getType() {
    return entries.get(0).type;
  }

  public Object getValue() {
    return entries.get(0).value;
  }

  public int size() {
    return entries.size();
  }

  public Attributes toAttributes() {
    Attributes attrs = new BasicAttributes(true);
    for (Entry e : entries) {
      Attribute attr = attrs.put(e.type, e.value);
      if (attr != null) {
        attr.add(e.value);
        attrs.put(attr);
      }
    }
    return attrs;
  }

  @Override
  public String toString() {
    return rdnString;
  }

  @Override
  public int compareTo(Object obj) {
    if (!(obj instanceof Rdn)) {
      throw new ClassCastException("The obj is not a Rdn");
    }
    if (obj == this) {
      return 0;
    }
    Rdn other = (Rdn) obj;
    int n = Math.min(entries.size(), other.entries.size());
    for (int i = 0; i < n; i++) {
      int c = entries.get(i).canonical().compareTo(other.entries.get(i).canonical());
      if (c != 0) {
        return c;
      }
    }
    return entries.size() - other.entries.size();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (!(obj instanceof Rdn)) {
      return false;
    }
    Rdn other = (Rdn) obj;
    if (entries.size() != other.entries.size()) {
      return false;
    }
    return compareTo(other) == 0;
  }

  @Override
  public int hashCode() {
    int h = 0;
    for (Entry e : entries) {
      h += e.canonical().hashCode();
    }
    return h;
  }

  public static String escapeValue(Object val) {
    if (val instanceof byte[]) {
      byte[] b = (byte[]) val;
      StringBuilder out = new StringBuilder("#");
      for (byte x : b) {
        out.append(Character.forDigit((x >> 4) & 0xf, 16)).append(Character.forDigit(x & 0xf, 16));
      }
      return out.toString();
    }
    String s = val.toString();
    StringBuilder out = new StringBuilder();
    int len = s.length();
    for (int i = 0; i < len; i++) {
      char c = s.charAt(i);
      if (c == ',' || c == '+' || c == '"' || c == '\\' || c == '<' || c == '>' || c == ';') {
        out.append('\\').append(c);
      } else if (i == 0 && (c == '#' || c == ' ')) {
        out.append('\\').append(c);
      } else if (i == len - 1 && c == ' ') {
        out.append('\\').append(c);
      } else if (c == 0) {
        out.append("\\00");
      } else {
        out.append(c);
      }
    }
    return out.toString();
  }

  public static Object unescapeValue(String val) {
    String s = val.trim();
    int n = s.length();
    if (n == 0) {
      return "";
    }
    if (s.charAt(0) == '#' && n > 1 && (n % 2) == 1) {
      boolean hex = true;
      for (int i = 1; i < n; i++) {
        if (Character.digit(s.charAt(i), 16) < 0) {
          hex = false;
          break;
        }
      }
      if (hex) {
        byte[] out = new byte[(n - 1) / 2];
        for (int i = 0; i < out.length; i++) {
          out[i] = (byte) ((Character.digit(s.charAt(1 + 2 * i), 16) << 4) | Character.digit(s.charAt(2 + 2 * i), 16));
        }
        return out;
      }
    }
    if (n >= 2 && s.charAt(0) == '"' && s.charAt(n - 1) == '"') {
      s = s.substring(1, n - 1);
      n = s.length();
    }
    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    StringBuilder out = new StringBuilder();
    int i = 0;
    while (i < n) {
      char c = s.charAt(i);
      if (c == '\\') {
        if (i + 1 >= n) {
          throw new IllegalArgumentException("Invalid value: " + val);
        }
        char d = s.charAt(i + 1);
        if (i + 2 < n && Character.digit(d, 16) >= 0 && Character.digit(s.charAt(i + 2), 16) >= 0) {
          bytes.write((Character.digit(d, 16) << 4) | Character.digit(s.charAt(i + 2), 16));
          i += 3;
          continue;
        }
        flush(bytes, out);
        out.append(d);
        i += 2;
      } else {
        flush(bytes, out);
        out.append(c);
        i++;
      }
    }
    flush(bytes, out);
    return out.toString();
  }

  private static void flush(ByteArrayOutputStream bytes, StringBuilder out) {
    if (bytes.size() > 0) {
      try {
        out.append(new String(bytes.toByteArray(), "UTF-8"));
      } catch (java.io.UnsupportedEncodingException e) {
        throw new IllegalStateException(e);
      }
      bytes.reset();
    }
  }

  private void writeObject(java.io.ObjectOutputStream s) throws java.io.IOException {
    s.defaultWriteObject();
    s.writeObject(rdnString);
  }

  private void readObject(java.io.ObjectInputStream s) throws java.io.IOException, ClassNotFoundException {
    s.defaultReadObject();
    String str = (String) s.readObject();
    entries = new ArrayList<Entry>();
    try {
      parse(str);
    } catch (InvalidNameException e) {
      throw new java.io.InvalidObjectException(e.getMessage());
    }
    rdnString = buildString();
  }
}
