/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.net.www.protocol.http;

import java.io.FileNotFoundException;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Authenticator;
import java.net.CookieHandler;
import java.net.HttpRetryException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.ProtocolException;
import java.net.ProxySelector;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TimeZone;
import sun.net.www.MessageHeader;
import sun.net.www.http.ChunkedOutputStream;
import sun.net.www.http.HttpClient;
import sun.net.www.http.PosterOutputStream;

public class HttpURLConnection extends java.net.HttpURLConnection {
  static final String kVersion = System.getProperty("java.version");
  public static final String userAgent = computeUserAgent();
  static final int kMaxRedirects = Integer.getInteger("http.maxRedirects", 20);
  static final int kMaxHeaderSize = Integer.getInteger("http.maxHeaderSize", 393216);
  private static final boolean kAllowRestrictedHeaders = Boolean.getBoolean("sun.net.http.allowRestrictedHeaders");
  private static final Set<String> kRestrictedHeaderSet = new HashSet<String>();
  private static final String[] kRestrictedHeaders = {
    "Access-Control-Request-Headers", "Access-Control-Request-Method", "Connection", "Content-Length",
    "Content-Transfer-Encoding", "Host", "Keep-Alive", "Origin", "Trailer", "Transfer-Encoding", "Upgrade", "Via",
  };
  private static final String[] kExcludeHeaders = {"Proxy-Authorization", "Authorization"};
  private static final String[] kExcludeHeaders2 = {"Proxy-Authorization", "Authorization", "Cookie", "Cookie2"};
  static final String kAcceptString = "*/*";

  static {
    for (String h : kRestrictedHeaders) {
      kRestrictedHeaderSet.add(h.toLowerCase());
    }
  }

  protected HttpClient http;
  protected Handler handler;
  protected Proxy instProxy;
  protected volatile Authenticator authenticator;
  private CookieHandler cookieHandler;
  private boolean usingProxy;
  private PrintStream ps;
  private InputStream errorStream;
  private boolean setUserCookies;
  private String userCookies;
  private String userCookies2;
  private MessageHeader requests;
  private final MessageHeader userHeaders;
  private boolean connecting;
  private MessageHeader responses;
  private InputStream inputStream;
  private PosterOutputStream poster;
  private boolean setRequests;
  private Exception rememberedException;
  private StreamingOutputStream strOutputStream;
  private int connectTimeout = -1;
  private int readTimeout = -1;
  String requestURI;
  private AuthenticationInfo currentServerCredentials;
  private AuthenticationInfo currentProxyCredentials;

  private static String computeUserAgent() {
    String agent = System.getProperty("http.agent");
    String version = System.getProperty("java.version");
    if (agent == null) {
      return "Java/" + version;
    }
    return agent + " Java/" + version;
  }

  protected HttpURLConnection(URL u, Handler handler) throws IOException {
    this(u, null, handler);
  }

  public HttpURLConnection(URL u, String host, int port) throws IOException {
    this(u, new Proxy(Proxy.Type.HTTP, InetSocketAddress.createUnresolved(host, port)));
  }

  public HttpURLConnection(URL u, Proxy p) throws IOException {
    this(u, p, new Handler());
  }

  protected HttpURLConnection(URL u, Proxy p, Handler handler) throws IOException {
    super(checkURL(u));
    requests = new MessageHeader();
    responses = new MessageHeader(kMaxHeaderSize);
    userHeaders = new MessageHeader();
    this.handler = handler;
    instProxy = p;
    cookieHandler = CookieHandler.getDefault();
  }

  private static URL checkURL(URL u) throws IOException {
    if (u != null) {
      if (u.toExternalForm().indexOf('\n') > -1) {
        throw new IOException("Illegal character in URL");
      }
    }
    return u;
  }

  @Override
  public void setAuthenticator(Authenticator auth) {
    if (connected || connecting) {
      throw new IllegalStateException("Authenticator must be set before connecting");
    }
    authenticator = Objects.requireNonNull(auth);
  }

  private static void checkMessageHeader(String key, String value) {
    char lf = '\n';
    int index = key.indexOf(lf);
    int index1 = key.indexOf('\r');
    if (index != -1 || index1 != -1) {
      throw new IllegalArgumentException("Illegal character(s) in message header field: " + key);
    }
    if (value == null) {
      return;
    }
    index = value.indexOf(lf);
    while (index != -1) {
      index++;
      if (index < value.length()) {
        char c = value.charAt(index);
        if ((c == ' ') || (c == '\t')) {
          index = value.indexOf(lf, index);
          continue;
        }
      }
      throw new IllegalArgumentException("Illegal character(s) in message header value: " + value);
    }
  }

