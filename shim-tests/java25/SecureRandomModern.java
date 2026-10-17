/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 * Minimum release: 21
 */

import java.security.*;
import java.util.*;

public class SecureRandomModern {
  interface Body {
    Object get() throws Exception;
  }

  static void probe(String name, Body body) {
    String out;
    try {
      out = String.valueOf(body.get());
    } catch (Throwable t) {
      out = "!" + t.getClass().getName() + ":" + t.getMessage();
    }
    System.out.println(name + "=" + out);
  }

  static String hex(byte[] b) {
    return HexFormat.of().formatHex(b);
  }

  static boolean spread(byte[] b) {
    Set<Integer> seen = new HashSet<>();
    for (byte v : b) {
      seen.add(v & 0xff);
    }
    return seen.size() > Math.min(64, b.length / 8);
  }

  public static void main(String[] args) throws Exception {
    probe("default", () -> {
      SecureRandom s = new SecureRandom();
      return s.getAlgorithm() + "|" + s + "|" + s.getProvider() + "|" + s.getParameters();
    });
    for (String alg : new String[] {"SHA1PRNG", "NativePRNG", "NativePRNGBlocking", "NativePRNGNonBlocking", "DRBG", "sha1prng", "nativeprng"}) {
      probe("alg " + alg, () -> {
        SecureRandom r = SecureRandom.getInstance(alg);
        byte[] a = new byte[512];
        r.nextBytes(a);
        byte[] seed = r.generateSeed(40);
        return r + "|" + r.getAlgorithm() + "|" + r.getParameters() + "|" + spread(a) + "|" + seed.length + "|" + r.getProvider().getName();
      });
    }
    probe("sha1prng seeded", () -> {
      SecureRandom sh = SecureRandom.getInstance("SHA1PRNG");
      sh.setSeed(new byte[] {1, 2, 3, 4});
      byte[] b = new byte[50];
      sh.nextBytes(b);
      byte[] c = new byte[7];
      sh.nextBytes(c);
      byte[] d = new byte[45];
      sh.nextBytes(d);
      return hex(b) + " " + hex(c) + " " + hex(d);
    });
    probe("sha1prng long seed", () -> {
      SecureRandom sh = SecureRandom.getInstance("SHA1PRNG");
      sh.setSeed(42L);
      return sh.nextInt() + " " + sh.nextLong() + " " + sh.nextDouble() + " " + sh.nextInt(100) + " " + sh.nextBoolean() + " "
          + sh.nextGaussian() + " " + sh.nextFloat() + " " + sh.nextInt(5, 9) + " " + sh.ints(3, 0, 50).boxed().toList();
    });
    probe("sha1prng zero long seed", () -> {
      SecureRandom a = SecureRandom.getInstance("SHA1PRNG");
      a.setSeed(new byte[] {9});
      a.setSeed(0L);
      SecureRandom b = SecureRandom.getInstance("SHA1PRNG");
      b.setSeed(new byte[] {9});
      return a.nextLong() == b.nextLong();
    });
    probe("sha1prng reseed mix", () -> {
      SecureRandom a = SecureRandom.getInstance("SHA1PRNG");
      a.setSeed(new byte[] {1});
      byte[] x = new byte[3];
      a.nextBytes(x);
      a.setSeed(new byte[] {2});
      byte[] y = new byte[30];
      a.nextBytes(y);
      a.setSeed(77L);
      byte[] z = new byte[21];
      a.nextBytes(z);
      return hex(x) + " " + hex(y) + " " + hex(z);
    });
    probe("seeded constructor", () -> {
      SecureRandom a = new SecureRandom(new byte[] {5, 6});
      return a.getAlgorithm() + " " + a.nextInt(1) ;
    });
    probe("empty", () -> {
      SecureRandom a = SecureRandom.getInstance("SHA1PRNG");
      a.setSeed(new byte[] {3});
      byte[] e = new byte[0];
      a.nextBytes(e);
      return hex(a.generateSeed(0)) + "|" + a.generateSeed(1).length + "|" + e.length;
    });
    probe("unknown", () -> SecureRandom.getInstance("FOO"));
    probe("null alg", () -> SecureRandom.getInstance(null));
    probe("provider ok", () -> SecureRandom.getInstance("SHA1PRNG", "SUN").getProvider());
    probe("provider missing", () -> SecureRandom.getInstance("SHA1PRNG", "NOPE"));
    probe("provider obj", () -> SecureRandom.getInstance("DRBG", Security.getProvider("SUN")).getAlgorithm());
    probe("params null", () -> SecureRandom.getInstance("DRBG", (SecureRandomParameters) null));
    probe("params sha1", () -> SecureRandom.getInstance("SHA1PRNG", DrbgParameters.instantiation(128, DrbgParameters.Capability.NONE, null)));
    probe("params native", () -> SecureRandom.getInstance("NativePRNG", DrbgParameters.instantiation(128, DrbgParameters.Capability.NONE, null)));
    probe("drbg params", () -> {
      SecureRandom d = SecureRandom.getInstance("DRBG", DrbgParameters.instantiation(256, DrbgParameters.Capability.PR_AND_RESEED, "ps".getBytes()));
      byte[] a = new byte[100];
      d.nextBytes(a, DrbgParameters.nextBytes(256, true, "extra".getBytes()));
      d.reseed();
      d.reseed(DrbgParameters.reseed(true, "x".getBytes()));
      d.setSeed(new byte[] {1, 2});
      byte[] b = new byte[100];
      d.nextBytes(b);
      return d + "|" + d.getParameters() + "|" + spread(a) + spread(b) + "|" + Arrays.equals(a, b);
    });
    probe("drbg default params", () -> {
      SecureRandom d = SecureRandom.getInstance("DRBG");
      DrbgParameters.Instantiation p = (DrbgParameters.Instantiation) d.getParameters();
      return p.getStrength() + " " + p.getCapability() + " " + p.getPersonalizationString() + " " + p;
    });
    probe("drbg high strength", () -> SecureRandom.getInstance("DRBG", DrbgParameters.instantiation(512, DrbgParameters.Capability.NONE, null)));
    probe("drbg neg strength", () -> DrbgParameters.instantiation(-2, DrbgParameters.Capability.NONE, null));
    probe("drbg nextBytes too strong", () -> {
      SecureRandom d = SecureRandom.getInstance("DRBG");
      d.nextBytes(new byte[4], DrbgParameters.nextBytes(256, false, null));
      return "ok";
    });
    probe("drbg pr unsupported", () -> {
      SecureRandom d = SecureRandom.getInstance("DRBG");
      d.nextBytes(new byte[4], DrbgParameters.nextBytes(128, true, null));
      return "ok";
    });
    probe("drbg reseed none", () -> {
      SecureRandom d = SecureRandom.getInstance("DRBG", DrbgParameters.instantiation(128, DrbgParameters.Capability.NONE, null));
      d.reseed();
      return "ok";
    });
    probe("nextBytes params null", () -> {
      SecureRandom d = SecureRandom.getInstance("DRBG");
      d.nextBytes(new byte[4], null);
      return "ok";
    });
    probe("nextBytes params unsupported", () -> {
      SecureRandom d = SecureRandom.getInstance("SHA1PRNG");
      d.nextBytes(new byte[4], DrbgParameters.nextBytes(128, false, null));
      return "ok";
    });
    probe("reseed unsupported", () -> {
      SecureRandom d = SecureRandom.getInstance("SHA1PRNG");
      d.reseed();
      return "ok";
    });
    probe("reseed null params", () -> {
      SecureRandom d = SecureRandom.getInstance("DRBG");
      d.reseed(null);
      return "ok";
    });
    probe("capability", () -> {
      StringBuilder sb = new StringBuilder();
      for (DrbgParameters.Capability c : DrbgParameters.Capability.values()) {
        sb.append(c).append(':').append(c.supportsReseeding()).append(c.supportsPredictionResistance()).append(' ');
      }
      return sb + " " + DrbgParameters.Capability.valueOf("NONE");
    });
    probe("strong", () -> {
      SecureRandom s = SecureRandom.getInstanceStrong();
      return s.getAlgorithm() + " " + s.getProvider().getName() + " " + s.generateSeed(8).length;
    });
    probe("getSeed", () -> SecureRandom.getSeed(24).length + " " + SecureRandom.getSeed(0).length);
    probe("getSeed neg", () -> SecureRandom.getSeed(-1));
    probe("generateSeed neg", () -> SecureRandom.getInstance("SHA1PRNG").generateSeed(-1));
    probe("random api", () -> {
      SecureRandom r = new SecureRandom();
      int n = r.nextInt(10);
      long l = r.nextLong(100);
      double d = r.nextDouble(1.0, 2.0);
      return (n >= 0 && n < 10) + " " + (l >= 0 && l < 100) + " " + (d >= 1.0 && d < 2.0) + " " + r.ints(5).count() + " " + r.doubles(3).count()
          + " " + (r.nextGaussian() == r.nextGaussian());
    });
    probe("serialization-free clone state", () -> {
      SecureRandom a = SecureRandom.getInstance("SHA1PRNG");
      a.setSeed(new byte[] {8});
      a.nextInt();
      return a.nextInt();
    });
    probe("shuffle deterministic", () -> {
      SecureRandom a = SecureRandom.getInstance("SHA1PRNG");
      a.setSeed(new byte[] {1, 1, 1});
      List<Integer> l = new ArrayList<>();
      for (int i = 0; i < 20; i++) {
        l.add(i);
      }
      Collections.shuffle(l, a);
      return l;
    });
  }
}
