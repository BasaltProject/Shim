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
import java.math.BigInteger;

public class IndexColorModel extends ColorModel {
  private int[] rgb;
  private int map_size;
  private int pixel_mask;
  private int transparent_index = -1;
  private boolean allgrayopaque;
  private BigInteger validBits;

  public IndexColorModel(int bits, int size, byte[] r, byte[] g, byte[] b) {
    super(bits, opaqueBits(bits), ColorSpace.getInstance(ColorSpace.CS_sRGB), false, false, Transparency.OPAQUE,
        ColorModel.getDefaultTransferType(bits));
    setRGBs(bits, size, r, g, b, null);
    calculatePixelMask();
  }

  public IndexColorModel(int bits, int size, byte[] r, byte[] g, byte[] b, int trans) {
    super(bits, opaqueBits(bits), ColorSpace.getInstance(ColorSpace.CS_sRGB), false, false, Transparency.OPAQUE,
        ColorModel.getDefaultTransferType(bits));
    setRGBs(bits, size, r, g, b, null);
    setTransparentPixel(trans);
    calculatePixelMask();
  }

  public IndexColorModel(int bits, int size, byte[] r, byte[] g, byte[] b, byte[] a) {
    super(bits, alphaBits(bits), ColorSpace.getInstance(ColorSpace.CS_sRGB), true, false, Transparency.OPAQUE,
        ColorModel.getDefaultTransferType(bits));
    setRGBs(bits, size, r, g, b, a);
    calculatePixelMask();
  }

  public IndexColorModel(int bits, int size, byte[] cmap, int start, boolean hasalpha) {
    this(bits, size, cmap, start, hasalpha, -1);
  }

  public IndexColorModel(int bits, int size, byte[] cmap, int start, boolean hasalpha, int trans) {
    super(bits, hasalpha ? alphaBits(bits) : opaqueBits(bits), ColorSpace.getInstance(ColorSpace.CS_sRGB), hasalpha,
        false, Transparency.OPAQUE, ColorModel.getDefaultTransferType(bits));
    if (bits < 1 || bits > 16) {
      throw new IllegalArgumentException("Number of bits must be between 1 and 16.");
    }
    if (size < 1) {
      throw new IllegalArgumentException("Map size (" + size + ") must be >= 1");
    }
    map_size = size;
    rgb = new int[calcRealMapSize(bits, size)];
    int j = start;
    int alpha = 0xff;
    boolean allgray = true;
    int transparency = OPAQUE;
    for (int i = 0; i < size; i++) {
      int r = cmap[j++] & 0xff;
      int g = cmap[j++] & 0xff;
      int b = cmap[j++] & 0xff;
      allgray = allgray && (r == g) && (g == b);
      if (hasalpha) {
        alpha = cmap[j++] & 0xff;
        if (alpha != 0xff) {
          if (alpha == 0x00) {
            if (transparency == OPAQUE) {
              transparency = BITMASK;
            }
            if (transparent_index < 0) {
              transparent_index = i;
            }
          } else {
            transparency = TRANSLUCENT;
          }
          allgray = false;
        }
      }
      rgb[i] = (alpha << 24) | (r << 16) | (g << 8) | b;
    }
    this.allgrayopaque = allgray;
    setTransparency(transparency);
    setTransparentPixel(trans);
    calculatePixelMask();
  }

  public IndexColorModel(int bits, int size, int[] cmap, int start, boolean hasalpha, int trans, int transferType) {
    super(bits, hasalpha ? alphaBits(bits) : opaqueBits(bits), ColorSpace.getInstance(ColorSpace.CS_sRGB), hasalpha,
        false, Transparency.OPAQUE, transferType);
    if (bits < 1 || bits > 16) {
      throw new IllegalArgumentException("Number of bits must be between 1 and 16.");
    }
    if (size < 1) {
      throw new IllegalArgumentException("Map size (" + size + ") must be >= 1");
    }
    if (transferType != DataBuffer.TYPE_BYTE && transferType != DataBuffer.TYPE_USHORT) {
      throw new IllegalArgumentException("transferType must be either DataBuffer.TYPE_BYTE or DataBuffer.TYPE_USHORT");
    }
    setRGBs(size, cmap, start, hasalpha);
    setTransparentPixel(trans);
    calculatePixelMask();
  }

