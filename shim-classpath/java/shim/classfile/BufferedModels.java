/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.classfile;

import java.lang.classfile.AccessFlags;
import java.lang.classfile.Attribute;
import java.lang.classfile.ClassModel;
import java.lang.classfile.CodeModel;
import java.lang.classfile.FieldElement;
import java.lang.classfile.FieldModel;
import java.lang.classfile.MethodElement;
import java.lang.classfile.MethodModel;
import java.lang.classfile.constantpool.Utf8Entry;
import java.lang.reflect.AccessFlag;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import shim.classfile.Elements.AccessFlagsImpl;

/** Member models assembled by builders rather than parsed from bytes. */
public final class BufferedModels {
    private BufferedModels() { }

    public static final class Method implements MethodModel {
        private final Utf8Entry name;
        private final Utf8Entry type;
        private final List<MethodElement> elements;

        Method(Utf8Entry name, Utf8Entry type, List<MethodElement> elements) {
            this.name = name;
            this.type = type;
            this.elements = new ArrayList<>(elements);
        }

        @Override public Optional<ClassModel> parent() { return Optional.empty(); }
        @Override public Utf8Entry methodName() { return name; }
        @Override public Utf8Entry methodType() { return type; }

        @Override
        public AccessFlags flags() {
            for (MethodElement e : elements) {
                if (e instanceof AccessFlags) {
                    return (AccessFlags) e;
                }
            }
            return new AccessFlagsImpl(AccessFlag.Location.METHOD, 0);
        }

        @Override
        public Optional<CodeModel> code() {
            for (MethodElement e : elements) {
                if (e instanceof CodeModel) {
                    return Optional.of((CodeModel) e);
                }
            }
            return Optional.empty();
        }

        @Override
        public List<Attribute<?>> attributes() {
            List<Attribute<?>> out = new ArrayList<>();
            for (MethodElement e : elements) {
                if (e instanceof Attribute<?>) {
                    out.add((Attribute<?>) e);
                }
            }
            return Collections.unmodifiableList(out);
        }

        @Override public void forEach(Consumer<? super MethodElement> consumer) { elements.forEach(consumer); }

        @Override
        public String toString() {
            return "MethodModel[methodName=" + name.stringValue() + ", methodType=" + type.stringValue()
                + ", flags=" + flags().flagsMask() + "]";
        }
    }

    public static final class Field implements FieldModel {
        private final Utf8Entry name;
        private final Utf8Entry type;
        private final List<FieldElement> elements;

        Field(Utf8Entry name, Utf8Entry type, List<FieldElement> elements) {
            this.name = name;
            this.type = type;
            this.elements = new ArrayList<>(elements);
        }

        @Override public Optional<ClassModel> parent() { return Optional.empty(); }
        @Override public Utf8Entry fieldName() { return name; }
        @Override public Utf8Entry fieldType() { return type; }

        @Override
        public AccessFlags flags() {
            for (FieldElement e : elements) {
                if (e instanceof AccessFlags) {
                    return (AccessFlags) e;
                }
            }
            return new AccessFlagsImpl(AccessFlag.Location.FIELD, 0);
        }

        @Override
        public List<Attribute<?>> attributes() {
            List<Attribute<?>> out = new ArrayList<>();
            for (FieldElement e : elements) {
                if (e instanceof Attribute<?>) {
                    out.add((Attribute<?>) e);
                }
            }
            return Collections.unmodifiableList(out);
        }

        @Override public void forEach(Consumer<? super FieldElement> consumer) { elements.forEach(consumer); }

        @Override
        public String toString() {
            return "FieldModel[fieldName=" + name.stringValue() + ", fieldType=" + type.stringValue()
                + ", flags=" + flags().flagsMask() + "]";
        }
    }
}
