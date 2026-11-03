/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.awt.image;

import java.awt.Point;
import java.awt.Rectangle;

public class Raster {
  protected SampleModel sampleModel;
  protected DataBuffer dataBuffer;
  protected int minX;
  protected int minY;
  protected int width;
  protected int height;
  protected int sampleModelTranslateX;
  protected int sampleModelTranslateY;
  protected int numBands;
  protected int numDataElements;
  protected Raster parent;

  public static WritableRaster createInterleavedRaster(int dataType, int w, int h, int bands, Point location) {
    int[] bandOffsets = new int[bands];
    for (int i = 0; i < bands; i++) {
      bandOffsets[i] = i;
    }
    return createInterleavedRaster(dataType, w, h, w * bands, bands, bandOffsets, location);
  }

  public static WritableRaster createInterleavedRaster(int dataType, int w, int h, int scanlineStride,
      int pixelStride, int[] bandOffsets, Point location) {
    if (dataType != DataBuffer.TYPE_BYTE && dataType != DataBuffer.TYPE_USHORT) {
      throw new IllegalArgumentException("Unsupported data type " + dataType);
    }
    if (w <= 0 || h <= 0) {
      throw new RasterFormatException("w and h must be > 0");
    }
    PixelInterleavedSampleModel sm = new PixelInterleavedSampleModel(dataType, w, h, pixelStride, scanlineStride,
        bandOffsets);
    return createWritableRaster(sm, sm.createDataBuffer(), location);
  }

  public static WritableRaster createInterleavedRaster(DataBuffer dataBuffer, int w, int h, int scanlineStride,
      int pixelStride, int[] bandOffsets, Point location) {
    int dataType = dataBuffer.getDataType();
    if (dataType != DataBuffer.TYPE_BYTE && dataType != DataBuffer.TYPE_USHORT) {
      throw new IllegalArgumentException("Unsupported data type " + dataType);
    }
    PixelInterleavedSampleModel sm = new PixelInterleavedSampleModel(dataType, w, h, pixelStride, scanlineStride,
        bandOffsets);
    return createWritableRaster(sm, dataBuffer, location);
  }

  public static WritableRaster createBandedRaster(int dataType, int w, int h, int bands, Point location) {
    if (bands < 1) {
      throw new ArrayIndexOutOfBoundsException("Number of bands (" + bands + ") must be greater than 0");
    }
    int[] bankIndices = new int[bands];
    int[] bandOffsets = new int[bands];
    for (int i = 0; i < bands; i++) {
      bankIndices[i] = i;
    }
    return createBandedRaster(dataType, w, h, w, bankIndices, bandOffsets, location);
  }

  public static WritableRaster createBandedRaster(int dataType, int w, int h, int scanlineStride,
      int[] bankIndices, int[] bandOffsets, Point location) {
    if (w <= 0 || h <= 0) {
      throw new RasterFormatException("w and h must be > 0");
    }
    int bands = bankIndices.length;
    if (bandOffsets.length != bands) {
      throw new ArrayIndexOutOfBoundsException("bankIndices.length != bandOffsets.length");
    }
    BandedSampleModel sm = new BandedSampleModel(dataType, w, h, scanlineStride, bankIndices, bandOffsets);
    return createWritableRaster(sm, sm.createDataBuffer(), location);
  }

  public static WritableRaster createBandedRaster(DataBuffer dataBuffer, int w, int h, int scanlineStride,
      int[] bankIndices, int[] bandOffsets, Point location) {
    BandedSampleModel sm = new BandedSampleModel(dataBuffer.getDataType(), w, h, scanlineStride, bankIndices,
        bandOffsets);
    return createWritableRaster(sm, dataBuffer, location);
  }

  public static WritableRaster createPackedRaster(int dataType, int w, int h, int[] bandMasks, Point location) {
    SinglePixelPackedSampleModel sm = new SinglePixelPackedSampleModel(dataType, w, h, bandMasks);
    return createWritableRaster(sm, sm.createDataBuffer(), location);
  }

