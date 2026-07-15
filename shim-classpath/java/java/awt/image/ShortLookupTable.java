/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.awt.image;

public class ShortLookupTable extends LookupTable {
  short[][] data;

  public ShortLookupTable(int offset, short[][] data) {
    super(offset, data.length);
    numComponents = data.length;
    numEntries = data[0].length;
    this.data = new short[numComponents][];
    for (int i = 0; i < numComponents; i++) {
      this.data[i] = data[i];
    }
  }

  public ShortLookupTable(int offset, short[] data) {
    super(offset, 1);
    numComponents = 1;
    numEntries = data.length;
    this.data = new short[1][];
    this.data[0] = data;
  }

  public final short[][] getTable() { return data; }

  public int[] lookupPixel(int[] src, int[] dst) {
    if (dst == null) {
      dst = new int[src.length];
    }
    if (numComponents == 1) {
      for (int i = 0; i < src.length; i++) {
        int s = src[i] - offset;
        if (s < 0) {
          throw new ArrayIndexOutOfBoundsException("src[" + i + "]-offset is less than zero");
        }
        dst[i] = data[0][s] & 0xffff;
      }
    } else {
      for (int i = 0; i < src.length; i++) {
        int s = src[i] - offset;
        if (s < 0) {
          throw new ArrayIndexOutOfBoundsException("src[" + i + "]-offset is less than zero");
        }
        dst[i] = data[i][s] & 0xffff;
      }
    }
    return dst;
  }

  public short[] lookupPixel(short[] src, short[] dst) {
    if (dst == null) {
      dst = new short[src.length];
    }
    if (numComponents == 1) {
      for (int i = 0; i < src.length; i++) {
        int s = (src[i] & 0xffff) - offset;
        if (s < 0) {
          throw new ArrayIndexOutOfBoundsException("src[" + i + "]-offset is less than zero");
        }
        dst[i] = data[0][s];
      }
    } else {
      for (int i = 0; i < src.length; i++) {
        int s = (src[i] & 0xffff) - offset;
        if (s < 0) {
          throw new ArrayIndexOutOfBoundsException("src[" + i + "]-offset is less than zero");
        }
        dst[i] = data[i][s];
      }
    }
    return dst;
  }
}