  private boolean isRestrictedHeader(String key, String value) {
    if (kAllowRestrictedHeaders) {
      return false;
    }
    key = key.toLowerCase();
    if (kRestrictedHeaderSet.contains(key)) {
      if (key.equals("connection") && value.equalsIgnoreCase("close")) {
        return false;
      }
      return true;
    } else if (key.startsWith("sec-")) {
      return true;
    }
    return false;
  }

  private boolean isExternalMessageHeaderAllowed(String key, String value) {
    checkMessageHeader(key, value);
    return !isRestrictedHeader(key, value);
  }

  @Override
  public synchronized void setRequestMethod(String method) throws ProtocolException {
    if (connected || connecting) {
      throw new IllegalStateException("Already connected");
    }
    super.setRequestMethod(method);
  }

  @Override
  public void connect() throws IOException {
    synchronized (this) {
      if (connected) {
        return;
      }
      plainConnect();
    }
  }

  protected void plainConnect() throws IOException {
    if (connected) {
      return;
    }
    if (super.url.getHost() == null || super.url.getHost().isEmpty()) {
      throw new UnknownHostException();
    }
    connecting = true;
    try {
      plainConnect0();
    } finally {
      connecting = false;
    }
  }

  protected void plainConnect0() throws IOException {
    if (instProxy == null) {
      ProxySelector sel = ProxySelector.getDefault();
      if (sel != null) {
        URI uri = toURI(url);
        List<Proxy> proxies = uri == null ? null : sel.select(uri);
        Iterator<Proxy> it = proxies == null ? Collections.<Proxy>emptyList().iterator() : proxies.iterator();
        if (!it.hasNext()) {
          http = getNewHttpClient(url, null, connectTimeout, true);
          http.setReadTimeout(readTimeout);
        }
        while (it.hasNext()) {
          Proxy p = it.next();
          try {
            http = getNewHttpClient(url, p, connectTimeout, true);
            http.setReadTimeout(readTimeout);
            break;
          } catch (IOException ioex) {
            if (p != Proxy.NO_PROXY && p.address() != null) {
              sel.connectFailed(uri, p.address(), ioex);
              if (!it.hasNext()) {
                http = getNewHttpClient(url, null, connectTimeout, false);
                http.setReadTimeout(readTimeout);
                break;
              }
            } else {
              throw ioex;
            }
          }
        }
      } else {
        http = getNewHttpClient(url, null, connectTimeout, true);
        http.setReadTimeout(readTimeout);
      }
    } else {
      http = getNewHttpClient(url, instProxy, connectTimeout, true);
      http.setReadTimeout(readTimeout);
    }
    usingProxy = http.getUsingProxy();
    ps = http.getOutputStream();
    connected = true;
  }

  protected HttpClient getNewHttpClient(URL url, Proxy p, int connectTimeout, boolean useCache) throws IOException {
    return HttpClient.New(url, p, connectTimeout, useCache);
  }

  static URI toURI(URL url) {
    try {
      return url.toURI();
    } catch (URISyntaxException | RuntimeException e) {
      return null;
    }
  }

  @Override
  public OutputStream getOutputStream() throws IOException {
    connecting = true;
    try {
      return getOutputStream0();
    } finally {
      connecting = false;
    }
  }

  private synchronized OutputStream getOutputStream0() throws IOException {
    try {
      if (!doOutput) {
        throw new ProtocolException("cannot write to a URLConnection if doOutput=false - call setDoOutput(true)");
      }
      if (method.equals("GET")) {
        method = "POST";
      }
      if ("TRACE".equals(method) && "http".equals(url.getProtocol())) {
        throw new ProtocolException("HTTP method TRACE doesn't support output");
      }
      if (inputStream != null) {
        throw new ProtocolException("Cannot write output after reading input.");
      }
      if (!connected) {
        connect();
      }
      if (streaming()) {
        if (strOutputStream == null) {
          if (chunkLength != -1) {
            strOutputStream = new StreamingOutputStream(new ChunkedOutputStream(ps, chunkLength), -1L);
          } else {
            long length = 0L;
            if (fixedContentLengthLong != -1) {
              length = fixedContentLengthLong;
            } else if (fixedContentLength != -1) {
              length = fixedContentLength;
            }
            strOutputStream = new StreamingOutputStream(ps, length);
          }
          writeRequests();
        }
        return strOutputStream;
      }
      if (poster == null) {
        poster = new PosterOutputStream();
      }
      return poster;
    } catch (RuntimeException e) {
      disconnectInternal();
      throw e;
    } catch (ProtocolException e) {
      int i = responseCode;
      disconnectInternal();
      responseCode = i;
      throw e;
    } catch (IOException e) {
      disconnectInternal();
      throw e;
    }
  }