  public static WritableRaster createPackedRaster(int dataType, int w, int h, int bands, int bitsPerBand,
      Point location) {
    if (bands <= 0) {
      throw new IllegalArgumentException("Number of bands (" + bands + ") must be greater than 0");
    }
    if (bitsPerBand <= 0) {
      throw new IllegalArgumentException("Number of bits per band (" + bitsPerBand + ") must be greater than 0");
    }
    if (bands * bitsPerBand > DataBuffer.getDataTypeSize(dataType)) {
      throw new IllegalArgumentException("bitsPerBand(" + bitsPerBand + ") * bands is greater than data type size.");
    }
    switch (dataType) {
    case DataBuffer.TYPE_BYTE:
    case DataBuffer.TYPE_USHORT:
    case DataBuffer.TYPE_INT:
      break;
    default:
      throw new IllegalArgumentException("Unsupported data type " + dataType);
    }
    if (bands > 1) {
      int[] masks = new int[bands];
      int mask = (1 << bitsPerBand) - 1;
      int shift = (bands - 1) * bitsPerBand;
      for (int i = 0; i < bands; i++) {
        masks[i] = mask << shift;
        shift -= bitsPerBand;
      }
      return createPackedRaster(dataType, w, h, masks, location);
    }
    MultiPixelPackedSampleModel sm = new MultiPixelPackedSampleModel(dataType, w, h, bitsPerBand);
    return createWritableRaster(sm, sm.createDataBuffer(), location);
  }

  public static WritableRaster createPackedRaster(DataBuffer dataBuffer, int w, int h, int bitsPerPixel,
      Point location) {
    if (dataBuffer == null) {
      throw new NullPointerException("DataBuffer cannot be null");
    }
    MultiPixelPackedSampleModel sm = new MultiPixelPackedSampleModel(dataBuffer.getDataType(), w, h, bitsPerPixel);
    return createWritableRaster(sm, dataBuffer, location);
  }

  public static WritableRaster createPackedRaster(DataBuffer dataBuffer, int w, int h, int scanlineStride,
      int[] bandMasks, Point location) {
    SinglePixelPackedSampleModel sm = new SinglePixelPackedSampleModel(dataBuffer.getDataType(), w, h,
        scanlineStride, bandMasks);
    return createWritableRaster(sm, dataBuffer, location);
  }

  public static Raster createRaster(SampleModel sm, DataBuffer db, Point location) {
    if (sm == null || db == null) {
      throw new NullPointerException("SampleModel and DataBuffer cannot be null");
    }
    if (location == null) {
      location = new Point(0, 0);
    }
    return new Raster(sm, db, location);
  }

  public static WritableRaster createWritableRaster(SampleModel sm, Point location) {
    if (location == null) {
      location = new Point(0, 0);
    }
    return createWritableRaster(sm, sm.createDataBuffer(), location);
  }

  public static WritableRaster createWritableRaster(SampleModel sm, DataBuffer db, Point location) {
    if (sm == null || db == null) {
      throw new NullPointerException("SampleModel and DataBuffer cannot be null");
    }
    if (location == null) {
      location = new Point(0, 0);
    }
    return new WritableRaster(sm, db, location);
  }

  protected Raster(SampleModel sampleModel, Point origin) {
    this(sampleModel, sampleModel.createDataBuffer(), new Rectangle(origin.x, origin.y, sampleModel.getWidth(),
        sampleModel.getHeight()), origin, null);
  }

  protected Raster(SampleModel sampleModel, DataBuffer dataBuffer, Point origin) {
    this(sampleModel, dataBuffer, new Rectangle(origin.x, origin.y, sampleModel.getWidth(), sampleModel.getHeight()),
        origin, null);
  }

