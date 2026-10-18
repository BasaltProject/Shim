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

package shim.file;

import java.net.URL;
import java.net.URLStreamHandler;
import java.net.URLConnection;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

public class Handler extends URLStreamHandler {
  protected URLConnection openConnection(URL url) {
    return new FileURLConnection(url);
  }

  private static class FileURLConnection extends URLConnection {
    private final File file;
    private boolean isDirectory;
    private String[] listing;
    private final java.util.LinkedHashMap<String, String> headers = new java.util.LinkedHashMap<String, String>();
    private InputStream stream;

    public FileURLConnection(URL url) {
      super(url);
      this.file = new File(decode(url.getPath()));
    }

    private static String decode(String path) {
      if (path.indexOf('%') < 0) {
        return path;
      }
      return java.net.URLDecoder.decode(path.replace("+", "%2B"), java.nio.charset.StandardCharsets.UTF_8);
    }

    @Override
    public synchronized void connect() throws IOException {
      if (connected) {
        return;
      }
      if (!file.exists()) {
        throw new FileNotFoundException(file.getPath() + " (No such file or directory)");
      }
      isDirectory = file.isDirectory();
      if (isDirectory) {
        listing = file.list();
        java.util.Arrays.sort(listing);
        headers.put("content-type", "text/plain");
      } else {
        String type = guessContentTypeFromName(file.getName());
        headers.put("content-type", type != null ? type : "content/unknown");
        headers.put("content-length", String.valueOf(file.length()));
        java.text.SimpleDateFormat df = new java.text.SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss 'GMT'",
            java.util.Locale.US);
        df.setTimeZone(java.util.TimeZone.getTimeZone("GMT"));
        headers.put("last-modified", df.format(new java.util.Date(file.lastModified())));
      }
      connected = true;
    }

    private void connectQuietly() {
      try {
        connect();
      } catch (IOException e) {
      }
    }

    @Override
    public String getHeaderField(String name) {
      connectQuietly();
      return name == null ? null : headers.get(name.toLowerCase(java.util.Locale.ROOT));
    }

    @Override
    public String getHeaderField(int n) {
      connectQuietly();
      int i = 0;
      for (String v : headers.values()) {
        if (i++ == n) {
          return v;
        }
      }
      return null;
    }

    @Override
    public String getHeaderFieldKey(int n) {
      connectQuietly();
      int i = 0;
      for (String k : headers.keySet()) {
        if (i++ == n) {
          return k;
        }
      }
      return null;
    }

    @Override
    public java.util.Map<String, java.util.List<String>> getHeaderFields() {
      connectQuietly();
      java.util.Map<String, java.util.List<String>> m = new java.util.HashMap<String, java.util.List<String>>();
      for (java.util.Map.Entry<String, String> e : headers.entrySet()) {
        m.put(e.getKey(), java.util.Collections.singletonList(e.getValue()));
      }
      return java.util.Collections.unmodifiableMap(m);
    }

    @Override
    public int getContentLength() {
      long len = getContentLengthLong();
      return len > Integer.MAX_VALUE ? -1 : (int) len;
    }

    @Override
    public long getContentLengthLong() {
      connectQuietly();
      if (!connected) {
        return -1;
      }
      return file.length();
    }

    private byte[] listingBytes() {
      StringBuilder sb = new StringBuilder();
      for (String name : listing) {
        sb.append(name).append('\n');
      }
      return sb.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
    }

    @Override
    public String getContentType() {
      connectQuietly();
      return connected ? headers.get("content-type") : "content/unknown";
    }

    @Override
    public long getLastModified() {
      return file.lastModified() / 1000 * 1000;
    }

    @Override
    public synchronized InputStream getInputStream() throws IOException {
      connect();
      if (stream == null) {
        stream = isDirectory ? new java.io.ByteArrayInputStream(listingBytes())
            : new java.io.BufferedInputStream(new FileInputStream(file));
      }
      return stream;
    }
  }
}
