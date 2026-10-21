/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.classfile;

import java.lang.classfile.BufWriter;
import java.lang.classfile.constantpool.ConstantPool;
import java.lang.classfile.constantpool.ConstantPoolBuilder;
import java.lang.classfile.constantpool.PoolEntry;
import java.util.Arrays;

public final class BufWriterImpl implements BufWriter {
    private final PoolImpl pool;
    private byte[] buf;
    private int len;
    private LabelContext labelContext;

    public BufWriterImpl(ConstantPoolBuilder pool) {
        this(pool, 256);
    }

    public BufWriterImpl(ConstantPoolBuilder pool, int capacity) {
        this.pool = (PoolImpl) pool;
        this.buf = new byte[Math.max(capacity, 16)];
    }

    private void ensure(int more) {
        if (len + more > buf.length) {
            buf = Arrays.copyOf(buf, Math.max(buf.length * 2, len + more));
        }
    }

    @Override public int size() { return len; }

    @Override
    public void writeU1(int x) {
        ensure(1);
        buf[len++] = (byte) x;
    }

    @Override
    public void writeU2(int x) {
        ensure(2);
        buf[len++] = (byte) (x >> 8);
        buf[len++] = (byte) x;
    }

    @Override
    public void writeInt(int x) {
        ensure(4);
        buf[len++] = (byte) (x >> 24);
        buf[len++] = (byte) (x >> 16);
        buf[len++] = (byte) (x >> 8);
        buf[len++] = (byte) x;
    }

    @Override
    public void writeLong(long x) {
        writeInt((int) (x >> 32));
        writeInt((int) x);
    }

    @Override public void writeFloat(float x) { writeInt(Float.floatToIntBits(x)); }
    @Override public void writeDouble(double x) { writeLong(Double.doubleToLongBits(x)); }
    @Override public void writeBytes(byte[] bytes) { writeBytes(bytes, 0, bytes.length); }

    @Override
    public void writeBytes(byte[] bytes, int off, int n) {
        ensure(n);
        System.arraycopy(bytes, off, buf, len, n);
        len += n;
    }

    @Override
    public void writeIntBytes(int intSize, long intValue) {
        for (int i = intSize - 1; i >= 0; i--) {
            writeU1((int) (intValue >> (8 * i)));
        }
    }

    @Override
    public void reserveSpace(int freeBytes) {
        ensure(freeBytes);
    }

    @Override
    public void patchInt(int offset, int size, int value) {
        for (int i = 0; i < size; i++) {
            buf[offset + i] = (byte) (value >> (8 * (size - 1 - i)));
        }
    }

    @Override public boolean canWriteDirect(ConstantPool other) { return pool.canWriteDirect(other); }
    @Override public ConstantPoolBuilder constantPool() { return pool; }

    @Override
    public void writeIndex(PoolEntry entry) {
        writeU2(pool.indexOf(entry));
    }

    @Override
    public void writeIndexOrZero(PoolEntry entry) {
        writeU2(entry == null ? 0 : pool.indexOf(entry));
    }

    void writeUtf8(String s) {
        int n = s.length();
        int bytes = 0;
        for (int i = 0; i < n; i++) {
            char c = s.charAt(i);
            bytes += c != 0 && c < 0x80 ? 1 : c < 0x800 ? 2 : 3;
        }
        if (bytes > 65535) {
            throw new IllegalArgumentException("string too long");
        }
        writeU2(bytes);
        ensure(bytes);
        for (int i = 0; i < n; i++) {
            char c = s.charAt(i);
            if (c != 0 && c < 0x80) {
                buf[len++] = (byte) c;
            } else if (c < 0x800) {
                buf[len++] = (byte) (0xc0 | (c >> 6));
                buf[len++] = (byte) (0x80 | (c & 0x3f));
            } else {
                buf[len++] = (byte) (0xe0 | (c >> 12));
                buf[len++] = (byte) (0x80 | ((c >> 6) & 0x3f));
                buf[len++] = (byte) (0x80 | (c & 0x3f));
            }
        }
    }

    public void writeBuf(BufWriterImpl other) {
        writeBytes(other.buf, 0, other.len);
    }

    public byte[] toByteArray() {
        return Arrays.copyOf(buf, len);
    }

    public LabelContext labelContext() { return labelContext; }

    public void setLabelContext(LabelContext labelContext) { this.labelContext = labelContext; }

    int u1At(int offset) { return buf[offset] & 0xff; }
}
