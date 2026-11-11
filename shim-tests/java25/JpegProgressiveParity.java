/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

import javax.imageio.*; import javax.imageio.plugins.jpeg.*; import javax.imageio.stream.*; import java.awt.image.*; import java.io.*; import java.util.*;
public class JpegProgressiveParity { static String crc(byte[] b){ java.util.zip.CRC32 c=new java.util.zip.CRC32(); c.update(b); return Long.toHexString(c.getValue())+" "+b.length; }
 public static void main(String[] a) throws Exception { Random rnd=new Random(7); int[][] sizes={{1,1},{7,9},{16,16},{33,17},{64,48},{100,75}}; for(int[] sz: sizes) for(int type: new int[]{BufferedImage.TYPE_INT_RGB, BufferedImage.TYPE_BYTE_GRAY}) for(float q: new float[]{0.75f,0.3f,0.95f}) for(boolean opt: new boolean[]{false,true}) for(int prog=0; prog<2; prog++){ BufferedImage im=new BufferedImage(sz[0],sz[1],type); for(int y=0;y<sz[1];y++)for(int x=0;x<sz[0];x++){ int v=(int)(128+100*Math.sin(x*0.3)*Math.cos(y*0.2))+rnd.nextInt(30); im.getRaster().setSample(x,y,0,Math.max(0,Math.min(255,v))); if(type==BufferedImage.TYPE_INT_RGB){ im.getRaster().setSample(x,y,1,(x*5+y*3)&255); im.getRaster().setSample(x,y,2,rnd.nextInt(256)); } }
   ImageWriter w=ImageIO.getImageWritersByFormatName("jpeg").next(); JPEGImageWriteParam p=(JPEGImageWriteParam)w.getDefaultWriteParam(); p.setCompressionMode(ImageWriteParam.MODE_EXPLICIT); p.setCompressionQuality(q); p.setOptimizeHuffmanTables(opt); if(prog==1) p.setProgressiveMode(ImageWriteParam.MODE_DEFAULT); ByteArrayOutputStream bo=new ByteArrayOutputStream(); ImageOutputStream os=ImageIO.createImageOutputStream(bo); w.setOutput(os); w.write(null,new IIOImage(im,null,null),p); os.close(); System.out.println(sz[0]+"x"+sz[1]+" t"+type+" q"+q+" o"+opt+" p"+prog+" "+crc(bo.toByteArray())); } } }
