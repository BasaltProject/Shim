/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 * Minimum release: 21
 */

import java.io.*;
import java.nio.ByteBuffer;
import java.security.*;
import java.util.*;

public class DigestProvidersModern {
  interface Body {
    Object get() throws Exception;
  }

  static void probe(String name, Body body) {
    String out;
    try {
      out = String.valueOf(body.get());
    } catch (Throwable t) {
      out = "!" + t.getClass().getName() + ":" + (t instanceof NullPointerException ? "npe" : t.getMessage());
    }
    System.out.println(name + "=" + out);
  }

  static String hex(byte[] b) {
    return HexFormat.of().formatHex(b);
  }

  static final String[] ALGS = {"MD2", "MD5", "SHA-1", "SHA-224", "SHA-256", "SHA-384", "SHA-512", "SHA-512/224",
    "SHA-512/256", "SHA3-224", "SHA3-256", "SHA3-384", "SHA3-512", "SHAKE128-256", "SHAKE256-512"};

  public static void main(String[] args) throws Exception {
    byte[] data = new byte[1000];
    for (int i = 0; i < data.length; i++) {
      data[i] = (byte) (i * 31 + 7);
    }
    for (String alg : ALGS) {
      probe("lengths " + alg, () -> {
        MessageDigest md = MessageDigest.getInstance(alg);
        MessageDigest all = MessageDigest.getInstance(alg);
        StringBuilder sb = new StringBuilder(md.getDigestLength() + " ");
        int[] sizes = {0, 1, 2, 3, 15, 16, 17, 31, 32, 33, 55, 56, 57, 63, 64, 65, 71, 72, 73, 111, 112, 113, 127, 128,
          129, 135, 136, 137, 143, 144, 145, 167, 168, 169, 255, 256, 257, 1000};
        for (int n : sizes) {
          md.update(data, 0, n);
          byte[] d = md.digest();
          all.update(d);
          sb.append(n).append(':').append(hex(d).substring(0, 8)).append(' ');
        }
        return sb + hex(all.digest());
      });
      probe("incremental " + alg, () -> {
        MessageDigest a = MessageDigest.getInstance(alg);
        MessageDigest b = MessageDigest.getInstance(alg);
        a.update(data);
        for (int i = 0; i < data.length; i += 7) {
          b.update(data, i, Math.min(7, data.length - i));
        }
        byte[] x = a.digest();
        byte[] y = b.digest();
        MessageDigest c = MessageDigest.getInstance(alg);
        for (byte v : data) {
          c.update(v);
        }
        return Arrays.equals(x, y) + " " + Arrays.equals(x, c.digest()) + " " + hex(x);
      });
      probe("clone buffer " + alg, () -> {
        MessageDigest a = MessageDigest.getInstance(alg);
        a.update(data, 0, 100);
        MessageDigest b = (MessageDigest) a.clone();
        a.update(data, 100, 50);
        ByteBuffer direct = ByteBuffer.allocateDirect(60);
        direct.put(data, 100, 50).flip();
        b.update(direct);
        ByteBuffer heap = ByteBuffer.wrap(data, 150, 10);
        a.update(heap);
        b.update(data, 150, 10);
        byte[] out = new byte[a.getDigestLength() + 3];
        int n = b.digest(out, 3, a.getDigestLength());
        return n + " " + hex(a.digest()) + " " + hex(out) + " " + direct.remaining() + " " + heap.remaining();
      });
      probe("reuse " + alg, () -> {
        MessageDigest a = MessageDigest.getInstance(alg);
        a.update(data, 0, 10);
        a.reset();
        String first = hex(a.digest());
        a.update(data, 0, 10);
        a.digest();
        String second = hex(a.digest(data));
        return first + " " + second + " " + a.getAlgorithm() + " " + a.getProvider().getName();
      });
    }
    probe("aliases", () -> {
      StringBuilder sb = new StringBuilder();
      for (String a : new String[] {"SHA", "SHA1", "sha-1", "sha-256", "SHA256", "SHA512/256", "SHAKE128", "OID.2.16.840.1.101.3.4.2.1",
        "2.16.840.1.101.3.4.2.8", "1.3.14.3.2.26", "md5", "Md2"}) {
        MessageDigest md = MessageDigest.getInstance(a);
        sb.append(a).append("->").append(md.getAlgorithm()).append(' ');
      }
      return sb;
    });
    probe("unknown", () -> MessageDigest.getInstance("FOO"));
    probe("null alg", () -> MessageDigest.getInstance(null));
    probe("unknown provider", () -> MessageDigest.getInstance("MD5", "NOPE"));
    probe("empty provider", () -> MessageDigest.getInstance("MD5", ""));
    probe("null provider", () -> MessageDigest.getInstance("MD5", (String) null));
    probe("provider obj null", () -> MessageDigest.getInstance("MD5", (Provider) null));
    probe("good provider", () -> MessageDigest.getInstance("MD5", "SUN").getProvider());
    probe("provider obj", () -> MessageDigest.getInstance("SHA-256", Security.getProvider("SUN")).getProvider());
    probe("toString", () -> {
      MessageDigest md = MessageDigest.getInstance("SHA-256");
      String a = md.toString();
      md.update((byte) 1);
      String b = md.toString();
      md.digest();
      return (a + b + md.toString()).replace("\n", "|");
    });
    probe("update bad", () -> MessageDigest.getInstance("MD5").digest(null));
    probe("update short", () -> {
      MessageDigest.getInstance("MD5").update(new byte[4], 2, 5);
      return "ok";
    });
    probe("update null range", () -> {
      MessageDigest.getInstance("MD5").update(null, 0, 1);
      return "ok";
    });
    probe("update neg", () -> {
      MessageDigest.getInstance("MD5").update(new byte[4], -1, 2);
      return "ok";
    });
    probe("digest short", () -> MessageDigest.getInstance("SHA-256").digest(new byte[10], 0, 10));
    probe("digest short buf", () -> MessageDigest.getInstance("SHA-256").digest(new byte[10], 0, 32));
    probe("digest bad ofs", () -> MessageDigest.getInstance("SHA-256").digest(new byte[40], 20, 32));
    probe("digest null", () -> MessageDigest.getInstance("SHA-256").digest(null, 0, 32));
    probe("digest neg ofs", () -> MessageDigest.getInstance("SHA-256").digest(new byte[40], -1, 32));
    probe("update oob", () -> {
      MessageDigest md = MessageDigest.getInstance("SHA-256");
      md.update(new byte[8], 4, 5);
      return "ok";
    });
    probe("isEqual", () -> MessageDigest.isEqual(new byte[] {1, 2}, new byte[] {1, 2}) + " " + MessageDigest.isEqual(new byte[] {1, 2}, new byte[] {1, 3})
        + " " + MessageDigest.isEqual(new byte[] {1, 2}, new byte[] {1}) + " " + MessageDigest.isEqual(null, null)
        + " " + MessageDigest.isEqual(new byte[0], new byte[0]) + " " + MessageDigest.isEqual(null, new byte[0])
        + " " + MessageDigest.isEqual(new byte[] {1}, new byte[0]) + " " + MessageDigest.isEqual(new byte[0], new byte[] {1}));
    probe("streams", () -> {
      MessageDigest md = MessageDigest.getInstance("SHA-256");
      DigestInputStream in = new DigestInputStream(new ByteArrayInputStream(data), md);
      byte[] buf = new byte[100];
      int total = 0;
      int n;
      while ((n = in.read(buf)) > 0) {
        total += n;
      }
      in.read();
      String a = hex(md.digest());
      MessageDigest md2 = MessageDigest.getInstance("SHA-256");
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      DigestOutputStream out = new DigestOutputStream(bos, md2);
      out.write(data, 0, 500);
      out.on(false);
      out.write(7);
      out.on(true);
      out.write(data, 500, 500);
      out.write(data[0]);
      return total + " " + a + " " + hex(md2.digest()) + " " + bos.size() + " " + in.getMessageDigest().getAlgorithm()
          + " " + in.toString().replace("\n", "|") + " " + out.getMessageDigest().getProvider();
    });
    probe("provider info", () -> Security.getProvider("SUN").getInfo() + " | " + Security.getProvider("SUN") + " | " + Security.getProvider("SUN").getVersion());
    probe("security props", () -> Security.getProperty("keystore.type") + " " + Security.getProperty("securerandom.source") + " "
        + Security.getProperty("ssl.KeyManagerFactory.algorithm") + " " + Security.getProperty("nope") + " " + Security.getProperty("crypto.policy"));
    probe("security algorithms", () -> new TreeSet<>(Security.getAlgorithms("MessageDigest")));
    probe("security algorithms none", () -> Security.getAlgorithms("Nothing") + " " + Security.getAlgorithms("") + " " + Security.getAlgorithms("MessageDigest.").size());
    probe("security algorithms case", () -> new TreeSet<>(Security.getAlgorithms("messagedigest")).size());
    probe("get providers filter", () -> {
      Provider[] ps = Security.getProviders("MessageDigest.SHA-256");
      Provider[] none = Security.getProviders("MessageDigest.FOO");
      Provider[] attr = Security.getProviders("MessageDigest.MD5 ImplementedIn:Software");
      Provider[] attr2 = Security.getProviders("MessageDigest.MD5 ImplementedIn:Hardware");
      return ps.length + " " + ps[0].getName() + " " + none + " " + attr.length + " " + attr2;
    });
    probe("get providers bad filter", () -> Security.getProviders("nodot"));
    probe("service lookup", () -> {
      Provider p = Security.getProvider("SUN");
      Provider.Service s = p.getService("MessageDigest", "sha-256");
      Provider.Service alias = p.getService("MessageDigest", "SHA256");
      Provider.Service none = p.getService("MessageDigest", "FOO");
      return s.getAlgorithm() + " " + s.getType() + " " + s.getClassName() + " " + s.getAttribute("ImplementedIn") + " " + (alias == s)
          + " " + none + " " + s.getProvider().getName() + " " + s.getAttribute("nope");
    });
    probe("service toString", () -> Security.getProvider("SUN").getService("MessageDigest", "MD5").toString().replace("\r", "\\r").replace("\n", "\\n"));
    probe("service lookup null", () -> Security.getProvider("SUN").getService(null, "x"));
    probe("service new instance", () -> Security.getProvider("SUN").getService("MessageDigest", "MD5").newInstance(null).getClass().getName());
    probe("service count", () -> {
      int n = 0;
      for (Provider.Service s : Security.getProvider("SUN").getServices()) {
        if (s.getType().equals("MessageDigest")) {
          n++;
        }
      }
      return n;
    });
    probe("custom provider", () -> {
      Provider p = new Provider("Custom", "1.5", "custom info") {
        {
          put("MessageDigest.MYMD5", "sun.security.provider.MD5");
          put("Alg.Alias.MessageDigest.MYALIAS", "MYMD5");
          put("MessageDigest.MYMD5 Flavor", "spicy");
        }
      };
      int pos = Security.addProvider(p);
      try {
        MessageDigest md = MessageDigest.getInstance("MyAlias");
        Provider.Service s = p.getService("MessageDigest", "mymd5");
        return (pos > 0) + " " + md.getAlgorithm() + " " + md.getProvider().getName() + " " + hex(md.digest()) + " " + s.getAttribute("Flavor")
            + " " + p.getVersion() + " " + p + " " + Security.addProvider(p)
            + " " + p.getProperty("MessageDigest.MYMD5");
      } finally {
        Security.removeProvider("Custom");
      }
    });
    probe("custom service", () -> {
      Provider p = new Provider("Svc", 2.0, "svc info") {
        {
          putService(new Provider.Service(this, "MessageDigest", "XMD", "sun.security.provider.SHA", List.of("XALIAS"), Map.of("K", "V")));
        }
      };
      Security.insertProviderAt(p, 1);
      try {
        MessageDigest md = MessageDigest.getInstance("xalias");
        return Security.getProviders()[0].getName() + " " + md.getProvider().getName() + " " + md.getAlgorithm() + " " + hex(md.digest())
            + " " + p.getService("MessageDigest", "XMD").getAttribute("K") + " " + p.get("MessageDigest.XMD") + " " + p.get("Alg.Alias.MessageDigest.XALIAS");
      } finally {
        Security.removeProvider("Svc");
      }
    });
    probe("bad class", () -> {
      Provider p = new Provider("Bad", "1", "bad") {
        {
          put("MessageDigest.BADCLS", "no.such.Clazz");
          put("MessageDigest.WRONG", "java.lang.String");
        }
      };
      Security.addProvider(p);
      try {
        String r1;
        try {
          MessageDigest.getInstance("BADCLS");
          r1 = "ok";
        } catch (NoSuchAlgorithmException e) {
          r1 = e.getMessage() + "/" + (e.getCause() != null ? e.getCause().getClass().getName() : "null");
        }
        try {
          MessageDigest.getInstance("WRONG");
          return r1 + " ok";
        } catch (NoSuchAlgorithmException e) {
          return r1 + " " + e.getMessage();
        }
      } finally {
        Security.removeProvider("Bad");
      }
    });
    probe("security setProperty", () -> {
      Security.setProperty("my.test.key", "abc");
      return Security.getProperty("my.test.key");
    });
    probe("remove provider", () -> {
      int before = Security.getProviders().length;
      Security.removeProvider("NoSuch");
      return before == Security.getProviders().length;
    });
    probe("exceptions", () -> {
      StringBuilder sb = new StringBuilder();
      sb.append(new InvalidKeyException("a", new RuntimeException("c")).getCause().getMessage()).append(' ');
      sb.append(new SignatureException(new RuntimeException("x")).getMessage()).append(' ');
      sb.append(new NoSuchProviderException("p").getMessage()).append(' ');
      sb.append(new InvalidParameterException() instanceof IllegalArgumentException).append(' ');
      sb.append(new KeyManagementException() instanceof KeyException).append(' ');
      sb.append(new UnrecoverableKeyException("z") instanceof UnrecoverableEntryException).append(' ');
      sb.append(new KeyStoreException("k").getMessage());
      return sb;
    });
  }
}
