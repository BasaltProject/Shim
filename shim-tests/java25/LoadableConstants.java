/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;

public class LoadableConstants {
    static int initialized;
    public static class Lazy {
        static { initialized++; }
        public static int target() { return 42; }
    }

    private static Object constant(Class<?> data, String name) throws Exception {
        return data.getMethod(name).invoke(null);
    }

    public static void main(String[] args) throws Exception {
        Class<?> data = Class.forName("LoadableConstantData");
        MethodType type = (MethodType) constant(data, "type");
        System.out.println(type.toMethodDescriptorString());
        System.out.println(type == constant(data, "type"));
        System.out.println(type == constant(data, "wideType"));
        System.out.println(type.equals(MethodType.methodType(long.class, int.class, String[].class)));
        System.out.println(constant(data, "concat"));
        System.out.println(type == constant(data, "wideType"));
        MethodType lazyType = (MethodType) constant(data, "lazyType");
        System.out.println(lazyType.parameterType(0) == Lazy.class);
        System.out.println(lazyType.returnType() == Lazy.class);
        System.out.println(initialized);
        String[] names = {"staticHandle", "virtualHandle", "constructorHandle", "lazyHandle"};
        for (String name : names) {
            MethodHandle handle = (MethodHandle) constant(data, name);
            System.out.println(handle.type().toMethodDescriptorString());
            System.out.println(handle == constant(data, name));
        }
        System.out.println(initialized);
    }
}
