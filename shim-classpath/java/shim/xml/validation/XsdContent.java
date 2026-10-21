/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.xml.validation;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import shim.xml.validation.XsdModel.Particle;
import shim.xml.validation.XsdModel.Wildcard;

/** A content model compiled to an NFA over element names; matching proceeds one child at a time. */
final class XsdContent {
  private static final int kUnrollLimit = 2000;

  private static final class Label {
    final String namespace;
    final String name;
    final XsdModel.Element decl;
    final Wildcard wildcard;
    Particle particle;
    int index;

    Label(XsdModel.Element decl, Wildcard wildcard) {
      this.decl = decl;
      this.wildcard = wildcard;
      this.namespace = decl == null ? null : decl.namespace;
      this.name = decl == null ? null : decl.name;
    }
  }

  private final XsdSchema schema;
  private final List<List<Label>> labels = new ArrayList<List<Label>>();
  private final List<List<Integer>> targets = new ArrayList<List<Integer>>();
  private final List<List<Integer>> epsilons = new ArrayList<List<Integer>>();
  private int start;
  private int accept;
  private int currentIndex = 1;
  private Particle all;

  /** The position in the content model reached so far. */
  static final class State {
    BitSet states;
    java.util.Set<String> seen = new java.util.HashSet<String>();
    boolean failed;
    Label last;
  }

  /** The outcome of offering a child element to the content model. */
  static final class Match {
    XsdModel.Element decl;
    Wildcard wildcard;
  }

  XsdContent(Particle root, XsdSchema schema) {
    this.schema = schema;
    if (root.kind == XsdModel.kAll) {
      this.all = root;
      return;
    }
    start = newState();
    int end = compile(root, start);
    accept = end;
  }

  private int newState() {
    labels.add(new ArrayList<Label>());
    targets.add(new ArrayList<Integer>());
    epsilons.add(new ArrayList<Integer>());
    return labels.size() - 1;
  }

  private void eps(int from, int to) {
    epsilons.get(from).add(Integer.valueOf(to));
  }

  private void trans(int from, Label label, int to) {
    labels.get(from).add(label);
    targets.get(from).add(Integer.valueOf(to));
  }

  private int compile(Particle p, int from) {
    int max = p.max;
    int min = p.min;
    if (max != XsdModel.kUnbounded && max > kUnrollLimit) {
      max = XsdModel.kUnbounded;
    }
    if (min > kUnrollLimit) {
      min = kUnrollLimit;
    }
    int cur = from;
    for (int i = 0; i < min; i++) {
      currentIndex = i + 1;
      cur = once(p, cur);
    }
    if (max == XsdModel.kUnbounded) {
      int loop = newState();
      eps(cur, loop);
      currentIndex = min + 1;
      int e = once(p, loop);
      eps(e, loop);
      return loop;
    }
    if (max > min) {
      int f = newState();
      for (int i = min; i < max; i++) {
        eps(cur, f);
        currentIndex = i + 1;
        cur = once(p, cur);
      }
      eps(cur, f);
      return f;
    }
    return cur;
  }

  private int once(Particle p, int from) {
    switch (p.kind) {
    case XsdModel.kElement: {
      XsdModel.Element decl = p.element != null ? p.element : schema.element(p.elementRef);
      if (decl == null) {
        decl = new XsdModel.Element();
        int brace = p.elementRef == null ? -1 : p.elementRef.indexOf('}');
        decl.name = p.elementRef == null ? "" : p.elementRef.substring(brace + 1);
        decl.namespace = brace > 0 ? p.elementRef.substring(1, brace) : "";
      }
      int to = newState();
      Label label = new Label(decl, null);
      label.particle = p;
      label.index = currentIndex;
      trans(from, label, to);
      return to;
    }
    case XsdModel.kAny: {
      int to = newState();
      trans(from, new Label(null, p.wildcard), to);
      return to;
    }
    case XsdModel.kSequence: {
      int cur = from;
      for (Particle c : p.children) {
        cur = compile(c, cur);
      }
      return cur;
    }
    case XsdModel.kChoice: {
      int end = newState();
      if (p.children.isEmpty()) {
        eps(from, end);
      }
      for (Particle c : p.children) {
        int s = newState();
        eps(from, s);
        int e = compile(c, s);
        eps(e, end);
      }
      return end;
    }
    case XsdModel.kGroupRef: {
      Particle g = schema.group(p.groupRef);
      if (g == null) {
        return from;
      }
      return once(g, from);
    }
    default:
      return from;
    }
  }

