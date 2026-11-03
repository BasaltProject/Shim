/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */
package sun.print;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ColorModel;
import java.awt.image.ImageObserver;
import java.awt.PaintContext;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.RenderableImage;
import java.awt.print.PrinterGraphics;
import java.awt.print.PrinterJob;
import java.io.IOException;
import java.text.AttributedCharacterIterator;
import shim.awt.ImageSupport;

final class PSPageGraphics extends ForwardingGraphics2D implements PrinterGraphics {
  private final PSDocument doc;
  private final PrinterJob job;

  PSPageGraphics(Graphics2D state, PSDocument doc, PrinterJob job) {
    super(state);
    this.doc = doc;
    this.job = job;
  }

  public PrinterJob getPrinterJob() {
    return job;
  }

  public Graphics create() {
    return new PSPageGraphics((Graphics2D) state.create(), doc, job);
  }

  private void out(String s) {
    try {
      doc.write(s);
    } catch (IOException e) {
      throw new PrintFailure(e);
    }
  }

  static final class PrintFailure extends RuntimeException {
    PrintFailure(IOException cause) {
      super(cause);
    }
  }

  private void path(Shape s, StringBuilder sb) {
    PathIterator it = s.getPathIterator(null);
    double[] c = new double[6];
    double cx = 0;
    double cy = 0;
    sb.append("n\n");
    while (!it.isDone()) {
      switch (it.currentSegment(c)) {
        case PathIterator.SEG_MOVETO:
          sb.append(PSDocument.num(c[0])).append(' ').append(PSDocument.num(c[1])).append(" m\n");
          cx = c[0];
          cy = c[1];
          break;
        case PathIterator.SEG_LINETO:
          sb.append(PSDocument.num(c[0])).append(' ').append(PSDocument.num(c[1])).append(" l\n");
          cx = c[0];
          cy = c[1];
          break;
        case PathIterator.SEG_QUADTO:
          double x1 = cx + 2.0 / 3.0 * (c[0] - cx);
          double y1 = cy + 2.0 / 3.0 * (c[1] - cy);
          double x2 = c[2] + 2.0 / 3.0 * (c[0] - c[2]);
          double y2 = c[3] + 2.0 / 3.0 * (c[1] - c[3]);
          sb.append(PSDocument.num(x1)).append(' ').append(PSDocument.num(y1)).append(' ').append(PSDocument.num(x2))
              .append(' ').append(PSDocument.num(y2)).append(' ').append(PSDocument.num(c[2])).append(' ')
              .append(PSDocument.num(c[3])).append(" c\n");
          cx = c[2];
          cy = c[3];
          break;
        case PathIterator.SEG_CUBICTO:
          for (int i = 0; i < 6; i++) {
            sb.append(PSDocument.num(c[i])).append(' ');
          }
          sb.append("c\n");
          cx = c[4];
          cy = c[5];
          break;
        default:
          sb.append("h\n");
      }
      it.next();
    }
  }

  private void clipPath(StringBuilder sb) {
    Shape clip = state.getClip();
    if (clip != null) {
      Shape device = state.getTransform().createTransformedShape(clip);
      path(device, sb);
      sb.append(isEvenOdd(device) ? "eoclip n\n" : "clip n\n");
    }
  }

  private static boolean isEvenOdd(Shape s) {
    return s.getPathIterator(null).getWindingRule() == PathIterator.WIND_EVEN_ODD;
  }

  private Color colorFor(Shape device) {
    Paint p = state.getPaint();
    if (p instanceof Color) {
      return (Color) p;
    }
    Rectangle bounds = device.getBounds();
    if (bounds.isEmpty()) {
      bounds = new Rectangle(bounds.x, bounds.y, 1, 1);
    }
    try {
      PaintContext ctx = p.createContext(ColorModel.getRGBdefault(), bounds, bounds, new AffineTransform(),
          state.getRenderingHints());
      Raster r = ctx.getRaster(bounds.x + bounds.width / 2, bounds.y + bounds.height / 2, 1, 1);
      Color c = new Color(ctx.getColorModel().getRGB(r.getDataElements(0, 0, null)), true);
      ctx.dispose();
      return c;
    } catch (RuntimeException e) {
      return state.getColor();
    }
  }

  private void paintDevice(Shape device) {
    if (doc == null) {
      return;
    }
    Rectangle2D b = device.getBounds2D();
    if (b.isEmpty() && !(device instanceof Path2D)) {
      if (b.getWidth() == 0 && b.getHeight() == 0) {
        return;
      }
    }
    Color c = colorFor(device);
    StringBuilder sb = new StringBuilder(256);
    sb.append("q\n");
    clipPath(sb);
    sb.append(PSDocument.num(c.getRed() / 255.0)).append(' ').append(PSDocument.num(c.getGreen() / 255.0)).append(' ')
        .append(PSDocument.num(c.getBlue() / 255.0)).append(" rg\n");
    path(device, sb);
    sb.append(isEvenOdd(device) ? "e\n" : "f\n").append("Q\n");
    out(sb.toString());
  }

