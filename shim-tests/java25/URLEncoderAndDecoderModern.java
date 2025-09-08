// Minimum release: 21
/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class URLEncoderAndDecoderModern {
  public static void main(String[] args) throws Exception {
    testBasicEncodingAndDecoding();
    testSpecialCharactersAndSurrogates();
    testStringCharsetOverloads();
    testDecoderErrorHandling();
    System.out.println("ALL TESTS PASSED");
  }

  private static void testBasicEncodingAndDecoding() {
    String original = "Hello World! 123 + foo/bar?a=b&c=d*";
    String encoded = URLEncoder.encode(original, StandardCharsets.UTF_8);

    if (!encoded.equals("Hello+World%21+123+%2B+foo%2Fbar%3Fa%3Db%26c%3Dd*")) {
      throw new AssertionError("unexpected encoded output: " + encoded);
    }

    String decoded = URLDecoder.decode(encoded, StandardCharsets.UTF_8);
    if (!decoded.equals(original)) {
      throw new AssertionError("roundtrip mismatch: " + decoded + " vs " + original);
    }
  }

  private static void testSpecialCharactersAndSurrogates() {
    String unicode = "Java 25 LTS: \u00e9\u00e0\u00fc \uD83D\uDE00";
    String encoded = URLEncoder.encode(unicode, StandardCharsets.UTF_8);
    String decoded = URLDecoder.decode(encoded, StandardCharsets.UTF_8);

    if (!decoded.equals(unicode)) {
      throw new AssertionError("unicode roundtrip mismatch: " + decoded);
    }

    if (!encoded.contains("%C3%A9") || !encoded.contains("%F0%9F%98%80")) {
      throw new AssertionError("expected UTF-8 byte escapes in: " + encoded);
    }
  }

  private static void testStringCharsetOverloads() throws Exception {
    String text = "test string with spaces & symbols";
    String enc1 = URLEncoder.encode(text, "UTF-8");
    String dec1 = URLDecoder.decode(enc1, "UTF-8");
    if (!dec1.equals(text)) {
      throw new AssertionError("string charset roundtrip mismatch");
    }

    try {
      URLEncoder.encode(text, "NON-EXISTENT-CHARSET");
      throw new AssertionError("expected UnsupportedEncodingException");
    } catch (java.io.UnsupportedEncodingException expected) {
    }

    try {
      URLDecoder.decode(text, "NON-EXISTENT-CHARSET");
      throw new AssertionError("expected UnsupportedEncodingException");
    } catch (java.io.UnsupportedEncodingException expected) {
    }
  }

  private static void testDecoderErrorHandling() {
    try {
      URLDecoder.decode("invalid%2", StandardCharsets.UTF_8);
      throw new AssertionError("expected IllegalArgumentException on trailing %");
    } catch (IllegalArgumentException expected) {
    }

    try {
      URLDecoder.decode("invalid%2G", StandardCharsets.UTF_8);
      throw new AssertionError("expected IllegalArgumentException on invalid hex");
    } catch (IllegalArgumentException expected) {
    }
  }
}
