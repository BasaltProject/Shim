/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
public class AsciiDecodingBench {
  public static void main(String[] args) {
    byte[] bytes=new byte[8194];Arrays.fill(bytes,(byte)'a');bytes[0]=(byte)0xff;bytes[8193]=(byte)0xff;
    long start=System.nanoTime(),sum=0;
    for(int i=0;i<1000;++i){String s=new String(bytes,1,8192,StandardCharsets.UTF_8);sum+=s.length()+s.charAt(0);}
    System.out.println("ascii_decode_ms="+(System.nanoTime()-start)/1000000+" checksum="+sum);
  }
}
