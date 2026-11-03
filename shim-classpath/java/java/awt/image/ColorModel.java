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

public abstract class ColorModel implements Transparency {
  private long pData;
  protected int pixel_bits;
  int[] nBits;
  int transparency = Transparency.TRANSLUCENT;
  boolean supportsAlpha = true;
  boolean isAlphaPremultiplied = false;
  int numComponents = -1;
  int numColorComponents = -1;
  ColorSpace colorSpace = ColorSpace.getInstance(ColorSpace.CS_sRGB);
  int colorSpaceType = ColorSpace.TYPE_RGB;
  int maxBits;
  boolean is_sRGB = true;
  protected int transferType;

  private static ColorModel RGBdefault;

  public static ColorModel getRGBdefault() {
    if (RGBdefault == null) {
      RGBdefault = new DirectColorModel(32, 0x00ff0000, 0x0000ff00, 0x000000ff, 0xff000000);
    }
    return RGBdefault;
  }

  public ColorModel(int bits) {
    pixel_bits = bits;
    if (bits < 1) {
      throw new IllegalArgumentException("Number of bits must be > 0");
    }
    numComponents = 4;
    numColorComponents = 3;
    maxBits = bits;
    transferType = getDefaultTransferType(bits);
  }

  protected ColorModel(int pixel_bits, int[] bits, ColorSpace cspace, boolean hasAlpha, boolean isAlphaPremultiplied,
      int transparency, int transferType) {
    colorSpace = cspace;
    colorSpaceType = cspace.getType();
    numColorComponents = cspace.getNumComponents();
    numComponents = numColorComponents + (hasAlpha ? 1 : 0);
    supportsAlpha = hasAlpha;
    if (bits.length < numComponents) {
      throw new IllegalArgumentException("Number of color/alpha components should be " + numComponents
          + " but length of bits array is " + bits.length);
    }
    if (transparency < Transparency.OPAQUE || transparency > Transparency.TRANSLUCENT) {
      throw new IllegalArgumentException("Unknown transparency: " + transparency);
    }
    if (supportsAlpha == false) {
      this.isAlphaPremultiplied = false;
      this.transparency = Transparency.OPAQUE;
    } else {
      this.isAlphaPremultiplied = isAlphaPremultiplied;
      this.transparency = transparency;
    }
    nBits = bits.clone();
    this.pixel_bits = pixel_bits;
    if (pixel_bits <= 0) {
      throw new IllegalArgumentException("Number of pixel bits must be > 0");
    }
    maxBits = 0;
    for (int i = 0; i < bits.length; i++) {
      if (bits[i] < 0) {
        throw new IllegalArgumentException("Number of bits must be >= 0");
      }
      if (maxBits < bits[i]) {
        maxBits = bits[i];
      }
    }
    if (maxBits == 0) {
      throw new IllegalArgumentException("There must be at least one component with > 0 pixel bits.");
    }
    if (cspace != ColorSpace.getInstance(ColorSpace.CS_sRGB)) {
      is_sRGB = false;
    }
    this.transferType = transferType;
  }

  static int getDefaultTransferType(int pixel_bits) {
    if (pixel_bits <= 8) {
      return DataBuffer.TYPE_BYTE;
    } else if (pixel_bits <= 16) {
      return DataBuffer.TYPE_USHORT;
    } else if (pixel_bits <= 32) {
      return DataBuffer.TYPE_INT;
    }
    return DataBuffer.TYPE_UNDEFINED;
  }

  public final boolean hasAlpha() { return supportsAlpha; }

  public final boolean isAlphaPremultiplied() { return isAlphaPremultiplied; }

  public final int getTransferType() { return transferType; }

  public int getPixelSize() { return pixel_bits; }

  public int getComponentSize(int componentIdx) {
    if (nBits == null) {
      throw new NullPointerException("Number of bits array is null.");
    }
    return nBits[componentIdx];
  }

  public int[] getComponentSize() {
    if (nBits != null) {
      return nBits.clone();
    }
    return null;
  }

  public int getTransparency() { return transparency; }

  public int getNumComponents() { return numComponents; }

  public int getNumColorComponents() { return numColorComponents; }

  public abstract int getRed(int pixel);

  public abstract int getGreen(int pixel);

