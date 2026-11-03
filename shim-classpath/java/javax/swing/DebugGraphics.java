/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Shape;
import java.io.PrintStream;

public class DebugGraphics extends Graphics {
  Graphics graphics;
  java.awt.Image buffer;
  int debugOptions;
  int graphicsID = graphicsCount++;
  int xOffset;
  int yOffset;
  private static int graphicsCount = 0;
  private static int flashTime = 100;
  private static int flashCount = 2;
  private static Color flashColor = Color.red;
  private static PrintStream logStream = System.out;

  public static final int LOG_OPTION = 1 << 0;
  public static final int FLASH_OPTION = 1 << 1;
  public static final int BUFFERED_OPTION = 1 << 2;
  public static final int NONE_OPTION = -1;

  public DebugGraphics() {
    super();
    buffer = null;
    xOffset = yOffset = 0;
  }

  public DebugGraphics(Graphics graphics, JComponent component) {
    this(graphics);
    this.component = component;
    setDebugOptions(component == null ? 0 : component.getDebugGraphicsOptions());
  }

  public DebugGraphics(Graphics graphics) {
    this();
    this.graphics = graphics;
  }

  private JComponent component;

  private void log(String method, String args) {
    if ((debugOptions & LOG_OPTION) != 0) {
      logStream.println(toShortString() + " " + method + ": " + args);
    }
  }

  public Graphics create() {
    DebugGraphics debugGraphics = new DebugGraphics();
    debugGraphics.graphics = graphics.create();
    debugGraphics.debugOptions = debugOptions;
    debugGraphics.buffer = buffer;
    debugGraphics.component = component;
    log("create", "");
    return debugGraphics;
  }

  public Graphics create(int x, int y, int width, int height) {
    DebugGraphics debugGraphics = new DebugGraphics();
    debugGraphics.graphics = graphics.create(x, y, width, height);
    debugGraphics.debugOptions = debugOptions;
    debugGraphics.buffer = buffer;
    debugGraphics.xOffset = xOffset + x;
    debugGraphics.yOffset = yOffset + y;
    debugGraphics.component = component;
    log("create", x + ", " + y + ", " + width + ", " + height);
    return debugGraphics;
  }

  public static void setFlashColor(Color flashColor) { DebugGraphics.flashColor = flashColor; }

  public static Color flashColor() { return flashColor; }

  public static void setFlashTime(int flashTime) { DebugGraphics.flashTime = flashTime; }

  public static int flashTime() { return flashTime; }

  public static void setFlashCount(int flashCount) { DebugGraphics.flashCount = flashCount; }

  public static int flashCount() { return flashCount; }

  public static void setLogStream(PrintStream stream) { logStream = stream; }

  public static PrintStream logStream() { return logStream; }

  public void setFont(Font aFont) {
    log("setFont", String.valueOf(aFont));
    graphics.setFont(aFont);
  }

  public Font getFont() { return graphics.getFont(); }

  public void setColor(Color aColor) {
    log("setColor", String.valueOf(aColor));
    graphics.setColor(aColor);
  }

  public Color getColor() { return graphics.getColor(); }

  public FontMetrics getFontMetrics() { return graphics.getFontMetrics(); }

  public FontMetrics getFontMetrics(Font f) { return graphics.getFontMetrics(f); }

  public void translate(int x, int y) {
    log("translate", x + ", " + y);
    xOffset += x;
    yOffset += y;
    graphics.translate(x, y);
  }

  public void setPaintMode() {
    log("setPaintMode", "");
    graphics.setPaintMode();
  }

  public void setXORMode(Color aColor) {
    log("setXORMode", String.valueOf(aColor));
    graphics.setXORMode(aColor);
  }

  public Rectangle getClipBounds() { return graphics.getClipBounds(); }

  public Shape getClip() { return graphics.getClip(); }

  public void setClip(Shape clip) {
    log("setClip", String.valueOf(clip));
    graphics.setClip(clip);
  }

  public void dispose() {
    log("dispose", "");
    graphics.dispose();
    graphics = null;
  }

