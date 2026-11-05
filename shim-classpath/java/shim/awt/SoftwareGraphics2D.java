/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.awt;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.CompositeContext;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.Paint;
import java.awt.PaintContext;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ColorModel;
import java.awt.image.DataBufferInt;
import java.awt.image.ImageObserver;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.awt.image.SinglePixelPackedSampleModel;
import java.awt.image.WritableRaster;
import java.awt.image.renderable.RenderableImage;
import java.text.AttributedCharacterIterator;
import java.util.Map;

public final class SoftwareGraphics2D extends Graphics2D {
  private final BufferedImage image;
  private final int width;
  private final int height;
  private final int[] fast;
  private final int fastBase;
  private final int fastStride;
  private final boolean fastAlpha;

  private AffineTransform tx = new AffineTransform();
  private Shape devClip;
  private Rectangle clipBounds;
  private byte[] clipMask;
  private Composite composite = AlphaComposite.SrcOver;
  private Paint paint = Color.WHITE;
  private Color color = Color.WHITE;
  private Stroke stroke = new BasicStroke();
  private Font font = new Font(Font.DIALOG, Font.PLAIN, 12);
  private RenderingHints hints = new RenderingHints(null);
  private Color background = Color.BLACK;
  private Color xorColor;
  private final boolean copyOfParent;

  public SoftwareGraphics2D(BufferedImage image) {
    this.image = image;
    this.width = image.getWidth();
    this.height = image.getHeight();
    this.copyOfParent = false;
    int type = image.getType();
    int[] buf = null;
    int base = 0;
    int stride = 0;
    boolean alpha = false;
    DataBufferInt db = null;
    SinglePixelPackedSampleModel sm = null;
    if ((type == BufferedImage.TYPE_INT_ARGB || type == BufferedImage.TYPE_INT_RGB)
        && ((db = image.getRaster().getDataBuffer() instanceof DataBufferInt ? (DataBufferInt) image.getRaster().getDataBuffer() : null) != null)
        && ((sm = image.getRaster().getSampleModel() instanceof SinglePixelPackedSampleModel ? (SinglePixelPackedSampleModel) image.getRaster().getSampleModel() : null) != null)) {
      buf = db.getData();
      WritableRaster r = image.getRaster();
      base = db.getOffset() - r.getSampleModelTranslateY() * sm.getScanlineStride() - r.getSampleModelTranslateX();
      stride = sm.getScanlineStride();
      alpha = type == BufferedImage.TYPE_INT_ARGB;
    }
    this.fast = buf;
    this.fastBase = base;
    this.fastStride = stride;
    this.fastAlpha = alpha;
    this.clipBounds = new Rectangle(0, 0, width, height);
    hints.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
    hints.put(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_DEFAULT);
    hints.put(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_OFF);
    hints.put(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
    hints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_DEFAULT);
    hints.put(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_DEFAULT);
    hints.put(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_DEFAULT);
    hints.put(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_DEFAULT);
    hints.put(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_DEFAULT);
  }

  private SoftwareGraphics2D(SoftwareGraphics2D o) {
    this.image = o.image;
    this.width = o.width;
    this.height = o.height;
    this.fast = o.fast;
    this.fastBase = o.fastBase;
    this.fastStride = o.fastStride;
    this.fastAlpha = o.fastAlpha;
    this.copyOfParent = true;
    this.tx = new AffineTransform(o.tx);
    this.devClip = o.devClip;
    this.clipBounds = new Rectangle(o.clipBounds);
    this.clipMask = o.clipMask;
    this.composite = o.composite;
    this.paint = o.paint;
    this.color = o.color;
    this.stroke = o.stroke;
    this.font = o.font;
    this.hints = (RenderingHints) o.hints.clone();
    this.background = o.background;
    this.xorColor = o.xorColor;
  }

  public Graphics create() {
    return new SoftwareGraphics2D(this);
  }

  public void dispose() { }

  private boolean aa() {
    return hints.get(RenderingHints.KEY_ANTIALIASING) == RenderingHints.VALUE_ANTIALIAS_ON;
  }

  private boolean textAa() {
    Object t = hints.get(RenderingHints.KEY_TEXT_ANTIALIASING);
    if (t == RenderingHints.VALUE_TEXT_ANTIALIAS_DEFAULT) {
      return aa();
    }
    return t != RenderingHints.VALUE_TEXT_ANTIALIAS_OFF;
  }

