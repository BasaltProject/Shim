/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.lang.classfile;

import java.lang.constant.ClassDesc;
import java.util.List;
import java.util.Optional;
import shim.classfile.SignatureFactories;
import shim.classfile.SignatureParser;
import shim.classfile.SignaturesImpl;

public interface Signature {
    public static Signature of(ClassDesc classDesc) {
        if (classDesc == null) {
            throw new NullPointerException();
        }
        if (classDesc.isArray()) {
            int depth = 0;
            ClassDesc c = classDesc;
            while (c.isArray()) {
                c = c.componentType();
                depth++;
            }
            return ArrayTypeSig.of(depth, of(c));
        }
        if (classDesc.isPrimitive()) {
            return BaseTypeSig.of(classDesc);
        }
        return ClassTypeSig.of(classDesc);
    }
    String signatureString();
    public static Signature parseFrom(String text) { return SignatureParser.parseType(text); }

    public interface ArrayTypeSig extends Signature.RefTypeSig {
        public static Signature.ArrayTypeSig of(Signature signature) { return of(1, signature); }
        public static Signature.ArrayTypeSig of(int i, Signature signature) {
            return SignaturesImpl.ArrayTypeSigImpl.of(i, signature);
        }
        Signature componentSignature();
    }

    public interface BaseTypeSig extends Signature {
        public static Signature.BaseTypeSig of(ClassDesc classDesc) {
            if (classDesc == null) {
                throw new NullPointerException();
            }
            if (!classDesc.isPrimitive()) {
                throw new IllegalArgumentException("primitive class type required");
            }
            return new SignaturesImpl.BaseTypeSigImpl(classDesc.descriptorString().charAt(0));
        }
        public static Signature.BaseTypeSig of(char c) {
            if ("BCDFIJSZV".indexOf(c) < 0) {
                throw new IllegalArgumentException("invalid base type signature");
            }
            return new SignaturesImpl.BaseTypeSigImpl(c);
        }
        char baseType();
    }

    public interface TypeArg {
        public static Signature.TypeArg.Bounded of(Signature.RefTypeSig refTypeSig) {
            return bounded(Bounded.WildcardIndicator.NONE, refTypeSig);
        }
        public static Signature.TypeArg.Bounded bounded(Signature.TypeArg.Bounded.WildcardIndicator wildcardIndicator,
                                                       Signature.RefTypeSig refTypeSig) {
            return new SignaturesImpl.TypeArgImpl(wildcardIndicator, refTypeSig);
        }
        public static Signature.TypeArg.Unbounded unbounded() {
            return SignaturesImpl.UnboundedTypeArgImpl.INSTANCE;
        }
        public static Signature.TypeArg.Bounded extendsOf(Signature.RefTypeSig refTypeSig) {
            return bounded(Bounded.WildcardIndicator.EXTENDS, refTypeSig);
        }
        public static Signature.TypeArg.Bounded superOf(Signature.RefTypeSig refTypeSig) {
            return bounded(Bounded.WildcardIndicator.SUPER, refTypeSig);
        }

        public interface Bounded extends Signature.TypeArg {
            Signature.RefTypeSig boundType();
            Signature.TypeArg.Bounded.WildcardIndicator wildcardIndicator();

            public enum WildcardIndicator {
                NONE,
                EXTENDS,
                SUPER;
            }
        }

        public interface Unbounded extends Signature.TypeArg {
        }
    }

    public interface ClassTypeSig extends Signature.RefTypeSig, Signature.ThrowableSig {
        String className();
        public static Signature.ClassTypeSig of(Signature.ClassTypeSig classTypeSig, String text,
                                                Signature.TypeArg... typeArgs) {
            if (text == null) {
                throw new NullPointerException();
            }
            String name = text.replace('.', '/');
            return new SignaturesImpl.ClassTypeSigImpl(Optional.ofNullable(classTypeSig), name,
                SignatureFactories.typeArgs(typeArgs));
        }
        public static Signature.ClassTypeSig of(Signature.ClassTypeSig classTypeSig, ClassDesc classDesc,
                                                Signature.TypeArg... typeArgs) {
            return of(classTypeSig, SignatureFactories.internalName(classDesc), typeArgs);
        }
        public static Signature.ClassTypeSig of(String text, Signature.TypeArg... typeArgs) {
            return of((Signature.ClassTypeSig) null, text, typeArgs);
        }
        public static Signature.ClassTypeSig of(ClassDesc classDesc, Signature.TypeArg... typeArgs) {
            return of((Signature.ClassTypeSig) null, classDesc, typeArgs);
        }
        default ClassDesc classDesc() {
            Optional<Signature.ClassTypeSig> outer = outerType();
            return outer.isPresent() ? outer.get().classDesc().nested(className())
                : ClassDesc.ofInternalName(className());
        }
        List<Signature.TypeArg> typeArgs();
        Optional<Signature.ClassTypeSig> outerType();
    }

    public interface ThrowableSig extends Signature {
    }

    public interface TypeParam {
        public static Signature.TypeParam of(String text, Optional<Signature.RefTypeSig> optional,
                                             Signature.RefTypeSig... refTypeSigs) {
            return SignatureFactories.typeParam(text, optional, refTypeSigs);
        }
        public static Signature.TypeParam of(String text, Signature.RefTypeSig refTypeSig,
                                             Signature.RefTypeSig... refTypeSigs) {
            return SignatureFactories.typeParam(text, Optional.ofNullable(refTypeSig), refTypeSigs);
        }
        String identifier();
        List<Signature.RefTypeSig> interfaceBounds();
        Optional<Signature.RefTypeSig> classBound();
    }

    public interface TypeVarSig extends Signature.RefTypeSig, Signature.ThrowableSig {
        public static Signature.TypeVarSig of(String text) {
            if (text == null) {
                throw new NullPointerException();
            }
            return new SignaturesImpl.TypeVarSigImpl(text);
        }
        String identifier();
    }

    public interface RefTypeSig extends Signature {
    }
}
