/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.xml.validation;

import java.util.ArrayList;
import java.util.List;

/** The component model of a compiled schema. */
final class XsdModel {
  private XsdModel() {
  }

  static final int kUnbounded = -1;

  static final class Wildcard {
    String namespace = "##any";
    String process = "strict";
    String targetNamespace = "";

    boolean allows(String ns) {
      if (ns == null) {
        ns = "";
      }
      String spec = namespace.trim();
      if (spec.equals("##any")) {
        return true;
      }
      if (spec.equals("##other")) {
        return !ns.isEmpty() && !ns.equals(targetNamespace);
      }
      for (String t : spec.split("\\s+")) {
        if (t.equals("##local") && ns.isEmpty()) {
          return true;
        }
        if (t.equals("##targetNamespace") && ns.equals(targetNamespace)) {
          return true;
        }
        if (t.equals(ns)) {
          return true;
        }
      }
      return false;
    }
  }

  static final class Element {
    String name;
    String namespace = "";
    String typeKey;
    Object inlineType;
    boolean nillable;
    boolean abstractElement;
    String defaultValue;
    String fixed;
    String substitutionGroup;
    boolean global;
    String key() {
      return XsdSchema.key(namespace, name);
    }
  }

  static final class Attribute {
    String name;
    String namespace = "";
    String typeKey;
    XsdSimple inlineType;
  }

  static final class AttributeUse {
    Attribute decl;
    String use = "optional";
    String defaultValue;
    String fixed;
    XsdSimple type;
  }

  static final int kEmpty = 0;
  static final int kSimple = 1;
  static final int kElementOnly = 2;
  static final int kMixed = 3;

  static final class Complex {
    String name;
    String namespace = "";
    boolean anonymous;
    String owner;
    boolean mixed;
    int content = kElementOnly;
    Particle particle;
    XsdSimple simple;
    final List<AttributeUse> attributes = new ArrayList<AttributeUse>();
    Wildcard anyAttribute;
    boolean abstractType;
    Complex base;
    boolean anyType;
    XsdContent compiled;
  }

  static final int kElement = 0;
  static final int kSequence = 1;
  static final int kChoice = 2;
  static final int kAll = 3;
  static final int kAny = 4;
  static final int kGroupRef = 5;

  static final class Particle {
    int kind;
    int min = 1;
    int max = 1;
    final List<Particle> children = new ArrayList<Particle>();
    Element element;
    String elementRef;
    String groupRef;
    Wildcard wildcard;
  }
}
