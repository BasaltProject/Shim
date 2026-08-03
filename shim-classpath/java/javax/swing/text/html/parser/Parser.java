/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */
package javax.swing.text.html.parser;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import javax.swing.text.ChangedCharSetException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.html.HTML;

public class Parser implements DTDConstants {
  protected DTD dtd = null;
  protected boolean strict = false;
  private SimpleAttributeSet attributes = new SimpleAttributeSet();
  private String src = "";
  private int pos;
  private int textStart;
  private final ArrayList<TagElement> stack = new ArrayList<TagElement>();
  private final StringBuilder pending = new StringBuilder();
  private boolean lastBlock = true;
  private boolean lastSpace = true;
  private int line = 1;

  private static final Set<String> NO_BLOCK_INSIDE = new HashSet<String>(Arrays.asList("p", "h1", "h2", "h3", "h4", "h5",
      "h6", "dt", "caption", "address", "pre", "title"));
  private static final Set<String> BLOCK_START = new HashSet<String>(Arrays.asList("p", "h1", "h2", "h3", "h4", "h5", "h6",
      "ul", "ol", "dl", "menu", "dir", "pre", "div", "table", "blockquote", "address", "hr", "form", "center", "li", "dt",
      "dd", "tr", "td", "th", "caption", "body", "head", "html", "noframes", "frameset", "frame", "isindex", "title",
      "meta", "link", "base", "style"));
  private static final Set<String> INLINE_BLOCK_BOUNDARY = new HashSet<String>(Arrays.asList("br"));

  public Parser(DTD dtd) {
    this.dtd = dtd;
  }

  protected SimpleAttributeSet getAttributes() { return attributes; }

  protected void flushAttributes() { attributes = new SimpleAttributeSet(); }

  protected int getCurrentLine() { return line; }

  protected int getCurrentPos() { return pos; }

  protected void error(String err, String arg1, String arg2, String arg3) { handleError(getCurrentLine(), err + " " + arg1 + " " + arg2 + " " + arg3); }

  protected void error(String err, String arg1, String arg2) { error(err, arg1, arg2, "?"); }

  protected void error(String err, String arg1) { error(err, arg1, "?", "?"); }

  protected void error(String err) { error(err, "?", "?", "?"); }

  protected void handleText(char[] text) { }

  protected void handleTitle(char[] text) { handleText(text); }

  protected void handleComment(char[] text) { }

  protected void handleEOFInComment() {
    int commentEndPos = src.indexOf('>', pos);
    pos = commentEndPos < 0 ? src.length() : commentEndPos + 1;
  }

  protected void handleEmptyTag(TagElement tag) throws ChangedCharSetException { }

  protected void handleStartTag(TagElement tag) { }

  protected void handleEndTag(TagElement tag) { }

  protected void handleError(int ln, String errorMsg) { }

  protected TagElement makeTag(Element elem, boolean fictional) { return new TagElement(elem, fictional); }

  protected TagElement makeTag(Element elem) { return makeTag(elem, false); }

  protected void markFirstTime(Element elem) { }

  public String parseDTDMarkup() throws IOException { return null; }

  protected boolean parseMarkupDeclarations(StringBuffer strBuff) throws IOException { return false; }

  protected void startTag(TagElement tag) throws ChangedCharSetException {
    Element elem = tag.getElement();
    if (elem.isEmpty()) {
      handleEmptyTag(tag);
      flushAttributes();
      return;
    }
    stack.add(tag);
    handleStartTag(tag);
    flushAttributes();
  }

  protected void endTag(boolean omitted) {
    if (stack.isEmpty()) {
      return;
    }
    TagElement tag = stack.remove(stack.size() - 1);
    handleEndTag(tag);
  }

  public synchronized void parse(Reader in) throws IOException {
    StringBuilder sb = new StringBuilder();
    char[] buf = new char[4096];
    int n;
    while ((n = in.read(buf)) > 0) {
      sb.append(buf, 0, n);
    }
    src = sb.toString();
    pos = 0;
    line = 1;
    stack.clear();
    pending.setLength(0);
    lastBlock = true;
    lastSpace = true;
    int len = src.length();
    while (pos < len) {
      char c = src.charAt(pos);
      if (c == '<') {
        if (src.startsWith("<!--", pos)) {
          flushText(false);
          int end = src.indexOf("-->", pos + 4);
          String body;
          if (end < 0) {
            body = src.substring(pos + 4);
            pos = len;
          } else {
            body = src.substring(pos + 4, end);
            pos = end + 3;
          }
          handleComment(body.toCharArray());
        } else if (src.startsWith("<!", pos) || src.startsWith("<?", pos)) {
          int end = src.indexOf('>', pos);
          pos = end < 0 ? len : end + 1;
        } else if (src.startsWith("</", pos) && pos + 2 < len && Character.isLetter(src.charAt(pos + 2))) {
          parseEndTag();
        } else if (pos + 1 < len && Character.isLetter(src.charAt(pos + 1))) {
          parseStartTag();
        } else {
          pending.append('<');
          pos++;
        }
      } else if (c == '&') {
        parseEntity();
      } else {
        if (c == '\n') {
          line++;
        }
        pending.append(c);
        pos++;
      }
    }
    flushText(false);
    while (!stack.isEmpty()) {
      endTag(true);
    }
  }

