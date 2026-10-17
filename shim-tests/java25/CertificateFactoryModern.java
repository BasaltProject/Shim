/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 * Minimum release: 21
 */

import java.io.*; import java.nio.file.*; import java.security.*; import java.security.cert.*; import java.util.*; import java.math.BigInteger;
public class CertificateFactoryModern {
  static final String ROOT_PEM = "-----BEGIN CERTIFICATE-----\nMIIDajCCAlKgAwIBAgIJAMrANhXiiL31MA0GCSqGSIb3DQEBCwUAMFAxCzAJBgNV\nBAYTAkVTMRMwEQYDVQQKEwpTaGltIFRlc3RzMRAwDgYDVQQLEwdUZXN0aW5nMRow\nGAYDVQQDExFTaGltIFRlc3QgUm9vdCBDQTAgFw0xOTEyMzEyMzAwMDBaGA8yMTE5\nMTIwNzIzMDAwMFowUDELMAkGA1UEBhMCRVMxEzARBgNVBAoTClNoaW0gVGVzdHMx\nEDAOBgNVBAsTB1Rlc3RpbmcxGjAYBgNVBAMTEVNoaW0gVGVzdCBSb290IENBMIIB\nIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAzDLZ7v4gR+7leAX7MwmINCcT\n/7lkBqdmPG3SJcbIiYYFnja3FJw4jYMEx3ef+Ec5ElZxeN9E2twUQ+8akp8hp79i\nSb4/dp9dXP5i4dq2J0OHeNxrSNAQ+jS7wRLHdxZWb45xUCw66z0jvr6ualSuZ2nv\nf2TpzCMjaeIYVSucs1qgTmDMJbWFgEclBP0Ll+gw6dFpXqniGoS1c++t4DQoqer4\nORMyl25vCoijvLrlh3+x6MhxXvnHoEI8QxjVNvf30D01o6gjW4p3Te5fEHAJ7lg+\nsUB/40f+TtxtmJ9uVnDknFJtvl2JjubnAJQX+Wuw84boiVRvEbpHZCB74SsKXwID\nAQABo0UwQzAdBgNVHQ4EFgQUD3MsE4j9K6q0ay5XGZu0gKpLFPIwDgYDVR0PAQH/\nBAQDAgEGMBIGA1UdEwEB/wQIMAYBAf8CAQEwDQYJKoZIhvcNAQELBQADggEBADAT\nXvaXg1LSq1CItA0nhDnAsHWdf3DSmeXVXuA65UMeetEfAl8dUrqbY2RQH77Rr9KR\n9GLvMeL82WWzgnYaQqZbufIHmi2Du0OBtjMTjdTyXfU1YsQA/4pLL/WDo9cTVHzB\nRgpR9wkqhpMLFM9BV0/beQmJG/tsl+sHTEt3DBUdSB4tMoiR84HVwCvJOOnqoSs0\nWaNToJqDH5PRPICqahXOanuT90k8m0F+Ykw/zoPfZSLfTVvufxxSYFfYU+/KG4WR\nYSOIkGQPadPSzZjpqdjDBQ2kLMxvGr4Jy6uboHnbYr68U3lrfHz3Hck8RErm5oW9\nhmK2UEMID4+eWxIuLTY=\n-----END CERTIFICATE-----\n";
  static final String INTER_PEM = "-----BEGIN CERTIFICATE-----\nMIICszCCAZugAwIBAgIJAMCF8urJpMB2MA0GCSqGSIb3DQEBCwUAMFAxCzAJBgNV\nBAYTAkVTMRMwEQYDVQQKEwpTaGltIFRlc3RzMRAwDgYDVQQLEwdUZXN0aW5nMRow\nGAYDVQQDExFTaGltIFRlc3QgUm9vdCBDQTAgFw0yMDA1MzEyMjAwMDBaGA8yMTE4\nMTIyNDIyMDAwMFowQzELMAkGA1UEBhMCRVMxEzARBgNVBAoTClNoaW0gVGVzdHMx\nHzAdBgNVBAMTFlNoaW0gVGVzdCBJbnRlcm1lZGlhdGUwWTATBgcqhkjOPQIBBggq\nhkjOPQMBBwNCAAR2KPKdVTiSuIXNdPkTq1jsVp0XN/n9CO4quQrAm1AgF6jeCErl\nJU3E1JpXKXHHp8Fq1A/Pz3S9irX4ruyi5oqSo2YwZDAdBgNVHQ4EFgQU+KNmkfVp\n8L29tMPWZtj6moMkBRcwDgYDVR0PAQH/BAQDAgGGMBIGA1UdEwEB/wQIMAYBAf8C\nAQAwHwYDVR0jBBgwFoAUD3MsE4j9K6q0ay5XGZu0gKpLFPIwDQYJKoZIhvcNAQEL\nBQADggEBACeQc7Y+BWg6s3ep47rcg3KxSemaN8FrEd/28RizACVqa8sERIM1se8I\nbACJiSnGEsf3vPTeYZerfQOcodOD8mQKXxbdMYATd/9ZRoweF3DD2Jox6wb49UAR\n+H/XRsKfmNX9raEVwI7HOJnJjuoYWXbhhqT9Y1KLOurcnOKV0wrDqeAGaObb+9Ro\nhhRHJie8hYnovZx2MfnpPHaiTe9jGSy0cDRuO0fE6IV90qmu7DTNf3fOpf79fpPM\nCB/AVzhlU1wiXe1ueaUJ26/P63B6cFTFvlsyeZNo6qMiHBWaZmDYdXFkxKonQg3W\nbWfi2W2KvPY05i7tNJPg1RS+22kyBH8=\n-----END CERTIFICATE-----\n";
  static final String LEAF_PEM = "-----BEGIN CERTIFICATE-----\nMIIDbjCCAxWgAwIBAgIIc35j6RzwqQQwCgYIKoZIzj0EAwIwQzELMAkGA1UEBhMC\nRVMxEzARBgNVBAoTClNoaW0gVGVzdHMxHzAdBgNVBAMTFlNoaW0gVGVzdCBJbnRl\ncm1lZGlhdGUwIBcNMjAxMjMxMjMwMDAwWhgPMjEwMzAyMjAyMzAwMDBaMFUxCzAJ\nBgNVBAYTAkVTMQ8wDQYDVQQHEwZNYWRyaWQxGTAXBgNVBAoTEFNoaW0gVGVzdHMs\nIEluYy4xGjAYBgNVBAMTEXNoaW0uZXhhbXBsZS50ZXN0MIIBIjANBgkqhkiG9w0B\nAQEFAAOCAQ8AMIIBCgKCAQEA8NkrklP8Tc7Cr6LzKDV1zX0qveSH9peTlr6X9IsV\nGo9dYYKh5eh1mn0yXj6aV5ZyAJXhD2e22hHK29hjHwkjD5PRIfIkxFRN83Hr8cPD\nfAxBeV23bUn8jGdYw1qVYuVel0By/7UP6Ms+iI76PFUhoAri0VAe0Zeua3EiCeqG\nnOJBGBWYTxbZ44DQeq0afadKz9ptw3nOuOqeYG5WWcS55itjfJe2WiY5RsDDtvpg\nROdODGWKdXFVd9sUUGQR0HoLse6k1Z/m6QdLOu7nEaoSFOW4w2/hs6FmYTiXthfI\nz/Vs3m2qTPT8RWV3lS8Cpl/7vzPAjIhH9Wg5GXdoRMn0tQIDAQABo4IBEjCCAQ4w\nHQYDVR0OBBYEFJoLvBR/ot+gYrkcagAsjEaA/FM5MA4GA1UdDwEB/wQEAwIFoDCB\nggYDVR0RBHsweYIRc2hpbS5leGFtcGxlLnRlc3SCEyouc2hpbS5leGFtcGxlLnRl\nc3SHBMAAAgeHECABDbgAAAAAAAAAAAAAAAGBFWRldkBzaGltLmV4YW1wbGUudGVz\ndIYbaHR0cHM6Ly9zaGltLmV4YW1wbGUudGVzdC94iAMqAwQwDAYDVR0TAQH/BAIw\nADAfBgNVHSMEGDAWgBT4o2aR9Wnwvb20w9Zm2PqagyQFFzApBgNVHSUEIjAgBggr\nBgEFBQcDAQYIKwYBBQUHAwIGCisGAQQBgjcKAwQwCgYIKoZIzj0EAwIDRwAwRAIg\nedR9K9JPLAAJSYGZOTglQEmhkJAHwot+7rHeH2zmQ0gCIFi0J8I0arBIB7Hiy3+w\ng1tKXxMicoH/JbxWkT1Q0PGK\n-----END CERTIFICATE-----\n";
  static final String EC384_PEM = "-----BEGIN CERTIFICATE-----\nMIIBqDCCAS2gAwIBAgIJAK0U9TOYxTHJMAoGCCqGSM49BAMDMCkxCzAJBgNVBAYT\nAlVTMRowGAYDVQQDExFTZWxmIFNpZ25lZCBFQzM4NDAeFw0yMjAzMDQwNDA2MDda\nFw0zMjAzMDEwNDA2MDdaMCkxCzAJBgNVBAYTAlVTMRowGAYDVQQDExFTZWxmIFNp\nZ25lZCBFQzM4NDB2MBAGByqGSM49AgEGBSuBBAAiA2IABB6TRO0iFXS305sl3oTy\nQfCIoi+z//Lpgc/+82U/DXrMNn+Fm3mgYFxETAJQLOU/t3R5i6zz60NWKZMqq6z2\nWn806AtD1e2NezKEU6RCW4a14otbCj7oz0013mye2p3cOaMhMB8wHQYDVR0OBBYE\nFB7Rtx2FhDOnjQpkPVCfZt/8nRJjMAoGCCqGSM49BAMDA2kAMGYCMQD5mfngIFbS\nVu5wk3SWFc+MZ/Ddwx6RE7bIWLSu0n2L68t83m7bevaXLiA5scBm1UgCMQDg4cY5\n4d/lGa2mvd1u2QuSCCl+CfsRaFdbxRzvdVmbc/dk2W3Bq3j4yJUgnnlIq7g=\n-----END CERTIFICATE-----\n";
  static final String SHA1_PEM = "-----BEGIN CERTIFICATE-----\nMIIB5zCCAVCgAwIBAgIJALAHxtQW1beDMA0GCSqGSIb3DQEBBQUAMCQxDzANBgNV\nBAoTBkxlZ2FjeTERMA8GA1UEAxMIT2xkIFNIQTEwHhcNMTAwNTA2MDUwODA5WhcN\nMjAwNTAzMDUwODA5WjAkMQ8wDQYDVQQKEwZMZWdhY3kxETAPBgNVBAMTCE9sZCBT\nSEExMIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDkajijHUvPZwuPKNqVS8dJ\nmiohEdkxXFTuhKz3n5hrkz1fQ9nzmsbq2F4FB8z5W4tuEaZ7NLUWm3ei7/5s+qeF\nicxG015Jk5rL72H8qEgzNtXL+tklr4526f0CZgRXi36EbzqX899SfUui+UJ0blgs\n4yNTXzeAFY43fTxOkrLlZQIDAQABoyEwHzAdBgNVHQ4EFgQUz3o+5FysrCGvtKqi\nCjyD/sLgFDUwDQYJKoZIhvcNAQEFBQADgYEAcCh+vUIPFtOj8MY8HIFijb76+Cd/\nuWOnJLIgOJDHCi3bCVwYtfjZIJ/4Xqy+VQ6FHaJ/SWNuY/3Xui7wL1A8R1yzfCIY\ntYHfKveY6zTpgomAbKn/68/VJ8jFO3nVFX1B5KACAWSYcPMgbspN9u24RvhRBAer\nH2hP1r67SILTpRA=\n-----END CERTIFICATE-----\nerror de herramienta de claves: java.util.MissingFormatArgumentException: Format specifier '%2$s'\n";

