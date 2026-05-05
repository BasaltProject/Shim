/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.classfile;

import java.lang.classfile.Label;
import java.lang.classfile.TypeAnnotation;
import java.lang.classfile.TypeAnnotation.TargetType;
import java.util.List;

/** Implementations of the type annotation targets and type path components. */
public final class TargetInfos {
    private TargetInfos() { }

    private static TargetType check(TargetType type, int first, int last) {
        if (type == null) {
            throw new NullPointerException("Cannot invoke \"" + TargetType.class.getName()
                + ".targetTypeValue()\" because \"targetType\" is null");
        }
        int v = type.targetTypeValue();
        if (v < first || v > last) {
            throw new IllegalArgumentException("Wrong target type specified " + type);
        }
        return type;
    }

    public static TypeAnnotation.EmptyTarget empty(TargetType type) {
        return new EmptyTargetImpl(check(type, TypeAnnotation.TargetInfo.TARGET_FIELD,
            TypeAnnotation.TargetInfo.TARGET_METHOD_RECEIVER));
    }

    public static TypeAnnotation.TypeParameterTarget typeParameter(TargetType type, int index) {
        return new TypeParameterTargetImpl(check(type, TypeAnnotation.TargetInfo.TARGET_CLASS_TYPE_PARAMETER,
            TypeAnnotation.TargetInfo.TARGET_METHOD_TYPE_PARAMETER), index);
    }

    public static TypeAnnotation.TypeParameterBoundTarget typeParameterBound(TargetType type, int index, int bound) {
        return new TypeParameterBoundTargetImpl(check(type, TypeAnnotation.TargetInfo.TARGET_CLASS_TYPE_PARAMETER_BOUND,
            TypeAnnotation.TargetInfo.TARGET_METHOD_TYPE_PARAMETER_BOUND), index, bound);
    }

    public static TypeAnnotation.LocalVarTarget variable(TargetType type, List<TypeAnnotation.LocalVarTargetInfo> t) {
        check(type, TypeAnnotation.TargetInfo.TARGET_LOCAL_VARIABLE,
            TypeAnnotation.TargetInfo.TARGET_RESOURCE_VARIABLE);
        return new LocalVarTargetImpl(type, TempEntries.copyOf(t));
    }

    public static TypeAnnotation.OffsetTarget offset(TargetType type, Label label) {
        check(type, TypeAnnotation.TargetInfo.TARGET_INSTANCEOF, TypeAnnotation.TargetInfo.TARGET_METHOD_REFERENCE);
        return new OffsetTargetImpl(type, TempEntries.nonNull(label));
    }

    public static TypeAnnotation.TypeArgumentTarget typeArgument(TargetType type, Label label, int index) {
        check(type, TypeAnnotation.TargetInfo.TARGET_CAST,
            TypeAnnotation.TargetInfo.TARGET_METHOD_REFERENCE_TYPE_ARGUMENT);
        return new TypeArgumentTargetImpl(type, TempEntries.nonNull(label), index);
    }

    public static TypeAnnotation.SupertypeTarget supertype(int index) { return new SupertypeTargetImpl(index); }
    public static TypeAnnotation.FormalParameterTarget formalParameter(int index) {
        return new FormalParameterTargetImpl(index);
    }

    public static TypeAnnotation.ThrowsTarget throwsTarget(int index) { return new ThrowsTargetImpl(index); }
    public static TypeAnnotation.CatchTarget catchTarget(int index) { return new CatchTargetImpl(index); }

    public static TypeAnnotation.LocalVarTargetInfo localVarInfo(Label start, Label end, int index) {
        return new LocalVarTargetInfoImpl(TempEntries.nonNull(start), TempEntries.nonNull(end), index);
    }

    public static TypeAnnotation.TypePathComponent pathComponent(TypeAnnotation.TypePathComponent.Kind kind,
                                                                 int index) {
        if (kind == null) {
            throw new NullPointerException("Cannot invoke \"" + TypeAnnotation.TypePathComponent.Kind.class.getName()
                + ".ordinal()\" because \"typePathKind\" is null");
        }
        switch (kind) {
            case ARRAY: return TypeAnnotation.TypePathComponent.ARRAY;
            case INNER_TYPE: return TypeAnnotation.TypePathComponent.INNER_TYPE;
            case WILDCARD: return TypeAnnotation.TypePathComponent.WILDCARD;
            default: return new TypePathComponentImpl(kind, index);
        }
    }

