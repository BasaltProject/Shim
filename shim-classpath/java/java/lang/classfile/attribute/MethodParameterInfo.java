/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.lang.classfile.attribute;

import java.lang.classfile.constantpool.Utf8Entry;
import java.lang.reflect.AccessFlag;
import java.util.Optional;
import java.util.Set;

public interface MethodParameterInfo {
    Optional<Utf8Entry> name();

    int flagsMask();

    default Set<AccessFlag> flags() {
        return AccessFlag.maskToAccessFlags(flagsMask(), AccessFlag.Location.METHOD_PARAMETER);
    }

    default boolean has(AccessFlag flag) {
        return shim.classfile.AttrSupport.has(AccessFlag.Location.METHOD_PARAMETER, flagsMask(), flag);
    }

    static MethodParameterInfo of(Optional<Utf8Entry> name, int flags) {
        return new shim.classfile.MethodParameterInfoImpl(name, flags);
    }

    static MethodParameterInfo of(Optional<String> name, AccessFlag... flags) {
        return ofParameter(name, shim.classfile.AttrSupport.mask(AccessFlag.Location.METHOD_PARAMETER, flags));
    }

    static MethodParameterInfo ofParameter(Optional<String> name, int flags) {
        return of(name.map(shim.classfile.AttrSupport.pool()::utf8Entry), flags);
    }
}
