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
import java.security.AllPermission;
import java.security.Permission;
import java.util.Collections;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import sun.net.www.MessageHeader;

public abstract class URLConnection {
  protected URL url;
  protected boolean doInput = true;
  protected boolean doOutput = false;
  private static boolean defaultAllowUserInteraction = false;
  protected boolean allowUserInteraction = defaultAllowUserInteraction;
  private static volatile boolean defaultUseCaches = true;
  private static final ConcurrentHashMap<String, Boolean> defaultCaching = new ConcurrentHashMap<String, Boolean>();
  protected boolean useCaches = defaultUseCaches;
  protected long ifModifiedSince = 0;
  protected boolean connected = false;
  private int connectTimeout;
  private int readTimeout;
  private MessageHeader requests;

  protected URLConnection(URL url) {
    this.url = url;
  }

  public URL getURL() {
    return url;
  }

  public void setConnectTimeout(int timeout) {
    if (timeout < 0) {
      throw new IllegalArgumentException("timeouts can't be negative");
    }
    connectTimeout = timeout;
  }

  public int getConnectTimeout() {
    return connectTimeout;
  }

  public void setReadTimeout(int timeout) {
    if (timeout < 0) {
      throw new IllegalArgumentException("timeouts can't be negative");
    }
    readTimeout = timeout;
  }

  public int getReadTimeout() {
    return readTimeout;
  }

  public int getContentLength() {
    long l = getContentLengthLong();
    if (l > Integer.MAX_VALUE) {
      return -1;
    }
    return (int) l;
  }

