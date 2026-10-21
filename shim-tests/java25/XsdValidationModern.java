/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 * Minimum release: 21
 */

import javax.xml.XMLConstants; import javax.xml.transform.stream.*; import javax.xml.validation.*; import java.io.*; import org.xml.sax.*; import java.util.*;
public class XsdValidationModern {
  public static void main(String[] args) {
    XsdPo.main2(args);
    XsdBuiltin.main2(args);
    XsdMore.main2(args);
    XsdOccurs.main2(args);
    XsdSources.main2(args);
  }
}

class XsdPo {
  static final String PO = "<xs:schema xmlns:xs='http://www.w3.org/2001/XMLSchema'>\n"
   + "<xs:element name='order'><xs:complexType><xs:sequence><xs:element name='id' type='xs:int'/>"
   + "<xs:element name='item' maxOccurs='unbounded'><xs:complexType><xs:sequence><xs:element name='name' type='xs:string'/><xs:element name='qty' type='qty'/></xs:sequence><xs:attribute name='sku' type='xs:string' use='required'/></xs:complexType></xs:element>"
   + "<xs:element name='note' type='xs:string' minOccurs='0'/></xs:sequence><xs:attribute name='status' type='status' default='new'/></xs:complexType></xs:element>"
   + "<xs:simpleType name='qty'><xs:restriction base='xs:positiveInteger'><xs:maxInclusive value='100'/></xs:restriction></xs:simpleType>"
   + "<xs:simpleType name='status'><xs:restriction base='xs:string'><xs:enumeration value='new'/><xs:enumeration value='shipped'/></xs:restriction></xs:simpleType></xs:schema>";
  static String run(String xsd, String xml, boolean all) {
    try {
      SchemaFactory f = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
      Schema s = f.newSchema(new StreamSource(new StringReader(xsd)));
      Validator v = s.newValidator();
      final List<String> errs = new ArrayList<>();
      if (all) { v.setErrorHandler(new ErrorHandler() { public void warning(SAXParseException e) { errs.add("W " + e.getMessage()); } public void error(SAXParseException e) { errs.add("E " + e.getLineNumber() + ":" + e.getColumnNumber() + " " + e.getMessage()); } public void fatalError(SAXParseException e) throws SAXException { errs.add("F " + e.getMessage()); throw e; } }); }
      v.validate(new StreamSource(new StringReader(xml)));
      return all ? (errs.isEmpty() ? "valid" : String.join(" || ", errs)) : "valid";
    } catch (SAXParseException e) { return "THROW " + e.getLineNumber() + ":" + e.getColumnNumber() + " " + e.getMessage(); }
    catch (Exception e) { return "EXC " + e.getClass().getSimpleName() + " " + e.getMessage(); }
  }
  static void main2(String[] a) { Locale.setDefault(Locale.ENGLISH);
    String[] xmls = {
      "<order status='new'><id>1</id><item sku='a'><name>n</name><qty>5</qty></item></order>",
      "<order><id>abc</id><item sku='a'><name>n</name><qty>5</qty></item></order>",
      "<order><id>1</id></order>",
      "<order><id>1</id><item sku='a'><name>n</name><qty>5</qty></item><extra/></order>",
      "<order><item sku='a'><name>n</name><qty>5</qty></item><id>1</id></order>",
      "<order><id>1</id><item sku='a'><name>n</name><qty>0</qty></item></order>",
      "<order><id>1</id><item sku='a'><name>n</name><qty>101</qty></item></order>",
      "<order status='bad'><id>1</id><item sku='a'><name>n</name><qty>5</qty></item></order>",
      "<order><id>1</id><item><name>n</name><qty>5</qty></item></order>",
      "<order><id>1</id><item sku='a' foo='b'><name>n</name><qty>5</qty></item></order>",
      "<order>text<id>1</id><item sku='a'><name>n</name><qty>5</qty></item></order>",
      "<other/>",
      "<order><id>1</id><item sku='a'><name>n</name></item></order>",
      "<order><id>1</id><item sku='a'><name>n</name><qty>5</qty></item><note>x</note><note>y</note></order>",
      "<order><id> 12 </id><item sku='a'><name/><qty>5</qty></item></order>",
      "<order><id>1</id><item sku='a'><name>n<b/></name><qty>5</qty></item></order>",
      "<order><id>99999999999</id><item sku='a'><name>n</name><qty>5</qty></item></order>"};
    for (int i = 0; i < xmls.length; i++) { System.out.println(i + " first: " + run(PO, xmls[i], false)); System.out.println(i + " all:   " + run(PO, xmls[i], true)); }
  }}

