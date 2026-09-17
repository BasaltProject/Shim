/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package jdk.javadoc.doclet;

public interface Taglet {
  java.util.Set<jdk.javadoc.doclet.Taglet.Location> getAllowedLocations();
  java.lang.String getName();
  default void init(jdk.javadoc.doclet.DocletEnvironment docletEnvironment, jdk.javadoc.doclet.Doclet doclet) {
    throw new UnsupportedOperationException();
  }
  default boolean isBlockTag() {
    throw new UnsupportedOperationException();
  }
  boolean isInlineTag();
  java.lang.String toString(java.util.List<? extends com.sun.source.doctree.DocTree> list, javax.lang.model.element.Element element);

  public enum Location {
    OVERVIEW, MODULE, PACKAGE, TYPE, CONSTRUCTOR, METHOD, FIELD;


  }
}
