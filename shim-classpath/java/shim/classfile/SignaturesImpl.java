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
import java.lang.classfile.Signature.TypeArg;
import java.util.List;
import java.util.Optional;

/** Implementations of generic signatures (JVMS 4.7.9.1). */
public final class SignaturesImpl {
    private SignaturesImpl() { }

    private static boolean same(Object a, Object b) {
        return a == null ? b == null : a.equals(b);
    }

    public static final class BaseTypeSigImpl implements Signature.BaseTypeSig {
        private final char baseType;

        public BaseTypeSigImpl(char baseType) {
            this.baseType = baseType;
        }

        @Override public char baseType() { return baseType; }
        @Override public String signatureString() { return String.valueOf(baseType); }
        @Override
        public boolean equals(Object o) {
            return o instanceof BaseTypeSigImpl && ((BaseTypeSigImpl) o).baseType == baseType;
        }

        @Override public int hashCode() { return baseType; }
        @Override public String toString() { return "BaseTypeSigImpl[baseType=" + baseType + "]"; }
    }

    public static final class TypeVarSigImpl implements Signature.TypeVarSig {
        private final String identifier;

        public TypeVarSigImpl(String identifier) {
            this.identifier = identifier;
        }

        @Override public String identifier() { return identifier; }
        @Override public String signatureString() { return "T" + identifier + ";"; }

        @Override
        public boolean equals(Object o) {
            return o instanceof TypeVarSigImpl && ((TypeVarSigImpl) o).identifier.equals(identifier);
        }

        @Override public int hashCode() { return identifier.hashCode(); }
        @Override public String toString() { return "TypeVarSigImpl[identifier=" + identifier + "]"; }
    }

    public static final class ArrayTypeSigImpl implements Signature.ArrayTypeSig {
        private final int arrayDepth;
        private final Signature elemType;

        public ArrayTypeSigImpl(int arrayDepth, Signature elemType) {
            this.arrayDepth = arrayDepth;
            this.elemType = elemType;
        }

        public static Signature.ArrayTypeSig of(int depth, Signature component) {
            TempEntries.nonNull(component);
            if (component instanceof ArrayTypeSigImpl) {
                ArrayTypeSigImpl a = (ArrayTypeSigImpl) component;
                return create(depth, a.arrayDepth, a.elemType);
            }
            return create(depth, 0, component);
        }

        private static Signature.ArrayTypeSig create(int depth, int more, Signature elem) {
            if (depth <= 0 || depth + more > 255) {
                throw new IllegalArgumentException("illegal array depth value");
            }
            return new ArrayTypeSigImpl(depth + more, elem);
        }

        @Override
        public Signature componentSignature() {
            return arrayDepth > 1 ? new ArrayTypeSigImpl(arrayDepth - 1, elemType) : elemType;
        }

        @Override
        public String signatureString() {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < arrayDepth; i++) {
                sb.append('[');
            }
            return sb.append(elemType.signatureString()).toString();
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof ArrayTypeSigImpl && ((ArrayTypeSigImpl) o).arrayDepth == arrayDepth
                && ((ArrayTypeSigImpl) o).elemType.equals(elemType);
        }