  public IndexColorModel(int bits, int size, int[] cmap, int start, int transferType, BigInteger validBits) {
    super(bits, alphaBits(bits), ColorSpace.getInstance(ColorSpace.CS_sRGB), true, false, Transparency.OPAQUE,
        transferType);
    if (bits < 1 || bits > 16) {
      throw new IllegalArgumentException("Number of bits must be between 1 and 16.");
    }
    if (size < 1) {
      throw new IllegalArgumentException("Map size (" + size + ") must be >= 1");
    }
    if (transferType != DataBuffer.TYPE_BYTE && transferType != DataBuffer.TYPE_USHORT) {
      throw new IllegalArgumentException("transferType must be either DataBuffer.TYPE_BYTE or DataBuffer.TYPE_USHORT");
    }
    if (validBits != null) {
      for (int i = 0; i < size; i++) {
        if (!validBits.testBit(i)) {
          this.validBits = validBits;
          break;
        }
      }
    }
    setRGBs(size, cmap, start, true);
    calculatePixelMask();
  }

  private static int[] opaqueBits(int bits) {
    return new int[] {8, 8, 8};
  }

  private static int[] alphaBits(int bits) {
    return new int[] {8, 8, 8, 8};
  }

  private void setRGBs(int bits, int size, byte[] r, byte[] g, byte[] b, byte[] a) {
    if (bits < 1 || bits > 16) {
      throw new IllegalArgumentException("Number of bits must be between 1 and 16.");
    }
    if (size < 1) {
      throw new IllegalArgumentException("Map size (" + size + ") must be >= 1");
    }
    map_size = size;
    rgb = new int[calcRealMapSize(bits, size)];
    int alpha = 0xff;
    int transparency = OPAQUE;
    boolean allgray = true;
    for (int i = 0; i < size; i++) {
      int rc = r[i] & 0xff;
      int gc = g[i] & 0xff;
      int bc = b[i] & 0xff;
      allgray = allgray && (rc == gc) && (gc == bc);
      if (a != null) {
        alpha = a[i] & 0xff;
        if (alpha != 0xff) {
          if (alpha == 0x00) {
            if (transparency == OPAQUE) {
              transparency = BITMASK;
            }
            if (transparent_index < 0) {
              transparent_index = i;
            }
          } else {
            transparency = TRANSLUCENT;
          }
          allgray = false;
        }
      }
      rgb[i] = (alpha << 24) | (rc << 16) | (gc << 8) | bc;
    }
    this.allgrayopaque = allgray;
    setTransparency(transparency);
  }

  private void setRGBs(int size, int[] cmap, int start, boolean hasalpha) {
    map_size = size;
    rgb = new int[calcRealMapSize(pixel_bits, size)];
    int j = start;
    int transparency = OPAQUE;
    boolean allgray = true;
    BigInteger validBits = this.validBits;
    for (int i = 0; i < size; i++, j++) {
      if (validBits != null && !validBits.testBit(i)) {
        continue;
      }
      int cmaprgb = cmap[j];
      int r = (cmaprgb >> 16) & 0xff;
      int g = (cmaprgb >> 8) & 0xff;
      int b = cmaprgb & 0xff;
      allgray = allgray && (r == g) && (g == b);
      if (hasalpha) {
        int alpha = cmaprgb >>> 24;
        if (alpha != 0xff) {
          if (alpha == 0x00) {
            if (transparency == OPAQUE) {
              transparency = BITMASK;
            }
            if (transparent_index < 0) {
              transparent_index = i;
            }
          } else {
            transparency = TRANSLUCENT;
          }
          allgray = false;
        }
      } else {
        cmaprgb |= 0xff000000;
      }
      rgb[i] = cmaprgb;
    }
    this.allgrayopaque = allgray;
    setTransparency(transparency);
  }

  private int calcRealMapSize(int bits, int size) {
    int newSize = Math.max(1 << bits, size);
    return Math.max(newSize, 256);
  }

  private void setTransparency(int transparency) {
    if (this.transparency != transparency) {
      this.transparency = transparency;
      if (transparency == OPAQUE) {
        supportsAlpha = false;
        numComponents = 3;
        nBits = opaqueBits(0);
      } else {
        supportsAlpha = true;
        numComponents = 4;
        nBits = alphaBits(0);
      }
    }
  }

  private void setTransparentPixel(int trans) {
    if (trans >= 0 && trans < map_size) {
      rgb[trans] &= 0x00ffffff;
      transparent_index = trans;
      allgrayopaque = false;
      if (this.transparency == OPAQUE) {
        setTransparency(BITMASK);
      }
    }
  }

  private void calculatePixelMask() {
    int maskBits = pixel_bits;
    if (maskBits == 3) {
      maskBits = 4;
    } else if (maskBits > 4 && maskBits < 8) {
      maskBits = 8;
    }
    pixel_mask = (1 << maskBits) - 1;
  }

  public final int getMapSize() { return map_size; }

  public final int getTransparentPixel() { return transparent_index; }

