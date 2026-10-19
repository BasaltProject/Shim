/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

import java.util.zip.CRC32;

/** Standalone benchmark: compare timings, not literal stdout across VMs. */
public class TextZipStartupBench {
  public static void main(String[] args) {
    StringBuilder builder = new StringBuilder();
    for (int i=0;i<2000;++i) builder.append("key: some value\n");
    String text=builder.toString();
    long start=System.nanoTime(); long count=0;
    for (int i=0;i<40;++i) count+=text.split("\n").length;
    System.out.println("split_ms="+(System.nanoTime()-start)/1000000+" parts="+count);
    byte[] bytes=new byte[65536];for(int i=0;i<bytes.length;++i)bytes[i]=(byte)i;
    start=System.nanoTime();CRC32 crc=new CRC32();
    for(int i=0;i<32;++i)crc.update(bytes);
    System.out.println("crc_ms="+(System.nanoTime()-start)/1000000+" value="+crc.getValue());
  }
}
