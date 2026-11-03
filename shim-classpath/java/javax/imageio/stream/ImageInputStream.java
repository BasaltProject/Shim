/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.imageio.stream;

public interface ImageInputStream extends java.io.DataInput, java.io.Closeable {
  void reset() throws java.io.IOException;
  long length() throws java.io.IOException;
  void flush() throws java.io.IOException;
  java.lang.String readLine() throws java.io.IOException;
  void close() throws java.io.IOException;
  int readInt() throws java.io.IOException;
  void mark();
  int read(byte[] p0) throws java.io.IOException;
  int read() throws java.io.IOException;
  int read(byte[] p0, int p1, int p2) throws java.io.IOException;
  java.lang.String readUTF() throws java.io.IOException;
  long readLong() throws java.io.IOException;
  byte readByte() throws java.io.IOException;
  short readShort() throws java.io.IOException;
  void readBytes(javax.imageio.stream.IIOByteBuffer p0, int p1) throws java.io.IOException;
  int readUnsignedShort() throws java.io.IOException;
  char readChar() throws java.io.IOException;
  float readFloat() throws java.io.IOException;
  boolean isCached();
  double readDouble() throws java.io.IOException;
  void readFully(int[] p0, int p1, int p2) throws java.io.IOException;
  void readFully(long[] p0, int p1, int p2) throws java.io.IOException;
  void readFully(float[] p0, int p1, int p2) throws java.io.IOException;
  void readFully(double[] p0, int p1, int p2) throws java.io.IOException;
  void readFully(byte[] p0, int p1, int p2) throws java.io.IOException;
  void readFully(byte[] p0) throws java.io.IOException;
  void readFully(short[] p0, int p1, int p2) throws java.io.IOException;
  void readFully(char[] p0, int p1, int p2) throws java.io.IOException;
  long skipBytes(long p0) throws java.io.IOException;
  int skipBytes(int p0) throws java.io.IOException;
  boolean readBoolean() throws java.io.IOException;
  int readUnsignedByte() throws java.io.IOException;
  void seek(long p0) throws java.io.IOException;
  java.nio.ByteOrder getByteOrder();
  int getBitOffset() throws java.io.IOException;
  boolean isCachedFile();
  boolean isCachedMemory();
  long getStreamPosition() throws java.io.IOException;
  void setByteOrder(java.nio.ByteOrder p0);
  void setBitOffset(int p0) throws java.io.IOException;
  void flushBefore(long p0) throws java.io.IOException;
  long readUnsignedInt() throws java.io.IOException;
  int readBit() throws java.io.IOException;
  long readBits(int p0) throws java.io.IOException;
  long getFlushedPosition();
}
