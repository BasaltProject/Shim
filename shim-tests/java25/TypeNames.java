/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

import java.lang.reflect.Type;

public class TypeNames {
    static class Niño { }
    public static void main(String[] args) {
        Class<?>[] classes = {void.class, boolean.class, byte.class, char.class, short.class, int.class,
                long.class, float.class, double.class, String.class, Runnable.class, int[][].class,
                Niño.class, Niño[][].class};
        for (Class<?> type : classes) {
            Type reflectionType = type;
            if (!type.getTypeName().equals(reflectionType.getTypeName())) throw new AssertionError(type);
            System.out.println(reflectionType.getTypeName());
        }
        Type custom = new Type() { public String toString() { return "custom type"; } };
        System.out.println(custom.getTypeName());
        Class<?> anonymous = custom.getClass();
        if (!anonymous.getTypeName().equals(anonymous.getName())) throw new AssertionError();
        System.out.println(anonymous.getTypeName());
    }
}
