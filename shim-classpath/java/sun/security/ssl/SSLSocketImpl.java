/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.security.ssl;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketOption;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;
import javax.net.ssl.HandshakeCompletedEvent;
import javax.net.ssl.HandshakeCompletedListener;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;

final class SSLSocketImpl extends SSLSocket {
  private final SSLContextImpl context;
  private SslConfig cfg;
  private final Socket raw;
  private final boolean autoClose;
  private String host;
  private TlsConnection conn;
  private InputStream rawIn;
  private OutputStream rawOut;
  private final Object handshakeLock = new Object();
  private final Object readLock = new Object();
  private final Object writeLock = new Object();
  private final List<HandshakeCompletedListener> listeners = new ArrayList<HandshakeCompletedListener>();
  private volatile boolean closed;
  private boolean modeSet;
  private SSLSessionImpl nullSession;
  private final SSLInputStream sslIn = new SSLInputStream();
  private final SSLOutputStream sslOut = new SSLOutputStream();

  SSLSocketImpl(SSLContextImpl context) {
    this(context, new Socket(), null, true, true);
  }

  SSLSocketImpl(SSLContextImpl context, String host, int port) throws IOException {
    this(context, new Socket(), host, true, true);
    connect(new InetSocketAddress(host, port), 0);
  }

  SSLSocketImpl(SSLContextImpl context, InetAddress address, int port) throws IOException {
    this(context, new Socket(), address.getHostName(), true, true);
    connect(new InetSocketAddress(address, port), 0);
  }

  SSLSocketImpl(SSLContextImpl context, String host, int port, InetAddress localAddress, int localPort)
      throws IOException {
    this(context, new Socket(), host, true, true);
    bind(new InetSocketAddress(localAddress, localPort));
    connect(new InetSocketAddress(host, port), 0);
  }

  SSLSocketImpl(SSLContextImpl context, InetAddress address, int port, InetAddress localAddress, int localPort)
      throws IOException {
    this(context, new Socket(), address.getHostName(), true, true);
    bind(new InetSocketAddress(localAddress, localPort));
    connect(new InetSocketAddress(address, port), 0);
  }

  SSLSocketImpl(SSLContextImpl context, Socket layered, String host, int port, boolean autoClose)
      throws IOException {
    this(context, layered, host, true, autoClose);
    if (layered == null) {
      throw new NullPointerException("the existing socket cannot be null");
    }
    if (!layered.isConnected()) {
      throw new SocketException("Underlying socket is not connected");
    }
  }

  SSLSocketImpl(SSLContextImpl context, Socket accepted, boolean clientMode) {
    this(context, accepted, null, clientMode, true);
  }

  private SSLSocketImpl(SSLContextImpl context, Socket raw, String host, boolean clientMode, boolean autoClose) {
    this.context = context;
    this.raw = raw;
    this.host = host;
    this.autoClose = autoClose;
    this.cfg = context.newConfig(clientMode);
    this.modeSet = true;
  }

  void applyConfig(SslConfig serverConfig) {
    synchronized (handshakeLock) {
      this.cfg = serverConfig.copy();
    }
  }

  private void startIfNeeded() throws IOException {
    synchronized (handshakeLock) {
      if (conn == null) {
        if (closed) {
          throw new SocketException("Socket is closed");
        }
        if (!raw.isConnected()) {
          throw new SocketException("Socket is not connected");
        }
        rawIn = raw.getInputStream();
        rawOut = raw.getOutputStream();
        String peer = host;
        if (peer == null && cfg.clientMode) {
          peer = raw.getInetAddress().getHostName();
        }
        conn = TlsConnections.create(context, cfg.copy(), peer, raw.getPort(), this);
      }
    }
  }

  @Override
  public void startHandshake() throws IOException {
    synchronized (handshakeLock) {
      startIfNeeded();
      if (conn.handshakeDone) {
        return;
      }
      if (conn.failure != null) {
        throw conn.failure;
      }
      try {
        conn.begin();
        flush();
        while (!conn.handshakeDone) {
          readRecord();
          flush();
        }
        flush();
      } catch (IOException e) {
        closeQuietly();
        throw e;
      }
      conn.handshakeJustFinished = false;
      fireHandshakeCompleted();
    }
  }

