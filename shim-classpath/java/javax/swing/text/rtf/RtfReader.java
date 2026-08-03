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
import java.util.List;
import java.util.Stack;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

final class RtfReader {
  private final String src;
  private final Document doc;
  private int offset;
  private int pos;
  private final List<String> fonts = new ArrayList<String>();
  private final List<Color> colors = new ArrayList<Color>();
  private boolean colorTableSeen;
  private final Stack<State> stack = new Stack<State>();
  private State cur = new State();
  private final StringBuilder text = new StringBuilder();
  private final List<Run> runs = new ArrayList<Run>();
  private final List<Para> paras = new ArrayList<Para>();
  private int uc = 1;

  private static final class State {
    boolean bold;
    boolean italic;
    boolean underline;
    String family;
    int size = -1;
    Color color;
    boolean skip;
    String dest;
    int fontIndex = -1;
    StringBuilder fontName;
    int red;
    int green;
    int blue;
    boolean colorUsed;
    boolean pard;

    State copy() {
      State s = new State();
      s.bold = bold;
      s.italic = italic;
      s.underline = underline;
      s.family = family;
      s.size = size;
      s.color = color;
      s.skip = skip;
      s.dest = dest;
      s.fontIndex = fontIndex;
      s.fontName = fontName;
      s.red = red;
      s.green = green;
      s.blue = blue;
      s.pard = pard;
      return s;
    }
  }

  private static final class Run {
    final int start;
    final int end;
    final MutableAttributeSet attrs;

    Run(int start, int end, MutableAttributeSet attrs) {
      this.start = start;
      this.end = end;
      this.attrs = attrs;
    }
  }

  private static final class Para {
    final int start;
    int end;
    MutableAttributeSet attrs = new SimpleAttributeSet();

    Para(int start) {
      this.start = start;
    }
  }

  private int align = -1;
  private float left;
  private float right;
  private float first;
  private boolean paraStyleSet;
  private int paraStart;
  private int runStart;

  RtfReader(String src, Document doc, int offset) {
    this.src = src;
    this.doc = doc;
    this.offset = offset;
  }

  private MutableAttributeSet charAttrs(State s) {
    SimpleAttributeSet a = new SimpleAttributeSet();
    StyleConstants.setBold(a, s.bold);
    StyleConstants.setItalic(a, s.italic);
    StyleConstants.setUnderline(a, s.underline);
    if (s.family != null) {
      StyleConstants.setFontFamily(a, s.family);
    }
    if (s.size > 0) {
      StyleConstants.setFontSize(a, s.size);
    }
    if (s.pard) {
      StyleConstants.setSpaceAbove(a, 0);
      StyleConstants.setSpaceBelow(a, 0);
    }
    if (s.color != null) {
      StyleConstants.setForeground(a, s.color);
    } else if (!colorTableSeen) {
      StyleConstants.setForeground(a, Color.black);
    }
    return a;
  }

  private void flushRun() {
    if (text.length() > runStart) {
      runs.add(new Run(runStart, text.length(), charAttrs(cur)));
      runStart = text.length();
    }
  }

  private void emit(String s) {
    if (cur.skip || s.length() == 0) {
      return;
    }
    if (cur.dest != null) {
      if (cur.dest.equals("fonttbl") && cur.fontName != null) {
        cur.fontName.append(s);
      }
      return;
    }
    text.append(s);
  }

  private void endParagraph(boolean applyAttrs) {
    flushRun();
    text.append('\n');
    flushRun();
    int end = text.length();
    Para p = new Para(paraStart);
    p.end = end;
    if (applyAttrs) {
      if (align >= 0) {
        StyleConstants.setAlignment(p.attrs, align);
      }
      StyleConstants.setFirstLineIndent(p.attrs, first);
      StyleConstants.setLeftIndent(p.attrs, left);
      StyleConstants.setRightIndent(p.attrs, right);
    }
    paras.add(p);
    paraStart = end;
    runStart = end;
  }

