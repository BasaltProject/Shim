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

public class Iso2022Modern {
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
    t("avail", () -> {
      List<String> l = new ArrayList<>();
      for (String n : new String[] {"ISO-2022-JP", "csISO2022JP", "iso2022jp", "JIS"}) {
        l.add(n + ":" + Charset.isSupported(n));
      }
      return l;
    });
    t("props", () -> {
      Charset cs = Charset.forName("ISO-2022-JP");
      return cs.name() + " " + new TreeSet<>(cs.aliases()) + " " + cs.canEncode() + " " + cs.isRegistered() + " "
          + cs.newEncoder().maxBytesPerChar() + " " + cs.newEncoder().averageBytesPerChar() + " "
          + cs.newDecoder().maxCharsPerByte() + " " + cs.newDecoder().averageCharsPerByte() + " "
          + hex(cs.newEncoder().replacement());
    });
    String[] samples = {"", "abc", "日本語", "abc日本語def", "ｱｲｳ", "\u00a5\u203e", "a\u00a5b", "漢字\n漢字", "ひらがなカタカナ", "全角ＡＢＣ１２３", "～〜", "€", "\u3000", "日本\r\n語", "a日b本c語"};
    for (String s : samples) {
      t("enc " + s.replace("\n", "\\n").replace("\r", "\\r"), () -> hex(s.getBytes(Charset.forName("ISO-2022-JP"))));
    }
    t("dec", () -> {
      Charset cs = Charset.forName("ISO-2022-JP");
      String[] in = {"1b244242" + "3c2b", "1b2442467c4b5c385c1b2842", "616263", "1b284a5c7e", "1b2840", "1b2442", "1b244224", "1b28", "1b2442213f1b2842", "1b2442303000", "0e41", "1b24412121", "1b2428443021", "1b28493021"};
      List<String> out = new ArrayList<>();
      for (String h : in) {
        byte[] b = new byte[h.length() / 2];
        for (int i = 0; i < b.length; i++) {
          b[i] = (byte) Integer.parseInt(h.substring(2 * i, 2 * i + 2), 16);
        }
        StringBuilder sb = new StringBuilder();
        for (char c : new String(b, cs).toCharArray()) {
          sb.append(String.format("%04x ", (int) c));
        }
        out.add(h + "->" + sb.toString().trim());
      }
      return String.join(" | ", out);
    });
    t("dec-strict", () -> {
      CharsetDecoder d = Charset.forName("ISO-2022-JP").newDecoder().onMalformedInput(CodingErrorAction.REPORT).onUnmappableCharacter(CodingErrorAction.REPORT);
      List<String> out = new ArrayList<>();
      for (String h : new String[] {"1b2442", "1b244221", "1b24422121", "1b2442ff21", "80", "1b2900", "0e", "0f", "1b2442213f", "1b244221"}) {
        byte[] b = new byte[h.length() / 2];
        for (int i = 0; i < b.length; i++) {
          b[i] = (byte) Integer.parseInt(h.substring(2 * i, 2 * i + 2), 16);
        }
        try {
          out.add(h + ":" + d.reset().decode(ByteBuffer.wrap(b)).toString().length());
        } catch (CharacterCodingException e) {
          out.add(h + ":" + e.getClass().getSimpleName());
        }
      }
      return out;
    });
    t("enc-strict", () -> {
      CharsetEncoder e = Charset.forName("ISO-2022-JP").newEncoder().onMalformedInput(CodingErrorAction.REPORT).onUnmappableCharacter(CodingErrorAction.REPORT);
      List<String> out = new ArrayList<>();
      for (String s : new String[] {"€", "a€", "\ud800", "日\ud800", "ｱ", "\u00e9"}) {
        try {
          out.add(hex(e.reset().encode(CharBuffer.wrap(s)).array()).length() + "");
        } catch (CharacterCodingException x) {
          out.add(x.getClass().getSimpleName());
        }
      }
      return out;
    });
    t("can-encode", () -> {
      CharsetEncoder e = Charset.forName("ISO-2022-JP").newEncoder();
      return e.canEncode('a') + " " + e.canEncode('日') + " " + e.canEncode('ｱ') + " " + e.canEncode('€') + " " + e.canEncode('\u00a5') + " " + e.canEncode("日本");
    });
    t("roundtrip", () -> {
      Charset cs = Charset.forName("ISO-2022-JP");
      String s = "テスト test 日本語 \u00a5 end\n次の行";
      return new String(s.getBytes(cs), cs).equals(s);
    });
    t("chunked-dec", () -> {
      Charset cs = Charset.forName("ISO-2022-JP");
      byte[] b = "abc日本語def".getBytes(cs);
      CharsetDecoder d = cs.newDecoder();
      StringBuilder sb = new StringBuilder();
      for (int i = 0; i < b.length; i++) {
        ByteBuffer in = ByteBuffer.wrap(b, i, 1);
        CharBuffer out = CharBuffer.allocate(8);
        d.decode(in, out, i == b.length - 1);
        out.flip();
        sb.append(out);
      }
      return sb;
    });
    t("chunked-enc", () -> {
      Charset cs = Charset.forName("ISO-2022-JP");
      CharsetEncoder e = cs.newEncoder();
      String s = "abc日本語def";
      StringBuilder sb = new StringBuilder();
      for (int i = 0; i < s.length(); i++) {
        ByteBuffer out = ByteBuffer.allocate(16);
        e.encode(CharBuffer.wrap(s, i, i + 1), out, i == s.length() - 1);
        out.flip();
        sb.append(hex(Arrays.copyOf(out.array(), out.limit())));
      }
      ByteBuffer out = ByteBuffer.allocate(16);
      e.flush(out);
      out.flip();
      return sb + " flush=" + hex(Arrays.copyOf(out.array(), out.limit()));
    });
  }
}
