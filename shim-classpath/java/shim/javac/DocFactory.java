/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.javac;

import com.sun.source.doctree.*;
import com.sun.source.util.DocTreeFactory;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.lang.model.element.Name;

/** Creates doc tree nodes for the DocTrees API. */
final class DocFactory implements DocTreeFactory {
  private static List<DocTree> copy(List<? extends DocTree> l) {
    return l == null ? new ArrayList<DocTree>() : new ArrayList<DocTree>(l);
  }

  private static DocParser.Ref ref(ReferenceTree r) {
    return r instanceof DocParser.Ref ? (DocParser.Ref) r : new DocParser.Ref(r.getSignature());
  }

  private static DocParser.Text text(TextTree t) {
    return t instanceof DocParser.Text ? (DocParser.Text) t : new DocParser.Text(t.getBody());
  }

  public DocTreeFactory at(int pos) {
    return this;
  }

  public List<DocTree> getFirstSentence(List<? extends DocTree> list) {
    return DocParser.firstSentence(copy(list));
  }

  public AttributeTree newAttributeTree(Name name, AttributeTree.ValueKind vk, List<? extends DocTree> v) {
    return new DocParser.Attr(name.toString(), vk, copy(v));
  }

  public AuthorTree newAuthorTree(List<? extends DocTree> l) {
    return new DocParser.Simple(DocTree.Kind.AUTHOR, "author", copy(l));
  }

  public LiteralTree newCodeTree(TextTree t) {
    return new DocParser.Lit(DocTree.Kind.CODE, text(t));
  }

  public CommentTree newCommentTree(String s) {
    return new DocParser.Comment(s);
  }

  public DeprecatedTree newDeprecatedTree(List<? extends DocTree> l) {
    return new DocParser.Simple(DocTree.Kind.DEPRECATED, "deprecated", copy(l));
  }

  public DocCommentTree newDocCommentTree(List<? extends DocTree> fullBody, List<? extends DocTree> tags) {
    return newDocCommentTree(fullBody, tags, Collections.<DocTree>emptyList(), Collections.<DocTree>emptyList());
  }

  public DocCommentTree newDocCommentTree(List<? extends DocTree> fullBody, List<? extends DocTree> tags,
      List<? extends DocTree> preamble, List<? extends DocTree> postamble) {
    List<DocTree> body = copy(fullBody);
    DocParser.Doc d = new DocParser.Doc(DocParser.firstSentence(new ArrayList<DocTree>(body)), body, copy(tags));
    d.preamble = copy(preamble);
    d.postamble = copy(postamble);
    return d;
  }

  public DocRootTree newDocRootTree() {
    return new DocParser.DocRoot();
  }

  public DocTypeTree newDocTypeTree(String s) {
    return new DocType(s);
  }

  public EndElementTree newEndElementTree(Name name) {
    return new DocParser.End(name.toString());
  }

  public EntityTree newEntityTree(Name name) {
    return new DocParser.Entity(name.toString());
  }

  public ErroneousTree newErroneousTree(String s, javax.tools.Diagnostic<javax.tools.JavaFileObject> d) {
    return new Erroneous(s, d);
  }

  public EscapeTree newEscapeTree(char c) {
    return new Escape(c);
  }

  public ThrowsTree newExceptionTree(ReferenceTree r, List<? extends DocTree> l) {
    return new DocParser.Thr(DocTree.Kind.EXCEPTION, ref(r), copy(l));
  }

  public HiddenTree newHiddenTree(List<? extends DocTree> l) {
    return new Hidden(copy(l));
  }

  public IdentifierTree newIdentifierTree(Name name) {
    return new DocParser.Ident(name.toString());
  }

  public IndexTree newIndexTree(DocTree term, List<? extends DocTree> l) {
    return new Index(term, copy(l));
  }

  public InheritDocTree newInheritDocTree() {
    return new DocParser.Inherit();
  }

