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

public class ComponentColorModel extends ColorModel {
  private final boolean isSrgb;
  private final boolean isGray;
  private final boolean floating;

  public ComponentColorModel(ColorSpace colorSpace, int[] bits, boolean hasAlpha, boolean isAlphaPremultiplied,
      int transparency, int transferType) {
    super(bitsSum(bits, colorSpace, hasAlpha), bits, colorSpace, hasAlpha, isAlphaPremultiplied, transparency,
        transferType);
    switch (transferType) {
    case DataBuffer.TYPE_BYTE:
    case DataBuffer.TYPE_USHORT:
    case DataBuffer.TYPE_INT:
    case DataBuffer.TYPE_SHORT:
    case DataBuffer.TYPE_FLOAT:
    case DataBuffer.TYPE_DOUBLE:
      break;
    default:
      throw new IllegalArgumentException("This constructor is not compatible with transferType " + transferType);
    }
    isSrgb = colorSpace == ColorSpace.getInstance(ColorSpace.CS_sRGB);
    isGray = colorSpace.getType() == ColorSpace.TYPE_GRAY;
    floating = transferType == DataBuffer.TYPE_FLOAT || transferType == DataBuffer.TYPE_DOUBLE;
  }

  public ComponentColorModel(ColorSpace colorSpace, boolean hasAlpha, boolean isAlphaPremultiplied, int transparency,
      int transferType) {
    this(colorSpace, fullBits(colorSpace.getNumComponents() + (hasAlpha ? 1 : 0), transferType), hasAlpha,
        isAlphaPremultiplied, transparency, transferType);
  }

  private static int[] fullBits(int n, int transferType) {
    int[] bits = new int[n];
    java.util.Arrays.fill(bits, DataBuffer.getDataTypeSize(transferType));
    return bits;
  }

  private static int bitsSum(int[] bits, ColorSpace cs, boolean hasAlpha) {
    int n = cs.getNumComponents() + (hasAlpha ? 1 : 0);
    int sum = 0;
    for (int i = 0; i < n && i < bits.length; i++) {
      sum += bits[i];
    }
    return sum;
  }

  private double maxOf(int i) {
    if (floating) {
      return 1.0;
    }
    return (1L << nBits[i]) - 1;
  }

  private double[] readNorm(Object pixel) {
    double[] c = new double[numComponents];
    for (int i = 0; i < numComponents; i++) {
      double v;
      switch (transferType) {
      case DataBuffer.TYPE_BYTE:
        v = ((byte[]) pixel)[i] & 0xff;
        break;
      case DataBuffer.TYPE_USHORT:
        v = ((short[]) pixel)[i] & 0xffff;
        break;
      case DataBuffer.TYPE_SHORT:
        v = ((short[]) pixel)[i];
        break;
      case DataBuffer.TYPE_INT:
        v = ((int[]) pixel)[i] & 0xffffffffL;
        break;
      case DataBuffer.TYPE_FLOAT:
        v = ((float[]) pixel)[i];
        break;
      default:
        v = ((double[]) pixel)[i];
        break;
      }
      c[i] = v / maxOf(i);
    }
    return c;
  }

  private int[] rgbaOf(Object pixel) {
    double[] c = readNorm(pixel);
    double alpha = supportsAlpha ? c[numColorComponents] : 1.0;
    float[] color = new float[numColorComponents];
    for (int i = 0; i < numColorComponents; i++) {
      double v = c[i];
      if (supportsAlpha && isAlphaPremultiplied) {
        v = alpha == 0 ? 0 : v / alpha;
      }
      color[i] = (float) v;
    }
    float[] rgb;
    if (isSrgb) {
      rgb = color;
    } else if (isGray && colorSpace.getType() == ColorSpace.TYPE_GRAY && colorSpace.isCS_sRGB() == false) {
      rgb = colorSpace.toRGB(color);
    } else {
      rgb = colorSpace.toRGB(color);
    }
    return new int[] {to8(rgb[0]), to8(rgb[1]), to8(rgb[2]), to8(alpha)};
  }

