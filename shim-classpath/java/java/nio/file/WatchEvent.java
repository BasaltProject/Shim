/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.nio.file;

public interface WatchEvent<T> {
  public interface Kind<T> {
    String name();
    Class<T> type();
  }

  public interface Modifier {
    String name();
  }

  Kind<T> kind();
  int count();
  T context();
}
