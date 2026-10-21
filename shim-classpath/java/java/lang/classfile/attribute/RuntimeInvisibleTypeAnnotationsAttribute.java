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
import java.lang.classfile.CodeElement;
import java.lang.classfile.FieldElement;
import java.lang.classfile.MethodElement;
import java.lang.classfile.TypeAnnotation;
import java.util.List;
import shim.classfile.RuntimeInvisibleTypeAnnotationsAttr;

public interface RuntimeInvisibleTypeAnnotationsAttribute extends Attribute<RuntimeInvisibleTypeAnnotationsAttribute>,
        ClassElement, MethodElement, FieldElement, CodeElement {
    List<TypeAnnotation> annotations();
    public static RuntimeInvisibleTypeAnnotationsAttribute of(List<TypeAnnotation> list) {
        return new RuntimeInvisibleTypeAnnotationsAttr(list);
    }
    public static RuntimeInvisibleTypeAnnotationsAttribute of(TypeAnnotation... typeAnnotations) {
        return of(List.of(typeAnnotations));
    }
}
