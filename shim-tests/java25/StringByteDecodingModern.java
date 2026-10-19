/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class StringByteDecodingModern {
  static int checks;
  static long checksum;
  static void expect(boolean value) {if(!value)throw new AssertionError("byte decoding check "+checks);++checks;}
  static void ranges(byte[] bytes, Charset charset) throws Exception {
    for(int from=0;from<=bytes.length;++from)for(int to=from;to<=bytes.length;++to) {
      String expected=charset.decode(ByteBuffer.wrap(bytes,from,to-from)).toString();
      String actual=new String(bytes,from,to-from,charset);
      expect(actual.equals(expected));
      expect(actual.equals(new String(bytes,from,to-from,charset.name())));
      checksum=checksum*31+actual.hashCode();
      byte[] copy=bytes.clone();String isolated=new String(copy,from,to-from,charset);Arrays.fill(copy,(byte)'!');expect(isolated.equals(expected));
    }
  }
  public static void main(String[] args) throws Exception {
    for(Charset cs:new Charset[]{StandardCharsets.UTF_8,StandardCharsets.US_ASCII,StandardCharsets.ISO_8859_1,StandardCharsets.UTF_16BE}) {
      for(int b=0;b<256;++b)ranges(new byte[]{(byte)b},cs);
      for(byte[] bytes:new byte[][] {new byte[0],{0,1,31,32,65,127}, {(byte)0xc3,(byte)0xa9}, {(byte)0xe2,(byte)0x82,(byte)0xac}, {(byte)0xf0,(byte)0x9f,(byte)0x98,(byte)0x80}, {(byte)0xc0,(byte)0x80}, {(byte)0xed,(byte)0xa0,(byte)0x80}, {(byte)0xff,(byte)0x80,(byte)0xf5,0x41}}) ranges(bytes,cs);
    }
    byte[] large=new byte[8195];Arrays.fill(large,(byte)'a');large[0]=(byte)0xff;large[large.length-1]=(byte)0xff;
    for(int i=0;i<3000;++i) {
      byte[] bytes=large.clone();String text=new String(bytes,1,8193,StandardCharsets.UTF_8);bytes[1]='!';expect(text.length()==8193 && text.charAt(0)=='a' && text.charAt(8192)=='a');
      if(i%256==0)System.gc();
    }
    try{new String(new byte[2],-1,1,StandardCharsets.UTF_8);throw new AssertionError();}catch(IndexOutOfBoundsException e){++checks;}
    try{new String(new byte[2],1,2,StandardCharsets.UTF_8);throw new AssertionError();}catch(IndexOutOfBoundsException e){++checks;}
    try{new String((byte[])null,0,0,StandardCharsets.UTF_8);throw new AssertionError();}catch(NullPointerException e){++checks;}
    try{new String(new byte[0],0,0,(Charset)null);throw new AssertionError();}catch(NullPointerException e){++checks;}
    System.out.println("byte decoding checks="+checks+" checksum="+checksum);
  }
}
