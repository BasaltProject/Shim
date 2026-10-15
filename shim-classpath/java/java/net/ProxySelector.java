/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.net;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

public abstract class ProxySelector {

  private static ProxySelector theProxySelector;

  public static ProxySelector getDefault() {
    if (theProxySelector == null) {
      theProxySelector = new DefaultProxySelector();
    }
    return theProxySelector;
  }

  public static void setDefault(ProxySelector ps) {
    theProxySelector = ps;
  }

  public static ProxySelector of(InetSocketAddress proxyAddress) {
    if (proxyAddress == null) {
      return getDefault();
    }
    return new StaticProxySelector(new Proxy(Proxy.Type.HTTP, proxyAddress));
  }

  public abstract List<Proxy> select(URI uri);

  public abstract void connectFailed(URI uri, SocketAddress sa, IOException ioe);

  private static class DefaultProxySelector extends ProxySelector {
    @Override
    public List<Proxy> select(URI uri) {
      if (uri == null) {
        throw new IllegalArgumentException("URI can't be null.");
      }
      return Collections.singletonList(Proxy.NO_PROXY);
    }

    @Override
    public void connectFailed(URI uri, SocketAddress sa, IOException ioe) {}
  }

  private static class StaticProxySelector extends ProxySelector {
    private final Proxy proxy;

    StaticProxySelector(Proxy proxy) {
      this.proxy = proxy;
    }

    @Override
    public List<Proxy> select(URI uri) {
      if (uri == null) {
        throw new IllegalArgumentException("URI can't be null.");
      }
      return Collections.singletonList(proxy);
    }

    @Override
    public void connectFailed(URI uri, SocketAddress sa, IOException ioe) {}
  }
}
