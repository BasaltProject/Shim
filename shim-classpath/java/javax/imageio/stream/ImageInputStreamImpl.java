/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.imageio.stream;

import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteOrder;
import java.util.Stack;

public abstract class ImageInputStreamImpl implements ImageInputStream {
  private Stack<Long> markByteStack = new Stack<>();
  private Stack<Integer> markBitStack = new Stack<>();
  private boolean isClosed = false;
  private final byte[] byteBuf = new byte[8];

  protected ByteOrder byteOrder = ByteOrder.BIG_ENDIAN;
  protected long streamPos;
  protected int bitOffset;
  protected long flushedPos = 0;

  public ImageInputStreamImpl() { }

  protected final void checkClosed() throws IOException {
    if (isClosed) {
      throw new IOException("closed");
    }
  }

  public void setByteOrder(ByteOrder byteOrder) {
    this.byteOrder = byteOrder;
  }

  public ByteOrder getByteOrder() {
    return byteOrder;
  }

  public abstract int read() throws IOException;

  public int read(byte[] b) throws IOException {
    return read(b, 0, b.length);
  }

  public abstract int read(byte[] b, int off, int len) throws IOException;

  public void readBytes(IIOByteBuffer buf, int len) throws IOException {
    if (len < 0) {
      throw new IndexOutOfBoundsException("len < 0!");
    }
    if (buf == null) {
      throw new NullPointerException("buf == null!");
    }
    byte[] data = new byte[len];
    len = read(data, 0, len);
    buf.setData(data);
    buf.setOffset(0);
    buf.setLength(len);
  }

  public boolean readBoolean() throws IOException {
    int ch = this.read();
    if (ch < 0) {
      throw new EOFException();
    }
    return (ch != 0);
  }

  public byte readByte() throws IOException {
    int ch = this.read();
    if (ch < 0) {
      throw new EOFException();
    }
    return (byte) ch;
  }

  public int readUnsignedByte() throws IOException {
    int ch = this.read();
    if (ch < 0) {
      throw new EOFException();
    }
    return ch;
  }

  public short readShort() throws IOException {
    if (read(byteBuf, 0, 2) != 2) {
      throw new EOFException();
    }
    if (byteOrder == ByteOrder.BIG_ENDIAN) {
      return (short) (((byteBuf[0] & 0xff) << 8) | (byteBuf[1] & 0xff));
    }
    return (short) (((byteBuf[1] & 0xff) << 8) | (byteBuf[0] & 0xff));
  }

  public int readUnsignedShort() throws IOException {
    return ((int) readShort()) & 0xffff;
  }

  public char readChar() throws IOException {
    return (char) readShort();
  }

  public int readInt() throws IOException {
    if (read(byteBuf, 0, 4) != 4) {
      throw new EOFException();
    }
    if (byteOrder == ByteOrder.BIG_ENDIAN) {
      return (((byteBuf[0] & 0xff) << 24) | ((byteBuf[1] & 0xff) << 16) | ((byteBuf[2] & 0xff) << 8)
          | ((byteBuf[3] & 0xff) << 0));
    }
    return (((byteBuf[3] & 0xff) << 24) | ((byteBuf[2] & 0xff) << 16) | ((byteBuf[1] & 0xff) << 8)
        | ((byteBuf[0] & 0xff) << 0));
  }

  public long readUnsignedInt() throws IOException {
    return ((long) readInt()) & 0xffffffffL;
  }

  public long readLong() throws IOException {
    int i1 = readInt();
    int i2 = readInt();
    if (byteOrder == ByteOrder.BIG_ENDIAN) {
      return ((long) i1 << 32) + (i2 & 0xFFFFFFFFL);
    }
    return ((long) i2 << 32) + (i1 & 0xFFFFFFFFL);
  }

  public float readFloat() throws IOException {
    return Float.intBitsToFloat(readInt());
  }

  public double readDouble() throws IOException {
    return Double.longBitsToDouble(readLong());
  }

  public String readLine() throws IOException {
    StringBuilder input = new StringBuilder();
    int c = -1;
    boolean eol = false;
    while (!eol) {
      switch (c = read()) {
      case -1:
      case '\n':
        eol = true;
        break;
      case '\r':
        eol = true;
        long cur = getStreamPosition();
        if ((read()) != '\n') {
          seek(cur);
        }
        break;
      default:
        input.append((char) c);
        break;
      }
    }
    if ((c == -1) && (input.length() == 0)) {
      return null;
    }
    return input.toString();
  }

  public String readUTF() throws IOException {
    this.bitOffset = 0;
    ByteOrder oldByteOrder = getByteOrder();
    setByteOrder(ByteOrder.BIG_ENDIAN);
    String ret;
    try {
      ret = java.io.DataInputStream.readUTF(this);
    } catch (IOException e) {
      setByteOrder(oldByteOrder);
      throw e;
    }
    setByteOrder(oldByteOrder);
    return ret;
  }

