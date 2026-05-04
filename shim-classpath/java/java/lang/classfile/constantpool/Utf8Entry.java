/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.lang.classfile.constantpool;

import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;

public interface Utf8Entry extends CharSequence, AnnotationConstantValueEntry {
    String stringValue();
    boolean equalsString(String text);
    boolean isFieldType(ClassDesc classDesc);
    boolean isMethodType(MethodTypeDesc methodTypeDesc);
}
