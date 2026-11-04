/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.source.util;

public interface Plugin {
  default boolean autoStart() {
    return false;
  }
  java.lang.String getName();
  void init(com.sun.source.util.JavacTask javacTask, java.lang.String... strings);
}
