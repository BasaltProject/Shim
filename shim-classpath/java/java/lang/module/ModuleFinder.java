/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.lang.module;

import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import shim.module.Finders;

public interface ModuleFinder {
  Optional<ModuleReference> find(String name);

  Set<ModuleReference> findAll();

  static ModuleFinder ofSystem() {
    return Finders.system();
  }

  static ModuleFinder of(Path... entries) {
    return Finders.ofPaths(entries.clone());
  }

  static ModuleFinder compose(ModuleFinder... finders) {
    return Finders.compose(finders.clone());
  }
}
