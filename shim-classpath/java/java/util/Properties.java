/*
 * Based on Avian (https://github.com/ReadyTalk/avian).
 * Original work Copyright (c) 2008-2015, Avian Contributors.
 * Original work licensed under ISC; see LICENSE.txt:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE.txt
 *
 * Modifications Copyright (c) 2026 BasaltMC.
 * Modifications licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: ISC AND MIT
 */

/* Copyright (c) 2008-2015, Avian Contributors

   Permission to use, copy, modify, and/or distribute this software
   for any purpose with or without fee is hereby granted, provided
   that the above copyright notice and this permission notice appear
   in all copies.

   There is NO WARRANTY for this software.  See license.txt for
   details. */

package java.util;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

public class Properties extends Hashtable<Object, Object> {
  private transient volatile ConcurrentHashMap<Object, Object> map;

  protected volatile Properties defaults;

  public Properties() {
    this(null, 8);
  }

  public Properties(int initialCapacity) {
    this(null, initialCapacity);
  }

  public Properties(Properties defaults) {
    this(defaults, 8);
  }

  private Properties(Properties defaults, int initialCapacity) {
    super((Void) null);
    map = new ConcurrentHashMap<>(initialCapacity);
    this.defaults = defaults;
  }

  public synchronized Object setProperty(String key, String value) {
    return put(key, value);
  }

  public synchronized void load(Reader reader) throws IOException {
    load0(new LineReader(Objects.requireNonNull(reader, "reader")));
  }

  public synchronized void load(InputStream inStream) throws IOException {
    load0(new LineReader(new InputStreamReader(Objects.requireNonNull(inStream, "inStream"),
                                               java.nio.charset.StandardCharsets.ISO_8859_1)));
  }

  private void load0(LineReader lr) throws IOException {
    StringBuilder outBuffer = new StringBuilder();
    int limit;
    int keyLen;
    int valueStart;
    boolean hasSep;
    boolean precedingBackslash;
    while ((limit = lr.readLine()) >= 0) {
      keyLen = 0;
      valueStart = limit;
      hasSep = false;
      precedingBackslash = false;
      while (keyLen < limit) {
        char c = lr.lineBuf[keyLen];
        if ((c == '=' || c == ':') && !precedingBackslash) {
          valueStart = keyLen + 1;
          hasSep = true;
          break;
        } else if ((c == ' ' || c == '\t' || c == '\f') && !precedingBackslash) {
          valueStart = keyLen + 1;
          break;
        }
        if (c == '\\') {
          precedingBackslash = !precedingBackslash;
        } else {
          precedingBackslash = false;
        }
        keyLen++;
      }
      while (valueStart < limit) {
        char c = lr.lineBuf[valueStart];
        if (c != ' ' && c != '\t' && c != '\f') {
          if (!hasSep && (c == '=' || c == ':')) {
            hasSep = true;
          } else {
            break;
          }
        }
        valueStart++;
      }
      String key = loadConvert(lr.lineBuf, 0, keyLen, outBuffer);
      String value = loadConvert(lr.lineBuf, valueStart, limit - valueStart, outBuffer);
      put(key, value);
    }
  }

  private static class LineReader {
    LineReader(Reader reader) {
      this.reader = reader;
      inCharBuf = new char[8192];
    }

    char[] inCharBuf;
    char[] lineBuf = new char[1024];
    int inLimit = 0;
    int inOff = 0;
    Reader reader;

