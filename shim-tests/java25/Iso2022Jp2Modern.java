/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 * Minimum release: 21
 */

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.*;
import java.util.*;
import java.util.concurrent.Callable;

public class Iso2022Jp2Modern {
  static void t(String name, Callable<Object> c) {
    try {
      System.out.println(name + " = " + c.call());
    } catch (Throwable e) {
      System.out.println(name + " ! " + e.getClass().getName() + ": " + e.getMessage());
    }
  }

  static String hex(byte[] b) {
    StringBuilder sb = new StringBuilder();
    for (byte x : b) {
      sb.append(String.format("%02x", x));
    }
    return sb.toString();
  }

  public static void main(String[] args) {
    Charset cs = Charset.forName("ISO-2022-JP-2");
    t("props", () -> cs.name() + " " + new TreeSet<>(cs.aliases()) + " " + cs.canEncode() + " " + cs.isRegistered()
        + " " + cs.newEncoder().maxBytesPerChar() + " " + cs.newEncoder().averageBytesPerChar() + " "
        + cs.newDecoder().maxCharsPerByte() + " " + cs.newDecoder().averageCharsPerByte() + " "
        + hex(cs.newEncoder().replacement()) + " " + Charset.isSupported("csISO2022JP2"));
    String[] ss = {"abc", "日本語", "é", "àéîõü", "α", "αβγ日", "中文", "한국어", "한a日", "日é中한α", "ÿ¡", "abc日def",
        "Ωx", "¥~", "ｱｲ", "日本\n日", "éé日", "α日α", "中日中", "한日한", "Ж", "€", "😀", "a©b", "日°", "ÀÉ日é\n é"};
    for (String s : ss) {
      t("enc " + s.replace("\n", "\\n"), () -> hex(s.getBytes(cs)));
    }
    String[] bs = {"1b2442 3021 1b2842", "1b2441 5650 1b2842", "1b242843 3021 1b2842", "1b242844 3021 1b2842",
        "1b2e41 1b4e 61 62", "1b2e46 1b4e 61", "1b2e41 61 1b4e 61", "1b2842 1b4e 61", "1b284a 5c7e 1b2842",
        "1b2849 3141", "0e 41", "1b2e41 0a 1b4e61", "1b2442 3021 0a 3021", "1b2441 5650 0a 5650", "1b242844 7e7e",
        "1b2442 7e7e", "1b2e41 1b4e 20", "1b2441 2121 1b242843 2121", "1b242841 5650", "1b242844 2b31 2b22 0a 2b31",
        "1b242844 226d 1b2842 62", "1b242844 3021 1b2442 3021 1b242844 3021"};
    for (String b : bs) {
      byte[] raw = HexFormat.of().parseHex(b.replace(" ", ""));
      t("dec " + b, () -> new String(raw, cs).chars().boxed().map(Integer::toHexString).toList());
    }
    for (String b : new String[] {"1b2442 3021", "1b2e41", "1b24", "1b", "1b2442 30", "1b2e41 1b4e", "1b242844",
        "1b242844 2b", "1b2428"}) {
      byte[] raw = HexFormat.of().parseHex(b.replace(" ", ""));
      t("strict " + b, () -> cs.newDecoder().decode(ByteBuffer.wrap(raw)).toString().chars().boxed()
          .map(Integer::toHexString).toList());
    }
    t("chunked", () -> {
      byte[] raw = "日本é語 ÀÉ\nx".getBytes(cs);
      CharsetDecoder d = cs.newDecoder();
      StringBuilder out = new StringBuilder();
      for (int step = 1; step <= 4; step++) {
        d.reset();
        out.setLength(0);
        CharBuffer cb = CharBuffer.allocate(64);
        ByteBuffer in = ByteBuffer.allocate(raw.length + 8);
        int fed = 0;
        in.limit(0);
        while (fed < raw.length) {
          int n = Math.min(step, raw.length - fed);
          in.compact();
          in.put(raw, fed, n);
          in.flip();
          fed += n;
          d.decode(in, cb, fed == raw.length);
        }
        d.flush(cb);
        cb.flip();
        out.append(cb);
      }
      return out + " " + hex(raw);
    });
  }
}