  public boolean streaming() {
    return fixedContentLength != -1 || fixedContentLengthLong != -1 || chunkLength != -1;
  }

  String getRequestURI() throws IOException {
    if (requestURI == null) {
      boolean useProxy = usingProxy();
      String file = url.getFile();
      if (file == null || file.isEmpty()) {
        file = "/";
      }
      if (file.indexOf('\n') != -1) {
        throw new MalformedURLException("Illegal character in URL");
      }
      if (useProxy && !"https".equalsIgnoreCase(url.getProtocol())) {
        StringBuilder sb = new StringBuilder(url.getProtocol()).append("://").append(url.getHost());
        if (url.getPort() != -1) {
          sb.append(':').append(url.getPort());
        }
        requestURI = sb.append(file).toString();
      } else {
        requestURI = file;
      }
    }
    return requestURI;
  }

  static String stripIPv6ZoneId(String host) {
    if (host.charAt(0) != '[') {
      return host;
    }
    int i = host.indexOf('%');
    if (i == -1) {
      return host;
    }
    return host.substring(0, i) + "]";
  }

  private void writeRequests() throws IOException {
    if (http.getUsingProxy() && !"https".equalsIgnoreCase(url.getProtocol())) {
      setPreemptiveProxyAuthentication(requests);
    }
    if (!setRequests) {
      String requestLine = method + " " + getRequestURI() + " HTTP/1.1";
      if (requests.getKey(requestLine) != 0) {
        requests.prepend(requestLine, null);
      }
      if (!getUseCaches()) {
        requests.setIfNotSet("Cache-Control", "no-cache");
        requests.setIfNotSet("Pragma", "no-cache");
      }
      requests.setIfNotSet("User-Agent", userAgent);
      int port = url.getPort();
      String host = stripIPv6ZoneId(url.getHost());
      if (port != -1 && port != url.getDefaultPort()) {
        host += ":" + port;
      }
      requests.setIfNotSet("Host", host);
      requests.setIfNotSet("Accept", kAcceptString);
      if (http.getHttpKeepAliveSet()) {
        if (http.getUsingProxy() && !"https".equalsIgnoreCase(url.getProtocol())) {
          requests.setIfNotSet("Proxy-Connection", "keep-alive");
        } else {
          requests.setIfNotSet("Connection", "keep-alive");
        }
      } else {
        requests.setIfNotSet("Connection", "close");
      }
      long modTime = getIfModifiedSince();
      if (modTime != 0) {
        Date date = new Date(modTime);
        SimpleDateFormat fo = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss 'GMT'", Locale.US);
        fo.setTimeZone(TimeZone.getTimeZone("GMT"));
        requests.setIfNotSet("If-Modified-Since", fo.format(date));
      }
      AuthenticationInfo sauth = AuthenticationInfo.getServerAuth(url);
      if (sauth != null && sauth.supportsPreemptiveAuthorization()) {
        requests.setIfNotSet(sauth.getHeaderName(), sauth.getHeaderValue(url, method));
        currentServerCredentials = sauth;
      }
      if (!method.equals("PUT") && (poster != null || streaming())) {
        requests.setIfNotSet("Content-type", "application/x-www-form-urlencoded");
      }
      boolean chunked = false;
      if (streaming()) {
        if (chunkLength != -1) {
          requests.set("Transfer-Encoding", "chunked");
          chunked = true;
        } else {
          if (fixedContentLengthLong != -1) {
            requests.set("Content-Length", String.valueOf(fixedContentLengthLong));
          } else if (fixedContentLength != -1) {
            requests.set("Content-Length", String.valueOf(fixedContentLength));
          }
        }
      } else if (poster != null) {
        synchronized (poster) {
          try {
            poster.close();
          } catch (IOException e) {
            // closing the in-memory buffer cannot fail
          }
          requests.set("Content-Length", String.valueOf(poster.size()));
        }
      }
      if (!chunked) {
        if (requests.findValue("Transfer-Encoding") != null) {
          requests.remove("Transfer-Encoding");
        }
      }
      setCookieHeader();
      setRequests = true;
    }
    http.writeRequests(requests, poster, streaming());
    if (ps.checkError()) {
      String proxyHost = http.getProxyHostUsed();
      int proxyPort = http.getProxyPortUsed();
      disconnectInternal();
      throw new IOException("Error writing to server");
    }
  }

