/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 * Minimum release: 21
 */

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;
import java.util.Random;
import java.util.TreeSet;
import java.util.zip.CRC32;

public class CharsetsCjkExtendedModern {
  static final String[] NAMES = {"x-Big5-HKSCS-2001", "x-Big5-Solaris", "x-MS950-HKSCS", "x-MS950-HKSCS-XP", "x-PCK",
      "x-euc-jp-linux", "x-eucJP-Open", "x-SJIS_0213", "x-MS932_0213", "x-EUC-TW"};
  static final int[][] PREFIXES = {{0x8e}, {0x8f}, {0x8e, 0xa1}, {0x8e, 0xa2}, {0x8e, 0xa3}, {0x8e, 0xa7},
      {0x8e, 0xb0}};

  static final CRC32 crc = new CRC32();

  static void add(String s) {
    for (int i = 0; i < s.length(); i++) {
      char c = s.charAt(i);
      crc.update(c >> 8);
      crc.update(c);
    }
  }

  static void addResult(CoderResult r, int pos) {
    add(r.isError() ? (r.isMalformed() ? "M" : "U") + r.length() : r.isOverflow() ? "O" : "_");
    add(Integer.toString(pos));
  }

  static String decodeTrace(CharsetDecoder d, byte[] in, int chunk, int cap) {
    d.reset();
    StringBuilder sb = new StringBuilder();
    ByteBuffer src = ByteBuffer.wrap(in);
    CharBuffer dst = CharBuffer.allocate(cap);
    int lim = 0;
    for (int guard = 0; guard < 10000; guard++) {
      lim = Math.min(in.length, Math.max(lim, src.position()) + chunk);
      src.limit(lim);
      boolean eoi = lim == in.length;
      int before = src.position();
      CoderResult r = d.decode(src, dst, eoi);
      if (r.isOverflow() && dst.position() == 0 && src.position() == before) {
        dst = CharBuffer.allocate(cap *= 2);
        continue;
      }
      dst.flip();
      sb.append(dst).append(r.isError() ? r.toString() : r.isOverflow() ? "O" : "U").append(src.position()).append(' ');
      dst.clear();
      if (r.isError()) {
        src.position(src.position() + r.length());
      } else if (r.isUnderflow() && eoi) {
        r = d.flush(dst);
        dst.flip();
        sb.append(dst).append(r.isOverflow() ? "FO" : "FU");
        break;
      }
    }
    return sb.toString();
  }

  static String encodeTrace(CharsetEncoder e, String in, int chunk, int cap) {
    e.reset();
    StringBuilder sb = new StringBuilder();
    CharBuffer src = CharBuffer.wrap(in);
    ByteBuffer dst = ByteBuffer.allocate(cap);
    int lim = 0;
    for (int guard = 0; guard < 10000; guard++) {
      lim = Math.min(in.length(), Math.max(lim, src.position()) + chunk);
      src.limit(lim);
      boolean eoi = lim == in.length();
      int before = src.position();
      CoderResult r = e.encode(src, dst, eoi);
      if (r.isOverflow() && dst.position() == 0 && src.position() == before) {
        dst = ByteBuffer.allocate(cap *= 2);
        continue;
      }
      for (int i = 0; i < dst.position(); i++) {
        sb.append(Integer.toHexString(dst.get(i) & 0xff)).append(',');
      }
      dst.clear();
      sb.append(r.isError() ? r.toString() : r.isOverflow() ? "O" : "U").append(src.position()).append(' ');
      if (r.isError()) {
        src.position(src.position() + r.length());
      } else if (r.isUnderflow() && eoi) {
        dst = ByteBuffer.allocate(16);
        r = e.flush(dst);
        for (int i = 0; i < dst.position(); i++) {
          sb.append(Integer.toHexString(dst.get(i) & 0xff)).append(',');
        }
        sb.append(r.isOverflow() ? "FO" : "FU");
        break;
      }
    }
    return sb.toString();
  }

  static long decodeAll(CharsetDecoder d, int[] prefix) {
    crc.reset();
    byte[] seq = new byte[prefix.length + 2];
    for (int i = 0; i < prefix.length; i++) {
      seq[i] = (byte) prefix[i];
    }
    CharBuffer out = CharBuffer.allocate(16);
    for (int b1 = 0; b1 < 256; b1++) {
      for (int b2 = 0; b2 < 256; b2++) {
        seq[prefix.length] = (byte) b1;
        seq[prefix.length + 1] = (byte) b2;
        d.reset();
        out.clear();
        ByteBuffer in = ByteBuffer.wrap(seq);
        CoderResult r = d.decode(in, out, true);
        addResult(r, in.position());
        if (!r.isError()) {
          addResult(d.flush(out), 0);
        }
        out.flip();
        add(out.toString());
      }
    }
    return crc.getValue();
  }