  public InheritDocTree newInheritDocTree(ReferenceTree r) {
    return new DocParser.Inherit();
  }

  public LinkTree newLinkPlainTree(ReferenceTree r, List<? extends DocTree> l) {
    return new DocParser.Link(DocTree.Kind.LINK_PLAIN, ref(r), copy(l));
  }

  public LinkTree newLinkTree(ReferenceTree r, List<? extends DocTree> l) {
    return new DocParser.Link(DocTree.Kind.LINK, ref(r), copy(l));
  }

  public LiteralTree newLiteralTree(TextTree t) {
    return new DocParser.Lit(DocTree.Kind.LITERAL, text(t));
  }

  public ParamTree newParamTree(boolean typeParam, IdentifierTree name, List<? extends DocTree> l) {
    DocParser.Ident id = name instanceof DocParser.Ident ? (DocParser.Ident) name
        : new DocParser.Ident(name.getName().toString());
    return new DocParser.Param(id, typeParam, copy(l));
  }

  public ProvidesTree newProvidesTree(ReferenceTree r, List<? extends DocTree> l) {
    return new Service(DocTree.Kind.PROVIDES, r, copy(l));
  }

  public RawTextTree newRawTextTree(DocTree.Kind kind, String s) {
    return new RawText(kind, s);
  }

  public ReferenceTree newReferenceTree(String s) {
    return new DocParser.Ref(s);
  }

  public ReturnTree newReturnTree(boolean inline, List<? extends DocTree> l) {
    return new DocParser.Ret(copy(l));
  }

  public ReturnTree newReturnTree(List<? extends DocTree> l) {
    return new DocParser.Ret(copy(l));
  }

  public SeeTree newSeeTree(List<? extends DocTree> l) {
    return new DocParser.See(copy(l));
  }

  public SerialDataTree newSerialDataTree(List<? extends DocTree> l) {
    return new Serial(DocTree.Kind.SERIAL_DATA, copy(l));
  }

  public SerialFieldTree newSerialFieldTree(IdentifierTree name, ReferenceTree type, List<? extends DocTree> l) {
    return new SerialField(name, type, copy(l));
  }

  public SerialTree newSerialTree(List<? extends DocTree> l) {
    return new Serial(DocTree.Kind.SERIAL, copy(l));
  }

  public SinceTree newSinceTree(List<? extends DocTree> l) {
    return new DocParser.Simple(DocTree.Kind.SINCE, "since", copy(l));
  }

  public SnippetTree newSnippetTree(List<? extends DocTree> attrs, TextTree body) {
    return new Snippet(copy(attrs), body);
  }

  public SpecTree newSpecTree(TextTree url, List<? extends DocTree> title) {
    return new Spec(url, copy(title));
  }

  public StartElementTree newStartElementTree(Name name, List<? extends DocTree> attrs, boolean selfClosing) {
    return new DocParser.Start(name.toString(), copy(attrs), selfClosing);
  }

  public SummaryTree newSummaryTree(List<? extends DocTree> l) {
    return new DocParser.Summary(copy(l));
  }

  public SystemPropertyTree newSystemPropertyTree(Name name) {
    return new SystemProperty(name);
  }

  public TextTree newTextTree(String s) {
    return new DocParser.Text(s);
  }

  public ThrowsTree newThrowsTree(ReferenceTree r, List<? extends DocTree> l) {
    return new DocParser.Thr(DocTree.Kind.THROWS, ref(r), copy(l));
  }

  public UnknownBlockTagTree newUnknownBlockTagTree(Name name, List<? extends DocTree> l) {
    return new DocParser.UnkBlock(name.toString(), copy(l));
  }

  public UnknownInlineTagTree newUnknownInlineTagTree(Name name, List<? extends DocTree> l) {
    return new DocParser.UnkInline(name.toString(), copy(l));
  }

  public UsesTree newUsesTree(ReferenceTree r, List<? extends DocTree> l) {
    return new Service(DocTree.Kind.USES, r, copy(l));
  }

