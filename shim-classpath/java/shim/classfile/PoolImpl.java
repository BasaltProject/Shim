/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.classfile;

import java.lang.classfile.BootstrapMethodEntry;
import java.lang.classfile.constantpool.*;
import java.lang.constant.MethodTypeDesc;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import shim.classfile.Entries.*;

/** The constant pool builder: interns entries so equal constants share one index. */
public final class PoolImpl implements ConstantPoolBuilder {
    private static final class Key {
        private final int tag;
        private final Object key;

        Key(int tag, Object key) {
            this.tag = tag;
            this.key = key;
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof Key && ((Key) o).tag == tag && ((Key) o).key.equals(key);
        }

        @Override
        public int hashCode() {
            return tag * 31 + key.hashCode();
        }
    }

    private final ArrayList<Entry> slots = new ArrayList<>();
    private final HashMap<Key, Entry> map = new HashMap<>();
    private final ArrayList<BsmE> bsms = new ArrayList<>();
    private final HashMap<Object, BsmE> bsmMap = new HashMap<>();
    private final ConstantPool source;

    public PoolImpl() {
        this.source = null;
        slots.add(null);
    }

    public PoolImpl(ClassReaderImpl source) {
        this.source = source;
        slots.add(null);
        for (int i = 0; i < source.bootstrapMethodCount(); i++) {
            bsms.add(null);
        }
        for (int i = 1; i < source.size(); i += source.entryByIndex(i).width()) {
            copy(source.entryByIndex(i));
        }
        for (int i = 0; i < source.bootstrapMethodCount(); i++) {
            bsmAt(i);
        }
    }

    private BsmE bsmAt(int i) {
        if (bsms.get(i) != null) {
            return bsms.get(i);
        }
        BootstrapMethodEntry b = source.bootstrapMethodEntry(i);
        List<LoadableConstantEntry> args = new ArrayList<>();
        for (LoadableConstantEntry a : b.arguments()) {
            args.add((LoadableConstantEntry) copy(a));
        }
        BsmE e = new BsmE(this, i, (MethodHandleE) copy(b.bootstrapMethod()), args);
        bsms.set(i, e);
        bsmMap.putIfAbsent(e.key(), e);
        return e;
    }

    private Entry copy(PoolEntry src) {
        int idx = src.index();
        if (idx < slots.size() && slots.get(idx) != null) {
            return slots.get(idx);
        }
        Entry e;
        switch (src.tag()) {
            case PoolEntry.TAG_UTF8:
                e = new Utf8E(this, idx, ((Utf8Entry) src).stringValue());
                break;
            case PoolEntry.TAG_INTEGER:
                e = new IntE(this, idx, ((IntegerEntry) src).intValue());
                break;
            case PoolEntry.TAG_FLOAT:
                e = new FloatE(this, idx, ((FloatEntry) src).floatValue());
                break;
            case PoolEntry.TAG_LONG:
                e = new LongE(this, idx, ((LongEntry) src).longValue());
                break;
            case PoolEntry.TAG_DOUBLE:
                e = new DoubleE(this, idx, ((DoubleEntry) src).doubleValue());
                break;
            case PoolEntry.TAG_CLASS:
                e = new ClassE(this, idx, (Utf8E) copy(((ClassEntry) src).name()));
                break;
            case PoolEntry.TAG_STRING:
                e = new StringE(this, idx, (Utf8E) copy(((StringEntry) src).utf8()));
                break;
            case PoolEntry.TAG_MODULE:
                e = new ModuleE(this, idx, (Utf8E) copy(((ModuleEntry) src).name()));
                break;
            case PoolEntry.TAG_PACKAGE:
                e = new PackageE(this, idx, (Utf8E) copy(((PackageEntry) src).name()));
                break;
            case PoolEntry.TAG_METHOD_TYPE:
                e = new MethodTypeE(this, idx, (Utf8E) copy(((MethodTypeEntry) src).descriptor()));
                break;
            case PoolEntry.TAG_NAME_AND_TYPE: {
                NameAndTypeEntry n = (NameAndTypeEntry) src;
                e = new NameTypeE(this, idx, (Utf8E) copy(n.name()), (Utf8E) copy(n.type()));
                break;
            }
            case PoolEntry.TAG_FIELDREF:
            case PoolEntry.TAG_METHODREF:
            case PoolEntry.TAG_INTERFACE_METHODREF: {
                MemberRefEntry r = (MemberRefEntry) src;
                ClassE owner = (ClassE) copy(r.owner());
                NameTypeE nt = (NameTypeE) copy(r.nameAndType());
                e = src.tag() == PoolEntry.TAG_FIELDREF ? new FieldRefE(this, idx, owner, nt)
                    : src.tag() == PoolEntry.TAG_METHODREF ? new MethodRefE(this, idx, owner, nt)
                    : new InterfaceMethodRefE(this, idx, owner, nt);
                break;
            }
            case PoolEntry.TAG_METHOD_HANDLE: {
                MethodHandleEntry h = (MethodHandleEntry) src;
                e = new MethodHandleE(this, idx, h.kind(), (RefE) copy(h.reference()));
                break;
            }
            case PoolEntry.TAG_INVOKE_DYNAMIC:
            case PoolEntry.TAG_DYNAMIC: {
                DynamicConstantPoolEntry d = (DynamicConstantPoolEntry) src;
                NameTypeE nt = (NameTypeE) copy(d.nameAndType());
                BsmE bsm = bsmAt(d.bootstrapMethodIndex());
                e = src.tag() == PoolEntry.TAG_DYNAMIC ? new ConstantDynE(this, idx, bsm, nt)
                    : new InvokeDynamicE(this, idx, bsm, nt);
                break;
            }
            default:
                throw new ConstantPoolException("Bad tag: " + src.tag());
        }
        place(e);
        return e;
    }

