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
import java.lang.classfile.BootstrapMethodEntry;
import java.lang.classfile.TypeAnnotation;
import java.lang.classfile.attribute.AnnotationDefaultAttribute;
import java.lang.classfile.attribute.BootstrapMethodsAttribute;
import java.lang.classfile.attribute.ConstantValueAttribute;
import java.lang.classfile.attribute.EnclosingMethodAttribute;
import java.lang.classfile.attribute.ExceptionsAttribute;
import java.lang.classfile.attribute.InnerClassInfo;
import java.lang.classfile.attribute.InnerClassesAttribute;
import java.lang.classfile.attribute.MethodParameterInfo;
import java.lang.classfile.attribute.MethodParametersAttribute;
import java.lang.classfile.attribute.ModuleAttribute;
import java.lang.classfile.attribute.ModuleExportInfo;
import java.lang.classfile.attribute.ModuleMainClassAttribute;
import java.lang.classfile.attribute.ModuleOpenInfo;
import java.lang.classfile.attribute.ModulePackagesAttribute;
import java.lang.classfile.attribute.ModuleProvideInfo;
import java.lang.classfile.attribute.ModuleRequireInfo;
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
import java.lang.classfile.constantpool.ClassEntry;
import java.lang.classfile.constantpool.InterfaceMethodRefEntry;
import java.lang.classfile.constantpool.LoadableConstantEntry;
import java.lang.classfile.constantpool.MemberRefEntry;
import java.lang.classfile.constantpool.MethodHandleEntry;
import java.lang.classfile.constantpool.ModuleEntry;
import java.lang.classfile.constantpool.PackageEntry;
import java.lang.classfile.constantpool.Utf8Entry;
import java.lang.constant.DirectMethodHandleDesc;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** The attribute part of the debug tree, shared by classes, members, record components and code. */
final class DebugAttributes {
    private DebugAttributes() { }

    static List<DebugNode> attributes(List<Attribute<?>> attributes) {
        List<DebugNode> nodes = new ArrayList<>();
        for (Attribute<?> attr : attributes) {
            DebugNode n = attribute(attr);
            if (n != null) {
                nodes.add(n);
            }
        }
        return nodes;
    }

    private static List<Object> internalNames(List<ClassEntry> entries) {
        List<Object> names = new ArrayList<>();
        for (ClassEntry ce : entries) {
            names.add(ce.asInternalName());
        }
        return names;
    }

    private static Object orNull(Optional<Utf8Entry> e) {
        return e.isPresent() ? e.get().stringValue() : null;
    }