  private int getPixel(int x, int y) {
    if (fast != null) {
      int v = fast[fastBase + y * fastStride + x];
      return fastAlpha ? v : v | 0xff000000;
    }
    int type = image.getType();
    if (type == BufferedImage.TYPE_BYTE_GRAY || type == BufferedImage.TYPE_USHORT_GRAY) {
      int g = image.getRaster().getSample(x, y, 0);
      g = type == BufferedImage.TYPE_BYTE_GRAY ? g : (g * 255 + 32895) / 65535;
      return 0xff000000 | g << 16 | g << 8 | g;
    }
    return image.getRGB(x, y);
  }

  private void setPixel(int x, int y, int argb) {
    if (fast != null) {
      fast[fastBase + y * fastStride + x] = fastAlpha ? argb : argb & 0x00ffffff;
      return;
    }
    int type = image.getType();
    if (type == BufferedImage.TYPE_BYTE_GRAY || type == BufferedImage.TYPE_USHORT_GRAY) {
      int r = argb >> 16 & 0xff;
      int gr = argb >> 8 & 0xff;
      int b = argb & 0xff;
      image.getRaster().setSample(x, y, 0, type == BufferedImage.TYPE_BYTE_GRAY ? (19672 * r + 38620 * gr + 7500 * b) >> 16
          : (19595 * r + 38470 * gr + 7471 * b) * 257 + 32768 >> 16);
      return;
    }
    image.setRGB(x, y, argb);
  }

  public Color getColor() { return color; }

  public void setColor(Color c) {
    if (c != null) {
      color = c;
      paint = c;
    }
  }

  public void setPaintMode() { xorColor = null; }

  public void setXORMode(Color c1) {
    if (c1 == null) {
      throw new IllegalArgumentException("null XORColor");
    }
    xorColor = c1;
  }

  public Font getFont() { return font; }

  public void setFont(Font f) {
    if (f != null) {
      font = f;
    }
  }

  public FontMetrics getFontMetrics(Font f) {
    return new SimpleFontMetrics(f, getFontRenderContext());
  }

  public FontRenderContext getFontRenderContext() {
    Object aaHint = hints.get(RenderingHints.KEY_TEXT_ANTIALIASING);
    Object fmHint = hints.get(RenderingHints.KEY_FRACTIONALMETRICS);
    return new FontRenderContext(tx.isIdentity() ? null : tx, aaHint, fmHint);
  }

  public Paint getPaint() { return paint; }

  public void setPaint(Paint p) {
    if (p == null) {
      return;
    }
    paint = p;
    Color c = null;
    if (((c = p instanceof Color ? (Color) p : null) != null)) {
      color = c;
    }
  }

  public Composite getComposite() { return composite; }

  public void setComposite(Composite comp) {
    if (comp == null) {
      throw new IllegalArgumentException("null Composite");
    }
    composite = comp;
  }

  public Stroke getStroke() { return stroke; }

  public void setStroke(Stroke s) {
    if (s == null) {
      throw new IllegalArgumentException("null Stroke");
    }
    stroke = s;
  }

  public Color getBackground() { return background; }

  public void setBackground(Color c) { background = c; }

  public void setRenderingHint(RenderingHints.Key key, Object value) {
    if (value == null) {
      hints.remove(key);
      return;
    }
    hints.put(key, value);
  }

  public Object getRenderingHint(RenderingHints.Key key) { return hints.get(key); }

  public void setRenderingHints(Map<?, ?> m) {
    hints = new RenderingHints(null);
    hints.putAll(m);
  }

  public void addRenderingHints(Map<?, ?> m) { hints.putAll(m); }

  public RenderingHints getRenderingHints() { return (RenderingHints) hints.clone(); }

  public GraphicsConfiguration getDeviceConfiguration() { return new ImageGraphicsConfiguration(image); }

  public AffineTransform getTransform() { return new AffineTransform(tx); }

  public void setTransform(AffineTransform t) { tx = new AffineTransform(t); }

  public void transform(AffineTransform t) { tx.concatenate(t); }

  public void translate(int x, int y) { tx.translate(x, y); }

  public void translate(double x, double y) { tx.translate(x, y); }

  public void rotate(double theta) { tx.rotate(theta); }

  public void rotate(double theta, double x, double y) { tx.rotate(theta, x, y); }

  public void scale(double sx, double sy) { tx.scale(sx, sy); }

  public void shear(double shx, double shy) { tx.shear(shx, shy); }

