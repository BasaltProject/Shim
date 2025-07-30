/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package java.util;

public interface SequencedSet<E> extends SequencedCollection<E>, Set<E> {
  @Override
  SequencedSet<E> reversed();
}
