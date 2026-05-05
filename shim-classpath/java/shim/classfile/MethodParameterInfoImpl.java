/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.classfile;

import java.lang.classfile.attribute.MethodParameterInfo;
import java.lang.classfile.constantpool.Utf8Entry;
import java.util.Objects;
import java.util.Optional;

public final class MethodParameterInfoImpl implements MethodParameterInfo {
    private final Optional<Utf8Entry> name;
    private final int flagsMask;

    public MethodParameterInfoImpl(Optional<Utf8Entry> name, int flagsMask) {
        this.name = Objects.requireNonNull(name);
        this.flagsMask = flagsMask;
    }

    @Override public Optional<Utf8Entry> name() { return name; }
    @Override public int flagsMask() { return flagsMask; }

    @Override
    public boolean equals(Object o) {
        return o instanceof MethodParameterInfoImpl && ((MethodParameterInfoImpl) o).name.equals(name)
            && ((MethodParameterInfoImpl) o).flagsMask == flagsMask;
    }

    @Override public int hashCode() { return name.hashCode() * 31 + flagsMask; }

    @Override
    public String toString() {
        return "UnboundMethodParameterInfo[name=" + name + ", flagsMask=" + flagsMask + "]";
    }
}