    private static boolean same(Object a, Object b) {
        return a == null ? b == null : a.equals(b);
    }

    abstract static class Base implements TypeAnnotation.TargetInfo {
        abstract Object[] components();
        abstract String[] names();
        abstract String kind();

        @Override
        public final boolean equals(Object o) {
            if (o == null || o.getClass() != getClass()) {
                return false;
            }
            Object[] a = components();
            Object[] b = ((Base) o).components();
            for (int i = 0; i < a.length; i++) {
                if (!same(a[i], b[i])) {
                    return false;
                }
            }
            return true;
        }

        @Override public final int hashCode() { return TempEntries.hash(components()); }

        @Override
        public final String toString() {
            StringBuilder sb = new StringBuilder(kind()).append('[');
            Object[] c = components();
            String[] n = names();
            for (int i = 0; i < c.length; i++) {
                if (i > 0) {
                    sb.append(", ");
                }
                sb.append(n[i]).append('=').append(c[i]);
            }
            return sb.append(']').toString();
        }
    }

    static final class EmptyTargetImpl extends Base implements TypeAnnotation.EmptyTarget {
        private final TargetType targetType;
        EmptyTargetImpl(TargetType t) { targetType = t; }
        @Override public TargetType targetType() { return targetType; }
        @Override Object[] components() { return new Object[] {targetType}; }
        @Override String[] names() { return new String[] {"targetType"}; }
        @Override String kind() { return "EmptyTargetImpl"; }
    }

    static final class TypeParameterTargetImpl extends Base implements TypeAnnotation.TypeParameterTarget {
        private final TargetType targetType;
        private final int index;
        TypeParameterTargetImpl(TargetType t, int i) { targetType = t; index = i; }
        @Override public TargetType targetType() { return targetType; }
        @Override public int typeParameterIndex() { return index; }
        @Override Object[] components() { return new Object[] {targetType, index}; }
        @Override String[] names() { return new String[] {"targetType", "typeParameterIndex"}; }
        @Override String kind() { return "TypeParameterTargetImpl"; }
    }

    static final class SupertypeTargetImpl extends Base implements TypeAnnotation.SupertypeTarget {
        private final int index;
        SupertypeTargetImpl(int i) { index = i; }
        @Override public TargetType targetType() { return TargetType.CLASS_EXTENDS; }
        @Override public int supertypeIndex() { return index; }
        @Override Object[] components() { return new Object[] {index}; }
        @Override String[] names() { return new String[] {"supertypeIndex"}; }
        @Override String kind() { return "SupertypeTargetImpl"; }
    }

    static final class TypeParameterBoundTargetImpl extends Base implements TypeAnnotation.TypeParameterBoundTarget {
        private final TargetType targetType;
        private final int index;
        private final int bound;
        TypeParameterBoundTargetImpl(TargetType t, int i, int b) { targetType = t; index = i; bound = b; }
        @Override public TargetType targetType() { return targetType; }
        @Override public int typeParameterIndex() { return index; }
        @Override public int boundIndex() { return bound; }
        @Override Object[] components() { return new Object[] {targetType, index, bound}; }
        @Override String[] names() { return new String[] {"targetType", "typeParameterIndex", "boundIndex"}; }
        @Override String kind() { return "TypeParameterBoundTargetImpl"; }
    }

    static final class FormalParameterTargetImpl extends Base implements TypeAnnotation.FormalParameterTarget {
        private final int index;
        FormalParameterTargetImpl(int i) { index = i; }
        @Override public TargetType targetType() { return TargetType.METHOD_FORMAL_PARAMETER; }
        @Override public int formalParameterIndex() { return index; }
        @Override Object[] components() { return new Object[] {index}; }
        @Override String[] names() { return new String[] {"formalParameterIndex"}; }
        @Override String kind() { return "FormalParameterTargetImpl"; }
    }

    static final class ThrowsTargetImpl extends Base implements TypeAnnotation.ThrowsTarget {
        private final int index;
        ThrowsTargetImpl(int i) { index = i; }
        @Override public TargetType targetType() { return TargetType.THROWS; }
        @Override public int throwsTargetIndex() { return index; }
        @Override Object[] components() { return new Object[] {index}; }
        @Override String[] names() { return new String[] {"throwsTargetIndex"}; }
        @Override String kind() { return "ThrowsTargetImpl"; }
    }

