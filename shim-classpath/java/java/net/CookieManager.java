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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CookieManager extends CookieHandler {

  private final CookiePolicy policy;
  private final CookieStore store;

  public CookieManager() {
    this(null, null);
  }

  public CookieManager(CookieStore store, CookiePolicy cookiePolicy) {
    this.policy = cookiePolicy != null ? cookiePolicy : CookiePolicy.ACCEPT_ORIGINAL_SERVER;
    this.store = store != null ? store : new InMemoryCookieStore();
  }

  public void setCookiePolicy(CookiePolicy cookiePolicy) {
  }

  public CookieStore getCookieStore() {
    return store;
  }

  @Override
  public Map<String, List<String>> get(URI uri, Map<String, List<String>> requestHeaders)
      throws IOException
  {
    if (uri == null || requestHeaders == null) {
      throw new IllegalArgumentException("Argument is null");
    }
    List<HttpCookie> cookies = store.get(uri);
    if (cookies.isEmpty()) {
      return Collections.emptyMap();
    }
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < cookies.size(); i++) {
      if (i > 0) sb.append("; ");
      sb.append(cookies.get(i).toString());
    }
    Map<String, List<String>> result = new HashMap<String, List<String>>();
    result.put("Cookie", Collections.singletonList(sb.toString()));
    return Collections.unmodifiableMap(result);
  }

  @Override
  public void put(URI uri, Map<String, List<String>> responseHeaders) throws IOException {
    if (uri == null || responseHeaders == null) {
      throw new IllegalArgumentException("Argument is null");
    }
    for (Map.Entry<String, List<String>> entry : responseHeaders.entrySet()) {
      String key = entry.getKey();
      if (key != null && (key.equalsIgnoreCase("Set-Cookie") || key.equalsIgnoreCase("Set-Cookie2"))) {
        for (String headerValue : entry.getValue()) {
          try {
            List<HttpCookie> cookies = HttpCookie.parse(headerValue);
            for (HttpCookie cookie : cookies) {
              if (cookie.getPath() == null) {
                String path = uri.getPath();
                if (path == null || path.isEmpty()) path = "/";
                cookie.setPath(path);
              }
              if (cookie.getDomain() == null) {
                cookie.setDomain(uri.getHost());
              }
              if (policy.shouldAccept(uri, cookie)) {
                store.add(uri, cookie);
              }
            }
          } catch (Exception ignored) {}
        }
      }
    }
  }

  private static class InMemoryCookieStore implements CookieStore {
    private final List<HttpCookie> cookies = new ArrayList<HttpCookie>();

    @Override
    public synchronized void add(URI uri, HttpCookie cookie) {
      if (cookie == null) throw new NullPointerException("cookie");
      cookies.remove(cookie);
      if (!cookie.hasExpired()) {
        cookies.add(cookie);
      }
    }

    @Override
    public synchronized List<HttpCookie> get(URI uri) {
      if (uri == null) throw new NullPointerException("uri");
      List<HttpCookie> result = new ArrayList<HttpCookie>();
      for (HttpCookie c : cookies) {
        if (!c.hasExpired()) {
          result.add(c);
        }
      }
      return Collections.unmodifiableList(result);
    }

    @Override
    public synchronized List<HttpCookie> getCookies() {
      return Collections.unmodifiableList(new ArrayList<HttpCookie>(cookies));
    }

    @Override
    public synchronized List<URI> getURIs() {
      return Collections.emptyList();
    }

    @Override
    public synchronized boolean remove(URI uri, HttpCookie cookie) {
      return cookies.remove(cookie);
    }

    @Override
    public synchronized boolean removeAll() {
      cookies.clear();
      return true;
    }
  }
}
