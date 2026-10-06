/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.source.doctree;

public interface DocTreeVisitor<R, P> {
  R visitAttribute(com.sun.source.doctree.AttributeTree attributeTree, P p);
  R visitAuthor(com.sun.source.doctree.AuthorTree authorTree, P p);
  R visitComment(com.sun.source.doctree.CommentTree commentTree, P p);
  R visitDeprecated(com.sun.source.doctree.DeprecatedTree deprecatedTree, P p);
  R visitDocComment(com.sun.source.doctree.DocCommentTree docCommentTree, P p);
  R visitDocRoot(com.sun.source.doctree.DocRootTree docRootTree, P p);
  default R visitDocType(com.sun.source.doctree.DocTypeTree docTypeTree, P p) {
    return visitOther(docTypeTree, p);
  }
  R visitEndElement(com.sun.source.doctree.EndElementTree endElementTree, P p);
  R visitEntity(com.sun.source.doctree.EntityTree entityTree, P p);
  R visitErroneous(com.sun.source.doctree.ErroneousTree erroneousTree, P p);
  default R visitEscape(com.sun.source.doctree.EscapeTree escapeTree, P p) {
    return visitOther(escapeTree, p);
  }
  default R visitHidden(com.sun.source.doctree.HiddenTree hiddenTree, P p) {
    return visitOther(hiddenTree, p);
  }
  R visitIdentifier(com.sun.source.doctree.IdentifierTree identifierTree, P p);
  default R visitIndex(com.sun.source.doctree.IndexTree indexTree, P p) {
    return visitOther(indexTree, p);
  }
  R visitInheritDoc(com.sun.source.doctree.InheritDocTree inheritDocTree, P p);
  R visitLink(com.sun.source.doctree.LinkTree linkTree, P p);
  R visitLiteral(com.sun.source.doctree.LiteralTree literalTree, P p);
  R visitOther(com.sun.source.doctree.DocTree docTree, P p);
  R visitParam(com.sun.source.doctree.ParamTree paramTree, P p);
  default R visitProvides(com.sun.source.doctree.ProvidesTree providesTree, P p) {
    return visitOther(providesTree, p);
  }
  default R visitRawText(com.sun.source.doctree.RawTextTree rawTextTree, P p) {
    return visitOther(rawTextTree, p);
  }
  R visitReference(com.sun.source.doctree.ReferenceTree referenceTree, P p);
  R visitReturn(com.sun.source.doctree.ReturnTree returnTree, P p);
  R visitSee(com.sun.source.doctree.SeeTree seeTree, P p);
  R visitSerial(com.sun.source.doctree.SerialTree serialTree, P p);
  R visitSerialData(com.sun.source.doctree.SerialDataTree serialDataTree, P p);
  R visitSerialField(com.sun.source.doctree.SerialFieldTree serialFieldTree, P p);
  R visitSince(com.sun.source.doctree.SinceTree sinceTree, P p);
  default R visitSnippet(com.sun.source.doctree.SnippetTree snippetTree, P p) {
    return visitOther(snippetTree, p);
  }
  default R visitSpec(com.sun.source.doctree.SpecTree specTree, P p) {
    return visitOther(specTree, p);
  }
  R visitStartElement(com.sun.source.doctree.StartElementTree startElementTree, P p);
  default R visitSummary(com.sun.source.doctree.SummaryTree summaryTree, P p) {
    return visitOther(summaryTree, p);
  }
  default R visitSystemProperty(com.sun.source.doctree.SystemPropertyTree systemPropertyTree, P p) {
    return visitOther(systemPropertyTree, p);
  }
  R visitText(com.sun.source.doctree.TextTree textTree, P p);
  R visitThrows(com.sun.source.doctree.ThrowsTree throwsTree, P p);
  R visitUnknownBlockTag(com.sun.source.doctree.UnknownBlockTagTree unknownBlockTagTree, P p);
  R visitUnknownInlineTag(com.sun.source.doctree.UnknownInlineTagTree unknownInlineTagTree, P p);
  default R visitUses(com.sun.source.doctree.UsesTree usesTree, P p) {
    return visitOther(usesTree, p);
  }
  R visitValue(com.sun.source.doctree.ValueTree valueTree, P p);
  R visitVersion(com.sun.source.doctree.VersionTree versionTree, P p);
}
