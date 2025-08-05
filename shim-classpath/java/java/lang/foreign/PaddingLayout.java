/*
 * Copyright (c) 2026 BasaltMC.
 *
 * SPDX-License-Identifier: MIT
 */

package java.lang.foreign;

public interface PaddingLayout extends MemoryLayout {
  @Override
  PaddingLayout withName(String name);

  @Override
  PaddingLayout withoutName();

  @Override
  PaddingLayout withByteAlignment(long byteAlignment);
}
