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
import java.lang.classfile.Attributes;
import java.lang.classfile.ClassFile;
import java.lang.classfile.CodeElement;
import java.lang.classfile.CodeModel;
import java.lang.classfile.attribute.StackMapTableAttribute;
import java.lang.constant.MethodTypeDesc;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Writes the body of a {@code Code} attribute: limits, bytecode, exception table and code attributes. */
final class CodeWriter {
    private CodeWriter() { }

    /** Whether a body built with short jumps fixed has a short forward jump that cannot reach its target. */
    static boolean needsLongJumps(BufferedCode code) {
        ClassFileImpl cf = code.context().classFile;
        if (cf == null || cf.shortJumps != ClassFile.ShortJumpsOption.FIX_SHORT_JUMPS) {
            return false;
        }
        CodeAssembler a = new CodeAssembler(code.context().pool, cf, code.builder().adoptedEntries());
        a.scan(code.elements());
        return !a.layout(false);
    }

    /** Fails as soon as a body is built when one of its short jumps cannot reach its target. */
    static void checkShortJumps(BufferedCode code, PoolImpl pool) {
        CodeAssembler a = new CodeAssembler(pool, code.context().classFile, code.builder().adoptedEntries());
        a.scan(code.elements());
        a.layout(false);
    }

    /** Writes {@code code} as a framed {@code Code} attribute of the method described by {@code ctx}. */
    static void writeAttribute(BufWriterImpl out, CodeModel code, MethodContext ctx) {
        if (code instanceof CodeImpl && out.canWriteDirect(((CodeImpl) code).reader)) {
            int pos = CodeTables.frame(out, Attributes.NAME_CODE);
            CodeImpl.MAPPER.writeAttribute(out, (CodeImpl) code);
            CodeTables.end(out, pos);
            return;
        }
        List<CodeElement> elements;
        CodeModel original = null;
        Map<CodeElement, Integer> adopted = null;
        if (code instanceof BufferedCode) {
            CodeBuilderImpl builder = ((BufferedCode) code).builder();
            elements = ((BufferedCode) code).elements();
            original = builder.original();
            if (builder.context.pool == out.constantPool()) {
                adopted = builder.adoptedEntries();
            }
        } else {
            List<CodeElement> all = new ArrayList<>();
            code.forEach(all::add);
            elements = all;
        }
        ClassFileImpl cf = ctx.classFile;
        PoolImpl pool = (PoolImpl) out.constantPool();
        CodeAssembler a = new CodeAssembler(pool, cf, adopted);
        a.scan(elements);
        if (!a.layout(false)) {
            a.layout(true);
        }
        int length = a.codeLength();
        int pos = CodeTables.frame(out, Attributes.NAME_CODE);
        if (length == 0 || length >= 65536) {
            throw new IllegalArgumentException(String.format("Code length %d is outside the allowed range in %s%s",
                length, ctx.name.stringValue(), describe(ctx.type.stringValue())));
        }
        byte[] bytes = a.encode();
        CodeLabelMap labels = new CodeLabelMap(ctx);
        a.bindAll(labels);
        List<int[]> handlers = a.handlerRows(false);
        boolean frames = cf.stackMaps == ClassFile.StackMapsOption.GENERATE_STACK_MAPS
            || cf.stackMaps == ClassFile.StackMapsOption.STACK_MAPS_WHEN_REQUIRED && ctx.majorVersion() >= 50;
        int maxStack;
        int maxLocals;
        Attribute<?> stackMaps = null;
        CodeImpl same = matchingOriginal(out, original, bytes, handlers);
        if (same != null && cf.stackMaps != ClassFile.StackMapsOption.GENERATE_STACK_MAPS) {
            maxStack = same.maxStack();
            maxLocals = same.maxLocals();
            if (cf.stackMaps != ClassFile.StackMapsOption.DROP_STACK_MAPS) {
                Optional<StackMapTableAttribute> sm = same.findAttribute(Attributes.stackMapTable());
                if (sm.isPresent()) {
                    stackMaps = sm.get();
                }
            }
        } else {
            StackMapGenerator.Result r = StackMapGenerator.generate(bytes, length, handlers, pool, ctx,
                cf.hierarchyResolver(), labels, frames);
            maxStack = r.maxStack;
            maxLocals = r.maxLocals;
            handlers = r.handlers;
            if (frames && !r.frames.isEmpty()) {
                stackMaps = new StackMapTableAttr(r.frames);
            }
        }
        if (stackMaps != null) {
            a.removeStackMaps();
        }
        out.writeU2(maxStack);
        out.writeU2(maxLocals);
        out.writeInt(length);
        out.writeBytes(bytes);
        a.handlerRows(true);
        out.writeU2(handlers.size());
        for (int[] h : handlers) {
            out.writeU2(h[0]);
            out.writeU2(h[1]);
            out.writeU2(h[2]);
            out.writeU2(h[3]);
        }
        LabelContext saved = out.labelContext();
        out.setLabelContext(labels);
        try {
            int countPos = out.size();
            out.writeU2(0);
            int count = 0;
            for (Attribute<?> attr : a.attributes) {
                if (AttrWriter.write(out, cf, attr, null)) {
                    count++;
                }
            }
            count += CodeTables.write(out, a);
            if (stackMaps != null && AttrWriter.write(out, cf, stackMaps, null)) {
                count++;
            }
            out.patchInt(countPos, 2, count);
        } finally {
            out.setLabelContext(saved);
        }
        CodeTables.end(out, pos);
    }

    private static String describe(String descriptor) {
        try {
            return MethodTypeDesc.ofDescriptor(descriptor).displayDescriptor();
        } catch (IllegalArgumentException e) {
            return descriptor;
        }
    }

    /** The parsed body a transformed body came from when it shares the pool and has the same code and handlers. */
    private static CodeImpl matchingOriginal(BufWriterImpl out, CodeModel original, byte[] bytes,
                                             List<int[]> handlers) {
        if (!(original instanceof CodeImpl)) {
            return null;
        }
        CodeImpl c = (CodeImpl) original;
        if (!out.canWriteDirect(c.reader) || c.codeLength() != bytes.length
                || !Arrays.equals(c.codeArray(), bytes)) {
            return null;
        }
        int p = c.codeStart + c.codeLength();
        int n = c.reader.readU2(p);
        if (n != handlers.size()) {
            return null;
        }
        for (int i = 0; i < n; i++) {
            int[] h = handlers.get(i);
            for (int k = 0; k < 4; k++) {
                if (c.reader.readU2(p + 2 + 8 * i + 2 * k) != h[k]) {
                    return null;
                }
            }
        }
        return c;
    }
}