  private void setDevClip(Shape s) {
    devClip = s;
    clipMask = null;
    Rectangle b = s == null ? new Rectangle(0, 0, width, height) : s.getBounds();
    Rectangle2D r2 = null;
    if (((r2 = s instanceof Rectangle2D ? (Rectangle2D) s : null) != null) && !(s instanceof Rectangle)) {
      b = new Rectangle((int) Math.floor(r2.getMinX() + 0.5), (int) Math.floor(r2.getMinY() + 0.5), 0, 0);
      int x1 = (int) Math.floor(r2.getMaxX() + 0.5);
      int y1 = (int) Math.floor(r2.getMaxY() + 0.5);
      b.width = x1 - b.x;
      b.height = y1 - b.y;
      devClip = new Rectangle(b);
    }
    clipBounds = b.intersection(new Rectangle(0, 0, width, height));
    if (clipBounds.width < 0 || clipBounds.height < 0) {
      clipBounds = new Rectangle(0, 0, 0, 0);
    }
  }

  private byte[] mask() {
    if (devClip == null || devClip instanceof Rectangle) {
      return null;
    }
    if (clipMask == null) {
      byte[] m = new byte[width * height];
      Rasterizer.fill(devClip, null, 0, 0, width, height, false, (y, x0, x1, cov, base) -> {
        for (int x = x0; x < x1; x++) {
          if (cov[x - base] > 0f) {
            m[y * width + x] = (byte) 255;
          }
        }
      });
      clipMask = m;
    }
    return clipMask;
  }

  private boolean cornerTransform() {
    int t = tx.getType();
    return (t & ~(AffineTransform.TYPE_TRANSLATION | AffineTransform.TYPE_MASK_SCALE)) == 0;
  }

  public Shape getClip() {
    if (devClip == null) {
      return null;
    }
    try {
      Rectangle r = null;
      if (((r = devClip instanceof Rectangle ? (Rectangle) devClip : null) != null) && tx.getType() <= AffineTransform.TYPE_TRANSLATION) {
        return new Rectangle(r.x - (int) tx.getTranslateX(), r.y - (int) tx.getTranslateY(), r.width, r.height);
      }
      AffineTransform inv = tx.createInverse();
      Rectangle2D r22 = null;
      if (((r22 = devClip instanceof Rectangle2D ? (Rectangle2D) devClip : null) != null) && cornerTransform()) {
        return inv.createTransformedShape(r22).getBounds2D();
      }
      return new Path2D.Double(devClip, inv);
    } catch (NoninvertibleTransformException e) {
      return null;
    }
  }

  public Rectangle getClipBounds() {
    Shape c = getClip();
    return c == null ? null : c.getBounds();
  }

  public void setClip(Shape s) {
    if (s == null) {
      setDevClip(null);
      return;
    }
    Rectangle r2 = null;
    if (((r2 = s instanceof Rectangle ? (Rectangle) s : null) != null) && tx.getType() <= AffineTransform.TYPE_TRANSLATION) {
      setDevClip(new Rectangle(r2.x + (int) tx.getTranslateX(), r2.y + (int) tx.getTranslateY(), r2.width, r2.height));
      return;
    }
    Rectangle2D r23 = null;
    if (((r23 = s instanceof Rectangle2D ? (Rectangle2D) s : null) != null) && cornerTransform()) {
      setDevClip(tx.createTransformedShape(r23).getBounds2D());
      return;
    }
    setDevClip(new Path2D.Double(s, tx));
  }

  public void setClip(int x, int y, int w, int h) { setClip(new Rectangle(x, y, w, h)); }

  public void clipRect(int x, int y, int w, int h) { clip(new Rectangle(x, y, w, h)); }

  public void clip(Shape s) {
    if (s == null) {
      setDevClip(null);
      return;
    }
    Shape dev;
    Rectangle r3 = null;
    Rectangle2D r24 = null;
    if (((r3 = s instanceof Rectangle ? (Rectangle) s : null) != null) && tx.getType() <= AffineTransform.TYPE_TRANSLATION) {
      dev = new Rectangle(r3.x + (int) tx.getTranslateX(), r3.y + (int) tx.getTranslateY(), r3.width, r3.height);
    } else if (((r24 = s instanceof Rectangle2D ? (Rectangle2D) s : null) != null) && cornerTransform()) {
      Rectangle2D b = tx.createTransformedShape(r24).getBounds2D();
      dev = new Rectangle((int) Math.floor(b.getMinX() + 0.5), (int) Math.floor(b.getMinY() + 0.5),
          (int) Math.floor(b.getMaxX() + 0.5) - (int) Math.floor(b.getMinX() + 0.5),
          (int) Math.floor(b.getMaxY() + 0.5) - (int) Math.floor(b.getMinY() + 0.5));
    } else {
      dev = new Path2D.Double(s, tx);
    }
    Rectangle a = null;
    Rectangle b = null;
    if (devClip == null) {
      setDevClip(dev);
    } else if (((a = devClip instanceof Rectangle ? (Rectangle) devClip : null) != null) && ((b = dev instanceof Rectangle ? (Rectangle) dev : null) != null)) {
      setDevClip(a.intersection(b));
    } else {
      java.awt.geom.Area area = new java.awt.geom.Area(devClip);
      area.intersect(new java.awt.geom.Area(dev));
      setDevClip(area);
    }
  }

