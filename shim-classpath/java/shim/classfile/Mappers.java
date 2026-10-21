/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.classfile;

import java.lang.classfile.Attributes;
import java.lang.classfile.AttributeMapper;
import java.lang.classfile.constantpool.Utf8Entry;
import java.util.function.Function;

/** Name to mapper registry of the standard attributes. */
public final class Mappers {
    private Mappers() { }

    public static AttributeMapper<?> standard(String name) {
        try {
            return lookup(name);
        } catch (UnsupportedOperationException notYet) {
            return null;
        }
    }

    private static AttributeMapper<?> lookup(String name) {
        switch (name) {
            case "AnnotationDefault":
                return Attributes.annotationDefault();
            case "BootstrapMethods":
                return Attributes.bootstrapMethods();
            case "CharacterRangeTable":
                return Attributes.characterRangeTable();
            case "Code":
                return Attributes.code();
            case "CompilationID":
                return Attributes.compilationId();
            case "ConstantValue":
                return Attributes.constantValue();
            case "Deprecated":
                return Attributes.deprecated();
            case "EnclosingMethod":
                return Attributes.enclosingMethod();
            case "Exceptions":
                return Attributes.exceptions();
            case "InnerClasses":
                return Attributes.innerClasses();
            case "LineNumberTable":
                return Attributes.lineNumberTable();
            case "LocalVariableTable":
                return Attributes.localVariableTable();
            case "LocalVariableTypeTable":
                return Attributes.localVariableTypeTable();
            case "MethodParameters":
                return Attributes.methodParameters();
            case "Module":
                return Attributes.module();
            case "ModuleHashes":
                return Attributes.moduleHashes();
            case "ModuleMainClass":
                return Attributes.moduleMainClass();
            case "ModulePackages":
                return Attributes.modulePackages();
            case "ModuleResolution":
                return Attributes.moduleResolution();
            case "ModuleTarget":
                return Attributes.moduleTarget();
            case "NestHost":
                return Attributes.nestHost();
            case "NestMembers":
                return Attributes.nestMembers();
            case "PermittedSubclasses":
                return Attributes.permittedSubclasses();
            case "Record":
                return Attributes.record();
            case "RuntimeInvisibleAnnotations":
                return Attributes.runtimeInvisibleAnnotations();
            case "RuntimeInvisibleParameterAnnotations":
                return Attributes.runtimeInvisibleParameterAnnotations();
            case "RuntimeInvisibleTypeAnnotations":
                return Attributes.runtimeInvisibleTypeAnnotations();
            case "RuntimeVisibleAnnotations":
                return Attributes.runtimeVisibleAnnotations();
            case "RuntimeVisibleParameterAnnotations":
                return Attributes.runtimeVisibleParameterAnnotations();
            case "RuntimeVisibleTypeAnnotations":
                return Attributes.runtimeVisibleTypeAnnotations();
            case "Signature":
                return Attributes.signature();
            case "SourceDebugExtension":
                return Attributes.sourceDebugExtension();
            case "SourceFile":
                return Attributes.sourceFile();
            case "SourceID":
                return Attributes.sourceId();
            case "StackMapTable":
                return Attributes.stackMapTable();
            case "Synthetic":
                return Attributes.synthetic();
            default:
                return null;
        }
    }

    /** Picks the mapper for an attribute: a custom one from the options, else standard, else unknown. */
    public static AttributeMapper<?> resolve(Utf8Entry name, Function<Utf8Entry, AttributeMapper<?>> custom) {
        AttributeMapper<?> m = standard(name.stringValue());
        if (m != null) {
            return m;
        }
        if (custom != null) {
            m = custom.apply(name);
            if (m != null) {
                return m;
            }
        }
        return UnknownAttr.MAPPER;
    }
}
