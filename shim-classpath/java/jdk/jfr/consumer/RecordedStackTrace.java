/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package jdk.jfr.consumer;

public final class RecordedStackTrace extends jdk.jfr.consumer.RecordedObject {
  public java.util.List<jdk.jfr.consumer.RecordedFrame> getFrames() { throw new UnsupportedOperationException("Flight Recorder is not available"); }
  public boolean isTruncated() { throw new UnsupportedOperationException("Flight Recorder is not available"); }
}
