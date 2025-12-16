/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 * Minimum release: 21
 */

import java.security.AlgorithmParameters;
import java.util.HexFormat;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class LegacyPbeModern {
  static final String[] ALGORITHMS = {"PBEWithMD5AndDES", "PBEWithMD5AndTripleDES", "PBEWithSHA1AndDESede",
      "PBEWithSHA1AndRC2_40", "PBEWithSHA1AndRC2_128", "PBEWithSHA1AndRC4_40", "PBEWithSHA1AndRC4_128"};

  interface Op { Object run() throws Exception; }

  static void t(String name, Op op) {
    try {
      System.out.println(name + " = " + op.run());
    } catch (Exception e) {
      System.out.println(name + " ! " + e.getClass().getSimpleName() + ": " + e.getMessage());
    }
  }

  public static void main(String[] args) throws Exception {
    byte[] data = "hello world, this is longer than a block".getBytes();
    byte[][] salts = {{1, 2, 3, 4, 5, 6, 7, 8}, {1, 2, 3, 4, 1, 2, 3, 4}, {9, 9, 9, 9, 9, 9, 9, 9},
        {9, 8, 7, 6, 5, 4, 3, 2, 1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10}};
    for (String algorithm : ALGORITHMS) {
      for (String password : new String[] {"password", "", "pässwörd€", "x"}) {
        for (byte[] salt : salts) {
          for (int iterations : new int[] {1, 2, 1000}) {
            String label = algorithm + " " + password.length() + " " + salt.length + " " + (int) salt[0] + " "
                + iterations;
            t(label, () -> {
              SecretKey key = SecretKeyFactory.getInstance(algorithm).generateSecret(new PBEKeySpec(
                  password.toCharArray()));
              Cipher cipher = Cipher.getInstance(algorithm);
              cipher.init(Cipher.ENCRYPT_MODE, key, new PBEParameterSpec(salt, iterations));
              byte[] encrypted = cipher.doFinal(data);
              Cipher back = Cipher.getInstance(algorithm);
              back.init(Cipher.DECRYPT_MODE, key, new PBEParameterSpec(salt, iterations));
              boolean same = java.util.Arrays.equals(back.doFinal(encrypted), data);
              return HexFormat.of().formatHex(encrypted) + " " + same + " "
                  + (cipher.getIV() == null ? "-" : HexFormat.of().formatHex(cipher.getIV()));
            });
          }
        }
      }
      byte[] salt = {1, 2, 3, 4, 5, 6, 7, 8};
      SecretKey key = SecretKeyFactory.getInstance(algorithm).generateSecret(new PBEKeySpec("pw".toCharArray()));
      Cipher cipher = Cipher.getInstance(algorithm);
      t(algorithm + " block", () -> cipher.getBlockSize());
      t(algorithm + " decrypt-without-parameters", () -> {
        cipher.init(Cipher.DECRYPT_MODE, key);
        return "ok";
      });
      t(algorithm + " wrong-spec", () -> {
        cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(new byte[8]));
        return "ok";
      });
      t(algorithm + " short-salt", () -> {
        cipher.init(Cipher.ENCRYPT_MODE, key, new PBEParameterSpec(new byte[4], 10));
        return "ok";
      });
      t(algorithm + " zero-iterations", () -> {
        cipher.init(Cipher.ENCRYPT_MODE, key, new PBEParameterSpec(salt, 0));
        return "ok";
      });
      t(algorithm + " bad-key", () -> {
        cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(new byte[8], "DES"), new PBEParameterSpec(salt, 10));
        return "ok";
      });
      t(algorithm + " parameters", () -> {
        cipher.init(Cipher.ENCRYPT_MODE, key, new PBEParameterSpec(salt, 100));
        AlgorithmParameters parameters = cipher.getParameters();
        return HexFormat.of().formatHex(parameters.getEncoded()) + " " + parameters.getAlgorithm() + " "
            + parameters.toString().replace(System.lineSeparator(), "|");
      });
      t(algorithm + " random-salt", () -> {
        cipher.init(Cipher.ENCRYPT_MODE, key);
        PBEParameterSpec spec = cipher.getParameters().getParameterSpec(PBEParameterSpec.class);
        return spec.getSalt().length + " " + spec.getIterationCount();
      });
      t(algorithm + " key-spec", () -> {
        SecretKeyFactory factory = SecretKeyFactory.getInstance(algorithm);
        PBEKeySpec spec = (PBEKeySpec) factory.getKeySpec(key, PBEKeySpec.class);
        return new String(spec.getPassword()) + " " + key.getFormat() + " "
            + HexFormat.of().formatHex(key.getEncoded());
      });
      t(algorithm + " wrong-key-spec", () -> SecretKeyFactory.getInstance(algorithm).generateSecret(
          new SecretKeySpec(new byte[8], "DES")));
    }
    byte[] rc4Key = {1, 2, 3, 4, 5, 6, 7, 8};
    for (String name : new String[] {"ARCFOUR", "RC4"}) {
      t(name + " cipher", () -> {
        Cipher c = Cipher.getInstance(name);
        c.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(rc4Key, "ARCFOUR"));
        byte[] first = c.update(data);
        byte[] second = c.doFinal(data);
        return HexFormat.of().formatHex(first) + " " + HexFormat.of().formatHex(second);
      });
    }
    t("ARCFOUR bad-key", () -> {
      Cipher c = Cipher.getInstance("ARCFOUR");
      c.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(new byte[3], "ARCFOUR"));
      return "ok";
    });
    t("ARCFOUR generator", () -> javax.crypto.KeyGenerator.getInstance("RC4").generateKey().getEncoded().length);
  }
}
