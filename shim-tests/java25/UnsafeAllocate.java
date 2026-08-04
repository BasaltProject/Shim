/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 * Minimum release: 21
 */

import java.lang.reflect.Field;

public class UnsafeAllocate {
    static class Ctor {
        static int inits;
        int value = 7;
        static { inits++; }
        Ctor() { throw new IllegalStateException("constructor ran"); }
    }

    public static void main(String[] args) throws Exception {
        Field f = Class.forName("sun.misc.Unsafe").getDeclaredField("theUnsafe");
        f.setAccessible(true);
        sun.misc.Unsafe u = (sun.misc.Unsafe) f.get(null);
        Ctor c = (Ctor) u.allocateInstance(Ctor.class);
        System.out.println(c.value + " " + Ctor.inits);
        for (Class<?> k : new Class<?>[] {Runnable.class, Number.class, int[].class, int.class}) {
            try {
                u.allocateInstance(k);
            } catch (InstantiationException e) {
                System.out.println("ie " + e.getMessage());
            }
        }
    }
}