  public ValueTree newValueTree(ReferenceTree r) {
    return new DocParser.Value(ref(r));
  }

  public ValueTree newValueTree(TextTree format, ReferenceTree r) {
    return new DocParser.Value(ref(r));
  }

  public VersionTree newVersionTree(List<? extends DocTree> l) {
    return new DocParser.Simple(DocTree.Kind.VERSION, "version", copy(l));
  }

  private static String join(List<? extends DocTree> l) {
    return DocParser.join(l);
  }

  static final class DocType extends DocParser.Node implements DocTypeTree {
    final String text;

    DocType(String t) {
      super(Kind.DOC_TYPE);
      text = t;
    }

    public String getText() {
      return text;
    }

    public <R, D> R accept(DocTreeVisitor<R, D> v, D d) {
      return v.visitDocType(this, d);
    }

    public String toString() {
      return "<!" + text + ">";
    }
  }

  static final class Erroneous extends DocParser.Node implements ErroneousTree {
    final String body;
    final javax.tools.Diagnostic<javax.tools.JavaFileObject> diagnostic;

    Erroneous(String b, javax.tools.Diagnostic<javax.tools.JavaFileObject> d) {
      super(Kind.ERRONEOUS);
      body = b;
      diagnostic = d;
    }

    public String getBody() {
      return body;
    }

    public javax.tools.Diagnostic<javax.tools.JavaFileObject> getDiagnostic() {
      return diagnostic;
    }

    public <R, D> R accept(DocTreeVisitor<R, D> v, D d) {
      return v.visitErroneous(this, d);
    }

    public String toString() {
      return body;
    }
  }

  static final class Escape extends DocParser.Node implements EscapeTree {
    final char ch;

    Escape(char c) {
      super(Kind.ESCAPE);
      ch = c;
    }

    public String getBody() {
      return String.valueOf(ch);
    }

    public <R, D> R accept(DocTreeVisitor<R, D> v, D d) {
      return v.visitEscape(this, d);
    }

    public String toString() {
      return "\\" + ch;
    }
  }

  static final class Hidden extends DocParser.Node implements HiddenTree {
    final List<DocTree> body;

    Hidden(List<DocTree> b) {
      super(Kind.HIDDEN);
      body = b;
    }

    public String getTagName() {
      return "hidden";
    }

    public List<? extends DocTree> getBody() {
      return body;
    }

    public <R, D> R accept(DocTreeVisitor<R, D> v, D d) {
      return v.visitHidden(this, d);
    }

    public String toString() {
      return "@hidden " + join(body);
    }
  }

  static final class Index extends DocParser.Node implements IndexTree {
    final DocTree term;
    final List<DocTree> description;

    Index(DocTree t, List<DocTree> d) {
      super(Kind.INDEX);
      term = t;
      description = d;
    }

    public String getTagName() {
      return "index";
    }

    public DocTree getSearchTerm() {
      return term;
    }

    public List<? extends DocTree> getDescription() {
      return description;
    }

    public <R, D> R accept(DocTreeVisitor<R, D> v, D d) {
      return v.visitIndex(this, d);
    }

    public String toString() {
      return "{@index " + term + (description.isEmpty() ? "" : " " + join(description)) + "}";
    }
  }

  static final class Service extends DocParser.Node implements ProvidesTree, UsesTree {
    final ReferenceTree service;
    final List<DocTree> description;

    Service(Kind k, ReferenceTree s, List<DocTree> d) {
      super(k);
      service = s;
      description = d;
    }

    public String getTagName() {
      return kind.tagName;
    }

    public ReferenceTree getServiceType() {
      return service;
    }

    public List<? extends DocTree> getDescription() {
      return description;
    }

    public <R, D> R accept(DocTreeVisitor<R, D> v, D d) {
      return kind == Kind.PROVIDES ? v.visitProvides(this, d) : v.visitUses(this, d);
    }

