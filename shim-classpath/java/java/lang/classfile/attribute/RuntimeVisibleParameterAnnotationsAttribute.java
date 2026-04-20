/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.lang.classfile.attribute;

import java.lang.classfile.Annotation;
import java.lang.classfile.Attribute;
import java.lang.classfile.MethodElement;
import java.util.List;
import shim.classfile.RuntimeVisibleParameterAnnotationsAttr;

public interface RuntimeVisibleParameterAnnotationsAttribute
        extends Attribute<RuntimeVisibleParameterAnnotationsAttribute>, MethodElement {
    List<List<Annotation>> parameterAnnotations();
    public static RuntimeVisibleParameterAnnotationsAttribute of(List<List<Annotation>> list) {
        return new RuntimeVisibleParameterAnnotationsAttr(list);
    }
}