  void run() throws BadLocationException {
    int n = src.length();
    pos = 0;
    while (pos < n) {
      char c = src.charAt(pos);
      if (c == '{') {
        flushRun();
        stack.push(cur.copy());
        cur = cur.copy();
        cur.fontName = null;
        pos++;
      } else if (c == '}') {
        flushRun();
        finishGroup();
        if (!stack.isEmpty()) {
          cur = stack.pop();
        }
        pos++;
      } else if (c == '\\') {
        control();
      } else if (c == '\r' || c == '\n') {
        pos++;
      } else {
        int st = pos;
        while (pos < n && src.charAt(pos) != '{' && src.charAt(pos) != '}' && src.charAt(pos) != '\\'
            && src.charAt(pos) != '\r' && src.charAt(pos) != '\n') {
          pos++;
        }
        String t = src.substring(st, pos);
        if (cur.dest != null && cur.dest.equals("colortbl")) {
          continue;
        }
        if (cur.dest != null && cur.dest.equals("fonttbl")) {
          int semi = t.indexOf(';');
          if (cur.fontName != null) {
            cur.fontName.append(semi >= 0 ? t.substring(0, semi) : t);
          }
          continue;
        }
        emit(t);
      }
    }
    flushRun();
    if (text.length() > paraStart) {
      endParagraph(true);
    }
    build();
  }

  private void finishGroup() {
    if (cur.dest != null && cur.dest.equals("fonttbl") && cur.fontName != null && cur.fontIndex >= 0) {
      while (fonts.size() <= cur.fontIndex) {
        fonts.add(null);
      }
      fonts.set(cur.fontIndex, cur.fontName.toString().trim());
    }
  }

  private void control() {
    int n = src.length();
    pos++;
    if (pos >= n) {
      return;
    }
    char c = src.charAt(pos);
    if (!Character.isLetter(c)) {
      pos++;
      if (c == '\'') {
        if (pos + 1 < n) {
          try {
            emit(String.valueOf((char) Integer.parseInt(src.substring(pos, pos + 2), 16)));
          } catch (NumberFormatException e) {
            // malformed escape is skipped
          }
          pos += 2;
        }
      } else if (c == '\\' || c == '{' || c == '}') {
        emit(String.valueOf(c));
      } else if (c == '*') {
        cur.skip = true;
      } else if (c == '~') {
        emit("\u00a0");
      } else if (c == '_') {
        emit("-");
      } else if (c == '\n' || c == '\r') {
        flushRun();
        endParagraph(true);
      }
      return;
    }
    int st = pos;
    while (pos < n && Character.isLetter(src.charAt(pos))) {
      pos++;
    }
    String word = src.substring(st, pos);
    boolean hasParam = false;
    int param = 0;
    int ps = pos;
    if (pos < n && (src.charAt(pos) == '-' || Character.isDigit(src.charAt(pos)))) {
      pos++;
      while (pos < n && Character.isDigit(src.charAt(pos))) {
        pos++;
      }
      try {
        param = Integer.parseInt(src.substring(ps, pos));
        hasParam = true;
      } catch (NumberFormatException e) {
        param = 0;
      }
    }
    if (pos < n && src.charAt(pos) == ' ') {
      pos++;
    }
    keyword(word, hasParam, param);
  }

