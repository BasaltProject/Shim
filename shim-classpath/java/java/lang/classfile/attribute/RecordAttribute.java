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
import java.lang.classfile.ClassElement;
import java.util.Arrays;
import java.util.List;

public interface RecordAttribute extends Attribute<RecordAttribute>, ClassElement {
    List<RecordComponentInfo> components();

    static RecordAttribute of(List<RecordComponentInfo> components) {
        return new shim.classfile.RecordAttr(components);
    }

    static RecordAttribute of(RecordComponentInfo... components) {
        return of(Arrays.asList(components));
    }
}
