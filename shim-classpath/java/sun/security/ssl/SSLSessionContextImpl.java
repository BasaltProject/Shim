/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.security.ssl;

import java.util.Collections;
import java.util.Enumeration;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSessionContext;

final class SSLSessionContextImpl implements SSLSessionContext {
  private int timeout = 86400;
  private int cacheSize;

  SSLSessionContextImpl() {
  }

  @Override
  public SSLSession getSession(byte[] sessionId) {
    if (sessionId == null) {
      throw new NullPointerException("session id cannot be null");
    }
    return null;
  }

  @Override
  public Enumeration<byte[]> getIds() {
    return Collections.emptyEnumeration();
  }

  @Override
  public void setSessionTimeout(int seconds) {
    if (seconds < 0) {
      throw new IllegalArgumentException();
    }
    timeout = seconds;
  }

  @Override
  public int getSessionTimeout() {
    return timeout;
  }

  @Override
  public void setSessionCacheSize(int size) {
    if (size < 0) {
      throw new IllegalArgumentException();
    }
    cacheSize = size;
  }

  @Override
  public int getSessionCacheSize() {
    return cacheSize;
  }
}