  private void closeQuietly() {
    try {
      flush();
    } catch (IOException ignored) {
      // the original failure is reported
    }
    try {
      closed = true;
      if (autoClose) {
        raw.close();
      }
    } catch (IOException ignored) {
      // best effort
    }
  }

  private void fireHandshakeCompleted() {
    final List<HandshakeCompletedListener> copy;
    synchronized (listeners) {
      copy = new ArrayList<HandshakeCompletedListener>(listeners);
    }
    if (copy.isEmpty()) {
      return;
    }
    final HandshakeCompletedEvent event = new HandshakeCompletedEvent(this, conn.session);
    Thread t = new Thread(new Runnable() {
      @Override
      public void run() {
        for (HandshakeCompletedListener l : copy) {
          try {
            l.handshakeCompleted(event);
          } catch (Throwable ignored) {
            // listener failures do not affect the connection
          }
        }
      }
    }, "HandshakeCompletedNotify-Thread");
    t.setDaemon(true);
    t.start();
  }

  private void flush() throws IOException {
    synchronized (writeLock) {
      byte[] rec;
      boolean wrote = false;
      while ((rec = conn.pollOutput()) != null) {
        rawOut.write(rec);
        wrote = true;
      }
      if (wrote) {
        rawOut.flush();
      }
    }
  }

  private void readFully(byte[] buf, int len, boolean header) throws IOException {
    int off = 0;
    while (off < len) {
      int n = rawIn.read(buf, off, len - off);
      if (n < 0) {
        if (off == 0 && header) {
          throw new EOFException();
        }
        throw new SSLException("Remote host terminated the handshake");
      }
      off += n;
    }
  }

  private boolean readRecord() throws IOException {
    byte[] header = new byte[5];
    try {
      readFully(header, 5, true);
    } catch (EOFException e) {
      conn.markInboundClosed();
      if (!conn.handshakeDone) {
        throw conn.abort(new SSLHandshakeException("Remote host terminated the handshake"));
      }
      return false;
    }
    int type = header[0] & 0xff;
    int major = header[1] & 0xff;
    int len = ((header[3] & 0xff) << 8) | (header[4] & 0xff);
    if (type < 20 || type > 23 || major != 3) {
      throw conn.fatal(TlsConnection.kAlertUnexpectedMessage, "Unrecognized SSL message, plaintext connection?");
    }
    if (len > TlsConnection.kMaxFragment + 2048) {
      throw conn.fatal(TlsConnection.kAlertRecordOverflow, "Record overflow");
    }
    byte[] body = new byte[len];
    readFully(body, len, false);
    conn.processRecord(type, body);
    return true;
  }

  private final class SSLInputStream extends InputStream {
    @Override
    public int read() throws IOException {
      byte[] one = new byte[1];
      int n = read(one, 0, 1);
      return n < 0 ? -1 : one[0] & 0xff;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
      if (b == null) {
        throw new NullPointerException();
      }
      if (off < 0 || len < 0 || len > b.length - off) {
        throw new IndexOutOfBoundsException();
      }
      if (len == 0) {
        return 0;
      }
      if (closed) {
        throw new SocketException("Socket closed");
      }
      startHandshake();
      synchronized (readLock) {
        while (true) {
          int n = conn.readApp(b, off, len);
          if (n > 0) {
            return n;
          }
          if (conn.failure != null) {
            throw conn.failure;
          }
          if (conn.inboundDone) {
            return -1;
          }
          if (closed) {
            throw new SocketException("Socket closed");
          }
          if (!readRecord()) {
            return -1;
          }
          flush();
        }
      }
    }

    @Override
    public int available() throws IOException {
      return conn == null ? 0 : conn.appAvailable();
    }

    @Override
    public void close() throws IOException {
      SSLSocketImpl.this.close();
    }
  }

