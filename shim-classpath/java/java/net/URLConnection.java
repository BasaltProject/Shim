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

  public Object getContent(Class<?>[] classes) throws IOException {
    Object content = getContent();
    for (Class<?> c : classes) {
      if (c.isInstance(content)) {
        return content;
      }
    }
    return null;
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

  private static volatile FileNameMap fileNameMap;

  public static FileNameMap getFileNameMap() {
    FileNameMap map = fileNameMap;
    if (map == null) {
      map = new FileNameMap() {
        public String getContentTypeFor(String fileName) {
          int i = fileName.lastIndexOf('#');
          if (i > 0) {
            fileName = fileName.substring(0, i - 1);
          }
          i = fileName.lastIndexOf('.');
          i = Math.max(i, fileName.lastIndexOf('/'));
          i = Math.max(i, fileName.lastIndexOf('?'));
          String ext = "";
          if (i != -1 && fileName.charAt(i) == '.') {
            ext = fileName.substring(i).toLowerCase(java.util.Locale.ROOT);
          }
          String[] entries = ContentTypesData.kEntries;
          for (int k = 0; k < entries.length; k += 2) {
            if (entries[k].equals(ext)) {
              return entries[k + 1];
            }
          }
          return null;
        }
      };
      fileNameMap = map;
    }
    return map;
  }

  public static void setFileNameMap(FileNameMap map) {
    fileNameMap = map;
  }

  public static String guessContentTypeFromName(String fname) {
    return getFileNameMap().getContentTypeFor(fname);
  }

  public static String guessContentTypeFromStream(InputStream is) throws IOException {
    if (!is.markSupported()) {
      return null;
    }
    is.mark(16);
    int c1 = is.read();
    int c2 = is.read();
    int c3 = is.read();
    int c4 = is.read();
    int c5 = is.read();
    int c6 = is.read();
    int c7 = is.read();
    int c8 = is.read();
    int c9 = is.read();
    int c10 = is.read();
    int c11 = is.read();
    int c12 = is.read();
    int c13 = is.read();
    int c14 = is.read();
    int c15 = is.read();
    int c16 = is.read();
    is.reset();
    if (c1 == 0xCA && c2 == 0xFE && c3 == 0xBA && c4 == 0xBE) {
      return "application/java-vm";
    }
    if (c1 == 0xAC && c2 == 0xED) {
      return "application/x-java-serialized-object";
    }
    if (c1 == '<') {
      if (c2 == '!' || ((c2 == 'h' && (c3 == 't' && c4 == 'm' || c3 == 'e' && c4 == 'a'))
          || (c2 == 'b' && c3 == 'o' && c4 == 'd') || (c2 == 'H' && (c3 == 'T' && c4 == 'M' || c3 == 'E' && c4 == 'A'))
          || (c2 == 'B' && c3 == 'O' && c4 == 'D'))) {
        return "text/html";
      }
      if (c2 == '?' && c3 == 'x' && c4 == 'm' && c5 == 'l' && c6 == ' ') {
        return "application/xml";
      }
    }
    if (c1 == 0xef && c2 == 0xbb && c3 == 0xbf) {
      if (c4 == '<' && c5 == '?' && c6 == 'x') {
        return "application/xml";
      }
    }
    if (c1 == 0xfe && c2 == 0xff) {
      if (c3 == 0 && c4 == '<' && c5 == 0 && c6 == '?' && c7 == 0 && c8 == 'x') {
        return "application/xml";
      }
    }
    if (c1 == 0xff && c2 == 0xfe) {
      if (c3 == '<' && c4 == 0 && c5 == '?' && c6 == 0 && c7 == 'x' && c8 == 0) {
        return "application/xml";
      }
    }
    if (c1 == 0x00 && c2 == 0x00 && c3 == 0xfe && c4 == 0xff) {
      if (c5 == 0 && c6 == 0 && c7 == 0 && c8 == '<' && c9 == 0 && c10 == 0 && c11 == 0 && c12 == '?' && c13 == 0
          && c14 == 0 && c15 == 0 && c16 == 'x') {
        return "application/xml";
      }
    }
    if (c1 == 0xff && c2 == 0xfe && c3 == 0x00 && c4 == 0x00) {
      if (c5 == '<' && c6 == 0 && c7 == 0 && c8 == 0 && c9 == '?' && c10 == 0 && c11 == 0 && c12 == 0 && c13 == 'x'
          && c14 == 0 && c15 == 0 && c16 == 0) {
        return "application/xml";
      }
    }
    if (c1 == 'G' && c2 == 'I' && c3 == 'F' && c4 == '8') {
      return "image/gif";
    }
    if (c1 == '#' && c2 == 'd' && c3 == 'e' && c4 == 'f') {
      return "image/x-bitmap";
    }
    if (c1 == '!' && c2 == ' ' && c3 == 'X' && c4 == 'P' && c5 == 'M' && c6 == '2') {
      return "image/x-pixmap";
    }
    if (c1 == 137 && c2 == 80 && c3 == 78 && c4 == 71 && c5 == 13 && c6 == 10 && c7 == 26 && c8 == 10) {
      return "image/png";
    }
    if (c1 == 0xFF && c2 == 0xD8 && c3 == 0xFF) {
      if (c4 == 0xE0 || c4 == 0xEE) {
        return "image/jpeg";
      }
      if ((c4 == 0xE1) && (c7 == 'E' && c8 == 'x' && c9 == 'i' && c10 == 'f' && c11 == 0)) {
        return "image/jpeg";
      }
    }
    if (c1 == 0x2E && c2 == 0x73 && c3 == 0x6E && c4 == 0x64) {
      return "audio/basic";
    }
    if (c1 == 0x64 && c2 == 0x6E && c3 == 0x73 && c4 == 0x2E) {
      return "audio/basic";
    }
    if (c1 == 'R' && c2 == 'I' && c3 == 'F' && c4 == 'F') {
      return "audio/x-wav";
    }
    return null;
  }
}
