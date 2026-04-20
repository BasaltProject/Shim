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
import java.lang.classfile.MethodElement;
import java.util.Arrays;
import java.util.List;

public interface MethodParametersAttribute extends Attribute<MethodParametersAttribute>, MethodElement {
    List<MethodParameterInfo> parameters();

    static MethodParametersAttribute of(List<MethodParameterInfo> parameters) {
        return new shim.classfile.MethodParametersAttr(parameters);
    }

    static MethodParametersAttribute of(MethodParameterInfo... parameters) {
        return of(Arrays.asList(parameters));
    }
}
