/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package sun.net.httpserver;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpPrincipal;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

class ExchangeImpl extends HttpExchange {
  private static final DateTimeFormatter kDate =
      DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss 'GMT'", Locale.US);
  private static final System.Logger kLogger = System.getLogger("com.sun.net.httpserver");

  private final ServerImpl.Connection connection;
  private final String method;
  private final URI uri;
  private final String protocol;
  private final boolean http10;
  private final Headers requestHeaders;
  private final Headers responseHeaders = new Headers();
  private final HttpContextImpl context;
  private final long requestLength;
  private final boolean chunkedRequest;
  private final boolean expectContinue;
  private final CountDownLatch done = new CountDownLatch(1);
  private final Map<String, Object> attributes = new HashMap<String, Object>();
  private final ServerImpl.RequestInput rawInput;

  private InputStream userIn;
  private OutputStream userOut;
  private final ServerImpl.SwitchableOutput responseOut;
  private InputStream bodyIn;
  private boolean sentContinue;
  private boolean sentHeaders;
  private boolean closed;
  private boolean closeConnection;
  private int responseCode = -1;
  private HttpPrincipal principal;

  ExchangeImpl(ServerImpl.Connection connection, String method, URI uri, String protocol, Headers headers,
      HttpContextImpl context, long requestLength, boolean chunked, ServerImpl.RequestInput rawInput) {
    this.connection = connection;
    this.method = method;
    this.uri = uri;
    this.protocol = protocol;
    this.http10 = protocol.equalsIgnoreCase("HTTP/1.0");
    this.requestHeaders = headers;
    this.context = context;
    this.requestLength = requestLength;
    this.chunkedRequest = chunked;
    String expect = headers.getFirst("Expect");
    this.expectContinue = expect != null && expect.equalsIgnoreCase("100-continue") && !http10;
    this.rawInput = rawInput;
    this.responseOut = new ServerImpl.SwitchableOutput();
    this.userOut = responseOut;
    String conn = headers.getFirst("Connection");
    if (conn != null && conn.equalsIgnoreCase("close")) {
      closeConnection = true;
    }
    if (http10) {
      closeConnection = !(conn != null && conn.equalsIgnoreCase("keep-alive"));
    }
  }

  boolean isHttp10() {
    return http10;
  }

  HttpContextImpl context() {
    return context;
  }

  void awaitDone() throws InterruptedException {
    done.await();
  }

  boolean shouldCloseConnection() {
    return closeConnection;
  }

  void complete() {
    done.countDown();
  }

  void closeAfterResponse() {
    closeConnection = true;
  }

  void markConnectionClosed() {
    closeConnection = true;
    done.countDown();
  }

  void setPrincipal(HttpPrincipal principal) {
    this.principal = principal;
  }

  @Override
  public Headers getRequestHeaders() {
    return requestHeaders;
  }

  @Override
  public Headers getResponseHeaders() {
    return responseHeaders;
  }

  @Override
  public URI getRequestURI() {
    return uri;
  }

  @Override
  public String getRequestMethod() {
    return method;
  }

  @Override
  public HttpContext getHttpContext() {
    return context;
  }

  @Override
  public synchronized void close() {
    if (closed) {
      return;
    }
    closed = true;
    try {
      if (bodyIn != null) {
        bodyIn.close();
      } else {
        drainRequest();
      }
      if (!sentHeaders) {
        markConnectionClosed();
        return;
      }
      userOut.close();
    } catch (IOException e) {
      kLogger.log(System.Logger.Level.TRACE, "ExchangeImpl.close", e);
      markConnectionClosed();
    }
  }

  private void drainRequest() throws IOException {
    InputStream in = requestBody();
    in.close();
  }

  private InputStream requestBody() {
    if (bodyIn == null) {
      if (chunkedRequest) {
        bodyIn = new ServerImpl.ChunkedInput(rawInput, this);
      } else {
        bodyIn = new ServerImpl.FixedInput(rawInput, requestLength, this);
      }
      userIn = bodyIn;
    }
    return bodyIn;
  }

