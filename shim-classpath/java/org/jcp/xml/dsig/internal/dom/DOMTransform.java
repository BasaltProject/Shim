/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package org.jcp.xml.dsig.internal.dom;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.spec.AlgorithmParameterSpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.xml.XMLConstants;
import javax.xml.crypto.Data;
import javax.xml.crypto.MarshalException;
import javax.xml.crypto.NodeSetData;
import javax.xml.crypto.OctetStreamData;
import javax.xml.crypto.XMLCryptoContext;
import javax.xml.crypto.XMLStructure;
import javax.xml.crypto.dom.DOMCryptoContext;
import javax.xml.crypto.dom.DOMStructure;
import javax.xml.crypto.dsig.CanonicalizationMethod;
import javax.xml.crypto.dsig.Transform;
import javax.xml.crypto.dsig.TransformException;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.spec.ExcC14NParameterSpec;
import javax.xml.crypto.dsig.spec.TransformParameterSpec;
import javax.xml.crypto.dsig.spec.XPathFilter2ParameterSpec;
import javax.xml.crypto.dsig.spec.XPathFilterParameterSpec;
import javax.xml.crypto.dsig.spec.XPathType;
import javax.xml.crypto.dsig.spec.XSLTTransformParameterSpec;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class DOMTransform implements Transform {
  static final String EXC_NS = "http://www.w3.org/2001/10/xml-exc-c14n#";
  static final String XPATH2_NS = "http://www.w3.org/2002/06/xmldsig-filter2";
  static final String[] KNOWN = {CanonicalizationMethod.INCLUSIVE, CanonicalizationMethod.INCLUSIVE_WITH_COMMENTS,
      CanonicalizationMethod.EXCLUSIVE, CanonicalizationMethod.EXCLUSIVE_WITH_COMMENTS,
      CanonicalizationMethod.INCLUSIVE_11, CanonicalizationMethod.INCLUSIVE_11_WITH_COMMENTS, Transform.BASE64,
      Transform.ENVELOPED, Transform.XPATH, Transform.XPATH2, Transform.XSLT};

  protected String algorithm;
  protected AlgorithmParameterSpec params;
  protected Element transformElem;
  protected Node here;
  protected boolean prefixListActive;
  protected String elementName = "Transform";

  public static boolean isKnown(String alg) {
    return Arrays.asList(KNOWN).contains(alg);
  }

  public DOMTransform(String algorithm, AlgorithmParameterSpec spec) throws InvalidAlgorithmParameterException {
    this.algorithm = algorithm;
    checkParams(spec);
    this.params = spec;
  }

  void checkParams(AlgorithmParameterSpec spec) throws InvalidAlgorithmParameterException {
    if (algorithm.equals(CanonicalizationMethod.INCLUSIVE) || algorithm.equals(CanonicalizationMethod.INCLUSIVE_WITH_COMMENTS)) {
      if (spec != null) {
        throw new InvalidAlgorithmParameterException("no parameters should be specified for Canonical XML C14N "
            + "algorithm");
      }
    } else if (algorithm.equals(CanonicalizationMethod.INCLUSIVE_11)
        || algorithm.equals(CanonicalizationMethod.INCLUSIVE_11_WITH_COMMENTS)) {
      if (spec != null) {
        throw new InvalidAlgorithmParameterException("no parameters should be specified for Canonical XML 1.1 "
            + "algorithm");
      }
    } else if (algorithm.equals(CanonicalizationMethod.EXCLUSIVE)
        || algorithm.equals(CanonicalizationMethod.EXCLUSIVE_WITH_COMMENTS)) {
      if (spec != null && !(spec instanceof ExcC14NParameterSpec)) {
        throw new InvalidAlgorithmParameterException("params must be of type ExcC14NParameterSpec");
      }
    } else if (algorithm.equals(Transform.BASE64) || algorithm.equals(Transform.ENVELOPED)) {
      if (spec != null) {
        throw new InvalidAlgorithmParameterException("params must be null");
      }
    } else if (algorithm.equals(Transform.XPATH)) {
      if (spec == null) {
        throw new InvalidAlgorithmParameterException("params are required");
      }
      if (!(spec instanceof XPathFilterParameterSpec)) {
        throw new InvalidAlgorithmParameterException("params must be of type XPathFilterParameterSpec");
      }
    } else if (algorithm.equals(Transform.XPATH2)) {
      if (spec == null) {
        throw new InvalidAlgorithmParameterException("params are required");
      }
      if (!(spec instanceof XPathFilter2ParameterSpec)) {
        throw new InvalidAlgorithmParameterException("params must be of type XPathFilter2ParameterSpec");
      }
    } else if (algorithm.equals(Transform.XSLT)) {
      if (spec == null) {
        throw new InvalidAlgorithmParameterException("params are required");
      }
      if (!(spec instanceof XSLTTransformParameterSpec)) {
        throw new InvalidAlgorithmParameterException("unrecognized params");
      }
    }
  }

  public DOMTransform(Element elem) throws MarshalException {
    this.transformElem = elem;
    this.algorithm = DOMUtils.getAttributeValue(elem, "Algorithm");
    if (algorithm == null || algorithm.isEmpty()) {
      throw new MarshalException("Algorithm attribute missing");
    }
    DOMUtils.checkAlgorithm(algorithm);
    this.here = elem;
    this.prefixListActive = true;
    Element child = DOMUtils.getFirstChildElement(elem);
    try {
      if (algorithm.equals(CanonicalizationMethod.EXCLUSIVE)
          || algorithm.equals(CanonicalizationMethod.EXCLUSIVE_WITH_COMMENTS)) {
        if (child != null) {
          String list = DOMUtils.getAttributeValue(child, "PrefixList");
          List<String> prefixes = new ArrayList<String>();
          if (list != null) {
            for (String p : list.trim().split("\\s+")) {
              if (!p.isEmpty()) {
                prefixes.add(p);
              }
            }
          }
          params = new ExcC14NParameterSpec(prefixes);
        }
      } else if (algorithm.equals(Transform.XPATH)) {
        if (child == null) {
          throw new MarshalException("XPath element missing");
        }
        params = new XPathFilterParameterSpec(DOMUtils.textOf(child), namespaces(child));
      } else if (algorithm.equals(Transform.XPATH2)) {
        List<XPathType> list = new ArrayList<XPathType>();
        for (Element x : DOMUtils.children(elem)) {
          String f = DOMUtils.getAttributeValue(x, "Filter");
          XPathType.Filter filter = "intersect".equals(f) ? XPathType.Filter.INTERSECT
              : "subtract".equals(f) ? XPathType.Filter.SUBTRACT : XPathType.Filter.UNION;
          list.add(new XPathType(DOMUtils.textOf(x), filter, namespaces(x)));
        }
        params = new XPathFilter2ParameterSpec(list);
      } else if (algorithm.equals(Transform.XSLT)) {
        if (child == null) {
          throw new MarshalException("stylesheet missing");
        }
        params = new XSLTTransformParameterSpec(new DOMStructure(child));
      }
    } catch (IllegalArgumentException e) {
      throw new MarshalException(e);
    }
  }

  static Map<String, String> namespaces(Element e) {
    Map<String, String> m = new HashMap<String, String>();
    for (Node n = e; n != null && n.getNodeType() == Node.ELEMENT_NODE; n = n.getParentNode()) {
      NamedNodeMap attrs = n.getAttributes();
      for (int i = 0; i < attrs.getLength(); i++) {
        Attr a = (Attr) attrs.item(i);
        if (a.getName().startsWith("xmlns:") && !m.containsKey(a.getName().substring(6))) {
          m.put(a.getName().substring(6), a.getValue());
        }
      }
    }
    return m;
  }

  public final String getAlgorithm() {
    return algorithm;
  }

  public final AlgorithmParameterSpec getParameterSpec() {
    return params;
  }

  public boolean isFeatureSupported(String feature) {
    if (feature == null) {
      throw new NullPointerException();
    }
    return false;
  }

  public void setHere(Node here) {
    this.here = here;
  }

  public void marshal(Node parent, String dsPrefix, DOMCryptoContext context) throws MarshalException {
    Document ownerDoc = DOMUtils.getOwnerDocument(parent);
    Element e = DOMUtils.createElement(ownerDoc, elementName, XMLSignature.XMLNS, dsPrefix);
    DOMUtils.setAttribute(e, "Algorithm", algorithm);
    marshalParams(e, ownerDoc, dsPrefix);
    transformElem = e;
    parent.appendChild(e);
    this.here = e;
    this.prefixListActive = true;
  }

  static String nsDecl(String prefix) {
    return prefix == null || prefix.isEmpty() ? "xmlns" : "xmlns:" + prefix;
  }

  void marshalParams(Element e, Document ownerDoc, String dsPrefix) throws MarshalException {
    if (params instanceof ExcC14NParameterSpec) {
      List<String> prefixes = ((ExcC14NParameterSpec) params).getPrefixList();
      if (prefixes.isEmpty()) {
        return;
      }
      Element inc = DOMUtils.createElement(ownerDoc, "InclusiveNamespaces", EXC_NS, dsPrefix);
      inc.setAttributeNS("http://www.w3.org/2000/xmlns/", nsDecl(dsPrefix), EXC_NS);
      StringBuilder sb = new StringBuilder();
      for (String p : prefixes) {
        if (sb.length() > 0) {
          sb.append(' ');
        }
        sb.append(p);
      }
      DOMUtils.setAttribute(inc, "PrefixList", sb.toString());
      e.appendChild(inc);
    } else if (params instanceof XPathFilterParameterSpec) {
      XPathFilterParameterSpec xp = (XPathFilterParameterSpec) params;
      Element x = DOMUtils.createElement(ownerDoc, "XPath", XMLSignature.XMLNS, dsPrefix);
      x.appendChild(ownerDoc.createTextNode(xp.getXPath()));
      for (Map.Entry<String, String> en : new java.util.TreeMap<String, String>(xp.getNamespaceMap()).entrySet()) {
        x.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:" + en.getKey(), en.getValue());
      }
      e.appendChild(x);
    } else if (params instanceof XPathFilter2ParameterSpec) {
      for (XPathType t : ((XPathFilter2ParameterSpec) params).getXPathList()) {
        Element x = DOMUtils.createElement(ownerDoc, "XPath", XPATH2_NS, dsPrefix);
        x.setAttributeNS("http://www.w3.org/2000/xmlns/", nsDecl(dsPrefix), XPATH2_NS);
        x.appendChild(ownerDoc.createTextNode(t.getExpression()));
        DOMUtils.setAttribute(x, "Filter", t.getFilter().toString());
        for (Map.Entry<String, String> en : new java.util.TreeMap<String, String>(t.getNamespaceMap()).entrySet()) {
          x.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:" + en.getKey(), en.getValue());
        }
        e.appendChild(x);
      }
    } else if (params instanceof XSLTTransformParameterSpec) {
      XMLStructure s = ((XSLTTransformParameterSpec) params).getStylesheet();
      if (!(s instanceof DOMStructure)) {
        throw new MarshalException("stylesheet must be a DOMStructure");
      }
      e.appendChild(ownerDoc.importNode(((DOMStructure) s).getNode(), true));
    }
  }

  public Data transform(Data data, XMLCryptoContext xc) throws TransformException {
    return transform(data, xc, null);
  }

  public Data transform(Data data, XMLCryptoContext xc, OutputStream os) throws TransformException {
    if (data == null) {
      throw new NullPointerException("data must not be null");
    }
    if (algorithm.equals(Transform.XSLT) && xc != null
        && Boolean.TRUE.equals(xc.getProperty("org.jcp.xml.dsig.secureValidation"))) {
      throw new TransformException("Transform " + algorithm + " is forbidden when secure validation is enabled");
    }
    try {
      if (algorithm.equals(Transform.ENVELOPED)) {
        return envelopedSignature(data);
      } else if (algorithm.equals(Transform.BASE64)) {
        return base64(data, os);
      } else if (algorithm.equals(Transform.XPATH)) {
        return xpath(data);
      } else if (algorithm.equals(Transform.XPATH2)) {
        return xpath2(data);
      } else if (algorithm.equals(Transform.XSLT)) {
        return xslt(data, os);
      }
      return canonicalize(data, os);
    } catch (IOException e) {
      throw new TransformException(e);
    }
  }

  static Document parse(InputStream in) throws TransformException {
    try {
      DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
      f.setNamespaceAware(true);
      DocumentBuilder b = f.newDocumentBuilder();
      return b.parse(in);
    } catch (Exception e) {
      throw new TransformException("Unable to parse octet stream as XML: " + e.getMessage(), e);
    }
  }

  static byte[] readAll(InputStream in) throws IOException {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    byte[] buf = new byte[4096];
    int n;
    while ((n = in.read(buf)) >= 0) {
      bos.write(buf, 0, n);
    }
    return bos.toByteArray();
  }

  static Set<Node> toSet(NodeSetData<?> d) {
    Set<Node> s = java.util.Collections.newSetFromMap(new IdentityHashMap<Node, Boolean>());
    for (Object o : d) {
      s.add((Node) o);
    }
    return s;
  }

  static Node documentOf(NodeSetData<?> d) {
    for (Object o : d) {
      Node n = (Node) o;
      if (n.getNodeType() == Node.DOCUMENT_NODE) {
        return n;
      }
      Document doc = n.getOwnerDocument();
      if (doc != null) {
        return doc;
      }
    }
    return null;
  }

  private Data canonicalize(Data data, OutputStream os) throws TransformException, IOException {
    boolean exclusive = algorithm.equals(CanonicalizationMethod.EXCLUSIVE)
        || algorithm.equals(CanonicalizationMethod.EXCLUSIVE_WITH_COMMENTS);
    boolean comments = algorithm.endsWith("WithComments");
    Set<String> prefixes = null;
    boolean c11 = algorithm.equals(CanonicalizationMethod.INCLUSIVE_11)
        || algorithm.equals(CanonicalizationMethod.INCLUSIVE_11_WITH_COMMENTS);
    if (params instanceof ExcC14NParameterSpec && prefixListActive) {
      prefixes = new HashSet<String>(((ExcC14NParameterSpec) params).getPrefixList());
    }
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    OutputStream target = os != null ? os : bos;
    if (data instanceof NodeSetData) {
      NodeSetData<?> nsd = (NodeSetData<?>) data;
      Node doc = documentOf(nsd);
      if (doc != null) {
        Set<Node> subset = toSet(nsd);
        C14N.canonicalize(doc, subset, exclusive, c11, comments, prefixes, target);
      }
    } else {
      Document doc = parse(((OctetStreamData) data).getOctetStream());
      C14N.canonicalize(doc, null, exclusive, c11, comments, prefixes, target);
    }
    if (os != null) {
      return null;
    }
    return new OctetStreamData(new ByteArrayInputStream(bos.toByteArray()));
  }

  private static boolean within(Node n, Node root) {
    for (Node p = n; p != null; p = (p.getNodeType() == Node.ATTRIBUTE_NODE ? ((Attr) p).getOwnerElement()
        : p.getParentNode())) {
      if (p == root) {
        return true;
      }
    }
    return false;
  }

  private Data envelopedSignature(Data data) throws TransformException {
    if (!(data instanceof NodeSetData)) {
      throw new TransformException("the enveloped-signature transform requires a node-set");
    }
    Node sig = here;
    while (sig != null && !(sig.getNodeType() == Node.ELEMENT_NODE && "Signature".equals(sig.getLocalName())
        && XMLSignature.XMLNS.equals(sig.getNamespaceURI()))) {
      sig = sig.getParentNode();
    }
    if (sig == null) {
      throw new TransformException("the enveloped-signature transform requires the signature element");
    }
    List<Node> out = new ArrayList<Node>();
    for (Object o : (NodeSetData<?>) data) {
      Node n = (Node) o;
      if (!within(n, sig)) {
        out.add(n);
      }
    }
    return new DOMNodeSetData(out);
  }

  private Data base64(Data data, OutputStream os) throws TransformException, IOException {
    String text;
    if (data instanceof NodeSetData) {
      StringBuilder sb = new StringBuilder();
      for (Object o : (NodeSetData<?>) data) {
        Node n = (Node) o;
        if (n.getNodeType() == Node.TEXT_NODE) {
          sb.append(n.getNodeValue());
        }
      }
      text = sb.toString();
    } else {
      text = new String(readAll(((OctetStreamData) data).getOctetStream()), java.nio.charset.StandardCharsets.ISO_8859_1);
    }
    byte[] decoded;
    try {
      decoded = DOMUtils.decodeBase64(text);
    } catch (IllegalArgumentException e) {
      throw new TransformException(e);
    }
    if (os != null) {
      os.write(decoded);
      return null;
    }
    return new OctetStreamData(new ByteArrayInputStream(decoded));
  }

  private NodeSetData<?> asNodeSet(Data data) throws TransformException {
    if (data instanceof NodeSetData) {
      return (NodeSetData<?>) data;
    }
    try {
      Document doc = parse(((OctetStreamData) data).getOctetStream());
      return new DOMSubTreeData(doc, false);
    } catch (RuntimeException e) {
      throw new TransformException(e);
    }
  }

  private XPath newXPath(Map<String, String> ns) {
    XPath xp = XPathFactory.newInstance().newXPath();
    final Map<String, String> map = ns;
    xp.setNamespaceContext(new javax.xml.namespace.NamespaceContext() {
      public String getNamespaceURI(String prefix) {
        if (prefix == null) {
          throw new IllegalArgumentException();
        }
        if (prefix.equals(XMLConstants.XML_NS_PREFIX)) {
          return XMLConstants.XML_NS_URI;
        }
        String u = map.get(prefix);
        return u == null ? XMLConstants.NULL_NS_URI : u;
      }

      public String getPrefix(String uri) {
        for (Map.Entry<String, String> e : map.entrySet()) {
          if (e.getValue().equals(uri)) {
            return e.getKey();
          }
        }
        return null;
      }

      public java.util.Iterator<String> getPrefixes(String uri) {
        List<String> l = new ArrayList<String>();
        String p = getPrefix(uri);
        if (p != null) {
          l.add(p);
        }
        return l.iterator();
      }
    });
    return xp;
  }

  private Data xpath(Data data) throws TransformException {
    NodeSetData<?> in = asNodeSet(data);
    XPathFilterParameterSpec spec = (XPathFilterParameterSpec) params;
    XPath xp = newXPath(spec.getNamespaceMap());
    List<Node> out = new ArrayList<Node>();
    try {
      javax.xml.xpath.XPathExpression expr = xp.compile(spec.getXPath());
      for (Object o : in) {
        Node n = (Node) o;
        Boolean b = (Boolean) expr.evaluate(n, XPathConstants.BOOLEAN);
        if (b != null && b.booleanValue()) {
          out.add(n);
        }
      }
    } catch (javax.xml.xpath.XPathExpressionException e) {
      throw new TransformException(e);
    }
    return new DOMNodeSetData(out);
  }

  private void addSubtree(Node n, Set<Node> s) {
    s.add(n);
    NamedNodeMap attrs = n.getAttributes();
    if (attrs != null) {
      for (int i = 0; i < attrs.getLength(); i++) {
        s.add(attrs.item(i));
      }
    }
    for (Node c = n.getFirstChild(); c != null; c = c.getNextSibling()) {
      addSubtree(c, s);
    }
  }

  private Data xpath2(Data data) throws TransformException {
    NodeSetData<?> in = asNodeSet(data);
    List<Node> inList = new ArrayList<Node>();
    Set<Node> current = java.util.Collections.newSetFromMap(new IdentityHashMap<Node, Boolean>());
    for (Object o : in) {
      inList.add((Node) o);
      current.add((Node) o);
    }
    Node doc = documentOf(in);
    for (XPathType t : ((XPathFilter2ParameterSpec) params).getXPathList()) {
      XPath xp = newXPath(t.getNamespaceMap());
      Set<Node> selected = java.util.Collections.newSetFromMap(new IdentityHashMap<Node, Boolean>());
      try {
        NodeList nl = (NodeList) xp.evaluate(t.getExpression(), doc, XPathConstants.NODESET);
        for (int i = 0; i < nl.getLength(); i++) {
          addSubtree(nl.item(i), selected);
        }
      } catch (javax.xml.xpath.XPathExpressionException e) {
        throw new TransformException(e);
      }
      if (t.getFilter() == XPathType.Filter.INTERSECT) {
        current.retainAll(selected);
      } else if (t.getFilter() == XPathType.Filter.SUBTRACT) {
        current.removeAll(selected);
      } else {
        current.addAll(selected);
      }
    }
    List<Node> out = new ArrayList<Node>();
    for (Node n : inList) {
      if (current.contains(n)) {
        out.add(n);
      }
    }
    return new DOMNodeSetData(out);
  }

  private Data xslt(Data data, OutputStream os) throws TransformException, IOException {
    byte[] input;
    if (data instanceof NodeSetData) {
      NodeSetData<?> nsd = (NodeSetData<?>) data;
      Node doc = documentOf(nsd);
      input = C14N.canonicalize(doc, toSet(nsd), false, false, null);
    } else {
      input = readAll(((OctetStreamData) data).getOctetStream());
    }
    XSLTTransformParameterSpec spec = (XSLTTransformParameterSpec) params;
    Node sheet = ((DOMStructure) spec.getStylesheet()).getNode();
    try {
      javax.xml.transform.Transformer t = javax.xml.transform.TransformerFactory.newInstance()
          .newTransformer(new javax.xml.transform.dom.DOMSource(sheet));
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      t.transform(new javax.xml.transform.stream.StreamSource(new ByteArrayInputStream(input)),
          new javax.xml.transform.stream.StreamResult(bos));
      if (os != null) {
        os.write(bos.toByteArray());
        return null;
      }
      return new OctetStreamData(new ByteArrayInputStream(bos.toByteArray()));
    } catch (javax.xml.transform.TransformerException e) {
      throw new TransformException(e);
    }
  }
}
