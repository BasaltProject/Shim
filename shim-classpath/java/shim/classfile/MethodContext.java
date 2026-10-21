/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.classfile;

import java.lang.classfile.constantpool.ClassEntry;
import java.lang.classfile.constantpool.Utf8Entry;

/** What a code builder has to know about the method it belongs to. */
public final class MethodContext {
    public final ClassFileImpl classFile;
    public final PoolImpl pool;
    public final ClassEntry thisClass;
    public final Utf8Entry name;
    public final Utf8Entry type;
    public final int flags;

    public MethodContext(ClassFileImpl classFile, PoolImpl pool, ClassEntry thisClass, Utf8Entry name, Utf8Entry type,
                         int flags) {
        this.classFile = classFile;
        this.pool = pool;
        this.thisClass = thisClass;
        this.name = name;
        this.type = type;
        this.flags = flags;
    }

    private int majorVersion = -1;

    /** The major version of the class the method is written to, or the latest when unknown. */
    public int majorVersion() {
        return majorVersion < 0 ? java.lang.classfile.ClassFile.latestMajorVersion() : majorVersion;
    }

    public void setMajorVersion(int majorVersion) {
        this.majorVersion = majorVersion;
    }

    public boolean isStatic() { return (flags & java.lang.classfile.ClassFile.ACC_STATIC) != 0; }
}
