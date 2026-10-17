/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 * Minimum release: 21
 */

import java.io.*;
import java.util.*;
import javax.xml.parsers.*;
import org.w3c.dom.*;
import org.w3c.dom.ls.*;
import org.w3c.dom.traversal.NodeFilter;
import org.xml.sax.*;

public class LsModern {
static class L1 {
  static String fail(Throwable t) { return "!" + t.getClass().getName() + ":" + String.valueOf(t.getMessage()).replace("\n", "\\n"); }
  static String q(String s) { return s == null ? "null" : "'" + s.replace("\n", "\\n").replace("\t", "\\t").replace("\r", "\\r") + "'"; }
  static Document parse(String xml, boolean ns) throws Exception { DocumentBuilderFactory f = DocumentBuilderFactory.newInstance(); f.setNamespaceAware(ns); f.setExpandEntityReferences(false); return f.newDocumentBuilder().parse(new InputSource(new StringReader(xml))); }
  public static void run() throws Exception {
    Locale.setDefault(Locale.ENGLISH);
    DOMImplementationLS ls = (DOMImplementationLS) DocumentBuilderFactory.newInstance().newDocumentBuilder().getDOMImplementation();
    String[] docs = {"<a/>", "<a>text</a>", "<?xml version='1.0' encoding='ISO-8859-1' standalone='yes'?><a x='1' y=\"&lt;&amp;&gt;&quot;'\">t&lt;&amp;&gt;\"'<b/>x<![CDATA[c<>&]]><!--cm--><?pi d?></a>", "<a xmlns='d' xmlns:p='u'><p:b p:c='1' e='2'/><c xmlns=''/></a>", "<!DOCTYPE a [<!ENTITY e 'v'><!ELEMENT a ANY>]><a>&e;</a>", "<a>  <b>  <c/>  </b>  </a>", "<a>\n  <b>t</b>\n  <c>u<d/></c>\n</a>", "<a>\u00e9\u4e2d\ud83d\ude00</a>", "<!--top--><a/><?pi x?>", "<a><b/><b/></a>", "<a a='1' b='2' c='3'/>", "<a>line1\nline2\r\nline3</a>", "<a x='a\nb\tc'/>"};
    for (int di = 0; di < docs.length; di++) {
      System.out.println("== " + q(docs[di]));
      for (int mode = 0; mode < 4; mode++) {
        try {
          Document d = parse(docs[di], true);
          LSSerializer s = ls.createLSSerializer();
          if (mode == 1) s.getDomConfig().setParameter("format-pretty-print", true);
          if (mode == 2) s.getDomConfig().setParameter("xml-declaration", false);
          if (mode == 3) { s.getDomConfig().setParameter("format-pretty-print", true); s.getDomConfig().setParameter("xml-declaration", false); }
          System.out.println("  doc mode" + mode + " " + q(s.writeToString(d)));
          if (mode == 0) { System.out.println("  elem " + q(s.writeToString(d.getDocumentElement()))); if (d.getDocumentElement().getFirstChild() != null) System.out.println("  child " + q(s.writeToString(d.getDocumentElement().getFirstChild()))); }
        } catch (Throwable t) { System.out.println("  mode" + mode + " " + fail(t)); }
      }
    }
    LSSerializer s = ls.createLSSerializer();
    DOMConfiguration c = s.getDomConfig();
    String[] params = {"canonical-form", "discard-default-content", "format-pretty-print", "ignore-unknown-character-denormalizations", "namespaces", "split-cdata-sections", "validate", "validate-if-schema", "well-formed", "xml-declaration", "cdata-sections", "comments", "entities", "infoset", "element-content-whitespace", "error-handler", "datatype-normalization", "normalize-characters", "check-character-normalization", "charset-overrides-xml-encoding", "disallow-doctype", "supported-media-types-only", "nope", "http://www.oracle.com/xml/jaxp/properties/isStandalone"};
    for (String p : params) { StringBuilder sb = new StringBuilder("param " + p + ": "); try { sb.append("get=" + c.getParameter(p)); } catch (Throwable t) { sb.append("get" + fail(t)); } try { sb.append(" canTrue=" + c.canSetParameter(p, true) + " canFalse=" + c.canSetParameter(p, false)); } catch (Throwable t) { sb.append(" can" + fail(t)); } System.out.println(sb); }
    System.out.println("names " + new TreeSet<String>(toList(c.getParameterNames())));
    try { c.setParameter("canonical-form", true); } catch (Throwable t) { System.out.println("set canonical " + fail(t)); }
    try { c.setParameter("nope", true); } catch (Throwable t) { System.out.println("set nope " + fail(t)); }
    try { c.setParameter("xml-declaration", "x"); } catch (Throwable t) { System.out.println("set xmldecl str " + fail(t)); }
    try { c.setParameter("format-pretty-print", null); System.out.println("pretty null -> " + c.getParameter("format-pretty-print")); } catch (Throwable t) { System.out.println("set pretty null " + fail(t)); }
    try { c.setParameter(null, true); } catch (Throwable t) { System.out.println("set null name " + fail(t)); }
    System.out.println("newline default " + q(s.getNewLine()) + " filter " + s.getFilter());
    s.setNewLine("\r\n"); System.out.println("newline " + q(s.getNewLine()));
    Document d = parse("<a><b>t</b><c/></a>", true);
    s.getDomConfig().setParameter("format-pretty-print", true); s.setNewLine("\r\n");
    System.out.println("pretty crlf " + q(s.writeToString(d)));
    s.setNewLine("\n"); s.setNewLine(null); System.out.println("newline null " + q(s.getNewLine()) + " " + q(s.writeToString(d)));
    LSOutput out = ls.createLSOutput(); System.out.println("output defaults " + out.getEncoding() + " " + out.getSystemId() + " " + out.getByteStream() + " " + out.getCharacterStream());
    ByteArrayOutputStream bos;
    bos = new ByteArrayOutputStream(); out = ls.createLSOutput(); out.setByteStream(bos); out.setEncoding("ISO-8859-1"); Document d2 = parse("<a>\u00e9\u4e2d</a>", true); LSSerializer s2 = ls.createLSSerializer(); System.out.println("write latin1 " + s2.write(d2, out) + " " + q(bos.toString("ISO-8859-1")));
    bos = new ByteArrayOutputStream(); out = ls.createLSOutput(); out.setByteStream(bos); out.setEncoding("UTF-16"); s2 = ls.createLSSerializer(); System.out.println("write utf16 " + s2.write(d2, out) + " " + Arrays.toString(bos.toByteArray()));
    StringWriter sw = new StringWriter(); out = ls.createLSOutput(); out.setCharacterStream(sw); out.setEncoding("UTF-8"); s2 = ls.createLSSerializer(); System.out.println("write chars " + s2.write(d2, out) + " " + q(sw.toString()));
    s2 = ls.createLSSerializer(); out = ls.createLSOutput(); try { System.out.println("write nothing " + s2.write(d2, out)); } catch (Throwable t) { System.out.println("write nothing " + fail(t)); }
    try { s2.write(null, ls.createLSOutput()); } catch (Throwable t) { System.out.println("write null node " + fail(t)); }
    try { s2.writeToString(null); } catch (Throwable t) { System.out.println("writeToString null " + fail(t)); }
    File tmp = File.createTempFile("lsx", ".xml"); tmp.deleteOnExit();
    try { System.out.println("writeToURI " + s2.writeToURI(d2, tmp.toURI().toString()) + " " + q(new String(java.nio.file.Files.readAllBytes(tmp.toPath()), "UTF-8"))); } catch (Throwable t) { System.out.println("writeToURI " + fail(t)); }
    LSSerializer sf = ls.createLSSerializer();
    sf.setFilter(new LSSerializerFilter() { public short acceptNode(Node n) { return n.getNodeType() == Node.COMMENT_NODE || (n.getNodeType() == Node.ELEMENT_NODE && n.getNodeName().equals("skip")) ? FILTER_REJECT : FILTER_ACCEPT; } public int getWhatToShow() { return NodeFilter.SHOW_ALL; } });
    System.out.println("filter " + q(sf.writeToString(parse("<a><!--c--><skip><x/></skip><b/></a>", true))));
    sf.setFilter(new LSSerializerFilter() { public short acceptNode(Node n) { return n.getNodeName().equals("skip") ? FILTER_SKIP : FILTER_ACCEPT; } public int getWhatToShow() { return NodeFilter.SHOW_ELEMENT; } });
    System.out.println("filter skip " + q(sf.writeToString(parse("<a><skip><x/></skip><b/></a>", true))));
    Document nd = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument(); Element r = nd.createElementNS("u", "p:r"); nd.appendChild(r); r.setAttributeNS("v", "q:x", "1"); r.appendChild(nd.createElementNS(null, "c")); r.appendChild(nd.createElementNS("u", "d")); r.appendChild(nd.createElement("l1"));
    LSSerializer sn = ls.createLSSerializer(); System.out.println("nsfixup " + q(sn.writeToString(nd)));
    sn.getDomConfig().setParameter("namespaces", false); System.out.println("nsfixup off " + q(sn.writeToString(nd)));
    Document od = parse("<a x='1'/>", true);
    Attr at = od.getDocumentElement().getAttributeNode("x"); LSSerializer so = ls.createLSSerializer();
    for (Node n : new Node[]{at, od.createTextNode("t<&>"), od.createComment("c"), od.createCDATASection("x"), od.createProcessingInstruction("t", "d"), od.createDocumentFragment(), od.createEntityReference("e"), od.getDoctype() == null ? od.createTextNode("nodoctype") : od.getDoctype()}) { try { System.out.println("node " + n.getNodeType() + " " + q(so.writeToString(n))); } catch (Throwable t) { System.out.println("node " + n.getNodeType() + " " + fail(t)); } }
    DocumentFragment fr = od.createDocumentFragment(); fr.appendChild(od.createElement("f1")); fr.appendChild(od.createTextNode("t")); fr.appendChild(od.createElement("f2")); System.out.println("fragment " + q(so.writeToString(fr)));
    Document bd = parse("<a/>", true); bd.getDocumentElement().appendChild(bd.createCDATASection("a]]>b")); System.out.println("cdata split " + q(so.writeToString(bd)));
    LSSerializer sc = ls.createLSSerializer(); sc.getDomConfig().setParameter("split-cdata-sections", false); try { System.out.println("cdata nosplit " + q(sc.writeToString(bd))); } catch (Throwable t) { System.out.println("cdata nosplit " + fail(t)); }
    Document cd = parse("<a/>", true); cd.getDocumentElement().appendChild(cd.createComment("a--b")); System.out.println("comment dashes " + q(so.writeToString(cd)));
    Document vd = parse("<a/>", true); vd.setXmlVersion("1.1"); System.out.println("xml11 " + q(so.writeToString(vd))); vd.setXmlStandalone(true); System.out.println("standalone " + q(so.writeToString(vd))); vd.setXmlVersion("1.0"); vd.setXmlStandalone(false);
    Document ed = parse("<a/>", true); ed.setXmlStandalone(true); System.out.println("standalone only " + q(so.writeToString(ed)));
    System.out.println("MODE consts " + DOMImplementationLS.MODE_SYNCHRONOUS + " " + DOMImplementationLS.MODE_ASYNCHRONOUS);
    try { ls.createLSParser(DOMImplementationLS.MODE_ASYNCHRONOUS, null); } catch (Throwable t) { System.out.println("async " + fail(t)); }
    try { ls.createLSParser((short) 5, null); } catch (Throwable t) { System.out.println("mode5 " + fail(t)); }
    try { ls.createLSParser(DOMImplementationLS.MODE_SYNCHRONOUS, "http://www.w3.org/TR/REC-xml"); System.out.println("dtd schema ok"); } catch (Throwable t) { System.out.println("dtd schema " + fail(t)); }
    try { ls.createLSParser(DOMImplementationLS.MODE_SYNCHRONOUS, "http://x"); } catch (Throwable t) { System.out.println("bad schema " + fail(t)); }
  }
  static List<String> toList(DOMStringList l) { List<String> r = new ArrayList<String>(); for (int i = 0; i < l.getLength(); i++) r.add(l.item(i)); return r; }
}

static class L2 {
  static String fail(Throwable t) { return "!" + t.getClass().getName() + ":" + String.valueOf(t.getMessage()).replace("\n", "\\n"); }
  static String q(String s) { return s == null ? "null" : "'" + s.replace("\n", "\\n").replace("\t", "\\t").replace("\r", "\\r") + "'"; }
  static String dump(Node n) {
    StringBuilder sb = new StringBuilder();
    switch (n.getNodeType()) {
      case Node.DOCUMENT_NODE: Document d = (Document) n; sb.append("#doc(v=" + d.getXmlVersion() + ",enc=" + d.getXmlEncoding() + ",sa=" + d.getXmlStandalone() + ",uri=" + d.getDocumentURI() + ",input=" + d.getInputEncoding() + ",strict=" + d.getStrictErrorChecking() + ")["); break;
      case Node.ELEMENT_NODE: sb.append("<" + n.getNodeName() + "|ns=" + n.getNamespaceURI() + "|ln=" + n.getLocalName()); NamedNodeMap m = n.getAttributes(); for (int i = 0; i < m.getLength(); i++) sb.append(" @" + m.item(i).getNodeName() + "=" + m.item(i).getNodeValue() + "|" + m.item(i).getNamespaceURI() + "|" + ((Attr) m.item(i)).getSpecified()); sb.append(">["); break;
      case Node.TEXT_NODE: sb.append("T" + q(n.getNodeValue())); break;
      case Node.CDATA_SECTION_NODE: sb.append("CD" + q(n.getNodeValue())); break;
      case Node.COMMENT_NODE: sb.append("C" + q(n.getNodeValue())); break;
      case Node.PROCESSING_INSTRUCTION_NODE: sb.append("PI" + n.getNodeName() + q(n.getNodeValue())); break;
      case Node.ENTITY_REFERENCE_NODE: sb.append("ER" + n.getNodeName() + "["); break;
      case Node.DOCUMENT_TYPE_NODE: sb.append("DT" + n.getNodeName() + "[" + ((DocumentType) n).getInternalSubset() + "]"); break;
      default: sb.append("?" + n.getNodeType());
    }
    for (Node c = n.getFirstChild(); c != null; c = c.getNextSibling()) sb.append(dump(c)).append(' ');
    if (n.getNodeType() == Node.DOCUMENT_NODE || n.getNodeType() == Node.ELEMENT_NODE || n.getNodeType() == Node.ENTITY_REFERENCE_NODE) sb.append("]");
    return sb.toString();
  }
  public static void run() throws Exception {
    Locale.setDefault(Locale.ENGLISH);
    DOMImplementationLS ls = (DOMImplementationLS) DocumentBuilderFactory.newInstance().newDocumentBuilder().getDOMImplementation();
    LSParser p = ls.createLSParser(DOMImplementationLS.MODE_SYNCHRONOUS, null);
    DOMConfiguration c = p.getDomConfig();
    String[] params = {"namespaces", "cdata-sections", "comments", "element-content-whitespace", "entities", "validate", "error-handler", "resource-resolver", "charset-overrides-xml-encoding", "disallow-doctype", "supported-media-types-only", "infoset", "well-formed", "datatype-normalization", "validate-if-schema", "namespace-declarations", "schema-location", "schema-type", "canonical-form", "normalize-characters", "check-character-normalization", "ignore-unknown-character-denormalizations", "split-cdata-sections", "format-pretty-print", "xml-declaration", "http://apache.org/xml/features/disallow-doctype-decl", "nope"};
    for (String x : params) { StringBuilder sb = new StringBuilder("param " + x + ": "); try { sb.append("get=" + c.getParameter(x)); } catch (Throwable t) { sb.append("get" + fail(t)); } try { sb.append(" canTrue=" + c.canSetParameter(x, true) + " canFalse=" + c.canSetParameter(x, false)); } catch (Throwable t) { sb.append(" can" + fail(t)); } System.out.println(sb); }
    List<String> names = new ArrayList<String>(); for (int i = 0; i < c.getParameterNames().getLength(); i++) names.add(c.getParameterNames().item(i)); Collections.sort(names); System.out.println("names " + names);
    System.out.println("async=" + p.getAsync() + " busy=" + p.getBusy() + " filter=" + p.getFilter());
    String[] docs = {"<a/>", "<?xml version='1.0' encoding='ISO-8859-1' standalone='yes'?><a x='1'>t<![CDATA[c]]><!--cm--><?pi d?>&amp;&#65;</a>", "<a xmlns='d' xmlns:p='u'><p:b p:c='1' e='2'/></a>", "<!DOCTYPE a [<!ENTITY e 'v'><!ATTLIST a x CDATA 'dflt'>]><a>&e;</a>", "<a>  <b/>  </a>", "<a><b></a>", "", "<a x=1/>", "<?xml version='1.1'?><a/>", "<a>\u00e9</a>"};
    for (String doc : docs) {
      System.out.println("== " + q(doc));
      for (int mode = 0; mode < 5; mode++) {
        LSParser pp = ls.createLSParser(DOMImplementationLS.MODE_SYNCHRONOUS, null);
        DOMConfiguration cc = pp.getDomConfig();
        if (mode == 1) { cc.setParameter("cdata-sections", false); cc.setParameter("comments", false); cc.setParameter("entities", false); }
        if (mode == 2) cc.setParameter("namespaces", false);
        if (mode == 3) cc.setParameter("element-content-whitespace", false);
        if (mode == 4) cc.setParameter("entities", false);
        LSInput in = ls.createLSInput(); in.setStringData(doc);
        try { Document d = pp.parse(in); System.out.println("  mode" + mode + " " + dump(d)); } catch (Throwable t) { System.out.println("  mode" + mode + " " + fail(t)); }
        if (mode >= 2) continue;
      }
    }
    LSParser pe = ls.createLSParser(DOMImplementationLS.MODE_SYNCHRONOUS, null);
    final List<String> errs = new ArrayList<String>();
    pe.getDomConfig().setParameter("error-handler", new DOMErrorHandler() { public boolean handleError(DOMError e) { errs.add(e.getSeverity() + ":" + e.getMessage() + ":" + e.getLocation().getLineNumber() + ":" + e.getLocation().getColumnNumber()); return true; } });
    LSInput in = ls.createLSInput(); in.setStringData("<a><b></a>"); try { System.out.println("err doc " + pe.parse(in)); } catch (Throwable t) { System.out.println("err " + fail(t)); } System.out.println("errs " + errs);
    errs.clear(); in = ls.createLSInput(); in.setStringData("<a>\n<b x='1' x='2'/></a>"); try { System.out.println("err doc " + pe.parse(in)); } catch (Throwable t) { System.out.println("err " + fail(t)); } System.out.println("errs " + errs);
    pe.getDomConfig().setParameter("error-handler", new DOMErrorHandler() { public boolean handleError(DOMError e) { return false; } });
    in = ls.createLSInput(); in.setStringData("<a><b></a>"); try { System.out.println("err2 doc " + pe.parse(in)); } catch (Throwable t) { System.out.println("err2 " + fail(t)); }
    in = ls.createLSInput(); System.out.println("input defaults " + in.getStringData() + " " + in.getSystemId() + " " + in.getEncoding() + " " + in.getCertifiedText() + " " + in.getBaseURI() + " " + in.getByteStream() + " " + in.getCharacterStream());
    try { p.parse(in); } catch (Throwable t) { System.out.println("parse empty input " + fail(t)); }
    in = ls.createLSInput(); in.setByteStream(new ByteArrayInputStream("<?xml version='1.0' encoding='ISO-8859-1'?><a>\u00e9</a>".getBytes("ISO-8859-1"))); try { Document d = p.parse(in); System.out.println("bytes " + dump(d)); } catch (Throwable t) { System.out.println("bytes " + fail(t)); }
    in = ls.createLSInput(); in.setByteStream(new ByteArrayInputStream("<a>\u00e9</a>".getBytes("UTF-8"))); in.setEncoding("UTF-8"); try { Document d = p.parse(in); System.out.println("bytes enc " + dump(d)); } catch (Throwable t) { System.out.println("bytes enc " + fail(t)); }
    in = ls.createLSInput(); in.setCharacterStream(new StringReader("<a>x</a>")); in.setSystemId("sys:id"); try { Document d = p.parse(in); System.out.println("chars " + dump(d)); } catch (Throwable t) { System.out.println("chars " + fail(t)); }
    File tmp = File.createTempFile("lsx", ".xml"); tmp.deleteOnExit(); java.nio.file.Files.write(tmp.toPath(), "<a>file</a>".getBytes("UTF-8"));
    try { Document d = p.parseURI(tmp.toURI().toString()); System.out.println("parseURI " + dump(d).replace(tmp.toURI().toString(), "URI")); } catch (Throwable t) { System.out.println("parseURI " + fail(t)); }
    try { p.parseURI("file:/nonexistent/x.xml"); } catch (Throwable t) { System.out.println("parseURI missing " + fail(t)); }
    try { p.parseURI(null); } catch (Throwable t) { System.out.println("parseURI null " + fail(t)); }
    try { p.parse(null); } catch (Throwable t) { System.out.println("parse null " + fail(t)); }
    try { Document tgt = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument(); p.parseWithContext(in, tgt, LSParser.ACTION_APPEND_AS_CHILDREN); } catch (Throwable t) { System.out.println("parseWithContext " + fail(t)); }
    p.abort(); System.out.println("abort ok");
    System.out.println("consts " + LSParser.ACTION_APPEND_AS_CHILDREN + " " + LSParser.ACTION_REPLACE_CHILDREN + " " + LSParser.ACTION_INSERT_BEFORE + " " + LSParser.ACTION_INSERT_AFTER + " " + LSParser.ACTION_REPLACE);
    LSParser pf = ls.createLSParser(DOMImplementationLS.MODE_SYNCHRONOUS, null);
    pf.setFilter(new LSParserFilter() { public short startElement(Element e) { return e.getNodeName().equals("skip") ? FILTER_SKIP : e.getNodeName().equals("rej") ? FILTER_REJECT : FILTER_ACCEPT; } public short acceptNode(Node n) { return n.getNodeType() == Node.COMMENT_NODE ? FILTER_REJECT : FILTER_ACCEPT; } public int getWhatToShow() { return org.w3c.dom.traversal.NodeFilter.SHOW_ALL; } });
    in = ls.createLSInput(); in.setStringData("<a><!--c--><skip><x/></skip><rej><y/></rej><b/></a>"); try { System.out.println("filter " + dump(pf.parse(in))); } catch (Throwable t) { System.out.println("filter " + fail(t)); }
    LSParser pr = ls.createLSParser(DOMImplementationLS.MODE_SYNCHRONOUS, null);
    pr.getDomConfig().setParameter("resource-resolver", new LSResourceResolver() { public LSInput resolveResource(String type, String ns, String pub, String sys, String base) { System.out.println("resolve " + type + "|" + ns + "|" + pub + "|" + sys + "|" + (base == null ? null : "base")); LSInput li = ls.createLSInput(); li.setStringData("<!ENTITY e2 'from resolver'>"); return li; } });
    in = ls.createLSInput(); in.setStringData("<!DOCTYPE a SYSTEM 'x.dtd'><a>&e2;</a>"); try { System.out.println("resolver " + dump(pr.parse(in))); } catch (Throwable t) { System.out.println("resolver " + fail(t)); }
  } }

static class L6 { 
  static void t(String l, Runnable r) { try { r.run(); System.out.println(l + ": ok"); } catch (DOMException e) { System.out.println(l + ": DOMException code=" + e.code + " " + e.getMessage()); } catch (Throwable e) { System.out.println(l + ": " + e); } }
  public static void run() throws Exception {
  Locale.setDefault(Locale.ENGLISH);
  final DOMImplementationLS ls = (DOMImplementationLS) DocumentBuilderFactory.newInstance().newDocumentBuilder().getDOMImplementation();
  for (int k = 0; k < 2; k++) {
    final DOMConfiguration c = k == 0 ? ls.createLSParser((short) 1, null).getDomConfig() : ls.createLSSerializer().getDomConfig();
    System.out.println(k == 0 ? "== parser" : "== serializer");
    String[] names = {"nope", "xml-declaration", "namespaces", "format-pretty-print", "error-handler", "resource-resolver", "well-formed", "canonical-form", "schema-type", "normalize-characters", "supported-media-types-only", "disallow-doctype", "infoset"};
    for (final String n : names) {
      for (final Object v : new Object[]{Boolean.TRUE, Boolean.FALSE, "str", null}) {
        t("set " + n + "=" + v, new Runnable() { public void run() { c.setParameter(n, v); } });
      }
    }
    t("get nope", new Runnable() { public void run() { c.getParameter("nope"); } });
  }
} }

  public static void main(String[] args) throws Exception {
    L1.run();
    System.out.println("##### L2");
    L2.run();
    System.out.println("##### L6");
    L6.run();
  }
}
