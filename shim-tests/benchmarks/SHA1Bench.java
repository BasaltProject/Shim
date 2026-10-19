/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */
import java.security.MessageDigest;
public class SHA1Bench {
  public static void main(String[] args) throws Exception {
    for(int size:new int[]{128,4096,1048576}) {
      byte[] input=new byte[size];for(int i=0;i<size;++i)input[i]=(byte)i;
      MessageDigest md=MessageDigest.getInstance("SHA-1");long start=System.nanoTime(),sum=0;
      int count=size==128?2000:size==4096?100:2;
      for(int i=0;i<count;++i) {byte[] out=md.digest(input);for(byte b:out)sum=sum*31+(b&255);}
      System.out.println("size="+size+" count="+count+" ms="+(System.nanoTime()-start)/1000000+" checksum="+sum);
    }
  }
}
