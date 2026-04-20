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
import java.lang.classfile.ClassElement;
import java.util.Arrays;
import java.util.List;

public interface InnerClassesAttribute extends Attribute<InnerClassesAttribute>, ClassElement {
    List<InnerClassInfo> classes();

    static InnerClassesAttribute of(List<InnerClassInfo> innerClasses) {
        return new shim.classfile.InnerClassesAttr(innerClasses);
    }

    static InnerClassesAttribute of(InnerClassInfo... innerClasses) {
        return of(Arrays.asList(innerClasses));
    }
}
