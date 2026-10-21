/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.xml.xpath;

import java.util.ArrayList;
import java.util.List;
import javax.xml.namespace.NamespaceContext;
import shim.xml.xpath.XPathLexer.Tok;

final class XPathParser {
  private final List<Tok> toks;
  private final NamespaceContext namespaces;
  private final boolean lenient;
  private boolean xslt;
  private int pos;

  private XPathParser(List<Tok> toks, NamespaceContext namespaces, boolean lenient) {
    this.toks = toks;
    this.namespaces = namespaces;
    this.lenient = lenient;
  }

  static XPathExpr parseXslt(String text, NamespaceContext namespaces) {
    XPathParser p = new XPathParser(XPathLexer.lex(text), namespaces, false);
    p.xslt = true;
    return p.finish();
  }

  static XPathExpr parse(String text, NamespaceContext namespaces, boolean lenient) {
    return new XPathParser(XPathLexer.lex(text), namespaces, lenient).finish();
  }

  private XPathExpr finish() {
    XPathExpr e = orExpr();
    if (peek().type != XPathLexer.kEnd) {
      StringBuilder sb = new StringBuilder();
      for (int i = pos; i < toks.size() - 1; i++) {
        sb.append(i > pos ? ", " : "").append('\'').append(toks.get(i).text).append('\'');
      }
      throw new XPathError("Extra illegal tokens: " + sb);
    }
    return e;
  }

  private Tok peek() {
    return toks.get(pos);
  }

  private Tok peek(int ahead) {
    return toks.get(Math.min(pos + ahead, toks.size() - 1));
  }

  private Tok next() {
    return toks.get(pos++);
  }

  private void expect(String op) {
    if (!peek().is(op)) {
      throw new XPathError("Expected " + op + ", but found: " + peek().text);
    }
    pos++;
  }

  private XPathExpr orExpr() {
    XPathExpr e = andExpr();
    while (peek().is("or")) {
      pos++;
      e = new XPathExpr.Binary("or", e, andExpr());
    }
    return e;
  }

  private XPathExpr andExpr() {
    XPathExpr e = equality();
    while (peek().is("and")) {
      pos++;
      e = new XPathExpr.Binary("and", e, equality());
    }
    return e;
  }

  private XPathExpr equality() {
    XPathExpr e = relational();
    while (peek().is("=") || peek().is("!=")) {
      String op = next().text;
      e = new XPathExpr.Binary(op, e, relational());
    }
    return e;
  }

  private XPathExpr relational() {
    XPathExpr e = additive();
    while (peek().is("<") || peek().is("<=") || peek().is(">") || peek().is(">=")) {
      String op = next().text;
      e = new XPathExpr.Binary(op, e, additive());
    }
    return e;
  }

  private XPathExpr additive() {
    XPathExpr e = multiplicative();
    while (peek().is("+") || peek().is("-")) {
      String op = next().text;
      e = new XPathExpr.Binary(op, e, multiplicative());
    }
    return e;
  }

  private XPathExpr multiplicative() {
    XPathExpr e = unary();
    while (peek().is("*") || peek().is("div") || peek().is("mod")) {
      String op = next().text;
      e = new XPathExpr.Binary(op, e, unary());
    }
    return e;
  }

  private XPathExpr unary() {
    if (peek().is("-")) {
      pos++;
      return new XPathExpr.Unary(union());
    }
    return union();
  }

  private XPathExpr union() {
    XPathExpr e = pathExpr();
    while (peek().is("|")) {
      pos++;
      e = new XPathExpr.Union(e, pathExpr());
    }
    return e;
  }

  private boolean startsStep(Tok t) {
    if (t.type == XPathLexer.kName) {
      return true;
    }
    return t.is("@") || t.is(".") || t.is("..");
  }

  private boolean startsPrimary(Tok t, Tok after) {
    if (t.type == XPathLexer.kVar || t.type == XPathLexer.kLiteral || t.type == XPathLexer.kNumber || t.is("(")) {
      return true;
    }
    return t.type == XPathLexer.kName && after.is("(") && !isNodeType(t);
  }

  private static boolean isNodeType(Tok t) {
    return t.prefix == null
        && (t.local.equals("node") || t.local.equals("text") || t.local.equals("comment")
            || t.local.equals("processing-instruction"));
  }

  private XPathExpr pathExpr() {
    Tok t = peek();
    if (t.is("/") || t.is("//")) {
      pos++;
      List<XPathSteps.Step> steps = new ArrayList<XPathSteps.Step>();
      if (t.is("//")) {
        if (!startsStep(peek())) {
          throw new XPathError("A location step was expected following the '/' or '//' token.");
        }
        steps.add(descendantStep());
        steps.add(step());
      } else if (startsStep(peek())) {
        steps.add(step());
      } else if (peek().is("/") || peek().is("//")) {
        throw new XPathError("A location step was expected following the '/' or '//' token.");
      } else {
        return new XPathExpr.Path(null, true, steps);
      }
      relativeRest(steps);
      return new XPathExpr.Path(null, true, steps);
    }
    if (startsPrimary(t, peek(1))) {
      XPathExpr primary = primary();
      List<XPathExpr> preds = predicates();
      XPathExpr base = preds.isEmpty() ? primary : new XPathExpr.Filter(primary, preds);
      if (peek().is("/") || peek().is("//")) {
        List<XPathSteps.Step> steps = new ArrayList<XPathSteps.Step>();
        relativeRest(steps);
        return new XPathExpr.Path(base, false, steps);
      }
      return base;
    }
    if (!startsStep(t)) {
      if (t.type == XPathLexer.kEnd) {
        throw new XPathError("A location path was expected, but the end of the XPath expression was found instead.");
      }
      throw new XPathError("A location path was expected, but the following token was encountered:  " + t.text);
    }
    List<XPathSteps.Step> steps = new ArrayList<XPathSteps.Step>();
    steps.add(step());
    relativeRest(steps);
    return new XPathExpr.Path(null, false, steps);
  }

