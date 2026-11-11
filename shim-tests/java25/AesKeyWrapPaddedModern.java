/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

import java.util.HexFormat;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class AesKeyWrapPaddedModern {
  public static void main(String[] args) throws Exception {
    HexFormat hex = HexFormat.of();
    SecretKeySpec kek = new SecretKeySpec(hex.parseHex("5840df6e29b02af1ab493b705bf16ea1ae8338f4dcc176a8"), "AES");
    Cipher wrap = Cipher.getInstance("AES/KWP/NoPadding");
    wrap.init(Cipher.ENCRYPT_MODE, kek);
    byte[] wrapped = wrap.doFinal(hex.parseHex("466f7250617369"));
    System.out.println(hex.formatHex(wrapped));
    Cipher unwrap = Cipher.getInstance("AES_192/KWP/NoPadding");
    unwrap.init(Cipher.DECRYPT_MODE, kek);
    System.out.println(hex.formatHex(unwrap.doFinal(wrapped)));
    byte[] longKey = hex.parseHex("c37b7e6492584340bed12207808941155068f738");
    wrap.init(Cipher.ENCRYPT_MODE, kek);
    byte[] w2 = wrap.doFinal(longKey);
    System.out.println(hex.formatHex(w2));
    unwrap.init(Cipher.DECRYPT_MODE, kek);
    System.out.println(hex.formatHex(unwrap.doFinal(w2)));
  }
}
