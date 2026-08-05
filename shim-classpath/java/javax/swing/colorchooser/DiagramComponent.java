/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */
package javax.swing.colorchooser;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import javax.swing.JComponent;

final class DiagramComponent extends JComponent {
  private static final int kSize = 177;
  private final ChannelModel model;
  private final ColorChooserPanel owner;
  private final boolean twoDimensional;
  private BufferedImage image;
  private int imageKey = Integer.MIN_VALUE;

  DiagramComponent(ChannelModel model, ColorChooserPanel owner, boolean twoDimensional) {
    this.model = model;
    this.owner = owner;
    this.twoDimensional = twoDimensional;
    MouseAdapter handler = new MouseAdapter() {
      public void mousePressed(MouseEvent e) {
        pick(e);
      }

      public void mouseDragged(MouseEvent e) {
        pick(e);
      }
    };
    addMouseListener(handler);
    addMouseMotionListener(handler);
  }

  public Dimension getPreferredSize() {
    return new Dimension(twoDimensional ? kSize : 19, kSize);
  }

  public Dimension getMinimumSize() {
    return getPreferredSize();
  }

  private void pick(MouseEvent e) {
    int sel = owner.selectedChannel();
    int[] v = owner.values();
    int x = Math.max(0, Math.min(getWidth() - 1, e.getX()));
    int y = Math.max(0, Math.min(getHeight() - 1, e.getY()));
    int yi = model.max[twoDimensional ? yAxis(sel) : sel] * (getHeight() - 1 - y) / (getHeight() - 1);
    if (twoDimensional) {
      v[xAxis(sel)] = model.max[xAxis(sel)] * x / (getWidth() - 1);
      v[yAxis(sel)] = yi;
    } else {
      v[sel] = yi;
    }
    owner.setValues(v);
  }

  private static int xAxis(int sel) {
    return sel == 0 ? 1 : 0;
  }

  private static int yAxis(int sel) {
    return sel == 2 ? 1 : 2;
  }

  protected void paintComponent(Graphics g) {
    int w = getWidth();
    int h = getHeight();
    int sel = owner.selectedChannel();
    int[] v = owner.values();
    int key = (sel << 24) ^ (twoDimensional ? 0 : v[xAxis(sel)] * 31 + v[yAxis(sel)] * 7919)
        ^ (twoDimensional ? v[sel] * 104729 : 0);
    if (image == null || image.getWidth() != w || image.getHeight() != h || key != imageKey) {
      image = new BufferedImage(Math.max(w, 1), Math.max(h, 1), BufferedImage.TYPE_INT_ARGB);
      int[] t = v.clone();
      for (int y = 0; y < h; y++) {
        int yv = model.max[twoDimensional ? yAxis(sel) : sel] * (h - 1 - y) / Math.max(h - 1, 1);
        for (int x = 0; x < w; x++) {
          if (twoDimensional) {
            t[xAxis(sel)] = model.max[xAxis(sel)] * x / Math.max(w - 1, 1);
            t[yAxis(sel)] = yv;
          } else {
            t[sel] = yv;
          }
          Color c = model.toColor(t);
          image.setRGB(x, y, c.getRGB() | 0xFF000000);
        }
      }
      imageKey = key;
    }
    g.drawImage(image, 0, 0, null);
    int mx = twoDimensional ? v[xAxis(sel)] * (w - 1) / model.max[xAxis(sel)] : w / 2;
    int my = (h - 1) - v[twoDimensional ? yAxis(sel) : sel] * (h - 1) / model.max[twoDimensional ? yAxis(sel) : sel];
    g.setXORMode(Color.white);
    if (twoDimensional) {
      g.drawOval(mx - 4, my - 4, 8, 8);
    } else {
      g.drawLine(0, my, w - 1, my);
    }
    g.setPaintMode();
  }
}
