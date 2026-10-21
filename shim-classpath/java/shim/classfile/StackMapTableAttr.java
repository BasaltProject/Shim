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
import java.lang.classfile.AttributedElement;
import java.lang.classfile.Attributes;
import java.lang.classfile.ClassModel;
import java.lang.classfile.CodeModel;
import java.lang.classfile.MethodModel;
import java.lang.classfile.attribute.StackMapFrameInfo;
import java.lang.classfile.attribute.StackMapFrameInfo.ObjectVerificationTypeInfo;
import java.lang.classfile.attribute.StackMapFrameInfo.SimpleVerificationTypeInfo;
import java.lang.classfile.attribute.StackMapFrameInfo.UninitializedVerificationTypeInfo;
import java.lang.classfile.attribute.StackMapFrameInfo.VerificationTypeInfo;
import java.lang.classfile.attribute.StackMapTableAttribute;
import java.lang.classfile.constantpool.ClassEntry;
import java.lang.classfile.constantpool.ConstantPoolBuilder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/** The {@code StackMapTable} attribute: frames decoded on demand when read, compact encodings when written. */
public final class StackMapTableAttr extends AttrBase<StackMapTableAttribute> implements StackMapTableAttribute {
    public static final Mapper<StackMapTableAttribute> MAPPER = new Mapper<>(Attributes.NAME_STACK_MAP_TABLE,
        AttributeMapper.AttributeStability.LABELS, StackMapTableAttr::read, StackMapTableAttr::write);

    /** Implemented by a label context that knows the method its code belongs to. */
    public interface MethodAware {
        MethodContext methodContext();
    }

    private static final SimpleVerificationTypeInfo[] SIMPLE = SimpleVerificationTypeInfo.values();

    private List<StackMapFrameInfo> entries;
    private final AttributedElement owner;
    private final ClassReaderImpl reader;
    private final int pos;

    public StackMapTableAttr(List<StackMapFrameInfo> entries) {
        super(MAPPER);
        this.entries = List.copyOf(entries);
        this.owner = null;
        this.reader = null;
        this.pos = 0;
    }

    private StackMapTableAttr(AttributedElement owner, ClassReaderImpl reader, int pos) {
        super(MAPPER, reader.readEntry(pos - 6, java.lang.classfile.constantpool.Utf8Entry.class));
        this.owner = owner;
        this.reader = reader;
        this.pos = pos;
    }

    /** The body length a parsed attribute declares, or -1 for a built one. */
    int declaredLength() {
        return reader == null ? -1 : reader.readInt(pos - 4);
    }

    /** Copies a parsed attribute with its header unchanged when the target pool is the source pool. */
    boolean copyDirect(BufWriterImpl out) {
        if (reader == null || !out.canWriteDirect(reader)) {
            return false;
        }
        reader.copyBytesTo(out, pos - 6, 6 + reader.readInt(pos - 4));
        return true;
    }

    private static StackMapTableAttribute read(AttributedElement owner, ClassReaderImpl cf, int pos) {
        return new StackMapTableAttr(owner, cf, pos);
    }

    @Override
    public List<StackMapFrameInfo> entries() {
        if (entries == null) {
            entries = decode();
        }
        return entries;
    }

    private List<StackMapFrameInfo> decode() {
        if (!(owner instanceof LabelContext)) {
            throw new IllegalArgumentException("StackMapTable outside of a code attribute");
        }
        return decodeFrames(reader, pos, (LabelContext) owner, initialLocalsOf(owner));
    }

    /** Decodes the frames of a {@code StackMapTable} body at {@code pos}. */
    public static List<StackMapFrameInfo> decodeFrames(ClassReaderImpl reader, int pos, LabelContext labels,
                                                       List<VerificationTypeInfo> initialLocals) {
        List<VerificationTypeInfo> locals = initialLocals;
        int p = pos;
        int n = reader.readU2(p);
        p += 2;
        List<StackMapFrameInfo> out = new ArrayList<>(n);
        int bci = -1;
        int[] cursor = new int[1];
        for (int i = 0; i < n; i++) {
            int type = reader.readU1(p++);
            List<VerificationTypeInfo> stack = List.of();
            int delta;
            cursor[0] = p;
            if (type < 64) {
                delta = type;
            } else if (type < 128) {
                delta = type - 64;
                stack = List.of(readType(reader, labels, cursor));
            } else if (type < 247) {
                throw new IllegalArgumentException("Invalid stackmap frame type: " + type);
            } else {
                delta = reader.readU2(cursor[0]);
                cursor[0] += 2;
                if (type == 247) {
                    stack = List.of(readType(reader, labels, cursor));
                } else if (type < 251) {
                    locals = locals.subList(0, locals.size() - (251 - type));
                } else if (type > 251 && type < 255) {
                    List<VerificationTypeInfo> l = new ArrayList<>(locals);
                    for (int k = 251; k < type; k++) {
                        l.add(readType(reader, labels, cursor));
                    }
                    locals = l;
                } else if (type == 255) {
                    locals = readTypes(reader, labels, cursor);
                    stack = readTypes(reader, labels, cursor);
                }
            }
            p = cursor[0];
            bci += delta + 1;
            out.add(new StackMapFrameImpl(type, labels.getLabel(bci), locals, stack));
        }
        return List.copyOf(out);
    }