  private void parseEntity() {
    int len = src.length();
    int end = pos + 1;
    if (end < len && src.charAt(end) == '#') {
      end++;
      boolean hex = end < len && (src.charAt(end) == 'x' || src.charAt(end) == 'X');
      if (hex) {
        end++;
      }
      int st = end;
      while (end < len && Character.digit(src.charAt(end), hex ? 16 : 10) >= 0) {
        end++;
      }
      if (end > st) {
        try {
          int cp = Integer.parseInt(src.substring(st, end), hex ? 16 : 10);
          pending.appendCodePoint(cp);
          pos = end < len && src.charAt(end) == ';' ? end + 1 : end;
          return;
        } catch (RuntimeException e) {
          // fall through to literal
        }
      }
    } else {
      while (end < len && Character.isLetterOrDigit(src.charAt(end))) {
        end++;
      }
      if (end > pos + 1) {
        Entity ent = dtd.getEntity(src.substring(pos + 1, end));
        if (ent != null) {
          pending.append(ent.getData());
          pos = end < len && src.charAt(end) == ';' ? end + 1 : end;
          return;
        }
      }
    }
    pending.append('&');
    pos++;
  }

  private boolean preformatted() {
    for (int i = stack.size() - 1; i >= 0; i--) {
      if (stack.get(i).isPreformatted()) {
        return true;
      }
    }
    return false;
  }

  private void flushText(boolean beforeBlock) {
    if (pending.length() == 0) {
      return;
    }
    String raw = pending.toString();
    pending.setLength(0);
    TagElement top = stack.isEmpty() ? null : stack.get(stack.size() - 1);
    String out;
    if (preformatted()) {
      out = raw;
    } else {
      StringBuilder sb = new StringBuilder();
      for (int i = 0; i < raw.length(); i++) {
        char ch = raw.charAt(i);
        if (ch == ' ' || ch == '\n' || ch == '\r' || ch == '\t' || ch == '\f') {
          if (!lastBlock && !lastSpace) {
            sb.append(' ');
            lastSpace = true;
          } else if (lastBlock) {
            continue;
          }
        } else {
          sb.append(ch);
          lastSpace = false;
          lastBlock = false;
        }
      }
      if (beforeBlock && sb.length() > 0 && sb.charAt(sb.length() - 1) == ' ') {
        sb.setLength(sb.length() - 1);
      }
      out = sb.toString();
    }
    if (out.length() == 0) {
      return;
    }
    if (preformatted()) {
      lastBlock = false;
      lastSpace = false;
    }
    char[] data = out.toCharArray();
    if (top != null && top.getElement().getName().equals("title")) {
      handleTitle(data);
    } else {
      handleText(data);
    }
  }

  private void parseStartTag() {
    int len = src.length();
    int i = pos + 1;
    int st = i;
    while (i < len && (Character.isLetterOrDigit(src.charAt(i)) || src.charAt(i) == '-' || src.charAt(i) == ':')) {
      i++;
    }
    String name = src.substring(st, i).toLowerCase();
    flushAttributes();
    boolean selfClose = false;
    while (i < len) {
      while (i < len && Character.isWhitespace(src.charAt(i))) {
        if (src.charAt(i) == '\n') {
          line++;
        }
        i++;
      }
      if (i >= len) {
        break;
      }
      char c = src.charAt(i);
      if (c == '>') {
        i++;
        break;
      }
      if (c == '/') {
        selfClose = true;
        i++;
        continue;
      }
      int as = i;
      while (i < len && !Character.isWhitespace(src.charAt(i)) && src.charAt(i) != '=' && src.charAt(i) != '>'
          && src.charAt(i) != '/') {
        i++;
      }
      if (i == as) {
        i++;
        continue;
      }
      String an = src.substring(as, i).toLowerCase();
      while (i < len && Character.isWhitespace(src.charAt(i))) {
        i++;
      }
      String val = an;
      if (i < len && src.charAt(i) == '=') {
        i++;
        while (i < len && Character.isWhitespace(src.charAt(i))) {
          i++;
        }
        if (i < len && (src.charAt(i) == '"' || src.charAt(i) == '\'')) {
          char q = src.charAt(i);
          int e = src.indexOf(q, i + 1);
          if (e < 0) {
            e = len;
          }
          val = decode(src.substring(i + 1, e));
          i = Math.min(len, e + 1);
        } else {
          int vs = i;
          while (i < len && !Character.isWhitespace(src.charAt(i)) && src.charAt(i) != '>') {
            i++;
          }
          val = decode(src.substring(vs, i));
        }
      }
      Object key = HTML.getAttributeKey(an);
      attributes.addAttribute(key == null ? an : key, val);
    }
    pos = i;
    Element elem = dtd.getElement(name);
    boolean block = BLOCK_START.contains(name);
    flushText(block);
    if (block) {
      lastBlock = true;
    }
    openImplied(name);
    TagElement tag = makeTag(elem);
    try {
      startTag(tag);
    } catch (ChangedCharSetException e) {
      // ignored
    }
    if (INLINE_BLOCK_BOUNDARY.contains(name)) {
      lastBlock = true;
    }
    if (name.equals("script") || name.equals("style")) {
      if (!selfClose) {
        int end = indexOfIgnoreCase(src, "</" + name, pos);
        String raw = end < 0 ? src.substring(pos) : src.substring(pos, end);
        pos = end < 0 ? len : end;
        if (raw.length() > 0) {
          handleText(raw.toCharArray());
        }
      }
    } else if (selfClose && !elem.isEmpty() && !stack.isEmpty()) {
      endTag(false);
    }
  }

