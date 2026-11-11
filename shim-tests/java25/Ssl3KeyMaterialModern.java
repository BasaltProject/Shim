/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */
// javac-args: --add-exports java.base/sun.security.internal.spec=ALL-UNNAMED
// jvm-args: --add-exports java.base/sun.security.internal.spec=ALL-UNNAMED
import javax.crypto.*;
import javax.crypto.spec.*;
import sun.security.internal.spec.*;
import java.util.*;
public class Ssl3KeyMaterialModern {
  public static void main(String[] a) throws Exception {
    byte[] pm = new byte[48]; byte[] r = new byte[32]; byte[] r2 = new byte[32];
    for (int v : new int[] {0x0300, 0x0301}) for (int mac : new int[] {0, 16, 20, 32, 48, 200}) for (int key : new int[] {0, 5, 16, 32}) for (int exp : new int[] {0, 5, 16, 40}) for (int iv : new int[] {0, 8, 16, 100}) {
      String s = v + " mac=" + mac + " key=" + key + " exp=" + exp + " iv=" + iv + " -> ";
      try {
        KeyGenerator g = KeyGenerator.getInstance("SunTlsKeyMaterial");
        g.init(new TlsKeyMaterialParameterSpec(new SecretKeySpec(pm, "TlsMasterSecret"), v >> 8, v & 255, r, r2, "AES", key, exp, iv, mac, "SHA-256", 32, 64));
        TlsKeyMaterialSpec k = (TlsKeyMaterialSpec) g.generateKey();
        s += Arrays.toString(k.getClientCipherKey() == null ? (byte[]) null : k.getClientCipherKey().getEncoded()).hashCode() + " " + (k.getClientIv() == null ? -1 : k.getClientIv().getIV().length);
      } catch (Throwable e) { s += e.getClass().getName() + ": " + e.getMessage(); }
      System.out.println(s);
    }
  }
}
