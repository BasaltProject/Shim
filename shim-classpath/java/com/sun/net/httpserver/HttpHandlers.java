/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package com.sun.net.httpserver;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

public final class HttpHandlers {
  private HttpHandlers() { }

  public static HttpHandler handleOrElse(Predicate<Request> handlerTest, HttpHandler handler,
      HttpHandler fallbackHandler) {
    Objects.requireNonNull(handlerTest);
    Objects.requireNonNull(handler);
    Objects.requireNonNull(fallbackHandler);
    return exchange -> {
      if (handlerTest.test(exchange)) {
        handler.handle(exchange);
      } else {
        fallbackHandler.handle(exchange);
      }
    };
  }

  public static HttpHandler of(int statusCode, Headers headers, String body) {
    Objects.requireNonNull(headers);
    Objects.requireNonNull(body);
    if (statusCode < 100 || statusCode > 999) {
      throw new IllegalArgumentException("statusCode must be 3-digit: " + statusCode);
    }
    final byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
    final Headers copy = new Headers();
    for (Map.Entry<String, List<String>> e : headers.entrySet()) {
      for (String v : e.getValue()) {
        copy.add(e.getKey(), v);
      }
    }
    return exchange -> {
      try (InputStream in = exchange.getRequestBody()) {
        in.readAllBytes();
      }
      exchange.getResponseHeaders().putAll(copy);
      if (exchange.getRequestMethod().equals("HEAD")) {
        exchange.getResponseHeaders().set("Content-Length", Integer.toString(bytes.length));
        exchange.sendResponseHeaders(statusCode, -1);
      } else {
        exchange.sendResponseHeaders(statusCode, bytes.length == 0 ? -1 : bytes.length);
        if (bytes.length > 0) {
          try (OutputStream out = exchange.getResponseBody()) {
            out.write(bytes);
          }
        }
      }
      exchange.close();
    };
  }
}
