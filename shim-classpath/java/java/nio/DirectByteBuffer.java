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


package java.nio;

import java.util.Objects;
import sun.misc.Unsafe;

public class DirectByteBuffer extends MappedByteBuffer {
  private static final Unsafe unsafe = Unsafe.getUnsafe();
  private static final int baseOffset = unsafe.arrayBaseOffset(byte[].class);

  protected final long address;
  private long mapBase;
  private long mapLength;

  public DirectByteBuffer(long address, int capacity, boolean readOnly) {
    this(address, -1, 0, capacity, capacity, readOnly);
  }

  public DirectByteBuffer(long address, int capacity) {
    this(address, capacity, false);
  }

  DirectByteBuffer(long address, int mark, int pos, int lim, int cap, boolean readOnly) {
    super(mark, pos, lim, cap);
    this.address = address;
    this.isReadOnly = readOnly;
  }

  public long address() {
    return address;
  }

  @Override
  long directAddress() {
    return address;
  }

  public void mapped(long base, long length) {
    mapBase = base;
    mapLength = length;
  }

  void forceMapped() {
    if (mapLength > 0) {
      shim.NioNative.msync(mapBase, mapLength);
    }
  }

  DirectByteBuffer make(long address, int mark, int pos, int lim, int cap, boolean readOnly) {
    return new DirectByteBuffer(address, mark, pos, lim, cap, readOnly);
  }

  @Override
  String bufferName() {
    return "java.nio.DirectByteBuffer" + (isReadOnly ? "R" : "");
  }

  @Override
  public boolean isReadOnly() {
    return isReadOnly;
  }

  @Override
  public boolean isDirect() {
    return true;
  }

  @Override
  public MappedByteBuffer slice() {
    int pos = position();
    int lim = limit();
    int rem = pos <= lim ? lim - pos : 0;
    return make(address + pos, -1, 0, rem, rem, isReadOnly);
  }

  @Override
  public MappedByteBuffer slice(int index, int length) {
    Objects.checkFromIndexSize(index, length, limit());
    return make(address + index, -1, 0, length, length, isReadOnly);
  }

  @Override
  public MappedByteBuffer duplicate() {
    return make(address, markValue(), position(), limit(), capacity(), isReadOnly);
  }

  @Override
  public ByteBuffer asReadOnlyBuffer() {
    return make(address, markValue(), position(), limit(), capacity(), true);
  }

  @Override
  byte doGet(int index) {
    return unsafe.getByte(address + index);
  }

  @Override
  void doPut(int index, byte b) {
    unsafe.putByte(address + index, b);
  }

  @Override
  public byte get() {
    return unsafe.getByte(address + nextGetIndex());
  }

  @Override
  public byte get(int index) {
    return unsafe.getByte(address + checkIndex(index));
  }

  @Override
  public ByteBuffer put(byte b) {
    if (isReadOnly) {
      throw new ReadOnlyBufferException();
    }
    unsafe.putByte(address + nextPutIndex(), b);
    return this;
  }

  @Override
  public ByteBuffer put(int index, byte b) {
    if (isReadOnly) {
      throw new ReadOnlyBufferException();
    }
    unsafe.putByte(address + checkIndex(index), b);
    return this;
  }

  @Override
  public ByteBuffer get(byte[] dst, int offset, int length) {
    Objects.checkFromIndexSize(offset, length, dst.length);
    int pos = position();
    if (length > limit() - pos) {
      throw new BufferUnderflowException();
    }
    unsafe.copyMemory(null, address + pos, dst, baseOffset + offset, length);
    position(pos + length);
    return this;
  }

  @Override
  public ByteBuffer put(byte[] src, int offset, int length) {
    if (isReadOnly) {
      throw new ReadOnlyBufferException();
    }
    Objects.checkFromIndexSize(offset, length, src.length);
    int pos = position();
    if (length > limit() - pos) {
      throw new BufferOverflowException();
    }
    unsafe.copyMemory(src, baseOffset + offset, null, address + pos, length);
    position(pos + length);
    return this;
  }

  @Override
  public ByteBuffer put(ByteBuffer src) {
    if (src instanceof DirectByteBuffer && src != this) {
      DirectByteBuffer sb = (DirectByteBuffer) src;
      long from = sb.address + sb.position();
      long to = address + position();
      int n = sb.remaining();
      if (to + n <= from || from + n <= to) {
        if (isReadOnly) {
          throw new ReadOnlyBufferException();
        }
        if (n > remaining()) {
          throw new BufferOverflowException();
        }
        unsafe.copyMemory(from, to, n);
        sb.position(sb.position() + n);
        position(position() + n);
        return this;
      }
    }
    return super.put(src);
  }

  @Override
  public MappedByteBuffer compact() {
    if (isReadOnly) {
      throw new ReadOnlyBufferException();
    }
    int pos = position();
    int lim = limit();
    int rem = pos <= lim ? lim - pos : 0;
    if (rem > 0 && pos > 0) {
      byte[] tmp = new byte[rem];
      unsafe.copyMemory(null, address + pos, tmp, baseOffset, rem);
      unsafe.copyMemory(tmp, baseOffset, null, address, rem);
    }
    position(rem);
    limit(capacity());
    discardMark();
    return this;
  }
}
