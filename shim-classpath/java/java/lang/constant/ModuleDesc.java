/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.lang.constant;

import java.util.Objects;

public interface ModuleDesc {
  static ModuleDesc of(String name) {
    Objects.requireNonNull(name, "name");
    for (int i = 0; i < name.length(); i++) {
      char c = name.charAt(i);
      if (c == '\\') {
        i++;
        if (i >= name.length()) {
          throw new IllegalArgumentException("Invalid module name: " + name);
        }
      } else if (c == '@' || c == ':' || c < 0x20) {
        throw new IllegalArgumentException("Invalid module name: " + name);
      }
    }
    return new ModuleDescImpl(name);
  }

  String name();
}
