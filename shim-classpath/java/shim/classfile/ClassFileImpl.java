/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.classfile;

import java.lang.classfile.AttributeMapper;
import java.lang.classfile.ClassBuilder;
import java.lang.classfile.ClassFile;
import java.lang.classfile.ClassHierarchyResolver;
import java.lang.classfile.ClassModel;
import java.lang.classfile.ClassTransform;
import java.lang.classfile.constantpool.ClassEntry;
import java.lang.classfile.constantpool.ConstantPoolBuilder;
import java.lang.classfile.constantpool.Utf8Entry;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/** The {@link ClassFile} context: the option set plus parse, build and transform entry points. */
public final class ClassFileImpl implements ClassFile {
    public static final ClassFileImpl DEFAULT = new ClassFileImpl(
        ClassFile.AttributesProcessingOption.PASS_ALL_ATTRIBUTES,
        ClassFile.StackMapsOption.STACK_MAPS_WHEN_REQUIRED,
        ClassFile.ShortJumpsOption.FIX_SHORT_JUMPS,
        ClassFile.LineNumbersOption.PASS_LINE_NUMBERS,
        ClassFile.DebugElementsOption.PASS_DEBUG,
        ClassFile.DeadLabelsOption.FAIL_ON_DEAD_LABELS,
        ClassFile.DeadCodeOption.PATCH_DEAD_CODE,
        ClassFile.ConstantPoolSharingOption.SHARED_POOL,
        null,
        null);

    public final ClassFile.AttributesProcessingOption attributesProcessing;
    public final ClassFile.StackMapsOption stackMaps;
    public final ClassFile.ShortJumpsOption shortJumps;
    public final ClassFile.LineNumbersOption lineNumbers;
    public final ClassFile.DebugElementsOption debugElements;
    public final ClassFile.DeadLabelsOption deadLabels;
    public final ClassFile.DeadCodeOption deadCode;
    public final ClassFile.ConstantPoolSharingOption poolSharing;
    private final ClassHierarchyResolver resolver;
    public final Function<Utf8Entry, AttributeMapper<?>> customAttributes;

    private ClassFileImpl(ClassFile.AttributesProcessingOption attributesProcessing, ClassFile.StackMapsOption stackMaps,
                          ClassFile.ShortJumpsOption shortJumps, ClassFile.LineNumbersOption lineNumbers,
                          ClassFile.DebugElementsOption debugElements, ClassFile.DeadLabelsOption deadLabels,
                          ClassFile.DeadCodeOption deadCode, ClassFile.ConstantPoolSharingOption poolSharing,
                          ClassHierarchyResolver resolver, Function<Utf8Entry, AttributeMapper<?>> customAttributes) {
        this.attributesProcessing = attributesProcessing;
        this.stackMaps = stackMaps;
        this.shortJumps = shortJumps;
        this.lineNumbers = lineNumbers;
        this.debugElements = debugElements;
        this.deadLabels = deadLabels;
        this.deadCode = deadCode;
        this.poolSharing = poolSharing;
        this.resolver = resolver;
        this.customAttributes = customAttributes;
    }

    /** The hierarchy resolver used to compute stack maps. */
    public ClassHierarchyResolver hierarchyResolver() {
        return resolver != null ? resolver : ClassHierarchyResolver.defaultResolver();
    }

    @Override
    public ClassFile withOptions(ClassFile.Option... options) {
        ClassFile.AttributesProcessingOption ap = attributesProcessing;
        ClassFile.StackMapsOption sm = stackMaps;
        ClassFile.ShortJumpsOption sj = shortJumps;
        ClassFile.LineNumbersOption ln = lineNumbers;
        ClassFile.DebugElementsOption de = debugElements;
        ClassFile.DeadLabelsOption dl = deadLabels;
        ClassFile.DeadCodeOption dc = deadCode;
        ClassFile.ConstantPoolSharingOption ps = poolSharing;
        ClassHierarchyResolver hr = resolver;
        Function<Utf8Entry, AttributeMapper<?>> ca = customAttributes;
        for (ClassFile.Option o : options) {
            if (o instanceof ClassFile.AttributesProcessingOption) {
                ap = (ClassFile.AttributesProcessingOption) o;
            } else if (o instanceof ClassFile.StackMapsOption) {
                sm = (ClassFile.StackMapsOption) o;
            } else if (o instanceof ClassFile.ShortJumpsOption) {
                sj = (ClassFile.ShortJumpsOption) o;
            } else if (o instanceof ClassFile.LineNumbersOption) {
                ln = (ClassFile.LineNumbersOption) o;
            } else if (o instanceof ClassFile.DebugElementsOption) {
                de = (ClassFile.DebugElementsOption) o;
            } else if (o instanceof ClassFile.DeadLabelsOption) {
                dl = (ClassFile.DeadLabelsOption) o;
            } else if (o instanceof ClassFile.DeadCodeOption) {
                dc = (ClassFile.DeadCodeOption) o;
            } else if (o instanceof ClassFile.ConstantPoolSharingOption) {
                ps = (ClassFile.ConstantPoolSharingOption) o;
            } else if (o instanceof ClassFile.ClassHierarchyResolverOption) {
                hr = ((ClassFile.ClassHierarchyResolverOption) o).classHierarchyResolver();
            } else if (o instanceof ClassFile.AttributeMapperOption) {
                ca = ((ClassFile.AttributeMapperOption) o).attributeMapper();
            } else {
                throw new IllegalArgumentException("Unknown option: " + o);
            }
        }
        return new ClassFileImpl(ap, sm, sj, ln, de, dl, dc, ps, hr, ca);
    }

    @Override
    public ClassModel parse(byte[] bytes) {
        ClassReaderImpl reader = new ClassReaderImpl(bytes, customAttributes);
        reader.options = this;
        return new ClassModelImpl(reader);
    }

    @Override
    public byte[] build(ClassEntry thisClass, ConstantPoolBuilder pool, Consumer<? super ClassBuilder> handler) {
        DirectClassBuilder builder = new DirectClassBuilder(this, (PoolImpl) pool, thisClass);
        handler.accept(builder);
        return builder.finish();
    }

    @Override
    public byte[] transformClass(ClassModel model, ClassEntry thisClass, ClassTransform transform) {
        ConstantPoolBuilder pool = poolSharing == ClassFile.ConstantPoolSharingOption.SHARED_POOL
            ? ConstantPoolBuilder.of(model) : ConstantPoolBuilder.of();
        return build(thisClass, pool, builder -> builder.transform(model, transform));
    }

    @Override
    public List<VerifyError> verify(ClassModel model) {
        return verify(((ClassReaderImpl) model.constantPool()).bytes());
    }

    @Override
    public List<VerifyError> verify(byte[] bytes) {
        List<VerifyError> errors = new ArrayList<>();
        try {
            ClassModel model = parse(bytes);
            Verifier.verify(this, model, errors);
        } catch (RuntimeException e) {
            errors.add(new VerifyError(String.valueOf(e.getMessage())));
        }
        return errors;
    }
}
