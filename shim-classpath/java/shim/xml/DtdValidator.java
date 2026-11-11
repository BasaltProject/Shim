/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package shim.xml;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Validates SAX-style events against the DTD of the document and reports each violation to a sink. */
public final class DtdValidator {
  public interface Sink {
    void error(String message);

    void errorAt(String message, int line, int column);

    int line();

    int column();
  }

  private static final class Node {
    final char op;
    final char sep;
    final String name;
    final List<Node> items = new ArrayList<Node>();
    int position = -1;

    Node(char op, char sep, String name) {
      this.op = op;
      this.sep = sep;
      this.name = name;
    }
  }

  private static final class Model {
    final String text;
    final Node root;
    final List<String> names = new ArrayList<String>();
    final List<BitSet> follow = new ArrayList<BitSet>();
    BitSet first;
    BitSet last;
    boolean nullable;

    Model(String text, Node root) {
      this.text = text;
      this.root = root;
    }
  }

  private static final class Frame {
    final String name;
    final XmlDtd.ElementDecl decl;
    final List<String> children = new ArrayList<String>();
    boolean textReported;

    Frame(String name, XmlDtd.ElementDecl decl) {
      this.name = name;
      this.decl = decl;
    }
  }

  private final XmlDtd dtd;
  private final Sink sink;
  private final Deque<Frame> stack = new ArrayDeque<Frame>();
  private final Set<String> ids = new HashSet<String>();
  private final List<String> idrefs = new ArrayList<String>();
  private final List<int[]> idrefPositions = new ArrayList<int[]>();
  private final Map<String, Model> models = new HashMap<String, Model>();
  private boolean sawRoot;

  public DtdValidator(XmlDtd dtd, Sink sink) {
    this.dtd = dtd;
    this.sink = sink;
  }

  public void startElement(String name, List<XmlPullParser.Attr> attrs, int line, int column) {
    if (!sawRoot) {
      sawRoot = true;
      if (dtd == null) {
        sink.errorAt("Document is invalid: no grammar found.", line, column);
        sink.errorAt("Document root element \"" + name + "\", must match DOCTYPE root \"null\".", line, column);
      } else if (dtd.rootName != null && !dtd.rootName.equals(name)) {
        sink.errorAt("Document root element \"" + name + "\", must match DOCTYPE root \"" + dtd.rootName + "\".",
            line, column);
      }
    }
    Frame parent = stack.peek();
    if (parent != null) {
      parent.children.add(name);
    }
    XmlDtd.ElementDecl decl = dtd == null ? null : dtd.elements.get(name);
    if (dtd != null && decl == null) {
      sink.error("Element type \"" + name + "\" must be declared.");
    }
    stack.push(new Frame(name, decl));
    if (dtd != null) {
      checkAttributes(name, attrs);
    }
  }

  private void checkAttributes(String element, List<XmlPullParser.Attr> attrs) {
    List<XmlDtd.AttDef> defs = dtd.attributes.get(element);
    if (defs != null) {
      for (XmlDtd.AttDef def : defs) {
        if ("#REQUIRED".equals(def.mode) && find(attrs, def.name) == null) {
          sink.error("Attribute \"" + def.name + "\" is required and must be specified for element type \"" + element
              + "\".");
        }
      }
    }
    for (XmlPullParser.Attr a : attrs) {
      if (!a.specified) {
        continue;
      }
      XmlDtd.AttDef def = dtd.findAttribute(element, a.name);
      if (def == null) {
        sink.error("Attribute \"" + a.name + "\" must be declared for element type \"" + element + "\".");
        continue;
      }
      checkValue(def, a.value);
    }
  }

  private static XmlPullParser.Attr find(List<XmlPullParser.Attr> attrs, String name) {
    for (XmlPullParser.Attr a : attrs) {
      if (a.specified && a.name.equals(name)) {
        return a;
      }
    }
    return null;
  }

  private void checkValue(XmlDtd.AttDef def, String value) {
    String type = def.type;
    if (type.equals("ID")) {
      if (!ids.add(value)) {
        sink.error("Attribute value \"" + value + "\" of type ID must be unique within the document.");
      }
    } else if (type.equals("IDREF")) {
      idrefs.add(value);
      idrefPositions.add(new int[] {sink.line(), sink.column()});
    } else if (type.equals("IDREFS")) {
      for (String s : value.trim().split("\\s+")) {
        idrefs.add(s);
        idrefPositions.add(new int[] {sink.line(), sink.column()});
      }
    } else if (type.startsWith("(") || type.startsWith("NOTATION")) {
      String list = type.substring(type.indexOf('(') + 1, type.lastIndexOf(')'));
      boolean found = false;
      StringBuilder shown = new StringBuilder();
      for (String s : list.split("\\|")) {
        String item = s.trim();
        if (item.equals(value)) {
          found = true;
        }
        shown.append(item).append(' ');
      }
      if (!found) {
        sink.error("Attribute \"" + def.name + "\" with value \"" + value + "\" must have a value from the list \""
            + shown + "\".");
      }
    }
    if ("#FIXED".equals(def.mode) && def.value != null && !def.value.equals(value)) {
      sink.error("Attribute \"" + def.name + "\" with value \"" + value + "\" must have a value of \"" + def.value
          + "\".");
    }
  }

