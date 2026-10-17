/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 * Minimum release: 21
 */

import java.io.*;
import java.util.*;
import javax.xml.namespace.*;
import javax.xml.stream.*;
import javax.xml.stream.events.*;

public class StaxModern {
static class S1 {
  static String name(int t) { switch (t) { case 1: return "START_ELEMENT"; case 2: return "END_ELEMENT"; case 3: return "PROCESSING_INSTRUCTION"; case 4: return "CHARACTERS"; case 5: return "COMMENT"; case 6: return "SPACE"; case 7: return "START_DOCUMENT"; case 8: return "END_DOCUMENT"; case 9: return "ENTITY_REFERENCE"; case 10: return "ATTRIBUTE"; case 11: return "DTD"; case 12: return "CDATA"; case 13: return "NAMESPACE"; case 14: return "NOTATION_DECLARATION"; case 15: return "ENTITY_DECLARATION"; default: return "?" + t; } }
  static String q(String s) { return s == null ? "null" : "'" + s.replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t") + "'"; }
  static String fail(Throwable t) { return "!" + t.getClass().getName() + ":" + String.valueOf(t.getMessage()).replace("\n", "\\n"); }
  static void dump(String xml, XMLInputFactory f, StringBuilder out) {
    XMLStreamReader r = null;
    try {
      r = f.createXMLStreamReader(new StringReader(xml));
      int guard = 0;
      while (guard++ < 200) {
        int t = r.getEventType();
        Location l = r.getLocation();
        out.append("  ").append(name(t)).append(" @").append(l.getLineNumber()).append(':').append(l.getColumnNumber());
        switch (t) {
          case 1: out.append(" <").append(r.getName()).append("> local=").append(r.getLocalName()).append(" prefix=").append(q(r.getPrefix())).append(" uri=").append(q(r.getNamespaceURI())).append(" attrs=").append(r.getAttributeCount()); for (int i = 0; i < r.getAttributeCount(); i++) out.append(" [").append(r.getAttributeName(i)).append('/').append(q(r.getAttributePrefix(i))).append('=').append(q(r.getAttributeValue(i))).append(':').append(r.getAttributeType(i)).append(r.isAttributeSpecified(i) ? "" : "(d)").append(']'); out.append(" ns=").append(r.getNamespaceCount()); for (int i = 0; i < r.getNamespaceCount(); i++) out.append(" {").append(q(r.getNamespacePrefix(i))).append('=').append(q(r.getNamespaceURI(i))).append('}'); break;
          case 2: out.append(" </").append(r.getName()).append("> ns=").append(r.getNamespaceCount()); for (int i = 0; i < r.getNamespaceCount(); i++) out.append(" {").append(q(r.getNamespacePrefix(i))).append('=').append(q(r.getNamespaceURI(i))).append('}'); break;
          case 3: out.append(" target=").append(q(r.getPITarget())).append(" data=").append(q(r.getPIData())); break;
          case 4: case 5: case 6: case 12: out.append(" text=").append(q(r.getText())).append(" ws=").append(r.isWhiteSpace()).append(" tlen=").append(r.getTextLength()); break;
          case 9: out.append(" name=").append(q(r.getLocalName())).append(" text=").append(q(r.getText())); break;
          case 11: out.append(" dtd=").append(q(r.getText())); break;
          case 7: out.append(" ver=").append(q(r.getVersion())).append(" enc=").append(q(r.getEncoding())).append(" cse=").append(q(r.getCharacterEncodingScheme())).append(" sa=").append(r.isStandalone()).append("/").append(r.standaloneSet()); break;
          default: break;
        }
        out.append('\n');
        if (!r.hasNext()) break;
        r.next();
      }
    } catch (Throwable t) { out.append("  ").append(fail(t)).append('\n'); }
    finally { try { if (r != null) r.close(); } catch (Throwable t) { } }
  }
  public static void run() throws Exception {
    String[] docs = {"<a/>", "<a>text</a>", "<?xml version='1.0' encoding='UTF-8' standalone='yes'?><a/>", "<?xml version=\"1.0\"?>\n<!-- c1 -->\n<?pi data?>\n<a x='1' y=\"2\">hi<b/>there<!--c--><![CDATA[cd<>]]>&amp;&lt;&#65;&#x42;</a>\n<!-- end -->", "<a xmlns='d' xmlns:p='u'><p:b p:c='1' d='2'/></a>", "<a>  <b/>  </a>", "<!DOCTYPE a [<!ENTITY e 'ent'><!ELEMENT a ANY><!ATTLIST a x CDATA 'dflt'>]><a>&e;</a>", "<!DOCTYPE a [<!ENTITY e 'ent'>]><a>x&e;y</a>", "<!DOCTYPE a SYSTEM 'a.dtd'><a/>", "<!DOCTYPE a PUBLIC '-//X//Y' 'a.dtd' [<!NOTATION n SYSTEM 'x'>]><a/>", "<a>&undefined;</a>", "<a><b></a>", "<a", "", "<a/><b/>", "text", "<a>&#0;</a>", "<a b='1' b='2'/>", "<?xml version='2.0'?><a/>", "<a>\u00e9\u4e2d\ud83d\ude00</a>", "<a>x<!--c1--><!--c2-->y</a>", "<a><![CDATA[a]]><![CDATA[b]]>c</a>", "<a>a&amp;b&lt;c</a>", "<!DOCTYPE a [<!ELEMENT a (b)*><!ELEMENT b EMPTY>]><a> <b/> </a>", "<!DOCTYPE a [<!ELEMENT a (#PCDATA|b)*><!ELEMENT b EMPTY>]><a> <b/> t </a>", "<a x='&lt;&#65;&amp;'/>", "<a x='a\nb\tc'/>", "<p:a xmlns:p='u'/>", "<p:a/>", "<a xmlns:p='u'><p:b xmlns:p='v'/></a>", "<a xmlns=''/>", "<a xml:lang='en'/>", "<a xmlns:xml='http://www.w3.org/XML/1998/namespace'/>", "<a><?pi?></a>", "<a><?pi  x  ?></a>", "\ufeff<a/>", " <a/>", "<a/> ", "<a/>x"};
    for (String doc : docs) { StringBuilder sb = new StringBuilder("== " + q(doc) + "\n"); dump(doc, XMLInputFactory.newInstance(), sb); System.out.print(sb); }
    System.out.println("##### coalescing");
    XMLInputFactory fc = XMLInputFactory.newInstance(); fc.setProperty(XMLInputFactory.IS_COALESCING, true);
    for (String doc : new String[]{docs[3], docs[7], docs[22], docs[23], docs[24], docs[19]}) { StringBuilder sb = new StringBuilder("== " + q(doc) + "\n"); dump(doc, fc, sb); System.out.print(sb); }
    System.out.println("##### no entity replacement");
    XMLInputFactory fe = XMLInputFactory.newInstance(); fe.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, false);
    for (String doc : new String[]{docs[6], docs[7], docs[10], docs[3]}) { StringBuilder sb = new StringBuilder("== " + q(doc) + "\n"); dump(doc, fe, sb); System.out.print(sb); }
    System.out.println("##### not namespace aware");
    XMLInputFactory fn = XMLInputFactory.newInstance(); fn.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, false);
    for (String doc : new String[]{docs[4], docs[29], docs[30], docs[31]}) { StringBuilder sb = new StringBuilder("== " + q(doc) + "\n"); dump(doc, fn, sb); System.out.print(sb); }
  } }

