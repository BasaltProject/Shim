/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.awt.image;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.util.Hashtable;
import java.util.Vector;

public class BufferedImage extends Image implements WritableRenderedImage, Transparency {
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

  private int imageType = TYPE_CUSTOM;
  private ColorModel colorModel;
  private final WritableRaster raster;
  private Hashtable<?, ?> properties;
  private Vector<TileObserver> tileObservers;
  private int writers;

  public BufferedImage(int width, int height, int imageType) {
    switch (imageType) {
    case TYPE_INT_RGB:
      colorModel = new DirectColorModel(24, 0x00ff0000, 0x0000ff00, 0x000000ff, 0x0);
      raster = colorModel.createCompatibleWritableRaster(width, height);
      break;
    case TYPE_INT_ARGB:
      colorModel = ColorModel.getRGBdefault();
      raster = colorModel.createCompatibleWritableRaster(width, height);
      break;
    case TYPE_INT_ARGB_PRE:
      colorModel = new DirectColorModel(ColorSpace.getInstance(ColorSpace.CS_sRGB), 32, 0x00ff0000, 0x0000ff00,
          0x000000ff, 0xff000000, true, DataBuffer.TYPE_INT);
      raster = colorModel.createCompatibleWritableRaster(width, height);
      break;
    case TYPE_INT_BGR:
      colorModel = new DirectColorModel(24, 0x000000ff, 0x0000ff00, 0x00ff0000);
      raster = colorModel.createCompatibleWritableRaster(width, height);
      break;
    case TYPE_3BYTE_BGR: {
      ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_sRGB);
      colorModel = new ComponentColorModel(cs, new int[] {8, 8, 8}, false, false, Transparency.OPAQUE,
          DataBuffer.TYPE_BYTE);
      raster = Raster.createInterleavedRaster(DataBuffer.TYPE_BYTE, width, height, width * 3, 3,
          new int[] {2, 1, 0}, null);
      break;
    }
    case TYPE_4BYTE_ABGR: {
      ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_sRGB);
      colorModel = new ComponentColorModel(cs, new int[] {8, 8, 8, 8}, true, false, Transparency.TRANSLUCENT,
          DataBuffer.TYPE_BYTE);
      raster = Raster.createInterleavedRaster(DataBuffer.TYPE_BYTE, width, height, width * 4, 4,
          new int[] {3, 2, 1, 0}, null);
      break;
    }
    case TYPE_4BYTE_ABGR_PRE: {
      ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_sRGB);
      colorModel = new ComponentColorModel(cs, new int[] {8, 8, 8, 8}, true, true, Transparency.TRANSLUCENT,
          DataBuffer.TYPE_BYTE);
      raster = Raster.createInterleavedRaster(DataBuffer.TYPE_BYTE, width, height, width * 4, 4,
          new int[] {3, 2, 1, 0}, null);
      break;
    }
    case TYPE_BYTE_GRAY: {
      ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_GRAY);
      colorModel = new ComponentColorModel(cs, new int[] {8}, false, false, Transparency.OPAQUE,
          DataBuffer.TYPE_BYTE);
      raster = colorModel.createCompatibleWritableRaster(width, height);
      break;
    }
    case TYPE_USHORT_GRAY: {
      ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_GRAY);
      colorModel = new ComponentColorModel(cs, new int[] {16}, false, false, Transparency.OPAQUE,
          DataBuffer.TYPE_USHORT);
      raster = colorModel.createCompatibleWritableRaster(width, height);
      break;
    }
    case TYPE_BYTE_BINARY: {
      byte[] arr = {(byte) 0, (byte) 255};
      colorModel = new IndexColorModel(1, 2, arr, arr, arr);
      raster = Raster.createPackedRaster(DataBuffer.TYPE_BYTE, width, height, 1, 1, null);
      break;
    }
    case TYPE_BYTE_INDEXED: {
      int[] rgb = new int[256];
      int index = 0;
      for (int r = 0; r < 256; r += 51) {
        for (int g = 0; g < 256; g += 51) {
          for (int b = 0; b < 256; b += 51) {
            rgb[index++] = (0xff << 24) | (r << 16) | (g << 8) | b;
          }
        }
      }
      int grayIncr = 256 / (256 - index);
      int gray = grayIncr * 3;
      for (; index < 256; index++) {
        rgb[index] = (0xff << 24) | (gray << 16) | (gray << 8) | gray;
        gray += grayIncr;
      }
      colorModel = new IndexColorModel(8, 256, rgb, 0, false, -1, DataBuffer.TYPE_BYTE);
      raster = Raster.createInterleavedRaster(DataBuffer.TYPE_BYTE, width, height, 1, null);
      break;
    }
    case TYPE_USHORT_565_RGB:
      colorModel = new DirectColorModel(ColorSpace.getInstance(ColorSpace.CS_sRGB), 16, 0xF800, 0x07E0, 0x001F, 0,
          false, DataBuffer.TYPE_USHORT);
      raster = colorModel.createCompatibleWritableRaster(width, height);
      break;
    case TYPE_USHORT_555_RGB:
      colorModel = new DirectColorModel(ColorSpace.getInstance(ColorSpace.CS_sRGB), 15, 0x7C00, 0x03E0, 0x001F, 0,
          false, DataBuffer.TYPE_USHORT);
      raster = colorModel.createCompatibleWritableRaster(width, height);
      break;
    default:
      throw new IllegalArgumentException("Unknown image type " + imageType);
    }
    this.imageType = imageType;
  }

  public BufferedImage(int width, int height, int imageType, IndexColorModel cm) {
    if (cm.hasAlpha() && isPremultiplied(cm)) {
      throw new IllegalArgumentException("This image types do not have premultiplied alpha.");
    }
    switch (imageType) {
    case TYPE_BYTE_BINARY: {
      int bits;
      int mapSize = cm.getMapSize();
      if (mapSize <= 2) {
        bits = 1;
      } else if (mapSize <= 4) {
        bits = 2;
      } else if (mapSize <= 16) {
        bits = 4;
      } else {
        throw new IllegalArgumentException("Color map for TYPE_BYTE_BINARY must have no more than 16 entries");
      }
      raster = Raster.createPackedRaster(DataBuffer.TYPE_BYTE, width, height, 1, bits, null);
      break;
    }
    case TYPE_BYTE_INDEXED:
      raster = Raster.createInterleavedRaster(DataBuffer.TYPE_BYTE, width, height, 1, null);
      break;
    default:
      throw new IllegalArgumentException("Invalid image type (" + imageType + ").  Image type must be either TYPE_BYTE_BINARY or  TYPE_BYTE_INDEXED");
    }
    if (!cm.isCompatibleRaster(raster)) {
      throw new IllegalArgumentException("Incompatible image type and IndexColorModel");
    }
    colorModel = cm;
    this.imageType = imageType;
  }

  private static boolean isPremultiplied(ColorModel cm) {
    return cm.isAlphaPremultiplied();
  }

  public BufferedImage(ColorModel cm, WritableRaster raster, boolean isRasterPremultiplied,
      Hashtable<?, ?> properties) {
    if (!cm.isCompatibleRaster(raster)) {
      throw new IllegalArgumentException("Raster " + raster + " is incompatible with ColorModel " + cm);
    }
    if ((raster.minX != 0) || (raster.minY != 0)) {
      throw new IllegalArgumentException("Raster " + raster + " has minX or minY not equal to zero: " + raster.minX
          + " " + raster.minY);
    }
    colorModel = cm;
    this.raster = raster;
    this.properties = properties;
    int numBands = raster.getNumBands();
    boolean isAlphaPre = cm.isAlphaPremultiplied();
    coerceData(isRasterPremultiplied);
    ColorSpace cs = cm.getColorSpace();
    int csType = cs.getType();
    SampleModel sm = raster.getSampleModel();
    ComponentSampleModel csm2 = null;
    IndexColorModel icm = null;
    if (cm instanceof IndexColorModel) {
      IndexColorModel icm0 = (IndexColorModel) cm;
      if (numBands == 1 && raster.getTransferType() == DataBuffer.TYPE_BYTE) {
        int bits = icm0.getPixelSize();
        if (sm instanceof MultiPixelPackedSampleModel && (bits == 1 || bits == 2 || bits == 4)) {
          imageType = TYPE_BYTE_BINARY;
        } else if (sm instanceof ComponentSampleModel && bits == 8) {
          imageType = TYPE_BYTE_INDEXED;
        }
      }
    } else     if (csType == ColorSpace.TYPE_RGB) {
      DirectColorModel dcm = null;
      DirectColorModel dcm2 = null;
      ComponentSampleModel csm = null;
      if (((dcm = cm instanceof DirectColorModel ? (DirectColorModel) cm : null) != null) && raster.getTransferType() == DataBuffer.TYPE_INT
          && sm instanceof SinglePixelPackedSampleModel) {
        int rm = dcm.getRedMask();
        int gm = dcm.getGreenMask();
        int bm = dcm.getBlueMask();
        int am = dcm.getAlphaMask();
        if (dcm.getPixelSize() == 32 && rm == 0x00ff0000 && gm == 0x0000ff00 && bm == 0x000000ff && am == 0xff000000) {
          imageType = isAlphaPre ? TYPE_INT_ARGB_PRE : TYPE_INT_ARGB;
        } else if (dcm.getPixelSize() == 24 && rm == 0x00ff0000 && gm == 0x0000ff00 && bm == 0x000000ff && am == 0) {
          imageType = TYPE_INT_RGB;
        } else if (dcm.getPixelSize() == 24 && rm == 0x000000ff && gm == 0x0000ff00 && bm == 0x00ff0000 && am == 0) {
          imageType = TYPE_INT_BGR;
        }
      } else if (((dcm2 = cm instanceof DirectColorModel ? (DirectColorModel) cm : null) != null) && raster.getTransferType() == DataBuffer.TYPE_USHORT
          && sm instanceof SinglePixelPackedSampleModel && !dcm2.hasAlpha()) {
        if (dcm2.getRedMask() == 0xF800 && dcm2.getGreenMask() == 0x07E0 && dcm2.getBlueMask() == 0x001F) {
          imageType = TYPE_USHORT_565_RGB;
        } else if (dcm2.getRedMask() == 0x7C00 && dcm2.getGreenMask() == 0x03E0 && dcm2.getBlueMask() == 0x001F) {
          imageType = TYPE_USHORT_555_RGB;
        }
      } else if (cm instanceof ComponentColorModel && raster.getTransferType() == DataBuffer.TYPE_BYTE
          && ((csm = sm instanceof ComponentSampleModel ? (ComponentSampleModel) sm : null) != null) && csm.getPixelStride() == numBands && raster.getNumDataElements() == numBands) {
        int[] offs = csm.getBandOffsets();
        if (numBands == 3 && offs[0] == 2 && offs[1] == 1 && offs[2] == 0 && !cm.hasAlpha()) {
          imageType = TYPE_3BYTE_BGR;
        } else if (numBands == 4 && offs[0] == 3 && offs[1] == 2 && offs[2] == 1 && offs[3] == 0 && cm.hasAlpha()) {
          imageType = isAlphaPre ? TYPE_4BYTE_ABGR_PRE : TYPE_4BYTE_ABGR;
        }
      }
    } else if (csType == ColorSpace.TYPE_GRAY && cm instanceof ComponentColorModel && !cm.hasAlpha()
        && ((csm2 = sm instanceof ComponentSampleModel ? (ComponentSampleModel) sm : null) != null) && csm2.getPixelStride() == 1 && numBands == 1) {
      if (raster.getTransferType() == DataBuffer.TYPE_BYTE && cm.getComponentSize(0) == 8) {
        imageType = TYPE_BYTE_GRAY;
      } else if (raster.getTransferType() == DataBuffer.TYPE_USHORT && cm.getComponentSize(0) == 16) {
        imageType = TYPE_USHORT_GRAY;
      }
    } else if (((icm = cm instanceof IndexColorModel ? (IndexColorModel) cm : null) != null) && numBands == 1 && raster.getTransferType() == DataBuffer.TYPE_BYTE) {
      int bits = icm.getPixelSize();
      if (sm instanceof MultiPixelPackedSampleModel && (bits == 1 || bits == 2 || bits == 4)) {
        imageType = TYPE_BYTE_BINARY;
      } else if (sm instanceof ComponentSampleModel && bits == 8) {
        imageType = TYPE_BYTE_INDEXED;
      }
    }
  }

  public int getType() {
    return imageType;
  }

  public ColorModel getColorModel() {
    return colorModel;
  }

  public WritableRaster getRaster() {
    return raster;
  }

  public WritableRaster getAlphaRaster() {
    return colorModel.getAlphaRaster(raster);
  }

  private int[] fastBuffer() {
    DataBufferInt db = null;
    if ((imageType == TYPE_INT_ARGB || imageType == TYPE_INT_RGB) && ((db = raster.getDataBuffer() instanceof DataBufferInt ? (DataBufferInt) raster.getDataBuffer() : null) != null)
        && raster.getSampleModel() instanceof SinglePixelPackedSampleModel) {
      return db.getData();
    }
    return null;
  }

  private int fastIndex(int x, int y) {
    SinglePixelPackedSampleModel sm = (SinglePixelPackedSampleModel) raster.getSampleModel();
    return raster.getDataBuffer().getOffset() + (y - raster.getSampleModelTranslateY()) * sm.getScanlineStride()
        + (x - raster.getSampleModelTranslateX());
  }

  public int getRGB(int x, int y) {
    int[] fast = fastBuffer();
    if (fast != null) {
      if (x < 0 || y < 0 || x >= raster.getWidth() || y >= raster.getHeight()) {
        throw new ArrayIndexOutOfBoundsException("Coordinate out of bounds!");
      }
      int v = fast[fastIndex(x, y)];
      return imageType == TYPE_INT_RGB ? v | 0xff000000 : v;
    }
    return colorModel.getRGB(raster.getDataElements(x, y, null));
  }

  public int[] getRGB(int startX, int startY, int w, int h, int[] rgbArray, int offset, int scansize) {
    int yoff = offset;
    int off;
    Object data;
    int nbands = raster.getNumBands();
    int dataType = raster.getDataBuffer().getDataType();
    switch (dataType) {
    case DataBuffer.TYPE_BYTE:
      data = new byte[nbands];
      break;
    case DataBuffer.TYPE_USHORT:
      data = new short[nbands];
      break;
    case DataBuffer.TYPE_INT:
      data = new int[nbands];
      break;
    case DataBuffer.TYPE_FLOAT:
      data = new float[nbands];
      break;
    case DataBuffer.TYPE_DOUBLE:
      data = new double[nbands];
      break;
    default:
      throw new IllegalArgumentException("Unknown data buffer type: " + dataType);
    }
    if (rgbArray == null) {
      rgbArray = new int[offset + h * scansize];
    }
    for (int y = startY; y < startY + h; y++, yoff += scansize) {
      off = yoff;
      for (int x = startX; x < startX + w; x++) {
        rgbArray[off++] = colorModel.getRGB(raster.getDataElements(x, y, data));
      }
    }
    return rgbArray;
  }

  public synchronized void setRGB(int x, int y, int rgb) {
    int[] fast = fastBuffer();
    if (fast != null) {
      if (x < 0 || y < 0 || x >= raster.getWidth() || y >= raster.getHeight()) {
        throw new ArrayIndexOutOfBoundsException("Coordinate out of bounds!");
      }
      fast[fastIndex(x, y)] = imageType == TYPE_INT_RGB ? rgb & 0x00ffffff : rgb;
      return;
    }
    raster.setDataElements(x, y, colorModel.getDataElements(rgb, null));
  }

  public void setRGB(int startX, int startY, int w, int h, int[] rgbArray, int offset, int scansize) {
    int yoff = offset;
    int off;
    Object pixel = null;
    for (int y = startY; y < startY + h; y++, yoff += scansize) {
      off = yoff;
      for (int x = startX; x < startX + w; x++) {
        pixel = colorModel.getDataElements(rgbArray[off++], pixel);
        raster.setDataElements(x, y, pixel);
      }
    }
  }

  public int getWidth() {
    return raster.getWidth();
  }

  public int getHeight() {
    return raster.getHeight();
  }

  public int getWidth(ImageObserver observer) {
    return raster.getWidth();
  }

  public int getHeight(ImageObserver observer) {
    return raster.getHeight();
  }

  public ImageProducer getSource() {
    int w = getWidth();
    int h = getHeight();
    int[] pixels = getRGB(0, 0, w, h, null, 0, w);
    MemoryImageSource source = new MemoryImageSource(w, h, ColorModel.getRGBdefault(), pixels, 0, w);
    return source;
  }

  public Object getProperty(String name, ImageObserver observer) {
    return getProperty(name);
  }

  public Object getProperty(String name) {
    if (name == null) {
      throw new NullPointerException("null property name is not allowed");
    }
    if (properties == null) {
      return Image.UndefinedProperty;
    }
    Object o = properties.get(name);
    if (o == null) {
      o = Image.UndefinedProperty;
    }
    return o;
  }

  public Graphics getGraphics() {
    return createGraphics();
  }

  public Graphics2D createGraphics() {
    return GraphicsEnvironment.getLocalGraphicsEnvironment().createGraphics(this);
  }

  public BufferedImage getSubimage(int x, int y, int w, int h) {
    return new BufferedImage(colorModel, raster.createWritableChild(x, y, w, h, 0, 0, null),
        colorModel.isAlphaPremultiplied(), properties);
  }

  public boolean isAlphaPremultiplied() {
    return colorModel.isAlphaPremultiplied();
  }

  public void coerceData(boolean isAlphaPremultiplied) {
    if (colorModel.hasAlpha() && colorModel.isAlphaPremultiplied() != isAlphaPremultiplied) {
      colorModel = colorModel.coerceData(raster, isAlphaPremultiplied);
    }
  }

  public String toString() {
    return "BufferedImage@" + Integer.toHexString(hashCode()) + ": type = " + imageType + " " + colorModel + " "
        + raster;
  }

  public Vector<RenderedImage> getSources() {
    return null;
  }

  public String[] getPropertyNames() {
    return null;
  }

  public int getMinX() {
    return raster.getMinX();
  }

  public int getMinY() {
    return raster.getMinY();
  }

  public SampleModel getSampleModel() {
    return raster.getSampleModel();
  }

  public int getNumXTiles() {
    return 1;
  }

  public int getNumYTiles() {
    return 1;
  }

  public int getMinTileX() {
    return 0;
  }

  public int getMinTileY() {
    return 0;
  }

  public int getTileWidth() {
    return raster.getWidth();
  }

  public int getTileHeight() {
    return raster.getHeight();
  }

  public int getTileGridXOffset() {
    return raster.getSampleModelTranslateX();
  }

  public int getTileGridYOffset() {
    return raster.getSampleModelTranslateY();
  }

  public Raster getTile(int tileX, int tileY) {
    if (tileX == 0 && tileY == 0) {
      return raster;
    }
    throw new ArrayIndexOutOfBoundsException("BufferedImages only have one tile with index 0,0");
  }

  public Raster getData() {
    int width = raster.getWidth();
    int height = raster.getHeight();
    int startX = raster.getMinX();
    int startY = raster.getMinY();
    WritableRaster wr = Raster.createWritableRaster(raster.getSampleModel(), new Point(raster.getSampleModelTranslateX(),
        raster.getSampleModelTranslateY()));
    Object tdata = null;
    for (int i = startY; i < startY + height; i++) {
      tdata = raster.getDataElements(startX, i, width, 1, tdata);
      wr.setDataElements(startX, i, width, 1, tdata);
    }
    return wr;
  }

  public Raster getData(Rectangle rect) {
    SampleModel sm = raster.getSampleModel();
    SampleModel nsm = sm.createCompatibleSampleModel(rect.width, rect.height);
    WritableRaster wr = Raster.createWritableRaster(nsm, rect.getLocation());
    int width = rect.width;
    int height = rect.height;
    int startX = rect.x;
    int startY = rect.y;
    Object tdata = null;
    for (int i = startY; i < startY + height; i++) {
      tdata = raster.getDataElements(startX, i, width, 1, tdata);
      wr.setDataElements(startX, i, width, 1, tdata);
    }
    return wr;
  }

  public WritableRaster copyData(WritableRaster outRaster) {
    if (outRaster == null) {
      return (WritableRaster) getData();
    }
    int width = outRaster.getWidth();
    int height = outRaster.getHeight();
    int startX = outRaster.getMinX();
    int startY = outRaster.getMinY();
    Object tdata = null;
    for (int i = startY; i < startY + height; i++) {
      tdata = raster.getDataElements(startX, i, width, 1, tdata);
      outRaster.setDataElements(startX, i, width, 1, tdata);
    }
    return outRaster;
  }

  public void setData(Raster r) {
    int width = r.getWidth();
    int height = r.getHeight();
    int startX = r.getMinX();
    int startY = r.getMinY();
    int[] tdata = null;
    Rectangle rclip = new Rectangle(startX, startY, width, height);
    Rectangle bclip = new Rectangle(0, 0, raster.getWidth(), raster.getHeight());
    Rectangle intersect = rclip.intersection(bclip);
    if (intersect.isEmpty()) {
      return;
    }
    width = intersect.width;
    height = intersect.height;
    startX = intersect.x;
    startY = intersect.y;
    for (int i = startY; i < startY + height; i++) {
      tdata = r.getPixels(startX, i, width, 1, tdata);
      raster.setPixels(startX, i, width, 1, tdata);
    }
  }

  public void addTileObserver(TileObserver to) {
    if (tileObservers == null) {
      tileObservers = new Vector<>();
    }
    tileObservers.addElement(to);
  }

  public void removeTileObserver(TileObserver to) {
    if (tileObservers != null) {
      tileObservers.removeElement(to);
    }
  }

  public boolean isTileWritable(int tileX, int tileY) {
    if (tileX == 0 && tileY == 0) {
      return true;
    }
    throw new IllegalArgumentException("Only 1 tile in image");
  }

  public Point[] getWritableTileIndices() {
    return new Point[] {new Point(0, 0)};
  }

  public boolean hasTileWriters() {
    return true;
  }

  public WritableRaster getWritableTile(int tileX, int tileY) {
    return raster;
  }

  public void releaseWritableTile(int tileX, int tileY) { }

  public int getTransparency() {
    return colorModel.getTransparency();
  }
}
