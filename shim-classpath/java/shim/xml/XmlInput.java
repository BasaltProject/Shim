/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.xml;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import org.xml.sax.InputSource;

public final class XmlInput {
  public final Reader reader;
  public final String systemId;
  public final String publicId;
  public final String baseUri;
  public String encodingProblem;
  public boolean encodingInvalidName;

  public XmlInput(Reader reader, String systemId, String publicId, String baseUri) {
    this.reader = reader;
    this.systemId = systemId;
    this.publicId = publicId;
    this.baseUri = baseUri;
  }

  public static XmlInput open(InputSource source, String base) throws IOException, XmlException {
    String systemId = source.getSystemId();
    String expanded = systemId == null ? null : expand(systemId, base);
    Reader chars = source.getCharacterStream();
    if (chars != null) {
      return new XmlInput(chars, expanded, source.getPublicId(), expanded);
    }
    InputStream bytes = source.getByteStream();
    String encoding = source.getEncoding();
    if (bytes == null) {
      if (expanded == null) {
        throw new XmlException("Premature end of file.", -1, -1, null, null);
      }
      bytes = openUrl(expanded);
    }
    Reader decoded = decode(bytes, encoding);
    XmlInput result = new XmlInput(decoded, expanded, source.getPublicId(), expanded);
    if (decoded instanceof Pending && ((Pending) decoded).problem != null) {
      result.encodingProblem = ((Pending) decoded).problem;
      result.encodingInvalidName = ((Pending) decoded).invalid;
    }
    return result;
  }

  static InputStream openUrl(String expanded) throws IOException {
    try {
      URL url = new URL(expanded);
      if (url.getProtocol().equals("file")) {
        return new java.io.FileInputStream(new File(URI.create(expanded.replace(" ", "%20"))));
      }
      return url.openStream();
    } catch (java.net.MalformedURLException e) {
      return new java.io.FileInputStream(expanded);
    }
  }

  public static String expandDecl(String systemId, String base) {
    String expanded = expand(systemId, base);
    if (base == null || base.isEmpty()) {
      return "file://" + expanded;
    }
    return expanded;
  }

  public static String expand(String systemId, String base) {
    try {
      URI uri = new URI(escape(systemId));
      if (uri.isAbsolute()) {
        return systemId;
      }
      if (base != null && !base.isEmpty()) {
        return new URI(escape(base)).resolve(uri).toString();
      }
      String dir = System.getProperty("user.dir");
      if (!dir.endsWith("/")) {
        dir = dir + "/";
      }
      String userDir = FileUri.fromPath(dir);
      String rel = uri.toString();
      if (rel.startsWith("/")) {
        return "file://" + rel;
      }
      return userDir + rel;
    } catch (java.net.URISyntaxException e) {
      return systemId;
    }
  }

