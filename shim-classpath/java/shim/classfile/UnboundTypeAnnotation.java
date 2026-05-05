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
import java.lang.classfile.TypeAnnotation;
import java.util.List;

public final class UnboundTypeAnnotation implements TypeAnnotation {
    private final TargetInfo targetInfo;
    private final List<TypePathComponent> targetPath;
    private final Annotation annotation;

    UnboundTypeAnnotation(TargetInfo targetInfo, List<TypePathComponent> targetPath, Annotation annotation) {
        this.targetInfo = targetInfo;
        this.targetPath = targetPath;
        this.annotation = annotation;
    }

    public static TypeAnnotation of(TargetInfo targetInfo, List<TypePathComponent> targetPath, Annotation annotation) {
        TempEntries.nonNull(targetInfo);
        List<TypePathComponent> path = TempEntries.copyOf(targetPath);
        return new UnboundTypeAnnotation(targetInfo, path, TempEntries.nonNull(annotation));
    }

    @Override public TargetInfo targetInfo() { return targetInfo; }
    @Override public List<TypePathComponent> targetPath() { return targetPath; }
    @Override public Annotation annotation() { return annotation; }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof UnboundTypeAnnotation)) {
            return false;
        }
        UnboundTypeAnnotation t = (UnboundTypeAnnotation) o;
        return targetInfo.equals(t.targetInfo) && targetPath.equals(t.targetPath) && annotation.equals(t.annotation);
    }

    @Override public int hashCode() { return TempEntries.hash(targetInfo, targetPath, annotation); }

    @Override
    public String toString() {
        return "UnboundTypeAnnotation[targetInfo=" + targetInfo + ", targetPath=" + targetPath + ", annotation="
            + annotation + "]";
    }
}
