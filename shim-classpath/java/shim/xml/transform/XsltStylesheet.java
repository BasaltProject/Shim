/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.xml.transform;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import shim.xml.xpath.XsltXPath;

/** A compiled XSLT 1.0 stylesheet: template rules, keys, output declarations and global variables. */
final class XsltStylesheet {
  static final String kXsl = "http://www.w3.org/1999/XSL/Transform";
  static final String kXmlns = "http://www.w3.org/2000/xmlns/";

  static final class Rule {
    Element template;
    String pattern;
    String mode;
    double priority;
    int precedence;
    int order;
    String[] quick;
  }

  static final class Key {
    String match;
    String use;
    Element element;
  }

  final TransformerFactoryImpl factory;
  final List<Rule> rules = new ArrayList<Rule>();
  final Map<String, Element> named = new HashMap<String, Element>();
  final Map<String, Integer> namedPrecedence = new HashMap<String, Integer>();
  final List<Element> globals = new ArrayList<Element>();
  final Map<String, List<Key>> keys = new HashMap<String, List<Key>>();
  final Map<String, List<Element>> attributeSets = new HashMap<String, List<Element>>();
  final Map<String, Element> decimalFormats = new HashMap<String, Element>();
  final Properties output = new Properties();
  final List<String> strip = new ArrayList<String>();
  final List<String> preserve = new ArrayList<String>();
  final List<String> cdataElements = new ArrayList<String>();
  final Document primary;
  String systemId;
  private int precedence;
  private int order;

  XsltStylesheet(TransformerFactoryImpl factory, Source source) throws TransformerConfigurationException {
    this.factory = factory;
    Document doc = load(source, factory);
    this.primary = doc;
    this.systemId = source.getSystemId();
    Element root = doc.getDocumentElement();
    if (root == null) {
      throw new TransformerConfigurationException("Stylesheet has no root element.");
    }
    if (isXsl(root, "stylesheet") || isXsl(root, "transform")) {
      validate(root);
      process(root, source.getSystemId());
    } else if (root.hasAttributeNS(kXsl, "version")) {
      Element template = doc.createElementNS(kXsl, "xsl:template");
      template.setAttribute("match", "/");
      Node copy = root.cloneNode(true);
      template.appendChild(copy);
      Rule r = new Rule();
      r.template = template;
      r.pattern = "/";
      r.mode = "";
      r.priority = 0.5;
      r.precedence = ++precedence;
      r.order = order++;
      rules.add(r);
    } else {
      throw new TransformerConfigurationException("The root element of the stylesheet is not xsl:stylesheet.");
    }
  }

  private static final String[] kExpressionAttributes = {"select", "test", "use", "value"};
  private static final String[] kPatternAttributes = {"match", "count", "from"};

  private void validate(Element e) throws TransformerConfigurationException {
    NamespaceContext ns = namespaces(e);
    if (kXsl.equals(e.getNamespaceURI())) {
      for (String a : kExpressionAttributes) {
        if (e.hasAttribute(a) && !(a.equals("value") && !isXsl(e, "number"))) {
          check(e.getAttribute(a), ns);
        }
      }
      for (String a : kPatternAttributes) {
        if (e.hasAttribute(a)) {
          for (String alt : splitUnion(e.getAttribute(a))) {
            check(alt.trim(), ns);
          }
        }
      }
    }
    NamedNodeMap attrs = e.getAttributes();
    for (int i = 0; i < attrs.getLength(); i++) {
      Node a = attrs.item(i);
      String v = a.getNodeValue();
      boolean avt = !kXsl.equals(e.getNamespaceURI()) && !kXsl.equals(a.getNamespaceURI())
          || kXsl.equals(e.getNamespaceURI()) && (a.getNodeName().equals("name") || a.getNodeName().equals("namespace"))
              && (isXsl(e, "element") || isXsl(e, "attribute"));
      if (avt && v.indexOf('{') >= 0) {
        validateAvt(v, ns);
      }
    }
    for (Node c = e.getFirstChild(); c != null; c = c.getNextSibling()) {
      if (c.getNodeType() == Node.ELEMENT_NODE) {
        validate((Element) c);
      }
    }
  }

  private static void validateAvt(String text, NamespaceContext ns) throws TransformerConfigurationException {
    int i = 0;
    while (i < text.length()) {
      char c = text.charAt(i);
      if (c == '{') {
        if (i + 1 < text.length() && text.charAt(i + 1) == '{') {
          i += 2;
          continue;
        }
        int j = i + 1;
        char quote = 0;
        while (j < text.length()) {
          char d = text.charAt(j);
          if (quote != 0) {
            if (d == quote) {
              quote = 0;
            }
          } else if (d == '\'' || d == '"') {
            quote = d;
          } else if (d == '}') {
            break;
          }
          j++;
        }
        if (j >= text.length()) {
          throw new TransformerConfigurationException("Attribute value template is missing a closing brace.");
        }
        check(text.substring(i + 1, j), ns);
        i = j + 1;
      } else {
        i++;
      }
    }
  }

