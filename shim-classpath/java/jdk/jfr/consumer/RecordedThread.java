/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package jdk.jfr.consumer;

public final class RecordedThread extends RecordedObject {
  RecordedThread(Chunk.TypeDef td, Object[] vals, Chunk chunk) {
    super(td, vals, chunk);
  }

  public RecordedThreadGroup getThreadGroup() {
    return (RecordedThreadGroup) getValue("group");
  }

  public boolean isVirtual() {
    return getBoolean("virtual");
  }

  public long getId() {
    return getLong("javaThreadId");
  }

  public String getOSName() {
    return getString("osName");
  }

  public long getOSThreadId() {
    return getLong("osThreadId");
  }

  public String getJavaName() {
    return getString("javaName");
  }

  public long getJavaThreadId() {
    return getLong("javaThreadId");
  }
}
