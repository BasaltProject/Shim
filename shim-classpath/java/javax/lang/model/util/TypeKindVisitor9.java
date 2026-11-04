/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.lang.model.util;

public class TypeKindVisitor9<R, P> extends javax.lang.model.util.TypeKindVisitor8<R, P> {
  protected TypeKindVisitor9() {
  }
  protected TypeKindVisitor9(R r) {
    super(r);
  }

  public R visitNoTypeAsModule(javax.lang.model.type.NoType noType, P p) {
    return defaultAction(noType, p);
  }
}
