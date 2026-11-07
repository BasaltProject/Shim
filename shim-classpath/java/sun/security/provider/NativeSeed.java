/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.security.provider;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.ProviderException;

final class NativeSeed {
  private NativeSeed() {
  }

  private static final boolean WINDOWS = System.getProperty("os.name", "").startsWith("Windows");

  // The system generator on Windows, which has no random device to read
  private static native boolean random0(byte[] out, int off, int len);

  static void read(String path, byte[] out, int off, int len) {
    if (WINDOWS) {
      if (!random0(out, off, len)) {
        throw new ProviderException("nextBytes() failed");
      }
      return;
    }
    try (FileInputStream in = new FileInputStream(path)) {
      int n = 0;
      while (n < len) {
        int count = in.read(out, off + n, len - n);
        if (count <= 0) {
          throw new IOException("Entropy source ended before filling request");
        }
        n += count;
      }
    } catch (IOException e) {
      throw new ProviderException("nextBytes() failed", e);
    }
  }

  static byte[] generate(int n) {
    byte[] b = new byte[n];
    read("/dev/urandom", b, 0, n);
    return b;
  }
}