  private void setCookieHeader() throws IOException {
    if (cookieHandler != null) {
      if (!setUserCookies) {
        setUserCookies = true;
        userCookies = requests.findValue("Cookie");
        userCookies2 = requests.findValue("Cookie2");
      }
      requests.remove("Cookie");
      requests.remove("Cookie2");
      URI uri = toURI(url);
      if (uri != null) {
        Map<String, List<String>> cookies = cookieHandler.get(uri, requests.getHeaders(kExcludeHeaders));
        if (!cookies.isEmpty()) {
          for (Map.Entry<String, List<String>> entry : cookies.entrySet()) {
            String key = entry.getKey();
            if (!"Cookie".equalsIgnoreCase(key) && !"Cookie2".equalsIgnoreCase(key)) {
              continue;
            }
            List<String> l = entry.getValue();
            if (l != null && !l.isEmpty()) {
              StringBuilder cookieValue = new StringBuilder();
              for (String value : l) {
                cookieValue.append(value).append("; ");
              }
              try {
                requests.add(key, cookieValue.substring(0, cookieValue.length() - 2));
              } catch (StringIndexOutOfBoundsException ignored) {
                // empty cookie list
              }
            }
          }
        }
      }
      if (userCookies != null) {
        int i;
        if ((i = requests.getKey("Cookie")) != -1) {
          requests.set("Cookie", requests.getValue(i) + ";" + userCookies);
        } else {
          requests.set("Cookie", userCookies);
        }
      }
      if (userCookies2 != null) {
        int i;
        if ((i = requests.getKey("Cookie2")) != -1) {
          requests.set("Cookie2", requests.getValue(i) + ";" + userCookies2);
        } else {
          requests.set("Cookie2", userCookies2);
        }
      }
    }
  }

  @Override
  public synchronized InputStream getInputStream() throws IOException {
    connecting = true;
    try {
      return getInputStream0();
    } finally {
      connecting = false;
    }
  }

  private PasswordAuthentication requestPassword(String host, InetAddress addr, int port, String protocol,
      String prompt, String scheme, URL url, Authenticator.RequestorType reqType) {
    return Authenticator.requestPasswordAuthentication(authenticator, host, addr, port, protocol, prompt, scheme, url,
        reqType);
  }

