/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.net.www.protocol.https;

import java.io.IOException;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;

public class Handler extends sun.net.www.protocol.http.Handler {
  @Override
  protected int getDefaultPort() {
    return 443;
  }

  public Handler() {
    super();
  }

  public Handler(String proxy, int port) {
    super(proxy, port);
  }

  @Override
  protected URLConnection openConnection(URL u) throws IOException {
    return openConnection(u, (Proxy) null);
  }

  @Override
  protected URLConnection openConnection(URL u, Proxy p) throws IOException {
    return new HttpsURLConnectionImpl(u, p, this);
  }
}
