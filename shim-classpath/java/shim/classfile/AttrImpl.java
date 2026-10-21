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
import java.lang.classfile.AttributeMapper;
import java.lang.classfile.constantpool.Utf8Entry;

/** Base of the standard attributes: either built by a factory or bound to the bytes of a class reader. */
public abstract class AttrImpl<A extends Attribute<A>> extends AttrBase<A> {
    protected final ClassReaderImpl reader;
    protected final int pos;

    protected AttrImpl(AttributeMapper<A> mapper) {
        super(mapper);
        this.reader = null;
        this.pos = -1;
    }

    protected AttrImpl(AttributeMapper<A> mapper, ClassReaderImpl reader, int pos) {
        super(mapper, reader.entryByIndex(reader.readU2(pos - 6), Utf8Entry.class));
        this.reader = reader;
        this.pos = pos;
    }

    protected final int payloadLength() {
        return reader.readInt(pos - 4);
    }

    /** Copies the original body when the attribute was read from a pool the output can use as is. */
    static boolean writeDirect(BufWriterImpl out, Attribute<?> attribute) {
        if (attribute instanceof AttrImpl) {
            AttrImpl<?> a = (AttrImpl<?>) attribute;
            if (a.reader != null && out.canWriteDirect(a.reader)) {
                a.reader.copyBytesTo(out, a.pos, a.payloadLength());
                return true;
            }
        }
        return false;
    }
}
