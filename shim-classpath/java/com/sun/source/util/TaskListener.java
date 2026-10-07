/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.source.util;

public interface TaskListener {
  default void finished(com.sun.source.util.TaskEvent taskEvent) {

  }
  default void started(com.sun.source.util.TaskEvent taskEvent) {

  }
}
