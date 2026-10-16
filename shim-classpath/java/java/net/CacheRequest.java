/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.net;

import java.io.IOException;
import java.io.OutputStream;

public abstract class CacheRequest {
  public CacheRequest() { }

  public abstract OutputStream getBody() throws IOException;

  public abstract void abort();
}
