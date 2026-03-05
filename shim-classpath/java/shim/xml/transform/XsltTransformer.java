/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.xml.transform;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.xpath.XPathFunction;
import javax.xml.xpath.XPathFunctionException;
import javax.xml.xpath.XPathFunctionResolver;
import javax.xml.xpath.XPathVariableResolver;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import shim.xml.ser.DomToSax;
import shim.xml.xpath.XsltXPath;

/** Executes a compiled stylesheet: the source tree is walked through template rules into a result tree. */
final class XsltTransformer extends IdentityTransformer {
  private static final String kXmlns = "http://www.w3.org/2000/xmlns/";

  private final XsltStylesheet sheet;
  private Document source;
  private Document resultDoc;
  private Node out;
  private Node currentNode;
  private XsltStylesheet.Rule currentRule;
  private Scope scope;
  private final Map<String, Object> globalValues = new HashMap<String, Object>();
  private final Map<String, Boolean> globalBusy = new HashMap<String, Boolean>();
  private final Map<Element, Map<String, XsltXPath>> expressions = new IdentityHashMap<Element, Map<String, XsltXPath>>();
  private final Map<Node, String> ids = new IdentityHashMap<Node, String>();
  private final Map<String, Map<String, List<Node>>> keyIndexes = new HashMap<String, Map<String, List<Node>>>();
  private final Map<String, Document> documents = new HashMap<String, Document>();
  private int idCounter = 10000;
  private final List<String> cdata = new ArrayList<String>();

  private static final class Scope {
    final Map<String, Object> values = new HashMap<String, Object>();
    final Scope parent;

    Scope(Scope parent) {
      this.parent = parent;
    }
  }

  private static final class Error extends RuntimeException {
    private static final long serialVersionUID = 1L;

    Error(String message) {
      super(message);
    }
  }

  XsltTransformer(TransformerFactoryImpl factory, XsltStylesheet sheet) {
    super(factory);
    this.sheet = sheet;
    for (String name : sheet.output.stringPropertyNames()) {
      defaultOutput(name, sheet.output.getProperty(name));
    }
    cdata.addAll(sheet.cdataElements);
    if ("html".equals(sheet.output.getProperty("method")) && sheet.output.getProperty("indent") == null) {
      defaultOutput("indent", "yes");
    }
  }

  @Override
  public void transform(Source src, Result result) throws TransformerException {
    if (src == null) {
      throw new NullPointerException("Source object passed to 'transform' is null.");
    }
    try {
      source = loadSource(src);
      globalValues.clear();
      globalBusy.clear();
      keyIndexes.clear();
      ids.clear();
      scope = new Scope(null);
      DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
      f.setNamespaceAware(true);
      resultDoc = f.newDocumentBuilder().newDocument();
      DocumentFragment root = resultDoc.createDocumentFragment();
      out = root;
      currentNode = source;
      for (Element g : sheet.globals) {
        globalValue(varName(g));
      }
      applyTemplates(Collections.singletonList((Node) source), "", null, null);
      emit(root, result);
    } catch (Error e) {
      throw new TransformerException(e.getMessage());
    } catch (TransformerException e) {
      throw e;
    } catch (RuntimeException e) {
      throw new TransformerException(e);
    } catch (javax.xml.parsers.ParserConfigurationException e) {
      throw new TransformerException(e);
    }
  }

  private Document loadSource(Source src) throws TransformerException {
    try {
      Document d;
      if (src instanceof DOMSource && ((DOMSource) src).getNode() instanceof Document) {
        d = (Document) ((Document) ((DOMSource) src).getNode()).cloneNode(true);
      } else {
        d = XsltStylesheet.load(src, factoryOf());
      }
      stripSource(d.getDocumentElement());
      return d;
    } catch (javax.xml.transform.TransformerConfigurationException e) {
      throw new TransformerException(e);
    }
  }

  private TransformerFactoryImpl factoryOf() {
    return sheet.factory;
  }

  private void stripSource(Node n) {
    if (n == null || (sheet.strip.isEmpty())) {
      return;
    }
    for (Node c = n.getFirstChild(); c != null;) {
      Node next = c.getNextSibling();
      if (c.getNodeType() == Node.TEXT_NODE && XsltStylesheet.isBlank(c.getNodeValue()) && n.getNodeType() == Node.ELEMENT_NODE
          && stripped((Element) n)) {
        n.removeChild(c);
      } else if (c.getNodeType() == Node.ELEMENT_NODE) {
        stripSource(c);
      }
      c = next;
    }
  }

