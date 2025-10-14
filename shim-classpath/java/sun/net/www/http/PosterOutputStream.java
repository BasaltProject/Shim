/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.net.www.http;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class PosterOutputStream extends ByteArrayOutputStream {
  private boolean closed;

  public PosterOutputStream() {
    super(256);
  }

  @Override
  public synchronized void write(int b) {
    if (closed) {
      return;
    }
    super.write(b);
  }

  @Override
  public synchronized void write(byte[] b, int off, int len) {
    if (closed) {
      return;
    }
    super.write(b, off, len);
  }

  @Override
  public synchronized void reset() {
    if (closed) {
      return;
    }
    super.reset();
  }

  @Override
  public synchronized void close() throws IOException {
    closed = true;
    super.close();
  }
}
