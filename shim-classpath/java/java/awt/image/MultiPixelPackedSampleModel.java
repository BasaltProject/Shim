/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.awt.image;

public class MultiPixelPackedSampleModel extends SampleModel {
  private final int pixelBitStride;
  private final int bitMask;
  private final int dataBitOffset;
  private final int scanlineStride;
  private final int dataElementSize;

  public MultiPixelPackedSampleModel(int dataType, int w, int h, int numberOfBits) {
    this(dataType, w, h, numberOfBits, (w * numberOfBits + DataBuffer.getDataTypeSize(dataType) - 1)
        / DataBuffer.getDataTypeSize(dataType), 0);
    if (dataType != DataBuffer.TYPE_BYTE && dataType != DataBuffer.TYPE_USHORT && dataType != DataBuffer.TYPE_INT) {
      throw new IllegalArgumentException("Unsupported data type " + dataType);
    }
  }

  public MultiPixelPackedSampleModel(int dataType, int w, int h, int numberOfBits, int scanlineStride,
      int dataBitOffset) {
    super(dataType, w, h, 1);
    if (dataType != DataBuffer.TYPE_BYTE && dataType != DataBuffer.TYPE_USHORT && dataType != DataBuffer.TYPE_INT) {
      throw new IllegalArgumentException("Unsupported data type " + dataType);
    }
    this.dataElementSize = DataBuffer.getDataTypeSize(dataType);
    this.pixelBitStride = numberOfBits;
    if (numberOfBits <= 0 || (numberOfBits & (numberOfBits - 1)) != 0 || numberOfBits > dataElementSize) {
      throw new RasterFormatException("MultiPixelPackedSampleModel requires bits per pixel be a power of 2 <= " + dataElementSize);
    }
    if (dataBitOffset % numberOfBits != 0) {
      throw new RasterFormatException("Data Bit offset must be multiple of the pixel bit stride");
    }
    this.scanlineStride = scanlineStride;
    this.dataBitOffset = dataBitOffset;
    this.bitMask = numberOfBits == 32 ? -1 : (1 << numberOfBits) - 1;
  }

  public SampleModel createCompatibleSampleModel(int w, int h) {
    return new MultiPixelPackedSampleModel(dataType, w, h, pixelBitStride);
  }

  public DataBuffer createDataBuffer() {
    int size = scanlineStride * height;
    if ((dataBitOffset + 7) / 8 > 0 && dataBitOffset > 0) {
      size += (dataBitOffset + dataElementSize - 1) / dataElementSize;
    }
    return newBuffer(dataType, size, 1);
  }

  public int getNumDataElements() { return 1; }

  public int[] getSampleSize() { return new int[] {pixelBitStride}; }

  public int getSampleSize(int band) { return pixelBitStride; }

  public int getOffset(int x, int y) {
    int off = y * scanlineStride;
    off += (x * pixelBitStride + dataBitOffset) / dataElementSize;
    return off;
  }

  public int getBitOffset(int x) {
    return (x * pixelBitStride + dataBitOffset) % dataElementSize;
  }

  public int getScanlineStride() { return scanlineStride; }

  public int getPixelBitStride() { return pixelBitStride; }

  public int getDataBitOffset() { return dataBitOffset; }

  public int getTransferType() {
    if (pixelBitStride > 16) {
      return DataBuffer.TYPE_INT;
    } else if (pixelBitStride > 8) {
      return DataBuffer.TYPE_USHORT;
    }
    return DataBuffer.TYPE_BYTE;
  }

  public SampleModel createSubsetSampleModel(int[] bands) {
    if (bands != null && bands.length != 1) {
      throw new RasterFormatException("MultiPixelPackedSampleModel has only one band.");
    }
    return createCompatibleSampleModel(width, height);
  }

  private void check(int x, int y) {
    if (x < 0 || y < 0 || x >= width || y >= height) {
      throw new ArrayIndexOutOfBoundsException("Coordinate out of bounds!");
    }
  }

  public int getSample(int x, int y, int b, DataBuffer data) {
    check(x, y);
    int bitnum = dataBitOffset + x * pixelBitStride;
    int element = data.getElem(y * scanlineStride + bitnum / dataElementSize);
    int shift = dataElementSize - (bitnum & (dataElementSize - 1)) - pixelBitStride;
    return (element >> shift) & bitMask;
  }

  public void setSample(int x, int y, int b, int s, DataBuffer data) {
    check(x, y);
    int bitnum = dataBitOffset + x * pixelBitStride;
    int index = y * scanlineStride + bitnum / dataElementSize;
    int shift = dataElementSize - (bitnum & (dataElementSize - 1)) - pixelBitStride;
    int element = data.getElem(index);
    element &= ~(bitMask << shift);
    element |= (s & bitMask) << shift;
    data.setElem(index, element);
  }

  public Object getDataElements(int x, int y, Object obj, DataBuffer data) {
    int v = getSample(x, y, 0, data);
    switch (getTransferType()) {
    case DataBuffer.TYPE_BYTE: {
      byte[] a = obj != null ? (byte[]) obj : new byte[1];
      a[0] = (byte) v;
      return a;
    }
    case DataBuffer.TYPE_USHORT: {
      short[] a = obj != null ? (short[]) obj : new short[1];
      a[0] = (short) v;
      return a;
    }
    default: {
      int[] a = obj != null ? (int[]) obj : new int[1];
      a[0] = v;
      return a;
    }
    }
  }

  public void setDataElements(int x, int y, Object obj, DataBuffer data) {
    int v;
    switch (getTransferType()) {
    case DataBuffer.TYPE_BYTE:
      v = ((byte[]) obj)[0] & 0xff;
      break;
    case DataBuffer.TYPE_USHORT:
      v = ((short[]) obj)[0] & 0xffff;
      break;
    default:
      v = ((int[]) obj)[0];
      break;
    }
    setSample(x, y, 0, v, data);
  }

  public int[] getPixel(int x, int y, int[] iArray, DataBuffer data) {
    int[] out = iArray != null ? iArray : new int[1];
    out[0] = getSample(x, y, 0, data);
    return out;
  }

  public void setPixel(int x, int y, int[] iArray, DataBuffer data) {
    setSample(x, y, 0, iArray[0], data);
  }

  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    MultiPixelPackedSampleModel that = null;
    if (!(((that = o instanceof MultiPixelPackedSampleModel ? (MultiPixelPackedSampleModel) o : null) != null))) {
      return false;
    }
    return width == that.width && height == that.height && numBands == that.numBands && dataType == that.dataType
        && pixelBitStride == that.pixelBitStride && bitMask == that.bitMask && dataBitOffset == that.dataBitOffset
        && scanlineStride == that.scanlineStride;
  }

  public int hashCode() {
    int h = width;
    h <<= 8;
    h ^= height;
    h <<= 8;
    h ^= numBands;
    h <<= 8;
    h ^= dataType;
    h <<= 8;
    h ^= pixelBitStride;
    h <<= 8;
    h ^= bitMask;
    h <<= 8;
    h ^= dataBitOffset;
    h <<= 8;
    h ^= scanlineStride;
    return h;
  }
}
