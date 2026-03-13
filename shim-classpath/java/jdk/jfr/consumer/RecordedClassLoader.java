/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package jdk.jfr.consumer;

public final class RecordedClassLoader extends jdk.jfr.consumer.RecordedObject {
  public java.lang.String getName() { throw new UnsupportedOperationException("Flight Recorder is not available"); }
  public long getId() { throw new UnsupportedOperationException("Flight Recorder is not available"); }
  public jdk.jfr.consumer.RecordedClass getType() { throw new UnsupportedOperationException("Flight Recorder is not available"); }
}
