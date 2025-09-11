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

public interface SeekableByteChannel extends ByteChannel {
  long position() throws IOException;
  SeekableByteChannel position(long newPosition) throws IOException;
  long size() throws IOException;
  SeekableByteChannel truncate(long size) throws IOException;
  int read(ByteBuffer dst) throws IOException;
  int write(ByteBuffer src) throws IOException;
}