  private void relativeRest(List<XPathSteps.Step> steps) {
    while (peek().is("/") || peek().is("//")) {
      boolean deep = next().is("//");
      if (!startsStep(peek())) {
        throw new XPathError("A location step was expected following the '/' or '//' token.");
      }
      if (deep) {
        steps.add(descendantStep());
      }
      steps.add(step());
    }
  }

  private XPathSteps.Step descendantStep() {
    return new XPathSteps.Step(XPathSteps.kDescendantOrSelf, XPathSteps.kTestNode, false, null, null, false,
        new ArrayList<XPathExpr>());
  }

  private XPathSteps.Step step() {
    Tok t = next();
    if (t.is(".")) {
      return new XPathSteps.Step(XPathSteps.kSelf, XPathSteps.kTestNode, false, null, null, false,
          new ArrayList<XPathExpr>());
    }
    if (t.is("..")) {
      return new XPathSteps.Step(XPathSteps.kParent, XPathSteps.kTestNode, false, null, null, false,
          new ArrayList<XPathExpr>());
    }
    int axis = XPathSteps.kChild;
    if (t.is("@")) {
      axis = XPathSteps.kAttribute;
      t = next();
    } else if (t.type == XPathLexer.kName && peek().is("::")) {
      axis = t.prefix == null ? XPathSteps.axisOf(t.local) : -1;
      if (axis < 0) {
        throw new XPathError("illegal axis name: " + (t.prefix == null ? t.local : t.prefix));
      }
      pos++;
      t = next();
    }
    if (t.type != XPathLexer.kName) {
      throw new XPathError("A node test that matches either NCName:* or QName was expected.");
    }
    XPathSteps.Step step;
    if (peek().is("(") && isNodeType(t)) {
      pos++;
      int test = XPathSteps.kTestNode;
      String literal = null;
      if (t.local.equals("text")) {
        test = XPathSteps.kTestText;
      } else if (t.local.equals("comment")) {
        test = XPathSteps.kTestComment;
      } else if (t.local.equals("processing-instruction")) {
        test = XPathSteps.kTestPi;
        if (peek().type == XPathLexer.kLiteral) {
          literal = next().local;
        } else if (peek().type == XPathLexer.kNumber) {
          throw new XPathError("Pattern literal (" + peek().text + ") needs to be quoted!");
        }
      }
      expect(")");
      step = new XPathSteps.Step(axis, test, false, null, literal, false, predicates());
    } else if (t.prefix == null && t.local.equals("*")) {
      step = new XPathSteps.Step(axis, XPathSteps.kTestName, true, null, null, true, predicates());
    } else {
      String uri = null;
      if (t.prefix != null) {
        uri = namespaces == null ? null : namespaces.getNamespaceURI(t.prefix);
        if (uri == null || uri.isEmpty()) {
          if (!lenient) {
            throw new XPathError("Prefix must resolve to a namespace: " + t.prefix);
          }
          uri = "\u0000unresolved";
        }
      }
      boolean any = t.local.equals("*");
      step = new XPathSteps.Step(axis, XPathSteps.kTestName, false, uri, any ? null : t.local, any, predicates());
    }
    return step;
  }

  private List<XPathExpr> predicates() {
    List<XPathExpr> out = new ArrayList<XPathExpr>();
    while (peek().is("[")) {
      pos++;
      out.add(orExpr());
      expect("]");
    }
    return out;
  }

  private XPathExpr primary() {
    Tok t = next();
    switch (t.type) {
      case XPathLexer.kVar:
        return new XPathExpr.Variable(t.prefix, t.local);
      case XPathLexer.kLiteral:
        return new XPathExpr.Literal(t.local);
      case XPathLexer.kNumber:
        return new XPathExpr.Literal(Double.valueOf(Double.parseDouble(t.local)));
      default:
        break;
    }
    if (t.is("(")) {
      XPathExpr e = orExpr();
      expect(")");
      return e;
    }
    pos++;
    List<XPathExpr> args = new ArrayList<XPathExpr>();
    if (!peek().is(")")) {
      args.add(orExpr());
      while (peek().is(",")) {
        pos++;
        args.add(orExpr());
      }
    }
    expect(")");
    if (t.prefix == null) {
      if (!(xslt && isXsltFunction(t.local))) {
        XPathFunctions.check(t.local, args.size());
      }
    } else if (!lenient) {
      String uri = namespaces.getNamespaceURI(t.prefix);
      if (uri == null || uri.isEmpty()) {
        throw new XPathError("Prefix must resolve to a namespace: " + t.prefix);
      }
    }
    XPathExpr.Call call = new XPathExpr.Call(t.prefix, t.local, args);
    call.external = t.prefix == null && xslt && isXsltFunction(t.local);
    return call;
  }

  private static boolean isXsltFunction(String name) {
    switch (name) {
    case "current":
    case "generate-id":
    case "key":
    case "document":
    case "format-number":
    case "system-property":
    case "element-available":
    case "function-available":
    case "unparsed-entity-uri":
      return true;
    default:
      return false;
    }
  }
}