  private final class SSLOutputStream extends OutputStream {
    @Override
    public void write(int b) throws IOException {
      write(new byte[] {(byte) b}, 0, 1);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
      if (b == null) {
        throw new NullPointerException();
      }
      if (off < 0 || len < 0 || len > b.length - off) {
        throw new IndexOutOfBoundsException();
      }
      if (closed) {
        throw new SocketException("Socket closed");
      }
      startHandshake();
      synchronized (writeLock) {
        if (conn.failure != null) {
          throw conn.failure;
        }
        int done = 0;
        while (done < len) {
          int n = Math.min(TlsConnection.kMaxFragment, len - done);
          rawOut.write(conn.encryptApp(b, off + done, n));
          done += n;
        }
        rawOut.flush();
      }
    }

    @Override
    public void flush() throws IOException {
      rawOut.flush();
    }

    @Override
    public void close() throws IOException {
      SSLSocketImpl.this.close();
    }
  }

  @Override
  public InputStream getInputStream() throws IOException {
    if (closed) {
      throw new SocketException("Socket is closed");
    }
    if (!raw.isConnected()) {
      throw new SocketException("Socket is not connected");
    }
    return sslIn;
  }

  @Override
  public OutputStream getOutputStream() throws IOException {
    if (closed) {
      throw new SocketException("Socket is closed");
    }
    if (!raw.isConnected()) {
      throw new SocketException("Socket is not connected");
    }
    return sslOut;
  }

  @Override
  public void close() throws IOException {
    synchronized (handshakeLock) {
      if (closed) {
        return;
      }
      closed = true;
    }
    try {
      if (conn != null && conn.handshakeStarted) {
        conn.closeOutbound();
        flush();
      }
    } catch (IOException ignored) {
      // the peer may already be gone
    } finally {
      if (autoClose || !raw.isConnected()) {
        raw.close();
      }
    }
  }

  @Override
  public void connect(SocketAddress endpoint) throws IOException {
    connect(endpoint, 0);
  }

  @Override
  public void connect(SocketAddress endpoint, int timeout) throws IOException {
    if (endpoint instanceof InetSocketAddress && host == null) {
      host = ((InetSocketAddress) endpoint).getHostString();
    }
    raw.connect(endpoint, timeout);
  }

  @Override
  public void bind(SocketAddress bindpoint) throws IOException {
    raw.bind(bindpoint);
  }

  @Override
  public InetAddress getInetAddress() {
    return raw.getInetAddress();
  }

  @Override
  public InetAddress getLocalAddress() {
    return raw.getLocalAddress();
  }

  @Override
  public int getPort() {
    return raw.getPort();
  }

  @Override
  public int getLocalPort() {
    return raw.getLocalPort();
  }

  @Override
  public SocketAddress getRemoteSocketAddress() {
    return raw.getRemoteSocketAddress();
  }

  @Override
  public SocketAddress getLocalSocketAddress() {
    return raw.getLocalSocketAddress();
  }

  @Override
  public SocketChannel getChannel() {
    return null;
  }

  @Override
  public void setTcpNoDelay(boolean on) throws SocketException {
    raw.setTcpNoDelay(on);
  }

  @Override
  public boolean getTcpNoDelay() throws SocketException {
    return raw.getTcpNoDelay();
  }

  @Override
  public void setSoLinger(boolean on, int linger) throws SocketException {
    raw.setSoLinger(on, linger);
  }

  @Override
  public int getSoLinger() throws SocketException {
    return raw.getSoLinger();
  }

  @Override
  public void sendUrgentData(int data) throws IOException {
    throw new SocketException("This method is not supported by SSLSockets");
  }

  @Override
  public void setOOBInline(boolean on) throws SocketException {
    throw new SocketException("This method is ineffective, since sending urgent data is not supported by SSLSockets");
  }

  @Override
  public boolean getOOBInline() throws SocketException {
    throw new SocketException("This method is ineffective, since sending urgent data is not supported by SSLSockets");
  }

  @Override
  public synchronized void setSoTimeout(int timeout) throws SocketException {
    raw.setSoTimeout(timeout);
  }

