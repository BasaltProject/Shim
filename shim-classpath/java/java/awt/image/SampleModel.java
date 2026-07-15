/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.awt.image;

public abstract class SampleModel {
  protected int width;
  protected int height;
  protected int numBands;
  protected int dataType;

  public SampleModel(int dataType, int w, int h, int numBands) {
    long size = (long) w * h;
    if (w <= 0 || h <= 0) {
      throw new IllegalArgumentException("Width (" + w + ") and height (" + h + ") must be > 0");
    }
    if (size >= Integer.MAX_VALUE) {
      throw new IllegalArgumentException("Dimensions (width=" + w + " height=" + h + ") are too large");
    }
    if (dataType < DataBuffer.TYPE_BYTE || dataType > DataBuffer.TYPE_DOUBLE && dataType != DataBuffer.TYPE_UNDEFINED) {
      throw new IllegalArgumentException("Unsupported dataType: " + dataType);
    }
    if (numBands <= 0) {
      throw new IllegalArgumentException("Number of bands must be > 0");
    }
    this.dataType = dataType;
    this.width = w;
    this.height = h;
    this.numBands = numBands;
  }

  public final int getWidth() { return width; }

  public final int getHeight() { return height; }

  public final int getNumBands() { return numBands; }

  public abstract int getNumDataElements();

  public final int getDataType() { return dataType; }

  public int getTransferType() { return dataType; }

  public abstract int[] getSampleSize();

  public abstract int getSampleSize(int band);

  public abstract SampleModel createCompatibleSampleModel(int w, int h);

  public abstract SampleModel createSubsetSampleModel(int[] bands);

  public abstract DataBuffer createDataBuffer();

  public abstract Object getDataElements(int x, int y, Object obj, DataBuffer data);

  public abstract void setDataElements(int x, int y, Object obj, DataBuffer data);

  public abstract int getSample(int x, int y, int b, DataBuffer data);

  public abstract void setSample(int x, int y, int b, int s, DataBuffer data);

  private void check(int x, int y) {
    if (x < 0 || y < 0 || x >= width || y >= height) {
      throw new ArrayIndexOutOfBoundsException("Coordinate out of bounds!");
    }
  }

  private void checkRect(int x, int y, int w, int h) {
    if (x < 0 || y < 0 || x + w > width || y + h > height || w < 0 || h < 0) {
      throw new ArrayIndexOutOfBoundsException("Coordinate out of bounds!");
    }
  }

  private Object newElements(int n) {
    switch (getTransferType()) {
    case DataBuffer.TYPE_BYTE:
      return new byte[n];
    case DataBuffer.TYPE_USHORT:
    case DataBuffer.TYPE_SHORT:
      return new short[n];
    case DataBuffer.TYPE_INT:
      return new int[n];
    case DataBuffer.TYPE_FLOAT:
      return new float[n];
    case DataBuffer.TYPE_DOUBLE:
      return new double[n];
    default:
      throw new IllegalArgumentException("Unsupported transfer type");
    }
  }

  public Object getDataElements(int x, int y, int w, int h, Object obj, DataBuffer data) {
    checkRect(x, y, w, h);
    int nd = getNumDataElements();
    Object pixel = null;
    Object out = obj != null ? obj : newElements(nd * w * h);
    int k = 0;
    for (int j = y; j < y + h; j++) {
      for (int i = x; i < x + w; i++) {
        pixel = getDataElements(i, j, pixel, data);
        System.arraycopy(pixel, 0, out, k, nd);
        k += nd;
      }
    }
    return out;
  }

  public void setDataElements(int x, int y, int w, int h, Object obj, DataBuffer data) {
    checkRect(x, y, w, h);
    int nd = getNumDataElements();
    Object pixel = newElements(nd);
    int k = 0;
    for (int j = y; j < y + h; j++) {
      for (int i = x; i < x + w; i++) {
        System.arraycopy(obj, k, pixel, 0, nd);
        setDataElements(i, j, pixel, data);
        k += nd;
      }
    }
  }

  public int[] getPixel(int x, int y, int[] iArray, DataBuffer data) {
    check(x, y);
    int[] out = iArray != null ? iArray : new int[numBands];
    for (int b = 0; b < numBands; b++) {
      out[b] = getSample(x, y, b, data);
    }
    return out;
  }

  public float[] getPixel(int x, int y, float[] fArray, DataBuffer data) {
    check(x, y);
    float[] out = fArray != null ? fArray : new float[numBands];
    for (int b = 0; b < numBands; b++) {
      out[b] = getSampleFloat(x, y, b, data);
    }
    return out;
  }

