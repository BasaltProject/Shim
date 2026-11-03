/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.awt.image;

public final class DataBufferUShort extends DataBuffer {
  private short[] data;
  private short[][] bankdata;

  public DataBufferUShort(int size) {
    super(TYPE_USHORT, size);
    data = new short[size];
    bankdata = new short[1][];
    bankdata[0] = data;
  }

  public DataBufferUShort(int size, int numBanks) {
    super(TYPE_USHORT, size, numBanks);
    bankdata = new short[numBanks][];
    for (int i = 0; i < numBanks; i++) {
      bankdata[i] = new short[size];
    }
    data = bankdata[0];
  }

  public DataBufferUShort(short[] dataArray, int size) {
    super(TYPE_USHORT, size);
    if (dataArray == null) {
      throw new NullPointerException("dataArray is null");
    }
    data = dataArray;
    bankdata = new short[1][];
    bankdata[0] = data;
  }

  public DataBufferUShort(short[] dataArray, int size, int offset) {
    super(TYPE_USHORT, size, 1, offset);
    if (dataArray == null) {
      throw new NullPointerException("dataArray is null");
    }
    if ((size + offset) > dataArray.length) {
      throw new IllegalArgumentException("Length of dataArray is less than size+offset.");
    }
    data = dataArray;
    bankdata = new short[1][];
    bankdata[0] = data;
  }

  public DataBufferUShort(short[][] dataArray, int size) {
    super(TYPE_USHORT, size, dataArray.length);
    bankdata = dataArray.clone();
    data = bankdata[0];
  }

  public DataBufferUShort(short[][] dataArray, int size, int[] offsets) {
    super(TYPE_USHORT, size, dataArray.length, offsets);
    bankdata = dataArray.clone();
    data = bankdata[0];
  }

  public short[] getData() { return data; }

  public short[] getData(int bank) { return bankdata[bank]; }

  public short[][] getBankData() { return bankdata.clone(); }

  @Override
  public int getElem(int i) { return data[i + offset] & 0xffff; }

  @Override
  public int getElem(int bank, int i) { return bankdata[bank][i + offsets[bank]] & 0xffff; }

  @Override
  public void setElem(int i, int val) { data[i + offset] = (short) val; }

  @Override
  public void setElem(int bank, int i, int val) { bankdata[bank][i + offsets[bank]] = (short) val; }
}
