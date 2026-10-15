/*
 * Copyright (c) 2026 BasaltMC.
 *
 * SPDX-License-Identifier: MIT
 */

package java.lang.foreign;

final class PathElements {
  private PathElements() {
  }

  static final class GroupByName implements MemoryLayout.PathElement {
    final String name;

    GroupByName(String name) {
      this.name = name;
    }
  }

  static final class GroupByIndex implements MemoryLayout.PathElement {
    final long index;

    GroupByIndex(long index) {
      this.index = index;
    }
  }

  static final class SequenceByIndex implements MemoryLayout.PathElement {
    final long index;

    SequenceByIndex(long index) {
      this.index = index;
    }
  }

  static final class SequenceByRange implements MemoryLayout.PathElement {
    final long start;
    final long step;

    SequenceByRange(long start, long step) {
      this.start = start;
      this.step = step;
    }
  }

  static final class OpenSequence implements MemoryLayout.PathElement {
    static final OpenSequence INSTANCE = new OpenSequence();

    private OpenSequence() {
    }
  }

  static final class Dereference implements MemoryLayout.PathElement {
    static final Dereference INSTANCE = new Dereference();

    private Dereference() {
    }
  }
}
