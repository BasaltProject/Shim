/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

// Minimum release: 17

import java.lang.reflect.RecordComponent;
import java.util.Arrays;
import java.util.Comparator;

public class ClassHierarchyReflection {
    public static void main(String[] args) {
        testNestmates();
        testSealedClasses();
        testRecordClasses();
        System.out.println("ALL CLASS HIERARCHY TESTS PASSED");
    }

    static class InnerA {}
    static class InnerB {
        static class DeepNested {}
    }
    interface InnerInterface {}

    private static void testNestmates() {
        Class<?> top = ClassHierarchyReflection.class;
        Class<?> a = InnerA.class;
        Class<?> b = InnerB.class;
        Class<?> deep = InnerB.DeepNested.class;
        Class<?> iface = InnerInterface.class;

        System.out.println("top host: " + top.getNestHost().getName());
        System.out.println("a host: " + a.getNestHost().getName());
        System.out.println("b host: " + b.getNestHost().getName());
        System.out.println("deep host: " + deep.getNestHost().getName());
        System.out.println("iface host: " + iface.getNestHost().getName());

        System.out.println("a nestmate b: " + a.isNestmateOf(b));
        System.out.println("a nestmate deep: " + a.isNestmateOf(deep));
        System.out.println("a nestmate iface: " + a.isNestmateOf(iface));
        System.out.println("top nestmate a: " + top.isNestmateOf(a));
        System.out.println("a nestmate String: " + a.isNestmateOf(String.class));
        try {
            a.isNestmateOf(null);
            throw new AssertionError();
        } catch (NullPointerException expected) {
            System.out.println("a nestmate null: npe");
        }

        System.out.println("int host: " + int.class.getNestHost().getName());
        System.out.println("int[] host: " + int[].class.getNestHost().getName());
        System.out.println("int nestmate int: " + int.class.isNestmateOf(int.class));
        System.out.println("int nestmate long: " + int.class.isNestmateOf(long.class));

        Class<?>[] members = top.getNestMembers();
        Arrays.sort(members, Comparator.comparing(Class::getName));
        System.out.println("members count: " + members.length);
        for (Class<?> m : members) {
            System.out.println("member: " + m.getSimpleName());
        }

        Class<?>[] aMembers = a.getNestMembers();
        Arrays.sort(aMembers, Comparator.comparing(Class::getName));
        System.out.println("aMembers equals members: " + Arrays.equals(members, aMembers));
    }

    sealed interface Shape permits Circle, Rectangle {}
    static final class Circle implements Shape {}
    static sealed class Rectangle implements Shape permits Square {}
    static final class Square extends Rectangle {}
    static class NormalClass {}

    private static void testSealedClasses() {
        System.out.println("Shape isSealed: " + Shape.class.isSealed());
        Class<?>[] shapePermitted = Shape.class.getPermittedSubclasses();
        Arrays.sort(shapePermitted, Comparator.comparing(Class::getName));
        System.out.println("Shape permitted count: " + shapePermitted.length);
        for (Class<?> p : shapePermitted) {
            System.out.println("Shape permitted: " + p.getSimpleName());
        }

        System.out.println("Circle isSealed: " + Circle.class.isSealed());
        System.out.println("Circle permitted: " + Circle.class.getPermittedSubclasses());

        System.out.println("Rectangle isSealed: " + Rectangle.class.isSealed());
        Class<?>[] rectPermitted = Rectangle.class.getPermittedSubclasses();
        System.out.println("Rectangle permitted count: " + rectPermitted.length);
        System.out.println("Rectangle permitted: " + rectPermitted[0].getSimpleName());

        System.out.println("Square isSealed: " + Square.class.isSealed());
        System.out.println("Square permitted: " + Square.class.getPermittedSubclasses());

        System.out.println("NormalClass isSealed: " + NormalClass.class.isSealed());
        System.out.println("NormalClass permitted: " + NormalClass.class.getPermittedSubclasses());
    }

    record Point(int x, int y) {}
    record EmptyRecord() {}

    private static void testRecordClasses() {
        System.out.println("Point isRecord: " + Point.class.isRecord());
        System.out.println("Point super: " + Point.class.getSuperclass().getName());
        System.out.println("EmptyRecord isRecord: " + EmptyRecord.class.isRecord());
        System.out.println("EmptyRecord super: " + EmptyRecord.class.getSuperclass().getName());
        System.out.println("NormalClass isRecord: " + NormalClass.class.isRecord());
        System.out.println("Record isRecord: " + Record.class.isRecord());

        Point p = new Point(10, 20);
        System.out.println("point x: " + p.x());
        System.out.println("point y: " + p.y());

        RecordComponent[] pointComps = Point.class.getRecordComponents();
        System.out.println("Point components count: " + pointComps.length);
        for (RecordComponent comp : pointComps) {
            System.out.println("comp: " + comp.getName() + " type: " + comp.getType().getName()
                + " accessor: " + comp.getAccessor().getName() + " declaring: " + comp.getDeclaringRecord().getSimpleName()
                + " str: " + comp);
        }

        RecordComponent[] emptyComps = EmptyRecord.class.getRecordComponents();
        System.out.println("EmptyRecord components count: " + emptyComps.length);

        System.out.println("NormalClass components: " + NormalClass.class.getRecordComponents());
        System.out.println("Record components: " + Record.class.getRecordComponents());
        System.out.println("String components: " + String.class.getRecordComponents());
    }
}
