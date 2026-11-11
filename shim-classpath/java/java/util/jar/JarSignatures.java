/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.util.jar;

import java.io.ByteArrayInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.CodeSigner;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.PublicKey;
import java.security.Signature;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import sun.security.util.DerInputStream;
import sun.security.util.DerValue;

/** Verifies the signature files of a signed jar and tracks which entries have been fully verified. */
final class JarSignatures {
  private final byte[] manifestBytes;
  private final Manifest manifest;
  private final Map<String, List<CodeSigner[]>> signersByEntry = new HashMap<String, List<CodeSigner[]>>();
  private final List<CodeSigner> manifestSigners = new ArrayList<CodeSigner>();
  private final Map<String, CodeSigner[]> verified = Collections.synchronizedMap(new HashMap<String, CodeSigner[]>());

  private JarSignatures(byte[] manifestBytes, Manifest manifest) {
    this.manifestBytes = manifestBytes;
    this.manifest = manifest;
  }

  static boolean isSignatureFile(String name) {
    String u = name.toUpperCase(java.util.Locale.ROOT);
    if (!u.startsWith("META-INF/") || u.indexOf('/', 9) >= 0) {
      return false;
    }
    return u.endsWith(".SF") || u.endsWith(".RSA") || u.endsWith(".DSA") || u.endsWith(".EC");
  }

  static JarSignatures load(JarFile jar, byte[] manifestBytes) throws IOException {
    Map<String, byte[]> sf = new LinkedHashMap<String, byte[]>();
    Map<String, byte[]> blocks = new HashMap<String, byte[]>();
    java.util.Enumeration<? extends ZipEntry> en = jar.rawEntries();
    while (en.hasMoreElements()) {
      ZipEntry ze = en.nextElement();
      String n = ze.getName();
      if (!isSignatureFile(n)) {
        continue;
      }
      String base = n.substring(9, n.lastIndexOf('.')).toUpperCase(java.util.Locale.ROOT);
      byte[] data;
      try (InputStream in = jar.rawInputStream(ze)) {
        data = in.readAllBytes();
      }
      if (n.toUpperCase(java.util.Locale.ROOT).endsWith(".SF")) {
        sf.put(base, data);
      } else {
        blocks.put(base, data);
      }
    }
    if (sf.isEmpty() || manifestBytes == null) {
      return null;
    }
    JarSignatures js = new JarSignatures(manifestBytes, new Manifest(new ByteArrayInputStream(manifestBytes)));
    Map<String, byte[]> sections = js.sections();
    boolean any = false;
    for (Map.Entry<String, byte[]> e : sf.entrySet()) {
      byte[] block = blocks.get(e.getKey());
      if (block == null) {
        continue;
      }
      CodeSigner[] signers = verifyBlock(block, e.getValue());
      js.addSignatureFile(e.getValue(), signers, sections);
      any = true;
    }
    return any ? js : null;
  }

  private void addSignatureFile(byte[] sfBytes, CodeSigner[] signers, Map<String, byte[]> sections)
      throws IOException {
    Manifest sf = new Manifest(new ByteArrayInputStream(sfBytes));
    Attributes main = sf.getMainAttributes();
    boolean wholeManifest = false;
    for (Object key : main.keySet()) {
      String k = key.toString();
      String upper = k.toUpperCase(java.util.Locale.ROOT);
      if (upper.endsWith("-DIGEST-MANIFEST")) {
        String alg = k.substring(0, k.length() - "-Digest-Manifest".length());
        if (matches(alg, manifestBytes, main.getValue(k))) {
          wholeManifest = true;
        } else {
          throw new SecurityException("invalid " + alg + " signature file digest for Manifest main attributes");
        }
      }
    }
    for (Map.Entry<String, Attributes> e : sf.getEntries().entrySet()) {
      byte[] section = sections.get(e.getKey());
      boolean ok = wholeManifest;
      for (Map.Entry<Object, Object> a : e.getValue().entrySet()) {
        String k = a.getKey().toString();
        if (k.toUpperCase(java.util.Locale.ROOT).endsWith("-DIGEST") && section != null) {
          String alg = k.substring(0, k.length() - "-Digest".length());
          if (matches(alg, section, a.getValue().toString())) {
            ok = true;
          } else {
            throw new SecurityException("invalid " + alg + " signature file digest for " + e.getKey());
          }
        }
      }
      if (ok) {
        signersByEntry.computeIfAbsent(e.getKey(), x -> new ArrayList<CodeSigner[]>()).add(signers);
      }
    }
    if (wholeManifest) {
      for (CodeSigner s : signers) {
        if (!manifestSigners.contains(s)) {
          manifestSigners.add(s);
        }
      }
      for (String name : manifest.getEntries().keySet()) {
        signersByEntry.computeIfAbsent(name, x -> new ArrayList<CodeSigner[]>());
        List<CodeSigner[]> l = signersByEntry.get(name);
        if (!containsSigners(l, signers)) {
          l.add(signers);
        }
      }
    }
  }

