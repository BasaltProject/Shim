/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.xml.validation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import shim.xml.validation.XsdModel.Attribute;
import shim.xml.validation.XsdModel.AttributeUse;
import shim.xml.validation.XsdModel.Complex;
import shim.xml.validation.XsdModel.Particle;
import shim.xml.validation.XsdModel.Wildcard;

/** A set of schema documents combined into one lookup structure, with components built on demand. */
final class XsdSchema {
  static final String kXs = XsdSimple.kXs;

  interface Loader {
    Document load(String publicId, String systemId, String base, String namespace) throws SAXException, IOException;
  }

  private final Map<String, Element> typeNodes = new HashMap<String, Element>();
  private final Map<String, Element> elementNodes = new HashMap<String, Element>();
  private final Map<String, Element> groupNodes = new HashMap<String, Element>();
  private final Map<String, Element> attributeGroupNodes = new HashMap<String, Element>();
  private final Map<String, Element> attributeNodes = new HashMap<String, Element>();
  private final Map<String, Object> types = new HashMap<String, Object>();
  private final Map<String, XsdModel.Element> elements = new HashMap<String, XsdModel.Element>();
  private final Map<String, Attribute> attributes = new HashMap<String, Attribute>();
  private final Map<Element, Doc> docs = new IdentityHashMap<Element, Doc>();
  private final Set<String> loaded = new HashSet<String>();
  private final Set<String> building = new HashSet<String>();
  private final Loader loader;
  final Map<String, List<String>> substitutes = new HashMap<String, List<String>>();
  final Set<String> knownNamespaces = new HashSet<String>();

  private static final class Doc {
    String targetNamespace = "";
    boolean elementQualified;
    boolean attributeQualified;
  }

  XsdSchema(Loader loader) {
    this.loader = loader;
  }

  static String key(String ns, String local) {
    return ns == null || ns.isEmpty() ? local : "{" + ns + "}" + local;
  }

  void add(Document doc, String systemId) throws SAXException, IOException {
    Element root = doc.getDocumentElement();
    if (root != null && !kXs.equals(root.getNamespaceURI())) {
      throw problem(root, systemId, "s4s-elt-schema-ns: The namespace of element '" + root.getNodeName()
          + "' must be from the schema namespace, '" + kXs + "'.");
    }
    if (root == null || !isXs(root, "schema")) {
      throw problem(root, systemId, "s4s-elt-must-match.1: The content of '" + (root == null ? "" : root.getNodeName())
          + "' must match (annotation?, (simpleType | complexType)?)), but a problem was found.");
    }
    Doc d = new Doc();
    d.targetNamespace = root.getAttribute("targetNamespace");
    d.elementQualified = "qualified".equals(root.getAttribute("elementFormDefault"));
    d.attributeQualified = "qualified".equals(root.getAttribute("attributeFormDefault"));
    docs.put(root, d);
    knownNamespaces.add(d.targetNamespace);
    for (Node c = root.getFirstChild(); c != null; c = c.getNextSibling()) {
      if (c.getNodeType() != Node.ELEMENT_NODE || !kXs.equals(c.getNamespaceURI())) {
        continue;
      }
      Element e = (Element) c;
      String local = e.getLocalName();
      String name = e.getAttribute("name");
      String k = key(d.targetNamespace, name);
      switch (local) {
      case "simpleType":
      case "complexType":
        typeNodes.put(k, e);
        break;
      case "element":
        elementNodes.put(k, e);
        break;
      case "group":
        groupNodes.put(k, e);
        break;
      case "attributeGroup":
        attributeGroupNodes.put(k, e);
        break;
      case "attribute":
        attributeNodes.put(k, e);
        break;
      case "import":
      case "include": {
        String loc = e.getAttribute("schemaLocation");
        if (!loc.isEmpty() && loader != null) {
          String ns = local.equals("import") ? e.getAttribute("namespace") : d.targetNamespace;
          Document sub = loader.load(null, loc, systemId, ns);
          if (sub != null && loaded.add(System.identityHashCode(sub) + loc)) {
            add(sub, loc);
          }
        }
        break;
      }
      default:
        break;
      }
    }
  }

