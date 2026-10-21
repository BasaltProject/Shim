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
import shim.classfile.TargetInfos;
import shim.classfile.UnboundTypeAnnotation;

public interface TypeAnnotation {
    public static TypeAnnotation of(TypeAnnotation.TargetInfo targetInfo, List<TypeAnnotation.TypePathComponent> list,
                                    Annotation annotation) {
            return UnboundTypeAnnotation.of(targetInfo, list, annotation);
        }
    Annotation annotation();
    TypeAnnotation.TargetInfo targetInfo();
    List<TypeAnnotation.TypePathComponent> targetPath();

    public interface TargetInfo {
        public static final int TARGET_CLASS_TYPE_PARAMETER = 0;
        public static final int TARGET_METHOD_TYPE_PARAMETER = 1;
        public static final int TARGET_CLASS_EXTENDS = 16;
        public static final int TARGET_CLASS_TYPE_PARAMETER_BOUND = 17;
        public static final int TARGET_METHOD_TYPE_PARAMETER_BOUND = 18;
        public static final int TARGET_FIELD = 19;
        public static final int TARGET_METHOD_RETURN = 20;
        public static final int TARGET_METHOD_RECEIVER = 21;
        public static final int TARGET_METHOD_FORMAL_PARAMETER = 22;
        public static final int TARGET_THROWS = 23;
        public static final int TARGET_LOCAL_VARIABLE = 64;
        public static final int TARGET_RESOURCE_VARIABLE = 65;
        public static final int TARGET_EXCEPTION_PARAMETER = 66;
        public static final int TARGET_INSTANCEOF = 67;
        public static final int TARGET_NEW = 68;
        public static final int TARGET_CONSTRUCTOR_REFERENCE = 69;
        public static final int TARGET_METHOD_REFERENCE = 70;
        public static final int TARGET_CAST = 71;
        public static final int TARGET_CONSTRUCTOR_INVOCATION_TYPE_ARGUMENT = 72;
        public static final int TARGET_METHOD_INVOCATION_TYPE_ARGUMENT = 73;
        public static final int TARGET_CONSTRUCTOR_REFERENCE_TYPE_ARGUMENT = 74;
        public static final int TARGET_METHOD_REFERENCE_TYPE_ARGUMENT = 75;
        default int size() {
            return targetType().sizeIfFixed();
        }
        public static TypeAnnotation.EmptyTarget of(TypeAnnotation.TargetType targetType) {
            return TargetInfos.empty(targetType);
        }
        public static TypeAnnotation.EmptyTarget ofField() {
            return of(TypeAnnotation.TargetType.FIELD);
        }
        TypeAnnotation.TargetType targetType();
        public static TypeAnnotation.TypeParameterTarget ofClassTypeParameter(int i) {
            return ofTypeParameter(TypeAnnotation.TargetType.CLASS_TYPE_PARAMETER, i);
        }
        public static TypeAnnotation.TypeParameterTarget ofMethodTypeParameter(int i) {
            return ofTypeParameter(TypeAnnotation.TargetType.METHOD_TYPE_PARAMETER, i);
        }
        public static TypeAnnotation.SupertypeTarget ofClassExtends(int i) {
            return TargetInfos.supertype(i);
        }
        public static TypeAnnotation.TypeParameterBoundTarget ofClassTypeParameterBound(int i, int i2) {
            return ofTypeParameterBound(TypeAnnotation.TargetType.CLASS_TYPE_PARAMETER_BOUND, i, i2);
        }
        public static TypeAnnotation.TypeParameterBoundTarget ofMethodTypeParameterBound(int i, int i2) {
            return ofTypeParameterBound(TypeAnnotation.TargetType.METHOD_TYPE_PARAMETER_BOUND, i, i2);
        }
        public static TypeAnnotation.EmptyTarget ofMethodReturn() {
            return of(TypeAnnotation.TargetType.METHOD_RETURN);
        }
        public static TypeAnnotation.EmptyTarget ofMethodReceiver() {
            return of(TypeAnnotation.TargetType.METHOD_RECEIVER);
        }
        public static TypeAnnotation.FormalParameterTarget ofMethodFormalParameter(int i) {
            return TargetInfos.formalParameter(i);
        }
        public static TypeAnnotation.ThrowsTarget ofThrows(int i) {
            return TargetInfos.throwsTarget(i);
        }
        public static TypeAnnotation.LocalVarTarget ofLocalVariable(List<TypeAnnotation.LocalVarTargetInfo> list) {
            return ofVariable(TypeAnnotation.TargetType.LOCAL_VARIABLE, list);
        }
        public static TypeAnnotation.LocalVarTarget ofResourceVariable(List<TypeAnnotation.LocalVarTargetInfo> list) {
            return ofVariable(TypeAnnotation.TargetType.RESOURCE_VARIABLE, list);
        }
        public static TypeAnnotation.CatchTarget ofExceptionParameter(int i) {
            return TargetInfos.catchTarget(i);
        }
        public static TypeAnnotation.OffsetTarget ofInstanceofExpr(Label label) {
            return ofOffset(TypeAnnotation.TargetType.INSTANCEOF, label);
        }
        public static TypeAnnotation.OffsetTarget ofNewExpr(Label label) {
            return ofOffset(TypeAnnotation.TargetType.NEW, label);
        }
        public static TypeAnnotation.OffsetTarget ofConstructorReference(Label label) {
            return ofOffset(TypeAnnotation.TargetType.CONSTRUCTOR_REFERENCE, label);
        }
        public static TypeAnnotation.OffsetTarget ofMethodReference(Label label) {
            return ofOffset(TypeAnnotation.TargetType.METHOD_REFERENCE, label);
        }
        public static TypeAnnotation.TypeArgumentTarget ofCastExpr(Label label, int i) {
            return ofTypeArgument(TypeAnnotation.TargetType.CAST, label, i);
        }
        public static TypeAnnotation.TypeArgumentTarget ofConstructorInvocationTypeArgument(Label label, int i) {
            return ofTypeArgument(TypeAnnotation.TargetType.CONSTRUCTOR_INVOCATION_TYPE_ARGUMENT, label, i);
        }
        public static TypeAnnotation.TypeArgumentTarget ofMethodInvocationTypeArgument(Label label, int i) {
            return ofTypeArgument(TypeAnnotation.TargetType.METHOD_INVOCATION_TYPE_ARGUMENT, label, i);
        }
        public static TypeAnnotation.TypeArgumentTarget ofConstructorReferenceTypeArgument(Label label, int i) {
            return ofTypeArgument(TypeAnnotation.TargetType.CONSTRUCTOR_REFERENCE_TYPE_ARGUMENT, label, i);
        }
        public static TypeAnnotation.TypeArgumentTarget ofMethodReferenceTypeArgument(Label label, int i) {
            return ofTypeArgument(TypeAnnotation.TargetType.METHOD_REFERENCE_TYPE_ARGUMENT, label, i);
        }
        public static TypeAnnotation.OffsetTarget ofOffset(TypeAnnotation.TargetType targetType, Label label) {
            return TargetInfos.offset(targetType, label);
        }
        public static TypeAnnotation.TypeParameterTarget ofTypeParameter(TypeAnnotation.TargetType targetType, int i) {
            return TargetInfos.typeParameter(targetType, i);
        }
        public static TypeAnnotation.TypeParameterBoundTarget ofTypeParameterBound(TypeAnnotation.TargetType targetType,
                                                                                   int i, int i2) {
            return TargetInfos.typeParameterBound(targetType, i, i2);
        }
        public static TypeAnnotation.LocalVarTarget ofVariable(TypeAnnotation.TargetType targetType,
                                                               List<TypeAnnotation.LocalVarTargetInfo> list) {
            return TargetInfos.variable(targetType, list);
        }
        public static TypeAnnotation.TypeArgumentTarget ofTypeArgument(TypeAnnotation.TargetType targetType,
                                                                       Label label, int i) {
            return TargetInfos.typeArgument(targetType, label, i);
        }
    }

