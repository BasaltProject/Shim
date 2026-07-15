/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.awt.image;

import java.util.Arrays;

public class SinglePixelPackedSampleModel extends SampleModel {
  private final int[] bitMasks;
  private final int[] bitOffsets;
  private final int[] bitSizes;
  private final int maxBitSize;
  private final int scanlineStride;

  public SinglePixelPackedSampleModel(int dataType, int w, int h, int[] bitMasks) {
    this(dataType, w, h, w, bitMasks);
    if (dataType != DataBuffer.TYPE_BYTE && dataType != DataBuffer.TYPE_USHORT && dataType != DataBuffer.TYPE_INT) {
      throw new IllegalArgumentException("Unsupported data type " + dataType);
    }
  }

  public SinglePixelPackedSampleModel(int dataType, int w, int h, int scanlineStride, int[] bitMasks) {
    super(dataType, w, h, bitMasks.length);
    if (dataType != DataBuffer.TYPE_BYTE && dataType != DataBuffer.TYPE_USHORT && dataType != DataBuffer.TYPE_INT) {
      throw new IllegalArgumentException("Unsupported data type " + dataType);
    }
    this.scanlineStride = scanlineStride;
    this.bitMasks = bitMasks.clone();
    this.bitOffsets = new int[numBands];
    this.bitSizes = new int[numBands];
    int max = 0;
    for (int i = 0; i < numBands; i++) {
      int mask = bitMasks[i];
      int off = mask == 0 ? 0 : Integer.numberOfTrailingZeros(mask);
      bitOffsets[i] = off;
      bitSizes[i] = Integer.bitCount(mask >>> off);
      if (mask != 0 && (mask >>> off) != (1 << bitSizes[i]) - 1 && bitSizes[i] != 32) {
        // non-contiguous masks keep the popcount as the size
      }
      max = Math.max(max, bitSizes[i]);
    }
    this.maxBitSize = max;
  }

  public int getNumDataElements() { return 1; }

  public SampleModel createCompatibleSampleModel(int w, int h) {
    return new SinglePixelPackedSampleModel(dataType, w, h, bitMasks);
  }

  public DataBuffer createDataBuffer() {
    return newBuffer(dataType, scanlineStride * (height - 1) + width, 1);
  }

  public int[] getSampleSize() { return bitSizes.clone(); }

  public int getSampleSize(int band) { return bitSizes[band]; }

  public int getOffset(int x, int y) { return y * scanlineStride + x; }

  public int[] getBitOffsets() { return bitOffsets.clone(); }

  public int[] getBitMasks() { return bitMasks.clone(); }

  public int getScanlineStride() { return scanlineStride; }

  public SampleModel createSubsetSampleModel(int[] bands) {
    if (bands.length > numBands) {
      throw new RasterFormatException("There are only " + numBands + " bands");
    }
    int[] masks = new int[bands.length];
    for (int i = 0; i < bands.length; i++) {
      masks[i] = bitMasks[bands[i]];
    }
    return new SinglePixelPackedSampleModel(dataType, width, height, scanlineStride, masks);
  }

  private void check(int x, int y) {
    if (x < 0 || y < 0 || x >= width || y >= height) {
      throw new ArrayIndexOutOfBoundsException("Coordinate out of bounds!");
    }
  }

  public Object getDataElements(int x, int y, Object obj, DataBuffer data) {
    check(x, y);
    int v = data.getElem(y * scanlineStride + x);
    switch (getTransferType()) {
    case DataBuffer.TYPE_BYTE: {
      byte[] a = obj != null ? (byte[]) obj : new byte[1];
      a[0] = (byte) v;
      return a;
    }
    case DataBuffer.TYPE_USHORT: {
      short[] a = obj != null ? (short[]) obj : new short[1];
      a[0] = (short) v;
      return a;
    }
    default: {
      int[] a = obj != null ? (int[]) obj : new int[1];
      a[0] = v;
      return a;
    }
    }
  }

  public void setDataElements(int x, int y, Object obj, DataBuffer data) {
    check(x, y);
    int v;
    switch (getTransferType()) {
    case DataBuffer.TYPE_BYTE:
      v = ((byte[]) obj)[0] & 0xff;
      break;
    case DataBuffer.TYPE_USHORT:
      v = ((short[]) obj)[0] & 0xffff;
      break;
    default:
      v = ((int[]) obj)[0];
      break;
    }
    data.setElem(y * scanlineStride + x, v);
  }

  public int getSample(int x, int y, int b, DataBuffer data) {
    check(x, y);
    return (data.getElem(y * scanlineStride + x) & bitMasks[b]) >>> bitOffsets[b];
  }

  public void setSample(int x, int y, int b, int s, DataBuffer data) {
    check(x, y);
    int i = y * scanlineStride + x;
    int v = data.getElem(i);
    v &= ~bitMasks[b];
    v |= (s << bitOffsets[b]) & bitMasks[b];
    data.setElem(i, v);
  }

  public int[] getPixel(int x, int y, int[] iArray, DataBuffer data) {
    check(x, y);
    int[] out = iArray != null ? iArray : new int[numBands];
    int v = data.getElem(y * scanlineStride + x);
    for (int b = 0; b < numBands; b++) {
      out[b] = (v & bitMasks[b]) >>> bitOffsets[b];
    }
    return out;
  }

  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    SinglePixelPackedSampleModel that = null;
    if (!(((that = o instanceof SinglePixelPackedSampleModel ? (SinglePixelPackedSampleModel) o : null) != null))) {
      return false;
    }
    return width == that.width && height == that.height && numBands == that.numBands && dataType == that.dataType
        && Arrays.equals(bitMasks, that.bitMasks) && Arrays.equals(bitOffsets, that.bitOffsets)
        && Arrays.equals(bitSizes, that.bitSizes) && maxBitSize == that.maxBitSize
        && scanlineStride == that.scanlineStride;
  }

  public int hashCode() {
    int h = width;
    h <<= 8;
    h ^= height;
    h <<= 8;
    h ^= numBands;
    h <<= 8;
    h ^= dataType;
    h <<= 8;
    for (int m : bitMasks) {
      h ^= m;
      h <<= 8;
    }
    for (int m : bitOffsets) {
      h ^= m;
      h <<= 8;
    }
    for (int m : bitSizes) {
      h ^= m;
      h <<= 8;
    }
    h ^= maxBitSize;
    h <<= 8;
    h ^= scanlineStride;
    return h;
  }
}