  private static void check(String expression, NamespaceContext ns) throws TransformerConfigurationException {
    try {
      new XsltXPath(expression, ns, null, null);
    } catch (RuntimeException e) {
      throw new TransformerConfigurationException("Invalid XPath expression '" + expression + "': " + e.getMessage());
    }
  }

  static boolean isXsl(Node n, String local) {
    return n.getNodeType() == Node.ELEMENT_NODE && kXsl.equals(n.getNamespaceURI())
        && local.equals(n.getLocalName());
  }

  static Document load(Source source, TransformerFactoryImpl factory) throws TransformerConfigurationException {
    try {
      if (source instanceof DOMSource) {
        Node n = ((DOMSource) source).getNode();
        if (n instanceof Document) {
          return (Document) ((Document) n).cloneNode(true);
        }
        DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
        f.setNamespaceAware(true);
        Document d = f.newDocumentBuilder().newDocument();
        d.appendChild(d.importNode(n, true));
        return d;
      }
      InputSource is = SAXSource.sourceToInputSource(source);
      if (is == null) {
        throw new TransformerConfigurationException("Stylesheet source has no contents.");
      }
      if (source instanceof StreamSource && is.getByteStream() == null && is.getCharacterStream() == null
          && is.getSystemId() != null && factory.getURIResolver() != null) {
        try {
          Source resolved = factory.getURIResolver().resolve(is.getSystemId(), null);
          if (resolved != null) {
            is = SAXSource.sourceToInputSource(resolved);
          }
        } catch (javax.xml.transform.TransformerException e) {
          throw new TransformerConfigurationException(e);
        }
      }
      DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
      f.setNamespaceAware(true);
      DocumentBuilder b = f.newDocumentBuilder();
      Document d = b.parse(is);
      return d;
    } catch (ParserConfigurationException e) {
      throw new TransformerConfigurationException(e);
    } catch (SAXException e) {
      throw new TransformerConfigurationException(e);
    } catch (IOException e) {
      throw new TransformerConfigurationException(e);
    }
  }

  private void process(Element root, String baseId) throws TransformerConfigurationException {
    stripStylesheetWhitespace(root);
    List<Element> children = new ArrayList<Element>();
    for (Node c = root.getFirstChild(); c != null; c = c.getNextSibling()) {
      if (c.getNodeType() == Node.ELEMENT_NODE) {
        children.add((Element) c);
      }
    }
    for (Element e : children) {
      if (isXsl(e, "import")) {
        include(e, baseId);
      }
    }
    precedence++;
    for (Element e : children) {
      String name = e.getLocalName();
      if (!kXsl.equals(e.getNamespaceURI())) {
        continue;
      }
      switch (name) {
      case "include":
        include(e, baseId);
        break;
      case "template":
        template(e);
        break;
      case "variable":
      case "param":
        globals.add(e);
        break;
      case "key": {
        Key k = new Key();
        k.match = e.getAttribute("match");
        k.use = e.getAttribute("use");
        k.element = e;
        List<Key> list = keys.get(e.getAttribute("name"));
        if (list == null) {
          list = new ArrayList<Key>();
          keys.put(e.getAttribute("name"), list);
        }
        list.add(k);
        break;
      }
      case "output": {
        NamedNodeMap attrs = e.getAttributes();
        for (int i = 0; i < attrs.getLength(); i++) {
          Node a = attrs.item(i);
          if (a.getNamespaceURI() == null || a.getNamespaceURI().isEmpty()) {
            if (a.getNodeName().equals("cdata-section-elements")) {
              for (String t : a.getNodeValue().trim().split("\\s+")) {
                if (!t.isEmpty()) {
                  cdataElements.add(t);
                }
              }
            } else {
              output.setProperty(a.getNodeName(), a.getNodeValue());
            }
          }
        }
        break;
      }
      case "strip-space":
        for (String t : e.getAttribute("elements").trim().split("\\s+")) {
          strip.add(t);
        }
        break;
      case "preserve-space":
        for (String t : e.getAttribute("elements").trim().split("\\s+")) {
          preserve.add(t);
        }
        break;
      case "attribute-set": {
        List<Element> list = attributeSets.get(e.getAttribute("name"));
        if (list == null) {
          list = new ArrayList<Element>();
          attributeSets.put(e.getAttribute("name"), list);
        }
        list.add(e);
        break;
      }
      case "decimal-format":
        decimalFormats.put(e.getAttribute("name"), e);
        break;
      default:
        break;
      }
    }
  }

