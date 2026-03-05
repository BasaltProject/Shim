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
import javax.xml.namespace.QName;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import shim.xml.xpath.XPathValues.NodeSet;

abstract class XPathExpr {
  abstract Object eval(XPathContext c);

  static NodeSet nodes(Object v) {
    if (!(v instanceof NodeSet)) {
      throw new XPathError(XPathError.kXPath, "Can not convert " + typeName(v) + " to a NodeList!");
    }
    return (NodeSet) v;
  }

  static String typeName(Object v) {
    if (v instanceof Double) {
      return "#NUMBER";
    }
    if (v instanceof Boolean) {
      return "#BOOLEAN";
    }
    return "#STRING";
  }

  static final class Literal extends XPathExpr {
    final Object value;

    Literal(Object value) {
      this.value = value;
    }

    Object eval(XPathContext c) {
      return value;
    }
  }

  static final class Variable extends XPathExpr {
    final String prefix;
    final String local;

    Variable(String prefix, String local) {
      this.prefix = prefix;
      this.local = local;
    }

    Object eval(XPathContext c) {
      if (c.shared.variables == null) {
        throw new XPathError("Attempting to resolve variable " + local + ", but a variable resolver is not set.");
      }
      String uri = "";
      if (prefix != null && c.shared.namespaces != null) {
        String u = c.shared.namespaces.getNamespaceURI(prefix);
        uri = u == null ? "" : u;
      }
      Object v = c.shared.variables.resolveVariable(new QName(uri, local));
      if (v == null) {
        throw new XPathError("resolveVariable for variable " + (uri.isEmpty() ? local : "{" + uri + "}" + local)
            + " returning null");
      }
      return fromJava(v, c);
    }
  }

  static Object fromJava(Object v, XPathContext c) {
    if (v instanceof Boolean || v instanceof String || v instanceof Double) {
      return v;
    }
    if (v instanceof Number) {
      return Double.valueOf(((Number) v).doubleValue());
    }
    if (v instanceof Node) {
      NodeSet s = new NodeSet();
      s.add((Node) v);
      return s;
    }
    if (v instanceof NodeList) {
      NodeSet s = new NodeSet();
      NodeList l = (NodeList) v;
      for (int i = 0; i < l.getLength(); i++) {
        s.add(l.item(i));
      }
      return s;
    }
    return String.valueOf(v);
  }

  static final class Unary extends XPathExpr {
    final XPathExpr operand;

    Unary(XPathExpr operand) {
      this.operand = operand;
    }

    Object eval(XPathContext c) {
      return Double.valueOf(-XPathValues.number(operand.eval(c), c));
    }
  }

  static final class Binary extends XPathExpr {
    final String op;
    final XPathExpr left;
    final XPathExpr right;

    Binary(String op, XPathExpr left, XPathExpr right) {
      this.op = op;
      this.left = left;
      this.right = right;
    }

    Object eval(XPathContext c) {
      if (op.equals("or")) {
        return Boolean.valueOf(XPathValues.bool(left.eval(c)) || XPathValues.bool(right.eval(c)));
      }
      if (op.equals("and")) {
        return Boolean.valueOf(XPathValues.bool(left.eval(c)) && XPathValues.bool(right.eval(c)));
      }
      Object a = left.eval(c);
      Object b = right.eval(c);
      switch (op) {
        case "=":
          return Boolean.valueOf(XPathValues.equalsOp(a, b, c, false));
        case "!=":
          return Boolean.valueOf(XPathValues.equalsOp(a, b, c, true));
        case "<":
        case "<=":
        case ">":
        case ">=":
          return Boolean.valueOf(XPathValues.relational(op, a, b, c));
        default:
          break;
      }
      double x = XPathValues.number(a, c);
      double y = XPathValues.number(b, c);
      switch (op) {
        case "+":
          return Double.valueOf(x + y);
        case "-":
          return Double.valueOf(x - y);
        case "*":
          return Double.valueOf(x * y);
        case "div":
          return Double.valueOf(x / y);
        default:
          return Double.valueOf(x % y);
      }
    }
  }

  static final class Union extends XPathExpr {
    final XPathExpr left;
    final XPathExpr right;

    Union(XPathExpr left, XPathExpr right) {
      this.left = left;
      this.right = right;
    }

    Object eval(XPathContext c) {
      List<Node> all = new ArrayList<Node>(nodes(left.eval(c)));
      all.addAll(nodes(right.eval(c)));
      NodeSet set = XPathValues.unique(all);
      XPathValues.sort(set, c);
      return set;
    }
  }

  static final class Filter extends XPathExpr {
    final XPathExpr primary;
    final List<XPathExpr> predicates;

    Filter(XPathExpr primary, List<XPathExpr> predicates) {
      this.primary = primary;
      this.predicates = predicates;
    }

    Object eval(XPathContext c) {
      NodeSet set = nodes(primary.eval(c));
      XPathValues.sort(set, c);
      List<Node> cur = set;
      for (XPathExpr p : predicates) {
        cur = XPathSteps.applyPredicate(cur, p, c);
      }
      NodeSet out = new NodeSet();
      out.addAll(cur);
      return out;
    }
  }

  static final class Path extends XPathExpr {
    final XPathExpr start;
    final boolean absolute;
    final List<XPathSteps.Step> steps;

    Path(XPathExpr start, boolean absolute, List<XPathSteps.Step> steps) {
      this.start = start;
      this.absolute = absolute;
      this.steps = steps;
    }

    Object eval(XPathContext c) {
      List<Node> cur;
      if (start != null) {
        cur = nodes(start.eval(c));
      } else if (absolute) {
        if (!c.hasContext || c.node == null) {
          throw new XPathError("Unable to evaluate expression using this context");
        }
        Node root = c.node;
        for (Node p = c.parent(root); p != null; p = c.parent(p)) {
          root = p;
        }
        cur = new ArrayList<Node>();
        cur.add(root);
      } else {
        if (!c.hasContext || c.node == null) {
          throw new XPathError("The context can not be null when the operation is context-dependent.");
        }
        cur = new ArrayList<Node>();
        cur.add(c.node);
      }
      for (XPathSteps.Step s : steps) {
        cur = XPathSteps.apply(s, cur, c);
      }
      NodeSet out = new NodeSet();
      out.addAll(cur);
      if (steps.isEmpty() && start != null) {
        XPathValues.sort(out, c);
      }
      return out;
    }
  }

  static final class Call extends XPathExpr {
    final String prefix;
    final String local;
    final List<XPathExpr> args;

    boolean external;

    Call(String prefix, String local, List<XPathExpr> args) {
      this.prefix = prefix;
      this.local = local;
      this.args = args;
    }

    Object eval(XPathContext c) {
      if (prefix != null || external) {
        return XPathFunctions.extension(this, c);
      }
      return XPathFunctions.call(local, args, c);
    }
  }
}