  private static String escape(String s) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < s.length(); i++) {
      char c = s.charAt(i);
      if (c == ' ' || c == '"' || c == '<' || c == '>' || c == '{' || c == '}' || c == '|' || c == '\\'
          || c == '^' || c == '`' || c > 0x7e) {
        byte[] b = String.valueOf(c).getBytes(java.nio.charset.StandardCharsets.UTF_8);
        for (byte x : b) {
          sb.append('%').append(Character.toUpperCase(Character.forDigit((x >> 4) & 15, 16)))
              .append(Character.toUpperCase(Character.forDigit(x & 15, 16)));
        }
      } else {
        sb.append(c);
      }
    }
    return sb.toString();
  }

  static Reader decode(InputStream raw, String forced) throws IOException, XmlException {
    BufferedInputStream in = raw instanceof BufferedInputStream ? (BufferedInputStream) raw : new BufferedInputStream(raw);
    in.mark(256);
    byte[] head = new byte[4];
    int n = 0;
    while (n < 4) {
      int r = in.read(head, n, 4 - n);
      if (r < 0) {
        break;
      }
      n += r;
    }
    in.reset();
    String name = forced;
    int skip = 0;
    int b0 = n > 0 ? head[0] & 0xff : -1;
    int b1 = n > 1 ? head[1] & 0xff : -1;
    int b2 = n > 2 ? head[2] & 0xff : -1;
    int b3 = n > 3 ? head[3] & 0xff : -1;
    String detected = null;
    if (b0 == 0xEF && b1 == 0xBB && b2 == 0xBF) {
      detected = "UTF-8";
      skip = 3;
    } else if (b0 == 0xFE && b1 == 0xFF) {
      detected = "UTF-16BE";
      skip = 2;
    } else if (b0 == 0xFF && b1 == 0xFE) {
      detected = "UTF-16LE";
      skip = 2;
    } else if (b0 == 0x00 && b1 == 0x3C && b2 == 0x00 && b3 == 0x3F) {
      detected = "UTF-16BE";
    } else if (b0 == 0x3C && b1 == 0x00 && b2 == 0x3F && b3 == 0x00) {
      detected = "UTF-16LE";
    }
    if (detected == null && name == null && b0 == 0x3C && b1 == 0x3F && b2 == 0x78 && b3 == 0x6D) {
      in.mark(256);
      byte[] buf = new byte[200];
      int got = 0;
      while (got < buf.length) {
        int r = in.read(buf, got, buf.length - got);
        if (r < 0) {
          break;
        }
        got += r;
      }
      in.reset();
      name = declaredEncoding(new String(buf, 0, got, java.nio.charset.StandardCharsets.ISO_8859_1));
    }
    if (detected != null && forced == null) {
      name = detected;
    }
    if (name == null) {
      name = "UTF-8";
    }
    if (skip > 0) {
      in.skip(skip);
    }
    String problem = null;
    boolean invalid = false;
    Reader reader = null;
    if (!name.matches("[A-Za-z][A-Za-z0-9._\\-]*")) {
      problem = name;
      invalid = true;
      name = "UTF-8";
    }
    Charset cs = null;
    if (problem == null) {
      try {
        cs = Charset.forName(name);
      } catch (IllegalCharsetNameException | UnsupportedCharsetException e) {
        problem = name;
        cs = Charset.forName("UTF-8");
      }
    } else {
      cs = Charset.forName("UTF-8");
    }
    if (cs.name().equals("UTF-8")) {
      reader = new Utf8Reader(in);
    } else {
      reader = new InputStreamReader(in, cs);
    }
    Pending pending = new Pending(reader, problem, invalid);
    return pending;
  }

  static final class Pending extends Reader {
    final Reader delegate;
    final String problem;
    final boolean invalid;

    Pending(Reader delegate, String problem, boolean invalid) {
      this.delegate = delegate;
      this.problem = problem;
      this.invalid = invalid;
    }

    public int read(char[] cbuf, int off, int len) throws IOException {
      return delegate.read(cbuf, off, len);
    }

    public void close() throws IOException {
      delegate.close();
    }
  }

  private static String declaredEncoding(String head) {
    int end = head.indexOf("?>");
    if (end < 0) {
      end = head.length();
    }
    String decl = head.substring(0, end);
    int at = decl.indexOf("encoding");
    if (at < 0) {
      return null;
    }
    int i = at + 8;
    while (i < decl.length() && (decl.charAt(i) == ' ' || decl.charAt(i) == '\t' || decl.charAt(i) == '\n' || decl.charAt(i) == '\r')) {
      i++;
    }
    if (i >= decl.length() || decl.charAt(i) != '=') {
      return null;
    }
    i++;
    while (i < decl.length() && (decl.charAt(i) == ' ' || decl.charAt(i) == '\t' || decl.charAt(i) == '\n' || decl.charAt(i) == '\r')) {
      i++;
    }
    if (i >= decl.length() || (decl.charAt(i) != '"' && decl.charAt(i) != '\'')) {
      return null;
    }
    char quote = decl.charAt(i++);
    int close = decl.indexOf(quote, i);
    if (close < 0) {
      return null;
    }
    return decl.substring(i, close);
  }
}
