/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.zip.Adler32;
import java.util.zip.CRC32;
import java.util.zip.CRC32C;
import java.util.zip.CheckedInputStream;
import java.util.zip.CheckedOutputStream;
import java.util.zip.Checksum;

public class ChecksumsAndCheckedStreamsModern {
  public static void main(String[] args) throws Exception {
    testChecksumValues();
    testByteBufferUpdates();
    testCheckedStreamsRoundtrip();
    System.out.println("PASS: ChecksumsAndCheckedStreamsModern");
  }

  private static void testChecksumValues() {
    byte[] data = "123456789".getBytes(StandardCharsets.US_ASCII);

    CRC32 crc32 = new CRC32();
    assertEquals(0L, crc32.getValue());
    crc32.update(data);
    assertEquals(0xCBF43926L, crc32.getValue());
    crc32.reset();
    assertEquals(0L, crc32.getValue());

    CRC32C crc32c = new CRC32C();
    assertEquals(0L, crc32c.getValue());
    crc32c.update(data);
    assertEquals(0xE3069283L, crc32c.getValue());
    crc32c.reset();
    assertEquals(0L, crc32c.getValue());

    Adler32 adler32 = new Adler32();
    assertEquals(1L, adler32.getValue());
    adler32.update(data);
    assertEquals(0x091E01DEL, adler32.getValue());
    adler32.reset();
    assertEquals(1L, adler32.getValue());
  }

  private static void testByteBufferUpdates() {
    byte[] data = "Hello Checksum Modern ByteBuffers".getBytes(StandardCharsets.UTF_8);

    CRC32C crcFromBytes = new CRC32C();
    crcFromBytes.update(data);

    CRC32C crcFromHeapBuf = new CRC32C();
    ByteBuffer heapBuf = ByteBuffer.wrap(data);
    crcFromHeapBuf.update(heapBuf);
    assertEquals(0, heapBuf.remaining());
    assertEquals(crcFromBytes.getValue(), crcFromHeapBuf.getValue());

    CRC32C crcFromDirectBuf = new CRC32C();
    ByteBuffer directBuf = ByteBuffer.allocateDirect(data.length);
    directBuf.put(data);
    directBuf.flip();
    crcFromDirectBuf.update(directBuf);
    assertEquals(0, directBuf.remaining());
    assertEquals(crcFromBytes.getValue(), crcFromDirectBuf.getValue());

    Adler32 adlerFromBytes = new Adler32();
    adlerFromBytes.update(data);

    Adler32 adlerFromDirectBuf = new Adler32();
    directBuf.flip();
    adlerFromDirectBuf.update(directBuf);
    assertEquals(adlerFromBytes.getValue(), adlerFromDirectBuf.getValue());
  }

  private static void testCheckedStreamsRoundtrip() throws Exception {
    byte[] payload = "Testing Checked Streams with CRC32C and Adler32 checksums!".getBytes(StandardCharsets.UTF_8);

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    CRC32C outCrc = new CRC32C();
    try (CheckedOutputStream cos = new CheckedOutputStream(baos, outCrc)) {
      cos.write(payload[0]);
      cos.write(payload, 1, payload.length - 1);
    }

    assertEquals(outCrc.getValue(), outCrc.getValue());
    byte[] outBytes = baos.toByteArray();

    ByteArrayInputStream bais = new ByteArrayInputStream(outBytes);
    CRC32C inCrc = new CRC32C();
    try (CheckedInputStream cis = new CheckedInputStream(bais, inCrc)) {
      byte[] readBuf = new byte[16];
      int n;
      while ((n = cis.read(readBuf)) != -1) {
      }
      assertEquals(outCrc.getValue(), cis.getChecksum().getValue());
    }

    ByteArrayOutputStream baosAdler = new ByteArrayOutputStream();
    Adler32 outAdler = new Adler32();
    try (CheckedOutputStream cos = new CheckedOutputStream(baosAdler, outAdler)) {
      cos.write(payload);
    }

    ByteArrayInputStream baisAdler = new ByteArrayInputStream(baosAdler.toByteArray());
    Adler32 inAdler = new Adler32();
    try (CheckedInputStream cis = new CheckedInputStream(baisAdler, inAdler)) {
      cis.skip(payload.length);
      assertEquals(outAdler.getValue(), inAdler.getValue());
    }
  }

  private static void assertEquals(long expected, long actual) {
    if (expected != actual) {
      throw new AssertionError("Expected: 0x" + Long.toHexString(expected) + " (" + expected + "), but got: 0x" + Long.toHexString(actual) + " (" + actual + ")");
    }
  }

  private static void assertEquals(int expected, int actual) {
    if (expected != actual) {
      throw new AssertionError("Expected: " + expected + ", but got: " + actual);
    }
  }
}
