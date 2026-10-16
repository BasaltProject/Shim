/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 * Minimum release: 21
 */

import java.text.Normalizer;
import java.util.function.Supplier;

public class NormalizerModern {
  static void probe(String name, Supplier<Object> body) {
    String out;
    try {
      out = String.valueOf(body.get());
    } catch (Throwable t) {
      out = "!" + t.getClass().getName() + ":" + t.getMessage();
    }
    System.out.println(name + "=" + out);
  }

  static String esc(String s) {
    StringBuilder sb = new StringBuilder();
    s.codePoints().forEach(c -> sb.append(String.format("%04x ", c)));
    return sb.toString();
  }

  public static void main(String[] args) {
    String[] samples = {"", "abc", "Ångström", "Å", "Å", "ẛ̣", "ḍ̇", "ḍ̇", "ﬁ", " x", "①", "㎒",
        "각", "각", "가", "한글", "ẹ́", "ẹ́", "क़", "क़", "̈́", "ཱི", "Ω", "𝅗𝅥", "𝅗𝅥", "\ud800x", "x\udc00", "à֮̀̕b",
        "῭", "΅", "ϓ", "¾", "ﷺ", "ｶﾞ", "ガ", " ", "Ạ̈́", "ṩ", "ṩ", "ୋ", "ো"};
    for (Normalizer.Form f : Normalizer.Form.values()) {
      probe("samples-" + f, () -> {
        StringBuilder sb = new StringBuilder();
        for (String s : samples) sb.append(esc(Normalizer.normalize(s, f))).append("| ");
        return sb;
      });
      probe("isNormalized-" + f, () -> {
        StringBuilder sb = new StringBuilder();
        for (String s : samples) sb.append(Normalizer.isNormalized(s, f) ? 'Y' : 'N');
        return sb;
      });
    }
    probe("idempotent", () -> {
      for (Normalizer.Form f : Normalizer.Form.values()) for (String s : samples) { String n = Normalizer.normalize(s, f); if (!Normalizer.normalize(n, f).equals(n)) return "FAIL " + f + " " + esc(s); }
      return "ok";
    });
    probe("full-sweep", () -> {
      long h = 0;
      for (int cp = 0; cp < 0x30000; cp = cp == 0x3000 ? 0xAC00 : cp == 0xAD00 ? 0xF900 : cp == 0x10000 ? 0x1D100 : cp == 0x1D200 ? 0x2F800 : cp == 0x2FA20 ? 0x30000 : cp + 1) {
        if (cp >= 0xD800 && cp <= 0xDFFF) continue;
        String s = new String(Character.toChars(cp)) + "́";
        for (Normalizer.Form f : Normalizer.Form.values()) h = h * 31 + Normalizer.normalize(s, f).hashCode();
        h = h * 31 + Normalizer.normalize(new String(Character.toChars(cp)), Normalizer.Form.NFKC).hashCode();
      }
      return h;
    });
    probe("charsequence", () -> Normalizer.normalize(new StringBuilder("Å"), Normalizer.Form.NFC).length());
    probe("null-form", () -> { try { Normalizer.normalize("a", null); return "none"; } catch (NullPointerException e) { return "npe"; } });
    probe("null-src", () -> { try { Normalizer.normalize(null, Normalizer.Form.NFC); return "none"; } catch (NullPointerException e) { return "npe"; } });
    probe("null-isnorm", () -> { try { Normalizer.isNormalized(null, Normalizer.Form.NFC); return "none"; } catch (NullPointerException e) { return "npe"; } });
    probe("forms", () -> java.util.Arrays.toString(Normalizer.Form.values()) + " " + Normalizer.Form.valueOf("NFKC").ordinal());
  }
}