    public interface TypePathComponent {
        public static final TypeAnnotation.TypePathComponent ARRAY =
            new TargetInfos.TypePathComponentImpl(Kind.ARRAY, 0);
        public static final TypeAnnotation.TypePathComponent INNER_TYPE =
            new TargetInfos.TypePathComponentImpl(Kind.INNER_TYPE, 0);
        public static final TypeAnnotation.TypePathComponent WILDCARD =
            new TargetInfos.TypePathComponentImpl(Kind.WILDCARD, 0);
        public static TypeAnnotation.TypePathComponent of(TypeAnnotation.TypePathComponent.Kind kind, int i) {
            return TargetInfos.pathComponent(kind, i);
        }
        int typeArgumentIndex();
        TypeAnnotation.TypePathComponent.Kind typePathKind();

        public enum Kind {
            ARRAY(0),
            INNER_TYPE(1),
            WILDCARD(2),
            TYPE_ARGUMENT(3);

            private final int tag;

            Kind(int tag) {
                this.tag = tag;
            }

            public int tag() { return tag; }
        }
    }

    public interface TypeArgumentTarget extends TypeAnnotation.TargetInfo {
        Label target();
        int typeArgumentIndex();
    }

    public interface OffsetTarget extends TypeAnnotation.TargetInfo {
        Label target();
    }

