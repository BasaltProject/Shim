/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

// Minimum release: 9
import java.lang.invoke.CallSite;
import java.lang.invoke.ConstantCallSite;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.StringConcatException;
import java.lang.invoke.StringConcatFactory;

public class StringConcatBootstrap {
    public static void main(String[] args) throws Exception {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        System.out.println(lookup.lookupClass() == StringConcatBootstrap.class);
        MethodType type = MethodType.methodType(String.class, int.class, long.class);
        CallSite site = StringConcatFactory.makeConcatWithConstants(lookup, "concat", type, "\1/\1");
        System.out.println(site instanceof ConstantCallSite);
        System.out.println(site.getTarget().type().toMethodDescriptorString());
        System.out.println(site.dynamicInvoker() == site.getTarget());
        System.out.println(StringConcatFactory.makeConcat(lookup, "concat",
                MethodType.methodType(Object.class, char.class)).getTarget().type().toMethodDescriptorString());
        try {
            site.setTarget(null);
            throw new AssertionError();
        } catch (UnsupportedOperationException expected) {
            System.out.println("immutable");
        }
        try {
            StringConcatFactory.makeConcatWithConstants(lookup, "concat", type, "\1");
            throw new AssertionError();
        } catch (StringConcatException expected) {
            System.out.println("recipe");
        }
        try {
            StringConcatFactory.makeConcat(MethodHandles.publicLookup(), "concat", type);
            throw new AssertionError();
        } catch (StringConcatException expected) {
            System.out.println("lookup");
        }
        try {
            StringConcatFactory.makeConcatWithConstants(lookup, "concat",
                    MethodType.methodType(String.class), "\2", new Object[] {null});
            throw new AssertionError();
        } catch (NullPointerException expected) {
            System.out.println("null constant");
        }
    }
}
