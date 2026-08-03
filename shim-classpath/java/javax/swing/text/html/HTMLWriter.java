/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */
package javax.swing.text.html;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import javax.swing.text.AbstractWriter;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.StyleConstants;

public class HTMLWriter extends AbstractWriter {
  private final List<HTML.Tag> openInline = new ArrayList<HTML.Tag>();
  private boolean inContent;
  private boolean inPre;
  private boolean inTextArea;
  private boolean inHead;

  public HTMLWriter(Writer w, HTMLDocument doc) {
    this(w, doc, 0, doc.getLength());
  }

  public HTMLWriter(Writer w, HTMLDocument doc, int pos, int len) {
    super(w, doc, pos, len);
    setLineLength(80);
    setCanWrapLines(true);
  }

  public void write() throws IOException, BadLocationException {
    writeElement(getDocument().getDefaultRootElement());
  }

  private void writeElement(Element elem) throws IOException, BadLocationException {
    AttributeSet attr = elem.getAttributes();
    if (!elem.isLeaf()) {
      if (!inRange(elem)) {
        return;
      }
      boolean synthesized = synthesizedElement(elem);
      Object name = attr.getAttribute(StyleConstants.NameAttribute);
      boolean emptyPara = elem.getElementCount() == 1 && elem.getElement(0).isLeaf()
          && attr.getAttribute(StyleConstants.NameAttribute) != null
          && elem.getElement(0).getAttributes().getAttribute(HTMLDocument.CR_KEY) != null;
      if (!synthesized) {
        startTag(elem);
      }
      boolean paragraph = synthesized || (name instanceof HTML.Tag && ((HTML.Tag) name).isParagraph());
      boolean preBlock = name == HTML.Tag.PRE;
      if (preBlock) {
        inPre = true;
      }
      if (paragraph && !emptyPara && !synthesized) {
        inContent = false;
      }
      if (synthesized && emptyPara) {
        indent();
      }
      for (int i = 0; i < elem.getElementCount(); i++) {
        writeElement(elem.getElement(i));
      }
      if (paragraph || synthesized) {
        closeOutUnwantedEmbeddedTags(null);
        if (!inPre && !isLineEmpty()) {
          writeLineSeparator();
        }
        inContent = false;
      }
      if (!synthesized) {
        endTag(elem);
      }
      if (preBlock) {
        inPre = false;
      }
      return;
    }
    if (!inRange(elem)) {
      return;
    }
    Object name = attr.getAttribute(StyleConstants.NameAttribute);
    if (name == HTML.Tag.CONTENT) {
      if (attr.getAttribute(HTMLDocument.CR_KEY) != null) {
        closeOutUnwantedEmbeddedTags(attr);
        return;
      }
      closeOutUnwantedEmbeddedTags(attr);
      if (!inContent && !inPre) {
        indent();
      }
      writeEmbeddedTags(attr);
      text(elem);
      inContent = true;
    } else if (name == HTML.Tag.COMMENT) {
      comment(elem);
    } else {
      closeOutUnwantedEmbeddedTags(attr);
      emptyTag(elem);
    }
  }

  protected void emptyTag(Element elem) throws BadLocationException, IOException {
    AttributeSet attr = elem.getAttributes();
    Object nameTag = attr.getAttribute(StyleConstants.NameAttribute);
    if (nameTag == HTML.Tag.TITLE || nameTag == HTML.Tag.META) {
      return;
    }
    if (!inContent && !inPre) {
      indent();
    }
    write('<');
    write(String.valueOf(nameTag));
    writeAttributes(attr);
    write('>');
    if (nameTag == HTML.Tag.TEXTAREA || nameTag == HTML.Tag.SELECT) {
      return;
    }
    if (!inContent && !inPre) {
      writeLineSeparator();
    }
  }

  protected void comment(Element elem) throws BadLocationException, IOException {
    AttributeSet as = elem.getAttributes();
    Object comment = as.getAttribute(HTML.Attribute.COMMENT);
    if (comment instanceof String) {
      writeComment((String) comment);
    }
  }

  private void writeComment(String string) throws IOException {
    write("<!--");
    write(string);
    write("-->");
    writeLineSeparator();
    indent();
  }

  protected void text(Element elem) throws BadLocationException, IOException {
    String contentStr = getText(elem);
    if (!inPre && contentStr.length() > 0 && contentStr.charAt(contentStr.length() - 1) == '\n') {
      contentStr = contentStr.substring(0, contentStr.length() - 1);
    }
    if (contentStr.length() > 0) {
      write(escape(contentStr));
    }
  }

