/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.javac;

import com.sun.source.doctree.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.lang.model.element.Name;

/** Parser for documentation comments producing com.sun.source.doctree trees. */
public final class DocParser {
  private DocParser() {
  }

  /* ---------- nodes ---------- */

  abstract static class Node implements DocTree {
    final Kind kind;
    Node(Kind k) {
      kind = k;
    }

    public Kind getKind() {
      return kind;
    }
  }

  static String join(List<? extends DocTree> l) {
    StringBuilder sb = new StringBuilder();
    for (DocTree t : l) {
      sb.append(t);
    }
    return sb.toString();
  }

  static final class Text extends Node implements TextTree {
    final String body;
    Text(String b) {
      super(Kind.TEXT);
      body = b;
    }

    public String getBody() {
      return body;
    }

    public <R, D> R accept(DocTreeVisitor<R, D> v, D d) {
      return v.visitText(this, d);
    }

    public String toString() {
      return body;
    }
  }

  static final class Comment extends Node implements CommentTree {
    final String body;
    Comment(String b) {
      super(Kind.COMMENT);
      body = b;
    }

    public String getBody() {
      return body;
    }

    public <R, D> R accept(DocTreeVisitor<R, D> v, D d) {
      return v.visitComment(this, d);
    }

    public String toString() {
      return body;
    }
  }

  static final class Ident extends Node implements IdentifierTree {
    final Name name;
    Ident(String n) {
      super(Kind.IDENTIFIER);
      name = NameImpl.of(n);
    }

    public Name getName() {
      return name;
    }

    public <R, D> R accept(DocTreeVisitor<R, D> v, D d) {
      return v.visitIdentifier(this, d);
    }

    public String toString() {
      return name.toString();
    }
  }

  static final class Entity extends Node implements EntityTree {
    final Name name;
    Entity(String n) {
      super(Kind.ENTITY);
      name = NameImpl.of(n);
    }

    public Name getName() {
      return name;
    }

    public <R, D> R accept(DocTreeVisitor<R, D> v, D d) {
      return v.visitEntity(this, d);
    }

    public String toString() {
      return "&" + name + ";";
    }
  }

  static final class Ref extends Node implements ReferenceTree {
    final String sig;
    Ref(String s) {
      super(Kind.REFERENCE);
      sig = s;
    }

    public String getSignature() {
      return sig;
    }

    public <R, D> R accept(DocTreeVisitor<R, D> v, D d) {
      return v.visitReference(this, d);
    }

    public String toString() {
      return sig;
    }
  }

  static final class Attr extends Node implements AttributeTree {
    final Name name;
    final ValueKind vk;
    final List<DocTree> value;
    Attr(String n, ValueKind vk, List<DocTree> v) {
      super(Kind.ATTRIBUTE);
      name = NameImpl.of(n);
      this.vk = vk;
      value = v;
    }

    public Name getName() {
      return name;
    }

    public ValueKind getValueKind() {
      return vk;
    }

    public List<? extends DocTree> getValue() {
      return value;
    }

    public <R, D> R accept(DocTreeVisitor<R, D> v, D d) {
      return v.visitAttribute(this, d);
    }

    public String toString() {
      switch (vk) {
        case EMPTY:
          return name.toString();
        case UNQUOTED:
          return name + "=" + join(value);
        case SINGLE:
          return name + "='" + join(value) + "'";
        default:
          return name + "=\"" + join(value) + "\"";
      }
    }
  }

  static final class Start extends Node implements StartElementTree {
    final Name name;
    final List<DocTree> attrs;
    final boolean selfClosing;
    Start(String n, List<DocTree> a, boolean sc) {
      super(Kind.START_ELEMENT);
      name = NameImpl.of(n);
      attrs = a;
      selfClosing = sc;
    }

    public Name getName() {
      return name;
    }

    public List<? extends DocTree> getAttributes() {
      return attrs;
    }

    public boolean isSelfClosing() {
      return selfClosing;
    }

    public <R, D> R accept(DocTreeVisitor<R, D> v, D d) {
      return v.visitStartElement(this, d);
    }

    public String toString() {
      StringBuilder sb = new StringBuilder("<").append(name);
      for (DocTree a : attrs) {
        sb.append(' ').append(a);
      }
      return sb.append(selfClosing ? "/>" : ">").toString();
    }
  }

