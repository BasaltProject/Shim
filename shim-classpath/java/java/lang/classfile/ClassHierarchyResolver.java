/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.lang.classfile;

import java.io.InputStream;
import java.lang.constant.ClassDesc;
import java.lang.constant.ConstantDescs;
import java.lang.invoke.MethodHandles;
import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import shim.classfile.ClassHierarchyImpl;
import shim.classfile.ClassHierarchyInfoImpl;

@FunctionalInterface public interface ClassHierarchyResolver {
    public static ClassHierarchyResolver of(Collection<ClassDesc> collection, Map<ClassDesc, ClassDesc> map) {
        return ClassHierarchyImpl.of(collection, map);
    }

    default ClassHierarchyResolver orElse(ClassHierarchyResolver classHierarchyResolver) {
        return ClassHierarchyImpl.orElse(this, classHierarchyResolver);
    }

    default ClassHierarchyResolver cached() {
        return ClassHierarchyImpl.cached(this);
    }

    default ClassHierarchyResolver cached(
            Supplier<Map<ClassDesc, ClassHierarchyResolver.ClassHierarchyInfo>> supplier) {
        return ClassHierarchyImpl.cached(this, supplier);
    }

    ClassHierarchyResolver.ClassHierarchyInfo getClassInfo(ClassDesc classDesc);
    public static ClassHierarchyResolver ofResourceParsing(ClassLoader classLoader) {
        return ClassHierarchyImpl.ofResourceParsing(classLoader);
    }

    public static ClassHierarchyResolver ofResourceParsing(Function<ClassDesc, InputStream> function) {
        return ClassHierarchyImpl.ofResourceParsing(function);
    }

    public static ClassHierarchyResolver defaultResolver() {
        return ClassHierarchyImpl.defaultResolver();
    }

    public static ClassHierarchyResolver ofClassLoading(ClassLoader classLoader) {
        return ClassHierarchyImpl.ofClassLoading(classLoader);
    }

    public static ClassHierarchyResolver ofClassLoading(MethodHandles.Lookup lookup) {
        return ClassHierarchyImpl.ofClassLoading(lookup);
    }

    public interface ClassHierarchyInfo {
        public static ClassHierarchyResolver.ClassHierarchyInfo ofClass(ClassDesc classDesc) {
            return new ClassHierarchyInfoImpl(classDesc, false);
        }

        public static ClassHierarchyResolver.ClassHierarchyInfo ofInterface() {
            return new ClassHierarchyInfoImpl(ConstantDescs.CD_Object, true);
        }
    }
}
