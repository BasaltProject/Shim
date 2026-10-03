/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package jdk.jfr.consumer;

public final class RecordedStackTrace extends RecordedObject {
  RecordedStackTrace(Chunk.TypeDef td, Object[] vals, Chunk chunk) {
    super(td, vals, chunk);
  }

  @SuppressWarnings("unchecked")
  public java.util.List<RecordedFrame> getFrames() {
    return (java.util.List<RecordedFrame>) getValue("frames");
  }

  public boolean isTruncated() {
    return getBoolean("truncated");
  }
}