  /** Verifies every cross reference of the loaded documents, throwing the first unresolved one. */
  void check(String systemId) throws SAXParseException {
    for (Element root : new ArrayList<Element>(docs.keySet())) {
      checkElement(root, systemId);
    }
  }

  private void checkElement(Element e, String systemId) throws SAXParseException {
    if (kXs.equals(e.getNamespaceURI())) {
      String local = e.getLocalName();
      switch (local) {
      case "element":
        if (e.hasAttribute("ref")) {
          reference(e, "ref", "element declaration", systemId);
        }
        if (e.hasAttribute("type")) {
          reference(e, "type", "type definition", systemId);
        }
        if (e.hasAttribute("substitutionGroup")) {
          reference(e, "substitutionGroup", "element declaration", systemId);
        }
        break;
      case "attribute":
        if (e.hasAttribute("ref")) {
          reference(e, "ref", "attribute declaration", systemId);
        }
        if (e.hasAttribute("type")) {
          reference(e, "type", "type definition", systemId);
        }
        break;
      case "extension":
      case "restriction":
        if (e.hasAttribute("base")) {
          reference(e, "base", "type definition", systemId);
        }
        break;
      case "list":
        if (e.hasAttribute("itemType")) {
          reference(e, "itemType", "type definition", systemId);
        }
        break;
      case "union":
        if (e.hasAttribute("memberTypes")) {
          for (String m : e.getAttribute("memberTypes").trim().split("\\s+")) {
            resolve(e, m, "type definition", systemId);
          }
        }
        break;
      case "group":
        if (e.hasAttribute("ref")) {
          reference(e, "ref", "model group definition", systemId);
        }
        break;
      case "attributeGroup":
        if (e.hasAttribute("ref")) {
          reference(e, "ref", "attribute group definition", systemId);
        }
        break;
      default:
        break;
      }
    }
    for (Node c = e.getFirstChild(); c != null; c = c.getNextSibling()) {
      if (c.getNodeType() == Node.ELEMENT_NODE) {
        checkElement((Element) c, systemId);
      }
    }
  }

  private void reference(Element e, String attribute, String kind, String systemId) throws SAXParseException {
    resolve(e, e.getAttribute(attribute), kind, systemId);
  }

  private void resolve(Element e, String value, String kind, String systemId) throws SAXParseException {
    String k = qname(e, value);
    boolean found;
    switch (kind) {
    case "type definition":
      found = typeNodes.containsKey(k) || k.startsWith("{" + kXs + "}") && builtinOrAny(k.substring(kXs.length() + 2)) != null;
      break;
    case "element declaration":
      found = elementNodes.containsKey(k);
      break;
    case "attribute declaration":
      found = attributeNodes.containsKey(k);
      break;
    case "model group definition":
      found = groupNodes.containsKey(k);
      break;
    default:
      found = attributeGroupNodes.containsKey(k);
      break;
    }
    if (!found) {
      throw problem(e, systemId, "src-resolve: Cannot resolve the name '" + value.trim() + "' to a(n) '" + kind
          + "' component.");
    }
  }

  static boolean isXs(Node n, String local) {
    return n.getNodeType() == Node.ELEMENT_NODE && kXs.equals(n.getNamespaceURI()) && local.equals(n.getLocalName());
  }

  private Doc docOf(Element e) {
    Node root = e.getOwnerDocument().getDocumentElement();
    Doc d = docs.get(root);
    return d == null ? new Doc() : d;
  }

  XsdModel.Element element(String key) {
    XsdModel.Element e = elements.get(key);
    if (e == null) {
      Element node = elementNodes.get(key);
      if (node != null) {
        e = buildElement(node, true);
        elements.put(key, e);
      }
    }
    return e;
  }

