/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

import java.awt.*; import java.awt.image.*; import javax.swing.*; import javax.swing.border.*;
public class BevelBorderParity { public static void main(String[] a){ for (int t : new int[]{BevelBorder.RAISED, BevelBorder.LOWERED}) for (boolean soft : new boolean[]{true,false}) { 
 BufferedImage im=new BufferedImage(12,9,1); Graphics2D g=im.createGraphics(); g.setColor(Color.WHITE); g.fillRect(0,0,12,9);
 Color r=Color.RED,gr=Color.GREEN,b=Color.BLUE,y=Color.YELLOW; Border bd = soft? new SoftBevelBorder(t,r,gr,b,y) : new BevelBorder(t,r,gr,b,y); bd.paintBorder(new JPanel(), g, 0,0,12,9);
 System.out.println((soft?"soft ":"hard ")+t+" insets "+bd.getBorderInsets(new JPanel()));
 for(int j=0;j<9;j++){StringBuilder s=new StringBuilder(); for(int i=0;i<12;i++){int p=im.getRGB(i,j)&0xffffff; s.append(p==0xffffff?'.':p==0xff0000?'R':p==0xff00?'G':p==0xff?'B':p==0xffff00?'Y':'?');} System.out.println(s);} } } }