  protected Raster(SampleModel sampleModel, DataBuffer dataBuffer, Rectangle aRegion, Point sampleModelTranslate,
      Raster parent) {
    if (sampleModel == null || dataBuffer == null || aRegion == null || sampleModelTranslate == null) {
      throw new NullPointerException("SampleModel, dataBuffer, aRegion and sampleModelTranslate cannot be null");
    }
    this.sampleModel = sampleModel;
    this.dataBuffer = dataBuffer;
    this.minX = aRegion.x;
    this.minY = aRegion.y;
    this.width = aRegion.width;
    this.height = aRegion.height;
    if (this.width <= 0 || this.height <= 0) {
      throw new RasterFormatException("negative or zero " + (this.width <= 0 ? "width" : "height"));
    }
    if (this.minX + this.width < this.minX) {
      throw new RasterFormatException("overflow condition for X coordinates of Raster");
    }
    if (this.minY + this.height < this.minY) {
      throw new RasterFormatException("overflow condition for Y coordinates of Raster");
    }
    this.sampleModelTranslateX = sampleModelTranslate.x;
    this.sampleModelTranslateY = sampleModelTranslate.y;
    this.numBands = sampleModel.getNumBands();
    this.numDataElements = sampleModel.getNumDataElements();
    this.parent = parent;
  }

  public Raster getParent() { return parent; }

  public final int getSampleModelTranslateX() { return sampleModelTranslateX; }

  public final int getSampleModelTranslateY() { return sampleModelTranslateY; }

  public WritableRaster createCompatibleWritableRaster() {
    return new WritableRaster(sampleModel, new Point(0, 0));
  }

  public WritableRaster createCompatibleWritableRaster(int w, int h) {
    if (w <= 0 || h <= 0) {
      throw new RasterFormatException("negative " + (w <= 0 ? "width" : "height"));
    }
    return new WritableRaster(sampleModel.createCompatibleSampleModel(w, h), new Point(0, 0));
  }

  public WritableRaster createCompatibleWritableRaster(Rectangle rect) {
    if (rect == null) {
      throw new NullPointerException("Rect cannot be null");
    }
    return createCompatibleWritableRaster(rect.x, rect.y, rect.width, rect.height);
  }

  public WritableRaster createCompatibleWritableRaster(int x, int y, int w, int h) {
    WritableRaster ret = createCompatibleWritableRaster(w, h);
    return ret.createWritableChild(0, 0, w, h, x, y, null);
  }

  public Raster createTranslatedChild(int childMinX, int childMinY) {
    return createChild(minX, minY, width, height, childMinX, childMinY, null);
  }

  public Raster createChild(int parentX, int parentY, int w, int h, int childMinX, int childMinY, int[] bandList) {
    if (parentX < this.minX) {
      throw new RasterFormatException("x lies outside raster");
    }
    if (parentY < this.minY) {
      throw new RasterFormatException("y lies outside raster");
    }
    if (parentX + w < parentX || parentX + w > this.width + this.minX) {
      throw new RasterFormatException("(x + width) is outside raster");
    }
    if (parentY + h < parentY || parentY + h > this.height + this.minY) {
      throw new RasterFormatException("(y + height) is outside raster");
    }
    SampleModel subSampleModel = bandList == null ? sampleModel : sampleModel.createSubsetSampleModel(bandList);
    int deltaX = childMinX - parentX;
    int deltaY = childMinY - parentY;
    return new Raster(subSampleModel, dataBuffer, new Rectangle(childMinX, childMinY, w, h),
        new Point(sampleModelTranslateX + deltaX, sampleModelTranslateY + deltaY), this);
  }

  public Rectangle getBounds() { return new Rectangle(minX, minY, width, height); }

  public final int getMinX() { return minX; }

  public final int getMinY() { return minY; }

  public final int getWidth() { return width; }

  public final int getHeight() { return height; }

  public final int getNumBands() { return numBands; }

  public final int getNumDataElements() { return sampleModel.getNumDataElements(); }

  public final int getTransferType() { return sampleModel.getTransferType(); }

  public DataBuffer getDataBuffer() { return dataBuffer; }

