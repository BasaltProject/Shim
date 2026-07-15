/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.awt.image;

import java.awt.Transparency;
import java.awt.color.ColorSpace;

public abstract class PackedColorModel extends ColorModel {
  int[] maskArray;
  int[] maskOffsets;
  float[] scaleFactors;

  public PackedColorModel(ColorSpace space, int bits, int[] colorMaskArray, int alphaMask,
      boolean isAlphaPremultiplied, int trans, int transferType) {
    super(bits, createBitsArray(colorMaskArray, alphaMask), space, (alphaMask == 0 ? false : true),
        isAlphaPremultiplied, trans, transferType);
    if (bits < 1 || bits > 32) {
      throw new IllegalArgumentException("Number of bits must be between 1 and 32.");
    }
    maskArray = new int[numComponents];
    maskOffsets = new int[numComponents];
    scaleFactors = new float[numComponents];
    for (int i = 0; i < numColorComponents; i++) {
      DecomposeMask(colorMaskArray[i], i, space.getName(i));
    }
    if (alphaMask != 0) {
      DecomposeMask(alphaMask, numColorComponents, "alpha");
      if (nBits[numComponents - 1] == 1) {
        transparency = Transparency.BITMASK;
      }
    }
  }

  public PackedColorModel(ColorSpace space, int bits, int rmask, int gmask, int bmask, int amask,
      boolean isAlphaPremultiplied, int trans, int transferType) {
    super(bits, createBitsArray(rmask, gmask, bmask, amask), space, (amask == 0 ? false : true),
        isAlphaPremultiplied, trans, transferType);
    if (space.getType() != ColorSpace.TYPE_RGB) {
      throw new IllegalArgumentException("ColorSpace must be TYPE_RGB.");
    }
    maskArray = new int[numComponents];
    maskOffsets = new int[numComponents];
    scaleFactors = new float[numComponents];
    DecomposeMask(rmask, 0, "red");
    DecomposeMask(gmask, 1, "green");
    DecomposeMask(bmask, 2, "blue");
    if (amask != 0) {
      DecomposeMask(amask, 3, "alpha");
      if (nBits[3] == 1) {
        transparency = Transparency.BITMASK;
      }
    }
  }

  public final int getMask(int index) {
    return maskArray[index];
  }

  public final int[] getMasks() {
    return maskArray.clone();
  }

  private void DecomposeMask(int mask, int idx, String componentName) {
    int off = 0;
    int count = nBits[idx];
    maskArray[idx] = mask;
    if (mask != 0) {
      off = Integer.numberOfTrailingZeros(mask);
      int shifted = mask >>> off;
      if (shifted + 1 != 0 && (shifted & (shifted + 1)) != 0) {
        throw new IllegalArgumentException(componentName + " mask " + Integer.toHexString(mask) + " must be contiguous");
      }
    }
    maskOffsets[idx] = off;
    if (count == 0) {
      scaleFactors[idx] = 256.0f;
    } else {
      scaleFactors[idx] = 255.0f / ((1 << count) - 1);
    }
  }

  private static int[] createBitsArray(int[] colorMaskArray, int alphaMask) {
    int numColors = colorMaskArray.length;
    int numAlpha = (alphaMask == 0) ? 0 : 1;
    int[] arr = new int[numColors + numAlpha];
    for (int i = 0; i < numColors; i++) {
      arr[i] = Integer.bitCount(colorMaskArray[i]);
      if (arr[i] > 32) {
        throw new IllegalArgumentException("Mask " + Integer.toHexString(colorMaskArray[i]) + " is too wide");
      }
    }
    if (numAlpha != 0) {
      arr[numColors] = Integer.bitCount(alphaMask);
    }
    return arr;
  }

  private static int[] createBitsArray(int rmask, int gmask, int bmask, int amask) {
    int[] arr = new int[3 + (amask == 0 ? 0 : 1)];
    arr[0] = Integer.bitCount(rmask);
    arr[1] = Integer.bitCount(gmask);
    arr[2] = Integer.bitCount(bmask);
    if (amask != 0) {
      arr[3] = Integer.bitCount(amask);
    }
    return arr;
  }

  public SampleModel createCompatibleSampleModel(int w, int h) {
    return new SinglePixelPackedSampleModel(transferType, w, h, maskArray);
  }

  public boolean isCompatibleSampleModel(SampleModel sm) {
    SinglePixelPackedSampleModel sppsm = null;
    if (!(((sppsm = sm instanceof SinglePixelPackedSampleModel ? (SinglePixelPackedSampleModel) sm : null) != null))) {
      return false;
    }
    if (sppsm.getNumBands() != numComponents) {
      return false;
    }
    int[] masks = sppsm.getBitMasks();
    for (int i = 0; i < numComponents; i++) {
      if (masks[i] != maskArray[i]) {
        return false;
      }
    }
    return sm.getTransferType() == transferType;
  }

  public WritableRaster getAlphaRaster(WritableRaster raster) {
    if (!hasAlpha()) {
      return null;
    }
    int x = raster.getMinX();
    int y = raster.getMinY();
    int[] band = new int[1];
    band[0] = raster.getNumBands() - 1;
    return raster.createWritableChild(x, y, raster.getWidth(), raster.getHeight(), x, y, band);
  }

  public boolean equals(Object obj) {
    PackedColorModel cm = null;
    if (!(((cm = obj instanceof PackedColorModel ? (PackedColorModel) obj : null) != null))) {
      return false;
    }
    if (!super.equals(obj)) {
      return false;
    }
    int numC = cm.getNumComponents();
    if (numC != numComponents) {
      return false;
    }
    for (int i = 0; i < numC; i++) {
      if (maskArray[i] != cm.getMask(i)) {
        return false;
      }
    }
    return true;
  }

  public int hashCode() {
    int h = super.hashCode();
    for (int m : maskArray) {
      h = h * 31 + m;
    }
    return h;
  }
}
