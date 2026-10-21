/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.lang.classfile;

import java.lang.classfile.constantpool.ClassEntry;
import java.lang.classfile.constantpool.ConstantPool;
import java.lang.classfile.constantpool.PoolEntry;
import java.lang.classfile.constantpool.Utf8Entry;
import java.util.Optional;
import java.util.function.Function;

public interface ClassReader extends ConstantPool {
    int flags();
    int readInt(int i);
    long readLong(int i);
    byte[] readBytes(int i, int i2);
    float readFloat(int i);
    ClassEntry thisClassEntry();
    Optional<ClassEntry> superclassEntry();
    <T extends PoolEntry> T readEntry(int i, Class<T> class_);
    PoolEntry readEntry(int i);
    void copyBytesTo(BufWriter bufWriter, int i, int i2);
    int readU2(int i);
    int classfileLength();
    Function<Utf8Entry, AttributeMapper<?>> customAttributes();
    int readU1(int i);
    PoolEntry readEntryOrNull(int i);
    <T extends PoolEntry> T readEntryOrNull(int i, Class<T> class_);
    double readDouble(int i);
    int readS1(int i);
    int readS2(int i);
}