    private static List<VerificationTypeInfo> readTypes(ClassReaderImpl reader, LabelContext labels, int[] cursor) {
        int n = reader.readU2(cursor[0]);
        cursor[0] += 2;
        List<VerificationTypeInfo> l = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            l.add(readType(reader, labels, cursor));
        }
        return l;
    }

    private static VerificationTypeInfo readType(ClassReaderImpl reader, LabelContext labels, int[] cursor) {
        int p = cursor[0];
        int tag = reader.readU1(p);
        switch (tag) {
            case VerificationTypeInfo.ITEM_OBJECT:
                cursor[0] = p + 3;
                return new ObjectVerificationTypeInfoImpl(reader.readEntry(p + 1, ClassEntry.class));
            case VerificationTypeInfo.ITEM_UNINITIALIZED:
                cursor[0] = p + 3;
                return new UninitializedVerificationTypeInfoImpl(labels.getLabel(reader.readU2(p + 1)));
            default:
                if (tag > VerificationTypeInfo.ITEM_UNINITIALIZED) {
                    throw new IllegalArgumentException("Invalid verification type tag: " + tag);
                }
                cursor[0] = p + 1;
                return SIMPLE[tag];
        }
    }

    private static List<VerificationTypeInfo> initialLocalsOf(AttributedElement owner) {
        if (owner instanceof CodeModel) {
            Optional<MethodModel> m = ((CodeModel) owner).parent();
            if (m.isPresent()) {
                MethodModel mm = m.get();
                Optional<ClassModel> c = mm.parent();
                ClassEntry thisClass = c.isPresent() ? c.get().thisClass() : null;
                return initialLocals(new PoolImpl(), thisClass, mm.methodName().stringValue(),
                    mm.methodType().stringValue(), mm.flags().flagsMask());
            }
        }
        return List.of();
    }

    /** The locals of the implicit frame at the start of a method. */
    public static List<VerificationTypeInfo> initialLocals(MethodContext m) {
        return initialLocals(new PoolImpl(), m.thisClass, m.name.stringValue(), m.type.stringValue(), m.flags);
    }

    static List<VerificationTypeInfo> initialLocals(ConstantPoolBuilder pool, ClassEntry thisClass, String name,
                                                    String desc, int flags) {
        List<VerificationTypeInfo> l = new ArrayList<>();
        if ((flags & java.lang.classfile.ClassFile.ACC_STATIC) == 0) {
            if ("<init>".equals(name) && (thisClass == null || !thisClass.name().equalsString("java/lang/Object"))) {
                l.add(SimpleVerificationTypeInfo.UNINITIALIZED_THIS);
            } else if (thisClass != null) {
                l.add(new ObjectVerificationTypeInfoImpl(thisClass));
            } else {
                l.add(SimpleVerificationTypeInfo.TOP);
            }
        }
        int i = 1;
        while (i < desc.length() && desc.charAt(i) != ')') {
            char c = desc.charAt(i);
            int end = i;
            while (desc.charAt(end) == '[') {
                end++;
            }
            if (desc.charAt(end) == 'L') {
                end = desc.indexOf(';', end);
            }
            if (c == 'L') {
                l.add(new ObjectVerificationTypeInfoImpl(pool.classEntry(pool.utf8Entry(desc.substring(i + 1, end)))));
            } else if (c == '[') {
                l.add(new ObjectVerificationTypeInfoImpl(pool.classEntry(pool.utf8Entry(desc.substring(i, end + 1)))));
            } else if (c == 'J') {
                l.add(SimpleVerificationTypeInfo.LONG);
            } else if (c == 'D') {
                l.add(SimpleVerificationTypeInfo.DOUBLE);
            } else if (c == 'F') {
                l.add(SimpleVerificationTypeInfo.FLOAT);
            } else {
                l.add(SimpleVerificationTypeInfo.INTEGER);
            }
            i = end + 1;
        }
        return l;
    }

    private static void write(BufWriterImpl out, StackMapTableAttribute attr) {
        LabelContext labels = out.labelContext();
        if (labels == null) {
            throw new IllegalArgumentException("StackMapTable outside of a code attribute");
        }
        List<VerificationTypeInfo> initial = labels instanceof MethodAware
            ? initialLocals(((MethodAware) labels).methodContext()) : List.<VerificationTypeInfo>of();
        writeFrames(out, attr.entries(), initial, labels);
    }

    /** Writes the body of a {@code StackMapTable} attribute for the code of {@code m}, labels from the writer. */
    public static void writeFrames(BufWriterImpl out, List<StackMapFrameInfo> frames, MethodContext m) {
        writeFrames(out, frames, initialLocals(m), out.labelContext());
    }

    /** Writes the body of a {@code StackMapTable} attribute, choosing the most compact encoding of each frame. */
    public static void writeFrames(BufWriterImpl out, List<StackMapFrameInfo> frames,
                                   List<VerificationTypeInfo> initialLocals, LabelContext labels) {
        out.writeU2(frames.size());
        int n = frames.size();
        long[] order = new long[n];
        for (int i = 0; i < n; i++) {
            int bci = labels.labelToBci(frames.get(i).target());
            if (bci < 0) {
                throw new IndexOutOfBoundsException("bitIndex < 0: " + bci);
            }
            order[i] = ((long) bci << 32) | i;
        }
        Arrays.sort(order);
        List<VerificationTypeInfo> prev = initialLocals;
        int prevBci = -1;
        for (int k = 0; k < n; k++) {
            StackMapFrameInfo f = frames.get((int) order[k]);
            int bci = (int) (order[k] >>> 32);
            if (bci == prevBci) {
                throw new IllegalArgumentException("Duplicated stack frame bytecode index: " + bci);
            }
            int delta = bci - prevBci - 1;
            List<VerificationTypeInfo> locals = f.locals();
            List<VerificationTypeInfo> stack = f.stack();
            int diff = locals.size() - prev.size();
            if (stack.isEmpty() && locals.equals(prev)) {
                if (delta < 64) {
                    out.writeU1(delta);
                } else {
                    out.writeU1(251);
                    out.writeU2(delta);
                }
            } else if (stack.size() == 1 && locals.equals(prev)) {
                if (delta < 64) {
                    out.writeU1(64 + delta);
                } else {
                    out.writeU1(247);
                    out.writeU2(delta);
                }
                writeType(out, stack.get(0), labels);
            } else if (stack.isEmpty() && diff < 0 && diff >= -3 && isPrefix(locals, prev)) {
                out.writeU1(251 + diff);
                out.writeU2(delta);
            } else if (stack.isEmpty() && diff > 0 && diff <= 3 && isPrefix(prev, locals)) {
                out.writeU1(251 + diff);
                out.writeU2(delta);
                for (int i = prev.size(); i < locals.size(); i++) {
                    writeType(out, locals.get(i), labels);
                }
            } else {
                out.writeU1(255);
                out.writeU2(delta);
                out.writeU2(locals.size());
                for (VerificationTypeInfo v : locals) {
                    writeType(out, v, labels);
                }
                out.writeU2(stack.size());
                for (VerificationTypeInfo v : stack) {
                    writeType(out, v, labels);
                }
            }
            prev = locals;
            prevBci = bci;
        }
    }

    private static boolean isPrefix(List<VerificationTypeInfo> shorter, List<VerificationTypeInfo> longer) {
        for (int i = 0; i < shorter.size(); i++) {
            if (!shorter.get(i).equals(longer.get(i))) {
                return false;
            }
        }
        return true;
    }

    private static void writeType(BufWriterImpl out, VerificationTypeInfo v, LabelContext labels) {
        out.writeU1(v.tag());
        if (v instanceof ObjectVerificationTypeInfo) {
            out.writeIndex(((ObjectVerificationTypeInfo) v).className());
        } else if (v instanceof UninitializedVerificationTypeInfo) {
            out.writeU2(labels.labelToBci(((UninitializedVerificationTypeInfo) v).newTarget()));
        }
    }
}
