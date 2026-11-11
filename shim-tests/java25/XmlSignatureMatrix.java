/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

import java.io.*;
import java.math.BigInteger;
import java.security.*;
import java.security.spec.*;
import java.util.*;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.crypto.*;
import javax.xml.crypto.dom.*;
import javax.xml.crypto.dsig.*;
import javax.xml.crypto.dsig.dom.*;
import javax.xml.crypto.dsig.keyinfo.*;
import javax.xml.crypto.dsig.spec.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;
import org.w3c.dom.*;

public class XmlSignatureMatrix {
  static final BigInteger N = new BigInteger("8317845ac67656b33a49d63d09f7a1b525d573bf5a218778a111155f552af8c2ea58e909bdd192a0d6f316c989878cc5d068d50be6550a757932dd83aa204d3c19e1915c89d24a8d71faf16dc04b1649beb2f60c33cb63bbe11568cf092b1cff59870ed6bbe036f123bea245e61053c0c61360ddfa56e0df8113dc6f58216407", 16);
  static final BigInteger E = new BigInteger("10001", 16);
  static final BigInteger D = new BigInteger("ad34a8336e6535d0e5b2f1f18293afd27156a36c922240b81445fe26e69464443fe4956330cbb0ade9a809292920c7ff8a6b91853c8914c0f4587fb197d41617baf614fd1e266669f7a03bc1f4802062f4f509ac309f87c08da940b9dd73f541577d9e54c1a172e30b49c6445f348f869d46a2c168204e5baccc5e5876b12d", 16);
  static PublicKey pub; static PrivateKey priv; static XMLSignatureFactory fac; static KeyInfoFactory kif;
  static final String XML = "<?xml version=\"1.0\"?><!-- pre --><doc xmlns=\"urn:d\" xmlns:x=\"urn:x\" xml:lang=\"en\">"
        + "<item x:a=\"1\" b=\"2\">hello &amp; world <!-- c --> é\r\n</item><other Id=\"o1\"><![CDATA[<cd>]]><s xmlns:y=\"urn:y\" y:q=\"1\"/></other><n>  7  </n></doc>";

  static Document parse(String s) throws Exception {
    DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
    f.setNamespaceAware(true);
    Document d = f.newDocumentBuilder().parse(new ByteArrayInputStream(s.getBytes("UTF-8")));
    NodeList nl = d.getElementsByTagNameNS("urn:d", "other");
    if (nl.getLength() > 0) ((Element) nl.item(0)).setIdAttribute("Id", true);
    return d;
  }
  static String ser(Node d) throws Exception {
    Transformer t = TransformerFactory.newInstance().newTransformer();
    t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
    StringWriter w = new StringWriter();
    t.transform(new DOMSource(d), new StreamResult(w));
    return w.toString();
  }
  static void ex(String n, Throwable e) {
    System.out.println(n + " !! " + e.getClass().getName() + ": " + e.getMessage() + (e.getCause() != null ? " / " + e.getCause().getClass().getName() + ": " + e.getCause().getMessage() : ""));
  }
  interface Body { void run() throws Exception; }
  static void t(String n, Body b) { try { b.run(); } catch (Throwable e) { ex(n, e); } }

  static void roundtrip(String name, Key signKey, Key valKey, String c14n, C14NMethodParameterSpec cp, String sigm, SignatureMethodParameterSpec sp,
      String dig, List<Transform> trs, String uri, KeyInfo ki) {
    t(name, () -> {
      Document doc = parse(XML);
      Reference r = fac.newReference(uri, fac.newDigestMethod(dig, null), trs, null, null);
      SignedInfo si = fac.newSignedInfo(fac.newCanonicalizationMethod(c14n, cp), fac.newSignatureMethod(sigm, sp), Collections.singletonList(r));
      XMLSignature sig = fac.newXMLSignature(si, ki);
      sig.sign(new DOMSignContext(signKey, doc.getDocumentElement()));
      String out = ser(doc);
      System.out.println(name + " signed " + out.replaceAll("<doc[^>]*>", "<doc>").length() + " " + out.hashCode());
      System.out.println(out);
      Document d2 = parse(out);
      DOMValidateContext vc = new DOMValidateContext(valKey, d2.getElementsByTagNameNS(XMLSignature.XMLNS, "Signature").item(0));
      XMLSignature s2 = fac.unmarshalXMLSignature(vc);
      boolean v = s2.validate(vc);
      System.out.println(name + " valid " + v + " ref " + s2.getSignedInfo().getReferences().get(0).validate(vc));
    });
  }

