/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */
package javax.swing.text.html;

import java.awt.Color;
import java.io.IOException;
import java.io.Writer;
import java.util.Enumeration;
import javax.swing.text.AbstractWriter;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

public class MinimalHTMLWriter extends AbstractWriter {
  private StyledDocument doc;
  private boolean inFontTag;

  public MinimalHTMLWriter(Writer w, StyledDocument doc) {
    super(w, doc);
    this.doc = doc;
  }

  public MinimalHTMLWriter(Writer w, StyledDocument doc, int pos, int len) {
    super(w, doc, pos, len);
    this.doc = doc;
  }

  public void write() throws IOException, BadLocationException {
    writeStartTag("<html>");
    writeHeader();
    writeBody();
    writeEndTag("</html>");
  }

  protected void writeStartTag(String tag) throws IOException {
    indent();
    write(tag);
    write('\n');
    incrIndent();
  }

  protected void writeEndTag(String endTag) throws IOException {
    decrIndent();
    indent();
    write(endTag);
    write('\n');
  }

  protected void writeHeader() throws IOException {
    writeStartTag("<head>");
    writeStartTag("<style>");
    writeStartTag("<!--");
    writeStyles();
    writeEndTag("-->");
    writeEndTag("</style>");
    writeEndTag("</head>");
  }

  protected void writeStyles() throws IOException {
    Enumeration<?> styleNames = ((javax.swing.text.DefaultStyledDocument) doc).getStyleNames();
    while (styleNames.hasMoreElements()) {
      Style s = doc.getStyle((String) styleNames.nextElement());
      if (s.getAttributeCount() == 1 && s.isDefined(StyleConstants.NameAttribute)) {
        continue;
      }
      indent();
      write("p." + s.getName() + " {\n");
      incrIndent();
      writeNonHTMLAttributes(s);
      decrIndent();
      indent();
      write("}\n");
    }
  }

  protected void writeNonHTMLAttributes(AttributeSet attr) throws IOException {
    if (attr.isDefined(StyleConstants.FontFamily)) {
      indent();
      write("family:" + StyleConstants.getFontFamily(attr) + ";\n");
    }
    if (attr.isDefined(StyleConstants.FontSize)) {
      indent();
      write("size:" + (StyleSheet.getIndexOfSize(StyleConstants.getFontSize(attr)) + 1) + ";\n");
    }
    if (attr.isDefined(StyleConstants.Bold)) {
      indent();
      write("bold:" + (StyleConstants.isBold(attr) ? "bold" : "normal") + ";\n");
    }
    if (attr.isDefined(StyleConstants.Italic)) {
      indent();
      write("italic:" + (StyleConstants.isItalic(attr) ? "italic" : "") + ";\n");
    }
    if (attr.isDefined(StyleConstants.Foreground)) {
      indent();
      write("foreground:" + hex(StyleConstants.getForeground(attr)) + ";\n");
    }
  }

  private static String hex(Color c) { return String.format("#%06x", c.getRGB() & 0xFFFFFF); }

  protected void writeBody() throws IOException, BadLocationException {
    writeStartTag("<body>");
    Element root = doc.getDefaultRootElement();
    for (int i = 0; i < root.getElementCount(); i++) {
      Element para = root.getElement(i);
      if (!inRange(para)) {
        continue;
      }
      writeStartParagraph(para);
      for (int j = 0; j < para.getElementCount(); j++) {
        Element leaf = para.getElement(j);
        if (inRange(leaf)) {
          writeContent(leaf, true);
        }
      }
      writeEndParagraph();
    }
    writeEndTag("</body>");
  }

  protected void writeStartParagraph(Element elem) throws IOException {
    AttributeSet attr = elem.getAttributes();
    Object resolveAttr = attr.getAttribute(StyleConstants.ResolveAttribute);
    indent();
    if (resolveAttr instanceof Style) {
      write("<p class=" + ((Style) resolveAttr).getName() + ">\n");
    } else {
      write("<p>\n");
    }
    incrIndent();
  }

  protected void writeEndParagraph() throws IOException { writeEndTag("</p>"); }

  protected void writeContent(Element elem, boolean needsIndenting) throws IOException, BadLocationException {
    AttributeSet attr = elem.getAttributes();
    String name = String.valueOf(attr.getAttribute(StyleConstants.NameAttribute));
    if (name.equals("icon")) {
      writeImage(elem);
      return;
    }
    if (name.equals("component")) {
      writeComponent(elem);
      return;
    }
    String text = getText(elem);
    if (text.endsWith("\n")) {
      text = text.substring(0, text.length() - 1);
    }
    if (text.length() == 0) {
      return;
    }
    StringBuilder style = new StringBuilder();
    if (attr.getAttribute(StyleConstants.Foreground) != null) {
      style.append("color: ").append(hex(StyleConstants.getForeground(attr)));
    }
    if (attr.getAttribute(StyleConstants.FontSize) != null) {
      style.append(style.length() > 0 ? "; " : "").append("font-size: ").append(StyleConstants.getFontSize(attr))
          .append("pt");
    }
    if (attr.getAttribute(StyleConstants.FontFamily) != null) {
      style.append(style.length() > 0 ? "; " : "").append("font-family: ")
          .append(StyleConstants.getFontFamily(attr));
    }
    if (StyleConstants.isBold(attr) && attr.getAttribute(StyleConstants.Bold) != null) {
      style.append(style.length() > 0 ? "; " : "").append("font-weight: bold");
    }
    if (StyleConstants.isItalic(attr) && attr.getAttribute(StyleConstants.Italic) != null) {
      style.append(style.length() > 0 ? "; " : "").append("font-style: italic");
    }
    if (StyleConstants.isUnderline(attr) && attr.getAttribute(StyleConstants.Underline) != null) {
      style.append(style.length() > 0 ? "; " : "").append("text-decoration: underline");
    }
    indent();
    write("<span style=\"" + style + "\">\n");
    incrIndent();
    indent();
    write(text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;") + "\n");
    decrIndent();
    indent();
    write("</span>\n");
  }

  protected void text(Element elem) throws IOException, BadLocationException {
    String contentStr = getText(elem);
    if (contentStr.length() > 0 && contentStr.charAt(contentStr.length() - 1) == '\n') {
      contentStr = contentStr.substring(0, contentStr.length() - 1);
    }
    if (contentStr.length() > 0) {
      write(contentStr);
    }
  }

  protected void writeAttributes(AttributeSet attr) throws IOException { }

  protected boolean isText(Element elem) {
    return AttributeNameIs(elem, "content");
  }

  private static boolean AttributeNameIs(Element elem, String name) {
    return name.equals(String.valueOf(elem.getAttributes().getAttribute(StyleConstants.NameAttribute)));
  }

  protected void writeLeaf(Element elem) throws IOException {
    try {
      writeContent(elem, true);
    } catch (BadLocationException e) {
      throw new IOException(e.getMessage());
    }
  }

  protected boolean inFontTag() { return inFontTag; }

  protected void writeImage(Element elem) throws IOException { }

  protected void writeComponent(Element elem) throws IOException { }

  protected void writeHTMLTags(AttributeSet attr) throws IOException { }

  protected void startFontTag(String style) throws IOException {
    inFontTag = true;
    write("<font style=\"" + style + "\">");
  }

  protected void endFontTag() throws IOException {
    inFontTag = false;
    write("</font>");
  }
}
