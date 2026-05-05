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
import java.util.HashMap;
import java.util.Map;

/** Class hierarchy queries of the verifier, answered through a resolver and remembered. */
final class VerifyHierarchy {
    private final ClassHierarchyResolver resolver;
    private final Map<String, ClassHierarchyResolver.ClassHierarchyInfo> cache = new HashMap<>();
    /** The offset the previous method's check stopped at, which messages of the next one may repeat. */
    int lastOffset;

    VerifyHierarchy(ClassHierarchyResolver resolver) {
        this.resolver = resolver;
    }

    private ClassHierarchyResolver.ClassHierarchyInfo info(String name) {
        ClassHierarchyResolver.ClassHierarchyInfo info = cache.get(name);
        if (info == null) {
            ClassDesc desc = ClassDesc.ofInternalName(name);
            info = resolver.getClassInfo(desc);
            if (info == null) {
                throw new IllegalArgumentException("Could not resolve class " + desc.displayName());
            }
            cache.put(name, info);
        }
        return info;
    }

    boolean isInterface(String name) {
        return ((ClassHierarchyInfoImpl) info(name)).isInterface();
    }

    String superOf(String name) {
        ClassDesc s = ((ClassHierarchyInfoImpl) info(name)).superClass();
        if (s == null) {
            return null;
        }
        String d = s.descriptorString();
        return d.substring(1, d.length() - 1);
    }

    /** Whether {@code sub} is {@code sup} or one of its subclasses. */
    boolean isSubclass(String sup, String sub) {
        for (String c = sub; c != null; c = superOf(c)) {
            if (c.equals(sup)) {
                return true;
            }
        }
        return false;
    }

    boolean isReferenceAssignable(VerifyType target, VerifyType from) {
        if (target.name.equals("java/lang/Object")) {
            return true;
        }
        if (!target.isArray()) {
            if (isInterface(target.name)) {
                return !from.isArray() || target.name.equals("java/lang/Cloneable")
                    || target.name.equals("java/io/Serializable");
            }
            if (from.isArray()) {
                return false;
            }
            return isSubclass(target.name, from.name);
        }
        if (!from.isArray()) {
            return false;
        }
        VerifyType tc = target.component();
        VerifyType fc = from.component();
        if (tc.kind == VerifyType.REF && fc.kind == VerifyType.REF) {
            return isReferenceAssignable(tc, fc);
        }
        return tc.equals(fc);
    }
}
