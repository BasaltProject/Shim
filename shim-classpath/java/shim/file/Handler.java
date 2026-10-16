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

    public int getContentLength() {
      long len = file.length();
      return len > Integer.MAX_VALUE ? -1 : (int) len;
    }

    public long getContentLengthLong() {
      return file.length();
    }

    public String getContentType() {
      String type = guessContentTypeFromName(file.getName());
      return type != null ? type : "content/unknown";
    }

    public long getLastModified() {
      return file.lastModified();
    }

    public InputStream getInputStream() throws IOException {
      return new FileInputStream(file);
    }

    public void connect() {
    }
  }
}
