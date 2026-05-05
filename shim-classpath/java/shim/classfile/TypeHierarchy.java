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
import java.lang.classfile.ClassHierarchyResolver.ClassHierarchyInfo;
import java.lang.constant.ClassDesc;
import java.util.HashMap;

/** Merges reference types through a class hierarchy resolver, remembering what it resolved. */
final class TypeHierarchy {
    private final ClassHierarchyResolver resolver;
    private final HashMap<String, ClassHierarchyInfoImpl> cache = new HashMap<>();

    TypeHierarchy(ClassHierarchyResolver resolver) {
        this.resolver = resolver;
    }

    private ClassHierarchyInfoImpl info(String name) {
        ClassHierarchyInfoImpl info = cache.get(name);
        if (info == null) {
            ClassDesc desc = ClassDesc.ofInternalName(name);
            ClassHierarchyInfo found = resolver.getClassInfo(desc);
            if (found == null) {
                throw new IllegalArgumentException("Could not resolve class " + desc.displayName());
            }
            info = (ClassHierarchyInfoImpl) found;
            cache.put(name, info);
        }
        return info;
    }

    private String superOf(String name) {
        ClassDesc s = info(name).superClass();
        if (s == null) {
            return null;
        }
        String d = s.descriptorString();
        return d.charAt(0) == 'L' ? d.substring(1, d.length() - 1) : d;
    }

    private boolean isAssignable(String target, String from) {
        String c = from;
        while (c != null) {
            if (c.equals(target)) {
                return true;
            }
            c = superOf(c);
        }
        return false;
    }

    /** Merges two class or array types, {@code into} being the type already in the frame. */
    VType merge(VType into, VType from) {
        if (into.equals(from) || into.isObjectClass()) {
            return into;
        }
        if (into.isArray()) {
            if (from.isArray()) {
                VType a = into.component();
                VType b = from.component();
                if (a.tag == VType.OBJECT && b.tag == VType.OBJECT) {
                    return merge(a, b).arrayOf();
                }
            }
            return VType.T_OBJECT;
        }
        ClassHierarchyInfoImpl intoInfo = info(into.name);
        if (from.isArray()) {
            return VType.T_OBJECT;
        }
        if (intoInfo.isInterface()) {
            return into;
        }
        if (info(from.name).isInterface()) {
            return VType.T_OBJECT;
        }
        if (isAssignable(into.name, from.name)) {
            return into;
        }
        if (isAssignable(from.name, into.name)) {
            return from;
        }
        String c = into.name;
        do {
            c = superOf(c);
            if (c == null) {
                return VType.T_OBJECT;
            }
        } while (!isAssignable(c, from.name));
        return VType.ref(c);
    }
}
