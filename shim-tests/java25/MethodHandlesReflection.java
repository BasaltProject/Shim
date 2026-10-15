/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

// Minimum release: 17

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;

public class MethodHandlesReflection {
    public interface Greeter {
        String greet(String name);
    }

    public static class GreeterImpl implements Greeter {
        public String greet(String name) {
            return "Greetings " + name;
        }
    }

    public static class Target {
        public int count = 10;
        public static String tag = "init";

        public Target() {}

        public Target(int count) {
            this.count = count;
        }

        public int add(int a, int b) {
            return a + b;
        }

        public static int multiply(int a, int b) {
            return a * b;
        }
    }

    public static void main(String[] args) throws Throwable {
        testLookup();
        testMethodHandles();
        testFieldHandles();
        testVarHandles();
        System.out.println("ALL METHOD HANDLES TESTS PASSED");
    }

    private static void testLookup() {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        System.out.println("lookup class match: " + (lookup.lookupClass() == MethodHandlesReflection.class));
        System.out.println("lookup full privilege: " + lookup.hasFullPrivilegeAccess());

        MethodHandles.Lookup dropped = lookup.dropLookupMode(MethodHandles.Lookup.PRIVATE);
        System.out.println("dropped has private: " + ((dropped.lookupModes() & MethodHandles.Lookup.PRIVATE) != 0));

        MethodHandles.Lookup inOther = lookup.in(String.class);
        System.out.println("inOther has private: " + ((inOther.lookupModes() & MethodHandles.Lookup.PRIVATE) != 0));
        System.out.println("inOther has package: " + ((inOther.lookupModes() & MethodHandles.Lookup.PACKAGE) != 0));
    }

    private static void testMethodHandles() throws Throwable {
        MethodHandles.Lookup lookup = MethodHandles.lookup();

        MethodHandle idStr = MethodHandles.identity(String.class);
        System.out.println("idStr invoke: " + idStr.invokeWithArguments("hello"));

        MethodHandle idInt = MethodHandles.identity(int.class);
        System.out.println("idInt invoke: " + idInt.invokeWithArguments(123));

        MethodHandle constH = MethodHandles.constant(String.class, "fixed");
        System.out.println("constH invoke: " + constH.invokeWithArguments());

        MethodHandle multH = lookup.findStatic(Target.class, "multiply", MethodType.methodType(int.class, int.class, int.class));
        System.out.println("multH invoke: " + multH.invokeWithArguments(6, 7));

        Target target = new Target(5);
        MethodHandle addH = lookup.findVirtual(Target.class, "add", MethodType.methodType(int.class, int.class, int.class));
        System.out.println("addH invoke: " + addH.invokeWithArguments(target, 8, 9));

        Greeter greeter = new GreeterImpl();
        MethodHandle greetH = lookup.findVirtual(Greeter.class, "greet", MethodType.methodType(String.class, String.class));
        System.out.println("greetH invoke: " + greetH.invokeWithArguments(greeter, "World"));

        MethodHandle ctorH = lookup.findConstructor(Target.class, MethodType.methodType(void.class, int.class));
        Target created = (Target) ctorH.invokeWithArguments(42);
        System.out.println("created count: " + created.count);

        MethodHandle boundAdd = addH.bindTo(target);
        System.out.println("boundAdd invoke: " + boundAdd.invokeWithArguments(10, 20));

        MethodHandle dropped = MethodHandles.dropArguments(multH, 1, String.class, double.class);
        System.out.println("dropped invoke: " + dropped.invokeWithArguments(4, "unused", 3.14, 5));

        Method intToStrM = Integer.class.getMethod("toHexString", int.class);
        MethodHandle intToStrH = lookup.unreflect(intToStrM);
        MethodHandle filtered = MethodHandles.filterReturnValue(multH, intToStrH);
        System.out.println("filtered invoke: " + filtered.invokeWithArguments(16, 2));

        Method addM = Target.class.getMethod("add", int.class, int.class);
        MethodHandle unrefAdd = lookup.unreflect(addM);
        System.out.println("unrefAdd invoke: " + unrefAdd.invokeWithArguments(target, 11, 22));

        Constructor<Target> ctorM = Target.class.getConstructor(int.class);
        MethodHandle unrefCtor = lookup.unreflectConstructor(ctorM);
        Target unrefCreated = (Target) unrefCtor.invokeWithArguments(99);
        System.out.println("unrefCreated count: " + unrefCreated.count);
    }

