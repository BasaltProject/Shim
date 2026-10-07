/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.source.util;

public class DocTreeScanner<R, P> implements com.sun.source.doctree.DocTreeVisitor<R, P> {
  public DocTreeScanner() {
  }

  public R reduce(R r, R r2) {
    return r;
  }
  public R scan(com.sun.source.doctree.DocTree docTree, P p) {
    return docTree == null ? null : docTree.accept(this, p);
  }
  public R scan(java.lang.Iterable<? extends com.sun.source.doctree.DocTree> iterable, P p) {
    R r = null;
    if (iterable != null) {
      boolean first = true;
      for (com.sun.source.doctree.DocTree node : iterable) {
        r = first ? scan(node, p) : scanAndReduce(node, p, r);
        first = false;
      }
    }
    return r;
  }
  public R visitAttribute(com.sun.source.doctree.AttributeTree attributeTree, P p) {
    return scan(attributeTree.getValue(), p);
  }
  public R visitAuthor(com.sun.source.doctree.AuthorTree authorTree, P p) {
    return scan(authorTree.getName(), p);
  }
  public R visitComment(com.sun.source.doctree.CommentTree commentTree, P p) {
    return null;
  }
  public R visitDeprecated(com.sun.source.doctree.DeprecatedTree deprecatedTree, P p) {
    return scan(deprecatedTree.getBody(), p);
  }
  public R visitDocComment(com.sun.source.doctree.DocCommentTree docCommentTree, P p) {
    R r = scan(docCommentTree.getFirstSentence(), p);
    r = scanAndReduce(docCommentTree.getBody(), p, r);
    r = scanAndReduce(docCommentTree.getBlockTags(), p, r);
    return r;
  }
  public R visitDocRoot(com.sun.source.doctree.DocRootTree docRootTree, P p) {
    return null;
  }
  public R visitDocType(com.sun.source.doctree.DocTypeTree docTypeTree, P p) {
    return null;
  }
  public R visitEndElement(com.sun.source.doctree.EndElementTree endElementTree, P p) {
    return null;
  }
  public R visitEntity(com.sun.source.doctree.EntityTree entityTree, P p) {
    return null;
  }
  public R visitErroneous(com.sun.source.doctree.ErroneousTree erroneousTree, P p) {
    return null;
  }
  public R visitEscape(com.sun.source.doctree.EscapeTree escapeTree, P p) {
    return null;
  }
  public R visitHidden(com.sun.source.doctree.HiddenTree hiddenTree, P p) {
    return scan(hiddenTree.getBody(), p);
  }
  public R visitIdentifier(com.sun.source.doctree.IdentifierTree identifierTree, P p) {
    return null;
  }
  public R visitIndex(com.sun.source.doctree.IndexTree indexTree, P p) {
    R r = scan(indexTree.getSearchTerm(), p);
    r = scanAndReduce(indexTree.getDescription(), p, r);
    return r;
  }
  public R visitInheritDoc(com.sun.source.doctree.InheritDocTree inheritDocTree, P p) {
    return null;
  }
  public R visitLink(com.sun.source.doctree.LinkTree linkTree, P p) {
    R r = scan(linkTree.getReference(), p);
    r = scanAndReduce(linkTree.getLabel(), p, r);
    return r;
  }
  public R visitLiteral(com.sun.source.doctree.LiteralTree literalTree, P p) {
    return scan(literalTree.getBody(), p);
  }
  public R visitOther(com.sun.source.doctree.DocTree docTree, P p) {
    return null;
  }
  public R visitParam(com.sun.source.doctree.ParamTree paramTree, P p) {
    R r = scan(paramTree.getName(), p);
    r = scanAndReduce(paramTree.getDescription(), p, r);
    return r;
  }
  public R visitProvides(com.sun.source.doctree.ProvidesTree providesTree, P p) {
    R r = scan(providesTree.getServiceType(), p);
    r = scanAndReduce(providesTree.getDescription(), p, r);
    return r;
  }
  public R visitRawText(com.sun.source.doctree.RawTextTree rawTextTree, P p) {
    return null;
  }
  public R visitReference(com.sun.source.doctree.ReferenceTree referenceTree, P p) {
    return null;
  }
  public R visitReturn(com.sun.source.doctree.ReturnTree returnTree, P p) {
    return scan(returnTree.getDescription(), p);
  }
  public R visitSee(com.sun.source.doctree.SeeTree seeTree, P p) {
    return scan(seeTree.getReference(), p);
  }
  public R visitSerial(com.sun.source.doctree.SerialTree serialTree, P p) {
    return scan(serialTree.getDescription(), p);
  }
  public R visitSerialData(com.sun.source.doctree.SerialDataTree serialDataTree, P p) {
    return scan(serialDataTree.getDescription(), p);
  }
  public R visitSerialField(com.sun.source.doctree.SerialFieldTree serialFieldTree, P p) {
    R r = scan(serialFieldTree.getName(), p);
    r = scanAndReduce(serialFieldTree.getType(), p, r);
    r = scanAndReduce(serialFieldTree.getDescription(), p, r);
    return r;
  }
  public R visitSince(com.sun.source.doctree.SinceTree sinceTree, P p) {
    return scan(sinceTree.getBody(), p);
  }
  public R visitSnippet(com.sun.source.doctree.SnippetTree snippetTree, P p) {
    R r = scan(snippetTree.getAttributes(), p);
    r = scanAndReduce(snippetTree.getBody(), p, r);
    return r;
  }
  public R visitSpec(com.sun.source.doctree.SpecTree specTree, P p) {
    R r = scan(specTree.getURL(), p);
    r = scanAndReduce(specTree.getTitle(), p, r);
    return r;
  }
  public R visitStartElement(com.sun.source.doctree.StartElementTree startElementTree, P p) {
    return scan(startElementTree.getAttributes(), p);
  }
  public R visitSummary(com.sun.source.doctree.SummaryTree summaryTree, P p) {
    return scan(summaryTree.getSummary(), p);
  }
  public R visitSystemProperty(com.sun.source.doctree.SystemPropertyTree systemPropertyTree, P p) {
    return null;
  }
  public R visitText(com.sun.source.doctree.TextTree textTree, P p) {
    return null;
  }
  public R visitThrows(com.sun.source.doctree.ThrowsTree throwsTree, P p) {
    R r = scan(throwsTree.getExceptionName(), p);
    r = scanAndReduce(throwsTree.getDescription(), p, r);
    return r;
  }
  public R visitUnknownBlockTag(com.sun.source.doctree.UnknownBlockTagTree unknownBlockTagTree, P p) {
    return scan(unknownBlockTagTree.getContent(), p);
  }
  public R visitUnknownInlineTag(com.sun.source.doctree.UnknownInlineTagTree unknownInlineTagTree, P p) {
    return scan(unknownInlineTagTree.getContent(), p);
  }
  public R visitUses(com.sun.source.doctree.UsesTree usesTree, P p) {
    R r = scan(usesTree.getServiceType(), p);
    r = scanAndReduce(usesTree.getDescription(), p, r);
    return r;
  }
  public R visitValue(com.sun.source.doctree.ValueTree valueTree, P p) {
    return scan(valueTree.getReference(), p);
  }
  public R visitVersion(com.sun.source.doctree.VersionTree versionTree, P p) {
    return scan(versionTree.getBody(), p);
  }

  private R scanAndReduce(com.sun.source.doctree.DocTree node, P p, R r) {
    return reduce(scan(node, p), r);
  }

  private R scanAndReduce(java.lang.Iterable<? extends com.sun.source.doctree.DocTree> nodes, P p, R r) {
    return reduce(scan(nodes, p), r);
  }
}
