/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.security.ssl;

import java.nio.ByteBuffer;
import java.nio.ReadOnlyBufferException;
import java.util.List;
import java.util.function.BiFunction;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSession;

final class SSLEngineImpl extends SSLEngine {
  private final SSLContextImpl context;
  private final SslConfig cfg;
  private TlsConnection conn;
  private boolean modeSet;
  private boolean clientMode = true;
  private SSLSessionImpl nullSession;

  SSLEngineImpl(SSLContextImpl context, String host, int port) {
    super(host, port);
    this.context = context;
    this.cfg = context.newConfig(true);
  }

  private void applyMode() {
    cfg.clientMode = clientMode;
    cfg.protocols = context.enabledProtocols(clientMode);
  }

  private synchronized void ensureConnection() throws SSLException {
    if (conn != null) {
      return;
    }
    if (!modeSet) {
      throw new IllegalStateException("Client/Server mode has not yet been set.");
    }
    conn = TlsConnections.create(context, cfg.copy(), getPeerHost(), getPeerPort(), this);
  }

  @Override
  public synchronized void beginHandshake() throws SSLException {
    ensureConnection();
    conn.begin();
  }

  private SSLEngineResult.HandshakeStatus handshakeStatus() {
    if (conn == null) {
      return SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING;
    }
    if (conn.hasOutput()) {
      return SSLEngineResult.HandshakeStatus.NEED_WRAP;
    }
    if (conn.handshakeJustFinished) {
      return SSLEngineResult.HandshakeStatus.FINISHED;
    }
    if (conn.handshakeStarted && !conn.handshakeDone && conn.failure == null) {
      return SSLEngineResult.HandshakeStatus.NEED_UNWRAP;
    }
    return SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING;
  }

  private SSLEngineResult result(SSLEngineResult.Status status, int consumed, int produced) {
    SSLEngineResult.HandshakeStatus hs = handshakeStatus();
    if (hs == SSLEngineResult.HandshakeStatus.FINISHED) {
      conn.handshakeJustFinished = false;
    }
    if (status == SSLEngineResult.Status.OK && conn != null && conn.outboundDone && conn.inboundDone
        && !conn.hasOutput()) {
      status = SSLEngineResult.Status.CLOSED;
    } else if (status == SSLEngineResult.Status.OK && conn != null && conn.outboundDone && !conn.hasOutput()) {
      status = SSLEngineResult.Status.CLOSED;
    }
    return new SSLEngineResult(status, hs, consumed, produced);
  }

  @Override
  public synchronized SSLEngineResult wrap(ByteBuffer[] srcs, int offset, int length, ByteBuffer dst)
      throws SSLException {
    if (srcs == null || dst == null) {
      throw new IllegalArgumentException("source or destination buffer is null");
    }
    if (dst.isReadOnly()) {
      throw new ReadOnlyBufferException();
    }
    if (offset < 0 || length < 0 || offset > srcs.length - length) {
      throw new IndexOutOfBoundsException();
    }
    long srcRemaining = 0;
    for (int i = offset; i < offset + length; i++) {
      if (srcs[i] == null) {
        throw new IllegalArgumentException("source buffer is null");
      }
      srcRemaining += srcs[i].remaining();
    }
    ensureConnection();
    conn.begin();
    if (conn.failure != null && !conn.hasOutput()) {
      throw conn.failure;
    }
    if (conn.hasOutput()) {
      byte[] rec = conn.peekOutput();
      if (dst.remaining() < rec.length) {
        return result(SSLEngineResult.Status.BUFFER_OVERFLOW, 0, 0);
      }
      dst.put(conn.pollOutput());
      return result(SSLEngineResult.Status.OK, 0, rec.length);
    }
    if (conn.outboundDone) {
      return result(SSLEngineResult.Status.CLOSED, 0, 0);
    }
    if (!conn.handshakeDone) {
      return result(SSLEngineResult.Status.OK, 0, 0);
    }
    if (srcRemaining == 0) {
      return result(SSLEngineResult.Status.OK, 0, 0);
    }
    int n = (int) Math.min(srcRemaining, TlsConnection.kMaxFragment);
    if (dst.remaining() < n + 29) {
      return result(SSLEngineResult.Status.BUFFER_OVERFLOW, 0, 0);
    }
    byte[] plain = new byte[n];
    int got = 0;
    for (int i = offset; i < offset + length && got < n; i++) {
      int take = Math.min(n - got, srcs[i].remaining());
      srcs[i].get(plain, got, take);
      got += take;
    }
    byte[] rec = conn.encryptApp(plain, 0, n);
    dst.put(rec);
    return result(SSLEngineResult.Status.OK, n, rec.length);
  }

