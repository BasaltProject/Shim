/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.classfile;

import java.lang.classfile.AttributeMapper;
import java.lang.classfile.attribute.UnknownAttribute;
import java.lang.classfile.constantpool.Utf8Entry;

/** An attribute no mapper claims: its name and raw bytes are preserved. */
public final class UnknownAttr extends AttrBase<UnknownAttribute> implements UnknownAttribute {
    public static final AttributeMapper<UnknownAttribute> MAPPER = new AttributeMapper<UnknownAttribute>() {
        @Override public String name() { return "<unknown>"; }

        @Override
        public UnknownAttribute readAttribute(java.lang.classfile.AttributedElement owner,
                                              java.lang.classfile.ClassReader cf, int pos) {
            ClassReaderImpl r = (ClassReaderImpl) cf;
            Utf8Entry name = r.entryByIndex(r.readU2(pos - 6), Utf8Entry.class);
            return new UnknownAttr(name, r.readBytes(pos, r.readInt(pos - 4)));
        }

        @Override
        public void writeAttribute(java.lang.classfile.BufWriter out, UnknownAttribute a) {
            out.writeBytes(a.contents());
        }

        @Override public AttributeStability stability() { return AttributeStability.UNKNOWN; }
        @Override public boolean allowMultiple() { return true; }
    };

    private final String nameText;
    private final byte[] contents;

    private UnknownAttr(Utf8Entry name, byte[] contents) {
        super(null, name);
        this.nameText = name.stringValue();
        this.contents = contents;
    }

    @Override public byte[] contents() { return contents; }
    @Override public java.lang.classfile.AttributeMapper<UnknownAttribute> attributeMapper() { return new Named(nameText); }
    @Override public String toString() { return "Attribute[name=" + nameText + "]"; }

    private static final class Named implements AttributeMapper<UnknownAttribute> {
        private final String name;

        Named(String name) { this.name = name; }

        @Override public String name() { return name; }

        @Override
        public UnknownAttribute readAttribute(java.lang.classfile.AttributedElement owner,
                                              java.lang.classfile.ClassReader cf, int pos) {
            return MAPPER.readAttribute(owner, cf, pos);
        }

        @Override public void writeAttribute(java.lang.classfile.BufWriter out, UnknownAttribute a) { out.writeBytes(a.contents()); }
        @Override public AttributeStability stability() { return AttributeStability.UNKNOWN; }
        @Override public boolean allowMultiple() { return true; }
    }
}
