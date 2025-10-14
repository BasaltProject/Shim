/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.security.ssl;

import java.security.Principal;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.net.ssl.ExtendedSSLSession;
import javax.net.ssl.SNIServerName;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSessionBindingEvent;
import javax.net.ssl.SSLSessionBindingListener;
import javax.net.ssl.SSLSessionContext;

final class SSLSessionImpl extends ExtendedSSLSession {
  private final long creationTime = System.currentTimeMillis();
  private final byte[] id;
  private final String peerHost;
  private final int peerPort;
  private final SSLSessionContextImpl context;
  private final Map<String, Object> values = new HashMap<String, Object>();
  private volatile boolean valid = true;
  private long lastAccessed = creationTime;

  private String protocol = "NONE";
  private String cipherSuite = "SSL_NULL_WITH_NULL_NULL";
  private X509Certificate[] peerCerts;
  private X509Certificate[] localCerts;
  private List<SNIServerName> requestedServerNames = Collections.emptyList();
  private String[] peerSignatureAlgorithms = new String[0];
  private String[] localSignatureAlgorithms = new String[0];

  SSLSessionImpl(SSLSessionContextImpl context, String peerHost, int peerPort) {
    this.context = context;
    this.peerHost = peerHost;
    this.peerPort = peerPort;
    byte[] sid = new byte[32];
    new SecureRandom().nextBytes(sid);
    this.id = sid;
  }

  static SSLSessionImpl nullSession() {
    return new SSLSessionImpl(null, null, -1, new byte[0]);
  }

  private SSLSessionImpl(SSLSessionContextImpl context, String peerHost, int peerPort, byte[] id) {
    this.context = context;
    this.peerHost = peerHost;
    this.peerPort = peerPort;
    this.id = id;
  }

  void setNegotiated(String protocol, String cipherSuite) {
    this.protocol = protocol;
    this.cipherSuite = cipherSuite;
  }

  void setPeerCertificates(X509Certificate[] certs) {
    this.peerCerts = certs;
  }

  void setLocalCertificates(X509Certificate[] certs) {
    this.localCerts = certs;
  }

  void setRequestedServerNames(List<SNIServerName> names) {
    this.requestedServerNames = names;
  }

  void setSignatureAlgorithms(String[] local, String[] peer) {
    this.localSignatureAlgorithms = local;
    this.peerSignatureAlgorithms = peer;
  }

  @Override
  public byte[] getId() {
    return id.clone();
  }

  @Override
  public SSLSessionContext getSessionContext() {
    return context;
  }

  @Override
  public long getCreationTime() {
    return creationTime;
  }

  @Override
  public long getLastAccessedTime() {
    return lastAccessed;
  }

  void touch() {
    lastAccessed = System.currentTimeMillis();
  }

  @Override
  public void invalidate() {
    valid = false;
  }

  @Override
  public boolean isValid() {
    return valid && context != null;
  }

  @Override
  public synchronized void putValue(String name, Object value) {
    if (name == null || value == null) {
      throw new IllegalArgumentException("arguments can not be null");
    }
    Object old = values.put(name, value);
    if (old instanceof SSLSessionBindingListener) {
      ((SSLSessionBindingListener) old).valueUnbound(new SSLSessionBindingEvent(this, name));
    }
    if (value instanceof SSLSessionBindingListener) {
      ((SSLSessionBindingListener) value).valueBound(new SSLSessionBindingEvent(this, name));
    }
  }

  @Override
  public synchronized Object getValue(String name) {
    if (name == null) {
      throw new IllegalArgumentException("argument can not be null");
    }
    return values.get(name);
  }

  @Override
  public synchronized void removeValue(String name) {
    if (name == null) {
      throw new IllegalArgumentException("argument can not be null");
    }
    Object old = values.remove(name);
    if (old instanceof SSLSessionBindingListener) {
      ((SSLSessionBindingListener) old).valueUnbound(new SSLSessionBindingEvent(this, name));
    }
  }

  @Override
  public synchronized String[] getValueNames() {
    return values.keySet().toArray(new String[0]);
  }

  @Override
  public Certificate[] getPeerCertificates() throws SSLPeerUnverifiedException {
    if (peerCerts == null) {
      throw new SSLPeerUnverifiedException("peer not authenticated");
    }
    return peerCerts.clone();
  }

  @Override
  public Certificate[] getLocalCertificates() {
    return localCerts == null ? null : localCerts.clone();
  }

  @Override
  public javax.security.cert.X509Certificate[] getPeerCertificateChain() throws SSLPeerUnverifiedException {
    if (peerCerts == null) {
      throw new SSLPeerUnverifiedException("peer not authenticated");
    }
    javax.security.cert.X509Certificate[] out = new javax.security.cert.X509Certificate[peerCerts.length];
    try {
      for (int i = 0; i < out.length; i++) {
        out[i] = javax.security.cert.X509Certificate.getInstance(peerCerts[i].getEncoded());
      }
    } catch (Exception e) {
      throw new SSLPeerUnverifiedException("Failed to convert certificate chain");
    }
    return out;
  }

  @Override
  public Principal getPeerPrincipal() throws SSLPeerUnverifiedException {
    if (peerCerts == null) {
      throw new SSLPeerUnverifiedException("peer not authenticated");
    }
    return peerCerts[0].getSubjectX500Principal();
  }

  @Override
  public Principal getLocalPrincipal() {
    return localCerts == null ? null : localCerts[0].getSubjectX500Principal();
  }

  @Override
  public String getCipherSuite() {
    return cipherSuite;
  }

  @Override
  public String getProtocol() {
    return protocol;
  }

  @Override
  public String getPeerHost() {
    return peerHost;
  }

  @Override
  public int getPeerPort() {
    return peerPort;
  }

  @Override
  public int getPacketBufferSize() {
    return 16709;
  }

  @Override
  public int getApplicationBufferSize() {
    return 16704;
  }

  @Override
  public String[] getLocalSupportedSignatureAlgorithms() {
    return localSignatureAlgorithms.clone();
  }

  @Override
  public String[] getPeerSupportedSignatureAlgorithms() {
    return peerSignatureAlgorithms.clone();
  }

  @Override
  public List<SNIServerName> getRequestedServerNames() {
    return Collections.unmodifiableList(requestedServerNames);
  }

  @Override
  public List<byte[]> getStatusResponses() {
    return Collections.emptyList();
  }

  @Override
  public String toString() {
    return "Session(" + creationTime + "|" + cipherSuite + ")";
  }
}
