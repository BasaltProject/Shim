/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

import java.util.zip.CRC32;
import java.nio.ByteBuffer;

public class CRC32Modern {
  static int checks;
  static long reference(byte[] data, int offset, int length) {
    int crc = -1;
    for (int i = offset; i < offset + length; ++i) {
      crc ^= data[i] & 255;
      for (int j = 0; j < 8; ++j) crc = (crc >>> 1) ^ ((crc & 1) == 0 ? 0 : 0xedb88320);
    }
    return (crc ^ 0xffffffffL) & 0xffffffffL;
  }
  static void expect(boolean value) { if (!value) throw new AssertionError("CRC check " + checks); ++checks; }
  public static void main(String[] args) throws Exception {
    byte[] bytes = new byte[10003];
    for (int i=0;i<bytes.length;++i) bytes[i]=(byte)(i * 71 + i/256);
    CRC32 crc = new CRC32();
    expect(crc.getValue() == 0);
    crc.update("123456789".getBytes("UTF-8")); expect(crc.getValue()==0xcbf43926L);
    for (int offset : new int[] {0, 1, 13, 256, bytes.length}) {
      for (int size : new int[] {0, 1, 7, 256, bytes.length-offset}) {
        if (size > bytes.length-offset) continue;
        crc.reset(); crc.update(bytes,offset,size); expect(crc.getValue()==reference(bytes,offset,size));
        crc.reset(); for (int i=offset;i<offset+size;++i) crc.update(bytes[i] | 0x10000);
        expect(crc.getValue()==reference(bytes,offset,size));
      }
    }
    crc.reset();
    for (int i=0;i<bytes.length;) { int n=Math.min(37,bytes.length-i);crc.update(bytes,i,n);i+=n; }
    expect(crc.getValue()==reference(bytes,0,bytes.length));
    crc.reset(); for (int i=0;i<bytes.length;++i) { if ((i&1)==0) crc.update(bytes,i,1);else crc.update(bytes[i]); }
    expect(crc.getValue()==reference(bytes,0,bytes.length));
    long saved=crc.getValue();
    for (int[] range : new int[][] {{-1,0},{0,-1},{bytes.length,1},{Integer.MAX_VALUE,1},{1,Integer.MAX_VALUE}}) {
      try {crc.update(bytes,range[0],range[1]);throw new AssertionError();} catch(IndexOutOfBoundsException expected) {expect(crc.getValue()==saved);}
    }
    try {crc.update((byte[])null,0,0);throw new AssertionError();} catch(NullPointerException expected) {expect(crc.getValue()==saved);}
    ByteBuffer heap=ByteBuffer.wrap(bytes); heap.position(3);heap.limit(bytes.length-2);
    ByteBuffer direct=ByteBuffer.allocateDirect(bytes.length);direct.put(bytes);direct.flip();direct.position(3);direct.limit(bytes.length-2);
    for (ByteBuffer buffer : new ByteBuffer[] {heap,heap.slice(),heap.asReadOnlyBuffer(),direct}) {
      crc.reset();crc.update(buffer);expect(crc.getValue()==reference(bytes,3,bytes.length-5));expect(buffer.position()==buffer.limit());
    }
    java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
    try (java.util.zip.GZIPOutputStream zip = new java.util.zip.GZIPOutputStream(out)) { zip.write(bytes); }
    byte[] compressed = out.toByteArray();
    try (java.util.zip.GZIPInputStream zip = new java.util.zip.GZIPInputStream(new java.io.ByteArrayInputStream(compressed))) {
      expect(java.util.Arrays.equals(bytes, zip.readAllBytes()));
    }
    compressed[compressed.length - 8] ^= 1;
    try (java.util.zip.GZIPInputStream zip = new java.util.zip.GZIPInputStream(new java.io.ByteArrayInputStream(compressed))) {
      zip.readAllBytes(); throw new AssertionError("bad CRC accepted");
    } catch (java.util.zip.ZipException expected) { ++checks; }
    System.out.println("CRC checks="+checks);
  }
}