class XsdBuiltin {
  static String run(String xsd, String xml) {
    try {
      SchemaFactory f = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
      Schema s = f.newSchema(new StreamSource(new StringReader(xsd)));
      Validator v = s.newValidator();
      v.validate(new StreamSource(new StringReader(xml)));
      return "valid";
    } catch (SAXParseException e) { return e.getMessage(); }
    catch (Exception e) { return "EXC " + e.getClass().getSimpleName() + " " + e.getMessage(); }
  }
  static String xsd(String type) { return "<xs:schema xmlns:xs='http://www.w3.org/2001/XMLSchema'><xs:element name='a' type='xs:" + type + "'/></xs:schema>"; }
  static String xsdRaw(String simple) { return "<xs:schema xmlns:xs='http://www.w3.org/2001/XMLSchema'><xs:element name='a'><xs:simpleType>" + simple + "</xs:simpleType></xs:element></xs:schema>"; }
  static void main2(String[] a) {
    Locale.setDefault(Locale.ENGLISH);
    String[] types = {"string","normalizedString","token","boolean","decimal","integer","nonPositiveInteger","negativeInteger","long","int","short","byte","nonNegativeInteger","unsignedLong","unsignedInt","unsignedShort","unsignedByte","positiveInteger","float","double","date","dateTime","time","duration","gYear","gYearMonth","gMonth","gMonthDay","gDay","anyURI","QName","NCName","Name","NMTOKEN","NMTOKENS","language","ID","IDREF","base64Binary","hexBinary","anySimpleType","NOTATION","ENTITY"};
    for (String t : types) System.out.println(t + ": " + run(xsd(t), "<a>zz zz</a>"));
    System.out.println("--- facets");
    String[][] fac = {
      {"<xs:restriction base='xs:string'><xs:length value='3'/></xs:restriction>", "ab"},
      {"<xs:restriction base='xs:string'><xs:minLength value='3'/></xs:restriction>", "ab"},
      {"<xs:restriction base='xs:string'><xs:maxLength value='1'/></xs:restriction>", "ab"},
      {"<xs:restriction base='xs:string'><xs:pattern value='[a-c]+'/></xs:restriction>", "abz"},
      {"<xs:restriction base='xs:decimal'><xs:totalDigits value='3'/></xs:restriction>", "1234"},
      {"<xs:restriction base='xs:decimal'><xs:fractionDigits value='1'/></xs:restriction>", "1.25"},
      {"<xs:restriction base='xs:int'><xs:minExclusive value='5'/></xs:restriction>", "5"},
      {"<xs:restriction base='xs:int'><xs:maxExclusive value='5'/></xs:restriction>", "5"},
      {"<xs:restriction base='xs:int'><xs:minInclusive value='5'/><xs:maxInclusive value='9'/></xs:restriction>", "10"},
      {"<xs:list itemType='xs:int'/>", "1 x 3"},
      {"<xs:union memberTypes='xs:int xs:boolean'/>", "zz"},
      {"<xs:restriction base='xs:string'><xs:enumeration value='a'/><xs:enumeration value='b'/></xs:restriction>", "c"},
      {"<xs:restriction base='xs:string'><xs:whiteSpace value='collapse'/><xs:enumeration value='a b'/></xs:restriction>", "  a   b "},
      {"<xs:restriction base='xs:string'><xs:minLength value='1'/></xs:restriction>", ""},
      {"<xs:restriction base='xs:int'/>", "1.5"},
      {"<xs:restriction base='xs:boolean'/>", "yes"},
      {"<xs:restriction base='xs:date'/>", "2020-13-45"},
      {"<xs:restriction base='xs:dateTime'/>", "2020-01-01"},
      {"<xs:restriction base='xs:double'/>", "1.5e"},
    };
    for (String[] f : fac) System.out.println(f[0].replace("xs:","") + " | '" + f[1] + "' => " + run(xsdRaw(f[0]), "<a>" + f[1] + "</a>"));
  }}

