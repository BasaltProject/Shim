/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 * Minimum release: 25
 * javac-args: --enable-preview --release 25
 * jvm-args: --enable-preview
 */

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.cert.*;
import java.security.spec.*;
import java.util.*;
import java.util.concurrent.Callable;
import javax.crypto.*;

public class PemApiModern {
  static final Map<String, String> kPem = new HashMap<>();

  static {
    kPem.put("rsa.pk8.pem", """
-----BEGIN PRIVATE KEY-----
MIICdQIBADANBgkqhkiG9w0BAQEFAASCAl8wggJbAgEAAoGBALX6ZnqQJr2qxxji
uvVBLibn98JitWCETYP7u26VbyBuapAQj0G+JQD+8oncDrJ2essT2fqNJCe6y9hI
Qn9FIYNZDnudf7jw/Uixgi0aM2RL53w1ihjbQYphPFGEjq3V05ngJXTW1e1Uz0zf
/6aa6h9rBIJClAAAHXeDtLhuwJJJAgMBAAECgYAsJxYINMOBj1cBPbm42di1zeB6
RHLXeSJJnKFMYmt1BfVLAQmRHO5jgHgeZMsYF9ERRr7DoM0d6p+c2WNw97f6enki
lPrftwuxkK1H9/C+XW1YS9tfiMZ7EUQ1HwddPp3hNXBOD+wALD3X6rXIzfL04bKj
y3HH6ZqkXa/GzxI49QJBAOeIIffOKGrVGqPG4e1EZLQPunbguf6nQHPeovhBqhfX
+GNFzjqVkXn7bNCxRgrBwragXLdOdLbYv6YcHjgQLDMCQQDJNaRU3b8umcTaT9m7
6z4GUrKsGqQi0BiiXpfgQKVNVPhrq8URMrBPOkQoheFhZGJcdnnnl/wGjwz2f/pl
KwuTAkAXfH+1m3wOGj/xjSMt5lowyceod3NmmaC4IEV32XFfqdDQgSxCVRnEvfRz
1we735vY2ruM/LBqO+J0/zUxirJxAkAyQ8hNuyeriJZ4w0HthBL2mWoxPGaMm9fL
mD3ws7WX4AY/P6X3aeZJpJCqXuey4NUBrEc/5+P573qZidcPtvUpAkBGeWIGuDsV
QjgXjNsMbdvMcHd2mXNIAlh2BpJyTso1qyWOHRd7kJKy7anf3LVp/lJHDLYEp3ts
eK8T+3jl4jZV
-----END PRIVATE KEY-----
""");
    kPem.put("ec.pk8.pem", """
-----BEGIN PRIVATE KEY-----
MIGHAgEAMBMGByqGSM49AgEGCCqGSM49AwEHBG0wawIBAQQgzZDGBd0LT+rgjupd
W6qlq9BsJh3S7lljSpyM950jXPOhRANCAASYoosv9a+nP4K7w+1/ozRZuZTunIX/
a+W/sBqro0InOtOqI5v7GW6O6i1Q4P0fN1x/cDbcKerTUnAkS3tyrXyu
-----END PRIVATE KEY-----
""");
    kPem.put("ed.pk8.pem", """
-----BEGIN PRIVATE KEY-----
MC4CAQAwBQYDK2VwBCIEIJzEe+NEtkQHcpNiBTJjooFEJqspIbDUJNeLNoLyZlMU
-----END PRIVATE KEY-----
""");
    kPem.put("rsa.pub.pem", """
-----BEGIN PUBLIC KEY-----
MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQC1+mZ6kCa9qscY4rr1QS4m5/fC
YrVghE2D+7tulW8gbmqQEI9BviUA/vKJ3A6ydnrLE9n6jSQnusvYSEJ/RSGDWQ57
nX+48P1IsYItGjNkS+d8NYoY20GKYTxRhI6t1dOZ4CV01tXtVM9M3/+mmuofawSC
QpQAAB13g7S4bsCSSQIDAQAB
-----END PUBLIC KEY-----
""");
    kPem.put("ec.pub.pem", """
-----BEGIN PUBLIC KEY-----
MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEmKKLL/Wvpz+Cu8Ptf6M0WbmU7pyF
/2vlv7Aaq6NCJzrTqiOb+xlujuotUOD9Hzdcf3A23Cnq01JwJEt7cq18rg==
-----END PUBLIC KEY-----
""");
    kPem.put("ed.pub.pem", """
-----BEGIN PUBLIC KEY-----
MCowBQYDK2VwAyEAVaRjnxRYNpBzKyqtAuGaeTuV0mqx8FyturWSjLemfRE=
-----END PUBLIC KEY-----
""");
    kPem.put("rsa.pkcs1.pem", """
-----BEGIN RSA PRIVATE KEY-----
MIICWwIBAAKBgQC1+mZ6kCa9qscY4rr1QS4m5/fCYrVghE2D+7tulW8gbmqQEI9B
viUA/vKJ3A6ydnrLE9n6jSQnusvYSEJ/RSGDWQ57nX+48P1IsYItGjNkS+d8NYoY
20GKYTxRhI6t1dOZ4CV01tXtVM9M3/+mmuofawSCQpQAAB13g7S4bsCSSQIDAQAB
AoGALCcWCDTDgY9XAT25uNnYtc3gekRy13kiSZyhTGJrdQX1SwEJkRzuY4B4HmTL
GBfREUa+w6DNHeqfnNljcPe3+np5IpT637cLsZCtR/fwvl1tWEvbX4jGexFENR8H
XT6d4TVwTg/sACw91+q1yM3y9OGyo8txx+mapF2vxs8SOPUCQQDniCH3zihq1Rqj
xuHtRGS0D7p24Ln+p0Bz3qL4QaoX1/hjRc46lZF5+2zQsUYKwcK2oFy3TnS22L+m
HB44ECwzAkEAyTWkVN2/LpnE2k/Zu+s+BlKyrBqkItAYol6X4EClTVT4a6vFETKw
TzpEKIXhYWRiXHZ555f8Bo8M9n/6ZSsLkwJAF3x/tZt8Dho/8Y0jLeZaMMnHqHdz
ZpmguCBFd9lxX6nQ0IEsQlUZxL30c9cHu9+b2Nq7jPywajvidP81MYqycQJAMkPI
Tbsnq4iWeMNB7YQS9plqMTxmjJvXy5g98LO1l+AGPz+l92nmSaSQql7nsuDVAaxH
P+fj+e96mYnXD7b1KQJARnliBrg7FUI4F4zbDG3bzHB3dplzSAJYdgaSck7KNasl
jh0Xe5CSsu2p39y1af5SRwy2BKd7bHivE/t45eI2VQ==
-----END RSA PRIVATE KEY-----
""");
    kPem.put("ec.sec1.pem", """
-----BEGIN EC PRIVATE KEY-----
MHcCAQEEIM2QxgXdC0/q4I7qXVuqpavQbCYd0u5ZY0qcjPedI1zzoAoGCCqGSM49
AwEHoUQDQgAEmKKLL/Wvpz+Cu8Ptf6M0WbmU7pyF/2vlv7Aaq6NCJzrTqiOb+xlu
juotUOD9Hzdcf3A23Cnq01JwJEt7cq18rg==
-----END EC PRIVATE KEY-----
""");
    kPem.put("cert.pem", """
-----BEGIN CERTIFICATE-----
MIIB7zCCAVigAwIBAgIBTTANBgkqhkiG9w0BAQsFADATMREwDwYDVQQDDAhwZW0g
dGVzdDAeFw0yNjEwMTcxODUzMDlaFw0zNjEwMTQxODUzMDlaMBMxETAPBgNVBAMM
CHBlbSB0ZXN0MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQC1+mZ6kCa9qscY
4rr1QS4m5/fCYrVghE2D+7tulW8gbmqQEI9BviUA/vKJ3A6ydnrLE9n6jSQnusvY
SEJ/RSGDWQ57nX+48P1IsYItGjNkS+d8NYoY20GKYTxRhI6t1dOZ4CV01tXtVM9M
3/+mmuofawSCQpQAAB13g7S4bsCSSQIDAQABo1MwUTAdBgNVHQ4EFgQUybwz0zA7
8q8MaX/FtjoofZLosvowHwYDVR0jBBgwFoAUybwz0zA78q8MaX/FtjoofZLosvow
DwYDVR0TAQH/BAUwAwEB/zANBgkqhkiG9w0BAQsFAAOBgQA2x5/sDx34o3WWCXBE
kg6WOa6lpgDS+XcNCn8/tCvrNvqZ/h/vuvwgbstfP8Tio08zXiv/18xOasYWVRQ+
eZiH8Po7OV7SC4uMscuQa4+Rz/1bNdvABsOVjGIK2nAHE4MTIXukuh0m131Q4yDv
eLx1zjrEY6ruEPOuGTnZHB1eDg==
-----END CERTIFICATE-----
""");
    kPem.put("crl.pem", """
-----BEGIN X509 CRL-----
MIHqMFUCAQEwDQYJKoZIhvcNAQELBQAwEzERMA8GA1UEAwwIcGVtIHRlc3QXDTI2
MTAxNzE4NTMyOVoXDTI2MTExNjE4NTMyOVqgDjAMMAoGA1UdFAQDAgEBMA0GCSqG
SIb3DQEBCwUAA4GBADIPGb5xTnUvkWTC5I14ClIHnkC1tQUaPMYfpFGiD+wRXZuV
4vpreM3mRrv9tX8rBr43Nvn8Wr5ASNnT9bSiSfLiLsHoN8hhZai6e/rvWRu1eu5W
kAB5y2ikqD52OPB8bjCujtbHyccxqJv7EzC1a9NkozPA9rlmWni1lGlGYSSw
-----END X509 CRL-----
""");
    kPem.put("rsa.enc.pem", """
-----BEGIN ENCRYPTED PRIVATE KEY-----
MIIC5TBfBgkqhkiG9w0BBQ0wUjAxBgkqhkiG9w0BBQwwJAQQKK2MJ3eCtVxAoJ3W
r8c7twICCAAwDAYIKoZIhvcNAgkFADAdBglghkgBZQMEAQIEEPXZN0IODQPxYwKc
hRSCXqEEggKAgygSnqapWHqOmcLUKAO6cx2AsGHRdiNvShSF1F1iJ4vr+yguQVRw
L3aLxJjUNaKguyk4D1uuC1rMaPoB13/ozdxrvd5YJaB0TLE6ndxrr326ASApgZ2Z
sa0dXQdVFNUlkfQqUcqH2Eq+KBjTM0KK+GzTxCEjBp+ES9V7cKJPGcAkeB4pXmRY
3PsQZjeA6y2x8m8zTzzOz/eFM76b0cPhblR6el9g/hvoucYXfgWOqBApYvgti/5U
HzNLGSx1lZ+6QTEpTrg4eUn3uwpZH3qdE8E5GhmJEPUdJrmotZXuFos9fr4pprUG
SuLFvcnMDq6r5CYLv0hZxXyns9LgixL4gsh/Do+hUHnyS69eosm3xJFirfvk8wnL
zQqq+uekXcpgMmnmS2xUBOHq1t9NaetlNHj4x8RS6sk8XcYQK7lJN9GgKyp3vQ4W
bdCXbuCnCWrnAbylEyGxHstYHSdSfUihXIOy0Lfcp0o8ISjkbDK5e4sjCd7a3CeA
HQrsNaJ0NiM74Zcfofev8muqCGrWjShUnBFcKVUFwK9TQaaZlYJ/Wevu/cupLMdl
NnrFlnKMmtBnzz+xU6L93IysiQR8ls9HOsYoPt477UHMYuF65NNL7pSqritMv+V3
yq9WxfMF6IZLqi3J775Sg8tmZLAvbBvx//lVoelPN+APwiEPFabvSR9vTiIGFJQU
RUaN/32slwtcEi9x3yNnQF7IcoTGTWsGjc+TmoGNqzW9FCf9XnVRrVc+3iAPS8J9
ipJb3q9KGmk2j4z4+UptoiyLQe6GYpg4g39beXDAv7X08uxB4dY9Pmb/zNX6uvtw
WdYBGssy1B6ovH8O/7i0Aj2O+YSIB66ybA==
-----END ENCRYPTED PRIVATE KEY-----
""");
  }

