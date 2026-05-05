/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.classfile;

import java.lang.classfile.ClassBuilder;
import java.lang.classfile.ClassElement;
import java.lang.classfile.ClassFileBuilder;
import java.lang.classfile.ClassFileElement;
import java.lang.classfile.ClassFileTransform;
import java.lang.classfile.ClassTransform;
import java.lang.classfile.CodeBuilder;
import java.lang.classfile.CodeElement;
import java.lang.classfile.CodeTransform;
import java.lang.classfile.FieldBuilder;
import java.lang.classfile.FieldElement;
import java.lang.classfile.FieldTransform;
import java.lang.classfile.MethodBuilder;
import java.lang.classfile.MethodElement;
import java.lang.classfile.MethodTransform;
import java.util.function.Supplier;

/** Composition and per-use resolution of the transforms. */
public final class Transforms {
    private Transforms() { }

    /** A transform that has to be turned into a fresh instance each time it is applied. */
    interface Resolvable {
        Object resolveTransform();
    }

    @SuppressWarnings("unchecked")
    public static <E extends ClassFileElement, B extends ClassFileBuilder<E, B>>
            ClassFileTransform<?, E, B> resolve(ClassFileTransform<?, E, B> transform) {
        return transform instanceof Resolvable ? (ClassFileTransform<?, E, B>) ((Resolvable) transform).resolveTransform() : transform;
    }

    private static ClassTransform resolve(ClassTransform t) {
        return t instanceof Resolvable ? (ClassTransform) ((Resolvable) t).resolveTransform() : t;
    }

    private static MethodTransform resolve(MethodTransform t) {
        return t instanceof Resolvable ? (MethodTransform) ((Resolvable) t).resolveTransform() : t;
    }

    private static FieldTransform resolve(FieldTransform t) {
        return t instanceof Resolvable ? (FieldTransform) ((Resolvable) t).resolveTransform() : t;
    }

    private static CodeTransform resolve(CodeTransform t) {
        return t instanceof Resolvable ? (CodeTransform) ((Resolvable) t).resolveTransform() : t;
    }

    public static final class ChainedClass implements ClassTransform, Resolvable {
        private final ClassTransform first;
        private final ClassTransform second;

        public ChainedClass(ClassTransform first, ClassTransform second) {
            this.first = first;
            this.second = second;
        }

        @Override public Object resolveTransform() { return new ChainedClass(resolve(first), resolve(second)); }

        @Override
        public void accept(ClassBuilder builder, ClassElement element) {
            first.accept(new Downstream(builder, second), element);
        }

        @Override
        public void atStart(ClassBuilder builder) {
            second.atStart(builder);
            first.atStart(new Downstream(builder, second));
        }

        @Override
        public void atEnd(ClassBuilder builder) {
            first.atEnd(new Downstream(builder, second));
            second.atEnd(builder);
        }

        private static final class Downstream extends AbstractClassBuilder {
            private final ClassBuilder target;
            private final ClassTransform next;

            Downstream(ClassBuilder target, ClassTransform next) {
                this.target = target;
                this.next = next;
            }

            @Override public ClassBuilder with(ClassElement e) { next.accept(target, e); return this; }
            @Override public java.lang.classfile.constantpool.ConstantPoolBuilder constantPool() { return target.constantPool(); }
            @Override ClassFileImpl classFile() { return ((AbstractClassBuilder) target).classFile(); }
            @Override java.lang.classfile.constantpool.ClassEntry thisClass() { return ((AbstractClassBuilder) target).thisClass(); }
        }
    }

    public static final class ChainedMethod implements MethodTransform, Resolvable {
        private final MethodTransform first;
        private final MethodTransform second;

        public ChainedMethod(MethodTransform first, MethodTransform second) {
            this.first = first;
            this.second = second;
        }

        @Override public Object resolveTransform() { return new ChainedMethod(resolve(first), resolve(second)); }

        @Override
        public void accept(MethodBuilder builder, MethodElement element) {
            first.accept(new Downstream(builder, second), element);
        }

        @Override
        public void atStart(MethodBuilder builder) {
            second.atStart(builder);
            first.atStart(new Downstream(builder, second));
        }

        @Override
        public void atEnd(MethodBuilder builder) {
            first.atEnd(new Downstream(builder, second));
            second.atEnd(builder);
        }

        private static final class Downstream extends MethodBuilderImpl {
            private final MethodBuilderImpl target;
            private final MethodTransform next;

            Downstream(MethodBuilder target, MethodTransform next) {
                super((MethodBuilderImpl) target);
                this.target = (MethodBuilderImpl) target;
                this.next = next;
            }

            @Override public MethodBuilder with(MethodElement e) { next.accept(target, e); return this; }
        }
    }

