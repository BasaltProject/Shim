/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.classfile;

import java.lang.classfile.AccessFlags;
import java.lang.classfile.ClassFileVersion;
import java.lang.classfile.Interfaces;
import java.lang.classfile.Superclass;
import java.lang.classfile.constantpool.ClassEntry;
import java.lang.reflect.AccessFlag;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/** The small class level elements: access flags, version, superclass and interfaces. */
public final class Elements {
    private Elements() { }

    public static final class AccessFlagsImpl implements AccessFlags {
        private final int mask;
        private final AccessFlag.Location location;

        public AccessFlagsImpl(AccessFlag.Location location, int mask) {
            this.mask = mask & 0xffff;
            this.location = location;
        }

        @Override public Set<AccessFlag> flags() { return AccessFlag.maskToAccessFlags(mask, location); }
        @Override public AccessFlag.Location location() { return location; }
        @Override public int flagsMask() { return mask; }
        @Override public boolean has(AccessFlag flag) { return (mask & flag.mask()) != 0 && flag.locations().contains(location); }
        @Override public String toString() { return "AccessFlags[flags=" + mask + "]"; }

        @Override
        public boolean equals(Object o) {
            return o instanceof AccessFlagsImpl && ((AccessFlagsImpl) o).mask == mask && ((AccessFlagsImpl) o).location == location;
        }

        @Override public int hashCode() { return mask * 31 + location.hashCode(); }
    }

    public static final class ClassFileVersionImpl implements ClassFileVersion {
        private final int major;
        private final int minor;

        public ClassFileVersionImpl(int major, int minor) {
            this.major = major;
            this.minor = minor;
        }

        @Override public int majorVersion() { return major; }
        @Override public int minorVersion() { return minor; }

        @Override
        public String toString() { return "ClassFileVersion[majorVersion=" + major + ", minorVersion=" + minor + "]"; }
    }

    public static final class SuperclassImpl implements Superclass {
        private final ClassEntry entry;

        public SuperclassImpl(ClassEntry entry) {
            this.entry = java.util.Objects.requireNonNull(entry);
        }

        @Override public ClassEntry superclassEntry() { return entry; }

        @Override
        public String toString() { return "Superclass[superclassEntry=" + entry.name().stringValue() + "]"; }
    }

    public static final class InterfacesImpl implements Interfaces {
        private final List<ClassEntry> list;

        public InterfacesImpl(List<ClassEntry> list) {
            this.list = List.copyOf(list);
        }

        @Override public List<ClassEntry> interfaces() { return list; }

        @Override
        public String toString() {
            return "Interfaces[interfaces=" + list.stream().map(e -> e.name().stringValue())
                .collect(Collectors.joining(", ")) + "]";
        }
    }
}
