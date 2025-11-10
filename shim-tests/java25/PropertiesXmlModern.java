/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 * Minimum release: 21
 */

import java.io.*;
import java.nio.charset.*;
import java.util.*;
import java.util.concurrent.Callable;
public class PropertiesXmlModern {
  static void t(String n, Callable<Object> c) { try { System.out.println(n + ": " + c.call()); } catch (Throwable e) { System.out.println(n + ": !" + e.getClass().getName() + ":" + (e instanceof NullPointerException ? null : e.getMessage())); } }
  static String store(Properties p, String c) throws Exception { ByteArrayOutputStream o = new ByteArrayOutputStream(); p.storeToXML(o, c); return o.toString("UTF-8"); }
  public static void main(String[] a) throws Exception {
    Locale.setDefault(Locale.ENGLISH);
    PropertiesXmlModern2.run();
    Properties p = new Properties(); p.setProperty("a", "1");
    t("basic", () -> store(p, null));
    t("comment", () -> store(p, "my <comment> & \"quotes\""));
    Properties q = new Properties(); q.setProperty("k<ey", "v&al\"ue'x>y"); q.setProperty("multi", "line1\nline2\ttab"); q.setProperty("uni", "é€😀");
    t("escape", () -> { String s = store(q, "c"); return s.replace("\n", "|"); });
    t("empty", () -> store(new Properties(), null));
    t("charset", () -> { ByteArrayOutputStream o = new ByteArrayOutputStream(); p.storeToXML(o, "x", StandardCharsets.ISO_8859_1); return o.toString("ISO-8859-1").replace("\n", "|"); });
    t("charsetName", () -> { ByteArrayOutputStream o = new ByteArrayOutputStream(); p.storeToXML(o, "x", "UTF-16"); return o.size(); });
    t("charsetBad", () -> { p.storeToXML(new ByteArrayOutputStream(), "x", "nope"); return 1; });
    t("charsetNull", () -> { p.storeToXML(new ByteArrayOutputStream(), "x", (String) null); return 1; });
    t("outNull", () -> { p.storeToXML(null, "x"); return 1; });
    t("defaults", () -> { Properties d = new Properties(); d.setProperty("dk", "dv"); Properties w = new Properties(d); w.setProperty("own", "1"); return store(w, null).replace("\n", "|"); });
    t("load", () -> { Properties r = new Properties(); r.loadFromXML(new ByteArrayInputStream(store(q, "c").getBytes("UTF-8"))); return new TreeMap<>(r); });
    t("loadManual", () -> { Properties r = new Properties(); r.loadFromXML(new ByteArrayInputStream("<?xml version=\"1.0\" encoding=\"UTF-8\"?><!DOCTYPE properties SYSTEM \"http://java.sun.com/dtd/properties.dtd\"><properties><comment>c</comment><entry key=\"x\">y</entry><entry key=\"e\"/><entry key=\"x\">z</entry></properties>".getBytes())); return new TreeMap<>(r); });
    t("loadNoDoctype", () -> { Properties r = new Properties(); r.loadFromXML(new ByteArrayInputStream("<?xml version=\"1.0\"?><properties><entry key=\"x\">y</entry></properties>".getBytes())); return new TreeMap<>(r); });
    t("loadBadRoot", () -> { new Properties().loadFromXML(new ByteArrayInputStream("<?xml version=\"1.0\"?><!DOCTYPE properties SYSTEM \"http://java.sun.com/dtd/properties.dtd\"><props/>".getBytes())); return 1; });
    t("loadBadEntry", () -> { new Properties().loadFromXML(new ByteArrayInputStream("<?xml version=\"1.0\"?><!DOCTYPE properties SYSTEM \"http://java.sun.com/dtd/properties.dtd\"><properties><entry>y</entry></properties>".getBytes())); return 1; });
    t("loadBadChild", () -> { new Properties().loadFromXML(new ByteArrayInputStream("<?xml version=\"1.0\"?><!DOCTYPE properties SYSTEM \"http://java.sun.com/dtd/properties.dtd\"><properties><foo/></properties>".getBytes())); return 1; });
    t("loadMalformed", () -> { new Properties().loadFromXML(new ByteArrayInputStream(("<?xml version=\"1.0\"?><!DOCTYPE properties SYSTEM \"http://java.sun.com/dtd/properties.dtd\"><properties>").getBytes())); return 1; });
    t("loadEmpty", () -> { new Properties().loadFromXML(new ByteArrayInputStream(new byte[0])); return 1; });
    t("loadNull", () -> { new Properties().loadFromXML(null); return 1; });
    t("loadMergesDefaults", () -> { Properties r = new Properties(); r.setProperty("pre", "1"); r.loadFromXML(new ByteArrayInputStream(store(p, null).getBytes())); return new TreeMap<>(r); });
    t("loadBadDtd", () -> { new Properties().loadFromXML(new ByteArrayInputStream("<?xml version=\"1.0\"?><!DOCTYPE properties SYSTEM \"http://example.com/other.dtd\"><properties/>".getBytes())); return 1; });
    t("loadLatin", () -> { Properties r = new Properties(); r.loadFromXML(new ByteArrayInputStream("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?><!DOCTYPE properties SYSTEM \"http://java.sun.com/dtd/properties.dtd\"><properties><entry key=\"k\">é</entry></properties>".getBytes("ISO-8859-1"))); return new TreeMap<>(r); });
    t("loadEntities", () -> { Properties r = new Properties(); r.loadFromXML(new ByteArrayInputStream("<?xml version=\"1.0\"?><!DOCTYPE properties SYSTEM \"http://java.sun.com/dtd/properties.dtd\"><properties><entry key=\"k\">&lt;&amp;&#65;&#x42;</entry></properties>".getBytes())); return new TreeMap<>(r); });
    t("loadCdata", () -> { Properties r = new Properties(); r.loadFromXML(new ByteArrayInputStream("<?xml version=\"1.0\"?><!DOCTYPE properties SYSTEM \"http://java.sun.com/dtd/properties.dtd\"><properties><entry key=\"k\"><![CDATA[<raw>]]></entry></properties>".getBytes())); return new TreeMap<>(r); });
    t("loadNested", () -> { Properties r = new Properties(); r.loadFromXML(new ByteArrayInputStream("<?xml version=\"1.0\"?><!DOCTYPE properties SYSTEM \"http://java.sun.com/dtd/properties.dtd\"><properties><entry key=\"k\"><b>x</b></entry></properties>".getBytes())); return new TreeMap<>(r); });
    t("loadDupComment", () -> { Properties r = new Properties(); r.loadFromXML(new ByteArrayInputStream("<?xml version=\"1.0\"?><!DOCTYPE properties SYSTEM \"http://java.sun.com/dtd/properties.dtd\"><properties><comment>a</comment><comment>b</comment><entry key=\"k\">v</entry></properties>".getBytes())); return new TreeMap<>(r); });
    t("loadEntityDecl", () -> { Properties r = new Properties(); r.loadFromXML(new ByteArrayInputStream("<?xml version=\"1.0\"?><!DOCTYPE properties SYSTEM \"http://java.sun.com/dtd/properties.dtd\" [<!ENTITY e \"ent\">]><properties><entry key=\"k\">&e;</entry></properties>".getBytes())); return new TreeMap<>(r); });
    t("loadWhitespace", () -> { Properties r = new Properties(); r.loadFromXML(new ByteArrayInputStream("<?xml version=\"1.0\"?><!DOCTYPE properties SYSTEM \"http://java.sun.com/dtd/properties.dtd\"><properties>\n  <entry key=\"k\">  spaced  </entry>\n</properties>".getBytes())); return new TreeMap<>(r); });
  }
}