  public boolean isDrawingBuffer() { return buffer != null; }

  public void setDebugOptions(int options) {
    if (options != 0) {
      if (options == NONE_OPTION) {
        if (debugOptions != 0) {
          System.err.println(toShortString() + " Disabling debug");
          debugOptions = 0;
        }
      } else {
        if (debugOptions != options) {
          debugOptions |= options;
          if ((debugOptions & LOG_OPTION) != 0) {
            System.err.println(toShortString() + " Enabling debug");
          }
        }
      }
    }
  }

  private String toShortString() {
    return "Graphics" + (buffer == null ? "" : "<B>") + "(" + graphicsID + "-" + debugOptions + ")";
  }

  public int getDebugOptions() { return debugOptions; }

  public void clearRect(int x, int y, int width, int height) {
    log("clearRect", "" + x + ", " + y + ", " + width + ", " + height);
    graphics.clearRect(x, y, width, height);
  }

  public void setClip(int x, int y, int width, int height) {
    log("setClip", "" + x + ", " + y + ", " + width + ", " + height);
    graphics.setClip(x, y, width, height);
  }

  public void clipRect(int x, int y, int width, int height) {
    log("clipRect", "" + x + ", " + y + ", " + width + ", " + height);
    graphics.clipRect(x, y, width, height);
  }

  public void drawLine(int x1, int y1, int x2, int y2) {
    log("drawLine", "" + x1 + ", " + y1 + ", " + x2 + ", " + y2);
    graphics.drawLine(x1, y1, x2, y2);
  }

  public void fillRect(int x, int y, int width, int height) {
    log("fillRect", "" + x + ", " + y + ", " + width + ", " + height);
    graphics.fillRect(x, y, width, height);
  }

  public void drawRect(int x, int y, int width, int height) {
    log("drawRect", "" + x + ", " + y + ", " + width + ", " + height);
    graphics.drawRect(x, y, width, height);
  }

  public void drawRoundRect(int x, int y, int width, int height, int arcWidth, int arcHeight) {
    log("drawRoundRect", "" + x + ", " + y + ", " + width + ", " + height + ", " + arcWidth + ", " + arcHeight);
    graphics.drawRoundRect(x, y, width, height, arcWidth, arcHeight);
  }

  public void fillRoundRect(int x, int y, int width, int height, int arcWidth, int arcHeight) {
    log("fillRoundRect", "" + x + ", " + y + ", " + width + ", " + height + ", " + arcWidth + ", " + arcHeight);
    graphics.fillRoundRect(x, y, width, height, arcWidth, arcHeight);
  }

  public void draw3DRect(int x, int y, int width, int height, boolean raised) {
    log("draw3DRect", "" + x + ", " + y + ", " + width + ", " + height + ", " + raised);
    graphics.draw3DRect(x, y, width, height, raised);
  }

  public void fill3DRect(int x, int y, int width, int height, boolean raised) {
    log("fill3DRect", "" + x + ", " + y + ", " + width + ", " + height + ", " + raised);
    graphics.fill3DRect(x, y, width, height, raised);
  }

  public void drawOval(int x, int y, int width, int height) {
    log("drawOval", "" + x + ", " + y + ", " + width + ", " + height);
    graphics.drawOval(x, y, width, height);
  }

  public void fillOval(int x, int y, int width, int height) {
    log("fillOval", "" + x + ", " + y + ", " + width + ", " + height);
    graphics.fillOval(x, y, width, height);
  }

  public void drawArc(int x, int y, int width, int height, int startAngle, int arcAngle) {
    log("drawArc", "" + x + ", " + y + ", " + width + ", " + height + ", " + startAngle + ", " + arcAngle);
    graphics.drawArc(x, y, width, height, startAngle, arcAngle);
  }

  public void fillArc(int x, int y, int width, int height, int startAngle, int arcAngle) {
    log("fillArc", "" + x + ", " + y + ", " + width + ", " + height + ", " + startAngle + ", " + arcAngle);
    graphics.fillArc(x, y, width, height, startAngle, arcAngle);
  }

