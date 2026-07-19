/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */
package shim.swing;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import javax.swing.Icon;
import javax.swing.plaf.UIResource;

public final class FileIcons implements Icon, UIResource, java.io.Serializable {
  public static final int kDirectory = 0;
  public static final int kFile = 1;
  public static final int kComputer = 2;
  public static final int kHardDrive = 3;
  public static final int kFloppy = 4;
  public static final int kHome = 5;
  public static final int kNewFolder = 6;
  public static final int kUpFolder = 7;
  public static final int kList = 8;
  public static final int kDetails = 9;
  public static final int kFrame = 10;

  private static final Color kOutline = new Color(0x4F4F4F);
  private static final Color kFolder = new Color(0xF1D677);
  private static final Color kFolderDark = new Color(0xC9A43A);
  private static final Color kPaper = new Color(0xFFFFFF);
  private static final Color kGreen = new Color(0x3E9B3E);

  private final int kind;
  private final int width;
  private final int height;

  public FileIcons(int kind) {
    this.kind = kind;
    switch (kind) {
      case kDirectory:
        width = 16;
        height = 18;
        break;
      case kFile:
        width = 16;
        height = 20;
        break;
      case kFrame:
      case kComputer:
      case kHardDrive:
      case kFloppy:
        width = 16;
        height = 16;
        break;
      default:
        width = 18;
        height = 18;
        break;
    }
  }

  public int getIconWidth() { return width; }

  public int getIconHeight() { return height; }

  public void paintIcon(Component c, Graphics g, int x, int y) {
    g.translate(x, y);
    switch (kind) {
      case kDirectory:
        folder(g, 1, 3, 14, 12);
        break;
      case kFile:
        file(g, 2, 1, 12, 17);
        break;
      case kComputer:
        g.setColor(kOutline);
        g.drawRect(1, 1, 13, 9);
        g.setColor(new Color(0x9CC3E6));
        g.fillRect(2, 2, 12, 8);
        g.setColor(kOutline);
        g.fillRect(5, 11, 6, 2);
        g.drawLine(3, 14, 12, 14);
        break;
      case kHardDrive:
        g.setColor(kOutline);
        g.drawRect(0, 5, 15, 7);
        g.setColor(new Color(0xD0D0D0));
        g.fillRect(1, 6, 14, 6);
        g.setColor(kOutline);
        g.fillRect(11, 9, 2, 1);
        break;
      case kFloppy:
        g.setColor(kOutline);
        g.drawRect(1, 1, 13, 13);
        g.setColor(new Color(0x5B7FA6));
        g.fillRect(2, 2, 12, 12);
        g.setColor(kPaper);
        g.fillRect(4, 2, 8, 5);
        g.setColor(kOutline);
        g.fillRect(5, 9, 6, 5);
        break;
      case kHome:
        g.setColor(kOutline);
        g.drawLine(9, 2, 2, 9);
        g.drawLine(9, 2, 16, 9);
        g.drawRect(4, 9, 9, 7);
        g.setColor(new Color(0xE8B26A));
        g.fillRect(5, 10, 8, 6);
        g.setColor(kOutline);
        g.fillRect(8, 12, 3, 4);
        break;
      case kNewFolder:
        folder(g, 1, 4, 14, 12);
        g.setColor(kGreen);
        g.fillRect(13, 1, 2, 7);
        g.fillRect(11, 3, 6, 2);
        break;
      case kUpFolder:
        folder(g, 1, 6, 14, 11);
        g.setColor(kGreen);
        g.drawLine(8, 1, 8, 6);
        g.drawLine(5, 4, 8, 1);
        g.drawLine(11, 4, 8, 1);
        break;
      case kList:
        for (int row = 0; row < 3; row++) {
          g.setColor(kOutline);
          g.drawRect(2, 3 + row * 4, 4, 2);
          g.drawRect(10, 3 + row * 4, 4, 2);
          g.setColor(new Color(0x7F9BC2));
          g.fillRect(3, 4 + row * 4, 3, 1);
          g.fillRect(11, 4 + row * 4, 3, 1);
        }
        break;
      case kFrame:
        g.setColor(new Color(0xB5651D));
        g.fillRect(3, 6, 8, 7);
        g.setColor(kOutline);
        g.drawRect(3, 6, 8, 7);
        g.drawArc(10, 7, 4, 4, -90, 180);
        g.setColor(new Color(0x4A90D9));
        g.drawLine(5, 2, 6, 4);
        g.drawLine(8, 1, 9, 4);
        break;
      default:
        for (int row = 0; row < 3; row++) {
          g.setColor(kOutline);
          g.drawRect(2, 3 + row * 4, 2, 2);
          g.drawLine(6, 4 + row * 4, 15, 4 + row * 4);
        }
        break;
    }
    g.translate(-x, -y);
  }

  private static void folder(Graphics g, int x, int y, int w, int h) {
    g.setColor(kFolderDark);
    g.fillRect(x, y - 2, w / 2 - 1, 3);
    g.setColor(kFolder);
    g.fillRect(x, y, w, h);
    g.setColor(kOutline);
    g.drawRect(x, y, w - 1, h - 1);
    g.drawLine(x, y - 2, x + w / 2 - 2, y - 2);
    g.drawLine(x, y - 2, x, y);
    g.drawLine(x + w / 2 - 2, y - 2, x + w / 2, y);
  }

  private static void file(Graphics g, int x, int y, int w, int h) {
    g.setColor(kPaper);
    g.fillRect(x, y, w, h);
    g.setColor(kOutline);
    g.drawLine(x, y, x + w - 5, y);
    g.drawLine(x + w - 5, y, x + w - 1, y + 4);
    g.drawLine(x + w - 1, y + 4, x + w - 1, y + h - 1);
    g.drawLine(x + w - 1, y + h - 1, x, y + h - 1);
    g.drawLine(x, y + h - 1, x, y);
    g.drawLine(x + w - 5, y, x + w - 5, y + 4);
    g.drawLine(x + w - 5, y + 4, x + w - 1, y + 4);
  }
}
