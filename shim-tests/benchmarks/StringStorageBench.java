/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */
import java.util.Arrays;

/** Standalone timing probe, not a literal-stdout compatibility test. */
public class StringStorageBench {
  static void result(String name, long start, long checksum) {
    System.out.println(name+" ms="+(System.nanoTime()-start)/1000000+" checksum="+checksum);
  }
  public static void main(String[] args) {
    char[] chars=new char[4096];Arrays.fill(chars,'a');chars[4095]='z';
    long start=System.nanoTime(), checksum=0;
    for(int i=0;i<1000;++i) {String s=new String(chars,1,4095);checksum+=s.length()+s.charAt(4094);}
    result("construct",start,checksum);
    String text=new String(chars);char[] out=new char[4100];
    start=System.nanoTime();checksum=0;
    for(int i=0;i<1000;++i) {text.getChars(1,4096,out,2);checksum+=out[4096];}
    result("getChars",start,checksum);
    start=System.nanoTime();checksum=0;
    for(int i=0;i<1000;++i) checksum+=text.indexOf("az");
    result("search",start,checksum);
    start=System.nanoTime();checksum=0;
    for(int i=0;i<500;++i) checksum+=text.replace("not-present","x").length();
    result("replace-miss",start,checksum);
  }
}
