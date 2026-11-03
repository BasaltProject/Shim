/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.imageio;

import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DirectColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.RenderedImage;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;

public class ImageTypeSpecifier {
  protected ColorModel colorModel;
  protected SampleModel sampleModel;

  public ImageTypeSpecifier(ColorModel colorModel, SampleModel sampleModel) {
    if (colorModel == null) {
      throw new IllegalArgumentException("colorModel == null!");
    }
    if (sampleModel == null) {
      throw new IllegalArgumentException("sampleModel == null!");
    }
    if (!colorModel.isCompatibleSampleModel(sampleModel)) {
      throw new IllegalArgumentException("sampleModel is incompatible with colorModel!");
    }
    this.colorModel = colorModel;
    this.sampleModel = sampleModel;
  }

  public ImageTypeSpecifier(RenderedImage image) {
    if (image == null) {
      throw new IllegalArgumentException("image == null!");
    }
    colorModel = image.getColorModel();
    sampleModel = image.getSampleModel();
  }

  public static ImageTypeSpecifier createPacked(ColorSpace colorSpace, int redMask, int greenMask, int blueMask,
      int alphaMask, int transferType, boolean isAlphaPremultiplied) {
    if (colorSpace == null) {
      throw new IllegalArgumentException("colorSpace == null!");
    }
    if (colorSpace.getType() != ColorSpace.TYPE_RGB) {
      throw new IllegalArgumentException("colorSpace is not of type TYPE_RGB!");
    }
    if (transferType != DataBuffer.TYPE_BYTE && transferType != DataBuffer.TYPE_USHORT
        && transferType != DataBuffer.TYPE_INT) {
      throw new IllegalArgumentException("Bad value for transferType!");
    }
    if (redMask == 0 && greenMask == 0 && blueMask == 0 && alphaMask == 0) {
      throw new IllegalArgumentException("No mask has at least 1 bit set!");
    }
    int bits = 32;
    ColorModel cm = new DirectColorModel(colorSpace, bits, redMask, greenMask, blueMask, alphaMask,
        isAlphaPremultiplied, transferType);
    return new ImageTypeSpecifier(cm, cm.createCompatibleSampleModel(1, 1));
  }

  public static ImageTypeSpecifier createInterleaved(ColorSpace colorSpace, int[] bandOffsets, int dataType,
      boolean hasAlpha, boolean isAlphaPremultiplied) {
    if (colorSpace == null) {
      throw new IllegalArgumentException("colorSpace == null!");
    }
    if (bandOffsets == null) {
      throw new IllegalArgumentException("bandOffsets == null!");
    }
    int numColorPlusAlpha = colorSpace.getNumComponents() + (hasAlpha ? 1 : 0);
    if (bandOffsets.length != numColorPlusAlpha) {
      throw new IllegalArgumentException("bandOffsets.length is wrong!");
    }
    ColorModel cm = new ComponentColorModel(colorSpace, hasAlpha, isAlphaPremultiplied,
        hasAlpha ? Transparency.TRANSLUCENT : Transparency.OPAQUE, dataType);
    SampleModel sm = new java.awt.image.PixelInterleavedSampleModel(dataType, 1, 1, bandOffsets.length,
        bandOffsets.length, bandOffsets);
    return new ImageTypeSpecifier(cm, sm);
  }

  public static ImageTypeSpecifier createBanded(ColorSpace colorSpace, int[] bankIndices, int[] bandOffsets,
      int dataType, boolean hasAlpha, boolean isAlphaPremultiplied) {
    if (colorSpace == null) {
      throw new IllegalArgumentException("colorSpace == null!");
    }
    ColorModel cm = new ComponentColorModel(colorSpace, hasAlpha, isAlphaPremultiplied,
        hasAlpha ? Transparency.TRANSLUCENT : Transparency.OPAQUE, dataType);
    SampleModel sm = new java.awt.image.BandedSampleModel(dataType, 1, 1, 1, bankIndices, bandOffsets);
    return new ImageTypeSpecifier(cm, sm);
  }

  public static ImageTypeSpecifier createGrayscale(int bits, int dataType, boolean isSigned) {
    return createGrayscale(bits, dataType, isSigned, false);
  }