  Object type(String key) {
    if (key.startsWith("{" + kXs + "}")) {
      return builtinOrAny(key.substring(kXs.length() + 2));
    }
    Object t = types.get(key);
    if (t == null) {
      Element node = typeNodes.get(key);
      if (node == null) {
        return null;
      }
      if (!building.add(key)) {
        return types.get(key);
      }
      try {
        t = isXs(node, "simpleType") ? buildSimple(node, node.getAttribute("name"), null)
            : buildComplex(node, node.getAttribute("name"), null);
        types.put(key, t);
      } finally {
        building.remove(key);
      }
    }
    return t;
  }

  private static final Complex kAnyType = anyType();

  private static Complex anyType() {
    Complex c = new Complex();
    c.name = "anyType";
    c.namespace = kXs;
    c.mixed = true;
    c.content = XsdModel.kMixed;
    c.anyType = true;
    Particle seq = new Particle();
    seq.kind = XsdModel.kSequence;
    seq.min = 1;
    seq.max = 1;
    Particle any = new Particle();
    any.kind = XsdModel.kAny;
    any.min = 0;
    any.max = XsdModel.kUnbounded;
    any.wildcard = new Wildcard();
    any.wildcard.process = "lax";
    seq.children.add(any);
    c.particle = seq;
    c.anyAttribute = new Wildcard();
    c.anyAttribute.process = "lax";
    return c;
  }

  static Object builtinOrAny(String local) {
    if (local.equals("anyType")) {
      return kAnyType;
    }
    return XsdSimple.builtin(local);
  }

  Attribute attribute(String key) {
    Attribute a = attributes.get(key);
    if (a == null) {
      Element node = attributeNodes.get(key);
      if (node != null) {
        a = buildAttribute(node, true);
        attributes.put(key, a);
      }
    }
    return a;
  }

  String qname(Element scope, String value) {
    value = value.trim();
    int colon = value.indexOf(':');
    String prefix = colon < 0 ? null : value.substring(0, colon);
    String local = colon < 0 ? value : value.substring(colon + 1);
    String ns = scope.lookupNamespaceURI(prefix);
    if (ns == null) {
      ns = "";
    }
    return key(ns, local);
  }

  private static int occurs(Element e, String attribute, int dflt) {
    String v = e.getAttribute(attribute);
    if (v.isEmpty()) {
      return dflt;
    }
    if (v.equals("unbounded")) {
      return XsdModel.kUnbounded;
    }
    return Integer.parseInt(v.trim());
  }

  private XsdModel.Element buildElement(Element e, boolean global) {
    Doc d = docOf(e);
    XsdModel.Element el = new XsdModel.Element();
    el.name = e.getAttribute("name");
    el.global = global;
    boolean qualified = global || "qualified".equals(e.getAttribute("form"))
        || (!e.hasAttribute("form") && d.elementQualified);
    el.namespace = qualified ? d.targetNamespace : "";
    if (e.hasAttribute("type")) {
      el.typeKey = qname(e, e.getAttribute("type"));
    }
    el.nillable = "true".equals(e.getAttribute("nillable"));
    el.abstractElement = "true".equals(e.getAttribute("abstract"));
    if (e.hasAttribute("default")) {
      el.defaultValue = e.getAttribute("default");
    }
    if (e.hasAttribute("fixed")) {
      el.fixed = e.getAttribute("fixed");
    }
    if (e.hasAttribute("substitutionGroup")) {
      el.substitutionGroup = qname(e, e.getAttribute("substitutionGroup"));
      List<String> list = substitutes.get(el.substitutionGroup);
      if (list == null) {
        list = new ArrayList<String>();
        substitutes.put(el.substitutionGroup, list);
      }
      list.add(el.key());
    }
    for (Node c = e.getFirstChild(); c != null; c = c.getNextSibling()) {
      if (isXs(c, "simpleType")) {
        el.inlineType = buildSimple((Element) c, "", el.name);
      } else if (isXs(c, "complexType")) {
        el.inlineType = buildComplex((Element) c, "", el.name);
      }
    }
    return el;
  }

