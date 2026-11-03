/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.awt.image;

public class PixelInterleavedSampleModel extends ComponentSampleModel {
  public PixelInterleavedSampleModel(int dataType, int w, int h, int pixelStride, int scanlineStride,
      int[] bandOffsets) {
    super(dataType, w, h, pixelStride, scanlineStride, bandOffsets);
    int minBandOff = this.bandOffsets[0];
    int maxBandOff = this.bandOffsets[0];
    for (int i = 1; i < this.bandOffsets.length; i++) {
      minBandOff = Math.min(minBandOff, this.bandOffsets[i]);
      maxBandOff = Math.max(maxBandOff, this.bandOffsets[i]);
    }
    maxBandOff -= minBandOff;
    if (maxBandOff > scanlineStride) {
      throw new IllegalArgumentException("Offset between bands is greater than the scanline stride");
    }
    if (pixelStride * w > scanlineStride) {
      throw new IllegalArgumentException("Pixel stride times width is greater than the scanline stride");
    }
    if (maxBandOff > pixelStride) {
      throw new IllegalArgumentException("Pixel stride is less than the offset between bands");
    }
  }

  @Override
  public SampleModel createCompatibleSampleModel(int w, int h) {
    int[] off = new int[bandOffsets.length];
    int min = bandOffsets[0];
    for (int o : bandOffsets) {
      min = Math.min(min, o);
    }
    for (int i = 0; i < off.length; i++) {
      off[i] = bandOffsets[i] - min;
    }
    return new PixelInterleavedSampleModel(dataType, w, h, pixelStride, pixelStride * w, off);
  }

  @Override
  public SampleModel createSubsetSampleModel(int[] bands) {
    int[] off = new int[bands.length];
    for (int i = 0; i < bands.length; i++) {
      off[i] = bandOffsets[bands[i]];
    }
    return new PixelInterleavedSampleModel(dataType, width, height, pixelStride, scanlineStride, off);
  }

  @Override
  public int hashCode() {
    return super.hashCode() ^ 0x1;
  }
}
