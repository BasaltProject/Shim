/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.lang.classfile.constantpool;

import java.lang.classfile.TypeKind;
import java.lang.constant.ClassDesc;
import java.lang.constant.ConstantDesc;
import java.lang.constant.DynamicConstantDesc;

public interface ConstantDynamicEntry extends DynamicConstantPoolEntry, LoadableConstantEntry {
    default DynamicConstantDesc<?> asSymbol() {
        ConstantDesc[] args = new ConstantDesc[bootstrap().arguments().size()];
        for (int i = 0; i < args.length; i++) {
            args[i] = bootstrap().arguments().get(i).constantValue();
        }
        return DynamicConstantDesc.ofNamed(bootstrap().bootstrapMethod().asSymbol(),
            name().stringValue(), typeSymbol(), args);
    }
    default TypeKind typeKind() { return TypeKind.fromDescriptor(type().stringValue()); }
    default ConstantDesc constantValue() { return asSymbol(); }
    default ClassDesc typeSymbol() { return ClassDesc.ofDescriptor(type().stringValue()); }
}