  public final void getReds(byte[] r) {
    for (int i = 0; i < map_size; i++) {
      r[i] = (byte) (rgb[i] >> 16);
    }
  }

  public final void getGreens(byte[] g) {
    for (int i = 0; i < map_size; i++) {
      g[i] = (byte) (rgb[i] >> 8);
    }
  }

  public final void getBlues(byte[] b) {
    for (int i = 0; i < map_size; i++) {
      b[i] = (byte) rgb[i];
    }
  }

  public final void getAlphas(byte[] a) {
    for (int i = 0; i < map_size; i++) {
      a[i] = (byte) (rgb[i] >> 24);
    }
  }

  public final void getRGBs(int[] rgb) {
    System.arraycopy(this.rgb, 0, rgb, 0, map_size);
  }

  public int getTransparency() { return transparency; }

  public int[] getComponentSize() {
    if (nBits == null) {
      nBits = supportsAlpha ? alphaBits(0) : opaqueBits(0);
    }
    return nBits.clone();
  }

  public final int getRed(int pixel) { return (rgb[pixel & pixel_mask] >> 16) & 0xff; }

  public final int getGreen(int pixel) { return (rgb[pixel & pixel_mask] >> 8) & 0xff; }

  public final int getBlue(int pixel) { return rgb[pixel & pixel_mask] & 0xff; }

  public final int getAlpha(int pixel) { return (rgb[pixel & pixel_mask] >> 24) & 0xff; }

  public final int getRGB(int pixel) { return rgb[pixel & pixel_mask]; }

  public Object getDataElements(int rgb, Object pixel) {
    int red = (rgb >> 16) & 0xff;
    int green = (rgb >> 8) & 0xff;
    int blue = rgb & 0xff;
    int alpha = (rgb >>> 24);
    int pix = 0;
    int minDist = Integer.MAX_VALUE;
    if (allgrayopaque && validBits == null) {
      int gray = (77 * red + 150 * green + 29 * blue + 128) / 256;
      for (int i = 0; i < map_size; i++) {
        int d = Math.abs(gray - (this.rgb[i] & 0xff));
        if (d < minDist) {
          minDist = d;
          pix = i;
        }
      }
      return packPixel(pix, pixel);
    }
    for (int i = 0; i < map_size; i++) {
      int c = this.rgb[i];
      if (c == rgb && (validBits == null || validBits.testBit(i))) {
        pix = i;
        break;
      }
      if (validBits != null && !validBits.testBit(i)) {
        continue;
      }
      int dr = red - ((c >> 16) & 0xff);
      int dg = green - ((c >> 8) & 0xff);
      int db = blue - (c & 0xff);
      int da = alpha - (c >>> 24);
      int dist = dr * dr + dg * dg + db * db + da * da;
      if (dist < minDist) {
        minDist = dist;
        pix = i;
      }
    }
    return packPixel(pix, pixel);
  }

  private Object packPixel(int pix, Object pixel) {
    switch (transferType) {
    case DataBuffer.TYPE_BYTE: {
      byte[] bdata = pixel == null ? new byte[1] : (byte[]) pixel;
      bdata[0] = (byte) (pix & 0xff);
      return bdata;
    }
    case DataBuffer.TYPE_USHORT: {
      short[] sdata = pixel == null ? new short[1] : (short[]) pixel;
      sdata[0] = (short) (pix & 0xffff);
      return sdata;
    }
    default:
      throw new UnsupportedOperationException("This method has not been implemented for transferType " + transferType);
    }
  }

  public int[] getComponents(int pixel, int[] components, int offset) {
    if (components == null) {
      components = new int[offset + numComponents];
    }
    components[offset + 0] = getRed(pixel);
    components[offset + 1] = getGreen(pixel);
    components[offset + 2] = getBlue(pixel);
    if (supportsAlpha && (components.length - offset) > 3) {
      components[offset + 3] = getAlpha(pixel);
    }
    return components;
  }

  public int[] getComponents(Object pixel, int[] components, int offset) {
    return getComponents(pixelOf(pixel), components, offset);
  }

  public int getDataElement(int[] components, int offset) {
    int rgb = (components[offset + 0] << 16) | (components[offset + 1] << 8) | components[offset + 2];
    if (supportsAlpha) {
      rgb |= (components[offset + 3] << 24);
    } else {
      rgb |= 0xff000000;
    }
    Object inData = getDataElements(rgb, null);
    return pixelOf(inData);
  }

  public Object getDataElements(int[] components, int offset, Object pixel) {
    int rgb = (components[offset + 0] << 16) | (components[offset + 1] << 8) | components[offset + 2];
    if (supportsAlpha) {
      rgb |= (components[offset + 3] << 24);
    } else {
      rgb &= 0x00ffffff;
    }
    return getDataElements(rgb, pixel);
  }

