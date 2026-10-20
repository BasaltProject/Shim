/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.tools;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.NestingKind;

public interface JavaFileObject extends FileObject {
  enum Kind {
    SOURCE(".java"), CLASS(".class"), HTML(".html"), OTHER("");

    public final String extension;

    Kind(String extension) {
      this.extension = java.util.Objects.requireNonNull(extension);
    }
  }

  Kind getKind();

  boolean isNameCompatible(String simpleName, Kind kind);

  NestingKind getNestingKind();

  Modifier getAccessLevel();
}
