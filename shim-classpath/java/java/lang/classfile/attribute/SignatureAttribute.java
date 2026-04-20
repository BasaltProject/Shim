/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.lang.classfile.attribute;

import java.lang.classfile.Attribute;
import java.lang.classfile.ClassElement;
import java.lang.classfile.ClassSignature;
import java.lang.classfile.FieldElement;
import java.lang.classfile.MethodElement;
import java.lang.classfile.MethodSignature;
import java.lang.classfile.Signature;
import java.lang.classfile.constantpool.Utf8Entry;

public interface SignatureAttribute extends Attribute<SignatureAttribute>, ClassElement, MethodElement, FieldElement {
    Utf8Entry signature();

    default ClassSignature asClassSignature() {
        return ClassSignature.parseFrom(signature().stringValue());
    }

    default MethodSignature asMethodSignature() {
        return MethodSignature.parseFrom(signature().stringValue());
    }

    default Signature asTypeSignature() {
        return Signature.parseFrom(signature().stringValue());
    }

    static SignatureAttribute of(ClassSignature classSignature) {
        return of(shim.classfile.AttrSupport.pool().utf8Entry(classSignature.signatureString()));
    }

    static SignatureAttribute of(MethodSignature methodSignature) {
        return of(shim.classfile.AttrSupport.pool().utf8Entry(methodSignature.signatureString()));
    }

    static SignatureAttribute of(Signature signature) {
        return of(shim.classfile.AttrSupport.pool().utf8Entry(signature.signatureString()));
    }

    static SignatureAttribute of(Utf8Entry signature) {
        return new shim.classfile.SignatureAttr(signature);
    }
}
