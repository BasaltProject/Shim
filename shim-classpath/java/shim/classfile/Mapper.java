/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.classfile;

import java.lang.classfile.Attribute;
import java.lang.classfile.AttributeMapper;
import java.lang.classfile.AttributedElement;
import java.lang.classfile.BufWriter;
import java.lang.classfile.ClassReader;

/** A standard attribute mapper built from a read function and a write function. */
public final class Mapper<A extends Attribute<A>> implements AttributeMapper<A> {
    @FunctionalInterface
    public interface Reader<A> {
        A read(AttributedElement owner, ClassReaderImpl cf, int pos);
    }

    @FunctionalInterface
    public interface Writer<A> {
        void write(BufWriterImpl out, A attribute);
    }

    private final String name;
    private final AttributeStability stability;
    private final boolean allowMultiple;
    private final Reader<A> reader;
    private final Writer<A> writer;

    public Mapper(String name, AttributeStability stability, boolean allowMultiple, Reader<A> reader,
                  Writer<A> writer) {
        this.name = name;
        this.stability = stability;
        this.allowMultiple = allowMultiple;
        this.reader = reader;
        this.writer = writer;
    }

    public Mapper(String name, AttributeStability stability, Reader<A> reader, Writer<A> writer) {
        this(name, stability, false, reader, writer);
    }

    @Override
    public String toString() {
        return "AttributeMapper[name=" + name + ", allowMultiple=" + allowMultiple + ", stability=" + stability + "]";
    }

    @Override public String name() { return name; }
    @Override public AttributeStability stability() { return stability; }
    @Override public boolean allowMultiple() { return allowMultiple; }

    @Override
    public A readAttribute(AttributedElement owner, ClassReader cf, int pos) {
        return reader.read(owner, (ClassReaderImpl) cf, pos);
    }

    @Override
    public void writeAttribute(BufWriter out, A attribute) {
        writer.write((BufWriterImpl) out, attribute);
    }
}