    private static void testFieldHandles() throws Throwable {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        Target target = new Target(100);

        MethodHandle getCount = lookup.findGetter(Target.class, "count", int.class);
        MethodHandle setCount = lookup.findSetter(Target.class, "count", int.class);
        System.out.println("getCount invoke: " + getCount.invokeWithArguments(target));
        setCount.invokeWithArguments(target, 200);
        System.out.println("getCount after set: " + getCount.invokeWithArguments(target));

        Field countF = Target.class.getField("count");
        MethodHandle unrefGet = lookup.unreflectGetter(countF);
        MethodHandle unrefSet = lookup.unreflectSetter(countF);
        unrefSet.invokeWithArguments(target, 300);
        System.out.println("unrefGet after set: " + unrefGet.invokeWithArguments(target));

        MethodHandle getTag = lookup.findStaticGetter(Target.class, "tag", String.class);
        MethodHandle setTag = lookup.findStaticSetter(Target.class, "tag", String.class);
        System.out.println("getTag invoke: " + getTag.invokeWithArguments());
        setTag.invokeWithArguments("modified");
        System.out.println("getTag after set: " + getTag.invokeWithArguments());
    }

    private static void testVarHandles() throws Throwable {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        Target target = new Target(50);

        VarHandle countVh = lookup.findVarHandle(Target.class, "count", int.class);
        System.out.println("countVh varType: " + countVh.varType().getName());
        System.out.println("countVh coords: " + countVh.coordinateTypes().size());

        MethodHandle getMh = countVh.toMethodHandle(VarHandle.AccessMode.GET);
        MethodHandle setMh = countVh.toMethodHandle(VarHandle.AccessMode.SET);
        MethodHandle casMh = countVh.toMethodHandle(VarHandle.AccessMode.COMPARE_AND_SET);
        MethodHandle gasMh = countVh.toMethodHandle(VarHandle.AccessMode.GET_AND_SET);
        MethodHandle gaaMh = countVh.toMethodHandle(VarHandle.AccessMode.GET_AND_ADD);

        System.out.println("countVh get: " + getMh.invokeWithArguments(target));
        setMh.invokeWithArguments(target, 60);
        System.out.println("countVh get after set: " + getMh.invokeWithArguments(target));
        System.out.println("countVh cas true: " + casMh.invokeWithArguments(target, 60, 70));
        System.out.println("countVh cas false: " + casMh.invokeWithArguments(target, 60, 80));
        System.out.println("countVh get: " + getMh.invokeWithArguments(target));
        System.out.println("countVh getAndSet: " + gasMh.invokeWithArguments(target, 90));
        System.out.println("countVh get: " + getMh.invokeWithArguments(target));
        System.out.println("countVh getAndAdd: " + gaaMh.invokeWithArguments(target, 10));
        System.out.println("countVh get: " + getMh.invokeWithArguments(target));

        VarHandle tagVh = lookup.findStaticVarHandle(Target.class, "tag", String.class);
        MethodHandle staticGet = tagVh.toMethodHandle(VarHandle.AccessMode.GET);
        MethodHandle staticSet = tagVh.toMethodHandle(VarHandle.AccessMode.SET);
        MethodHandle staticCas = tagVh.toMethodHandle(VarHandle.AccessMode.COMPARE_AND_SET);

        System.out.println("tagVh get: " + staticGet.invokeWithArguments());
        staticSet.invokeWithArguments("static_var");
        System.out.println("tagVh get after set: " + staticGet.invokeWithArguments());
        System.out.println("tagVh cas: " + staticCas.invokeWithArguments("static_var", "updated_var"));
        System.out.println("tagVh get: " + staticGet.invokeWithArguments());

        VarHandle arrayVh = MethodHandles.arrayElementVarHandle(int[].class);
        int[] arr = new int[]{100, 200, 300};
        MethodHandle arrayGet = arrayVh.toMethodHandle(VarHandle.AccessMode.GET);
        MethodHandle arraySet = arrayVh.toMethodHandle(VarHandle.AccessMode.SET);
        MethodHandle arrayCas = arrayVh.toMethodHandle(VarHandle.AccessMode.COMPARE_AND_SET);
        MethodHandle arrayGaa = arrayVh.toMethodHandle(VarHandle.AccessMode.GET_AND_ADD);

        System.out.println("arrayVh get: " + arrayGet.invokeWithArguments(arr, 1));
        arraySet.invokeWithArguments(arr, 1, 250);
        System.out.println("arrayVh get after set: " + arrayGet.invokeWithArguments(arr, 1));
        System.out.println("arrayVh cas: " + arrayCas.invokeWithArguments(arr, 1, 250, 275));
        System.out.println("arrayVh get: " + arrayGet.invokeWithArguments(arr, 1));
        System.out.println("arrayVh getAndAdd: " + arrayGaa.invokeWithArguments(arr, 1, 25));
        System.out.println("arrayVh get: " + arrayGet.invokeWithArguments(arr, 1));
    }
}
