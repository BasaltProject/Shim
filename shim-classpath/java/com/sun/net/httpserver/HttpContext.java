/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package com.sun.net.httpserver;

import java.util.List;
import java.util.Map;

public abstract class HttpContext {
  protected HttpContext() { }

  public abstract HttpHandler getHandler();

  public abstract void setHandler(HttpHandler handler);

  public abstract String getPath();

  public abstract HttpServer getServer();

  public abstract Map<String, Object> getAttributes();

  public abstract List<Filter> getFilters();

  public abstract Authenticator setAuthenticator(Authenticator auth);

  public abstract Authenticator getAuthenticator();
}
