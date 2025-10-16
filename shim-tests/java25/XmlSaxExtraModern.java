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
public class XmlSaxExtraModern {
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

  static final Map<String, String> files = new HashMap<>();
  static String run(InputSource src, boolean ns, boolean prefixes, boolean pos) {
    Log h = new Log(); h.pos = pos;
    try {
      SAXParserFactory f = SAXParserFactory.newInstance(); f.setNamespaceAware(ns);
      SAXParser p = f.newSAXParser(); XMLReader r = p.getXMLReader();
      r.setFeature("http://xml.org/sax/features/namespace-prefixes", prefixes);
      r.setProperty("http://xml.org/sax/properties/lexical-handler", h); r.setProperty("http://xml.org/sax/properties/declaration-handler", h);
      r.setContentHandler(h); r.setErrorHandler(h); r.setDTDHandler(h);
      r.setEntityResolver(new EntityResolver() { public InputSource resolveEntity(String pub, String sys) { h.flush(); h.out.append("resolve ").append(pub).append(' ').append(sys == null ? null : sys.substring(sys.lastIndexOf('/') + 1)).append('\n'); String k = sys.substring(sys.lastIndexOf('/') + 1); String c = files.get(k); return c == null ? null : new InputSource(new StringReader(c)); } });
      r.parse(src);
    } catch (Throwable t) { h.flush(); h.out.append("EXC ").append(t.getClass().getName()); if (!(t instanceof IOException)) h.out.append(':').append(String.valueOf(t.getMessage()).replace("\n", "\\n")); h.out.append('\n'); }
    return h.out.toString();
  }
  static void t(String name, String xml, boolean ns, boolean prefixes) { System.out.println("### " + name + " ns=" + ns + " :: " + Log.esc(xml)); System.out.print(run(new InputSource(new StringReader(xml)), ns, prefixes, false)); }
  static void tb(String name, byte[] data, String forced) { System.out.println("### " + name + " :: " + data.length + " bytes enc=" + forced); InputSource is = new InputSource(new ByteArrayInputStream(data)); if (forced != null) is.setEncoding(forced); System.out.print(run(is, true, false, false)); }
  public static void main(String[] a) throws Exception {
    Locale.setDefault(Locale.ENGLISH);
    String[] dtd = {
      "<!DOCTYPE a [<!ATTLIST a id ID #REQUIRED n NMTOKEN #IMPLIED ns NMTOKENS #IMPLIED r IDREF #IMPLIED rs IDREFS #IMPLIED e ENTITY #IMPLIED es ENTITIES #IMPLIED c CDATA #FIXED \"fx\">]><a id=\" x1 \" n=\"  tok  \" ns=\" a   b\tc \" r=\"x1\" rs=\"x1  x1\"/>",
      "<!DOCTYPE a [<!ATTLIST a t (x|y|z) \"y\" u (  p | q ) #IMPLIED>]><a u=\"  q \"/>",
      "<!DOCTYPE a [<!NOTATION gif SYSTEM \"viewer\"><!NOTATION png PUBLIC \"-//png\"><!NOTATION jpg PUBLIC \"-//jpg\" \"jpg.exe\"><!ATTLIST a fmt NOTATION (gif|png) #IMPLIED>]><a fmt=\"gif\"/>",
      "<!DOCTYPE a [<!ELEMENT a (b , c? , (d | e)+ , f*)><!ELEMENT b (#PCDATA)><!ELEMENT c EMPTY><!ELEMENT d ANY><!ELEMENT e ( #PCDATA | x | y )*><!ELEMENT f (g)>]><a/>",
      "<!DOCTYPE a [<!ELEMENT a (b,c)><!ELEMENT b EMPTY><!ELEMENT c EMPTY>]><a>\n <b/>\n <c/>\n</a>",
      "<!DOCTYPE a [<!ELEMENT a (#PCDATA)>]><a>\n text \n</a>",
      "<!DOCTYPE a [<!ELEMENT a (b)*><!ELEMENT b ANY>]><a> <b> </b> </a>",
      "<!DOCTYPE a [<!ENTITY % e1 \"<!ELEMENT a ANY>\"> %e1; <!ENTITY e2 \"v\">]><a>&e2;</a>",
      "<!DOCTYPE a [<!ENTITY e \"&#65;&#x42;&amp;\">]><a>&e;</a>",
      "<!DOCTYPE a [<!ENTITY e \"a&#38;#60;b\">]><a>&e;</a>",
      "<!DOCTYPE a [<!ENTITY e \"<b/>t\"><!ENTITY f \"[&e;]\">]><a>&f;</a>",
      "<!DOCTYPE a [<!ENTITY e \"v1\"><!ATTLIST a x CDATA \"d&e;\">]><a/>",
      "<!DOCTYPE a [<!ENTITY e \"v&amp;w\">]><a x=\"1&e;2\"/>",
      "<!DOCTYPE a [<!ENTITY e \"<b/>\">]><a x=\"&e;\"/>",
      "<!DOCTYPE a [<!ENTITY e SYSTEM \"e.xml\">]><a x=\"&e;\"/>",
      "<!DOCTYPE a [<!ENTITY u SYSTEM \"u.gif\" NDATA gif><!NOTATION gif SYSTEM \"v\">]><a>&u;</a>",
      "<!DOCTYPE a PUBLIC \"-//pub  lic\n//id\" \"s.dtd\" [ ]><a/>",
      "<!DOCTYPE a [<!-- c1 --><?pi in dtd?><!ENTITY e \"v\"><!-- c2 -->]><a/>",
      "<!DOCTYPE a [\n<!ENTITY e \"v\">\n]\n>\n<a/>",
      "<!DOCTYPE a [<!ENTITY lt \"&#38;#60;\"><!ENTITY amp \"&#38;#38;\">]><a>&lt;&amp;</a>",
      "<?xml version=\"1.0\" standalone=\"yes\"?><!DOCTYPE a [<!ENTITY e \"v\">]><a>&e;</a>",
      "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<!-- c --><a/>",
      "<?xml  version = '1.0'  encoding = 'UTF-8'  standalone = 'no' ?><a/>",
      "<a>x<![CDATA[ y ]]>z<![CDATA[]]></a>",
      "<a xml:space=\"preserve\">  <b/>  </a>",
      "<a>&#x1F600;&#128512;</a>",
      "<a>\ud800\udc00</a>",
      "<a b=\"\ud800\udc00\"/>",
      "<a\n b='1'\n c='2'\n/>",
      "<a>text<!--c-->more<?p d?>end</a>",
      "<a b=\"1\" ><b/></a >",
      "<a><b/><b/></a>",
      "<root xmlns:a=\"u1\" xmlns:b=\"u2\"><a:x b:y=\"1\"/><x xmlns=\"u3\"><y xmlns=\"\"/></x></root>",
      "<a xmlns:p=\"u\"><p:b xmlns:p=\"v\"><p:c/></p:b><p:d/></a>",
      "<a xmlns=\"u\" b=\"1\" xmlns:p=\"v\" p:b=\"2\"/>",
      "<a xml:lang=\"en\"><b xml:base=\"x\"/></a>",
    };
    for (int i = 0; i < dtd.length; i++) { t("d" + i, dtd[i], false, false); t("d" + i, dtd[i], true, false); }
    files.put("ext.dtd", "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<!ENTITY ee \"from ext\"><!ELEMENT a ANY><!ATTLIST a dflt CDATA \"dv\">");
    files.put("ext2.dtd", "<!ENTITY % inc \"INCLUDE\"><!ENTITY % ign \"IGNORE\"><![%inc;[<!ENTITY included \"yes\">]]><![%ign;[<!ENTITY ignored \"no\">]]><!ENTITY % pe SYSTEM \"pe.ent\">%pe;");
    files.put("pe.ent", "<!ENTITY frompe \"pe-value\">");
    files.put("ext.xml", "<?xml version=\"1.0\"?><ignored/>text from ext");
    files.put("ext2.xml", "<b>inner</b>tail");
    files.put("bad.xml", "<b>unclosed");
    files.put("a.dtd", "<!ELEMENT a (b)>");
    String[] ext = {
      "<!DOCTYPE a SYSTEM \"ext.dtd\"><a>&ee;</a>",
      "<!DOCTYPE a SYSTEM \"ext.dtd\"><a dflt=\"x\">&ee;</a>",
      "<!DOCTYPE a SYSTEM \"ext2.dtd\"><a>&included;&frompe;</a>",
      "<!DOCTYPE a SYSTEM \"ext2.dtd\"><a>&ignored;</a>",
      "<!DOCTYPE a [<!ENTITY e SYSTEM \"ext.xml\">]><a>[&e;]</a>",
      "<!DOCTYPE a [<!ENTITY e SYSTEM \"ext2.xml\">]><a>&e;</a>",
      "<!DOCTYPE a [<!ENTITY e SYSTEM \"bad.xml\">]><a>&e;</a>",
      "<!DOCTYPE a [<!ENTITY e SYSTEM \"nothere.xml\">]><a>&e;</a>",
      "<!DOCTYPE a [<!ENTITY e PUBLIC \"-//x\" \"ext.xml\">]><a>&e;</a>",
      "<!DOCTYPE a SYSTEM \"a.dtd\" [<!ELEMENT a (b)><!ELEMENT b EMPTY>]><a><b/></a>",
      "<!DOCTYPE a SYSTEM \"nothere.dtd\"><a/>",
    };
    for (int i = 0; i < ext.length; i++) { t("e" + i, ext[i], true, false); }
    byte[] utf8 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><a>\u00e9\u4e2d</a>".getBytes("UTF-8");
    tb("utf8", utf8, null);
    tb("latin1", "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?><a>\u00e9</a>".getBytes("ISO-8859-1"), null);
    tb("latin1 forced utf8", "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?><a>\u00e9</a>".getBytes("ISO-8859-1"), "UTF-8");
    tb("utf16le bom", "\ufeff<?xml version=\"1.0\" encoding=\"UTF-16\"?><a>\u00e9\u4e2d</a>".getBytes("UTF-16LE"), null);
    tb("utf16be bom", ("<a>\u00e9</a>").getBytes("UTF-16"), null);
    tb("utf16be nobom", ("<a>\u00e9</a>").getBytes("UTF-16BE"), null);
    tb("utf16le nobom", ("<a>\u00e9</a>").getBytes("UTF-16LE"), null);
    tb("utf8 bom", ("\ufeff<a>x</a>").getBytes("UTF-8"), null);
    tb("ascii decl", "<?xml version=\"1.0\" encoding=\"US-ASCII\"?><a>x</a>".getBytes("US-ASCII"), null);
    tb("bad encoding", "<?xml version=\"1.0\" encoding=\"nope-nope\"?><a>x</a>".getBytes("US-ASCII"), null);
    tb("bad encoding name", "<?xml version=\"1.0\" encoding=\"1bad\"?><a>x</a>".getBytes("US-ASCII"), null);
    tb("empty", new byte[0], null);
    String[] more = {
      "<a>\u0000</a>", "<a b=\"\u0001\"/>", "<!-- \u0002 --><a/>", "<a><?pi \u0003?></a>", "<a><![CDATA[\u0004]]></a>", "\u0005<a/>", "<a/>\u0006", "<a>\ufffe</a>", "<a>\uffff</a>",
      "<a>&#x0;</a>", "<a>&#;</a>", "<a>&#x;</a>", "<a>&#65</a>", "<a>&#abc;</a>", "<a>&#xzz;</a>", "<a>&#-1;</a>", "<a b=\"&#0;\"/>", "<a b=\"&#65\"/>",
      "<a b=\"&amp\"/>", "<a b=\"&unknown;\"/>", "<a b='&lt;&gt;'/>", "<a b=\"a\"b\"/>", "<a b=\"1\" b=\"1\"/>", "<a b = \"1\" c/>", "<a b=\"1\"c=\"2\"/>", "<a =\"1\"/>",
      "<a></a ><b/>", "<a></a>\n\n  <!-- c -->\n", "<a>x</a>y", "<a>x</a><", "<a>x</a><?", "<a>x</a><!", "<a>x</a><!-", "<a>x</a><!--", "<a>x</a>&", "<a>x</a>&amp;",
      "<a>&amp;&unknown;</a>", "<a>&lt</a>", "<a>&lt;;</a>", "<a>&;</a>", "<a>& amp;</a>",
      "<a><b></a>", "<a></b></a>", "<a><b/></a></a>", "<a><b></b>", "</a>", "<a/></a>", "<a><![CDATA[x]]</a>", "<a><![CDAT[x]]></a>", "<a><!x></a>", "<a><!-x--></a>", "<a><!-- x --!></a>", "<a><!-- x ---></a>",
      "<a><?pi?>x</a>", "<a><?pi ?></a>", "<a><? pi?></a>", "<a><?p?i?></a>", "<a><?pi \n data\n?></a>", "<a><?XML x?></a>", "<a><?xml-stylesheet x?></a>", "<a><?x:y z?></a>",
      "<a:b/>", "<a:b xmlns:a=\"\"/>", "<a b:c=\"1\" xmlns:b=\"\"/>", "<a xmlns=\"\"/>", "<a xmlns:p=\"a b\"/>", "<a xmlns:p=\"http://www.w3.org/XML/1998/namespace\"/>", "<a xmlns=\"http://www.w3.org/2000/xmlns/\"/>",
      "<!DOCTYPE>", "<!DOCTYPE a", "<!DOCTYPE a >", "<!DOCTYPE a []>", "<!DOCTYPE a [>", "<!DOCTYPE a [<!ELEMENT>]>", "<!DOCTYPE a [<!ENTITY>]>", "<!DOCTYPE a [<!ENTITY e>]>", "<!DOCTYPE a [<!ENTITY e \"v>]>", "<!DOCTYPE a [<!ATTLIST>]>",
      "<!DOCTYPE a [<!ATTLIST a>]><a/>", "<!DOCTYPE a [<!ATTLIST a b>]><a/>", "<!DOCTYPE a [<!ATTLIST a b CDATA>]><a/>", "<!DOCTYPE a [<!ATTLIST a b FOO #IMPLIED>]><a/>", "<!DOCTYPE a [<!NOTATION n>]><a/>", "<!DOCTYPE a [<!NOTATION n SYSTEM>]><a/>",
      "<!DOCTYPE a [%p;]><a/>", "<!DOCTYPE a [<!ENTITY % p \"x\"> %p ]><a/>", "<!DOCTYPE a [<!ENTITY e \"%p;\">]><a/>", "<!DOCTYPE a [<!ENTITY e \"&\">]><a/>", "<!DOCTYPE a [<![INCLUDE[ ]]>]><a/>",
      "<!DOCTYPE a [ ] ><a/>", "<!DOCTYPE a PUBLIC \"x\"><a/>", "<!DOCTYPE a PUBLIC><a/>", "<!DOCTYPE a SYSTEM><a/>", "<!DOCTYPE a PUBLIC \"\u0001\" \"s\"><a/>", "<!DOCTYPE a FOO><a/>", "<!DOCTYPEa><a/>",
      "<?xml version?><a/>", "<?xml version=?><a/>", "<?xml version=1.0?><a/>", "<?xml version=\"1.0\" encoding?><a/>",
      " <?xml version=\"1.0\"?><a/>", "<?xml version=\"1.0\" ?><a/>", "<?xml version=\"1.0\"  ?><a/>", "<?xml version=\"1.1\"?><a/>", "<?xml version=\"1.01\"?><a/>",
    };
    for (int i = 0; i < more.length; i++) { t("m" + i, more[i], false, false); t("m" + i, more[i], true, false); }
  }
}