  @Override
  public synchronized int getSoTimeout() throws SocketException {
    return raw.getSoTimeout();
  }

  @Override
  public synchronized void setSendBufferSize(int size) throws SocketException {
    raw.setSendBufferSize(size);
  }

  @Override
  public synchronized int getSendBufferSize() throws SocketException {
    return raw.getSendBufferSize();
  }

  @Override
  public synchronized void setReceiveBufferSize(int size) throws SocketException {
    raw.setReceiveBufferSize(size);
  }

  @Override
  public synchronized int getReceiveBufferSize() throws SocketException {
    return raw.getReceiveBufferSize();
  }

  @Override
  public void setKeepAlive(boolean on) throws SocketException {
    raw.setKeepAlive(on);
  }

  @Override
  public boolean getKeepAlive() throws SocketException {
    return raw.getKeepAlive();
  }

  @Override
  public void setTrafficClass(int tc) throws SocketException {
    raw.setTrafficClass(tc);
  }

  @Override
  public int getTrafficClass() throws SocketException {
    return raw.getTrafficClass();
  }

  @Override
  public void setReuseAddress(boolean on) throws SocketException {
    raw.setReuseAddress(on);
  }

  @Override
  public boolean getReuseAddress() throws SocketException {
    return raw.getReuseAddress();
  }

  @Override
  public void shutdownInput() throws IOException {
    if (!isConnected()) {
      throw new SocketException("Socket is not connected");
    }
    throw new UnsupportedOperationException("The method shutdownInput() is not supported in SSLSocket");
  }

  @Override
  public void shutdownOutput() throws IOException {
    if (!isConnected()) {
      throw new SocketException("Socket is not connected");
    }
    throw new UnsupportedOperationException("The method shutdownOutput() is not supported in SSLSocket");
  }

  @Override
  public boolean isConnected() {
    return raw.isConnected();
  }

  @Override
  public boolean isBound() {
    return raw.isBound();
  }

  @Override
  public boolean isClosed() {
    return closed || raw.isClosed();
  }

  @Override
  public boolean isInputShutdown() {
    return closed;
  }

  @Override
  public boolean isOutputShutdown() {
    return closed;
  }

  @Override
  public <T> Socket setOption(SocketOption<T> name, T value) throws IOException {
    raw.setOption(name, value);
    return this;
  }

  @Override
  public <T> T getOption(SocketOption<T> name) throws IOException {
    return raw.getOption(name);
  }

  @Override
  public Set<SocketOption<?>> supportedOptions() {
    return raw.supportedOptions();
  }

  @Override
  public String toString() {
    return "SSLSocket[hostname=" + host + ", " + raw + "]";
  }

  @Override
  public String[] getSupportedCipherSuites() {
    return TlsParams.supportedCipherSuites();
  }

  @Override
  public synchronized String[] getEnabledCipherSuites() {
    return cfg.cipherSuites.clone();
  }

  @Override
  public synchronized void setEnabledCipherSuites(String[] suites) {
    TlsParams.checkSuites(suites);
    cfg.cipherSuites = suites.clone();
  }

  @Override
  public String[] getSupportedProtocols() {
    return TlsParams.supportedProtocols();
  }

  @Override
  public synchronized String[] getEnabledProtocols() {
    return cfg.protocols.clone();
  }

  @Override
  public synchronized void setEnabledProtocols(String[] protocols) {
    TlsParams.checkProtocols(protocols);
    cfg.protocols = protocols.clone();
  }

  @Override
  public SSLSession getSession() {
    try {
      startHandshake();
    } catch (IOException e) {
      if (nullSession == null) {
        nullSession = SSLSessionImpl.nullSession();
      }
      return nullSession;
    }
    return conn.session;
  }

  @Override
  public SSLSession getHandshakeSession() {
    synchronized (handshakeLock) {
      if (conn != null && conn.handshakeStarted && !conn.handshakeDone) {
        return conn.session;
      }
      return null;
    }
  }

