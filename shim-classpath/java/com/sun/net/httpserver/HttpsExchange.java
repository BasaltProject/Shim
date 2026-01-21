/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package com.sun.net.httpserver;

import javax.net.ssl.SSLSession;

public abstract class HttpsExchange extends HttpExchange {
  protected HttpsExchange() { }

  public abstract SSLSession getSSLSession();
}