  private static int indexOfIgnoreCase(String s, String what, int from) {
    return s.toLowerCase().indexOf(what, from);
  }

  private String decode(String s) {
    if (s.indexOf('&') < 0) {
      return s;
    }
    String savedSrc = src;
    int savedPos = pos;
    StringBuilder savedPending = new StringBuilder(pending);
    pending.setLength(0);
    src = s;
    pos = 0;
    while (pos < src.length()) {
      if (src.charAt(pos) == '&') {
        parseEntity();
      } else {
        pending.append(src.charAt(pos++));
      }
    }
    String r = pending.toString();
    pending.setLength(0);
    pending.append(savedPending);
    src = savedSrc;
    pos = savedPos;
    return r;
  }

  private void openImplied(String name) {
    Set<String> closers = null;
    int n = stack.size();
    if (BLOCK_START.contains(name) && !name.equals("body") && !name.equals("head") && !name.equals("html")) {
      for (int i = n - 1; i >= 0; i--) {
        String t = stack.get(i).getElement().getName();
        if (NO_BLOCK_INSIDE.contains(t)) {
          if (!(t.equals("title")) && !(t.equals("pre") && !name.equals("p"))) {
            closeTo(i);
          }
          break;
        }
        if (isContainer(t)) {
          break;
        }
      }
    }
    if (name.equals("li")) {
      closeNearest("li", "ul", "ol", "menu", "dir");
    } else if (name.equals("dt") || name.equals("dd")) {
      closeNearest("dt", "dl");
      closeNearest("dd", "dl");
    } else if (name.equals("tr")) {
      closeNearest("tr", "table");
    } else if (name.equals("td") || name.equals("th")) {
      closeNearest("td", "tr", "table");
      closeNearest("th", "tr", "table");
    } else if (name.equals("option")) {
      closeNearest("option", "select");
    }
  }

  private static boolean isContainer(String t) {
    return t.equals("li") || t.equals("td") || t.equals("th") || t.equals("div") || t.equals("body")
        || t.equals("blockquote") || t.equals("dd") || t.equals("form") || t.equals("center") || t.equals("table")
        || t.equals("ul") || t.equals("ol") || t.equals("dl") || t.equals("tr") || t.equals("menu") || t.equals("dir");
  }

  private void closeNearest(String target, String... stops) {
    for (int i = stack.size() - 1; i >= 0; i--) {
      String t = stack.get(i).getElement().getName();
      if (t.equals(target)) {
        closeTo(i);
        return;
      }
      for (String s : stops) {
        if (t.equals(s)) {
          return;
        }
      }
    }
  }

  private void closeTo(int index) {
    while (stack.size() > index) {
      endTag(true);
    }
  }

  private void parseEndTag() {
    int len = src.length();
    int i = pos + 2;
    int st = i;
    while (i < len && (Character.isLetterOrDigit(src.charAt(i)) || src.charAt(i) == '-' || src.charAt(i) == ':')) {
      i++;
    }
    String name = src.substring(st, i).toLowerCase();
    int e = src.indexOf('>', i);
    pos = e < 0 ? len : e + 1;
    boolean block = BLOCK_START.contains(name);
    flushText(block);
    boolean textSeen = !lastBlock;
    if (block) {
      lastBlock = true;
    }
    for (int k = stack.size() - 1; k >= 0; k--) {
      if (stack.get(k).getElement().getName().equals(name)) {
        closeTo(k);
        return;
      }
    }
    if (name.equals("p") && !textSeen) {
      TagElement t = makeTag(dtd.getElement("p"), true);
      try {
        startTag(t);
      } catch (ChangedCharSetException ex) {
        // ignored
      }
      endTag(false);
    }
  }
}
