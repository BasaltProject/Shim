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
import java.lang.classfile.BootstrapMethodEntry;
import java.lang.classfile.BufWriter;
import java.lang.classfile.ClassReader;
import java.lang.classfile.attribute.BootstrapMethodsAttribute;
import java.lang.classfile.constantpool.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import shim.classfile.Entries.*;

/** Reads a class file image: the constant pool is decoded on demand, everything else by offset. */
public final class ClassReaderImpl implements ClassReader {
    private final byte[] buf;
    private final Function<Utf8Entry, AttributeMapper<?>> customAttributes;
    private final int[] offsets;
    private final Entry[] cache;
    private final int poolEnd;
    private int membersStart = -1;
    private int methodsStart;
    private int attributesStart;
    private List<BsmE> bsms;
    /** The options of the context that parsed this class. */
    public ClassFileImpl options = ClassFileImpl.DEFAULT;

    public ClassReaderImpl(byte[] bytes, Function<Utf8Entry, AttributeMapper<?>> customAttributes) {
        this.buf = bytes;
        this.customAttributes = customAttributes;
        if (bytes.length < 10 || readInt(0) != 0xCAFEBABE) {
            throw new IllegalArgumentException("Bad magic number");
        }
        int count = readU2(8);
        offsets = new int[count];
        cache = new Entry[count];
        int p = 10;
        for (int i = 1; i < count; i++) {
            offsets[i] = p;
            int tag = readU1(p);
            switch (tag) {
                case PoolEntry.TAG_UTF8:
                    p += 3 + readU2(p + 1);
                    break;
                case PoolEntry.TAG_INTEGER:
                case PoolEntry.TAG_FLOAT:
                case PoolEntry.TAG_FIELDREF:
                case PoolEntry.TAG_METHODREF:
                case PoolEntry.TAG_INTERFACE_METHODREF:
                case PoolEntry.TAG_NAME_AND_TYPE:
                case PoolEntry.TAG_DYNAMIC:
                case PoolEntry.TAG_INVOKE_DYNAMIC:
                    p += 5;
                    break;
                case PoolEntry.TAG_LONG:
                case PoolEntry.TAG_DOUBLE:
                    p += 9;
                    i++;
                    break;
                case PoolEntry.TAG_CLASS:
                case PoolEntry.TAG_STRING:
                case PoolEntry.TAG_METHOD_TYPE:
                case PoolEntry.TAG_MODULE:
                case PoolEntry.TAG_PACKAGE:
                    p += 3;
                    break;
                case PoolEntry.TAG_METHOD_HANDLE:
                    p += 4;
                    break;
                default:
                    throw new ConstantPoolException("Bad tag (" + tag + ") at index (" + i + ") position (" + p + ")");
            }
        }
        check(p + 8);
        poolEnd = p;
    }

    private void check(int end) {
        if (end > buf.length || end < 0) {
            throw new IllegalArgumentException("Reading beyond classfile bounds");
        }
    }

    public byte[] bytes() { return buf; }

    @Override public int classfileLength() { return buf.length; }
    @Override public Function<Utf8Entry, AttributeMapper<?>> customAttributes() { return customAttributes; }

    @Override
    public int readU1(int p) {
        check(p + 1);
        return buf[p] & 0xff;
    }

    @Override
    public int readU2(int p) {
        check(p + 2);
        return ((buf[p] & 0xff) << 8) | (buf[p + 1] & 0xff);
    }

    @Override public int readS1(int p) { return (byte) readU1(p); }
    @Override public int readS2(int p) { return (short) readU2(p); }

    @Override
    public int readInt(int p) {
        check(p + 4);
        return ((buf[p] & 0xff) << 24) | ((buf[p + 1] & 0xff) << 16) | ((buf[p + 2] & 0xff) << 8) | (buf[p + 3] & 0xff);
    }

    @Override public long readLong(int p) { return ((long) readInt(p) << 32) | (readInt(p + 4) & 0xffffffffL); }
    @Override public float readFloat(int p) { return Float.intBitsToFloat(readInt(p)); }
    @Override public double readDouble(int p) { return Double.longBitsToDouble(readLong(p)); }

    @Override
    public byte[] readBytes(int p, int len) {
        return java.util.Arrays.copyOfRange(buf, p, p + len);
    }

    @Override
    public void copyBytesTo(BufWriter out, int p, int len) {
        check(p + len);
        out.writeBytes(buf, p, len);
    }

    @Override public int flags() { return readU2(poolEnd); }

    @Override public ClassEntry thisClassEntry() { return readEntry(poolEnd + 2, ClassEntry.class); }

    @Override
    public Optional<ClassEntry> superclassEntry() {
        return Optional.ofNullable(readEntryOrNull(poolEnd + 4, ClassEntry.class));
    }

