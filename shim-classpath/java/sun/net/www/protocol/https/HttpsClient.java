/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.net.www.protocol.https;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.Proxy;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import sun.net.www.MessageHeader;
import sun.net.www.http.HttpClient;
import sun.security.util.HostnameChecker;

final class HttpsClient extends HttpClient {
  private final SSLSocketFactory sslSocketFactory;
  private final HostnameVerifier hv;
  private final DelegateHttpsURLConnection httpuc;
  private SSLSession session;

  private HttpsClient(SSLSocketFactory sf, URL url, HostnameVerifier hv, Proxy proxy, int connectTimeout,
      DelegateHttpsURLConnection httpuc) throws IOException {
    super(url, proxy, connectTimeout);
    this.sslSocketFactory = sf;
    this.hv = hv;
    this.httpuc = httpuc;
  }

  static HttpsClient New(SSLSocketFactory sf, URL url, HostnameVerifier hv, Proxy p, boolean useCache,
      int connectTimeout, DelegateHttpsURLConnection httpuc) throws IOException {
    if (useCache && kKeepAliveProp) {
      HttpClient cached = HttpClient.cached(url, contextFor(p, sf, hv), connectTimeout);
      if (cached instanceof HttpsClient) {
        return (HttpsClient) cached;
      }
    }
    HttpsClient ret = new HttpsClient(sf, url, hv, p, connectTimeout, httpuc);
    ret.openServer();
    return ret;
  }

  private static Object contextFor(Proxy p, SSLSocketFactory sf, HostnameVerifier hv) {
    return Arrays.asList(p == null || p == Proxy.NO_PROXY ? null : p, sf, hv);
  }

  @Override
  protected Object cacheContext() {
    return contextFor(proxy, sslSocketFactory, hv);
  }

  SSLSession getSession() {
    return session;
  }

  @Override
  protected void afterConnect() throws IOException {
    String host = url.getHost();
    int port = url.getPort() == -1 ? url.getDefaultPort() : url.getPort();
    if (usingProxy) {
      doTunnel(host, port);
    }
    SSLSocket s = (SSLSocket) sslSocketFactory.createSocket(serverSocket, host, port, true);
    boolean defaultVerifier = hv == null || hv.getClass().getName().endsWith("HttpsURLConnection$DefaultHostnameVerifier");
    boolean needToCheckSpoofing = true;
    if (defaultVerifier) {
      SSLParameters parameters = s.getSSLParameters();
      parameters.setEndpointIdentificationAlgorithm("HTTPS");
      s.setSSLParameters(parameters);
      needToCheckSpoofing = false;
    }
    if (readTimeout >= 0) {
      s.setSoTimeout(readTimeout);
    }
    s.startHandshake();
    session = s.getSession();
    serverSocket = s;
    serverOutput = new PrintStream(new BufferedOutputStream(s.getOutputStream()), false, StandardCharsets.ISO_8859_1);
    serverInput = new BufferedInputStream(s.getInputStream());
    if (needToCheckSpoofing) {
      checkURLSpoofing(hv);
    }
  }

  private void doTunnel(String host, int port) throws IOException {
    String hostPort = host + ":" + port;
    String auth = httpuc == null ? null : httpuc.proxyAuthorizationFor(proxyHostUsed, proxyPortUsed, null, true);
    for (int attempt = 0; attempt < 3; attempt++) {
      MessageHeader req = new MessageHeader();
      req.add("CONNECT " + hostPort + " HTTP/1.1", null);
      req.add("Host", hostPort);
      req.add("User-Agent", sun.net.www.protocol.http.HttpURLConnection.userAgentString());
      req.add("Proxy-Connection", "keep-alive");
      if (auth != null) {
        req.add("Proxy-Authorization", auth);
      }
      req.print(serverOutput);
      serverOutput.flush();
      MessageHeader resp = new MessageHeader();
      resp.parseHeader(serverInput);
      String status = resp.getValue(0);
      int code = -1;
      if (status != null) {
        String[] parts = status.split(" ", 3);
        if (parts.length >= 2) {
          try {
            code = Integer.parseInt(parts[1]);
          } catch (NumberFormatException e) {
            code = -1;
          }
        }
      }
      String cl = resp.findValue("Content-Length");
      if (code != 200 && cl != null) {
        try {
          serverInput.readNBytes(Integer.parseInt(cl.trim()));
        } catch (NumberFormatException e) {
          // no readable body
        }
      }
      if (code == 200) {
        return;
      }
      if (code == 407 && httpuc != null && attempt == 0) {
        auth = httpuc.proxyAuthorizationFor(proxyHostUsed, proxyPortUsed, resp, false);
        if (auth != null) {
          continue;
        }
      }
      closeServer();
      throw new IOException("Unable to tunnel through proxy. Proxy returns \"" + status + "\"");
    }
  }

  private void checkURLSpoofing(HostnameVerifier hostnameVerifier) throws IOException {
    String host = url.getHost();
    if (host != null && host.startsWith("[") && host.endsWith("]")) {
      host = host.substring(1, host.length() - 1);
    }
    try {
      HostnameChecker checker = HostnameChecker.getInstance(HostnameChecker.kTypeTls);
      Certificate[] peerCerts = session.getPeerCertificates();
      if (peerCerts[0] instanceof X509Certificate) {
        checker.match(host, (X509Certificate) peerCerts[0], false);
        return;
      }
    } catch (SSLPeerUnverifiedException e) {
      // no certificates to match, fall through to the verifier
    } catch (CertificateException cpe) {
      // the verifier gets a chance to accept the host
    }
    if (hostnameVerifier != null && hostnameVerifier.verify(host, session)) {
      return;
    }
    serverSocket.close();
    session.invalidate();
    throw new IOException("Wrong HTTPS hostname: should be <" + url.getHost() + ">");
  }
}