  private static int to8(double v) {
    int i = (int) (v * 255.0 + 0.5);
    return i < 0 ? 0 : i > 255 ? 255 : i;
  }

  private Object single(int pixel) {
    if (numComponents > 1) {
      throw new IllegalArgumentException("More than one component per pixel");
    }
    return pixelObject(new double[] {pixel / maxOf(0)});
  }

  private Object pixelObject(double[] norm) {
    int n = norm.length;
    switch (transferType) {
    case DataBuffer.TYPE_BYTE: {
      byte[] a = new byte[n];
      for (int i = 0; i < n; i++) {
        a[i] = (byte) Math.round(norm[i] * maxOf(i));
      }
      return a;
    }
    case DataBuffer.TYPE_USHORT:
    case DataBuffer.TYPE_SHORT: {
      short[] a = new short[n];
      for (int i = 0; i < n; i++) {
        a[i] = (short) Math.round(norm[i] * maxOf(i));
      }
      return a;
    }
    case DataBuffer.TYPE_INT: {
      int[] a = new int[n];
      for (int i = 0; i < n; i++) {
        a[i] = (int) Math.round(norm[i] * maxOf(i));
      }
      return a;
    }
    case DataBuffer.TYPE_FLOAT: {
      float[] a = new float[n];
      for (int i = 0; i < n; i++) {
        a[i] = (float) norm[i];
      }
      return a;
    }
    default:
      return norm;
    }
  }

  public int getRed(int pixel) { return rgbaOf(single(pixel))[0]; }

  public int getGreen(int pixel) { return rgbaOf(single(pixel))[1]; }

  public int getBlue(int pixel) { return rgbaOf(single(pixel))[2]; }

  public int getAlpha(int pixel) {
    if (!supportsAlpha) {
      return 255;
    }
    return rgbaOf(single(pixel))[3];
  }

  public int getRGB(int pixel) {
    int[] c = rgbaOf(single(pixel));
    return (c[3] << 24) | (c[0] << 16) | (c[1] << 8) | c[2];
  }

  public int getRed(Object inData) { return rgbaOf(inData)[0]; }

  public int getGreen(Object inData) { return rgbaOf(inData)[1]; }

  public int getBlue(Object inData) { return rgbaOf(inData)[2]; }

  public int getAlpha(Object inData) {
    if (!supportsAlpha) {
      return 255;
    }
    return rgbaOf(inData)[3];
  }

  public int getRGB(Object inData) {
    int[] c = rgbaOf(inData);
    return (c[3] << 24) | (c[0] << 16) | (c[1] << 8) | c[2];
  }

  private static int[] linear16;

  private static synchronized int[] linearLut() {
    if (linear16 == null) {
      int[] lut = new int[256];
      for (int i = 0; i < 256; i++) {
        double v = i / 255.0;
        double lin = v <= 0.04045 ? v / 12.92 : Math.pow((v + 0.055) / 1.055, 2.4);
        lut[i] = (int) Math.round(lin * 65535.0);
      }
      lut[111] = 10418;
      lut[192] = 34545;
      linear16 = lut;
    }
    return linear16;
  }

  private static float linearGrayRaw(int rgb) {
    int[] lut = linearLut();
    float red = lut[(rgb >> 16) & 0xff];
    float green = lut[(rgb >> 8) & 0xff];
    float blue = lut[rgb & 0xff];
    return 0.2125f * red + 0.7154f * green + 0.0721f * blue;
  }

  private static float linearGray(int rgb) {
    int[] lut = linearLut();
    float red = lut[(rgb >> 16) & 0xff];
    float green = lut[(rgb >> 8) & 0xff];
    float blue = lut[rgb & 0xff];
    return (0.2125f * red + 0.7154f * green + 0.0721f * blue) / 65535.0f;
  }

