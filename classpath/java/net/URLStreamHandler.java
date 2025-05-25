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

package java.net;

import java.io.IOException;

public abstract class URLStreamHandler {
  protected void parseURL(URL url, String s, int start, int end)
    throws MalformedURLException
  {
    String protocol = s.substring(0, start - 1);
    s = s.substring(start, end);

    String host = null;
    int port = -1;
    if (s.startsWith("//")) {
      s = s.substring(2);
      int colon = s.indexOf(':');
      int slash = s.indexOf('/');
      if (slash < 0) {
        if (colon < 0) {
          host = s;
        } else {
          host = s.substring(0, colon);
          port = Integer.parseInt(s.substring(colon + 1));
        }
        s = "";
      } else {
        if (colon < 0 || colon > slash) {
          host = s.substring(0, slash);
        } else {
          host = s.substring(0, colon);
          port = Integer.parseInt(s.substring(colon + 1, slash));
        }
        s = s.substring(slash);
      }
    }

    String file = null;
    if (s.length() > 0) {
      file = s;
    }

    url.set(protocol, host, port, file, null);
  }

  private static boolean equals(String a, String b) {
    return (a == null && b == null) || (a != null && a.equals(b));
  }

  protected boolean equals(URL a, URL b) {
    return equals(a.getHost(), b.getHost())
      && (a.getPort() == b.getPort())
      && equals(a.getFile(), b.getFile());
  }

  protected String toExternalForm(URL url) {
    StringBuilder sb = new StringBuilder();
    sb.append(url.getProtocol()).append(":");
    if (url.getHost() != null) {
      sb.append("//").append(url.getHost());
      if (url.getPort() >= 0) {
        sb.append(":").append(url.getPort());
      }
    }
    if (url.getFile() != null) {
      sb.append(url.getFile());
    }
    return sb.toString();
  }

  protected abstract URLConnection openConnection(URL url) throws IOException;
}