  private void include(Element e, String baseId) throws TransformerConfigurationException {
    String href = e.getAttribute("href");
    Source resolved = null;
    try {
      if (factory.getURIResolver() != null) {
        resolved = factory.getURIResolver().resolve(href, baseId);
      }
    } catch (javax.xml.transform.TransformerException ex) {
      throw new TransformerConfigurationException(ex);
    }
    if (resolved == null) {
      String id = baseId == null ? href : java.net.URI.create(baseId).resolve(href).toString();
      resolved = new StreamSource(id);
    }
    Document d = load(resolved, factory);
    Element root = d.getDocumentElement();
    Document into = primary;
    Element imported = (Element) into.importNode(root, true);
    process(imported, resolved.getSystemId() != null ? resolved.getSystemId() : baseId);
  }

  private void template(Element e) {
    String mode = e.getAttribute("mode");
    String name = e.getAttribute("name");
    if (!name.isEmpty()) {
      Integer old = namedPrecedence.get(name);
      if (old == null || old.intValue() <= precedence) {
        named.put(name, e);
        namedPrecedence.put(name, Integer.valueOf(precedence));
      }
    }
    String match = e.getAttribute("match");
    if (match.isEmpty()) {
      return;
    }
    boolean explicit = e.hasAttribute("priority");
    double explicitPriority = explicit ? Double.parseDouble(e.getAttribute("priority").trim()) : 0;
    for (String alt : splitUnion(match)) {
      Rule r = new Rule();
      r.template = e;
      r.pattern = alt.trim();
      r.mode = mode;
      r.priority = explicit ? explicitPriority : defaultPriority(r.pattern);
      r.precedence = precedence;
      r.order = order++;
      r.quick = quickFilter(r.pattern);
      rules.add(r);
    }
  }

  static List<String> splitUnion(String pattern) {
    List<String> out = new ArrayList<String>();
    int depth = 0;
    int start = 0;
    char quote = 0;
    for (int i = 0; i < pattern.length(); i++) {
      char c = pattern.charAt(i);
      if (quote != 0) {
        if (c == quote) {
          quote = 0;
        }
      } else if (c == '\'' || c == '"') {
        quote = c;
      } else if (c == '[' || c == '(') {
        depth++;
      } else if (c == ']' || c == ')') {
        depth--;
      } else if (c == '|' && depth == 0) {
        out.add(pattern.substring(start, i));
        start = i + 1;
      }
    }
    out.add(pattern.substring(start));
    return out;
  }

  static String lastStep(String pattern) {
    int depth = 0;
    char quote = 0;
    int cut = 0;
    for (int i = 0; i < pattern.length(); i++) {
      char c = pattern.charAt(i);
      if (quote != 0) {
        if (c == quote) {
          quote = 0;
        }
      } else if (c == '\'' || c == '"') {
        quote = c;
      } else if (c == '[' || c == '(') {
        depth++;
      } else if (c == ']' || c == ')') {
        depth--;
      } else if (c == '/' && depth == 0) {
        cut = i + 1;
      }
    }
    return pattern.substring(cut).trim();
  }

  static String stripPredicates(String step) {
    StringBuilder sb = new StringBuilder();
    int depth = 0;
    for (int i = 0; i < step.length(); i++) {
      char c = step.charAt(i);
      if (c == '[') {
        depth++;
      } else if (c == ']') {
        depth--;
      } else if (depth == 0) {
        sb.append(c);
      }
    }
    return sb.toString().trim();
  }

  static double defaultPriority(String pattern) {
    String s = pattern.trim();
    if (s.equals("/")) {
      return 0.5;
    }
    if (s.startsWith("child::")) {
      s = s.substring(7);
    } else if (s.startsWith("attribute::")) {
      s = s.substring(11);
    } else if (s.startsWith("@")) {
      s = s.substring(1);
    }
    if (s.equals("text()") || s.equals("comment()") || s.equals("node()") || s.equals("processing-instruction()")) {
      return -0.5;
    }
    if (s.startsWith("processing-instruction(") && s.indexOf('/') < 0 && s.indexOf('[') < 0) {
      return 0;
    }
    if (s.indexOf('/') >= 0 || s.indexOf('[') >= 0 || s.indexOf('(') >= 0 || s.indexOf("::") >= 0) {
      return 0.5;
    }
    if (s.equals("*")) {
      return -0.5;
    }
    if (s.endsWith(":*")) {
      return -0.25;
    }
    return 0;
  }

