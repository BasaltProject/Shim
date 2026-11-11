/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

import java.security.*;
import java.util.Arrays;
import javax.crypto.KEM;

public class PostQuantumModern {
  public static void main(String[] a) throws Exception {
    for (String n : new String[] {"ML-KEM-512", "ML-KEM-768", "ML-KEM-1024"}) {
      KeyPair kp = KeyPairGenerator.getInstance(n).generateKeyPair();
      KEM kem = KEM.getInstance(n);
      KEM.Encapsulated e = kem.newEncapsulator(kp.getPublic()).encapsulate();
      byte[] k = kem.newDecapsulator(kp.getPrivate()).decapsulate(e.encapsulation()).getEncoded();
      System.out.println(n + " " + kp.getPublic().getEncoded().length + " " + kp.getPrivate().getEncoded().length + " "
          + e.encapsulation().length + " " + Arrays.equals(k, e.key().getEncoded()));
    }
    byte[] msg = "message".getBytes();
    for (String n : new String[] {"ML-DSA-44", "ML-DSA-65", "ML-DSA-87"}) {
      KeyPair kp = KeyPairGenerator.getInstance(n).generateKeyPair();
      Signature s = Signature.getInstance(n);
      s.initSign(kp.getPrivate());
      s.update(msg);
      byte[] sig = s.sign();
      s.initVerify(kp.getPublic());
      s.update(msg);
      boolean ok = s.verify(sig);
      s.initVerify(kp.getPublic());
      s.update("other".getBytes());
      System.out.println(n + " " + kp.getPublic().getEncoded().length + " " + sig.length + " " + ok + " " + s.verify(sig));
    }
  }
}
