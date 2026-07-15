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

public class DirectColorModel extends PackedColorModel {
  private int red_mask;
  private int green_mask;
  private int blue_mask;
  private int alpha_mask;
  private int red_offset;
  private int green_offset;
  private int blue_offset;
  private int alpha_offset;
  private int red_scale;
  private int green_scale;
  private int blue_scale;
  private int alpha_scale;
  private boolean is_LinearRGB;

  public DirectColorModel(int bits, int rmask, int gmask, int bmask) {
    this(bits, rmask, gmask, bmask, 0);
  }

  public DirectColorModel(int bits, int rmask, int gmask, int bmask, int amask) {
    super(ColorSpace.getInstance(ColorSpace.CS_sRGB), bits, rmask, gmask, bmask, amask, false,
        amask == 0 ? Transparency.OPAQUE : Transparency.TRANSLUCENT, ColorModel.getDefaultTransferType(bits));
    setFields();
  }

  public DirectColorModel(ColorSpace space, int bits, int rmask, int gmask, int bmask, int amask,
      boolean isAlphaPremultiplied, int transferType) {
    super(space, bits, rmask, gmask, bmask, amask, isAlphaPremultiplied,
        amask == 0 ? Transparency.OPAQUE : Transparency.TRANSLUCENT, transferType);
    if (ColorModel.getDefaultTransferType(bits) != transferType && transferType != DataBuffer.TYPE_INT
        && !(transferType == DataBuffer.TYPE_USHORT && bits <= 16) && !(transferType == DataBuffer.TYPE_BYTE && bits <= 8)) {
      throw new IllegalArgumentException("transferType is not one of DataBuffer.TYPE_BYTE, DataBuffer.TYPE_USHORT, or DataBuffer.TYPE_INT.");
    }
    setFields();
  }

  public final int getRedMask() { return maskArray[0]; }

  public final int getGreenMask() { return maskArray[1]; }

  public final int getBlueMask() { return maskArray[2]; }

  public final int getAlphaMask() {
    if (supportsAlpha) {
      return maskArray[3];
    }
    return 0;
  }

  private void setFields() {
    red_mask = maskArray[0];
    red_offset = maskOffsets[0];
    green_mask = maskArray[1];
    green_offset = maskOffsets[1];
    blue_mask = maskArray[2];
    blue_offset = maskOffsets[2];
    if (nBits[0] < 8) {
      red_scale = (1 << nBits[0]) - 1;
    }
    if (nBits[1] < 8) {
      green_scale = (1 << nBits[1]) - 1;
    }
    if (nBits[2] < 8) {
      blue_scale = (1 << nBits[2]) - 1;
    }
    if (supportsAlpha) {
      alpha_mask = maskArray[3];
      alpha_offset = maskOffsets[3];
      if (nBits[3] < 8) {
        alpha_scale = (1 << nBits[3]) - 1;
      }
    }
    is_LinearRGB = colorSpace == ColorSpace.getInstance(ColorSpace.CS_LINEAR_RGB);
  }

  private int comp(int pixel, int mask, int offset, int bits) {
    int c = (pixel & mask) >>> offset;
    return c;
  }

  private int alphaRaw(int pixel) {
    if (!supportsAlpha) {
      return 255;
    }
    return scaleTo8((pixel & alpha_mask) >>> alpha_offset, nBits[3]);
  }

  private int color8(int pixel, int mask, int offset, int bits) {
    int c = scaleTo8((pixel & mask) >>> offset, bits);
    if (supportsAlpha && isAlphaPremultiplied) {
      int a = alphaRaw(pixel);
      c = a == 0 ? 0 : (int) (c * (255.0f / a) + 0.5f);
      if (c > 255) {
        c = 255;
      }
    }
    return c;
  }

  public final int getRed(int pixel) {
    return fix(color8(pixel, red_mask, red_offset, nBits[0]));
  }

  public final int getGreen(int pixel) {
    return fix(color8(pixel, green_mask, green_offset, nBits[1]));
  }

  public final int getBlue(int pixel) {
    return fix(color8(pixel, blue_mask, blue_offset, nBits[2]));
  }

  public final int getAlpha(int pixel) {
    return alphaRaw(pixel);
  }

  private int fix(int c) {
    if (is_LinearRGB) {
      double v = c / 255.0;
      v = v <= 0.0031308 ? v * 12.92 : 1.055 * Math.pow(v, 1 / 2.4) - 0.055;
      return (int) Math.round(v * 255);
    }
    return c;
  }

  public final int getRGB(int pixel) {
    return (getAlpha(pixel) << 24) | (getRed(pixel) << 16) | (getGreen(pixel) << 8) | (getBlue(pixel) << 0);
  }

  public int getRed(Object inData) { return getRed(pixelOf(inData)); }

  public int getGreen(Object inData) { return getGreen(pixelOf(inData)); }

  public int getBlue(Object inData) { return getBlue(pixelOf(inData)); }

  public int getAlpha(Object inData) { return getAlpha(pixelOf(inData)); }

  public int getRGB(Object inData) { return getRGB(pixelOf(inData)); }