  public static ImageTypeSpecifier createGrayscale(int bits, int dataType, boolean isSigned,
      boolean isAlphaPremultiplied) {
    if (bits != 1 && bits != 2 && bits != 4 && bits != 8 && bits != 16) {
      throw new IllegalArgumentException("Bad value for bits!");
    }
    if (dataType != DataBuffer.TYPE_BYTE && dataType != DataBuffer.TYPE_SHORT && dataType != DataBuffer.TYPE_USHORT) {
      throw new IllegalArgumentException("Bad value for dataType!");
    }
    if (bits > 8 && dataType == DataBuffer.TYPE_BYTE) {
      throw new IllegalArgumentException("Too many bits for dataType!");
    }
    ColorSpace gray = ColorSpace.getInstance(ColorSpace.CS_GRAY);
    if ((bits == 8 && dataType == DataBuffer.TYPE_BYTE) || (bits == 16 && (dataType == DataBuffer.TYPE_SHORT
        || dataType == DataBuffer.TYPE_USHORT))) {
      ColorModel cm = new ComponentColorModel(gray, new int[] {bits}, false, false, Transparency.OPAQUE, dataType);
      return new ImageTypeSpecifier(cm, cm.createCompatibleSampleModel(1, 1));
    }
    int numEntries = 1 << bits;
    byte[] arr = new byte[numEntries];
    for (int i = 0; i < numEntries; i++) {
      arr[i] = (byte) (i * 255 / (numEntries - 1));
    }
    ColorModel cm = new IndexColorModel(bits, numEntries, arr, arr, arr);
    return new ImageTypeSpecifier(cm, cm.createCompatibleSampleModel(1, 1));
  }

  public static ImageTypeSpecifier createIndexed(byte[] redLUT, byte[] greenLUT, byte[] blueLUT, byte[] alphaLUT,
      int bits, int dataType) {
    if (redLUT == null || greenLUT == null || blueLUT == null) {
      throw new IllegalArgumentException("LUT is null!");
    }
    if (bits != 1 && bits != 2 && bits != 4 && bits != 8 && bits != 16) {
      throw new IllegalArgumentException("Bad value for bits!");
    }
    int size = 1 << bits;
    IndexColorModel cm;
    if (alphaLUT == null) {
      cm = new IndexColorModel(bits, size, redLUT, greenLUT, blueLUT);
    } else {
      cm = new IndexColorModel(bits, size, redLUT, greenLUT, blueLUT, alphaLUT);
    }
    SampleModel sm = cm.createCompatibleSampleModel(1, 1);
    return new ImageTypeSpecifier(cm, sm);
  }

  public static ImageTypeSpecifier createFromBufferedImageType(int bufferedImageType) {
    if (bufferedImageType >= BufferedImage.TYPE_INT_RGB && bufferedImageType <= BufferedImage.TYPE_BYTE_INDEXED) {
      BufferedImage bi = new BufferedImage(1, 1, bufferedImageType);
      return new ImageTypeSpecifier(bi.getColorModel(), bi.getSampleModel());
    } else if (bufferedImageType == BufferedImage.TYPE_CUSTOM) {
      throw new IllegalArgumentException("Cannot create from TYPE_CUSTOM!");
    }
    throw new IllegalArgumentException("Invalid BufferedImage type!");
  }

  public static ImageTypeSpecifier createFromRenderedImage(RenderedImage image) {
    if (image == null) {
      throw new IllegalArgumentException("image == null!");
    }
    if (image instanceof BufferedImage) {
      int bufferedImageType = ((BufferedImage) image).getType();
      if (bufferedImageType != BufferedImage.TYPE_CUSTOM) {
        return createFromBufferedImageType(bufferedImageType);
      }
    }
    return new ImageTypeSpecifier(image);
  }

  public int getBufferedImageType() {
    BufferedImage bi = createBufferedImage(1, 1);
    return bi.getType();
  }

  public int getNumComponents() { return colorModel.getNumComponents(); }

  public int getNumBands() { return sampleModel.getNumBands(); }

  public int getBitsPerBand(int band) {
    if (band < 0 | band >= getNumBands()) {
      throw new IllegalArgumentException("band out of range!");
    }
    return sampleModel.getSampleSize(band);
  }

  public SampleModel getSampleModel() { return sampleModel; }

  public SampleModel getSampleModel(int width, int height) {
    if ((long) width * height > Integer.MAX_VALUE) {
      throw new IllegalArgumentException("width*height > Integer.MAX_VALUE!");
    }
    return sampleModel.createCompatibleSampleModel(width, height);
  }

  public ColorModel getColorModel() { return colorModel; }

  public BufferedImage createBufferedImage(int width, int height) {
    try {
      SampleModel sampleModel = getSampleModel(width, height);
      WritableRaster raster = java.awt.image.Raster.createWritableRaster(sampleModel, new java.awt.Point(0, 0));
      return new BufferedImage(colorModel, raster, colorModel.isAlphaPremultiplied(), null);
    } catch (NegativeArraySizeException e) {
      throw new IllegalArgumentException("Array size > Integer.MAX_VALUE!");
    }
  }

  public boolean equals(Object o) {
    if ((o == null) || !(o instanceof ImageTypeSpecifier)) {
      return false;
    }
    ImageTypeSpecifier that = (ImageTypeSpecifier) o;
    return (colorModel.equals(that.colorModel)) && (sampleModel.equals(that.sampleModel));
  }

  public int hashCode() {
    return (9 * colorModel.hashCode()) + (14 * sampleModel.hashCode());
  }
}
