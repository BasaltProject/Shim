/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.lang.classfile.constantpool;

public interface PoolEntry {
    public static final int TAG_CLASS = 7;
    public static final int TAG_DOUBLE = 6;
    public static final int TAG_DYNAMIC = 17;
    public static final int TAG_FIELDREF = 9;
    public static final int TAG_FLOAT = 4;
    public static final int TAG_INTEGER = 3;
    public static final int TAG_INTERFACE_METHODREF = 11;
    public static final int TAG_INVOKE_DYNAMIC = 18;
    public static final int TAG_LONG = 5;
    public static final int TAG_METHOD_HANDLE = 15;
    public static final int TAG_METHODREF = 10;
    public static final int TAG_METHOD_TYPE = 16;
    public static final int TAG_MODULE = 19;
    public static final int TAG_NAME_AND_TYPE = 12;
    public static final int TAG_PACKAGE = 20;
    public static final int TAG_STRING = 8;
    public static final int TAG_UTF8 = 1;
    int index();
    int tag();
    ConstantPool constantPool();
    int width();
}