    static final class CatchTargetImpl extends Base implements TypeAnnotation.CatchTarget {
        private final int index;
        CatchTargetImpl(int i) { index = i; }
        @Override public TargetType targetType() { return TargetType.EXCEPTION_PARAMETER; }
        @Override public int exceptionTableIndex() { return index; }
        @Override Object[] components() { return new Object[] {index}; }
        @Override String[] names() { return new String[] {"exceptionTableIndex"}; }
        @Override String kind() { return "CatchTargetImpl"; }
    }

    static final class LocalVarTargetImpl extends Base implements TypeAnnotation.LocalVarTarget {
        private final TargetType targetType;
        private final List<TypeAnnotation.LocalVarTargetInfo> table;
        LocalVarTargetImpl(TargetType t, List<TypeAnnotation.LocalVarTargetInfo> l) { targetType = t; table = l; }
        @Override public TargetType targetType() { return targetType; }
        @Override public List<TypeAnnotation.LocalVarTargetInfo> table() { return table; }
        @Override public int size() { return 2 + 6 * table.size(); }
        @Override Object[] components() { return new Object[] {targetType, table}; }
        @Override String[] names() { return new String[] {"targetType", "table"}; }
        @Override String kind() { return "LocalVarTargetImpl"; }
    }

    static final class OffsetTargetImpl extends Base implements TypeAnnotation.OffsetTarget {
        private final TargetType targetType;
        private final Label target;
        OffsetTargetImpl(TargetType t, Label l) { targetType = t; target = l; }
        @Override public TargetType targetType() { return targetType; }
        @Override public Label target() { return target; }
        @Override Object[] components() { return new Object[] {targetType, target}; }
        @Override String[] names() { return new String[] {"targetType", "target"}; }
        @Override String kind() { return "OffsetTargetImpl"; }
    }

    static final class TypeArgumentTargetImpl extends Base implements TypeAnnotation.TypeArgumentTarget {
        private final TargetType targetType;
        private final Label target;
        private final int index;
        TypeArgumentTargetImpl(TargetType t, Label l, int i) { targetType = t; target = l; index = i; }
        @Override public TargetType targetType() { return targetType; }
        @Override public Label target() { return target; }
        @Override public int typeArgumentIndex() { return index; }
        @Override Object[] components() { return new Object[] {targetType, target, index}; }
        @Override String[] names() { return new String[] {"targetType", "target", "typeArgumentIndex"}; }
        @Override String kind() { return "TypeArgumentTargetImpl"; }
    }

    static final class LocalVarTargetInfoImpl implements TypeAnnotation.LocalVarTargetInfo {
        private final Label start;
        private final Label end;
        private final int index;

        LocalVarTargetInfoImpl(Label s, Label e, int i) {
            start = s;
            end = e;
            index = i;
        }

        @Override public Label startLabel() { return start; }
        @Override public Label endLabel() { return end; }
        @Override public int index() { return index; }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof LocalVarTargetInfoImpl)) {
                return false;
            }
            LocalVarTargetInfoImpl x = (LocalVarTargetInfoImpl) o;
            return start.equals(x.start) && end.equals(x.end) && index == x.index;
        }

        @Override public int hashCode() { return TempEntries.hash(start, end, index); }

        @Override
        public String toString() {
            return "LocalVarTargetInfoImpl[startLabel=" + start + ", endLabel=" + end + ", index=" + index + "]";
        }
    }

    public static final class TypePathComponentImpl implements TypeAnnotation.TypePathComponent {
        private final Kind kind;
        private final int index;

        public TypePathComponentImpl(Kind kind, int index) {
            this.kind = kind;
            this.index = index;
        }

        @Override public Kind typePathKind() { return kind; }
        @Override public int typeArgumentIndex() { return index; }

        @Override
        public boolean equals(Object o) {
            return o instanceof TypePathComponentImpl && ((TypePathComponentImpl) o).kind == kind
                && ((TypePathComponentImpl) o).index == index;
        }

        @Override public int hashCode() { return TempEntries.hash(kind, index); }

        @Override
        public String toString() {
            return "TypePathComponentImpl[typePathKind=" + kind + ", typeArgumentIndex=" + index + "]";
        }
    }
}
