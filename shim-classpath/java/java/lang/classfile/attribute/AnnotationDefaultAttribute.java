/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.lang.classfile.attribute;

import java.lang.classfile.AnnotationValue;
import java.lang.classfile.Attribute;
import java.lang.classfile.MethodElement;
import shim.classfile.AnnotationDefaultAttr;

public interface AnnotationDefaultAttribute extends Attribute<AnnotationDefaultAttribute>, MethodElement {
    public static AnnotationDefaultAttribute of(AnnotationValue annotationValue) {
        return new AnnotationDefaultAttr(annotationValue);
    }
    AnnotationValue defaultValue();
}
