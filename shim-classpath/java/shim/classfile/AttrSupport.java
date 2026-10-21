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
import java.lang.classfile.attribute.UnknownAttribute;
import java.lang.classfile.constantpool.ClassEntry;
import java.lang.classfile.constantpool.ModuleEntry;
import java.lang.classfile.constantpool.PackageEntry;
import java.lang.classfile.constantpool.PoolEntry;
import java.lang.constant.ClassDesc;
import java.lang.constant.ModuleDesc;
import java.lang.constant.PackageDesc;
import java.lang.reflect.AccessFlag;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/** Helpers shared by the standard attribute implementations. */
public final class AttrSupport {
    private AttrSupport() { }

    /** A fresh pool for entries created by factories that take symbols. */
    public static PoolImpl pool() {
        return new PoolImpl();
    }

    public static int mask(AccessFlag.Location location, AccessFlag... flags) {
        int mask = 0;
        for (AccessFlag f : flags) {
            if (!f.locations().contains(location)) {
                throw new IllegalArgumentException("unexpected flag: " + f + " use in target location: " + location);
            }
            mask |= f.mask();
        }
        return mask;
    }

    public static int mask(AccessFlag.Location location, Collection<AccessFlag> flags) {
        return mask(location, flags.toArray(new AccessFlag[0]));
    }

    public static boolean has(AccessFlag.Location location, int mask, AccessFlag flag) {
        return (flag.mask() & mask) == flag.mask() && flag.locations().contains(location);
    }

    public static List<ClassEntry> classEntries(List<ClassDesc> descs) {
        PoolImpl pool = pool();
        ClassEntry[] out = new ClassEntry[descs.size()];
        for (int i = 0; i < out.length; i++) {
            out[i] = pool.classEntry(descs.get(i));
        }
        return List.of(out);
    }

    public static List<ModuleEntry> moduleEntries(PoolImpl pool, List<ModuleDesc> descs) {
        ModuleEntry[] out = new ModuleEntry[descs.size()];
        for (int i = 0; i < out.length; i++) {
            out[i] = pool.moduleEntry(descs.get(i));
        }
        return List.of(out);
    }

    public static List<PackageEntry> packageEntries(List<PackageDesc> descs) {
        PoolImpl pool = pool();
        PackageEntry[] out = new PackageEntry[descs.size()];
        for (int i = 0; i < out.length; i++) {
            out[i] = pool.packageEntry(descs.get(i));
        }
        return List.of(out);
    }

    /** Record style hash of the given components. */
    public static int hash(Object... components) {
        int h = 0;
        for (Object c : components) {
            h = h * 31 + Objects.hashCode(c);
        }
        return h;
    }

    /** Reads {@code count} entries of type {@code cls} from consecutive u2 indexes at {@code p}. */
    public static <T extends PoolEntry> List<T> readEntries(ClassReaderImpl r, int p, int count, Class<T> cls) {
        List<T> out = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            out.add(r.readEntry(p + 2 * i, cls));
        }
        return List.copyOf(out);
    }

    /** Reads a u2 count followed by that many entry indexes. */
    public static <T extends PoolEntry> List<T> readEntryList(ClassReaderImpl r, int p, Class<T> cls) {
        return readEntries(r, p + 2, r.readU2(p), cls);
    }

    public static void writeEntryList(BufWriterImpl out, List<? extends PoolEntry> entries) {
        out.writeU2(entries.size());
        for (PoolEntry e : entries) {
            out.writeIndex(e);
        }
    }

    /** True when the mapper writes only the body and the attribute framing is added around it. */
    public static boolean framedByWriter(Attribute<?> attribute) {
        return attribute.attributeMapper() instanceof Mapper || attribute instanceof UnknownAttribute;
    }

    /** Writes a nested attribute table, such as the attributes of a record component. */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void writeAttributes(BufWriterImpl out, List<Attribute<?>> attributes) {
        out.writeU2(attributes.size());
        for (Attribute<?> a : attributes) {
            AttributeMapper mapper = a.attributeMapper();
            if (!framedByWriter(a)) {
                mapper.writeAttribute(out, a);
                continue;
            }
            out.writeIndex(out.constantPool().utf8Entry(mapper.name()));
            int pos = out.size();
            out.writeInt(0);
            mapper.writeAttribute(out, a);
            out.patchInt(pos, 4, out.size() - pos - 4);
        }
    }
}
