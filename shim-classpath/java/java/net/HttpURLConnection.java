/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.net;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

public abstract class HttpURLConnection extends URLConnection {
  protected String method = "GET";
  protected int responseCode = -1;
  protected String responseMessage = null;
  protected boolean instanceFollowRedirects = true;
  private static boolean followRedirects = true;

  private static final String[] VALID_METHODS = {
    "GET", "POST", "HEAD", "OPTIONS", "PUT", "DELETE", "TRACE"
  };

  public static final int HTTP_OK = 200;
  public static final int HTTP_CREATED = 201;
  public static final int HTTP_ACCEPTED = 202;
  public static final int HTTP_NOT_AUTHORITATIVE = 203;
  public static final int HTTP_NO_CONTENT = 204;
  public static final int HTTP_RESET = 205;
  public static final int HTTP_PARTIAL = 206;

  public static final int HTTP_MULT_CHOICE = 300;
  public static final int HTTP_MOVED_PERM = 301;
  public static final int HTTP_MOVED_TEMP = 302;
  public static final int HTTP_SEE_OTHER = 303;
  public static final int HTTP_NOT_MODIFIED = 304;
  public static final int HTTP_USE_PROXY = 305;

  public static final int HTTP_BAD_REQUEST = 400;
  public static final int HTTP_UNAUTHORIZED = 401;
  public static final int HTTP_PAYMENT_REQUIRED = 402;
  public static final int HTTP_FORBIDDEN = 403;
  public static final int HTTP_NOT_FOUND = 404;
  public static final int HTTP_BAD_METHOD = 405;
  public static final int HTTP_NOT_ACCEPTABLE = 406;
  public static final int HTTP_PROXY_AUTH = 407;
  public static final int HTTP_CLIENT_TIMEOUT = 408;
  public static final int HTTP_CONFLICT = 409;
  public static final int HTTP_GONE = 410;
  public static final int HTTP_LENGTH_REQUIRED = 411;
  public static final int HTTP_PRECON_FAILED = 412;
  public static final int HTTP_ENTITY_TOO_LARGE = 413;
  public static final int HTTP_REQ_TOO_LONG = 414;
  public static final int HTTP_UNSUPPORTED_TYPE = 415;

  public static final int HTTP_SERVER_ERROR = 500;
  public static final int HTTP_INTERNAL_ERROR = 500;
  public static final int HTTP_NOT_IMPLEMENTED = 501;
  public static final int HTTP_BAD_GATEWAY = 502;
  public static final int HTTP_UNAVAILABLE = 503;
  public static final int HTTP_GATEWAY_TIMEOUT = 504;
  public static final int HTTP_VERSION = 505;

  protected HttpURLConnection(URL u) {
    super(u);
  }

  public static void setFollowRedirects(boolean set) {
    followRedirects = set;
  }

  public static boolean getFollowRedirects() {
    return followRedirects;
  }

  public void setInstanceFollowRedirects(boolean followRedirects) {
    this.instanceFollowRedirects = followRedirects;
  }

  public boolean getInstanceFollowRedirects() {
    return instanceFollowRedirects;
  }

  public void setRequestMethod(String method) throws ProtocolException {
    if (connected) {
      throw new ProtocolException("Already connected");
    }
    String m = method.toUpperCase(Locale.ROOT);
    for (String valid : VALID_METHODS) {
      if (valid.equals(m)) {
        this.method = m;
        return;
      }
    }
    throw new ProtocolException("Invalid HTTP method: " + method);
  }

  public String getRequestMethod() {
    return method;
  }

  public int getResponseCode() throws IOException {
    if (responseCode != -1) {
      return responseCode;
    }
    getInputStream();
    String status = getHeaderField(0);
    if (status != null && status.startsWith("HTTP/1.")) {
      int space = status.indexOf(' ');
      if (space > 0) {
        int nextSpace = status.indexOf(' ', space + 1);
        try {
          if (nextSpace > 0) {
            responseCode = Integer.parseInt(status.substring(space + 1, nextSpace));
            responseMessage = status.substring(nextSpace + 1);
          } else {
            responseCode = Integer.parseInt(status.substring(space + 1));
            responseMessage = "";
          }
        } catch (NumberFormatException ignored) {
        }
      }
    }
    return responseCode;
  }

  public String getResponseMessage() throws IOException {
    getResponseCode();
    return responseMessage;
  }

  public abstract void disconnect();

  public abstract boolean usingProxy();

  public void setFixedLengthStreamingMode(int contentLength) {
    setFixedLengthStreamingMode((long) contentLength);
  }

  public void setFixedLengthStreamingMode(long contentLength) {
    if (connected) {
      throw new IllegalStateException("Already connected");
    }
    if (contentLength < 0) {
      throw new IllegalArgumentException("contentLength < 0");
    }
  }

  public void setChunkedStreamingMode(int chunklen) {
    if (connected) {
      throw new IllegalStateException("Already connected");
    }
  }

  public InputStream getErrorStream() {
    return null;
  }
}
