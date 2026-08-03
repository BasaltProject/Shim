/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */
package javax.swing.text.rtf;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

final class RtfWriter {
  private RtfWriter() { }

  private static final Object[] kOrder = {
      StyleConstants.LeftIndent, "li", StyleConstants.RightIndent, "ri", StyleConstants.FirstLineIndent, "fi",
      StyleConstants.Alignment, "q", StyleConstants.SpaceAbove, "sa", StyleConstants.SpaceBelow, "sb",
      StyleConstants.FontFamily, "f", StyleConstants.FontSize, "fs", StyleConstants.Italic, "i",
      StyleConstants.Bold, "b", StyleConstants.Underline, "ul", StyleConstants.LineSpacing, "sl",
      StyleConstants.Foreground, "cf"};

  static String generate(Document doc, int pos, int len) throws BadLocationException {
    List<String> fonts = new ArrayList<String>();
    fonts.add("Monospaced");
    List<Color> colors = new ArrayList<Color>();
    colors.add(Color.black);
    StringBuilder body = new StringBuilder();
    boolean styled = doc instanceof StyledDocument;
    Object[] current = new Object[kOrder.length / 2];
    Element root = doc.getDefaultRootElement();
    for (int pi = 0; pi < root.getElementCount(); pi++) {
      Element para = root.getElement(pi);
      StringBuilder pb = new StringBuilder();
      boolean keywordsPending = false;
      for (int ci = 0; ci < para.getElementCount(); ci++) {
        Element run = para.getElement(ci);
        String t = doc.getText(run.getStartOffset(), Math.min(run.getEndOffset(), doc.getLength() + 1)
            - run.getStartOffset());
        if (t.endsWith("\n")) {
          t = t.substring(0, t.length() - 1);
        }
        if (styled) {
          StringBuilder ch = new StringBuilder();
          AttributeSet a2 = run.getAttributes();
          for (int k = 0; k < current.length; k++) {
            Object key = kOrder[2 * k];
            String kw = (String) kOrder[2 * k + 1];
            Object v = a2.getAttribute(key);
            if (v == null) {
              Object def = defaultFor(k);
              if (current[k] == null || def == null || def.equals(current[k])) {
                continue;
              }
              v = def;
            }
            if (k == 6) {
              String fam = (String) v;
              if (!fam.equals(current[k])) {
                int idx = fonts.indexOf(fam);
                if (idx < 0) {
                  fonts.add(fam);
                  idx = fonts.size() - 1;
                }
                ch.append("\\f").append(idx);
                current[k] = fam;
              }
            } else if (k == 12) {
              Color c = (Color) v;
              if (!c.equals(current[k])) {
                int idx = colors.indexOf(c);
                if (idx < 0) {
                  colors.add(c);
                  idx = colors.size() - 1;
                }
                ch.append("\\cf").append(idx);
                current[k] = c;
              }
            } else if (k == 3) {
              Integer al = (Integer) v;
              if (!al.equals(current[k])) {
                ch.append("\\q").append(al.intValue() == 1 ? 'c' : al.intValue() == 2 ? 'r' : al.intValue() == 3 ? 'j'
                    : 'l');
                current[k] = al;
              }
            } else if (v instanceof Boolean) {
              if (!v.equals(current[k])) {
                ch.append("\\").append(kw).append(((Boolean) v).booleanValue() ? "" : "0");
                current[k] = v;
              }
            } else if (k == 7) {
              Integer sz = Integer.valueOf(((Number) v).intValue());
              if (!sz.equals(current[k])) {
                ch.append("\\fs").append(sz.intValue() * 2);
                current[k] = sz;
              }
            } else {
              Integer iv = Integer.valueOf((int) (((Number) v).floatValue() * 20));
              if (!iv.equals(current[k])) {
                ch.append("\\").append(kw).append(iv.intValue());
                current[k] = iv;
              }
            }
          }
          pb.append(ch);
          if ((ch.length() > 0 || keywordsPending) && t.length() > 0) {
            pb.append(' ');
          }
          keywordsPending = ch.length() > 0 && t.length() == 0 || keywordsPending && t.length() == 0;
        }
        escape(pb, t);
      }
      body.append(pb);
      body.append("\\par\n");
    }
    StringBuilder out = new StringBuilder();
    out.append("{\\rtf1\\ansi\n{\\fonttbl");
    for (int i = 0; i < fonts.size(); i++) {
      out.append("\\f").append(i).append("\\fnil ").append(fonts.get(i)).append(';');
    }
    out.append("}\n");
    if (colors.size() > 1) {
      out.append("{\\colortbl");
      for (Color c : colors) {
        out.append("\\red").append(c.getRed()).append("\\green").append(c.getGreen()).append("\\blue")
            .append(c.getBlue()).append(';');
      }
      out.append("}\n");
    }
    out.append("\n").append(body).append("}\n");
    return out.toString();
  }

  private static Object defaultFor(int k) {
    switch (k) {
      case 0:
      case 1:
      case 2:
      case 11:
        return Integer.valueOf(0);
      case 7:
        return Integer.valueOf(12);
      case 8:
      case 9:
      case 10:
        return Boolean.FALSE;
      default:
        return null;
    }
  }

  private static void escape(StringBuilder sb, String t) {
    for (int i = 0; i < t.length(); i++) {
      char c = t.charAt(i);
      if (c == '\\' || c == '{' || c == '}') {
        sb.append('\\').append(c);
      } else if (c == '\t') {
        sb.append("\\tab ");
      } else if (c > 127) {
        if (c < 256) {
          sb.append("\\'").append(Integer.toHexString(c | 0x100).substring(1));
        } else {
          sb.append("\\u").append((int) c).append(" ?");
        }
      } else {
        sb.append(c);
      }
    }
  }
}