  private Attribute buildAttribute(Element e, boolean global) {
    Doc d = docOf(e);
    Attribute a = new Attribute();
    a.name = e.getAttribute("name");
    boolean qualified = global || "qualified".equals(e.getAttribute("form"))
        || (!e.hasAttribute("form") && d.attributeQualified);
    a.namespace = qualified ? d.targetNamespace : "";
    if (e.hasAttribute("type")) {
      a.typeKey = qname(e, e.getAttribute("type"));
    }
    for (Node c = e.getFirstChild(); c != null; c = c.getNextSibling()) {
      if (isXs(c, "simpleType")) {
        a.inlineType = buildSimple((Element) c, "", a.name);
      }
    }
    return a;
  }

  XsdSimple simpleOf(Attribute a) {
    if (a.inlineType != null) {
      return a.inlineType;
    }
    if (a.typeKey != null) {
      Object t = type(a.typeKey);
      if (t instanceof XsdSimple) {
        return (XsdSimple) t;
      }
    }
    return XsdSimple.builtin("anySimpleType");
  }

  Object typeOf(XsdModel.Element e) {
    if (e.inlineType != null) {
      return e.inlineType;
    }
    if (e.typeKey != null) {
      Object t = type(e.typeKey);
      return t == null ? kAnyType : t;
    }
    if (e.substitutionGroup != null) {
      XsdModel.Element head = element(e.substitutionGroup);
      if (head != null && head != e) {
        return typeOf(head);
      }
    }
    return kAnyType;
  }

  private XsdSimple resolveSimple(Element scope, String qualified) {
    Object t = type(qname(scope, qualified));
    return t instanceof XsdSimple ? (XsdSimple) t : XsdSimple.builtin("anySimpleType");
  }

  XsdSimple buildSimple(Element st, String name, String owner) {
    Doc d = docOf(st);
    XsdSimple t = new XsdSimple(d.targetNamespace, name);
    t.anonymous = name.isEmpty();
    t.anonymousOwner = owner;
    for (Node c = st.getFirstChild(); c != null; c = c.getNextSibling()) {
      if (isXs(c, "restriction")) {
        Element r = (Element) c;
        XsdSimple base = null;
        if (r.hasAttribute("base")) {
          base = resolveSimple(r, r.getAttribute("base"));
        } else {
          for (Node x = r.getFirstChild(); x != null; x = x.getNextSibling()) {
            if (isXs(x, "simpleType")) {
              base = buildSimple((Element) x, "", owner);
            }
          }
        }
        if (base == null) {
          base = XsdSimple.builtin("anySimpleType");
        }
        t.base = base;
        t.variety = base.variety;
        t.lexical = base.lexical;
        t.members.addAll(base.members);
        t.idKind = base.idKind;
        t.idrefKind = base.idrefKind;
        facets(r, t);
      } else if (isXs(c, "list")) {
        Element l = (Element) c;
        t.base = XsdSimple.builtin("anySimpleType");
        t.variety = XsdSimple.Variety.LIST;
        if (l.hasAttribute("itemType")) {
          t.itemType = resolveSimple(l, l.getAttribute("itemType"));
        } else {
          for (Node x = l.getFirstChild(); x != null; x = x.getNextSibling()) {
            if (isXs(x, "simpleType")) {
              t.itemType = buildSimple((Element) x, "", owner);
            }
          }
        }
      } else if (isXs(c, "union")) {
        Element u = (Element) c;
        t.base = XsdSimple.builtin("anySimpleType");
        t.variety = XsdSimple.Variety.UNION;
        if (u.hasAttribute("memberTypes")) {
          for (String m : u.getAttribute("memberTypes").trim().split("\\s+")) {
            t.members.add(resolveSimple(u, m));
          }
        }
        for (Node x = u.getFirstChild(); x != null; x = x.getNextSibling()) {
          if (isXs(x, "simpleType")) {
            t.members.add(buildSimple((Element) x, "", owner));
          }
        }
      }
    }
    return t;
  }

