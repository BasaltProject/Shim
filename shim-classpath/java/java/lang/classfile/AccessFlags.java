/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.lang.classfile;

import java.lang.reflect.AccessFlag;
import java.util.Set;

public interface AccessFlags extends ClassElement, MethodElement, FieldElement {
    Set<AccessFlag> flags();
    AccessFlag.Location location();
    int flagsMask();
    boolean has(AccessFlag accessFlag);
}
