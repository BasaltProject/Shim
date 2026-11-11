/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

import java.awt.*; import java.awt.image.*;
public class AlphaBlendParity { public static void main(String[] a){ StringBuilder sb=new StringBuilder(); for(int ty: new int[]{BufferedImage.TYPE_INT_ARGB,BufferedImage.TYPE_INT_RGB}){ BufferedImage im=new BufferedImage(100,4,ty); Graphics2D g=im.createGraphics(); g.setPaint(new GradientPaint(0,0,new Color(255,0,0,200),100,0,new Color(0,0,255,100))); g.fillRect(0,0,100,4); g.dispose(); for(int x=0;x<100;x++) sb.append(Integer.toHexString(im.getRGB(x,1))).append(' '); sb.append('\n'); } System.out.println(sb); } }
