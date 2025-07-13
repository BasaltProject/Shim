/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

// Minimum release: 12
public class ClassDescriptors {
    static int initialized;
    static class Lazy { static { ++initialized; } }
    static class Niño { }
    public static void main(String[] args) {
        Class<?>[] types = {void.class, boolean.class, byte.class, char.class, short.class, int.class,
                long.class, float.class, double.class, Object.class, Runnable.class, String[][].class,
                int[][].class, Lazy.class, Niño.class};
        for (Class<?> type : types) {
            java.lang.invoke.TypeDescriptor.OfField<Class<?>> field = type;
            if (!field.descriptorString().equals(type.descriptorString())
                    || field.componentType() != type.componentType()
                    || field.isArray() != type.isArray() || field.isPrimitive() != type.isPrimitive())
                throw new AssertionError(type);
            System.out.println(field.descriptorString());
            System.out.println("package " + type.getPackageName());
            if (type.componentType() != type.getComponentType()) throw new AssertionError(type);
            if (type == void.class) {
                try { type.arrayType(); throw new AssertionError(); }
                catch (UnsupportedOperationException expected) { System.out.println("void rejected"); }
            } else {
                Class<?> array = field.arrayType();
                if (array.componentType() != type || array != type.arrayType()) throw new AssertionError(type);
                System.out.println(array.descriptorString());
            }
        }
        System.out.println(java.util.Map.Entry.class.getPackageName());
        System.out.println(java.util.Map.Entry[][].class.getPackageName());
        System.out.println(new Object() {}.getClass().getPackageName());
        Class<?> deep = int.class;
        for (int i = 0; i < 255; ++i) deep = deep.arrayType();
        System.out.println(deep.descriptorString().length());
        try { deep.arrayType(); throw new AssertionError(); }
        catch (UnsupportedOperationException expected) { System.out.println("rank rejected"); }
        java.lang.invoke.TypeDescriptor.OfMethod<Class<?>, java.lang.invoke.MethodType> method =
                java.lang.invoke.MethodType.methodType(void.class, int.class, String.class);
        System.out.println(method.descriptorString());
        System.out.println(method.changeReturnType(long.class).descriptorString());
        System.out.println(method.changeParameterType(0, double.class).descriptorString());
        System.out.println(method.insertParameterTypes(1, boolean.class).descriptorString());
        System.out.println(method.dropParameterTypes(0, 1).descriptorString());
        if (method.returnType() != void.class || method.parameterType(0) != int.class
                || method.parameterArray()[1] != String.class || method.parameterList().get(0) != int.class
                || method.parameterCount() != 2) throw new AssertionError();
        System.out.println(initialized);
    }
}