  private boolean stripped(Element e) {
    String xmlSpace = e.getAttributeNS("http://www.w3.org/XML/1998/namespace", "space");
    if (xmlSpace.equals("preserve")) {
      return false;
    }
    int best = -1;
    boolean result = false;
    for (String t : sheet.strip) {
      int rank = nameTestRank(t, e);
      if (rank > best) {
        best = rank;
        result = true;
      }
    }
    for (String t : sheet.preserve) {
      int rank = nameTestRank(t, e);
      if (rank >= best && rank >= 0) {
        best = rank;
        result = false;
      }
    }
    return result;
  }

  private int nameTestRank(String test, Element e) {
    if (test.equals("*")) {
      return 0;
    }
    int colon = test.indexOf(':');
    if (colon < 0) {
      return test.equals(XsltStylesheet.localName(e)) && (e.getNamespaceURI() == null || e.getNamespaceURI().isEmpty()) ? 2 : -1;
    }
    String ns = sheet.primary.getDocumentElement().lookupNamespaceURI(test.substring(0, colon));
    String local = test.substring(colon + 1);
    if (ns == null || !ns.equals(e.getNamespaceURI())) {
      return -1;
    }
    return local.equals("*") ? 1 : local.equals(XsltStylesheet.localName(e)) ? 2 : -1;
  }

  private void emit(DocumentFragment root, Result result) throws TransformerException {
    String method = property("method");
    boolean explicit = explicitOutput("method") != null;
    Element first = null;
    int elements = 0;
    boolean text = false;
    for (Node c = root.getFirstChild(); c != null; c = c.getNextSibling()) {
      if (c.getNodeType() == Node.ELEMENT_NODE) {
        elements++;
        if (first == null) {
          first = (Element) c;
        }
      } else if (c.getNodeType() == Node.TEXT_NODE && !XsltStylesheet.isBlank(c.getNodeValue())) {
        text = true;
      }
    }
    if (!explicit && first != null && XsltStylesheet.localName(first).equalsIgnoreCase("html")
        && (first.getNamespaceURI() == null || first.getNamespaceURI().isEmpty())) {
      setOutputProperty("method", "html");
    }
    if ("text".equals(property("method"))) {
      StringBuilder sb = new StringBuilder();
      collectText(root, sb);
      java.io.Writer w = null;
      SerializationTarget t = openTarget(result);
      try {
        if (t.writer != null) {
          t.writer.write(sb.toString());
          t.writer.flush();
        } else {
          t.handler.startDocument();
          t.handler.characters(sb.toString().toCharArray(), 0, sb.length());
          t.handler.endDocument();
        }
        t.finish();
      } catch (java.io.IOException e) {
        throw new TransformerException(e);
      } catch (org.xml.sax.SAXException e) {
        throw new TransformerException(e);
      } finally {
        t.close();
      }
      return;
    }
    Node feed = root;
    if (elements == 1 && !text) {
      Document d = resultDoc;
      Document wrapper;
      try {
        DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
        f.setNamespaceAware(true);
        wrapper = f.newDocumentBuilder().newDocument();
      } catch (javax.xml.parsers.ParserConfigurationException e) {
        throw new TransformerException(e);
      }
      for (Node c = root.getFirstChild(); c != null; c = c.getNextSibling()) {
        if (c.getNodeType() == Node.ELEMENT_NODE || c.getNodeType() == Node.COMMENT_NODE
            || c.getNodeType() == Node.PROCESSING_INSTRUCTION_NODE) {
          wrapper.appendChild(wrapper.importNode(c, true));
        }
      }
      wrapper.setXmlStandalone(true);
      feed = wrapper;
    }
    super.transform(new DOMSource(feed), result);
  }

  private static void collectText(Node n, StringBuilder sb) {
    for (Node c = n.getFirstChild(); c != null; c = c.getNextSibling()) {
      if (c.getNodeType() == Node.TEXT_NODE || c.getNodeType() == Node.CDATA_SECTION_NODE) {
        sb.append(c.getNodeValue());
      } else if (c.getNodeType() == Node.ELEMENT_NODE || c.getNodeType() == Node.DOCUMENT_FRAGMENT_NODE) {
        collectText(c, sb);
      }
    }
  }

