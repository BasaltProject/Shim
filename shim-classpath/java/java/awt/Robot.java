/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.awt;

public class Robot {
  public Robot() throws java.awt.AWTException {
    throw new java.awt.AWTException("headless environment");
  }
  public Robot(java.awt.GraphicsDevice p0) throws java.awt.AWTException {
    throw new java.awt.AWTException("headless environment");
  }
  public java.lang.String toString() { throw new UnsupportedOperationException(); }
  public void delay(int p0) { throw new UnsupportedOperationException(); }
  public boolean isAutoWaitForIdle() { throw new UnsupportedOperationException(); }
  public void mousePress(int p0) { throw new UnsupportedOperationException(); }
  public void mouseRelease(int p0) { throw new UnsupportedOperationException(); }
  public void mouseWheel(int p0) { throw new UnsupportedOperationException(); }
  public void keyPress(int p0) { throw new UnsupportedOperationException(); }
  public void keyRelease(int p0) { throw new UnsupportedOperationException(); }
  public void waitForIdle() { throw new UnsupportedOperationException(); }
  public int getAutoDelay() { throw new UnsupportedOperationException(); }
  public java.awt.Color getPixelColor(int p0, int p1) { throw new UnsupportedOperationException(); }
  public java.awt.image.BufferedImage createScreenCapture(java.awt.Rectangle p0) { throw new UnsupportedOperationException(); }
  public java.awt.image.MultiResolutionImage createMultiResolutionScreenCapture(java.awt.Rectangle p0) { throw new UnsupportedOperationException(); }
  public void setAutoWaitForIdle(boolean p0) { throw new UnsupportedOperationException(); }
  public void setAutoDelay(int p0) { throw new UnsupportedOperationException(); }
  public void mouseMove(int p0, int p1) { throw new UnsupportedOperationException(); }
}
