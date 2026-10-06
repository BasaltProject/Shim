/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.nio.file;

public enum SensitivityWatchEventModifier implements java.nio.file.WatchEvent.Modifier {
  HIGH, MEDIUM, LOW;


  public int sensitivityValueInSeconds() {
    throw new UnsupportedOperationException();
  }
}