  private synchronized InputStream getInputStream0() throws IOException {
    if (!doInput) {
      throw new ProtocolException("Cannot read from URLConnection if doInput=false (call setDoInput(true))");
    }
    if (rememberedException != null) {
      if (rememberedException instanceof RuntimeException) {
        throw new RuntimeException(rememberedException);
      } else {
        throw getChainedException((IOException) rememberedException);
      }
    }
    if (inputStream != null) {
      return inputStream;
    }
    if (streaming()) {
      if (strOutputStream == null) {
        getOutputStream();
      }
      strOutputStream.close();
      if (!strOutputStream.writtenOK()) {
        throw new IOException("Incomplete output stream");
      }
    }
    int redirects = 0;
    AuthenticationInfo serverAuthentication = null;
    AuthenticationInfo proxyAuthentication = null;
    try {
      do {
        if (!connected) {
          connect();
        }
        ps = http.getOutputStream();
        if (!streaming() || !setRequests) {
          writeRequests();
        }
        http.parseHTTP(responses, this);
        inputStream = http.getInputStream();
        int respCode = getResponseCode();
        if (respCode == -1) {
          disconnectInternal();
          throw new IOException("Invalid Http response");
        }
        if (cookieHandler != null) {
          URI uri = toURI(url);
          if (uri != null) {
            cookieHandler.put(uri, responses.getHeaders());
          }
        }
        if (respCode == HTTP_PROXY_AUTH && http.getUsingProxy() && !"https".equalsIgnoreCase(url.getProtocol())) {
          AuthenticationHeader hdr = new AuthenticationHeader("Proxy-Authenticate", responses);
          if (proxyAuthentication != null && proxyAuthentication.isAuthorizationStale(hdr.raw() == null ? "" : hdr.raw())) {
            disconnectWeb();
            redirects++;
            requests.set(proxyAuthentication.getHeaderName(), proxyAuthentication.getHeaderValue(url, method));
            currentProxyCredentials = proxyAuthentication;
            setCookieHeader();
            continue;
          }
          if (proxyAuthentication != null) {
            proxyAuthentication.removeFromCache();
          }
          proxyAuthentication = authenticate(hdr, true);
          currentProxyCredentials = proxyAuthentication;
          if (proxyAuthentication != null) {
            disconnectWeb();
            redirects++;
            setCookieHeader();
            continue;
          }
        }
        if (respCode == HTTP_UNAUTHORIZED) {
          AuthenticationHeader srvHdr = new AuthenticationHeader("WWW-Authenticate", responses);
          String raw = srvHdr.raw();
          if (serverAuthentication != null) {
            if (raw != null && serverAuthentication.isAuthorizationStale(raw)) {
              disconnectWeb();
              redirects++;
              requests.set(serverAuthentication.getHeaderName(), serverAuthentication.getHeaderValue(url, method));
              currentServerCredentials = serverAuthentication;
              setCookieHeader();
              continue;
            } else {
              serverAuthentication.removeFromCache();
            }
          } else if (currentServerCredentials != null) {
            currentServerCredentials.removeFromCache();
          }
          serverAuthentication = authenticate(srvHdr, false);
          currentServerCredentials = serverAuthentication;
          if (serverAuthentication != null) {
            disconnectWeb();
            redirects++;
            setCookieHeader();
            continue;
          }
        }
        if (serverAuthentication != null) {
          if (serverAuthentication instanceof BasicAuthentication) {
            String npath = AuthenticationInfo.reducePath(url.getPath());
            String opath = serverAuthentication.path;
            if (!opath.startsWith(npath) || npath.length() >= opath.length()) {
              npath = BasicAuthentication.getRootPath(opath, npath);
            }
            BasicAuthentication a = (BasicAuthentication) serverAuthentication.clone();
            serverAuthentication.removeFromCache();
            a.path = npath;
            serverAuthentication = a;
          }
          serverAuthentication.addToCache();
        }
        if (proxyAuthentication != null) {
          proxyAuthentication.addToCache();
        }
        if (followRedirect()) {
          redirects++;
          setCookieHeader();
          continue;
        }
        int cl = -1;
        try {
          cl = getContentLength();
        } catch (Exception ignored) {
          // unknown length
        }
        if (respCode >= 400) {
          if (respCode == 404 || respCode == 410) {
            errorStream = inputStream;
            throw new FileNotFoundException(url.toString());
          } else {
            errorStream = inputStream;
            throw new IOException("Server returned HTTP response code: " + respCode + " for URL: " + url.toString());
          }
        }
        poster = null;
        strOutputStream = null;
        return inputStream;
      } while (redirects < kMaxRedirects);
      throw new ProtocolException("Server redirected too many times (" + redirects + ")");
    } catch (RuntimeException e) {
      disconnectInternal();
      rememberedException = e;
      throw e;
    } catch (IOException e) {
      rememberedException = e;
      throw e;
    }
  }