  private void text(String s) {
    if (s.isEmpty()) {
      return;
    }
    Node last = out.getLastChild();
    if (last != null && last.getNodeType() == Node.TEXT_NODE) {
      ((org.w3c.dom.Text) last).appendData(s);
    } else {
      out.appendChild(resultDoc.createTextNode(s));
    }
  }

  private void attribute(String name, String ns, String value) {
    if (out.getNodeType() != Node.ELEMENT_NODE) {
      throw new Error("Attribute '" + name + "' outside of element.");
    }
    ((Element) out).setAttributeNS(ns == null || ns.isEmpty() ? null : ns, name, value);
  }

  private void applyTemplates(List<Node> nodes, String mode, List<Element> sorts, Element sortScope) {
    List<Node> list = nodes;
    if (sorts != null && !sorts.isEmpty()) {
      list = XsltSupport.sort(this, nodes, sorts);
    }
    int size = list.size();
    for (int i = 0; i < size; i++) {
      applyTo(list.get(i), mode, i + 1, size, null);
    }
  }

  private void applyTo(Node node, String mode, int position, int size, Scope params) {
    XsltStylesheet.Rule rule = findRule(node, mode, Integer.MAX_VALUE, position, size);
    Node savedCurrent = currentNode;
    XsltStylesheet.Rule savedRule = currentRule;
    int[] savedPos = {curPos, curSize};
    currentNode = node;
    curPos = position;
    curSize = size;
    try {
      if (rule == null) {
        builtIn(node, mode);
      } else {
        currentRule = rule;
        invoke(rule.template, params);
      }
    } finally {
      currentNode = savedCurrent;
      currentRule = savedRule;
      curPos = savedPos[0];
      curSize = savedPos[1];
    }
  }

  private int curPos = 1;
  private int curSize = 1;

  private void builtIn(Node node, String mode) {
    switch (node.getNodeType()) {
    case Node.DOCUMENT_NODE:
    case Node.ELEMENT_NODE: {
      List<Node> kids = new ArrayList<Node>();
      for (Node c = node.getFirstChild(); c != null; c = c.getNextSibling()) {
        if (c.getNodeType() != Node.DOCUMENT_TYPE_NODE) {
          kids.add(c);
        }
      }
      applyTemplatesList(kids, mode);
      break;
    }
    case Node.TEXT_NODE:
    case Node.CDATA_SECTION_NODE:
    case Node.ATTRIBUTE_NODE:
      text(node.getNodeValue());
      break;
    default:
      break;
    }
  }

  private void applyTemplatesList(List<Node> kids, String mode) {
    int size = kids.size();
    for (int i = 0; i < size; i++) {
      applyTo(kids.get(i), mode, i + 1, size, null);
    }
  }

  private XsltStylesheet.Rule findRule(Node node, String mode, int belowPrecedence, int position, int size) {
    XsltStylesheet.Rule best = null;
    for (XsltStylesheet.Rule r : sheet.rules) {
      if (!r.mode.equals(mode) || r.precedence >= belowPrecedence) {
        continue;
      }
      if (best != null) {
        if (r.precedence < best.precedence || (r.precedence == best.precedence && r.priority < best.priority)
            || (r.precedence == best.precedence && r.priority == best.priority && r.order < best.order)) {
          continue;
        }
      }
      if (!sheet.quickAccepts(r.quick, node)) {
        continue;
      }
      if (matches(r, node)) {
        best = r;
      }
    }
    return best;
  }

  private boolean matches(XsltStylesheet.Rule r, Node node) {
    XsltXPath p = compile(r.template, "match:" + r.pattern, r.pattern);
    Node saved = currentNode;
    try {
      for (Node a = node; a != null; a = a.getNodeType() == Node.ATTRIBUTE_NODE ? ((Attr) a).getOwnerElement() : a.getParentNode()) {
        Object v = p.evaluate(a, 1, 1);
        if (v instanceof List && ((List<?>) v).contains(node)) {
          return true;
        }
        if (r.pattern.startsWith("/") && !r.pattern.startsWith("//") && r.pattern.length() > 1) {
          break;
        }
      }
      return false;
    } catch (RuntimeException e) {
      return false;
    } finally {
      currentNode = saved;
    }
  }