  public boolean hitClip(int x, int y, int w, int h) {
    Rectangle r = tx.createTransformedShape(new Rectangle(x, y, w, h)).getBounds();
    return clipBounds.intersects(r);
  }

  private final class Fill implements Rasterizer.RowSink {
    private final int srcColor;
    private final PaintContext pctx;
    private final CompositeContext cctx;
    private final int rule;
    private final float extra;
    private final byte[] mask;
    private Raster pr;

    Fill(Paint p, byte[] mask, Rectangle devBounds, Rectangle2D userBounds) {
      this.mask = mask;
      AlphaComposite ac = null;
      if (((ac = composite instanceof AlphaComposite ? (AlphaComposite) composite : null) != null)) {
        rule = ac.getRule();
        extra = ac.getAlpha();
        cctx = null;
      } else {
        rule = -1;
        extra = 1f;
        cctx = composite.createContext(ColorModel.getRGBdefault(), ColorModel.getRGBdefault(), hints);
      }
      Color c2 = null;
      if (((c2 = p instanceof Color ? (Color) p : null) != null)) {
        srcColor = c2.getRGB();
        pctx = null;
      } else {
        srcColor = 0;
        pctx = p.createContext(ColorModel.getRGBdefault(), devBounds, userBounds, tx, hints);
      }
    }

    public void row(int y, int xs, int xe, float[] cov, int base) {
      int[] srcRow = null;
      if (pctx != null) {
        Raster r = pctx.getRaster(xs, y, xe - xs, 1);
        ColorModel cm = pctx.getColorModel();
        srcRow = new int[xe - xs];
        Object px = null;
        for (int i = 0; i < srcRow.length; i++) {
          px = r.getDataElements(r.getMinX() + i, r.getMinY(), px);
          srcRow[i] = cm.getRGB(px);
        }
      }
      for (int x = xs; x < xe; x++) {
        float c = cov[x - base];
        if (c <= 0f) {
          continue;
        }
        int cv = (int) (c * 255f + 0.5f);
        if (cv > 255) {
          cv = 255;
        }
        if (mask != null) {
          if (mask[y * width + x] == 0) {
            continue;
          }
        }
        int src = srcRow != null ? srcRow[x - xs] : srcColor;
        int dst = getPixel(x, y);
        if (xorColor != null) {
          int mix = (src ^ xorColor.getRGB()) & 0x00ffffff;
          setPixel(x, y, (dst ^ mix) | 0xff000000);
          continue;
        }
        int out;
        if (rule == AlphaComposite.SRC_OVER) {
          out = Blend.srcOver(src, dst, extra == 1f ? cv : (int) (cv * extra + 0.5f));
        } else if (rule >= 0) {
          int res = Blend.compose(rule, src, dst, extra);
          out = cv == 255 ? res : lerp(dst, res, cv);
        } else {
          out = custom(src, dst, cv);
        }
        setPixel(x, y, out);
      }
    }

    private int custom(int src, int dst, int cv) {
      WritableRaster s = ColorModel.getRGBdefault().createCompatibleWritableRaster(1, 1);
      WritableRaster d = ColorModel.getRGBdefault().createCompatibleWritableRaster(1, 1);
      WritableRaster o = ColorModel.getRGBdefault().createCompatibleWritableRaster(1, 1);
      s.setDataElements(0, 0, new int[] {src});
      d.setDataElements(0, 0, new int[] {dst});
      cctx.compose(s, d, o);
      int res = ((int[]) o.getDataElements(0, 0, null))[0];
      return cv == 255 ? res : lerp(dst, res, cv);
    }
  }

  private static int lerp(int a, int b, int t) {
    int u = 255 - t;
    int aa = (((a >>> 24) * u + (b >>> 24) * t) + 127) / 255;
    int ar = ((((a >> 16) & 0xff) * u + ((b >> 16) & 0xff) * t) + 127) / 255;
    int ag = ((((a >> 8) & 0xff) * u + ((b >> 8) & 0xff) * t) + 127) / 255;
    int ab = (((a & 0xff) * u + (b & 0xff) * t) + 127) / 255;
    return (aa << 24) | (ar << 16) | (ag << 8) | ab;
  }

