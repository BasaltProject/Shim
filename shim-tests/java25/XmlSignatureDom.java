/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.xml.crypto.dsig.CanonicalizationMethod;
import javax.xml.crypto.dsig.DigestMethod;
import javax.xml.crypto.dsig.Reference;
import javax.xml.crypto.dsig.SignatureMethod;
import javax.xml.crypto.dsig.SignedInfo;
import javax.xml.crypto.dsig.Transform;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.dom.DOMValidateContext;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.KeyInfoFactory;
import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;
import javax.xml.crypto.dsig.spec.ExcC14NParameterSpec;
import javax.xml.crypto.dsig.spec.TransformParameterSpec;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class XmlSignatureDom {
  static final BigInteger N = new BigInteger("8317845ac67656b33a49d63d09f7a1b525d573bf5a218778a111155f552af8c2ea58e909bdd192a0d6f316c989878cc5d068d50be6550a757932dd83aa204d3c19e1915c89d24a8d71faf16dc04b1649beb2f60c33cb63bbe11568cf092b1cff59870ed6bbe036f123bea245e61053c0c61360ddfa56e0df8113dc6f58216407", 16);
  static final BigInteger E = new BigInteger("10001", 16);
  static final BigInteger D = new BigInteger("ad34a8336e6535d0e5b2f1f18293afd27156a36c922240b81445fe26e69464443fe4956330cbb0ade9a809292920c7ff8a6b91853c8914c0f4587fb197d41617baf614fd1e266669f7a03bc1f4802062f4f509ac309f87c08da940b9dd73f541577d9e54c1a172e30b49c6445f348f869d46a2c168204e5baccc5e5876b12d", 16);

  static Document parse(String s) throws Exception {
    DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
    f.setNamespaceAware(true);
    return f.newDocumentBuilder().parse(new ByteArrayInputStream(s.getBytes("UTF-8")));
  }

  static String serialize(Document d) throws Exception {
    Transformer t = TransformerFactory.newInstance().newTransformer();
    t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
    StringWriter w = new StringWriter();
    t.transform(new DOMSource(d), new StreamResult(w));
    return w.toString();
  }

  public static void main(String[] args) throws Exception {
    KeyFactory kf = KeyFactory.getInstance("RSA");
    PublicKey pub = kf.generatePublic(new RSAPublicKeySpec(N, E));
    PrivateKey priv = kf.generatePrivate(new RSAPrivateKeySpec(N, D));
    XMLSignatureFactory fac = XMLSignatureFactory.getInstance("DOM");
    KeyInfoFactory kif = fac.getKeyInfoFactory();
    String xml = "<?xml version=\"1.0\"?><!-- pre --><doc xmlns=\"urn:d\" xmlns:x=\"urn:x\">"
        + "<item x:a=\"1\" b=\"2\">hello &amp; world</item><other Id=\"o1\"><![CDATA[<cd>]]></other></doc>";
    for (String prefix : new String[] {null, "ds"}) {
      Document doc = parse(xml);
      ((Element) doc.getElementsByTagNameNS("urn:d", "other").item(0)).setIdAttribute("Id", true);
      Reference whole = fac.newReference("", fac.newDigestMethod(DigestMethod.SHA256, null), Collections.singletonList(
          fac.newTransform(Transform.ENVELOPED, (TransformParameterSpec) null)), null, null);
      Reference part = fac.newReference("#o1", fac.newDigestMethod(DigestMethod.SHA512, null), Collections.singletonList(
          fac.newTransform(CanonicalizationMethod.EXCLUSIVE, new ExcC14NParameterSpec(Arrays.asList("x")))), null, "r2");
      SignedInfo si = fac.newSignedInfo(
          fac.newCanonicalizationMethod(CanonicalizationMethod.EXCLUSIVE, (C14NMethodParameterSpec) null),
          fac.newSignatureMethod(SignatureMethod.RSA_SHA256, null), Arrays.asList(whole, part));
      KeyInfo ki = kif.newKeyInfo(Arrays.asList(kif.newKeyName("k"), kif.newKeyValue(pub)));
      DOMSignContext sc = new DOMSignContext(priv, doc.getDocumentElement());
      sc.setDefaultNamespacePrefix(prefix);
      fac.newXMLSignature(si, ki, null, "sig", "sv").sign(sc);
      String out = serialize(doc);
      System.out.println(out);
      Document d2 = parse(out);
      ((Element) d2.getElementsByTagNameNS("urn:d", "other").item(0)).setIdAttribute("Id", true);
      DOMValidateContext vc = new DOMValidateContext(pub, d2.getElementsByTagNameNS(XMLSignature.XMLNS, "Signature").item(0));
      XMLSignature s = fac.unmarshalXMLSignature(vc);
      System.out.println("valid " + s.validate(vc) + " " + s.getSignedInfo().getReferences().size());
      Document d3 = parse(out.replace("hello", "HELLO"));
      ((Element) d3.getElementsByTagNameNS("urn:d", "other").item(0)).setIdAttribute("Id", true);
      DOMValidateContext vc3 = new DOMValidateContext(pub, d3.getElementsByTagNameNS(XMLSignature.XMLNS, "Signature").item(0));
      System.out.println("tampered " + fac.unmarshalXMLSignature(vc3).validate(vc3));
    }
  }
}