static class S2 {
  static String q(Object s) { return s == null ? "null" : s instanceof String ? "'" + ((String) s).replace("\n", "\\n") + "'" : String.valueOf(s); }
  static String fail(Throwable t) { return "!" + t.getClass().getName() + ":" + String.valueOf(t.getMessage()).replace("\n", "\\n"); }
  interface G { Object get(XMLStreamReader r) throws Exception; }
  static Map<String, G> getters = new LinkedHashMap<String, G>();
  static { 
    getters.put("getName", r -> r.getName()); getters.put("getLocalName", r -> r.getLocalName()); getters.put("getPrefix", r -> r.getPrefix()); getters.put("getNamespaceURI", r -> r.getNamespaceURI()); getters.put("getNamespaceURI(p)", r -> r.getNamespaceURI("p")); getters.put("getNamespaceURI(null)", r -> r.getNamespaceURI(null)); getters.put("hasName", r -> r.hasName());
    getters.put("getText", r -> r.getText()); getters.put("getTextLength", r -> r.getTextLength()); getters.put("hasText", r -> r.hasText()); getters.put("isWhiteSpace", r -> r.isWhiteSpace()); getters.put("isCharacters", r -> r.isCharacters());
    getters.put("getTextCharacters(4)", r -> { char[] b = new char[3]; int n = r.getTextCharacters(0, b, 0, 3); return n + ":" + new String(b, 0, n); });
    getters.put("getAttributeCount", r -> r.getAttributeCount()); getters.put("getAttributeName(0)", r -> r.getAttributeName(0)); getters.put("getAttributeValue(0)", r -> r.getAttributeValue(0)); getters.put("getAttributeValue(ns,x)", r -> r.getAttributeValue(null, "x")); getters.put("getAttributeValue(ns,nope)", r -> r.getAttributeValue(null, "nope")); getters.put("getAttributeValue(5)", r -> r.getAttributeValue(5)); getters.put("getAttributeLocalName(0)", r -> r.getAttributeLocalName(0)); getters.put("getAttributeNamespace(0)", r -> r.getAttributeNamespace(0)); getters.put("getAttributePrefix(0)", r -> r.getAttributePrefix(0)); getters.put("getAttributeType(0)", r -> r.getAttributeType(0)); getters.put("isAttributeSpecified(0)", r -> r.isAttributeSpecified(0));
    getters.put("getNamespaceCount", r -> r.getNamespaceCount()); getters.put("getNamespacePrefix(0)", r -> r.getNamespacePrefix(0)); getters.put("getNamespaceURI(0)", r -> r.getNamespaceURI(0)); getters.put("getNamespaceContext.p", r -> r.getNamespaceContext().getNamespaceURI("p")); getters.put("getNamespaceContext.prefix(u)", r -> r.getNamespaceContext().getPrefix("u")); 
    getters.put("getPITarget", r -> r.getPITarget()); getters.put("getPIData", r -> r.getPIData());
    getters.put("getElementText", r -> r.getElementText()); getters.put("require(START_ELEMENT)", r -> { r.require(XMLStreamConstants.START_ELEMENT, null, null); return "ok"; }); getters.put("require(END,null,b)", r -> { r.require(XMLStreamConstants.END_ELEMENT, null, "b"); return "ok"; });
    getters.put("isStartElement", r -> r.isStartElement()); getters.put("isEndElement", r -> r.isEndElement()); 
    getters.put("getVersion", r -> r.getVersion()); getters.put("getEncoding", r -> r.getEncoding()); getters.put("getCharacterEncodingScheme", r -> r.getCharacterEncodingScheme()); getters.put("isStandalone", r -> r.isStandalone()); getters.put("standaloneSet", r -> r.standaloneSet());
    getters.put("getProperty(x)", r -> r.getProperty("x")); getters.put("getProperty(null)", r -> r.getProperty(null)); getters.put("getProperty(report-cdata)", r -> r.getProperty("http://java.sun.com/xml/stream/properties/report-cdata-event")); getters.put("getProperty(coalescing)", r -> r.getProperty(XMLInputFactory.IS_COALESCING)); getters.put("getProperty(nsaware)", r -> r.getProperty(XMLInputFactory.IS_NAMESPACE_AWARE));
    getters.put("getLocation", r -> r.getLocation().getLineNumber() + ":" + r.getLocation().getColumnNumber() + " sys=" + r.getLocation().getSystemId() + " pub=" + r.getLocation().getPublicId());
  }
  public static void run() throws Exception {
    String xml = "<?xml version='1.0'?><!DOCTYPE a [<!ENTITY e 'v'>]><a xmlns:p='u' x='1'><b/>t<!--c--><?pi d?>&e;<c>tx</c></a>";
    XMLInputFactory f = XMLInputFactory.newInstance(); f.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, false);
    XMLStreamReader r = f.createXMLStreamReader(new StringReader(xml));
    int guard = 0;
    while (true) {
      int t = r.getEventType();
      System.out.println("##### state " + S1.name(t));
      for (Map.Entry<String, G> g : getters.entrySet()) {
        if (g.getKey().equals("getElementText") || g.getKey().startsWith("require")) continue;
        String res; try { res = q(g.getValue().get(r)); } catch (Throwable x) { res = fail(x); }
        System.out.println("  " + g.getKey() + " = " + res);
      }
      if (!r.hasNext()) break;
      r.next();
      if (guard++ > 50) break;
    }
    System.out.println("##### after end");
    try { r.next(); } catch (Throwable x) { System.out.println("next after end " + fail(x)); }
    try { System.out.println("hasNext " + r.hasNext()); } catch (Throwable x) { System.out.println("hasNext " + fail(x)); }
    try { r.close(); r.close(); System.out.println("closed twice"); } catch (Throwable x) { System.out.println("close " + fail(x)); }
    try { System.out.println("after close type " + r.getEventType()); } catch (Throwable x) { System.out.println("after close type " + fail(x)); }
    String[] docs = {"<a>text</a>", "<a>t<!--c-->u<?pi?>v</a>", "<a><b/></a>", "<a>x<b/>y</a>", "<a>&amp;t<![CDATA[c]]></a>", "<a></a>", "<a/>", "<a> <b/> </a>", "<a>t<b/></a>", "<a><!--c--><b/></a>", "<?pi?><a/>", "<a/><!--c-->"};
    for (String doc : docs) {
      XMLStreamReader x = XMLInputFactory.newInstance().createXMLStreamReader(new StringReader(doc));
      StringBuilder sb = new StringBuilder("== " + q(doc) + " ");
      try { x.next(); sb.append("next=" + S1.name(x.getEventType()) + " "); sb.append("elementText=" + q(x.getElementText()) + " "); sb.append("after=" + S1.name(x.getEventType())); } catch (Throwable e) { sb.append(fail(e)); }
      System.out.println(sb);
      x = XMLInputFactory.newInstance().createXMLStreamReader(new StringReader(doc)); sb = new StringBuilder("   nextTag: ");
      try { for (int i = 0; i < 6; i++) { int tt = x.nextTag(); sb.append(S1.name(tt)).append(i == 5 ? "" : ","); } } catch (Throwable e) { sb.append(fail(e)); }
      System.out.println(sb);
      x = XMLInputFactory.newInstance().createXMLStreamReader(new StringReader(doc)); sb = new StringBuilder("   require: ");
      try { x.next(); x.require(XMLStreamConstants.START_ELEMENT, null, "a"); sb.append("ok1 "); x.require(XMLStreamConstants.START_ELEMENT, "", "a"); sb.append("ok2 "); x.require(XMLStreamConstants.START_ELEMENT, "zz", "a"); sb.append("ok3 "); } catch (Throwable e) { sb.append(fail(e)); }
      System.out.println(sb);
      try { x.require(XMLStreamConstants.END_ELEMENT, null, null); } catch (Throwable e) { System.out.println("   require2 " + fail(e)); }
      try { x.require(XMLStreamConstants.START_ELEMENT, null, "zz"); } catch (Throwable e) { System.out.println("   require3 " + fail(e)); }
    }
    XMLInputFactory ff = XMLInputFactory.newInstance();
    String[] props = {XMLInputFactory.IS_NAMESPACE_AWARE, XMLInputFactory.IS_VALIDATING, XMLInputFactory.IS_COALESCING, XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, XMLInputFactory.SUPPORT_DTD, XMLInputFactory.REPORTER, XMLInputFactory.RESOLVER, XMLInputFactory.ALLOCATOR, "http://java.sun.com/xml/stream/properties/report-cdata-event", "http://java.sun.com/xml/stream/properties/implementation-name", "http://java.sun.com/xml/stream/properties/ignore-external-dtd", "javax.xml.stream.isSupportingExternalEntities", "jdk.xml.entityExpansionLimit", "http://javax.xml.XMLConstants/property/accessExternalDTD", "http://www.oracle.com/xml/jaxp/properties/isStandalone", "nope", "http://java.sun.com/xml/stream/properties/reuse-instance", "http://java.sun.com/xml/stream/properties/report-cdata-event"};
    for (String p : props) { String res; try { res = "supported=" + ff.isPropertySupported(p) + " value=" + q(ff.getProperty(p)); } catch (Throwable x) { res = fail(x); } System.out.println("prop " + p + " " + res); }
    try { ff.setProperty("nope", true); } catch (Throwable x) { System.out.println("set nope " + fail(x)); }
    try { ff.getProperty(null); } catch (Throwable x) { System.out.println("get null " + fail(x)); }
    try { ff.setProperty(null, true); } catch (Throwable x) { System.out.println("set null " + fail(x)); }
    try { ff.setProperty(XMLInputFactory.IS_COALESCING, "x"); System.out.println("coalescing str " + ff.getProperty(XMLInputFactory.IS_COALESCING)); } catch (Throwable x) { System.out.println("set coal str " + fail(x)); }
    try { ff.setProperty(XMLInputFactory.IS_VALIDATING, true); System.out.println("validating " + ff.getProperty(XMLInputFactory.IS_VALIDATING)); } catch (Throwable x) { System.out.println("set validating " + fail(x)); }
    try { ff.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, null); System.out.println("nsaware null " + ff.getProperty(XMLInputFactory.IS_NAMESPACE_AWARE)); } catch (Throwable x) { System.out.println("set nsaware null " + fail(x)); }
    try { ff.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, "false"); System.out.println("nsaware 'false' " + ff.getProperty(XMLInputFactory.IS_NAMESPACE_AWARE)); } catch (Throwable x) { System.out.println("set nsaware str " + fail(x)); }
    System.out.println("reporter/resolver/allocator " + ff.getXMLReporter() + " " + ff.getXMLResolver() + " " + ff.getEventAllocator());
    try { XMLInputFactory.newInstance().createXMLStreamReader((Reader) null); } catch (Throwable x) { System.out.println("create null reader " + fail(x)); }
    try { XMLInputFactory.newInstance().createXMLStreamReader((InputStream) null); } catch (Throwable x) { System.out.println("create null stream " + fail(x)); }
    try { XMLInputFactory.newInstance().createXMLStreamReader(new ByteArrayInputStream("<a/>".getBytes()), "nope-enc"); } catch (Throwable x) { System.out.println("create bad enc " + fail(x)); }
    try { XMLInputFactory.newInstance().createXMLStreamReader((javax.xml.transform.Source) null); } catch (Throwable x) { System.out.println("create null source " + fail(x)); }
    try { XMLStreamReader sr = XMLInputFactory.newInstance().createXMLStreamReader(new javax.xml.transform.stream.StreamSource(new StringReader("<a/>"))); System.out.println("streamsource " + S1.name(sr.getEventType())); } catch (Throwable x) { System.out.println("create streamsource " + fail(x)); }
    try { XMLStreamReader sr = XMLInputFactory.newInstance().createXMLStreamReader(new javax.xml.transform.sax.SAXSource()); } catch (Throwable x) { System.out.println("create saxsource " + fail(x)); }
    try { XMLStreamReader sr = XMLInputFactory.newInstance().createXMLStreamReader(new javax.xml.transform.stream.StreamSource()); } catch (Throwable x) { System.out.println("create empty streamsource " + fail(x)); }
    try { XMLStreamReader sr = XMLInputFactory.newInstance().createXMLStreamReader("sys:id", new StringReader("<a/>")); System.out.println("sysid " + sr.getLocation().getSystemId() + " " + S1.name(sr.next())); } catch (Throwable x) { System.out.println("create sysid " + fail(x)); }
    byte[][] bytes = { "<a/>".getBytes("UTF-8"), "<?xml version='1.0' encoding='ISO-8859-1'?><a>é</a>".getBytes("ISO-8859-1"), "<?xml version='1.0' encoding='UTF-16'?><a>x</a>".getBytes("UTF-16"), "﻿<a>x</a>".getBytes("UTF-8"), "<a>é中</a>".getBytes("UTF-8")};
    for (byte[] b : bytes) { try { XMLStreamReader sr = XMLInputFactory.newInstance().createXMLStreamReader(new ByteArrayInputStream(b)); StringBuilder sb = new StringBuilder("bytes: enc=" + q(sr.getEncoding()) + " cse=" + q(sr.getCharacterEncodingScheme()) + " "); while (sr.hasNext()) { int tt = sr.next(); sb.append(S1.name(tt)); if (tt == 4) sb.append("(" + q(sr.getText()) + ")"); sb.append(' '); } System.out.println(sb); } catch (Throwable x) { System.out.println("bytes " + fail(x)); } }
    try { XMLStreamReader sr = XMLInputFactory.newInstance().createXMLStreamReader(new ByteArrayInputStream("<a>é</a>".getBytes("UTF-8")), "UTF-8"); System.out.println("with enc: " + q(sr.getEncoding()) + " " + q(sr.getCharacterEncodingScheme())); } catch (Throwable x) { System.out.println("with enc " + fail(x)); }
    XMLStreamReader fr = XMLInputFactory.newInstance().createFilteredReader(XMLInputFactory.newInstance().createXMLStreamReader(new StringReader("<a><b/>t<!--c--></a>")), new StreamFilter() { public boolean accept(XMLStreamReader rr) { return rr.isStartElement() || rr.isEndElement(); } });
    StringBuilder sb = new StringBuilder("filtered: " + S1.name(fr.getEventType()) + " "); while (fr.hasNext()) { sb.append(S1.name(fr.next())).append(' '); } System.out.println(sb);
  } }