    public String toString() {
      return "@" + kind.tagName + " " + service + (description.isEmpty() ? "" : " " + join(description));
    }
  }

  static final class RawText extends DocParser.Node implements RawTextTree {
    final String content;

    RawText(Kind k, String c) {
      super(k);
      content = c;
    }

    public String getContent() {
      return content;
    }

    public <R, D> R accept(DocTreeVisitor<R, D> v, D d) {
      return v.visitRawText(this, d);
    }

    public String toString() {
      return content;
    }
  }

  static final class Serial extends DocParser.Node implements SerialTree, SerialDataTree {
    final List<DocTree> description;

    Serial(Kind k, List<DocTree> d) {
      super(k);
      description = d;
    }

    public String getTagName() {
      return kind.tagName;
    }

    public List<? extends DocTree> getDescription() {
      return description;
    }

    public <R, D> R accept(DocTreeVisitor<R, D> v, D d) {
      return kind == Kind.SERIAL ? v.visitSerial(this, d) : v.visitSerialData(this, d);
    }

    public String toString() {
      return "@" + kind.tagName + (description.isEmpty() ? "" : " " + join(description));
    }
  }

  static final class SerialField extends DocParser.Node implements SerialFieldTree {
    final IdentifierTree name;
    final ReferenceTree type;
    final List<DocTree> description;

    SerialField(IdentifierTree n, ReferenceTree t, List<DocTree> d) {
      super(Kind.SERIAL_FIELD);
      name = n;
      type = t;
      description = d;
    }

    public String getTagName() {
      return "serialField";
    }

    public IdentifierTree getName() {
      return name;
    }

    public ReferenceTree getType() {
      return type;
    }

    public List<? extends DocTree> getDescription() {
      return description;
    }

    public <R, D> R accept(DocTreeVisitor<R, D> v, D d) {
      return v.visitSerialField(this, d);
    }

    public String toString() {
      return "@serialField " + name + " " + type + (description.isEmpty() ? "" : " " + join(description));
    }
  }

  static final class Snippet extends DocParser.Node implements SnippetTree {
    final List<DocTree> attributes;
    final TextTree body;

    Snippet(List<DocTree> a, TextTree b) {
      super(Kind.SNIPPET);
      attributes = a;
      body = b;
    }

    public String getTagName() {
      return "snippet";
    }

    public List<? extends DocTree> getAttributes() {
      return attributes;
    }

    public TextTree getBody() {
      return body;
    }

    public <R, D> R accept(DocTreeVisitor<R, D> v, D d) {
      return v.visitSnippet(this, d);
    }

    public String toString() {
      return "{@snippet " + join(attributes) + (body == null ? "" : ":\n" + body.getBody()) + "}";
    }
  }

  static final class Spec extends DocParser.Node implements SpecTree {
    final TextTree url;
    final List<DocTree> title;

    Spec(TextTree u, List<DocTree> t) {
      super(Kind.SPEC);
      url = u;
      title = t;
    }

    public String getTagName() {
      return "spec";
    }

    public TextTree getURL() {
      return url;
    }

    public List<? extends DocTree> getTitle() {
      return title;
    }

    public <R, D> R accept(DocTreeVisitor<R, D> v, D d) {
      return v.visitSpec(this, d);
    }

    public String toString() {
      return "@spec " + url.getBody() + (title.isEmpty() ? "" : " " + join(title));
    }
  }

  static final class SystemProperty extends DocParser.Node implements SystemPropertyTree {
    final Name name;

    SystemProperty(Name n) {
      super(Kind.SYSTEM_PROPERTY);
      name = n;
    }

    public String getTagName() {
      return "systemProperty";
    }

    public Name getPropertyName() {
      return name;
    }

    public <R, D> R accept(DocTreeVisitor<R, D> v, D d) {
      return v.visitSystemProperty(this, d);
    }

    public String toString() {
      return "{@systemProperty " + name + "}";
    }
  }
}
