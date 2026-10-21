/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.lang.classfile;

import java.lang.classfile.attribute.AnnotationDefaultAttribute;
import java.lang.classfile.attribute.BootstrapMethodsAttribute;
import java.lang.classfile.attribute.CharacterRangeTableAttribute;
import java.lang.classfile.attribute.CodeAttribute;
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
import java.lang.classfile.attribute.ModuleHashesAttribute;
import java.lang.classfile.attribute.ModuleMainClassAttribute;
import java.lang.classfile.attribute.ModulePackagesAttribute;
import java.lang.classfile.attribute.ModuleResolutionAttribute;
import java.lang.classfile.attribute.ModuleTargetAttribute;
import java.lang.classfile.attribute.NestHostAttribute;
import java.lang.classfile.attribute.NestMembersAttribute;
import java.lang.classfile.attribute.PermittedSubclassesAttribute;
import java.lang.classfile.attribute.RecordAttribute;
import java.lang.classfile.attribute.RuntimeInvisibleAnnotationsAttribute;
import java.lang.classfile.attribute.RuntimeInvisibleParameterAnnotationsAttribute;
import java.lang.classfile.attribute.RuntimeInvisibleTypeAnnotationsAttribute;
import java.lang.classfile.attribute.RuntimeVisibleAnnotationsAttribute;
import java.lang.classfile.attribute.RuntimeVisibleParameterAnnotationsAttribute;
import java.lang.classfile.attribute.RuntimeVisibleTypeAnnotationsAttribute;
import java.lang.classfile.attribute.SignatureAttribute;
import java.lang.classfile.attribute.SourceDebugExtensionAttribute;
import java.lang.classfile.attribute.SourceFileAttribute;
import java.lang.classfile.attribute.SourceIDAttribute;
import java.lang.classfile.attribute.StackMapTableAttribute;
import java.lang.classfile.attribute.SyntheticAttribute;