class XsdMore {
  static String run(String xsd, String xml, boolean all) {
    try {
      SchemaFactory f = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
      Schema s = f.newSchema(new StreamSource(new StringReader(xsd)));
      Validator v = s.newValidator();
      final List<String> errs = new ArrayList<>();
      if (all) { v.setErrorHandler(new ErrorHandler() { public void warning(SAXParseException e) { errs.add("W " + e.getMessage()); } public void error(SAXParseException e) { errs.add("E " + e.getLineNumber() + ":" + e.getColumnNumber() + " " + e.getMessage()); } public void fatalError(SAXParseException e) throws SAXException { errs.add("F " + e.getMessage()); throw e; } }); }
      v.validate(new StreamSource(new StringReader(xml)));
      return all ? (errs.isEmpty() ? "valid" : String.join(" || ", errs)) : "valid";
    } catch (SAXParseException e) { return "THROW " + e.getLineNumber() + ":" + e.getColumnNumber() + " " + e.getMessage(); }
    catch (Exception e) { return "EXC " + e.getClass().getSimpleName(); }
  }
  static void t(String name, String xsd, String... xmls) {
    for (int i = 0; i < xmls.length; i++) System.out.println(name + "#" + i + " " + run(xsd, xmls[i], true));
  }
  static final String XS = "xmlns:xs='http://www.w3.org/2001/XMLSchema'";
  static void main2(String[] a) {
    t("choice", "<xs:schema " + XS + "><xs:element name='r'><xs:complexType><xs:choice maxOccurs='3'><xs:element name='a' type='xs:string'/><xs:element name='b' type='xs:int'/></xs:choice></xs:complexType></xs:element></xs:schema>",
        "<r><a>x</a><b>1</b><a/></r>", "<r/>", "<r><a/><a/><a/><a/></r>", "<r><c/></r>", "<r><b>z</b></r>");
    t("all", "<xs:schema " + XS + "><xs:element name='r'><xs:complexType><xs:all><xs:element name='a' type='xs:string'/><xs:element name='b' type='xs:string' minOccurs='0'/></xs:all></xs:complexType></xs:element></xs:schema>",
        "<r><b/><a/></r>", "<r><a/></r>", "<r><b/></r>", "<r><a/><a/></r>");
    t("occurs", "<xs:schema " + XS + "><xs:element name='r'><xs:complexType><xs:sequence><xs:element name='a' minOccurs='2' maxOccurs='3' type='xs:string'/><xs:element name='b' minOccurs='0' type='xs:string'/></xs:sequence></xs:complexType></xs:element></xs:schema>",
        "<r><a/></r>", "<r><a/><a/></r>", "<r><a/><a/><a/><a/></r>", "<r><a/><a/><b/><b/></r>");
    t("attrs", "<xs:schema " + XS + "><xs:element name='r'><xs:complexType><xs:attribute name='n' type='xs:int' use='required'/><xs:attribute name='f' type='xs:string' fixed='ok'/><xs:attribute name='p' type='xs:string' use='prohibited'/></xs:complexType></xs:element></xs:schema>",
        "<r n='1'/>", "<r n='x'/>", "<r n='1' f='no'/>", "<r n='1' p='z'/>", "<r/>", "<r n='1' f='ok'/>");
    t("simpleContent", "<xs:schema " + XS + "><xs:element name='r'><xs:complexType><xs:simpleContent><xs:extension base='xs:int'><xs:attribute name='u' type='xs:string'/></xs:extension></xs:simpleContent></xs:complexType></xs:element></xs:schema>",
        "<r u='a'>5</r>", "<r>x</r>", "<r><a/></r>", "<r/>");
    t("extension", "<xs:schema " + XS + "><xs:complexType name='base'><xs:sequence><xs:element name='a' type='xs:string'/></xs:sequence><xs:attribute name='x' type='xs:string'/></xs:complexType><xs:element name='r'><xs:complexType><xs:complexContent><xs:extension base='base'><xs:sequence><xs:element name='b' type='xs:int'/></xs:sequence><xs:attribute name='y' type='xs:string'/></xs:extension></xs:complexContent></xs:complexType></xs:element></xs:schema>",
        "<r x='1' y='2'><a/><b>3</b></r>", "<r><a/></r>", "<r><b>1</b></r>");
    t("mixed-empty", "<xs:schema " + XS + "><xs:element name='m'><xs:complexType mixed='true'><xs:sequence><xs:element name='i' minOccurs='0' maxOccurs='unbounded' type='xs:string'/></xs:sequence></xs:complexType></xs:element><xs:element name='e'><xs:complexType/></xs:element></xs:schema>",
        "<m>text<i/>more</m>", "<m><z/></m>");
    t("empty", "<xs:schema " + XS + "><xs:element name='e'><xs:complexType/></xs:element></xs:schema>", "<e/>", "<e>t</e>", "<e><a/></e>", "<e> </e>");
    t("ref-group", "<xs:schema " + XS + "><xs:element name='r'><xs:complexType><xs:sequence><xs:group ref='g'/><xs:element ref='tail'/></xs:sequence></xs:complexType></xs:element><xs:group name='g'><xs:sequence><xs:element name='h1' type='xs:string'/><xs:element name='h2' type='xs:string'/></xs:sequence></xs:group><xs:element name='tail' type='xs:boolean'/></xs:schema>",
        "<r><h1/><h2/><tail>true</tail></r>", "<r><h1/><tail>true</tail></r>", "<r><h1/><h2/><tail>maybe</tail></r>");
    t("namespaces", "<xs:schema " + XS + " targetNamespace='urn:t' xmlns='urn:t' elementFormDefault='qualified'><xs:element name='r'><xs:complexType><xs:sequence><xs:element name='a' type='xs:string'/></xs:sequence></xs:complexType></xs:element></xs:schema>",
        "<r xmlns='urn:t'><a/></r>", "<r><a/></r>", "<p:r xmlns:p='urn:t'><p:a/></p:r>", "<r xmlns='urn:t'><b/></r>", "<r xmlns='urn:t'/>");
    t("unqualified-locals", "<xs:schema " + XS + " targetNamespace='urn:t' xmlns:t='urn:t'><xs:element name='r'><xs:complexType><xs:sequence><xs:element name='a' type='xs:string'/></xs:sequence></xs:complexType></xs:element></xs:schema>",
        "<t:r xmlns:t='urn:t'><a/></t:r>", "<t:r xmlns:t='urn:t'><t:a/></t:r>");
    t("nillable", "<xs:schema " + XS + "><xs:element name='r'><xs:complexType><xs:sequence><xs:element name='n' type='xs:int' nillable='true'/><xs:element name='m' type='xs:int'/></xs:sequence></xs:complexType></xs:element></xs:schema>",
        "<r xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'><n xsi:nil='true'/><m>1</m></r>", "<r xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'><n>1</n><m xsi:nil='true'/></r>", "<r xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'><n xsi:nil='true'>5</n><m>1</m></r>");
    t("ids", "<xs:schema " + XS + "><xs:element name='r'><xs:complexType><xs:sequence><xs:element name='i' maxOccurs='unbounded'><xs:complexType><xs:attribute name='id' type='xs:ID'/><xs:attribute name='ref' type='xs:IDREF'/></xs:complexType></xs:element></xs:sequence></xs:complexType></xs:element></xs:schema>",
        "<r><i id='a'/><i id='b' ref='a'/></r>", "<r><i id='a'/><i id='a'/></r>", "<r><i ref='zz'/></r>");
    t("any", "<xs:schema " + XS + "><xs:element name='r'><xs:complexType><xs:sequence><xs:any processContents='lax' minOccurs='0' maxOccurs='unbounded'/></xs:sequence></xs:complexType></xs:element><xs:element name='known' type='xs:int'/></xs:schema>",
        "<r><x/><y>1</y></r>", "<r><known>5</known></r>", "<r><known>no</known></r>");
    t("subst", "<xs:schema " + XS + "><xs:element name='r'><xs:complexType><xs:sequence><xs:element ref='head'/></xs:sequence></xs:complexType></xs:element><xs:element name='head' type='xs:string'/><xs:element name='alt' substitutionGroup='head' type='xs:string'/></xs:schema>",
        "<r><head/></r>", "<r><alt/></r>", "<r><other/></r>");
    t("default-value", "<xs:schema " + XS + "><xs:element name='r'><xs:complexType><xs:sequence><xs:element name='a' type='xs:int' default='7'/><xs:element name='b' type='xs:string' fixed='k'/></xs:sequence></xs:complexType></xs:element></xs:schema>",
        "<r><a/><b>k</b></r>", "<r><a>3</a><b/></r>", "<r><a>3</a><b>z</b></r>");
    t("union-list", "<xs:schema " + XS + "><xs:element name='r'><xs:complexType><xs:sequence><xs:element name='l' type='L'/><xs:element name='u' type='U'/></xs:sequence></xs:complexType></xs:element><xs:simpleType name='L'><xs:list itemType='xs:int'/></xs:simpleType><xs:simpleType name='U'><xs:union memberTypes='xs:int xs:boolean'/></xs:simpleType></xs:schema>",
        "<r><l>1 2 3</l><u>true</u></r>", "<r><l>1 b</l><u>5</u></r>", "<r><l/><u>nope</u></r>");
    System.out.println("bad schema: " + run("<xs:schema " + XS + "><xs:element name='a' type='nosuch'/></xs:schema>", "<a/>", false));
    System.out.println("not a schema: " + run("<foo/>", "<a/>", false));
    System.out.println("malformed doc: " + run("<xs:schema " + XS + "><xs:element name='a' type='xs:string'/></xs:schema>", "<a>", false));
  }
}