    int readLine() throws IOException {
      int len = 0;
      char c = 0;
      boolean skipWhiteSpace = true;
      boolean isCommentLine = false;
      boolean isNewLine = true;
      boolean appendedLineBegin = false;
      boolean precedingBackslash = false;
      boolean skipLF = false;
      while (true) {
        if (inOff >= inLimit) {
          inLimit = reader.read(inCharBuf);
          inOff = 0;
          if (inLimit <= 0) {
            if (len == 0 || isCommentLine) {
              return -1;
            }
            if (precedingBackslash) {
              len--;
            }
            return len;
          }
        }
        c = inCharBuf[inOff++];
        if (skipLF) {
          skipLF = false;
          if (c == '\n') {
            continue;
          }
        }
        if (skipWhiteSpace) {
          if (c == ' ' || c == '\t' || c == '\f') {
            continue;
          }
          if (!appendedLineBegin && (c == '\r' || c == '\n')) {
            continue;
          }
          skipWhiteSpace = false;
          appendedLineBegin = false;
        }
        if (isNewLine) {
          isNewLine = false;
          if (c == '#' || c == '!') {
            isCommentLine = true;
            continue;
          }
        }
        if (c != '\n' && c != '\r') {
          if (len == lineBuf.length) {
            lineBuf = Arrays.copyOf(lineBuf, lineBuf.length * 2);
          }
          lineBuf[len++] = c;
          if (c == '\\') {
            precedingBackslash = !precedingBackslash;
          } else {
            precedingBackslash = false;
          }
        } else {
          if (isCommentLine || len == 0) {
            isCommentLine = false;
            isNewLine = true;
            skipWhiteSpace = true;
            len = 0;
            continue;
          }
          if (inOff >= inLimit) {
            inLimit = reader.read(inCharBuf);
            inOff = 0;
            if (inLimit <= 0) {
              if (precedingBackslash) {
                len--;
              }
              return len;
            }
          }
          if (precedingBackslash) {
            len -= 1;
            skipWhiteSpace = true;
            appendedLineBegin = true;
            precedingBackslash = false;
            if (c == '\r') {
              skipLF = true;
            }
          } else {
            return len;
          }
        }
      }
    }
  }

  private String loadConvert(char[] in, int off, int len, StringBuilder out) {
    char aChar;
    int end = off + len;
    int start = off;
    while (off < end) {
      aChar = in[off++];
      if (aChar == '\\') {
        break;
      }
    }
    if (off == end) {
      return new String(in, start, len);
    }
    if (out.capacity() < len) {
      out.ensureCapacity(len);
    }
    out.setLength(0);
    off--;
    out.append(in, start, off - start);
    while (off < end) {
      aChar = in[off++];
      if (aChar == '\\') {
        aChar = in[off++];
        if (aChar == 'u') {
          if (off > end - 4) {
            throw new IllegalArgumentException("Malformed \\uxxxx encoding.");
          }
          int value = 0;
          for (int i = 0; i < 4; i++) {
            aChar = in[off++];
            switch (aChar) {
            case '0': case '1': case '2': case '3': case '4':
            case '5': case '6': case '7': case '8': case '9':
              value = (value << 4) + aChar - '0';
              break;
            case 'a': case 'b': case 'c': case 'd': case 'e': case 'f':
              value = (value << 4) + 10 + aChar - 'a';
              break;
            case 'A': case 'B': case 'C': case 'D': case 'E': case 'F':
              value = (value << 4) + 10 + aChar - 'A';
              break;
            default:
              throw new IllegalArgumentException("Malformed \\uxxxx encoding.");
            }
          }
          out.append((char) value);
        } else {
          if (aChar == 't') {
            aChar = '\t';
          } else if (aChar == 'r') {
            aChar = '\r';
          } else if (aChar == 'n') {
            aChar = '\n';
          } else if (aChar == 'f') {
            aChar = '\f';
          }
          out.append(aChar);
        }
      } else {
        out.append(aChar);
      }
    }
    return out.toString();
  }

  private String saveConvert(String theString, boolean escapeSpace, boolean escapeUnicode) {
    int len = theString.length();
    int bufLen = len * 2;
    if (bufLen < 0) {
      bufLen = Integer.MAX_VALUE;
    }
    StringBuilder outBuffer = new StringBuilder(bufLen);
    for (int x = 0; x < len; x++) {
      char aChar = theString.charAt(x);
      if ((aChar > 61) && (aChar < 127)) {
        if (aChar == '\\') {
          outBuffer.append('\\');
          outBuffer.append('\\');
          continue;
        }
        outBuffer.append(aChar);
        continue;
      }
      switch (aChar) {
      case ' ':
        if (x == 0 || escapeSpace) {
          outBuffer.append('\\');
        }
        outBuffer.append(' ');
        break;
      case '\t':
        outBuffer.append('\\');
        outBuffer.append('t');
        break;
      case '\n':
        outBuffer.append('\\');
        outBuffer.append('n');
        break;
      case '\r':
        outBuffer.append('\\');
        outBuffer.append('r');
        break;
      case '\f':
        outBuffer.append('\\');
        outBuffer.append('f');
        break;
      case '=':
      case ':':
      case '#':
      case '!':
        outBuffer.append('\\');
        outBuffer.append(aChar);
        break;
      default:
        if (((aChar < 0x0020) || (aChar > 0x007e)) & escapeUnicode) {
          outBuffer.append('\\');
          outBuffer.append('u');
          outBuffer.append(toHex((aChar >> 12) & 0xF));
          outBuffer.append(toHex((aChar >> 8) & 0xF));
          outBuffer.append(toHex((aChar >> 4) & 0xF));
          outBuffer.append(toHex(aChar & 0xF));
        } else {
          outBuffer.append(aChar);
        }
      }
    }
    return outBuffer.toString();
  }

