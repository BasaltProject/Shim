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
import java.lang.classfile.AttributedElement;
import java.lang.classfile.constantpool.Utf8Entry;
import java.lang.constant.ClassDesc;
import java.util.Arrays;
import java.util.List;

public interface RecordComponentInfo extends AttributedElement {
    Utf8Entry name();

    Utf8Entry descriptor();

    default ClassDesc descriptorSymbol() {
        return ClassDesc.ofDescriptor(descriptor().stringValue());
    }

    static RecordComponentInfo of(Utf8Entry name, Utf8Entry descriptor, List<Attribute<?>> attributes) {
        return new shim.classfile.RecordComponentInfoImpl(name, descriptor, attributes);
    }

    static RecordComponentInfo of(Utf8Entry name, Utf8Entry descriptor, Attribute<?>... attributes) {
        return of(name, descriptor, Arrays.asList(attributes));
    }

    static RecordComponentInfo of(String name, ClassDesc descriptor, List<Attribute<?>> attributes) {
        shim.classfile.PoolImpl pool = shim.classfile.AttrSupport.pool();
        return of(pool.utf8Entry(name), pool.utf8Entry(descriptor), attributes);
    }

    static RecordComponentInfo of(String name, ClassDesc descriptor, Attribute<?>... attributes) {
        return of(name, descriptor, Arrays.asList(attributes));
    }
}