  static final class End extends Node implements EndElementTree {
    final Name name;
    End(String n) {
      super(Kind.END_ELEMENT);
      name = NameImpl.of(n);
    }

    public Name getName() {
      return name;
    }

    public <R, D> R accept(DocTreeVisitor<R, D> v, D d) {
      return v.visitEndElement(this, d);
    }

    public String toString() {
      return "</" + name + ">";
    }
  }

  /* inline tags */
  static final class Link extends Node implements LinkTree {
    final Ref ref;
    final List<DocTree> label;
    Link(Kind k, Ref r, List<DocTree> l) {
      super(k);
      ref = r;
      label = l;
    }

    public String getTagName() {
      return kind == Kind.LINK ? "link" : "linkplain";
    }

    public ReferenceTree getReference() {
      return ref;
    }

    public List<? extends DocTree> getLabel() {
      return label;
    }

    public <R, D> R accept(DocTreeVisitor<R, D> v, D d) {
      return v.visitLink(this, d);
    }

    public String toString() {
      return "{@" + getTagName() + " " + ref + (label.isEmpty() ? "" : " " + join(label)) + "}";
    }
  }

  static final class Lit extends Node implements LiteralTree {
    final Text body;
    Lit(Kind k, Text b) {
      super(k);
      body = b;
    }

    public String getTagName() {
      return kind == Kind.CODE ? "code" : "literal";
    }

    public TextTree getBody() {
      return body;
    }

    public <R, D> R accept(DocTreeVisitor<R, D> v, D d) {
      return kind == Kind.CODE ? v.visitLiteral(this, d) : v.visitLiteral(this, d);
    }

    public String toString() {
      return "{@" + getTagName() + " " + body + "}";
    }
  }

  static final class Value extends Node implements ValueTree {
    final Ref ref;
    Value(Ref r) {
      super(Kind.VALUE);
      ref = r;
    }

    public String getTagName() {
      return "value";
    }

    public ReferenceTree getReference() {
      return ref;
    }

    public <R, D> R accept(DocTreeVisitor<R, D> v, D d) {
      return v.visitValue(this, d);
    }

    public String toString() {
      return ref == null ? "{@value}" : "{@value " + ref + "}";
    }
  }

  static final class Inherit extends Node implements InheritDocTree {
    Inherit() {
      super(Kind.INHERIT_DOC);
    }

    public String getTagName() {
      return "inheritDoc";
    }

    public <R, D> R accept(DocTreeVisitor<R, D> v, D d) {
      return v.visitInheritDoc(this, d);
    }

    public String toString() {
      return "{@inheritDoc}";
    }
  }

  static final class DocRoot extends Node implements DocRootTree {
    DocRoot() {
      super(Kind.DOC_ROOT);
    }

    public String getTagName() {
      return "docRoot";
    }

    public <R, D> R accept(DocTreeVisitor<R, D> v, D d) {
      return v.visitDocRoot(this, d);
    }

    public String toString() {
      return "{@docRoot}";
    }
  }

  static final class Summary extends Node implements SummaryTree {
    final List<DocTree> content;
    Summary(List<DocTree> c) {
      super(Kind.SUMMARY);
      content = c;
    }

    public String getTagName() {
      return "summary";
    }

    public List<? extends DocTree> getSummary() {
      return content;
    }

    public <R, D> R accept(DocTreeVisitor<R, D> v, D d) {
      return v.visitSummary(this, d);
    }

    public String toString() {
      return "{@summary " + join(content) + "}";
    }
  }

  static final class UnkInline extends Node implements UnknownInlineTagTree {
    final String name;
    final List<DocTree> content;
    UnkInline(String n, List<DocTree> c) {
      super(Kind.UNKNOWN_INLINE_TAG);
      name = n;
      content = c;
    }

    public String getTagName() {
      return name;
    }

    public List<? extends DocTree> getContent() {
      return content;
    }

    public <R, D> R accept(DocTreeVisitor<R, D> v, D d) {
      return v.visitUnknownInlineTag(this, d);
    }

    public String toString() {
      return "{@" + name + " " + join(content) + "}";
    }
  }