  public WritableRaster createCompatibleWritableRaster(int w, int h) {
    WritableRaster raster;
    if (pixel_bits == 1 || pixel_bits == 2 || pixel_bits == 4) {
      raster = Raster.createPackedRaster(DataBuffer.TYPE_BYTE, w, h, 1, pixel_bits, null);
    } else if (pixel_bits <= 8) {
      raster = Raster.createInterleavedRaster(DataBuffer.TYPE_BYTE, w, h, 1, null);
    } else if (pixel_bits <= 16) {
      raster = Raster.createInterleavedRaster(DataBuffer.TYPE_USHORT, w, h, 1, null);
    } else {
      throw new UnsupportedOperationException("This method is not supported for pixel bits > 16");
    }
    return raster;
  }

  public boolean isCompatibleRaster(Raster raster) {
    int size = raster.getSampleModel().getSampleSize(0);
    return ((raster.getTransferType() == transferType) && (raster.getNumBands() == 1) && ((1 << size) >= map_size));
  }

  public SampleModel createCompatibleSampleModel(int w, int h) {
    int[] off = new int[1];
    off[0] = 0;
    if (pixel_bits == 1 || pixel_bits == 2 || pixel_bits == 4) {
      return new MultiPixelPackedSampleModel(transferType, w, h, pixel_bits);
    }
    return new ComponentSampleModel(transferType, w, h, 1, w, off);
  }

  public boolean isCompatibleSampleModel(SampleModel sm) {
    if (!(sm instanceof ComponentSampleModel) && !(sm instanceof MultiPixelPackedSampleModel)) {
      return false;
    }
    if (sm.getTransferType() != transferType) {
      return false;
    }
    if (sm.getNumBands() != 1) {
      return false;
    }
    return true;
  }

  public BufferedImage convertToIntDiscrete(Raster raster, boolean forceARGB) {
    ColorModel cm;
    if (!isCompatibleRaster(raster)) {
      throw new IllegalArgumentException("This raster is not compatible with this IndexColorModel.");
    }
    if (forceARGB || transparency == TRANSLUCENT) {
      cm = ColorModel.getRGBdefault();
    } else if (transparency == BITMASK) {
      cm = new DirectColorModel(25, 0xff0000, 0x00ff00, 0x0000ff, 0x1000000);
    } else {
      cm = new DirectColorModel(24, 0xff0000, 0x00ff00, 0x0000ff);
    }
    int w = raster.getWidth();
    int h = raster.getHeight();
    WritableRaster discrete = cm.createCompatibleWritableRaster(w, h);
    Object obj = null;
    int[] data = null;
    int rX = raster.getMinX();
    int rY = raster.getMinY();
    for (int y = 0; y < h; y++, rY++) {
      obj = raster.getDataElements(rX, rY, w, 1, obj);
      if (obj instanceof int[]) {
        data = (int[]) obj;
      } else {
        data = SampleModelHelper.toInts(obj, data);
      }
      for (int x = 0; x < w; x++) {
        data[x] = rgb[data[x] & pixel_mask];
      }
      discrete.setDataElements(0, y, w, 1, data);
    }
    return new BufferedImage(cm, discrete, false, null);
  }

  public boolean isValid(int pixel) {
    return ((pixel >= 0 && pixel < map_size) && (validBits == null || validBits.testBit(pixel)));
  }

  public boolean isValid() { return validBits == null; }

  public BigInteger getValidPixels() {
    if (validBits == null) {
      return BigInteger.ONE.shiftLeft(map_size).subtract(BigInteger.ONE);
    }
    return validBits;
  }

  public String toString() {
    return "IndexColorModel: #pixelBits = " + pixel_bits + " numComponents = " + numComponents + " color space = "
        + colorSpace + " transparency = " + transparency + " transIndex   = " + transparent_index + " has alpha = "
        + supportsAlpha + " isAlphaPre = " + isAlphaPremultiplied;
  }

  public int hashCode() {
    return super.hashCode() * 31 + java.util.Arrays.hashCode(rgb) + map_size;
  }

  public boolean equals(Object obj) {
    IndexColorModel cm = null;
    if (!(((cm = obj instanceof IndexColorModel ? (IndexColorModel) obj : null) != null))) {
      return false;
    }
    return super.equals(obj) && map_size == cm.map_size && transparent_index == cm.transparent_index
        && java.util.Arrays.equals(java.util.Arrays.copyOf(rgb, map_size), java.util.Arrays.copyOf(cm.rgb, cm.map_size));
  }
}