    private void place(Entry e) {
        int idx = e.index;
        while (slots.size() < idx + e.width()) {
            slots.add(null);
        }
        slots.set(idx, e);
        map.putIfAbsent(new Key(e.tag(), e.key()), e);
    }

    private <T extends Entry> T add(T e) {
        place(e);
        return e;
    }

    @SuppressWarnings("unchecked")
    private <T extends Entry> T find(int tag, Object key) {
        return (T) map.get(new Key(tag, key));
    }

    @Override public int size() { return slots.size(); }

    @Override
    public PoolEntry entryByIndex(int index) {
        if (index <= 0 || index >= slots.size()) {
            throw new ConstantPoolException("Bad CP index: " + index);
        }
        Entry e = slots.get(index);
        if (e == null) {
            throw new ConstantPoolException("Unusable CP index: " + index);
        }
        return e;
    }

    @Override
    public <T extends PoolEntry> T entryByIndex(int index, Class<T> cls) {
        PoolEntry e = entryByIndex(index);
        if (!cls.isInstance(e)) {
            throw new ConstantPoolException("Not a " + cls.getSimpleName() + " at index: " + index);
        }
        return cls.cast(e);
    }

    @Override public int bootstrapMethodCount() { return bsms.size(); }

    @Override
    public BootstrapMethodEntry bootstrapMethodEntry(int index) {
        if (index < 0 || index >= bsms.size()) {
            throw new ConstantPoolException("Bad BSM index: " + index);
        }
        return bsms.get(index);
    }

    @Override
    public boolean canWriteDirect(ConstantPool other) {
        return other == this || (source != null && other == source);
    }

    int indexOf(PoolEntry e) {
        return canWriteDirect(e.constantPool()) ? e.index() : adopt(e).index;
    }

    int bsmIndexOf(BootstrapMethodEntry b) {
        return canWriteDirect(b.constantPool()) ? b.bsmIndex() : adoptBsm(b).index;
    }

    Entry adopt(PoolEntry e) {
        if (e instanceof Entry && ((Entry) e).pool == this) {
            return (Entry) e;
        }
        switch (e.tag()) {
            case PoolEntry.TAG_UTF8: return utf8(((Utf8Entry) e).stringValue());
            case PoolEntry.TAG_INTEGER: return intE(((IntegerEntry) e).intValue());
            case PoolEntry.TAG_FLOAT: return floatE(((FloatEntry) e).floatValue());
            case PoolEntry.TAG_LONG: return longE(((LongEntry) e).longValue());
            case PoolEntry.TAG_DOUBLE: return doubleE(((DoubleEntry) e).doubleValue());
            case PoolEntry.TAG_CLASS: return classE(utf8(((ClassEntry) e).name().stringValue()));
            case PoolEntry.TAG_STRING: return stringE(utf8(((StringEntry) e).utf8().stringValue()));
            case PoolEntry.TAG_MODULE: return moduleE(utf8(((ModuleEntry) e).name().stringValue()));
            case PoolEntry.TAG_PACKAGE: return packageE(utf8(((PackageEntry) e).name().stringValue()));
            case PoolEntry.TAG_METHOD_TYPE: return methodTypeE(utf8(((MethodTypeEntry) e).descriptor().stringValue()));
            case PoolEntry.TAG_NAME_AND_TYPE: {
                NameAndTypeEntry n = (NameAndTypeEntry) e;
                return nameTypeE(utf8(n.name().stringValue()), utf8(n.type().stringValue()));
            }
            case PoolEntry.TAG_FIELDREF:
            case PoolEntry.TAG_METHODREF:
            case PoolEntry.TAG_INTERFACE_METHODREF: {
                MemberRefEntry r = (MemberRefEntry) e;
                return refE(e.tag(), (ClassE) adopt(r.owner()), (NameTypeE) adopt(r.nameAndType()));
            }
            case PoolEntry.TAG_METHOD_HANDLE: {
                MethodHandleEntry h = (MethodHandleEntry) e;
                return handleE(h.kind(), (RefE) adopt(h.reference()));
            }
            case PoolEntry.TAG_INVOKE_DYNAMIC: {
                InvokeDynamicEntry d = (InvokeDynamicEntry) e;
                return dynE(e.tag(), adoptBsm(d.bootstrap()), (NameTypeE) adopt(d.nameAndType()));
            }
            case PoolEntry.TAG_DYNAMIC: {
                ConstantDynamicEntry d = (ConstantDynamicEntry) e;
                return dynE(e.tag(), adoptBsm(d.bootstrap()), (NameTypeE) adopt(d.nameAndType()));
            }
            default: throw new ConstantPoolException("Bad tag: " + e.tag());
        }
    }