  private void invoke(Element template, Scope params) {
    Scope saved = scope;
    scope = new Scope(globalScope());
    try {
      for (Node c = template.getFirstChild(); c != null; c = c.getNextSibling()) {
        if (XsltStylesheet.isXsl(c, "param")) {
          Element p = (Element) c;
          String name = varName(p);
          Object v;
          if (params != null && params.values.containsKey(name)) {
            v = params.values.get(name);
          } else {
            v = variableValue(p);
          }
          scope.values.put(name, v);
        } else {
          break;
        }
      }
      children(template);
    } finally {
      scope = saved;
    }
  }

  private Scope globalScope() {
    Scope s = scope;
    while (s.parent != null) {
      s = s.parent;
    }
    return s;
  }

  private void children(Element parent) {
    for (Node c = parent.getFirstChild(); c != null; c = c.getNextSibling()) {
      instruction(c);
    }
  }

  private void instruction(Node n) {
    switch (n.getNodeType()) {
    case Node.TEXT_NODE:
    case Node.CDATA_SECTION_NODE:
      text(n.getNodeValue());
      return;
    case Node.ELEMENT_NODE:
      break;
    default:
      return;
    }
    Element e = (Element) n;
    if (!XsltStylesheet.kXsl.equals(e.getNamespaceURI())) {
      literal(e);
      return;
    }
    String name = e.getLocalName();
    switch (name) {
    case "apply-templates":
      applyTemplatesInstr(e);
      break;
    case "call-template": {
      Element t = sheet.named.get(e.getAttribute("name"));
      if (t == null) {
        throw new Error("Template '" + e.getAttribute("name") + "' not found.");
      }
      Scope params = withParams(e);
      invokeNamed(t, params);
      break;
    }
    case "for-each":
      forEach(e);
      break;
    case "value-of":
      text(XsltXPath.string(eval(e, "select")));
      break;
    case "copy-of":
      copyOf(e);
      break;
    case "copy":
      copy(e);
      break;
    case "if":
      if (XsltXPath.bool(eval(e, "test"))) {
        scopedChildren(e);
      }
      break;
    case "choose":
      choose(e);
      break;
    case "variable":
      scope.values.put(varName(e), variableValue(e));
      break;
    case "param":
      break;
    case "text":
      text(e.getTextContent());
      break;
    case "element":
      elementInstr(e);
      break;
    case "attribute":
      attributeInstr(e);
      break;
    case "comment": {
      String v = bodyString(e);
      out.appendChild(resultDoc.createComment(v));
      break;
    }
    case "processing-instruction": {
      String v = bodyString(e);
      out.appendChild(resultDoc.createProcessingInstruction(avt(e, "name"), v));
      break;
    }
    case "number":
      text(XsltSupport.number(this, e));
      break;
    case "message": {
      String m = bodyString(e);
      try {
        getErrorListener().warning(new TransformerException(m));
      } catch (TransformerException ex) {
        throw new Error(ex.getMessage());
      }
      if ("yes".equals(e.getAttribute("terminate"))) {
        throw new Error(m);
      }
      break;
    }
    case "apply-imports": {
      if (currentRule != null) {
        XsltStylesheet.Rule r = findRule(currentNode, currentRule.mode, currentRule.precedence, curPos, curSize);
        if (r != null) {
          XsltStylesheet.Rule saved = currentRule;
          currentRule = r;
          try {
            invoke(r.template, null);
          } finally {
            currentRule = saved;
          }
        } else {
          builtIn(currentNode, currentRule.mode);
        }
      }
      break;
    }
    case "fallback":
    case "sort":
    case "with-param":
      break;
    default:
      throw new Error("Unsupported XSLT instruction: xsl:" + name);
    }
  }

  private void scopedChildren(Element e) {
    Scope saved = scope;
    scope = new Scope(saved);
    try {
      children(e);
    } finally {
      scope = saved;
    }
  }

  private void invokeNamed(Element t, Scope params) {
    Scope saved = scope;
    scope = new Scope(globalScope());
    try {
      for (Node c = t.getFirstChild(); c != null; c = c.getNextSibling()) {
        if (XsltStylesheet.isXsl(c, "param")) {
          String name = varName((Element) c);
          scope.values.put(name, params != null && params.values.containsKey(name) ? params.values.get(name)
              : variableValue((Element) c));
        } else {
          break;
        }
      }
      children(t);
    } finally {
      scope = saved;
    }
  }

