/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.cert.CertPath;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
import jdk.security.jarsigner.JarSigner;

public class JarSignerBasics {
  static final String KEY = "MIIEvAIBADANBgkqhkiG9w0BAQEFAASCBKYwggSiAgEAAoIBAQCpmdFmhDd7VVQDwgh/4aS0T+/c1IWHcMYIeXxfvc8f3i3Mc5JTdEtrOIHfwaERrnmF9mLhOX0p4oArq4K7HPozOipZ71EOKx39lqi+voeub5AqEffvaYs7yYLCiNXcTtOqCbrHnZmfVYbAt9WXJJffAtAArxtw6uI641576JcAzAMWXVnurB9Qys8X8gI8H85oJ6ssRyug2WZDwj1/mbA2MrU4rEdkYczxIJI1t8G4NoZglPGKXWUvAIBZxLi09cI0btwnoe02UqCvQhocmUkB21/7RqUvQM0pPHQZYrj4NyCITvA2sEozdo1k0fdoMxjyIB49w7S+iYT0kZ0/pI3BAgMBAAECggEAANaTSZ+OgDOGTXlDsEU7Uore5/ZY22f0gliOe5EWMEt9aixVvDfh1tmc8JQMAqDAQff/NjMF7Rlk/AxEPm5Fxzewy4xlgAORAaypRG2v2TIU7CyN90XsHsBRvyIoxYqqIJntHWcYOhiguJBzBcToA02f3VJAOVOvmPIwYIYcS5Awfa6VX25t5WppyEe22XTR0NmD6G+d1u1hZ0C1WoTkt353D3Kl35Q61O1OVK34uP4GBftVFQQRsLrbbf8t6cY918ykQkynelh7w14O4geoolYISe68TvCxtUd4adZp+OcFdvS3dcdcHb37S6QH9LHUivZXmRiDb9cTmKamjCnYAQKBgQDZTtjo/g02322U29AwbqXlBJ39JUry79v1s5GOokHNeka/np6QohipdBlLHua5WlY7+Fc2pw6TP7U5te4S5JVUrZazL1HsNWCRWjKbsYG3G0veib9Gg5hgHJtzvVltIwVB3/vMDQ13aOXwAdlWPXRoRL5rDlnFIruvRLiRFfAoAQKBgQDHzGxT+uqZxfm3aIhR1aRHYrVBolbcigmdXKorwG9GbLCGqGZnVJ+7cMRvJq6HyLFpHfjXbMQEhJ2VxQ9RdsrEHb86nXYn2z9PA8qkpUa9fMN/pLZE9H7NB1mtT3b6Mn2XyZAndrSg5DMyDmBBQKZSSnOSaIrliSUpTQCLxc5lwQKBgGevbWPORDPYa4rJTw6s3M7nYxK0Royf0c3sPhlWZhtKqpMndfUAws+nq6HiPqhV6Ct5FPPoxdWQYkIFAnFVS8M5iFw6ydXwqgDwjpEnIwNhLNQj2XYluACz5aBVymN+gDWsT5JO8Av3qy7XUwIJFI/jrQ+bq02agv7fxUjEtPABAoGAcL9sNiv3bZVp6NUlcEDGdr4+GO66Y7PF3zZmlU//u1ckeqjXOOJpTUZCo5LJlLhtXStbvH2VVBNeNiFBFrCZlfhPbI1lGvK/lCBIn4bhF5crX4Gcleb/E0Zdlc6tDl2lwB0vqwVywwRj25B/QvNvqCp5c5ppH0S4YskgUL/sTEECgYBm4ToniVOdtRR36qfTclMxujSBlVAjCPK6LYhCQONVuUwgzINglUvOYRL3ZAUkAY3A6dWHQX5w8qRV9PhCVRRKsusLqY/45kNSGXFWAO6f5IjJElsMckwehm6GXBTRlzToAa8FeZXaRI7uP/+0WvHF+rbIn3Ob65tO/PYrYNtlxA==";
  static final String CERT = "MIIDKzCCAhOgAwIBAgIUPr23ihaoR2oLFd59vw9uB+lmOj4wDQYJKoZIhvcNAQELBQAwJTESMBAGA1UEAwwJU2hpbSBUZXN0MQ8wDQYDVQQKDAZCYXNhbHQwHhcNMjYxMTAxMDMzOTEwWhcNMzYxMDI5MDMzOTEwWjAlMRIwEAYDVQQDDAlTaGltIFRlc3QxDzANBgNVBAoMBkJhc2FsdDCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBAKmZ0WaEN3tVVAPCCH/hpLRP79zUhYdwxgh5fF+9zx/eLcxzklN0S2s4gd/BoRGueYX2YuE5fSnigCurgrsc+jM6KlnvUQ4rHf2WqL6+h65vkCoR9+9pizvJgsKI1dxO06oJusedmZ9VhsC31Zckl98C0ACvG3Dq4jrjXnvolwDMAxZdWe6sH1DKzxfyAjwfzmgnqyxHK6DZZkPCPX+ZsDYytTisR2RhzPEgkjW3wbg2hmCU8YpdZS8AgFnEuLT1wjRu3Ceh7TZSoK9CGhyZSQHbX/tGpS9AzSk8dBliuPg3IIhO8DawSjN2jWTR92gzGPIgHj3DtL6JhPSRnT+kjcECAwEAAaNTMFEwHQYDVR0OBBYEFC5/bu8AHKX0TW7tRNc4mnwjOhrDMB8GA1UdIwQYMBaAFC5/bu8AHKX0TW7tRNc4mnwjOhrDMA8GA1UdEwEB/wQFMAMBAf8wDQYJKoZIhvcNAQELBQADggEBAHscXGbXqW6AAsALk4eQKTbcgTDBKvxSSW9kaDXmY/b86jz3QZKWI5GMN+GssW44ruaonq0A56qHv4ch7HUudMokMEnz0xHlMTpWWgZ9LwlX9KNixg8fPXVJLJiiTx5NAtCS8hvuikN3JMKgYVJ9ACpvUg7wJkTMbJ7aiSEDZLcl2NiZ8zcBtX7OD/19yN9VgWmyq9TcQpD2l6vQzlfmO09c3oXZQBYoNtxDnjFnvN40Guajy9hGTaJSxZdRuHB2AchCXbdf2/BGdI+DrQyvS3RI96xmxdd+IZHZXbg4dKzjTeJzQo3OKvS7QV3e8RJsxIFlO95Ynz1xBlGttrcQD4w=";