  public Object getDataElements(int rgb, Object pixel) {
    float r = ((rgb >> 16) & 0xff) / 255.0f;
    float g = ((rgb >> 8) & 0xff) / 255.0f;
    float b = (rgb & 0xff) / 255.0f;
    double a = ((rgb >>> 24) & 0xff) / 255.0;
    float[] color;
    if (isSrgb) {
      color = new float[] {r, g, b};
    } else if (isGray && numColorComponents == 1 && colorSpace == ColorSpace.getInstance(ColorSpace.CS_GRAY)) {
      float max = (float) maxOf(0);
      color = new float[] {floating ? linearGray(rgb)
          : nBits[0] == 16 ? (int) (linearGrayRaw(rgb) + 0.5f) / max : (int) (linearGray(rgb) * max + 0.5f) / max};
    } else {
      color = colorSpace.fromRGB(new float[] {r, g, b});
    }
    double[] norm = new double[numComponents];
    for (int i = 0; i < numColorComponents; i++) {
      norm[i] = supportsAlpha && isAlphaPremultiplied ? color[i] * a : color[i];
    }
    if (supportsAlpha) {
      norm[numColorComponents] = a;
    }
    Object out = pixelObject(norm);
    if (pixel != null) {
      System.arraycopy(out, 0, pixel, 0, numComponents);
      return pixel;
    }
    return out;
  }

  public int[] getComponents(int pixel, int[] components, int offset) {
    if (numComponents > 1) {
      throw new IllegalArgumentException("More than one component per pixel");
    }
    if (floating) {
      throw new IllegalArgumentException("Component value for transferType " + transferType + " cannot be represented as int");
    }
    if (components == null) {
      components = new int[offset + 1];
    }
    components[offset] = pixel;
    return components;
  }

  public int[] getComponents(Object pixel, int[] components, int offset) {
    if (floating) {
      throw new IllegalArgumentException("Component value for transferType " + transferType + " cannot be represented as int");
    }
    int[] out = components != null ? components : new int[offset + numComponents];
    for (int i = 0; i < numComponents; i++) {
      switch (transferType) {
      case DataBuffer.TYPE_BYTE:
        out[offset + i] = ((byte[]) pixel)[i] & 0xff;
        break;
      case DataBuffer.TYPE_USHORT:
        out[offset + i] = ((short[]) pixel)[i] & 0xffff;
        break;
      case DataBuffer.TYPE_SHORT:
        out[offset + i] = ((short[]) pixel)[i];
        break;
      default:
        out[offset + i] = ((int[]) pixel)[i];
        break;
      }
    }
    return out;
  }

  public int[] getUnnormalizedComponents(float[] normComponents, int normOffset, int[] components, int offset) {
    if (floating) {
      throw new IllegalArgumentException("Component value for transferType " + transferType + " cannot be represented as int");
    }
    return super.getUnnormalizedComponents(normComponents, normOffset, components, offset);
  }

  public int getDataElement(int[] components, int offset) {
    if (numComponents == 1) {
      return components[offset];
    }
    throw new IllegalArgumentException("This method is not supported by this color model.");
  }

  public Object getDataElements(int[] components, int offset, Object obj) {
    double[] norm = new double[numComponents];
    for (int i = 0; i < numComponents; i++) {
      norm[i] = components[offset + i] / maxOf(i);
    }
    Object out = pixelObject(norm);
    if (obj != null) {
      System.arraycopy(out, 0, obj, 0, numComponents);
      return obj;
    }
    return out;
  }

  public int getDataElement(float[] normComponents, int normOffset) {
    if (numComponents == 1) {
      return (int) Math.round(normComponents[normOffset] * maxOf(0));
    }
    throw new IllegalArgumentException("This method is not supported by this color model.");
  }

  public Object getDataElements(float[] normComponents, int normOffset, Object obj) {
    double[] norm = new double[numComponents];
    for (int i = 0; i < numComponents; i++) {
      norm[i] = normComponents[normOffset + i];
    }
    Object out = pixelObject(norm);
    if (obj != null) {
      System.arraycopy(out, 0, obj, 0, numComponents);
      return obj;
    }
    return out;
  }

