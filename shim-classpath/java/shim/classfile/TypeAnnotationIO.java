/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.classfile;

import java.lang.classfile.Annotation;
import java.lang.classfile.Label;
import java.lang.classfile.TypeAnnotation;
import java.lang.classfile.TypeAnnotation.TargetInfo;
import java.lang.classfile.TypeAnnotation.TargetType;
import java.lang.classfile.TypeAnnotation.TypePathComponent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Reads and writes type annotations (JVMS 4.7.20). */
final class TypeAnnotationIO {
    private static final TargetType[] BY_VALUE = new TargetType[256];

    static {
        for (TargetType t : TargetType.values()) {
            BY_VALUE[t.targetTypeValue()] = t;
        }
    }

    private TypeAnnotationIO() { }

    static List<TypeAnnotation> readAll(ClassReaderImpl r, int pos, LabelContext labels) {
        AnnotationIO in = new AnnotationIO(r, pos);
        int n = in.u2();
        List<TypeAnnotation> out = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            out.add(read(in, labels));
        }
        return Collections.unmodifiableList(out);
    }

    private static Label label(AnnotationIO in, LabelContext labels, int value) {
        if (labels == null) {
            throw new IllegalArgumentException("Unexpected targetType '" + value
                + "' in TypeAnnotation outside of Code attribute, pos = " + (in.pos - 1));
        }
        return labels.getLabel(in.u2());
    }

    private static TypeAnnotation read(AnnotationIO in, LabelContext labels) {
        int value = in.u1();
        TargetType type = BY_VALUE[value];
        if (type == null) {
            throw new IllegalArgumentException("Unexpected targetType '" + value + "' in TypeAnnotation, pos = "
                + (in.pos - 1));
        }
        TargetInfo info;
        switch (type) {
            case CLASS_TYPE_PARAMETER:
            case METHOD_TYPE_PARAMETER:
                info = TargetInfos.typeParameter(type, in.u1());
                break;
            case CLASS_EXTENDS:
                info = TargetInfos.supertype(in.u2());
                break;
            case CLASS_TYPE_PARAMETER_BOUND:
            case METHOD_TYPE_PARAMETER_BOUND: {
                int index = in.u1();
                info = TargetInfos.typeParameterBound(type, index, in.u1());
                break;
            }
            case FIELD:
            case METHOD_RETURN:
            case METHOD_RECEIVER:
                info = TargetInfos.empty(type);
                break;
            case METHOD_FORMAL_PARAMETER:
                info = TargetInfos.formalParameter(in.u1());
                break;
            case THROWS:
                info = TargetInfos.throwsTarget(in.u2());
                break;
            case LOCAL_VARIABLE:
            case RESOURCE_VARIABLE: {
                int n = in.u2();
                List<TypeAnnotation.LocalVarTargetInfo> table = new ArrayList<>(n);
                for (int i = 0; i < n; i++) {
                    int start = in.peekU2();
                    Label s = label(in, labels, value);
                    Label e = labels.getLabel(start + in.u2());
                    table.add(TargetInfos.localVarInfo(s, e, in.u2()));
                }
                info = new TargetInfos.LocalVarTargetImpl(type, Collections.unmodifiableList(table));
                break;
            }
            case EXCEPTION_PARAMETER:
                info = TargetInfos.catchTarget(in.u2());
                break;
            case INSTANCEOF:
            case NEW:
            case CONSTRUCTOR_REFERENCE:
            case METHOD_REFERENCE:
                info = TargetInfos.offset(type, label(in, labels, value));
                break;
            default: {
                Label l = label(in, labels, value);
                info = TargetInfos.typeArgument(type, l, in.u1());
                break;
            }
        }
        int n = in.u1();
        List<TypePathComponent> path = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            int kind = in.u1();
            int index = in.u1();
            if (kind > 3) {
                throw new IllegalArgumentException("Unknown type annotation path component kind: " + kind);
            }
            path.add(TargetInfos.pathComponent(TypePathComponent.Kind.values()[kind], index));
        }
        Annotation a = in.annotation();
        return new UnboundTypeAnnotation(info, Collections.unmodifiableList(path), a);
    }

    static void writeAll(BufWriterImpl out, List<TypeAnnotation> annotations) {
        out.writeU2(annotations.size());
        for (TypeAnnotation a : annotations) {
            write(out, a);
        }
    }

    private static int bci(BufWriterImpl out, Label label, TargetType type) {
        LabelContext lc = out.labelContext();
        if (lc == null) {
            throw new IllegalArgumentException("Illegal targetType '" + type
                + "' in TypeAnnotation outside of Code attribute");
        }
        try {
            return lc.labelToBci(label);
        } catch (IllegalArgumentException unbound) {
            return -1;
        }
    }

    private static void write(BufWriterImpl out, TypeAnnotation ta) {
        TargetInfo info = ta.targetInfo();
        TargetType type = info.targetType();
        out.writeU1(type.targetTypeValue());
        switch (type) {
            case CLASS_TYPE_PARAMETER:
            case METHOD_TYPE_PARAMETER:
                out.writeU1(((TypeAnnotation.TypeParameterTarget) info).typeParameterIndex());
                break;
            case CLASS_EXTENDS:
                out.writeU2(((TypeAnnotation.SupertypeTarget) info).supertypeIndex());
                break;
            case CLASS_TYPE_PARAMETER_BOUND:
            case METHOD_TYPE_PARAMETER_BOUND:
                out.writeU1(((TypeAnnotation.TypeParameterBoundTarget) info).typeParameterIndex());
                out.writeU1(((TypeAnnotation.TypeParameterBoundTarget) info).boundIndex());
                break;
            case FIELD:
            case METHOD_RETURN:
            case METHOD_RECEIVER:
                break;
            case METHOD_FORMAL_PARAMETER:
                out.writeU1(((TypeAnnotation.FormalParameterTarget) info).formalParameterIndex());
                break;
            case THROWS:
                out.writeU2(((TypeAnnotation.ThrowsTarget) info).throwsTargetIndex());
                break;
            case LOCAL_VARIABLE:
            case RESOURCE_VARIABLE: {
                List<TypeAnnotation.LocalVarTargetInfo> table = ((TypeAnnotation.LocalVarTarget) info).table();
                out.writeU2(table.size());
                for (TypeAnnotation.LocalVarTargetInfo e : table) {
                    int start = bci(out, e.startLabel(), type);
                    out.writeU2(start);
                    out.writeU2(bci(out, e.endLabel(), type) - start);
                    out.writeU2(e.index());
                }
                break;
            }
            case EXCEPTION_PARAMETER:
                out.writeU2(((TypeAnnotation.CatchTarget) info).exceptionTableIndex());
                break;
            case INSTANCEOF:
            case NEW:
            case CONSTRUCTOR_REFERENCE:
            case METHOD_REFERENCE:
                out.writeU2(bci(out, ((TypeAnnotation.OffsetTarget) info).target(), type));
                break;
            default:
                out.writeU2(bci(out, ((TypeAnnotation.TypeArgumentTarget) info).target(), type));
                out.writeU1(((TypeAnnotation.TypeArgumentTarget) info).typeArgumentIndex());
                break;
        }
        out.writeU1(ta.targetPath().size());
        for (TypePathComponent c : ta.targetPath()) {
            out.writeU1(c.typePathKind().tag());
            out.writeU1(c.typeArgumentIndex());
        }
        AnnotationIO.writeAnnotation(out, ta.annotation());
    }
}