  public SampleModel getSampleModel() { return sampleModel; }

  private void check(int x, int y) {
    if (x < minX || y < minY || x >= minX + width || y >= minY + height) {
      throw new ArrayIndexOutOfBoundsException("Coordinate out of bounds!");
    }
  }

  private void checkRect(int x, int y, int w, int h) {
    if (x < minX || y < minY || w < 0 || h < 0 || x + w > minX + width || y + h > minY + height) {
      throw new ArrayIndexOutOfBoundsException("Coordinate out of bounds!");
    }
  }

  public Object getDataElements(int x, int y, Object outData) {
    check(x, y);
    return sampleModel.getDataElements(x - sampleModelTranslateX, y - sampleModelTranslateY, outData, dataBuffer);
  }

  public Object getDataElements(int x, int y, int w, int h, Object outData) {
    checkRect(x, y, w, h);
    return sampleModel.getDataElements(x - sampleModelTranslateX, y - sampleModelTranslateY, w, h, outData,
        dataBuffer);
  }

  public int[] getPixel(int x, int y, int[] iArray) {
    check(x, y);
    return sampleModel.getPixel(x - sampleModelTranslateX, y - sampleModelTranslateY, iArray, dataBuffer);
  }

  public float[] getPixel(int x, int y, float[] fArray) {
    check(x, y);
    return sampleModel.getPixel(x - sampleModelTranslateX, y - sampleModelTranslateY, fArray, dataBuffer);
  }

  public double[] getPixel(int x, int y, double[] dArray) {
    check(x, y);
    return sampleModel.getPixel(x - sampleModelTranslateX, y - sampleModelTranslateY, dArray, dataBuffer);
  }

  public int[] getPixels(int x, int y, int w, int h, int[] iArray) {
    checkRect(x, y, w, h);
    return sampleModel.getPixels(x - sampleModelTranslateX, y - sampleModelTranslateY, w, h, iArray, dataBuffer);
  }

  public float[] getPixels(int x, int y, int w, int h, float[] fArray) {
    checkRect(x, y, w, h);
    return sampleModel.getPixels(x - sampleModelTranslateX, y - sampleModelTranslateY, w, h, fArray, dataBuffer);
  }

  public double[] getPixels(int x, int y, int w, int h, double[] dArray) {
    checkRect(x, y, w, h);
    return sampleModel.getPixels(x - sampleModelTranslateX, y - sampleModelTranslateY, w, h, dArray, dataBuffer);
  }

  public int getSample(int x, int y, int b) {
    check(x, y);
    return sampleModel.getSample(x - sampleModelTranslateX, y - sampleModelTranslateY, b, dataBuffer);
  }

  public float getSampleFloat(int x, int y, int b) {
    check(x, y);
    return sampleModel.getSampleFloat(x - sampleModelTranslateX, y - sampleModelTranslateY, b, dataBuffer);
  }

  public double getSampleDouble(int x, int y, int b) {
    check(x, y);
    return sampleModel.getSampleDouble(x - sampleModelTranslateX, y - sampleModelTranslateY, b, dataBuffer);
  }

  public int[] getSamples(int x, int y, int w, int h, int b, int[] iArray) {
    checkRect(x, y, w, h);
    return sampleModel.getSamples(x - sampleModelTranslateX, y - sampleModelTranslateY, w, h, b, iArray, dataBuffer);
  }

  public float[] getSamples(int x, int y, int w, int h, int b, float[] fArray) {
    checkRect(x, y, w, h);
    return sampleModel.getSamples(x - sampleModelTranslateX, y - sampleModelTranslateY, w, h, b, fArray, dataBuffer);
  }

  public double[] getSamples(int x, int y, int w, int h, int b, double[] dArray) {
    checkRect(x, y, w, h);
    return sampleModel.getSamples(x - sampleModelTranslateX, y - sampleModelTranslateY, w, h, b, dArray, dataBuffer);
  }
}
