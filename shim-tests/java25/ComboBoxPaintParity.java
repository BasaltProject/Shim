/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

import javax.swing.*; import java.awt.*; import java.awt.image.*; import java.util.zip.CRC32;
public class ComboBoxPaintParity { public static void main(String[] a) {
  for (int[] sz : new int[][]{{120, 26}, {80, 24}, {200, 30}}) { JComboBox<String> c = new JComboBox<>(new String[]{"", "x"}); c.setSelectedIndex(0); c.setSize(sz[0], sz[1]); c.doLayout();
    BufferedImage im = new BufferedImage(sz[0], sz[1], 1); Graphics2D g = im.createGraphics(); g.setColor(Color.WHITE); g.fillRect(0, 0, sz[0], sz[1]); c.paint(g); CRC32 k = new CRC32(); for (int y = 0; y < sz[1]; y++) for (int x = 0; x < sz[0]; x++) k.update(im.getRGB(x, y) & 0xffffff);
    System.out.println(sz[0] + "x" + sz[1] + " " + Long.toHexString(k.getValue())); } } }
