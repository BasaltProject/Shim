/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

import javax.swing.*; import java.awt.*; import java.awt.image.*; import java.util.zip.CRC32;
public class SliderPaintParity { public static void main(String[] a) throws Exception { SwingUtilities.invokeAndWait(() -> run()); }
  static void run() {
  for (int o : new int[]{0, 1}) for (int v : new int[]{0, 30, 100}) for (boolean ticks : new boolean[]{false, true}) { JSlider s = new JSlider(o, 0, 100, v); s.setPaintTicks(ticks); s.setMajorTickSpacing(25); s.setMinorTickSpacing(5);
    int w = o == 0 ? 160 : 40, h = o == 0 ? 40 : 160; s.setSize(w, h); s.doLayout(); BufferedImage im = new BufferedImage(w, h, 1); Graphics2D g = im.createGraphics(); g.setColor(Color.WHITE); g.fillRect(0, 0, w, h); s.paint(g);
    CRC32 c = new CRC32(); for (int y = 0; y < h; y++) for (int x = 0; x < w; x++) c.update(im.getRGB(x, y) & 0xffffff); System.out.println(o + " " + v + " " + ticks + " " + Long.toHexString(c.getValue())); } } }
