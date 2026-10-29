/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */
import java.security.MessageDigest;
import java.security.DigestException;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class SHA1Modern {
  static int checks;
  static long checksum;
  static void expect(boolean value) {if(!value)throw new AssertionError("SHA1 check "+checks);++checks;}
  static void same(byte[] expected, byte[] actual) {
    expect(Arrays.equals(expected,actual));
    for(byte b:actual)checksum=checksum*31+(b&255);
  }
  static String hex(byte[] value) {
    StringBuilder out=new StringBuilder();
    for(byte b:value)out.append("0123456789abcdef".charAt((b>>>4)&15)).append("0123456789abcdef".charAt(b&15));
    return out.toString();
  }
  static MessageDigest fresh() throws Exception {return MessageDigest.getInstance("SHA-1");}
  static void exercise(int size) throws Exception {
    byte[] data=new byte[size];for(int i=0;i<size;++i)data[i]=(byte)(i*71+i/13);
    byte[] expected=fresh().digest(data);
    // Include all one-shot results in the JDK differential digest, not just
    // comparisons within the same provider.
    same(expected,expected);
    MessageDigest md=fresh();
    for(int step:new int[]{1,7,63,64,65,4096}) {
      if(step==1 && size>1024)continue;
      for(int i=0;i<size;) {int n=Math.min(step,size-i);md.update(data,i,n);md.update(data,i,0);i+=n;}
      same(expected,md.digest());
    }
    byte[] framed=new byte[size+8];System.arraycopy(data,0,framed,3,size);
    md.update(framed,3,size);same(expected,md.digest());
    ByteBuffer heap=ByteBuffer.wrap(framed,3,size);
    ByteBuffer direct=ByteBuffer.allocateDirect(size+8);direct.put(framed);direct.flip();direct.position(3);direct.limit(size+3);
    for(ByteBuffer b:new ByteBuffer[]{heap,heap.slice(),heap.asReadOnlyBuffer(),direct}) {
      md.update(b);expect(!b.hasRemaining());same(expected,md.digest());
    }
    int split=size/2;md.update(data,0,split);
    MessageDigest clone=(MessageDigest)md.clone();
    md.update(data,split,size-split);same(expected,md.digest());
    clone.update(data,split,size-split);same(expected,clone.digest());
    md.update(data);md.reset();same(expected,md.digest(data));
    byte[] output=new byte[29];Arrays.fill(output,(byte)0x5a);md.update(data);expect(md.digest(output,4,21)==20);
    same(expected,Arrays.copyOfRange(output,4,24));expect(output[3]==0x5a && output[24]==0x5a);
    md.update(data);
    try{md.digest(new byte[19],0,19);throw new AssertionError();}catch(DigestException e){++checks;}
    same(expected,md.digest());
  }
  public static void main(String[] args) throws Exception {
    expect(hex(fresh().digest(new byte[0])).equals("da39a3ee5e6b4b0d3255bfef95601890afd80709"));
    expect(hex(fresh().digest("abc".getBytes("US-ASCII"))).equals("a9993e364706816aba3e25717850c26c9cd0d89d"));
    expect(hex(fresh().digest("abcdbcdecdefdefgefghfghighijhijkijkljklmklmnlmnomnopnopq".getBytes("US-ASCII"))).equals("84983e441c3bd26ebaae4aa1f95129e5e54670f1"));
    for(int size=0;size<=256;++size)exercise(size);
    for(int size:new int[]{511,512,1023,1024,4095,4096,65535,65536,65537,131073})exercise(size);
    byte[] million=new byte[1000000];Arrays.fill(million,(byte)'a');
    expect(hex(fresh().digest(million)).equals("34aa973cd4c4daa4f61eeb2bdbad27316534016f"));
    MessageDigest md=fresh();md.update("prefix".getBytes("US-ASCII"));
    byte[] expected=fresh().digest("prefix".getBytes("US-ASCII"));
    try{md.update(new byte[8],-1,1);throw new AssertionError();}catch(IndexOutOfBoundsException e){++checks;}
    try{md.update(new byte[8],0,-1);throw new AssertionError();}catch(IndexOutOfBoundsException e){++checks;}
    same(expected,md.digest());
    System.out.println("SHA1 checks="+checks+" checksum="+checksum);
  }
}
