/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

import java.lang.invoke.MethodType;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class MethodDescriptors {
    static int initialized;
    public static class Lazy {
        static { initialized++; }
    }
    public static class Café {}
    private static class Loader extends ClassLoader {
        Class<?> define(byte[] code) {
            return defineClass(null, code, 0, code.length);
        }
    }

    private static String repeated(String text, int count) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < count; ++i) result.append(text);
        return result.toString();
    }

    private static void invalid(String descriptor) {
        try {
            MethodType.fromMethodDescriptorString(descriptor, null);
            throw new AssertionError("Accepted " + descriptor);
        } catch (IllegalArgumentException | TypeNotPresentException expected) {
            System.out.println(expected.getClass().getSimpleName());
        }
    }

    public static void main(String[] args) throws Exception {
        System.out.println(ClassLoader.getSystemClassLoader() == MethodDescriptors.class.getClassLoader());
        String[] descriptors = {"()V", "(ZBCSIFJD)D", "([[I[Ljava/lang/String;)Ljava/lang/Object;",
                "(LMethodDescriptors$Café;)LMethodDescriptors$Café;"};
        for (String descriptor : descriptors) {
            MethodType parsed = MethodType.fromMethodDescriptorString(descriptor, null);
            System.out.println(parsed.toMethodDescriptorString());
            System.out.println(parsed.equals(MethodType.methodType(parsed.returnType(), parsed.parameterArray())));
        }
        MethodType unicode = MethodType.methodType(Café.class, Café.class);
        System.out.println(unicode.toMethodDescriptorString());
        MethodType lazy = MethodType.fromMethodDescriptorString(
                "(LMethodDescriptors$Lazy;)[LMethodDescriptors$Lazy;", null);
        System.out.println(initialized);
        System.out.println(lazy.parameterType(0) == Lazy.class);
        System.out.println(initialized);
        Class.forName("MethodDescriptors$Lazy", true, MethodDescriptors.class.getClassLoader());
        System.out.println(initialized);
        String[] malformed = {"", "I", "()", "(I", "(V)V", "([V)V", "()[V", "()VV", "()Q",
                "(L;)V", "(Ljava.lang.String;)V", "(Ljava/lang/String)V", "(I))V", "([)V"};
        for (String descriptor : malformed) invalid(descriptor);
        invalid("(" + repeated("I", 256) + ")V");
        invalid("(" + repeated("J", 128) + ")V");
        invalid("(" + repeated("[", 256) + "I)V");
        System.out.println(MethodType.fromMethodDescriptorString("(" + repeated("I", 255) + ")V", null).parameterCount());
        System.out.println(MethodType.fromMethodDescriptorString("(" + repeated("J", 127) + "I)V", null).parameterCount());
        try {
            MethodType.fromMethodDescriptorString(null, null);
            throw new AssertionError();
        } catch (NullPointerException expected) {
            System.out.println("null");
        }
        try {
            MethodType.fromMethodDescriptorString("()Lmissing/Absent;", null);
            throw new AssertionError();
        } catch (TypeNotPresentException expected) {
            System.out.println(expected.typeName());
            System.out.println(expected.getCause() instanceof ClassNotFoundException);
        }
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (InputStream stream = MethodDescriptors.class.getResourceAsStream("MethodDescriptors$Café.class")) {
            byte[] buffer = new byte[256];
            int count;
            while ((count = stream.read(buffer)) != -1) bytes.write(buffer, 0, count);
        }
        Loader firstLoader = new Loader();
        Loader secondLoader = new Loader();
        Class<?> first = firstLoader.define(bytes.toByteArray());
        Class<?> second = secondLoader.define(bytes.toByteArray());
        String descriptor = "(LMethodDescriptors$Café;)LMethodDescriptors$Café;";
        MethodType firstType = MethodType.fromMethodDescriptorString(descriptor, firstLoader);
        MethodType secondType = MethodType.fromMethodDescriptorString(descriptor, secondLoader);
        System.out.println(firstType.parameterType(0) == first);
        System.out.println(secondType.returnType() == second);
        System.out.println(firstType.equals(secondType));
    }
}
