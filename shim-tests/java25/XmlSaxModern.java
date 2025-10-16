/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 * Minimum release: 21
 */

import java.io.*; import java.util.*;
import javax.xml.parsers.*;
import org.xml.sax.*; import org.xml.sax.ext.*; import org.xml.sax.helpers.*;
public class XmlSaxModern {
  static class Log extends DefaultHandler2 {
    final StringBuilder out = new StringBuilder(); final StringBuilder text = new StringBuilder(); Locator loc; boolean pos;
    String textAt = ""; String at() { return pos && loc != null ? "@" + loc.getLineNumber() + ":" + loc.getColumnNumber() : ""; }
    void flush() { if (text.length() > 0) { out.append("chars[").append(esc(text.toString())).append("]").append(textAt).append('\n'); text.setLength(0); } }
    static String esc(String s) { return s.replace("\n", "\\n").replace("\t", "\\t").replace("\r", "\\r"); }
    public void setDocumentLocator(Locator l) { loc = l; }
    public void startDocument() { flush(); out.append("startDoc").append(at()).append('\n'); }
    public void endDocument() { flush(); out.append("endDoc").append(at()).append('\n'); }
    public void startPrefixMapping(String p, String u) { flush(); out.append("startNs ").append(p).append('=').append(u).append('\n'); }
    public void endPrefixMapping(String p) { flush(); out.append("endNs ").append(p).append('\n'); }
    public void startElement(String u, String l, String q, Attributes a) { flush(); out.append("start {").append(u).append('}').append(l).append(' ').append(q); for (int i = 0; i < a.getLength(); i++) out.append(" [").append(a.getQName(i)).append('|').append(a.getURI(i)).append('|').append(a.getLocalName(i)).append('|').append(a.getType(i)).append('=').append(esc(a.getValue(i))).append(a instanceof Attributes2 ? ((Attributes2) a).isSpecified(i) ? "" : "(def)" : "").append(']'); out.append(at()).append('\n'); }
    public void endElement(String u, String l, String q) { flush(); out.append("end {").append(u).append('}').append(l).append(' ').append(q).append(at()).append('\n'); }
    public void characters(char[] c, int s, int n) { text.append(c, s, n); textAt = at(); }
    public void ignorableWhitespace(char[] c, int s, int n) { flush(); out.append("ignorable[").append(esc(new String(c, s, n))).append("]\n"); }
    public void processingInstruction(String t, String d) { flush(); out.append("pi ").append(t).append(" [").append(d).append("]").append(at()).append('\n'); }
    public void skippedEntity(String n) { flush(); out.append("skipped ").append(n).append('\n'); }
    public void startCDATA() { flush(); out.append("startCDATA\n"); }
    public void endCDATA() { flush(); out.append("endCDATA\n"); }
    public void startDTD(String n, String p, String s) { flush(); out.append("startDTD ").append(n).append(' ').append(p).append(' ').append(s).append('\n'); }
    public void endDTD() { flush(); out.append("endDTD\n"); }
    public void startEntity(String n) { }
    public void endEntity(String n) { }
    public void comment(char[] c, int s, int n) { flush(); out.append("comment [").append(esc(new String(c, s, n))).append("]").append(at()).append('\n'); }
    public void attributeDecl(String e, String a, String t, String m, String v) { flush(); out.append("attDecl ").append(e).append(' ').append(a).append(' ').append(t).append(' ').append(m).append(' ').append(v).append('\n'); }
    public void elementDecl(String n, String m) { flush(); out.append("elemDecl ").append(n).append(' ').append(m).append('\n'); }
    public void externalEntityDecl(String n, String p, String s) { flush(); out.append("extEntity ").append(n).append(' ').append(p).append(' ').append(s).append('\n'); }
    public void internalEntityDecl(String n, String v) { flush(); out.append("intEntity ").append(n).append(' ').append(esc(v)).append('\n'); }
    public void notationDecl(String n, String p, String s) { flush(); out.append("notation ").append(n).append(' ').append(p).append(' ').append(s).append('\n'); }
    public void unparsedEntityDecl(String n, String p, String s, String nn) { flush(); out.append("unparsed ").append(n).append(' ').append(p).append(' ').append(s).append(' ').append(nn).append('\n'); }
    public void warning(SAXParseException e) { flush(); out.append("WARN ").append(err(e)).append('\n'); }
    public void error(SAXParseException e) { flush(); out.append("ERROR ").append(err(e)).append('\n'); }
    public void fatalError(SAXParseException e) throws SAXException { flush(); out.append("FATAL ").append(err(e)).append('\n'); throw e; }
    static String err(SAXParseException e) { return e.getLineNumber() + ":" + e.getColumnNumber() + " " + e.getMessage(); }
  }
  static String run(String xml, boolean ns, boolean prefixes, boolean pos) {
    Log h = new Log(); h.pos = pos;
    try {
      SAXParserFactory f = SAXParserFactory.newInstance(); f.setNamespaceAware(ns);
      SAXParser p = f.newSAXParser(); XMLReader r = p.getXMLReader();
      r.setFeature("http://xml.org/sax/features/namespace-prefixes", prefixes);
      r.setProperty("http://xml.org/sax/properties/lexical-handler", h); r.setProperty("http://xml.org/sax/properties/declaration-handler", h);
      r.setContentHandler(h); r.setErrorHandler(h); r.setDTDHandler(h); r.setEntityResolver(h);
      r.parse(new InputSource(new StringReader(xml)));
    } catch (Throwable t) { h.flush(); h.out.append("EXC ").append(t.getClass().getName()); if (!(t instanceof IOException)) h.out.append(':').append(String.valueOf(t.getMessage()).replace("\n", "\\n")); h.out.append('\n'); }
    return h.out.toString();
  }
  static void t(String name, String xml, boolean ns, boolean prefixes, boolean pos0) { boolean pos = pos0 && !xml.contains("&") && !xml.contains("DOCTYPE") && !xml.contains("\r") && xml.chars().allMatch(c -> c < 128); System.out.println("### " + name + " ns=" + ns + " prefixes=" + prefixes + " pos=" + pos + " :: " + Log.esc(xml)); System.out.print(run(xml, ns, prefixes, pos)); }
  public static void main(String[] a) {
    Locale.setDefault(Locale.ENGLISH);
    String[] ok = {
      "<a/>", "<a></a>", "<a>text</a>", "<?xml version=\"1.0\"?><a/>", "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n<a b=\"c\" d='e'>x<b/>y</a>\n",
      "<a><!-- c --><?pi data?><![CDATA[<x>&]]>t</a>", "<a>&lt;&gt;&amp;&quot;&apos;&#65;&#x42;</a>", "<a b=\"&lt;&amp;&#10;&#9;&#13;x\ty\nz\"/>", "<a>line1\r\nline2\rline3\nline4</a>",
      "<!-- before --><?pi before?><a/><!-- after --><?pi after?>", "<a xmlns=\"u\"><b xmlns:p=\"v\" p:c=\"1\" d=\"2\"/></a>", "<p:a xmlns:p=\"u\"><p:b/></p:a>", "<a xmlns:p=\"u\" p:x=\"1\" xmlns:q=\"w\"/>",
      "<a xmlns=\"u\" xmlns=\"v\"/>x", "<!DOCTYPE a [<!ENTITY e \"hello &amp; <b>x</b>\">]><a>&e;</a>", "<!DOCTYPE a [<!ENTITY e \"v\"><!ATTLIST a x CDATA \"def\" y ID #IMPLIED z (p|q) \"p\">]><a>&e;</a>",
      "<!DOCTYPE a SYSTEM \"x.dtd\"><a/>", "<!DOCTYPE a PUBLIC \"-//X//Y\" \"x.dtd\" [ <!ELEMENT a (b*)> <!ELEMENT b EMPTY> ]><a> <b/> </a>", "<!DOCTYPE a [<!ELEMENT a (#PCDATA|b)*><!ELEMENT b ANY>]><a> t <b> x </b> </a>",
      "<!DOCTYPE a [<!NOTATION n SYSTEM \"s\"><!ENTITY u SYSTEM \"f.gif\" NDATA n>]><a/>", "<!DOCTYPE a [<!ENTITY % pe \"<!ENTITY e2 'x'>\"> %pe;]><a>&e2;</a>", "<a>\u00e9\u4e2d\ud83d\ude00</a>",
      "<a  b = \"1\"  c='2'  ></a  >", "<a><b><c/></b></a>", "<a>]]&gt;</a>", "<a>   </a>", "<a>\n  <b/>\n</a>", "<a b=\"  x   y  \"/>", "<?xml version='1.1'?><a/>", "<a xml:lang=\"en\" xml:space=\"preserve\"/>",
      "<a><![CDATA[]]></a>", "<a><![CDATA[x]]><![CDATA[y]]></a>", "<a><!----></a>", "<a><?pi?></a>", "<a><?pi  spaced data  ?></a>", "<a>&#x10000;</a>", "\ufeff<a/>", "<a b=\"\"/>", "<_a.b-c/>", "<a:b:c xmlns:a=\"u\"/>"
    };
    for (int i = 0; i < ok.length; i++) { t("ok" + i, ok[i], false, false, true); t("ok" + i, ok[i], true, false, true); }
    t("pfx", "<a xmlns:p=\"u\" p:x=\"1\"><p:b xmlns=\"d\"/></a>", true, true, false);
    t("pfx2", "<a xmlns=\"d\" xmlns:p=\"u\"/>", true, true, false);
    String[] bad = {
      "", " ", "x", "<a>", "<a></b>", "<a><b></a></b>", "<a b=c/>", "<a b/>", "<a b=\"1\" b=\"2\"/>", "<a>&undefined;</a>", "<a>&</a>", "<a>&amp</a>", "<a b=\"<\"/>", "<a b=\"&\"/>", "<a/><b/>", "<a/>x", "<a/><!-- ok --><b/>",
      "<a><!-- -- --></a>", "<a><!-- x </a>", "<a><![CDATA[x</a>", "<a><?pi x</a>", "<a><?xml x?></a>", "<?xml version=\"1.0\"?>\n<?xml version=\"1.0\"?><a/>", "<a>\u0001</a>", "<a>&#0;</a>", "<a>&#x110000;</a>", "<a>&#xD800;</a>",
      "<1a/>", "<a 1b=\"x\"/>", "<a b='x\"/>", "< a/>", "<a/ >", "<a b=\"1\"c=\"2\"/>", "<a></ a>", "<a>]]></a>", "<!DOCTYPE a><!DOCTYPE a><a/>", "<a><!DOCTYPE a></a>", "<?xml version=\"2.0\"?><a/>", "<?xml encoding=\"UTF-8\"?><a/>",
      "<?xml version=\"1.0\" encoding=\"nope\"?><a/>", "<?xml version=\"1.0\" standalone=\"maybe\"?><a/>", "<?xml version=\"1.0\"?", "<a", "<a b", "<a b=", "<a b=\"x", "<!-- unterminated", "<!DOCTYPE a [", "<!DOCTYPE a [<!ENTITY e \"x\">", 
      "<p:a/>", "<a p:b=\"1\"/>", "<a xmlns:p=\"\"/>", "<a xmlns:xml=\"x\"/>", "<a xmlns:xmlns=\"x\"/>", "<:a/>", "<a:/>", "<a b:=\"1\"/>", "<!DOCTYPE a [<!ENTITY e \"&e;\">]><a>&e;</a>", "<!DOCTYPE a [<!ENTITY e \"<b>\">]><a>&e;</a>",
      "<!DOCTYPE a [<!ENTITY e \"x\"><!ENTITY e \"y\">]><a>&e;</a>", "<a>&e;</a><!DOCTYPE a [<!ENTITY e \"x\">]>", "<!DOCTYPE a [<!ELEMENT a>]><a/>", "<!DOCTYPE a [<!FOO>]><a/>", "<a b=\"1\" xmlns:b=\"u\" b:c=\"1\" b:c=\"2\" xmlns:d=\"u\" d:c=\"3\"/>"
    };
    for (int i = 0; i < bad.length; i++) { t("bad" + i, bad[i], false, false, true); t("bad" + i, bad[i], true, false, true); }
  }
}
