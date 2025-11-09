/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */


import java.io.ByteArrayOutputStream;
import java.io.IOError;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Reader;
import java.io.StringReader;
import java.lang.IO;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class ModernIOAndUnicodeChar {
  public static void main(String[] args) throws Exception {
    testIOAndIOError();
    testReaderOfAndReadAll();
    testCharsetFallback();
    testCharacterUnicodeProperties();
    System.out.println("ModernIOAndUnicodeChar PASS");
  }

  private static void testIOAndIOError() {
    PrintStream originalOut = System.out;
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    PrintStream testOut = new PrintStream(baos);
    System.setOut(testOut);
    try {
      IO.println("hello via IO.println");
      IO.println();
      IO.print("partial text");
    } finally {
      System.setOut(originalOut);
    }
    String output = baos.toString();
    if (!output.contains("hello via IO.println\n\npartial text")) {
      throw new AssertionError("Unexpected IO output: " + output);
    }

    try {
      throw new IOError(new IOException("simulated disk fault"));
    } catch (IOError err) {
      if (!(err.getCause() instanceof IOException)) {
        throw new AssertionError("Expected IOException cause in IOError");
      }
    }
  }

  private static void testReaderOfAndReadAll() throws Exception {
    String text = "Line 1\nLine 2\r\nLine 3";
    Reader r = Reader.of(text);

    int firstChar = r.read();
    if (firstChar != 'L') {
      throw new AssertionError("Expected 'L', got " + (char) firstChar);
    }

    char[] buf = new char[5];
    int readCount = r.read(buf, 0, 5);
    if (readCount != 5 || !new String(buf).equals("ine 1")) {
      throw new AssertionError("Expected 'ine 1', got " + new String(buf));
    }

    long skipped = r.skip(1);
    if (skipped != 1) {
      throw new AssertionError("Expected 1 char skipped");
    }

    String rest = r.readAllAsString();
    if (!rest.equals("Line 2\r\nLine 3")) {
      throw new AssertionError("Unexpected readAllAsString: " + rest);
    }
    if (r.read() != -1) {
      throw new AssertionError("Expected EOF");
    }

    Reader rLines = Reader.of("Alpha\nBeta\nGamma");
    List<String> lines = rLines.readAllLines();
    if (lines.size() != 3 || !lines.get(0).equals("Alpha") || !lines.get(2).equals("Gamma")) {
      throw new AssertionError("Unexpected readAllLines: " + lines);
    }

    Reader customReader = new StringReader("Custom 1\nCustom 2");
    List<String> customLines = customReader.readAllLines();
    if (customLines.size() != 2 || !customLines.get(1).equals("Custom 2")) {
      throw new AssertionError("Unexpected custom reader readAllLines: " + customLines);
    }
  }

  private static void testCharsetFallback() {
    Charset utf8 = Charset.forName("UTF-8", StandardCharsets.ISO_8859_1);
    if (utf8 != StandardCharsets.UTF_8) {
      throw new AssertionError("Expected UTF-8 charset");
    }

    Charset fallback = Charset.forName("UNKNOWN-CHARSET-12345", StandardCharsets.US_ASCII);
    if (fallback != StandardCharsets.US_ASCII) {
      throw new AssertionError("Expected fallback US-ASCII");
    }

    try {
      Charset.forName(null, StandardCharsets.UTF_8);
      throw new AssertionError("Expected IllegalArgumentException for null name");
    } catch (IllegalArgumentException expected) {
    }
  }

  private static void testCharacterUnicodeProperties() {
    if (Character.getType('A') != Character.UPPERCASE_LETTER) {
      throw new AssertionError("Expected UPPERCASE_LETTER for 'A'");
    }
    if (Character.getType('z') != Character.LOWERCASE_LETTER) {
      throw new AssertionError("Expected LOWERCASE_LETTER for 'z'");
    }
    if (Character.getType('0') != Character.DECIMAL_DIGIT_NUMBER) {
      throw new AssertionError("Expected DECIMAL_DIGIT_NUMBER for '0'");
    }
    if (Character.getType('$') != Character.CURRENCY_SYMBOL) {
      throw new AssertionError("Expected CURRENCY_SYMBOL for '$'");
    }
    if (Character.getType('_') != Character.CONNECTOR_PUNCTUATION) {
      throw new AssertionError("Expected CONNECTOR_PUNCTUATION for '_'");
    }
    if (Character.getType(0x0008) != Character.CONTROL) {
      throw new AssertionError("Expected CONTROL for 0x0008");
    }
    if (Character.getType(0x200C) != Character.FORMAT) {
      throw new AssertionError("Expected FORMAT for 0x200C");
    }
    if (Character.getType(0x2160) != Character.LETTER_NUMBER) {
      throw new AssertionError("Expected LETTER_NUMBER for Roman numeral I");
    }

    if (!Character.isIdentifierIgnorable(0x0008)) {
      throw new AssertionError("0x0008 must be ignorable");
    }
    if (!Character.isIdentifierIgnorable(0x200C)) {
      throw new AssertionError("0x200C must be ignorable");
    }
    if (Character.isIdentifierIgnorable('A')) {
      throw new AssertionError("'A' must not be ignorable");
    }

    if (!Character.isJavaIdentifierStart('A') || !Character.isJavaIdentifierStart('$') || !Character.isJavaIdentifierStart('_')) {
      throw new AssertionError("A, $, _ must be java identifier start");
    }
    if (Character.isJavaIdentifierStart('0')) {
      throw new AssertionError("0 must not be java identifier start");
    }

    if (!Character.isUnicodeIdentifierStart('A') || !Character.isUnicodeIdentifierStart(0x2160)) {
      throw new AssertionError("A and Roman I must be unicode identifier start");
    }
    if (Character.isUnicodeIdentifierStart('$') || Character.isUnicodeIdentifierStart('_') || Character.isUnicodeIdentifierStart('0')) {
      throw new AssertionError("$, _, 0 must not be unicode identifier start");
    }

    if (!Character.isUnicodeIdentifierPart('A') || !Character.isUnicodeIdentifierPart('_') || !Character.isUnicodeIdentifierPart('0')) {
      throw new AssertionError("A, _, 0 must be unicode identifier part");
    }
    if (Character.isUnicodeIdentifierPart('$')) {
      throw new AssertionError("$ must not be unicode identifier part");
    }

    if (!Character.isISOControl(0x001F) || !Character.isISOControl(0x007F) || Character.isISOControl(0x0020)) {
      throw new AssertionError("isISOControl mismatch");
    }
  }
}
