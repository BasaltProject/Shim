/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package jdk.jfr.consumer;

public final class RecordedThreadGroup extends RecordedObject {
  RecordedThreadGroup(Chunk.TypeDef td, Object[] vals, Chunk chunk) {
    super(td, vals, chunk);
  }

  public RecordedThreadGroup getParent() {
    return (RecordedThreadGroup) getValue("parent");
  }

  public String getName() {
    return getString("name");
  }
}
