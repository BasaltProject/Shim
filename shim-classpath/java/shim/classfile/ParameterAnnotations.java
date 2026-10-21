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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

final class ParameterAnnotations {
    private ParameterAnnotations() { }

    static List<List<Annotation>> copy(List<List<Annotation>> params) {
        if (params == null) {
            throw new NullPointerException("Cannot invoke \"java.util.List.toArray()\" because \"elements\" is null");
        }
        List<List<Annotation>> out = new ArrayList<>(params.size());
        for (List<Annotation> p : params) {
            out.add(TempEntries.copyOf(p));
        }
        return Collections.unmodifiableList(out);
    }

    static List<List<Annotation>> read(ClassReaderImpl r, int pos) {
        AnnotationIO in = new AnnotationIO(r, pos);
        int n = in.u1();
        List<List<Annotation>> out = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            out.add(in.annotations());
        }
        return Collections.unmodifiableList(out);
    }

    static void write(BufWriterImpl out, List<List<Annotation>> params) {
        out.writeU1(params.size());
        for (List<Annotation> p : params) {
            AnnotationIO.writeAnnotations(out, p);
        }
    }
}