  public double[] getPixel(int x, int y, double[] dArray, DataBuffer data) {
    check(x, y);
    double[] out = dArray != null ? dArray : new double[numBands];
    for (int b = 0; b < numBands; b++) {
      out[b] = getSampleDouble(x, y, b, data);
    }
    return out;
  }

  public int[] getPixels(int x, int y, int w, int h, int[] iArray, DataBuffer data) {
    checkRect(x, y, w, h);
    int[] out = iArray != null ? iArray : new int[numBands * w * h];
    int k = 0;
    for (int j = y; j < y + h; j++) {
      for (int i = x; i < x + w; i++) {
        for (int b = 0; b < numBands; b++) {
          out[k++] = getSample(i, j, b, data);
        }
      }
    }
    return out;
  }

  public float[] getPixels(int x, int y, int w, int h, float[] fArray, DataBuffer data) {
    checkRect(x, y, w, h);
    float[] out = fArray != null ? fArray : new float[numBands * w * h];
    int k = 0;
    for (int j = y; j < y + h; j++) {
      for (int i = x; i < x + w; i++) {
        for (int b = 0; b < numBands; b++) {
          out[k++] = getSampleFloat(i, j, b, data);
        }
      }
    }
    return out;
  }

  public double[] getPixels(int x, int y, int w, int h, double[] dArray, DataBuffer data) {
    checkRect(x, y, w, h);
    double[] out = dArray != null ? dArray : new double[numBands * w * h];
    int k = 0;
    for (int j = y; j < y + h; j++) {
      for (int i = x; i < x + w; i++) {
        for (int b = 0; b < numBands; b++) {
          out[k++] = getSampleDouble(i, j, b, data);
        }
      }
    }
    return out;
  }

  public float getSampleFloat(int x, int y, int b, DataBuffer data) {
    return (float) getSample(x, y, b, data);
  }

  public double getSampleDouble(int x, int y, int b, DataBuffer data) {
    return (double) getSample(x, y, b, data);
  }

  public int[] getSamples(int x, int y, int w, int h, int b, int[] iArray, DataBuffer data) {
    checkRect(x, y, w, h);
    int[] out = iArray != null ? iArray : new int[w * h];
    int k = 0;
    for (int j = y; j < y + h; j++) {
      for (int i = x; i < x + w; i++) {
        out[k++] = getSample(i, j, b, data);
      }
    }
    return out;
  }

  public float[] getSamples(int x, int y, int w, int h, int b, float[] fArray, DataBuffer data) {
    checkRect(x, y, w, h);
    float[] out = fArray != null ? fArray : new float[w * h];
    int k = 0;
    for (int j = y; j < y + h; j++) {
      for (int i = x; i < x + w; i++) {
        out[k++] = getSampleFloat(i, j, b, data);
      }
    }
    return out;
  }

  public double[] getSamples(int x, int y, int w, int h, int b, double[] dArray, DataBuffer data) {
    checkRect(x, y, w, h);
    double[] out = dArray != null ? dArray : new double[w * h];
    int k = 0;
    for (int j = y; j < y + h; j++) {
      for (int i = x; i < x + w; i++) {
        out[k++] = getSampleDouble(i, j, b, data);
      }
    }
    return out;
  }

  public void setPixel(int x, int y, int[] iArray, DataBuffer data) {
    check(x, y);
    for (int b = 0; b < numBands; b++) {
      setSample(x, y, b, iArray[b], data);
    }
  }

  public void setPixel(int x, int y, float[] fArray, DataBuffer data) {
    check(x, y);
    for (int b = 0; b < numBands; b++) {
      setSample(x, y, b, fArray[b], data);
    }
  }

  public void setPixel(int x, int y, double[] dArray, DataBuffer data) {
    check(x, y);
    for (int b = 0; b < numBands; b++) {
      setSample(x, y, b, dArray[b], data);
    }
  }

  public void setPixels(int x, int y, int w, int h, int[] iArray, DataBuffer data) {
    checkRect(x, y, w, h);
    int k = 0;
    for (int j = y; j < y + h; j++) {
      for (int i = x; i < x + w; i++) {
        for (int b = 0; b < numBands; b++) {
          setSample(i, j, b, iArray[k++], data);
        }
      }
    }
  }

  public void setPixels(int x, int y, int w, int h, float[] fArray, DataBuffer data) {
    checkRect(x, y, w, h);
    int k = 0;
    for (int j = y; j < y + h; j++) {
      for (int i = x; i < x + w; i++) {
        for (int b = 0; b < numBands; b++) {
          setSample(i, j, b, fArray[k++], data);
        }
      }
    }
  }

