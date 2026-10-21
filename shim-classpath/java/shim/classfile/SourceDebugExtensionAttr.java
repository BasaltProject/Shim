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
import java.lang.classfile.attribute.SourceDebugExtensionAttribute;
import java.util.Objects;

public final class SourceDebugExtensionAttr extends AttrImpl<SourceDebugExtensionAttribute>
        implements SourceDebugExtensionAttribute {
    public static final Mapper<SourceDebugExtensionAttribute> MAPPER = new Mapper<>("SourceDebugExtension",
        AttributeStability.STATELESS, (owner, r, p) -> new SourceDebugExtensionAttr(r, p),
        SourceDebugExtensionAttr::write);

    private final byte[] contents;

    public SourceDebugExtensionAttr(byte[] contents) {
        super(MAPPER);
        this.contents = Objects.requireNonNull(contents);
    }

    private SourceDebugExtensionAttr(ClassReaderImpl r, int p) {
        super(MAPPER, r, p);
        this.contents = null;
    }

    @Override
    public byte[] contents() {
        return reader == null ? contents : reader.readBytes(pos, payloadLength());
    }

    private static void write(BufWriterImpl out, SourceDebugExtensionAttribute a) {
        if (!writeDirect(out, a)) {
            out.writeBytes(a.contents());
        }
    }
}
