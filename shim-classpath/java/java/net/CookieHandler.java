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
import java.util.List;
import java.util.Map;

public abstract class CookieHandler {

  private static CookieHandler theCookieHandler;

  public static synchronized CookieHandler getDefault() {
    return theCookieHandler;
  }

  public static synchronized void setDefault(CookieHandler cHandler) {
    theCookieHandler = cHandler;
  }

  public abstract Map<String, List<String>> get(URI uri, Map<String, List<String>> requestHeaders)
      throws IOException;

  public abstract void put(URI uri, Map<String, List<String>> responseHeaders) throws IOException;
}
