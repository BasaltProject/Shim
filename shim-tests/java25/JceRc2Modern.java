/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 * Minimum release: 21
 */

import javax.crypto.*; import javax.crypto.spec.*; import java.util.*;
public class JceRc2Modern {
  static String hx(byte[] b) { return HexFormat.of().formatHex(b); }
  public static void main(String[] a) throws Exception {
    byte[] zero = new byte[8];
    Cipher c = Cipher.getInstance("RC2/ECB/NoPadding");
    c.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(new byte[8], "RC2"), new RC2ParameterSpec(63));
    System.out.println(hx(c.doFinal(zero)));
    byte[] key = new byte[16]; for (int i = 0; i < 16; i++) key[i] = (byte) (i * 7 + 1);
    byte[] data = new byte[37]; for (int i = 0; i < data.length; i++) data[i] = (byte) (i * 13 + 5);
    byte[] iv = {1, 2, 3, 4, 5, 6, 7, 8};
    for (int bits : new int[]{40, 64, 128, 1024}) {
      for (String t : new String[]{"RC2/CBC/PKCS5Padding", "RC2/ECB/PKCS5Padding", "RC2/CFB/NoPadding", "RC2/OFB8/NoPadding", "RC2/CTR/NoPadding"}) {
        try {
          Cipher e = Cipher.getInstance(t);
          byte[] k = Arrays.copyOf(key, bits == 40 ? 5 : bits == 64 ? 8 : 16);
          e.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(k, "RC2"), t.contains("ECB") ? new RC2ParameterSpec(bits) : new RC2ParameterSpec(bits, iv));
          byte[] ct = e.doFinal(data);
          Cipher d = Cipher.getInstance(t);
          d.init(Cipher.DECRYPT_MODE, new SecretKeySpec(k, "RC2"), t.contains("ECB") ? new RC2ParameterSpec(bits) : new RC2ParameterSpec(bits, iv));
          System.out.println(bits + " " + t + " " + hx(ct) + " " + Arrays.equals(d.doFinal(ct), data));
        } catch (Exception ex) { System.out.println(bits + " " + t + " !" + ex); }
      }
    }
    Cipher n = Cipher.getInstance("RC2/CBC/PKCS5Padding");
    n.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(Arrays.copyOf(key, 5), "RC2"), new IvParameterSpec(iv));
    System.out.println(hx(n.doFinal(data)) + " " + n.getBlockSize() + " " + hx(n.getIV()));
    try { n.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(new byte[0 + 1], "AES")); System.out.println(n.getParameters() != null); } catch (Exception ex) { System.out.println(ex); }
    try { Cipher.getInstance("RC2").init(Cipher.ENCRYPT_MODE, new SecretKeySpec(new byte[129], "RC2")); } catch (Exception ex) { System.out.println(ex); }
    System.out.println(Cipher.getInstance("RC2").getBlockSize());
  }
}
