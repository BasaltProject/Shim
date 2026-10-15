/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.net;

import java.util.List;

public interface CookieStore {
  public void add(URI uri, HttpCookie cookie);
  public List<HttpCookie> get(URI uri);
  public List<HttpCookie> getCookies();
  public List<URI> getURIs();
  public boolean remove(URI uri, HttpCookie cookie);
  public boolean removeAll();
}
