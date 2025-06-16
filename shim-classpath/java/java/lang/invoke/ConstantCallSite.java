/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package java.lang.invoke;

public class ConstantCallSite extends CallSite {
    public ConstantCallSite(MethodHandle target) {
        super(target);
    }

    public final MethodHandle getTarget() {
        return target();
    }

    public final void setTarget(MethodHandle target) {
        throw new UnsupportedOperationException();
    }

    public final MethodHandle dynamicInvoker() {
        return getTarget();
    }
}
