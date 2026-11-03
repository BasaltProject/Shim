/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.awt.image;

import java.util.Arrays;

public class ComponentSampleModel extends SampleModel {
  protected int[] bandOffsets;
  protected int[] bankIndices;
  protected int numBands = 1;
  protected int numBanks = 1;
  protected int scanlineStride;
  protected int pixelStride;

  public ComponentSampleModel(int dataType, int w, int h, int pixelStride, int scanlineStride, int[] bandOffsets) {
    super(dataType, w, h, bandOffsets.length);
    init(dataType, pixelStride, scanlineStride, bandOffsets, new int[bandOffsets.length]);
  }

  public ComponentSampleModel(int dataType, int w, int h, int pixelStride, int scanlineStride, int[] bankIndices,
      int[] bandOffsets) {
    super(dataType, w, h, bandOffsets.length);
    init(dataType, pixelStride, scanlineStride, bandOffsets, bankIndices);
  }

  private void init(int dataType, int pixelStride, int scanlineStride, int[] bandOffsets, int[] bankIndices) {
    if (dataType < DataBuffer.TYPE_BYTE || dataType > DataBuffer.TYPE_DOUBLE) {
      throw new IllegalArgumentException("Unsupported dataType: " + dataType);
    }
    if (pixelStride < 0) {
      throw new IllegalArgumentException("Pixel stride must be >= 0");
    }
    if (scanlineStride < 0) {
      throw new IllegalArgumentException("Scanline stride must be >= 0");
    }
    if (bandOffsets.length < 1) {
      throw new IllegalArgumentException("Number of bands must be > 0");
    }
    if (bandOffsets.length != bankIndices.length) {
      throw new IllegalArgumentException("Length of bandOffsets must equal length of bankIndices.");
    }
    this.pixelStride = pixelStride;
    this.scanlineStride = scanlineStride;
    this.bandOffsets = bandOffsets.clone();
    this.bankIndices = bankIndices.clone();
    this.numBands = bandOffsets.length;
    int max = 0;
    for (int b : bankIndices) {
      if (b < 0) {
        throw new IllegalArgumentException("Index of bank " + b + " is negative.");
      }
      max = Math.max(max, b);
    }
    this.numBanks = max + 1;
    super.numBands = bandOffsets.length;
  }

  private int bufferSize() {
    int min = bandOffsets[0];
    int max = bandOffsets[0];
    for (int o : bandOffsets) {
      min = Math.min(min, o);
      max = Math.max(max, o);
    }
    return max + (height - 1) * scanlineStride + (width - 1) * pixelStride + 1;
  }

  private int[] normalizedOffsets() {
    int[] out = new int[numBands];
    int min = bandOffsets[0];
    for (int o : bandOffsets) {
      min = Math.min(min, o);
    }
    for (int i = 0; i < numBands; i++) {
      out[i] = bandOffsets[i] - min;
    }
    return out;
  }

  public SampleModel createCompatibleSampleModel(int w, int h) {
    int[] off = normalizedOffsets();
    int bandSpan = 0;
    for (int o : off) {
      bandSpan = Math.max(bandSpan, o);
    }
    int ps = pixelStride;
    int ss = Math.max(ps * w, 0);
    if (numBanks == 1 && ps > 0) {
      ss = ps * w;
      return new ComponentSampleModel(dataType, w, h, ps, ss, bankIndices, off);
    }
    return new ComponentSampleModel(dataType, w, h, ps, w * Math.max(ps, 1), bankIndices, off);
  }

  public SampleModel createSubsetSampleModel(int[] bands) {
    if (bands.length > numBands) {
      throw new RasterFormatException("There are only " + numBands + " bands");
    }
    int[] off = new int[bands.length];
    int[] bank = new int[bands.length];
    for (int i = 0; i < bands.length; i++) {
      off[i] = bandOffsets[bands[i]];
      bank[i] = bankIndices[bands[i]];
    }
    return new ComponentSampleModel(dataType, width, height, pixelStride, scanlineStride, bank, off);
  }

