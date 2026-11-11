/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.security.ssl;

import javax.net.ssl.SSLException;

final class TlsConnections {
  private TlsConnections() {
  }

  static TlsConnection create(SSLContextImpl context, SslConfig cfg, String host, int port, Object owner)
      throws SSLException {
    TlsConnection c = cfg.clientMode ? new ClientConnection(context, cfg, host, port, owner)
        : new ServerConnection(context, cfg, host, port, owner);
    c.dtls = cfg.datagram;
    return c;
  }
}
