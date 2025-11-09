/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.net.www.protocol.https;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Authenticator;
import java.net.ProtocolException;
import java.net.Proxy;
import java.net.URL;
import java.security.cert.Certificate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;

public class HttpsURLConnectionImpl extends HttpsURLConnection {
  private final DelegateHttpsURLConnection delegate;

  HttpsURLConnectionImpl(URL u, Handler handler) throws IOException {
    this(u, null, handler);
  }

  HttpsURLConnectionImpl(URL u, Proxy p, Handler handler) throws IOException {
    super(u);
    delegate = new DelegateHttpsURLConnection(u, p, handler, this);
  }

  protected HttpsURLConnectionImpl(URL u) throws IOException {
    this(u, (Handler) null);
  }

  @Override
  public URL getURL() {
    return delegate.getURL();
  }

  @Override
  public void connect() throws IOException {
    delegate.connect();
  }

  @Override
  public boolean usingProxy() {
    return delegate.usingProxy();
  }

  @Override
  public Map<String, List<String>> getHeaderFields() {
    return delegate.getHeaderFields();
  }

  @Override
  public InputStream getInputStream() throws IOException {
    return delegate.getInputStream();
  }

  @Override
  public OutputStream getOutputStream() throws IOException {
    return delegate.getOutputStream();
  }

  @Override
  public InputStream getErrorStream() {
    return delegate.getErrorStream();
  }

  @Override
  public void disconnect() {
    delegate.disconnect();
  }

  @Override
  public String getCipherSuite() {
    return delegate.cipherSuite();
  }

  @Override
  public Certificate[] getLocalCertificates() {
    return delegate.localCertificates();
  }

  @Override
  public Certificate[] getServerCertificates() throws SSLPeerUnverifiedException {
    return delegate.serverCertificates();
  }

  @Override
  public Optional<SSLSession> getSSLSession() {
    return Optional.ofNullable(delegate.sslSession());
  }

  @Override
  public void setAuthenticator(Authenticator auth) {
    delegate.setAuthenticator(auth);
  }

  @Override
  public void setRequestMethod(String method) throws ProtocolException {
    delegate.setRequestMethod(method);
  }

  @Override
  public String getRequestMethod() {
    return delegate.getRequestMethod();
  }

  @Override
  public int getResponseCode() throws IOException {
    return delegate.getResponseCode();
  }

  @Override
  public String getResponseMessage() throws IOException {
    return delegate.getResponseMessage();
  }

  @Override
  public void setInstanceFollowRedirects(boolean followRedirects) {
    delegate.setInstanceFollowRedirects(followRedirects);
  }

  @Override
  public boolean getInstanceFollowRedirects() {
    return delegate.getInstanceFollowRedirects();
  }

  @Override
  public void setFixedLengthStreamingMode(int contentLength) {
    delegate.setFixedLengthStreamingMode(contentLength);
  }

  @Override
  public void setFixedLengthStreamingMode(long contentLength) {
    delegate.setFixedLengthStreamingMode(contentLength);
  }

  @Override
  public void setChunkedStreamingMode(int chunklen) {
    delegate.setChunkedStreamingMode(chunklen);
  }

  @Override
  public String getHeaderField(String name) {
    return delegate.getHeaderField(name);
  }

  @Override
  public String getHeaderField(int n) {
    return delegate.getHeaderField(n);
  }

  @Override
  public String getHeaderFieldKey(int n) {
    return delegate.getHeaderFieldKey(n);
  }

  @Override
  public int getHeaderFieldInt(String name, int Default) {
    return delegate.getHeaderFieldInt(name, Default);
  }

  @Override
  public long getHeaderFieldLong(String name, long Default) {
    return delegate.getHeaderFieldLong(name, Default);
  }

  @Override
  public long getHeaderFieldDate(String name, long Default) {
    return delegate.getHeaderFieldDate(name, Default);
  }

  @Override
  public int getContentLength() {
    return delegate.getContentLength();
  }

  @Override
  public long getContentLengthLong() {
    return delegate.getContentLengthLong();
  }

  @Override
  public String getContentType() {
    return delegate.getContentType();
  }

  @Override
  public String getContentEncoding() {
    return delegate.getContentEncoding();
  }

  @Override
  public long getExpiration() {
    return delegate.getExpiration();
  }

  @Override
  public long getDate() {
    return delegate.getDate();
  }

  @Override
  public long getLastModified() {
    return delegate.getLastModified();
  }

  @Override
  public Object getContent() throws IOException {
    return delegate.getContent();
  }

  @Override
  public Object getContent(Class<?>[] classes) throws IOException {
    return delegate.getContent(classes);
  }

  @Override
  public java.security.Permission getPermission() throws IOException {
    return delegate.getPermission();
  }

  @Override
  public void setConnectTimeout(int timeout) {
    delegate.setConnectTimeout(timeout);
  }

  @Override
  public int getConnectTimeout() {
    return delegate.getConnectTimeout();
  }

  @Override
  public void setReadTimeout(int timeout) {
    delegate.setReadTimeout(timeout);
  }

  @Override
  public int getReadTimeout() {
    return delegate.getReadTimeout();
  }

  @Override
  public void setDoInput(boolean doinput) {
    delegate.setDoInput(doinput);
  }

  @Override
  public boolean getDoInput() {
    return delegate.getDoInput();
  }

  @Override
  public void setDoOutput(boolean dooutput) {
    delegate.setDoOutput(dooutput);
  }

  @Override
  public boolean getDoOutput() {
    return delegate.getDoOutput();
  }

  @Override
  public void setAllowUserInteraction(boolean allowuserinteraction) {
    delegate.setAllowUserInteraction(allowuserinteraction);
  }

  @Override
  public boolean getAllowUserInteraction() {
    return delegate.getAllowUserInteraction();
  }

  @Override
  public void setUseCaches(boolean usecaches) {
    delegate.setUseCaches(usecaches);
  }

  @Override
  public boolean getUseCaches() {
    return delegate.getUseCaches();
  }

  @Override
  public void setIfModifiedSince(long ifmodifiedsince) {
    delegate.setIfModifiedSince(ifmodifiedsince);
  }

  @Override
  public long getIfModifiedSince() {
    return delegate.getIfModifiedSince();
  }

  @Override
  public boolean getDefaultUseCaches() {
    return delegate.getDefaultUseCaches();
  }

  @Override
  public void setDefaultUseCaches(boolean defaultusecaches) {
    delegate.setDefaultUseCaches(defaultusecaches);
  }

  @Override
  public void setRequestProperty(String key, String value) {
    delegate.setRequestProperty(key, value);
  }

  @Override
  public void addRequestProperty(String key, String value) {
    delegate.addRequestProperty(key, value);
  }

  @Override
  public String getRequestProperty(String key) {
    return delegate.getRequestProperty(key);
  }

  @Override
  public Map<String, List<String>> getRequestProperties() {
    return delegate.getRequestProperties();
  }

  @Override
  public String toString() {
    return delegate.toString();
  }

  @Override
  public boolean equals(Object obj) {
    return this == obj || (obj instanceof HttpsURLConnectionImpl
        && delegate == ((HttpsURLConnectionImpl) obj).delegate);
  }

  @Override
  public int hashCode() {
    return delegate.hashCode();
  }
}
