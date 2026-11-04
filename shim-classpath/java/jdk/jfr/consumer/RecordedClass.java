/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package jdk.jfr.consumer;

public final class RecordedClass extends RecordedObject {
  RecordedClass(Chunk.TypeDef td, Object[] vals, Chunk chunk) {
    super(td, vals, chunk);
  }

  public int getModifiers() {
    return getInt("modifiers");
  }

  public RecordedClassLoader getClassLoader() {
    return (RecordedClassLoader) getValue("classLoader");
  }

  public String getName() {
    return getString("name");
  }

  public long getId() {
    return 0;
  }
}