  private AuthenticationInfo authenticate(AuthenticationHeader hdr, boolean proxy) {
    AuthenticationHeader.Challenge c = hdr.preferred();
    if (c == null) {
      return null;
    }
    String realm = c.parser.findValue("realm", "");
    String scheme = c.scheme.equalsIgnoreCase("Digest") ? "Digest" : "Basic";
    String host = proxy ? http.getProxyHostUsed() : url.getHost();
    int port = proxy ? http.getProxyPortUsed() : (url.getPort() == -1 ? url.getDefaultPort() : url.getPort());
    String key = proxy ? AuthenticationInfo.proxyAuthKey(host, port, realm, scheme)
        : AuthenticationInfo.serverAuthKey(url, realm, scheme);
    AuthenticationInfo cached = proxy ? AuthenticationInfo.getProxyAuth(host, port)
        : AuthenticationInfo.getServerAuth(key);
    PasswordAuthentication pw;
    InetAddress addr = null;
    try {
      addr = InetAddress.getByName(host);
    } catch (UnknownHostException e) {
      // the authenticator receives a null address
    }
    String protocol = proxy ? "http" : url.getProtocol();
    pw = requestPassword(host, addr, port, protocol, realm, scheme, url,
        proxy ? Authenticator.RequestorType.PROXY : Authenticator.RequestorType.SERVER);
    if (pw == null) {
      return null;
    }
    AuthenticationInfo info;
    if (scheme.equals("Digest")) {
      info = proxy ? new DigestAuthentication(true, host, port, realm, pw, c.parser)
          : new DigestAuthentication(false, url, realm, pw, c.parser);
    } else {
      String charset = c.parser.findValue("charset");
      info = proxy ? new BasicAuthentication(true, host, port, realm, pw, charset)
          : new BasicAuthentication(false, url, realm, pw, charset);
    }
    requests.set(info.getHeaderName(), info.getHeaderValue(url, method));
    return info;
  }

  public String proxyAuthorizationFor(String host, int port, MessageHeader response, boolean firstAttempt) {
    if (firstAttempt) {
      AuthenticationInfo cached = AuthenticationInfo.getProxyAuth(host, port);
      if (cached != null && cached.supportsPreemptiveAuthorization()) {
        return cached.getHeaderValue(url, "CONNECT");
      }
      return null;
    }
    AuthenticationHeader hdr = new AuthenticationHeader("Proxy-Authenticate", response);
    AuthenticationHeader.Challenge c = hdr.preferred();
    if (c == null) {
      return null;
    }
    String realm = c.parser.findValue("realm", "");
    String scheme = c.scheme.equalsIgnoreCase("Digest") ? "Digest" : "Basic";
    InetAddress addr = null;
    try {
      addr = InetAddress.getByName(host);
    } catch (UnknownHostException e) {
      // the authenticator receives a null address
    }
    PasswordAuthentication pw = requestPassword(host, addr, port, "http", realm, scheme, url,
        Authenticator.RequestorType.PROXY);
    if (pw == null) {
      return null;
    }
    AuthenticationInfo info = scheme.equals("Digest") ? new DigestAuthentication(true, host, port, realm, pw, c.parser)
        : new BasicAuthentication(true, host, port, realm, pw, c.parser.findValue("charset"));
    info.addToCache();
    return info.getHeaderValue(url, "CONNECT");
  }

  public static String userAgentString() {
    return userAgent;
  }

  private void setPreemptiveProxyAuthentication(MessageHeader requests) {
    AuthenticationInfo pauth = AuthenticationInfo.getProxyAuth(http.getProxyHostUsed(), http.getProxyPortUsed());
    if (pauth != null && pauth.supportsPreemptiveAuthorization()) {
      requests.setIfNotSet(pauth.getHeaderName(), pauth.getHeaderValue(url, method));
      currentProxyCredentials = pauth;
    }
  }

  private IOException getChainedException(final IOException rememberedException) {
    try {
      final Object[] args = {rememberedException.getMessage()};
      IOException chainedException = (IOException) rememberedException.getClass()
          .getConstructor(new Class<?>[] {String.class}).newInstance(args);
      chainedException.initCause(rememberedException);
      return chainedException;
    } catch (Exception ignored) {
      return rememberedException;
    }
  }

  @Override
  public InputStream getErrorStream() {
    if (connected && responseCode >= 400) {
      if (errorStream != null) {
        return errorStream;
      } else if (inputStream != null) {
        return inputStream;
      }
    }
    return null;
  }

  private boolean followRedirect() throws IOException {
    if (!getInstanceFollowRedirects()) {
      return false;
    }
    final int stat = getResponseCode();
    if (stat < 300 || stat > 307 || stat == 306 || stat == HTTP_NOT_MODIFIED) {
      return false;
    }
    final String loc = getHeaderField("Location");
    if (loc == null) {
      return false;
    }
    URL locUrl;
    try {
      locUrl = new URL(loc);
      if (!url.getProtocol().equalsIgnoreCase(locUrl.getProtocol())) {
        return false;
      }
    } catch (MalformedURLException mue) {
      locUrl = new URL(url, loc);
    }
    return followRedirect0(loc, stat, locUrl);
  }

