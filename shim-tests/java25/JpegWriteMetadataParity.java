/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

import javax.imageio.*; import javax.imageio.metadata.*; import javax.imageio.plugins.jpeg.*; import javax.imageio.stream.*; import java.awt.image.*; import java.io.*; import java.util.*; import org.w3c.dom.*;
public class JpegWriteMetadataParity { static String markers(byte[] b){ StringBuilder sb=new StringBuilder(); int i=2; while(i<b.length-1){ if((b[i]&255)!=0xff){i++;continue;} int m=b[i+1]&255; if(m==0xd9){sb.append("EOI");break;} if(m==0||m==0xff||(m>=0xd0&&m<=0xd7)){i++;continue;} int len=((b[i+2]&255)<<8)|(b[i+3]&255); sb.append(String.format("%02x:%d ",m,len)); if(m==0xda){ // skip entropy
   i+=2+len; while(i<b.length-1 && !((b[i]&255)==0xff && (b[i+1]&255)!=0 && ((b[i+1]&255)<0xd0||(b[i+1]&255)>0xd7))) i++; continue;} i+=2+len; } return sb.toString(); }
 static byte[] write(BufferedImage im, IIOMetadata md, ImageWriteParam p) throws Exception { ImageWriter w=ImageIO.getImageWritersByFormatName("jpeg").next(); ByteArrayOutputStream bo=new ByteArrayOutputStream(); ImageOutputStream os=ImageIO.createImageOutputStream(bo); w.setOutput(os); w.write(null,new IIOImage(im,null,md),p==null?w.getDefaultWriteParam():p); os.close(); return bo.toByteArray(); }
 static String crc(byte[] b){ java.util.zip.CRC32 c=new java.util.zip.CRC32(); c.update(b); return Long.toHexString(c.getValue())+" len="+b.length; }
 public static void main(String[] a) throws Exception { BufferedImage im=new BufferedImage(40,24,BufferedImage.TYPE_INT_RGB); for(int y=0;y<24;y++)for(int x=0;x<40;x++) im.setRGB(x,y,(x*6)<<16|(y*9&255)<<8|90); ImageWriter w=ImageIO.getImageWritersByFormatName("jpeg").next(); ImageWriteParam p=w.getDefaultWriteParam(); ImageTypeSpecifier its=ImageTypeSpecifier.createFromRenderedImage(im);
  byte[] b0=write(im,null,null); System.out.println("none "+crc(b0)+" "+markers(b0));
  IIOMetadata md=w.getDefaultImageMetadata(its,p); byte[] b1=write(im,md,null); System.out.println("default "+crc(b1)+" "+markers(b1));
  // modify: remove JFIF, add comment, density
  md=w.getDefaultImageMetadata(its,p); Node root=md.getAsTree(md.getNativeMetadataFormatName()); IIOMetadataNode j=(IIOMetadataNode)((IIOMetadataNode)root).getElementsByTagName("app0JFIF").item(0); j.setAttribute("Xdensity","72"); j.setAttribute("Ydensity","72"); j.setAttribute("resUnits","1"); IIOMetadataNode seq=(IIOMetadataNode)root.getLastChild(); IIOMetadataNode com=new IIOMetadataNode("com"); com.setAttribute("comment","hello"); seq.insertBefore(com, seq.getFirstChild()); IIOMetadataNode unk=new IIOMetadataNode("unknown"); unk.setAttribute("MarkerTag","225"); unk.setUserObject(new byte[]{1,2,3}); seq.insertBefore(unk, seq.getFirstChild()); md.setFromTree(md.getNativeMetadataFormatName(), root); byte[] b2=write(im,md,null); System.out.println("modified "+crc(b2)+" "+markers(b2));
  // no JFIF
  md=w.getDefaultImageMetadata(its,p); root=md.getAsTree(md.getNativeMetadataFormatName()); IIOMetadataNode var=(IIOMetadataNode)root.getFirstChild(); while(var.getFirstChild()!=null) var.removeChild(var.getFirstChild()); md.setFromTree(md.getNativeMetadataFormatName(), root); byte[] b3=write(im,md,null); System.out.println("nojfif "+crc(b3)+" "+markers(b3));
  // 4:4:4
  md=w.getDefaultImageMetadata(its,p); root=md.getAsTree(md.getNativeMetadataFormatName()); IIOMetadataNode sof=(IIOMetadataNode)((IIOMetadataNode)root).getElementsByTagName("sof").item(0); IIOMetadataNode cs=(IIOMetadataNode)sof.getFirstChild(); cs.setAttribute("HsamplingFactor","1"); cs.setAttribute("VsamplingFactor","1"); md.setFromTree(md.getNativeMetadataFormatName(), root); byte[] b4=write(im,md,null); System.out.println("444 "+crc(b4)+" "+markers(b4));
  // gray and quality
  BufferedImage g=new BufferedImage(40,24,BufferedImage.TYPE_BYTE_GRAY); g.getRaster().setSample(5,5,0,200); byte[] b6=write(g,null,null); System.out.println("gray "+crc(b6)+" "+markers(b6));
  // optimize huffman
  ((JPEGImageWriteParam)p).setOptimizeHuffmanTables(true); byte[] b7=write(im,null,p); System.out.println("optimized "+crc(b7)+" "+markers(b7)); } }
