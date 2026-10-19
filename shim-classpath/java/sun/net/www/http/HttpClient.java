/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.net.www.http;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import sun.net.www.MessageHeader;

public class HttpClient {
  public static final boolean kKeepAliveProp = !"false".equalsIgnoreCase(System.getProperty("http.keepAlive", "true"));
  static final boolean kRetryPostProp = !"false".equalsIgnoreCase(System.getProperty("sun.net.http.retryPost", "true"));

  protected Socket serverSocket;
  protected PrintStream serverOutput;
  protected InputStream serverInput;
  protected InputStream rawInput;
  protected URL url;
  protected boolean usingProxy;
  protected Proxy proxy;
  protected String proxyHostUsed;
  protected int proxyPortUsed = -1;
  protected int connectTimeout = -1;
  protected int readTimeout = -1;
  protected boolean cachedHttpClient;
  protected boolean inCache;
  private boolean keepingAlive;
  private int keepAliveTimeout;
  private int keepAliveConnections;
  private boolean failedOnce;
  private MessageHeader requests;
  private PosterOutputStream poster;
  private boolean streaming;
  private boolean responseHasBody;
  private boolean disableKeepAlive;

  protected HttpClient() {
  }

  protected HttpClient(URL url, Proxy proxy, int connectTimeout) throws IOException {
    this.url = url;
    this.proxy = proxy;
    this.connectTimeout = connectTimeout;
    this.usingProxy = proxy != null && proxy.type() == Proxy.Type.HTTP;
    if (usingProxy) {
      InetSocketAddress addr = (InetSocketAddress) proxy.address();
      proxyHostUsed = addr.getHostString();
      proxyPortUsed = addr.getPort();
    }
  }

  protected Object cacheContext() {
    return proxy == null || proxy == Proxy.NO_PROXY ? null : proxy;
  }

  public static HttpClient New(URL url, Proxy p, int connectTimeout, boolean useCache) throws IOException {
    HttpClient ret = null;
    if (useCache && kKeepAliveProp) {
      ret = cached(url, p == null || p == Proxy.NO_PROXY ? null : p, connectTimeout);
      if (ret != null) {
        return ret;
      }
    }
    ret = new HttpClient(url, p, connectTimeout);
    ret.openServer();
    return ret;
  }

  public static HttpClient cached(URL url, Object context, int connectTimeout) {
    HttpClient ret = KeepAliveCache.get(url, context);
    if (ret != null) {
      ret.cachedHttpClient = true;
      ret.inCache = false;
      ret.connectTimeout = connectTimeout;
    }
    return ret;
  }

  protected void openServer() throws IOException {
    String host;
    int port;
    if (usingProxy) {
      host = proxyHostUsed;
      port = proxyPortUsed;
    } else {
      host = url.getHost();
      port = url.getPort() < 0 ? url.getDefaultPort() : url.getPort();
    }
    Socket s;
    if (proxy != null && proxy.type() == Proxy.Type.SOCKS) {
      s = new Socket(proxy);
    } else {
      s = createSocket();
    }
    s.connect(new InetSocketAddress(host, port), Math.max(connectTimeout, 0));
    s.setTcpNoDelay(true);
    if (readTimeout >= 0) {
      s.setSoTimeout(readTimeout);
    }
    serverSocket = s;
    serverOutput = new PrintStream(new BufferedOutputStream(s.getOutputStream()), false, StandardCharsets.ISO_8859_1);
    serverInput = new BufferedInputStream(s.getInputStream());
    rawInput = serverInput;
    afterConnect();
  }

  protected Socket createSocket() throws IOException {
    return new Socket();
  }

  protected void afterConnect() throws IOException {
  }

  public boolean needsTunneling() {
    return false;
  }

  public boolean isUsable() {
    if (serverSocket == null || serverSocket.isClosed()) {
      return false;
    }
    try {
      return rawInput.available() == 0;
    } catch (IOException e) {
      return false;
    }
  }

  public String getProxyHostUsed() {
    return proxyHostUsed;
  }

  public int getProxyPortUsed() {
    return proxyPortUsed;
  }

  public boolean getUsingProxy() {
    return usingProxy;
  }

  public boolean getHttpKeepAliveSet() {
    return kKeepAliveProp;
  }

  public void setReadTimeout(int timeout) {
    readTimeout = timeout;
    if (serverSocket != null) {
      try {
        serverSocket.setSoTimeout(Math.max(timeout, 0));
      } catch (SocketException e) {
        // the socket is already unusable
      }
    }
  }

  public int getReadTimeout() {
    return readTimeout;
  }

  public Socket getSocket() {
    return serverSocket;
  }

  public PrintStream getOutputStream() {
    return serverOutput;
  }

  public InputStream getInputStream() {
    return serverInput;
  }

  public URL getURL() {
    return url;
  }

  public boolean isKeepingAlive() {
    return keepingAlive;
  }

  public void closeServer() {
    keepingAlive = false;
    try {
      if (serverSocket != null) {
        serverSocket.close();
      }
    } catch (IOException e) {
    }
    serverSocket = null;
  }

  public void finished() {
    if (keepingAlive && serverSocket != null && !inCache) {
      inCache = true;
      requests = null;
      poster = null;
      KeepAliveCache.put(url, cacheContext(), this, keepAliveTimeout, keepAliveConnections);
    } else if (!keepingAlive) {
      closeServer();
    }
  }

