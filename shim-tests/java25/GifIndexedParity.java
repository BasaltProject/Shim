/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

import javax.imageio.*; import java.awt.image.*; import java.io.*; import java.util.*;
public class GifIndexedParity { public static void main(String[] a) throws Exception { for(int bits: new int[]{1,2,4,8}) for(int type: new int[]{BufferedImage.TYPE_BYTE_BINARY, BufferedImage.TYPE_BYTE_INDEXED}){ if(type==BufferedImage.TYPE_BYTE_BINARY && bits==8) continue; int n=Math.min(1<<bits,3); byte[] r=new byte[n],g=new byte[n],b=new byte[n]; for(int i=0;i<n;i++){r[i]=(byte)(i*40+9);g[i]=(byte)(i*50+7);b[i]=(byte)(i*60+5);} IndexColorModel icm=new IndexColorModel(bits,n,r,g,b); BufferedImage im=new BufferedImage(4,4,type,icm); for(int y=0;y<4;y++)for(int x=0;x<4;x++) im.getRaster().setSample(x,y,0,(x+y)%n); ByteArrayOutputStream bo=new ByteArrayOutputStream(); try{ ImageIO.write(im,"gif",bo); byte[] q=bo.toByteArray(); System.out.println(bits+" t"+type+" flags="+Integer.toHexString(q[10]&255)+" len="+q.length+" lzw="+(q[q.length-1-(q.length-1-(q.length-14)) ]));}catch(Throwable t){System.out.println(bits+" "+t);} } } }
