/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing;

public class SizeSequence {
  private int[] sizes;
  private long[] starts;

  public SizeSequence() { this(0); }

  public SizeSequence(int numEntries) { this(numEntries, 0); }

  public SizeSequence(int numEntries, int value) {
    this(new int[numEntries]);
    java.util.Arrays.fill(sizes, value);
    starts = null;
  }

  public SizeSequence(int[] sizes) {
    this.sizes = new int[0];
    setSizes(sizes);
  }

  void setSizes(int length, int size) {
    sizes = new int[length];
    java.util.Arrays.fill(sizes, size);
    starts = null;
  }

  public void setSizes(int[] sizes) {
    this.sizes = sizes.clone();
    starts = null;
  }

  public int[] getSizes() { return sizes.clone(); }

  private void ensureStarts() {
    if (starts == null) {
      starts = new long[sizes.length + 1];
      for (int i = 0; i < sizes.length; i++) {
        starts[i + 1] = starts[i] + sizes[i];
      }
    }
  }

  public int getPosition(int index) {
    if (index < 0) {
      return 0;
    }
    ensureStarts();
    if (index > sizes.length) {
      index = sizes.length;
    }
    return (int) starts[index];
  }

  public int getIndex(int position) {
    ensureStarts();
    if (position < 0) {
      return 0;
    }
    int lo = 0;
    int hi = sizes.length;
    while (lo < hi) {
      int mid = (lo + hi) >>> 1;
      if (starts[mid + 1] <= position) {
        lo = mid + 1;
      } else {
        hi = mid;
      }
    }
    return lo;
  }

  public int getSize(int index) {
    if (index < 0 || index >= sizes.length) {
      return 0;
    }
    return sizes[index];
  }

  public void setSize(int index, int size) {
    if (index >= 0 && index < sizes.length) {
      sizes[index] = size;
      starts = null;
    }
  }

  public void insertEntries(int start, int length, int value) {
    int[] n = new int[sizes.length + length];
    System.arraycopy(sizes, 0, n, 0, start);
    java.util.Arrays.fill(n, start, start + length, value);
    System.arraycopy(sizes, start, n, start + length, sizes.length - start);
    sizes = n;
    starts = null;
  }

  public void removeEntries(int start, int length) {
    int[] n = new int[sizes.length - length];
    System.arraycopy(sizes, 0, n, 0, start);
    System.arraycopy(sizes, start + length, n, start, sizes.length - start - length);
    sizes = n;
    starts = null;
  }
}
