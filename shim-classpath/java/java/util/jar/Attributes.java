/*
 * Based on Avian (https://github.com/ReadyTalk/avian).
 * Original work Copyright (c) 2008-2015, Avian Contributors.
 * Original work licensed under ISC; see LICENSE.txt:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE.txt
 *
 * Modifications Copyright (c) 2026 BasaltMC.
 * Modifications licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: ISC AND MIT
 */

/* Copyright (c) 2008-2015, Avian Contributors

   Permission to use, copy, modify, and/or distribute this software
   for any purpose with or without fee is hereby granted, provided
   that the above copyright notice and this permission notice appear
   in all copies.

   There is NO WARRANTY for this software.  See license.txt for
   details. */

package java.util.jar;

import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class Attributes implements Map<Object, Object>, Cloneable {
  protected Map<Object, Object> map;

  public Attributes() {
    this(11);
  }

  public Attributes(int size) {
    this.map = new HashMap<Object, Object>(size);
  }

  public Attributes(Attributes attr) {
    this.map = new HashMap<Object, Object>(attr.map);
  }

  public Object get(Object key) {
    return map.get(key);
  }

  public String getValue(String name) {
    return (String) get(new Name(name));
  }

  public String getValue(Name name) {
    return (String) get(name);
  }

  public Object put(Object key, Object value) {
    return map.put((Name) key, (String) value);
  }

  public String putValue(String name, String value) {
    return (String) put(new Name(name), value);
  }

  public Object remove(Object key) {
    return map.remove(key);
  }

  public boolean containsValue(Object value) {
    return map.containsValue(value);
  }

  public boolean containsKey(Object key) {
    return map.containsKey(key);
  }

  public void putAll(Map<?, ?> attr) {
    if (!Attributes.class.isInstance(attr)) {
      for (Map.Entry<?, ?> me : attr.entrySet()) {
        put(me.getKey(), me.getValue());
      }
    } else {
      map.putAll(attr);
    }
  }

  public void clear() {
    map.clear();
  }

  public int size() {
    return map.size();
  }

  public boolean isEmpty() {
    return map.isEmpty();
  }

  public Set<Object> keySet() {
    return map.keySet();
  }

  public Collection<Object> values() {
    return map.values();
  }

  public Set<Map.Entry<Object, Object>> entrySet() {
    return map.entrySet();
  }

  public boolean equals(Object o) {
    return map.equals(o);
  }

  public int hashCode() {
    return map.hashCode();
  }

  public Object clone() {
    return new Attributes(this);
  }

  public static class Name {
    private final String name;
    private final int hashCode;

    private static final int MAX_NAME_LENGTH = 70;

    public static final Name MANIFEST_VERSION = new Name("Manifest-Version");
    public static final Name SIGNATURE_VERSION = new Name("Signature-Version");
    public static final Name CONTENT_TYPE = new Name("Content-Type");
    public static final Name CLASS_PATH = new Name("Class-Path");
    public static final Name MAIN_CLASS = new Name("Main-Class");
    public static final Name SEALED = new Name("Sealed");
    public static final Name EXTENSION_LIST = new Name("Extension-List");
    public static final Name EXTENSION_NAME = new Name("Extension-Name");
    public static final Name EXTENSION_INSTALLATION = new Name("Extension-Installation");
    public static final Name IMPLEMENTATION_TITLE = new Name("Implementation-Title");
    public static final Name IMPLEMENTATION_VERSION = new Name("Implementation-Version");
    public static final Name IMPLEMENTATION_VENDOR = new Name("Implementation-Vendor");
    public static final Name IMPLEMENTATION_VENDOR_ID = new Name("Implementation-Vendor-Id");
    public static final Name IMPLEMENTATION_URL = new Name("Implementation-URL");
    public static final Name SPECIFICATION_TITLE = new Name("Specification-Title");
    public static final Name SPECIFICATION_VERSION = new Name("Specification-Version");
    public static final Name SPECIFICATION_VENDOR = new Name("Specification-Vendor");
    public static final Name MULTI_RELEASE = new Name("Multi-Release");

    public Name(String s) {
      Objects.requireNonNull(s, "name");
      int len = s.length();
      if (len == 0 || len > MAX_NAME_LENGTH) {
        throw new IllegalArgumentException(s);
      }
      for (int i = 0; i < len; i++) {
        char ch = s.charAt(i);
        if (!((ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z') ||
              (ch >= '0' && ch <= '9') || ch == '_' || ch == '-')) {
          throw new IllegalArgumentException(s);
        }
      }
      this.name = s.intern();
      this.hashCode = s.toLowerCase(Locale.ROOT).hashCode();
    }

    public boolean equals(Object o) {
      if (this == o) return true;
      if (o instanceof Name) {
        Name other = (Name) o;
        return name.equalsIgnoreCase(other.name);
      }
      return false;
    }

    public int hashCode() {
      return hashCode;
    }

    public String toString() {
      return name;
    }
  }
}
