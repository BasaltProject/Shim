/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.lang.instrument;

public final class ClassDefinition {
  private final Class<?> mClass;
  private final byte[] mClassFile;

  public ClassDefinition(Class<?> theClass, byte[] theClassFile) {
    if (theClass == null || theClassFile == null) {
      throw new NullPointerException();
    }
    mClass = theClass;
    mClassFile = theClassFile;
  }

  public Class<?> getDefinitionClass() {
    return mClass;
  }

  public byte[] getDefinitionClassFile() {
    return mClassFile;
  }
}