  private static String escape(String s) {
    StringBuilder sb = null;
    for (int i = 0; i < s.length(); i++) {
      char c = s.charAt(i);
      String rep = c == '<' ? "&lt;" : c == '>' ? "&gt;" : c == '&' ? "&amp;" : c == '"' ? "&quot;"
          : c == ' ' ? "&nbsp;" : null;
      if (rep != null && sb == null) {
        sb = new StringBuilder(s.substring(0, i));
      }
      if (sb != null) {
        sb.append(rep != null ? rep : String.valueOf(c));
      }
    }
    return sb == null ? s : sb.toString();
  }

  protected void writeAttributes(AttributeSet attr) throws IOException {
    Enumeration<?> names = attr.getAttributeNames();
    while (names.hasMoreElements()) {
      Object name = names.nextElement();
      if (name instanceof HTML.Tag || name instanceof CSS.Attribute || name == StyleConstants.ResolveAttribute
          || name == StyleConstants.NameAttribute || name == HTMLDocument.CR_KEY
          || name == HTMLEditorKit.ParserCallback.IMPLIED || name == HTML.Attribute.ENDTAG
          || name == HTML.Attribute.COMMENT) {
        continue;
      }
      String key = name.toString();
      if (name == HTML.Attribute.SELECTED || name == HTML.Attribute.CHECKED) {
        write(" " + key);
        continue;
      }
      write(" " + key + "=\"" + escape(String.valueOf(attr.getAttribute(name))) + "\"");
    }
  }

  protected void output(char[] chars, int start, int length) throws IOException {
    super.output(chars, start, length);
  }

  protected void textAreaContent(AttributeSet attr) throws BadLocationException, IOException {
    Element elem = (Element) attr.getAttribute(StyleConstants.ResolveAttribute);
    if (elem != null) {
      write(getText(elem));
    }
  }

  protected boolean isBlockTag(AttributeSet attr) {
    Object o = attr.getAttribute(StyleConstants.NameAttribute);
    if (o instanceof HTML.Tag) {
      return ((HTML.Tag) o).isBlock();
    }
    return false;
  }

  protected boolean synthesizedElement(Element elem) {
    return matchNameAttribute(elem.getAttributes(), HTML.Tag.IMPLIED);
  }

  protected boolean matchNameAttribute(AttributeSet attr, HTML.Tag tag) {
    Object o = attr.getAttribute(StyleConstants.NameAttribute);
    return o instanceof HTML.Tag && o == tag;
  }

  protected void startTag(Element elem) throws IOException, BadLocationException {
    AttributeSet attr = elem.getAttributes();
    Object name = attr.getAttribute(StyleConstants.NameAttribute);
    if (name == HTML.Tag.PRE) {
      inPre = true;
    }
    indent();
    write('<');
    write(String.valueOf(name));
    writeAttributes(attr);
    write('>');
    if (name != HTML.Tag.PRE) {
      writeLineSeparator();
    }
    incrIndent();
  }

  protected void endTag(Element elem) throws IOException {
    AttributeSet attr = elem.getAttributes();
    Object name = attr.getAttribute(StyleConstants.NameAttribute);
    decrIndent();
    if (name != HTML.Tag.PRE) {
      indent();
    }
    write("</");
    write(String.valueOf(name));
    write('>');
    writeLineSeparator();
    if (name == HTML.Tag.PRE) {
      inPre = false;
    }
  }

  protected void writeEmbeddedTags(AttributeSet attr) throws IOException {
    for (Enumeration<?> en = attr.getAttributeNames(); en.hasMoreElements();) {
      Object k = en.nextElement();
      if (k instanceof HTML.Tag && k != HTML.Tag.CONTENT && !openInline.contains(k)) {
        HTML.Tag t = (HTML.Tag) k;
        write('<');
        write(t.toString());
        Object v = attr.getAttribute(k);
        if (v instanceof AttributeSet) {
          writeAttributes((AttributeSet) v);
          writeInlineStyle((AttributeSet) v);
        }
        write('>');
        openInline.add(t);
      }
    }
  }

  private void writeInlineStyle(AttributeSet a) throws IOException {
    StringBuilder sb = null;
    for (Enumeration<?> en = a.getAttributeNames(); en.hasMoreElements();) {
      Object k = en.nextElement();
      if (k instanceof CSS.Attribute) {
        sb = sb == null ? new StringBuilder() : sb.append("; ");
        sb.append(k).append(": ").append(a.getAttribute(k));
      }
    }
    if (sb != null) {
      write(" style=\"" + sb + "\"");
    }
  }

  protected void closeOutUnwantedEmbeddedTags(AttributeSet attr) throws IOException {
    for (int i = openInline.size() - 1; i >= 0; i--) {
      HTML.Tag t = openInline.get(i);
      if (attr == null || !attr.isDefined(t)) {
        write("</" + t + ">");
        openInline.remove(i);
      }
    }
  }

  protected void selectContent(AttributeSet attr) throws IOException { }

  protected void writeOption(Option option) throws IOException { }

  protected void writeLineSeparator() throws IOException { super.writeLineSeparator(); }
}