    private static DebugNode attribute(Attribute<?> attr) {
        if (attr instanceof BootstrapMethodsAttribute) {
            DebugNode list = DebugNode.blockList("bootstrap methods");
            for (BootstrapMethodEntry bm : ((BootstrapMethodsAttribute) attr).bootstrapMethods()) {
                MethodHandleEntry mh = bm.bootstrapMethod();
                MemberRefEntry ref = mh.reference();
                DebugNode n = DebugNode.flowMap("bm");
                n.with("index", bm.bsmIndex());
                n.with("kind", DirectMethodHandleDesc.Kind.valueOf(mh.kind(),
                        ref instanceof InterfaceMethodRefEntry).name());
                n.with("owner", ref.owner().asInternalName());
                n.with("name", ref.nameAndType().name().stringValue());
                List<Object> args = new ArrayList<>();
                for (LoadableConstantEntry a : bm.arguments()) {
                    args.add(a.constantValue());
                }
                n.with(DebugNode.flowList("args", args));
                list.add(n);
            }
            return list;
        }
        if (attr instanceof ConstantValueAttribute) {
            return DebugNode.leaf("constant value", ((ConstantValueAttribute) attr).constant().constantValue());
        }
        if (attr instanceof EnclosingMethodAttribute) {
            EnclosingMethodAttribute ema = (EnclosingMethodAttribute) attr;
            return DebugNode.flowMap("enclosing method")
                    .with("class", ema.enclosingClass().asInternalName())
                    .with("method name", orNull(ema.enclosingMethodName()))
                    .with("method type", orNull(ema.enclosingMethodType()));
        }
        if (attr instanceof ExceptionsAttribute) {
            return DebugNode.flowList("exceptions", internalNames(((ExceptionsAttribute) attr).exceptions()));
        }
        if (attr instanceof InnerClassesAttribute) {
            DebugNode list = DebugNode.blockList("inner classes");
            for (InnerClassInfo ic : ((InnerClassesAttribute) attr).classes()) {
                Optional<ClassEntry> outer = ic.outerClass();
                list.add(DebugNode.flowMap("cls")
                        .with("inner class", ic.innerClass().asInternalName())
                        .with("outer class", outer.isPresent() ? outer.get().asInternalName() : null)
                        .with("inner name", orNull(ic.innerName()))
                        .with(DebugPrinter.flags(ic.flags())));
            }
            return list;
        }
        if (attr instanceof MethodParametersAttribute) {
            for (MethodParameterInfo p : ((MethodParametersAttribute) attr).parameters()) {
                orNull(p.name());
                p.flags();
            }
            return null;
        }
        if (attr instanceof ModuleAttribute) {
            return module((ModuleAttribute) attr);
        }
        if (attr instanceof ModuleMainClassAttribute) {
            return DebugNode.leaf("module main class", ((ModuleMainClassAttribute) attr).mainClass().asInternalName());
        }
        if (attr instanceof ModulePackagesAttribute) {
            List<Object> names = new ArrayList<>();
            for (PackageEntry p : ((ModulePackagesAttribute) attr).packages()) {
                names.add(p.asSymbol().name());
            }
            return DebugNode.flowList("module packages", names);
        }
        if (attr instanceof NestHostAttribute) {
            return DebugNode.leaf("nest host", ((NestHostAttribute) attr).nestHost().asInternalName());
        }
        if (attr instanceof NestMembersAttribute) {
            return DebugNode.flowList("nest members", internalNames(((NestMembersAttribute) attr).nestMembers()));
        }
        if (attr instanceof PermittedSubclassesAttribute) {
            return DebugNode.flowList("permitted subclasses",
                    internalNames(((PermittedSubclassesAttribute) attr).permittedSubclasses()));
        }
        if (attr instanceof RecordAttribute) {
            DebugNode list = DebugNode.blockList("record components");
            for (RecordComponentInfo rc : ((RecordAttribute) attr).components()) {
                DebugNode n = DebugNode.blockMap("record component")
                        .with("name", rc.name().stringValue())
                        .with("type", rc.descriptor().stringValue());
                n.with(DebugPrinter.attributeNames(rc));
                n.withAll(attributes(rc.attributes()));
                list.add(n);
            }
            return list;
        }
        if (attr instanceof AnnotationDefaultAttribute) {
            return valueMap(DebugNode.flowMap("annotation default"),
                    ((AnnotationDefaultAttribute) attr).defaultValue());
        }
        if (attr instanceof RuntimeInvisibleAnnotationsAttribute) {
            return annotations("invisible annotations", ((RuntimeInvisibleAnnotationsAttribute) attr).annotations());
        }
        if (attr instanceof RuntimeVisibleAnnotationsAttribute) {
            return annotations("visible annotations", ((RuntimeVisibleAnnotationsAttribute) attr).annotations());
        }
        if (attr instanceof RuntimeInvisibleParameterAnnotationsAttribute) {
            return parameterAnnotations("invisible parameter annotations",
                    ((RuntimeInvisibleParameterAnnotationsAttribute) attr).parameterAnnotations());
        }
        if (attr instanceof RuntimeVisibleParameterAnnotationsAttribute) {
            return parameterAnnotations("visible parameter annotations",
                    ((RuntimeVisibleParameterAnnotationsAttribute) attr).parameterAnnotations());
        }
        if (attr instanceof RuntimeInvisibleTypeAnnotationsAttribute) {
            return typeAnnotations("invisible type annotations",
                    ((RuntimeInvisibleTypeAnnotationsAttribute) attr).annotations());
        }
        if (attr instanceof RuntimeVisibleTypeAnnotationsAttribute) {
            return typeAnnotations("visible type annotations",
                    ((RuntimeVisibleTypeAnnotationsAttribute) attr).annotations());
        }
        if (attr instanceof SignatureAttribute) {
            return DebugNode.leaf("signature", ((SignatureAttribute) attr).signature().stringValue());
        }
        if (attr instanceof SourceFileAttribute) {
            return DebugNode.leaf("source file", ((SourceFileAttribute) attr).sourceFile().stringValue());
        }
        return null;
    }

    private static DebugNode module(ModuleAttribute ma) {
        DebugNode n = DebugNode.blockMap("module");
        n.with("name", ma.moduleName().name().stringValue());
        n.with(DebugPrinter.flags(ma.moduleFlags()));
        n.with("version", orNull(ma.moduleVersion()));
        n.with(DebugNode.flowList("uses", internalNames(ma.uses())));
        DebugNode requires = DebugNode.blockList("requires");
        for (ModuleRequireInfo req : ma.requires()) {
            Optional<Utf8Entry> v = req.requiresVersion();
            requires.add(DebugNode.flowMap("req")
                    .with("name", req.requires().name().stringValue())
                    .with(DebugPrinter.flags(req.requiresFlags()))
                    .with("version", v.isPresent() ? v.get().stringValue() : "null"));
        }
        n.with(requires);
        DebugNode exports = DebugNode.blockList("exports");
        for (ModuleExportInfo exp : ma.exports()) {
            exports.add(DebugNode.flowMap("exp")
                    .with("package", exp.exportedPackage().asSymbol().name())
                    .with(DebugPrinter.flags(exp.exportsFlags()))
                    .with(DebugNode.flowList("to", moduleNames(exp.exportsTo()))));
        }
        n.with(exports);
        DebugNode opens = DebugNode.blockList("opens");
        for (ModuleOpenInfo opn : ma.opens()) {
            opens.add(DebugNode.flowMap("opn")
                    .with("package", opn.openedPackage().asSymbol().name())
                    .with(DebugPrinter.flags(opn.opensFlags()))
                    .with(DebugNode.flowList("to", moduleNames(opn.opensTo()))));
        }
        n.with(opens);
        DebugNode provides = DebugNode.blockList("provides");
        for (ModuleProvideInfo prov : ma.provides()) {
            provides.add(DebugNode.flowMap("prov")
                    .with("class", prov.provides().asInternalName())
                    .with(DebugNode.flowList("with", internalNames(prov.providesWith()))));
        }
        n.with(provides);
        return n;
    }

