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
import java.lang.constant.ClassDesc;
import java.util.Arrays;
import java.util.List;

public interface ModuleProvideInfo {
    ClassEntry provides();

    List<ClassEntry> providesWith();

    static ModuleProvideInfo of(ClassEntry provides, List<ClassEntry> providesWith) {
        return new shim.classfile.ModuleProvideInfoImpl(provides, providesWith);
    }

    static ModuleProvideInfo of(ClassEntry provides, ClassEntry... providesWith) {
        return of(provides, Arrays.asList(providesWith));
    }

    static ModuleProvideInfo of(ClassDesc provides, List<ClassDesc> providesWith) {
        shim.classfile.PoolImpl pool = shim.classfile.AttrSupport.pool();
        ClassEntry service = pool.classEntry(provides);
        ClassEntry[] impls = new ClassEntry[providesWith.size()];
        for (int i = 0; i < impls.length; i++) {
            impls[i] = pool.classEntry(providesWith.get(i));
        }
        return of(service, impls);
    }

    static ModuleProvideInfo of(ClassDesc provides, ClassDesc... providesWith) {
        return of(provides, Arrays.asList(providesWith));
    }
}
