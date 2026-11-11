/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

import javax.swing.*; import java.awt.*; import java.awt.image.*; import java.util.zip.CRC32;
public class ScrollBarPaintParity { public static void main(String[] a) {
  for (int o : new int[]{0, 1}) for (int[] v : new int[][]{{0, 20}, {20, 20}, {80, 20}, {0, 100}}) { JScrollBar sb = new JScrollBar(o, v[0], v[1], 0, 100); int w = o == 0 ? 160 : 18, h = o == 0 ? 18 : 120; sb.setSize(w, h); sb.doLayout();
    BufferedImage im = new BufferedImage(w, h, 1); Graphics2D g = im.createGraphics(); g.setColor(Color.WHITE); g.fillRect(0, 0, w, h); sb.paint(g); CRC32 c = new CRC32(); for (int y = 0; y < h; y++) for (int x = 0; x < w; x++) c.update(im.getRGB(x, y) & 0xffffff);
    System.out.println(o + " " + v[0] + " " + v[1] + " " + Long.toHexString(c.getValue())); } } }
