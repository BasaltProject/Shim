/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package com.sun.net.httpserver;

import java.util.Objects;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;

public class HttpsConfigurator {
  private final SSLContext context;

  public HttpsConfigurator(SSLContext context) {
    this.context = Objects.requireNonNull(context, "null SSLContext");
  }

  public SSLContext getSSLContext() {
    return context;
  }

  public void configure(HttpsParameters params) {
    params.setSSLParameters(getSSLContext().getDefaultSSLParameters());
  }
}