  private Scope withParams(Element e) {
    Scope params = new Scope(null);
    for (Node c = e.getFirstChild(); c != null; c = c.getNextSibling()) {
      if (XsltStylesheet.isXsl(c, "with-param")) {
        params.values.put(varName((Element) c), variableValue((Element) c));
      }
    }
    return params;
  }

  private List<Element> sorts(Element e) {
    List<Element> list = new ArrayList<Element>();
    for (Node c = e.getFirstChild(); c != null; c = c.getNextSibling()) {
      if (XsltStylesheet.isXsl(c, "sort")) {
        list.add((Element) c);
      }
    }
    return list;
  }

  @SuppressWarnings("unchecked")
  private List<Node> nodeSet(Object v) {
    if (v instanceof List) {
      return (List<Node>) v;
    }
    if (v instanceof Node) {
      return Collections.singletonList((Node) v);
    }
    throw new Error("Expression does not evaluate to a node-set.");
  }

  private void applyTemplatesInstr(Element e) {
    List<Node> nodes;
    if (e.hasAttribute("select")) {
      nodes = nodeSet(eval(e, "select"));
    } else {
      nodes = new ArrayList<Node>();
      for (Node c = currentNode.getFirstChild(); c != null; c = c.getNextSibling()) {
        if (c.getNodeType() != Node.DOCUMENT_TYPE_NODE) {
          nodes.add(c);
        }
      }
    }
    List<Element> sorts = sorts(e);
    if (!sorts.isEmpty()) {
      nodes = XsltSupport.sort(this, nodes, sorts);
    }
    Scope params = withParams(e);
    String mode = e.hasAttribute("mode") ? e.getAttribute("mode") : "";
    int size = nodes.size();
    for (int i = 0; i < size; i++) {
      applyTo(nodes.get(i), mode, i + 1, size, params);
    }
  }

  private void forEach(Element e) {
    List<Node> nodes = nodeSet(eval(e, "select"));
    List<Element> sorts = sorts(e);
    if (!sorts.isEmpty()) {
      nodes = XsltSupport.sort(this, nodes, sorts);
    }
    int size = nodes.size();
    Node saved = currentNode;
    int sp = curPos;
    int ss = curSize;
    try {
      for (int i = 0; i < size; i++) {
        currentNode = nodes.get(i);
        curPos = i + 1;
        curSize = size;
        scopedChildren(e);
      }
    } finally {
      currentNode = saved;
      curPos = sp;
      curSize = ss;
    }
  }

  private void choose(Element e) {
    for (Node c = e.getFirstChild(); c != null; c = c.getNextSibling()) {
      if (XsltStylesheet.isXsl(c, "when")) {
        if (XsltXPath.bool(eval((Element) c, "test"))) {
          scopedChildren((Element) c);
          return;
        }
      } else if (XsltStylesheet.isXsl(c, "otherwise")) {
        scopedChildren((Element) c);
        return;
      }
    }
  }

  private void copyOf(Element e) {
    Object v = eval(e, "select");
    if (v instanceof List) {
      for (Object o : (List<?>) v) {
        copyNode((Node) o);
      }
    } else if (v instanceof Node) {
      copyNode((Node) v);
    } else {
      text(XsltXPath.string(v));
    }
  }

  private void copyNode(Node n) {
    switch (n.getNodeType()) {
    case Node.DOCUMENT_NODE:
    case Node.DOCUMENT_FRAGMENT_NODE:
      for (Node c = n.getFirstChild(); c != null; c = c.getNextSibling()) {
        if (c.getNodeType() != Node.DOCUMENT_TYPE_NODE) {
          copyNode(c);
        }
      }
      break;
    case Node.ATTRIBUTE_NODE:
      attribute(n.getNodeName(), n.getNamespaceURI(), n.getNodeValue());
      break;
    case Node.TEXT_NODE:
    case Node.CDATA_SECTION_NODE:
      text(n.getNodeValue());
      break;
    default:
      out.appendChild(resultDoc.importNode(n, true));
      break;
    }
  }

