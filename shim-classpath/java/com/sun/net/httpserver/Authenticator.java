/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package com.sun.net.httpserver;

public abstract class Authenticator {
  protected Authenticator() { }

  public abstract static class Result { }

  public static class Failure extends Result {
    private final int responseCode;

    public Failure(int responseCode) {
      this.responseCode = responseCode;
    }

    public int getResponseCode() {
      return responseCode;
    }
  }

  public static class Success extends Result {
    private final HttpPrincipal principal;

    public Success(HttpPrincipal p) {
      principal = p;
    }

    public HttpPrincipal getPrincipal() {
      return principal;
    }
  }

  public static class Retry extends Result {
    private final int responseCode;

    public Retry(int responseCode) {
      this.responseCode = responseCode;
    }

    public int getResponseCode() {
      return responseCode;
    }
  }

  public abstract Result authenticate(HttpExchange exch);
}