static class S3 {
  static String q(Object s) { return s == null ? "null" : s instanceof String ? "'" + ((String) s).replace("\n", "\\n") + "'" : String.valueOf(s); }
  static String fail(Throwable t) { return "!" + t.getClass().getName() + ":" + String.valueOf(t.getMessage()).replace("\n", "\\n"); }
  static void show(XMLEvent e) throws Exception {
    StringWriter w = new StringWriter(); try { e.writeAsEncodedUnicode(w); } catch (Throwable t) { w.write(fail(t)); }
    StringBuilder sb = new StringBuilder(S1.name(e.getEventType()) + " toString=" + q(e.toString()) + " written=" + q(w.toString()) + " loc=" + e.getLocation().getLineNumber() + ":" + e.getLocation().getColumnNumber() + " flags=" + (e.isStartElement() ? "S" : "") + (e.isEndElement() ? "E" : "") + (e.isCharacters() ? "C" : "") + (e.isAttribute() ? "A" : "") + (e.isNamespace() ? "N" : "") + (e.isStartDocument() ? "SD" : "") + (e.isEndDocument() ? "ED" : "") + (e.isEntityReference() ? "ER" : "") + (e.isProcessingInstruction() ? "PI" : ""));
    sb.append(" schema=" + e.getSchemaType());
    if (e.isStartElement()) { StartElement se = e.asStartElement(); sb.append(" name=" + se.getName() + " attrs="); for (Iterator<Attribute> it = se.getAttributes(); it.hasNext();) { Attribute a = it.next(); sb.append("[" + a.getName() + "=" + q(a.getValue()) + ":" + a.getDTDType() + ":" + a.isSpecified() + ":" + a + "]"); } sb.append(" ns="); for (Iterator<Namespace> it = se.getNamespaces(); it.hasNext();) { Namespace n = it.next(); sb.append("[" + q(n.getPrefix()) + "=" + q(n.getNamespaceURI()) + ":" + n.isDefaultNamespaceDeclaration() + ":" + n + "]"); } sb.append(" attrByName=" + se.getAttributeByName(new QName("x")) + " nsctx=" + (se.getNamespaceContext() == null ? "null" : q(se.getNamespaceContext().getNamespaceURI("p")))); }
    if (e.isEndElement()) { EndElement ee = e.asEndElement(); sb.append(" name=" + ee.getName() + " ns="); for (Iterator<Namespace> it = ee.getNamespaces(); it.hasNext();) sb.append(it.next()); }
    if (e.isCharacters()) { Characters c = e.asCharacters(); sb.append(" data=" + q(c.getData()) + " ws=" + c.isWhiteSpace() + " cdata=" + c.isCData() + " ign=" + c.isIgnorableWhiteSpace()); }
    if (e.getEventType() == XMLStreamConstants.COMMENT) sb.append(" text=" + q(((Comment) e).getText()));
    if (e.isProcessingInstruction()) sb.append(" target=" + q(((ProcessingInstruction) e).getTarget()) + " data=" + q(((ProcessingInstruction) e).getData()));
    if (e.isEntityReference()) { EntityReference er = (EntityReference) e; sb.append(" name=" + er.getName() + " decl=" + (er.getDeclaration() == null ? "null" : er.getDeclaration().getName() + "/" + er.getDeclaration().getReplacementText())); }
    if (e.isStartDocument()) { StartDocument sd = (StartDocument) e; sb.append(" ver=" + q(sd.getVersion()) + " enc=" + q(sd.getCharacterEncodingScheme()) + " encSet=" + sd.encodingSet() + " sa=" + sd.isStandalone() + " saSet=" + sd.standaloneSet() + " sys=" + q(sd.getSystemId())); }
    if (e.getEventType() == XMLStreamConstants.DTD) { DTD d = (DTD) e; sb.append(" dtd=" + q(d.getDocumentTypeDeclaration()) + " ents=" + (d.getEntities() == null ? "null" : d.getEntities().size()) + " nots=" + (d.getNotations() == null ? "null" : d.getNotations().size()) + " pm=" + d.getProcessedDTD()); }
    for (Class<?> c : new Class<?>[]{StartElement.class, EndElement.class, Characters.class, Comment.class}) { try { if (c == StartElement.class) e.asStartElement(); else if (c == EndElement.class) e.asEndElement(); else if (c == Characters.class) e.asCharacters(); } catch (Throwable t) { sb.append(" as" + c.getSimpleName() + "=" + t.getClass().getName()); } }
    System.out.println(sb);
  }
  public static void run() throws Exception {
    String[] docs = {"<?xml version='1.0' encoding='UTF-8' standalone='yes'?><!DOCTYPE a [<!ENTITY e 'ent'><!NOTATION n SYSTEM 'x'><!ENTITY u SYSTEM 'x.dat' NDATA n>]><!--c--><?pi d?><a xmlns='d' xmlns:p='u' x='1' p:y='2'>t<![CDATA[cd]]>  &#65;<p:b/><c xmlns=''/>&amp;</a>", "<a>&e;</a>", "<!DOCTYPE a [<!ELEMENT a (b)*><!ELEMENT b EMPTY>]><a> <b/> </a>"};
    for (int di = 0; di < docs.length; di++) {
      for (int mode = 0; mode < 3; mode++) {
        if (di == 1 && mode == 0) continue;
        XMLInputFactory f = XMLInputFactory.newInstance();
        if (mode == 1) f.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, false);
        if (mode == 2) { f.setProperty(XMLInputFactory.IS_COALESCING, true); f.setProperty("http://java.sun.com/xml/stream/properties/report-cdata-event", true); }
        System.out.println("##### doc " + di + " mode " + mode);
        String xml = docs[di].replace("<a>&e;", di == 1 ? "<!DOCTYPE a [<!ENTITY e 'v'>]><a>&e;" : "<a>&e;");
        if (di == 1) xml = "<!DOCTYPE a [<!ENTITY e 'v'>]><a>&e;</a>";
        XMLEventReader r = f.createXMLEventReader(new StringReader(xml));
        try { while (r.hasNext()) { show(r.nextEvent()); } } catch (Throwable t) { System.out.println("!! " + fail(t)); }
        try { r.nextEvent(); } catch (Throwable t) { System.out.println("after end: " + fail(t)); }
        try { System.out.println("peek " + r.peek()); } catch (Throwable t) { System.out.println("peek " + fail(t)); }
      }
    }
    XMLEventReader r = XMLInputFactory.newInstance().createXMLEventReader(new StringReader("<a><b>t</b> <c/></a>"));
    System.out.println("peek0 " + S1.name(r.peek().getEventType()) + " " + S1.name(r.peek().getEventType()) + " next=" + S1.name(r.nextEvent().getEventType()));
    System.out.println("nextTag " + r.nextTag() + " peek=" + S1.name(r.peek().getEventType()));
    System.out.println("nextTag b " + r.nextTag());
    System.out.println("elementText " + r.getElementText());
    try { System.out.println("nextTag2 " + r.nextTag()); System.out.println("nextTag3 " + r.nextTag()); System.out.println("nextTag4 " + r.nextTag()); System.out.println("nextTag5 " + r.nextTag());} catch (Throwable t) { System.out.println("nextTag " + fail(t)); }
    try { System.out.println("prop " + r.getProperty("javax.xml.stream.isCoalescing") + " " + r.getProperty("x")); } catch (Throwable t) { System.out.println("prop " + fail(t)); }
    try { r.getProperty(null); } catch (Throwable t) { System.out.println("prop null " + fail(t)); }
    r.close();
    try { r.nextEvent(); } catch (Throwable t) { System.out.println("after close " + fail(t)); }
    try { System.out.println("hasNext after close " + r.hasNext()); } catch (Throwable t) { System.out.println("hasNext after close " + fail(t)); }
    XMLEventReader r2 = XMLInputFactory.newInstance().createXMLEventReader(new StringReader("<a>t</a>"));
    try { r2.getElementText(); } catch (Throwable t) { System.out.println("elementText at start " + fail(t)); }
    r2.nextEvent(); r2.nextEvent();
    try { r2.getElementText(); } catch (Throwable t) { System.out.println("elementText at chars " + fail(t)); }
    XMLEventReader r3 = XMLInputFactory.newInstance().createXMLEventReader(new StringReader("<a>t<b/></a>")); r3.nextEvent(); r3.nextEvent();
    XMLStreamReader sr = XMLInputFactory.newInstance().createXMLStreamReader(new StringReader("<a x='1'>t</a>")); sr.next();
    XMLEventReader er = XMLInputFactory.newInstance().createXMLEventReader(sr);
    try { while (er.hasNext()) show(er.nextEvent()); } catch (Throwable t) { System.out.println("from stream " + fail(t)); }
    XMLEventReader fr = XMLInputFactory.newInstance().createFilteredReader(XMLInputFactory.newInstance().createXMLEventReader(new StringReader("<a><b/>t</a>")), new EventFilter() { public boolean accept(XMLEvent e) { return e.isStartElement(); } });
    StringBuilder sb = new StringBuilder("filtered ev: "); while (fr.hasNext()) sb.append(S1.name(fr.nextEvent().getEventType())).append(' '); System.out.println(sb);
    for (String bad : new String[]{"<a>", "<a></b>", "", "<a x=1/>"}) { XMLEventReader br = XMLInputFactory.newInstance().createXMLEventReader(new StringReader(bad)); StringBuilder s2 = new StringBuilder("bad " + q(bad) + ": "); try { while (br.hasNext()) s2.append(S1.name(br.nextEvent().getEventType())).append(' '); } catch (Throwable t) { s2.append(fail(t)); } System.out.println(s2); }
  } }