class PropertiesXmlModern2 {
  static final String H = "<?xml version=\"1.0\"?><!DOCTYPE properties SYSTEM \"http://java.sun.com/dtd/properties.dtd\">";
  static void t(String n, String body) { try { Properties r = new Properties(); r.loadFromXML(new ByteArrayInputStream((H + body).getBytes("UTF-8"))); System.out.println(n + ": " + new TreeMap<>(r)); } catch (Throwable e) { System.out.println(n + ": !" + e.getClass().getName() + ":" + e.getMessage().replace("\n", "|")); } }
  static void run() throws Exception {
    t("commentAfter", "<properties><entry key=\"a\">1</entry><comment>c</comment></properties>");
    t("textInProps", "<properties>text<entry key=\"a\">1</entry></properties>");
    t("nestedEntry", "<properties><entry key=\"a\"><entry key=\"b\">1</entry></entry></properties>");
    t("nestedComment", "<properties><comment><entry key=\"b\">1</entry></comment></properties>");
    t("extraAttr", "<properties><entry key=\"a\" x=\"1\">1</entry></properties>");
    t("extraAttrProps", "<properties x=\"1\"><entry key=\"a\">1</entry></properties>");
    t("emptyKey", "<properties><entry key=\"\">1</entry></properties>");
    t("noEntries", "<properties/>");
    t("commentOnly", "<properties><comment>x</comment></properties>");
    t("twoComments", "<properties><comment>x</comment><comment>y</comment></properties>");
    t("entryNoClose", "<properties><entry key=\"a\">1</properties>");
    t("pi", "<properties><?pi data?><entry key=\"a\">1</entry></properties>");
    t("comments", "<properties><!-- c --><entry key=\"a\">1<!-- c -->2</entry></properties>");
    t("attrWs", "<properties><entry key=\"  a\tb\n\">1</entry></properties>");
    t("keyCharRef", "<properties><entry key=\"&#65;&amp;\">1</entry></properties>");
    t("unknownEntity", "<properties><entry key=\"a\">&foo;</entry></properties>");
    t("nsAttr", "<properties xmlns=\"urn:x\"><entry key=\"a\">1</entry></properties>");
    t("version11", "<properties><entry key=\"a\">1</entry></properties>");
    t("badDoctypeRoot", "<properties><entry key=\"a\">v</entry></properties><extra/>");
  }
}
