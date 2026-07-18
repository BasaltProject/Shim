/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */
package javax.swing.plaf.basic;

import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Shape;
import java.io.StringReader;
import java.net.URL;
import javax.swing.JComponent;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.Position;
import javax.swing.text.StyleConstants;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.ImageView;
import javax.swing.text.html.StyleSheet;

public class BasicHTML {
  public static final String propertyKey = "html";
  public static final String documentBaseKey = "html.base";
  static final String htmlDisable = "html.disable";
  private static final String sourceKey = "shim.html.source";
  private static BasicEditorKit basicHTMLFactory;
  private static ViewFactory basicHTMLViewFactory;

  public BasicHTML() { }

  public static View createHTMLView(JComponent c, String html) {
    BasicEditorKit kit = getFactory();
    Document doc = kit.createDefaultDocument(c.getFont(), c.getForeground());
    Object base = c.getClientProperty(documentBaseKey);
    if (base instanceof URL) {
      ((HTMLDocument) doc).setBase((URL) base);
    }
    try {
      kit.read(new StringReader(html), doc, 0);
    } catch (Throwable e) {
      // an unreadable document renders as empty
    }
    ViewFactory f = kit.getViewFactory();
    View hview = f.create(doc.getDefaultRootElement());
    return new Renderer(c, f, hview);
  }

  public static int getHTMLBaseline(View view, int w, int h) {
    if (w < 0 || h < 0) {
      throw new IllegalArgumentException("Width and height must be >= 0");
    }
    if (view instanceof Renderer) {
      return getBaseline(view.getView(0), w, h);
    }
    return -1;
  }

  private static int getBaseline(View view, int w, int h) {
    if (hasParagraph(view)) {
      view.setSize(w, h);
      return getBaseline(view, new Rectangle(0, 0, w, h));
    }
    return -1;
  }

  private static int getBaseline(View view, Shape bounds) {
    if (view.getViewCount() == 0) {
      return -1;
    }
    AttributeSet attributes = view.getElement().getAttributes();
    Object name = null;
    if (attributes != null) {
      name = attributes.getAttribute(StyleConstants.NameAttribute);
    }
    int index = 0;
    if (name == HTML.Tag.HTML && view.getViewCount() > 1) {
      index++;
    }
    bounds = view.getChildAllocation(index, bounds);
    if (bounds == null) {
      return -1;
    }
    View child = view.getView(index);
    if (view instanceof javax.swing.text.ParagraphView) {
      Rectangle rect = bounds instanceof Rectangle ? (Rectangle) bounds : bounds.getBounds();
      return rect.y + (int) (rect.height * child.getAlignment(View.Y_AXIS));
    }
    return getBaseline(child, bounds);
  }

  private static boolean hasParagraph(View view) {
    if (view instanceof javax.swing.text.ParagraphView) {
      return true;
    }
    if (view.getViewCount() == 0) {
      return false;
    }
    AttributeSet attributes = view.getElement().getAttributes();
    Object name = attributes != null ? attributes.getAttribute(StyleConstants.NameAttribute) : null;
    int index = 0;
    if (name == HTML.Tag.HTML && view.getViewCount() > 1) {
      index = 1;
    }
    return hasParagraph(view.getView(index));
  }

  public static boolean isHTMLString(String s) {
    if (s != null) {
      if ((s.length() >= 6) && (s.charAt(0) == '<') && (s.charAt(5) == '>')) {
        String tag = s.substring(1, 5);
        return tag.equalsIgnoreCase(propertyKey);
      }
    }
    return false;
  }

  public static void updateRenderer(JComponent c, String text) {
    View value = null;
    View oldValue = (View) c.getClientProperty(BasicHTML.propertyKey);
    Boolean htmlDisabled = (Boolean) c.getClientProperty(htmlDisable);
    if (htmlDisabled != Boolean.TRUE && BasicHTML.isHTMLString(text)) {
      value = BasicHTML.createHTMLView(c, text);
    }
    if (value != oldValue && oldValue != null) {
      for (int i = 0; i < oldValue.getViewCount(); i++) {
        oldValue.getView(i).setParent(null);
      }
    }
    c.putClientProperty(BasicHTML.propertyKey, value);
    c.putClientProperty(sourceKey, value == null ? null : new Object[] {text, c.getFont(), c.getForeground()});
  }

  public static View rendererFor(JComponent c, String text) {
    if (c == null || !isHTMLString(text) || c.getClientProperty(htmlDisable) == Boolean.TRUE) {
      return null;
    }
    View v = (View) c.getClientProperty(propertyKey);
    Object[] src = (Object[]) c.getClientProperty(sourceKey);
    if (v == null || src == null || !src[0].equals(text) || !java.util.Objects.equals(src[1], c.getFont())
        || !java.util.Objects.equals(src[2], c.getForeground())) {
      updateRenderer(c, text);
      v = (View) c.getClientProperty(propertyKey);
    }
    return v;
  }