  public Object getDataElements(int rgb, Object pixel) {
    int a = (rgb >>> 24) & 0xff;
    int r = (rgb >> 16) & 0xff;
    int g = (rgb >> 8) & 0xff;
    int b = rgb & 0xff;
    if (is_LinearRGB) {
      r = lin(r);
      g = lin(g);
      b = lin(b);
    }
    if (supportsAlpha && isAlphaPremultiplied) {
      float f = a / 255.0f;
      r = (int) (r * f + 0.5f);
      g = (int) (g * f + 0.5f);
      b = (int) (b * f + 0.5f);
    }
    int value = (scaleFrom8(r, nBits[0]) << red_offset) & red_mask;
    value |= (scaleFrom8(g, nBits[1]) << green_offset) & green_mask;
    value |= (scaleFrom8(b, nBits[2]) << blue_offset) & blue_mask;
    if (supportsAlpha) {
      value |= (scaleFrom8(a, nBits[3]) << alpha_offset) & alpha_mask;
    }
    return wrap(value, pixel);
  }

  private static int lin(int c) {
    double v = c / 255.0;
    v = v <= 0.04045 ? v / 12.92 : Math.pow((v + 0.055) / 1.055, 2.4);
    return (int) Math.round(v * 255);
  }

  private Object wrap(int value, Object pixel) {
    switch (transferType) {
    case DataBuffer.TYPE_BYTE: {
      byte[] bdata = pixel == null ? new byte[1] : (byte[]) pixel;
      bdata[0] = (byte) value;
      return bdata;
    }
    case DataBuffer.TYPE_USHORT: {
      short[] sdata = pixel == null ? new short[1] : (short[]) pixel;
      sdata[0] = (short) value;
      return sdata;
    }
    case DataBuffer.TYPE_INT: {
      int[] idata = pixel == null ? new int[1] : (int[]) pixel;
      idata[0] = value;
      return idata;
    }
    default:
      throw new UnsupportedOperationException("This method has not been implemented for transferType " + transferType);
    }
  }

  public final int[] getComponents(int pixel, int[] components, int offset) {
    if (components == null) {
      components = new int[offset + numComponents];
    }
    for (int i = 0; i < numComponents; i++) {
      components[offset + i] = (pixel & maskArray[i]) >>> maskOffsets[i];
    }
    return components;
  }

  public final int[] getComponents(Object pixel, int[] components, int offset) {
    return getComponents(pixelOf(pixel), components, offset);
  }

  public final WritableRaster createCompatibleWritableRaster(int w, int h) {
    if ((w <= 0) || (h <= 0)) {
      throw new IllegalArgumentException("Width (" + w + ") and height (" + h + ") cannot be <= 0");
    }
    int[] bandmasks;
    if (supportsAlpha) {
      bandmasks = new int[4];
      bandmasks[3] = alpha_mask;
    } else {
      bandmasks = new int[3];
    }
    bandmasks[0] = red_mask;
    bandmasks[1] = green_mask;
    bandmasks[2] = blue_mask;
    return Raster.createPackedRaster(transferType, w, h, bandmasks, null);
  }

  public int getDataElement(int[] components, int offset) {
    int pixel = 0;
    for (int i = 0; i < numComponents; i++) {
      pixel |= (components[offset + i] << maskOffsets[i]) & maskArray[i];
    }
    return pixel;
  }

  public Object getDataElements(int[] components, int offset, Object obj) {
    return wrap(getDataElement(components, offset), obj);
  }

  public final ColorModel coerceData(WritableRaster raster, boolean isAlphaPremultiplied) {
    if (!supportsAlpha || this.isAlphaPremultiplied == isAlphaPremultiplied) {
      return this;
    }
    int w = raster.getWidth();
    int h = raster.getHeight();
    int aIdx = raster.getNumBands() - 1;
    float normAlphaScale = (float) ((1 << nBits[aIdx]) - 1);
    int rminX = raster.getMinX();
    int rY = raster.getMinY();
    int[] pix = null;
    for (int y = 0; y < h; y++, rY++) {
      int rX = rminX;
      for (int x = 0; x < w; x++, rX++) {
        pix = raster.getPixel(rX, rY, pix);
        float normAlpha = pix[aIdx] / normAlphaScale;
        if (isAlphaPremultiplied) {
          for (int c = 0; c < aIdx; c++) {
            pix[c] = (int) (pix[c] * normAlpha + 0.5f);
          }
        } else if (normAlpha != 0.0f) {
          float inv = 1.0f / normAlpha;
          for (int c = 0; c < aIdx; c++) {
            pix[c] = Math.min((int) (pix[c] * inv + 0.5f), (1 << nBits[c]) - 1);
          }
        }
        raster.setPixel(rX, rY, pix);
      }
    }
    return new DirectColorModel(colorSpace, pixel_bits, maskArray[0], maskArray[1], maskArray[2], maskArray[3],
        isAlphaPremultiplied, transferType);
  }

  public boolean isCompatibleRaster(Raster raster) {
    SampleModel sm = raster.getSampleModel();
    SinglePixelPackedSampleModel sppsm = null;
    if (((sppsm = sm instanceof SinglePixelPackedSampleModel ? (SinglePixelPackedSampleModel) sm : null) != null)) {
      if (sppsm.getNumBands() != getNumComponents()) {
        return false;
      }
      int[] bitMasks = sppsm.getBitMasks();
      for (int i = 0; i < numComponents; i++) {
        if (bitMasks[i] != maskArray[i]) {
          return false;
        }
      }
      return raster.getTransferType() == transferType;
    }
    return false;
  }

  public String toString() {
    return "DirectColorModel: rmask=" + Integer.toHexString(red_mask) + " gmask=" + Integer.toHexString(green_mask)
        + " bmask=" + Integer.toHexString(blue_mask) + " amask=" + Integer.toHexString(alpha_mask);
  }
}