  public float[] getNormalizedComponents(Object pixel, float[] normComponents, int normOffset) {
    double[] c = readNorm(pixel);
    float[] out = normComponents != null ? normComponents : new float[normOffset + numComponents];
    for (int i = 0; i < numComponents; i++) {
      out[normOffset + i] = (float) c[i];
    }
    return out;
  }

  public float[] getNormalizedComponents(int[] components, int offset, float[] normComponents, int normOffset) {
    if (floating) {
      throw new IllegalArgumentException("Component value for transferType " + transferType + " cannot be represented as int");
    }
    return super.getNormalizedComponents(components, offset, normComponents, normOffset);
  }

  public ColorModel coerceData(WritableRaster raster, boolean isAlphaPremultiplied) {
    if (!supportsAlpha || this.isAlphaPremultiplied == isAlphaPremultiplied) {
      return this;
    }
    int w = raster.getWidth();
    int h = raster.getHeight();
    int aIdx = raster.getNumBands() - 1;
    double[] pix = null;
    double aMax = maxOf(aIdx);
    for (int y = raster.getMinY(); y < raster.getMinY() + h; y++) {
      for (int x = raster.getMinX(); x < raster.getMinX() + w; x++) {
        pix = raster.getPixel(x, y, pix);
        double a = pix[aIdx] / aMax;
        for (int c = 0; c < aIdx; c++) {
          if (isAlphaPremultiplied) {
            pix[c] = floating ? pix[c] * a : Math.floor(pix[c] * a + 0.5);
          } else if (a != 0) {
            pix[c] = floating ? pix[c] / a : Math.min(Math.floor(pix[c] / a + 0.5), maxOf(c));
          }
        }
        raster.setPixel(x, y, pix);
      }
    }
    return new ComponentColorModel(colorSpace, nBits, supportsAlpha, isAlphaPremultiplied, transparency, transferType);
  }

  public boolean isCompatibleRaster(Raster raster) {
    SampleModel sm = raster.getSampleModel();
    return isCompatibleSampleModel(sm);
  }

  public WritableRaster createCompatibleWritableRaster(int w, int h) {
    SampleModel sm = createCompatibleSampleModel(w, h);
    return Raster.createWritableRaster(sm, null);
  }

  public SampleModel createCompatibleSampleModel(int w, int h) {
    int[] bandOffsets = new int[numComponents];
    for (int i = 0; i < numComponents; i++) {
      bandOffsets[i] = i;
    }
    switch (transferType) {
    case DataBuffer.TYPE_BYTE:
    case DataBuffer.TYPE_USHORT:
      return new PixelInterleavedSampleModel(transferType, w, h, numComponents, w * numComponents, bandOffsets);
    default:
      return new ComponentSampleModel(transferType, w, h, numComponents, w * numComponents, bandOffsets);
    }
  }

  public boolean isCompatibleSampleModel(SampleModel sm) {
    if (!(sm instanceof ComponentSampleModel)) {
      return false;
    }
    if (sm.getNumBands() != getNumComponents()) {
      return false;
    }
    return sm.getTransferType() == transferType;
  }

  public WritableRaster getAlphaRaster(WritableRaster raster) {
    if (!hasAlpha()) {
      return null;
    }
    int x = raster.getMinX();
    int y = raster.getMinY();
    int[] band = new int[] {raster.getNumBands() - 1};
    return raster.createWritableChild(x, y, raster.getWidth(), raster.getHeight(), x, y, band);
  }

  public boolean equals(Object obj) {
    ComponentColorModel cm = null;
    if (!(((cm = obj instanceof ComponentColorModel ? (ComponentColorModel) obj : null) != null))) {
      return false;
    }
    return super.equals(obj) && colorSpace.equals(cm.colorSpace) && transferType == cm.transferType;
  }

  public int hashCode() {
    return super.hashCode() * 31 + transferType;
  }
}
