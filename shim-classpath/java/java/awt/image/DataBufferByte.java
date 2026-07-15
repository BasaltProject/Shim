/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.awt.image;

public final class DataBufferByte extends DataBuffer {
  private byte[] data;
  private byte[][] bankdata;

  public DataBufferByte(int size) {
    super(TYPE_BYTE, size);
    data = new byte[size];
    bankdata = new byte[1][];
    bankdata[0] = data;
  }

  public DataBufferByte(int size, int numBanks) {
    super(TYPE_BYTE, size, numBanks);
    bankdata = new byte[numBanks][];
    for (int i = 0; i < numBanks; i++) {
      bankdata[i] = new byte[size];
    }
    data = bankdata[0];
  }

  public DataBufferByte(byte[] dataArray, int size) {
    super(TYPE_BYTE, size);
    if (dataArray == null) {
      throw new NullPointerException("dataArray is null");
    }
    data = dataArray;
    bankdata = new byte[1][];
    bankdata[0] = data;
  }

  public DataBufferByte(byte[] dataArray, int size, int offset) {
    super(TYPE_BYTE, size, 1, offset);
    if (dataArray == null) {
      throw new NullPointerException("dataArray is null");
    }
    if ((size + offset) > dataArray.length) {
      throw new IllegalArgumentException("Length of dataArray is less than size+offset.");
    }
    data = dataArray;
    bankdata = new byte[1][];
    bankdata[0] = data;
  }

  public DataBufferByte(byte[][] dataArray, int size) {
    super(TYPE_BYTE, size, dataArray.length);
    bankdata = dataArray.clone();
    data = bankdata[0];
  }

  public DataBufferByte(byte[][] dataArray, int size, int[] offsets) {
    super(TYPE_BYTE, size, dataArray.length, offsets);
    bankdata = dataArray.clone();
    data = bankdata[0];
  }

  public byte[] getData() { return data; }

  public byte[] getData(int bank) { return bankdata[bank]; }

  public byte[][] getBankData() { return bankdata.clone(); }

  @Override
  public int getElem(int i) { return data[i + offset] & 0xff; }

  @Override
  public int getElem(int bank, int i) { return bankdata[bank][i + offsets[bank]] & 0xff; }

  @Override
  public void setElem(int i, int val) { data[i + offset] = (byte) val; }

  @Override
  public void setElem(int bank, int i, int val) { bankdata[bank][i + offsets[bank]] = (byte) val; }
}
