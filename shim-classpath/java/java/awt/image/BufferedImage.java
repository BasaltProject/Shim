/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.awt.image;

import java.awt.Image;
import java.awt.Transparency;

public class BufferedImage extends Image implements RenderedImage, Transparency {
  public static final int TYPE_CUSTOM = 0;
  public static final int TYPE_INT_RGB = 1;
  public static final int TYPE_INT_ARGB = 2;
  public static final int TYPE_INT_ARGB_PRE = 3;
  public static final int TYPE_INT_BGR = 4;
  public static final int TYPE_3BYTE_BGR = 5;
  public static final int TYPE_4BYTE_ABGR = 6;
  public static final int TYPE_4BYTE_ABGR_PRE = 7;
  public static final int TYPE_USHORT_565_RGB = 8;
  public static final int TYPE_USHORT_555_RGB = 9;
  public static final int TYPE_BYTE_GRAY = 10;
  public static final int TYPE_USHORT_GRAY = 11;
  public static final int TYPE_BYTE_BINARY = 12;
  public static final int TYPE_BYTE_INDEXED = 13;

  private static final String kGrayToRgb = "000d161c22262a2e3235383b3d40424547494b4d4f51535556585a5c5d5f606263656668696a6c6d6e7071727375767778797a7c7d7e7f808182838485868788898a8b8c8d8e8f90919293949495969798999a9b9b9c9d9e9f9fa0a1a2a3a3a4a5a6a7a7a8a9aaaaabacadadaeafafb0b1b2b2b3b4b4b5b6b6b7b8b9b9babbbbbcbdbdbebebfc0c0c1c2c2c3c4c4c5c5c6c7c7c8c8c9cacacbcbcccdcdcececfd0d0d1d1d2d2d3d4d4d5d5d6d6d7d7d8d8d9dadadbdbdcdcdddddededfdfe0e0e1e2e2e3e3e4e4e5e5e6e6e7e7e8e8e9e9eaeaebebececededeeeeeeefeff0f0f1f1f2f2f3f3f4f4f5f5f6f6f6f7f7f8f8f9f9fafafbfbfbfcfcfdfdfefeffff";

  private final int width;
  private final int height;
  private final int type;
  private final boolean alphaOverride;
  private final int[] data;
  private final int offset;
  private final int scan;
  private final int originX;
  private final int originY;

  public BufferedImage(int width, int height, int imageType) {
    this(width, height, imageType, false);
  }

  public BufferedImage(int width, int height, int imageType, boolean forceAlpha) {
    this.alphaOverride = forceAlpha;
    if (width <= 0 || height <= 0) {
      throw new IllegalArgumentException("Width (" + width + ") and height (" + height + ") cannot be <= 0");
    }
    switch (imageType) {
    case TYPE_INT_RGB:
    case TYPE_INT_ARGB:
    case TYPE_INT_ARGB_PRE:
    case TYPE_INT_BGR:
    case TYPE_3BYTE_BGR:
    case TYPE_4BYTE_ABGR:
    case TYPE_4BYTE_ABGR_PRE:
    case TYPE_BYTE_GRAY:
    case TYPE_BYTE_INDEXED:
    case TYPE_BYTE_BINARY:
      break;
    case TYPE_CUSTOM:
      if (forceAlpha) {
        break;
      }
      throw new IllegalArgumentException("Unknown image type " + imageType);
    default:
      throw new IllegalArgumentException("Unknown image type " + imageType);
    }
    this.width = width;
    this.height = height;
    this.type = imageType;
    this.data = new int[Math.multiplyExact(width, height)];
    this.offset = 0;
    this.scan = width;
    this.originX = 0;
    this.originY = 0;
    if (!forceAlpha && hasAlpha(imageType) == false && imageType != TYPE_BYTE_GRAY) {
      java.util.Arrays.fill(data, 0xff000000);
    }
  }

  private BufferedImage(BufferedImage parent, int x, int y, int w, int h) {
    this.width = w;
    this.height = h;
    this.type = parent.type;
    this.alphaOverride = parent.alphaOverride;
    this.data = parent.data;
    this.offset = parent.offset + y * parent.scan + x;
    this.scan = parent.scan;
    this.originX = parent.originX + x;
    this.originY = parent.originY + y;
  }

  private static boolean hasAlpha(int t) {
    return t == TYPE_INT_ARGB || t == TYPE_INT_ARGB_PRE || t == TYPE_4BYTE_ABGR || t == TYPE_4BYTE_ABGR_PRE;
  }

  private static boolean premultiplied(int t) {
    return t == TYPE_INT_ARGB_PRE || t == TYPE_4BYTE_ABGR_PRE;
  }

  private static double linear(int c) {
    double v = c / 255.0;
    return v <= 0.04045 ? v / 12.92 : Math.pow((v + 0.055) / 1.055, 2.4);
  }

  private static int grayOf(int rgb) {
    double y = 0.2126 * linear((rgb >> 16) & 0xff) + 0.7152 * linear((rgb >> 8) & 0xff) + 0.0722 * linear(rgb & 0xff);
    return (int) (y * 255 + 0.5);
  }

  private int encode(int rgb) {
    if (type == TYPE_BYTE_GRAY) {
      return grayOf(rgb);
    }
    if (alphaOverride) {
      return rgb;
    }
    if (!hasAlpha(type)) {
      return 0xff000000 | rgb;
    }
    if (premultiplied(type)) {
      int a = rgb >>> 24;
      int r = (int) (((rgb >> 16) & 0xff) * (a / 255.0f) + 0.5f);
      int g = (int) (((rgb >> 8) & 0xff) * (a / 255.0f) + 0.5f);
      int b = (int) ((rgb & 0xff) * (a / 255.0f) + 0.5f);
      return (a << 24) | (r << 16) | (g << 8) | b;
    }
    return rgb;
  }

