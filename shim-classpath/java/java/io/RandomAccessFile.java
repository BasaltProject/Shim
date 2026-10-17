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

import java.nio.channels.FileChannel;
import java.util.Objects;
import shim.FileNative;

public class RandomAccessFile implements DataOutput, DataInput, Closeable {
  private final FileDescriptor fd;
  private final boolean rw;
  private final String path;
  private final boolean readable;
  private volatile boolean closed;
  private FileChannel channel;
  private final byte[] buffer8 = new byte[8];

  public RandomAccessFile(String name, String mode) throws FileNotFoundException {
    this(name != null ? new File(name) : null, mode);
  }

  public RandomAccessFile(File file, String mode) throws FileNotFoundException {
    String name = (file != null ? file.getPath() : null);
    int flags;
    if (mode.equals("r")) {
      flags = FileNative.READ;
    } else if (mode.equals("rw")) {
      flags = FileNative.READ | FileNative.WRITE | FileNative.CREATE;
    } else if (mode.equals("rws")) {
      flags = FileNative.READ | FileNative.WRITE | FileNative.CREATE | FileNative.SYNC;
    } else if (mode.equals("rwd")) {
      flags = FileNative.READ | FileNative.WRITE | FileNative.CREATE | FileNative.DSYNC;
    } else {
      throw new IllegalArgumentException("Illegal mode \"" + mode + "\" must be one of \"r\", \"rw\", \"rws\","
          + " or \"rwd\"");
    }
    if (name == null) {
      throw new NullPointerException();
    }
    if (file.isInvalid()) {
      throw new FileNotFoundException("Invalid file path");
    }
    this.rw = (flags & FileNative.WRITE) != 0;
    this.readable = true;
    this.path = name;
    this.fd = new FileDescriptor();
    int r = FileNative.open(name, flags, 0666);
    if (r < 0) {
      throw new FileNotFoundException(name + " (" + FileNative.message(r) + ")");
    }
    fd.fd = r;
  }

  RandomAccessFile(FileDescriptor fd, String path, boolean readable, boolean writable) {
    this.fd = fd;
    this.path = path;
    this.readable = readable;
    this.rw = writable;
  }

  public final FileDescriptor getFD() throws IOException {
    if (fd != null) {
      return fd;
    }
    throw new IOException();
  }

  public final FileChannel getChannel() {
    synchronized (this) {
      if (channel == null) {
        channel = sun.nio.ch.FileChannelImpl.open(fd, path, readable, rw, false, this);
      }
      return channel;
    }
  }

  private void ensureOpen() throws IOException {
    if (closed || fd.fd == -1) {
      throw new IOException("Stream Closed");
    }
  }

  public int read() throws IOException {
    ensureOpen();
    int r = FileNative.readByte(fd.fd);
    if (r < -1) {
      throw new IOException(FileNative.strerror(-2 - r));
    }
    return r;
  }

  public int read(byte[] b, int off, int len) throws IOException {
    Objects.checkFromIndexSize(off, len, b.length);
    ensureOpen();
    if (len == 0) {
      return 0;
    }
    int r = FileNative.read(fd.fd, b, off, len);
    if (r < 0) {
      throw new IOException(FileNative.message(r));
    }
    return r == 0 ? -1 : r;
  }

  public int read(byte[] b) throws IOException {
    return read(b, 0, b.length);
  }

  public final void readFully(byte[] b) throws IOException {
    readFully(b, 0, b.length);
  }

  public final void readFully(byte[] b, int off, int len) throws IOException {
    int n = 0;
    do {
      int count = this.read(b, off + n, len - n);
      if (count < 0) {
        throw new EOFException();
      }
      n += count;
    } while (n < len);
  }

  public int skipBytes(int n) throws IOException {
    long pos;
    long len;
    long newpos;
    if (n <= 0) {
      return 0;
    }
    pos = getFilePointer();
    len = length();
    newpos = pos + n;
    if (newpos > len) {
      newpos = len;
    }
    seek(newpos);
    return (int) (newpos - pos);
  }

  public void write(int b) throws IOException {
    ensureOpen();
    int r = FileNative.writeByte(fd.fd, b);
    if (r < 0) {
      throw new IOException(FileNative.message(r));
    }
  }

  public void write(byte[] b) throws IOException {
    write(b, 0, b.length);
  }

  public void write(byte[] b, int off, int len) throws IOException {
    Objects.checkFromIndexSize(off, len, b.length);
    ensureOpen();
    int r = FileNative.write(fd.fd, b, off, len);
    if (r < 0) {
      throw new IOException(FileNative.message(r));
    }
  }

  public long getFilePointer() throws IOException {
    ensureOpen();
    long r = FileNative.seek(fd.fd, 0, 1);
    if (r < 0) {
      throw new IOException(FileNative.message((int) r));
    }
    return r;
  }

  public void seek(long pos) throws IOException {
    if (pos < 0) {
      throw new IOException("Negative seek offset");
    }
    ensureOpen();
    long r = FileNative.seek(fd.fd, pos, 0);
    if (r < 0) {
      throw new IOException(FileNative.message((int) r));
    }
  }