  /* block tags */
  static final class Param extends Node implements ParamTree {
    final Ident name;
    final boolean typeParam;
    final List<DocTree> desc;
    Param(Ident n, boolean tp, List<DocTree> d) {
      super(Kind.PARAM);
      name = n;
      typeParam = tp;
      desc = d;
    }

    public String getTagName() {
      return "param";
    }

    public IdentifierTree getName() {
      return name;
    }

    public boolean isTypeParameter() {
      return typeParam;
    }

    public List<? extends DocTree> getDescription() {
      return desc;
    }

    public <R, D> R accept(DocTreeVisitor<R, D> v, D d) {
      return v.visitParam(this, d);
    }

    public String toString() {
      return "@param " + (typeParam ? "<" + name + ">" : name.toString()) + (desc.isEmpty() ? "" : " " + join(desc));
    }
  }

  static final class Ret extends Node implements ReturnTree {
    final List<DocTree> desc;
    Ret(List<DocTree> d) {
      super(Kind.RETURN);
      desc = d;
    }

    public String getTagName() {
      return "return";
    }

    public List<? extends DocTree> getDescription() {
      return desc;
    }

    public <R, D> R accept(DocTreeVisitor<R, D> v, D d) {
      return v.visitReturn(this, d);
    }

    public String toString() {
      return "@return " + join(desc);
    }
  }

  static final class Thr extends Node implements ThrowsTree {
    final Ref name;
    final List<DocTree> desc;
    Thr(Kind k, Ref n, List<DocTree> d) {
      super(k);
      name = n;
      desc = d;
    }

    public String getTagName() {
      return kind == Kind.THROWS ? "throws" : "exception";
    }

    public ReferenceTree getExceptionName() {
      return name;
    }

    public List<? extends DocTree> getDescription() {
      return desc;
    }

    public <R, D> R accept(DocTreeVisitor<R, D> v, D d) {
      return v.visitThrows(this, d);
    }

    public String toString() {
      return "@" + getTagName() + " " + name + (desc.isEmpty() ? "" : " " + join(desc));
    }
  }

  static final class See extends Node implements SeeTree {
    final List<DocTree> ref;
    See(List<DocTree> r) {
      super(Kind.SEE);
      ref = r;
    }

    public String getTagName() {
      return "see";
    }

    public List<? extends DocTree> getReference() {
      return ref;
    }

    public <R, D> R accept(DocTreeVisitor<R, D> v, D d) {
      return v.visitSee(this, d);
    }

    public String toString() {
      return "@see " + join(ref);
    }
  }

  static final class Simple extends Node implements AuthorTree, SinceTree, VersionTree, DeprecatedTree {
    final String tag;
    final List<DocTree> content;
    Simple(Kind k, String t, List<DocTree> c) {
      super(k);
      tag = t;
      content = c;
    }

    public String getTagName() {
      return tag;
    }

    public List<? extends DocTree> getName() {
      return content;
    }

    public List<? extends DocTree> getBody() {
      return content;
    }

    public <R, D> R accept(DocTreeVisitor<R, D> v, D d) {
      switch (kind) {
        case AUTHOR:
          return v.visitAuthor(this, d);
        case SINCE:
          return v.visitSince(this, d);
        case VERSION:
          return v.visitVersion(this, d);
        default:
          return v.visitDeprecated(this, d);
      }
    }

    public String toString() {
      return "@" + tag + (content.isEmpty() ? "" : " " + join(content));
    }
  }

  static final class UnkBlock extends Node implements UnknownBlockTagTree {
    final String name;
    final List<DocTree> content;
    UnkBlock(String n, List<DocTree> c) {
      super(Kind.UNKNOWN_BLOCK_TAG);
      name = n;
      content = c;
    }

    public String getTagName() {
      return name;
    }

    public List<? extends DocTree> getContent() {
      return content;
    }

    public <R, D> R accept(DocTreeVisitor<R, D> v, D d) {
      return v.visitUnknownBlockTag(this, d);
    }

    public String toString() {
      return "@" + name + (content.isEmpty() ? "" : " " + join(content));
    }
  }

