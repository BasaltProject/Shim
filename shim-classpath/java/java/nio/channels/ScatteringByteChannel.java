/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.nio.channels;

import java.io.IOException;
import java.nio.ByteBuffer;

public interface ScatteringByteChannel extends ReadableByteChannel {
  public long read(ByteBuffer[] dsts) throws IOException;
  public long read(ByteBuffer[] dsts, int offset, int length) throws IOException;
}
