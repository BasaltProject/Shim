/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

import javax.swing.*; import javax.swing.border.*; import java.awt.*; import java.awt.image.*;
public class TitledBorderParity {
  static void run(String n, TitledBorder tb) { JPanel p=new JPanel(); p.setBorder(tb); p.setSize(100,60); BufferedImage im=new BufferedImage(100,60,1); Graphics2D g=im.createGraphics(); g.setColor(Color.WHITE); g.fillRect(0,0,100,60); p.setBackground(Color.WHITE); p.paint(g);
    // border-only rows/cols of red
    int minx=999,miny=999,maxx=-1,maxy=-1; java.util.TreeSet<Integer> gaps=new java.util.TreeSet<>(); 
    for(int y=0;y<60;y++)for(int x=0;x<100;x++) if((im.getRGB(x,y)&0xffffff)==0xff0000){minx=Math.min(minx,x);miny=Math.min(miny,y);maxx=Math.max(maxx,x);maxy=Math.max(maxy,y);}
    StringBuilder top=new StringBuilder(); for(int x=0;x<100;x++) top.append((im.getRGB(x,miny)&0xffffff)==0xff0000?'#':'.');
    System.out.println(n+" box "+minx+","+miny+" - "+maxx+","+maxy+" insets "+tb.getBorderInsets(p)+"\n  "+top); }
  public static void main(String[] a){
    Border r1=new LineBorder(Color.RED,1), r5=new LineBorder(Color.RED,5);
    int[] pos={TitledBorder.DEFAULT_POSITION,TitledBorder.ABOVE_TOP,TitledBorder.TOP,TitledBorder.BELOW_TOP,TitledBorder.ABOVE_BOTTOM,TitledBorder.BOTTOM,TitledBorder.BELOW_BOTTOM};
    int[] just={TitledBorder.LEFT,TitledBorder.CENTER,TitledBorder.RIGHT};
    for (Border b : new Border[]{r1,r5}) for (int ps : pos) { TitledBorder tb=new TitledBorder(b,"Title",TitledBorder.LEFT,ps); tb.setTitleColor(Color.BLACK); run("t"+((LineBorder)b).getThickness()+" pos"+ps, tb); }
    for (int j : just) { TitledBorder tb=new TitledBorder(r1,"Title",j,TitledBorder.TOP); run("just"+j, tb); }
  } }
