/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

// Minimum release: 17

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.Arrays;

public class ReflectionModern {
    interface SampleInterface {
        default String defaultGreeting(String name) {
            return "Hello, " + name;
        }

        static void staticHelper() {}

        void abstractMethod(int a, long b);
    }

    static class SampleTarget implements SampleInterface {
        public static final int CONSTANT = 42;
        private volatile String status;

        public SampleTarget() {}

        public SampleTarget(String status, int ... extra) {
            this.status = status;
        }

        public synchronized void abstractMethod(int a, long b) {}

        public static double compute(double x, double y) {
            return x * y;
        }

        public final String varargMethod(String prefix, Object ... items) {
            return prefix + items.length;
        }
    }

    public static void main(String[] args) throws Throwable {
        testExecutableAndParameters();
        testMethodDefault();
        testGenericStrings();
        testAccessibleObject();
        testModifierUtilities();
        System.out.println("ALL REFLECTION MODERN TESTS PASSED");
    }

    private static void testExecutableAndParameters() throws Throwable {
        Method m = SampleTarget.class.getMethod("varargMethod", String.class, Object[].class);
        if (!(m instanceof Executable)) {
            throw new AssertionError("Method must be instance of Executable");
        }
        int pCount = m.getParameterCount();
        Parameter[] params = m.getParameters();
        if (params.length != pCount) {
            throw new AssertionError("Mismatch in param count: " + params.length + " vs " + pCount);
        }

        Parameter p0 = params[0];
        Parameter p1 = params[1];

        boolean p0Var = p0.isVarArgs();
        boolean p1Var = p1.isVarArgs();

        Executable decl = p0.getDeclaringExecutable();
        boolean declMatch = decl.equals(m);

        Constructor<?> c = SampleTarget.class.getConstructor(String.class, int[].class);
        if (!(c instanceof Executable)) {
            throw new AssertionError("Constructor must be instance of Executable");
        }
        int cCount = c.getParameterCount();
        Parameter[] cParams = c.getParameters();
        boolean cVar = c.isVarArgs();

        System.out.println("Executable & Parameters: pCount=" + pCount + " p0Type=" + p0.getType().getSimpleName() +
            " p0Var=" + p0Var + " p1Type=" + p1.getType().getSimpleName() + " p1Var=" + p1Var +
            " declMatch=" + declMatch +
            " cCount=" + cCount + " cParams=" + cParams.length + " cVar=" + cVar);
    }

    private static void testMethodDefault() throws Throwable {
        Method defM = SampleInterface.class.getMethod("defaultGreeting", String.class);
        Method absM = SampleInterface.class.getMethod("abstractMethod", int.class, long.class);
        Method statM = SampleInterface.class.getMethod("staticHelper");
        Method targetM = SampleTarget.class.getMethod("abstractMethod", int.class, long.class);

        boolean defIsDef = defM.isDefault();
        boolean absIsDef = absM.isDefault();
        boolean statIsDef = statM.isDefault();
        boolean targetIsDef = targetM.isDefault();

        System.out.println("Method.isDefault: def=" + defIsDef + " abs=" + absIsDef +
            " stat=" + statIsDef + " target=" + targetIsDef);
    }

    private static void testGenericStrings() throws Throwable {
        Class<?> clazz = SampleTarget.class;
        Class<?> iface = SampleInterface.class;
        String classGen = clazz.toGenericString();
        String ifaceGen = iface.toGenericString();

        Field fConst = SampleTarget.class.getField("CONSTANT");
        Field fStatus = SampleTarget.class.getDeclaredField("status");
        String fConstGen = fConst.toGenericString();
        String fStatusGen = fStatus.toGenericString();

        Method mCompute = SampleTarget.class.getMethod("compute", double.class, double.class);
        String mGen = mCompute.toGenericString();

        Constructor<?> cDef = SampleTarget.class.getConstructor();
        String cGen = cDef.toGenericString();

        System.out.println("toGenericString: class=" + classGen + " iface=" + ifaceGen +
            " fConst=" + fConstGen + " fStatus=" + fStatusGen +
            " mCompute=" + mGen + " cDef=" + cGen);
    }

    private static void testAccessibleObject() throws Throwable {
        SampleTarget target = new SampleTarget();
        Field f = SampleTarget.class.getDeclaredField("status");
        boolean canBefore = f.canAccess(target);
        boolean tried = f.trySetAccessible();
        boolean canAfter = f.canAccess(target);

        boolean caughtNull = false;
        try {
            f.setAccessible(false);
            f.canAccess(null);
        } catch (IllegalArgumentException e) {
            caughtNull = true;
        }

        System.out.println("AccessibleObject: canBefore=" + canBefore + " tried=" + tried +
            " canAfter=" + canAfter + " caughtNull=" + caughtNull);
    }

    private static void testModifierUtilities() {
        int m1 = Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL;
        String s1 = Modifier.toString(m1);

        int m2 = Modifier.PROTECTED | Modifier.SYNCHRONIZED;
        String s2 = Modifier.toString(m2);

        int m3 = Modifier.PRIVATE | Modifier.VOLATILE;
        String s3 = Modifier.toString(m3);

        boolean isSync = Modifier.isSynchronized(m2);
        boolean isVol = Modifier.isVolatile(m3);
        boolean isStrict = Modifier.isStrict(Modifier.STRICT);

        int classMods = Modifier.classModifiers();
        int methMods = Modifier.methodModifiers();
        int fieldMods = Modifier.fieldModifiers();
        int paramMods = Modifier.parameterModifiers();

        System.out.println("Modifier: s1=" + s1 + " s2=" + s2 + " s3=" + s3 +
            " isSync=" + isSync + " isVol=" + isVol + " isStrict=" + isStrict +
            " hasClassPublic=" + ((classMods & Modifier.PUBLIC) != 0) +
            " hasMethSync=" + ((methMods & Modifier.SYNCHRONIZED) != 0) +
            " hasFieldVol=" + ((fieldMods & Modifier.VOLATILE) != 0) +
            " paramFinalOnly=" + (paramMods == Modifier.FINAL));
    }
}
