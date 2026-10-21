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
import java.lang.classfile.attribute.EnclosingMethodAttribute;
import java.lang.classfile.constantpool.ClassEntry;
import java.lang.classfile.constantpool.NameAndTypeEntry;
import java.util.Objects;
import java.util.Optional;

public final class EnclosingMethodAttr extends AttrImpl<EnclosingMethodAttribute>
        implements EnclosingMethodAttribute {
    public static final Mapper<EnclosingMethodAttribute> MAPPER = new Mapper<>("EnclosingMethod",
        AttributeStability.CP_REFS, (owner, r, p) -> new EnclosingMethodAttr(r, p), EnclosingMethodAttr::write);

    private ClassEntry enclosingClass;
    private NameAndTypeEntry method;
    private boolean methodRead;

    public EnclosingMethodAttr(ClassEntry enclosingClass, Optional<NameAndTypeEntry> method) {
        super(MAPPER);
        this.enclosingClass = Objects.requireNonNull(enclosingClass);
        this.method = method.orElse(null);
        this.methodRead = true;
    }

    private EnclosingMethodAttr(ClassReaderImpl r, int p) {
        super(MAPPER, r, p);
    }

    @Override
    public ClassEntry enclosingClass() {
        if (enclosingClass == null) {
            enclosingClass = reader.readEntry(pos, ClassEntry.class);
        }
        return enclosingClass;
    }

    @Override
    public Optional<NameAndTypeEntry> enclosingMethod() {
        if (!methodRead) {
            method = reader.readEntryOrNull(pos + 2, NameAndTypeEntry.class);
            methodRead = true;
        }
        return Optional.ofNullable(method);
    }

    private static void write(BufWriterImpl out, EnclosingMethodAttribute a) {
        if (!writeDirect(out, a)) {
            out.writeIndex(a.enclosingClass());
            out.writeIndexOrZero(a.enclosingMethod().orElse(null));
        }
    }
}
