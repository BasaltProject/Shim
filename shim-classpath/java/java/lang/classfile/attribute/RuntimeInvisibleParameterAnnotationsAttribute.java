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
import shim.classfile.RuntimeInvisibleParameterAnnotationsAttr;

public interface RuntimeInvisibleParameterAnnotationsAttribute
        extends Attribute<RuntimeInvisibleParameterAnnotationsAttribute>, MethodElement {
    List<List<Annotation>> parameterAnnotations();
    public static RuntimeInvisibleParameterAnnotationsAttribute of(List<List<Annotation>> list) {
        return new RuntimeInvisibleParameterAnnotationsAttr(list);
    }
}
