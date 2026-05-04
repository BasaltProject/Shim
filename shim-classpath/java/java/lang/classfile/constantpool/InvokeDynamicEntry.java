/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.lang.classfile.constantpool;

import java.lang.constant.ConstantDesc;
import java.lang.constant.DynamicCallSiteDesc;
import java.lang.constant.MethodTypeDesc;

public interface InvokeDynamicEntry extends DynamicConstantPoolEntry {
    default DynamicCallSiteDesc asSymbol() {
        ConstantDesc[] args = new ConstantDesc[bootstrap().arguments().size()];
        for (int i = 0; i < args.length; i++) {
            args[i] = bootstrap().arguments().get(i).constantValue();
        }
        return DynamicCallSiteDesc.of(bootstrap().bootstrapMethod().asSymbol(),
            name().stringValue(), typeSymbol(), args);
    }
    default MethodTypeDesc typeSymbol() { return MethodTypeDesc.ofDescriptor(type().stringValue()); }
}
