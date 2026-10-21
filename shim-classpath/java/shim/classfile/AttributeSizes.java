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
import java.lang.classfile.AnnotationElement;
import java.lang.classfile.AnnotationValue;
import java.lang.classfile.Attribute;
import java.lang.classfile.TypeAnnotation;
import java.lang.classfile.attribute.AnnotationDefaultAttribute;
import java.lang.classfile.attribute.BootstrapMethodsAttribute;
import java.lang.classfile.attribute.CharacterRangeTableAttribute;
import java.lang.classfile.attribute.CompilationIDAttribute;
import java.lang.classfile.attribute.ConstantValueAttribute;
import java.lang.classfile.attribute.DeprecatedAttribute;
import java.lang.classfile.attribute.EnclosingMethodAttribute;
import java.lang.classfile.attribute.ExceptionsAttribute;
import java.lang.classfile.attribute.InnerClassesAttribute;
import java.lang.classfile.attribute.LineNumberTableAttribute;
import java.lang.classfile.attribute.LocalVariableTableAttribute;
import java.lang.classfile.attribute.LocalVariableTypeTableAttribute;
import java.lang.classfile.attribute.MethodParametersAttribute;
import java.lang.classfile.attribute.ModuleAttribute;
import java.lang.classfile.attribute.ModuleExportInfo;
import java.lang.classfile.attribute.ModuleHashInfo;
import java.lang.classfile.attribute.ModuleHashesAttribute;
import java.lang.classfile.attribute.ModuleMainClassAttribute;
import java.lang.classfile.attribute.ModuleOpenInfo;
import java.lang.classfile.attribute.ModulePackagesAttribute;
import java.lang.classfile.attribute.ModuleProvideInfo;
import java.lang.classfile.attribute.ModuleResolutionAttribute;
import java.lang.classfile.attribute.ModuleTargetAttribute;
import java.lang.classfile.attribute.NestHostAttribute;
import java.lang.classfile.attribute.NestMembersAttribute;
import java.lang.classfile.attribute.PermittedSubclassesAttribute;
import java.lang.classfile.attribute.RecordAttribute;
import java.lang.classfile.attribute.RecordComponentInfo;
import java.lang.classfile.attribute.RuntimeInvisibleAnnotationsAttribute;
import java.lang.classfile.attribute.RuntimeInvisibleParameterAnnotationsAttribute;
import java.lang.classfile.attribute.RuntimeInvisibleTypeAnnotationsAttribute;
import java.lang.classfile.attribute.RuntimeVisibleAnnotationsAttribute;
import java.lang.classfile.attribute.RuntimeVisibleParameterAnnotationsAttribute;
import java.lang.classfile.attribute.RuntimeVisibleTypeAnnotationsAttribute;
import java.lang.classfile.attribute.SignatureAttribute;
import java.lang.classfile.attribute.SourceFileAttribute;
import java.lang.classfile.attribute.SourceIDAttribute;
import java.lang.classfile.attribute.StackMapFrameInfo;
import java.lang.classfile.attribute.StackMapTableAttribute;
import java.lang.classfile.attribute.SyntheticAttribute;
import java.lang.classfile.attribute.UnknownAttribute;
import java.util.List;

/** The length each standard attribute must declare, computed from its parsed contents. */
final class AttributeSizes {
    private AttributeSizes() { }

