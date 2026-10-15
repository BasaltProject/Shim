/*
 * Copyright (c) 2026 BasaltMC.
 *
 * SPDX-License-Identifier: MIT
 */

package java.lang.foreign;

public interface SequenceLayout extends MemoryLayout {
  MemoryLayout elementLayout();

  long elementCount();

  SequenceLayout withElementCount(long elementCount);

  SequenceLayout reshape(long... elementCounts);

  SequenceLayout flatten();

  @Override
  SequenceLayout withName(String name);

  @Override
  SequenceLayout withoutName();

  @Override
  SequenceLayout withByteAlignment(long byteAlignment);
}
