/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

import javax.swing.*; import java.awt.*; import java.awt.image.*;
public class ProgressBarPaintParity { public static void main(String[] a){ for (int v : new int[]{0,1,40,100}) for (boolean str : new boolean[]{false}) { JProgressBar pb=new JProgressBar(0,100); pb.setValue(v); pb.setSize(40,12); BufferedImage im=new BufferedImage(40,12,1); Graphics2D g=im.createGraphics(); g.setColor(Color.WHITE); g.fillRect(0,0,40,12); pb.paint(g); System.out.println("v="+v+" "+pb.getInsets()); for(int y=0;y<12;y++){StringBuilder s=new StringBuilder(); for(int x=0;x<40;x++){int p=im.getRGB(x,y)&0xffffff; s.append(p==0x7a8a99?'D':p==0x6382bf?'P':p==0xa3b8cc?'F':p==0xb8cfe5?'L':p==0xeeeeee?'.':p==0xffffff?'w':'?');} System.out.println(s);} }
 JProgressBar pv=new JProgressBar(JProgressBar.VERTICAL,0,100); pv.setValue(40); pv.setSize(12,40); BufferedImage im=new BufferedImage(12,40,1); Graphics2D g=im.createGraphics(); pv.paint(g); for(int y=0;y<40;y++){StringBuilder s=new StringBuilder(); for(int x=0;x<12;x++){int p=im.getRGB(x,y)&0xffffff; s.append(p==0x7a8a99?'D':p==0x6382bf?'P':p==0xa3b8cc?'F':p==0xb8cfe5?'L':p==0xeeeeee?'.':'?');} System.out.println(s);} 
 JProgressBar pi=new JProgressBar(0,100); pi.setIndeterminate(true); pi.setSize(40,12); BufferedImage i2=new BufferedImage(40,12,1); Graphics2D g2=i2.createGraphics(); pi.paint(g2); for(int y=0;y<12;y++){StringBuilder s=new StringBuilder(); for(int x=0;x<40;x++){int p=i2.getRGB(x,y)&0xffffff; s.append(p==0x7a8a99?'D':p==0x6382bf?'P':p==0xa3b8cc?'F':p==0xb8cfe5?'L':p==0xeeeeee?'.':'?');} System.out.println(s);} } }
