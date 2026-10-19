/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package com.sun.net.httpserver;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

public abstract class Filter {
  protected Filter() { }

  public static class Chain {
    private final Iterator<Filter> iter;
    private final HttpHandler handler;

    public Chain(List<Filter> filters, HttpHandler handler) {
      iter = filters.iterator();
      this.handler = handler;
    }

    public void doFilter(HttpExchange exchange) throws IOException {
      if (!iter.hasNext()) {
        handler.handle(exchange);
      } else {
        Filter f = iter.next();
        f.doFilter(exchange, this);
      }
    }
  }

  public abstract void doFilter(HttpExchange exchange, Chain chain) throws IOException;

  public abstract String description();

  public static Filter beforeHandler(String description, Consumer<HttpExchange> operation) {
    Objects.requireNonNull(description);
    Objects.requireNonNull(operation);
    return new Filter() {
      @Override
      public void doFilter(HttpExchange exchange, Chain chain) throws IOException {
        operation.accept(exchange);
        chain.doFilter(exchange);
      }

      @Override
      public String description() {
        return description;
      }
    };
  }

  public static Filter afterHandler(String description, Consumer<HttpExchange> operation) {
    Objects.requireNonNull(description);
    Objects.requireNonNull(operation);
    return new Filter() {
      @Override
      public void doFilter(HttpExchange exchange, Chain chain) throws IOException {
        chain.doFilter(exchange);
        operation.accept(exchange);
      }

      @Override
      public String description() {
        return description;
      }
    };
  }

  public static Filter adaptRequest(String description, UnaryOperator<Request> requestOperator) {
    Objects.requireNonNull(description);
    Objects.requireNonNull(requestOperator);
    return new Filter() {
      @Override
      public void doFilter(HttpExchange exchange, Chain chain) throws IOException {
        Request request = requestOperator.apply(exchange);
        if (request != exchange) {
          exchange = new sun.net.httpserver.DelegatingExchange(exchange, request);
        }
        chain.doFilter(exchange);
      }

      @Override
      public String description() {
        return description;
      }
    };
  }
}