  static byte[] jar(boolean withManifest) throws Exception {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    try (ZipOutputStream z = new ZipOutputStream(bos)) {
      if (withManifest) {
        z.putNextEntry(new ZipEntry("META-INF/MANIFEST.MF"));
        z.write("Manifest-Version: 1.0\r\nMain-Class: X\r\n\r\n".getBytes());
        z.closeEntry();
      }
      for (String n : new String[] {"zeta", "alpha", "mid/x", "Beta", "m2", "a1", "dir/", "dir/sub/q"}) {
        z.putNextEntry(new ZipEntry(n));
        if (!n.endsWith("/")) {
          z.write(n.getBytes());
        }
        z.closeEntry();
      }
    }
    return bos.toByteArray();
  }

  static void show(String title, byte[] signed, List<String> events) throws Exception {
    File f = File.createTempFile("jsb", ".jar");
    try (FileOutputStream o = new FileOutputStream(f)) {
      o.write(signed);
    }
    System.out.println("== " + title + " " + events);
    try (ZipFile z = new ZipFile(f)) {
      for (Enumeration<? extends ZipEntry> e = z.entries(); e.hasMoreElements();) {
        ZipEntry en = e.nextElement();
        System.out.println(en.getName() + (en.getName().endsWith(".RSA") ? " block" : ""));
      }
      for (String n : new String[] {"META-INF/MANIFEST.MF", "META-INF/TESTER.SF"}) {
        System.out.println("-- " + n);
        for (String line : new String(z.getInputStream(z.getEntry(n)).readAllBytes()).split("\r?\n", -1)) {
          if (!line.startsWith("Created-By")) {
            System.out.println(line);
          }
        }
      }
    }
    f.delete();
  }

  public static void main(String[] args) throws Exception {
    System.setProperty("java.version", "25");
    System.setProperty("java.vendor", "Test");
    PrivateKey key = KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(Base64.getDecoder().decode(KEY)));
    CertificateFactory cf = CertificateFactory.getInstance("X.509");
    X509Certificate cert = (X509Certificate) cf.generateCertificate(new ByteArrayInputStream(Base64.getDecoder().decode(CERT)));
    CertPath path = cf.generateCertPath(Collections.singletonList(cert));
    System.out.println(JarSigner.Builder.getDefaultDigestAlgorithm() + " " + JarSigner.Builder.getDefaultSignatureAlgorithm(key));
    for (boolean withManifest : new boolean[] {false, true}) {
      List<String> events = new ArrayList<>();
      JarSigner signer = new JarSigner.Builder(key, path).signerName("tester").digestAlgorithm("SHA-256")
          .eventHandler((a, n) -> events.add(a + ":" + n)).build();
      System.out.println(signer.getSignerName() + " " + signer.getDigestAlgorithm() + " " + signer.getSignatureAlgorithm()
          + " " + signer.getProperty("sectionsonly") + " " + signer.getProperty("tsadigestalg"));
      File in = File.createTempFile("jsb", ".jar");
      try (FileOutputStream o = new FileOutputStream(in)) {
        o.write(jar(withManifest));
      }
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      try (ZipFile z = new ZipFile(in)) {
        signer.sign(z, out);
      }
      in.delete();
      show(withManifest ? "with manifest" : "without manifest", out.toByteArray(), events);
    }
    try {
      new JarSigner.Builder(key, path).signerName("waytoolongname");
    } catch (IllegalArgumentException e) {
      System.out.println(e.getMessage());
    }
    try {
      new JarSigner.Builder(key, path).setProperty("zzz", "1");
    } catch (UnsupportedOperationException e) {
      System.out.println(e.getMessage());
    }
  }
}
