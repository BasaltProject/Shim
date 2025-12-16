/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 * Minimum release: 21
 */

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.MalformedInputException;
import java.nio.charset.UnmappableCharacterException;
import java.util.Arrays;

public class CjkCharsetsModern {
  static final String[] NAMES = {"GBK", "GB2312", "GB18030", "Big5", "Big5-HKSCS", "EUC-KR", "x-windows-949",
      "Shift_JIS", "windows-31j", "EUC-JP", "JIS_X0212-1990", "x-JIS0208", "x-Johab", "x-windows-950", "x-mswin-936"};

  static String report(Charset cs, byte[] bytes) {
    CharsetDecoder decoder = cs.newDecoder().onMalformedInput(CodingErrorAction.REPORT)
        .onUnmappableCharacter(CodingErrorAction.REPORT);
    try {
      return "ok" + decoder.decode(ByteBuffer.wrap(bytes)).length();
    } catch (MalformedInputException e) {
      return "malformed" + e.getInputLength();
    } catch (UnmappableCharacterException e) {
      return "unmappable" + e.getInputLength();
    } catch (CharacterCodingException e) {
      return "error";
    }
  }

  public static void main(String[] args) throws Exception {
    for (String name : NAMES) {
      Charset cs = Charset.forName(name);
      long decode = 7;
      for (int b1 = 0; b1 < 256; b1++) {
        for (int b2 = 0; b2 < 256; b2++) {
          decode = decode * 31 + new String(new byte[] {(byte) b1, (byte) b2, 0x41}, cs).hashCode();
        }
      }
      for (int b1 : new int[] {0x8e, 0x8f, 0xa1, 0xb0, 0xe0, 0xfe, 0x81, 0x84, 0x90, 0x80, 0xff}) {
        for (int b2 = 0; b2 < 256; b2++) {
          for (int b3 : new int[] {0x30, 0x41, 0x81, 0xa1, 0xb0, 0xfe, 0x7f}) {
            decode = decode * 31 + new String(new byte[] {(byte) b1, (byte) b2, (byte) b3, 0x41, 0x42}, cs).hashCode();
          }
        }
      }
      long encode = 11;
      for (int c = 0; c < 65536; c++) {
        if (c >= 0xD800 && c < 0xE000) {
          continue;
        }
        encode = encode * 31 + Arrays.hashCode(String.valueOf((char) c).getBytes(cs));
      }
      for (int cp : new int[] {0x10000, 0x1F600, 0x20000, 0x2A6D6, 0x2F800, 0x10FFFF, 0x1D11E, 0x29E3D}) {
        encode = encode * 31 + Arrays.hashCode(new String(Character.toChars(cp)).getBytes(cs));
      }
      StringBuilder mixed = new StringBuilder();
      for (int c = 0x4E00; c < 0x9FFF; c += 3) {
        mixed.append((char) c);
      }
      mixed.append("😀abcé€\uD800x");
      byte[] encoded = mixed.toString().getBytes(cs);
      System.out.println(name + " " + cs.name() + " " + cs.aliases().size() + " " + cs.canEncode() + " decode=" + decode
          + " encode=" + encode + " mixed=" + Arrays.hashCode(encoded) + " " + new String(encoded, cs).hashCode()
          + " " + report(cs, new byte[] {(byte) 0x81, 0x30, (byte) 0x81}) + " " + report(cs, new byte[] {(byte) 0xa1})
          + " " + cs.newEncoder().maxBytesPerChar() + " " + cs.newEncoder().canEncode('中'));
    }
    System.out.println(Charset.availableCharsets().containsKey("GB18030") + " " + Charset.isSupported("CP936") + " "
        + Charset.forName("sjis").name() + " " + Charset.forName("euc_kr").name());
  }
}