  private static void writeComments(BufferedWriter bw, String comments) throws IOException {
    bw.write("#");
    int len = comments.length();
    int current = 0;
    int last = 0;
    while (current < len) {
      char c = comments.charAt(current);
      if (c > '\u00ff' || c == '\n' || c == '\r') {
        if (last != current) {
          bw.write(comments.substring(last, current));
        }
        if (c > '\u00ff') {
          bw.write("\\u");
          bw.write(Integer.toHexString(c).toUpperCase().length() < 4
                       ? "0000".substring(Integer.toHexString(c).length()) + Integer.toHexString(c).toUpperCase()
                       : Integer.toHexString(c).toUpperCase());
        } else {
          bw.newLine();
          if (c == '\r' && current != len - 1 && comments.charAt(current + 1) == '\n') {
            current++;
          }
          if (current == len - 1
              || (comments.charAt(current + 1) != '#' && comments.charAt(current + 1) != '!')) {
            bw.write("#");
          }
        }
        last = current + 1;
      }
      current++;
    }
    if (last != current) {
      bw.write(comments.substring(last, current));
    }
    bw.newLine();
  }

  public void store(Writer writer, String comments) throws IOException {
    store0((writer instanceof BufferedWriter) ? (BufferedWriter) writer : new BufferedWriter(writer), comments, false);
  }

  public void store(OutputStream out, String comments) throws IOException {
    store0(new BufferedWriter(new OutputStreamWriter(out, java.nio.charset.StandardCharsets.ISO_8859_1)), comments, true);
  }

  private void store0(BufferedWriter bw, String comments, boolean escUnicode) throws IOException {
    if (comments != null) {
      writeComments(bw, comments);
    }
    bw.write("#" + new Date().toString());
    bw.newLine();
    synchronized (this) {
      for (Map.Entry<Object, Object> e : entrySet()) {
        String key = (String) e.getKey();
        String val = (String) e.getValue();
        key = saveConvert(key, true, escUnicode);
        val = saveConvert(val, false, escUnicode);
        bw.write(key + "=" + val);
        bw.newLine();
      }
    }
    bw.flush();
  }

  public String getProperty(String key) {
    Object oval = map.get(key);
    String sval = (oval instanceof String) ? (String) oval : null;
    Properties defaults;
    return ((sval == null) && ((defaults = this.defaults) != null)) ? defaults.getProperty(key) : sval;
  }

  public String getProperty(String key, String defaultValue) {
    String val = getProperty(key);
    return (val == null) ? defaultValue : val;
  }

  public Enumeration<?> propertyNames() {
    Hashtable<String, Object> h = new Hashtable<>();
    enumerate(h);
    return h.keys();
  }

  public Set<String> stringPropertyNames() {
    Map<String, String> h = new HashMap<>();
    enumerateStringProperties(h);
    return Collections.unmodifiableSet(h.keySet());
  }

  public void list(PrintStream out) {
    out.println("-- listing properties --");
    Map<String, Object> h = new HashMap<>();
    enumerate(h);
    for (Map.Entry<String, Object> e : h.entrySet()) {
      String key = e.getKey();
      String val = (String) e.getValue();
      if (val.length() > 40) {
        val = val.substring(0, 37) + "...";
      }
      out.println(key + "=" + val);
    }
  }

  public void list(PrintWriter out) {
    out.println("-- listing properties --");
    Map<String, Object> h = new HashMap<>();
    enumerate(h);
    for (Map.Entry<String, Object> e : h.entrySet()) {
      String key = e.getKey();
      String val = (String) e.getValue();
      if (val.length() > 40) {
        val = val.substring(0, 37) + "...";
      }
      out.println(key + "=" + val);
    }
  }