  private int decode(int v) {
    if (type == TYPE_BYTE_GRAY) {
      int g = Integer.parseInt(kGrayToRgb.substring(2 * (v & 0xff), 2 * (v & 0xff) + 2), 16);
      return 0xff000000 | (g << 16) | (g << 8) | g;
    }
    if (premultiplied(type)) {
      int a = v >>> 24;
      if (a == 0 || a == 255) {
        return v;
      }
      float scale = 255.0f / a;
      int r = Math.min(255, (int) (((v >> 16) & 0xff) * scale + 0.5f));
      int g = Math.min(255, (int) (((v >> 8) & 0xff) * scale + 0.5f));
      int b = Math.min(255, (int) ((v & 0xff) * scale + 0.5f));
      return (a << 24) | (r << 16) | (g << 8) | b;
    }
    return v;
  }

  public int getType() {
    return type;
  }

  @Override
  public int getWidth() {
    return width;
  }

  @Override
  public int getHeight() {
    return height;
  }

  public int getWidth(ImageObserver observer) {
    return width;
  }

  public int getHeight(ImageObserver observer) {
    return height;
  }

  @Override
  public int getMinX() {
    return 0;
  }

  @Override
  public int getMinY() {
    return 0;
  }

  public int getRGB(int x, int y) {
    if (x < 0 || y < 0 || x >= width || y >= height) {
      throw new ArrayIndexOutOfBoundsException("Coordinate out of bounds!");
    }
    return decode(data[offset + y * scan + x]);
  }

  public int[] getRGB(int startX, int startY, int w, int h, int[] rgbArray, int off, int scansize) {
    int yoff = off;
    if (rgbArray == null) {
      rgbArray = new int[off + h * scansize];
    }
    for (int y = startY; y < startY + h; y++, yoff += scansize) {
      int xoff = yoff;
      for (int x = startX; x < startX + w; x++) {
        rgbArray[xoff++] = getRGB(x, y);
      }
    }
    return rgbArray;
  }

  public synchronized void setRGB(int x, int y, int rgb) {
    if (x < 0 || y < 0 || x >= width || y >= height) {
      throw new ArrayIndexOutOfBoundsException("Coordinate out of bounds!");
    }
    data[offset + y * scan + x] = encode(rgb);
  }

  public void setRGB(int startX, int startY, int w, int h, int[] rgbArray, int off, int scansize) {
    int yoff = off;
    for (int y = startY; y < startY + h; y++, yoff += scansize) {
      int xoff = yoff;
      for (int x = startX; x < startX + w; x++) {
        setRGB(x, y, rgbArray[xoff++]);
      }
    }
  }

  public BufferedImage getSubimage(int x, int y, int w, int h) {
    if (x < 0) {
      throw new java.awt.image.RasterFormatException("x lies outside raster");
    }
    if (y < 0) {
      throw new java.awt.image.RasterFormatException("y lies outside raster");
    }
    if (w <= 0) {
      throw new java.awt.image.RasterFormatException("negative or zero " + "width");
    }
    if (h <= 0) {
      throw new java.awt.image.RasterFormatException("negative or zero " + "height");
    }
    if (x + w < x || x + w > width) {
      throw new java.awt.image.RasterFormatException("(x + width) is outside raster");
    }
    if (y + h < y || y + h > height) {
      throw new java.awt.image.RasterFormatException("(y + height) is outside raster");
    }
    return new BufferedImage(this, x, y, w, h);
  }

  public WritableRaster getRaster() {
    return new WritableRaster(width, height, type == TYPE_BYTE_GRAY ? 1 : (hasAlpha(type) ? 4 : 3)) {
      @Override
      public int getSample(int x, int y, int b) {
        if (x < 0 || y < 0 || x >= width || y >= height) {
          throw new ArrayIndexOutOfBoundsException("Coordinate out of bounds!");
        }
        int v = data[offset + y * scan + x];
        if (type == TYPE_BYTE_GRAY) {
          return v & 0xff;
        }
        return b == 0 ? (v >> 16) & 0xff : (b == 1 ? (v >> 8) & 0xff : (b == 2 ? v & 0xff : v >>> 24));
      }

      @Override
      public void setSample(int x, int y, int b, int s) {
        if (x < 0 || y < 0 || x >= width || y >= height) {
          throw new ArrayIndexOutOfBoundsException("Coordinate out of bounds!");
        }
        int i = offset + y * scan + x;
        if (type == TYPE_BYTE_GRAY) {
          data[i] = s & 0xff;
          return;
        }
        int shift = b == 0 ? 16 : (b == 1 ? 8 : (b == 2 ? 0 : 24));
        data[i] = (data[i] & ~(0xff << shift)) | ((s & 0xff) << shift);
      }
    };
  }

  public boolean isAlphaPremultiplied() {
    return premultiplied(type);
  }

  @Override
  public int getTransparency() {
    return hasAlpha(type) || alphaOverride ? Transparency.TRANSLUCENT : Transparency.OPAQUE;
  }

  public Object getProperty(String name) {
    if (name == null) {
      throw new NullPointerException("null property name is not allowed");
    }
    return Image.UndefinedProperty;
  }

  public Object getProperty(String name, ImageObserver observer) {
    return getProperty(name);
  }

  public String[] getPropertyNames() {
    return null;
  }

  @Override
  public String toString() {
    return "BufferedImage@" + Integer.toHexString(hashCode()) + ": type = " + type;
  }
}
