/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.net.http;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.TreeMap;
import java.util.function.BiPredicate;

public final class HttpHeaders {

  private final Map<String, List<String>> headers;

  private HttpHeaders(Map<String, List<String>> headers) {
    this.headers = headers;
  }

  public static HttpHeaders of(Map<String, List<String>> headerMap, BiPredicate<String, String> filter) {
    Objects.requireNonNull(headerMap, "headerMap");
    Objects.requireNonNull(filter, "filter");
    Map<String, List<String>> map = new TreeMap<String, List<String>>(String.CASE_INSENSITIVE_ORDER);
    for (Map.Entry<String, List<String>> entry : headerMap.entrySet()) {
      String key = entry.getKey();
      if (key == null) continue;
      List<String> values = entry.getValue();
      if (values == null) continue;
      List<String> filtered = new ArrayList<String>();
      for (String val : values) {
        if (val != null && filter.test(key, val)) {
          filtered.add(val);
        }
      }
      if (!filtered.isEmpty()) {
        map.put(key, Collections.unmodifiableList(filtered));
      }
    }
    return new HttpHeaders(Collections.unmodifiableMap(map));
  }

  public Optional<String> firstValue(String name) {
    Objects.requireNonNull(name, "name");
    List<String> list = headers.get(name);
    return (list != null && !list.isEmpty()) ? Optional.of(list.get(0)) : Optional.empty();
  }

  public OptionalLong firstValueAsLong(String name) {
    return firstValue(name)
        .map(s -> {
          try {
            return OptionalLong.of(Long.parseLong(s));
          } catch (NumberFormatException e) {
            return OptionalLong.empty();
          }
        })
        .orElseGet(OptionalLong::empty);
  }

  public List<String> allValues(String name) {
    Objects.requireNonNull(name, "name");
    List<String> list = headers.get(name);
    return list != null ? list : Collections.emptyList();
  }

  public Map<String, List<String>> map() {
    return headers;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) return true;
    if (!(obj instanceof HttpHeaders)) return false;
    HttpHeaders other = (HttpHeaders) obj;
    return headers.equals(other.headers);
  }

  @Override
  public int hashCode() {
    return headers.hashCode();
  }

  @Override
  public String toString() {
    return super.toString() + " { " + headers.toString() + " }";
  }
}