public final class Attributes {
    public static final String NAME_ANNOTATION_DEFAULT = "AnnotationDefault";
    public static final String NAME_BOOTSTRAP_METHODS = "BootstrapMethods";
    public static final String NAME_CHARACTER_RANGE_TABLE = "CharacterRangeTable";
    public static final String NAME_CODE = "Code";
    public static final String NAME_COMPILATION_ID = "CompilationID";
    public static final String NAME_CONSTANT_VALUE = "ConstantValue";
    public static final String NAME_DEPRECATED = "Deprecated";
    public static final String NAME_ENCLOSING_METHOD = "EnclosingMethod";
    public static final String NAME_EXCEPTIONS = "Exceptions";
    public static final String NAME_INNER_CLASSES = "InnerClasses";
    public static final String NAME_LINE_NUMBER_TABLE = "LineNumberTable";
    public static final String NAME_LOCAL_VARIABLE_TABLE = "LocalVariableTable";
    public static final String NAME_LOCAL_VARIABLE_TYPE_TABLE = "LocalVariableTypeTable";
    public static final String NAME_METHOD_PARAMETERS = "MethodParameters";
    public static final String NAME_MODULE = "Module";
    public static final String NAME_MODULE_HASHES = "ModuleHashes";
    public static final String NAME_MODULE_MAIN_CLASS = "ModuleMainClass";
    public static final String NAME_MODULE_PACKAGES = "ModulePackages";
    public static final String NAME_MODULE_RESOLUTION = "ModuleResolution";
    public static final String NAME_MODULE_TARGET = "ModuleTarget";
    public static final String NAME_NEST_HOST = "NestHost";
    public static final String NAME_NEST_MEMBERS = "NestMembers";
    public static final String NAME_PERMITTED_SUBCLASSES = "PermittedSubclasses";
    public static final String NAME_RECORD = "Record";
    public static final String NAME_RUNTIME_INVISIBLE_ANNOTATIONS = "RuntimeInvisibleAnnotations";
    public static final String NAME_RUNTIME_INVISIBLE_PARAMETER_ANNOTATIONS = "RuntimeInvisibleParameterAnnotations";
    public static final String NAME_RUNTIME_INVISIBLE_TYPE_ANNOTATIONS = "RuntimeInvisibleTypeAnnotations";
    public static final String NAME_RUNTIME_VISIBLE_ANNOTATIONS = "RuntimeVisibleAnnotations";
    public static final String NAME_RUNTIME_VISIBLE_PARAMETER_ANNOTATIONS = "RuntimeVisibleParameterAnnotations";
    public static final String NAME_RUNTIME_VISIBLE_TYPE_ANNOTATIONS = "RuntimeVisibleTypeAnnotations";
    public static final String NAME_SIGNATURE = "Signature";
    public static final String NAME_SOURCE_DEBUG_EXTENSION = "SourceDebugExtension";
    public static final String NAME_SOURCE_FILE = "SourceFile";
    public static final String NAME_SOURCE_ID = "SourceID";
    public static final String NAME_STACK_MAP_TABLE = "StackMapTable";
    public static final String NAME_SYNTHETIC = "Synthetic";
    public static AttributeMapper<SignatureAttribute> signature() { return shim.classfile.SignatureAttr.MAPPER; }
    public static AttributeMapper<AnnotationDefaultAttribute> annotationDefault() {
        return shim.classfile.AnnotationDefaultAttr.MAPPER;
    }
    public static AttributeMapper<CodeAttribute> code() { return shim.classfile.CodeImpl.MAPPER; }
    public static AttributeMapper<RecordAttribute> record() { return shim.classfile.RecordAttr.MAPPER; }
    public static AttributeMapper<ModuleAttribute> module() { return shim.classfile.ModuleAttr.MAPPER; }
    public static AttributeMapper<ModuleTargetAttribute> moduleTarget() { return shim.classfile.ModuleTargetAttr.MAPPER; }
    public static AttributeMapper<ModuleResolutionAttribute> moduleResolution() { return shim.classfile.ModuleResolutionAttr.MAPPER; }
    public static AttributeMapper<ModuleHashesAttribute> moduleHashes() { return shim.classfile.ModuleHashesAttr.MAPPER; }
    public static AttributeMapper<SyntheticAttribute> synthetic() { return shim.classfile.SyntheticAttr.MAPPER; }
    public static AttributeMapper<ConstantValueAttribute> constantValue() { return shim.classfile.ConstantValueAttr.MAPPER; }
    public static AttributeMapper<RuntimeVisibleAnnotationsAttribute> runtimeVisibleAnnotations() {
        return shim.classfile.RuntimeVisibleAnnotationsAttr.MAPPER;
    }
    public static AttributeMapper<BootstrapMethodsAttribute> bootstrapMethods() { return shim.classfile.BootstrapMethodsAttr.MAPPER; }
    public static AttributeMapper<CharacterRangeTableAttribute> characterRangeTable() { return shim.classfile.CharacterRangeTableAttr.MAPPER; }
    public static AttributeMapper<CompilationIDAttribute> compilationId() { return shim.classfile.CompilationIDAttr.MAPPER; }
    public static AttributeMapper<DeprecatedAttribute> deprecated() { return shim.classfile.DeprecatedAttr.MAPPER; }
    public static AttributeMapper<EnclosingMethodAttribute> enclosingMethod() { return shim.classfile.EnclosingMethodAttr.MAPPER; }
    public static AttributeMapper<ExceptionsAttribute> exceptions() { return shim.classfile.ExceptionsAttr.MAPPER; }
    public static AttributeMapper<InnerClassesAttribute> innerClasses() { return shim.classfile.InnerClassesAttr.MAPPER; }
    public static AttributeMapper<LineNumberTableAttribute> lineNumberTable() { return shim.classfile.LineNumberTableAttr.MAPPER; }
    public static AttributeMapper<LocalVariableTableAttribute> localVariableTable() { return shim.classfile.LocalVariableTableAttr.MAPPER; }
    public static AttributeMapper<LocalVariableTypeTableAttribute> localVariableTypeTable() { return shim.classfile.LocalVariableTypeTableAttr.MAPPER; }
    public static AttributeMapper<MethodParametersAttribute> methodParameters() { return shim.classfile.MethodParametersAttr.MAPPER; }
    public static AttributeMapper<ModuleMainClassAttribute> moduleMainClass() { return shim.classfile.ModuleMainClassAttr.MAPPER; }
    public static AttributeMapper<ModulePackagesAttribute> modulePackages() { return shim.classfile.ModulePackagesAttr.MAPPER; }
    public static AttributeMapper<NestHostAttribute> nestHost() { return shim.classfile.NestHostAttr.MAPPER; }
    public static AttributeMapper<NestMembersAttribute> nestMembers() { return shim.classfile.NestMembersAttr.MAPPER; }
    public static AttributeMapper<PermittedSubclassesAttribute> permittedSubclasses() { return shim.classfile.PermittedSubclassesAttr.MAPPER; }
    public static AttributeMapper<RuntimeInvisibleAnnotationsAttribute> runtimeInvisibleAnnotations() {
        return shim.classfile.RuntimeInvisibleAnnotationsAttr.MAPPER;
    }
    public static AttributeMapper<RuntimeInvisibleParameterAnnotationsAttribute>
            runtimeInvisibleParameterAnnotations() {
        return shim.classfile.RuntimeInvisibleParameterAnnotationsAttr.MAPPER;
    }
    public static AttributeMapper<RuntimeInvisibleTypeAnnotationsAttribute> runtimeInvisibleTypeAnnotations() {
        return shim.classfile.RuntimeInvisibleTypeAnnotationsAttr.MAPPER;
    }
    public static AttributeMapper<RuntimeVisibleParameterAnnotationsAttribute> runtimeVisibleParameterAnnotations() {
        return shim.classfile.RuntimeVisibleParameterAnnotationsAttr.MAPPER;
    }
    public static AttributeMapper<RuntimeVisibleTypeAnnotationsAttribute> runtimeVisibleTypeAnnotations() {
        return shim.classfile.RuntimeVisibleTypeAnnotationsAttr.MAPPER;
    }
    public static AttributeMapper<SourceDebugExtensionAttribute> sourceDebugExtension() { return shim.classfile.SourceDebugExtensionAttr.MAPPER; }
    public static AttributeMapper<SourceFileAttribute> sourceFile() { return shim.classfile.SourceFileAttr.MAPPER; }
    public static AttributeMapper<SourceIDAttribute> sourceId() { return shim.classfile.SourceIDAttr.MAPPER; }
    public static AttributeMapper<StackMapTableAttribute> stackMapTable() { return shim.classfile.StackMapTableAttr.MAPPER; }
}