  public long length() throws IOException {
    ensureOpen();
    long r = FileNative.size(fd.fd);
    if (r < 0) {
      throw new IOException(FileNative.message((int) r));
    }
    return r;
  }

  public void setLength(long newLength) throws IOException {
    ensureOpen();
    long pos = getFilePointer();
    int r = FileNative.truncate(fd.fd, newLength);
    if (r < 0) {
      throw new IOException(FileNative.message(r));
    }
    if (pos > newLength) {
      seek(newLength);
    }
  }

  public void close() throws IOException {
    if (closed) {
      return;
    }
    closed = true;
    if (channel != null) {
      channel.close();
    }
    fd.closeAll();
  }

  public final boolean readBoolean() throws IOException {
    int ch = this.read();
    if (ch < 0) {
      throw new EOFException();
    }
    return ch != 0;
  }

  public final byte readByte() throws IOException {
    int ch = this.read();
    if (ch < 0) {
      throw new EOFException();
    }
    return (byte) ch;
  }

  public final int readUnsignedByte() throws IOException {
    int ch = this.read();
    if (ch < 0) {
      throw new EOFException();
    }
    return ch;
  }

  public final short readShort() throws IOException {
    int ch1 = this.read();
    int ch2 = this.read();
    if ((ch1 | ch2) < 0) {
      throw new EOFException();
    }
    return (short) ((ch1 << 8) + ch2);
  }

  public final int readUnsignedShort() throws IOException {
    int ch1 = this.read();
    int ch2 = this.read();
    if ((ch1 | ch2) < 0) {
      throw new EOFException();
    }
    return (ch1 << 8) + ch2;
  }

  public final char readChar() throws IOException {
    int ch1 = this.read();
    int ch2 = this.read();
    if ((ch1 | ch2) < 0) {
      throw new EOFException();
    }
    return (char) ((ch1 << 8) + ch2);
  }

  public final int readInt() throws IOException {
    int ch1 = this.read();
    int ch2 = this.read();
    int ch3 = this.read();
    int ch4 = this.read();
    if ((ch1 | ch2 | ch3 | ch4) < 0) {
      throw new EOFException();
    }
    return (ch1 << 24) + (ch2 << 16) + (ch3 << 8) + ch4;
  }

  public final long readLong() throws IOException {
    return ((long) (readInt()) << 32) + (readInt() & 0xFFFFFFFFL);
  }

  public final float readFloat() throws IOException {
    return Float.intBitsToFloat(readInt());
  }

  public final double readDouble() throws IOException {
    return Double.longBitsToDouble(readLong());
  }

  public final String readLine() throws IOException {
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
        long cur = getFilePointer();
        if (read() != '\n') {
          seek(cur);
        }
        break;
      default:
        input.append((char) c);
        break;
      }
    }
    if (c == -1 && input.length() == 0) {
      return null;
    }
    return input.toString();
  }

  public final String readUTF() throws IOException {
    return DataInputStream.readUTF(this);
  }

  public final void writeBoolean(boolean v) throws IOException {
    write(v ? 1 : 0);
  }

  public final void writeByte(int v) throws IOException {
    write(v);
  }

  public final void writeShort(int v) throws IOException {
    buffer8[0] = (byte) (v >>> 8);
    buffer8[1] = (byte) v;
    write(buffer8, 0, 2);
  }

  public final void writeChar(int v) throws IOException {
    writeShort(v);
  }

  public final void writeInt(int v) throws IOException {
    buffer8[0] = (byte) (v >>> 24);
    buffer8[1] = (byte) (v >>> 16);
    buffer8[2] = (byte) (v >>> 8);
    buffer8[3] = (byte) v;
    write(buffer8, 0, 4);
  }

  public final void writeLong(long v) throws IOException {
    for (int i = 0; i < 8; i++) {
      buffer8[i] = (byte) (v >>> (56 - 8 * i));
    }
    write(buffer8, 0, 8);
  }

  public final void writeFloat(float v) throws IOException {
    writeInt(Float.floatToIntBits(v));
  }

  public final void writeDouble(double v) throws IOException {
    writeLong(Double.doubleToLongBits(v));
  }

  public final void writeBytes(String s) throws IOException {
    int len = s.length();
    byte[] b = new byte[len];
    for (int i = 0; i < len; i++) {
      b[i] = (byte) s.charAt(i);
    }
    write(b, 0, len);
  }

  public final void writeChars(String s) throws IOException {
    int clen = s.length();
    int blen = 2 * clen;
    byte[] b = new byte[blen];
    for (int i = 0, j = 0; i < clen; i++) {
      char c = s.charAt(i);
      b[j++] = (byte) (c >>> 8);
      b[j++] = (byte) c;
    }
    write(b, 0, blen);
  }

  public final void writeUTF(String str) throws IOException {
    DataOutputStream.writeUTF(str, this);
  }
}
