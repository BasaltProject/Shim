/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.util.zip;

import java.nio.ByteBuffer;
import java.util.Objects;

public interface Checksum {
  void update(int b);

  void update(byte[] b, int off, int len);

  default void update(byte[] b) {
    Objects.requireNonNull(b, "b");
    update(b, 0, b.length);
  }

  default void update(ByteBuffer buffer) {
    Objects.requireNonNull(buffer, "buffer");
    int rem = buffer.remaining();
    if (rem <= 0) {
      return;
    }
    if (buffer.hasArray()) {
      update(buffer.array(), buffer.arrayOffset() + buffer.position(), rem);
      buffer.position(buffer.limit());
    } else {
      byte[] b = new byte[Math.min(rem, 4096)];
      while (buffer.hasRemaining()) {
        int n = Math.min(buffer.remaining(), b.length);
        buffer.get(b, 0, n);
        update(b, 0, n);
      }
    }
  }

  long getValue();

  void reset();
}
