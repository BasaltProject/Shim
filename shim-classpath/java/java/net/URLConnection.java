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

package java.net;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public abstract class URLConnection {
  protected final URL url;
  protected boolean doInput = true;
  protected boolean doOutput = false;
  protected boolean useCaches = true;
  protected boolean connected = false;
  protected boolean allowUserInteraction = false;
  protected long ifModifiedSince = 0;

  private static boolean defaultAllowUserInteraction = false;
  private static boolean defaultUseCaches = true;

  private int connectTimeout = 0;
  private int readTimeout = 0;
  private final Map<String, List<String>> requestProperties = new HashMap<String, List<String>>();

  protected URLConnection(URL url) {
    this.url = url;
  }

  public URL getURL() {
    return url;
  }

  public int getConnectTimeout() {
    return connectTimeout;
  }

  public void setConnectTimeout(int timeout) {
    if (timeout < 0) {
      throw new IllegalArgumentException("timeout < 0");
    }
    this.connectTimeout = timeout;
  }

  public int getReadTimeout() {
    return readTimeout;
  }

  public void setReadTimeout(int timeout) {
    if (timeout < 0) {
      throw new IllegalArgumentException("timeout < 0");
    }
    this.readTimeout = timeout;
  }

  public void setRequestProperty(String key, String value) {
    if (connected) {
      throw new IllegalStateException("Already connected");
    }
    Objects.requireNonNull(key, "key");
    List<String> list = new ArrayList<String>();
    if (value != null) {
      list.add(value);
    }
    requestProperties.put(key, list);
  }

  public void addRequestProperty(String key, String value) {
    if (connected) {
      throw new IllegalStateException("Already connected");
    }
    Objects.requireNonNull(key, "key");
    List<String> list = requestProperties.get(key);
    if (list == null) {
      list = new ArrayList<String>();
      requestProperties.put(key, list);
    }
    if (value != null) {
      list.add(value);
    }
  }

  public String getRequestProperty(String key) {
    if (connected) {
      throw new IllegalStateException("Already connected");
    }
    if (key == null) return null;
    List<String> list = requestProperties.get(key);
    return (list != null && !list.isEmpty()) ? list.get(list.size() - 1) : null;
  }

  public Map<String, List<String>> getRequestProperties() {
    if (connected) {
      throw new IllegalStateException("Already connected");
    }
    Map<String, List<String>> copy = new HashMap<String, List<String>>();
    for (Map.Entry<String, List<String>> entry : requestProperties.entrySet()) {
      copy.put(entry.getKey(), Collections.unmodifiableList(new ArrayList<String>(entry.getValue())));
    }
    return Collections.unmodifiableMap(copy);
  }

  public Object getContent() throws IOException {
    return getInputStream();
  }

  public int getContentLength() {
    long len = getContentLengthLong();
    return len > Integer.MAX_VALUE ? -1 : (int) len;
  }

  public long getContentLengthLong() {
    return getHeaderFieldLong("content-length", -1L);
  }

  public String getContentType() {
    return getHeaderField("content-type");
  }

  public String getContentEncoding() {
    return getHeaderField("content-encoding");
  }

  public long getExpiration() {
    return getHeaderFieldDate("expires", 0);
  }

  public long getDate() {
    return getHeaderFieldDate("date", 0);
  }

  public long getLastModified() {
    return getHeaderFieldDate("last-modified", 0);
  }

  public abstract void connect() throws IOException;

  public InputStream getInputStream() throws IOException {
    throw new UnknownServiceException();
  }

  public OutputStream getOutputStream() throws IOException {
    throw new UnknownServiceException();
  }

  public boolean getDoInput() {
    return doInput;
  }

  public boolean getDoOutput() {
    return doOutput;
  }

  public void setDoInput(boolean v) {
    if (connected) {
      throw new IllegalStateException("Already connected");
    }
    doInput = v;
  }

  public void setDoOutput(boolean v) {
    if (connected) {
      throw new IllegalStateException("Already connected");
    }
    doOutput = v;
  }

  public void setUseCaches(boolean v) {
    if (connected) {
      throw new IllegalStateException("Already connected");
    }
    useCaches = v;
  }

  public boolean getUseCaches() {
    return useCaches;
  }

  public void setIfModifiedSince(long ifModifiedSince) {
    if (connected) {
      throw new IllegalStateException("Already connected");
    }
    this.ifModifiedSince = ifModifiedSince;
  }

  public long getIfModifiedSince() {
    return ifModifiedSince;
  }

  public boolean getDefaultUseCaches() {
    return defaultUseCaches;
  }

  public void setDefaultUseCaches(boolean defaultVal) {
    defaultUseCaches = defaultVal;
  }

  public void setAllowUserInteraction(boolean allowUserInteraction) {
    if (connected) {
      throw new IllegalStateException("Already connected");
    }
    this.allowUserInteraction = allowUserInteraction;
  }

  public boolean getAllowUserInteraction() {
    return allowUserInteraction;
  }

  public static void setDefaultAllowUserInteraction(boolean defaultVal) {
    defaultAllowUserInteraction = defaultVal;
  }

  public static boolean getDefaultAllowUserInteraction() {
    return defaultAllowUserInteraction;
  }

  public String getHeaderField(String name) {
    String result = null;
    if (name != null) {
      List<String> values = getHeaderFields().get(name.toLowerCase());
      if (values != null && !values.isEmpty()) {
        result = values.get(values.size() - 1);
      }
    }
    return result;
  }

  public String getHeaderField(int n) {
    return null;
  }

  public String getHeaderFieldKey(int n) {
    return null;
  }

  public int getHeaderFieldInt(String name, int Default) {
    return (int) getHeaderFieldLong(name, Default);
  }

  public long getHeaderFieldLong(String name, long Default) {
    long result = Default;
    try {
      String val = getHeaderField(name);
      if (val != null) {
        result = Long.parseLong(val.trim());
      }
    } catch (Exception ignored) {
    }
    return result;
  }

  public long getHeaderFieldDate(String name, long Default) {
    return Default;
  }

  public Map<String, List<String>> getHeaderFields() {
    return Collections.emptyMap();
  }

  public static String guessContentTypeFromName(String fname) {
    if (fname == null) return null;
    String lower = fname.toLowerCase();
    if (lower.endsWith(".html") || lower.endsWith(".htm")) return "text/html";
    if (lower.endsWith(".txt")) return "text/plain";
    if (lower.endsWith(".json")) return "application/json";
    if (lower.endsWith(".png")) return "image/png";
    if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
    if (lower.endsWith(".gif")) return "image/gif";
    if (lower.endsWith(".xml")) return "application/xml";
    if (lower.endsWith(".zip")) return "application/zip";
    return null;
  }

  public static String guessContentTypeFromStream(InputStream is) throws IOException {
    return null;
  }
}
