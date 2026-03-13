/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package jdk.jfr.consumer;

public final class RecordedFrame extends jdk.jfr.consumer.RecordedObject {
  public jdk.jfr.consumer.RecordedMethod getMethod() { throw new UnsupportedOperationException("Flight Recorder is not available"); }
  public java.lang.String getType() { throw new UnsupportedOperationException("Flight Recorder is not available"); }
  public int getLineNumber() { throw new UnsupportedOperationException("Flight Recorder is not available"); }
  public boolean isJavaFrame() { throw new UnsupportedOperationException("Flight Recorder is not available"); }
  public int getBytecodeIndex() { throw new UnsupportedOperationException("Flight Recorder is not available"); }
}
