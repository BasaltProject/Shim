/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.lang.classfile;

import java.lang.constant.MethodTypeDesc;
import java.util.List;
import shim.classfile.SignatureFactories;
import shim.classfile.SignatureParser;

public interface MethodSignature {
    public static MethodSignature of(Signature signature, Signature... signatures) {
        return of(List.<Signature.TypeParam>of(), List.<Signature.ThrowableSig>of(), signature, signatures);
    }
    public static MethodSignature of(MethodTypeDesc methodTypeDesc) {
        return SignatureFactories.methodSignature(methodTypeDesc);
    }
    public static MethodSignature of(List<Signature.TypeParam> list, List<Signature.ThrowableSig> list2,
                                     Signature signature, Signature... signatures) {
        return SignatureFactories.methodSignature(list, list2, signature, signatures);
    }
    Signature result();
    List<Signature> arguments();
    String signatureString();
    public static MethodSignature parseFrom(String text) { return SignatureParser.parseMethod(text); }
    List<Signature.TypeParam> typeParameters();
    List<Signature.ThrowableSig> throwableSignatures();
}