  private void fillDevice(Shape shape, AffineTransform at, boolean antialias, Paint p, Rectangle2D userBounds) {
    if (clipBounds.isEmpty()) {
      return;
    }
    Fill sink = new Fill(p, mask(), clipBounds, userBounds);
    Rasterizer.fill(shape, at, clipBounds.x, clipBounds.y, clipBounds.x + clipBounds.width,
        clipBounds.y + clipBounds.height, antialias, sink);
    if (sink.pctx != null) {
      sink.pctx.dispose();
    }
  }

  public void fill(Shape s) {
    if (!aa() && stroke instanceof BasicStroke
        && hints.get(RenderingHints.KEY_STROKE_CONTROL) != RenderingHints.VALUE_STROKE_PURE) {
      fillDevice(Rasterizer.adjusted(s, tx), null, false, paint, s.getBounds2D());
      return;
    }
    fillDevice(s, tx, aa(), paint, s.getBounds2D());
  }

  private double devWidth(Stroke st) {
    BasicStroke bs = null;
    if (!(((bs = st instanceof BasicStroke ? (BasicStroke) st : null) != null))) {
      return 2;
    }
    double sx = Math.sqrt(Math.abs(tx.getDeterminant()));
    return bs.getLineWidth() * sx;
  }

  public void draw(Shape s) {
    Shape stroked = stroke.createStrokedShape(s);
    AffineTransform at = tx;
    if (!aa() && cornerTransform() && stroke instanceof BasicStroke) {
      long w = Math.round(devWidth(stroke));
      if (w <= 1 || (w & 1) == 1) {
        at = new AffineTransform(tx);
        at.preConcatenate(AffineTransform.getTranslateInstance(0.5, 0.5));
      }
    }
    fillDevice(stroked, at, aa(), paint, s.getBounds2D());
  }

  public boolean hit(Rectangle rect, Shape s, boolean onStroke) {
    if (onStroke) {
      s = stroke.createStrokedShape(s);
    }
    s = tx.createTransformedShape(s);
    return s.intersects(rect);
  }

  public void clearRect(int x, int y, int w, int h) {
    Composite c = composite;
    Paint p = paint;
    composite = AlphaComposite.Src;
    paint = background;
    fill(new Rectangle(x, y, w, h));
    composite = c;
    paint = p;
  }

  public void fillRect(int x, int y, int w, int h) {
    if (w <= 0 || h <= 0) {
      return;
    }
    fill(new Rectangle(x, y, w, h));
  }

  private boolean thin() {
    BasicStroke bs2 = null;
    return !aa() && ((bs2 = stroke instanceof BasicStroke ? (BasicStroke) stroke : null) != null) && bs2.getLineWidth() <= 1f && bs2.getDashArray() == null
        && cornerTransform() && Math.abs(tx.getScaleX()) == 1 && Math.abs(tx.getScaleY()) == 1
        && composite instanceof AlphaComposite && paint instanceof Color;
  }

  private void bresenham(int x0, int y0, int x1, int y1) {
    int dx = Math.abs(x1 - x0);
    int dy = Math.abs(y1 - y0);
    int sx = x0 < x1 ? 1 : -1;
    int sy = y0 < y1 ? 1 : -1;
    int err = dx - dy;
    byte[] m = mask();
    int rule = ((AlphaComposite) composite).getRule();
    float extra = ((AlphaComposite) composite).getAlpha();
    int src = ((Color) paint).getRGB();
    while (true) {
      if (x0 >= clipBounds.x && x0 < clipBounds.x + clipBounds.width && y0 >= clipBounds.y
          && y0 < clipBounds.y + clipBounds.height && (m == null || m[y0 * width + x0] != 0)) {
        int dst = getPixel(x0, y0);
        if (xorColor != null) {
          setPixel(x0, y0, (dst ^ ((src ^ xorColor.getRGB()) & 0x00ffffff)) | 0xff000000);
        } else if (rule == AlphaComposite.SRC_OVER) {
          setPixel(x0, y0, Blend.srcOver(src, dst, (int) (255 * extra + 0.5f)));
        } else {
          setPixel(x0, y0, Blend.compose(rule, src, dst, extra));
        }
      }
      if (x0 == x1 && y0 == y1) {
        break;
      }
      int e2 = 2 * err;
      if (e2 > -dy) {
        err -= dy;
        x0 += sx;
      }
      if (e2 < dx) {
        err += dx;
        y0 += sy;
      }
    }
  }

