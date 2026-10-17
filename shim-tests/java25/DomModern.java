/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 * Minimum release: 21
 */

import java.io.*; import java.util.*;
import javax.xml.parsers.*;
import org.w3c.dom.*; import org.w3c.dom.ls.*;
import org.xml.sax.*;
public class DomModern {
  interface Body { Object get() throws Exception; }
  static void p(String n, Body b) {
    String o;
    try { Object r = b.get(); o = r instanceof Node ? nodeStr((Node) r) : String.valueOf(r); }
    catch (DOMException e) { o = "!DOMException:" + e.code + ":" + e.getMessage(); }
    catch (Throwable t) { o = "!" + t.getClass().getName() + (t instanceof NullPointerException ? "" : ":" + t.getMessage()); }
    System.out.println(n + "=" + o);
  }
  static String nodeStr(Node n) { return n == null ? "null" : n.toString(); }
  static String esc(String s) { return s == null ? "null" : s.replace("\n", "\\n").replace("\t", "\\t"); }
  static void dump(Node n, StringBuilder sb, int depth) {
    for (int i = 0; i < depth; i++) sb.append("  ");
    sb.append(n.getNodeType()).append(' ').append(n.getNodeName()).append(" v=").append(esc(n.getNodeValue()));
    if (n.getNamespaceURI() != null || n.getLocalName() != null || n.getPrefix() != null) sb.append(" ns=").append(n.getNamespaceURI()).append(" l=").append(n.getLocalName()).append(" p=").append(n.getPrefix());
    if (n instanceof Text && ((Text) n).isElementContentWhitespace()) sb.append(" ecw");
    sb.append('\n');
    NamedNodeMap m = n.getAttributes();
    if (m != null) for (int i = 0; i < m.getLength(); i++) { Attr a = (Attr) m.item(i); for (int j = 0; j <= depth; j++) sb.append("  "); sb.append("@").append(a.getName()).append('=').append(esc(a.getValue())).append(" spec=").append(a.getSpecified()).append(" id=").append(a.isId()).append(" ns=").append(a.getNamespaceURI()).append(" l=").append(a.getLocalName()).append(" p=").append(a.getPrefix()).append(" t=").append(a.getSchemaTypeInfo().getTypeName()).append('\n'); }
    for (Node c = n.getFirstChild(); c != null; c = c.getNextSibling()) dump(c, sb, depth + 1);
  }
  static String dump(Node n) { StringBuilder sb = new StringBuilder(); dump(n, sb, 0); return sb.toString(); }
  static Document parse(String xml, boolean ns) throws Exception { DocumentBuilderFactory f = DocumentBuilderFactory.newInstance(); f.setNamespaceAware(ns); return f.newDocumentBuilder().parse(new InputSource(new StringReader(xml))); }
  static Document parse(String xml, boolean ns, boolean coalesce, boolean noComments, boolean noWs, boolean expand) throws Exception { DocumentBuilderFactory f = DocumentBuilderFactory.newInstance(); f.setNamespaceAware(ns); f.setCoalescing(coalesce); f.setIgnoringComments(noComments); f.setIgnoringElementContentWhitespace(noWs); f.setExpandEntityReferences(expand); return f.newDocumentBuilder().parse(new InputSource(new StringReader(xml))); }
  public static void main(String[] args) throws Exception {
    Locale.setDefault(Locale.ENGLISH);
    System.setErr(System.out);
    String doc1 = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><!DOCTYPE r [<!ENTITY e \"ev\"><!NOTATION n SYSTEM \"s\"><!ENTITY u SYSTEM \"u.gif\" NDATA n><!ATTLIST r id ID #IMPLIED d CDATA \"dv\" en (a|b) \"a\"><!ELEMENT r ANY>]><!-- c --><r z=\"1\" b=\"2\" id=\"x\"><a>t1<![CDATA[cd]]>t2</a><?pi data?>&e;<b xmlns=\"u\" xmlns:p=\"v\" p:q=\"w\"/> <c/></r><!-- after -->";
    for (boolean ns : new boolean[]{false, true}) {
      Document d = parse(doc1, ns);
      System.out.println("== doc1 ns=" + ns); System.out.print(dump(d));
      DocumentType dt = d.getDoctype();
      p("doctype", () -> dt + "|" + dt.getName() + "|" + dt.getPublicId() + "|" + dt.getSystemId() + "|" + esc(dt.getInternalSubset()));
      p("entities", () -> { StringBuilder sb = new StringBuilder(); for (int i = 0; i < dt.getEntities().getLength(); i++) { Entity e = (Entity) dt.getEntities().item(i); sb.append(e.getNodeName()).append(':').append(e.getNotationName()).append(':').append(e.getSystemId()).append(':').append(e.getPublicId()).append(';'); } for (int i = 0; i < dt.getNotations().getLength(); i++) { Notation e = (Notation) dt.getNotations().item(i); sb.append(e.getNodeName()).append(':').append(e.getSystemId()).append(':').append(e.getPublicId()).append(';'); } return sb; });
      p("docinfo", () -> d.getXmlVersion() + "|" + d.getXmlEncoding() + "|" + d.getInputEncoding() + "|" + d.getXmlStandalone() + "|" + d.getDocumentURI() + "|" + d.getStrictErrorChecking() + "|" + d.getBaseURI());
      p("byid", () -> d.getElementById("x") + "|" + d.getElementById("nope"));
      p("tagname", () -> d.getElementsByTagName("*").getLength() + "|" + d.getElementsByTagName("a").getLength() + "|" + d.getElementsByTagNameNS("u", "b").getLength() + "|" + d.getElementsByTagNameNS("*", "*").getLength() + "|" + d.getElementsByTagNameNS(null, "c").getLength());
      p("textcontent", () -> d.getDocumentElement().getTextContent() + "|" + d.getTextContent() + "|" + d.getDoctype().getTextContent() + "|" + d.getFirstChild().getTextContent());
    }
    String doc2 = "<!DOCTYPE a [<!ELEMENT a (b)*><!ELEMENT b EMPTY><!ENTITY e \"t<b/>u\">]><a><!--c--> <b/> <![CDATA[x]]>y&e;</a>";
    for (int mode = 0; mode < 6; mode++) {
      final int m = mode;
      Document d = parse(doc2, m % 2 == 1, m >= 2 && m < 4, m >= 2 && m < 4, m >= 2 && m < 4, m < 4);
      System.out.println("== doc2 mode=" + m); System.out.print(dump(d.getDocumentElement()));
    }
    Document d = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
    p("new doc", () -> d + "|" + d.getXmlVersion() + "|" + d.getXmlStandalone() + "|" + d.getInputEncoding() + "|" + d.getXmlEncoding() + "|" + d.getDocumentURI() + "|" + d.getDoctype() + "|" + d.getDocumentElement() + "|" + d.getBaseURI() + "|" + d.getStrictErrorChecking());
    String[] names = {"a", "a:b", "1a", "a b", "", "a:b:c", ":a", "a:", "xml:a", "xmlns", "xmlns:a", "_a", "a.b-c", "\u00e9", "a\u0300"};
    for (String nm : names) {
      p("createElement " + nm, () -> d.createElement(nm));
      p("createAttribute " + nm, () -> d.createAttribute(nm));
      p("createPI " + nm, () -> d.createProcessingInstruction(nm, "d"));
      p("createEntityReference " + nm, () -> d.createEntityReference(nm));
      for (String ns : new String[]{null, "", "http://u", "http://www.w3.org/XML/1998/namespace", "http://www.w3.org/2000/xmlns/"}) {
        p("createElementNS " + ns + " " + nm, () -> { Element e = d.createElementNS(ns, nm); return e + "|" + e.getNamespaceURI() + "|" + e.getPrefix() + "|" + e.getLocalName() + "|" + e.getTagName(); });
        p("createAttributeNS " + ns + " " + nm, () -> { Attr e = d.createAttributeNS(ns, nm); return e + "|" + e.getNamespaceURI() + "|" + e.getPrefix() + "|" + e.getLocalName() + "|" + e.getName(); });
      }
    }
    p("createText", () -> d.createTextNode("t") + "|" + d.createTextNode(null) + "|" + d.createComment("c") + "|" + d.createCDATASection("cd") + "|" + d.createDocumentFragment());
    p("createText null", () -> { Text t = d.createTextNode(null); return t.getData() + "|" + t.getLength(); });
    p("owner", () -> d.createElement("a").getOwnerDocument() == d);
    DOMImplementation impl = d.getImplementation();
    String[] feats = {"Core", "XML", "XMLVersion", "Events", "Traversal", "Range", "LS", "LS-Async", "Views", "ElementTraversal", "+Core", "core", "xml", "Foo", ""};
    String[] vers = {null, "", "1.0", "2.0", "3.0", "1.1", "4.0"};
    for (String f : feats) { StringBuilder sb = new StringBuilder(); for (String v : vers) sb.append(impl.hasFeature(f, v) ? 1 : 0); System.out.println("hasFeature " + f + "=" + sb); }
    p("getFeature", () -> (impl.getFeature("Core", "3.0") != null) + "|" + impl.getFeature("Foo", "1.0") + "|" + (impl.getFeature("LS", "3.0") != null) + "|" + impl.getFeature("Core", "9.0"));
    p("createDocumentType", () -> { DocumentType t = impl.createDocumentType("html", "-//W3C", "x.dtd"); return t + "|" + t.getName() + "|" + t.getPublicId() + "|" + t.getSystemId() + "|" + t.getInternalSubset() + "|" + t.getOwnerDocument() + "|" + t.getEntities().getLength(); });
    for (String nm : new String[]{"a", "a:b", "1a", "a:b:c", ":a", "", "a b"}) p("createDocumentType " + nm, () -> impl.createDocumentType(nm, null, null));
    p("createDocument", () -> { Document x = impl.createDocument("http://u", "p:root", null); return x + "|" + x.getDocumentElement() + "|" + x.getDocumentElement().getNamespaceURI() + "|" + x.getDoctype(); });
    p("createDocument null", () -> { Document x = impl.createDocument(null, null, null); return x + "|" + x.getDocumentElement(); });
    p("createDocument dt", () -> { DocumentType t = impl.createDocumentType("r", "pub", "sys"); Document x = impl.createDocument(null, "r", t); return x.getDoctype() == t && t.getOwnerDocument() == x; });
    p("createDocument bad ns", () -> impl.createDocument(null, "p:r", null));
    p("createDocument dt owned", () -> { DocumentType t = impl.createDocumentType("r", null, null); impl.createDocument(null, "r", t); return impl.createDocument(null, "r", t); });
    p("createDocument bad name", () -> impl.createDocument("u", "1a", null));
    Document s = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
    Element root = s.createElement("root");
    p("append root", () -> s.appendChild(root));
    p("append second root", () -> s.appendChild(s.createElement("again")));
    p("append text to doc", () -> s.appendChild(s.createTextNode("t")));
    p("append comment to doc", () -> s.insertBefore(s.createComment("c"), root));
    p("append pi to doc", () -> s.appendChild(s.createProcessingInstruction("p", "d")));
    p("append cdata to doc", () -> s.appendChild(s.createCDATASection("d")));
    p("append attr to elem", () -> root.appendChild(s.createAttribute("a")));
    p("append doc to elem", () -> root.appendChild(s));
    p("append self", () -> root.appendChild(root));
    Element child = s.createElement("child");
    p("append child", () -> root.appendChild(child));
    p("append ancestor to descendant", () -> child.appendChild(root));
    p("insertBefore not child", () -> root.insertBefore(s.createElement("x"), s.createElement("y")));
    p("insertBefore self", () -> root.insertBefore(child, child));
    p("replaceChild same", () -> root.replaceChild(child, child));
    p("removeChild not child", () -> root.removeChild(s.createElement("z")));
    p("removeChild null", () -> root.removeChild(null));
    Document other = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
    p("append foreign", () -> root.appendChild(other.createElement("f")));
    p("insert foreign text", () -> root.appendChild(other.createTextNode("f")));
    DocumentFragment frag = s.createDocumentFragment();
    frag.appendChild(s.createElement("f1")); frag.appendChild(s.createTextNode("ft")); frag.appendChild(s.createElement("f2"));
    p("append fragment", () -> root.appendChild(frag) + "|" + frag.hasChildNodes() + "|" + root.getChildNodes().getLength());
    p("fragment into doc", () -> { DocumentFragment f2 = s.createDocumentFragment(); f2.appendChild(s.createElement("e1")); return s.appendChild(f2); });
    p("fragment two elements", () -> { Document x = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument(); DocumentFragment f2 = x.createDocumentFragment(); f2.appendChild(x.createElement("e1")); f2.appendChild(x.createElement("e2")); return x.appendChild(f2); });
    p("children", () -> { StringBuilder sb = new StringBuilder(); for (Node n = root.getFirstChild(); n != null; n = n.getNextSibling()) sb.append(n.getNodeName()).append(','); sb.append(root.getLastChild()).append(root.getChildNodes().item(1)).append(root.getChildNodes().item(99)).append(root.getChildNodes().item(-1)); return sb; });
    p("move child", () -> { Node moved = root.getFirstChild(); root.appendChild(moved); return (root.getLastChild() == moved) + "|" + root.getChildNodes().getLength(); });
    p("replace", () -> { Node old = root.getFirstChild(); Node n = root.replaceChild(s.createElement("rep"), old); return n + "|" + old.getParentNode() + "|" + root.getFirstChild(); });
    p("remove", () -> { Node n = root.removeChild(root.getFirstChild()); return n + "|" + root.getChildNodes().getLength() + "|" + n.getParentNode() + "|" + n.getNextSibling(); });
    p("text content set", () -> { root.setTextContent("hello"); return root.getFirstChild() + "|" + root.getChildNodes().getLength(); });
    p("text content clear", () -> { root.setTextContent(""); return root.hasChildNodes(); });
    p("text content null", () -> { root.setTextContent(null); return root.hasChildNodes(); });
    p("doctype twice", () -> { Document x = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument(); x.appendChild(impl.createDocumentType("a", null, null)); return x.appendChild(impl.createDocumentType("b", null, null)); });
    p("doctype after element", () -> { Document x = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument(); x.appendChild(x.createElement("a")); x.insertBefore(impl.createDocumentType("a", null, null), x.getDocumentElement()); return x.getDoctype() + "|" + x.getFirstChild(); });
    p("replace doc element", () -> { Document x = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument(); x.appendChild(x.createElement("a")); x.replaceChild(x.createElement("b"), x.getDocumentElement()); return x.getDocumentElement(); });
    p("replace doc element with text", () -> { Document x = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument(); x.appendChild(x.createElement("a")); return x.replaceChild(x.createTextNode("t"), x.getDocumentElement()); });
    p("strict false", () -> { Document x = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument(); x.setStrictErrorChecking(false); x.appendChild(x.createElement("a")); x.appendChild(x.createElement("b")); return x.getChildNodes().getLength() + "|" + x.createElement("1bad"); });
  }
}
