/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.lang.classfile;

import java.io.IOException;
import java.lang.classfile.attribute.ModuleAttribute;
import java.lang.classfile.constantpool.ClassEntry;
import java.lang.classfile.constantpool.ConstantPoolBuilder;
import java.lang.classfile.constantpool.Utf8Entry;
import java.lang.constant.ClassDesc;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public interface ClassFile {
    public static final int MAGIC_NUMBER = -889275714;
    public static final int ACC_PUBLIC = 1;
    public static final int ACC_PROTECTED = 4;
    public static final int ACC_PRIVATE = 2;
    public static final int ACC_INTERFACE = 512;
    public static final int ACC_ENUM = 16384;
    public static final int ACC_ANNOTATION = 8192;
    public static final int ACC_SUPER = 32;
    public static final int ACC_ABSTRACT = 1024;
    public static final int ACC_VOLATILE = 64;
    public static final int ACC_TRANSIENT = 128;
    public static final int ACC_SYNTHETIC = 4096;
    public static final int ACC_STATIC = 8;
    public static final int ACC_FINAL = 16;
    public static final int ACC_SYNCHRONIZED = 32;
    public static final int ACC_BRIDGE = 64;
    public static final int ACC_VARARGS = 128;
    public static final int ACC_NATIVE = 256;
    public static final int ACC_STRICT = 2048;
    public static final int ACC_MODULE = 32768;
    public static final int ACC_OPEN = 32;
    public static final int ACC_MANDATED = 32768;
    public static final int ACC_TRANSITIVE = 32;
    public static final int ACC_STATIC_PHASE = 64;
    public static final int JAVA_1_VERSION = 45;
    public static final int JAVA_2_VERSION = 46;
    public static final int JAVA_3_VERSION = 47;
    public static final int JAVA_4_VERSION = 48;
    public static final int JAVA_5_VERSION = 49;
    public static final int JAVA_6_VERSION = 50;
    public static final int JAVA_7_VERSION = 51;
    public static final int JAVA_8_VERSION = 52;
    public static final int JAVA_9_VERSION = 53;
    public static final int JAVA_10_VERSION = 54;
    public static final int JAVA_11_VERSION = 55;
    public static final int JAVA_12_VERSION = 56;
    public static final int JAVA_13_VERSION = 57;
    public static final int JAVA_14_VERSION = 58;
    public static final int JAVA_15_VERSION = 59;
    public static final int JAVA_16_VERSION = 60;
    public static final int JAVA_17_VERSION = 61;
    public static final int JAVA_18_VERSION = 62;
    public static final int JAVA_19_VERSION = 63;
    public static final int JAVA_20_VERSION = 64;
    public static final int JAVA_21_VERSION = 65;
    public static final int JAVA_22_VERSION = 66;
    public static final int JAVA_23_VERSION = 67;
    public static final int JAVA_24_VERSION = 68;
    public static final int JAVA_25_VERSION = 69;
    public static final int PREVIEW_MINOR_VERSION = 65535;
    public static ClassFile of() { return shim.classfile.ClassFileImpl.DEFAULT; }
    public static ClassFile of(ClassFile.Option... options) { return shim.classfile.ClassFileImpl.DEFAULT.withOptions(options); }
    ClassModel parse(byte[] bs);
    default ClassModel parse(Path path) throws IOException { return parse(java.nio.file.Files.readAllBytes(path)); }
    default byte[] transformClass(ClassModel classModel, ClassDesc classDesc, ClassTransform classTransform) { return transformClass(classModel, ConstantPoolBuilder.of().classEntry(classDesc), classTransform); }
    default byte[] transformClass(ClassModel classModel, ClassTransform classTransform) { return transformClass(classModel, classModel.thisClass(), classTransform); }
    byte[] transformClass(ClassModel classModel, ClassEntry classEntry, ClassTransform classTransform);
    public static int latestMajorVersion() { return 69; }
    default byte[] build(ClassDesc classDesc, Consumer<? super ClassBuilder> consumer) {
        ConstantPoolBuilder pool = ConstantPoolBuilder.of();
        return build(pool.classEntry(classDesc), pool, consumer);
    }
    byte[] build(ClassEntry classEntry, ConstantPoolBuilder constantPoolBuilder, Consumer<? super ClassBuilder> consumer);
    List<VerifyError> verify(ClassModel classModel);
    default List<VerifyError> verify(Path path) throws IOException { return verify(java.nio.file.Files.readAllBytes(path)); }
    List<VerifyError> verify(byte[] bs);
    ClassFile withOptions(ClassFile.Option... options);
    default byte[] buildModule(ModuleAttribute moduleAttribute, Consumer<? super ClassBuilder> consumer) {
        ConstantPoolBuilder pool = ConstantPoolBuilder.of();
        return build(pool.classEntry(ClassDesc.of("module-info")), pool, builder -> {
            builder.withFlags(ACC_MODULE);
            builder.with(moduleAttribute);
            consumer.accept(builder);
        });
    }
    default byte[] buildModule(ModuleAttribute moduleAttribute) { return buildModule(moduleAttribute, builder -> { }); }
    default void buildModuleTo(Path path, ModuleAttribute moduleAttribute, Consumer<? super ClassBuilder> consumer) throws IOException { java.nio.file.Files.write(path, buildModule(moduleAttribute, consumer)); }
    default void buildModuleTo(Path path, ModuleAttribute moduleAttribute) throws IOException { java.nio.file.Files.write(path, buildModule(moduleAttribute)); }
    default void buildTo(Path path, ClassDesc classDesc, Consumer<ClassBuilder> consumer) throws IOException { java.nio.file.Files.write(path, build(classDesc, consumer)); }
    default void buildTo(Path path, ClassEntry classEntry, ConstantPoolBuilder constantPoolBuilder, Consumer<? super ClassBuilder> consumer) throws IOException { java.nio.file.Files.write(path, build(classEntry, constantPoolBuilder, consumer)); }
    public static int latestMinorVersion() { return 0; }

    public interface Option {
    }

    public enum AttributesProcessingOption implements ClassFile.Option {
        PASS_ALL_ATTRIBUTES,
        DROP_UNKNOWN_ATTRIBUTES,
        DROP_UNSTABLE_ATTRIBUTES;
    }

    public enum StackMapsOption implements ClassFile.Option {
        STACK_MAPS_WHEN_REQUIRED,
        GENERATE_STACK_MAPS,
        DROP_STACK_MAPS;
    }

    public enum ShortJumpsOption implements ClassFile.Option {
        FIX_SHORT_JUMPS,
        FAIL_ON_SHORT_JUMPS;
    }

    public enum LineNumbersOption implements ClassFile.Option {
        PASS_LINE_NUMBERS,
        DROP_LINE_NUMBERS;
    }

    public enum DebugElementsOption implements ClassFile.Option {
        PASS_DEBUG,
        DROP_DEBUG;
    }

    public enum DeadLabelsOption implements ClassFile.Option {
        FAIL_ON_DEAD_LABELS,
        DROP_DEAD_LABELS;
    }

    public enum DeadCodeOption implements ClassFile.Option {
        PATCH_DEAD_CODE,
        KEEP_DEAD_CODE;
    }

    public enum ConstantPoolSharingOption implements ClassFile.Option {
        SHARED_POOL,
        NEW_POOL;
    }

    public interface ClassHierarchyResolverOption extends ClassFile.Option {
        public static ClassFile.ClassHierarchyResolverOption of(ClassHierarchyResolver classHierarchyResolver) {             return () -> classHierarchyResolver; }
        ClassHierarchyResolver classHierarchyResolver();
    }

    public interface AttributeMapperOption extends ClassFile.Option {
        public static ClassFile.AttributeMapperOption of(Function<Utf8Entry, AttributeMapper<?>> function) {             return () -> function; }
        Function<Utf8Entry, AttributeMapper<?>> attributeMapper();
    }
}
