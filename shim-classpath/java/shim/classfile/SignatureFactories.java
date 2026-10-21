/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.classfile;

import java.lang.classfile.ClassSignature;
import java.lang.classfile.MethodSignature;
import java.lang.classfile.Signature;
import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/** Argument checks and construction behind the signature factories. */
public final class SignatureFactories {
    private SignatureFactories() { }

    public static List<Signature.TypeArg> typeArgs(Signature.TypeArg[] args) {
        return TempEntries.listOf(args);
    }

    public static String internalName(ClassDesc desc) {
        TempEntries.nonNull(desc);
        if (!desc.isClassOrInterface()) {
            throw new IllegalArgumentException(desc.descriptorString());
        }
        String d = desc.descriptorString();
        return d.substring(1, d.length() - 1);
    }

    public static Signature.TypeParam typeParam(String id, Optional<Signature.RefTypeSig> classBound,
                                                Signature.RefTypeSig[] interfaceBounds) {
        TempEntries.nonNull(id);
        TempEntries.nonNull(classBound);
        return new SignaturesImpl.TypeParamImpl(id, classBound, TempEntries.listOf(interfaceBounds));
    }

    private static <T> List<T> requireList(List<T> list) {
        TempEntries.nonNull(list);
        List<T> out = new ArrayList<>(list.size());
        for (T t : list) {
            out.add(TempEntries.nonNull(t));
        }
        return Collections.unmodifiableList(out);
    }

    public static ClassSignature classSignature(List<Signature.TypeParam> typeParams, Signature.ClassTypeSig superclass,
                                                Signature.ClassTypeSig[] interfaces) {
        List<Signature.TypeParam> tps = requireList(typeParams);
        TempEntries.nonNull(superclass);
        return new SignaturesImpl.ClassSignatureImpl(tps, superclass, TempEntries.listOf(interfaces));
    }

    public static MethodSignature methodSignature(List<Signature.TypeParam> typeParams,
                                                  List<Signature.ThrowableSig> exceptions, Signature result,
                                                  Signature[] arguments) {
        List<Signature.TypeParam> tps = requireList(typeParams);
        List<Signature.ThrowableSig> exs = requireList(exceptions);
        TempEntries.nonNull(result);
        return new SignaturesImpl.MethodSignatureImpl(tps, exs, result, TempEntries.listOf(arguments));
    }

    public static MethodSignature methodSignature(MethodTypeDesc desc) {
        TempEntries.nonNull(desc);
        Signature result = Signature.of(desc.returnType());
        List<Signature> args = new ArrayList<>(desc.parameterCount());
        for (int i = 0; i < desc.parameterCount(); i++) {
            args.add(Signature.of(desc.parameterType(i)));
        }
        return new SignaturesImpl.MethodSignatureImpl(Collections.<Signature.TypeParam>emptyList(),
            Collections.<Signature.ThrowableSig>emptyList(), result, Collections.unmodifiableList(args));
    }
}
