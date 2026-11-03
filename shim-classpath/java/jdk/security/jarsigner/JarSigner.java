/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package jdk.security.jarsigner;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.CertPath;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public final class JarSigner {
  private static final Set<String> PROPERTY_KEYS = new HashSet<>(java.util.Arrays.asList("tsadigestalg",
      "tsapolicyid", "internalsf", "sectionsonly"));

  public static class Builder {
    private final PrivateKey privateKey;
    private final X509Certificate[] certChain;
    private final Map<String, String> properties = new HashMap<>();
    private String digestalg = "SHA-384";
    private String sigalg;
    private Provider digestProvider;
    private Provider sigProvider;
    private URI tsaUrl;
    private String signerName;
    private BiConsumer<String, String> handler;

    public Builder(PrivateKey privateKey, CertPath certPath) {
      List<? extends Certificate> certs = certPath.getCertificates();
      if (certs.isEmpty()) {
        throw new IllegalArgumentException("certPath cannot be empty");
      }
      if (privateKey == null) {
        throw new NullPointerException("privateKey");
      }
      this.privateKey = privateKey;
      this.certChain = certs.toArray(new X509Certificate[certs.size()]);
      this.sigalg = getDefaultSignatureAlgorithm(privateKey);
    }

    public Builder(KeyStore.PrivateKeyEntry entry) {
      this.privateKey = entry.getPrivateKey();
      Certificate[] chain = entry.getCertificateChain();
      this.certChain = new X509Certificate[chain.length];
      for (int i = 0; i < chain.length; i++) {
        this.certChain[i] = (X509Certificate) chain[i];
      }
      this.sigalg = getDefaultSignatureAlgorithm(privateKey);
    }

    public Builder digestAlgorithm(String algorithm) throws NoSuchAlgorithmException {
      MessageDigest.getInstance(Objects.requireNonNull(algorithm));
      this.digestalg = algorithm;
      this.digestProvider = null;
      return this;
    }

    public Builder digestAlgorithm(String algorithm, Provider provider) throws NoSuchAlgorithmException {
      MessageDigest.getInstance(Objects.requireNonNull(algorithm), Objects.requireNonNull(provider));
      this.digestalg = algorithm;
      this.digestProvider = provider;
      return this;
    }

    public Builder signatureAlgorithm(String algorithm) throws NoSuchAlgorithmException {
      Signature.getInstance(Objects.requireNonNull(algorithm));
      this.sigalg = algorithm;
      this.sigProvider = null;
      return this;
    }

    public Builder signatureAlgorithm(String algorithm, Provider provider) throws NoSuchAlgorithmException {
      Signature.getInstance(Objects.requireNonNull(algorithm), Objects.requireNonNull(provider));
      this.sigalg = algorithm;
      this.sigProvider = provider;
      return this;
    }

    public Builder tsa(URI uri) {
      this.tsaUrl = Objects.requireNonNull(uri);
      return this;
    }

    public Builder signerName(String name) {
      if (name.isEmpty() || name.length() > 8) {
        throw new IllegalArgumentException("Name too long");
      }
      name = name.toUpperCase(Locale.ENGLISH);
      for (int i = 0; i < name.length(); i++) {
        char c = name.charAt(i);
        if (!((c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9') || c == '-' || c == '_')) {
          throw new IllegalArgumentException("Invalid characters in name");
        }
      }
      this.signerName = name;
      return this;
    }

    public Builder setProperty(String key, String value) {
      Objects.requireNonNull(key);
      Objects.requireNonNull(value);
      key = key.toLowerCase(Locale.US);
      if (!PROPERTY_KEYS.contains(key)) {
        throw new UnsupportedOperationException("Unsupported key " + key);
      }
      if (key.equals("tsadigestalg")) {
        try {
          MessageDigest.getInstance(value);
        } catch (NoSuchAlgorithmException nsae) {
          throw new IllegalArgumentException("Invalid tsadigestalg", nsae);
        }
      } else if (key.equals("internalsf") || key.equals("sectionsonly")) {
        if (!value.equals("true") && !value.equals("false")) {
          throw new IllegalArgumentException("Invalid " + (key.equals("internalsf") ? "interalsf" : key) + " value");
        }
      }
      properties.put(key, value);
      return this;
    }

    public Builder eventHandler(BiConsumer<String, String> handler) {
      this.handler = Objects.requireNonNull(handler);
      return this;
    }

    public static String getDefaultDigestAlgorithm() {
      return "SHA-384";
    }

    public static String getDefaultSignatureAlgorithm(PrivateKey key) {
      final String alg = key.getAlgorithm();
      if (alg.equals("RSA")) {
        int size = ((java.security.interfaces.RSAKey) key).getModulus().bitLength();
        return size <= 3072 ? "SHA384withRSA" : "SHA512withRSA";
      } else if (alg.equals("DSA")) {
        return "SHA256withDSA";
      } else if (alg.equals("EC")) {
        int size = ((java.security.interfaces.ECKey) key).getParams().getOrder().bitLength();
        return size <= 384 ? "SHA384withECDSA" : "SHA512withECDSA";
      } else if (alg.equals("Ed25519") || alg.equals("EdDSA")) {
        return "Ed25519";
      } else if (alg.equals("Ed448")) {
        return "Ed448";
      }
      throw new IllegalArgumentException("Unsupported key algorithm " + alg);
    }

    public JarSigner build() {
      return new JarSigner(this);
    }
  }

  private final PrivateKey privateKey;
  private final X509Certificate[] certChain;
  private final Map<String, String> properties;
  private final String digestalg;
  private final String sigalg;
  private final Provider digestProvider;
  private final Provider sigProvider;
  private final URI tsaUrl;
  private final String signerName;
  private final BiConsumer<String, String> handler;

  private JarSigner(Builder builder) {
    this.privateKey = builder.privateKey;
    this.certChain = builder.certChain;
    this.digestalg = builder.digestalg;
    this.digestProvider = builder.digestProvider;
    this.sigalg = builder.sigalg;
    this.sigProvider = builder.sigProvider;
    this.tsaUrl = builder.tsaUrl;
    this.signerName = builder.signerName == null ? "SIGNER" : builder.signerName;
    this.handler = builder.handler;
    this.properties = new HashMap<>(builder.properties);
    String keyAlg = privateKey.getAlgorithm();
    String sa = sigalg.toUpperCase(Locale.ENGLISH);
    if (!(sa.endsWith("WITH" + keyAlg.toUpperCase(Locale.ENGLISH)) || (keyAlg.equals("EC") && sa.endsWith("WITHECDSA"))
        || sa.equals(keyAlg.toUpperCase(Locale.ENGLISH)) || sa.equals("RSASSA-PSS") && keyAlg.startsWith("RSA"))) {
      throw new IllegalArgumentException("key algorithm not compatible with signature algorithm");
    }
  }

  public String getDigestAlgorithm() {
    return digestalg;
  }

  public String getSignatureAlgorithm() {
    return sigalg;
  }

  public URI getTsa() {
    return tsaUrl;
  }

  public String getSignerName() {
    return signerName;
  }

  public String getProperty(String key) {
    key = Objects.requireNonNull(key).toLowerCase(Locale.US);
    if (!PROPERTY_KEYS.contains(key)) {
      throw new UnsupportedOperationException("Unsupported key " + key);
    }
    String v = properties.get(key);
    if (v != null) {
      return v;
    }
    switch (key) {
      case "tsadigestalg":
        return "SHA-384";
      case "internalsf":
      case "sectionsonly":
        return "false";
      default:
        return null;
    }
  }

  private void event(String action, String name) {
    if (handler != null) {
      handler.accept(action, name);
    }
  }

  private MessageDigest newDigest() throws NoSuchAlgorithmException {
    return digestProvider == null ? MessageDigest.getInstance(digestalg)
        : MessageDigest.getInstance(digestalg, digestProvider);
  }

  public void sign(ZipFile file, OutputStream os) {
    try {
      sign0(Objects.requireNonNull(file), Objects.requireNonNull(os));
    } catch (SignatureException | InvalidKeyException | IOException | NoSuchAlgorithmException
        | CertificateEncodingException | java.security.InvalidAlgorithmParameterException e) {
      throw new JarSignerException("Error applying signature", e);
    }
  }

  private static boolean isAnySigFile(String name) {
    String upper = name.toUpperCase(Locale.ENGLISH);
    if (!upper.startsWith("META-INF/") || upper.indexOf('/', 9) >= 0) {
      return false;
    }
    return upper.endsWith(".SF") || upper.endsWith(".RSA") || upper.endsWith(".DSA") || upper.endsWith(".EC");
  }

  private static boolean isSigFile(String name, String signer) {
    String upper = name.toUpperCase(Locale.ENGLISH);
    if (!upper.startsWith("META-INF/")) {
      return false;
    }
    String base = upper.substring(9);
    int dot = base.lastIndexOf('.');
    if (dot < 0) {
      return false;
    }
    String ext = base.substring(dot + 1);
    if (!(ext.equals("SF") || ext.equals("RSA") || ext.equals("DSA") || ext.equals("EC"))) {
      return false;
    }
    return base.substring(0, dot).equals(signer);
  }

  private void sign0(ZipFile zipFile, OutputStream os) throws IOException, NoSuchAlgorithmException,
      SignatureException, InvalidKeyException, CertificateEncodingException,
      java.security.InvalidAlgorithmParameterException {
    String algName = digestalg;
    Manifest manifest = new Manifest();
    Map<String, Attributes> entries = manifest.getEntries();
    boolean hasManifest = false;
    boolean mfModified = false;
    byte[] mfRaw = null;
    List<ZipEntry> zes = new ArrayList<>();
    for (Enumeration<? extends ZipEntry> en = zipFile.entries(); en.hasMoreElements();) {
      ZipEntry ze = en.nextElement();
      if (ze.getName().equalsIgnoreCase(JarFile.MANIFEST_NAME)) {
        hasManifest = true;
        try (InputStream is = zipFile.getInputStream(ze)) {
          mfRaw = is.readAllBytes();
        }
        manifest.read(new ByteArrayInputStream(mfRaw));
      } else {
        zes.add(ze);
      }
    }
    Attributes mattr = manifest.getMainAttributes();
    if (!hasManifest) {
      mattr.putValue("Manifest-Version", "1.0");
      mattr.putValue("Created-By", System.getProperty("java.version") + " (" + System.getProperty("java.vendor") + ")");
      mfModified = true;
    }
    final String digestAttr = algName + "-Digest";
    boolean existingSf = false;
    boolean existingBlock = false;
    String blockExt = privateKey.getAlgorithm().equals("RSA") ? "RSA" : privateKey.getAlgorithm().equals("DSA")
        ? "DSA" : "EC";
    final List<ZipEntry> kept = new ArrayList<>();
    for (ZipEntry ze : zes) {
      if (isSigFile(ze.getName(), signerName)) {
        if (ze.getName().toUpperCase(Locale.ENGLISH).endsWith(".SF")) {
          existingSf = true;
        } else {
          existingBlock = true;
        }
        continue;
      }
      kept.add(ze);
    }
    for (ZipEntry ze : kept) {
      String name = ze.getName();
      if (ze.isDirectory() || isAnySigFile(name)) {
        continue;
      }
      MessageDigest md = newDigest();
      try (InputStream is = zipFile.getInputStream(ze)) {
        byte[] buf = new byte[8192];
        int n;
        while ((n = is.read(buf)) >= 0) {
          md.update(buf, 0, n);
        }
      }
      String b64 = Base64.getEncoder().encodeToString(md.digest());
      Attributes attrs = entries.get(name);
      if (attrs == null) {
        attrs = new Attributes();
        entries.put(name, attrs);
        mfModified = true;
      }
      String old = attrs.getValue(digestAttr);
      if (!b64.equals(old)) {
        attrs.putValue(digestAttr, b64);
        mfModified = true;
      }
    }
    byte[] mfBytes;
    if (mfModified || mfRaw == null) {
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      manifest.write(bos);
      mfBytes = bos.toByteArray();
    } else {
      mfBytes = mfRaw;
    }
    event(hasManifest && mfModified ? "updating" : "adding", JarFile.MANIFEST_NAME);
    final String sfName = "META-INF/" + signerName + ".SF";
    final String blockName = "META-INF/" + signerName + "." + blockExt;
    byte[] sfBytes = buildSignatureFile(manifest, mfBytes);
    event(existingSf ? "updating" : "adding", sfName);
    byte[] block = buildBlock(sfBytes);
    event(existingBlock ? "updating" : "adding", blockName);
    try (ZipOutputStream zos = new ZipOutputStream(os)) {
      putBytes(zos, JarFile.MANIFEST_NAME, mfBytes);
      putBytes(zos, sfName, sfBytes);
      putBytes(zos, blockName, block);
      for (ZipEntry ze : kept) {
        ZipEntry out = new ZipEntry(ze.getName());
        if (ze.getTime() != -1) {
          out.setTime(ze.getTime());
        }
        zos.putNextEntry(out);
        if (!ze.isDirectory()) {
          try (InputStream is = zipFile.getInputStream(ze)) {
            is.transferTo(zos);
          }
        }
        event(ze.isDirectory() || isAnySigFile(ze.getName()) ? "adding" : "signing", ze.getName());
        zos.closeEntry();
      }
    }
  }

  private static void putBytes(ZipOutputStream zos, String name, byte[] bytes) throws IOException {
    zos.putNextEntry(new ZipEntry(name));
    zos.write(bytes);
    zos.closeEntry();
  }

  private byte[] buildSignatureFile(Manifest manifest, byte[] mfBytes) throws IOException, NoSuchAlgorithmException {
    final String alg = digestalg;
    Manifest sf = new Manifest();
    Attributes main = sf.getMainAttributes();
    main.putValue("Signature-Version", "1.0");
    main.putValue("Created-By", System.getProperty("java.version") + " (" + System.getProperty("java.vendor") + ")");
    MessageDigest md = newDigest();
    byte[] mfDigest = md.digest(mfBytes);
    main.putValue(alg + "-Digest-Manifest", Base64.getEncoder().encodeToString(mfDigest));
    int mainEnd = indexOfBlankLine(mfBytes);
    md = newDigest();
    byte[] mainDigest = md.digest(java.util.Arrays.copyOfRange(mfBytes, 0, mainEnd));
    main.putValue(alg + "-Digest-Manifest-Main-Attributes", Base64.getEncoder().encodeToString(mainDigest));
    for (Map.Entry<String, Attributes> en : manifest.getEntries().entrySet()) {
      ByteArrayOutputStream section = new ByteArrayOutputStream();
      writeSection(section, en.getKey(), en.getValue());
      Attributes a = new Attributes();
      md = newDigest();
      a.putValue(alg + "-Digest", Base64.getEncoder().encodeToString(md.digest(section.toByteArray())));
      sf.getEntries().put(en.getKey(), a);
    }
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    sf.write(bos);
    return bos.toByteArray();
  }

  private static void writeSection(OutputStream out, String name, Attributes attrs) throws IOException {
    Manifest m = new Manifest();
    m.getMainAttributes().putValue("Manifest-Version", "1.0");
    m.getEntries().put(name, attrs);
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    m.write(bos);
    byte[] all = bos.toByteArray();
    int start = indexOfBlankLine(all);
    out.write(all, start, all.length - start);
  }

  private static int indexOfBlankLine(byte[] b) {
    for (int i = 0; i + 3 < b.length; i++) {
      if (b[i] == '\r' && b[i + 1] == '\n' && b[i + 2] == '\r' && b[i + 3] == '\n') {
        return i + 4;
      }
    }
    return b.length;
  }

  private byte[] buildBlock(byte[] sf) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException,
      CertificateEncodingException, java.security.InvalidAlgorithmParameterException, IOException {
    final String digestOid = JarSignerDer.digestOid(sigalg, digestalg);
    final String sigOid = JarSignerDer.signatureOid(sigalg);
    final boolean pss = sigalg.equals("RSASSA-PSS");
    final boolean nullParams = !pss && !(sigalg.toUpperCase(Locale.ENGLISH).contains("DSA") && !sigalg.toUpperCase(
        Locale.ENGLISH).contains("RSA")) && !sigalg.startsWith("Ed");
    MessageDigest md = MessageDigest.getInstance(JarSignerDer.digestName(sigalg, digestalg));
    byte[] messageDigest = md.digest(sf);
    byte[] digestAlgId = JarSignerDer.algId(digestOid, true);
    final int saltLen = md.getDigestLength();
    final byte[] pssParams = pss ? JarSignerDer.pssParams(digestOid, saltLen) : null;
    byte[] sigAlgId = pss ? JarSignerDer.seq(JarSignerDer.oid(sigOid), pssParams)
        : JarSignerDer.algId(sigOid, nullParams);
    byte[] sigAlgContent = pss ? JarSignerDer.concat(JarSignerDer.oid(sigOid), pssParams)
        : JarSignerDer.algIdContent(sigOid, nullParams);
    List<byte[]> attrs = new ArrayList<>();
    attrs.add(JarSignerDer.seq(JarSignerDer.oid("1.2.840.113549.1.9.3"), JarSignerDer.set(JarSignerDer.oid(
        "1.2.840.113549.1.7.1"))));
    attrs.add(JarSignerDer.seq(JarSignerDer.oid("1.2.840.113549.1.9.5"), JarSignerDer.set(JarSignerDer.utcNow())));
    attrs.add(JarSignerDer.seq(JarSignerDer.oid("1.2.840.113549.1.9.52"), JarSignerDer.set(JarSignerDer.seq(
        digestAlgId, JarSignerDer.tagged(0xA1, sigAlgContent)))));
    attrs.add(JarSignerDer.seq(JarSignerDer.oid("1.2.840.113549.1.9.4"), JarSignerDer.set(JarSignerDer.octets(
        messageDigest))));
    Collections.sort(attrs, JarSignerDer.BYTE_ORDER);
    byte[] attrsContent = JarSignerDer.concat(attrs);
    byte[] toSign = JarSignerDer.tagged(0x31, attrsContent);
    Signature s = sigProvider == null ? Signature.getInstance(sigalg) : Signature.getInstance(sigalg, sigProvider);
    if (pss) {
      String dn = JarSignerDer.digestName(sigalg, digestalg);
      s.setParameter(new java.security.spec.PSSParameterSpec(dn, "MGF1", new java.security.spec.MGF1ParameterSpec(dn),
          saltLen, 1));
    }
    s.initSign(privateKey);
    s.update(toSign);
    byte[] sigBytes = s.sign();
    X509Certificate signer = certChain[0];
    byte[] issuerAndSerial = JarSignerDer.seq(signer.getIssuerX500Principal().getEncoded(), JarSignerDer.integer(
        signer.getSerialNumber()));
    byte[] signerInfo;
    if (tsaUrl != null) {
      byte[] token = fetchTimestamp(sigBytes);
      byte[] unsigned = JarSignerDer.tagged(0xA1, JarSignerDer.seq(JarSignerDer.oid("1.2.840.113549.1.9.16.2.14"),
          JarSignerDer.set(token)));
      signerInfo = JarSignerDer.seq(JarSignerDer.integer(java.math.BigInteger.ONE), issuerAndSerial, digestAlgId,
          JarSignerDer.tagged(0xA0, attrsContent), sigAlgId, JarSignerDer.octets(sigBytes), unsigned);
    } else {
      signerInfo = JarSignerDer.seq(JarSignerDer.integer(java.math.BigInteger.ONE), issuerAndSerial,
          digestAlgId, JarSignerDer.tagged(0xA0, attrsContent), sigAlgId, JarSignerDer.octets(sigBytes));
    }
    List<byte[]> certs = new ArrayList<>();
    for (X509Certificate c : certChain) {
      certs.add(c.getEncoded());
    }
    byte[] signedData = JarSignerDer.seq(JarSignerDer.integer(java.math.BigInteger.ONE), JarSignerDer.set(
        digestAlgId), JarSignerDer.seq(JarSignerDer.oid("1.2.840.113549.1.7.1")), JarSignerDer.tagged(0xA0,
            JarSignerDer.concat(certs)), JarSignerDer.set(signerInfo));
    return JarSignerDer.seq(JarSignerDer.oid("1.2.840.113549.1.7.2"), JarSignerDer.tagged(0xA0, signedData));
  }

  private byte[] fetchTimestamp(byte[] signature) throws IOException, NoSuchAlgorithmException {
    String tsaAlg = getProperty("tsadigestalg");
    MessageDigest md = MessageDigest.getInstance(tsaAlg);
    byte[] imprint = md.digest(signature);
    String policy = getProperty("tsapolicyid");
    byte[] nonce = new byte[8];
    new java.security.SecureRandom().nextBytes(nonce);
    List<byte[]> parts = new ArrayList<>();
    parts.add(JarSignerDer.integer(java.math.BigInteger.ONE));
    parts.add(JarSignerDer.seq(JarSignerDer.algId(JarSignerDer.digestOid("", tsaAlg), true), JarSignerDer.octets(imprint)));
    if (policy != null) {
      parts.add(JarSignerDer.oid(policy));
    }
    parts.add(JarSignerDer.integer(new java.math.BigInteger(1, nonce)));
    parts.add(new byte[] {0x01, 0x01, (byte) 0xFF});
    byte[] req = JarSignerDer.seq(parts.toArray(new byte[0][]));
    java.net.HttpURLConnection c = (java.net.HttpURLConnection) tsaUrl.toURL().openConnection();
    c.setDoOutput(true);
    c.setRequestMethod("POST");
    c.setRequestProperty("Content-Type", "application/timestamp-query");
    c.setRequestProperty("Accept", "application/timestamp-reply");
    try (OutputStream o = c.getOutputStream()) {
      o.write(req);
    }
    byte[] resp;
    try (InputStream in = c.getInputStream()) {
      resp = in.readAllBytes();
    }
    sun.security.util.DerInputStream top = new sun.security.util.DerValue(resp).data();
    sun.security.util.DerValue status = top.getDerValue();
    int st = status.data().getDerValue().getBigInteger().intValue();
    if (st > 1) {
      throw new IOException("Error generating timestamp: status " + st);
    }
    return top.getDerValue().toByteArray();
  }
}
