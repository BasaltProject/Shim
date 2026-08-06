/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 * Minimum release: 21
 */

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

public class CatchExceptionArity {
    static int fail(Throwable t) { return -1; }

    static int failOne(Throwable t, String s) { return s.length(); }

    public static void main(String[] args) throws Throwable {
        MethodHandles.Lookup l = MethodHandles.lookup();
        MethodHandle parse = l.findStatic(Integer.class, "parseInt", MethodType.methodType(int.class, String.class));
        MethodHandle target = MethodHandles.dropArguments(parse, 1, Object.class);
        MethodHandle c = MethodHandles.catchException(target, Throwable.class,
            l.findStatic(CatchExceptionArity.class, "fail", MethodType.methodType(int.class, Throwable.class)));
        System.out.println((int) c.invokeExact("12", (Object) null) + " " + (int) c.invokeExact("abc", (Object) null));
        MethodHandle d = MethodHandles.catchException(target, Throwable.class,
            l.findStatic(CatchExceptionArity.class, "failOne", MethodType.methodType(int.class, Throwable.class, String.class)));
        System.out.println((int) d.invokeExact("abc", (Object) null));
        System.out.println(c.invokeWithArguments("zz", null));
    }
}