    private BsmE adoptBsm(BootstrapMethodEntry b) {
        if (b instanceof BsmE && ((BsmE) b).pool == this) {
            return (BsmE) b;
        }
        MethodHandleE handle = (MethodHandleE) adopt(b.bootstrapMethod());
        List<LoadableConstantEntry> args = new ArrayList<>(b.arguments().size());
        for (LoadableConstantEntry a : b.arguments()) {
            args.add((LoadableConstantEntry) adopt(a));
        }
        return bsmE(handle, args);
    }

    Utf8E utf8(String s) {
        Utf8E e = find(PoolEntry.TAG_UTF8, s);
        return e != null ? e : add(new Utf8E(this, slots.size(), s));
    }

    private IntE intE(int v) {
        IntE e = find(PoolEntry.TAG_INTEGER, v);
        return e != null ? e : add(new IntE(this, slots.size(), v));
    }

    private FloatE floatE(float v) {
        FloatE e = v != v ? null : find(PoolEntry.TAG_FLOAT, Float.floatToRawIntBits(v));
        return e != null ? e : add(new FloatE(this, slots.size(), v));
    }

    private LongE longE(long v) {
        LongE e = find(PoolEntry.TAG_LONG, v);
        return e != null ? e : add(new LongE(this, slots.size(), v));
    }

    private DoubleE doubleE(double v) {
        DoubleE e = v != v ? null : find(PoolEntry.TAG_DOUBLE, Double.doubleToRawLongBits(v));
        return e != null ? e : add(new DoubleE(this, slots.size(), v));
    }

    private ClassE classE(Utf8E name) {
        ClassE e = find(PoolEntry.TAG_CLASS, name.value);
        return e != null ? e : add(new ClassE(this, slots.size(), name));
    }

    private StringE stringE(Utf8E s) {
        StringE e = find(PoolEntry.TAG_STRING, s.value);
        return e != null ? e : add(new StringE(this, slots.size(), s));
    }

    private ModuleE moduleE(Utf8E name) {
        ModuleE e = find(PoolEntry.TAG_MODULE, name.value);
        return e != null ? e : add(new ModuleE(this, slots.size(), name));
    }

    private PackageE packageE(Utf8E name) {
        PackageE e = find(PoolEntry.TAG_PACKAGE, name.value);
        return e != null ? e : add(new PackageE(this, slots.size(), name));
    }

    private MethodTypeE methodTypeE(Utf8E d) {
        MethodTypeE e = find(PoolEntry.TAG_METHOD_TYPE, d.value);
        return e != null ? e : add(new MethodTypeE(this, slots.size(), d));
    }

    private NameTypeE nameTypeE(Utf8E n, Utf8E t) {
        NameTypeE e = find(PoolEntry.TAG_NAME_AND_TYPE, List.of(n.value, t.value));
        return e != null ? e : add(new NameTypeE(this, slots.size(), n, t));
    }

    private RefE refE(int tag, ClassE owner, NameTypeE nt) {
        Object key = List.of(owner.name.value, nt.name.value, nt.type.value);
        RefE e = find(tag, key);
        if (e != null) {
            return e;
        }
        int idx = slots.size();
        return add(tag == PoolEntry.TAG_FIELDREF ? new FieldRefE(this, idx, owner, nt)
            : tag == PoolEntry.TAG_METHODREF ? new MethodRefE(this, idx, owner, nt)
            : new InterfaceMethodRefE(this, idx, owner, nt));
    }

    private MethodHandleE handleE(int kind, RefE ref) {
        MethodHandleE e = find(PoolEntry.TAG_METHOD_HANDLE, List.of(kind, ref.key(), ref.tag()));
        return e != null ? e : add(new MethodHandleE(this, slots.size(), kind, ref));
    }