  public void setPixels(int x, int y, int w, int h, double[] dArray, DataBuffer data) {
    checkRect(x, y, w, h);
    int k = 0;
    for (int j = y; j < y + h; j++) {
      for (int i = x; i < x + w; i++) {
        for (int b = 0; b < numBands; b++) {
          setSample(i, j, b, dArray[k++], data);
        }
      }
    }
  }

  public void setSample(int x, int y, int b, float s, DataBuffer data) {
    setSample(x, y, b, (int) s, data);
  }

  public void setSample(int x, int y, int b, double s, DataBuffer data) {
    setSample(x, y, b, (int) s, data);
  }

  public void setSamples(int x, int y, int w, int h, int b, int[] iArray, DataBuffer data) {
    checkRect(x, y, w, h);
    int k = 0;
    for (int j = y; j < y + h; j++) {
      for (int i = x; i < x + w; i++) {
        setSample(i, j, b, iArray[k++], data);
      }
    }
  }

  public void setSamples(int x, int y, int w, int h, int b, float[] fArray, DataBuffer data) {
    checkRect(x, y, w, h);
    int k = 0;
    for (int j = y; j < y + h; j++) {
      for (int i = x; i < x + w; i++) {
        setSample(i, j, b, fArray[k++], data);
      }
    }
  }

  public void setSamples(int x, int y, int w, int h, int b, double[] dArray, DataBuffer data) {
    checkRect(x, y, w, h);
    int k = 0;
    for (int j = y; j < y + h; j++) {
      for (int i = x; i < x + w; i++) {
        setSample(i, j, b, dArray[k++], data);
      }
    }
  }

  static int typeBits(int dataType) {
    return DataBuffer.getDataTypeSize(dataType);
  }

  static DataBuffer newBuffer(int dataType, int size, int banks) {
    switch (dataType) {
    case DataBuffer.TYPE_BYTE:
      return new DataBufferByte(size, banks);
    case DataBuffer.TYPE_USHORT:
      return new DataBufferUShort(size, banks);
    case DataBuffer.TYPE_SHORT:
      return new DataBufferShort(size, banks);
    case DataBuffer.TYPE_INT:
      return new DataBufferInt(size, banks);
    case DataBuffer.TYPE_FLOAT:
      return new DataBufferFloat(size, banks);
    case DataBuffer.TYPE_DOUBLE:
      return new DataBufferDouble(size, banks);
    default:
      throw new IllegalArgumentException("Unsupported data type " + dataType);
    }
  }

  static Object elementsFor(int dataType, int n) {
    switch (dataType) {
    case DataBuffer.TYPE_BYTE:
      return new byte[n];
    case DataBuffer.TYPE_USHORT:
    case DataBuffer.TYPE_SHORT:
      return new short[n];
    case DataBuffer.TYPE_INT:
      return new int[n];
    case DataBuffer.TYPE_FLOAT:
      return new float[n];
    case DataBuffer.TYPE_DOUBLE:
      return new double[n];
    default:
      throw new ClassCastException("Unsupported data type " + dataType);
    }
  }

  static void putElement(int dataType, Object arr, int i, DataBuffer data, int bank, int index) {
    switch (dataType) {
    case DataBuffer.TYPE_BYTE:
      ((byte[]) arr)[i] = (byte) data.getElem(bank, index);
      break;
    case DataBuffer.TYPE_USHORT:
    case DataBuffer.TYPE_SHORT:
      ((short[]) arr)[i] = (short) data.getElem(bank, index);
      break;
    case DataBuffer.TYPE_INT:
      ((int[]) arr)[i] = data.getElem(bank, index);
      break;
    case DataBuffer.TYPE_FLOAT:
      ((float[]) arr)[i] = data.getElemFloat(bank, index);
      break;
    default:
      ((double[]) arr)[i] = data.getElemDouble(bank, index);
      break;
    }
  }

  static void takeElement(int dataType, Object arr, int i, DataBuffer data, int bank, int index) {
    switch (dataType) {
    case DataBuffer.TYPE_BYTE:
      data.setElem(bank, index, ((byte[]) arr)[i] & 0xff);
      break;
    case DataBuffer.TYPE_USHORT:
      data.setElem(bank, index, ((short[]) arr)[i] & 0xffff);
      break;
    case DataBuffer.TYPE_SHORT:
      data.setElem(bank, index, ((short[]) arr)[i]);
      break;
    case DataBuffer.TYPE_INT:
      data.setElem(bank, index, ((int[]) arr)[i]);
      break;
    case DataBuffer.TYPE_FLOAT:
      data.setElemFloat(bank, index, ((float[]) arr)[i]);
      break;
    default:
      data.setElemDouble(bank, index, ((double[]) arr)[i]);
      break;
    }
  }
}
