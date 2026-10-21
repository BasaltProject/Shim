/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package jdk.jfr.consumer;

public final class RecordedThread extends jdk.jfr.consumer.RecordedObject {
  public jdk.jfr.consumer.RecordedThreadGroup getThreadGroup() { throw new UnsupportedOperationException("Flight Recorder is not available"); }
  public boolean isVirtual() { throw new UnsupportedOperationException("Flight Recorder is not available"); }
  public long getId() { throw new UnsupportedOperationException("Flight Recorder is not available"); }
  public java.lang.String getOSName() { throw new UnsupportedOperationException("Flight Recorder is not available"); }
  public long getOSThreadId() { throw new UnsupportedOperationException("Flight Recorder is not available"); }
  public java.lang.String getJavaName() { throw new UnsupportedOperationException("Flight Recorder is not available"); }
  public long getJavaThreadId() { throw new UnsupportedOperationException("Flight Recorder is not available"); }
}