    /** The expected body length of {@code a}, or -1 when the attribute is not checked. */
    static int of(Attribute<?> a) {
        if (a instanceof SourceFileAttribute) {
            ((SourceFileAttribute) a).sourceFile();
            return 2;
        }
        if (a instanceof SignatureAttribute) {
            ((SignatureAttribute) a).signature();
            return 2;
        }
        if (a instanceof DeprecatedAttribute || a instanceof SyntheticAttribute) {
            return 0;
        }
        if (a instanceof ConstantValueAttribute) {
            ((ConstantValueAttribute) a).constant();
            return 2;
        }
        if (a instanceof InnerClassesAttribute) {
            return 2 + 8 * ((InnerClassesAttribute) a).classes().size();
        }
        if (a instanceof EnclosingMethodAttribute) {
            ((EnclosingMethodAttribute) a).enclosingClass();
            ((EnclosingMethodAttribute) a).enclosingMethod();
            return 4;
        }
        if (a instanceof NestHostAttribute) {
            ((NestHostAttribute) a).nestHost();
            return 2;
        }
        if (a instanceof NestMembersAttribute) {
            return 2 + 2 * ((NestMembersAttribute) a).nestMembers().size();
        }
        if (a instanceof PermittedSubclassesAttribute) {
            return 2 + 2 * ((PermittedSubclassesAttribute) a).permittedSubclasses().size();
        }
        if (a instanceof ModulePackagesAttribute) {
            return 2 + 2 * ((ModulePackagesAttribute) a).packages().size();
        }
        if (a instanceof ExceptionsAttribute) {
            return 2 + 2 * ((ExceptionsAttribute) a).exceptions().size();
        }
        if (a instanceof ModuleMainClassAttribute) {
            ((ModuleMainClassAttribute) a).mainClass();
            return 2;
        }
        if (a instanceof ModuleTargetAttribute) {
            ((ModuleTargetAttribute) a).targetPlatform();
            return 2;
        }
        if (a instanceof ModuleResolutionAttribute) {
            return 2;
        }
        if (a instanceof CompilationIDAttribute) {
            ((CompilationIDAttribute) a).compilationId();
            return 2;
        }
        if (a instanceof SourceIDAttribute) {
            ((SourceIDAttribute) a).sourceId();
            return 2;
        }
        if (a instanceof ModuleHashesAttribute) {
            ModuleHashesAttribute h = (ModuleHashesAttribute) a;
            h.algorithm();
            int n = 4;
            for (ModuleHashInfo i : h.hashes()) {
                n += 4 + i.hash().length;
            }
            return n;
        }
        if (a instanceof MethodParametersAttribute) {
            return 1 + 4 * ((MethodParametersAttribute) a).parameters().size();
        }
        if (a instanceof BootstrapMethodsAttribute) {
            int n = 2;
            for (java.lang.classfile.BootstrapMethodEntry b : ((BootstrapMethodsAttribute) a).bootstrapMethods()) {
                n += 4 + 2 * b.arguments().size();
            }
            return n;
        }
        if (a instanceof RecordAttribute) {
            int n = 2;
            for (RecordComponentInfo c : ((RecordAttribute) a).components()) {
                n += 6;
                for (Attribute<?> nested : c.attributes()) {
                    n += 6 + nestedSize(nested);
                }
            }
            return n;
        }
        if (a instanceof ModuleAttribute) {
            return moduleSize((ModuleAttribute) a);
        }
        if (a instanceof RuntimeVisibleAnnotationsAttribute) {
            return annotationsSize(((RuntimeVisibleAnnotationsAttribute) a).annotations());
        }
        if (a instanceof RuntimeInvisibleAnnotationsAttribute) {
            return annotationsSize(((RuntimeInvisibleAnnotationsAttribute) a).annotations());
        }
        if (a instanceof RuntimeVisibleParameterAnnotationsAttribute) {
            return parameterAnnotationsSize(((RuntimeVisibleParameterAnnotationsAttribute) a).parameterAnnotations());
        }
        if (a instanceof RuntimeInvisibleParameterAnnotationsAttribute) {
            return parameterAnnotationsSize(
                ((RuntimeInvisibleParameterAnnotationsAttribute) a).parameterAnnotations());
        }
        if (a instanceof RuntimeVisibleTypeAnnotationsAttribute) {
            return typeAnnotationsSize(((RuntimeVisibleTypeAnnotationsAttribute) a).annotations());
        }
        if (a instanceof RuntimeInvisibleTypeAnnotationsAttribute) {
            return typeAnnotationsSize(((RuntimeInvisibleTypeAnnotationsAttribute) a).annotations());
        }
        if (a instanceof AnnotationDefaultAttribute) {
            return valueSize(((AnnotationDefaultAttribute) a).defaultValue());
        }
        if (a instanceof LineNumberTableAttribute) {
            return 2 + 4 * ((LineNumberTableAttribute) a).lineNumbers().size();
        }
        if (a instanceof LocalVariableTableAttribute) {
            return 2 + 10 * ((LocalVariableTableAttribute) a).localVariables().size();
        }
        if (a instanceof LocalVariableTypeTableAttribute) {
            return 2 + 10 * ((LocalVariableTypeTableAttribute) a).localVariableTypes().size();
        }
        if (a instanceof CharacterRangeTableAttribute) {
            return 2 + 14 * ((CharacterRangeTableAttribute) a).characterRangeTable().size();
        }
        if (a instanceof StackMapTableAttribute) {
            return stackMapSize(((StackMapTableAttribute) a).entries());
        }
        return -1;
    }