  private void facets(Element r, XsdSimple t) {
    List<String> patterns = new ArrayList<String>();
    for (Node c = r.getFirstChild(); c != null; c = c.getNextSibling()) {
      if (c.getNodeType() != Node.ELEMENT_NODE || !kXs.equals(c.getNamespaceURI())) {
        continue;
      }
      Element f = (Element) c;
      String v = f.getAttribute("value");
      switch (f.getLocalName()) {
      case "length":
        t.length = Integer.valueOf(v.trim());
        break;
      case "minLength":
        t.minLength = Integer.valueOf(v.trim());
        break;
      case "maxLength":
        t.maxLength = Integer.valueOf(v.trim());
        break;
      case "totalDigits":
        t.totalDigits = Integer.valueOf(v.trim());
        break;
      case "fractionDigits":
        t.fractionDigits = Integer.valueOf(v.trim());
        break;
      case "minInclusive":
        t.minInclusive = v.trim();
        break;
      case "maxInclusive":
        t.maxInclusive = v.trim();
        break;
      case "minExclusive":
        t.minExclusive = v.trim();
        break;
      case "maxExclusive":
        t.maxExclusive = v.trim();
        break;
      case "whiteSpace":
        t.whiteSpace = v.equals("preserve") ? XsdSimple.kPreserve : v.equals("replace") ? XsdSimple.kReplace
            : XsdSimple.kCollapse;
        break;
      case "enumeration":
        if (t.enumeration == null) {
          t.enumeration = new ArrayList<String>();
        }
        t.enumeration.add(v);
        break;
      case "pattern":
        patterns.add(v);
        break;
      default:
        break;
      }
    }
    if (!patterns.isEmpty()) {
      t.addPatternGroup(patterns);
    }
  }