  public void readFully(byte[] b, int off, int len) throws IOException {
    if (off < 0 || len < 0 || off + len > b.length || off + len < 0) {
      throw new IndexOutOfBoundsException("off < 0 || len < 0 || off + len > b.length || off + len < 0!");
    }
    while (len > 0) {
      int nbytes = read(b, off, len);
      if (nbytes == -1) {
        throw new EOFException();
      }
      off += nbytes;
      len -= nbytes;
    }
  }

  public void readFully(byte[] b) throws IOException {
    readFully(b, 0, b.length);
  }

  public void readFully(short[] s, int off, int len) throws IOException {
    for (int i = 0; i < len; i++) {
      s[off + i] = readShort();
    }
  }

  public void readFully(char[] c, int off, int len) throws IOException {
    for (int i = 0; i < len; i++) {
      c[off + i] = readChar();
    }
  }

  public void readFully(int[] i, int off, int len) throws IOException {
    for (int k = 0; k < len; k++) {
      i[off + k] = readInt();
    }
  }

  public void readFully(long[] l, int off, int len) throws IOException {
    for (int k = 0; k < len; k++) {
      l[off + k] = readLong();
    }
  }

  public void readFully(float[] f, int off, int len) throws IOException {
    for (int k = 0; k < len; k++) {
      f[off + k] = readFloat();
    }
  }

  public void readFully(double[] d, int off, int len) throws IOException {
    for (int k = 0; k < len; k++) {
      d[off + k] = readDouble();
    }
  }

  public long getStreamPosition() throws IOException {
    checkClosed();
    return streamPos;
  }

  public int getBitOffset() throws IOException {
    checkClosed();
    return bitOffset;
  }

  public void setBitOffset(int bitOffset) throws IOException {
    checkClosed();
    if (bitOffset < 0 || bitOffset > 7) {
      throw new IllegalArgumentException("bitOffset must be betwwen 0 and 7!");
    }
    this.bitOffset = bitOffset;
  }

  public int readBit() throws IOException {
    checkClosed();
    int newBitOffset = (this.bitOffset + 1) & 0x7;
    int val = read();
    if (val == -1) {
      throw new EOFException();
    }
    if (newBitOffset != 0) {
      val >>= 8 - newBitOffset;
      seek(getStreamPosition() - 1);
    }
    this.bitOffset = newBitOffset;
    return val & 0x1;
  }

  public long readBits(int numBits) throws IOException {
    checkClosed();
    if (numBits < 0 || numBits > 64) {
      throw new IllegalArgumentException();
    }
    if (numBits == 0) {
      return 0L;
    }
    int bitsToRead = numBits + bitOffset;
    int newBitOffset = (this.bitOffset + numBits) & 0x7;
    long accum = 0L;
    int bytesToRead = (bitsToRead + 7) / 8;
    for (int i = 0; i < bytesToRead; i++) {
      int val = read();
      if (val == -1) {
        throw new EOFException();
      }
      accum = (accum << 8) | val;
    }
    if (newBitOffset != 0) {
      seek(getStreamPosition() - 1);
    }
    accum >>= (bytesToRead * 8 - bitsToRead);
    if (numBits < 64) {
      accum &= (1L << numBits) - 1;
    }
    this.bitOffset = newBitOffset;
    return accum;
  }

  public long length() {
    return -1L;
  }

  public int skipBytes(int n) throws IOException {
    long pos = getStreamPosition();
    seek(pos + n);
    return (int) (getStreamPosition() - pos);
  }

  public long skipBytes(long n) throws IOException {
    long pos = getStreamPosition();
    seek(pos + n);
    return getStreamPosition() - pos;
  }

  public void seek(long pos) throws IOException {
    checkClosed();
    if (pos < flushedPos) {
      throw new IndexOutOfBoundsException("pos < flushedPos!");
    }
    this.streamPos = pos;
    this.bitOffset = 0;
  }

  public void mark() {
    try {
      markByteStack.push(Long.valueOf(getStreamPosition()));
      markBitStack.push(Integer.valueOf(getBitOffset()));
    } catch (IOException e) {
      // ignore
    }
  }

  public void reset() throws IOException {
    if (markByteStack.empty()) {
      return;
    }
    long pos = markByteStack.pop().longValue();
    seek(pos);
    int offset = markBitStack.pop().intValue();
    setBitOffset(offset);
  }

  public void flushBefore(long pos) throws IOException {
    checkClosed();
    if (pos < flushedPos) {
      throw new IndexOutOfBoundsException("pos < flushedPos!");
    }
    if (pos > getStreamPosition()) {
      throw new IndexOutOfBoundsException("pos > getStreamPosition()!");
    }
    flushedPos = pos;
  }

  public void flush() throws IOException {
    flushBefore(getStreamPosition());
  }

  public long getFlushedPosition() {
    return flushedPos;
  }

  public boolean isCached() {
    return false;
  }

  public boolean isCachedMemory() {
    return false;
  }

  public boolean isCachedFile() {
    return false;
  }

  public void close() throws IOException {
    checkClosed();
    isClosed = true;
  }

  protected void finalize() throws Throwable {
    if (!isClosed) {
      try {
        close();
      } catch (IOException e) {
        // ignore
      }
    }
    super.finalize();
  }
}
