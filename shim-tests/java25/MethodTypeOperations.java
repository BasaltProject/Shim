/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

// Minimum release: 17
import java.lang.invoke.MethodType;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MethodTypeOperations {
    public static class Token {}

    private static class Loader extends ClassLoader {
        Class<?> token(byte[] code) {
            return defineClass(null, code, 0, code.length);
        }
    }

    private interface Action { void run(); }

    private static void rejects(Class<?> expected, Action action) {
        try {
            action.run();
            throw new AssertionError("Accepted invalid method type");
        } catch (RuntimeException error) {
            if (!expected.isInstance(error)) throw new AssertionError(error);
            System.out.println(expected.getSimpleName());
        }
    }

    public static void main(String[] args) throws Exception {
        Class<?>[] input = {int.class, String[].class, double.class};
        MethodType type = MethodType.methodType(long.class, input);
        input[0] = Object.class;
        Class<?>[] output = type.parameterArray();
        output[0] = Object.class;
        System.out.println(type);
        System.out.println(type.descriptorString());
        System.out.println(type.parameterCount());
        System.out.println(type.parameterType(0) == int.class);
        System.out.println(type.lastParameterType() == double.class);
        System.out.println(MethodType.methodType(void.class).lastParameterType() == void.class);
        MethodType copy = MethodType.methodType(long.class, type.parameterList());
        System.out.println(type.equals(copy));
        System.out.println(type.hashCode() == copy.hashCode());
        List<Class<?>> hashTypes = new ArrayList<Class<?>>();
        hashTypes.add(type.returnType());
        hashTypes.addAll(type.parameterList());
        System.out.println(type.hashCode() == hashTypes.hashCode());
        System.out.println(type.parameterList().equals(copy.parameterList()));
        List<String> values = new ArrayList<String>();
        values.add("a");
        values.add(null);
        System.out.println(values.equals(Arrays.asList("a", null)));
        System.out.println(values.hashCode() == Arrays.asList("a", null).hashCode());
        System.out.println(values.equals(Arrays.asList("a")));
        System.out.println(values.equals(Arrays.asList("a", "b")));
        System.out.println(values.equals(null));
        List<String> synchronizedValues = Collections.synchronizedList(values);
        System.out.println(synchronizedValues.equals(Arrays.asList("a", null)));
        System.out.println(synchronizedValues.hashCode() == values.hashCode());
        System.out.println(type.parameterList().hashCode() == copy.parameterList().hashCode());
        System.out.println(type.equals(null));
        System.out.println(type.equals("(int,String[],double)long"));
        System.out.println(type.equals(type.changeReturnType(int.class)));
        System.out.println(type.changeParameterType(0, boolean.class));
        System.out.println(type.changeReturnType(void.class));
        System.out.println(type.insertParameterTypes(1, byte.class, char.class));
        System.out.println(type.appendParameterTypes(Arrays.<Class<?>>asList(Object.class, float.class)));
        System.out.println(type.insertParameterTypes(0, Arrays.<Class<?>>asList(short.class)));
        System.out.println(type.dropParameterTypes(1, 3));
        System.out.println(type.dropParameterTypes(0, 3));
        System.out.println(type.dropParameterTypes(3, 3).equals(type));
        System.out.println(MethodType.methodType(boolean.class, type));
        System.out.println(MethodType.genericMethodType(2, true));
        System.out.println(type.erase());
        System.out.println(type.generic());
        System.out.println(type.wrap());
        System.out.println(type.wrap().unwrap().equals(type));
        System.out.println(MethodType.methodType(Void.class, Void.class).unwrap());
        System.out.println(MethodType.methodType(void.class).hasPrimitives());
        System.out.println(MethodType.methodType(Object.class, Void.class).hasWrappers());
        System.out.println(MethodType.methodType(Void.class).hasWrappers());
        System.out.println(type.wrap().hasWrappers());
        System.out.println(type.wrap().hasPrimitives());
        MethodType all = MethodType.methodType(void.class, boolean.class, byte.class,
                char.class, short.class, int.class, float.class, long.class, double.class);
        System.out.println(all.wrap());
        System.out.println(all.wrap().unwrap().equals(all));
        System.out.println(MethodType.genericMethodType(255).parameterCount());
        Class<?>[] wide = new Class<?>[128];
        Arrays.fill(wide, long.class);
        wide[127] = int.class;
        System.out.println(MethodType.methodType(void.class, wide).parameterCount());
        rejects(IllegalArgumentException.class, () -> MethodType.methodType(void.class, void.class));
        rejects(NullPointerException.class, () -> MethodType.methodType(null));
        rejects(NullPointerException.class, () -> MethodType.methodType(void.class, new Class<?>[] {null}));
        rejects(NullPointerException.class, () -> MethodType.methodType(void.class, (Class<?>[]) null));
        rejects(IllegalArgumentException.class, () -> MethodType.genericMethodType(-1));
        rejects(IllegalArgumentException.class, () -> MethodType.genericMethodType(256));
        rejects(IllegalArgumentException.class, () -> MethodType.genericMethodType(255, true));
        rejects(IllegalArgumentException.class, () -> MethodType.methodType(void.class, wide).appendParameterTypes(int.class));
        wide[127] = long.class;
        rejects(IllegalArgumentException.class, () -> MethodType.methodType(void.class, wide));
        rejects(IndexOutOfBoundsException.class, () -> type.parameterType(-1));
        rejects(IndexOutOfBoundsException.class, () -> type.changeParameterType(3, int.class));
        rejects(IndexOutOfBoundsException.class, () -> type.insertParameterTypes(4));
        rejects(IndexOutOfBoundsException.class, () -> type.dropParameterTypes(2, 1));
        rejects(IndexOutOfBoundsException.class, () -> type.dropParameterTypes(0, 4));
        rejects(UnsupportedOperationException.class, () -> type.parameterList().set(0, Object.class));
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (InputStream stream = MethodTypeOperations.class.getResourceAsStream("MethodTypeOperations$Token.class")) {
            byte[] buffer = new byte[256];
            int count;
            while ((count = stream.read(buffer)) != -1) bytes.write(buffer, 0, count);
        }
        Class<?> first = new Loader().token(bytes.toByteArray());
        Class<?> second = new Loader().token(bytes.toByteArray());
        MethodType firstType = MethodType.methodType(first, first);
        MethodType secondType = MethodType.methodType(second, second);
        System.out.println(firstType.toMethodDescriptorString().equals(secondType.toMethodDescriptorString()));
        System.out.println(firstType.equals(secondType));
        System.out.println(firstType.changeReturnType(Object.class).equals(secondType.changeReturnType(Object.class)));
        System.out.println(firstType.changeParameterType(0, Object.class).equals(secondType.changeParameterType(0, Object.class)));
    }
}
