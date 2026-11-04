/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.source.util;

public interface DocTreeFactory {
  com.sun.source.util.DocTreeFactory at(int i);
  java.util.List<com.sun.source.doctree.DocTree> getFirstSentence(java.util.List<? extends com.sun.source.doctree.DocTree> list);
  com.sun.source.doctree.AttributeTree newAttributeTree(javax.lang.model.element.Name name, com.sun.source.doctree.AttributeTree.ValueKind valueKind, java.util.List<? extends com.sun.source.doctree.DocTree> list);
  com.sun.source.doctree.AuthorTree newAuthorTree(java.util.List<? extends com.sun.source.doctree.DocTree> list);
  com.sun.source.doctree.LiteralTree newCodeTree(com.sun.source.doctree.TextTree textTree);
  com.sun.source.doctree.CommentTree newCommentTree(java.lang.String string);
  com.sun.source.doctree.DeprecatedTree newDeprecatedTree(java.util.List<? extends com.sun.source.doctree.DocTree> list);
  com.sun.source.doctree.DocCommentTree newDocCommentTree(java.util.List<? extends com.sun.source.doctree.DocTree> list, java.util.List<? extends com.sun.source.doctree.DocTree> list2, java.util.List<? extends com.sun.source.doctree.DocTree> list3, java.util.List<? extends com.sun.source.doctree.DocTree> list4);
  com.sun.source.doctree.DocCommentTree newDocCommentTree(java.util.List<? extends com.sun.source.doctree.DocTree> list, java.util.List<? extends com.sun.source.doctree.DocTree> list2);
  com.sun.source.doctree.DocRootTree newDocRootTree();
  com.sun.source.doctree.DocTypeTree newDocTypeTree(java.lang.String string);
  com.sun.source.doctree.EndElementTree newEndElementTree(javax.lang.model.element.Name name);
  com.sun.source.doctree.EntityTree newEntityTree(javax.lang.model.element.Name name);
  com.sun.source.doctree.ErroneousTree newErroneousTree(java.lang.String string, javax.tools.Diagnostic<javax.tools.JavaFileObject> diagnostic);
  com.sun.source.doctree.EscapeTree newEscapeTree(char c);
  com.sun.source.doctree.ThrowsTree newExceptionTree(com.sun.source.doctree.ReferenceTree referenceTree, java.util.List<? extends com.sun.source.doctree.DocTree> list);
  com.sun.source.doctree.HiddenTree newHiddenTree(java.util.List<? extends com.sun.source.doctree.DocTree> list);
  com.sun.source.doctree.IdentifierTree newIdentifierTree(javax.lang.model.element.Name name);
  com.sun.source.doctree.IndexTree newIndexTree(com.sun.source.doctree.DocTree docTree, java.util.List<? extends com.sun.source.doctree.DocTree> list);
  com.sun.source.doctree.InheritDocTree newInheritDocTree();
  default com.sun.source.doctree.InheritDocTree newInheritDocTree(com.sun.source.doctree.ReferenceTree referenceTree) {
    throw new UnsupportedOperationException();
  }
  com.sun.source.doctree.LinkTree newLinkPlainTree(com.sun.source.doctree.ReferenceTree referenceTree, java.util.List<? extends com.sun.source.doctree.DocTree> list);
  com.sun.source.doctree.LinkTree newLinkTree(com.sun.source.doctree.ReferenceTree referenceTree, java.util.List<? extends com.sun.source.doctree.DocTree> list);
  com.sun.source.doctree.LiteralTree newLiteralTree(com.sun.source.doctree.TextTree textTree);
  com.sun.source.doctree.ParamTree newParamTree(boolean flag, com.sun.source.doctree.IdentifierTree identifierTree, java.util.List<? extends com.sun.source.doctree.DocTree> list);
  com.sun.source.doctree.ProvidesTree newProvidesTree(com.sun.source.doctree.ReferenceTree referenceTree, java.util.List<? extends com.sun.source.doctree.DocTree> list);
  com.sun.source.doctree.RawTextTree newRawTextTree(com.sun.source.doctree.DocTree.Kind kind, java.lang.String string);
  com.sun.source.doctree.ReferenceTree newReferenceTree(java.lang.String string);
  default com.sun.source.doctree.ReturnTree newReturnTree(boolean flag, java.util.List<? extends com.sun.source.doctree.DocTree> list) {
    throw new UnsupportedOperationException();
  }
  com.sun.source.doctree.ReturnTree newReturnTree(java.util.List<? extends com.sun.source.doctree.DocTree> list);
  com.sun.source.doctree.SeeTree newSeeTree(java.util.List<? extends com.sun.source.doctree.DocTree> list);
  com.sun.source.doctree.SerialDataTree newSerialDataTree(java.util.List<? extends com.sun.source.doctree.DocTree> list);
  com.sun.source.doctree.SerialFieldTree newSerialFieldTree(com.sun.source.doctree.IdentifierTree identifierTree, com.sun.source.doctree.ReferenceTree referenceTree, java.util.List<? extends com.sun.source.doctree.DocTree> list);
  com.sun.source.doctree.SerialTree newSerialTree(java.util.List<? extends com.sun.source.doctree.DocTree> list);
  com.sun.source.doctree.SinceTree newSinceTree(java.util.List<? extends com.sun.source.doctree.DocTree> list);
  com.sun.source.doctree.SnippetTree newSnippetTree(java.util.List<? extends com.sun.source.doctree.DocTree> list, com.sun.source.doctree.TextTree textTree);
  com.sun.source.doctree.SpecTree newSpecTree(com.sun.source.doctree.TextTree textTree, java.util.List<? extends com.sun.source.doctree.DocTree> list);
  com.sun.source.doctree.StartElementTree newStartElementTree(javax.lang.model.element.Name name, java.util.List<? extends com.sun.source.doctree.DocTree> list, boolean flag);
  default com.sun.source.doctree.SummaryTree newSummaryTree(java.util.List<? extends com.sun.source.doctree.DocTree> list) {
    throw new UnsupportedOperationException();
  }
  com.sun.source.doctree.SystemPropertyTree newSystemPropertyTree(javax.lang.model.element.Name name);
  com.sun.source.doctree.TextTree newTextTree(java.lang.String string);
  com.sun.source.doctree.ThrowsTree newThrowsTree(com.sun.source.doctree.ReferenceTree referenceTree, java.util.List<? extends com.sun.source.doctree.DocTree> list);
  com.sun.source.doctree.UnknownBlockTagTree newUnknownBlockTagTree(javax.lang.model.element.Name name, java.util.List<? extends com.sun.source.doctree.DocTree> list);
  com.sun.source.doctree.UnknownInlineTagTree newUnknownInlineTagTree(javax.lang.model.element.Name name, java.util.List<? extends com.sun.source.doctree.DocTree> list);
  com.sun.source.doctree.UsesTree newUsesTree(com.sun.source.doctree.ReferenceTree referenceTree, java.util.List<? extends com.sun.source.doctree.DocTree> list);
  com.sun.source.doctree.ValueTree newValueTree(com.sun.source.doctree.ReferenceTree referenceTree);
  default com.sun.source.doctree.ValueTree newValueTree(com.sun.source.doctree.TextTree textTree, com.sun.source.doctree.ReferenceTree referenceTree) {
    throw new UnsupportedOperationException();
  }
  com.sun.source.doctree.VersionTree newVersionTree(java.util.List<? extends com.sun.source.doctree.DocTree> list);
}