  /** Returns {kind, prefix-resolved-namespace-or-null, local-or-null} describing what the last step can match. */
  String[] quickFilter(String pattern) {
    String step = stripPredicates(lastStep(pattern));
    boolean attribute = false;
    if (step.startsWith("@")) {
      attribute = true;
      step = step.substring(1);
    } else if (step.startsWith("attribute::")) {
      attribute = true;
      step = step.substring(11);
    } else if (step.startsWith("child::")) {
      step = step.substring(7);
    }
    if (step.isEmpty() || step.indexOf('(') >= 0 && !step.equals("text()") && !step.equals("node()")
        && !step.equals("comment()") || step.indexOf("::") >= 0) {
      if (step.equals("text()") || step.equals("comment()")) {
        return new String[] {step, null, null};
      }
      return null;
    }
    if (step.equals("text()") || step.equals("comment()") || step.equals("node()")) {
      return new String[] {attribute ? "attr-node" : step, null, null};
    }
    if (step.equals("*")) {
      return new String[] {attribute ? "attr" : "element", null, null};
    }
    String local = step;
    String prefix = null;
    int colon = step.indexOf(':');
    if (colon > 0) {
      prefix = step.substring(0, colon);
      local = step.substring(colon + 1);
    }
    String ns = "";
    if (prefix != null) {
      ns = primary.getDocumentElement().lookupNamespaceURI(prefix);
      if (ns == null) {
        return null;
      }
    }
    return new String[] {attribute ? "attr" : "element", ns, local.equals("*") ? null : local};
  }

  boolean quickAccepts(String[] q, Node n) {
    if (q == null) {
      return true;
    }
    short type = n.getNodeType();
    switch (q[0]) {
    case "text()":
      return type == Node.TEXT_NODE || type == Node.CDATA_SECTION_NODE;
    case "comment()":
      return type == Node.COMMENT_NODE;
    case "node()":
      return type != Node.ATTRIBUTE_NODE;
    case "attr-node":
      return type == Node.ATTRIBUTE_NODE;
    case "element":
    case "attr": {
      short want = q[0].equals("element") ? Node.ELEMENT_NODE : Node.ATTRIBUTE_NODE;
      if (type != want) {
        return false;
      }
      if (q[2] != null && !q[2].equals(localName(n))) {
        return false;
      }
      String ns = n.getNamespaceURI() == null ? "" : n.getNamespaceURI();
      return q[1] == null || q[1].equals(ns);
    }
    default:
      return true;
    }
  }

  static String localName(Node n) {
    String l = n.getLocalName();
    if (l != null) {
      return l;
    }
    String name = n.getNodeName();
    int colon = name.indexOf(':');
    return colon < 0 ? name : name.substring(colon + 1);
  }

  private void stripStylesheetWhitespace(Node n) {
    for (Node c = n.getFirstChild(); c != null;) {
      Node next = c.getNextSibling();
      if (c.getNodeType() == Node.TEXT_NODE && isBlank(c.getNodeValue()) && !isXsl(n, "text")
          && !"preserve".equals(xmlSpace(n))) {
        n.removeChild(c);
      } else if (c.getNodeType() == Node.COMMENT_NODE || c.getNodeType() == Node.PROCESSING_INSTRUCTION_NODE) {
        n.removeChild(c);
      } else if (c.getNodeType() == Node.ELEMENT_NODE) {
        stripStylesheetWhitespace(c);
      }
      c = next;
    }
  }

  private static String xmlSpace(Node n) {
    for (Node x = n; x != null && x.getNodeType() == Node.ELEMENT_NODE; x = x.getParentNode()) {
      String v = ((Element) x).getAttributeNS("http://www.w3.org/XML/1998/namespace", "space");
      if (!v.isEmpty()) {
        return v;
      }
    }
    return "";
  }

  static boolean isBlank(String s) {
    for (int i = 0; i < s.length(); i++) {
      char c = s.charAt(i);
      if (c != ' ' && c != '\t' && c != '\n' && c != '\r') {
        return false;
      }
    }
    return true;
  }

  /** Namespace context that resolves prefixes through the in-scope declarations of a stylesheet element. */
  static NamespaceContext namespaces(final Element scope) {
    return new NamespaceContext() {
      public String getNamespaceURI(String prefix) {
        if (prefix == null) {
          throw new IllegalArgumentException();
        }
        if (prefix.equals("xml")) {
          return "http://www.w3.org/XML/1998/namespace";
        }
        if (prefix.isEmpty()) {
          return "";
        }
        String u = scope.lookupNamespaceURI(prefix);
        return u == null ? "" : u;
      }

      public String getPrefix(String uri) {
        return scope.lookupPrefix(uri);
      }

      public java.util.Iterator<String> getPrefixes(String uri) {
        String p = scope.lookupPrefix(uri);
        List<String> l = new ArrayList<String>();
        if (p != null) {
          l.add(p);
        }
        return l.iterator();
      }
    };
  }

  Map<String, XsltXPath> cache = new LinkedHashMap<String, XsltXPath>();
}
