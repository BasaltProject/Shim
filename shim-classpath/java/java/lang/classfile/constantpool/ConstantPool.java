/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.lang.classfile.constantpool;

import java.lang.classfile.BootstrapMethodEntry;
import java.util.Iterator;
import java.util.NoSuchElementException;

public interface ConstantPool extends Iterable<PoolEntry> {
    int size();
    default Iterator<PoolEntry> iterator() {
        return new Iterator<PoolEntry>() {
            private int next = 1;

            @Override
            public boolean hasNext() { return next < size(); }

            @Override
            public PoolEntry next() {
                if (next >= size()) {
                    throw new NoSuchElementException();
                }
                PoolEntry e = entryByIndex(next);
                next += e.width();
                return e;
            }
        };
    }
    BootstrapMethodEntry bootstrapMethodEntry(int i);
    PoolEntry entryByIndex(int i);
    <T extends PoolEntry> T entryByIndex(int i, Class<T> class_);
    int bootstrapMethodCount();
}
