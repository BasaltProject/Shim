/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

import java.awt.*; import java.awt.image.*; import java.awt.geom.*;
public class GradientPaintParity { public static void main(String[] a){ Object[][] cfg={{0f,0f,100f,0f,false},{0f,0f,0f,80f,false},{0f,0f,100f,80f,false},{10f,10f,60f,50f,true},{0f,0f,7f,3f,true},{50.5f,20.25f,10.3f,70.7f,false}}; for(Object[] c: cfg) for(int ty: new int[]{BufferedImage.TYPE_INT_RGB,BufferedImage.TYPE_INT_ARGB}){ BufferedImage im=new BufferedImage(100,80,ty); Graphics2D g=im.createGraphics(); g.setPaint(new GradientPaint((Float)c[0],(Float)c[1],new Color(255,0,0,200),(Float)c[2],(Float)c[3],new Color(0,0,255,100),(Boolean)c[4])); g.fillRect(0,0,100,80); g.dispose(); java.util.zip.CRC32 cr=new java.util.zip.CRC32(); int mism=0; for(int y=0;y<80;y++)for(int x=0;x<100;x++) cr.update(im.getRGB(x,y)); System.out.println(java.util.Arrays.toString(c)+" t"+ty+" "+Long.toHexString(cr.getValue())+" "+Integer.toHexString(im.getRGB(37,41))+" "+Integer.toHexString(im.getRGB(80,3))); } } }
