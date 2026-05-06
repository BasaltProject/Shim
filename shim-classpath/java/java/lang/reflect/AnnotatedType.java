/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.lang.reflect;

public interface AnnotatedType extends AnnotatedElement {
  Type getType();

  default AnnotatedType getAnnotatedOwnerType() {
    return null;
  }

  static AnnotatedType of(Type type) {
    return AnnotatedTypeImpl.create(type);
  }
}