    @Override public PoolEntry readEntry(int p) { return entryByIndex(readU2(p)); }

    @Override public <T extends PoolEntry> T readEntry(int p, Class<T> cls) { return entryByIndex(readU2(p), cls); }

    @Override
    public PoolEntry readEntryOrNull(int p) {
        int idx = readU2(p);
        return idx == 0 ? null : entryByIndex(idx);
    }

    @Override
    public <T extends PoolEntry> T readEntryOrNull(int p, Class<T> cls) {
        int idx = readU2(p);
        return idx == 0 ? null : entryByIndex(idx, cls);
    }

    @Override public int size() { return offsets.length; }

    @Override
    public PoolEntry entryByIndex(int index) {
        if (index <= 0 || index >= offsets.length) {
            throw new ConstantPoolException("Bad CP index: " + index);
        }
        Entry e = cache[index];
        if (e == null) {
            if (offsets[index] == 0) {
                throw new ConstantPoolException("Unusable CP index: " + index);
            }
            e = decode(index);
            cache[index] = e;
        }
        return e;
    }

    @Override
    public <T extends PoolEntry> T entryByIndex(int index, Class<T> cls) {
        boolean decoded = index > 0 && index < cache.length && cache[index] != null;
        PoolEntry e = entryByIndex(index);
        if (!cls.isInstance(e)) {
            if (decoded) {
                throw new ConstantPoolException("Not a " + cls.getSimpleName() + " at index: " + index);
            }
            throw new ConstantPoolException("Bad tag (" + e.tag() + ") at index (" + index + ") position ("
                + offsets[index] + "), expected " + cls.getSimpleName());
        }
        return cls.cast(e);
    }

    private Utf8E utf8At(int p) {
        return (Utf8E) entryByIndex(readU2(p), Utf8Entry.class);
    }

    private Entry decode(int index) {
        int p = offsets[index];
        int tag = readU1(p);
        switch (tag) {
            case PoolEntry.TAG_UTF8: return new Utf8E(this, index, decodeUtf8(p + 3, readU2(p + 1)));
            case PoolEntry.TAG_INTEGER: return new IntE(this, index, readInt(p + 1));
            case PoolEntry.TAG_FLOAT: return new FloatE(this, index, readFloat(p + 1));
            case PoolEntry.TAG_LONG: return new LongE(this, index, readLong(p + 1));
            case PoolEntry.TAG_DOUBLE: return new DoubleE(this, index, readDouble(p + 1));
            case PoolEntry.TAG_CLASS: return new ClassE(this, index, utf8At(p + 1));
            case PoolEntry.TAG_STRING: return new StringE(this, index, utf8At(p + 1));
            case PoolEntry.TAG_METHOD_TYPE: return new MethodTypeE(this, index, utf8At(p + 1));
            case PoolEntry.TAG_MODULE: return new ModuleE(this, index, utf8At(p + 1));
            case PoolEntry.TAG_PACKAGE: return new PackageE(this, index, utf8At(p + 1));
            case PoolEntry.TAG_NAME_AND_TYPE: return new NameTypeE(this, index, utf8At(p + 1), utf8At(p + 3));
            case PoolEntry.TAG_FIELDREF:
                return new FieldRefE(this, index, (ClassE) readEntry(p + 1, ClassEntry.class),
                    (NameTypeE) readEntry(p + 3, NameAndTypeEntry.class));
            case PoolEntry.TAG_METHODREF:
                return new MethodRefE(this, index, (ClassE) readEntry(p + 1, ClassEntry.class),
                    (NameTypeE) readEntry(p + 3, NameAndTypeEntry.class));
            case PoolEntry.TAG_INTERFACE_METHODREF:
                return new InterfaceMethodRefE(this, index, (ClassE) readEntry(p + 1, ClassEntry.class),
                    (NameTypeE) readEntry(p + 3, NameAndTypeEntry.class));
            case PoolEntry.TAG_METHOD_HANDLE:
                return new MethodHandleE(this, index, readU1(p + 1), (RefE) readEntry(p + 2, MemberRefEntry.class));
            case PoolEntry.TAG_DYNAMIC:
                return new ConstantDynE(this, index, (BsmE) bootstrapMethodEntry(readU2(p + 1)),
                    (NameTypeE) readEntry(p + 3, NameAndTypeEntry.class));
            case PoolEntry.TAG_INVOKE_DYNAMIC:
                return new InvokeDynamicE(this, index, (BsmE) bootstrapMethodEntry(readU2(p + 1)),
                    (NameTypeE) readEntry(p + 3, NameAndTypeEntry.class));
            default: throw new ConstantPoolException("Bad tag " + tag + " at index " + index);
        }
    }