  static final class Doc extends Node implements DocCommentTree {
    final List<DocTree> first;
    final List<DocTree> body;
    final List<DocTree> tags;
    List<DocTree> preamble = Collections.<DocTree>emptyList();
    List<DocTree> postamble = Collections.<DocTree>emptyList();
    Doc(List<DocTree> f, List<DocTree> b, List<DocTree> t) {
      super(Kind.DOC_COMMENT);
      first = f;
      body = b;
      tags = t;
    }

    public List<? extends DocTree> getFirstSentence() {
      return ModelUtils.j(first);
    }

    public List<? extends DocTree> getFullBody() {
      return body;
    }

    public List<? extends DocTree> getBody() {
      int n = Math.min(first.size(), body.size());
      List<DocTree> rest = new ArrayList<DocTree>();
      if (n > 0 && first.get(n - 1) instanceof Text && body.get(n - 1) instanceof Text) {
        String all = ((Text) body.get(n - 1)).body;
        String part = ((Text) first.get(n - 1)).body;
        if (all.length() > part.length()) {
          rest.add(new Text(all.substring(part.length()).replaceAll("^\\s+", "")));
        }
      }
      rest.addAll(body.subList(n, body.size()));
      return rest;
    }

    public List<? extends DocTree> getBlockTags() {
      return tags;
    }

    public List<? extends DocTree> getPreamble() {
      return preamble;
    }

    public List<? extends DocTree> getPostamble() {
      return postamble;
    }

    public <R, D> R accept(DocTreeVisitor<R, D> v, D d) {
      return v.visitDocComment(this, d);
    }

    public String toString() {
      StringBuilder sb = new StringBuilder(join(body));
      for (DocTree t : tags) {
        if (sb.length() > 0) {
          sb.append('\n');
        }
        sb.append(t);
      }
      return sb.toString();
    }
  }

  /* ---------- parsing ---------- */

  public static DocCommentTree parse(String text) {
    List<String> bodyLines = new ArrayList<String>();
    List<String[]> tagsRaw = new ArrayList<String[]>();
    String[] lines = text.split("\n", -1);
    boolean inPre = false;
    for (int k = 0; k < lines.length; k++) {
      if (!inPre && k > 0) {
        lines[k] = lines[k].replaceAll("^[ \\t]+", "");
      }
      String low = lines[k].toLowerCase();
      if (low.contains("<pre")) {
        inPre = true;
      }
      if (low.contains("</pre")) {
        inPre = false;
      }
    }
    StringBuilder cur = new StringBuilder();
    String curTag = null;
    int braces = 0;
    for (String line : lines) {
      String t = line.replaceAll("^\\s+", "");
      if (braces == 0 && t.startsWith("@") && t.length() > 1 && Character.isLetter(t.charAt(1))) {
        if (curTag == null) {
          bodyLines.add(cur.toString());
        } else {
          tagsRaw.add(new String[] {curTag, cur.toString()});
        }
        int sp = 1;
        while (sp < t.length() && !Character.isWhitespace(t.charAt(sp))) {
          sp++;
        }
        curTag = t.substring(1, sp);
        cur = new StringBuilder(t.substring(sp));
        braces = depth(t, 0);
        continue;
      }
      if (cur.length() > 0 || curTag == null && !bodyLines.isEmpty() || curTag != null) {
        cur.append('\n');
      }
      cur.append(line);
      braces = Math.max(0, braces + depth(line, 0));
    }
    if (curTag == null) {
      bodyLines.add(cur.toString());
    } else {
      tagsRaw.add(new String[] {curTag, cur.toString()});
    }
    String body = bodyLines.isEmpty() ? "" : bodyLines.get(0);
    List<DocTree> bodyNodes = inline(trimBody(body));
    List<DocTree> tags = new ArrayList<DocTree>();
    for (String[] tr : tagsRaw) {
      tags.add(blockTag(tr[0], tr[1]));
    }
    return new Doc(firstSentence(bodyNodes), bodyNodes, tags);
  }

  private static int depth(String s, int from) {
    int d = 0;
    for (int i = from; i < s.length(); i++) {
      char c = s.charAt(i);
      if (c == '{' && i + 1 < s.length() && s.charAt(i + 1) == '@') {
        d++;
      } else if (c == '}') {
        d--;
      }
    }
    return d;
  }

