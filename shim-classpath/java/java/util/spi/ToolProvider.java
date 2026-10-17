/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.util.spi;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Objects;
import java.util.Optional;
import java.util.ServiceLoader;

public interface ToolProvider {
  String name();

  default Optional<String> description() {
    return Optional.empty();
  }

  int run(PrintWriter out, PrintWriter err, String... args);

  default int run(PrintStream out, PrintStream err, String... args) {
    Objects.requireNonNull(out);
    Objects.requireNonNull(err);
    PrintWriter outWriter = new PrintWriter(out);
    PrintWriter errWriter = new PrintWriter(err);
    try {
      return run(outWriter, errWriter, args);
    } finally {
      outWriter.flush();
      errWriter.flush();
    }
  }

  static Optional<ToolProvider> findFirst(String name) {
    Objects.requireNonNull(name);
    ServiceLoader<ToolProvider> sl = ServiceLoader.load(ToolProvider.class, ClassLoader.getSystemClassLoader());
    for (ToolProvider provider : sl) {
      if (provider.name().equals(name)) {
        return Optional.of(provider);
      }
    }
    return Optional.empty();
  }
}