class XsdOccurs {
  static String run(String xsd, String xml) {
    try { Schema s = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI).newSchema(new StreamSource(new StringReader(xsd))); s.newValidator().validate(new StreamSource(new StringReader(xml))); return "valid"; }
    catch (SAXParseException e) { return e.getLineNumber() + ":" + e.getColumnNumber() + " " + e.getMessage(); } catch (Exception e) { return "EXC " + e; }
  }
  static void main2(String[] x) {
    Locale.setDefault(Locale.ENGLISH);
    String xs = "xmlns:xs='http://www.w3.org/2001/XMLSchema'";
    String[][] cases = {
      {"<xs:element name='a' minOccurs='3' maxOccurs='5' type='xs:string'/><xs:element name='b' minOccurs='0' type='xs:string'/>", "<r><a/></r>", "<r><a/><a/></r>", "<r><a/><c/></r>", "<r><a/><a/><a/><a/><a/><a/></r>", "<r><a/><a/><a/><a/><a/><b/><b/></r>", "<r><a/><a/><a/><b/><a/></r>"},
      {"<xs:element name='a' minOccurs='2' maxOccurs='unbounded' type='xs:string'/>", "<r><a/></r>", "<r/>", "<r><a/><a/><a/></r>"},
      {"<xs:element name='a' maxOccurs='2' type='xs:string'/>", "<r><a/><a/><a/></r>", "<r/>"},
      {"<xs:sequence minOccurs='2' maxOccurs='2'><xs:element name='a' type='xs:string'/><xs:element name='b' type='xs:string'/></xs:sequence>", "<r><a/><b/></r>", "<r><a/><b/><a/></r>", "<r><a/><b/><a/><b/><a/></r>"},
      {"<xs:choice minOccurs='2' maxOccurs='3'><xs:element name='a' type='xs:string'/><xs:element name='b' type='xs:string'/></xs:choice>", "<r><a/></r>", "<r><a/><b/><a/><b/></r>"},
    };
    for (String[] c : cases) {
      String xsd = "<xs:schema " + xs + "><xs:element name='r'><xs:complexType><xs:sequence>" + c[0] + "</xs:sequence></xs:complexType></xs:element></xs:schema>";
      for (int i = 1; i < c.length; i++) System.out.println(c[0].substring(0, Math.min(60, c[0].length())) + " | " + c[i] + " => " + run(xsd, c[i]));
    }
  }}

