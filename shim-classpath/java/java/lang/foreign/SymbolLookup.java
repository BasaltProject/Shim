/*
 * Copyright (c) 2026 BasaltMC.
 *
 * SPDX-License-Identifier: MIT
 */

package java.lang.foreign;

import java.nio.file.Path;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;

@FunctionalInterface
public interface SymbolLookup {
  Optional<MemorySegment> find(String name);

  default MemorySegment findOrThrow(String name) {
    return find(name).orElseThrow(() -> new NoSuchElementException("Cannot find symbol: " + name));
  }

  default SymbolLookup or(SymbolLookup other) {
    Objects.requireNonNull(other, "other");
    return name -> find(name).or(() -> other.find(name));
  }

  static SymbolLookup loaderLookup() {
    return NativeLinkerImpl.LOADER_LOOKUP;
  }

  static SymbolLookup libraryLookup(String name, Arena arena) {
    Objects.requireNonNull(name, "name");
    Objects.requireNonNull(arena, "arena");
    return NativeLinkerImpl.libraryLookup(name, arena);
  }

  static SymbolLookup libraryLookup(Path path, Arena arena) {
    Objects.requireNonNull(path, "path");
    Objects.requireNonNull(arena, "arena");
    return libraryLookup(path.toString(), arena);
  }
}