    private static int nestedSize(Attribute<?> a) {
        if (a instanceof UnknownAttribute) {
            return ((UnknownAttribute) a).contents().length;
        }
        int n = of(a);
        return n < 0 ? 0 : n;
    }

    private static int moduleSize(ModuleAttribute m) {
        m.moduleName();
        m.moduleVersion();
        int n = 16 + 6 * m.requires().size() + 2 * m.uses().size();
        for (ModuleExportInfo e : m.exports()) {
            n += 6 + 2 * e.exportsTo().size();
        }
        for (ModuleOpenInfo o : m.opens()) {
            n += 6 + 2 * o.opensTo().size();
        }
        for (ModuleProvideInfo p : m.provides()) {
            n += 4 + 2 * p.providesWith().size();
        }
        return n;
    }

    private static int annotationsSize(List<Annotation> annotations) {
        int n = 2;
        for (Annotation a : annotations) {
            n += annotationSize(a);
        }
        return n;
    }

    private static int parameterAnnotationsSize(List<List<Annotation>> parameters) {
        int n = 1;
        for (List<Annotation> l : parameters) {
            n += annotationsSize(l);
        }
        return n;
    }

    private static int annotationSize(Annotation a) {
        int n = 4;
        for (AnnotationElement e : a.elements()) {
            n += 2 + valueSize(e.value());
        }
        return n;
    }

    private static int valueSize(AnnotationValue v) {
        if (v instanceof AnnotationValue.OfEnum) {
            return 5;
        }
        if (v instanceof AnnotationValue.OfAnnotation) {
            return 1 + annotationSize(((AnnotationValue.OfAnnotation) v).annotation());
        }
        if (v instanceof AnnotationValue.OfArray) {
            int n = 3;
            for (AnnotationValue e : ((AnnotationValue.OfArray) v).values()) {
                n += valueSize(e);
            }
            return n;
        }
        return 3;
    }

    private static int typeAnnotationsSize(List<TypeAnnotation> annotations) {
        int n = 2;
        for (TypeAnnotation t : annotations) {
            n += 1 + targetInfoSize(t.targetInfo()) + 1 + 2 * t.targetPath().size() + annotationSize(t.annotation());
        }
        return n;
    }

    private static int targetInfoSize(TypeAnnotation.TargetInfo t) {
        if (t instanceof TypeAnnotation.TypeParameterTarget || t instanceof TypeAnnotation.FormalParameterTarget) {
            return 1;
        }
        if (t instanceof TypeAnnotation.TypeParameterBoundTarget) {
            return 2;
        }
        if (t instanceof TypeAnnotation.EmptyTarget) {
            return 0;
        }
        if (t instanceof TypeAnnotation.LocalVarTarget) {
            return 2 + 6 * ((TypeAnnotation.LocalVarTarget) t).table().size();
        }
        if (t instanceof TypeAnnotation.TypeArgumentTarget) {
            return 3;
        }
        return 2;
    }

    private static int stackMapSize(List<StackMapFrameInfo> frames) {
        int n = 2;
        for (StackMapFrameInfo f : frames) {
            int type = f.frameType();
            if (type < 64) {
                n += 1;
            } else if (type < 128) {
                n += 1 + typesSize(f.stack(), 0);
            } else if (type == 247) {
                n += 3 + typesSize(f.stack(), 0);
            } else if (type < 252) {
                n += 3;
            } else if (type < 255) {
                n += 3 + typesSize(f.locals(), f.locals().size() - (type - 251));
            } else {
                n += 7 + typesSize(f.locals(), 0) + typesSize(f.stack(), 0);
            }
        }
        return n;
    }

    private static int typesSize(List<StackMapFrameInfo.VerificationTypeInfo> types, int from) {
        int n = 0;
        for (int i = Math.max(from, 0); i < types.size(); i++) {
            StackMapFrameInfo.VerificationTypeInfo v = types.get(i);
            n += v instanceof StackMapFrameInfo.ObjectVerificationTypeInfo
                || v instanceof StackMapFrameInfo.UninitializedVerificationTypeInfo ? 3 : 1;
        }
        return n;
    }
}
