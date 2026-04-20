/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.lang.classfile;

import java.lang.reflect.AccessFlag;
import java.util.function.Consumer;

public interface MethodBuilder extends ClassFileBuilder<MethodElement, MethodBuilder> {
    default MethodBuilder withFlags(int i) { return with(new shim.classfile.Elements.AccessFlagsImpl(AccessFlag.Location.METHOD, i)); }
    default MethodBuilder withFlags(AccessFlag... accessFlags) {
        int mask = 0;
        for (AccessFlag f : accessFlags) {
            mask |= f.mask();
        }
        return withFlags(mask);
    }
    MethodBuilder withCode(Consumer<? super CodeBuilder> consumer);
    MethodBuilder transformCode(CodeModel codeModel, CodeTransform codeTransform);
}
