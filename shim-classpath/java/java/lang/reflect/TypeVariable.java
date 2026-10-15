/*
 * Based on Avian (https://github.com/ReadyTalk/avian).
 * Original work Copyright (c) 2008-2015, Avian Contributors.
 * Original work licensed under ISC; see LICENSE.txt:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE.txt
 *
 * Modifications Copyright (c) 2026 BasaltMC.
 * Modifications licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: ISC AND MIT
 */

package java.lang.reflect;

public interface TypeVariable<D extends GenericDeclaration> extends Type, AnnotatedElement {
  Type[] getBounds();
  D getGenericDeclaration();
  String getName();

  default AnnotatedType[] getAnnotatedBounds() {
    Type[] bounds = getBounds();
    AnnotatedType[] res = new AnnotatedType[bounds.length];
    for (int i = 0; i < bounds.length; i++) {
      res[i] = new AnnotatedTypeImpl(bounds[i]);
    }
    return res;
  }
}