  private static boolean containsSigners(List<CodeSigner[]> l, CodeSigner[] s) {
    for (CodeSigner[] c : l) {
      if (Arrays.equals(c, s)) {
        return true;
      }
    }
    return false;
  }

  private static boolean matches(String alg, byte[] data, String b64) {
    try {
      byte[] d = MessageDigest.getInstance(alg).digest(data);
      return MessageDigest.isEqual(d, Base64.getMimeDecoder().decode(b64.trim()));
    } catch (GeneralSecurityException | IllegalArgumentException e) {
      return false;
    }
  }

  private Map<String, byte[]> sections() {
    Map<String, byte[]> out = new HashMap<String, byte[]>();
    int n = manifestBytes.length;
    int start = 0;
    int i = 0;
    while (i < n) {
      int lineEnd = i;
      while (lineEnd < n && manifestBytes[lineEnd] != '\n' && manifestBytes[lineEnd] != '\r') {
        lineEnd++;
      }
      int next = lineEnd;
      if (next < n && manifestBytes[next] == '\r') {
        next++;
      }
      if (next < n && manifestBytes[next] == '\n') {
        next++;
      }
      if (lineEnd == i) {
        if (i > start) {
          addSection(out, start, next);
        }
        start = next;
      }
      i = next;
      if (next == lineEnd) {
        i++;
      }
    }
    if (start < n) {
      addSection(out, start, n);
    }
    return out;
  }

  private void addSection(Map<String, byte[]> out, int from, int to) {
    String text = new String(manifestBytes, from, to - from, StandardCharsets.UTF_8);
    String unfolded = text.replace("\r\n ", "").replace("\n ", "").replace("\r ", "");
    for (String line : unfolded.split("\r\n|\n|\r")) {
      if (line.regionMatches(true, 0, "Name:", 0, 5)) {
        out.put(line.substring(5).trim(), Arrays.copyOfRange(manifestBytes, from, to));
        return;
      }
    }
  }

  private static final Map<String, String> kDigestOids = new HashMap<String, String>();

  static {
    kDigestOids.put("1.3.14.3.2.26", "SHA-1");
    kDigestOids.put("2.16.840.1.101.3.4.2.1", "SHA-256");
    kDigestOids.put("2.16.840.1.101.3.4.2.2", "SHA-384");
    kDigestOids.put("2.16.840.1.101.3.4.2.3", "SHA-512");
    kDigestOids.put("2.16.840.1.101.3.4.2.4", "SHA-224");
    kDigestOids.put("1.2.840.113549.2.5", "MD5");
    kDigestOids.put("2.16.840.1.101.3.4.2.7", "SHA3-224");
    kDigestOids.put("2.16.840.1.101.3.4.2.8", "SHA3-256");
    kDigestOids.put("2.16.840.1.101.3.4.2.9", "SHA3-384");
    kDigestOids.put("2.16.840.1.101.3.4.2.10", "SHA3-512");
  }

