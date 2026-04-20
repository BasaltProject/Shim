/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.lang.classfile.attribute;

import java.lang.classfile.Attribute;
import java.lang.classfile.CodeModel;
import java.lang.classfile.Label;

public interface CodeAttribute extends Attribute<CodeAttribute>, CodeModel {
    int maxLocals();
    int codeLength();
    int labelToBci(Label label);
    int maxStack();
    byte[] codeArray();
}
