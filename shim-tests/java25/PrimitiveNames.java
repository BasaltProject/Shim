/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

// Minimum release: 22
public class PrimitiveNames {
    public static void main(String[] args) {
        Class<?>[] types = {boolean.class, byte.class, char.class, short.class, int.class,
                long.class, float.class, double.class};
        for (Class<?> type : types) {
            Class<?> found = Class.forPrimitiveName(new String(type.getName()));
            if (found != type) throw new AssertionError(type);
            System.out.println(found.descriptorString());
            System.out.println(found.toString());
        }
        for (String name : new String[] {"void", "", "Int", "INT", " int", "int ", "I", "[I",
                "int[]", "java.lang.Integer", "missing.Type", "niño"}) {
            System.out.println(name + ":" + Class.forPrimitiveName(name));
        }
        try { Class.forPrimitiveName(null); throw new AssertionError(); }
        catch (NullPointerException expected) { System.out.println("null rejected"); }
    }
}