  private void closure(BitSet set) {
    List<Integer> stack = new ArrayList<Integer>();
    for (int i = set.nextSetBit(0); i >= 0; i = set.nextSetBit(i + 1)) {
      stack.add(Integer.valueOf(i));
    }
    while (!stack.isEmpty()) {
      int s = stack.remove(stack.size() - 1).intValue();
      for (Integer t : epsilons.get(s)) {
        if (!set.get(t.intValue())) {
          set.set(t.intValue());
          stack.add(t);
        }
      }
    }
  }

  State begin() {
    State st = new State();
    if (all == null) {
      st.states = new BitSet();
      st.states.set(start);
      closure(st.states);
    }
    return st;
  }

  private boolean substitutes(String headKey, XsdModel.Element candidate) {
    List<String> members = schema.substitutes.get(headKey);
    if (members == null) {
      return false;
    }
    for (String m : members) {
      if (m.equals(candidate.key()) || substitutes(m, candidate)) {
        return true;
      }
    }
    return false;
  }

  private Match matchLabel(Label l, String ns, String name) {
    Match m = new Match();
    if (l.decl != null) {
      if (l.decl.name.equals(name) && l.decl.namespace.equals(ns)) {
        m.decl = l.decl;
        return m;
      }
      XsdModel.Element cand = schema.element(XsdSchema.key(ns, name));
      if (cand != null && substitutes(l.decl.key(), cand)) {
        m.decl = cand;
        return m;
      }
      return null;
    }
    if (l.wildcard.allows(ns)) {
      m.wildcard = l.wildcard;
      return m;
    }
    return null;
  }

  /** Offers a child; returns null when the child is not allowed here. */
  Match step(State st, String ns, String name) {
    if (all != null) {
      for (Particle c : all.children) {
        XsdModel.Element decl = c.element != null ? c.element : schema.element(c.elementRef);
        if (decl != null && decl.name.equals(name) && decl.namespace.equals(ns)) {
          if (st.seen.contains(decl.key())) {
            return null;
          }
          st.seen.add(decl.key());
          Match m = new Match();
          m.decl = decl;
          return m;
        }
      }
      return null;
    }
    BitSet next = new BitSet();
    Match found = null;
    Label foundLabel = null;
    for (int s = st.states.nextSetBit(0); s >= 0; s = st.states.nextSetBit(s + 1)) {
      List<Label> ls = labels.get(s);
      for (int i = 0; i < ls.size(); i++) {
        Match m = matchLabel(ls.get(i), ns, name);
        if (m != null) {
          if (found == null || (found.decl == null && m.decl != null)) {
            found = m;
            foundLabel = ls.get(i);
          }
          next.set(targets.get(s).get(i).intValue());
        }
      }
    }
    if (found == null) {
      return null;
    }
    closure(next);
    st.states = next;
    st.last = foundLabel;
    return found;
  }

  private static boolean counted(Particle p) {
    return p != null && p.kind == XsdModel.kElement && (p.min > 1 || (p.max > 1 && p.max != XsdModel.kUnbounded));
  }

