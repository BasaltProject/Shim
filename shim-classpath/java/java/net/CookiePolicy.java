/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.net;

public interface CookiePolicy {
  public static final CookiePolicy ACCEPT_ALL = (uri, cookie) -> true;
  public static final CookiePolicy ACCEPT_NONE = (uri, cookie) -> false;
  public static final CookiePolicy ACCEPT_ORIGINAL_SERVER = (uri, cookie) -> {
    if (uri == null || cookie == null) return false;
    String host = uri.getHost();
    return host != null && host.equalsIgnoreCase(cookie.getDomain());
  };

  public boolean shouldAccept(URI uri, HttpCookie cookie);
}
