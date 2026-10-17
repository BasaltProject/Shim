/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.xml;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;

final class XmlScanner {
  static final int kEof = -1;
  static final int kEndOfEntity = -2;

  static final class Frame {
    final String name;
    final boolean isInternal;
    final String systemId;
    final String publicId;
    final String baseUri;
    char[] buf;
    int pos;
    int limit;
    Reader reader;
    int line = 1;
    int col = 1;
    boolean eof;
    boolean pendingLf;
    IOException failure;

    Frame(String name, boolean internal, String systemId, String publicId, String baseUri) {
      this.name = name;
      this.isInternal = internal;
      this.systemId = systemId;
      this.publicId = publicId;
      this.baseUri = baseUri;
    }
  }

  private final ArrayList<Frame> stack = new ArrayList<Frame>();
  private Frame cur;

  Frame current() {
    return cur;
  }

  int depth() {
    return stack.size();
  }

  void pushInternal(String name, String text, String baseUri) {
    Frame f = new Frame(name, true, null, null, baseUri);
    f.buf = text.toCharArray();
    f.limit = f.buf.length;
    f.eof = true;
    push(f);
  }

  void pushReader(String name, Reader reader, String systemId, String publicId, String baseUri) {
    Frame f = new Frame(name, false, systemId, publicId, baseUri);
    f.buf = new char[4096];
    f.reader = reader;
    push(f);
  }

  private void push(Frame f) {
    if (cur != null) {
      stack.add(cur);
    }
    cur = f;
  }

  void pop() {
    if (cur != null && cur.reader != null) {
      try {
        cur.reader.close();
      } catch (IOException ignored) {
      }
    }
    cur = stack.isEmpty() ? null : stack.remove(stack.size() - 1);
  }

  boolean isTopFrame() {
    return stack.isEmpty();
  }

  String entityPath(String last) {
    StringBuilder sb = new StringBuilder();
    for (Frame f : stack) {
      if (f.name != null) {
        sb.append(f.name).append(" -> ");
      }
    }
    if (cur != null && cur.name != null) {
      sb.append(cur.name).append(" -> ");
    }
    sb.append(last);
    return sb.toString();
  }

  boolean isActiveEntity(String name) {
    if (cur != null && name.equals(cur.name)) {
      return true;
    }
    for (Frame f : stack) {
      if (name.equals(f.name)) {
        return true;
      }
    }
    return false;
  }

  private boolean fill(int need) throws IOException {
    Frame f = cur;
    while (f.limit - f.pos < need && !f.eof) {
      if (f.pos > 0) {
        System.arraycopy(f.buf, f.pos, f.buf, 0, f.limit - f.pos);
        f.limit -= f.pos;
        f.pos = 0;
      }
      if (f.limit == f.buf.length) {
        char[] bigger = new char[f.buf.length * 2];
        System.arraycopy(f.buf, 0, bigger, 0, f.limit);
        f.buf = bigger;
      }
      int n;
      try {
        n = f.reader.read(f.buf, f.limit, f.buf.length - f.limit);
      } catch (XmlMalformedException e) {
        f.failure = e;
        f.eof = true;
        n = -1;
      }
      if (n < 0) {
        f.eof = true;
      } else {
        int w = f.limit;
        for (int i = f.limit; i < f.limit + n; i++) {
          char c = f.buf[i];
          if (c == '\r') {
            f.pendingLf = true;
            f.buf[w++] = '\n';
          } else if (c == '\n' && f.pendingLf) {
            f.pendingLf = false;
          } else {
            f.pendingLf = false;
            f.buf[w++] = c;
          }
        }
        f.limit = w;
      }
    }
    return f.limit - f.pos >= need;
  }

  int peek() throws IOException {
    return peek(0);
  }

  int peek(int k) throws IOException {
    Frame f = cur;
    if (f == null) {
      return kEof;
    }
    if (f.limit - f.pos <= k) {
      if (f.reader != null) {
        fill(k + 1);
      }
      if (f.limit - f.pos <= k) {
        if (k == 0 && f.failure != null) {
          IOException e = f.failure;
          f.failure = null;
          throw e;
        }
        return k == 0 ? (stack.isEmpty() ? kEof : kEndOfEntity) : kEof;
      }
    }
    return f.buf[f.pos + k];
  }

  int read() throws IOException {
    int c = peek();
    if (c < 0) {
      return c;
    }
    Frame f = cur;
    f.pos++;
    if (c == '\n') {
      f.line++;
      f.col = 1;
    } else {
      f.col++;
    }
    return c;
  }

  boolean skipString(String s) throws IOException {
    int n = s.length();
    for (int i = 0; i < n; i++) {
      if (peek(i) != s.charAt(i)) {
        return false;
      }
    }
    for (int i = 0; i < n; i++) {
      read();
    }
    return true;
  }

  boolean lookingAt(String s) throws IOException {
    for (int i = 0; i < s.length(); i++) {
      if (peek(i) != s.charAt(i)) {
        return false;
      }
    }
    return true;
  }

  boolean skipSpaces() throws IOException {
    boolean any = false;
    while (true) {
      int c = peek();
      if (c >= 0 && XmlChars.isSpace(c)) {
        read();
        any = true;
      } else {
        return any;
      }
    }
  }

  String scanName() throws IOException {
    int c = peek();
    if (c < 0 || !XmlChars.isNameStart(c)) {
      if (c >= 0xD800 && c <= 0xDBFF) {
        int d = peek(1);
        if (d >= 0xDC00 && d <= 0xDFFF && XmlChars.isNameStart(Character.toCodePoint((char) c, (char) d))) {
          return scanNameTail();
        }
      }
      return null;
    }
    return scanNameTail();
  }

  private String scanNameTail() throws IOException {
    StringBuilder sb = new StringBuilder();
    while (true) {
      int c = peek();
      if (c < 0) {
        break;
      }
      if (c >= 0xD800 && c <= 0xDBFF) {
        int d = peek(1);
        if (d >= 0xDC00 && d <= 0xDFFF && XmlChars.isName(Character.toCodePoint((char) c, (char) d))) {
          sb.append((char) read());
          sb.append((char) read());
          continue;
        }
        break;
      }
      if (!XmlChars.isName(c)) {
        break;
      }
      sb.append((char) read());
    }
    return sb.toString();
  }

  String scanNmtoken() throws IOException {
    StringBuilder sb = new StringBuilder();
    while (true) {
      int c = peek();
      if (c >= 0 && XmlChars.isName(c)) {
        sb.append((char) read());
      } else {
        break;
      }
    }
    return sb.length() == 0 ? null : sb.toString();
  }

  int line() {
    return cur == null ? -1 : cur.line;
  }

  int col() {
    return cur == null ? -1 : cur.col;
  }

  String systemId() {
    for (int i = stack.size(); i >= 0; i--) {
      Frame f = i == stack.size() ? cur : stack.get(i);
      if (f != null && f.systemId != null) {
        return f.systemId;
      }
    }
    return null;
  }

  String publicId() {
    for (int i = stack.size(); i >= 0; i--) {
      Frame f = i == stack.size() ? cur : stack.get(i);
      if (f != null && f.publicId != null) {
        return f.publicId;
      }
    }
    return null;
  }

  String baseUri() {
    if (cur == null) {
      return null;
    }
    return cur.baseUri;
  }

  void closeAll() {
    while (cur != null) {
      pop();
    }
  }
}