    private static List<Object> moduleNames(List<ModuleEntry> modules) {
        List<Object> names = new ArrayList<>();
        for (ModuleEntry me : modules) {
            names.add(me.name().stringValue());
        }
        return names;
    }

    static DebugNode annotations(String name, List<Annotation> annos) {
        DebugNode list = DebugNode.blockList(name);
        for (Annotation a : annos) {
            list.add(annotation(a));
        }
        return list;
    }

    private static DebugNode annotation(Annotation a) {
        return DebugNode.flowMap("anno")
                .with("annotation class", a.className().stringValue())
                .with(pairs(a.elements()));
    }

    private static DebugNode pairs(List<AnnotationElement> elements) {
        DebugNode list = DebugNode.flowList("values");
        for (AnnotationElement e : elements) {
            DebugNode pair = DebugNode.flowMap("pair").with("name", e.name().stringValue());
            pair.with(valueMap(DebugNode.flowMap("value"), e.value()));
            list.add(pair);
        }
        return list;
    }

    private static DebugNode parameterAnnotations(String name, List<List<Annotation>> params) {
        DebugNode map = DebugNode.blockMap(name);
        for (int i = 0; i < params.size(); i++) {
            List<Annotation> annos = params.get(i);
            if (!annos.isEmpty()) {
                DebugNode list = DebugNode.flowList("parameter " + (i + 1));
                for (Annotation a : annos) {
                    list.add(annotation(a));
                }
                map.with(list);
            }
        }
        return map;
    }

    static DebugNode typeAnnotation(TypeAnnotation ta) {
        return DebugNode.flowMap("anno")
                .with("annotation class", ta.annotation().className().stringValue())
                .with("target info", ta.targetInfo().targetType().name())
                .with(pairs(ta.annotation().elements()));
    }

    private static DebugNode typeAnnotations(String name, List<TypeAnnotation> annos) {
        DebugNode list = DebugNode.blockList(name);
        for (TypeAnnotation ta : annos) {
            list.add(typeAnnotation(ta));
        }
        return list;
    }

    private static DebugNode valueMap(DebugNode map, AnnotationValue v) {
        if (v instanceof AnnotationValue.OfString) {
            return map.with("string", ((AnnotationValue.OfString) v).stringValue());
        } else if (v instanceof AnnotationValue.OfDouble) {
            return map.with("double", String.valueOf(((AnnotationValue.OfDouble) v).doubleValue()));
        } else if (v instanceof AnnotationValue.OfFloat) {
            return map.with("float", String.valueOf(((AnnotationValue.OfFloat) v).floatValue()));
        } else if (v instanceof AnnotationValue.OfLong) {
            return map.with("long", String.valueOf(((AnnotationValue.OfLong) v).longValue()));
        } else if (v instanceof AnnotationValue.OfInt) {
            return map.with("int", String.valueOf(((AnnotationValue.OfInt) v).intValue()));
        } else if (v instanceof AnnotationValue.OfShort) {
            return map.with("short", String.valueOf(((AnnotationValue.OfShort) v).shortValue()));
        } else if (v instanceof AnnotationValue.OfChar) {
            return map.with("char", String.valueOf(((AnnotationValue.OfChar) v).charValue()));
        } else if (v instanceof AnnotationValue.OfByte) {
            return map.with("byte", String.valueOf(((AnnotationValue.OfByte) v).byteValue()));
        } else if (v instanceof AnnotationValue.OfBoolean) {
            return map.with("boolean", String.valueOf(((AnnotationValue.OfBoolean) v).booleanValue()));
        } else if (v instanceof AnnotationValue.OfClass) {
            return map.with("class", ((AnnotationValue.OfClass) v).className().stringValue());
        } else if (v instanceof AnnotationValue.OfEnum) {
            AnnotationValue.OfEnum ev = (AnnotationValue.OfEnum) v;
            return map.with("enum class", ev.className().stringValue())
                    .with("constant name", ev.constantName().stringValue());
        } else if (v instanceof AnnotationValue.OfAnnotation) {
            return map.with("annotation class",
                    ((AnnotationValue.OfAnnotation) v).annotation().className().stringValue());
        } else {
            DebugNode list = DebugNode.flowList("array");
            for (AnnotationValue item : ((AnnotationValue.OfArray) v).values()) {
                list.add(valueMap(DebugNode.flowMap("value"), item));
            }
            return map.with(list);
        }
    }
}
