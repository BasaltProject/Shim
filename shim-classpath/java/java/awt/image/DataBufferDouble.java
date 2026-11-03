/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.awt.image;

public final class DataBufferDouble extends DataBuffer {
  private double[] data;
  private double[][] bankdata;

  public DataBufferDouble(int size) {
    super(TYPE_DOUBLE, size);
    data = new double[size];
    bankdata = new double[1][];
    bankdata[0] = data;
  }

  public DataBufferDouble(int size, int numBanks) {
    super(TYPE_DOUBLE, size, numBanks);
    bankdata = new double[numBanks][];
    for (int i = 0; i < numBanks; i++) {
      bankdata[i] = new double[size];
    }
    data = bankdata[0];
  }

  public DataBufferDouble(double[] dataArray, int size) {
    super(TYPE_DOUBLE, size);
    if (dataArray == null) {
      throw new NullPointerException("dataArray is null");
    }
    data = dataArray;
    bankdata = new double[1][];
    bankdata[0] = data;
  }

  public DataBufferDouble(double[] dataArray, int size, int offset) {
    super(TYPE_DOUBLE, size, 1, offset);
    if (dataArray == null) {
      throw new NullPointerException("dataArray is null");
    }
    if ((size + offset) > dataArray.length) {
      throw new IllegalArgumentException("Length of dataArray is less than size+offset.");
    }
    data = dataArray;
    bankdata = new double[1][];
    bankdata[0] = data;
  }

  public DataBufferDouble(double[][] dataArray, int size) {
    super(TYPE_DOUBLE, size, dataArray.length);
    bankdata = dataArray.clone();
    data = bankdata[0];
  }

  public DataBufferDouble(double[][] dataArray, int size, int[] offsets) {
    super(TYPE_DOUBLE, size, dataArray.length, offsets);
    bankdata = dataArray.clone();
    data = bankdata[0];
  }

  public double[] getData() { return data; }

  public double[] getData(int bank) { return bankdata[bank]; }

  public double[][] getBankData() { return bankdata.clone(); }

  @Override
  public int getElem(int i) { return (int) data[i + offset]; }

  @Override
  public int getElem(int bank, int i) { return (int) bankdata[bank][i + offsets[bank]]; }

  @Override
  public void setElem(int i, int val) { data[i + offset] = (double) val; }

  @Override
  public void setElem(int bank, int i, int val) { bankdata[bank][i + offsets[bank]] = (double) val; }

  @Override
  public double getElemDouble(int i) { return data[i + offset]; }

  @Override
  public double getElemDouble(int bank, int i) { return bankdata[bank][i + offsets[bank]]; }

  @Override
  public float getElemFloat(int i) { return (float) data[i + offset]; }

  @Override
  public float getElemFloat(int bank, int i) { return (float) bankdata[bank][i + offsets[bank]]; }

  @Override
  public void setElemDouble(int i, double val) { data[i + offset] = val; }

  @Override
  public void setElemDouble(int bank, int i, double val) { bankdata[bank][i + offsets[bank]] = val; }

  @Override
  public void setElemFloat(int i, float val) { data[i + offset] = (double) val; }

  @Override
  public void setElemFloat(int bank, int i, float val) { bankdata[bank][i + offsets[bank]] = (double) val; }
}