  @Override
  public synchronized SSLEngineResult unwrap(ByteBuffer src, ByteBuffer[] dsts, int offset, int length)
      throws SSLException {
    if (src == null || dsts == null) {
      throw new IllegalArgumentException("source or destination buffer is null");
    }
    if (offset < 0 || length < 0 || offset > dsts.length - length) {
      throw new IndexOutOfBoundsException();
    }
    long dstRemaining = 0;
    for (int i = offset; i < offset + length; i++) {
      if (dsts[i] == null) {
        throw new IllegalArgumentException("destination buffer is null");
      }
      if (dsts[i].isReadOnly()) {
        throw new ReadOnlyBufferException();
      }
      dstRemaining += dsts[i].remaining();
    }
    ensureConnection();
    conn.begin();
    if (conn.appAvailable() > 0) {
      return drainApp(0, dsts, offset, length);
    }
    if (conn.failure != null) {
      throw conn.failure;
    }
    if (conn.inboundDone) {
      return result(SSLEngineResult.Status.CLOSED, 0, 0);
    }
    if (src.remaining() < 5) {
      return result(SSLEngineResult.Status.BUFFER_UNDERFLOW, 0, 0);
    }
    int pos = src.position();
    int type = src.get(pos) & 0xff;
    int major = src.get(pos + 1) & 0xff;
    int len = ((src.get(pos + 3) & 0xff) << 8) | (src.get(pos + 4) & 0xff);
    if (type < 20 || type > 23 || major != 3) {
      throw conn.fatal(TlsConnection.kAlertUnexpectedMessage, "Unrecognized SSL message, plaintext connection?");
    }
    if (len > TlsConnection.kMaxFragment + 2048) {
      throw conn.fatal(TlsConnection.kAlertRecordOverflow, "Record overflow");
    }
    if (src.remaining() < 5 + len) {
      return result(SSLEngineResult.Status.BUFFER_UNDERFLOW, 0, 0);
    }
    if (type == RecordCipher.kContentApplicationData && conn.handshakeDone && dstRemaining < Math.max(0, len - 24)) {
      return result(SSLEngineResult.Status.BUFFER_OVERFLOW, 0, 0);
    }
    byte[] fragment = new byte[len];
    for (int i = 0; i < len; i++) {
      fragment[i] = src.get(pos + 5 + i);
    }
    src.position(pos + 5 + len);
    conn.processRecord(type, fragment);
    SSLEngineResult drained = drainApp(5 + len, dsts, offset, length);
    if (conn.inboundDone && conn.appAvailable() == 0 && drained.getStatus() == SSLEngineResult.Status.OK) {
      return new SSLEngineResult(SSLEngineResult.Status.CLOSED, drained.getHandshakeStatus(), drained.bytesConsumed(),
          drained.bytesProduced());
    }
    return drained;
  }

  private SSLEngineResult drainApp(int consumed, ByteBuffer[] dsts, int offset, int length) {
    int produced = 0;
    for (int i = offset; i < offset + length && conn.appAvailable() > 0; i++) {
      int room = dsts[i].remaining();
      if (room == 0) {
        continue;
      }
      byte[] tmp = new byte[Math.min(room, conn.appAvailable())];
      int n = conn.readApp(tmp, 0, tmp.length);
      dsts[i].put(tmp, 0, n);
      produced += n;
    }
    if (conn.appAvailable() > 0 && produced == 0) {
      return result(SSLEngineResult.Status.BUFFER_OVERFLOW, consumed, 0);
    }
    return result(SSLEngineResult.Status.OK, consumed, produced);
  }