class XsdSources {
  static void main2(String[] a) {
    String xsd = "<xs:schema xmlns:xs='http://www.w3.org/2001/XMLSchema'><xs:element name='r'><xs:complexType><xs:sequence><xs:element name='n' type='xs:int'/></xs:sequence></xs:complexType></xs:element></xs:schema>";
    try {
      Schema s = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI).newSchema(new StreamSource(new StringReader(xsd)));
      for (String xml : new String[] {"<r><n>1</n></r>", "<r><n>x</n></r>", "<r/>"}) {
        javax.xml.parsers.DocumentBuilderFactory f = javax.xml.parsers.DocumentBuilderFactory.newInstance();
        f.setNamespaceAware(true);
        org.w3c.dom.Document d = f.newDocumentBuilder().parse(new InputSource(new StringReader(xml)));
        try {
          s.newValidator().validate(new javax.xml.transform.dom.DOMSource(d));
          System.out.println("dom valid");
        } catch (SAXParseException e) {
          System.out.println("dom " + e.getMessage());
        }
        try {
          ValidatorHandler h = s.newValidatorHandler();
          javax.xml.parsers.SAXParserFactory spf = javax.xml.parsers.SAXParserFactory.newInstance();
          spf.setNamespaceAware(true);
          org.xml.sax.XMLReader r = spf.newSAXParser().getXMLReader();
          r.setContentHandler(h);
          r.parse(new InputSource(new StringReader(xml)));
          System.out.println("handler valid");
        } catch (SAXParseException e) {
          System.out.println("handler " + e.getMessage());
        }
        try {
          StringWriter w = new StringWriter();
          s.newValidator().validate(new StreamSource(new StringReader(xml)), new javax.xml.transform.stream.StreamResult(w));
          System.out.println("result valid " + w.toString().replace("\n", "").length());
        } catch (SAXParseException e) {
          System.out.println("result " + e.getMessage());
        }
      }
    } catch (Exception e) {
      System.out.println("EXC " + e);
    }
  }
}
