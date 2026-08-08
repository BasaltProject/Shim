/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.imageio.stream;

public interface ImageOutputStream extends javax.imageio.stream.ImageInputStream, java.io.DataOutput {
  void write(byte[] p0, int p1, int p2) throws java.io.IOException;
  void write(byte[] p0) throws java.io.IOException;
  void write(int p0) throws java.io.IOException;
  void writeInt(int p0) throws java.io.IOException;
  void writeUTF(java.lang.String p0) throws java.io.IOException;
  void writeLong(long p0) throws java.io.IOException;
  void writeByte(int p0) throws java.io.IOException;
  void writeShort(int p0) throws java.io.IOException;
  void writeBytes(java.lang.String p0) throws java.io.IOException;
  void writeChar(int p0) throws java.io.IOException;
  void writeFloat(float p0) throws java.io.IOException;
  void writeDouble(double p0) throws java.io.IOException;
  void writeBoolean(boolean p0) throws java.io.IOException;
  void writeChars(char[] p0, int p1, int p2) throws java.io.IOException;
  void writeChars(java.lang.String p0) throws java.io.IOException;
  void flushBefore(long p0) throws java.io.IOException;
  void writeBits(long p0, int p1) throws java.io.IOException;
  void writeBit(int p0) throws java.io.IOException;
  void writeInts(int[] p0, int p1, int p2) throws java.io.IOException;
  void writeLongs(long[] p0, int p1, int p2) throws java.io.IOException;
  void writeFloats(float[] p0, int p1, int p2) throws java.io.IOException;
  void writeDoubles(double[] p0, int p1, int p2) throws java.io.IOException;
  void writeShorts(short[] p0, int p1, int p2) throws java.io.IOException;
}
