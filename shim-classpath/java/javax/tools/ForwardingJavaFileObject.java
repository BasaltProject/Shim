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

public class ForwardingJavaFileObject<F extends JavaFileObject> extends ForwardingFileObject<F>
    implements JavaFileObject {
  protected ForwardingJavaFileObject(F fileObject) {
    super(fileObject);
  }

  public Kind getKind() {
    return fileObject.getKind();
  }

  public boolean isNameCompatible(String simpleName, Kind kind) {
    return fileObject.isNameCompatible(simpleName, kind);
  }

  public NestingKind getNestingKind() {
    return fileObject.getNestingKind();
  }

  public Modifier getAccessLevel() {
    return fileObject.getAccessLevel();
  }
}
