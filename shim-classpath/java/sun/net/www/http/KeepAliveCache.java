/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.net.www.http;

import java.net.URL;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

final class KeepAliveCache {
  private static final class Key {
    final String protocol;
    final String host;
    final int port;
    final Object context;

    Key(URL url, Object context) {
      this.protocol = url.getProtocol();
      this.host = url.getHost();
      this.port = url.getPort();
      this.context = context;
    }

    @Override
    public boolean equals(Object o) {
      if (!(o instanceof Key)) {
        return false;
      }
      Key k = (Key) o;
      return protocol.equals(k.protocol) && host.equalsIgnoreCase(k.host) && port == k.port
          && Objects.equals(context, k.context);
    }

    @Override
    public int hashCode() {
      return protocol.hashCode() + host.toLowerCase().hashCode() + port + Objects.hashCode(context);
    }
  }

  private static final class Entry {
    final HttpClient client;
    final long expires;

    Entry(HttpClient client, long expires) {
      this.client = client;
      this.expires = expires;
    }
  }

  private static final Map<Key, ArrayDeque<Entry>> kCache = new HashMap<Key, ArrayDeque<Entry>>();

  private KeepAliveCache() {
  }

  static synchronized void put(URL url, Object context, HttpClient client, int timeoutSeconds, int maxConnections) {
    Key key = new Key(url, context);
    ArrayDeque<Entry> list = kCache.get(key);
    if (list == null) {
      list = new ArrayDeque<Entry>();
      kCache.put(key, list);
    }
    long now = System.currentTimeMillis();
    for (Iterator<Entry> it = list.iterator(); it.hasNext();) {
      Entry e = it.next();
      if (e.expires <= now) {
        it.remove();
        e.client.closeServer();
      }
    }
    while (list.size() >= Math.max(1, maxConnections)) {
      list.removeLast().client.closeServer();
    }
    list.addFirst(new Entry(client, now + timeoutSeconds * 1000L));
  }

  static synchronized HttpClient get(URL url, Object context) {
    Key key = new Key(url, context);
    ArrayDeque<Entry> list = kCache.get(key);
    if (list == null) {
      return null;
    }
    long now = System.currentTimeMillis();
    while (!list.isEmpty()) {
      Entry e = list.removeFirst();
      if (e.expires > now && e.client.isUsable()) {
        return e.client;
      }
      e.client.closeServer();
    }
    kCache.remove(key);
    return null;
  }
}
