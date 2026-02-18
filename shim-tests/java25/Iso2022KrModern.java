/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 * Minimum release: 21
 */

import java.nio.*; import java.nio.charset.*; import java.util.*;
public class Iso2022KrModern {
  static String hex(byte[] b) { StringBuilder sb = new StringBuilder(); for (byte x : b) sb.append(String.format("%02x", x)); return sb.toString(); }
  static byte[] unhex(String h) { byte[] b = new byte[h.length() / 2]; for (int i = 0; i < b.length; i++) b[i] = (byte) Integer.parseInt(h.substring(2 * i, 2 * i + 2), 16); return b; }
  public static void main(String[] a) throws Exception {
    Charset cs = Charset.forName("ISO-2022-KR");
    System.out.println(cs.name() + " " + new TreeSet<>(cs.aliases()) + " " + cs.canEncode() + " " + cs.newEncoder().maxBytesPerChar() + " " + cs.newEncoder().averageBytesPerChar() + " " + cs.newDecoder().maxCharsPerByte() + " " + cs.newDecoder().averageCharsPerByte() + " " + hex(cs.newEncoder().replacement()));
    for (String s : new String[]{"", "abc", "한국어", "abc한국어def", "한\n한", "a한b\r\n국", "€", "한€", "日本", "\u00a5"}) System.out.println("enc " + s.replace("\n","\\n").replace("\r","\\r") + " = " + hex(s.getBytes(cs)));
    for (String h : new String[]{"1b242943", "1b2429430e4a550f", "1b2429430e4a55", "0e4a55", "1b2429430e4a550f0a0e4a55", "616263", "1b24294361620e4a550f6364", "1b2429430e", "1b2429430e4a", "1b2429430e0a", "80", "1b2429430e4a2100", "1b242943ff"}) {
      CharsetDecoder d = cs.newDecoder().onMalformedInput(CodingErrorAction.REPLACE).onUnmappableCharacter(CodingErrorAction.REPLACE);
      StringBuilder sb = new StringBuilder();
      for (char c : d.decode(ByteBuffer.wrap(unhex(h))).toString().toCharArray()) sb.append(String.format("%04x ", (int) c));
      CharsetDecoder s = cs.newDecoder().onMalformedInput(CodingErrorAction.REPORT).onUnmappableCharacter(CodingErrorAction.REPORT);
      String st; try { st = "" + s.decode(ByteBuffer.wrap(unhex(h))).length(); } catch (CharacterCodingException e) { st = e.getClass().getSimpleName(); }
      System.out.println("dec " + h + " -> " + sb.toString().trim() + " | " + st);
    }
    CharsetEncoder e = cs.newEncoder().onMalformedInput(CodingErrorAction.REPORT).onUnmappableCharacter(CodingErrorAction.REPORT);
    for (String s : new String[]{"€", "a€", "\ud800", "한", "ｱ"}) { try { System.out.println("encs " + hex(e.reset().encode(CharBuffer.wrap(s)).array())); } catch (CharacterCodingException x) { System.out.println("encs " + x.getClass().getSimpleName()); } }
    CharsetEncoder e2 = cs.newEncoder();
    ByteBuffer o = ByteBuffer.allocate(40); e2.encode(CharBuffer.wrap("한a"), o, false); o.flip(); System.out.println("partial " + hex(Arrays.copyOf(o.array(), o.limit())));
    e2.reset(); o = ByteBuffer.allocate(40); e2.encode(CharBuffer.wrap("한"), o, true); e2.flush(o); o.flip(); System.out.println("full " + hex(Arrays.copyOf(o.array(), o.limit())));
    e2.reset(); o = ByteBuffer.allocate(40); e2.encode(CharBuffer.wrap("abc"), o, true); e2.flush(o); o.flip(); System.out.println("asciionly " + hex(Arrays.copyOf(o.array(), o.limit())));
    System.out.println("can " + e2.canEncode('a') + e2.canEncode('한') + e2.canEncode('€') + e2.canEncode('ｱ'));
  }
}
