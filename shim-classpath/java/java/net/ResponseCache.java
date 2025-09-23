/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.net;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public abstract class ResponseCache {
  private static ResponseCache theResponseCache;

  public static synchronized ResponseCache getDefault() {
    return theResponseCache;
  }

  public static synchronized void setDefault(ResponseCache responseCache) {
    theResponseCache = responseCache;
  }

  public abstract CacheResponse get(URI uri, String rqstMethod, Map<String, List<String>> rqstHeaders)
      throws IOException;

  public abstract CacheRequest put(URI uri, URLConnection conn) throws IOException;
}