  private static String trimBody(String s) {
    int a = 0;
    while (a < s.length() && Character.isWhitespace(s.charAt(a))) {
      a++;
    }
    int b = s.length();
    while (b > a && Character.isWhitespace(s.charAt(b - 1))) {
      b--;
    }
    return s.substring(a, b);
  }

  private static DocTree blockTag(String name, String raw) {
    String c = trimBody(raw);
    if (name.equals("param")) {
      int sp = 0;
      while (sp < c.length() && !Character.isWhitespace(c.charAt(sp))) {
        sp++;
      }
      String n = c.substring(0, sp);
      boolean tp = n.startsWith("<") && n.endsWith(">");
      if (tp) {
        n = n.substring(1, n.length() - 1);
      }
      return new Param(new Ident(n), tp, inline(trimBody(c.substring(sp))));
    }
    if (name.equals("return")) {
      return new Ret(inline(c));
    }
    if (name.equals("throws") || name.equals("exception")) {
      int sp = 0;
      while (sp < c.length() && !Character.isWhitespace(c.charAt(sp))) {
        sp++;
      }
      return new Thr(name.equals("throws") ? DocTree.Kind.THROWS : DocTree.Kind.EXCEPTION,
          new Ref(c.substring(0, sp)), inline(trimBody(c.substring(sp))));
    }
    if (name.equals("see")) {
      if (c.startsWith("\"") || c.startsWith("<")) {
        return new See(inline(c));
      }
      int end = refEnd(c);
      List<DocTree> l = new ArrayList<DocTree>();
      l.add(new Ref(c.substring(0, end)));
      String rest = trimBody(c.substring(end));
      if (!rest.isEmpty()) {
        l.addAll(inline(rest));
      }
      return new See(l);
    }
    if (name.equals("author")) {
      return new Simple(DocTree.Kind.AUTHOR, name, inline(c));
    }
    if (name.equals("since")) {
      return new Simple(DocTree.Kind.SINCE, name, inline(c));
    }
    if (name.equals("version")) {
      return new Simple(DocTree.Kind.VERSION, name, inline(c));
    }
    if (name.equals("deprecated")) {
      return new Simple(DocTree.Kind.DEPRECATED, name, inline(c));
    }
    return new UnkBlock(name, inline(c));
  }

  private static int refEnd(String s) {
    int i = 0;
    int paren = 0;
    while (i < s.length()) {
      char ch = s.charAt(i);
      if (ch == '(') {
        paren++;
      } else if (ch == ')') {
        paren--;
      } else if (Character.isWhitespace(ch) && paren <= 0) {
        break;
      }
      i++;
    }
    return i;
  }

  static List<DocTree> inline(String s) {
    List<DocTree> out = new ArrayList<DocTree>();
    StringBuilder text = new StringBuilder();
    int i = 0;
    int n = s.length();
    while (i < n) {
      char c = s.charAt(i);
      if (c == '{' && s.startsWith("{@", i)) {
        int end = matchBrace(s, i);
        if (end > 0) {
          flush(out, text);
          out.add(inlineTag(s.substring(i + 2, end)));
          i = end + 1;
          continue;
        }
      } else if (c == '<' && i + 1 < n) {
        int end = s.indexOf('>', i);
        if (s.startsWith("<!--", i)) {
          int ce = s.indexOf("-->", i);
          if (ce > 0) {
            flush(out, text);
            out.add(new Comment(s.substring(i, ce + 3)));
            i = ce + 3;
            continue;
          }
        } else if (end > 0 && (Character.isLetter(s.charAt(i + 1)) || s.charAt(i + 1) == '/')) {
          flush(out, text);
          out.add(element(s.substring(i + 1, end)));
          i = end + 1;
          continue;
        }
      } else if (c == '&') {
        int semi = s.indexOf(';', i);
        if (semi > i + 1 && semi - i < 12 && s.substring(i + 1, semi).matches("#?[A-Za-z0-9]+")) {
          flush(out, text);
          out.add(new Entity(s.substring(i + 1, semi)));
          i = semi + 1;
          continue;
        }
      }
      text.append(c);
      i++;
    }
    flush(out, text);
    return out;
  }

  private static void flush(List<DocTree> out, StringBuilder text) {
    if (text.length() > 0) {
      out.add(new Text(text.toString()));
      text.setLength(0);
    }
  }

