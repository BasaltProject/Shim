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
import java.lang.constant.DirectMethodHandleDesc;

public interface MethodHandleEntry extends LoadableConstantEntry {
    int kind();
    MemberRefEntry reference();
    DirectMethodHandleDesc asSymbol();
    default ConstantDesc constantValue() { return asSymbol(); }
}