  public abstract int getBlue(int pixel);

  public abstract int getAlpha(int pixel);

  public int getRGB(int pixel) {
    return (getAlpha(pixel) << 24) | (getRed(pixel) << 16) | (getGreen(pixel) << 8) | (getBlue(pixel) << 0);
  }

  int pixelOf(Object inData) {
    switch (transferType) {
    case DataBuffer.TYPE_BYTE:
      return ((byte[]) inData)[0] & 0xff;
    case DataBuffer.TYPE_USHORT:
      return ((short[]) inData)[0] & 0xffff;
    case DataBuffer.TYPE_INT:
      return ((int[]) inData)[0];
    default:
      throw new UnsupportedOperationException("This method has not been implemented for transferType " + transferType);
    }
  }

  public int getRed(Object inData) { return getRed(pixelOf(inData)); }

  public int getGreen(Object inData) { return getGreen(pixelOf(inData)); }

  public int getBlue(Object inData) { return getBlue(pixelOf(inData)); }

  public int getAlpha(Object inData) { return getAlpha(pixelOf(inData)); }

  public int getRGB(Object inData) {
    return (getAlpha(inData) << 24) | (getRed(inData) << 16) | (getGreen(inData) << 8) | (getBlue(inData) << 0);
  }

  public Object getDataElements(int rgb, Object pixel) {
    throw new UnsupportedOperationException("This method is not supported by this color model.");
  }

  public int[] getComponents(int pixel, int[] components, int offset) {
    throw new UnsupportedOperationException("This method is not supported by this color model.");
  }

  public int[] getComponents(Object pixel, int[] components, int offset) {
    throw new UnsupportedOperationException("This method is not supported by this color model.");
  }

  public int[] getUnnormalizedComponents(float[] normComponents, int normOffset, int[] components, int offset) {
    if (colorSpace == null) {
      throw new UnsupportedOperationException("This method is not supported by this color model.");
    }
    if (nBits == null) {
      throw new UnsupportedOperationException("This method is not supported.  Unable to determine #bits per component.");
    }
    if ((normComponents.length - normOffset) < numComponents) {
      throw new IllegalArgumentException("Incorrect number of components.  Expecting " + numComponents);
    }
    if (components == null) {
      components = new int[offset + numComponents];
    }
    if (supportsAlpha && isAlphaPremultiplied) {
      float normAlpha = normComponents[normOffset + numColorComponents];
      for (int i = 0; i < numColorComponents; i++) {
        components[offset + i] = (int) (normComponents[normOffset + i] * ((1 << nBits[i]) - 1) * normAlpha + 0.5f);
      }
      components[offset + numColorComponents] = (int) (normAlpha * ((1 << nBits[numColorComponents]) - 1) + 0.5f);
    } else {
      for (int i = 0; i < numComponents; i++) {
        components[offset + i] = (int) (normComponents[normOffset + i] * ((1 << nBits[i]) - 1) + 0.5f);
      }
    }
    return components;
  }

  public float[] getNormalizedComponents(int[] components, int offset, float[] normComponents, int normOffset) {
    if (colorSpace == null) {
      throw new UnsupportedOperationException("This method is not supported by this color model.");
    }
    if (nBits == null) {
      throw new UnsupportedOperationException("This method is not supported.  Unable to determine #bits per component.");
    }
    if ((components.length - offset) < numComponents) {
      throw new IllegalArgumentException("Incorrect number of components.  Expecting " + numComponents);
    }
    if (normComponents == null) {
      normComponents = new float[numComponents + normOffset];
    }
    if (supportsAlpha && isAlphaPremultiplied) {
      float normAlpha = components[offset + numColorComponents];
      normAlpha /= (float) ((1 << nBits[numColorComponents]) - 1);
      if (normAlpha != 0.0f) {
        for (int i = 0; i < numColorComponents; i++) {
          normComponents[normOffset + i] = components[offset + i] / (normAlpha * ((float) ((1 << nBits[i]) - 1)));
        }
      } else {
        for (int i = 0; i < numColorComponents; i++) {
          normComponents[normOffset + i] = 0.0f;
        }
      }
      normComponents[normOffset + numColorComponents] = normAlpha;
    } else {
      for (int i = 0; i < numComponents; i++) {
        normComponents[normOffset + i] = components[offset + i] / ((float) ((1 << nBits[i]) - 1));
      }
    }
    return normComponents;
  }