    public interface CatchTarget extends TypeAnnotation.TargetInfo {
        int exceptionTableIndex();
    }

    public interface LocalVarTargetInfo {
        int index();
        public static TypeAnnotation.LocalVarTargetInfo of(Label label, Label label2, int i) {
            return TargetInfos.localVarInfo(label, label2, i);
        }
        Label endLabel();
        Label startLabel();
    }

    public interface LocalVarTarget extends TypeAnnotation.TargetInfo {
        List<TypeAnnotation.LocalVarTargetInfo> table();
    }

    public interface ThrowsTarget extends TypeAnnotation.TargetInfo {
        int throwsTargetIndex();
    }

    public interface FormalParameterTarget extends TypeAnnotation.TargetInfo {
        int formalParameterIndex();
    }

    public interface EmptyTarget extends TypeAnnotation.TargetInfo {
    }

    public interface TypeParameterBoundTarget extends TypeAnnotation.TargetInfo {
        int typeParameterIndex();
        int boundIndex();
    }

    public interface SupertypeTarget extends TypeAnnotation.TargetInfo {
        int supertypeIndex();
    }

    public interface TypeParameterTarget extends TypeAnnotation.TargetInfo {
        int typeParameterIndex();
    }

    public enum TargetType {
        CLASS_TYPE_PARAMETER(0, 1),
        METHOD_TYPE_PARAMETER(1, 1),
        CLASS_EXTENDS(16, 2),
        CLASS_TYPE_PARAMETER_BOUND(17, 2),
        METHOD_TYPE_PARAMETER_BOUND(18, 2),
        FIELD(19, 0),
        METHOD_RETURN(20, 0),
        METHOD_RECEIVER(21, 0),
        METHOD_FORMAL_PARAMETER(22, 1),
        THROWS(23, 2),
        LOCAL_VARIABLE(64, -1),
        RESOURCE_VARIABLE(65, -1),
        EXCEPTION_PARAMETER(66, 2),
        INSTANCEOF(67, 2),
        NEW(68, 2),
        CONSTRUCTOR_REFERENCE(69, 2),
        METHOD_REFERENCE(70, 2),
        CAST(71, 3),
        CONSTRUCTOR_INVOCATION_TYPE_ARGUMENT(72, 3),
        METHOD_INVOCATION_TYPE_ARGUMENT(73, 3),
        CONSTRUCTOR_REFERENCE_TYPE_ARGUMENT(74, 3),
        METHOD_REFERENCE_TYPE_ARGUMENT(75, 3);

        private final int targetTypeValue;
        private final int sizeIfFixed;

        TargetType(int targetTypeValue, int sizeIfFixed) {
            this.targetTypeValue = targetTypeValue;
            this.sizeIfFixed = sizeIfFixed;
        }

        public int sizeIfFixed() { return sizeIfFixed; }
        public int targetTypeValue() { return targetTypeValue; }
    }
}
