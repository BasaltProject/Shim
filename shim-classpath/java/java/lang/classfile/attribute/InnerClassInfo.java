/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.lang.classfile.attribute;

import java.lang.classfile.constantpool.ClassEntry;
import java.lang.classfile.constantpool.Utf8Entry;
import java.lang.constant.ClassDesc;
import java.lang.reflect.AccessFlag;
import java.util.Optional;
import java.util.Set;

public interface InnerClassInfo {
    ClassEntry innerClass();

    Optional<ClassEntry> outerClass();

    Optional<Utf8Entry> innerName();

    int flagsMask();

    default Set<AccessFlag> flags() {
        return AccessFlag.maskToAccessFlags(flagsMask(), AccessFlag.Location.INNER_CLASS);
    }

    default boolean has(AccessFlag flag) {
        return shim.classfile.AttrSupport.has(AccessFlag.Location.INNER_CLASS, flagsMask(), flag);
    }

    static InnerClassInfo of(ClassEntry innerClass, Optional<ClassEntry> outerClass, Optional<Utf8Entry> innerName,
                             int flags) {
        return new shim.classfile.InnerClassInfoImpl(innerClass, outerClass, innerName, flags);
    }

    static InnerClassInfo of(ClassDesc innerClass, Optional<ClassDesc> outerClass, Optional<String> innerName,
                             int flags) {
        shim.classfile.PoolImpl pool = shim.classfile.AttrSupport.pool();
        return new shim.classfile.InnerClassInfoImpl(pool.classEntry(innerClass), outerClass.map(pool::classEntry),
            innerName.map(pool::utf8Entry), flags);
    }

    static InnerClassInfo of(ClassDesc innerClass, Optional<ClassDesc> outerClass, Optional<String> innerName,
                             AccessFlag... flags) {
        return of(innerClass, outerClass, innerName,
            shim.classfile.AttrSupport.mask(AccessFlag.Location.INNER_CLASS, flags));
    }
}
