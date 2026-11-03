/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.awt.image;

public final class DataBufferFloat extends DataBuffer {
  private float[] data;
  private float[][] bankdata;

  public DataBufferFloat(int size) {
    super(TYPE_FLOAT, size);
    data = new float[size];
    bankdata = new float[1][];
    bankdata[0] = data;
  }

  public DataBufferFloat(int size, int numBanks) {
    super(TYPE_FLOAT, size, numBanks);
    bankdata = new float[numBanks][];
    for (int i = 0; i < numBanks; i++) {
      bankdata[i] = new float[size];
    }
    data = bankdata[0];
  }

  public DataBufferFloat(float[] dataArray, int size) {
    super(TYPE_FLOAT, size);
    if (dataArray == null) {
      throw new NullPointerException("dataArray is null");
    }
    data = dataArray;
    bankdata = new float[1][];
    bankdata[0] = data;
  }

  public DataBufferFloat(float[] dataArray, int size, int offset) {
    super(TYPE_FLOAT, size, 1, offset);
    if (dataArray == null) {
      throw new NullPointerException("dataArray is null");
    }
    if ((size + offset) > dataArray.length) {
      throw new IllegalArgumentException("Length of dataArray is less than size+offset.");
    }
    data = dataArray;
    bankdata = new float[1][];
    bankdata[0] = data;
  }

  public DataBufferFloat(float[][] dataArray, int size) {
    super(TYPE_FLOAT, size, dataArray.length);
    bankdata = dataArray.clone();
    data = bankdata[0];
  }

  public DataBufferFloat(float[][] dataArray, int size, int[] offsets) {
    super(TYPE_FLOAT, size, dataArray.length, offsets);
    bankdata = dataArray.clone();
    data = bankdata[0];
  }

  public float[] getData() { return data; }

  public float[] getData(int bank) { return bankdata[bank]; }

  public float[][] getBankData() { return bankdata.clone(); }

  @Override
  public int getElem(int i) { return (int) data[i + offset]; }

  @Override
  public int getElem(int bank, int i) { return (int) bankdata[bank][i + offsets[bank]]; }

  @Override
  public void setElem(int i, int val) { data[i + offset] = (float) val; }

  @Override
  public void setElem(int bank, int i, int val) { bankdata[bank][i + offsets[bank]] = (float) val; }

  @Override
  public float getElemFloat(int i) { return data[i + offset]; }

  @Override
  public float getElemFloat(int bank, int i) { return bankdata[bank][i + offsets[bank]]; }

  @Override
  public double getElemDouble(int i) { return data[i + offset]; }

  @Override
  public double getElemDouble(int bank, int i) { return bankdata[bank][i + offsets[bank]]; }

  @Override
  public void setElemFloat(int i, float val) { data[i + offset] = val; }

  @Override
  public void setElemFloat(int bank, int i, float val) { bankdata[bank][i + offsets[bank]] = val; }

  @Override
  public void setElemDouble(int i, double val) { data[i + offset] = (float) val; }

  @Override
  public void setElemDouble(int bank, int i, double val) { bankdata[bank][i + offsets[bank]] = (float) val; }
}
