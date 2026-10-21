/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.classfile;

import java.lang.classfile.attribute.InnerClassInfo;
import java.lang.classfile.constantpool.ClassEntry;
import java.lang.classfile.constantpool.Utf8Entry;
import java.util.Objects;
import java.util.Optional;

public final class InnerClassInfoImpl implements InnerClassInfo {
    private final ClassEntry innerClass;
    private final Optional<ClassEntry> outerClass;
    private final Optional<Utf8Entry> innerName;
    private final int flagsMask;

    public InnerClassInfoImpl(ClassEntry innerClass, Optional<ClassEntry> outerClass, Optional<Utf8Entry> innerName,
                              int flagsMask) {
        this.innerClass = Objects.requireNonNull(innerClass);
        this.outerClass = Objects.requireNonNull(outerClass);
        this.innerName = Objects.requireNonNull(innerName);
        this.flagsMask = flagsMask;
    }

    @Override public ClassEntry innerClass() { return innerClass; }
    @Override public Optional<ClassEntry> outerClass() { return outerClass; }
    @Override public Optional<Utf8Entry> innerName() { return innerName; }
    @Override public int flagsMask() { return flagsMask; }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof InnerClassInfoImpl)) {
            return false;
        }
        InnerClassInfoImpl x = (InnerClassInfoImpl) o;
        return innerClass.equals(x.innerClass) && outerClass.equals(x.outerClass) && innerName.equals(x.innerName)
            && flagsMask == x.flagsMask;
    }

    @Override
    public int hashCode() {
        return AttrSupport.hash(innerClass, outerClass, innerName, flagsMask);
    }

    @Override
    public String toString() {
        return "UnboundInnerClassInfo[innerClass=" + innerClass + ", outerClass=" + outerClass + ", innerName="
            + innerName + ", flagsMask=" + flagsMask + "]";
    }
}