    private String decodeUtf8(int p, int len) {
        check(p + len);
        char[] out = new char[len];
        int n = 0;
        int end = p + len;
        while (p < end) {
            int c = buf[p++] & 0xff;
            if (c < 0x80) {
                out[n++] = (char) c;
            } else if ((c >> 5) == 6 && p < end) {
                out[n++] = (char) (((c & 0x1f) << 6) | (buf[p++] & 0x3f));
            } else if ((c >> 4) == 14 && p + 1 < end) {
                out[n++] = (char) (((c & 0x0f) << 12) | ((buf[p] & 0x3f) << 6) | (buf[p + 1] & 0x3f));
                p += 2;
            } else {
                throw new ConstantPoolException("Malformed modified UTF-8 at " + (p - 1));
            }
        }
        return new String(out, 0, n);
    }

    private void layout() {
        if (membersStart >= 0) {
            return;
        }
        int p = poolEnd + 6;
        p += 2 + 2 * readU2(p);
        int start = p;
        p = skipMembers(p);
        methodsStart = p;
        p = skipMembers(p);
        attributesStart = p;
        membersStart = start;
    }

    private int skipMembers(int p) {
        int n = readU2(p);
        p += 2;
        for (int i = 0; i < n; i++) {
            p = skipAttributes(p + 6);
        }
        return p;
    }

    /** Returns the offset just past the attribute table starting at {@code p}. */
    public int skipAttributes(int p) {
        int n = readU2(p);
        p += 2;
        for (int i = 0; i < n; i++) {
            p += 6 + readInt(p + 2);
            check(p);
        }
        return p;
    }

    public int interfacesOffset() { return poolEnd + 6; }
    public int fieldsOffset() { layout(); return membersStart; }
    public int methodsOffset() { layout(); return methodsStart; }
    public int classAttributesOffset() { layout(); return attributesStart; }

    /** Reads the attribute table at {@code p} into attribute objects, resolving mappers by name. */
    public List<Attribute<?>> readAttributes(java.lang.classfile.AttributedElement owner, int p) {
        int n = readU2(p);
        p += 2;
        List<Attribute<?>> out = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            Utf8Entry name = entryByIndex(readU2(p), Utf8Entry.class);
            int len = readInt(p + 2);
            if (len < 0 || p + 6 + len > buf.length) {
                throw new IllegalArgumentException("attribute " + name.stringValue() + " too big to handle");
            }
            AttributeMapper<?> mapper = Mappers.resolve(name, customAttributes);
            Attribute<?> a = (Attribute<?>) mapper.readAttribute(owner, this, p + 6);
            out.add(a);
            p += 6 + len;
        }
        return out;
    }

    @Override
    public int bootstrapMethodCount() {
        return bsmTable().size();
    }

    @Override
    public BootstrapMethodEntry bootstrapMethodEntry(int index) {
        List<BsmE> t = bsmTable();
        if (index < 0 || index >= t.size()) {
            throw new ConstantPoolException("Bad BSM index: " + index);
        }
        return t.get(index);
    }

    private List<BsmE> bsmTable() {
        if (bsms == null) {
            List<BsmE> out = new ArrayList<>();
            int p = classAttributesOffset();
            int n = readU2(p);
            p += 2;
            for (int i = 0; i < n; i++) {
                int len = readInt(p + 2);
                if (readU2(p) != 0 && entryByIndex(readU2(p)) instanceof Utf8E
                        && ((Utf8E) entryByIndex(readU2(p))).value.equals(Attributes.NAME_BOOTSTRAP_METHODS)) {
                    int q = p + 6;
                    int count = readU2(q);
                    q += 2;
                    bsms = out;
                    for (int k = 0; k < count; k++) {
                        int handle = readU2(q);
                        int args = readU2(q + 2);
                        out.add(null);
                        bsmDefs.add(new int[] {handle, q + 4, args});
                        q += 4 + 2 * args;
                    }
                    break;
                }
                p += 6 + len;
            }
            if (bsms == null) {
                bsms = out;
            }
        }
        return new BsmView();
    }

    private final List<int[]> bsmDefs = new ArrayList<>();

    private final class BsmView extends java.util.AbstractList<BsmE> {
        @Override public int size() { return bsms.size(); }

        @Override
        public BsmE get(int i) {
            BsmE e = bsms.get(i);
            if (e == null) {
                int[] def = bsmDefs.get(i);
                List<LoadableConstantEntry> args = new ArrayList<>(def[2]);
                for (int k = 0; k < def[2]; k++) {
                    args.add(readEntry(def[1] + 2 * k, LoadableConstantEntry.class));
                }
                e = new BsmE(ClassReaderImpl.this, i,
                    (MethodHandleE) entryByIndex(def[0], MethodHandleEntry.class), args);
                bsms.set(i, e);
            }
            return e;
        }
    }
}