  public float[] getNormalizedComponents(Object pixel, float[] normComponents, int normOffset) {
    int[] components = getComponents(pixel, null, 0);
    return getNormalizedComponents(components, 0, normComponents, normOffset);
  }

  public int getDataElement(int[] components, int offset) {
    throw new UnsupportedOperationException("This method is not supported by this color model.");
  }

  public Object getDataElements(int[] components, int offset, Object obj) {
    throw new UnsupportedOperationException("This method has not been implemented for this color model.");
  }

  public int getDataElement(float[] normComponents, int normOffset) {
    int[] components = getUnnormalizedComponents(normComponents, normOffset, null, 0);
    return getDataElement(components, 0);
  }

  public Object getDataElements(float[] normComponents, int normOffset, Object obj) {
    int[] components = getUnnormalizedComponents(normComponents, normOffset, null, 0);
    return getDataElements(components, 0, obj);
  }

  public boolean equals(Object obj) {
    ColorModel cm = null;
    if (!(((cm = obj instanceof ColorModel ? (ColorModel) obj : null) != null))) {
      return false;
    }
    if (this == obj) {
      return true;
    }
    if (supportsAlpha != cm.hasAlpha() || isAlphaPremultiplied != cm.isAlphaPremultiplied()
        || pixel_bits != cm.getPixelSize() || transparency != cm.getTransparency() || numComponents != cm.getNumComponents()) {
      return false;
    }
    int[] nb = cm.getComponentSize();
    if (nBits != null && nb != null) {
      if (nBits.length != nb.length) {
        return false;
      }
      for (int i = 0; i < numComponents; i++) {
        if (nBits[i] != nb[i]) {
          return false;
        }
      }
    } else {
      return nBits == null && nb == null;
    }
    return true;
  }

  public int hashCode() {
    int result = 0;
    result = (supportsAlpha ? 2 : 3) + (isAlphaPremultiplied ? 4 : 5) + pixel_bits * 6 + transparency * 7
        + numComponents * 8;
    if (nBits != null) {
      for (int i = 0; i < numComponents; i++) {
        result = result + nBits[i] * (i + 9);
      }
    }
    return result;
  }

  public final ColorSpace getColorSpace() { return colorSpace; }

  public ColorModel coerceData(WritableRaster raster, boolean isAlphaPremultiplied) {
    throw new UnsupportedOperationException("This method is not supported by this color model");
  }

  public boolean isCompatibleRaster(Raster raster) {
    throw new UnsupportedOperationException("This method has not been implemented for this ColorModel.");
  }

  public WritableRaster createCompatibleWritableRaster(int w, int h) {
    throw new UnsupportedOperationException("This method is not supported by this color model");
  }

  public SampleModel createCompatibleSampleModel(int w, int h) {
    throw new UnsupportedOperationException("This method is not supported by this color model");
  }

  public boolean isCompatibleSampleModel(SampleModel sm) {
    throw new UnsupportedOperationException("This method is not supported by this color model");
  }

  public WritableRaster getAlphaRaster(WritableRaster raster) {
    return null;
  }

  public String toString() {
    return "ColorModel: #pixelBits = " + pixel_bits + " numComponents = " + numComponents + " color space = "
        + colorSpace + " transparency = " + transparencyName() + " has alpha = " + supportsAlpha + " isAlphaPre = "
        + isAlphaPremultiplied;
  }

  private String transparencyName() {
    switch (transparency) {
    case OPAQUE:
      return "Transparency.OPAQUE";
    case BITMASK:
      return "Transparency.BITMASK";
    default:
      return "Transparency.TRANSLUCENT";
    }
  }

  static int scaleTo8(int c, int bits) {
    if (bits == 8) {
      return c;
    }
    int max = (1 << bits) - 1;
    return (int) (c * (255.0f / max) + 0.5f);
  }

  static int scaleFrom8(int c, int bits) {
    if (bits == 8) {
      return c;
    }
    int max = (1 << bits) - 1;
    return (int) (c * (max / 255.0f) + 0.5f);
  }
}
