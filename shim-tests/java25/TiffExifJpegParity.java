/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

import javax.imageio.*; import javax.imageio.stream.*; import java.awt.image.*; import java.io.*; import java.nio.*; import java.util.*;
public class TiffExifJpegParity { static String crc(byte[] b){ java.util.zip.CRC32 c=new java.util.zip.CRC32(); c.update(b); return Long.toHexString(c.getValue())+" len="+b.length; }
 static void dump(byte[] b){ ByteBuffer bb=ByteBuffer.wrap(b).order(b[0]=='I'?ByteOrder.LITTLE_ENDIAN:ByteOrder.BIG_ENDIAN); int ifd=bb.getInt(4); int n=bb.getShort(ifd)&0xffff; StringBuilder sb=new StringBuilder(); for(int i=0;i<n;i++){ int o=ifd+2+i*12; sb.append(String.format("tag%d t%d c%d v%d; ",bb.getShort(o)&0xffff,bb.getShort(o+2),bb.getInt(o+4),bb.getShort(o+2)==3?(bb.getShort(o+8)&0xffff):bb.getInt(o+8))); } System.out.println(sb+" next="+bb.getInt(ifd+2+n*12)); }
 public static void main(String[] a) throws Exception { for(int type: new int[]{BufferedImage.TYPE_INT_RGB, BufferedImage.TYPE_3BYTE_BGR, BufferedImage.TYPE_BYTE_GRAY}) { BufferedImage im=new BufferedImage(40,24,type); for(int y=0;y<24;y++)for(int x=0;x<40;x++) im.setRGB(x,y,(x*6)<<16|(y*9&255)<<8|90); ImageWriter w=ImageIO.getImageWritersByFormatName("tiff").next(); javax.imageio.ImageWriteParam p=w.getDefaultWriteParam(); p.setCompressionMode(ImageWriteParam.MODE_EXPLICIT); p.setCompressionType("Exif JPEG"); ByteArrayOutputStream bo=new ByteArrayOutputStream(); ImageOutputStream os=ImageIO.createImageOutputStream(bo); w.setOutput(os); try{ w.write(null,new IIOImage(im,null,null),p); os.close(); byte[] b=bo.toByteArray(); System.out.println("type "+type+" "+crc(b)); dump(b);}catch(Throwable t){ System.out.println("type "+type+" EXC "+t); }
   // read back
   } } }