  @Override
  public Runnable getDelegatedTask() {
    return null;
  }

  @Override
  public synchronized void closeInbound() throws SSLException {
    if (conn == null) {
      return;
    }
    boolean gotCloseNotify = conn.inboundDone && conn.failure == null;
    conn.markInboundClosed();
    if (!gotCloseNotify && conn.handshakeDone) {
      throw new SSLException("Inbound closed before receiving peer's close_notify: possible truncation attack?");
    }
    if (!conn.handshakeDone && conn.handshakeStarted) {
      throw new SSLException("Inbound closed before receiving peer's close_notify: possible truncation attack?");
    }
  }

  @Override
  public synchronized boolean isInboundDone() {
    return conn != null && conn.inboundDone && conn.appAvailable() == 0;
  }

  @Override
  public synchronized void closeOutbound() {
    if (conn == null) {
      try {
        ensureConnection();
      } catch (RuntimeException | SSLException e) {
        return;
      }
    }
    conn.closeOutbound();
  }

  @Override
  public synchronized boolean isOutboundDone() {
    return conn != null && conn.outboundDone && !conn.hasOutput();
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
  public synchronized SSLSession getSession() {
    if (conn != null && conn.handshakeDone) {
      return conn.session;
    }
    if (nullSession == null) {
      nullSession = SSLSessionImpl.nullSession();
    }
    return nullSession;
  }

  @Override
  public synchronized SSLSession getHandshakeSession() {
    if (conn != null && conn.handshakeStarted && !conn.handshakeDone) {
      return conn.session;
    }
    return null;
  }

  @Override
  public synchronized SSLEngineResult.HandshakeStatus getHandshakeStatus() {
    return handshakeStatusPeek();
  }

  private SSLEngineResult.HandshakeStatus handshakeStatusPeek() {
    if (conn == null) {
      return SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING;
    }
    if (conn.hasOutput()) {
      return SSLEngineResult.HandshakeStatus.NEED_WRAP;
    }
    if (conn.handshakeStarted && !conn.handshakeDone && conn.failure == null) {
      return SSLEngineResult.HandshakeStatus.NEED_UNWRAP;
    }
    return SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING;
  }

  @Override
  public synchronized void setUseClientMode(boolean mode) {
    if (conn != null && conn.handshakeStarted) {
      throw new IllegalArgumentException("Cannot change mode after SSL traffic has started");
    }
    if (!modeSet || clientMode != mode) {
      clientMode = mode;
      modeSet = true;
      applyMode();
    }
  }

  @Override
  public synchronized boolean getUseClientMode() {
    return clientMode;
  }

  @Override
  public synchronized void setNeedClientAuth(boolean need) {
    cfg.needClientAuth = need;
    if (need) {
      cfg.wantClientAuth = false;
    }
  }

  @Override
  public synchronized boolean getNeedClientAuth() {
    return cfg.needClientAuth;
  }

  @Override
  public synchronized void setWantClientAuth(boolean want) {
    cfg.wantClientAuth = want;
    if (want) {
      cfg.needClientAuth = false;
    }
  }

  @Override
  public synchronized boolean getWantClientAuth() {
    return cfg.wantClientAuth;
  }

  @Override
  public synchronized void setEnableSessionCreation(boolean flag) {
    cfg.enableSessionCreation = flag;
  }

  @Override
  public synchronized boolean getEnableSessionCreation() {
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
      BiFunction<SSLEngine, List<String>, String> selector) {
    cfg.alpnSelector = selector;
  }

  @Override
  @SuppressWarnings("unchecked")
  public synchronized BiFunction<SSLEngine, List<String>, String> getHandshakeApplicationProtocolSelector() {
    return (BiFunction<SSLEngine, List<String>, String>) cfg.alpnSelector;
  }
}