  private void copy(Element e) {
    Node n = currentNode;
    switch (n.getNodeType()) {
    case Node.ELEMENT_NODE: {
      Element copy = resultDoc.createElementNS(n.getNamespaceURI(), n.getNodeName());
      out.appendChild(copy);
      Node saved = out;
      out = copy;
      try {
        useAttributeSets(e.getAttribute("use-attribute-sets"));
        scopedChildren(e);
      } finally {
        out = saved;
      }
      break;
    }
    case Node.ATTRIBUTE_NODE:
      attribute(n.getNodeName(), n.getNamespaceURI(), n.getNodeValue());
      break;
    case Node.TEXT_NODE:
    case Node.CDATA_SECTION_NODE:
      text(n.getNodeValue());
      break;
    case Node.COMMENT_NODE:
    case Node.PROCESSING_INSTRUCTION_NODE:
      out.appendChild(resultDoc.importNode(n, false));
      break;
    default:
      scopedChildren(e);
      break;
    }
  }

  private void elementInstr(Element e) {
    String name = avt(e, "name");
    String ns = e.hasAttribute("namespace") ? avt(e, "namespace") : null;
    if (ns == null) {
      int colon = name.indexOf(':');
      String prefix = colon < 0 ? null : name.substring(0, colon);
      ns = prefix == null ? e.lookupNamespaceURI(null) : e.lookupNamespaceURI(prefix);
    }
    Element el = resultDoc.createElementNS(ns == null || ns.isEmpty() ? null : ns, name);
    out.appendChild(el);
    Node saved = out;
    out = el;
    try {
      useAttributeSets(e.getAttribute("use-attribute-sets"));
      scopedChildren(e);
    } finally {
      out = saved;
    }
  }

  private void attributeInstr(Element e) {
    String name = avt(e, "name");
    String ns = e.hasAttribute("namespace") ? avt(e, "namespace") : null;
    if (ns == null) {
      int colon = name.indexOf(':');
      if (colon > 0) {
        ns = e.lookupNamespaceURI(name.substring(0, colon));
      }
    }
    attribute(name, ns, bodyString(e));
  }

  private void useAttributeSets(String names) {
    if (names == null || names.trim().isEmpty()) {
      return;
    }
    for (String n : names.trim().split("\\s+")) {
      List<Element> sets = sheet.attributeSets.get(n);
      if (sets == null) {
        continue;
      }
      for (Element s : sets) {
        useAttributeSets(s.getAttribute("use-attribute-sets"));
        scopedChildren(s);
      }
    }
  }

  private String bodyString(Element e) {
    Node saved = out;
    DocumentFragment f = resultDoc.createDocumentFragment();
    out = f;
    try {
      scopedChildren(e);
    } finally {
      out = saved;
    }
    StringBuilder sb = new StringBuilder();
    collectText(f, sb);
    return sb.toString();
  }

  private void literal(Element e) {
    String ns = e.getNamespaceURI();
    Element el = resultDoc.createElementNS(ns == null || ns.isEmpty() ? null : ns, e.getNodeName());
    NamedNodeMap attrs = e.getAttributes();
    List<String> excluded = new ArrayList<String>();
    String ex = e.getAttributeNS(XsltStylesheet.kXsl, "exclude-result-prefixes");
    if (!ex.isEmpty()) {
      Collections.addAll(excluded, ex.trim().split("\\s+"));
    }
    for (int i = 0; i < attrs.getLength(); i++) {
      Attr a = (Attr) attrs.item(i);
      String an = a.getNodeName();
      if (XsltStylesheet.kXsl.equals(a.getNamespaceURI())) {
        continue;
      }
      if (an.equals("xmlns") || an.startsWith("xmlns:")) {
        String prefix = an.equals("xmlns") ? "" : an.substring(6);
        if (XsltStylesheet.kXsl.equals(a.getValue()) || excluded.contains(prefix)) {
          continue;
        }
        el.setAttributeNS(kXmlns, an, a.getValue());
        continue;
      }
      String value = avtText(e, a.getValue());
      el.setAttributeNS(a.getNamespaceURI(), an, value);
    }
    out.appendChild(el);
    Node saved = out;
    out = el;
    try {
      useAttributeSets(e.getAttributeNS(XsltStylesheet.kXsl, "use-attribute-sets"));
      scopedChildren(e);
    } finally {
      out = saved;
    }
  }

  static String varName(Element e) {
    String n = e.getAttribute("name");
    int colon = n.indexOf(':');
    if (colon > 0) {
      String ns = e.lookupNamespaceURI(n.substring(0, colon));
      return "{" + (ns == null ? "" : ns) + "}" + n.substring(colon + 1);
    }
    return n;
  }

