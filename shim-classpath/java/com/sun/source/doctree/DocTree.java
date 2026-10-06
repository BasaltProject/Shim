/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.source.doctree;

public interface DocTree {
  <R, D> R accept(com.sun.source.doctree.DocTreeVisitor<R, D> docTreeVisitor, D d);
  com.sun.source.doctree.DocTree.Kind getKind();

  public enum Kind {
    ATTRIBUTE, AUTHOR("author"), CODE("code"), COMMENT, DEPRECATED("deprecated"), DOC_COMMENT, DOC_ROOT("docRoot"),
    DOC_TYPE, END_ELEMENT, ENTITY, ERRONEOUS, ESCAPE, EXCEPTION("exception"), HIDDEN("hidden"), IDENTIFIER,
    INDEX("index"), INHERIT_DOC("inheritDoc"), LINK("link"), LINK_PLAIN("linkplain"), LITERAL("literal"), MARKDOWN,
    PARAM("param"), PROVIDES("provides"), REFERENCE, RETURN("return"), SEE("see"), SERIAL("serial"),
    SERIAL_DATA("serialData"), SERIAL_FIELD("serialField"), SINCE("since"), SNIPPET("snippet"), SPEC("spec"),
    START_ELEMENT, SYSTEM_PROPERTY("systemProperty"), SUMMARY("summary"), TEXT, THROWS("throws"), UNKNOWN_BLOCK_TAG,
    UNKNOWN_INLINE_TAG, USES("uses"), VALUE("value"), VERSION("version"), OTHER;

    public final java.lang.String tagName;

    Kind() {
      tagName = null;
    }

    Kind(java.lang.String tagName) {
      this.tagName = tagName;
    }
  }
}
