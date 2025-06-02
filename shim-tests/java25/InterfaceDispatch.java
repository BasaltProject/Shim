/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

public class InterfaceDispatch {
    interface Marker {}
    interface Operation { int value(); }
    static class Parent implements Marker, Operation {
        public int hashCode() { return 37; }
        public String toString() { return "parent"; }
        public boolean equals(Object other) { return this == other; }
        public int value() { return 11; }
    }
    static class Child extends Parent {
        public int hashCode() { return 73; }
        public String toString() { return "child"; }
        public int value() { return 29; }
    }
    public static void main(String[] args) {
        Marker marker = new Child();
        System.out.println(marker.hashCode());
        System.out.println(marker.toString());
        System.out.println(marker.equals(marker));
        System.out.println(marker.equals(new Parent()));
        Operation operation = (Operation) marker;
        System.out.println(operation.value());
    }
}