  private static CodeSigner[] verifyBlock(byte[] block, byte[] sfBytes) throws IOException {
    try {
      DerValue[] top = new DerInputStream(block).getSequence(2);
      DerValue signedData = new DerInputStream(top[1].getDataBytes()).getDerValue();
      DerInputStream sd = signedData.data();
      sd.getDerValue();
      sd.getDerValue();
      sd.getDerValue();
      List<X509Certificate> certs = new ArrayList<X509Certificate>();
      CertificateFactory cf = CertificateFactory.getInstance("X.509");
      DerValue next = sd.getDerValue();
      if (next.tag == (byte) 0xA0) {
        DerInputStream cs = next.data();
        while (cs.available() > 0) {
          certs.add((X509Certificate) cf.generateCertificate(new ByteArrayInputStream(cs.getDerValue().toByteArray())));
        }
        next = sd.getDerValue();
      }
      if (next.tag == (byte) 0xA1) {
        next = sd.getDerValue();
      }
      List<CodeSigner> signers = new ArrayList<CodeSigner>();
      DerInputStream infos = next.data();
      while (infos.available() > 0) {
        signers.add(verifySigner(infos.getDerValue(), certs, sfBytes, cf));
      }
      return signers.toArray(new CodeSigner[0]);
    } catch (GeneralSecurityException | RuntimeException e) {
      throw new SecurityException("invalid signature block file: " + e.getMessage(), e);
    }
  }

  private static CodeSigner verifySigner(DerValue info, List<X509Certificate> certs, byte[] sf, CertificateFactory cf)
      throws IOException, GeneralSecurityException {
    DerInputStream in = info.data();
    in.getDerValue();
    DerValue issuerSerial = in.getDerValue();
    DerInputStream is = issuerSerial.data();
    byte[] issuer = is.getDerValue().toByteArray();
    BigInteger serial = is.getDerValue().getBigInteger();
    String digestAlg = kDigestOids.get(new DerInputStream(in.getDerValue().getDataBytes()).getDerValue().getOID()
        .toString());
    if (digestAlg == null) {
      throw new GeneralSecurityException("unsupported digest algorithm");
    }
    DerValue v = in.getDerValue();
    DerValue attrs = null;
    if (v.tag == (byte) 0xA0) {
      attrs = v;
      v = in.getDerValue();
    }
    byte[] signature = in.getDerValue().getOctetString();
    X509Certificate signerCert = null;
    for (X509Certificate c : certs) {
      if (c.getSerialNumber().equals(serial) && Arrays.equals(c.getIssuerX500Principal().getEncoded(), issuer)) {
        signerCert = c;
        break;
      }
    }
    if (signerCert == null) {
      throw new GeneralSecurityException("signer certificate not found");
    }
    byte[] signed = sf;
    if (attrs != null) {
      byte[] md = MessageDigest.getInstance(digestAlg).digest(sf);
      boolean found = false;
      DerInputStream as = attrs.data();
      while (as.available() > 0) {
        DerInputStream attr = as.getDerValue().data();
        String oid = attr.getDerValue().getOID().toString();
        if (oid.equals("1.2.840.113549.1.9.4")) {
          byte[] got = new DerInputStream(attr.getDerValue().getDataBytes()).getDerValue().getOctetString();
          found = MessageDigest.isEqual(md, got);
        }
      }
      if (!found) {
        throw new SecurityException("invalid " + digestAlg + " signature file digest for message digest attribute");
      }
      signed = new DerValue((byte) 0x31, attrs.getDataBytes()).toByteArray();
    }
    PublicKey key = signerCert.getPublicKey();
    String keyAlg = key.getAlgorithm();
    String sigName;
    if (keyAlg.equals("RSA")) {
      sigName = digestAlg.replace("-", "") + "withRSA";
    } else if (keyAlg.equals("EC")) {
      sigName = digestAlg.replace("-", "") + "withECDSA";
    } else if (keyAlg.equals("DSA")) {
      sigName = digestAlg.replace("-", "") + "withDSA";
    } else {
      sigName = keyAlg;
    }
    Signature s = Signature.getInstance(sigName);
    s.initVerify(key);
    s.update(signed);
    if (!s.verify(signature)) {
      throw new SecurityException("SHA signature verification failed for the signature block file");
    }
    List<X509Certificate> chain = new ArrayList<X509Certificate>();
    chain.add(signerCert);
    X509Certificate cur = signerCert;
    boolean grew = true;
    while (grew) {
      grew = false;
      if (cur.getSubjectX500Principal().equals(cur.getIssuerX500Principal())) {
        break;
      }
      for (X509Certificate c : certs) {
        if (!chain.contains(c) && c.getSubjectX500Principal().equals(cur.getIssuerX500Principal())) {
          chain.add(c);
          cur = c;
          grew = true;
          break;
        }
      }
    }
    return new CodeSigner(cf.generateCertPath(chain), null);
  }

