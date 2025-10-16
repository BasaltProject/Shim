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
public class DomModern2 {
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
  static String nodes(NamedNodeMap l) { StringBuilder sb = new StringBuilder("{"); for (int i = 0; i < l.getLength(); i++) sb.append(i > 0 ? "," : "").append(l.item(i).getNodeName()); return sb.append("}").toString(); }
  static String nodes(NodeList l) { StringBuilder sb = new StringBuilder("["); for (int i = 0; i < l.getLength(); i++) sb.append(i > 0 ? "," : "").append(l.item(i).getNodeName()); return sb.append("]").toString(); }
  static Document newDoc() throws Exception { return DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument(); }
  public static void main(String[] args) throws Exception {
    Locale.setDefault(Locale.ENGLISH);
    System.setErr(System.out);
    Document d = newDoc();
    Element e = d.createElement("e");
    d.appendChild(e);
    // attributes
    p("getAttribute missing", () -> "[" + e.getAttribute("x") + "]" + e.hasAttribute("x") + e.getAttributeNode("x") + e.hasAttributes());
    p("setAttribute", () -> { e.setAttribute("z", "1"); e.setAttribute("a", "2"); e.setAttribute("m", "3"); return nodes(e.getAttributes()) + e.getAttribute("a") + e.hasAttributes() + e.getAttributes().getLength(); });
    p("setAttribute bad", () -> { e.setAttribute("1x", "v"); return "no"; });
    p("setAttribute null value", () -> { e.setAttribute("n", null); return "[" + e.getAttribute("n") + "]"; });
    p("setAttribute replace", () -> { e.setAttribute("a", "two"); return e.getAttribute("a") + nodes(e.getAttributes()); });
    p("removeAttribute", () -> { e.removeAttribute("a"); e.removeAttribute("nothere"); return nodes(e.getAttributes()); });
    Attr at = d.createAttribute("att");
    p("attr defaults", () -> at + "|" + at.getValue() + "|" + at.getSpecified() + "|" + at.getOwnerElement() + "|" + at.getParentNode() + "|" + at.getFirstChild() + "|" + at.isId() + "|" + at.getChildNodes().getLength() + "|" + at.getNodeValue());
    p("attr setValue", () -> { at.setValue("v1"); return at.getValue() + "|" + at.getFirstChild() + "|" + at.getChildNodes().getLength() + "|" + at.getTextContent(); });
    p("setAttributeNode", () -> { Attr old = e.setAttributeNode(at); return old + "|" + at.getOwnerElement() + "|" + nodes(e.getAttributes()); });
    p("setAttributeNode inuse", () -> { Element e2 = d.createElement("e2"); return e2.setAttributeNode(at); });
    p("setAttributeNode replace", () -> { Attr a2 = d.createAttribute("att"); a2.setValue("v2"); Attr old = e.setAttributeNode(a2); return old + "|" + old.getOwnerElement() + "|" + a2.getOwnerElement().getNodeName() + "|" + e.getAttribute("att"); });
    p("setAttributeNode same", () -> { Attr cur = e.getAttributeNode("att"); return e.setAttributeNode(cur) + "|" + nodes(e.getAttributes()); });
    p("setAttributeNode foreign", () -> { Document o = newDoc(); return e.setAttributeNode(o.createAttribute("f")); });
    p("removeAttributeNode", () -> { Attr cur = e.getAttributeNode("att"); Attr r = e.removeAttributeNode(cur); return r + "|" + r.getOwnerElement() + "|" + nodes(e.getAttributes()); });
    p("removeAttributeNode missing", () -> e.removeAttributeNode(d.createAttribute("zz")));
    p("attr node value to text", () -> { Attr a3 = d.createAttribute("t"); a3.setNodeValue("x&y"); return a3 + "|" + a3.getValue() + "|" + a3.getNodeValue(); });
    p("attr with entity ref", () -> { Attr a4 = d.createAttribute("er"); a4.appendChild(d.createTextNode("a")); a4.appendChild(d.createTextNode("b")); return a4.getValue() + "|" + a4.getChildNodes().getLength(); });
    p("attr append element", () -> d.createAttribute("x").appendChild(d.createElement("a")));
    // namespaced attributes
    Element ns = d.createElementNS("http://ns", "p:el");
    p("ns elem", () -> ns + "|" + ns.getNamespaceURI() + "|" + ns.getPrefix() + "|" + ns.getLocalName() + "|" + ns.getTagName() + "|" + ns.getNodeName());
    p("setAttributeNS", () -> { ns.setAttributeNS("http://a", "a:x", "1"); ns.setAttributeNS("http://a", "b:x", "2"); ns.setAttributeNS(null, "plain", "3"); ns.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:q", "http://q"); return nodes(ns.getAttributes()) + "|" + ns.getAttributeNS("http://a", "x") + "|" + ns.getAttribute("b:x") + "|" + ns.getAttributeNode("a:x"); });
    p("setAttributeNS prefix change", () -> { Attr a = ns.getAttributeNodeNS("http://a", "x"); return a.getName() + "|" + a.getPrefix() + "|" + ns.getAttributes().getLength(); });
    p("getAttributeNS missing", () -> "[" + ns.getAttributeNS("http://a", "nope") + "][" + ns.getAttributeNS(null, "plain") + "][" + ns.getAttributeNS("", "plain") + "]" + ns.hasAttributeNS("http://a", "x") + ns.hasAttributeNS("http://a", "nope"));
    p("setAttributeNS errors", () -> { StringBuilder sb = new StringBuilder(); String[][] cases = {{null, "a:b"}, {"http://u", "1a"}, {"http://u", ":a"}, {"http://u", "a:b:c"}, {"http://www.w3.org/2000/xmlns/", "a"}, {"http://x", "xmlns"}, {null, "xmlns"}, {"http://x", "xml:a"}, {"http://www.w3.org/XML/1998/namespace", "xml:lang"}}; for (String[] c : cases) { try { ns.setAttributeNS(c[0], c[1], "v"); sb.append("ok;"); } catch (DOMException x) { sb.append(x.code).append(';'); } } return sb; });
    p("removeAttributeNS", () -> { ns.removeAttributeNS("http://a", "x"); ns.removeAttributeNS("http://a", "nope"); return nodes(ns.getAttributes()); });
    p("setAttributeNodeNS", () -> { Attr a = d.createAttributeNS("http://n", "n:a"); a.setValue("nv"); Attr old = ns.setAttributeNodeNS(a); Attr a2 = d.createAttributeNS("http://n", "m:a"); a2.setValue("nv2"); Attr old2 = ns.setAttributeNodeNS(a2); return old + "|" + old2 + "|" + nodes(ns.getAttributes()) + "|" + old2.getOwnerElement(); });
    p("getNamedItem", () -> { NamedNodeMap m = ns.getAttributes(); return m.getNamedItem("plain") + "|" + m.getNamedItemNS("http://n", "a") + "|" + m.getNamedItem("zzz") + "|" + m.item(0) + "|" + m.item(99) + "|" + m.item(-1); });
    p("removeNamedItem", () -> { NamedNodeMap m = ns.getAttributes(); Node r = m.removeNamedItem("plain"); return r + "|" + ((Attr) r).getOwnerElement() + "|" + m.getLength(); });
    p("removeNamedItem missing", () -> ns.getAttributes().removeNamedItem("missing"));
    p("removeNamedItemNS missing", () -> ns.getAttributes().removeNamedItemNS("http://x", "missing"));
    p("setNamedItem non-attr", () -> ns.getAttributes().setNamedItem(d.createTextNode("t")));
    p("setNamedItemNS", () -> { Attr a = d.createAttributeNS("http://s", "s:z"); return ns.getAttributes().setNamedItemNS(a) + "|" + a.getOwnerElement().getNodeName(); });
    p("element setPrefix", () -> { Element x = d.createElementNS("http://ns", "p:el"); x.setPrefix("q"); String r1 = x.getTagName() + "|" + x.getPrefix(); x.setPrefix(null); return r1 + "|" + x.getTagName() + "|" + x.getPrefix(); });
    p("element setPrefix errors", () -> { StringBuilder sb = new StringBuilder(); for (String pf : new String[]{"1a", "a:b", "xml", "xmlns", "a b", ""}) { Element x = d.createElementNS("http://ns", "p:el"); try { x.setPrefix(pf); sb.append("ok:").append(x.getTagName()).append(';'); } catch (DOMException z) { sb.append(z.code).append(';'); } } Element y = d.createElement("plain"); try { y.setPrefix("p"); sb.append("ok:").append(y.getTagName()); } catch (DOMException z) { sb.append(z.code); } Element nn = d.createElementNS(null, "n"); try { nn.setPrefix("p"); sb.append("ok"); } catch (DOMException z) { sb.append(z.code); } return sb; });
    p("attr setPrefix", () -> { Attr a = d.createAttributeNS("http://a", "a:x"); a.setPrefix("b"); return a.getName() + "|" + a.getNodeName() + "|" + a.getPrefix(); });
    p("level1 ns props", () -> { Element l1 = d.createElement("x:y"); return l1.getPrefix() + "|" + l1.getLocalName() + "|" + l1.getNamespaceURI() + "|" + l1.getTagName(); });
    // elements by tag
    Document td = parse("<r xmlns:p='u'><a/><b><a/><p:a/></b><a x='1'/></r>", true);
    NodeList live = td.getElementsByTagName("a");
    p("tag lists", () -> nodes(live) + live.getLength() + nodes(td.getElementsByTagName("*")) + nodes(td.getElementsByTagNameNS("u", "a")) + nodes(td.getElementsByTagNameNS("*", "a")) + nodes(td.getElementsByTagNameNS(null, "a")) + nodes(td.getElementsByTagNameNS("u", "*")) + nodes(td.getDocumentElement().getElementsByTagName("a")) + nodes(td.getElementsByTagName("p:a")) + nodes(td.getElementsByTagName("none")));
    p("live list", () -> { Element b = (Element) td.getElementsByTagName("b").item(0); int before = live.getLength(); b.removeChild(b.getFirstChild()); int after = live.getLength(); td.getDocumentElement().appendChild(td.createElement("a")); return before + "|" + after + "|" + live.getLength() + "|" + live.item(live.getLength() - 1) + "|" + live.item(99); });
    p("element traversal", () -> { ElementTraversal r = (ElementTraversal) td.getDocumentElement(); return r.getChildElementCount() + "|" + r.getFirstElementChild() + "|" + r.getLastElementChild() + "|" + ((ElementTraversal) r.getFirstElementChild()).getNextElementSibling() + "|" + ((ElementTraversal) r.getLastElementChild()).getPreviousElementSibling() + "|" + ((ElementTraversal) r).getNextElementSibling(); });
    // ids
    p("ids", () -> { Document x = newDoc(); Element r = x.createElement("r"); x.appendChild(r); r.setAttribute("id", "i1"); r.setAttribute("other", "i2"); String s1 = x.getElementById("i1") + ""; r.setIdAttribute("id", true); String s2 = x.getElementById("i1") + "|" + r.getAttributeNode("id").isId(); r.setIdAttribute("id", false); String s3 = x.getElementById("i1") + ""; r.setIdAttributeNode(r.getAttributeNode("other"), true); String s4 = x.getElementById("i2") + ""; return s1 + "|" + s2 + "|" + s3 + "|" + s4; });
    p("ids errors", () -> { Document x = newDoc(); Element r = x.createElement("r"); try { r.setIdAttribute("nope", true); } catch (DOMException z) { return z.code; } return "no"; });
    p("ids ns", () -> { Document x = newDoc(); Element r = x.createElementNS("u", "r"); r.setAttributeNS("u", "p:k", "v1"); r.setIdAttributeNS("u", "k", true); x.appendChild(r); return x.getElementById("v1") + "|" + r.getAttributeNodeNS("u", "k").isId(); });
    // character data
    Text t = d.createTextNode("hello");
    p("text basics", () -> t + "|" + t.getData() + "|" + t.getLength() + "|" + t.getNodeValue() + "|" + t.getNodeName() + "|" + t.getNodeType() + "|" + t.isElementContentWhitespace() + "|" + t.getTextContent());
    p("substringData", () -> t.substringData(1, 3) + "|" + t.substringData(0, 100) + "|" + t.substringData(5, 1) + "|[" + t.substringData(5, 1) + "]");
    p("substringData bad", () -> t.substringData(6, 1));
    p("substringData neg", () -> t.substringData(-1, 1));
    p("substringData negcount", () -> t.substringData(0, -1));
    p("insertData", () -> { t.insertData(2, "XX"); return t.getData(); });
    p("insertData bad", () -> { t.insertData(99, "X"); return "no"; });
    p("deleteData", () -> { t.deleteData(2, 2); t.deleteData(3, 100); return t.getData(); });
    p("deleteData bad", () -> { t.deleteData(99, 1); return "no"; });
    p("deleteData negcount", () -> { t.deleteData(0, -1); return "no"; });
    p("replaceData", () -> { t.replaceData(1, 1, "EE"); return t.getData(); });
    p("replaceData bad", () -> { t.replaceData(50, 1, "x"); return "no"; });
    p("appendData", () -> { t.appendData("!"); t.setData("new"); t.setNodeValue("nv"); return t.getData(); });
    p("setData null", () -> { Text x = d.createTextNode("a"); x.setData(null); return x.getData() + "|" + x; });
    p("splitText", () -> { Element p = d.createElement("p"); Text tx = d.createTextNode("abcdef"); p.appendChild(tx); Text r = tx.splitText(2); return tx.getData() + "|" + r.getData() + "|" + p.getChildNodes().getLength() + "|" + tx.getNextSibling().getNodeValue() + "|" + r.getPreviousSibling().getNodeValue(); });
    p("splitText end", () -> { Text tx = d.createTextNode("abc"); Text r = tx.splitText(3); return r.getData().length() + "|" + tx.getData(); });
    p("splitText bad", () -> d.createTextNode("abc").splitText(4));
    p("splitText detached", () -> { Text tx = d.createTextNode("abc"); Text r = tx.splitText(1); return tx.getData() + r.getData() + r.getParentNode(); });
    p("normalize", () -> { Element p = d.createElement("p"); p.appendChild(d.createTextNode("a")); p.appendChild(d.createTextNode("")); p.appendChild(d.createTextNode("b")); p.appendChild(d.createCDATASection("c")); p.appendChild(d.createTextNode("d")); p.appendChild(d.createTextNode("e")); Element q = d.createElement("q"); q.appendChild(d.createTextNode("")); q.appendChild(d.createTextNode("x")); q.appendChild(d.createTextNode("y")); p.appendChild(q); p.normalize(); return dump(p).replace("\n", "/"); });
    p("wholeText", () -> { Element p = d.createElement("p"); Text a = d.createTextNode("A"); p.appendChild(a); p.appendChild(d.createCDATASection("B")); Text c = d.createTextNode("C"); p.appendChild(c); p.appendChild(d.createComment("x")); p.appendChild(d.createTextNode("D")); return a.getWholeText() + "|" + c.getWholeText() + "|" + ((Text) p.getLastChild()).getWholeText(); });
    p("replaceWholeText", () -> { Element p = d.createElement("p"); Text a = d.createTextNode("A"); p.appendChild(a); p.appendChild(d.createCDATASection("B")); p.appendChild(d.createTextNode("C")); p.appendChild(d.createComment("x")); p.appendChild(d.createTextNode("D")); Text r = a.replaceWholeText("NEW"); return r + "|" + r.getData() + "|" + (r == a) + "|" + p.getChildNodes().getLength() + "|" + dump(p).replace("\n", "/"); });
    p("replaceWholeText empty", () -> { Element p = d.createElement("p"); Text a = d.createTextNode("A"); p.appendChild(a); Text r = a.replaceWholeText(""); return r + "|" + p.getChildNodes().getLength(); });
    p("comment", () -> { Comment c = d.createComment("hi"); c.appendData("!"); c.insertData(0, ">"); return c + "|" + c.getData() + "|" + c.getNodeName() + "|" + c.getNodeType() + "|" + c.getTextContent() + "|" + c.getLength(); });
    p("pi", () -> { ProcessingInstruction pi = d.createProcessingInstruction("tgt", "data"); pi.setData("d2"); return pi + "|" + pi.getTarget() + "|" + pi.getData() + "|" + pi.getNodeName() + "|" + pi.getNodeValue() + "|" + pi.getNodeType() + "|" + pi.getTextContent(); });
    p("pi setNodeValue", () -> { ProcessingInstruction pi = d.createProcessingInstruction("tgt", "data"); pi.setNodeValue("zz"); pi.setTextContent("tc"); return pi.getData(); });
    p("cdata", () -> { CDATASection c = d.createCDATASection("raw"); return c + "|" + c.getNodeName() + "|" + c.getNodeType() + "|" + c.getData() + "|" + (c instanceof Text) + "|" + ((Text) c).getWholeText(); });
    p("entity reference", () -> { EntityReference er = d.createEntityReference("nbsp"); return er + "|" + er.getNodeName() + "|" + er.getNodeType() + "|" + er.getNodeValue() + "|" + er.getTextContent() + "|" + er.hasChildNodes(); });
    p("fragment", () -> { DocumentFragment f = d.createDocumentFragment(); f.appendChild(d.createTextNode("a")); f.appendChild(d.createElement("b")); f.setTextContent("zz"); return f + "|" + f.getNodeName() + "|" + f.getNodeValue() + "|" + f.getTextContent() + "|" + f.getChildNodes().getLength() + "|" + f.getParentNode(); });
    // clone / equal
    Document cd = parse("<!DOCTYPE r [<!ATTLIST r d CDATA 'dv'>]><r a='1' xmlns:p='u'><p:c x='y'>t<!--c--><![CDATA[cd]]></p:c><?pi d?></r>", true);
    Element cr = cd.getDocumentElement();
    p("cloneNode shallow", () -> { Node c = cr.cloneNode(false); return dump(c).replace("\n", "/") + "|" + c.getParentNode() + "|" + c.getOwnerDocument().equals(cd) + "|" + c.hasChildNodes() + "|" + ((Element) c).getAttributes().getLength(); });
    p("cloneNode deep", () -> { Node c = cr.cloneNode(true); return dump(c).replace("\n", "/") + "|" + c.isEqualNode(cr) + "|" + c.isSameNode(cr) + "|" + (c != cr); });
    p("cloneNode attr", () -> { Attr a = cr.getAttributeNode("d"); Attr c = (Attr) a.cloneNode(false); return c + "|" + c.getSpecified() + "|" + c.getOwnerElement() + "|" + c.getValue() + "|" + a.getSpecified(); });
    p("cloneNode doc", () -> { Document c = (Document) cd.cloneNode(true); return dump(c).replace("\n", "/") + "|" + (c.getDoctype() != cd.getDoctype()); });
    p("cloneNode doc shallow", () -> { Document c = (Document) cd.cloneNode(false); return c.getChildNodes().getLength() + "|" + c.getDocumentElement(); });
    p("cloneNode text", () -> { Node c = cr.getFirstChild().getFirstChild().cloneNode(true); return c + "|" + c.getNodeType(); });
    p("isEqualNode", () -> { Element a = d.createElement("a"); Element b = d.createElement("a"); a.setAttribute("x", "1"); b.setAttribute("x", "1"); boolean e1 = a.isEqualNode(b); b.setAttribute("x", "2"); boolean e2 = a.isEqualNode(b); b.setAttribute("x", "1"); a.appendChild(d.createTextNode("t")); boolean e3 = a.isEqualNode(b); b.appendChild(d.createTextNode("t")); boolean e4 = a.isEqualNode(b); Element c = d.createElementNS("u", "a"); return e1 + "|" + e2 + "|" + e3 + "|" + e4 + "|" + a.isEqualNode(c) + "|" + a.isEqualNode(null) + "|" + a.isEqualNode(a) + "|" + d.createTextNode("x").isEqualNode(d.createComment("x")); });
    p("isSameNode", () -> cr.isSameNode(cr) + "|" + cr.isSameNode(cd.getDocumentElement()) + "|" + cr.isSameNode(null));
    // positions
    Document pd = parse("<r><a><b/><c/></a><d/></r>", false);
    Element pr = pd.getDocumentElement(); Element pa = (Element) pr.getFirstChild(); Element pb = (Element) pa.getFirstChild(); Element pc = (Element) pa.getLastChild(); Element pdd = (Element) pr.getLastChild();
    p("compareDocumentPosition", () -> { Node[] ns2 = {pd, pr, pa, pb, pc, pdd}; StringBuilder sb = new StringBuilder(); for (Node x : ns2) { for (Node y : ns2) sb.append(x.compareDocumentPosition(y)).append(','); sb.append('/'); } return sb; });
    p("compareDocumentPosition attr", () -> { pb.setAttribute("q", "1"); pc.setAttribute("q", "1"); Attr bq = pb.getAttributeNode("q"); Attr cq = pc.getAttributeNode("q"); return pb.compareDocumentPosition(bq) + "," + bq.compareDocumentPosition(pb) + "," + bq.compareDocumentPosition(cq) + "," + cq.compareDocumentPosition(bq) + "," + pr.compareDocumentPosition(bq) + "," + bq.compareDocumentPosition(pr) + "," + bq.compareDocumentPosition(bq); });
    p("compareDocumentPosition disconnected", () -> { Element x = d.createElement("x"); short pos = pr.compareDocumentPosition(x); return (pos & Node.DOCUMENT_POSITION_DISCONNECTED) != 0 && (pos & Node.DOCUMENT_POSITION_IMPLEMENTATION_SPECIFIC) != 0 && ((pos & (Node.DOCUMENT_POSITION_PRECEDING | Node.DOCUMENT_POSITION_FOLLOWING)) != 0); });
    p("compareDocumentPosition siblings", () -> pb.compareDocumentPosition(pc) + "|" + pc.compareDocumentPosition(pb) + "|" + pa.compareDocumentPosition(pdd) + "|" + pdd.compareDocumentPosition(pb));
    // namespace lookup
    Document nd = parse("<r xmlns='d' xmlns:p='u' xmlns:q='v'><a xmlns:p='w'><b p:x='1'/></a><c xmlns=''/><p:d xmlns:r='u'/></r>", true);
    Element nr = nd.getDocumentElement(); Element na = (Element) nr.getFirstChild(); Element nb = (Element) na.getFirstChild(); Element nc = (Element) nr.getChildNodes().item(1); Element ndd = (Element) nr.getLastChild();
    p("lookupNamespaceURI", () -> { StringBuilder sb = new StringBuilder(); for (Node x : new Node[]{nd, nr, na, nb, nc, ndd, nb.getAttributeNode("p:x"), nb.getAttributeNode("p:x").getFirstChild()}) { sb.append(x.lookupNamespaceURI(null)).append('/').append(x.lookupNamespaceURI("p")).append('/').append(x.lookupNamespaceURI("q")).append('/').append(x.lookupNamespaceURI("zz")).append('/').append(x.lookupNamespaceURI("xml")).append(';'); } return sb; });
    p("lookupPrefix", () -> { StringBuilder sb = new StringBuilder(); for (Node x : new Node[]{nd, nr, na, nb, nc, ndd}) { sb.append(x.lookupPrefix("u")).append('/').append(x.lookupPrefix("w")).append('/').append(x.lookupPrefix("v")).append('/').append(x.lookupPrefix("d")).append('/').append(x.lookupPrefix("nope")).append('/').append(x.lookupPrefix(null)).append(';'); } return sb; });
    p("isDefaultNamespace", () -> { StringBuilder sb = new StringBuilder(); for (Node x : new Node[]{nd, nr, na, nb, nc, ndd}) { sb.append(x.isDefaultNamespace("d")).append('/').append(x.isDefaultNamespace("u")).append('/').append(x.isDefaultNamespace(null)).append('/').append(x.isDefaultNamespace("")).append(';'); } return sb; });
    p("lookup on level1", () -> { Element l1 = d.createElement("x"); return l1.lookupNamespaceURI(null) + "|" + l1.lookupPrefix("u") + "|" + l1.isDefaultNamespace(null); });
    p("baseURI", () -> { Document x = parse("<r xml:base='http://h/a/'><c xml:base='b/'><e/></c><f xml:base='/abs'/><g xml:base='http://o/x'/></r>", true); Element r = x.getDocumentElement(); Element c = (Element) r.getFirstChild(); return x.getBaseURI() + "|" + r.getBaseURI() + "|" + c.getBaseURI() + "|" + c.getFirstChild().getBaseURI() + "|" + ((Element) r.getChildNodes().item(1)).getBaseURI() + "|" + ((Element) r.getLastChild()).getBaseURI() + "|" + c.getAttributeNode("xml:base").getBaseURI(); });
    // user data
    p("userData", () -> { Element x = d.createElement("x"); Object o1 = x.setUserData("k", "v1", null); Object o2 = x.setUserData("k", "v2", null); Object g = x.getUserData("k"); Object removed = x.setUserData("k", null, null); return o1 + "|" + o2 + "|" + g + "|" + removed + "|" + x.getUserData("k") + "|" + x.getUserData("none"); });
    p("userData handlers", () -> { final StringBuilder log = new StringBuilder(); UserDataHandler h = (op, key, data, src, dst) -> log.append(op).append(':').append(key).append(':').append(data).append(':').append(src == null ? null : src.getNodeName()).append(':').append(dst == null ? null : dst.getNodeName()).append(';'); Element x = d.createElement("x"); x.setUserData("k", "val", h); x.cloneNode(true); Document other = newDoc(); other.importNode(x, true); other.adoptNode(x); x.setUserData("k2", "val2", h); Element y = d.createElement("y"); y.setUserData("k", "v", h); d.renameNode(y, null, "z"); return log; });
    // import / adopt / rename
    p("importNode", () -> { Document other = newDoc(); Node imp = other.importNode(cr, true); return dump(imp).replace("\n", "/") + "|" + (imp.getOwnerDocument() == other) + "|" + imp.getParentNode(); });
    p("importNode shallow", () -> { Document other = newDoc(); Node imp = other.importNode(cr, false); return dump(imp).replace("\n", "/"); });
    p("importNode attr", () -> { Document other = newDoc(); Attr a = (Attr) other.importNode(cr.getAttributeNode("a"), false); return a + "|" + a.getOwnerElement() + "|" + a.getOwnerDocument().equals(other) + "|" + a.getValue(); });
    p("importNode doc", () -> newDoc().importNode(cd, true));
    p("importNode doctype", () -> newDoc().importNode(cd.getDoctype(), true));
    p("importNode types", () -> { Document other = newDoc(); StringBuilder sb = new StringBuilder(); for (Node x : new Node[]{d.createTextNode("t"), d.createComment("c"), d.createCDATASection("cd"), d.createProcessingInstruction("p", "d"), d.createEntityReference("er"), d.createDocumentFragment()}) sb.append(other.importNode(x, true)).append(';'); return sb; });
    p("adoptNode", () -> { Document other = newDoc(); Element x = d.createElement("adopt"); x.setAttribute("a", "1"); x.appendChild(d.createTextNode("t")); Node r = other.adoptNode(x); return (r == x) + "|" + (x.getOwnerDocument() == other) + "|" + (x.getFirstChild().getOwnerDocument() == other) + "|" + (x.getAttributeNode("a").getOwnerDocument() == other); });
    p("adoptNode attached", () -> { Document other = newDoc(); Element par = d.createElement("par"); Element x = d.createElement("x"); par.appendChild(x); other.adoptNode(x); return x.getParentNode() + "|" + par.hasChildNodes(); });
    p("adoptNode attr", () -> { Document other = newDoc(); Element par = d.createElement("par"); par.setAttribute("a", "1"); Attr at2 = par.getAttributeNode("a"); other.adoptNode(at2); return at2.getOwnerElement() + "|" + par.hasAttributes() + "|" + (at2.getOwnerDocument() == other); });
    p("adoptNode doc", () -> newDoc().adoptNode(cd));
    p("adoptNode doctype", () -> newDoc().adoptNode(cd.getDoctype()));
    p("adoptNode same doc", () -> { Element par = d.createElement("par"); Element x = d.createElement("x"); par.appendChild(x); Node r = d.adoptNode(x); return (r == x) + "|" + x.getParentNode(); });
    p("renameNode element", () -> { Element x = d.createElement("old"); x.setAttribute("a", "1"); x.appendChild(d.createTextNode("t")); Element par = d.createElement("par"); par.appendChild(x); Node r = d.renameNode(x, null, "newname"); return r + "|" + (r == x) + "|" + r.getParentNode() + "|" + ((Element) r).getAttribute("a") + "|" + r.getFirstChild() + "|" + r.getLocalName(); });
    p("renameNode element ns", () -> { Element x = d.createElement("old"); x.setAttribute("a", "1"); x.appendChild(d.createTextNode("t")); Element par = d.createElement("par"); par.appendChild(x); Node r = d.renameNode(x, "http://n", "p:newname"); return r + "|" + (r == x) + "|" + r.getParentNode() + "|" + ((Element) r).getAttribute("a") + "|" + r.getFirstChild() + "|" + r.getLocalName() + "|" + r.getNamespaceURI() + "|" + r.getPrefix() + "|" + par.getFirstChild(); });
    p("renameNode element nsToNs", () -> { Element x = d.createElementNS("http://o", "o:old"); Node r = d.renameNode(x, "http://n", "p:newname"); return r + "|" + (r == x) + "|" + r.getNamespaceURI() + "|" + r.getLocalName(); });
    p("renameNode attr", () -> { Element par = d.createElement("par"); par.setAttribute("a", "1"); Attr a = par.getAttributeNode("a"); Node r = d.renameNode(a, null, "b"); return r + "|" + (r == a) + "|" + par.getAttribute("b") + "|" + par.hasAttribute("a") + "|" + ((Attr) r).getOwnerElement().getNodeName(); });
    p("renameNode attr ns", () -> { Element par = d.createElement("par"); par.setAttribute("a", "1"); Attr a = par.getAttributeNode("a"); Node r = d.renameNode(a, "http://n", "n:b"); return r + "|" + (r == a) + "|" + par.getAttributeNS("http://n", "b") + "|" + par.hasAttribute("a") + "|" + r.getLocalName(); });
    p("renameNode errors", () -> { StringBuilder sb = new StringBuilder(); Element x = d.createElement("x"); String[][] cases = {{null, "1a"}, {null, ""}, {"http://u", "nocolon"}, {null, "a:b"}, {"http://u", "a:b:c"}}; for (String[] c : cases) { try { d.renameNode(x, c[0], c[1]); sb.append("ok:").append(x.getNodeName()).append(';'); } catch (DOMException z) { sb.append(z.code).append(';'); } } try { d.renameNode(d.createTextNode("t"), null, "x"); } catch (DOMException z) { sb.append(z.code).append(';'); } try { d.renameNode(newDoc().createElement("o"), null, "x"); } catch (DOMException z) { sb.append(z.code).append(';'); } return sb; });
    // config
    p("domConfig names", () -> { DOMConfiguration c = d.getDomConfig(); DOMStringList l = c.getParameterNames(); StringBuilder sb = new StringBuilder(); for (int i = 0; i < l.getLength(); i++) sb.append(l.item(i)).append(','); return sb.append(l.contains("comments")).append(l.contains("nope")); });
    p("domConfig get", () -> { DOMConfiguration c = d.getDomConfig(); StringBuilder sb = new StringBuilder(); for (String n : new String[]{"comments", "datatype-normalization", "cdata-sections", "entities", "split-cdata-sections", "namespaces", "validate", "infoset", "normalize-characters", "canonical-form", "validate-if-schema", "check-character-normalization", "well-formed", "namespace-declarations", "element-content-whitespace", "error-handler", "schema-type", "schema-location", "resource-resolver", "COMMENTS"}) sb.append(n).append('=').append(c.getParameter(n)).append(';'); return sb; });
    p("domConfig get unknown", () -> d.getDomConfig().getParameter("nope"));
    p("domConfig set unknown", () -> { d.getDomConfig().setParameter("nope", true); return "no"; });
    p("domConfig set unsupported", () -> { d.getDomConfig().setParameter("canonical-form", true); return "no"; });
    p("domConfig set mismatch", () -> { d.getDomConfig().setParameter("comments", "x"); return "no"; });
    p("domConfig set null", () -> { d.getDomConfig().setParameter("comments", null); return "no"; });
    p("domConfig handler mismatch", () -> { d.getDomConfig().setParameter("error-handler", "x"); return "no"; });
    p("domConfig canSet", () -> { DOMConfiguration c = d.getDomConfig(); return c.canSetParameter("comments", true) + "," + c.canSetParameter("comments", false) + "," + c.canSetParameter("canonical-form", true) + "," + c.canSetParameter("canonical-form", false) + "," + c.canSetParameter("nope", true) + "," + c.canSetParameter("comments", "x") + "," + c.canSetParameter("error-handler", null) + "," + c.canSetParameter("schema-type", "http://www.w3.org/2001/XMLSchema") + "," + c.canSetParameter("validate", true) + "," + c.canSetParameter("normalize-characters", true) + "," + c.canSetParameter("infoset", true); });
    p("domConfig infoset", () -> { DOMConfiguration c = newDoc().getDomConfig(); c.setParameter("infoset", true); return c.getParameter("infoset") + "|" + c.getParameter("cdata-sections") + "|" + c.getParameter("entities") + "|" + c.getParameter("namespace-declarations") + "|" + c.getParameter("comments") + "|" + c.getParameter("element-content-whitespace"); });
    p("normalizeDocument", () -> { Document x = newDoc(); Element r = x.createElement("r"); x.appendChild(r); r.appendChild(x.createTextNode("a")); r.appendChild(x.createTextNode("b")); r.appendChild(x.createCDATASection("c")); r.appendChild(x.createComment("cm")); r.appendChild(x.createTextNode("")); x.normalizeDocument(); return dump(r).replace("\n", "/"); });
    p("normalizeDocument nocdata nocomments", () -> { Document x = newDoc(); Element r = x.createElement("r"); x.appendChild(r); r.appendChild(x.createTextNode("a")); r.appendChild(x.createCDATASection("c")); r.appendChild(x.createComment("cm")); r.appendChild(x.createTextNode("z")); x.getDomConfig().setParameter("cdata-sections", false); x.getDomConfig().setParameter("comments", false); x.normalizeDocument(); return dump(r).replace("\n", "/"); });
    p("normalizeDocument namespaces", () -> { Document x = newDoc(); Element r = x.createElementNS("http://a", "a:r"); x.appendChild(r); Element c = x.createElementNS("http://b", "c"); r.appendChild(c); c.setAttributeNS("http://c", "c:x", "1"); Element e2 = x.createElementNS("http://c", "x"); r.appendChild(e2); x.normalizeDocument(); return dump(r).replace("\n", "/"); });
    p("normalizeDocument nsDecl", () -> { Document x = newDoc(); Element r = x.createElementNS("http://a", "a:r"); x.appendChild(r); x.getDomConfig().setParameter("namespace-declarations", false); x.normalizeDocument(); return dump(r).replace("\n", "/"); });
    // readonly / nodes from parse
    p("entity readonly", () -> { Document x = parse("<!DOCTYPE r [<!ENTITY e 'v'><!NOTATION n SYSTEM 's'>]><r/>", false); Entity en = (Entity) x.getDoctype().getEntities().item(0); StringBuilder sb = new StringBuilder(); try { en.setNodeValue("x"); sb.append("ok;"); } catch (DOMException z) { sb.append(z.code).append(';'); } try { en.appendChild(x.createTextNode("t")); } catch (DOMException z) { sb.append(z.code).append(';'); } try { x.getDoctype().getEntities().removeNamedItem("e"); } catch (DOMException z) { sb.append(z.code).append(';'); } try { x.getDoctype().getEntities().setNamedItem(en); } catch (DOMException z) { sb.append(z.code).append(';'); } try { x.getDoctype().getNotations().removeNamedItem("n"); } catch (DOMException z) { sb.append(z.code).append(';'); } return sb; });
    p("doctype ops", () -> { Document x = parse("<!DOCTYPE r [<!ENTITY e 'v'>]><r/>", false); DocumentType dt = x.getDoctype(); StringBuilder sb = new StringBuilder(); try { dt.appendChild(x.createElement("a")); } catch (DOMException z) { sb.append(z.code).append(';'); } try { dt.setNodeValue("x"); sb.append("ok;"); } catch (DOMException z) { sb.append(z.code).append(';'); } try { dt.setPrefix("p"); sb.append("ok;"); } catch (DOMException z) { sb.append(z.code).append(';'); } sb.append(dt.getNodeValue()).append(dt.getParentNode() == x).append(dt.getTextContent()); Node cl = dt.cloneNode(true); sb.append(cl.getOwnerDocument() == x); return sb; });
    p("serialization of node toString", () -> { Document x = parse("<!DOCTYPE r [<!ENTITY e 'v'>]><r a='1'><!--c--><?p d?>t</r>", false); return x + "|" + x.getDoctype() + "|" + x.getDocumentElement() + "|" + x.getDocumentElement().getAttributeNode("a") + "|" + x.getDocumentElement().getChildNodes().item(0) + "|" + x.getDocumentElement().getChildNodes().item(1) + "|" + x.getDocumentElement().getChildNodes().item(2) + "|" + x.getDoctype().getEntities().item(0) + "|" + x.getDocumentElement().getAttributes().getLength(); });
    p("isSupported", () -> cr.isSupported("Core", "2.0") + "|" + cr.isSupported("XML", "3.0") + "|" + cr.isSupported("Foo", "1.0") + "|" + cr.isSupported("Events", null) + "|" + cr.getFeature("Core", "3.0") + "|" + cr.getFeature("Foo", "1.0"));
    p("hasAttributes types", () -> d.createTextNode("t").hasAttributes() + "|" + d.createElement("a").hasAttributes() + "|" + d.hasAttributes() + "|" + d.createAttribute("a").hasAttributes() + "|" + d.createTextNode("t").getAttributes());
    p("node props texts", () -> { Node[] arr = {d, d.createElement("a"), d.createAttribute("b"), d.createTextNode("t"), d.createCDATASection("c"), d.createComment("m"), d.createProcessingInstruction("p", "d"), d.createDocumentFragment(), d.createEntityReference("e")}; StringBuilder sb = new StringBuilder(); for (Node x : arr) sb.append(x.getNodeType()).append(':').append(x.getNodeName()).append(':').append(x.getNodeValue()).append(':').append(x.getTextContent()).append(':').append(x.getBaseURI()).append(':').append(x.getNamespaceURI()).append(':').append(x.getLocalName()).append(':').append(x.getPrefix()).append(';'); return sb; });
    p("document props", () -> { Document x = newDoc(); x.setXmlStandalone(true); x.setXmlVersion("1.1"); x.setDocumentURI("http://doc/uri"); String s1 = x.getXmlStandalone() + "|" + x.getXmlVersion() + "|" + x.getDocumentURI() + "|" + x.getBaseURI(); try { x.setXmlVersion("2.0"); } catch (DOMException z) { s1 += "|" + z.code; } return s1; });
    p("document text", () -> { Document x = newDoc(); x.setTextContent("zz"); x.setNodeValue("zz"); return x.getTextContent() + "|" + x.getNodeValue() + "|" + x.getOwnerDocument() + "|" + x.getParentNode() + "|" + x.getAttributes() + "|" + x.getNodeName(); });
    p("document replace child text", () -> { Document x = newDoc(); Element r = x.createElement("r"); x.appendChild(r); x.appendChild(x.createComment("c")); return nodes(x.getChildNodes()) + x.getLastChild() + x.getFirstChild() + x.getChildNodes().item(5); });
  }
}