    private BsmE bsmE(MethodHandleE handle, List<LoadableConstantEntry> args) {
        List<Object> key = new ArrayList<>();
        key.add(handle);
        key.addAll(args);
        BsmE e = bsmMap.get(key);
        if (e == null) {
            e = new BsmE(this, bsms.size(), handle, args);
            bsms.add(e);
            bsmMap.put(key, e);
        }
        return e;
    }

    private DynE dynE(int tag, BsmE bsm, NameTypeE nt) {
        Object key = List.of(bsm.key(), nt.name.value, nt.type.value);
        DynE e = find(tag, key);
        if (e != null) {
            return e;
        }
        int idx = slots.size();
        return add(tag == PoolEntry.TAG_DYNAMIC ? new ConstantDynE(this, idx, bsm, nt)
            : new InvokeDynamicE(this, idx, bsm, nt));
    }

    @Override public Utf8Entry utf8Entry(String text) { return utf8(text); }
    @Override public IntegerEntry intEntry(int value) { return intE(value); }
    @Override public FloatEntry floatEntry(float value) { return floatE(value); }
    @Override public LongEntry longEntry(long value) { return longE(value); }
    @Override public DoubleEntry doubleEntry(double value) { return doubleE(value); }

    @Override
    public ClassEntry classEntry(Utf8Entry name) {
        return classE((Utf8E) adopt(name));
    }

    @Override
    public NameAndTypeEntry nameAndTypeEntry(Utf8Entry name, Utf8Entry type) {
        return nameTypeE((Utf8E) adopt(name), (Utf8E) adopt(type));
    }

    @Override
    public FieldRefEntry fieldRefEntry(ClassEntry owner, NameAndTypeEntry nt) {
        return (FieldRefEntry) refE(PoolEntry.TAG_FIELDREF, (ClassE) adopt(owner), (NameTypeE) adopt(nt));
    }

    @Override
    public MethodRefEntry methodRefEntry(ClassEntry owner, NameAndTypeEntry nt) {
        return (MethodRefEntry) refE(PoolEntry.TAG_METHODREF, (ClassE) adopt(owner), (NameTypeE) adopt(nt));
    }

    @Override
    public InterfaceMethodRefEntry interfaceMethodRefEntry(ClassEntry owner, NameAndTypeEntry nt) {
        return (InterfaceMethodRefEntry) refE(PoolEntry.TAG_INTERFACE_METHODREF, (ClassE) adopt(owner),
            (NameTypeE) adopt(nt));
    }

    @Override public PackageEntry packageEntry(Utf8Entry name) { return packageE((Utf8E) adopt(name)); }
    @Override public ModuleEntry moduleEntry(Utf8Entry name) { return moduleE((Utf8E) adopt(name)); }
    @Override public MethodTypeEntry methodTypeEntry(Utf8Entry descriptor) { return methodTypeE((Utf8E) adopt(descriptor)); }
    @Override public MethodTypeEntry methodTypeEntry(MethodTypeDesc desc) { return methodTypeE(utf8(desc.descriptorString())); }
    @Override public StringEntry stringEntry(Utf8Entry value) { return stringE((Utf8E) adopt(value)); }

    @Override
    public MethodHandleEntry methodHandleEntry(int kind, MemberRefEntry ref) {
        return handleE(kind, (RefE) adopt(ref));
    }

    @Override
    public InvokeDynamicEntry invokeDynamicEntry(BootstrapMethodEntry bsm, NameAndTypeEntry nt) {
        return (InvokeDynamicEntry) dynE(PoolEntry.TAG_INVOKE_DYNAMIC, adoptBsm(bsm), (NameTypeE) adopt(nt));
    }

    @Override
    public ConstantDynamicEntry constantDynamicEntry(BootstrapMethodEntry bsm, NameAndTypeEntry nt) {
        return (ConstantDynamicEntry) dynE(PoolEntry.TAG_DYNAMIC, adoptBsm(bsm), (NameTypeE) adopt(nt));
    }

    @Override
    public BootstrapMethodEntry bsmEntry(MethodHandleEntry handle, List<LoadableConstantEntry> args) {
        List<LoadableConstantEntry> adopted = new ArrayList<>(args.size());
        for (LoadableConstantEntry a : args) {
            adopted.add((LoadableConstantEntry) adopt(a));
        }
        return bsmE((MethodHandleE) adopt(handle), adopted);
    }

    /** Writes the pool (count and entries) in index order. */
    void writeTo(BufWriterImpl out) {
        out.writeU2(slots.size());
        for (Entry e : slots) {
            if (e != null) {
                e.write(out);
            }
        }
    }

    List<BsmE> bootstrapMethods() { return bsms; }

    Map<Object, BsmE> bsmIndex() { return bsmMap; }
}
