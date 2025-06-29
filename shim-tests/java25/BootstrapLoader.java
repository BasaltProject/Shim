/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

public class BootstrapLoader {
    private static class Loader extends ClassLoader {
        Loader() { super(); }
        Loader(ClassLoader parent) { super(parent); }
    }

    public static void main(String[] args) throws Exception {
        Loader isolated = new Loader(null);
        Loader ordinary = new Loader();
        System.out.println(isolated.getParent() == null);
        System.out.println(ordinary.getParent() == ClassLoader.getSystemClassLoader());
        System.out.println(Object.class.getClassLoader() == null);
        System.out.println(String.class.getClassLoader() == null);
        System.out.println(int.class.getClassLoader() == null);
        System.out.println(String[].class.getClassLoader() == null);
        System.out.println(int[][].class.getClassLoader() == null);
        System.out.println(BootstrapLoader[].class.getClassLoader() == BootstrapLoader.class.getClassLoader());
        System.out.println(isolated.loadClass("java.lang.String") == String.class);
        System.out.println(ordinary.loadClass("BootstrapLoader") == BootstrapLoader.class);
        System.out.println(Class.forName("java.lang.String", false, null) == String.class);
        try {
            isolated.loadClass("BootstrapLoader");
            throw new AssertionError();
        } catch (ClassNotFoundException expected) {
            System.out.println("isolated");
        }
        try {
            Class.forName("BootstrapLoader", false, null);
            throw new AssertionError();
        } catch (ClassNotFoundException expected) {
            System.out.println("bootstrap only");
        }
        System.out.println(Object.class.getResource("Object.class") != null);
        System.out.println(isolated.getResource("java/lang/Object.class") != null);
        System.out.println(isolated.getResource("BootstrapLoader.class") == null);
        System.out.println(ordinary.getResource("BootstrapLoader.class") != null);
        System.out.println(Object.class.getDeclaredClasses().length == 0);
    }
}
