/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.imageio.stream;

import java.io.IOException;
import java.io.UTFDataFormatException;
import java.nio.ByteOrder;

public abstract class ImageOutputStreamImpl extends ImageInputStreamImpl implements ImageOutputStream {
  public ImageOutputStreamImpl() { }

  public abstract void write(int b) throws IOException;

  public void write(byte[] b) throws IOException {
    write(b, 0, b.length);
  }

  public abstract void write(byte[] b, int off, int len) throws IOException;

  public void writeBoolean(boolean v) throws IOException {
    write(v ? 1 : 0);
  }

  public void writeByte(int v) throws IOException {
    write(v);
  }

  public void writeShort(int v) throws IOException {
    if (byteOrder == ByteOrder.BIG_ENDIAN) {
      write(new byte[] {(byte) (v >>> 8), (byte) v}, 0, 2);
    } else {
      write(new byte[] {(byte) v, (byte) (v >>> 8)}, 0, 2);
    }
  }

  public void writeChar(int v) throws IOException {
    writeShort(v);
  }

  public void writeInt(int v) throws IOException {
    if (byteOrder == ByteOrder.BIG_ENDIAN) {
      write(new byte[] {(byte) (v >>> 24), (byte) (v >>> 16), (byte) (v >>> 8), (byte) v}, 0, 4);
    } else {
      write(new byte[] {(byte) v, (byte) (v >>> 8), (byte) (v >>> 16), (byte) (v >>> 24)}, 0, 4);
    }
  }

  public void writeLong(long v) throws IOException {
    if (byteOrder == ByteOrder.BIG_ENDIAN) {
      writeInt((int) (v >>> 32));
      writeInt((int) v);
    } else {
      writeInt((int) v);
      writeInt((int) (v >>> 32));
    }
  }

  public void writeFloat(float v) throws IOException {
    writeInt(Float.floatToIntBits(v));
  }

  public void writeDouble(double v) throws IOException {
    writeLong(Double.doubleToLongBits(v));
  }

  public void writeBytes(String s) throws IOException {
    int len = s.length();
    for (int i = 0; i < len; i++) {
      write((byte) s.charAt(i));
    }
  }

  public void writeChars(String s) throws IOException {
    int len = s.length();
    for (int i = 0; i < len; i++) {
      writeChar(s.charAt(i));
    }
  }

  public void writeUTF(String s) throws IOException {
    int strlen = s.length();
    int utflen = 0;
    for (int i = 0; i < strlen; i++) {
      int c = s.charAt(i);
      if ((c >= 0x0001) && (c <= 0x007F)) {
        utflen++;
      } else if (c > 0x07FF) {
        utflen += 3;
      } else {
        utflen += 2;
      }
    }
    if (utflen > 65535) {
      throw new UTFDataFormatException("utflen > 65536!");
    }
    byte[] bytearr = new byte[utflen + 2];
    int count = 0;
    bytearr[count++] = (byte) ((utflen >>> 8) & 0xFF);
    bytearr[count++] = (byte) ((utflen >>> 0) & 0xFF);
    for (int i = 0; i < strlen; i++) {
      int c = s.charAt(i);
      if ((c >= 0x0001) && (c <= 0x007F)) {
        bytearr[count++] = (byte) c;
      } else if (c > 0x07FF) {
        bytearr[count++] = (byte) (0xE0 | ((c >> 12) & 0x0F));
        bytearr[count++] = (byte) (0x80 | ((c >> 6) & 0x3F));
        bytearr[count++] = (byte) (0x80 | ((c >> 0) & 0x3F));
      } else {
        bytearr[count++] = (byte) (0xC0 | ((c >> 6) & 0x1F));
        bytearr[count++] = (byte) (0x80 | ((c >> 0) & 0x3F));
      }
    }
    write(bytearr, 0, utflen + 2);
  }

  public void writeShorts(short[] s, int off, int len) throws IOException {
    for (int i = 0; i < len; i++) {
      writeShort(s[off + i]);
    }
  }

  public void writeChars(char[] c, int off, int len) throws IOException {
    for (int i = 0; i < len; i++) {
      writeChar(c[off + i]);
    }
  }

  public void writeInts(int[] i, int off, int len) throws IOException {
    for (int k = 0; k < len; k++) {
      writeInt(i[off + k]);
    }
  }

  public void writeLongs(long[] l, int off, int len) throws IOException {
    for (int k = 0; k < len; k++) {
      writeLong(l[off + k]);
    }
  }

  public void writeFloats(float[] f, int off, int len) throws IOException {
    for (int k = 0; k < len; k++) {
      writeFloat(f[off + k]);
    }
  }

  public void writeDoubles(double[] d, int off, int len) throws IOException {
    for (int k = 0; k < len; k++) {
      writeDouble(d[off + k]);
    }
  }

  public void writeBit(int bit) throws IOException {
    writeBits((1L & bit), 1);
  }

  public void writeBits(long bits, int numBits) throws IOException {
    checkClosed();
    if (numBits < 0 || numBits > 64) {
      throw new IllegalArgumentException("Bad value for numBits!");
    }
    if (numBits == 0) {
      return;
    }
    long pos = getStreamPosition();
    if (getBitOffset() > 0) {
      int offset = getBitOffset();
      int partialByte = read();
      if (partialByte != -1) {
        seek(pos);
      } else {
        partialByte = 0;
      }
      if (numBits + offset < 8) {
        int shift = 8 - (offset + numBits);
        int mask = -1 >>> (32 - numBits);
        partialByte &= ~(mask << shift);
        partialByte |= (bits & mask) << shift;
        write(partialByte);
        seek(pos);
        setBitOffset(offset + numBits);
        numBits = 0;
      } else {
        int bitsToWrite = 8 - offset;
        int mask = -1 >>> (32 - bitsToWrite);
        partialByte &= ~mask;
        partialByte |= (bits >> (numBits - bitsToWrite)) & mask;
        write(partialByte);
        numBits -= bitsToWrite;
      }
    }
    if (numBits > 7) {
      int extra = numBits % 8;
      for (int numBytes = numBits / 8; numBytes > 0; numBytes--) {
        int shift = (numBytes - 1) * 8 + extra;
        int value = (int) ((shift == 0) ? bits & 0xFF : (bits >> shift) & 0xFF);
        write(value);
      }
      numBits = extra;
    }
    if (numBits != 0) {
      int partialByte = read();
      if (partialByte != -1) {
        seek(getStreamPosition() - 1);
      } else {
        partialByte = 0;
      }
      int shift = 8 - numBits;
      int mask = -1 >>> (32 - numBits);
      partialByte &= ~(mask << shift);
      partialByte |= (bits & mask) << shift;
      write(partialByte);
      seek(getStreamPosition() - 1);
      setBitOffset(numBits);
    }
  }

  protected final void flushBits() throws IOException {
    checkClosed();
    int bitOffset = getBitOffset();
    if (bitOffset != 0) {
      int partialByte = read();
      if (partialByte < 0) {
        partialByte = 0;
        bitOffset = 0;
      } else {
        seek(getStreamPosition() - 1);
        partialByte &= -1 << (8 - bitOffset);
      }
      write(partialByte);
      setBitOffset(0);
    }
  }
}