  Object variableValue(Element e) {
    if (e.hasAttribute("select")) {
      return eval(e, "select");
    }
    if (!e.hasChildNodes()) {
      return "";
    }
    Node saved = out;
    DocumentFragment f = resultDoc.createDocumentFragment();
    out = f;
    try {
      scopedChildren(e);
    } finally {
      out = saved;
    }
    return f;
  }

  private Object globalValue(String name) {
    if (globalValues.containsKey(name)) {
      return globalValues.get(name);
    }
    for (Element g : sheet.globals) {
      if (varName(g).equals(name)) {
        if (globalBusy.containsKey(name)) {
          throw new Error("Circular reference in variable " + name);
        }
        globalBusy.put(name, Boolean.TRUE);
        Scope saved = scope;
        Node savedNode = currentNode;
        scope = new Scope(null);
        currentNode = source;
        Object v;
        try {
          if (XsltStylesheet.isXsl(g, "param") && getParameterValue(name) != null) {
            v = getParameterValue(name);
          } else {
            v = variableValue(g);
          }
        } finally {
          scope = saved;
          currentNode = savedNode;
          globalBusy.remove(name);
        }
        globalValues.put(name, v);
        return v;
      }
    }
    return null;
  }

  private Object getParameterValue(String name) {
    Object v = getParameter(name);
    if (v == null && name.startsWith("{}")) {
      v = getParameter(name.substring(2));
    }
    return v;
  }

  Object lookupVariable(String name) {
    for (Scope s = scope; s != null; s = s.parent) {
      if (s.values.containsKey(name)) {
        return s.values.get(name);
      }
    }
    return globalValue(name);
  }

  Node currentNode() {
    return currentNode;
  }

  void setContext(Node node, int position, int size) {
    currentNode = node;
    curPos = position;
    curSize = size;
  }

  int contextPosition() {
    return curPos;
  }

  int contextSize() {
    return curSize;
  }

  XsltStylesheet stylesheet() {
    return sheet;
  }

  Document sourceDocument() {
    return source;
  }

  XsltXPath compile(Element scopeElement, String key, String text) {
    Map<String, XsltXPath> map = expressions.get(scopeElement);
    if (map == null) {
      map = new HashMap<String, XsltXPath>();
      expressions.put(scopeElement, map);
    }
    XsltXPath x = map.get(key);
    if (x == null) {
      NamespaceContext ns = XsltStylesheet.namespaces(scopeElement);
      try {
        x = new XsltXPath(text, ns, variableResolver, functionResolver);
      } catch (RuntimeException e) {
        throw new Error("Cannot compile expression '" + text + "': " + e.getMessage());
      }
      map.put(key, x);
    }
    return x;
  }

  Object eval(Element e, String attribute) {
    String text = e.getAttribute(attribute);
    return evalText(e, attribute + ":" + text, text);
  }

  Object evalText(Element e, String key, String text) {
    XsltXPath x = compile(e, key, text);
    try {
      return x.evaluate(currentNode, curPos, curSize);
    } catch (Error err) {
      throw err;
    } catch (RuntimeException ex) {
      throw new Error(ex.getMessage() == null ? ex.toString() : ex.getMessage());
    }
  }

  String avt(Element e, String attribute) {
    return avtText(e, e.getAttribute(attribute));
  }