  Complex buildComplex(Element ct, String name, String owner) {
    Doc d = docOf(ct);
    Complex c = new Complex();
    c.name = name;
    c.namespace = d.targetNamespace;
    c.anonymous = name.isEmpty();
    c.owner = owner;
    c.mixed = "true".equals(ct.getAttribute("mixed"));
    c.abstractType = "true".equals(ct.getAttribute("abstract"));
    Element body = null;
    for (Node x = ct.getFirstChild(); x != null; x = x.getNextSibling()) {
      if (isXs(x, "simpleContent") || isXs(x, "complexContent")) {
        body = (Element) x;
      }
    }
    if (body == null) {
      modelContent(ct, c);
      c.content = c.particle == null ? XsdModel.kEmpty : c.mixed ? XsdModel.kMixed : XsdModel.kElementOnly;
      return c;
    }
    Element derivation = null;
    for (Node x = body.getFirstChild(); x != null; x = x.getNextSibling()) {
      if (isXs(x, "extension") || isXs(x, "restriction")) {
        derivation = (Element) x;
      }
    }
    if (derivation == null) {
      return c;
    }
    boolean extension = isXs(derivation, "extension");
    String baseKey = qname(derivation, derivation.getAttribute("base"));
    Object base = type(baseKey);
    if (isXs(body, "simpleContent")) {
      c.content = XsdModel.kSimple;
      if (base instanceof Complex) {
        Complex bc = (Complex) base;
        c.simple = bc.simple;
        c.attributes.addAll(bc.attributes);
        c.anyAttribute = bc.anyAttribute;
      } else if (base instanceof XsdSimple) {
        c.simple = (XsdSimple) base;
      }
      if (!extension) {
        XsdSimple restricted = new XsdSimple(d.targetNamespace, "");
        restricted.anonymous = true;
        restricted.anonymousOwner = owner;
        restricted.base = c.simple == null ? XsdSimple.builtin("anySimpleType") : c.simple;
        restricted.variety = restricted.base.variety;
        restricted.lexical = restricted.base.lexical;
        restricted.members.addAll(restricted.base.members);
        facets(derivation, restricted);
        for (Node x = derivation.getFirstChild(); x != null; x = x.getNextSibling()) {
          if (isXs(x, "simpleType")) {
            restricted.base = buildSimple((Element) x, "", owner);
            restricted.lexical = restricted.base.lexical;
          }
        }
        c.simple = restricted;
      }
      attributeContent(derivation, c, !extension);
      return c;
    }
    c.mixed = c.mixed || "true".equals(body.getAttribute("mixed"));
    Complex bc = base instanceof Complex ? (Complex) base : null;
    c.base = bc;
    if (extension) {
      if (bc != null) {
        c.attributes.addAll(bc.attributes);
        c.anyAttribute = bc.anyAttribute;
      }
      Complex own = new Complex();
      modelContent(derivation, own);
      Particle merged;
      if (bc != null && bc.particle != null && own.particle != null) {
        merged = new Particle();
        merged.kind = XsdModel.kSequence;
        merged.children.add(bc.particle);
        merged.children.add(own.particle);
      } else {
        merged = own.particle != null ? own.particle : bc == null ? null : bc.particle;
      }
      c.particle = merged;
      if (bc != null && bc.mixed) {
        c.mixed = true;
      }
      attributeContent(derivation, c, false);
    } else {
      Complex own = new Complex();
      modelContent(derivation, own);
      c.particle = own.particle;
      if (bc != null) {
        c.attributes.addAll(bc.attributes);
        c.anyAttribute = bc.anyAttribute;
      }
      attributeContent(derivation, c, true);
    }
    c.content = c.particle == null ? (c.mixed ? XsdModel.kMixed : XsdModel.kEmpty)
        : c.mixed ? XsdModel.kMixed : XsdModel.kElementOnly;
    return c;
  }

  private void modelContent(Element parent, Complex c) {
    for (Node x = parent.getFirstChild(); x != null; x = x.getNextSibling()) {
      if (isXs(x, "sequence") || isXs(x, "choice") || isXs(x, "all") || isXs(x, "group")) {
        c.particle = particle((Element) x);
      }
    }
    attributeContent(parent, c, false);
  }

  private void attributeContent(Element parent, Complex c, boolean replace) {
    for (Node x = parent.getFirstChild(); x != null; x = x.getNextSibling()) {
      if (isXs(x, "attribute")) {
        attributeUse((Element) x, c);
      } else if (isXs(x, "attributeGroup")) {
        attributeGroup((Element) x, c);
      } else if (isXs(x, "anyAttribute")) {
        Wildcard w = new Wildcard();
        Element e = (Element) x;
        w.namespace = e.hasAttribute("namespace") ? e.getAttribute("namespace") : "##any";
        w.process = e.hasAttribute("processContents") ? e.getAttribute("processContents") : "strict";
        w.targetNamespace = docOf(e).targetNamespace;
        c.anyAttribute = w;
      }
    }
  }

  private void attributeGroup(Element ref, Complex c) {
    Element node = attributeGroupNodes.get(qname(ref, ref.getAttribute("ref")));
    if (node != null) {
      attributeContent(node, c, false);
    }
  }

  private void attributeUse(Element e, Complex c) {
    AttributeUse use = new AttributeUse();
    if (e.hasAttribute("ref")) {
      String k = qname(e, e.getAttribute("ref"));
      Attribute decl = attribute(k);
      if (decl == null) {
        return;
      }
      use.decl = decl;
    } else {
      use.decl = buildAttribute(e, false);
    }
    use.type = simpleOf(use.decl);
    if (e.hasAttribute("use")) {
      use.use = e.getAttribute("use");
    }
    if (e.hasAttribute("default")) {
      use.defaultValue = e.getAttribute("default");
    }
    if (e.hasAttribute("fixed")) {
      use.fixed = e.getAttribute("fixed");
    }
    for (int i = 0; i < c.attributes.size(); i++) {
      AttributeUse old = c.attributes.get(i);
      if (old.decl.name.equals(use.decl.name) && old.decl.namespace.equals(use.decl.namespace)) {
        c.attributes.set(i, use);
        return;
      }
    }
    c.attributes.add(use);
  }

