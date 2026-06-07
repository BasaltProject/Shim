/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.annotation.processing;

public interface Processor {
  java.lang.Iterable<? extends javax.annotation.processing.Completion> getCompletions(javax.lang.model.element.Element element, javax.lang.model.element.AnnotationMirror annotationMirror, javax.lang.model.element.ExecutableElement executableElement, java.lang.String string);
  java.util.Set<java.lang.String> getSupportedAnnotationTypes();
  java.util.Set<java.lang.String> getSupportedOptions();
  javax.lang.model.SourceVersion getSupportedSourceVersion();
  void init(javax.annotation.processing.ProcessingEnvironment processingEnvironment);
  boolean process(java.util.Set<? extends javax.lang.model.element.TypeElement> set, javax.annotation.processing.RoundEnvironment roundEnvironment);
}