  String avtText(Element e, String text) {
    if (text.indexOf('{') < 0 && text.indexOf('}') < 0) {
      return text;
    }
    StringBuilder sb = new StringBuilder();
    int i = 0;
    while (i < text.length()) {
      char c = text.charAt(i);
      if (c == '{') {
        if (i + 1 < text.length() && text.charAt(i + 1) == '{') {
          sb.append('{');
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
        String expr = text.substring(i + 1, j);
        sb.append(XsltXPath.string(evalText(e, "avt:" + expr, expr)));
        i = j + 1;
      } else if (c == '}') {
        sb.append('}');
        i += (i + 1 < text.length() && text.charAt(i + 1) == '}') ? 2 : 1;
      } else {
        sb.append(c);
        i++;
      }
    }
    return sb.toString();
  }

  private final XPathVariableResolver variableResolver = new XPathVariableResolver() {
    public Object resolveVariable(QName name) {
      String key = name.getNamespaceURI().isEmpty() ? name.getLocalPart()
          : "{" + name.getNamespaceURI() + "}" + name.getLocalPart();
      Object v = lookupVariable(key);
      if (v == null && !name.getNamespaceURI().isEmpty()) {
        v = lookupVariable(name.getLocalPart());
      }
      if (v == null) {
        throw new Error("Variable '" + name.getLocalPart() + "' is not defined.");
      }
      return v;
    }
  };

  private final XPathFunctionResolver functionResolver = new XPathFunctionResolver() {
    public XPathFunction resolveFunction(QName name, final int arity) {
      final String local = name.getLocalPart();
      if (!name.getNamespaceURI().isEmpty()) {
        return null;
      }
      return new XPathFunction() {
        public Object evaluate(List args) throws XPathFunctionException {
          try {
            return XsltSupport.function(XsltTransformer.this, local, args);
          } catch (Error e) {
            throw new XPathFunctionException(e.getMessage());
          }
        }
      };
    }
  };

  String generateId(Node n) {
    String id = ids.get(n);
    if (id == null) {
      id = "N" + (idCounter++);
      ids.put(n, id);
    }
    return id;
  }

  List<Node> keyLookup(String name, Object value) {
    List<XsltStylesheet.Key> defs = sheet.keys.get(name);
    if (defs == null) {
      throw new Error("Key '" + name + "' is not declared.");
    }
    Map<String, List<Node>> index = keyIndexes.get(name);
    if (index == null) {
      index = new HashMap<String, List<Node>>();
      List<Node> all = new ArrayList<Node>();
      XsltSupport.collectAll(source, all);
      Node saved = currentNode;
      try {
        for (XsltStylesheet.Key k : defs) {
          for (Node n : all) {
            if (!matchesPattern(k.element, k.match, n)) {
              continue;
            }
            currentNode = n;
            Object use = evalText(k.element, "use:" + k.use, k.use);
            if (use instanceof List) {
              for (Object o : (List<?>) use) {
                add(index, XsltXPath.stringValue((Node) o), n);
              }
            } else {
              add(index, XsltXPath.string(use), n);
            }
          }
        }
      } finally {
        currentNode = saved;
      }
      keyIndexes.put(name, index);
    }
    List<Node> result = new ArrayList<Node>();
    if (value instanceof List) {
      for (Object o : (List<?>) value) {
        List<Node> l = index.get(XsltXPath.stringValue((Node) o));
        if (l != null) {
          result.addAll(l);
        }
      }
    } else {
      List<Node> l = index.get(XsltXPath.string(value));
      if (l != null) {
        result.addAll(l);
      }
    }
    return result;
  }

  private static void add(Map<String, List<Node>> index, String key, Node n) {
    List<Node> l = index.get(key);
    if (l == null) {
      l = new ArrayList<Node>();
      index.put(key, l);
    }
    if (!l.contains(n)) {
      l.add(n);
    }
  }

  private boolean matchesPattern(Element scopeElement, String pattern, Node node) {
    for (String alt : XsltStylesheet.splitUnion(pattern)) {
      XsltXPath p = compile(scopeElement, "keymatch:" + alt, alt.trim());
      for (Node a = node; a != null; a = a.getNodeType() == Node.ATTRIBUTE_NODE ? ((Attr) a).getOwnerElement() : a.getParentNode()) {
        Object v = p.evaluate(a, 1, 1);
        if (v instanceof List && ((List<?>) v).contains(node)) {
          return true;
        }
      }
    }
    return false;
  }

  Document document(String href) {
    Document d = documents.get(href);
    if (d == null) {
      try {
        Source s = null;
        if (getURIResolver() != null) {
          s = getURIResolver().resolve(href, sheet.systemId);
        }
        if (s == null) {
          String id = sheet.systemId == null ? href : java.net.URI.create(sheet.systemId).resolve(href).toString();
          s = new javax.xml.transform.stream.StreamSource(id);
        }
        d = XsltStylesheet.load(s, sheet.factory);
      } catch (TransformerException e) {
        return null;
      }
      documents.put(href, d);
    }
    return d;
  }

  Document sheetDocument() {
    return sheet.primary;
  }

  Document resultDocument() {
    return resultDoc;
  }

  Map<String, org.w3c.dom.Element> decimalFormats() {
    return sheet.decimalFormats;
  }
}