  private Particle particle(Element e) {
    Particle p = new Particle();
    p.min = occurs(e, "minOccurs", 1);
    p.max = occurs(e, "maxOccurs", 1);
    String local = e.getLocalName();
    switch (local) {
    case "sequence":
    case "choice":
    case "all":
      p.kind = local.equals("sequence") ? XsdModel.kSequence : local.equals("choice") ? XsdModel.kChoice : XsdModel.kAll;
      for (Node x = e.getFirstChild(); x != null; x = x.getNextSibling()) {
        if (x.getNodeType() == Node.ELEMENT_NODE && kXs.equals(x.getNamespaceURI())
            && !isXs(x, "annotation")) {
          p.children.add(particle((Element) x));
        }
      }
      break;
    case "group":
      p.kind = XsdModel.kGroupRef;
      p.groupRef = qname(e, e.getAttribute("ref"));
      break;
    case "any": {
      p.kind = XsdModel.kAny;
      Wildcard w = new Wildcard();
      w.namespace = e.hasAttribute("namespace") ? e.getAttribute("namespace") : "##any";
      w.process = e.hasAttribute("processContents") ? e.getAttribute("processContents") : "strict";
      w.targetNamespace = docOf(e).targetNamespace;
      p.wildcard = w;
      break;
    }
    default:
      p.kind = XsdModel.kElement;
      if (e.hasAttribute("ref")) {
        p.elementRef = qname(e, e.getAttribute("ref"));
      } else {
        p.element = buildElement(e, false);
      }
      break;
    }
    return p;
  }

  Particle group(String key) {
    Element node = groupNodes.get(key);
    if (node == null) {
      return null;
    }
    for (Node x = node.getFirstChild(); x != null; x = x.getNextSibling()) {
      if (isXs(x, "sequence") || isXs(x, "choice") || isXs(x, "all")) {
        return particle((Element) x);
      }
    }
    return null;
  }

  static Document parse(Source source, Loader loader) throws SAXException, IOException {
    if (source instanceof DOMSource && ((DOMSource) source).getNode() instanceof Document) {
      return (Document) ((DOMSource) source).getNode();
    }
    InputSource is = SAXSource.sourceToInputSource(source);
    shim.xml.SaxReader reader = new shim.xml.SaxReader(true);
    reader.setFeature("http://xml.org/sax/features/namespaces", true);
    reader.setFeature("http://xml.org/sax/features/namespace-prefixes", true);
    shim.xml.dom.DomBuilder builder = new shim.xml.dom.DomBuilder(reader, true, true, false, false, false, null);
    builder.recordPositions();
    reader.setContentHandler(builder);
    reader.setProperty("http://xml.org/sax/properties/lexical-handler", builder);
    reader.setErrorHandler(new org.xml.sax.helpers.DefaultHandler() {
      public void fatalError(SAXParseException e) throws SAXException {
        System.err.println("[Fatal Error] :" + e.getLineNumber() + ":" + e.getColumnNumber() + ": " + e.getMessage());
        throw e;
      }
    });
    reader.parse(is);
    return builder.document();
  }

  static SAXParseException problem(Node node, String systemId, String message) {
    Object pos = node == null ? null : node.getUserData("shim.position");
    int line = -1;
    int column = -1;
    if (pos instanceof int[]) {
      line = ((int[]) pos)[0];
      column = ((int[]) pos)[1];
    }
    return new SAXParseException(message, null, systemId, line, column);
  }
}
