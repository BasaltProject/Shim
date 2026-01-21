/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package com.sun.net.httpserver;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;

public class Headers implements Map<String, List<String>> {
  HashMap<String, List<String>> map;

  public Headers() {
    map = new HashMap<String, List<String>>(32);
  }

  public Headers(Map<String, List<String>> headers) {
    Objects.requireNonNull(headers);
    map = new HashMap<String, List<String>>(Math.max(32, headers.size() * 2));
    for (Map.Entry<String, List<String>> e : headers.entrySet()) {
      put(e.getKey(), e.getValue());
    }
  }

  private static String normalize(String key) {
    Objects.requireNonNull(key);
    int len = key.length();
    if (len == 0) {
      return key;
    }
    checkText(key);
    char[] b = null;
    char c = key.charAt(0);
    if (c >= 'a' && c <= 'z') {
      b = key.toCharArray();
      b[0] = (char) (c - 32);
    }
    for (int i = 1; i < len; i++) {
      c = key.charAt(i);
      if (c >= 'A' && c <= 'Z') {
        if (b == null) {
          b = key.toCharArray();
        }
        b[i] = (char) (c + 32);
      }
    }
    return b == null ? key : String.valueOf(b);
  }

  private static void checkText(String text) {
    int len = text.length();
    for (int i = 0; i < len; i++) {
      char c = text.charAt(i);
      if (c == '\r') {
        if (i + 1 >= len || text.charAt(i + 1) != '\n') {
          throw new IllegalArgumentException("Illegal CR found in header");
        }
        if (i + 2 >= len || (text.charAt(i + 2) != ' ' && text.charAt(i + 2) != '\t')) {
          throw new IllegalArgumentException("No whitespace found after CRLF in header");
        }
        i += 2;
      } else if (c == '\n') {
        throw new IllegalArgumentException("Illegal LF found in header");
      }
    }
  }

  private static void checkValues(List<String> values) {
    for (String v : values) {
      Objects.requireNonNull(v);
      checkText(v);
    }
  }

  @Override
  public int size() {
    return map.size();
  }

  @Override
  public boolean isEmpty() {
    return map.isEmpty();
  }

  @Override
  public boolean containsKey(Object key) {
    Objects.requireNonNull(key);
    if (!(key instanceof String)) {
      return false;
    }
    return map.containsKey(normalize((String) key));
  }

  @Override
  public boolean containsValue(Object value) {
    return map.containsValue(value);
  }

  @Override
  public List<String> get(Object key) {
    return map.get(normalize((String) key));
  }

  public String getFirst(String key) {
    List<String> l = map.get(normalize(key));
    if (l == null || l.isEmpty()) {
      return null;
    }
    return l.get(0);
  }

  @Override
  public List<String> put(String key, List<String> value) {
    for (String v : value) {
      Objects.requireNonNull(v);
      checkText(v);
    }
    return map.put(normalize(key), value);
  }

  public void add(String key, String value) {
    Objects.requireNonNull(value);
    checkText(value);
    String k = normalize(key);
    List<String> l = map.get(k);
    if (l == null) {
      l = new ArrayList<String>(1);
      map.put(k, l);
    }
    l.add(value);
  }

  public void set(String key, String value) {
    Objects.requireNonNull(value);
    checkText(value);
    List<String> l = new ArrayList<String>(1);
    l.add(value);
    map.put(normalize(key), l);
  }

  @Override
  public List<String> remove(Object key) {
    Objects.requireNonNull(key);
    if (!(key instanceof String)) {
      return null;
    }
    return map.remove(normalize((String) key));
  }

  @Override
  public void putAll(Map<? extends String, ? extends List<String>> t) {
    for (Map.Entry<? extends String, ? extends List<String>> e : t.entrySet()) {
      put(e.getKey(), e.getValue());
    }
  }

  @Override
  public void clear() {
    map.clear();
  }

  @Override
  public Set<String> keySet() {
    return map.keySet();
  }

  @Override
  public Collection<List<String>> values() {
    return map.values();
  }

  @Override
  public Set<Map.Entry<String, List<String>>> entrySet() {
    return map.entrySet();
  }

  @Override
  public void replaceAll(BiFunction<? super String, ? super List<String>, ? extends List<String>> function) {
    Objects.requireNonNull(function);
    map.replaceAll((k, v) -> {
      List<String> newValue = function.apply(k, v);
      Objects.requireNonNull(newValue);
      checkValues(newValue);
      return newValue;
    });
  }

  @Override
  public boolean equals(Object o) {
    return map.equals(o);
  }

  @Override
  public int hashCode() {
    return map.hashCode();
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + " { " + map.toString() + " }";
  }

  public static Headers of(String... headers) {
    Objects.requireNonNull(headers);
    if (headers.length == 0) {
      return sun.net.httpserver.UnmodifiableHeaders.of(new Headers());
    }
    if (headers.length % 2 != 0) {
      throw new IllegalArgumentException("wrong number, " + headers.length + ", of elements");
    }
    Headers h = new Headers();
    for (int i = 0; i < headers.length; i += 2) {
      String name = headers[i];
      String value = headers[i + 1];
      h.add(name, value);
    }
    return sun.net.httpserver.UnmodifiableHeaders.of(h);
  }

  public static Headers of(Map<String, List<String>> headers) {
    Objects.requireNonNull(headers);
    Headers h = new Headers();
    for (Map.Entry<String, List<String>> e : headers.entrySet()) {
      String name = e.getKey();
      for (String value : e.getValue()) {
        h.add(name, value);
      }
      if (e.getValue().isEmpty()) {
        h.put(name, new ArrayList<String>());
      }
    }
    return sun.net.httpserver.UnmodifiableHeaders.of(h);
  }
}