  public void fill(Shape s) {
    paintDevice(state.getTransform().createTransformedShape(s));
  }

  public void draw(Shape s) {
    Shape stroked = state.getStroke().createStrokedShape(s);
    paintDevice(state.getTransform().createTransformedShape(stroked));
  }

  public void drawString(String str, int x, int y) {
    drawString(str, (float) x, (float) y);
  }

  public void drawString(String str, float x, float y) {
    if (str == null) {
      throw new NullPointerException("String is null");
    }
    if (str.isEmpty()) {
      return;
    }
    GlyphVector gv = getFont().createGlyphVector(getFontRenderContext(), str);
    fill(gv.getOutline(x, y));
  }

  public void drawString(AttributedCharacterIterator iterator, int x, int y) {
    drawString(iterator, (float) x, (float) y);
  }

  public void drawString(AttributedCharacterIterator iterator, float x, float y) {
    StringBuilder sb = new StringBuilder();
    for (char ch = iterator.first(); ch != AttributedCharacterIterator.DONE; ch = iterator.next()) {
      sb.append(ch);
    }
    drawString(sb.toString(), x, y);
  }

  public void drawGlyphVector(GlyphVector g, float x, float y) {
    fill(g.getOutline(x, y));
  }

  public void drawLine(int x1, int y1, int x2, int y2) {
    draw(new Line2D.Float(x1, y1, x2, y2));
  }

  public void fillRect(int x, int y, int w, int h) {
    fill(new Rectangle(x, y, w, h));
  }

  public void clearRect(int x, int y, int w, int h) {
    Paint p = state.getPaint();
    state.setColor(state.getBackground());
    fill(new Rectangle(x, y, w, h));
    state.setPaint(p);
  }

  public void drawRoundRect(int x, int y, int w, int h, int aw, int ah) {
    draw(new RoundRectangle2D.Float(x, y, w, h, aw, ah));
  }

  public void fillRoundRect(int x, int y, int w, int h, int aw, int ah) {
    fill(new RoundRectangle2D.Float(x, y, w, h, aw, ah));
  }

  public void drawOval(int x, int y, int w, int h) {
    draw(new Ellipse2D.Float(x, y, w, h));
  }

  public void fillOval(int x, int y, int w, int h) {
    fill(new Ellipse2D.Float(x, y, w, h));
  }

  public void drawArc(int x, int y, int w, int h, int start, int extent) {
    draw(new Arc2D.Float(x, y, w, h, start, extent, Arc2D.OPEN));
  }

  public void fillArc(int x, int y, int w, int h, int start, int extent) {
    fill(new Arc2D.Float(x, y, w, h, start, extent, Arc2D.PIE));
  }

  public void drawPolyline(int[] xs, int[] ys, int n) {
    Path2D.Float p = new Path2D.Float();
    for (int i = 0; i < n; i++) {
      if (i == 0) {
        p.moveTo(xs[i], ys[i]);
      } else {
        p.lineTo(xs[i], ys[i]);
      }
    }
    draw(p);
  }

  public void drawPolygon(int[] xs, int[] ys, int n) {
    draw(new java.awt.Polygon(xs, ys, n));
  }

  public void fillPolygon(int[] xs, int[] ys, int n) {
    fill(new java.awt.Polygon(xs, ys, n));
  }

  private void paintImage(BufferedImage img, AffineTransform userToImage, Color bg) {
    if (doc == null || img == null) {
      return;
    }
    int w = img.getWidth();
    int h = img.getHeight();
    AffineTransform t = state.getTransform();
    t.concatenate(userToImage);
    double[] m = new double[6];
    t.getMatrix(m);
    StringBuilder sb = new StringBuilder(w * h * 6 + 512);
    sb.append("q\n");
    clipPath(sb);
    sb.append('[');
    for (int i = 0; i < 6; i++) {
      sb.append(i == 0 ? "" : " ").append(PSDocument.num(m[i]));
    }
    sb.append("] concat\n").append(w).append(' ').append(h).append(" scale\n");
    sb.append("/DeviceRGB setcolorspace\n<< /ImageType 1 /Width ").append(w).append(" /Height ").append(h)
        .append(" /BitsPerComponent 8 /Decode [0 1 0 1 0 1] /ImageMatrix [").append(w).append(" 0 0 ").append(h)
        .append(" 0 0] /DataSource currentfile /ASCIIHexDecode filter >> image\n");
    int bgRgb = bg == null ? 0xFFFFFF : bg.getRGB();
    char[] hex = "0123456789abcdef".toCharArray();
    int[] row = new int[w];
    for (int y = 0; y < h; y++) {
      img.getRGB(0, y, w, 1, row, 0, w);
      for (int x = 0; x < w; x++) {
        int p = row[x];
        int a = p >>> 24;
        int r = blend((p >> 16) & 255, (bgRgb >> 16) & 255, a);
        int g = blend((p >> 8) & 255, (bgRgb >> 8) & 255, a);
        int b = blend(p & 255, bgRgb & 255, a);
        sb.append(hex[r >> 4]).append(hex[r & 15]).append(hex[g >> 4]).append(hex[g & 15]).append(hex[b >> 4])
            .append(hex[b & 15]);
      }
      sb.append('\n');
    }
    sb.append(">\nQ\n");
    out(sb.toString());
  }

