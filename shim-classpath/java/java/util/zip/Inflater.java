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

package java.util.zip;

import java.nio.ByteBuffer;

public class Inflater {
  private static final int Z_STREAM_END = 1;
  private static final int Z_NEED_DICT = 2;
  private static final int Z_DATA_ERROR = -3;
  private static final int Z_MEM_ERROR = -4;
  private static final int Z_BUF_ERROR = -5;

  private long peer;
  private byte[] input = new byte[0];
  private int offset;
  private int length;
  private ByteBuffer inputBuffer;
  private int inputBufferStart;
  private boolean needDict;
  private boolean finished;
  private final boolean nowrap;
  private long bytesRead;
  private long bytesWritten;

  public Inflater(boolean nowrap) {
    this.nowrap = nowrap;
    peer = make(nowrap);
  }

  public Inflater() {
    this(false);
  }

  private static native long make(boolean nowrap);

  private void ensureOpen() {
    if (peer == 0) {
      throw new IllegalStateException("Inflater has been closed");
    }
  }

  public synchronized boolean finished() {
    return finished;
  }

  public synchronized boolean needsDictionary() {
    return needDict;
  }

  public synchronized boolean needsInput() {
    return length == 0;
  }

  public synchronized int getRemaining() {
    return length;
  }

  public synchronized int drainRemaining(byte[] dest, int off, int len) {
    if (length <= 0) {
      return 0;
    }
    int n = Math.min(len, length);
    System.arraycopy(input, offset, dest, off, n);
    offset += n;
    length -= n;
    if (inputBuffer != null) {
      inputBuffer.position(inputBufferStart + offset);
    }
    return n;
  }

  public void setInput(byte[] input) {
    setInput(input, 0, input.length);
  }

  public synchronized void setInput(byte[] input, int offset, int length) {
    java.util.Objects.checkFromIndexSize(offset, length, input.length);
    this.input = input;
    this.offset = offset;
    this.length = length;
    this.inputBuffer = null;
  }

  public synchronized void setInput(ByteBuffer input) {
    java.util.Objects.requireNonNull(input);
    byte[] copy = new byte[input.remaining()];
    inputBufferStart = input.position();
    input.get(copy);
    input.position(inputBufferStart);
    this.input = copy;
    this.offset = 0;
    this.length = copy.length;
    this.inputBuffer = input;
  }

  public void setDictionary(byte[] dictionary) {
    setDictionary(dictionary, 0, dictionary.length);
  }

  public synchronized void setDictionary(byte[] dictionary, int off, int len) {
    ensureOpen();
    java.util.Objects.checkFromIndexSize(off, len, dictionary.length);
    if (!setDictionary(peer, dictionary, off, len)) {
      throw new IllegalArgumentException();
    }
    needDict = false;
  }

  public synchronized void setDictionary(ByteBuffer dictionary) {
    byte[] copy = new byte[dictionary.remaining()];
    int pos = dictionary.position();
    dictionary.get(copy);
    dictionary.position(pos + copy.length);
    setDictionary(copy, 0, copy.length);
  }

  public synchronized void reset() {
    ensureOpen();
    dispose(peer);
    peer = make(nowrap);
    input = new byte[0];
    offset = length = 0;
    inputBuffer = null;
    needDict = finished = false;
    bytesRead = 0;
    bytesWritten = 0;
  }

  public int inflate(byte[] output) throws DataFormatException {
    return inflate(output, 0, output.length);
  }

  public synchronized int inflate(byte[] output, int off, int len) throws DataFormatException {
    java.util.Objects.checkFromIndexSize(off, len, output.length);
    ensureOpen();
    int[] results = new int[3];
    inflate(peer, input, offset, length, output, off, len, results);
    switch (results[0]) {
    case Z_NEED_DICT:
      needDict = true;
      break;
    case Z_STREAM_END:
      finished = true;
      break;
    case Z_DATA_ERROR:
      offset += results[1];
      length -= results[1];
      bytesRead += results[1];
      bytesWritten += results[2];
      throw new DataFormatException(message(peer));
    case Z_MEM_ERROR:
      throw new OutOfMemoryError();
    default:
      if (results[0] < 0 && results[0] != Z_BUF_ERROR) {
        throw new InternalError(message(peer));
      }
    }
    this.offset += results[1];
    this.length -= results[1];
    this.bytesRead += results[1];
    this.bytesWritten += results[2];
    if (inputBuffer != null) {
      inputBuffer.position(inputBufferStart + this.offset);
    }
    return results[2];
  }

  public synchronized int inflate(ByteBuffer output) throws DataFormatException {
    if (output.isReadOnly()) {
      throw new java.nio.ReadOnlyBufferException();
    }
    byte[] tmp = new byte[output.remaining()];
    int n = inflate(tmp, 0, tmp.length);
    output.put(tmp, 0, n);
    return n;
  }

  public synchronized int getAdler() {
    ensureOpen();
    return getAdler(peer);
  }

  public synchronized int getTotalIn() {
    return (int) getBytesRead();
  }

  public synchronized long getBytesRead() {
    ensureOpen();
    return bytesRead;
  }

  public synchronized int getTotalOut() {
    return (int) getBytesWritten();
  }

  public synchronized long getBytesWritten() {
    ensureOpen();
    return bytesWritten;
  }

  public synchronized void end() {
    if (peer != 0) {
      dispose(peer);
      peer = 0;
      input = new byte[0];
      inputBuffer = null;
    }
  }

  public void close() {
    end();
  }

  public void dispose() {
    end();
  }

  private static native void inflate
    (long peer,
     byte[] input, int inputOffset, int inputLength,
     byte[] output, int outputOffset, int outputLength,
     int[] results);

  private static native boolean setDictionary(long peer, byte[] dictionary, int off, int len);

  private static native int getAdler(long peer);

  private static native String message(long peer);

  private static native void dispose(long peer);
}