  interface Body { Object get() throws Exception; }
  static void p(String n, Body b) { String o; try { Object r = b.get(); o = r instanceof byte[] ? "bytes" + ((byte[]) r).length : String.valueOf(r); } catch (Throwable t) { o = "!" + t.getClass().getName() + ":" + t.getMessage(); } System.out.println(n + "=" + o.replaceAll("@[0-9a-f]+", "@H")); }
  static String hx(byte[] b) { return b == null ? "null" : HexFormat.of().formatHex(b); }
  static String dt(Date d) { java.text.SimpleDateFormat f = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'"); f.setTimeZone(TimeZone.getTimeZone("UTC")); return f.format(d); }
  public static void main(String[] a) throws Exception {
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    CertificateFactory cf = CertificateFactory.getInstance("X.509");
    p("cf", () -> cf.getType() + " " + cf.getProvider().getName());
    for (String n : new String[]{"root", "inter", "leaf", "ec384", "sha1"}) {
      X509Certificate c = (X509Certificate) cf.generateCertificate(new ByteArrayInputStream((n.equals("root") ? ROOT_PEM : n.equals("inter") ? INTER_PEM : n.equals("leaf") ? LEAF_PEM : n.equals("ec384") ? EC384_PEM : SHA1_PEM).getBytes()));
      System.out.println("== " + n);
      p(n + " basic", () -> c.getVersion() + " " + c.getSerialNumber().toString(16) + " " + c.getType() + " " + dt(c.getNotBefore()) + " " + dt(c.getNotAfter()) + " " + c.getSigAlgName() + " " + c.getSigAlgOID() + " " + hx(c.getSigAlgParams()));
      p(n + " names", () -> c.getSubjectX500Principal() + " | " + c.getIssuerX500Principal() + " | " + c.getSubjectDN() + " | " + c.getIssuerDN() + " | " + c.getSubjectDN().getName() + " | " + c.getSubjectDN().getClass().getName() + " | " + c.getSubjectX500Principal().getName() + " | " + c.getIssuerX500Principal().getName(javax.security.auth.x500.X500Principal.RFC1779));
      p(n + " key", () -> c.getPublicKey().getAlgorithm() + " " + c.getPublicKey().getFormat() + " " + c.getPublicKey().getClass().getName() + " " + hx(MessageDigest.getInstance("SHA-256").digest(c.getPublicKey().getEncoded())).substring(0, 16));
      p(n + " keystr", () -> c.getPublicKey().toString().replace("\n", "|").substring(0, Math.min(90, c.getPublicKey().toString().length())));
      p(n + " usage", () -> Arrays.toString(c.getKeyUsage()) + " " + c.getBasicConstraints() + " " + c.getExtendedKeyUsage());
      p(n + " san", () -> c.getSubjectAlternativeNames() + " | " + c.getIssuerAlternativeNames());
      p(n + " exts", () -> new TreeSet<>(c.getCriticalExtensionOIDs() == null ? Set.of() : c.getCriticalExtensionOIDs()) + " | " + new TreeSet<>(c.getNonCriticalExtensionOIDs() == null ? Set.of() : c.getNonCriticalExtensionOIDs()) + " | " + c.hasUnsupportedCriticalExtension());
      for (String oid : new String[]{"2.5.29.19", "2.5.29.15", "2.5.29.17", "2.5.29.37", "2.5.29.14", "2.5.29.35", "1.2.3.4"}) p(n + " ext " + oid, () -> hx(c.getExtensionValue(oid)));
      p(n + " tbs", () -> hx(MessageDigest.getInstance("SHA-256").digest(c.getTBSCertificate())).substring(0, 16) + " " + c.getTBSCertificate().length + " " + c.getSignature().length + " " + c.getEncoded().length);
      p(n + " uids", () -> Arrays.toString(c.getIssuerUniqueID()) + Arrays.toString(c.getSubjectUniqueID()));
      p(n + " validity", () -> { c.checkValidity(); return "ok"; });
      p(n + " validity past", () -> { c.checkValidity(new Date(0)); return "ok"; });
      p(n + " validity future", () -> { c.checkValidity(new Date(5000000000000L)); return "ok"; });
      p(n + " self-verify", () -> { c.verify(c.getPublicKey()); return "ok"; });
      p(n + " tostring", () -> c.toString());
      p(n + " eq", () -> { X509Certificate d = (X509Certificate) cf.generateCertificate(new ByteArrayInputStream(c.getEncoded())); return c.equals(d) + " " + (c.hashCode() == d.hashCode()) + " " + c.equals(null) + " " + c.hashCode(); });
    }
    X509Certificate root = (X509Certificate) cf.generateCertificate(new ByteArrayInputStream(ROOT_PEM.getBytes()));
    X509Certificate inter = (X509Certificate) cf.generateCertificate(new ByteArrayInputStream(INTER_PEM.getBytes()));
    X509Certificate leaf = (X509Certificate) cf.generateCertificate(new ByteArrayInputStream(LEAF_PEM.getBytes()));
    p("verify chain", () -> { inter.verify(root.getPublicKey()); leaf.verify(inter.getPublicKey()); return "ok"; });
    p("verify wrong", () -> { leaf.verify(root.getPublicKey()); return "ok"; });
    p("verify wrong alg", () -> { leaf.verify(inter.getPublicKey(), "SunEC"); return "ok"; });
    p("verify provider", () -> { leaf.verify(inter.getPublicKey(), Security.getProvider("SunEC")); return "ok"; });
    p("verify bad provider", () -> { leaf.verify(inter.getPublicKey(), "NOPE"); return "ok"; });
    p("verify null", () -> { leaf.verify(null); return "ok"; });
    p("cf multi", () -> cf.generateCertificates(new ByteArrayInputStream((new String(ROOT_PEM.getBytes()) + new String(INTER_PEM.getBytes())).getBytes())).size());
    p("cf der", () -> ((X509Certificate) cf.generateCertificate(new ByteArrayInputStream(leaf.getEncoded()))).equals(leaf));
    p("cf der multi", () -> { ByteArrayOutputStream bo = new ByteArrayOutputStream(); bo.write(root.getEncoded()); bo.write(leaf.getEncoded()); return cf.generateCertificates(new ByteArrayInputStream(bo.toByteArray())).size(); });
    p("cf stream consumed", () -> { ByteArrayOutputStream bo = new ByteArrayOutputStream(); bo.write(root.getEncoded()); bo.write(leaf.getEncoded()); ByteArrayInputStream in = new ByteArrayInputStream(bo.toByteArray()); X509Certificate c1 = (X509Certificate) cf.generateCertificate(in); X509Certificate c2 = (X509Certificate) cf.generateCertificate(in); return c1.equals(root) + " " + c2.equals(leaf) + " " + in.available(); });
    p("cf pem stream consumed", () -> { String pem = new String(ROOT_PEM.getBytes()) + new String(LEAF_PEM.getBytes()); ByteArrayInputStream in = new ByteArrayInputStream(pem.getBytes()); X509Certificate c1 = (X509Certificate) cf.generateCertificate(in); X509Certificate c2 = (X509Certificate) cf.generateCertificate(in); return c1.equals(root) + " " + c2.equals(leaf) + " " + in.available(); });
    p("cf garbage", () -> cf.generateCertificate(new ByteArrayInputStream(new byte[]{1, 2, 3, 4})));
    p("cf empty", () -> cf.generateCertificate(new ByteArrayInputStream(new byte[0])));
    p("cf truncated", () -> cf.generateCertificate(new ByteArrayInputStream(Arrays.copyOf(leaf.getEncoded(), 100))));
    p("cf bad pem", () -> cf.generateCertificate(new ByteArrayInputStream("-----BEGIN CERTIFICATE-----\nAAAA\n-----END CERTIFICATE-----\n".getBytes())));
    p("cf pem no end", () -> cf.generateCertificate(new ByteArrayInputStream("-----BEGIN CERTIFICATE-----\nAAAA\n".getBytes())));
    p("cf pem text prefix", () -> cf.generateCertificate(new ByteArrayInputStream(("some text\n" + new String(ROOT_PEM.getBytes())).getBytes())).equals(root));
    p("cf pem crlf", () -> cf.generateCertificate(new ByteArrayInputStream(new String(ROOT_PEM.getBytes()).replace("\n", "\r\n").getBytes())).equals(root));
    p("cf pem trusted", () -> cf.generateCertificate(new ByteArrayInputStream(new String(ROOT_PEM.getBytes()).replace("BEGIN CERTIFICATE", "BEGIN TRUSTED CERTIFICATE").replace("END CERTIFICATE", "END TRUSTED CERTIFICATE").getBytes())));
    p("cf null", () -> cf.generateCertificate(null));
    p("cf bad type", () -> CertificateFactory.getInstance("FOO"));
    p("cf bad provider", () -> CertificateFactory.getInstance("X.509", "NOPE"));
    p("cf alias", () -> CertificateFactory.getInstance("X509").getType());
    p("cf certpath", () -> { CertPath cp = cf.generateCertPath(List.of(leaf, inter)); return cp.getType() + " " + cp.getCertificates().size() + " " + cp.getEncoded().length + " " + cp.getEncoded("PkiPath").length + " " + cp.getEncoded("PKCS7").length + " " + cf.getCertPathEncodings().next(); });
    p("cf certpath encodings", () -> { List<String> l = new ArrayList<>(); for (Iterator<String> it = cf.getCertPathEncodings(); it.hasNext();) l.add(it.next()); return l; });
    p("cf certpath roundtrip", () -> { CertPath cp = cf.generateCertPath(List.of(leaf, inter)); CertPath cp2 = cf.generateCertPath(new ByteArrayInputStream(cp.getEncoded())); CertPath cp3 = cf.generateCertPath(new ByteArrayInputStream(cp.getEncoded("PKCS7")), "PKCS7"); return cp.equals(cp2) + " " + cp.equals(cp3) + " " + cp2.getCertificates().size(); });
    p("cf certpath bad enc", () -> cf.generateCertPath(new ByteArrayInputStream(new byte[3]), "FOO"));
    p("cf certpath bad type", () -> cf.generateCertPath((List) List.of("x")));
    p("cf certpath empty", () -> cf.generateCertPath(List.of()).getCertificates().size());
    p("cf certpath toString", () -> cf.generateCertPath(List.of(root)).toString().length() > 100);
    p("cf pkcs7", () -> cf.generateCertificates(new ByteArrayInputStream(cf.generateCertPath(List.of(leaf, inter)).getEncoded("PKCS7"))).size());
    p("cf crl none", () -> cf.generateCRL(new ByteArrayInputStream(new byte[]{1, 2})));

    p("pkix valid", () -> { CertPath cp = cf.generateCertPath(List.of(leaf, inter)); PKIXParameters pp = new PKIXParameters(Set.of(new TrustAnchor(root, null))); pp.setRevocationEnabled(false); PKIXCertPathValidatorResult r = (PKIXCertPathValidatorResult) CertPathValidator.getInstance("PKIX").validate(cp, pp); return r.getTrustAnchor().getTrustedCert().equals(root) + " " + r.getPublicKey().equals(leaf.getPublicKey()); });
    p("pkix no anchor", () -> { CertPath cp = cf.generateCertPath(List.of(leaf, inter)); PKIXParameters pp = new PKIXParameters(Set.of(new TrustAnchor(leaf, null))); pp.setRevocationEnabled(false); CertPathValidator.getInstance("PKIX").validate(cp, pp); return "ok"; });
    p("pkix expired", () -> { CertPath cp = cf.generateCertPath(List.of(leaf, inter)); PKIXParameters pp = new PKIXParameters(Set.of(new TrustAnchor(root, null))); pp.setRevocationEnabled(false); pp.setDate(new Date(5000000000000L)); CertPathValidator.getInstance("PKIX").validate(cp, pp); return "ok"; });
    p("pkix skipped inter", () -> { CertPath cp = cf.generateCertPath(List.of(leaf)); PKIXParameters pp = new PKIXParameters(Set.of(new TrustAnchor(root, null))); pp.setRevocationEnabled(false); CertPathValidator.getInstance("PKIX").validate(cp, pp); return "ok"; });
    p("pkix builder", () -> { X509CertSelector sel = new X509CertSelector(); sel.setCertificate(leaf); PKIXBuilderParameters bp = new PKIXBuilderParameters(Set.of(new TrustAnchor(root, null)), sel); bp.setRevocationEnabled(false); bp.addCertStore(CertStore.getInstance("Collection", new CollectionCertStoreParameters(List.of(leaf, inter)))); PKIXCertPathBuilderResult r = (PKIXCertPathBuilderResult) CertPathBuilder.getInstance("PKIX").build(bp); return r.getCertPath().getCertificates().size(); });
    p("pkix empty anchors", () -> new PKIXParameters(new HashSet<TrustAnchor>()));
    p("certstore", () -> { CertStore cs = CertStore.getInstance("Collection", new CollectionCertStoreParameters(List.of(root, leaf))); X509CertSelector sel = new X509CertSelector(); sel.setSubject(leaf.getSubjectX500Principal()); return cs.getCertificates(sel).size() + " " + cs.getCertificates(null).size() + " " + cs.getType(); });
    p("selector", () -> { X509CertSelector sel = new X509CertSelector(); sel.setSerialNumber(leaf.getSerialNumber()); sel.setIssuer(leaf.getIssuerX500Principal()); return sel.match(leaf) + " " + sel.match(root); });
    p("cf crls empty", () -> cf.generateCRLs(new ByteArrayInputStream(new byte[0])).size());
  }
}
