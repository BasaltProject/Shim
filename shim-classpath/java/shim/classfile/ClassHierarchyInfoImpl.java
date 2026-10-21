/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.classfile;

import java.lang.classfile.ClassHierarchyResolver;
import java.lang.constant.ClassDesc;
import java.util.Objects;

/** The superclass and interface flag of one class, as a class hierarchy resolver reports it. */
public final class ClassHierarchyInfoImpl implements ClassHierarchyResolver.ClassHierarchyInfo {
    private final ClassDesc superClass;
    private final boolean isInterface;

    public ClassHierarchyInfoImpl(ClassDesc superClass, boolean isInterface) {
        this.superClass = superClass;
        this.isInterface = isInterface;
    }

    public ClassDesc superClass() { return superClass; }

    public boolean isInterface() { return isInterface; }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ClassHierarchyInfoImpl)) {
            return false;
        }
        ClassHierarchyInfoImpl other = (ClassHierarchyInfoImpl) o;
        return isInterface == other.isInterface && Objects.equals(superClass, other.superClass);
    }

    @Override
    public int hashCode() {
        return 31 * Objects.hashCode(superClass) + Boolean.hashCode(isInterface);
    }

    @Override
    public String toString() {
        return "ClassHierarchyInfoImpl[superClass=" + superClass + ", isInterface=" + isInterface + "]";
    }
}
