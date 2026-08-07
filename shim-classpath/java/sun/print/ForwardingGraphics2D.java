/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */
package sun.print;

import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ImageObserver;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.RenderableImage;
import java.text.AttributedCharacterIterator;
import java.util.Map;

abstract class ForwardingGraphics2D extends Graphics2D {
  protected final Graphics2D state;

  ForwardingGraphics2D(Graphics2D state) {
    this.state = state;
  }

  public void translate(int x, int y) { state.translate(x, y); }
  public void translate(double tx, double ty) { state.translate(tx, ty); }
  public void rotate(double theta) { state.rotate(theta); }
  public void rotate(double theta, double x, double y) { state.rotate(theta, x, y); }
  public void scale(double sx, double sy) { state.scale(sx, sy); }
  public void shear(double shx, double shy) { state.shear(shx, shy); }
  public void transform(AffineTransform tx) { state.transform(tx); }
  public void setTransform(AffineTransform tx) { state.setTransform(tx); }
  public AffineTransform getTransform() { return state.getTransform(); }
  public Color getColor() { return state.getColor(); }
  public void setColor(Color c) { state.setColor(c); }
  public void setPaintMode() { state.setPaintMode(); }
  public void setXORMode(Color c1) { state.setXORMode(c1); }
  public Font getFont() { return state.getFont(); }
  public void setFont(Font font) { state.setFont(font); }
  public FontMetrics getFontMetrics(Font f) { return state.getFontMetrics(f); }
  public Rectangle getClipBounds() { return state.getClipBounds(); }
  public void clipRect(int x, int y, int w, int h) { state.clipRect(x, y, w, h); }
  public void setClip(int x, int y, int w, int h) { state.setClip(x, y, w, h); }
  public Shape getClip() { return state.getClip(); }
  public void setClip(Shape clip) { state.setClip(clip); }
  public void clip(Shape s) { state.clip(s); }
  public void copyArea(int x, int y, int w, int h, int dx, int dy) { }
  public GraphicsConfiguration getDeviceConfiguration() { return state.getDeviceConfiguration(); }
  public void setComposite(Composite comp) { state.setComposite(comp); }
  public Composite getComposite() { return state.getComposite(); }
  public void setPaint(Paint paint) { state.setPaint(paint); }
  public Paint getPaint() { return state.getPaint(); }
  public void setStroke(Stroke s) { state.setStroke(s); }
  public Stroke getStroke() { return state.getStroke(); }
  public void setRenderingHint(RenderingHints.Key k, Object v) { state.setRenderingHint(k, v); }
  public Object getRenderingHint(RenderingHints.Key k) { return state.getRenderingHint(k); }
  public void setRenderingHints(Map<?, ?> hints) { state.setRenderingHints(hints); }
  public void addRenderingHints(Map<?, ?> hints) { state.addRenderingHints(hints); }
  public RenderingHints getRenderingHints() { return state.getRenderingHints(); }
  public void setBackground(Color color) { state.setBackground(color); }
  public Color getBackground() { return state.getBackground(); }
  public FontRenderContext getFontRenderContext() { return state.getFontRenderContext(); }
  public boolean hit(Rectangle rect, Shape s, boolean onStroke) { return state.hit(rect, s, onStroke); }
  public void dispose() { state.dispose(); }
}
