/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.classfile;

import java.lang.constant.ClassDesc;

/** Name helpers shared by the interfaces and the implementation. */
public final class Names {
    private Names() { }

    public static String internalName(ClassDesc desc) {
        String d = desc.descriptorString();
        if (d.charAt(0) != 'L') {
            throw new IllegalArgumentException("Cannot be encoded as ClassEntry: " + desc.displayName());
        }
        return d.substring(1, d.length() - 1);
    }
}
