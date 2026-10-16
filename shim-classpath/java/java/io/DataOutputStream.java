/*
 * Based on Avian (https://github.com/ReadyTalk/avian).
 * Original work Copyright (c) 2008-2015, Avian Contributors.
 * Original work licensed under ISC; see LICENSE.txt:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE.txt
 *
 * Modifications Copyright (c) 2026 BasaltMC.
 * Modifications licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: ISC AND MIT
 */

/* Copyright (c) 2008-2015, Avian Contributors

   Permission to use, copy, modify, and/or distribute this software
   for any purpose with or without fee is hereby granted, provided
   that the above copyright notice and this permission notice appear
   in all copies.

   There is NO WARRANTY for this software.  See license.txt for
   details. */

package java.io;

public class DataOutputStream extends FilterOutputStream implements DataOutput {
  protected int written;
  private byte[] bytearr = null;
  private final byte[] writeBuffer = new byte[8];

  public DataOutputStream(OutputStream out) {
    super(out);
  }

  private void incCount(int value) {
    int temp = written + value;
    if (temp < 0) {
      temp = Integer.MAX_VALUE;
    }
    written = temp;
  }

  public synchronized void write(int b) throws IOException {
    out.write(b);
    incCount(1);
  }

  public synchronized void write(byte[] b, int off, int len) throws IOException {
    out.write(b, off, len);
    incCount(len);
  }

  public void flush() throws IOException {
    out.flush();
  }

  public final void writeBoolean(boolean v) throws IOException {
    out.write(v ? 1 : 0);
    incCount(1);
  }

  public final void writeByte(int v) throws IOException {
    out.write(v);
    incCount(1);
  }

  public final void writeShort(int v) throws IOException {
    writeBuffer[0] = (byte) (v >>> 8);
    writeBuffer[1] = (byte) v;
    out.write(writeBuffer, 0, 2);
    incCount(2);
  }

  public final void writeChar(int v) throws IOException {
    writeBuffer[0] = (byte) (v >>> 8);
    writeBuffer[1] = (byte) v;
    out.write(writeBuffer, 0, 2);
    incCount(2);
  }

  public final void writeInt(int v) throws IOException {
    writeBuffer[0] = (byte) (v >>> 24);
    writeBuffer[1] = (byte) (v >>> 16);
    writeBuffer[2] = (byte) (v >>> 8);
    writeBuffer[3] = (byte) v;
    out.write(writeBuffer, 0, 4);
    incCount(4);
  }

  public final void writeLong(long v) throws IOException {
    writeBuffer[0] = (byte) (v >>> 56);
    writeBuffer[1] = (byte) (v >>> 48);
    writeBuffer[2] = (byte) (v >>> 40);
    writeBuffer[3] = (byte) (v >>> 32);
    writeBuffer[4] = (byte) (v >>> 24);
    writeBuffer[5] = (byte) (v >>> 16);
    writeBuffer[6] = (byte) (v >>> 8);
    writeBuffer[7] = (byte) v;
    out.write(writeBuffer, 0, 8);
    incCount(8);
  }

  public final void writeFloat(float v) throws IOException {
    writeInt(Float.floatToIntBits(v));
  }

  public final void writeDouble(double v) throws IOException {
    writeLong(Double.doubleToLongBits(v));
  }

  public final void writeBytes(String s) throws IOException {
    int len = s.length();
    for (int i = 0; i < len; i++) {
      out.write((byte) s.charAt(i));
    }
    incCount(len);
  }

  public final void writeChars(String s) throws IOException {
    int len = s.length();
    for (int i = 0; i < len; i++) {
      int v = s.charAt(i);
      writeBuffer[0] = (byte) (v >>> 8);
      writeBuffer[1] = (byte) v;
      out.write(writeBuffer, 0, 2);
    }
    incCount(len * 2);
  }

  public final void writeUTF(String str) throws IOException {
    writeUTF(str, this);
  }

  static int writeUTF(String str, DataOutput out) throws IOException {
    int strlen = str.length();
    int utflen = strlen;
    for (int i = 0; i < strlen; i++) {
      int c = str.charAt(i);
      if (c >= 0x80 || c == 0) {
        utflen += c >= 0x800 ? 2 : 1;
      }
    }
    if (utflen > 65535 || utflen < strlen) {
      throw new UTFDataFormatException(tooLongMsg(str, utflen));
    }
    final byte[] bytearr;
    if (out instanceof DataOutputStream) {
      DataOutputStream dos = (DataOutputStream) out;
      if (dos.bytearr == null || dos.bytearr.length < utflen + 2) {
        dos.bytearr = new byte[utflen + 2];
      }
      bytearr = dos.bytearr;
    } else {
      bytearr = new byte[utflen + 2];
    }
    int count = 0;
    bytearr[count++] = (byte) (utflen >>> 8);
    bytearr[count++] = (byte) utflen;
    int i = 0;
    for (i = 0; i < strlen; i++) {
      int c = str.charAt(i);
      if (!(c >= 0x0001 && c <= 0x007F)) {
        break;
      }
      bytearr[count++] = (byte) c;
    }
    for (; i < strlen; i++) {
      int c = str.charAt(i);
      if (c >= 0x0001 && c <= 0x007F) {
        bytearr[count++] = (byte) c;
      } else if (c > 0x07FF) {
        bytearr[count++] = (byte) (0xE0 | ((c >> 12) & 0x0F));
        bytearr[count++] = (byte) (0x80 | ((c >> 6) & 0x3F));
        bytearr[count++] = (byte) (0x80 | (c & 0x3F));
      } else {
        bytearr[count++] = (byte) (0xC0 | ((c >> 6) & 0x1F));
        bytearr[count++] = (byte) (0x80 | (c & 0x3F));
      }
    }
    out.write(bytearr, 0, utflen + 2);
    return utflen + 2;
  }

  private static String tooLongMsg(String s, int bits32) {
    int slen = s.length();
    String head = s.substring(0, 8);
    String tail = s.substring(slen - 8, slen);
    long actualLength = (long) slen + Integer.toUnsignedLong(bits32 - slen);
    return "encoded string (" + head + "..." + tail + ") too long: " + actualLength + " bytes";
  }

  public final int size() {
    return written;
  }
}