        @Override public int hashCode() { return arrayDepth * 31 + elemType.hashCode(); }
        @Override
        public String toString() {
            return "ArrayTypeSigImpl[arrayDepth=" + arrayDepth + ", elemType=" + elemType + "]";
        }
    }

    public static final class ClassTypeSigImpl implements Signature.ClassTypeSig {
        private final Optional<Signature.ClassTypeSig> outerType;
        private final String className;
        private final List<TypeArg> typeArgs;

        public ClassTypeSigImpl(Optional<Signature.ClassTypeSig> outerType, String className, List<TypeArg> typeArgs) {
            this.outerType = outerType;
            this.className = className;
            this.typeArgs = typeArgs;
        }

        @Override public Optional<Signature.ClassTypeSig> outerType() { return outerType; }
        @Override public String className() { return className; }
        @Override public List<TypeArg> typeArgs() { return typeArgs; }

        @Override
        public String signatureString() {
            StringBuilder sb = new StringBuilder();
            if (outerType.isPresent()) {
                String o = outerType.get().signatureString();
                sb.append(o, 0, o.length() - 1).append('.');
            } else {
                sb.append('L');
            }
            sb.append(className);
            if (!typeArgs.isEmpty()) {
                sb.append('<');
                for (TypeArg a : typeArgs) {
                    sb.append(typeArgString(a));
                }
                sb.append('>');
            }
            return sb.append(';').toString();
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof ClassTypeSigImpl)) {
                return false;
            }
            ClassTypeSigImpl c = (ClassTypeSigImpl) o;
            return outerType.equals(c.outerType) && className.equals(c.className) && typeArgs.equals(c.typeArgs);
        }

        @Override public int hashCode() { return TempEntries.hash(outerType, className, typeArgs); }

        @Override
        public String toString() {
            return "ClassTypeSigImpl[outerType=" + outerType + ", className=" + className + ", typeArgs=" + typeArgs
                + "]";
        }
    }

    static String typeArgString(TypeArg a) {
        if (a instanceof TypeArg.Unbounded) {
            return "*";
        }
        TypeArg.Bounded b = (TypeArg.Bounded) a;
        String s = b.boundType().signatureString();
        switch (b.wildcardIndicator()) {
            case EXTENDS: return "+" + s;
            case SUPER: return "-" + s;
            default: return s;
        }
    }

    public enum UnboundedTypeArgImpl implements TypeArg.Unbounded {
        INSTANCE
    }

    public static final class TypeArgImpl implements TypeArg.Bounded {
        private final WildcardIndicator wildcardIndicator;
        private final Signature.RefTypeSig boundType;

        public TypeArgImpl(WildcardIndicator wildcardIndicator, Signature.RefTypeSig boundType) {
            this.wildcardIndicator = TempEntries.nonNull(wildcardIndicator);
            this.boundType = TempEntries.nonNull(boundType);
        }

        @Override public WildcardIndicator wildcardIndicator() { return wildcardIndicator; }
        @Override public Signature.RefTypeSig boundType() { return boundType; }

        @Override
        public boolean equals(Object o) {
            return o instanceof TypeArgImpl && ((TypeArgImpl) o).wildcardIndicator == wildcardIndicator
                && ((TypeArgImpl) o).boundType.equals(boundType);
        }

        @Override public int hashCode() { return TempEntries.hash(wildcardIndicator, boundType); }

        @Override
        public String toString() {
            return "TypeArgImpl[wildcardIndicator=" + wildcardIndicator + ", boundType=" + boundType + "]";
        }
    }

    public static final class TypeParamImpl implements Signature.TypeParam {
        private final String identifier;
        private final Optional<Signature.RefTypeSig> classBound;
        private final List<Signature.RefTypeSig> interfaceBounds;

        public TypeParamImpl(String identifier, Optional<Signature.RefTypeSig> classBound,
                             List<Signature.RefTypeSig> interfaceBounds) {
            this.identifier = identifier;
            this.classBound = classBound;
            this.interfaceBounds = interfaceBounds;
        }

        @Override public String identifier() { return identifier; }
        @Override public Optional<Signature.RefTypeSig> classBound() { return classBound; }
        @Override public List<Signature.RefTypeSig> interfaceBounds() { return interfaceBounds; }

        String signatureString() {
            StringBuilder sb = new StringBuilder(identifier).append(':');
            if (classBound.isPresent()) {
                sb.append(classBound.get().signatureString());
            }
            for (Signature.RefTypeSig s : interfaceBounds) {
                sb.append(':').append(s.signatureString());
            }
            return sb.toString();
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof TypeParamImpl)) {
                return false;
            }
            TypeParamImpl t = (TypeParamImpl) o;
            return identifier.equals(t.identifier) && classBound.equals(t.classBound)
                && interfaceBounds.equals(t.interfaceBounds);
        }

        @Override public int hashCode() { return TempEntries.hash(identifier, classBound, interfaceBounds); }

        @Override
        public String toString() {
            return "TypeParamImpl[identifier=" + identifier + ", classBound=" + classBound + ", interfaceBounds="
                + interfaceBounds + "]";
        }
    }

    static String typeParamsString(List<Signature.TypeParam> params) {
        if (params.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder("<");
        for (Signature.TypeParam p : params) {
            if (p instanceof TypeParamImpl) {
                sb.append(((TypeParamImpl) p).signatureString());
            } else {
                sb.append(new TypeParamImpl(p.identifier(), p.classBound(), p.interfaceBounds()).signatureString());
            }
        }
        return sb.append('>').toString();
    }

    public static final class ClassSignatureImpl implements ClassSignature {
        private final List<Signature.TypeParam> typeParameters;
        private final Signature.ClassTypeSig superclassSignature;
        private final List<Signature.ClassTypeSig> superinterfaceSignatures;

        public ClassSignatureImpl(List<Signature.TypeParam> typeParameters, Signature.ClassTypeSig superclassSignature,
                                  List<Signature.ClassTypeSig> superinterfaceSignatures) {
            this.typeParameters = typeParameters;
            this.superclassSignature = superclassSignature;
            this.superinterfaceSignatures = superinterfaceSignatures;
        }

        @Override public List<Signature.TypeParam> typeParameters() { return typeParameters; }
        @Override public Signature.ClassTypeSig superclassSignature() { return superclassSignature; }
        @Override public List<Signature.ClassTypeSig> superinterfaceSignatures() { return superinterfaceSignatures; }

        @Override
        public String signatureString() {
            StringBuilder sb = new StringBuilder(typeParamsString(typeParameters));
            sb.append(superclassSignature.signatureString());
            for (Signature.ClassTypeSig s : superinterfaceSignatures) {
                sb.append(s.signatureString());
            }
            return sb.toString();
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof ClassSignatureImpl)) {
                return false;
            }
            ClassSignatureImpl c = (ClassSignatureImpl) o;
            return typeParameters.equals(c.typeParameters) && same(superclassSignature, c.superclassSignature)
                && superinterfaceSignatures.equals(c.superinterfaceSignatures);
        }

        @Override
        public int hashCode() {
            return TempEntries.hash(typeParameters, superclassSignature, superinterfaceSignatures);
        }

        @Override
        public String toString() {
            return "ClassSignatureImpl[typeParameters=" + typeParameters + ", superclassSignature="
                + superclassSignature + ", superinterfaceSignatures=" + superinterfaceSignatures + "]";
        }
    }

    public static final class MethodSignatureImpl implements MethodSignature {
        private final List<Signature.TypeParam> typeParameters;
        private final List<Signature.ThrowableSig> throwableSignatures;
        private final Signature result;
        private final List<Signature> arguments;

        public MethodSignatureImpl(List<Signature.TypeParam> typeParameters,
                                   List<Signature.ThrowableSig> throwableSignatures, Signature result,
                                   List<Signature> arguments) {
            this.typeParameters = typeParameters;
            this.throwableSignatures = throwableSignatures;
            this.result = result;
            this.arguments = arguments;
        }

        @Override public List<Signature.TypeParam> typeParameters() { return typeParameters; }
        @Override public List<Signature.ThrowableSig> throwableSignatures() { return throwableSignatures; }
        @Override public Signature result() { return result; }
        @Override public List<Signature> arguments() { return arguments; }

        @Override
        public String signatureString() {
            StringBuilder sb = new StringBuilder(typeParamsString(typeParameters)).append('(');
            for (Signature s : arguments) {
                sb.append(s.signatureString());
            }
            sb.append(')').append(result.signatureString());
            for (Signature.ThrowableSig s : throwableSignatures) {
                sb.append('^').append(s.signatureString());
            }
            return sb.toString();
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof MethodSignatureImpl)) {
                return false;
            }
            MethodSignatureImpl m = (MethodSignatureImpl) o;
            return typeParameters.equals(m.typeParameters) && throwableSignatures.equals(m.throwableSignatures)
                && result.equals(m.result) && arguments.equals(m.arguments);
        }

        @Override
        public int hashCode() {
            return TempEntries.hash(typeParameters, throwableSignatures, result, arguments);
        }

        @Override
        public String toString() {
            return "MethodSignatureImpl[typeParameters=" + typeParameters + ", throwableSignatures="
                + throwableSignatures + ", result=" + result + ", arguments=" + arguments + "]";
        }
    }
}