  private static int blend(int src, int dst, int alpha) {
    return (src * alpha + dst * (255 - alpha) + 127) / 255;
  }

  public boolean drawImage(Image img, AffineTransform xform, ImageObserver obs) {
    if (img == null) {
      return true;
    }
    paintImage(ImageSupport.toBuffered(img), xform == null ? new AffineTransform() : xform, null);
    return true;
  }

  public void drawImage(BufferedImage img, BufferedImageOp op, int x, int y) {
    if (img == null) {
      return;
    }
    BufferedImage src = op == null ? img : op.filter(img, null);
    paintImage(src, AffineTransform.getTranslateInstance(x, y), null);
  }

  public void drawRenderedImage(RenderedImage img, AffineTransform xform) {
    if (img != null) {
      paintImage(ImageSupport.toBuffered(img), xform == null ? new AffineTransform() : xform, null);
    }
  }

  public void drawRenderableImage(RenderableImage img, AffineTransform xform) {
    if (img != null) {
      drawRenderedImage(img.createDefaultRendering(), xform);
    }
  }

  public boolean drawImage(Image img, int x, int y, ImageObserver o) {
    return drawImage(img, x, y, null, o);
  }

  public boolean drawImage(Image img, int x, int y, int w, int h, ImageObserver o) {
    return drawImage(img, x, y, w, h, null, o);
  }

  public boolean drawImage(Image img, int x, int y, Color bg, ImageObserver o) {
    if (img == null) {
      return true;
    }
    paintImage(ImageSupport.toBuffered(img), AffineTransform.getTranslateInstance(x, y), bg);
    return true;
  }

  public boolean drawImage(Image img, int x, int y, int w, int h, Color bg, ImageObserver o) {
    if (img == null) {
      return true;
    }
    BufferedImage b = ImageSupport.toBuffered(img);
    if (w == 0 || h == 0 || b.getWidth() == 0 || b.getHeight() == 0) {
      return true;
    }
    AffineTransform t = AffineTransform.getTranslateInstance(x, y);
    t.scale((double) w / b.getWidth(), (double) h / b.getHeight());
    paintImage(b, t, bg);
    return true;
  }

  public boolean drawImage(Image img, int dx1, int dy1, int dx2, int dy2, int sx1, int sy1, int sx2, int sy2,
      ImageObserver o) {
    return drawImage(img, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, null, o);
  }

  public boolean drawImage(Image img, int dx1, int dy1, int dx2, int dy2, int sx1, int sy1, int sx2, int sy2,
      Color bg, ImageObserver o) {
    if (img == null) {
      return true;
    }
    BufferedImage b = ImageSupport.toBuffered(img);
    int sw = sx2 - sx1;
    int sh = sy2 - sy1;
    if (sw == 0 || sh == 0) {
      return true;
    }
    int x = Math.min(sx1, sx2);
    int y = Math.min(sy1, sy2);
    int cw = Math.abs(sw);
    int ch = Math.abs(sh);
    x = Math.max(0, x);
    y = Math.max(0, y);
    cw = Math.min(cw, b.getWidth() - x);
    ch = Math.min(ch, b.getHeight() - y);
    if (cw <= 0 || ch <= 0) {
      return true;
    }
    BufferedImage sub = b.getSubimage(x, y, cw, ch);
    AffineTransform t = AffineTransform.getTranslateInstance(dx1, dy1);
    t.scale((double) (dx2 - dx1) / sw, (double) (dy2 - dy1) / sh);
    if (sw < 0) {
      t.translate(cw, 0);
      t.scale(-1, 1);
    }
    if (sh < 0) {
      t.translate(0, ch);
      t.scale(1, -1);
    }
    paintImage(sub, t, bg);
    return true;
  }
}
