/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.lang.reflect;

public interface AnnotatedWildcardType extends AnnotatedType {
  AnnotatedType[] getAnnotatedLowerBounds();

  AnnotatedType[] getAnnotatedUpperBounds();

  @Override
  default AnnotatedType getAnnotatedOwnerType() {
    return null;
  }
}
