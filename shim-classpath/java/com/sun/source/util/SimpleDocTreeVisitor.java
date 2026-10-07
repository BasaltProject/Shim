/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.source.util;

public class SimpleDocTreeVisitor<R, P> implements com.sun.source.doctree.DocTreeVisitor<R, P> {
  protected final R DEFAULT_VALUE;

  protected SimpleDocTreeVisitor() {
    DEFAULT_VALUE = null;
  }
  protected SimpleDocTreeVisitor(R r) {
    DEFAULT_VALUE = r;
  }

  protected R defaultAction(com.sun.source.doctree.DocTree docTree, P p) {
    return DEFAULT_VALUE;
  }
  public final R visit(com.sun.source.doctree.DocTree docTree, P p) {
    return docTree == null ? null : docTree.accept(this, p);
  }
  public final R visit(java.lang.Iterable<? extends com.sun.source.doctree.DocTree> iterable, P p) {
    R r = null;
    if (iterable != null) {
      for (com.sun.source.doctree.DocTree node : iterable) {
        r = visit(node, p);
      }
    }
    return r;
  }
  public R visitAttribute(com.sun.source.doctree.AttributeTree attributeTree, P p) {
    return defaultAction(attributeTree, p);
  }
  public R visitAuthor(com.sun.source.doctree.AuthorTree authorTree, P p) {
    return defaultAction(authorTree, p);
  }
  public R visitComment(com.sun.source.doctree.CommentTree commentTree, P p) {
    return defaultAction(commentTree, p);
  }
  public R visitDeprecated(com.sun.source.doctree.DeprecatedTree deprecatedTree, P p) {
    return defaultAction(deprecatedTree, p);
  }
  public R visitDocComment(com.sun.source.doctree.DocCommentTree docCommentTree, P p) {
    return defaultAction(docCommentTree, p);
  }
  public R visitDocRoot(com.sun.source.doctree.DocRootTree docRootTree, P p) {
    return defaultAction(docRootTree, p);
  }
  public R visitDocType(com.sun.source.doctree.DocTypeTree docTypeTree, P p) {
    return defaultAction(docTypeTree, p);
  }
  public R visitEndElement(com.sun.source.doctree.EndElementTree endElementTree, P p) {
    return defaultAction(endElementTree, p);
  }
  public R visitEntity(com.sun.source.doctree.EntityTree entityTree, P p) {
    return defaultAction(entityTree, p);
  }
  public R visitErroneous(com.sun.source.doctree.ErroneousTree erroneousTree, P p) {
    return defaultAction(erroneousTree, p);
  }
  public R visitEscape(com.sun.source.doctree.EscapeTree escapeTree, P p) {
    return defaultAction(escapeTree, p);
  }
  public R visitHidden(com.sun.source.doctree.HiddenTree hiddenTree, P p) {
    return defaultAction(hiddenTree, p);
  }
  public R visitIdentifier(com.sun.source.doctree.IdentifierTree identifierTree, P p) {
    return defaultAction(identifierTree, p);
  }
  public R visitIndex(com.sun.source.doctree.IndexTree indexTree, P p) {
    return defaultAction(indexTree, p);
  }
  public R visitInheritDoc(com.sun.source.doctree.InheritDocTree inheritDocTree, P p) {
    return defaultAction(inheritDocTree, p);
  }
  public R visitLink(com.sun.source.doctree.LinkTree linkTree, P p) {
    return defaultAction(linkTree, p);
  }
  public R visitLiteral(com.sun.source.doctree.LiteralTree literalTree, P p) {
    return defaultAction(literalTree, p);
  }
  public R visitOther(com.sun.source.doctree.DocTree docTree, P p) {
    return defaultAction(docTree, p);
  }
  public R visitParam(com.sun.source.doctree.ParamTree paramTree, P p) {
    return defaultAction(paramTree, p);
  }
  public R visitProvides(com.sun.source.doctree.ProvidesTree providesTree, P p) {
    return defaultAction(providesTree, p);
  }
  public R visitRawText(com.sun.source.doctree.RawTextTree rawTextTree, P p) {
    return defaultAction(rawTextTree, p);
  }
  public R visitReference(com.sun.source.doctree.ReferenceTree referenceTree, P p) {
    return defaultAction(referenceTree, p);
  }
  public R visitReturn(com.sun.source.doctree.ReturnTree returnTree, P p) {
    return defaultAction(returnTree, p);
  }
  public R visitSee(com.sun.source.doctree.SeeTree seeTree, P p) {
    return defaultAction(seeTree, p);
  }
  public R visitSerial(com.sun.source.doctree.SerialTree serialTree, P p) {
    return defaultAction(serialTree, p);
  }
  public R visitSerialData(com.sun.source.doctree.SerialDataTree serialDataTree, P p) {
    return defaultAction(serialDataTree, p);
  }
  public R visitSerialField(com.sun.source.doctree.SerialFieldTree serialFieldTree, P p) {
    return defaultAction(serialFieldTree, p);
  }
  public R visitSince(com.sun.source.doctree.SinceTree sinceTree, P p) {
    return defaultAction(sinceTree, p);
  }
  public R visitSnippet(com.sun.source.doctree.SnippetTree snippetTree, P p) {
    return defaultAction(snippetTree, p);
  }
  public R visitSpec(com.sun.source.doctree.SpecTree specTree, P p) {
    return defaultAction(specTree, p);
  }
  public R visitStartElement(com.sun.source.doctree.StartElementTree startElementTree, P p) {
    return defaultAction(startElementTree, p);
  }
  public R visitSummary(com.sun.source.doctree.SummaryTree summaryTree, P p) {
    return defaultAction(summaryTree, p);
  }
  public R visitSystemProperty(com.sun.source.doctree.SystemPropertyTree systemPropertyTree, P p) {
    return defaultAction(systemPropertyTree, p);
  }
  public R visitText(com.sun.source.doctree.TextTree textTree, P p) {
    return defaultAction(textTree, p);
  }
  public R visitThrows(com.sun.source.doctree.ThrowsTree throwsTree, P p) {
    return defaultAction(throwsTree, p);
  }
  public R visitUnknownBlockTag(com.sun.source.doctree.UnknownBlockTagTree unknownBlockTagTree, P p) {
    return defaultAction(unknownBlockTagTree, p);
  }
  public R visitUnknownInlineTag(com.sun.source.doctree.UnknownInlineTagTree unknownInlineTagTree, P p) {
    return defaultAction(unknownInlineTagTree, p);
  }
  public R visitUses(com.sun.source.doctree.UsesTree usesTree, P p) {
    return defaultAction(usesTree, p);
  }
  public R visitValue(com.sun.source.doctree.ValueTree valueTree, P p) {
    return defaultAction(valueTree, p);
  }
  public R visitVersion(com.sun.source.doctree.VersionTree versionTree, P p) {
    return defaultAction(versionTree, p);
  }
}