  private void enumerate(Map<String, Object> h) {
    if (defaults != null) {
      defaults.enumerate(h);
    }
    for (Map.Entry<Object, Object> e : entrySet()) {
      String key = (String) e.getKey();
      h.put(key, e.getValue());
    }
  }

  private void enumerateStringProperties(Map<String, String> h) {
    if (defaults != null) {
      defaults.enumerateStringProperties(h);
    }
    for (Map.Entry<Object, Object> e : entrySet()) {
      Object k = e.getKey();
      Object v = e.getValue();
      if (k instanceof String && v instanceof String) {
        h.put((String) k, (String) v);
      }
    }
  }

  private static char toHex(int nibble) {
    return hexDigit[(nibble & 0xF)];
  }

  private static final char[] hexDigit = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

  @Override
  public int size() {
    return map.size();
  }

  @Override
  public boolean isEmpty() {
    return map.isEmpty();
  }

  @Override
  public Enumeration<Object> keys() {
    return Collections.enumeration(map.keySet());
  }

  @Override
  public Enumeration<Object> elements() {
    return Collections.enumeration(map.values());
  }

  @Override
  public boolean contains(Object value) {
    return map.contains(value);
  }

  @Override
  public boolean containsValue(Object value) {
    return map.containsValue(value);
  }

  @Override
  public boolean containsKey(Object key) {
    return map.containsKey(key);
  }

  @Override
  public Object get(Object key) {
    return map.get(key);
  }

  @Override
  public synchronized Object put(Object key, Object value) {
    return map.put(key, value);
  }

  @Override
  public synchronized Object remove(Object key) {
    return map.remove(key);
  }

  @Override
  public synchronized void putAll(Map<?, ?> t) {
    map.putAll(t);
  }

  @Override
  public synchronized void clear() {
    map.clear();
  }

  @Override
  public synchronized String toString() {
    return map.toString();
  }

  @Override
  public Set<Object> keySet() {
    return Collections.synchronizedSet(map.keySet(), this);
  }

  @Override
  public Collection<Object> values() {
    return Collections.synchronizedCollection(map.values(), this);
  }

  @Override
  public Set<Map.Entry<Object, Object>> entrySet() {
    return Collections.synchronizedSet(map.entrySet(), this);
  }

  @Override
  public synchronized boolean equals(Object o) {
    return map.equals(o);
  }

  @Override
  public synchronized int hashCode() {
    return map.hashCode();
  }

  @Override
  public Object getOrDefault(Object key, Object defaultValue) {
    return map.getOrDefault(key, defaultValue);
  }

  @Override
  public synchronized void forEach(BiConsumer<? super Object, ? super Object> action) {
    map.forEach(action);
  }

  @Override
  public synchronized void replaceAll(BiFunction<? super Object, ? super Object, ?> function) {
    map.replaceAll(function);
  }

  @Override
  public synchronized Object putIfAbsent(Object key, Object value) {
    return map.putIfAbsent(key, value);
  }

  @Override
  public synchronized boolean remove(Object key, Object value) {
    return map.remove(key, value);
  }

  @Override
  public synchronized boolean replace(Object key, Object oldValue, Object newValue) {
    return map.replace(key, oldValue, newValue);
  }

  @Override
  public synchronized Object replace(Object key, Object value) {
    return map.replace(key, value);
  }

  @Override
  public synchronized Object computeIfAbsent(Object key, Function<? super Object, ?> mappingFunction) {
    return map.computeIfAbsent(key, mappingFunction);
  }

  @Override
  public synchronized Object computeIfPresent(Object key, BiFunction<? super Object, ? super Object, ?> remappingFunction) {
    return map.computeIfPresent(key, remappingFunction);
  }

  @Override
  public synchronized Object compute(Object key, BiFunction<? super Object, ? super Object, ?> remappingFunction) {
    return map.compute(key, remappingFunction);
  }

  @Override
  public synchronized Object merge(Object key, Object value, BiFunction<? super Object, ? super Object, ?> remappingFunction) {
    return map.merge(key, value, remappingFunction);
  }

  @Override
  protected void rehash() { }

  @Override
  public synchronized Object clone() {
    Properties clone = (Properties) cloneHashtable();
    clone.map = new ConcurrentHashMap<>(map);
    return clone;
  }
}