  public void characters(String text, boolean whitespaceOnly) {
    Frame f = stack.peek();
    if (f == null || f.decl == null || f.textReported) {
      return;
    }
    if (f.decl.kind == XmlDtd.kEmpty || (f.decl.kind == XmlDtd.kChildren && !whitespaceOnly)) {
      f.textReported = true;
      sink.errorAt("The content of element type \"" + f.name + "\" must match \"" + display(f.decl) + "\".",
          sink.line(), sink.column() + 2);
    }
  }

  public void endElement() {
    Frame f = stack.pop();
    XmlDtd.ElementDecl decl = f.decl;
    if (decl == null) {
      return;
    }
    if (decl.kind == XmlDtd.kEmpty) {
      if (!f.children.isEmpty() && !f.textReported) {
        sink.error("The content of element type \"" + f.name + "\" must match \"EMPTY\".");
      }
    } else if (decl.kind == XmlDtd.kMixed) {
      Set<String> allowed = mixedNames(decl.model);
      for (String c : f.children) {
        if (!allowed.contains(c)) {
          sink.error("The content of element type \"" + f.name + "\" must match \"" + display(decl) + "\".");
          return;
        }
      }
    } else if (decl.kind == XmlDtd.kChildren) {
      Model m = models.get(decl.name);
      if (m == null) {
        m = compile(decl.model);
        models.put(decl.name, m);
      }
      int r = match(m, f.children);
      if (r >= 0 && !f.textReported) {
        sink.error("The content of element type \"" + f.name + (r == f.children.size()
            ? "\" is incomplete, it must match \"" : "\" must match \"") + m.text + "\".");
      }
    }
  }

  public void endDocument() {
    for (int i = 0; i < idrefs.size(); i++) {
      String ref = idrefs.get(i);
      if (!ids.contains(ref)) {
        sink.errorAt("An element with the identifier \"" + ref + "\" must appear in the document.",
            idrefPositions.get(i)[0], idrefPositions.get(i)[1]);
      }
    }
  }

  private static String display(XmlDtd.ElementDecl decl) {
    if (decl.kind == XmlDtd.kChildren) {
      return compile(decl.model).text;
    }
    return decl.model;
  }

  private static Set<String> mixedNames(String model) {
    Set<String> out = new LinkedHashSet<String>();
    String inner = model.substring(1, model.lastIndexOf(')'));
    for (String s : inner.split("\\|")) {
      if (!s.equals("#PCDATA")) {
        out.add(s);
      }
    }
    return out;
  }

  private static final class Parser {
    final String s;
    int i;

    Parser(String s) {
      this.s = s;
    }

    Node group() {
      i++;
      List<Node> items = new ArrayList<Node>();
      char sep = 0;
      while (true) {
        items.add(item());
        char c = s.charAt(i);
        if (c == ',' || c == '|') {
          sep = c;
          i++;
        } else {
          break;
        }
      }
      i++;
      if (items.size() == 1) {
        return withOp(items.get(0));
      }
      Node g = new Node(' ', sep, null);
      g.items.addAll(items);
      return withOp(g);
    }

    Node item() {
      if (s.charAt(i) == '(') {
        return group();
      }
      int start = i;
      while (i < s.length() && ",|)?*+".indexOf(s.charAt(i)) < 0) {
        i++;
      }
      return withOp(new Node(' ', ' ', s.substring(start, i)));
    }

    Node withOp(Node n) {
      if (i < s.length() && "?*+".indexOf(s.charAt(i)) >= 0) {
        Node u = new Node(s.charAt(i++), ' ', null);
        u.items.add(n);
        return u;
      }
      return n;
    }
  }

  private static void flatten(Node n) {
    for (Node c : n.items) {
      flatten(c);
    }
    if (n.name == null && n.op == ' ') {
      List<Node> out = new ArrayList<Node>();
      for (Node c : n.items) {
        if (c.name == null && c.op == ' ' && c.sep == n.sep) {
          out.addAll(c.items);
        } else {
          out.add(c);
        }
      }
      n.items.clear();
      n.items.addAll(out);
    }
  }

