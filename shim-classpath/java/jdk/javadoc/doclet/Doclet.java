/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package jdk.javadoc.doclet;

public interface Doclet {
  java.lang.String getName();
  java.util.Set<? extends jdk.javadoc.doclet.Doclet.Option> getSupportedOptions();
  javax.lang.model.SourceVersion getSupportedSourceVersion();
  void init(java.util.Locale locale, jdk.javadoc.doclet.Reporter reporter);
  boolean run(jdk.javadoc.doclet.DocletEnvironment docletEnvironment);

  public interface Option {
    int getArgumentCount();
    java.lang.String getDescription();
    jdk.javadoc.doclet.Doclet.Option.Kind getKind();
    java.util.List<java.lang.String> getNames();
    java.lang.String getParameters();
    boolean process(java.lang.String string, java.util.List<java.lang.String> list);

    public enum Kind {
      EXTENDED, STANDARD, OTHER;


    }
  }
}
