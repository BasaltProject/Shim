/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

import javax.imageio.*; import javax.imageio.stream.*; import java.awt.image.*; import java.io.*; import java.util.*;
public class GifOctreeParity { static String crc(byte[] b){ java.util.zip.CRC32 c=new java.util.zip.CRC32(); c.update(b); return Long.toHexString(c.getValue())+" len="+b.length; }
 public static void main(String[] a) throws Exception { Random rnd=new Random(3); int[][] sizes={{4,4},{20,10},{64,48},{100,80}}; for(int[] sz: sizes) for(int mode=0; mode<4; mode++){ int type = mode==3? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB; BufferedImage im=new BufferedImage(sz[0],sz[1],type); for(int y=0;y<sz[1];y++)for(int x=0;x<sz[0];x++){ int rgb; if(mode==0) rgb=((x*3)&255)<<16|((y*5)&255)<<8|((x+y)*2&255); else if(mode==1) rgb=(rnd.nextInt(4)*85)<<16|(rnd.nextInt(4)*85)<<8|(rnd.nextInt(4)*85); else if(mode==2) rgb=rnd.nextInt(1<<24); else rgb=((x&1)==0?0xff000000:0x00000000)|((x*7)&255)<<16|((y*9)&255)<<8|90; im.setRGB(x,y,rgb); }
   ByteArrayOutputStream bo=new ByteArrayOutputStream(); try{ boolean ok=ImageIO.write(im,"gif",bo); System.out.println(sz[0]+"x"+sz[1]+" mode"+mode+" "+ok+" "+crc(bo.toByteArray())); }catch(Throwable t){ System.out.println(sz[0]+"x"+sz[1]+" mode"+mode+" EXC "+t); } } } }
