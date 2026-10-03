/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package jdk.jfr.consumer;

public final class RecordedClassLoader extends RecordedObject {
  RecordedClassLoader(Chunk.TypeDef td, Object[] vals, Chunk chunk) {
    super(td, vals, chunk);
  }

  public RecordedClass getType() {
    return (RecordedClass) getValue("type");
  }

  public String getName() {
    return getString("name");
  }

  public long getId() {
    return 0;
  }
}
