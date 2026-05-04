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
import java.lang.classfile.Attributes;
import java.lang.classfile.ClassFile;
import java.lang.classfile.CodeModel;
import java.lang.classfile.attribute.BootstrapMethodsAttribute;
import java.lang.classfile.attribute.CodeAttribute;
import java.lang.classfile.constantpool.LoadableConstantEntry;
import shim.classfile.Entries.BsmE;

/** Writes attributes with their name and length framing. */
final class AttrWriter {
    private AttrWriter() { }

    /** Writes {@code element} as an attribute; returns false when the options drop it. */
    @SuppressWarnings({"unchecked", "rawtypes"})
    static boolean write(BufWriterImpl out, ClassFileImpl cf, Object element, MethodContext ctx) {
        if (element instanceof CodeModel && ctx != null) {
            if (element instanceof CodeAttribute && ((CodeAttribute) element).attributeMapper() != Attributes.code()) {
                return false;
            }
            CodeWriter.writeAttribute(out, (CodeModel) element, ctx);
            return true;
        }
        Attribute attr = (Attribute) element;
        AttributeMapper mapper = attr.attributeMapper();
        if (attr instanceof BootstrapMethodsAttribute) {
            return false;
        }
        AttributeMapper.AttributeStability st = mapper.stability();
        if (cf.attributesProcessing == ClassFile.AttributesProcessingOption.DROP_UNKNOWN_ATTRIBUTES
                && st == AttributeMapper.AttributeStability.UNKNOWN) {
            return false;
        }
        if (cf.attributesProcessing == ClassFile.AttributesProcessingOption.DROP_UNSTABLE_ATTRIBUTES
                && (st == AttributeMapper.AttributeStability.UNKNOWN || st == AttributeMapper.AttributeStability.UNSTABLE)) {
            return false;
        }
        if (attr instanceof RuntimeVisibleTypeAnnotationsAttr
                && ((RuntimeVisibleTypeAnnotationsAttr) attr).copyDirect(out)
                || attr instanceof RuntimeInvisibleTypeAnnotationsAttr
                && ((RuntimeInvisibleTypeAnnotationsAttr) attr).copyDirect(out)
                || attr instanceof StackMapTableAttr && ((StackMapTableAttr) attr).copyDirect(out)) {
            return true;
        }
        if (!AttrSupport.framedByWriter(attr)) {
            mapper.writeAttribute(out, attr);
            return true;
        }
        int pos = frame(out, mapper.name());
        mapper.writeAttribute(out, attr);
        end(out, pos);
        return true;
    }

    private static int frame(BufWriterImpl out, String name) {
        out.writeIndex(out.constantPool().utf8Entry(name));
        int pos = out.size();
        out.writeInt(0);
        return pos;
    }

    private static void end(BufWriterImpl out, int pos) {
        out.patchInt(pos, 4, out.size() - pos - 4);
    }

    static void writeBootstrapMethods(BufWriterImpl out, PoolImpl pool) {
        int pos = frame(out, Attributes.NAME_BOOTSTRAP_METHODS);
        out.writeU2(pool.bootstrapMethods().size());
        for (BsmE b : pool.bootstrapMethods()) {
            out.writeIndex(b.bootstrapMethod());
            out.writeU2(b.arguments().size());
            for (LoadableConstantEntry a : b.arguments()) {
                out.writeIndex(a);
            }
        }
        end(out, pos);
    }
}