  private static int matchBrace(String s, int from) {
    int d = 0;
    for (int i = from; i < s.length(); i++) {
      char c = s.charAt(i);
      if (c == '{') {
        d++;
      } else if (c == '}') {
        d--;
        if (d == 0) {
          return i;
        }
      }
    }
    return -1;
  }

  private static DocTree inlineTag(String inner) {
    int sp = 0;
    while (sp < inner.length() && !Character.isWhitespace(inner.charAt(sp))) {
      sp++;
    }
    String name = inner.substring(0, sp);
    String rest = sp < inner.length() ? inner.substring(sp + 1) : "";
    if (name.equals("link") || name.equals("linkplain")) {
      String r = trimBody(rest);
      int end = refEnd(r);
      return new Link(name.equals("link") ? DocTree.Kind.LINK : DocTree.Kind.LINK_PLAIN, new Ref(r.substring(0, end)),
          inline(trimBody(r.substring(end))));
    }
    if (name.equals("code") || name.equals("literal")) {
      return new Lit(name.equals("code") ? DocTree.Kind.CODE : DocTree.Kind.LITERAL, new Text(rest));
    }
    if (name.equals("value")) {
      String r = trimBody(rest);
      return new Value(r.isEmpty() ? null : new Ref(r));
    }
    if (name.equals("inheritDoc")) {
      return new Inherit();
    }
    if (name.equals("docRoot")) {
      return new DocRoot();
    }
    if (name.equals("summary")) {
      return new Summary(inline(trimBody(rest)));
    }
    return new UnkInline(name, inline(trimBody(rest)));
  }

  private static DocTree element(String inner) {
    if (inner.startsWith("/")) {
      return new End(inner.substring(1).trim());
    }
    boolean selfClosing = inner.endsWith("/");
    if (selfClosing) {
      inner = inner.substring(0, inner.length() - 1);
    }
    int sp = 0;
    while (sp < inner.length() && !Character.isWhitespace(inner.charAt(sp))) {
      sp++;
    }
    String name = inner.substring(0, sp);
    List<DocTree> attrs = new ArrayList<DocTree>();
    String a = inner.substring(sp);
    java.util.regex.Matcher m = java.util.regex.Pattern
        .compile("\\s*([A-Za-z_:][-A-Za-z0-9_:.]*)(?:\\s*=\\s*(?:\"([^\"]*)\"|'([^']*)'|([^\\s\"'>]+)))?").matcher(a);
    int pos = 0;
    while (pos < a.length() && m.find(pos) && m.start() == pos) {
      String an = m.group(1);
      if (m.group(2) != null) {
        attrs.add(new Attr(an, AttributeTree.ValueKind.DOUBLE, inline(m.group(2))));
      } else if (m.group(3) != null) {
        attrs.add(new Attr(an, AttributeTree.ValueKind.SINGLE, inline(m.group(3))));
      } else if (m.group(4) != null) {
        attrs.add(new Attr(an, AttributeTree.ValueKind.UNQUOTED, inline(m.group(4))));
      } else {
        attrs.add(new Attr(an, AttributeTree.ValueKind.EMPTY, new ArrayList<DocTree>()));
      }
      pos = m.end();
    }
    return new Start(name, attrs, selfClosing);
  }

  private static boolean blockElement(String n) {
    n = n.toLowerCase();
    return n.equals("p") || n.equals("ul") || n.equals("ol") || n.equals("table") || n.equals("pre")
        || n.equals("div") || n.equals("dl") || n.equals("blockquote") || n.matches("h[1-6]");
  }

  static List<DocTree> firstSentence(List<DocTree> body) {
    List<DocTree> out = new ArrayList<DocTree>();
    for (DocTree t : body) {
      if (t instanceof Start && blockElement(((Start) t).name.toString())) {
        break;
      }
      if (t instanceof Text) {
        String s = ((Text) t).body;
        int cut = -1;
        for (int i = 0; i < s.length(); i++) {
          if (s.charAt(i) == '.' && (i + 1 == s.length() || Character.isWhitespace(s.charAt(i + 1)))) {
            cut = i + 1;
            break;
          }
        }
        if (cut >= 0) {
          out.add(new Text(s.substring(0, cut)));
          return out;
        }
      }
      out.add(t);
    }
    return out;
  }
}