  private boolean followRedirect0(String loc, int stat, URL locUrl) throws IOException {
    disconnectWeb();
    if (streaming()) {
      throw new HttpRetryException("cannot retry due to redirection, in streaming mode", stat, loc);
    }
    responses = new MessageHeader(kMaxHeaderSize);
    final URL preRedirectURL = url;
    url = locUrl;
    requestURI = null;
    if (method.equals("POST") && !Boolean.getBoolean("http.strictPostRedirect") && (stat != 307)) {
      requests = new MessageHeader();
      setRequests = false;
      super.setRequestMethod("GET");
      poster = null;
      connect();
      if (!sameDestination(preRedirectURL, url)) {
        userCookies = null;
        userCookies2 = null;
      }
    } else {
      connect();
      if (http != null) {
        requests.set(0, method + " " + getRequestURI() + " HTTP/1.1", null);
        int port = url.getPort();
        String host = stripIPv6ZoneId(url.getHost());
        if (port != -1 && port != url.getDefaultPort()) {
          host += ":" + port;
        }
        requests.set("Host", host);
      }
      if (!sameDestination(preRedirectURL, url)) {
        userCookies = null;
        userCookies2 = null;
        requests.remove("Cookie");
        requests.remove("Cookie2");
        requests.remove("Authorization");
        AuthenticationInfo sauth = AuthenticationInfo.getServerAuth(url);
        if (sauth != null && sauth.supportsPreemptiveAuthorization()) {
          requests.setIfNotSet(sauth.getHeaderName(), sauth.getHeaderValue(url, method));
          currentServerCredentials = sauth;
        }
      }
    }
    return true;
  }

  private static boolean sameDestination(URL firstURL, URL secondURL) {
    int port1 = firstURL.getPort();
    int port2 = secondURL.getPort();
    if (port1 == -1) {
      port1 = firstURL.getDefaultPort();
    }
    if (port2 == -1) {
      port2 = secondURL.getDefaultPort();
    }
    return firstURL.getProtocol().equalsIgnoreCase(secondURL.getProtocol()) && port1 == port2
        && firstURL.getHost().equalsIgnoreCase(secondURL.getHost());
  }

  private void disconnectWeb() throws IOException {
    responseCode = -1;
    if (inputStream != null) {
      try {
        inputStream.close();
      } catch (IOException e) {
        // the connection is dropped below
      }
    }
    inputStream = null;
    http = null;
    connected = false;
  }

  private void disconnectInternal() {
    responseCode = -1;
    inputStream = null;
    if (http != null) {
      http.closeServer();
      http = null;
      connected = false;
    }
  }

  @Override
  public void disconnect() {
    responseCode = -1;
    if (http != null) {
      if (inputStream != null) {
        try {
          inputStream.close();
        } catch (IOException ioe) {
          // already closed
        }
      } else {
        http.closeServer();
      }
      http = null;
      connected = false;
    }
  }

  @Override
  public boolean usingProxy() {
    if (usingProxy) {
      return true;
    }
    return http != null && http.getUsingProxy();
  }

  private static String filterHeaderField(String name, String value) {
    return value;
  }

  private Map<String, List<String>> getFilteredHeaderFields() {
    return responses.getHeaders();
  }

  @Override
  public String getHeaderField(String name) {
    try {
      getInputStream();
    } catch (IOException e) {
      // the headers received so far are still reported
    }
    return filterHeaderField(name, responses.findValue(name));
  }

  @Override
  public Map<String, List<String>> getHeaderFields() {
    try {
      getInputStream();
    } catch (IOException e) {
      // the headers received so far are still reported
    }
    return getFilteredHeaderFields();
  }

  @Override
  public String getHeaderField(int n) {
    try {
      getInputStream();
    } catch (IOException e) {
      // the headers received so far are still reported
    }
    return filterHeaderField(responses.getKey(n), responses.getValue(n));
  }

  @Override
  public String getHeaderFieldKey(int n) {
    try {
      getInputStream();
    } catch (IOException e) {
      // the headers received so far are still reported
    }
    return responses.getKey(n);
  }

  @Override
  public synchronized void setRequestProperty(String key, String value) {
    if (connected || connecting) {
      throw new IllegalStateException("Already connected");
    }
    if (key == null) {
      throw new NullPointerException("key is null");
    }
    if (isExternalMessageHeaderAllowed(key, value)) {
      requests.set(key, value);
      if (!key.equalsIgnoreCase("Content-Type")) {
        userHeaders.set(key, value);
      }
    }
  }

