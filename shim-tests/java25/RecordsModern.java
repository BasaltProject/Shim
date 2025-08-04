/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

// Minimum release: 17

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.RecordComponent;
import java.lang.runtime.ObjectMethods;

public class RecordsModern {
    public record Point(int x, int y) {}
    public record Empty() {}
    public record Mixed(String name, long id, double value, boolean active) {}

    public static void main(String[] args) throws Throwable {
        testBasicRecords();
        testRecordEquality();
        testEmptyRecord();
        testMixedRecord();
        testRecordReflection();
        testObjectMethodsDirect();
        System.out.println("ALL RECORDS MODERN TESTS PASSED");
    }

    private static void testBasicRecords() {
        Point p1 = new Point(10, 20);
        System.out.println("p1.x: " + p1.x());
        System.out.println("p1.y: " + p1.y());
        System.out.println("p1.toString: " + p1.toString());
        System.out.println("p1.hashCode: " + p1.hashCode());

        Point p2 = new Point(10, 20);
        Point p3 = new Point(10, 21);
        System.out.println("p1.equals(p2): " + p1.equals(p2));
        System.out.println("p1.equals(p3): " + p1.equals(p3));
        System.out.println("p1.equals(null): " + p1.equals(null));
        System.out.println("p1.equals(other): " + p1.equals("string"));
    }

    private static void testRecordEquality() {
        record Floats(float f, double d) {}
        Floats f1 = new Floats(Float.NaN, -0.0);
        Floats f2 = new Floats(Float.NaN, 0.0);
        Floats f3 = new Floats(Float.NaN, -0.0);

        System.out.println("floats f1.equals(f2): " + f1.equals(f2));
        System.out.println("floats f1.equals(f3): " + f1.equals(f3));
        System.out.println("floats f1.hashCode == f3.hashCode: " + (f1.hashCode() == f3.hashCode()));
    }

    private static void testEmptyRecord() {
        Empty e1 = new Empty();
        Empty e2 = new Empty();
        System.out.println("e1.toString: " + e1.toString());
        System.out.println("e1.hashCode: " + e1.hashCode());
        System.out.println("e1.equals(e2): " + e1.equals(e2));
    }

    private static void testMixedRecord() {
        Mixed m1 = new Mixed("test", 123456789L, 3.14159, true);
        System.out.println("m1.name: " + m1.name());
        System.out.println("m1.id: " + m1.id());
        System.out.println("m1.value: " + m1.value());
        System.out.println("m1.active: " + m1.active());
        System.out.println("m1.toString: " + m1.toString());
        System.out.println("m1.hashCode: " + m1.hashCode());
    }

    private static void testRecordReflection() {
        Class<?> clazz = Point.class;
        System.out.println("clazz.isRecord: " + clazz.isRecord());
        RecordComponent[] components = clazz.getRecordComponents();
        System.out.println("components length: " + components.length);
        for (RecordComponent rc : components) {
            System.out.println("rc: " + rc.getName() + " type: " + rc.getType().getName());
        }
    }

    private static void testObjectMethodsDirect() throws Throwable {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        var getterX = lookup.findVirtual(Point.class, "x", MethodType.methodType(int.class));
        var getterY = lookup.findVirtual(Point.class, "y", MethodType.methodType(int.class));

        var cs = ObjectMethods.bootstrap(
            lookup,
            "toString",
            MethodType.methodType(String.class, Point.class),
            Point.class,
            "x;y",
            getterX, getterY
        );
        System.out.println("direct bootstrap toString: " + (cs != null));
    }
}
