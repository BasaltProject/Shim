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
import java.lang.classfile.BootstrapMethodEntry;
import java.util.List;

public interface BootstrapMethodsAttribute extends Attribute<BootstrapMethodsAttribute> {
    List<BootstrapMethodEntry> bootstrapMethods();

    int bootstrapMethodsSize();
}
