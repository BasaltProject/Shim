/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.nio.file;

import java.util.List;

public interface WatchKey {
  boolean isValid();

  List<WatchEvent<?>> pollEvents();

  boolean reset();

  void cancel();

  Watchable watchable();
}