  public void writeRequests(MessageHeader head, PosterOutputStream pos, boolean streaming) {
    requests = head;
    poster = pos;
    this.streaming = streaming;
    head.print(serverOutput);
    if (!streaming && pos != null) {
      byte[] data = pos.toByteArray();
      serverOutput.write(data, 0, data.length);
    }
    serverOutput.flush();
  }

  public void writeRequests(MessageHeader head) {
    writeRequests(head, null, false);
  }

  public boolean parseHTTP(MessageHeader responses, HttpURLConnection httpuc) throws IOException {
    try {
      return parseHTTPHeader(responses, httpuc);
    } catch (SocketTimeoutException stex) {
      closeServer();
      throw stex;
    } catch (IOException e) {
      closeServer();
      cachedHttpClient = false;
      if (!failedOnce && requests != null) {
        failedOnce = true;
        if (requests.getKey(0) != null && requests.getKey(0).equals("CONNECT") || streaming
            || (httpuc.getRequestMethod().equals("POST") && !kRetryPostProp)) {
          throw e;
        }
        responses.reset();
        openServer();
        writeRequests(requests, poster, streaming);
        return parseHTTP(responses, httpuc);
      }
      throw e;
    }
  }

  private boolean parseHTTPHeader(MessageHeader responses, HttpURLConnection httpuc) throws IOException {
    keepingAlive = false;
    keepAliveConnections = -1;
    keepAliveTimeout = 0;
    boolean ret = false;
    byte[] b = new byte[8];
    InputStream in = rawInput;
    in.mark(10);
    int nread = 0;
    while (nread < 8) {
      int n = in.read(b, nread, 8 - nread);
      if (n < 0) {
        break;
      }
      nread += n;
    }
    String keep = null;
    int code = 0;
    if (nread == 8 && b[0] == 'H' && b[1] == 'T' && b[2] == 'T' && b[3] == 'P' && b[4] == '/' && b[5] == '1'
        && b[6] == '.') {
      in.reset();
      responses.parseHeader(in);
      String status = responses.getValue(0);
      if (status != null) {
        int sp = status.indexOf(' ');
        if (sp > 0) {
          int sp2 = status.indexOf(' ', sp + 1);
          try {
            code = Integer.parseInt(status.substring(sp + 1, sp2 > 0 ? sp2 : status.length()).trim());
          } catch (NumberFormatException e) {
            code = 0;
          }
        }
      }
      if (code == 100) {
        responses.reset();
        return parseHTTPHeader(responses, httpuc);
      }
      ret = true;
      keep = usingProxy ? responses.findValue("Proxy-Connection") : null;
      if (keep == null) {
        keep = responses.findValue("Connection");
      }
      if (keep != null && keep.equalsIgnoreCase("keep-alive")) {
        HeaderParser hp = new HeaderParser(responses.findValue("Keep-Alive"));
        keepAliveConnections = hp.findInt("max", usingProxy ? 50 : 5);
        keepAliveTimeout = hp.findInt("timeout", usingProxy ? 60 : 5);
      } else if (b[7] != '0') {
        if (keep != null || !kKeepAliveProp) {
          keepAliveConnections = 1;
        } else {
          keepAliveConnections = 5;
          keepAliveTimeout = 5;
        }
      }
      if (requests != null && requests.findValue("Connection") != null
          && requests.findValue("Connection").equalsIgnoreCase("close")) {
        keepAliveConnections = -1;
      }
    } else if (nread != 8) {
      if (!failedOnce && requests != null) {
        failedOnce = true;
        if (!(requests.getKey(0) != null && requests.getKey(0).startsWith("CONNECT")) && !streaming
            && !(httpuc.getRequestMethod().equals("POST") && !kRetryPostProp)) {
          closeServer();
          cachedHttpClient = false;
          responses.reset();
          openServer();
          writeRequests(requests, poster, streaming);
          return parseHTTP(responses, httpuc);
        }
      }
      throw new SocketException("Unexpected end of file from server");
    } else {
      in.reset();
      responses.set("Content-type", "unknown/unknown");
    }
    int cl = -1;
    String te = responses.findValue("Transfer-Encoding");
    InputStream body = in;
    boolean chunked = false;
    String requestLine = requests != null ? requests.getKey(0) : null;
    boolean noBody = ret && (code == 204 || code == 304 || code < 200 && code >= 100 || requestLine != null
        && requestLine.startsWith("HEAD"));
    if (!ret) {
      keepAliveConnections = -1;
    } else if (noBody) {
      cl = 0;
    } else if (te != null && te.equalsIgnoreCase("chunked")) {
      chunked = true;
    } else {
      String cls = responses.findValue("content-length");
      if (cls != null) {
        try {
          cl = Integer.parseInt(cls.trim());
        } catch (NumberFormatException e) {
          cl = -1;
        }
      }
    }
    keepingAlive = ret && keepAliveConnections > 1 && (chunked || cl >= 0);
    if (chunked) {
      serverInput = new ChunkedInputStream(in, this, responses);
    } else if (cl >= 0 && ret) {
      serverInput = new KeepAliveStream(in, cl, this);
    } else {
      serverInput = new ClosingInputStream(in, this);
    }
    failedOnce = false;
    return ret;
  }
}
