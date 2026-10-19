/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package sun.net.httpserver;

import com.sun.net.httpserver.Headers;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;

public final class UnmodifiableHeaders extends Headers {
  private final Headers headers;

  private UnmodifiableHeaders(Headers headers) {
    this.headers = headers;
    for (Map.Entry<String, List<String>> e : headers.entrySet()) {
      e.setValue(Collections.unmodifiableList(new ArrayList<String>(e.getValue())));
    }
  }

  public static UnmodifiableHeaders of(Headers headers) {
    return new UnmodifiableHeaders(headers);
  }

  private static UnsupportedOperationException unsupported() {
    return new UnsupportedOperationException("unsupported operation");
  }

  @Override
  public int size() {
    return headers.size();
  }

  @Override
  public boolean isEmpty() {
    return headers.isEmpty();
  }

  @Override
  public boolean containsKey(Object key) {
    return headers.containsKey(key);
  }

  @Override
  public boolean containsValue(Object value) {
    return headers.containsValue(value);
  }

  @Override
  public List<String> get(Object key) {
    return headers.get(key);
  }

  @Override
  public String getFirst(String key) {
    return headers.getFirst(key);
  }

  @Override
  public List<String> put(String key, List<String> value) {
    throw unsupported();
  }

  @Override
  public void add(String key, String value) {
    throw unsupported();
  }

  @Override
  public void set(String key, String value) {
    throw unsupported();
  }

  @Override
  public List<String> remove(Object key) {
    throw unsupported();
  }

  @Override
  public void putAll(Map<? extends String, ? extends List<String>> t) {
    throw unsupported();
  }

  @Override
  public void clear() {
    throw unsupported();
  }

  @Override
  public Set<String> keySet() {
    return Collections.unmodifiableSet(headers.keySet());
  }

  @Override
  public Collection<List<String>> values() {
    return Collections.unmodifiableCollection(headers.values());
  }

  @Override
  public Set<Map.Entry<String, List<String>>> entrySet() {
    return Collections.unmodifiableMap(new java.util.HashMap<String, List<String>>(headers)).entrySet();
  }

  @Override
  public void replaceAll(BiFunction<? super String, ? super List<String>, ? extends List<String>> function) {
    throw unsupported();
  }

  @Override
  public boolean equals(Object o) {
    return headers.equals(o);
  }

  @Override
  public int hashCode() {
    return headers.hashCode();
  }

  @Override
  public String toString() {
    return headers.toString();
  }
}
