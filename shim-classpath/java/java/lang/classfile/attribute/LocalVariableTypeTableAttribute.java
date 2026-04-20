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
import java.util.List;

public interface LocalVariableTypeTableAttribute extends Attribute<LocalVariableTypeTableAttribute> {
    List<LocalVariableTypeInfo> localVariableTypes();

    static LocalVariableTypeTableAttribute of(List<LocalVariableTypeInfo> locals) {
        return new shim.classfile.LocalVariableTypeTableAttr(locals);
    }
}
