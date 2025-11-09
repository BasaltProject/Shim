/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

public class EmojiAndModernIo {

  private static void assertEquals(Object expected, Object actual, String message) {
    if (expected == null && actual == null) {
      return;
    }
    if (expected != null && expected.equals(actual)) {
      return;
    }
    throw new AssertionError(message + " expected: " + expected + ", actual: " + actual);
  }

  private static void assertTrue(boolean condition, String message) {
    if (!condition) {
      throw new AssertionError(message);
    }
  }

  private static void testEmojiProperties() {
    assertTrue(Character.isEmoji(0x1F600), "0x1F600 is emoji");
    assertTrue(Character.isEmojiPresentation(0x1F600), "0x1F600 is emoji presentation");
    assertTrue(!Character.isEmojiModifier(0x1F600), "0x1F600 is not modifier");
    assertTrue(!Character.isEmojiModifierBase(0x1F600), "0x1F600 is not modifier base");
    assertTrue(!Character.isEmojiComponent(0x1F600), "0x1F600 is not component");
    assertTrue(Character.isExtendedPictographic(0x1F600), "0x1F600 is extended pictographic");

    assertTrue(Character.isEmoji(0x1F44D), "0x1F44D is emoji");
    assertTrue(Character.isEmojiModifierBase(0x1F44D), "0x1F44D is modifier base");

    assertTrue(Character.isEmoji(0x2764), "0x2764 is emoji");
    assertTrue(!Character.isEmojiPresentation(0x2764), "0x2764 is not emoji presentation");
    assertTrue(Character.isExtendedPictographic(0x2764), "0x2764 is extended pictographic");

    assertTrue(Character.isEmoji(0x1F3FB), "0x1F3FB is emoji");
    assertTrue(Character.isEmojiModifier(0x1F3FB), "0x1F3FB is modifier");
    assertTrue(Character.isEmojiComponent(0x1F3FB), "0x1F3FB is component");

    assertTrue(!Character.isEmoji(0x200D), "0x200D is not emoji");
    assertTrue(Character.isEmojiComponent(0x200D), "0x200D is component");

    assertTrue(!Character.isEmoji(0xFE0F), "0xFE0F is not emoji");
    assertTrue(Character.isEmojiComponent(0xFE0F), "0xFE0F is component");

    assertTrue(Character.isEmoji(0x23), "0x23 is emoji");
    assertTrue(Character.isEmojiComponent(0x23), "0x23 is component");

    assertTrue(!Character.isEmoji(0x41), "0x41 is not emoji");
    assertTrue(!Character.isEmojiComponent(0x41), "0x41 is not component");
  }

  private static void testInputStreamSkipNBytes() throws Exception {
    byte[] data = new byte[] { 10, 20, 30, 40, 50, 60, 70, 80 };
    ByteArrayInputStream in = new ByteArrayInputStream(data);

    in.skipNBytes(3);
    assertEquals(40, in.read(), "next byte after skipNBytes 3");

    in.skipNBytes(0);
    assertEquals(50, in.read(), "next byte after skipNBytes 0");

    in.skipNBytes(2);
    assertEquals(80, in.read(), "last byte after skipNBytes 2");

    try {
      in.skipNBytes(1);
      throw new AssertionError("skipNBytes past EOF should throw EOFException");
    } catch (EOFException expected) {
    }

    InputStream nullIn = InputStream.nullInputStream();
    nullIn.skipNBytes(0);
    try {
      nullIn.skipNBytes(5);
      throw new AssertionError("nullInputStream skipNBytes should throw EOFException");
    } catch (EOFException expected) {
    }
  }

  private static void testByteArrayOutputStream() {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    baos.writeBytes(new byte[] { 'H', 'e', 'l', 'l', 'o' });
    baos.write(' ');
    baos.writeBytes(new byte[] { 'W', 'o', 'r', 'l', 'd' });

    assertEquals("Hello World", baos.toString(StandardCharsets.UTF_8), "toString with UTF-8 charset");
    assertEquals(11, baos.size(), "size check");
  }

  private static void testPrintStreamAndPrintWriterConstructors() throws Exception {
    File tmpPs = File.createTempFile("shim_ps_test", ".txt");
    tmpPs.deleteOnExit();

    try (PrintStream ps = new PrintStream(tmpPs, StandardCharsets.UTF_8)) {
      ps.print("PrintStream test message");
    }

    try (FileInputStream fis = new FileInputStream(tmpPs)) {
      byte[] bytes = fis.readAllBytes();
      assertEquals("PrintStream test message", new String(bytes, StandardCharsets.UTF_8), "PrintStream file output");
    }

    File tmpPw = File.createTempFile("shim_pw_test", ".txt");
    tmpPw.deleteOnExit();

    try (PrintWriter pw = new PrintWriter(tmpPw, StandardCharsets.UTF_8)) {
      pw.print("PrintWriter test message");
    }

    try (FileInputStream fis = new FileInputStream(tmpPw)) {
      byte[] bytes = fis.readAllBytes();
      assertEquals("PrintWriter test message", new String(bytes, StandardCharsets.UTF_8), "PrintWriter file output");
    }
  }

  public static void main(String[] args) throws Exception {
    testEmojiProperties();
    testInputStreamSkipNBytes();
    testByteArrayOutputStream();
    testPrintStreamAndPrintWriterConstructors();
    System.out.println("OK");
  }
}