  public static void main(String[] a) throws Exception {
    KeyFactory kf = KeyFactory.getInstance("RSA");
    pub = kf.generatePublic(new RSAPublicKeySpec(N, E));
    priv = kf.generatePrivate(new RSAPrivateKeySpec(N, D));
    fac = XMLSignatureFactory.getInstance("DOM");
    kif = fac.getKeyInfoFactory();
    System.out.println(fac.getMechanismType() + " " + fac.getProvider().getName() + " " + fac.getProvider().getInfo() + " " + fac.getProvider().getVersionStr());
    System.out.println(new TreeSet<>(Arrays.asList(fac.getProvider().getServices().stream().map(s -> s.getType() + "/" + s.getAlgorithm() + "=" + s.getClassName()).toArray(String[]::new))));
    String[] c14ns = {CanonicalizationMethod.INCLUSIVE, CanonicalizationMethod.INCLUSIVE_WITH_COMMENTS, CanonicalizationMethod.EXCLUSIVE,
        CanonicalizationMethod.EXCLUSIVE_WITH_COMMENTS, "http://www.w3.org/2006/12/xml-c14n11", "http://www.w3.org/2006/12/xml-c14n11#WithComments"};
    for (String c : c14ns) {
      C14NMethodParameterSpec cp = c.contains("xml-exc") ? new ExcC14NParameterSpec(Arrays.asList("x", "#default")) : null;
      roundtrip("c14n " + c, priv, pub, c, cp, SignatureMethod.RSA_SHA256, null, DigestMethod.SHA256, Collections.singletonList(mkTr(c, cp)), "", null);
    }
    String[] digs = {DigestMethod.SHA1, "http://www.w3.org/2001/04/xmldsig-more#sha224", DigestMethod.SHA256, "http://www.w3.org/2001/04/xmldsig-more#sha384", DigestMethod.SHA512,
        "http://www.w3.org/2007/05/xmldsig-more#sha3-256", "http://www.w3.org/2001/04/xmldsig-more#md5", DigestMethod.RIPEMD160, "urn:bogus"};
    for (String d : digs) roundtrip("dig " + d, priv, pub, CanonicalizationMethod.EXCLUSIVE, null, SignatureMethod.RSA_SHA256, null, d, Collections.emptyList(), "#o1", null);
    String[] rsas = {SignatureMethod.RSA_SHA1, "http://www.w3.org/2001/04/xmldsig-more#rsa-sha224", "http://www.w3.org/2001/04/xmldsig-more#rsa-sha256", "http://www.w3.org/2001/04/xmldsig-more#rsa-sha384",
        "http://www.w3.org/2001/04/xmldsig-more#rsa-sha512", "http://www.w3.org/2001/04/xmldsig-more#rsa-ripemd160", "urn:bogus"};
    for (String r : rsas) roundtrip("rsa " + r, priv, pub, CanonicalizationMethod.EXCLUSIVE, null, r, null, DigestMethod.SHA256, Collections.emptyList(), "#o1", null);
    String[] hm = {SignatureMethod.HMAC_SHA1, "http://www.w3.org/2001/04/xmldsig-more#hmac-sha224", "http://www.w3.org/2001/04/xmldsig-more#hmac-sha256", "http://www.w3.org/2001/04/xmldsig-more#hmac-sha384", "http://www.w3.org/2001/04/xmldsig-more#hmac-sha512", "http://www.w3.org/2001/04/xmldsig-more#hmac-ripemd160"};
    SecretKeySpec hk = new SecretKeySpec("0123456789abcdef0123".getBytes(), "HmacSHA256");
    for (String h : hm) roundtrip("hmac " + h, hk, hk, CanonicalizationMethod.INCLUSIVE, null, h, null, DigestMethod.SHA256, Collections.emptyList(), "#o1", null);
    roundtrip("hmac len", hk, hk, CanonicalizationMethod.INCLUSIVE, null, SignatureMethod.HMAC_SHA1, new HMACParameterSpec(40), DigestMethod.SHA256, Collections.emptyList(), "#o1", null);
    // transforms
    t("xpath", () -> {
      Transform x = fac.newTransform(Transform.XPATH, new XPathFilterParameterSpec("not(ancestor-or-self::dsig:Signature)", Collections.singletonMap("dsig", XMLSignature.XMLNS)));
      roundtrip("xpath", priv, pub, CanonicalizationMethod.INCLUSIVE, null, SignatureMethod.RSA_SHA256, null, DigestMethod.SHA256, Arrays.asList(x), "", null);
    });
    t("xpath2", () -> {
      Transform x = fac.newTransform(Transform.XPATH2, new XPathFilter2ParameterSpec(Arrays.asList(new XPathType("//x:nothing", XPathType.Filter.SUBTRACT, Collections.singletonMap("x", "urn:d")))));
      roundtrip("xpath2", priv, pub, CanonicalizationMethod.INCLUSIVE, null, SignatureMethod.RSA_SHA256, null, DigestMethod.SHA256, Arrays.asList(x), "", null);
      Transform y = fac.newTransform(Transform.XPATH2, new XPathFilter2ParameterSpec(Arrays.asList(new XPathType("//y:other", XPathType.Filter.INTERSECT, Collections.singletonMap("y", "urn:d")), new XPathType("//y:s", XPathType.Filter.UNION, Collections.singletonMap("y", "urn:d")))));
      roundtrip("xpath2b", priv, pub, CanonicalizationMethod.INCLUSIVE, null, SignatureMethod.RSA_SHA256, null, DigestMethod.SHA256, Arrays.asList(y), "", null);
    });
    roundtrip("base64", priv, pub, CanonicalizationMethod.INCLUSIVE, null, SignatureMethod.RSA_SHA256, null, DigestMethod.SHA256, safe(() -> Arrays.asList(fac.newTransform(Transform.BASE64, (TransformParameterSpec) null))), "#o1", null);
    roundtrip("env+exc", priv, pub, CanonicalizationMethod.INCLUSIVE, null, SignatureMethod.RSA_SHA256, null, DigestMethod.SHA256, safe(() -> Arrays.asList(fac.newTransform(Transform.ENVELOPED, (TransformParameterSpec) null), fac.newTransform(CanonicalizationMethod.EXCLUSIVE_WITH_COMMENTS, (TransformParameterSpec) null))), "", null);
    roundtrip("xslt", priv, pub, CanonicalizationMethod.INCLUSIVE, null, SignatureMethod.RSA_SHA256, null, DigestMethod.SHA256, safe(() -> {
      Document xs = parse("<xsl:stylesheet version=\"1.0\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\"><xsl:output method=\"xml\"/><xsl:template match=\"/\"><r><xsl:value-of select=\"count(//*)\"/></r></xsl:template></xsl:stylesheet>");
      return Arrays.asList(fac.newTransform(Transform.XSLT, new XSLTTransformParameterSpec(new DOMStructure(xs.getDocumentElement()))));
    }), "", null);
    // keyinfo
    roundtrip("keyinfo", priv, pub, CanonicalizationMethod.INCLUSIVE, null, SignatureMethod.RSA_SHA256, null, DigestMethod.SHA256, Collections.emptyList(), "#o1",
        safe(() -> kif.newKeyInfo(Arrays.asList(kif.newKeyName("kn"), kif.newKeyValue(pub), kif.newPGPData(new byte[]{1,2,3,4,5,6,7,8}), kif.newRetrievalMethod("#o1", null, null), kif.newX509IssuerSerial("CN=a", BigInteger.TEN), kif.newX509Data(Arrays.asList("CN=a", new byte[]{9}))), "kid")));
    // enveloping, manifest, properties, objects
    t("enveloping", () -> {
      Document doc = parse("<root/>");
      XMLObject obj = fac.newXMLObject(Collections.singletonList(new DOMStructure(doc.createElementNS("urn:o", "o:payload"))), "obj1", "text/xml", "UTF-8");
      Reference r = fac.newReference("#obj1", fac.newDigestMethod(DigestMethod.SHA256, null));
      Reference mr = fac.newReference("#obj1", fac.newDigestMethod(DigestMethod.SHA1, null), null, Manifest.TYPE, "mid");
      Manifest m = fac.newManifest(Collections.singletonList(mr), "man");
      XMLObject mo = fac.newXMLObject(Collections.singletonList(m), "manobj", null, null);
      SignatureProperty sp = fac.newSignatureProperty(Collections.singletonList(new DOMStructure(doc.createElementNS("urn:p", "p:prop"))), "#sig", "sp1");
      SignatureProperties sps = fac.newSignatureProperties(Collections.singletonList(sp), "sps");
      XMLObject po = fac.newXMLObject(Collections.singletonList(sps), "spobj", null, null);
      SignedInfo si = fac.newSignedInfo(fac.newCanonicalizationMethod(CanonicalizationMethod.INCLUSIVE, (C14NMethodParameterSpec) null), fac.newSignatureMethod(SignatureMethod.RSA_SHA256, null), Arrays.asList(r, fac.newReference("#man", fac.newDigestMethod(DigestMethod.SHA256, null), null, Manifest.TYPE, null)), "si");
      XMLSignature sig = fac.newXMLSignature(si, null, Arrays.asList(obj, mo, po), "sig", "sv");
      DOMSignContext sc = new DOMSignContext(priv, doc.getDocumentElement());
      sc.putNamespacePrefix(XMLSignature.XMLNS, "ds");
      sig.sign(sc);
      String out = ser(doc);
      System.out.println(out);
      Document d2 = parse(out);
      Element e = (Element) d2.getElementsByTagNameNS(XMLSignature.XMLNS, "Signature").item(0);
      for (String id : new String[] {"obj1", "man", "spobj", "sig"}) { NodeList nl = d2.getElementsByTagName("*"); for (int i = 0; i < nl.getLength(); i++) { Element x = (Element) nl.item(i); if (id.equals(x.getAttribute("Id"))) x.setIdAttribute("Id", true); } }
      DOMValidateContext vc = new DOMValidateContext(pub, e);
      XMLSignature s2 = fac.unmarshalXMLSignature(vc);
      System.out.println("valid " + s2.validate(vc) + " " + s2.getId() + " " + s2.getObjects().size() + " " + s2.getSignatureValue().getId() + " " + Arrays.toString(s2.getSignatureValue().getValue()).length());
      System.out.println(s2.getSignedInfo().getId() + " " + s2.getSignedInfo().getCanonicalizationMethod().getAlgorithm());
      for (Object r2 : s2.getSignedInfo().getReferences()) { Reference rr = (Reference) r2; System.out.println(rr.getURI() + " " + rr.getType() + " " + rr.isFeatureSupported("x") + " " + rr.getDigestMethod().getAlgorithm() + " " + rr.getDigestValue().length + " " + rr.getCalculatedDigestValue().length + " " + rr.validate(vc)); }
      Manifest mm = (Manifest) ((XMLObject) s2.getObjects().get(1)).getContent().get(0);
      System.out.println(mm.getId() + " " + mm.getReferences().size() + " " + ((Reference) mm.getReferences().get(0)).validate(vc));
      System.out.println(s2.getSignatureValue().validate(vc));
      SignatureProperties q = (SignatureProperties) ((XMLObject) s2.getObjects().get(2)).getContent().get(0);
      System.out.println(q.getId() + " " + ((SignatureProperty) q.getProperties().get(0)).getTarget());
    });
    // validation failure + status + dereferencer + keyselector
    t("tamper", () -> {
      Document doc = parse(XML);
      Reference r = fac.newReference("#o1", fac.newDigestMethod(DigestMethod.SHA256, null));
      SignedInfo si = fac.newSignedInfo(fac.newCanonicalizationMethod(CanonicalizationMethod.INCLUSIVE, (C14NMethodParameterSpec) null), fac.newSignatureMethod(SignatureMethod.RSA_SHA256, null), Collections.singletonList(r));
      fac.newXMLSignature(si, kif.newKeyInfo(Collections.singletonList(kif.newKeyValue(pub)))).sign(new DOMSignContext(priv, doc.getDocumentElement()));
      String out = ser(doc);
      Document d2 = parse(out.replace("<cd>", "&lt;ce&gt;").replace("&lt;cd&gt;", "&lt;ce&gt;"));
      DOMValidateContext vc = new DOMValidateContext(new KeySelector() {
        public KeySelectorResult select(KeyInfo k, Purpose p, AlgorithmMethod m, XMLCryptoContext c) throws KeySelectorException {
          System.out.println("sel " + p + " " + m.getAlgorithm() + " " + k.getContent().size());
          return () -> pub; } }, d2.getElementsByTagNameNS(XMLSignature.XMLNS, "Signature").item(0));
      vc.setProperty("org.jcp.xml.dsig.secureValidation", Boolean.TRUE);
      XMLSignature s2 = fac.unmarshalXMLSignature(vc);
      System.out.println("v " + s2.validate(vc) + " " + s2.getSignatureValue().validate(vc) + " " + ((Reference) s2.getSignedInfo().getReferences().get(0)).validate(vc));
      System.out.println(vc.getProperty("org.jcp.xml.dsig.secureValidation") + " " + vc.getBaseURI() + " " + vc.getNode().getNodeName() + " " + vc.getURIDereferencer().getClass().getName() + " " + vc.getNamespacePrefix(XMLSignature.XMLNS, "zz") + " " + fac.getURIDereferencer().getClass().getName());
      Reference rr = (Reference) s2.getSignedInfo().getReferences().get(0);
      Data data = rr.getDereferencedData();
      System.out.println(data.getClass().getName() + " " + (rr.getDigestInputStream() != null));
      byte[] bs = new byte[1000]; int n = rr.getDigestInputStream().read(bs);
      System.out.println(n + " " + new String(bs, 0, n));
    });
    t("badkey", () -> {
      Document doc = parse(XML);
      SignedInfo si = fac.newSignedInfo(fac.newCanonicalizationMethod(CanonicalizationMethod.INCLUSIVE, (C14NMethodParameterSpec) null), fac.newSignatureMethod(SignatureMethod.RSA_SHA256, null), Collections.singletonList(fac.newReference("", fac.newDigestMethod(DigestMethod.SHA256, null))));
      fac.newXMLSignature(si, null).sign(new DOMSignContext(pub, doc.getDocumentElement()));
    });
    t("nokey", () -> {
      Document doc = parse(XML);
      SignedInfo si = fac.newSignedInfo(fac.newCanonicalizationMethod(CanonicalizationMethod.INCLUSIVE, (C14NMethodParameterSpec) null), fac.newSignatureMethod(SignatureMethod.RSA_SHA256, null), Collections.singletonList(fac.newReference("", fac.newDigestMethod(DigestMethod.SHA256, null))));
      fac.newXMLSignature(si, null).sign(new DOMSignContext(priv, doc.getDocumentElement()));
      Document d2 = parse(ser(doc));
      DOMValidateContext vc = new DOMValidateContext(new KeySelector() { public KeySelectorResult select(KeyInfo k, Purpose p, AlgorithmMethod m, XMLCryptoContext c) throws KeySelectorException { throw new KeySelectorException("none"); } }, d2.getElementsByTagNameNS(XMLSignature.XMLNS, "Signature").item(0));
      fac.unmarshalXMLSignature(vc).validate(vc);
    });
    t("secure", () -> {
      Document doc = parse(XML);
      SignedInfo si = fac.newSignedInfo(fac.newCanonicalizationMethod(CanonicalizationMethod.INCLUSIVE, (C14NMethodParameterSpec) null), fac.newSignatureMethod(SignatureMethod.RSA_SHA1, null), Collections.singletonList(fac.newReference("", fac.newDigestMethod("http://www.w3.org/2001/04/xmldsig-more#md5", null))));
      XMLSignature s = fac.newXMLSignature(si, null);
      s.sign(new DOMSignContext(priv, doc.getDocumentElement()));
      Document d2 = parse(ser(doc));
      DOMValidateContext vc = new DOMValidateContext(pub, d2.getElementsByTagNameNS(XMLSignature.XMLNS, "Signature").item(0));
      try { fac.unmarshalXMLSignature(vc).validate(vc); } catch (Exception e) { ex("secure-md5", e); }
      vc.setProperty("org.jcp.xml.dsig.secureValidation", Boolean.FALSE);
      System.out.println("insecure " + fac.unmarshalXMLSignature(vc).validate(vc));
    });
    t("factory", () -> {
      for (String m : new String[] {"DOM", "dom", "XML"}) t("f " + m, () -> System.out.println(XMLSignatureFactory.getInstance(m).getMechanismType()));
      t("fp", () -> System.out.println(XMLSignatureFactory.getInstance("DOM", "XMLDSig").getProvider().getName()));
      t("fp2", () -> XMLSignatureFactory.getInstance("DOM", "Nope"));
      t("fp3", () -> System.out.println(XMLSignatureFactory.getInstance().getMechanismType()));
      t("feat", () -> System.out.println(fac.isFeatureSupported("x") + " " + fac.isFeatureSupported(XMLStructure.class.getName())));
      t("ts", () -> { TransformService ts = TransformService.getInstance(CanonicalizationMethod.EXCLUSIVE, "DOM"); System.out.println(ts.getAlgorithm() + " " + ts.getMechanismType() + " " + ts.getProvider().getName() + " " + ts.getParameterSpec()); });
      t("ts2", () -> TransformService.getInstance("urn:bogus", "DOM"));
      t("ts3", () -> { TransformService ts = TransformService.getInstance(Transform.XPATH, "DOM"); ts.init(new XPathFilterParameterSpec("a")); System.out.println(((XPathFilterParameterSpec) ts.getParameterSpec()).getXPath()); });
      t("ts4", () -> { TransformService ts = TransformService.getInstance(Transform.XPATH, "DOM"); ts.init(null); });
      t("ts5", () -> { TransformService ts = TransformService.getInstance(Transform.ENVELOPED, "DOM"); ts.init(new XPathFilterParameterSpec("a")); });
      t("tx", () -> { TransformService ts = TransformService.getInstance(CanonicalizationMethod.INCLUSIVE, "DOM"); ts.init(null);
        OctetStreamData od = new OctetStreamData(new ByteArrayInputStream("<a  b='1'/>".getBytes()));
        Data d = ts.transform(od, null); System.out.println(d.getClass().getName()); ByteArrayOutputStream bo = new ByteArrayOutputStream(); byte[] b = ((OctetStreamData) d).getOctetStream().readAllBytes(); System.out.println(new String(b)); });
      t("ctx", () -> { DOMSignContext c = new DOMSignContext(priv, parse("<a/>").getDocumentElement()); System.out.println((c.getKeySelector() != null) + " " + c.getBaseURI() + " " + c.getDefaultNamespacePrefix() + " " + c.getParent().getNodeName() + " " + c.getNextSibling() + " " + c.putNamespacePrefix("urn:z", "zz") + " " + c.getNamespacePrefix("urn:z", "d") + " " + c.putNamespacePrefix("urn:z", "zz"));  });
    });
    t("pre", () -> {
      Document doc = parse(XML);
      DOMSignContext sc = new DOMSignContext(priv, doc.getDocumentElement());
      sc.setProperty("javax.xml.crypto.dsig.cacheReference", Boolean.TRUE);
      Reference r = fac.newReference("", fac.newDigestMethod(DigestMethod.SHA256, null));
      SignedInfo si = fac.newSignedInfo(fac.newCanonicalizationMethod(CanonicalizationMethod.INCLUSIVE, (C14NMethodParameterSpec) null), fac.newSignatureMethod(SignatureMethod.RSA_SHA256, null), Collections.singletonList(r));
      XMLSignature s = fac.newXMLSignature(si, null);
      s.sign(sc);
      System.out.println(((Reference) s.getSignedInfo().getReferences().get(0)).getDereferencedData().getClass().getName());
      System.out.println(s.getSignedInfo().getCanonicalizedData().readAllBytes().length);
      System.out.println((s.getKeySelectorResult() != null) + " " + s.isFeatureSupported("a"));
    });
  }
  interface Sup<T> { T get() throws Exception; }
  static <T> T safe(Sup<T> s) { try { return s.get(); } catch (Exception e) { throw new RuntimeException(e); } }
  static Transform mkTr(String c, C14NMethodParameterSpec cp) { return safe(() -> fac.newTransform(c, (TransformParameterSpec) cp)); }
}
