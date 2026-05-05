/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.classfile;

import java.lang.classfile.AttributeMapper.AttributeStability;
import java.lang.classfile.attribute.SignatureAttribute;
import java.lang.classfile.constantpool.Utf8Entry;
import java.util.Objects;

public final class SignatureAttr extends AttrImpl<SignatureAttribute> implements SignatureAttribute {
    public static final Mapper<SignatureAttribute> MAPPER = new Mapper<>("Signature", AttributeStability.CP_REFS,
        (owner, r, p) -> new SignatureAttr(r, p), SignatureAttr::write);

    private Utf8Entry value;

    public SignatureAttr(Utf8Entry value) {
        super(MAPPER);
        this.value = Objects.requireNonNull(value);
    }

    private SignatureAttr(ClassReaderImpl r, int p) {
        super(MAPPER, r, p);
    }

    @Override
    public Utf8Entry signature() {
        if (value == null) {
            value = reader.readEntry(pos, Utf8Entry.class);
        }
        return value;
    }

    private static void write(BufWriterImpl out, SignatureAttribute a) {
        if (!writeDirect(out, a)) {
            out.writeIndex(a.signature());
        }
    }
}
