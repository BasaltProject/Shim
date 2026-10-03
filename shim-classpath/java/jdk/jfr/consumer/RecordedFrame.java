/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package jdk.jfr.consumer;

public final class RecordedFrame extends RecordedObject {
  RecordedFrame(Chunk.TypeDef td, Object[] vals, Chunk chunk) {
    super(td, vals, chunk);
  }

  public boolean isJavaFrame() {
    String t = getType();
    return t != null && (t.equals("Interpreted") || t.equals("JIT compiled") || t.equals("Inlined"));
  }

  public int getBytecodeIndex() {
    return getInt("bytecodeIndex");
  }

  public int getLineNumber() {
    return getInt("lineNumber");
  }

  public String getType() {
    return getString("type");
  }

  public RecordedMethod getMethod() {
    return (RecordedMethod) getValue("method");
  }
}