  /** The error for a child that the content model rejects at this point. */
  String unexpected(State st, String ns, String name, String shown) {
    if (all == null) {
      Label last = st.last;
      if (last != null && last.decl != null && counted(last.particle) && last.particle.max != XsdModel.kUnbounded
          && last.index == last.particle.max && last.decl.name.equals(name) && last.decl.namespace.equals(ns)) {
        String expected = expected(st);
        return "cvc-complex-type.2.4." + (expected.isEmpty() ? "f" : "e") + ": '" + display(ns, name)
            + "' can occur a maximum of '" + last.particle.max + "' times in the current sequence. This limit was "
            + "exceeded. " + (expected.isEmpty() ? "No child element is expected at this point."
                : "At this point one of '{" + expected + "}' is expected.");
      }
      for (int s = st.states.nextSetBit(0); s >= 0; s = st.states.nextSetBit(s + 1)) {
        for (Label l : labels.get(s)) {
          if (l.decl != null && counted(l.particle) && l.index > 1 && l.index <= l.particle.min) {
            int remaining = l.particle.min - (l.index - 1);
            return "cvc-complex-type.2.4." + (remaining == 1 ? "g" : "h") + ": Invalid content was found starting with "
                + "element '" + shown + "'. '" + display(l.decl.namespace, l.decl.name) + "' is expected to occur a "
                + "minimum of '" + l.particle.min + "' times in the current sequence. "
                + (remaining == 1 ? "One more instance is" : "'" + remaining + "' more instances are")
                + " required to satisfy this constraint.";
          }
        }
      }
    }
    String expected = expected(st);
    if (expected.isEmpty()) {
      return "cvc-complex-type.2.4.d: Invalid content was found starting with element '" + shown
          + "'. No child element is expected at this point.";
    }
    return "cvc-complex-type.2.4.a: Invalid content was found starting with element '" + shown + "'. One of '{"
        + expected + "}' is expected.";
  }

  /** The error for content that ends before the model is satisfied. */
  String incomplete(State st, String element) {
    if (all == null) {
      for (int s = st.states.nextSetBit(0); s >= 0; s = st.states.nextSetBit(s + 1)) {
        for (Label l : labels.get(s)) {
          if (l.decl != null && counted(l.particle) && l.index > 1 && l.index <= l.particle.min) {
            int remaining = l.particle.min - (l.index - 1);
            return "cvc-complex-type.2.4." + (remaining == 1 ? "i" : "j") + ": The content of element '" + element
                + "' is not complete. '" + display(l.decl.namespace, l.decl.name) + "' is expected to occur a minimum "
                + "of '" + l.particle.min + "' times. "
                + (remaining == 1 ? "One more instance is" : "'" + remaining + "' more instances are")
                + " required to satisfy this constraint.";
          }
        }
      }
    }
    return "cvc-complex-type.2.4.b: The content of element '" + element + "' is not complete. One of '{"
        + expected(st) + "}' is expected.";
  }

  boolean canEnd(State st) {
    if (all != null) {
      for (Particle c : all.children) {
        XsdModel.Element decl = c.element != null ? c.element : schema.element(c.elementRef);
        if (c.min > 0 && decl != null && !st.seen.contains(decl.key())) {
          return false;
        }
      }
      return all.min > 0 || !st.seen.isEmpty() || true;
    }
    return st.states.get(accept);
  }

  /** The element names acceptable at the current position, formatted for error messages. */
  String expected(State st) {
    Set<String> names = new LinkedHashSet<String>();
    if (all != null) {
      for (Particle c : all.children) {
        XsdModel.Element decl = c.element != null ? c.element : schema.element(c.elementRef);
        if (decl != null && !st.seen.contains(decl.key())) {
          names.add(display(decl.namespace, decl.name));
        }
      }
    } else {
      for (int s = st.states.nextSetBit(0); s >= 0; s = st.states.nextSetBit(s + 1)) {
        for (Label l : labels.get(s)) {
          if (l.decl != null) {
            names.add(display(l.decl.namespace, l.decl.name));
            List<String> members = schema.substitutes.get(l.decl.key());
            if (members != null) {
              for (String m : members) {
                XsdModel.Element me = schema.element(m);
                if (me != null) {
                  names.add(display(me.namespace, me.name));
                }
              }
            }
          } else {
            names.add("WC[" + l.wildcard.namespace + "]");
          }
        }
      }
    }
    return names.isEmpty() ? "" : String.join(", ", names);
  }

  private static String display(String ns, String name) {
    return ns == null || ns.isEmpty() ? name : "\"" + ns + "\":" + name;
  }
}
