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

package shim.jar;

import java.net.URL;
import java.net.MalformedURLException;
import java.net.URLStreamHandler;
import java.net.JarURLConnection;
import java.net.URLConnection;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.jar.JarFile;
import java.util.jar.JarEntry;

public class Handler extends URLStreamHandler {
  protected URLConnection openConnection(URL url) throws IOException {
    return new MyJarURLConnection(url);
  }

  @Override
  protected int getDefaultPort() {
    return -1;
  }

  @Override
  protected void parseURL(URL url, String spec, int start, int limit) {
    String file = null;
    String ref = null;
    int refPos = spec.indexOf('#', limit);
    boolean refOnly = refPos == start;
    if (refPos > -1) {
      ref = spec.substring(refPos + 1, spec.length());
      if (refOnly) {
        file = url.getFile();
      }
    }
    boolean absoluteSpec = false;
    if (spec.length() >= 4) {
      absoluteSpec = spec.substring(0, 4).equalsIgnoreCase("jar:");
    }
    spec = spec.substring(start, limit);
    if (absoluteSpec) {
      file = parseAbsoluteSpec(spec);
    } else if (!refOnly) {
      file = parseContextSpec(url, spec);
      int bangSlash = indexOfBangSlash(file);
      String toBangSlash = file.substring(0, bangSlash);
      String afterBangSlash = file.substring(bangSlash);
      file = toBangSlash + canonize(afterBangSlash);
    }
    setURL(url, "jar", "", -1, file, ref);
  }

  private static int indexOfBangSlash(String spec) {
    int indexOfBang = spec.length();
    while ((indexOfBang = spec.lastIndexOf('!', indexOfBang)) != -1) {
      if ((indexOfBang != (spec.length() - 1)) && (spec.charAt(indexOfBang + 1) == '/')) {
        return indexOfBang + 1;
      } else {
        indexOfBang--;
      }
    }
    return -1;
  }

  private static String parseAbsoluteSpec(String spec) {
    URL url = null;
    int index = -1;
    if ((index = indexOfBangSlash(spec)) == -1) {
      throw new NullPointerException("no !/ in spec");
    }
    try {
      String innerSpec = spec.substring(0, index - 1);
      if (innerSpec.regionMatches(true, 0, "jar:", 0, 4)) {
        throw new NullPointerException("Nested JAR URLs are not supported");
      }
      url = new URL(innerSpec);
    } catch (MalformedURLException e) {
      throw new NullPointerException("invalid url: " + spec + " (" + e + ")");
    }
    return spec;
  }

  private static String parseContextSpec(URL url, String spec) {
    String ctxFile = url.getFile();
    if (spec.startsWith("/")) {
      int bangSlash = indexOfBangSlash(ctxFile);
      if (bangSlash == -1) {
        throw new NullPointerException("malformed context url:" + url + ": no !/");
      }
      ctxFile = ctxFile.substring(0, bangSlash);
    }
    if (!ctxFile.endsWith("/") && (!spec.startsWith("/"))) {
      int lastSlash = ctxFile.lastIndexOf('/');
      if (lastSlash == -1) {
        throw new NullPointerException("malformed context url:" + url);
      }
      ctxFile = ctxFile.substring(0, lastSlash + 1);
    }
    return ctxFile + spec;
  }

  private static String canonize(String path) {
    int i;
    while ((i = path.indexOf("/./")) >= 0) {
      path = path.substring(0, i) + path.substring(i + 2);
    }
    i = 0;
    while ((i = path.indexOf("/../", i)) >= 0) {
      int limit;
      if (i > 0 && (limit = path.lastIndexOf('/', i - 1)) >= 0 && (path.indexOf("/../", limit) != 0)) {
        path = path.substring(0, limit) + path.substring(i + 3);
        i = 0;
      } else {
        i = i + 3;
      }
    }
    while (path.endsWith("/..")) {
      i = path.indexOf("/..");
      int limit;
      if ((limit = path.lastIndexOf('/', i - 1)) >= 0) {
        path = path.substring(0, limit + 1);
      } else {
        break;
      }
    }
    if (path.startsWith("./") && path.length() > 2) {
      path = path.substring(2);
    }
    if (path.endsWith("/.")) {
      path = path.substring(0, path.length() - 1);
    }
    return path;
  }

  private static class MyJarURLConnection extends JarURLConnection {
    private JarFile file;
    private JarEntry entry;

    MyJarURLConnection(URL url) throws MalformedURLException {
      super(url);
    }

    @Override
    public synchronized void connect() throws IOException {
      if (connected) {
        return;
      }
      URL jarUrl = getJarFileURL();
      String path = java.net.URLDecoder.decode(jarUrl.getFile().replace("+", "%2B"), "UTF-8");
      if (!"file".equals(jarUrl.getProtocol())) {
        throw new IOException("Unsupported jar file url protocol: " + jarUrl.getProtocol());
      }
      file = getUseCaches() ? shim.JarCache.open(new java.io.File(path)) : new JarFile(path);
      String name = getEntryName();
      if (name != null) {
        entry = file.getJarEntry(name);
        if (entry == null) {
          if (!getUseCaches()) {
            file.close();
          }
          file = null;
          throw new FileNotFoundException("JAR entry not found in jar file");
        }
      }
      jarFileURLConnection = jarUrl.openConnection();
      connected = true;
    }

    @Override
    public JarFile getJarFile() throws IOException {
      connect();
      return file;
    }

    @Override
    public JarEntry getJarEntry() throws IOException {
      connect();
      return entry;
    }

    @Override
    public int getContentLength() {
      long l = getContentLengthLong();
      return l > Integer.MAX_VALUE ? -1 : (int) l;
    }

    @Override
    public long getContentLengthLong() {
      try {
        connect();
        if (entry != null) {
          return entry.getSize();
        }
        return new java.io.File(file.getName()).length();
      } catch (IOException e) {
        return -1;
      }
    }

    @Override
    public String getContentType() {
      try {
        connect();
      } catch (IOException e) {
        return null;
      }
      if (entry == null) {
        return "x-java/jar";
      }
      String type = guessContentTypeFromName(getEntryName());
      return type != null ? type : "content/unknown";
    }

    @Override
    public long getLastModified() {
      try {
        connect();
      } catch (IOException e) {
        return 0;
      }
      return jarFileURLConnection.getLastModified();
    }

    @Override
    public String getHeaderField(String name) {
      try {
        connect();
      } catch (IOException e) {
        return null;
      }
      return jarFileURLConnection.getHeaderField(name);
    }

    @Override
    public String getHeaderField(int n) {
      try {
        connect();
      } catch (IOException e) {
        return null;
      }
      return jarFileURLConnection.getHeaderField(n);
    }

    @Override
    public String getHeaderFieldKey(int n) {
      try {
        connect();
      } catch (IOException e) {
        return null;
      }
      return jarFileURLConnection.getHeaderFieldKey(n);
    }

    @Override
    public java.util.Map<String, java.util.List<String>> getHeaderFields() {
      try {
        connect();
      } catch (IOException e) {
        return java.util.Collections.emptyMap();
      }
      return jarFileURLConnection.getHeaderFields();
    }

    @Override
    public InputStream getInputStream() throws IOException {
      connect();
      if (entry == null) {
        throw new IOException("no entry name specified");
      }
      InputStream stream = file.getInputStream(entry);
      if (getUseCaches()) return stream;
      final JarFile owned = file;
      return new java.io.FilterInputStream(stream) {
        @Override
        public void close() throws IOException {
          try {
            super.close();
          } finally {
            owned.close();
          }
        }
      };
    }
  }
}