  public void drawLine(int x1, int y1, int x2, int y2) {
    if (thin()) {
      double[] p = {x1, y1, x2, y2};
      tx.transform(p, 0, p, 0, 2);
      bresenham((int) Math.round(p[0]), (int) Math.round(p[1]), (int) Math.round(p[2]), (int) Math.round(p[3]));
      return;
    }
    draw(new java.awt.geom.Line2D.Float(x1, y1, x2, y2));
  }

  public void drawRect(int x, int y, int w, int h) {
    if (w < 0 || h < 0) {
      return;
    }
    if (w == 0 || h == 0) {
      drawLine(x, y, x + w, y + h);
      return;
    }
    draw(new Rectangle(x, y, w, h));
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

  public void drawArc(int x, int y, int w, int h, int start, int arc) {
    draw(new Arc2D.Float(x, y, w, h, start, arc, Arc2D.OPEN));
  }

  public void fillArc(int x, int y, int w, int h, int start, int arc) {
    fill(new Arc2D.Float(x, y, w, h, start, arc, Arc2D.PIE));
  }

  public void drawPolyline(int[] xs, int[] ys, int n) {
    if (n < 2) {
      return;
    }
    if (thin()) {
      for (int i = 0; i + 1 < n; i++) {
        drawLine(xs[i], ys[i], xs[i + 1], ys[i + 1]);
      }
      return;
    }
    Path2D.Float p = new Path2D.Float();
    p.moveTo(xs[0], ys[0]);
    for (int i = 1; i < n; i++) {
      p.lineTo(xs[i], ys[i]);
    }
    draw(p);
  }

  public void drawPolygon(int[] xs, int[] ys, int n) {
    if (n < 1) {
      return;
    }
    draw(new Polygon(xs, ys, n));
  }

  public void fillPolygon(int[] xs, int[] ys, int n) {
    if (n < 1) {
      return;
    }
    fill(new Polygon(xs, ys, n));
  }

  private void drawTextShape(Shape s) {
    fillDevice(s, tx, textAa(), paint, s.getBounds2D());
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
    GlyphVector gv = font.createGlyphVector(getFontRenderContext(), str);
    drawTextShape(gv.getOutline(x, y));
  }

  public void drawString(AttributedCharacterIterator it, int x, int y) {
    drawString(it, (float) x, (float) y);
  }

  public void drawString(AttributedCharacterIterator it, float x, float y) {
    if (it == null) {
      throw new NullPointerException("AttributedCharacterIterator is null");
    }
    StringBuilder sb = new StringBuilder();
    for (char c = it.first(); c != java.text.CharacterIterator.DONE; c = it.next()) {
      sb.append(c);
    }
    Font saved = font;
    Object f = it.getAttribute(java.awt.font.TextAttribute.FONT);
    Font ff = null;
    if (((ff = f instanceof Font ? (Font) f : null) != null)) {
      font = ff;
    }
    drawString(sb.toString(), x, y);
    font = saved;
  }

  public void drawGlyphVector(GlyphVector g, float x, float y) {
    drawTextShape(g.getOutline(x, y));
  }

  public void copyArea(int x, int y, int w, int h, int dx, int dy) {
    double[] p = {x, y, x + dx, y + dy};
    tx.transform(p, 0, p, 0, 2);
    int sx = (int) Math.round(p[0]);
    int sy = (int) Math.round(p[1]);
    int tx2 = (int) Math.round(p[2]);
    int ty2 = (int) Math.round(p[3]);
    Rectangle src = new Rectangle(sx, sy, w, h).intersection(new Rectangle(0, 0, width, height));
    if (src.isEmpty()) {
      return;
    }
    int[] buf = image.getRGB(src.x, src.y, src.width, src.height, null, 0, src.width);
    int ox = tx2 - sx;
    int oy = ty2 - sy;
    byte[] m = mask();
    for (int j = 0; j < src.height; j++) {
      for (int i = 0; i < src.width; i++) {
        int px = src.x + i + ox;
        int py = src.y + j + oy;
        if (px >= clipBounds.x && px < clipBounds.x + clipBounds.width && py >= clipBounds.y
            && py < clipBounds.y + clipBounds.height && (m == null || m[py * width + px] != 0)) {
          setPixel(px, py, buf[j * src.width + i]);
        }
      }
    }
  }

  public boolean drawImage(Image img, AffineTransform xform, ImageObserver obs) {
    if (img == null) {
      return true;
    }
    BufferedImage src = ImageSupport.toBuffered(img);
    AffineTransform t = xform == null ? new AffineTransform() : xform;
    paintImage(src, 0, 0, src.getWidth(), src.getHeight(), t, null);
    return true;
  }

  public void drawImage(BufferedImage img, BufferedImageOp op, int x, int y) {
    if (img == null) {
      return;
    }
    BufferedImage filtered = op == null ? img : op.filter(img, null);
    paintImage(filtered, 0, 0, filtered.getWidth(), filtered.getHeight(), AffineTransform.getTranslateInstance(x, y),
        null);
  }

  public void drawRenderedImage(RenderedImage img, AffineTransform xform) {
    if (img == null) {
      return;
    }
    BufferedImage src = ImageSupport.toBuffered(img);
    paintImage(src, 0, 0, src.getWidth(), src.getHeight(), xform == null ? new AffineTransform() : xform, null);
  }

  public void drawRenderableImage(RenderableImage img, AffineTransform xform) {
    if (img == null) {
      return;
    }
    RenderedImage r = img.createDefaultRendering();
    AffineTransform t = new AffineTransform();
    if (xform != null) {
      t.concatenate(xform);
    }
    drawRenderedImage(r, t);
  }

  public boolean drawImage(Image img, int x, int y, ImageObserver o) {
    return drawImage(img, x, y, null, o);
  }

  public boolean drawImage(Image img, int x, int y, Color bg, ImageObserver o) {
    if (img == null) {
      return true;
    }
    BufferedImage src = ImageSupport.toBuffered(img);
    paintImage(src, 0, 0, src.getWidth(), src.getHeight(), AffineTransform.getTranslateInstance(x, y), bg);
    return true;
  }

  public boolean drawImage(Image img, int x, int y, int w, int h, ImageObserver o) {
    return drawImage(img, x, y, w, h, null, o);
  }

  public boolean drawImage(Image img, int x, int y, int w, int h, Color bg, ImageObserver o) {
    if (img == null) {
      return true;
    }
    if (w == 0 || h == 0) {
      return true;
    }
    BufferedImage src = ImageSupport.toBuffered(img);
    AffineTransform t = AffineTransform.getTranslateInstance(x, y);
    t.scale((double) w / src.getWidth(), (double) h / src.getHeight());
    paintImage(src, 0, 0, src.getWidth(), src.getHeight(), t, bg);
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
    if (dx1 == dx2 || dy1 == dy2 || sx1 == sx2 || sy1 == sy2) {
      return true;
    }
    BufferedImage src = ImageSupport.toBuffered(img);
    AffineTransform t = new AffineTransform();
    t.translate(dx1, dy1);
    t.scale((double) (dx2 - dx1) / (sx2 - sx1), (double) (dy2 - dy1) / (sy2 - sy1));
    t.translate(-sx1, -sy1);
    int rx0 = Math.min(sx1, sx2);
    int rx1 = Math.max(sx1, sx2);
    int ry0 = Math.min(sy1, sy2);
    int ry1 = Math.max(sy1, sy2);
    paintImage(src, rx0, ry0, rx1, ry1, t, bg);
    return true;
  }

  private void paintImage(BufferedImage src, int sx0, int sy0, int sx1, int sy1, AffineTransform imgToUser, Color bg) {
    sx0 = Math.max(sx0, 0);
    sy0 = Math.max(sy0, 0);
    sx1 = Math.min(sx1, src.getWidth());
    sy1 = Math.min(sy1, src.getHeight());
    if (sx0 >= sx1 || sy0 >= sy1 || clipBounds.isEmpty()) {
      return;
    }
    AffineTransform total = new AffineTransform(tx);
    total.concatenate(imgToUser);
    AffineTransform inv;
    try {
      inv = total.createInverse();
    } catch (NoninvertibleTransformException e) {
      return;
    }
    Shape quad = total.createTransformedShape(new Rectangle(sx0, sy0, sx1 - sx0, sy1 - sy0));
    Rectangle db = quad.getBounds().intersection(clipBounds);
    if (db.isEmpty()) {
      return;
    }
    boolean bilinear = hints.get(RenderingHints.KEY_INTERPOLATION) != RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR;
    boolean translateOnly = total.getType() <= AffineTransform.TYPE_TRANSLATION
        && total.getTranslateX() == Math.rint(total.getTranslateX())
        && total.getTranslateY() == Math.rint(total.getTranslateY());
    byte[] m = mask();
    AlphaComposite ac2 = null;
    int rule = ((ac2 = composite instanceof AlphaComposite ? (AlphaComposite) composite : null) != null) ? ac2.getRule() : -1;
    AlphaComposite ac3 = null;
    float extra = ((ac3 = composite instanceof AlphaComposite ? (AlphaComposite) composite : null) != null) ? ac3.getAlpha() : 1f;
    CompositeContext cctx = rule < 0 ? composite.createContext(ColorModel.getRGBdefault(), ColorModel.getRGBdefault(), hints) : null;
    boolean exactQuad = quad instanceof Rectangle2D || isAxisAligned(total);
    double[] pt = new double[2];
    int bgArgb = bg == null ? 0 : bg.getRGB();
    for (int y = db.y; y < db.y + db.height; y++) {
      for (int x = db.x; x < db.x + db.width; x++) {
        if (m != null && m[y * width + x] == 0) {
          continue;
        }
        pt[0] = x + 0.5;
        pt[1] = y + 0.5;
        inv.transform(pt, 0, pt, 0, 1);
        double u = pt[0];
        double v = pt[1];
        if (u < sx0 || v < sy0 || u >= sx1 || v >= sy1) {
          continue;
        }
        if (!exactQuad && !quad.contains(x + 0.5, y + 0.5)) {
          continue;
        }
        int s;
        if (translateOnly || !bilinear) {
          s = src.getRGB(Math.min(sx1 - 1, (int) Math.floor(u)), Math.min(sy1 - 1, (int) Math.floor(v)));
        } else {
          s = sampleBilinear(src, u - 0.5, v - 0.5, sx0, sy0, sx1, sy1);
        }
        int dst = getPixel(x, y);
        if (bg != null) {
          dst = Blend.srcOver(bgArgb, dst, 255);
          if (rule == AlphaComposite.SRC_OVER && extra == 1f) {
            setPixel(x, y, Blend.srcOver(s, dst, 255));
            continue;
          }
        }
        int out;
        if (xorColor != null) {
          out = (dst ^ ((s ^ xorColor.getRGB()) & 0x00ffffff)) | 0xff000000;
        } else if (rule == AlphaComposite.SRC_OVER) {
          out = Blend.srcOver(s, dst, (int) (255 * extra + 0.5f));
        } else if (rule >= 0) {
          out = Blend.compose(rule, s, dst, extra);
        } else {
          WritableRaster sr = ColorModel.getRGBdefault().createCompatibleWritableRaster(1, 1);
          WritableRaster dr = ColorModel.getRGBdefault().createCompatibleWritableRaster(1, 1);
          WritableRaster orr = ColorModel.getRGBdefault().createCompatibleWritableRaster(1, 1);
          sr.setDataElements(0, 0, new int[] {s});
          dr.setDataElements(0, 0, new int[] {dst});
          cctx.compose(sr, dr, orr);
          out = ((int[]) orr.getDataElements(0, 0, null))[0];
        }
        setPixel(x, y, out);
      }
    }
  }

  private static boolean isAxisAligned(AffineTransform t) {
    return t.getShearX() == 0 && t.getShearY() == 0;
  }

  private static int sampleBilinear(BufferedImage src, double u, double v, int x0, int y0, int x1, int y1) {
    int ix = (int) Math.floor(u);
    int iy = (int) Math.floor(v);
    double fx = u - ix;
    double fy = v - iy;
    int p00 = src.getRGB(clamp(ix, x0, x1 - 1), clamp(iy, y0, y1 - 1));
    int p10 = src.getRGB(clamp(ix + 1, x0, x1 - 1), clamp(iy, y0, y1 - 1));
    int p01 = src.getRGB(clamp(ix, x0, x1 - 1), clamp(iy + 1, y0, y1 - 1));
    int p11 = src.getRGB(clamp(ix + 1, x0, x1 - 1), clamp(iy + 1, y0, y1 - 1));
    double a = 0;
    double r = 0;
    double g = 0;
    double b = 0;
    int[] ps = {p00, p10, p01, p11};
    double[] ws = {(1 - fx) * (1 - fy), fx * (1 - fy), (1 - fx) * fy, fx * fy};
    for (int i = 0; i < 4; i++) {
      double wa = (ps[i] >>> 24) * ws[i];
      a += wa;
      r += ((ps[i] >> 16) & 0xff) * wa;
      g += ((ps[i] >> 8) & 0xff) * wa;
      b += (ps[i] & 0xff) * wa;
    }
    if (a <= 0) {
      return 0;
    }
    return ((int) (a + 0.5) << 24) | ((int) (r / a + 0.5) << 16) | ((int) (g / a + 0.5) << 8) | (int) (b / a + 0.5);
  }

  private static int clamp(int v, int lo, int hi) {
    return v < lo ? lo : v > hi ? hi : v;
  }
}
