/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package com.sun.net.httpserver;

import java.net.URI;
import java.util.List;
import java.util.Objects;

public interface Request {
  URI getRequestURI();

  String getRequestMethod();

  Headers getRequestHeaders();

  default Request with(String headerName, List<String> headerValues) {
    Objects.requireNonNull(headerName);
    Objects.requireNonNull(headerValues);
    for (int i = 0; i < headerName.length(); i++) {
      if (!sun.net.httpserver.Utils.isTokenChar(headerName.charAt(i))) {
        throw new IllegalArgumentException("illegal character in key at index " + i);
      }
    }
    Request source = this;
    return new Request() {
      private Headers headers;

      @Override
      public URI getRequestURI() {
        return source.getRequestURI();
      }

      @Override
      public String getRequestMethod() {
        return source.getRequestMethod();
      }

      @Override
      public synchronized Headers getRequestHeaders() {
        if (headers == null) {
          Headers merged = new Headers();
          for (java.util.Map.Entry<String, List<String>> e : source.getRequestHeaders().entrySet()) {
            for (String v : e.getValue()) {
              merged.add(e.getKey(), v);
            }
          }
          if (!merged.containsKey(headerName)) {
            merged.put(headerName, new java.util.ArrayList<String>(headerValues));
          }
          headers = Headers.of(merged);
        }
        return headers;
      }
    };
  }
}
