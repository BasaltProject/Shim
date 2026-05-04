/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.classfile;

import java.lang.classfile.Attribute;
import java.lang.classfile.attribute.RecordComponentInfo;
import java.lang.classfile.constantpool.Utf8Entry;
import java.util.List;

/** A record component read from a class file; its attributes are read on first use. */
public final class BoundRecordComponentInfo implements RecordComponentInfo {
    private final ClassReaderImpl reader;
    private final int pos;
    private List<Attribute<?>> attributes;

    BoundRecordComponentInfo(ClassReaderImpl reader, int pos) {
        this.reader = reader;
        this.pos = pos;
    }

    @Override public Utf8Entry name() { return reader.readEntry(pos, Utf8Entry.class); }
    @Override public Utf8Entry descriptor() { return reader.readEntry(pos + 2, Utf8Entry.class); }

    @Override
    public List<Attribute<?>> attributes() {
        if (attributes == null) {
            attributes = List.copyOf(reader.readAttributes(this, pos + 4));
        }
        return attributes;
    }

    int end() {
        return reader.skipAttributes(pos + 4);
    }

    @Override
    public String toString() {
        return "jdk.internal.classfile.impl.BoundRecordComponentInfo@" + Integer.toHexString(hashCode());
    }
}