  CodeSigner[] signersOf(String name) {
    CodeSigner[] done = verified.get(name);
    return done == null ? null : done.clone();
  }

  boolean isSigned(String name) {
    return signersByEntry.containsKey(name);
  }

  boolean isManifest(String name) {
    return JarFile.MANIFEST_NAME.equalsIgnoreCase(name);
  }

  InputStream wrap(String name, InputStream raw) {
    if (isManifest(name)) {
      return new FilterInputStream(raw) {
        private boolean done;

        @Override
        public int read() throws IOException {
          int b = super.read();
          if (b < 0) {
            finish();
          }
          return b;
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
          int n = super.read(b, off, len);
          if (n < 0) {
            finish();
          }
          return n;
        }

        private void finish() {
          if (!done && !manifestSigners.isEmpty()) {
            done = true;
            verified.put(name, manifestSigners.toArray(new CodeSigner[0]));
          }
        }
      };
    }
    List<CodeSigner[]> lists = signersByEntry.get(name);
    Attributes attrs = manifest.getAttributes(name);
    if (lists == null || attrs == null) {
      return raw;
    }
    final List<MessageDigest> mds = new ArrayList<MessageDigest>();
    final List<byte[]> expected = new ArrayList<byte[]>();
    final List<String> algs = new ArrayList<String>();
    for (Map.Entry<Object, Object> a : attrs.entrySet()) {
      String k = a.getKey().toString();
      if (k.toUpperCase(java.util.Locale.ROOT).endsWith("-DIGEST")) {
        String alg = k.substring(0, k.length() - 7);
        try {
          mds.add(MessageDigest.getInstance(alg));
          expected.add(Base64.getMimeDecoder().decode(a.getValue().toString().trim()));
          algs.add(alg);
        } catch (GeneralSecurityException | IllegalArgumentException e) {
          continue;
        }
      }
    }
    if (mds.isEmpty()) {
      return raw;
    }
    final List<CodeSigner> merged = new ArrayList<CodeSigner>();
    for (CodeSigner[] l : lists) {
      for (CodeSigner s : l) {
        if (!merged.contains(s)) {
          merged.add(s);
        }
      }
    }
    return new FilterInputStream(raw) {
      private boolean done;

      @Override
      public int read() throws IOException {
        int b = super.read();
        if (b >= 0) {
          for (MessageDigest md : mds) {
            md.update((byte) b);
          }
        } else {
          finish();
        }
        return b;
      }

      @Override
      public int read(byte[] b, int off, int len) throws IOException {
        int n = super.read(b, off, len);
        if (n > 0) {
          for (MessageDigest md : mds) {
            md.update(b, off, n);
          }
        } else if (n < 0) {
          finish();
        }
        return n;
      }

      private void finish() {
        if (done) {
          return;
        }
        done = true;
        for (int i = 0; i < mds.size(); i++) {
          if (!MessageDigest.isEqual(mds.get(i).digest(), expected.get(i))) {
            throw new SecurityException(algs.get(i) + " digest error for " + name);
          }
        }
        verified.put(name, merged.toArray(new CodeSigner[0]));
      }
    };
  }
}
