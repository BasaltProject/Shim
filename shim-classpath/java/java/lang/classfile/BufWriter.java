/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.lang.classfile;

import java.lang.classfile.constantpool.ConstantPool;
import java.lang.classfile.constantpool.ConstantPoolBuilder;
import java.lang.classfile.constantpool.PoolEntry;

public interface BufWriter {
    int size();
    void writeInt(int i);
    void writeLong(long l);
    void writeBytes(byte[] bs);
    void writeBytes(byte[] bs, int i, int i2);
    void writeFloat(float f);
    boolean canWriteDirect(ConstantPool constantPool);
    ConstantPoolBuilder constantPool();
    void writeIndex(PoolEntry poolEntry);
    void patchInt(int i, int i2, int i3);
    void writeU2(int i);
    void writeIndexOrZero(PoolEntry poolEntry);
    void writeU1(int i);
    void reserveSpace(int i);
    void writeIntBytes(int i, long l);
    void writeDouble(double d);
}