static class S4 {
  static String q(Object s) { return s == null ? "null" : s instanceof String ? "'" + ((String) s).replace("\n", "\\n") + "'" : String.valueOf(s); }
  static String fail(Throwable t) { return "!" + t.getClass().getName() + ":" + String.valueOf(t.getMessage()).replace("\n", "\\n"); }
  static void show(String label, XMLEvent e) {
    StringWriter w = new StringWriter(); try { e.writeAsEncodedUnicode(w); } catch (Throwable t) { w.write(fail(t)); }
    StringBuilder sb = new StringBuilder(label + " => type=" + S1.name(e.getEventType()) + " toString=" + q(e.toString()) + " written=" + q(w.toString()));
    if (e instanceof StartElement) { StartElement se = (StartElement) e; sb.append(" name=" + se.getName() + "/" + se.getName().getPrefix() + " attrs="); for (Iterator<Attribute> it = se.getAttributes(); it.hasNext();) sb.append(it.next() + ";"); sb.append(" ns="); for (Iterator<Namespace> it = se.getNamespaces(); it.hasNext();) sb.append(it.next() + ";"); sb.append(" ctx=" + (se.getNamespaceContext() == null ? "null" : se.getNamespaceContext().getNamespaceURI("p"))); }
    if (e instanceof EndElement) { EndElement ee = (EndElement) e; sb.append(" name=" + ee.getName() + "/" + ee.getName().getPrefix() + " ns="); for (Iterator<Namespace> it = ee.getNamespaces(); it.hasNext();) sb.append(it.next() + ";"); }
    if (e instanceof Characters) { Characters c = (Characters) e; sb.append(" data=" + q(c.getData()) + " ws=" + c.isWhiteSpace() + " cdata=" + c.isCData() + " ign=" + c.isIgnorableWhiteSpace() + " isChars=" + e.isCharacters()); }
    if (e instanceof Attribute) { Attribute a = (Attribute) e; sb.append(" aname=" + a.getName() + " value=" + q(a.getValue()) + " dtd=" + a.getDTDType() + " spec=" + a.isSpecified() + " isAttr=" + e.isAttribute() + " isNs=" + e.isNamespace()); }
    if (e instanceof Namespace) { Namespace n = (Namespace) e; sb.append(" prefix=" + q(n.getPrefix()) + " uri=" + q(n.getNamespaceURI()) + " def=" + n.isDefaultNamespaceDeclaration()); }
    if (e instanceof StartDocument) { StartDocument d = (StartDocument) e; sb.append(" sys=" + q(d.getSystemId()) + " enc=" + q(d.getCharacterEncodingScheme()) + " encSet=" + d.encodingSet() + " sa=" + d.isStandalone() + " saSet=" + d.standaloneSet() + " ver=" + q(d.getVersion())); }
    if (e instanceof DTD) { DTD d = (DTD) e; sb.append(" dtd=" + q(d.getDocumentTypeDeclaration()) + " ents=" + d.getEntities() + " nots=" + d.getNotations()); }
    if (e instanceof EntityReference) { EntityReference r = (EntityReference) e; sb.append(" name=" + r.getName() + " decl=" + (r.getDeclaration() == null ? "null" : r.getDeclaration().getName() + "/" + r.getDeclaration().getReplacementText())); }
    if (e instanceof Comment) sb.append(" text=" + q(((Comment) e).getText()));
    if (e instanceof ProcessingInstruction) sb.append(" target=" + q(((ProcessingInstruction) e).getTarget()) + " data=" + q(((ProcessingInstruction) e).getData()));
    System.out.println(sb);
  }
  interface C { XMLEvent make(XMLEventFactory f) throws Exception; }
  static void t(String label, C c) { XMLEventFactory f = XMLEventFactory.newInstance(); try { show(label, c.make(f)); } catch (Throwable t) { System.out.println(label + " => " + fail(t)); } }
  public static void run() throws Exception {
    t("attr 4", f -> f.createAttribute("p", "u", "x", "1")); t("attr 4 noprefix", f -> f.createAttribute("", "u", "x", "1")); t("attr 2", f -> f.createAttribute("x", "1")); t("attr qname", f -> f.createAttribute(new QName("u", "x", "p"), "1")); t("attr null value", f -> f.createAttribute("x", null)); t("attr null name", f -> f.createAttribute((String) null, "1"));     t("ns uri", f -> f.createNamespace("u")); t("ns 2", f -> f.createNamespace("p", "u")); t("ns empty prefix", f -> f.createNamespace("", "u")); t("ns null prefix", f -> f.createNamespace(null, "u")); t("ns null uri", f -> f.createNamespace("p", null)); t("ns uri null", f -> f.createNamespace((String) null));
    t("start qname", f -> f.createStartElement(new QName("u", "a", "p"), Arrays.asList((Attribute) f.createAttribute("x", "1")).iterator(), Arrays.asList((Namespace) f.createNamespace("p", "u")).iterator()));
    t("start 3", f -> f.createStartElement("p", "u", "a")); t("start 3 noprefix", f -> f.createStartElement("", "", "a")); t("start 3 nulls", f -> f.createStartElement(null, null, "a"));
    t("start 5", f -> f.createStartElement("p", "u", "a", Arrays.asList((Attribute) f.createAttribute("x", "1")).iterator(), Arrays.asList((Namespace) f.createNamespace("p", "u")).iterator()));
    t("start 6", f -> f.createStartElement("p", "u", "a", null, null, new NamespaceContext() { public String getNamespaceURI(String p) { return "ctx-" + p; } public String getPrefix(String u) { return null; } public Iterator<String> getPrefixes(String u) { return null; } }));
    t("start null attrs", f -> f.createStartElement(new QName("a"), null, null));
    t("end qname", f -> f.createEndElement(new QName("u", "a", "p"), Arrays.asList((Namespace) null).iterator())); t("end qname null", f -> f.createEndElement(new QName("u", "a", "p"), null));
    t("end 3", f -> f.createEndElement("p", "u", "a")); t("end 4", f -> f.createEndElement("p", "u", "a", Arrays.asList((Namespace) f.createNamespace("p", "u")).iterator()));
    t("chars", f -> f.createCharacters("a<&>b")); t("chars null", f -> f.createCharacters(null)); t("cdata", f -> f.createCData("a<&>b")); t("space", f -> f.createSpace("  ")); t("ignorable", f -> f.createIgnorableSpace("  ")); t("chars ws", f -> f.createCharacters(" \t\n")); t("chars empty", f -> f.createCharacters(""));
    t("startdoc", f -> f.createStartDocument()); t("startdoc 3", f -> f.createStartDocument("ISO-8859-1", "1.1", true)); t("startdoc 3 false", f -> f.createStartDocument("UTF-8", "1.0", false)); t("startdoc 2", f -> f.createStartDocument("UTF-8", "1.0")); t("startdoc 1", f -> f.createStartDocument("UTF-16")); t("startdoc nulls", f -> f.createStartDocument(null, null));
    t("enddoc", f -> f.createEndDocument()); t("comment", f -> f.createComment("c")); t("comment null", f -> f.createComment(null)); t("pi", f -> f.createProcessingInstruction("t", "d")); t("pi null data", f -> f.createProcessingInstruction("t", null)); t("pi null", f -> f.createProcessingInstruction(null, null)); t("dtd", f -> f.createDTD("<!DOCTYPE a>")); t("dtd null", f -> f.createDTD(null));
    XMLEventFactory f = XMLEventFactory.newInstance(); f.setLocation(new Location() { public int getLineNumber() { return 7; } public int getColumnNumber() { return 8; } public int getCharacterOffset() { return 9; } public String getPublicId() { return "pub"; } public String getSystemId() { return "sys"; } });
    XMLEvent e = f.createComment("x"); System.out.println("with location: " + e.getLocation().getLineNumber() + ":" + e.getLocation().getColumnNumber() + "/" + e.getLocation().getCharacterOffset() + " " + e.getLocation().getSystemId());
    e = f.createStartElement("p", "u", "a"); System.out.println("with location2: " + e.getLocation().getLineNumber());
    StringWriter sw = new StringWriter(); XMLEventWriter ew = XMLOutputFactory.newInstance().createXMLEventWriter(sw);
    XMLEventFactory ef = XMLEventFactory.newInstance();
    try { ew.add(ef.createStartDocument()); ew.add(ef.createStartElement("p", "u", "a")); ew.add(ef.createNamespace("p", "u")); ew.add(ef.createAttribute("x", "<1>")); ew.add(ef.createCharacters("t&")); ew.add(ef.createCData("cd")); ew.add(ef.createComment("c")); ew.add(ef.createProcessingInstruction("t", "d")); ew.add(ef.createEntityReference("e", null)); ew.add(ef.createSpace(" ")); ew.add(ef.createEndElement("p", "u", "a")); ew.add(ef.createEndDocument()); ew.flush(); } catch (Throwable t) { System.out.println("ew " + fail(t)); }
    System.out.println("event writer: " + sw);
    sw = new StringWriter(); ew = XMLOutputFactory.newInstance().createXMLEventWriter(sw);
    try { ew.add(XMLInputFactory.newInstance().createXMLEventReader(new StringReader("<?xml version='1.0' encoding='UTF-8' standalone='yes'?><!DOCTYPE a [<!ENTITY e 'v'>]><a xmlns:p='u' x='1'><p:b p:y='2'>t&e;<![CDATA[c]]></p:b><!--c--><?pi d?><c/></a>"))); ew.flush(); } catch (Throwable t) { System.out.println("ew2 " + fail(t)); }
    System.out.println("event writer 2: " + sw);
    try { ew.add((XMLEventReader) null); } catch (Throwable t) { System.out.println("ew null reader " + fail(t)); }
    ew.setPrefix("p", "u"); System.out.println("ew prefix " + ew.getPrefix("u") + " " + ew.getNamespaceContext().getNamespaceURI("p"));
  } }

