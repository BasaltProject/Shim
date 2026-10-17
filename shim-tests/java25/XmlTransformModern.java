/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 * Minimum release: 21
 */

import java.io.*; import java.nio.file.*; import java.util.*;
import javax.xml.parsers.*; import javax.xml.transform.*; import javax.xml.transform.dom.*; import javax.xml.transform.sax.*; import javax.xml.transform.stream.*;
import org.w3c.dom.*; import org.xml.sax.*; import org.xml.sax.ext.*; import org.xml.sax.helpers.*;
public class XmlTransformModern {
  interface Body { Object get() throws Exception; }
  static void p(String n, Body b) {
    String o;
    try { Object r = b.get(); o = String.valueOf(r); }
    catch (Throwable t) { o = "!" + t.getClass().getName() + (t instanceof NullPointerException ? "" : ":" + t.getMessage()); }
    System.out.println(n + "=" + o);
  }
  static String esc(String s) { return s.replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t"); }
  static Document parse(String xml, boolean ns) throws Exception { DocumentBuilderFactory f = DocumentBuilderFactory.newInstance(); f.setNamespaceAware(ns); return f.newDocumentBuilder().parse(new InputSource(new StringReader(xml))); }
  static Document newDoc() throws Exception { return DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument(); }
  static String tr(Source src, String... props) throws Exception { Transformer t = TransformerFactory.newInstance().newTransformer(); for (int i = 0; i < props.length; i += 2) t.setOutputProperty(props[i], props[i + 1]); StringWriter w = new StringWriter(); t.transform(src, new StreamResult(w)); return esc(w.toString()); }
  static String trBytes(Source src, String enc, String... props) throws Exception { Transformer t = TransformerFactory.newInstance().newTransformer(); t.setOutputProperty(OutputKeys.ENCODING, enc); for (int i = 0; i < props.length; i += 2) t.setOutputProperty(props[i], props[i + 1]); ByteArrayOutputStream bo = new ByteArrayOutputStream(); t.transform(src, new StreamResult(bo)); StringBuilder sb = new StringBuilder(); sb.append(bo.size()).append(':'); byte[] b = bo.toByteArray(); for (int i = 0; i < Math.min(b.length, 120); i++) { int v = b[i] & 0xff; if (v >= 32 && v < 127) sb.append((char) v); else sb.append('<').append(Integer.toHexString(v)).append('>'); } return sb.toString(); }
  public static void main(String[] args) throws Exception {
    Locale.setDefault(Locale.ENGLISH);
    System.setErr(System.out);
    String[] docs = {
      "<a b=\"1\" a=\"2\"><c/><d>t &amp; &lt;x&gt; \"q\" 'a'</d><!--c--><?p d?><![CDATA[<cd>]]></a>",
      "<a xmlns=\"u\" xmlns:p=\"v\"><p:b p:c=\"1\"/>\u00e9\u4e2d\ud83d\ude00</a>",
      "<a>  <b>x</b>\n  <c> y </c>\n</a>",
      "<a b=\"x&#10;y&#9;z&#13;&quot;&amp;&lt;&gt;'\"/>",
      "<a><b/>text<c/></a>",
      "<a>t<b/>u</a>",
      "<a><b><![CDATA[x]]></b><c><!--c--></c><d><?p d?></d></a>",
      "<?xml version=\"1.0\" standalone=\"yes\"?><a/>",
      "<a>]]&gt; &#xA0;\u00a0\u00e9</a>",
      "<r xmlns:x=\"ux\" xmlns=\"d\"><x:a><b xmlns=\"\"><c/></b></x:a></r>",
    };
    for (int i = 0; i < docs.length; i++) {
      final String x = docs[i]; final int idx = i;
      for (boolean ns : new boolean[]{false, true}) {
        Document d = parse(x, ns);
        System.out.println("== doc" + idx + " ns=" + ns + " :: " + esc(x));
        p("doc", () -> tr(new DOMSource(d)));
        p("elem", () -> tr(new DOMSource(d.getDocumentElement())));
        p("child", () -> tr(new DOMSource(d.getDocumentElement().getFirstChild())));
        p("indent", () -> tr(new DOMSource(d), OutputKeys.INDENT, "yes"));
        p("indent omit", () -> tr(new DOMSource(d), OutputKeys.INDENT, "yes", OutputKeys.OMIT_XML_DECLARATION, "yes"));
        p("indent 2", () -> tr(new DOMSource(d), OutputKeys.INDENT, "yes", OutputKeys.OMIT_XML_DECLARATION, "yes", "{http://xml.apache.org/xslt}indent-amount", "2"));
        p("omit", () -> tr(new DOMSource(d), OutputKeys.OMIT_XML_DECLARATION, "yes"));
        p("standalone yes", () -> tr(new DOMSource(d), OutputKeys.STANDALONE, "yes"));
        p("version", () -> tr(new DOMSource(d), OutputKeys.VERSION, "1.1"));
        p("doctype", () -> tr(new DOMSource(d), OutputKeys.DOCTYPE_SYSTEM, "x.dtd"));
        p("doctype both", () -> tr(new DOMSource(d), OutputKeys.DOCTYPE_SYSTEM, "x.dtd", OutputKeys.DOCTYPE_PUBLIC, "-//X//Y", OutputKeys.INDENT, "yes"));
        p("doctype public only", () -> tr(new DOMSource(d), OutputKeys.DOCTYPE_PUBLIC, "-//X//Y"));
        p("cdata elements", () -> tr(new DOMSource(d), OutputKeys.CDATA_SECTION_ELEMENTS, "b d a"));
        p("text method", () -> tr(new DOMSource(d), OutputKeys.METHOD, "text"));
        p("latin1", () -> trBytes(new DOMSource(d), "ISO-8859-1"));
        p("ascii", () -> trBytes(new DOMSource(d), "US-ASCII"));
        p("utf16", () -> trBytes(new DOMSource(d), "UTF-16"));
        p("utf16le", () -> trBytes(new DOMSource(d), "UTF-16LE"));
        p("utf8", () -> trBytes(new DOMSource(d), "UTF-8"));
        p("unknown enc", () -> trBytes(new DOMSource(d), "NOPE-ENC"));
        p("stream src", () -> tr(new StreamSource(new StringReader(x))));
        p("stream indent", () -> tr(new StreamSource(new StringReader(x)), OutputKeys.INDENT, "yes"));
        p("stream standalone", () -> tr(new StreamSource(new StringReader(x)), OutputKeys.STANDALONE, "yes"));
        p("stream doctype", () -> tr(new StreamSource(new StringReader(x)), OutputKeys.DOCTYPE_SYSTEM, "x.dtd", OutputKeys.INDENT, "yes"));
        p("sax src", () -> { XMLReader r = ns ? XMLReaderFactory.createXMLReader() : SAXParserFactory.newInstance().newSAXParser().getXMLReader(); return tr(new SAXSource(r, new InputSource(new StringReader(x)))); });
      }
    }
    p("built ns", () -> { Document d = newDoc(); Element r = d.createElementNS("http://a", "a:root"); d.appendChild(r); Element c = d.createElementNS("http://b", "c"); r.appendChild(c); c.setAttributeNS("http://c", "c:x", "1"); c.setAttributeNS(null, "plain", "2"); Element e = d.createElementNS(null, "e"); r.appendChild(e); Element f = d.createElementNS("http://a", "f"); r.appendChild(f); return tr(new DOMSource(d)); });
    p("built ns declared", () -> { Document d = newDoc(); Element r = d.createElementNS("http://a", "a:root"); d.appendChild(r); r.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:a", "http://a"); r.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns", "http://dflt"); Element c = d.createElementNS("http://dflt", "c"); r.appendChild(c); Element e = d.createElementNS(null, "e"); r.appendChild(e); return tr(new DOMSource(d)); });
    p("built level1", () -> { Document d = newDoc(); Element r = d.createElement("root"); d.appendChild(r); r.setAttribute("z", "1"); r.setAttribute("a", "2"); r.appendChild(d.createTextNode("t")); r.appendChild(d.createElement("e")); r.appendChild(d.createEntityReference("amp")); r.appendChild(d.createCDATASection("cd")); r.appendChild(d.createProcessingInstruction("t", "")); r.appendChild(d.createComment("")); return tr(new DOMSource(d)); });
    p("built fragment", () -> { Document d = newDoc(); DocumentFragment f = d.createDocumentFragment(); f.appendChild(d.createElement("a")); f.appendChild(d.createElement("b")); return tr(new DOMSource(f)); });
    p("built text node", () -> { Document d = newDoc(); return tr(new DOMSource(d.createTextNode("a<b"))); });
    p("built comment node", () -> { Document d = newDoc(); return tr(new DOMSource(d.createComment("cm"))); });
    p("built attr node", () -> { Document d = newDoc(); Attr a = d.createAttribute("n"); a.setValue("v"); return tr(new DOMSource(a)); });
    p("doctype in dom", () -> { Document d = parse("<!DOCTYPE a [<!ENTITY e 'v'>]><a>&e;</a>", false); return tr(new DOMSource(d)); });
    p("entity ref dom", () -> { DocumentBuilderFactory f = DocumentBuilderFactory.newInstance(); f.setExpandEntityReferences(false); Document d = f.newDocumentBuilder().parse(new InputSource(new StringReader("<!DOCTYPE a [<!ENTITY e 'v'>]><a>x&e;y</a>"))); return tr(new DOMSource(d)); });
    p("dom result new", () -> { DOMResult r = new DOMResult(); TransformerFactory.newInstance().newTransformer().transform(new StreamSource(new StringReader("<a b='1'>t<c/></a>")), r); Node n = r.getNode(); return n + "|" + n.getFirstChild() + "|" + n.getFirstChild().getChildNodes().getLength() + "|" + ((Element) n.getFirstChild()).getAttribute("b") + "|" + r.getNextSibling() + "|" + r.getSystemId(); });
    p("dom result into", () -> { Document t = newDoc(); Element h = t.createElement("holder"); t.appendChild(h); h.appendChild(t.createComment("first")); DOMResult r = new DOMResult(h); TransformerFactory.newInstance().newTransformer().transform(new StreamSource(new StringReader("<a/>")), r); return h.getChildNodes().getLength() + "|" + h.getLastChild() + "|" + (r.getNode() == h); });
    p("dom result sibling", () -> { Document t = newDoc(); Element h = t.createElement("holder"); t.appendChild(h); Node first = h.appendChild(t.createComment("first")); Node second = h.appendChild(t.createComment("second")); DOMResult r = new DOMResult(h, second); TransformerFactory.newInstance().newTransformer().transform(new DOMSource(parse("<a/>", false)), r); return h.getChildNodes().getLength() + "|" + h.getChildNodes().item(1) + "|" + h.getChildNodes().item(2); });
    p("dom to dom", () -> { Document src = parse("<a xmlns='u' b='1'><!--c--><![CDATA[x]]>t</a>", true); DOMResult r = new DOMResult(); TransformerFactory.newInstance().newTransformer().transform(new DOMSource(src), r); Document out = (Document) r.getNode(); Element e = out.getDocumentElement(); return e.getNamespaceURI() + "|" + e.getLocalName() + "|" + e.getChildNodes().getLength() + "|" + e.getFirstChild() + "|" + e.getChildNodes().item(1) + "|" + src.isEqualNode(out); });
    p("sax result", () -> { final StringBuilder log = new StringBuilder(); DefaultHandler2 h = new DefaultHandler2() { public void startDocument() { log.append("SD;"); } public void endDocument() { log.append("ED;"); } public void startPrefixMapping(String p, String u) { log.append("SP(" + p + "=" + u + ");"); } public void endPrefixMapping(String p) { log.append("EP(" + p + ");"); } public void startElement(String u, String l, String q, Attributes a) { log.append("SE(" + u + "," + l + "," + q + "," + a.getLength() + ");"); } public void endElement(String u, String l, String q) { log.append("EE(" + q + ");"); } public void characters(char[] c, int s, int n) { log.append("C(" + new String(c, s, n) + ");"); } public void comment(char[] c, int s, int n) { log.append("CM(" + new String(c, s, n) + ");"); } public void startCDATA() { log.append("SC;"); } public void endCDATA() { log.append("EC;"); } public void processingInstruction(String t, String d) { log.append("PI(" + t + "," + d + ");"); } }; TransformerFactory.newInstance().newTransformer().transform(new StreamSource(new StringReader("<a xmlns='u' xmlns:p='v' p:x='1'><!--c--><![CDATA[x]]>t<?p d?></a>")), new SAXResult(h)); return log; });
    p("sax result dom src", () -> { final StringBuilder log = new StringBuilder(); DefaultHandler2 h = new DefaultHandler2() { public void startDocument() { log.append("SD;"); } public void endDocument() { log.append("ED;"); } public void startPrefixMapping(String p, String u) { log.append("SP(" + p + "=" + u + ");"); } public void endPrefixMapping(String p) { log.append("EP(" + p + ");"); } public void startElement(String u, String l, String q, Attributes a) { log.append("SE(" + u + "," + l + "," + q + "," + a.getLength() + ");"); } public void endElement(String u, String l, String q) { log.append("EE(" + q + ");"); } public void characters(char[] c, int s, int n) { log.append("C(" + new String(c, s, n) + ");"); } public void comment(char[] c, int s, int n) { log.append("CM(" + new String(c, s, n) + ");"); } public void startCDATA() { log.append("SC;"); } public void endCDATA() { log.append("EC;"); } }; TransformerFactory.newInstance().newTransformer().transform(new DOMSource(parse("<a xmlns='u' xmlns:p='v' p:x='1'><!--c--><![CDATA[x]]>t</a>", true)), new SAXResult(h)); return log; });
    p("file result", () -> { Path f = Files.createTempFile("tm", ".xml"); try { TransformerFactory.newInstance().newTransformer().transform(new StreamSource(new StringReader("<a>\u00e9</a>")), new StreamResult(f.toFile())); return esc(new String(Files.readAllBytes(f), "UTF-8")); } finally { Files.deleteIfExists(f); } });
    p("systemid result", () -> { Path f = Files.createTempFile("tm", ".xml"); try { TransformerFactory.newInstance().newTransformer().transform(new StreamSource(new StringReader("<a/>")), new StreamResult(f.toUri().toString())); return esc(new String(Files.readAllBytes(f), "UTF-8")); } finally { Files.deleteIfExists(f); } });
    p("file source", () -> { Path f = Files.createTempFile("tm", ".xml"); try { Files.write(f, "<a>file</a>".getBytes("UTF-8")); return tr(new StreamSource(f.toFile())); } finally { Files.deleteIfExists(f); } });
    p("bytes source", () -> tr(new StreamSource(new ByteArrayInputStream("<?xml version='1.0' encoding='ISO-8859-1'?><a>\u00e9</a>".getBytes("ISO-8859-1")))));
    p("empty result", () -> { TransformerFactory.newInstance().newTransformer().transform(new StreamSource(new StringReader("<a/>")), new StreamResult()); return "no"; });
    p("bad xml source", () -> { TransformerFactory.newInstance().newTransformer().transform(new StreamSource(new StringReader("<a>")), new StreamResult(new StringWriter())); return "no"; });
    p("bad xml cause", () -> { try { TransformerFactory.newInstance().newTransformer().transform(new StreamSource(new StringReader("<a>")), new StreamResult(new StringWriter())); return "no"; } catch (TransformerException e) { return e.getCause() == null ? "nocause" : e.getCause().getClass().getName(); } });
    p("bad result type", () -> { TransformerFactory.newInstance().newTransformer().transform(new StreamSource(new StringReader("<a/>")), new Result() { public void setSystemId(String s) { } public String getSystemId() { return null; } }); return "no"; });
    p("factory", () -> { TransformerFactory tf = TransformerFactory.newInstance(); return (tf instanceof SAXTransformerFactory) + "|" + tf.getFeature(DOMSource.FEATURE) + "|" + tf.getFeature(DOMResult.FEATURE) + "|" + tf.getFeature(SAXSource.FEATURE) + "|" + tf.getFeature(SAXResult.FEATURE) + "|" + tf.getFeature(StreamSource.FEATURE) + "|" + tf.getFeature(StreamResult.FEATURE) + "|" + tf.getFeature(SAXTransformerFactory.FEATURE) + "|" + tf.getFeature(SAXTransformerFactory.FEATURE_XMLFILTER) + "|" + tf.getFeature("nope") + "|" + tf.getFeature(javax.xml.XMLConstants.FEATURE_SECURE_PROCESSING) + "|" + tf.getURIResolver(); });
    p("factory setFeature", () -> { TransformerFactory tf = TransformerFactory.newInstance(); tf.setFeature(javax.xml.XMLConstants.FEATURE_SECURE_PROCESSING, true); return tf.getFeature(javax.xml.XMLConstants.FEATURE_SECURE_PROCESSING); });
    p("factory setFeature bad", () -> { TransformerFactory.newInstance().setFeature("nope", true); return "no"; });
    p("factory attribute", () -> { TransformerFactory tf = TransformerFactory.newInstance(); tf.setAttribute("indent-number", 3); Transformer t = tf.newTransformer(); t.setOutputProperty(OutputKeys.INDENT, "yes"); StringWriter w = new StringWriter(); t.transform(new DOMSource(parse("<a><b/></a>", false)), new StreamResult(w)); return esc(w.toString()); });
    p("factory attribute bad", () -> { TransformerFactory.newInstance().setAttribute("nope", 3); return "no"; });
    p("factory attribute bad type", () -> { TransformerFactory.newInstance().setAttribute("indent-number", new Object()); return "no"; });
    p("transformer props", () -> { Transformer t = TransformerFactory.newInstance().newTransformer(); StringBuilder sb = new StringBuilder(); for (String k : new String[]{"method", "version", "encoding", "omit-xml-declaration", "standalone", "doctype-public", "doctype-system", "cdata-section-elements", "indent", "media-type", "{http://xml.apache.org/xslt}indent-amount", "{x}custom"}) sb.append(k).append('=').append(t.getOutputProperty(k)).append(';'); return sb + new TreeMap<Object, Object>(t.getOutputProperties()).toString(); });
    p("transformer set props", () -> { Transformer t = TransformerFactory.newInstance().newTransformer(); t.setOutputProperty("indent", "yes"); t.setOutputProperty("{x}custom", "v"); t.setOutputProperty("method", "html"); Properties pr = t.getOutputProperties(); return new TreeMap<Object, Object>(pr) + "|" + t.getOutputProperty("indent") + "|" + t.getOutputProperty("encoding"); });
    p("transformer bad prop", () -> { TransformerFactory.newInstance().newTransformer().setOutputProperty("nope", "x"); return "no"; });
    p("transformer bad prop get", () -> TransformerFactory.newInstance().newTransformer().getOutputProperty("nope"));
    p("transformer setOutputProperties", () -> { Transformer t = TransformerFactory.newInstance().newTransformer(); Properties pr = new Properties(); pr.setProperty("indent", "yes"); t.setOutputProperties(pr); String a = t.getOutputProperty("indent"); t.setOutputProperties(null); return a + "|" + t.getOutputProperty("indent") + "|" + t.getOutputProperties().size(); });
    p("transformer setOutputProperties bad", () -> { Properties pr = new Properties(); pr.setProperty("bad", "x"); TransformerFactory.newInstance().newTransformer().setOutputProperties(pr); return "no"; });
    p("transformer params", () -> { Transformer t = TransformerFactory.newInstance().newTransformer(); t.setParameter("p", "v"); String a = String.valueOf(t.getParameter("p")); t.clearParameters(); return a + "|" + t.getParameter("p"); });
    p("transformer null param", () -> { TransformerFactory.newInstance().newTransformer().setParameter("p", null); return "no"; });
    p("transformer reset", () -> { Transformer t = TransformerFactory.newInstance().newTransformer(); t.setOutputProperty("indent", "yes"); t.setParameter("p", "v"); t.reset(); return t.getOutputProperty("indent") + "|" + t.getParameter("p"); });
    p("transformer error listener null", () -> { TransformerFactory.newInstance().newTransformer().setErrorListener(null); return "no"; });
    p("factory error listener null", () -> { TransformerFactory.newInstance().setErrorListener(null); return "no"; });
    p("resolvers", () -> { Transformer t = TransformerFactory.newInstance().newTransformer(); URIResolver r = (h, b) -> null; t.setURIResolver(r); return (t.getURIResolver() == r) + "|" + t.getErrorListener().getClass().getSimpleName().length(); });
    p("handler", () -> { SAXTransformerFactory tf = (SAXTransformerFactory) TransformerFactory.newInstance(); TransformerHandler h = tf.newTransformerHandler(); StringWriter w = new StringWriter(); h.setResult(new StreamResult(w)); h.getTransformer().setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes"); h.startDocument(); h.startElement("", "a", "a", new AttributesImpl()); h.characters("t<".toCharArray(), 0, 2); h.endElement("", "a", "a"); h.endDocument(); return w + "|" + h.getSystemId(); });
    p("handler null result", () -> { SAXTransformerFactory tf = (SAXTransformerFactory) TransformerFactory.newInstance(); tf.newTransformerHandler().setResult(null); return "no"; });
    p("null stylesheet", () -> { TransformerFactory.newInstance().newTransformer((Source) null); return "no"; });
    p("transformer class", () -> TransformerFactory.newInstance().newTransformer() instanceof Transformer);
    Runtime.getRuntime().halt(0);
  }
}
