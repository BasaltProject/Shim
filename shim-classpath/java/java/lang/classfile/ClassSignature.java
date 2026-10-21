/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.lang.classfile;

import java.util.List;
import shim.classfile.SignatureFactories;
import shim.classfile.SignatureParser;

public interface ClassSignature {
    public static ClassSignature of(Signature.ClassTypeSig classTypeSig, Signature.ClassTypeSig... classTypeSigs) {
        return of(List.<Signature.TypeParam>of(), classTypeSig, classTypeSigs);
    }
    public static ClassSignature of(List<Signature.TypeParam> list, Signature.ClassTypeSig classTypeSig,
                                    Signature.ClassTypeSig... classTypeSigs) {
        return SignatureFactories.classSignature(list, classTypeSig, classTypeSigs);
    }
    String signatureString();
    public static ClassSignature parseFrom(String text) { return SignatureParser.parseClass(text); }
    List<Signature.TypeParam> typeParameters();
    Signature.ClassTypeSig superclassSignature();
    List<Signature.ClassTypeSig> superinterfaceSignatures();
}
