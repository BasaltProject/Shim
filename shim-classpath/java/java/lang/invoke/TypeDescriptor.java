/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package java.lang.invoke;

import java.util.List;

public interface TypeDescriptor {
    String descriptorString();

    interface OfField<F extends OfField<F>> extends TypeDescriptor {
        boolean isArray();
        boolean isPrimitive();
        F componentType();
        F arrayType();
    }

    interface OfMethod<F extends OfField<F>, M extends OfMethod<F, M>> extends TypeDescriptor {
        int parameterCount();
        F parameterType(int index);
        F returnType();
        F[] parameterArray();
        List<F> parameterList();
        M changeReturnType(F returnType);
        M changeParameterType(int index, F parameterType);
        M dropParameterTypes(int start, int end);
        M insertParameterTypes(int position, F... parameterTypes);
    }
}