  static long encodeAll(CharsetEncoder e) {
    crc.reset();
    ByteBuffer out = ByteBuffer.allocate(32);
    for (int cp = 0; cp < 0x110000; cp++) {
      if (cp >= 0xd800 && cp < 0xe000) {
        continue;
      }
      e.reset();
      out.clear();
      CharBuffer in = CharBuffer.wrap(Character.toChars(cp));
      CoderResult r = e.encode(in, out, true);
      if (!r.isError()) {
        r = e.flush(out);
      }
      if (r.isError()) {
        crc.update(r.isMalformed() ? 1 : 2);
        crc.update(r.length());
      } else {
        crc.update(out.array(), 0, out.position());
      }
      if (cp < 0x10000) {
        e.reset();
        crc.update(e.canEncode((char) cp) ? 3 : 4);
      }
    }
    return crc.getValue();
  }

  static String hex(byte[] b) {
    StringBuilder sb = new StringBuilder();
    for (byte x : b) {
      sb.append(String.format("%02x", x));
    }
    return sb.toString();
  }

  public static void main(String[] args) throws Exception {
    String[] related = {"US-ASCII", "Big5", "GB2312", "EUC-JP", "Shift_JIS", "JIS_X0201", "windows-31j",
        "x-windows-950", "x-EUC-TW", "ISO-2022-JP"};
    for (String name : NAMES) {
      Charset cs = Charset.forName(name);
      StringBuilder contains = new StringBuilder();
      for (String r : related) {
        contains.append(cs.contains(Charset.forName(r)) ? '1' : '0');
      }
      CharsetDecoder d = cs.newDecoder();
      System.out.println(name + " props " + cs.name() + " " + new TreeSet<String>(cs.aliases()) + " "
          + cs.isRegistered() + " " + cs.canEncode() + " " + contains + " " + cs.contains(cs) + " "
          + d.averageCharsPerByte() + " " + d.maxCharsPerByte() + " "
          + (cs.canEncode() ? cs.newEncoder().averageBytesPerChar() + " " + cs.newEncoder().maxBytesPerChar() + " "
          + hex(cs.newEncoder().replacement()) : ""));
      StringBuilder dec = new StringBuilder();
      dec.append(decodeAll(d, new int[0]));
      for (int[] p : PREFIXES) {
        dec.append(' ').append(decodeAll(d, p));
      }
      System.out.println(name + " decode " + dec);
      Random rnd = new Random(name.hashCode());
      if (cs.canEncode()) {
        CharsetEncoder e = cs.newEncoder();
        System.out.println(name + " encode " + encodeAll(e));
        String text = "a\u3042\u4e2d\uac00\u00e9\uff71\u20ac\u309a\u304b\u309a\u02e9\u02e5\n\r\ud800\udc00\ud800";
        for (int round = 0; round < 60; round++) {
          StringBuilder sb = new StringBuilder();
          for (int i = 0; i < 12; i++) {
            int k = rnd.nextInt(6);
            sb.append(k < 3 ? (char) (0x4e00 + rnd.nextInt(0x5000)) : k < 5 ? text.charAt(rnd.nextInt(text.length()))
                : (char) rnd.nextInt(0x10000));
          }
          String s = sb.toString();
          e.onMalformedInput(CodingErrorAction.REPORT).onUnmappableCharacter(CodingErrorAction.REPORT);
          crc.reset();
          add(encodeTrace(e, s, 1 + rnd.nextInt(3), 1 + rnd.nextInt(4)));
          add(encodeTrace(e, s, 1000, 1000));
          e.onMalformedInput(CodingErrorAction.REPLACE).onUnmappableCharacter(CodingErrorAction.REPLACE);
          add(encodeTrace(e, s, 1 + rnd.nextInt(3), 1 + rnd.nextInt(4)));
          add(hex(s.getBytes(cs)));
          add(Boolean.toString(e.reset().canEncode(s)));
          if (round % 20 == 0) {
            System.out.println(name + " text " + round + " " + hex(s.getBytes(cs)));
          }
          System.out.println(name + " enc " + round + " " + crc.getValue());
        }
      }
      for (int round = 0; round < 60; round++) {
        byte[] in = new byte[4 + rnd.nextInt(16)];
        rnd.nextBytes(in);
        for (int i = 0; i < in.length; i++) {
          if (rnd.nextInt(5) == 0) {
            in[i] = (byte) (rnd.nextBoolean() ? 0x0e : 0x0f);
          }
        }
        d.onMalformedInput(CodingErrorAction.REPORT).onUnmappableCharacter(CodingErrorAction.REPORT);
        crc.reset();
        add(decodeTrace(d, in, 1 + rnd.nextInt(3), 1 + rnd.nextInt(3)));
        add(decodeTrace(d, in, 1000, 1000));
        add(new String(in, cs));
        if (round % 20 == 0) {
          System.out.println(name + " bytes " + round + " " + decodeTrace(d, in, 1000, 1000));
        }
        System.out.println(name + " dec " + round + " " + crc.getValue());
      }
    }
  }
}
