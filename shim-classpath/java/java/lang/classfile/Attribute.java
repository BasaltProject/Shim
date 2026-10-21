/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.lang.classfile;

import java.lang.classfile.constantpool.Utf8Entry;

public interface Attribute<A extends Attribute<A>> extends ClassFileElement {
    Utf8Entry attributeName();
    AttributeMapper<A> attributeMapper();
}