  void sendContinueIfNeeded() throws IOException {
    if (expectContinue && !sentContinue) {
      sentContinue = true;
      connection.writeRaw("HTTP/1.1 100 Continue\r\nContent-Length: 0\r\n\r\n");
    }
  }

  @Override
  public InputStream getRequestBody() {
    if (userIn != null) {
      return userIn;
    }
    requestBody();
    return userIn;
  }

  @Override
  public OutputStream getResponseBody() {
    return userOut;
  }

  private boolean isHeadRequest() {
    return method.equals("HEAD");
  }

  @Override
  public synchronized void sendResponseHeaders(int rCode, long contentLen) throws IOException {
    if (sentHeaders) {
      throw new IOException("headers already sent");
    }
    this.responseCode = rCode;
    String statusLine = "HTTP/1.1 " + rCode + " " + Responses.get(rCode) + "\r\n";
    boolean noContentToSend = false;
    boolean informational = rCode >= 100 && rCode < 200;
    if (informational || rCode == 204 || rCode == 304) {
      if (contentLen != -1) {
        kLogger.log(System.Logger.Level.WARNING,
            "sendResponseHeaders: rCode = " + rCode + ": forcing contentLen = -1");
      }
      contentLen = -1;
    }
    responseHeaders.set("Date", ZonedDateTime.now(ZoneOffset.UTC).format(kDate));
    if (isHeadRequest() || rCode == 304) {
      if (contentLen >= 0) {
        kLogger.log(System.Logger.Level.WARNING,
            "sendResponseHeaders: being invoked with a content length for a HEAD request");
      }
      noContentToSend = true;
      contentLen = 0;
    } else if (contentLen == 0) {
      if (http10) {
        responseOut.set(new ServerImpl.UndefLengthOutput(connection, this));
        closeConnection = true;
      } else {
        responseHeaders.set("Transfer-encoding", "chunked");
        responseOut.set(new ServerImpl.ChunkedOutput(connection, this));
      }
    } else {
      if (contentLen == -1) {
        noContentToSend = true;
        contentLen = 0;
      }
      if (!informational && rCode != 204 && rCode != 304) {
        responseHeaders.set("Content-length", Long.toString(contentLen));
      }
      responseOut.set(new ServerImpl.FixedOutput(connection, this, contentLen));
    }
    if (http10) {
      boolean keepAlive = !closeConnection;
      if (keepAlive) {
        responseHeaders.set("Connection", "keep-alive");
        responseHeaders.set("Keep-alive", "timeout=" + (ServerImpl.kIdleMillis / 1000));
      } else {
        responseHeaders.set("Connection", "close");
      }
    }
    sentHeaders = true;
    StringBuilder sb = new StringBuilder(statusLine);
    for (Map.Entry<String, java.util.List<String>> e : responseHeaders.entrySet()) {
      for (String v : e.getValue()) {
        sb.append(e.getKey()).append(": ").append(v).append("\r\n");
      }
    }
    sb.append("\r\n");
    connection.writeRaw(sb.toString());
    if (noContentToSend) {
      responseOut.finish();
      complete();
    }
  }

  @Override
  public InetSocketAddress getRemoteAddress() {
    return connection.remoteAddress();
  }

  @Override
  public int getResponseCode() {
    return responseCode;
  }

  @Override
  public InetSocketAddress getLocalAddress() {
    return connection.localAddress();
  }

  @Override
  public String getProtocol() {
    return protocol;
  }

  @Override
  public Object getAttribute(String name) {
    if (name == null) {
      throw new NullPointerException("null name parameter");
    }
    return attributes.get(name);
  }

  @Override
  public void setAttribute(String name, Object value) {
    if (name == null) {
      throw new NullPointerException("null name parameter");
    }
    attributes.put(name, value);
  }

  @Override
  public void setStreams(InputStream i, OutputStream o) {
    if (i != null) {
      userIn = i;
    }
    if (o != null) {
      userOut = o;
    }
  }

  @Override
  public HttpPrincipal getPrincipal() {
    return principal;
  }

  javax.net.ssl.SSLSession sslSession() {
    return connection.sslSession();
  }
}