  private static String show(Node n) {
    if (n.name != null) {
      return n.name;
    }
    if (n.op != ' ') {
      Node c = n.items.get(0);
      return (c.name != null ? "(" + c.name + ")" : show(c)) + n.op;
    }
    StringBuilder sb = new StringBuilder("(");
    for (int i = 0; i < n.items.size(); i++) {
      if (i > 0) {
        sb.append(n.sep);
      }
      sb.append(show(n.items.get(i)));
    }
    return sb.append(')').toString();
  }

  private static Model compile(String text) {
    Parser p = new Parser(text);
    Node root = p.group();
    flatten(root);
    Model m = new Model(root.name != null ? "(" + root.name + ")" : show(root), root);
    number(root, m);
    for (int i = 0; i < m.names.size(); i++) {
      m.follow.add(new BitSet());
    }
    analyze(root, m);
    m.first = first(root);
    m.last = last(root);
    m.nullable = nullable(root);
    return m;
  }

  private static void number(Node n, Model m) {
    if (n.name != null) {
      n.position = m.names.size();
      m.names.add(n.name);
    }
    for (Node c : n.items) {
      number(c, m);
    }
  }

  private static boolean nullable(Node n) {
    if (n.name != null) {
      return false;
    }
    if (n.op == '?' || n.op == '*') {
      return true;
    }
    if (n.op == '+') {
      return nullable(n.items.get(0));
    }
    if (n.sep == '|') {
      for (Node c : n.items) {
        if (nullable(c)) {
          return true;
        }
      }
      return false;
    }
    for (Node c : n.items) {
      if (!nullable(c)) {
        return false;
      }
    }
    return true;
  }

  private static BitSet first(Node n) {
    BitSet b = new BitSet();
    if (n.name != null) {
      b.set(n.position);
    } else if (n.op != ' ') {
      b.or(first(n.items.get(0)));
    } else if (n.sep == '|') {
      for (Node c : n.items) {
        b.or(first(c));
      }
    } else {
      for (Node c : n.items) {
        b.or(first(c));
        if (!nullable(c)) {
          break;
        }
      }
    }
    return b;
  }

  private static BitSet last(Node n) {
    BitSet b = new BitSet();
    if (n.name != null) {
      b.set(n.position);
    } else if (n.op != ' ') {
      b.or(last(n.items.get(0)));
    } else if (n.sep == '|') {
      for (Node c : n.items) {
        b.or(last(c));
      }
    } else {
      for (int i = n.items.size() - 1; i >= 0; i--) {
        b.or(last(n.items.get(i)));
        if (!nullable(n.items.get(i))) {
          break;
        }
      }
    }
    return b;
  }

  private static void analyze(Node n, Model m) {
    for (Node c : n.items) {
      analyze(c, m);
    }
    if (n.name != null) {
      return;
    }
    if (n.op == '*' || n.op == '+') {
      BitSet f = first(n.items.get(0));
      BitSet l = last(n.items.get(0));
      for (int p = l.nextSetBit(0); p >= 0; p = l.nextSetBit(p + 1)) {
        m.follow.get(p).or(f);
      }
    } else if (n.op == ' ' && n.sep == ',') {
      for (int i = 0; i + 1 < n.items.size(); i++) {
        BitSet l = last(n.items.get(i));
        BitSet f = new BitSet();
        for (int j = i + 1; j < n.items.size(); j++) {
          f.or(first(n.items.get(j)));
          if (!nullable(n.items.get(j))) {
            break;
          }
        }
        for (int p = l.nextSetBit(0); p >= 0; p = l.nextSetBit(p + 1)) {
          m.follow.get(p).or(f);
        }
      }
    }
  }

  private static int match(Model m, List<String> children) {
    BitSet current = null;
    for (int i = 0; i < children.size(); i++) {
      BitSet candidates = new BitSet();
      if (current == null) {
        candidates.or(m.first);
      } else {
        for (int p = current.nextSetBit(0); p >= 0; p = current.nextSetBit(p + 1)) {
          candidates.or(m.follow.get(p));
        }
      }
      BitSet next = new BitSet();
      for (int p = candidates.nextSetBit(0); p >= 0; p = candidates.nextSetBit(p + 1)) {
        if (m.names.get(p).equals(children.get(i))) {
          next.set(p);
        }
      }
      if (next.isEmpty()) {
        return i;
      }
      current = next;
    }
    if (current == null) {
      return m.nullable ? -1 : 0;
    }
    BitSet fin = (BitSet) current.clone();
    fin.and(m.last);
    return fin.isEmpty() ? children.size() : -1;
  }
}
