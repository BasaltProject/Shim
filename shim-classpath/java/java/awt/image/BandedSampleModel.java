/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.awt.image;

public final class BandedSampleModel extends ComponentSampleModel {
  public BandedSampleModel(int dataType, int w, int h, int numBands) {
    super(dataType, w, h, 1, w, identity(numBands), new int[numBands]);
  }

  public BandedSampleModel(int dataType, int w, int h, int scanlineStride, int[] bankIndices, int[] bandOffsets) {
    super(dataType, w, h, 1, scanlineStride, bankIndices, bandOffsets);
  }

  private static int[] identity(int n) {
    int[] out = new int[n];
    for (int i = 0; i < n; i++) {
      out[i] = i;
    }
    return out;
  }

  @Override
  public SampleModel createCompatibleSampleModel(int w, int h) {
    int[] off = new int[bandOffsets.length];
    return new BandedSampleModel(dataType, w, h, w, bankIndices, off);
  }

  @Override
  public SampleModel createSubsetSampleModel(int[] bands) {
    if (bands.length > bankIndices.length) {
      throw new RasterFormatException("There are only " + bankIndices.length + " bands");
    }
    int[] bank = new int[bands.length];
    int[] off = new int[bands.length];
    for (int i = 0; i < bands.length; i++) {
      bank[i] = bankIndices[bands[i]];
      off[i] = bandOffsets[bands[i]];
    }
    return new BandedSampleModel(dataType, width, height, scanlineStride, bank, off);
  }

  @Override
  public DataBuffer createDataBuffer() {
    int size = scanlineStride * height;
    for (int o : bandOffsets) {
      size = Math.max(size, o + scanlineStride * height);
    }
    return newBuffer(dataType, size, numBanks);
  }

  @Override
  public int hashCode() {
    return super.hashCode() ^ 0x2;
  }
}
