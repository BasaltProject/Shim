/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.classfile;

import java.lang.classfile.attribute.StackMapFrameInfo;
import java.lang.classfile.constantpool.ClassEntry;
import java.util.Objects;

/** A verification type naming a class or array type. */
public final class ObjectVerificationTypeInfoImpl implements StackMapFrameInfo.ObjectVerificationTypeInfo {
    private final ClassEntry className;

    public ObjectVerificationTypeInfoImpl(ClassEntry className) {
        this.className = Objects.requireNonNull(className);
    }

    @Override public int tag() { return ITEM_OBJECT; }
    @Override public ClassEntry className() { return className; }

    @Override
    public boolean equals(Object o) {
        return o instanceof ObjectVerificationTypeInfoImpl
            && className.equals(((ObjectVerificationTypeInfoImpl) o).className);
    }

    @Override public int hashCode() { return 31 + className.hashCode(); }
    @Override public String toString() { return className.asInternalName(); }
}