  private void keyword(String w, boolean has, int p) {
    flushRun();
    if (w.equals("fonttbl")) {
      flushRun();
      cur.dest = "fonttbl";
      cur.skip = false;
    } else if (w.equals("colortbl")) {
      flushRun();
      cur.dest = "colortbl";
      colorTableSeen = true;
      cur.skip = false;
      colors.clear();
      colors.add(null);
      int semi = pos;
      int end = src.indexOf('}', pos);
      if (end < 0) {
        end = src.length();
      }
      parseColorTable(src.substring(pos, end));
      pos = end;
    } else if (w.equals("stylesheet") || w.equals("info") || w.equals("pict") || w.equals("header")
        || w.equals("footer") || w.equals("fldinst") || w.equals("generator") || w.equals("listtable")
        || w.equals("listoverridetable") || w.equals("rsidtbl")) {
      cur.skip = true;
    } else if (w.equals("f")) {
      if (cur.dest != null && cur.dest.equals("fonttbl")) {
        cur.fontIndex = p;
        cur.fontName = new StringBuilder();
      } else if (has && p >= 0 && p < fonts.size() && fonts.get(p) != null) {
        cur.family = fonts.get(p);
      }
    } else if (w.equals("fs")) {
      cur.size = Math.max(1, p / 2);
    } else if (w.equals("b")) {
      cur.bold = !has || p != 0;
    } else if (w.equals("i")) {
      cur.italic = !has || p != 0;
    } else if (w.equals("ul")) {
      cur.underline = !has || p != 0;
    } else if (w.equals("ulnone")) {
      cur.underline = false;
    } else if (w.equals("cf")) {
      cur.color = has && p > 0 && p < colors.size() ? colors.get(p) : null;
    } else if (w.equals("plain")) {
      cur.bold = false;
      cur.italic = false;
      cur.underline = false;
      cur.color = null;
      cur.size = -1;
      cur.family = null;
    } else if (w.equals("par")) {
      flushRun();
      endParagraph(true);
    } else if (w.equals("line")) {
      flushRun();
      endParagraph(false);
    } else if (w.equals("tab")) {
      emit("\t");
    } else if (w.equals("pard")) {
      cur.pard = true;
      align = -1;
      left = right = first = 0;
    } else if (w.equals("qc")) {
      align = StyleConstants.ALIGN_CENTER;
    } else if (w.equals("ql")) {
      align = StyleConstants.ALIGN_LEFT;
    } else if (w.equals("qr")) {
      align = StyleConstants.ALIGN_RIGHT;
    } else if (w.equals("qj")) {
      align = StyleConstants.ALIGN_JUSTIFIED;
    } else if (w.equals("li")) {
      left = p / 20f;
    } else if (w.equals("ri")) {
      right = p / 20f;
    } else if (w.equals("fi")) {
      first = p / 20f;
    } else if (w.equals("uc")) {
      uc = p;
    } else if (w.equals("u")) {
      emit(String.valueOf((char) (short) p));
      int skipChars = uc;
      while (skipChars > 0 && pos < src.length()) {
        if (src.charAt(pos) == '\\' && pos + 1 < src.length() && src.charAt(pos + 1) == '\'') {
          pos += 4;
        } else {
          pos++;
        }
        skipChars--;
      }
    } else if (w.equals("emdash")) {
      emit("\u2014");
    } else if (w.equals("endash")) {
      emit("\u2013");
    } else if (w.equals("bullet")) {
      emit("\u2022");
    } else if (w.equals("lquote")) {
      emit("\u2018");
    } else if (w.equals("rquote")) {
      emit("\u2019");
    } else if (w.equals("ldblquote")) {
      emit("\u201c");
    } else if (w.equals("rdblquote")) {
      emit("\u201d");
    }
  }

  private void parseColorTable(String body) {
    for (String entry : body.split(";", -1)) {
      if (entry.trim().length() == 0) {
        continue;
      }
      int r = 0;
      int g = 0;
      int b = 0;
      for (String part : entry.split("\\\\")) {
        part = part.trim();
        try {
          if (part.startsWith("red")) {
            r = Integer.parseInt(part.substring(3).trim());
          } else if (part.startsWith("green")) {
            g = Integer.parseInt(part.substring(5).trim());
          } else if (part.startsWith("blue")) {
            b = Integer.parseInt(part.substring(4).trim());
          }
        } catch (NumberFormatException e) {
          // malformed component keeps zero
        }
      }
      colors.add(new Color(r, g, b));
    }
  }

  private void build() throws BadLocationException {
    if (text.length() == 0) {
      return;
    }
    String all = text.toString();
    int base = offset;
    StyledDocument sd = doc instanceof StyledDocument ? (StyledDocument) doc : null;
    doc.insertString(base, all, null);
    if (sd == null) {
      return;
    }
    for (Run r : runs) {
      int end = r.end;
      if (end > r.start) {
        sd.setCharacterAttributes(base + r.start, end - r.start, r.attrs, false);
      }
    }
    for (Para p : paras) {
      int end = p.end;
      if (end > p.start && p.attrs.getAttributeCount() > 0) {
        sd.setParagraphAttributes(base + p.start, Math.max(1, end - p.start - 1), p.attrs, false);
      }
    }
  }
}
