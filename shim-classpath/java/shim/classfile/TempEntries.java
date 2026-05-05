/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.classfile;

import java.lang.classfile.constantpool.DoubleEntry;
import java.lang.classfile.constantpool.FloatEntry;
import java.lang.classfile.constantpool.IntegerEntry;
import java.lang.classfile.constantpool.LongEntry;
import java.lang.classfile.constantpool.Utf8Entry;
import java.lang.constant.ClassDesc;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Pool entries not bound to any class file (index -1), plus the null checks shared by the factories. */
final class TempEntries {
    private static final PoolImpl POOL = new PoolImpl();

    private TempEntries() { }

    static Utf8Entry utf8(String s) {
        if (s == null) {
            throw new NullPointerException("Cannot invoke \"String.hashCode()\" because \"s\" is null");
        }
        return new Entries.Utf8E(POOL, -1, s);
    }

    static Utf8Entry descriptor(ClassDesc desc) {
        return utf8(descriptorString(desc));
    }

    static String descriptorString(ClassDesc desc) {
        if (desc == null) {
            throw new NullPointerException(
                "Cannot invoke \"java.lang.constant.ClassDesc.descriptorString()\" because \"desc\" is null");
        }
        return desc.descriptorString();
    }

    static IntegerEntry intEntry(int v) { return new Entries.IntE(POOL, -1, v); }
    static FloatEntry floatEntry(float v) { return new Entries.FloatE(POOL, -1, v); }
    static LongEntry longEntry(long v) { return new Entries.LongE(POOL, -1, v); }
    static DoubleEntry doubleEntry(double v) { return new Entries.DoubleE(POOL, -1, v); }

    static <T> T nonNull(T o) {
        if (o == null) {
            throw new NullPointerException();
        }
        return o;
    }

    /** Immutable copy of an array, failing like {@code List.of(E...)}. */
    @SafeVarargs
    static <T> List<T> listOf(T... elements) {
        if (elements == null) {
            throw new NullPointerException("Cannot read the array length because \"elements\" is null");
        }
        List<T> out = new ArrayList<>(elements.length);
        for (T e : elements) {
            out.add(nonNull(e));
        }
        return Collections.unmodifiableList(out);
    }

    /** Immutable copy of a list, failing like {@code List.copyOf}. */
    static <T> List<T> copyOf(List<? extends T> list) {
        if (list == null) {
            throw new NullPointerException("Cannot invoke \"java.util.Collection.isEmpty()\" because \"coll\" is null");
        }
        List<T> out = new ArrayList<>(list.size());
        for (T e : list) {
            out.add(nonNull(e));
        }
        return Collections.unmodifiableList(out);
    }

    static int hash(Object... components) {
        int h = 0;
        for (Object c : components) {
            h = h * 31 + (c == null ? 0 : c.hashCode());
        }
        return h;
    }
}
