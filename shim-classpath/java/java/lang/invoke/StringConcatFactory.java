/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package java.lang.invoke;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import shim.Assembler;
import shim.Classes;
import shim.ConstantPool;
import shim.ConstantPool.PoolEntry;
import shim.SystemClassLoader;

import static shim.Stream.write1;
import static shim.Stream.write2;
import static shim.Stream.write4;
import static shim.Stream.set4;

public final class StringConcatFactory {
    private static int nextNumber;
    private static final int kMaxSlots = 200;

    private StringConcatFactory() {
    }

    public static CallSite makeConcat(MethodHandles.Lookup lookup, String name, MethodType type)
            throws StringConcatException {
        if (type == null) {
            throw new NullPointerException();
        }
        StringBuilder recipe = new StringBuilder();
        for (MethodType.Parameter ignored : type.parameters()) {
            recipe.append('\1');
        }
        return makeConcatWithConstants(lookup, name, type, recipe.toString());
    }

    public static CallSite makeConcatWithConstants(MethodHandles.Lookup lookup, String name,
            MethodType type, String recipe, Object... constants) throws StringConcatException {
        if (lookup == null || name == null || type == null || recipe == null || constants == null) {
            throw new NullPointerException();
        }
        if (!lookup.hasFullPrivilegeAccess() || type.footprint() > kMaxSlots
                || !type.returnType().isAssignableFrom(String.class)) {
            throw new StringConcatException("Invalid lookup or concatenation method type");
        }
        List<MethodType.Parameter> parameters = new ArrayList<MethodType.Parameter>();
        for (MethodType.Parameter parameter : type.parameters()) {
            parameters.add(parameter);
        }
        int dynamicCount = 0;
        int constantCount = 0;
        for (int i = 0; i < recipe.length(); ++i) {
            if (recipe.charAt(i) == '\1') {
                ++dynamicCount;
            } else if (recipe.charAt(i) == '\2') {
                ++constantCount;
            }
        }
        if (dynamicCount != parameters.size() || constantCount != constants.length) {
            throw new StringConcatException("Recipe argument counts do not match");
        }
        String[] strings = new String[constants.length];
        for (int i = 0; i < constants.length; ++i) {
            if (constants[i] == null) {
                throw new NullPointerException();
            }
            strings[i] = String.valueOf(constants[i]);
        }
        String className;
        synchronized (StringConcatFactory.class) {
            className = "shim/concat/Concat$" + nextNumber++;
        }
        try {
            List<PoolEntry> pool = new ArrayList<PoolEntry>();
            byte[] code = concatenate(pool, type, parameters, recipe, strings);
            Assembler.MethodData method = new Assembler.MethodData(
                    Assembler.ACC_PUBLIC | Assembler.ACC_STATIC,
                    ConstantPool.addUtf8(pool, "make"),
                    ConstantPool.addUtf8(pool, type.toMethodDescriptorString()), code);
            int nameIndex = ConstantPool.addClass(pool, className);
            int parentIndex = ConstantPool.addClass(pool, "java/lang/Object");
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Assembler.writeClass(out, pool, nameIndex, parentIndex, new int[0],
                    new Assembler.FieldData[0], new Assembler.MethodData[] {method});
            byte[] data = out.toByteArray();
            Class<?> generated = SystemClassLoader.getClass(
                    Classes.defineVMClass(type.loader, data, 0, data.length));
            return new ConstantCallSite(new MethodHandle(MethodHandle.REF_invokeStatic, type.loader,
                    Classes.toVMMethod(generated.getMethod("make", type.parameterArray()))));
        } catch (IOException exception) {
            throw new StringConcatException("Unable to generate concatenation method", exception);
        } catch (NoSuchMethodException exception) {
            throw new StringConcatException("Unable to link concatenation method", exception);
        }
    }

    private static void invoke(ByteArrayOutputStream code, List<PoolEntry> pool,
            String owner, String name, String descriptor, int opcode) throws IOException {
        write1(code, opcode);
        write2(code, ConstantPool.addMethodRef(pool, owner, name, descriptor) + 1);
    }

    private static void literal(ByteArrayOutputStream code, List<PoolEntry> pool, String value)
            throws IOException {
        write1(code, Assembler.ldc_w);
        write2(code, ConstantPool.addString(pool, value) + 1);
        invoke(code, pool, "java/lang/StringBuilder", "append",
                "(Ljava/lang/String;)Ljava/lang/StringBuilder;", Assembler.invokevirtual);
    }

    private static byte[] concatenate(List<PoolEntry> pool, MethodType type,
            List<MethodType.Parameter> parameters, String recipe, String[] constants) throws IOException {
        ByteArrayOutputStream code = new ByteArrayOutputStream();
        write2(code, 3);
        write2(code, type.footprint());
        write4(code, 0);
        write1(code, Assembler.new_);
        write2(code, ConstantPool.addClass(pool, "java/lang/StringBuilder") + 1);
        write1(code, Assembler.dup);
        invoke(code, pool, "java/lang/StringBuilder", "<init>", "()V", Assembler.invokespecial);
        int dynamic = 0;
        int constant = 0;
        int start = 0;
        for (int i = 0; i < recipe.length(); ++i) {
            char marker = recipe.charAt(i);
            if (marker != '\1' && marker != '\2') {
                continue;
            }
            if (i > start) {
                literal(code, pool, recipe.substring(start, i));
            }
            if (marker == '\2') {
                literal(code, pool, constants[constant++]);
            } else {
                MethodType.Parameter parameter = parameters.get(dynamic++);
                write1(code, parameter.load());
                write1(code, parameter.position());
                String descriptor;
                switch (parameter.spec().charAt(0)) {
                    case 'B':
                    case 'S': descriptor = "I"; break;
                    case 'L':
                    case '[': descriptor = "Ljava/lang/Object;"; break;
                    default: descriptor = parameter.spec();
                }
                invoke(code, pool, "java/lang/StringBuilder", "append",
                        "(" + descriptor + ")Ljava/lang/StringBuilder;", Assembler.invokevirtual);
            }
            start = i + 1;
        }
        if (start < recipe.length()) {
            literal(code, pool, recipe.substring(start));
        }
        invoke(code, pool, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;",
                Assembler.invokevirtual);
        write1(code, Assembler.areturn);
        write2(code, 0);
        write2(code, 0);
        byte[] result = code.toByteArray();
        set4(result, 4, result.length - 12);
        return result;
    }
}
