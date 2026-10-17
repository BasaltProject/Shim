/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.lang.module;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.Optional;
import java.util.stream.Stream;

public interface ModuleReader extends Closeable {
  Optional<URI> find(String name) throws IOException;

  default Optional<InputStream> open(String name) throws IOException {
    Optional<URI> ouri = find(name);
    if (ouri.isPresent()) {
      return Optional.of(ouri.get().toURL().openStream());
    }
    return Optional.empty();
  }

  default Optional<ByteBuffer> read(String name) throws IOException {
    Optional<InputStream> oin = open(name);
    if (oin.isPresent()) {
      try (InputStream in = oin.get()) {
        return Optional.of(ByteBuffer.wrap(in.readAllBytes()));
      }
    }
    return Optional.empty();
  }

  default void release(ByteBuffer bb) {
    java.util.Objects.requireNonNull(bb);
  }

  Stream<String> list() throws IOException;

  @Override
  void close() throws IOException;
}