  static BasicEditorKit getFactory() {
    if (basicHTMLFactory == null) {
      basicHTMLViewFactory = new BasicHTMLViewFactory();
      basicHTMLFactory = new BasicEditorKit();
    }
    return basicHTMLFactory;
  }

  static class BasicEditorKit extends HTMLEditorKit {
    private static StyleSheet defaultStyles;

    public StyleSheet getStyleSheet() {
      if (defaultStyles == null) {
        defaultStyles = new StyleSheet();
        defaultStyles.addRule("p { margin-top: 0; margin-bottom: 0; margin-left: 0; margin-right: 0 }"
            + " body { margin-top: 0; margin-bottom: 0; margin-left: 0; margin-right: 0 }");
        defaultStyles.addStyleSheet(super.getStyleSheet());
      }
      return defaultStyles;
    }

    public Document createDefaultDocument(Font defaultFont, Color foreground) {
      StyleSheet styles = getStyleSheet();
      StyleSheet ss = new StyleSheet();
      ss.addStyleSheet(styles);
      BasicDocument doc = new BasicDocument(ss, defaultFont, foreground);
      doc.setAsynchronousLoadPriority(Integer.MAX_VALUE);
      doc.setPreservesUnknownTags(false);
      return doc;
    }

    public ViewFactory getViewFactory() { return basicHTMLViewFactory; }
  }

  static class BasicDocument extends HTMLDocument {
    BasicDocument(StyleSheet s, Font defaultFont, Color foreground) {
      super(s);
      setPreservesUnknownTags(false);
      setFontAndColor(defaultFont, foreground);
    }

    private void setFontAndColor(Font font, Color fg) {
      StringBuilder css = new StringBuilder("body { ");
      if (font != null) {
        css.append("font-family: ").append(font.getFamily()).append("; font-size: ").append(font.getSize())
            .append("pt;");
        if (font.isBold()) {
          css.append(" font-weight: 700;");
        }
        if (font.isItalic()) {
          css.append(" font-style: italic;");
        }
      }
      if (fg != null) {
        css.append(" color: #").append(String.format("%06x", fg.getRGB() & 0xFFFFFF)).append(";");
      }
      css.append(" }");
      getStyleSheet().addRule(css.toString());
    }
  }

  static class BasicHTMLViewFactory extends HTMLEditorKit.HTMLFactory {
    public View create(Element elem) {
      View view = super.create(elem);
      if (view instanceof ImageView) {
        ((ImageView) view).setLoadsSynchronously(true);
      }
      return view;
    }
  }

  static class Renderer extends View implements ViewFactory {
    private int width;
    private View view;
    private ViewFactory factory;
    private JComponent host;

    Renderer(JComponent c, ViewFactory f, View v) {
      super(null);
      host = c;
      factory = f;
      view = v;
      view.setParent(this);
      setSize(view.getPreferredSpan(X_AXIS), view.getPreferredSpan(Y_AXIS));
    }

    public AttributeSet getAttributes() { return null; }

    public float getPreferredSpan(int axis) {
      if (axis == X_AXIS) {
        return width;
      }
      return view.getPreferredSpan(axis);
    }

    public float getMinimumSpan(int axis) { return view.getMinimumSpan(axis); }

    public float getMaximumSpan(int axis) { return Integer.MAX_VALUE; }

    public void preferenceChanged(View child, boolean width, boolean height) {
      host.revalidate();
      host.repaint();
    }

    public float getAlignment(int axis) { return view.getAlignment(axis); }

    public void paint(Graphics g, Shape allocation) {
      Rectangle alloc = allocation.getBounds();
      view.setSize(alloc.width, alloc.height);
      view.paint(g, allocation);
    }

    public void setParent(View parent) { throw new Error("Can't set parent on root view"); }

    public int getViewCount() { return 1; }

    public View getView(int n) { return view; }

    public Shape modelToView(int pos, Shape a, Position.Bias b) throws BadLocationException {
      return view.modelToView(pos, a, b);
    }

    public Shape modelToView(int p0, Position.Bias b0, int p1, Position.Bias b1, Shape a)
        throws BadLocationException {
      return view.modelToView(p0, b0, p1, b1, a);
    }

    public int viewToModel(float x, float y, Shape a, Position.Bias[] bias) {
      return view.viewToModel(x, y, a, bias);
    }

    public Document getDocument() { return view.getDocument(); }

    public int getStartOffset() { return view.getStartOffset(); }

    public int getEndOffset() { return view.getEndOffset(); }

    public Element getElement() { return view.getElement(); }

    public void setSize(float width, float height) {
      this.width = (int) width;
      view.setSize(width, height);
    }

    public Container getContainer() { return host; }

    public ViewFactory getViewFactory() { return factory; }

    public View create(Element elem) { return factory.create(elem); }
  }
}