  @Override
  public synchronized void addRequestProperty(String key, String value) {
    if (connected || connecting) {
      throw new IllegalStateException("Already connected");
    }
    if (key == null) {
      throw new NullPointerException("key is null");
    }
    if (isExternalMessageHeaderAllowed(key, value)) {
      requests.add(key, value);
      if (!key.equalsIgnoreCase("Content-Type")) {
        userHeaders.add(key, value);
      }
    }
  }

  void setAuthenticationProperty(String key, String value) {
    checkMessageHeader(key, value);
    requests.set(key, value);
  }

  @Override
  public synchronized String getRequestProperty(String key) {
    if (key == null) {
      return null;
    }
    for (int i = 0; i < kExcludeHeaders.length; i++) {
      if (key.equalsIgnoreCase(kExcludeHeaders[i])) {
        return null;
      }
    }
    if (!setUserCookies) {
      if (key.equalsIgnoreCase("Cookie")) {
        return userCookies;
      }
      if (key.equalsIgnoreCase("Cookie2")) {
        return userCookies2;
      }
    }
    return requests.findValue(key);
  }

  @Override
  public synchronized Map<String, List<String>> getRequestProperties() {
    if (connected) {
      throw new IllegalStateException("Already connected");
    }
    if (setUserCookies) {
      return requests.getHeaders(kExcludeHeaders);
    }
    Map<String, List<String>> userCookiesMap = null;
    if (userCookies != null || userCookies2 != null) {
      userCookiesMap = new HashMap<String, List<String>>();
      if (userCookies != null) {
        userCookiesMap.put("Cookie", Arrays.asList(userCookies));
      }
      if (userCookies2 != null) {
        userCookiesMap.put("Cookie2", Arrays.asList(userCookies2));
      }
    }
    return requests.filterAndAddHeaders(kExcludeHeaders2, userCookiesMap);
  }

  @Override
  public void setConnectTimeout(int timeout) {
    if (timeout < 0) {
      throw new IllegalArgumentException("timeouts can't be negative");
    }
    connectTimeout = timeout;
    super.setConnectTimeout(timeout);
  }

  @Override
  public int getConnectTimeout() {
    return connectTimeout < 0 ? 0 : connectTimeout;
  }

  @Override
  public void setReadTimeout(int timeout) {
    if (timeout < 0) {
      throw new IllegalArgumentException("timeouts can't be negative");
    }
    readTimeout = timeout;
    super.setReadTimeout(timeout);
  }

  @Override
  public int getReadTimeout() {
    return readTimeout < 0 ? 0 : readTimeout;
  }

  public CookieHandler getCookieHandler() {
    return cookieHandler;
  }

  String getMethod() {
    return method;
  }

  private final class StreamingOutputStream extends FilterOutputStream {
    private long expected;
    private long written;
    private boolean closed;
    private boolean error;
    private IOException errorExcp;

    StreamingOutputStream(OutputStream os, long expectedLength) {
      super(os);
      expected = expectedLength;
      written = 0L;
      closed = false;
      error = false;
    }

    @Override
    public void write(int b) throws IOException {
      checkError();
      written++;
      if (expected != -1L && written > expected) {
        throw new IOException("too many bytes written");
      }
      out.write(b);
    }

    @Override
    public void write(byte[] b) throws IOException {
      write(b, 0, b.length);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
      checkError();
      written += len;
      if (expected != -1L && written > expected) {
        out.close();
        throw new IOException("too many bytes written");
      }
      out.write(b, off, len);
    }

    @Override
    public void flush() throws IOException {
      checkError();
      out.flush();
    }

    @Override
    public void close() throws IOException {
      checkError();
      if (closed) {
        return;
      }
      closed = true;
      if (expected != -1L) {
        if (written != expected) {
          error = true;
          errorExcp = new IOException("insufficient data written");
          out.close();
          throw errorExcp;
        }
        super.flush();
      } else {
        super.close();
      }
    }

    private void checkError() throws IOException {
      if (closed && error) {
        IOException e = errorExcp;
        throw e;
      }
      if (((PrintStream) out).checkError()) {
        throw new IOException("Error writing request body to server");
      }
    }

    boolean writtenOK() {
      return closed && !error;
    }
  }
}
