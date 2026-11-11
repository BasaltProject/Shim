/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

import javax.swing.*; import java.beans.*; import java.io.*; import java.awt.*;
public class XmlSwingParity { static void enc(Object o){ ByteArrayOutputStream b=new ByteArrayOutputStream(); XMLEncoder e=new XMLEncoder(b); e.setExceptionListener(x->System.out.println("EXC "+x)); e.writeObject(o); e.close(); String s=b.toString(); System.out.println(s.replaceAll("version=\"[^\"]*\"","version=V")); }
 public static void main(String[] a){ JButton b=new JButton("Hi"); enc(b); JLabel l=new JLabel("x"); l.setForeground(Color.RED); enc(l); } }