  static String rd(String f) {
    return kPem.get(f);
  }

  static String hash(byte[] b) throws Exception {
    return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(b)).substring(0, 12);
  }

  static String d(Object r) throws Exception {
    if (r == null) {
      return "null";
    }
    if (r instanceof byte[] b) {
      return "bytes" + b.length;
    }
    if (r instanceof KeyPair kp) {
      return "KeyPair(" + d(kp.getPublic()) + ", " + d(kp.getPrivate()) + ")";
    }
    if (r instanceof PrivateKey k) {
      return "PrivateKey " + k.getAlgorithm() + " " + k.getFormat() + " " + hash(k.getEncoded());
    }
    if (r instanceof PublicKey k) {
      return "PublicKey " + k.getAlgorithm() + " " + k.getFormat() + " " + hash(k.getEncoded());
    }
    if (r instanceof X509Certificate c) {
      return "cert " + c.getSubjectX500Principal() + " " + c.getSerialNumber();
    }
    if (r instanceof X509CRL c) {
      return "crl " + c.getIssuerX500Principal();
    }
    if (r instanceof EncryptedPrivateKeyInfo e) {
      return "epki " + e.getAlgName() + " " + e.getEncoded().length;
    }
    if (r instanceof PEMRecord p) {
      return "rec " + p.type() + " " + p.content().length() + " ld=" + (p.leadingData() == null ? "null" : p.leadingData().length);
    }
    if (r instanceof EncodedKeySpec s) {
      return "spec " + s.getFormat() + " " + hash(s.getEncoded());
    }
    if (r instanceof PEMDecoder || r instanceof PEMEncoder) {
      return r.getClass().getSimpleName();
    }
    return String.valueOf(r);
  }

  static void t(String n, Callable<Object> c) {
    try {
      Object r = c.call();
      System.out.println(n + " = " + d(r).replace("\n", "\\n").replace("\r", "\\r"));
    } catch (Throwable e) {
      System.out.println(n + " ! " + e.getClass().getName() + ": " + String.valueOf(e.getMessage()).replaceAll("Cannot cast \\S+ to", "Cannot cast OBJ to").replaceAll("[a-z]+\\.[a-z.]*[A-Za-z0-9]+@[0-9a-f]+", "OBJ"));
    }
  }

  public static void main(String[] a) throws Exception {
    decoding();
    encoding();
  }

  static void decoding() throws Exception { PEMDecoder d=PEMDecoder.of();
  for (String f: new String[]{"rsa.pk8.pem","ec.pk8.pem","ed.pk8.pem","rsa.pub.pem","ec.pub.pem","ed.pub.pem","rsa.pkcs1.pem","ec.sec1.pem","cert.pem","crl.pem","rsa.enc.pem"}) { String s=rd(f); t("dec "+f,()->d.decode(s)); }
  t("dec enc with pw",()->d.withDecryption("secret".toCharArray()).decode(rd("rsa.enc.pem")));
  t("dec enc wrong pw",()->d.withDecryption("nope".toCharArray()).decode(rd("rsa.enc.pem")));
  t("dec plain with pw",()->d.withDecryption("secret".toCharArray()).decode(rd("rsa.pk8.pem")));
  t("dec cls PrivateKey",()->d.decode(rd("rsa.pk8.pem"),PrivateKey.class));
  t("dec cls RSAPrivateKey",()->d.decode(rd("rsa.pk8.pem"),java.security.interfaces.RSAPrivateKey.class));
  t("dec cls PublicKey wrong",()->d.decode(rd("rsa.pk8.pem"),PublicKey.class));
  t("dec cls X509Certificate",()->d.decode(rd("cert.pem"),X509Certificate.class));
  t("dec cls PEMRecord",()->d.decode(rd("cert.pem"),PEMRecord.class));
  t("dec cls PEMRecord pk",()->d.decode(rd("rsa.pk8.pem"),PEMRecord.class));
  t("dec cls KeyPair",()->d.decode(rd("rsa.pk8.pem"),KeyPair.class));
  t("dec cls PKCS8Spec",()->d.decode(rd("rsa.pk8.pem"),PKCS8EncodedKeySpec.class));
  t("dec cls X509Spec",()->d.decode(rd("rsa.pub.pem"),X509EncodedKeySpec.class));
  t("dec cls EPKI",()->d.decode(rd("rsa.enc.pem"),EncryptedPrivateKeyInfo.class));
  t("dec cls EPKI plain",()->d.decode(rd("rsa.pk8.pem"),EncryptedPrivateKeyInfo.class));
  t("dec cls null",()->d.decode(rd("cert.pem"),null));
  t("dec null",()->d.decode((String)null));
  t("dec empty",()->d.decode(""));
  t("dec garbage",()->d.decode("hello world"));
  t("dec leading",()->{ DEREncodable r=d.decode("some leading\ntext\n"+rd("cert.pem")); return r; });
  t("dec leading data",()->{ PEMRecord r=d.decode("some leading\ntext\n"+rd("cert.pem"),PEMRecord.class); return new String(r.leadingData())+"|"+r.type()+"|"+r.content().length(); });
  t("dec two",()->d.decode(rd("cert.pem")+rd("rsa.pub.pem")));
  t("dec stream two",()->{ InputStream in=new ByteArrayInputStream((rd("cert.pem")+rd("rsa.pub.pem")).getBytes()); Object x=d.decode(in); Object y=d.decode(in); Object z; try{ z=d.decode(in);}catch(Exception e){ z=e.getClass().getName()+": "+e.getMessage(); } return x.getClass().getSimpleName()+" "+y.getClass().getSimpleName()+" "+z; });
  t("dec stream empty",()->d.decode(new ByteArrayInputStream(new byte[0])));
  t("dec bad b64",()->d.decode("-----BEGIN PUBLIC KEY-----\n!!!!\n-----END PUBLIC KEY-----\n"));
  t("dec mismatch",()->d.decode("-----BEGIN PUBLIC KEY-----\nAAAA\n-----END PRIVATE KEY-----\n"));
  t("dec missing end",()->d.decode("-----BEGIN PUBLIC KEY-----\nAAAA\n"));
  t("dec unknown type",()->d.decode("-----BEGIN FOO BAR-----\nAAAA\n-----END FOO BAR-----\n"));
  t("dec bad der",()->d.decode("-----BEGIN PUBLIC KEY-----\nAAAA\n-----END PUBLIC KEY-----\n"));
  t("dec crlf",()->d.decode(rd("cert.pem").replace("\n","\r\n")));
  t("dec no trailing nl",()->d.decode(rd("cert.pem").trim()));
  t("dec indent",()->d.decode("  "+rd("cert.pem")));
  t("dec lower",()->d.decode(rd("cert.pem").replace("BEGIN","begin")));
  t("dec long lines",()->{ String s=rd("rsa.pub.pem"); String[] ls=s.split("\n"); StringBuilder sb=new StringBuilder(ls[0]+"\n"); for(int i=1;i<ls.length-1;i++) sb.append(ls[i]); sb.append("\n"+ls[ls.length-1]+"\n"); return d.decode(sb.toString()); });
  t("dec blank lines",()->d.decode(rd("cert.pem").replace("\n","\n\n")));
  t("withFactory null",()->d.withFactory(null));
  t("withDecryption null",()->d.withDecryption(null));
  t("withDecryption empty",()->d.withDecryption(new char[0]).decode(rd("rsa.enc.pem")));
  t("PEMRecord ctor",()->new PEMRecord("TYPE","AAAA"));
  t("PEMRecord ctor null type",()->new PEMRecord(null,"AAAA"));
  t("PEMRecord ctor null content",()->new PEMRecord("T",null));
  t("PEMRecord bad type",()->new PEMRecord("t y!","AAAA"));
  t("PEMRecord bad content",()->new PEMRecord("T","!!!"));
  t("PEMRecord str",()->new PEMRecord("TYPE","AAAA", new byte[]{65}));
  t("PEMRecord str2",()->new PEMRecord("TYPE","AAAA"));
  t("PEMRecord ld null",()->new PEMRecord("TYPE","AAAA").leadingData());
  t("PEMRecord eq",()->new PEMRecord("TYPE","AAAA").equals(new PEMRecord("TYPE","AAAA")));
  t("PEMRecord eq ld",()->new PEMRecord("TYPE","AAAA",new byte[]{1}).equals(new PEMRecord("TYPE","AAAA",new byte[]{1})));
  t("PEMRecord hash",()->new PEMRecord("TYPE","AAAA").hashCode()==new PEMRecord("TYPE","AAAA").hashCode());
  t("PEMRecord content ws",()->new PEMRecord("T","AA\nAA").content());
  t("PEMRecord getEncoded?",()->{ PEMRecord r=new PEMRecord("T","AAAA"); try { return r.getClass().getMethod("getEncoded"); } catch(Exception e){ return "none"; } });
   }

  static void encoding() throws Exception {
    PEMDecoder d = PEMDecoder.of();
    PEMEncoder e = PEMEncoder.of();
  for (String f: new String[]{"rsa.pk8.pem","ec.pk8.pem","ed.pk8.pem","rsa.pub.pem","ec.pub.pem","ed.pub.pem","cert.pem","crl.pem","rsa.enc.pem","rsa.pkcs1.pem","ec.sec1.pem"}) { String s=rd(f); t("rt "+f,()->{ String o=e.encodeToString(d.decode(s)); return o.equals(s)+" "+(o.equals(s)?"":o.substring(0,Math.min(60,o.length()))); }); }
  t("enc bytes",()->{ byte[] b=e.encode(d.decode(rd("cert.pem"))); return new String(b).equals(rd("cert.pem")); });
  t("enc null",()->e.encode(null)); t("enc str null",()->e.encodeToString(null));
  t("enc keypair pk class",()->{ KeyPair kp=(KeyPair)d.decode(rd("ec.pk8.pem")); return kp.getPrivate().getClass().getSimpleName()+" "+kp.getPublic().getClass().getSimpleName(); });
  t("enc keypair",()->{ KeyPair kp=(KeyPair)d.decode(rd("ec.pk8.pem")); return e.encodeToString(kp).equals(rd("ec.pk8.pem")); });
  t("enc private only of keypair",()->{ KeyPair kp=(KeyPair)d.decode(rd("ec.pk8.pem")); String s=e.encodeToString(kp.getPrivate()); return s.equals(rd("ec.pk8.pem"))+" "+s.length()+" "+rd("ec.pk8.pem").length(); });
  t("enc pub of keypair",()->{ KeyPair kp=(KeyPair)d.decode(rd("ec.pk8.pem")); return e.encodeToString(kp.getPublic()).equals(rd("ec.pub.pem")); });
  t("enc keypair rsa",()->{ KeyPair kp=new KeyPair((PublicKey)d.decode(rd("rsa.pub.pem")),(PrivateKey)d.decode(rd("rsa.pk8.pem"))); String s=e.encodeToString(kp); return s.equals(rd("rsa.pk8.pem"))+" "+s.length()+" "+rd("rsa.pk8.pem").length()+" "+s.substring(0,40); });
  t("enc keypair rsa decode back",()->{ KeyPair kp=new KeyPair((PublicKey)d.decode(rd("rsa.pub.pem")),(PrivateKey)d.decode(rd("rsa.pk8.pem"))); return d.decode(e.encodeToString(kp)).getClass().getSimpleName(); });
  t("enc keypair mismatched",()->{ KeyPair kp=new KeyPair((PublicKey)d.decode(rd("rsa.pub.pem")),((KeyPair)d.decode(rd("ec.pk8.pem"))).getPrivate()); return e.encodeToString(kp).length(); });
  t("enc spec pkcs8",()->{ PrivateKey k=(PrivateKey)d.decode(rd("rsa.pk8.pem")); return e.encodeToString(new PKCS8EncodedKeySpec(k.getEncoded())).equals(rd("rsa.pk8.pem")); });
  t("enc spec x509",()->{ PublicKey k=(PublicKey)d.decode(rd("rsa.pub.pem")); return e.encodeToString(new X509EncodedKeySpec(k.getEncoded())).equals(rd("rsa.pub.pem")); });
  t("enc record",()->e.encodeToString(new PEMRecord("TYPE","AAAA")));
  t("enc record leading",()->e.encodeToString(new PEMRecord("TYPE","AAAA",new byte[]{65,10})));
  t("enc record long",()->e.encodeToString(new PEMRecord("TYPE","A".repeat(130))));
  t("enc record ws",()->e.encodeToString(new PEMRecord("TYPE","AAAA\nAAAA")));
  t("enc record from decode",()->{ PEMRecord r=d.decode("lead\n"+rd("cert.pem"),PEMRecord.class); return e.encodeToString(r).equals("lead\n"+rd("cert.pem")); });
  t("withEnc null",()->e.withEncryption(null));
  t("withEnc empty",()->e.withEncryption(new char[0]).encodeToString((PrivateKey)d.decode(rd("rsa.pk8.pem"))).substring(0,40));
  t("enc w/ pw",()->{ String s=e.withEncryption("pw".toCharArray()).encodeToString((PrivateKey)d.decode(rd("rsa.pk8.pem"))); return s.substring(0,36)+" "+(d.withDecryption("pw".toCharArray()).decode(s) instanceof PrivateKey); });
  t("enc w/ pw twice differs",()->{ PEMEncoder pe=e.withEncryption("pw".toCharArray()); PrivateKey k=(PrivateKey)d.decode(rd("rsa.pk8.pem")); return !pe.encodeToString(k).equals(pe.encodeToString(k)); });
  t("enc w/ pw decode no pw",()->{ String s=e.withEncryption("pw".toCharArray()).encodeToString((PrivateKey)d.decode(rd("rsa.pk8.pem"))); return d.decode(s).getClass().getSimpleName(); });
  t("enc w/ pw pub",()->e.withEncryption("pw".toCharArray()).encodeToString(d.decode(rd("rsa.pub.pem"))));
  t("enc w/ pw cert",()->e.withEncryption("pw".toCharArray()).encodeToString(d.decode(rd("cert.pem"))));
  t("enc w/ pw keypair",()->{ String s=e.withEncryption("pw".toCharArray()).encodeToString(d.decode(rd("ec.pk8.pem"))); return s.substring(0,36)+" "+d.withDecryption("pw".toCharArray()).decode(s).getClass().getSimpleName(); });
  t("enc w/ pw record",()->e.withEncryption("pw".toCharArray()).encodeToString(new PEMRecord("T","AAAA")));
  t("enc w/ pw epki",()->e.withEncryption("pw".toCharArray()).encodeToString(d.decode(rd("rsa.enc.pem"))).equals(rd("rsa.enc.pem")));
  t("dec EC KeyPair as PrivateKey",()->d.decode(rd("ec.pk8.pem"),PrivateKey.class));
  t("dec EC KeyPair as PublicKey",()->d.decode(rd("ec.pk8.pem"),PublicKey.class));
  t("dec EC KeyPair as KeyPair",()->d.decode(rd("ec.pk8.pem"),KeyPair.class));
  t("dec RSA pk8 as KeyPair",()->d.decode(rd("rsa.pk8.pem"),KeyPair.class));
  t("dec ed pk8 as KeyPair",()->d.decode(rd("ed.pk8.pem"),KeyPair.class));
  t("dec cert as X509Certificate",()->((X509Certificate)d.decode(rd("cert.pem"))).getSubjectX500Principal());
  t("dec crl",()->((X509CRL)d.decode(rd("crl.pem"))).getIssuerX500Principal());
  t("dec pkcs1 alg",()->((PrivateKey)d.decode(rd("rsa.pkcs1.pem"))).getAlgorithm());
  t("dec pkcs1 as PEMRecord",()->d.decode(rd("rsa.pkcs1.pem"),PEMRecord.class).type());
  t("dec sec1 as PrivateKey",()->d.decode(rd("ec.sec1.pem"),PrivateKey.class));
  t("dec sec1 type",()->((PEMRecord)d.decode(rd("ec.sec1.pem"))).type());
  t("dec enc type",()->((EncryptedPrivateKeyInfo)d.decode(rd("rsa.enc.pem"))).getAlgName());
  t("dec withDecryption pw on pub",()->d.withDecryption("x".toCharArray()).decode(rd("rsa.pub.pem")).getClass().getSimpleName());
  t("dec withDecryption cls PrivateKey",()->d.withDecryption("secret".toCharArray()).decode(rd("rsa.enc.pem"),PrivateKey.class).getClass().getSimpleName());
  t("dec withDecryption cls EPKI",()->d.withDecryption("secret".toCharArray()).decode(rd("rsa.enc.pem"),EncryptedPrivateKeyInfo.class).getClass().getSimpleName());
  t("dec no pw cls PrivateKey",()->d.decode(rd("rsa.enc.pem"),PrivateKey.class));
  t("dec ed algo",()->((PrivateKey)d.decode(rd("ed.pk8.pem"))).getAlgorithm()+" "+((PublicKey)d.decode(rd("ed.pub.pem"))).getAlgorithm());
  t("dec withFactory",()->d.withFactory(Security.getProvider("SunEC")).decode(rd("ec.pub.pem")).getClass().getSimpleName());
  t("dec withFactory rsa-sunec",()->d.withFactory(Security.getProvider("SunEC")).decode(rd("rsa.pub.pem")).getClass().getSimpleName());
  t("dec attrs",()->{ KeyPair kp=(KeyPair)d.decode(rd("ec.pk8.pem")); return kp.getPrivate().getAlgorithm()+" "+kp.getPrivate().getFormat()+" "+kp.getPublic().getFormat()+" "+kp.getPublic().getAlgorithm(); });
  t("pem record getters",()->{ PEMRecord r=new PEMRecord("TYPE","AAAA\nBBBB",new byte[]{1,2}); return r.type()+"|"+r.content()+"|"+Arrays.toString(r.leadingData()); });
  t("pem record leadingData clone",()->{ byte[] ld={1,2}; PEMRecord r=new PEMRecord("T","AAAA",ld); ld[0]=9; return r.leadingData()[0]+" "+(r.leadingData()==r.leadingData()); });
  t("pem record toString decode",()->d.decode(new PEMRecord("TYPE","AAAA").toString()).getClass().getSimpleName());
  t("stream decode cls",()->d.decode(new ByteArrayInputStream(rd("cert.pem").getBytes()),X509Certificate.class).getType());
  t("stream decode partial",()->{ InputStream in=new ByteArrayInputStream((rd("cert.pem")+"trailing text").getBytes()); d.decode(in); return new String(in.readAllBytes()); });
  t("stream decode leading",()->{ InputStream in=new ByteArrayInputStream(("lead\n"+rd("cert.pem")).getBytes()); PEMRecord r=(PEMRecord)d.decode(in,PEMRecord.class); return new String(r.leadingData()); });
  t("stream decode nonpem trailing",()->{ InputStream in=new ByteArrayInputStream("not pem at all".getBytes()); return d.decode(in); });
   }
}
