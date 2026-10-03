/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package jdk.jfr.consumer;

public final class RecordedMethod extends RecordedObject {
  RecordedMethod(Chunk.TypeDef td, Object[] vals, Chunk chunk) {
    super(td, vals, chunk);
  }

  public RecordedClass getType() {
    return (RecordedClass) getValue("type");
  }

  public String getName() {
    return getString("name");
  }

  public String getDescriptor() {
    return getString("descriptor");
  }

  public int getModifiers() {
    return getInt("modifiers");
  }

  public boolean isHidden() {
    return getBoolean("hidden");
  }
}