  public void drawPolygon(int[] xPoints, int[] yPoints, int nPoints) {
    log("drawPolygon", "" + nPoints);
    graphics.drawPolygon(xPoints, yPoints, nPoints);
  }

  public void fillPolygon(int[] xPoints, int[] yPoints, int nPoints) {
    log("fillPolygon", "" + nPoints);
    graphics.fillPolygon(xPoints, yPoints, nPoints);
  }

  public void drawPolyline(int[] xPoints, int[] yPoints, int nPoints) {
    log("drawPolyline", "" + nPoints);
    graphics.drawPolyline(xPoints, yPoints, nPoints);
  }

  public void drawString(java.lang.String str, int x, int y) {
    log("drawString", "" + str + ", " + x + ", " + y);
    graphics.drawString(str, x, y);
  }

  public void drawString(java.text.AttributedCharacterIterator iterator, int x, int y) {
    log("drawString", "" + iterator + ", " + x + ", " + y);
    graphics.drawString(iterator, x, y);
  }

  public void copyArea(int x, int y, int width, int height, int dx, int dy) {
    log("copyArea", "" + x + ", " + y + ", " + width + ", " + height + ", " + dx + ", " + dy);
    graphics.copyArea(x, y, width, height, dx, dy);
  }

  public void drawChars(char[] data, int offset, int length, int x, int y) {
    log("drawChars", x + ", " + y);
    graphics.drawChars(data, offset, length, x, y);
  }

  public void drawBytes(byte[] data, int offset, int length, int x, int y) {
    log("drawBytes", x + ", " + y);
    graphics.drawBytes(data, offset, length, x, y);
  }

  public boolean drawImage(java.awt.Image img, int x, int y, java.awt.image.ImageObserver observer) {
    log("drawImage", "" + img + ", " + x + ", " + y + ", " + observer);
    return graphics.drawImage(img, x, y, observer);
  }

  public boolean drawImage(java.awt.Image img, int x, int y, int width, int height, java.awt.image.ImageObserver observer) {
    log("drawImage", "" + img + ", " + x + ", " + y + ", " + width + ", " + height + ", " + observer);
    return graphics.drawImage(img, x, y, width, height, observer);
  }

  public boolean drawImage(java.awt.Image img, int x, int y, java.awt.Color bgcolor, java.awt.image.ImageObserver observer) {
    log("drawImage", "" + img + ", " + x + ", " + y + ", " + bgcolor + ", " + observer);
    return graphics.drawImage(img, x, y, bgcolor, observer);
  }

  public boolean drawImage(java.awt.Image img, int x, int y, int width, int height, java.awt.Color bgcolor, java.awt.image.ImageObserver observer) {
    log("drawImage", "" + img + ", " + x + ", " + y + ", " + width + ", " + height + ", " + bgcolor + ", " + observer);
    return graphics.drawImage(img, x, y, width, height, bgcolor, observer);
  }

  public boolean drawImage(java.awt.Image img, int dx1, int dy1, int dx2, int dy2, int sx1, int sy1, int sx2, int sy2, java.awt.image.ImageObserver observer) {
    log("drawImage", "" + img + ", " + dx1 + ", " + dy1 + ", " + dx2 + ", " + dy2 + ", " + sx1 + ", " + sy1 + ", " + sx2 + ", " + sy2 + ", " + observer);
    return graphics.drawImage(img, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, observer);
  }

  public boolean drawImage(java.awt.Image img, int dx1, int dy1, int dx2, int dy2, int sx1, int sy1, int sx2, int sy2, java.awt.Color bgcolor, java.awt.image.ImageObserver observer) {
    log("drawImage", "" + img + ", " + dx1 + ", " + dy1 + ", " + dx2 + ", " + dy2 + ", " + sx1 + ", " + sy1 + ", " + sx2 + ", " + sy2 + ", " + bgcolor + ", " + observer);
    return graphics.drawImage(img, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, bgcolor, observer);
  }
}
