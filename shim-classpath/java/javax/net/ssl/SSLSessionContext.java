/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.net.ssl;

import java.util.Enumeration;

public interface SSLSessionContext {
  SSLSession getSession(byte[] sessionId);

  Enumeration<byte[]> getIds();

  void setSessionTimeout(int seconds) throws IllegalArgumentException;

  int getSessionTimeout();

  void setSessionCacheSize(int size) throws IllegalArgumentException;

  int getSessionCacheSize();
}