    public static final class ChainedField implements FieldTransform, Resolvable {
        private final FieldTransform first;
        private final FieldTransform second;

        public ChainedField(FieldTransform first, FieldTransform second) {
            this.first = first;
            this.second = second;
        }

        @Override public Object resolveTransform() { return new ChainedField(resolve(first), resolve(second)); }

        @Override
        public void accept(FieldBuilder builder, FieldElement element) {
            first.accept(new Downstream(builder, second), element);
        }

        @Override
        public void atStart(FieldBuilder builder) {
            second.atStart(builder);
            first.atStart(new Downstream(builder, second));
        }

        @Override
        public void atEnd(FieldBuilder builder) {
            first.atEnd(new Downstream(builder, second));
            second.atEnd(builder);
        }

        private static final class Downstream extends FieldBuilderImpl {
            private final FieldBuilderImpl target;
            private final FieldTransform next;

            Downstream(FieldBuilder target, FieldTransform next) {
                super((FieldBuilderImpl) target);
                this.target = (FieldBuilderImpl) target;
                this.next = next;
            }

            @Override public FieldBuilder with(FieldElement e) { next.accept(target, e); return this; }
        }
    }

    public static final class ChainedCode implements CodeTransform, Resolvable {
        private final CodeTransform first;
        private final CodeTransform second;

        public ChainedCode(CodeTransform first, CodeTransform second) {
            this.first = first;
            this.second = second;
        }

        @Override public Object resolveTransform() { return new ChainedCode(resolve(first), resolve(second)); }

        @Override
        public void accept(CodeBuilder builder, CodeElement element) {
            first.accept(CodeBuilderImpl.forwarding(builder, second), element);
        }

        @Override
        public void atStart(CodeBuilder builder) {
            second.atStart(builder);
            first.atStart(CodeBuilderImpl.forwarding(builder, second));
        }

        @Override
        public void atEnd(CodeBuilder builder) {
            first.atEnd(CodeBuilderImpl.forwarding(builder, second));
            second.atEnd(builder);
        }
    }

    public static final class StatefulClass implements ClassTransform, Resolvable {
        private final Supplier<ClassTransform> supplier;
        private ClassTransform delegate;

        public StatefulClass(Supplier<ClassTransform> supplier) { this.supplier = supplier; }

        @Override public Object resolveTransform() { return resolve(supplier.get()); }
        private ClassTransform current() { return delegate == null ? delegate = resolve(supplier.get()) : delegate; }
        @Override public void accept(ClassBuilder b, ClassElement e) { current().accept(b, e); }
        @Override public void atStart(ClassBuilder b) { current().atStart(b); }
        @Override public void atEnd(ClassBuilder b) { current().atEnd(b); }
    }

    public static final class StatefulMethod implements MethodTransform, Resolvable {
        private final Supplier<MethodTransform> supplier;
        private MethodTransform delegate;

        public StatefulMethod(Supplier<MethodTransform> supplier) { this.supplier = supplier; }

        @Override public Object resolveTransform() { return resolve(supplier.get()); }
        private MethodTransform current() { return delegate == null ? delegate = resolve(supplier.get()) : delegate; }
        @Override public void accept(MethodBuilder b, MethodElement e) { current().accept(b, e); }
        @Override public void atStart(MethodBuilder b) { current().atStart(b); }
        @Override public void atEnd(MethodBuilder b) { current().atEnd(b); }
    }

    public static final class StatefulField implements FieldTransform, Resolvable {
        private final Supplier<FieldTransform> supplier;
        private FieldTransform delegate;

        public StatefulField(Supplier<FieldTransform> supplier) { this.supplier = supplier; }

        @Override public Object resolveTransform() { return resolve(supplier.get()); }
        private FieldTransform current() { return delegate == null ? delegate = resolve(supplier.get()) : delegate; }
        @Override public void accept(FieldBuilder b, FieldElement e) { current().accept(b, e); }
        @Override public void atStart(FieldBuilder b) { current().atStart(b); }
        @Override public void atEnd(FieldBuilder b) { current().atEnd(b); }
    }

    public static final class StatefulCode implements CodeTransform, Resolvable {
        private final Supplier<CodeTransform> supplier;
        private CodeTransform delegate;

        public StatefulCode(Supplier<CodeTransform> supplier) { this.supplier = supplier; }

        @Override public Object resolveTransform() { return resolve(supplier.get()); }
        private CodeTransform current() { return delegate == null ? delegate = resolve(supplier.get()) : delegate; }
        @Override public void accept(CodeBuilder b, CodeElement e) { current().accept(b, e); }
        @Override public void atStart(CodeBuilder b) { current().atStart(b); }
        @Override public void atEnd(CodeBuilder b) { current().atEnd(b); }
    }
}
