/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package jdk.internal.random;

import java.util.Objects;
import java.util.random.RandomGenerator.SplittableGenerator;
import java.util.stream.Stream;

public final class SplitStreams {
  private SplitStreams() {
  }

  public static Stream<SplittableGenerator> splits(SplittableGenerator g) {
    return Stream.generate(g::split);
  }

  public static Stream<SplittableGenerator> splits(SplittableGenerator g, long streamSize) {
    if (streamSize < 0L) {
      throw new IllegalArgumentException("size must be non-negative");
    }
    return splits(g).limit(streamSize);
  }

  public static Stream<SplittableGenerator> splits(SplittableGenerator g, SplittableGenerator source) {
    Objects.requireNonNull(source, "source");
    return Stream.generate(() -> g.split(source));
  }

  public static Stream<SplittableGenerator> splits(SplittableGenerator g, long streamSize,
      SplittableGenerator source) {
    if (streamSize < 0L) {
      throw new IllegalArgumentException("size must be non-negative");
    }
    return splits(g, source).limit(streamSize);
  }
}
