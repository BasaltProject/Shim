/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.lang.classfile;

public interface AttributeMapper<A extends Attribute<A>> {
    String name();
    void writeAttribute(BufWriter bufWriter, A a);
    A readAttribute(AttributedElement attributedElement, ClassReader classReader, int i);
    AttributeMapper.AttributeStability stability();
    default boolean allowMultiple() { return false; }

    public enum AttributeStability {
        STATELESS,
        CP_REFS,
        LABELS,
        UNSTABLE,
        UNKNOWN;
    }
}
