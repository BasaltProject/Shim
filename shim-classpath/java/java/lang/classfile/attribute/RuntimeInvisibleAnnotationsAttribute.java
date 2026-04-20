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
import java.lang.classfile.ClassElement;
import java.lang.classfile.FieldElement;
import java.lang.classfile.MethodElement;
import java.util.List;
import shim.classfile.RuntimeInvisibleAnnotationsAttr;

public interface RuntimeInvisibleAnnotationsAttribute extends Attribute<RuntimeInvisibleAnnotationsAttribute>,
        ClassElement, MethodElement, FieldElement {
    List<Annotation> annotations();
    public static RuntimeInvisibleAnnotationsAttribute of(List<Annotation> list) {
        return new RuntimeInvisibleAnnotationsAttr(list);
    }
    public static RuntimeInvisibleAnnotationsAttribute of(Annotation... annotations) {
        return of(List.of(annotations));
    }
}