static class S5 {
  interface T { void run(XMLStreamWriter w) throws Exception; }
  static String fail(Throwable t) { return "!" + t.getClass().getName() + ":" + String.valueOf(t.getMessage()).replace("\n", "\\n"); }
  static int n;
  static void t(String name, T t) { t(name, false, t); }
  static void t(String name, boolean repairing, T t) {
    StringWriter sw = new StringWriter(); String res;
    try { XMLOutputFactory f = XMLOutputFactory.newInstance(); if (repairing) f.setProperty(XMLOutputFactory.IS_REPAIRING_NAMESPACES, true); XMLStreamWriter w = f.createXMLStreamWriter(sw); try { t.run(w); w.flush(); res = "ok"; } catch (Throwable x) { res = fail(x); } try { w.flush(); } catch (Throwable x) { } }
    catch (Throwable x) { res = "create" + fail(x); }
    System.out.println(String.format("%-28s %s | %s", name, res, sw.toString().replace("\n", "\\n")));
  }
  public static void run() throws Exception {
    t("empty", w -> {});
    t("startDoc", w -> w.writeStartDocument());
    t("startDoc ver", w -> w.writeStartDocument("1.1"));
    t("startDoc enc ver", w -> w.writeStartDocument("UTF-8", "1.0"));
    t("startDoc enc ver 2", w -> w.writeStartDocument("ISO-8859-1", "1.0"));
    t("startDoc null", w -> w.writeStartDocument(null));
    t("startDoc null null", w -> w.writeStartDocument(null, null));
    t("startDoc twice", w -> { w.writeStartDocument(); w.writeStartDocument(); });
    t("endDoc", w -> w.writeEndDocument());
    t("endDoc after", w -> { w.writeStartDocument(); w.writeStartElement("a"); w.writeStartElement("b"); w.writeEndDocument(); });
    t("simple", w -> { w.writeStartElement("a"); w.writeCharacters("t"); w.writeEndElement(); });
    t("open only", w -> { w.writeStartElement("a"); });
    t("open attr", w -> { w.writeStartElement("a"); w.writeAttribute("x", "1"); });
    t("attr quoting", w -> { w.writeStartElement("a"); w.writeAttribute("x", "<&>\"'\n\t"); w.writeEndElement(); });
    t("text escaping", w -> { w.writeStartElement("a"); w.writeCharacters("<&>\"'\n\t]]>"); w.writeEndElement(); });
    t("chars array", w -> { w.writeStartElement("a"); w.writeCharacters("xabcx".toCharArray(), 1, 3); w.writeEndElement(); });
    t("chars null", w -> { w.writeStartElement("a"); w.writeCharacters((String) null); w.writeEndElement(); });
    t("chars empty", w -> { w.writeStartElement("a"); w.writeCharacters(""); w.writeEndElement(); });
    t("empty element", w -> { w.writeEmptyElement("a"); });
    t("empty element attr", w -> { w.writeEmptyElement("a"); w.writeAttribute("x", "1"); });
    t("empty then text", w -> { w.writeStartElement("r"); w.writeEmptyElement("a"); w.writeCharacters("t"); w.writeEndElement(); });
    t("nested", w -> { w.writeStartElement("a"); w.writeStartElement("b"); w.writeEndElement(); w.writeEndElement(); });
    t("end without start", w -> w.writeEndElement());
    t("attr no start", w -> w.writeAttribute("x", "1"));
    t("attr after content", w -> { w.writeStartElement("a"); w.writeCharacters("t"); w.writeAttribute("x", "1"); });
    t("attr ns", w -> { w.writeStartElement("a"); w.writeAttribute("p", "u", "x", "1"); });
    t("attr ns2", w -> { w.writeStartElement("a"); w.writeNamespace("p", "u"); w.writeAttribute("p", "u", "x", "1"); w.writeEndElement(); });
    t("attr ns3", w -> { w.writeStartElement("a"); w.writeAttribute("u", "x", "1"); });
    t("ns decl", w -> { w.writeStartElement("a"); w.writeNamespace("p", "u"); w.writeDefaultNamespace("d"); w.writeEndElement(); });
    t("ns decl xmlns", w -> { w.writeStartElement("a"); w.writeNamespace("xmlns", "u"); w.writeNamespace(null, "u"); w.writeNamespace("", "v"); w.writeEndElement(); });
    t("start ns", w -> { w.writeStartElement("p", "a", "u"); w.writeNamespace("p", "u"); w.writeEndElement(); });
    t("start ns noDecl", w -> { w.writeStartElement("p", "a", "u"); w.writeEndElement(); });
    t("start ns 2", w -> { w.writeStartElement("u", "a"); w.writeEndElement(); });
    t("start ns prefix set", w -> { w.setPrefix("p", "u"); w.writeStartElement("u", "a"); w.writeEndElement(); });
    t("start ns default", w -> { w.setDefaultNamespace("d"); w.writeStartElement("d", "a"); w.writeEndElement(); });
    t("start ns default 2", w -> { w.writeStartElement("a"); w.setDefaultNamespace("d"); w.writeDefaultNamespace("d"); w.writeStartElement("d", "b"); w.writeEndElement(); w.writeEndElement(); });
    t("setPrefix ctx", w -> { w.writeStartElement("a"); w.setPrefix("p", "u"); w.writeStartElement("p", "b", "u"); w.writeEndElement(); w.writeEndElement(); System.out.print(w.getPrefix("u") + "|" + w.getNamespaceContext().getNamespaceURI("p") + "|"); });
    t("getPrefix", w -> { w.writeStartElement("a"); w.writeNamespace("p", "u"); System.out.print(w.getPrefix("u") + "|" + w.getPrefix("zz") + "|" + w.getNamespaceContext().getPrefix("u") + "|" + w.getNamespaceContext().getNamespaceURI("p") + "|"); });
    t("getPrefix null", w -> { w.getPrefix(null); });
    t("setPrefix null", w -> { w.setPrefix(null, "u"); });
    t("setPrefix null2", w -> { w.setPrefix("p", null); });
    t("setDefaultNamespace null", w -> { w.setDefaultNamespace(null); });
    t("comment", w -> { w.writeComment("c"); w.writeStartElement("a"); w.writeComment("c2"); w.writeEndElement(); });
    t("comment null", w -> { w.writeComment(null); });
    t("comment dash", w -> { w.writeComment("a--b"); });
    t("pi", w -> { w.writeProcessingInstruction("t"); w.writeProcessingInstruction("t", "d"); });
    t("pi null", w -> { w.writeProcessingInstruction(null); });
    t("pi xml", w -> { w.writeProcessingInstruction("xml", "d"); });
    t("cdata", w -> { w.writeStartElement("a"); w.writeCData("<x>"); w.writeCData("a]]>b"); w.writeEndElement(); });
    t("cdata null", w -> { w.writeStartElement("a"); w.writeCData(null); w.writeEndElement(); });
    t("dtd", w -> { w.writeDTD("<!DOCTYPE a>"); w.writeStartElement("a"); w.writeEndElement(); });
    t("entityRef", w -> { w.writeStartElement("a"); w.writeEntityRef("e"); w.writeEndElement(); });
    t("entityRef null", w -> { w.writeStartElement("a"); w.writeEntityRef(null); w.writeEndElement(); });
    t("full doc", w -> { w.writeStartDocument("UTF-8", "1.0"); w.writeStartElement("a"); w.writeAttribute("x", "1"); w.writeStartElement("b"); w.writeCharacters("t"); w.writeEndElement(); w.writeEmptyElement("c"); w.writeEndElement(); w.writeEndDocument(); });
    t("unicode", w -> { w.writeStartElement("a"); w.writeCharacters("é中😀"); w.writeAttribute("x", "é"); w.writeEndElement(); });
    t("control", w -> { w.writeStartElement("a"); w.writeCharacters("a\u0001b"); w.writeEndElement(); });
    t("bad names", w -> { w.writeStartElement("1a"); w.writeEndElement(); });
    t("empty name", w -> { w.writeStartElement(""); w.writeEndElement(); });
    t("null name", w -> { w.writeStartElement((String) null); });
    t("attr null name", w -> { w.writeStartElement("a"); w.writeAttribute(null, "1"); });
    t("attr null value", w -> { w.writeStartElement("a"); w.writeAttribute("x", null); w.writeEndElement(); });
    t("flush close", w -> { w.writeStartElement("a"); w.close(); });
    t("write after close", w -> { w.close(); w.writeStartElement("a"); });
    t("getProperty", w -> { System.out.print(w.getProperty("javax.xml.stream.isRepairingNamespaces") + "|"); });
    t("getProperty x", w -> { w.getProperty("x"); });
    t("getProperty null", w -> { w.getProperty(null); });
    t("R simple", true, w -> { w.writeStartElement("a"); w.writeCharacters("t"); w.writeEndElement(); });
    t("R ns elem", true, w -> { w.writeStartElement("p", "a", "u"); w.writeEndElement(); });
    t("R ns elem noprefix", true, w -> { w.writeStartElement("", "a", "u"); w.writeEndElement(); });
    t("R ns attr", true, w -> { w.writeStartElement("a"); w.writeAttribute("p", "u", "x", "1"); w.writeEndElement(); });
    t("R explicit ns", true, w -> { w.writeStartElement("p", "a", "u"); w.writeNamespace("p", "u"); w.writeEndElement(); });
    t("R explicit default", true, w -> { w.writeStartElement("", "a", "d"); w.writeDefaultNamespace("d"); w.writeEndElement(); });
    t("R nested same", true, w -> { w.writeStartElement("p", "a", "u"); w.writeStartElement("p", "b", "u"); w.writeEndElement(); w.writeEndElement(); });
    t("R nested diff", true, w -> { w.writeStartElement("p", "a", "u"); w.writeStartElement("p", "b", "v"); w.writeEndElement(); w.writeEndElement(); });
    t("R prefix set", true, w -> { w.setPrefix("q", "u"); w.writeStartElement("u", "a"); w.writeEndElement(); });
    t("R two attrs", true, w -> { w.writeStartElement("a"); w.writeAttribute("p", "u", "x", "1"); w.writeAttribute("q", "u", "y", "2"); w.writeEndElement(); });
    XMLOutputFactory f = XMLOutputFactory.newInstance();
    for (String p : new String[]{XMLOutputFactory.IS_REPAIRING_NAMESPACES, "javax.xml.stream.isPrefixDefaulting", "escapeCharacters", "http://java.sun.com/xml/stream/properties/outputstream-encoding", "http://java.sun.com/xml/stream/properties/writestartdocument-always", "nope"}) { try { System.out.println("prop " + p + " " + f.isPropertySupported(p) + " " + f.getProperty(p)); } catch (Throwable x) { System.out.println("prop " + p + fail(x)); } }
    try { f.setProperty("nope", true); } catch (Throwable x) { System.out.println("set nope " + fail(x)); }
    try { f.setProperty(null, true); } catch (Throwable x) { System.out.println("set null " + fail(x)); }
    try { f.getProperty(null); } catch (Throwable x) { System.out.println("get null " + fail(x)); }
    try { f.setProperty(XMLOutputFactory.IS_REPAIRING_NAMESPACES, null); System.out.println("repair null " + f.getProperty(XMLOutputFactory.IS_REPAIRING_NAMESPACES)); } catch (Throwable x) { System.out.println("repair null " + fail(x)); }
    ByteArrayOutputStream bos = new ByteArrayOutputStream(); XMLStreamWriter w = f.createXMLStreamWriter(bos); w.writeStartDocument(); w.writeStartElement("a"); w.writeCharacters("é中😀"); w.writeEndElement(); w.writeEndDocument(); w.close(); System.out.println("utf8 bytes " + Arrays.toString(bos.toByteArray()));
    bos = new ByteArrayOutputStream(); w = f.createXMLStreamWriter(bos, "ISO-8859-1"); w.writeStartDocument("ISO-8859-1", "1.0"); w.writeStartElement("a"); w.writeAttribute("x", "中"); w.writeCharacters("é中😀"); w.writeEndElement(); w.writeEndDocument(); w.close(); System.out.println("latin1 " + bos.toString("ISO-8859-1"));
    bos = new ByteArrayOutputStream(); w = f.createXMLStreamWriter(bos, "UTF-16"); w.writeStartDocument(); w.writeStartElement("a"); w.writeEndElement(); w.close(); System.out.println("utf16 " + Arrays.toString(bos.toByteArray()));
    try { f.createXMLStreamWriter(new ByteArrayOutputStream(), "nope-enc"); } catch (Throwable x) { System.out.println("bad enc " + fail(x)); }
    try { f.createXMLStreamWriter((Writer) null); } catch (Throwable x) { System.out.println("null writer " + fail(x)); }
    try { f.createXMLStreamWriter((OutputStream) null); } catch (Throwable x) { System.out.println("null stream " + fail(x)); }
    try { f.createXMLStreamWriter((javax.xml.transform.Result) null); } catch (Throwable x) { System.out.println("null result " + x.getClass().getName()); }
    try { StringWriter sw2 = new StringWriter(); XMLStreamWriter w2 = f.createXMLStreamWriter(new javax.xml.transform.stream.StreamResult(sw2)); w2.writeEmptyElement("x"); w2.close(); System.out.println("result " + sw2); } catch (Throwable x) { System.out.println("streamresult " + fail(x)); }
    try { f.createXMLStreamWriter(new javax.xml.transform.stream.StreamResult()); } catch (Throwable x) { System.out.println("empty streamresult " + fail(x)); }
  } }

  public static void main(String[] args) throws Exception {
    Locale.setDefault(Locale.ENGLISH);
    S1.run();
    System.out.println("##### S2");
    S2.run();
    System.out.println("##### S3");
    S3.run();
    System.out.println("##### S4");
    S4.run();
    System.out.println("##### S5");
    S5.run();
  }
}
