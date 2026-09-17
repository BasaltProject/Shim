/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package jdk.javadoc.doclet;

public interface DocletEnvironment {
  com.sun.source.util.DocTrees getDocTrees();
  javax.lang.model.util.Elements getElementUtils();
  javax.tools.JavaFileObject.Kind getFileKind(javax.lang.model.element.TypeElement typeElement);
  java.util.Set<? extends javax.lang.model.element.Element> getIncludedElements();
  javax.tools.JavaFileManager getJavaFileManager();
  jdk.javadoc.doclet.DocletEnvironment.ModuleMode getModuleMode();
  javax.lang.model.SourceVersion getSourceVersion();
  java.util.Set<? extends javax.lang.model.element.Element> getSpecifiedElements();
  javax.lang.model.util.Types getTypeUtils();
  boolean isIncluded(javax.lang.model.element.Element element);
  boolean isSelected(javax.lang.model.element.Element element);

  public enum ModuleMode {
    API, ALL;


  }
}