  @Override
  public void addHandshakeCompletedListener(HandshakeCompletedListener listener) {
    if (listener == null) {
      throw new IllegalArgumentException("listener is null");
    }
    synchronized (listeners) {
      listeners.add(listener);
    }
  }

  @Override
  public void removeHandshakeCompletedListener(HandshakeCompletedListener listener) {
    synchronized (listeners) {
      if (listeners.isEmpty()) {
        throw new IllegalArgumentException("no listeners");
      }
      if (!listeners.remove(listener)) {
        throw new IllegalArgumentException("listener not registered");
      }
    }
  }

  @Override
  public void setUseClientMode(boolean mode) {
    synchronized (handshakeLock) {
      if (conn != null && conn.handshakeStarted) {
        throw new IllegalArgumentException("Cannot change mode after SSL traffic has started");
      }
      if (cfg.clientMode != mode) {
        cfg.clientMode = mode;
        cfg.protocols = context.enabledProtocols(mode);
      }
    }
  }

  @Override
  public boolean getUseClientMode() {
    return cfg.clientMode;
  }

  @Override
  public void setNeedClientAuth(boolean need) {
    cfg.needClientAuth = need;
    if (need) {
      cfg.wantClientAuth = false;
    }
  }

  @Override
  public boolean getNeedClientAuth() {
    return cfg.needClientAuth;
  }

  @Override
  public void setWantClientAuth(boolean want) {
    cfg.wantClientAuth = want;
    if (want) {
      cfg.needClientAuth = false;
    }
  }

  @Override
  public boolean getWantClientAuth() {
    return cfg.wantClientAuth;
  }

  @Override
  public void setEnableSessionCreation(boolean flag) {
    cfg.enableSessionCreation = flag;
  }

  @Override
  public boolean getEnableSessionCreation() {
    return cfg.enableSessionCreation;
  }

  @Override
  public synchronized SSLParameters getSSLParameters() {
    SSLParameters params = super.getSSLParameters();
    params.setEndpointIdentificationAlgorithm(cfg.endpointIdentification);
    params.setServerNames(cfg.serverNames);
    params.setSNIMatchers(cfg.sniMatchers);
    params.setUseCipherSuitesOrder(cfg.useCipherSuitesOrder);
    params.setApplicationProtocols(cfg.applicationProtocols);
    params.setSignatureSchemes(cfg.signatureSchemes);
    params.setNamedGroups(cfg.namedGroups);
    return params;
  }

  @Override
  public synchronized void setSSLParameters(SSLParameters params) {
    super.setSSLParameters(params);
    cfg.endpointIdentification = params.getEndpointIdentificationAlgorithm();
    if (params.getServerNames() != null) {
      cfg.serverNames = params.getServerNames();
    }
    if (params.getSNIMatchers() != null) {
      cfg.sniMatchers = params.getSNIMatchers();
    }
    cfg.useCipherSuitesOrder = params.getUseCipherSuitesOrder();
    cfg.applicationProtocols = params.getApplicationProtocols();
    if (params.getSignatureSchemes() != null) {
      cfg.signatureSchemes = params.getSignatureSchemes();
    }
    if (params.getNamedGroups() != null) {
      cfg.namedGroups = params.getNamedGroups();
    }
  }

  @Override
  public synchronized String getApplicationProtocol() {
    if (conn == null || !conn.handshakeDone) {
      return null;
    }
    return conn.negotiatedApplicationProtocol == null ? "" : conn.negotiatedApplicationProtocol;
  }

  @Override
  public synchronized String getHandshakeApplicationProtocol() {
    return conn == null ? null : conn.negotiatedApplicationProtocol;
  }

  @Override
  public synchronized void setHandshakeApplicationProtocolSelector(
      BiFunction<SSLSocket, List<String>, String> selector) {
    cfg.alpnSelector = selector;
  }

  @Override
  @SuppressWarnings("unchecked")
  public synchronized BiFunction<SSLSocket, List<String>, String> getHandshakeApplicationProtocolSelector() {
    return (BiFunction<SSLSocket, List<String>, String>) cfg.alpnSelector;
  }
}