  public long getContentLengthLong() {
    return getHeaderFieldLong("content-length", -1);
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

  public String getHeaderField(String name) {
    return null;
  }

  public Map<String, List<String>> getHeaderFields() {
    return Collections.emptyMap();
  }

  public int getHeaderFieldInt(String name, int Default) {
    String value = getHeaderField(name);
    try {
      return Integer.parseInt(value);
    } catch (Exception e) {
      // not a number, use the default
    }
    return Default;
  }

  public long getHeaderFieldLong(String name, long Default) {
    String value = getHeaderField(name);
    try {
      return Long.parseLong(value);
    } catch (Exception e) {
      // not a number, use the default
    }
    return Default;
  }

  @SuppressWarnings("deprecation")
  public long getHeaderFieldDate(String name, long Default) {
    String value = getHeaderField(name);
    try {
      return Date.parse(value);
    } catch (Exception e) {
      // not a date, use the default
    }
    return Default;
  }

  public String getHeaderFieldKey(int n) {
    return null;
  }

  public String getHeaderField(int n) {
    return null;
  }

  public Object getContent() throws IOException {
    getInputStream();
    return getContentHandler().getContent(this);
  }

  public Object getContent(Class<?>[] classes) throws IOException {
    getInputStream();
    return getContentHandler().getContent(this, classes);
  }

  public Permission getPermission() throws IOException {
    return new AllPermission();
  }

  public abstract void connect() throws IOException;

  public InputStream getInputStream() throws IOException {
    throw new UnknownServiceException("protocol doesn't support input");
  }

  public OutputStream getOutputStream() throws IOException {
    throw new UnknownServiceException("protocol doesn't support output");
  }

  @Override
  public String toString() {
    return this.getClass().getName() + ":" + url;
  }

  public void setDoInput(boolean doinput) {
    checkConnected();
    doInput = doinput;
  }

  public boolean getDoInput() {
    return doInput;
  }

  public void setDoOutput(boolean dooutput) {
    checkConnected();
    doOutput = dooutput;
  }

  public boolean getDoOutput() {
    return doOutput;
  }

  public void setAllowUserInteraction(boolean allowuserinteraction) {
    checkConnected();
    allowUserInteraction = allowuserinteraction;
  }

  public boolean getAllowUserInteraction() {
    return allowUserInteraction;
  }

  public static void setDefaultAllowUserInteraction(boolean defaultallowuserinteraction) {
    defaultAllowUserInteraction = defaultallowuserinteraction;
  }

  public static boolean getDefaultAllowUserInteraction() {
    return defaultAllowUserInteraction;
  }

  public void setUseCaches(boolean usecaches) {
    checkConnected();
    useCaches = usecaches;
  }

  public boolean getUseCaches() {
    return useCaches;
  }

  public void setIfModifiedSince(long ifmodifiedsince) {
    checkConnected();
    ifModifiedSince = ifmodifiedsince;
  }

  public long getIfModifiedSince() {
    return ifModifiedSince;
  }

  public boolean getDefaultUseCaches() {
    return defaultUseCaches;
  }

  public void setDefaultUseCaches(boolean defaultusecaches) {
    defaultUseCaches = defaultusecaches;
  }

  public static void setDefaultUseCaches(String protocol, boolean defaultVal) {
    if (protocol == null) {
      throw new NullPointerException("protocol is null");
    }
    if (protocol.isEmpty()) {
      throw new IllegalArgumentException("protocol is empty");
    }
    defaultCaching.put(protocol.toLowerCase(java.util.Locale.ROOT), Boolean.valueOf(defaultVal));
  }

  public static boolean getDefaultUseCaches(String protocol) {
    Boolean protoDefault = defaultCaching.get(protocol.toLowerCase(java.util.Locale.ROOT));
    if (protoDefault != null) {
      return protoDefault.booleanValue();
    }
    return defaultUseCaches;
  }

  public void setRequestProperty(String key, String value) {
    checkConnected();
    if (key == null) {
      throw new NullPointerException("key is null");
    }
    if (requests == null) {
      requests = new MessageHeader();
    }
    requests.set(key, value);
  }

  public void addRequestProperty(String key, String value) {
    checkConnected();
    if (key == null) {
      throw new NullPointerException("key is null");
    }
    if (requests == null) {
      requests = new MessageHeader();
    }
    requests.add(key, value);
  }

  public String getRequestProperty(String key) {
    checkConnected();
    if (requests == null) {
      return null;
    }
    return requests.findValue(key);
  }

  public Map<String, List<String>> getRequestProperties() {
    checkConnected();
    if (requests == null) {
      return Collections.emptyMap();
    }
    return requests.getHeaders(null);
  }

  private static Hashtable<String, ContentHandler> handlers = new Hashtable<String, ContentHandler>();
  private static volatile ContentHandlerFactory factory;

  public static synchronized void setContentHandlerFactory(ContentHandlerFactory fac) {
    if (factory != null) {
      throw new Error("factory already defined");
    }
    factory = fac;
  }

  private ContentHandler getContentHandler() throws UnknownServiceException {
    String contentType = stripOffParameters(getContentType());
    ContentHandler handler = null;
    if (contentType == null) {
      throw new UnknownServiceException("no content-type");
    }
    try {
      handler = handlers.get(contentType);
      if (handler != null) {
        return handler;
      }
    } catch (Exception e) {
      // fall through to a fresh lookup
    }
    if (factory != null) {
      handler = factory.createContentHandler(contentType);
    }
    if (handler == null) {
      try {
        handler = lookupContentHandlerClassFor(contentType);
      } catch (Exception e) {
        handler = UnknownContentHandler.kInstance;
      }
      handlers.put(contentType, handler);
    }
    return handler;
  }

  private String stripOffParameters(String contentType) {
    if (contentType == null) {
      return null;
    }
    int index = contentType.indexOf(';');
    if (index > 0) {
      return contentType.substring(0, index);
    }
    return contentType;
  }

  private ContentHandler lookupContentHandlerClassFor(String contentType) {
    String contentHandlerClassName = typeToPackageName(contentType);
    String packagePrefixList = getContentHandlerPkgPrefixes();
    java.util.StringTokenizer packagePrefixIter = new java.util.StringTokenizer(packagePrefixList, "|");
    while (packagePrefixIter.hasMoreTokens()) {
      String packagePrefix = packagePrefixIter.nextToken().trim();
      try {
        String clsName = packagePrefix + "." + contentHandlerClassName;
        Class<?> cls = null;
        try {
          cls = Class.forName(clsName);
        } catch (ClassNotFoundException e) {
          ClassLoader cl = ClassLoader.getSystemClassLoader();
          if (cl != null) {
            cls = cl.loadClass(clsName);
          }
        }
        if (cls != null) {
          return (ContentHandler) cls.getDeclaredConstructor().newInstance();
        }
      } catch (Exception e) {
        // try the next prefix
      }
    }
    return UnknownContentHandler.kInstance;
  }

  private String typeToPackageName(String contentType) {
    contentType = contentType.toLowerCase();
    int len = contentType.length();
    char[] nm = new char[len];
    contentType.getChars(0, len, nm, 0);
    for (int i = 0; i < len; i++) {
      char c = nm[i];
      if (c == '/') {
        nm[i] = '.';
      } else if (!('A' <= c && c <= 'Z' || 'a' <= c && c <= 'z' || '0' <= c && c <= '9')) {
        nm[i] = '_';
      }
    }
    return new String(nm);
  }

  private String getContentHandlerPkgPrefixes() {
    String packagePrefixList = System.getProperty("java.content.handler.pkgs", "");
    if (!packagePrefixList.isEmpty()) {
      packagePrefixList += "|";
    }
    return packagePrefixList + "sun.net.www.content";
  }

  public static String guessContentTypeFromName(String fname) {
    return getFileNameMap().getContentTypeFor(fname);
  }

  private void checkConnected() {
    if (connected) {
      throw new IllegalStateException("Already connected");
    }
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