  public DataBuffer createDataBuffer() {
    return newBuffer(dataType, bufferSize(), numBanks);
  }

  public int getOffset(int x, int y) {
    return y * scanlineStride + x * pixelStride + bandOffsets[0];
  }

  public int getOffset(int x, int y, int b) {
    return y * scanlineStride + x * pixelStride + bandOffsets[b];
  }

  public final int[] getSampleSize() {
    int[] out = new int[numBands];
    Arrays.fill(out, typeBits(dataType));
    return out;
  }

  public final int getSampleSize(int band) {
    return typeBits(dataType);
  }

  public final int[] getBankIndices() { return bankIndices.clone(); }

  public final int[] getBandOffsets() { return bandOffsets.clone(); }

  public final int getScanlineStride() { return scanlineStride; }

  public final int getPixelStride() { return pixelStride; }

  public final int getNumDataElements() { return getNumBands(); }

  private void check(int x, int y) {
    if (x < 0 || y < 0 || x >= width || y >= height) {
      throw new ArrayIndexOutOfBoundsException("Coordinate out of bounds!");
    }
  }

  public Object getDataElements(int x, int y, Object obj, DataBuffer data) {
    check(x, y);
    int type = getTransferType();
    Object out = obj != null ? obj : elementsFor(type, numBands);
    int base = y * scanlineStride + x * pixelStride;
    for (int b = 0; b < numBands; b++) {
      putElement(type, out, b, data, bankIndices[b], base + bandOffsets[b]);
    }
    return out;
  }

  public void setDataElements(int x, int y, Object obj, DataBuffer data) {
    check(x, y);
    int type = getTransferType();
    int base = y * scanlineStride + x * pixelStride;
    for (int b = 0; b < numBands; b++) {
      takeElement(type, obj, b, data, bankIndices[b], base + bandOffsets[b]);
    }
  }

  public int getSample(int x, int y, int b, DataBuffer data) {
    check(x, y);
    return data.getElem(bankIndices[b], y * scanlineStride + x * pixelStride + bandOffsets[b]);
  }

  public float getSampleFloat(int x, int y, int b, DataBuffer data) {
    check(x, y);
    return data.getElemFloat(bankIndices[b], y * scanlineStride + x * pixelStride + bandOffsets[b]);
  }

  public double getSampleDouble(int x, int y, int b, DataBuffer data) {
    check(x, y);
    return data.getElemDouble(bankIndices[b], y * scanlineStride + x * pixelStride + bandOffsets[b]);
  }

  public void setSample(int x, int y, int b, int s, DataBuffer data) {
    check(x, y);
    data.setElem(bankIndices[b], y * scanlineStride + x * pixelStride + bandOffsets[b], s);
  }

  public void setSample(int x, int y, int b, float s, DataBuffer data) {
    check(x, y);
    data.setElemFloat(bankIndices[b], y * scanlineStride + x * pixelStride + bandOffsets[b], s);
  }

  public void setSample(int x, int y, int b, double s, DataBuffer data) {
    check(x, y);
    data.setElemDouble(bankIndices[b], y * scanlineStride + x * pixelStride + bandOffsets[b], s);
  }

  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    ComponentSampleModel that = null;
    if (!(((that = o instanceof ComponentSampleModel ? (ComponentSampleModel) o : null) != null))) {
      return false;
    }
    return width == that.width && height == that.height && numBands == that.numBands && dataType == that.dataType
        && Arrays.equals(bandOffsets, that.bandOffsets) && Arrays.equals(bankIndices, that.bankIndices)
        && numBands == that.numBands && numBanks == that.numBanks && scanlineStride == that.scanlineStride
        && pixelStride == that.pixelStride;
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
    for (int o : bandOffsets) {
      h ^= o;
      h <<= 8;
    }
    for (int o : bankIndices) {
      h ^= o;
      h <<= 8;
    }
    h ^= numBands;
    h <<= 8;
    h ^= numBanks;
    h <<= 8;
    h ^= scanlineStride;
    h <<= 8;
    h ^= pixelStride;
    return h;
  }
}
