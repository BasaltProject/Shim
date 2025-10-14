/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 * Minimum release: 21
 */

import javax.net.ssl.*; import java.util.*; import java.security.*; import java.nio.charset.StandardCharsets;
public class SslApiModern {
  interface Body { Object get() throws Exception; }
  static void p(String n, Body b) { String o; try { Object r = b.get(); o = r instanceof Object[] ? Arrays.deepToString((Object[]) r) : String.valueOf(r); } catch (Throwable t) { o = "!" + t.getClass().getName() + ":" + t.getMessage() + (t.getCause() != null ? " <- " + t.getCause().getClass().getName() + ":" + t.getCause().getMessage() : ""); } System.out.println(n + "=" + o.replaceAll("@[0-9a-f]+", "@H")); }
  static class Name extends SNIServerName { Name(int t, byte[] b) { super(t, b); } }
  public static void main(String[] a) throws Exception {
    for (String h : new String[]{"example.com", "Example.COM", "bücher.example", "a", "", "a.", "xn--bcher-kva.example", "a b.c", "..", "-a.b"}) {
      p("sni " + h, () -> { SNIHostName n = new SNIHostName(h); return n + " | " + n.getAsciiName() + " | " + n.getType() + " | " + Arrays.toString(n.getEncoded()) + " | " + n.hashCode(); });
    }
    p("sni null", () -> new SNIHostName((String) null));
    p("sni bytes", () -> new SNIHostName("example.com".getBytes(StandardCharsets.UTF_8)));
    p("sni bytes bad", () -> new SNIHostName(new byte[]{(byte) 0xff, (byte) 0xfe}));
    p("sni bytes null", () -> new SNIHostName((byte[]) null));
    p("sni equals", () -> new SNIHostName("EXAMPLE.com").equals(new SNIHostName("example.COM")) + " " + new SNIHostName("a.b").equals(new Name(0, "a.b".getBytes())) + " " + new SNIHostName("a.b").equals(new SNIHostName("a.c")));
    p("name toString", () -> new Name(0, new byte[]{1, (byte) 0xab, 0x10}) + " | " + new Name(5, new byte[0]) + " | " + new Name(255, new byte[]{9}));
    p("name equals", () -> new Name(1, new byte[]{1}).equals(new Name(1, new byte[]{1})) + " " + new Name(1, new byte[]{1}).equals(new Name(2, new byte[]{1})) + " " + new Name(1, new byte[]{1}).hashCode());
    p("name neg", () -> new Name(-1, new byte[0]));
    p("name big", () -> new Name(256, new byte[0]));
    p("name null", () -> new Name(1, null));
    p("name clone", () -> { byte[] b = {1, 2}; Name n = new Name(1, b); b[0] = 9; n.getEncoded()[1] = 7; return Arrays.toString(n.getEncoded()); });
    p("matcher", () -> { SNIMatcher m = SNIHostName.createSNIMatcher("(.*\\.)?example\\.com"); return m.getType() + " " + m.matches(new SNIHostName("www.example.com")) + " " + m.matches(new SNIHostName("example.org")) + " " + m.matches(new SNIHostName("WWW.EXAMPLE.COM")) + " " + m.matches(new Name(0, "api.example.com".getBytes())) + " " + m.matches(new Name(0, new byte[]{(byte) 0xff})); });
    p("matcher null", () -> SNIHostName.createSNIMatcher(null));
    p("matcher bad regex", () -> SNIHostName.createSNIMatcher("("));
    p("matcher null name", () -> SNIHostName.createSNIMatcher("a").matches(null));
    p("matcher wrong type", () -> SNIHostName.createSNIMatcher("a").matches(new Name(3, new byte[0])));
    p("matcher type", () -> { try { new SNIMatcher(-1) { public boolean matches(SNIServerName n) { return false; } }; return "no"; } catch (IllegalArgumentException e) { return e.getMessage(); } });
    p("constants", () -> StandardConstants.SNI_HOST_NAME);
    p("params defaults", () -> { SSLParameters s = new SSLParameters(); return Arrays.toString(s.getCipherSuites()) + Arrays.toString(s.getProtocols()) + s.getWantClientAuth() + s.getNeedClientAuth() + s.getAlgorithmConstraints() + s.getEndpointIdentificationAlgorithm() + s.getServerNames() + s.getSNIMatchers() + s.getUseCipherSuitesOrder() + s.getEnableRetransmissions() + s.getMaximumPacketSize() + Arrays.toString(s.getApplicationProtocols()) + Arrays.toString(s.getSignatureSchemes()) + Arrays.toString(s.getNamedGroups()); });
    p("params ctor", () -> { SSLParameters s = new SSLParameters(new String[]{"A"}, new String[]{"B", "C"}); return Arrays.toString(s.getCipherSuites()) + Arrays.toString(s.getProtocols()); });
    p("params auth", () -> { SSLParameters s = new SSLParameters(); s.setWantClientAuth(true); boolean w1 = s.getWantClientAuth(); s.setNeedClientAuth(true); boolean w2 = s.getWantClientAuth(); s.setWantClientAuth(true); return w1 + " " + w2 + " " + s.getNeedClientAuth() + " " + s.getWantClientAuth(); });
    p("params clone", () -> { String[] c = {"x"}; SSLParameters s = new SSLParameters(c); c[0] = "y"; s.getCipherSuites()[0] = "z"; return s.getCipherSuites()[0]; });
    p("params names", () -> { SSLParameters s = new SSLParameters(); s.setServerNames(List.of(new SNIHostName("a.b"), new Name(5, new byte[]{1}))); List<SNIServerName> l = s.getServerNames(); try { l.add(null); } catch (UnsupportedOperationException e) { return l + " immutable"; } return l; });
    p("params names dup", () -> { SSLParameters s = new SSLParameters(); s.setServerNames(List.of(new SNIHostName("a.b"), new SNIHostName("c.d"))); return s; });
    p("params names empty/null", () -> { SSLParameters s = new SSLParameters(); s.setServerNames(new ArrayList<>()); Object e = s.getServerNames(); s.setServerNames(null); return e + " " + s.getServerNames(); });
    p("params matchers", () -> { SSLParameters s = new SSLParameters(); s.setSNIMatchers(List.of(SNIHostName.createSNIMatcher("a"))); int n = s.getSNIMatchers().size(); s.setSNIMatchers(new ArrayList<>()); Object e = s.getSNIMatchers(); s.setSNIMatchers(null); return n + " " + e + " " + s.getSNIMatchers(); });
    p("params matchers dup", () -> { SSLParameters s = new SSLParameters(); s.setSNIMatchers(List.of(SNIHostName.createSNIMatcher("a"), SNIHostName.createSNIMatcher("b"))); return s; });
    p("params alpn", () -> { SSLParameters s = new SSLParameters(); s.setApplicationProtocols(new String[]{"h2", "http/1.1"}); return Arrays.toString(s.getApplicationProtocols()); });
    p("params alpn null", () -> { new SSLParameters().setApplicationProtocols(null); return "x"; });
    p("params alpn bad", () -> { new SSLParameters().setApplicationProtocols(new String[]{"a", ""}); return "x"; });
    p("params alpn bad2", () -> { new SSLParameters().setApplicationProtocols(new String[]{null}); return "x"; });
    p("params schemes", () -> { SSLParameters s = new SSLParameters(); s.setSignatureSchemes(new String[]{"a"}); String[] r = s.getSignatureSchemes(); s.setSignatureSchemes(null); return Arrays.toString(r) + Arrays.toString(s.getSignatureSchemes()); });
    p("params schemes bad", () -> { new SSLParameters().setSignatureSchemes(new String[]{" "}); return "x"; });
    p("params groups", () -> { SSLParameters s = new SSLParameters(); s.setNamedGroups(new String[]{"x25519"}); return Arrays.toString(s.getNamedGroups()); });
    p("params groups bad", () -> { new SSLParameters().setNamedGroups(new String[]{null}); return "x"; });
    p("params packet", () -> { SSLParameters s = new SSLParameters(); s.setMaximumPacketSize(1400); s.setEnableRetransmissions(false); s.setUseCipherSuitesOrder(true); s.setEndpointIdentificationAlgorithm("HTTPS"); return s.getMaximumPacketSize() + " " + s.getEnableRetransmissions() + " " + s.getUseCipherSuitesOrder() + " " + s.getEndpointIdentificationAlgorithm(); });
    p("params packet neg", () -> { new SSLParameters().setMaximumPacketSize(-1); return "x"; });
    p("result", () -> { SSLEngineResult r = new SSLEngineResult(SSLEngineResult.Status.OK, SSLEngineResult.HandshakeStatus.NEED_WRAP, 3, 4); return r + " | " + r.getStatus() + r.getHandshakeStatus() + r.bytesConsumed() + r.bytesProduced() + r.sequenceNumber(); });
    p("result seq", () -> new SSLEngineResult(SSLEngineResult.Status.CLOSED, SSLEngineResult.HandshakeStatus.FINISHED, 0, 0, 7L).sequenceNumber());
    p("result null", () -> new SSLEngineResult(null, SSLEngineResult.HandshakeStatus.FINISHED, 0, 0));
    p("result null2", () -> new SSLEngineResult(SSLEngineResult.Status.OK, null, 0, 0));
    p("result neg", () -> new SSLEngineResult(SSLEngineResult.Status.OK, SSLEngineResult.HandshakeStatus.FINISHED, -1, 0));
    p("result neg2", () -> new SSLEngineResult(SSLEngineResult.Status.OK, SSLEngineResult.HandshakeStatus.FINISHED, 0, -1));
    p("result enums", () -> Arrays.toString(SSLEngineResult.Status.values()) + Arrays.toString(SSLEngineResult.HandshakeStatus.values()));
    p("ctx provider", () -> { SSLContext c = SSLContext.getInstance("TLS"); return c.getProtocol() + " " + c.getProvider().getName(); });
    p("ctx names", () -> { StringBuilder sb = new StringBuilder(); for (String n : new String[]{"TLS", "TLSv1.2", "TLSv1.3", "Default", "SSL", "tls", "TLSV1.3"}) { try { sb.append(SSLContext.getInstance(n).getProtocol()).append(' '); } catch (Exception e) { sb.append(e.getClass().getSimpleName()).append(' '); } } return sb; });
    p("ctx bad", () -> SSLContext.getInstance("FOO"));
    p("ctx null", () -> SSLContext.getInstance(null));
    p("ctx bad provider", () -> SSLContext.getInstance("TLS", "NOPE"));
    p("ctx uninit factory", () -> SSLContext.getInstance("TLS").getSocketFactory());
    p("ctx uninit server factory", () -> SSLContext.getInstance("TLS").getServerSocketFactory());
    p("ctx uninit engine", () -> SSLContext.getInstance("TLS").createSSLEngine());
    p("ctx default", () -> { SSLContext d = SSLContext.getDefault(); return d.getProtocol() + " " + (SSLContext.getDefault() == d); });
    p("ctx set default null", () -> { SSLContext.setDefault(null); return "x"; });
    p("ctx init", () -> { SSLContext c = SSLContext.getInstance("TLS"); c.init(null, null, null); SSLEngine e = c.createSSLEngine("host", 99); return e.getPeerHost() + e.getPeerPort() + " " + c.getClientSessionContext().getSessionTimeout() + " " + (c.getServerSessionContext() != c.getClientSessionContext()); });
    p("ctx tmf default", () -> TrustManagerFactory.getDefaultAlgorithm() + " " + KeyManagerFactory.getDefaultAlgorithm());
    p("tmf names", () -> { StringBuilder sb = new StringBuilder(); for (String n : new String[]{"PKIX", "SunX509", "X509", "SunPKIX", "X.509", "Foo"}) { try { sb.append(TrustManagerFactory.getInstance(n).getAlgorithm()).append(' '); } catch (Exception e) { sb.append(e.getClass().getSimpleName()).append(' '); } } return sb; });
    p("kmf names", () -> { StringBuilder sb = new StringBuilder(); for (String n : new String[]{"SunX509", "NewSunX509", "PKIX", "Foo"}) { try { sb.append(KeyManagerFactory.getInstance(n).getAlgorithm()).append(' '); } catch (Exception e) { sb.append(e.getClass().getSimpleName()).append(' '); } } return sb; });
    p("tmf uninit", () -> TrustManagerFactory.getInstance("PKIX").getTrustManagers());
    p("kmf uninit", () -> KeyManagerFactory.getInstance("SunX509").getKeyManagers());
    p("tmf null alg", () -> TrustManagerFactory.getInstance(null));
    p("tmf params", () -> { TrustManagerFactory.getInstance("PKIX").init((ManagerFactoryParameters) new ManagerFactoryParameters() {}); return "x"; });
    p("kmf params", () -> { KeyManagerFactory.getInstance("SunX509").init(new ManagerFactoryParameters() {}); return "x"; });
    p("tmf empty ks", () -> { KeyStore ks = KeyStore.getInstance("PKCS12"); ks.load(null, null); TrustManagerFactory t = TrustManagerFactory.getInstance("PKIX"); t.init(ks); X509TrustManager m = (X509TrustManager) t.getTrustManagers()[0]; return t.getTrustManagers().length + " " + m.getAcceptedIssuers().length + " " + (m instanceof X509ExtendedTrustManager); });
    p("tm checks", () -> { KeyStore ks = KeyStore.getInstance("PKCS12"); ks.load(null, null); TrustManagerFactory t = TrustManagerFactory.getInstance("PKIX"); t.init(ks); X509TrustManager m = (X509TrustManager) t.getTrustManagers()[0]; try { m.checkServerTrusted(null, "RSA"); } catch (Exception e) { return e.getClass().getName() + ":" + e.getMessage(); } return "x"; });
    p("tm checks2", () -> { KeyStore ks = KeyStore.getInstance("PKCS12"); ks.load(null, null); TrustManagerFactory t = TrustManagerFactory.getInstance("PKIX"); t.init(ks); X509TrustManager m = (X509TrustManager) t.getTrustManagers()[0]; try { m.checkClientTrusted(new java.security.cert.X509Certificate[0], "RSA"); } catch (Exception e) { return e.getClass().getName() + ":" + e.getMessage(); } return "x"; });
    p("kmf empty ks", () -> { KeyStore ks = KeyStore.getInstance("PKCS12"); ks.load(null, null); KeyManagerFactory k = KeyManagerFactory.getInstance("SunX509"); k.init(ks, new char[0]); X509KeyManager m = (X509KeyManager) k.getKeyManagers()[0]; return k.getKeyManagers().length + " " + m.getClientAliases("RSA", null) + " " + m.chooseServerAlias("RSA", null, null) + " " + m.getCertificateChain("x") + " " + m.getPrivateKey("x"); });
    p("https defaults", () -> { HostnameVerifier v = HttpsURLConnection.getDefaultHostnameVerifier(); return v.verify("a", null) + " " + (HttpsURLConnection.getDefaultSSLSocketFactory() != null); });
    p("https setters", () -> { HttpsURLConnection.setDefaultHostnameVerifier(null); return "x"; });
    p("https setters2", () -> { HttpsURLConnection.setDefaultSSLSocketFactory(null); return "x"; });
    p("socketfactory default", () -> SocketFactoryCheck.get());
    p("engine modes", () -> { SSLContext c = SSLContext.getInstance("TLS"); c.init(null, null, null); SSLEngine e = c.createSSLEngine(); try { e.beginHandshake(); } catch (Exception x) { return x.getClass().getName() + ":" + x.getMessage(); } return "x"; });
    p("engine wrap nomode", () -> { SSLContext c = SSLContext.getInstance("TLS"); c.init(null, null, null); SSLEngine e = c.createSSLEngine(); return e.wrap(java.nio.ByteBuffer.allocate(1), java.nio.ByteBuffer.allocate(100)); });
    p("engine defaults", () -> { SSLContext c = SSLContext.getInstance("TLS"); c.init(null, null, null); SSLEngine e = c.createSSLEngine(); e.setUseClientMode(true); return e.getUseClientMode() + " " + e.getNeedClientAuth() + e.getWantClientAuth() + e.getEnableSessionCreation() + " " + e.getHandshakeStatus() + " " + e.isInboundDone() + e.isOutboundDone() + " " + e.getSession().getProtocol() + e.getSession().getCipherSuite() + " " + e.getSession().getPacketBufferSize() + " " + e.getSession().getApplicationBufferSize() + " " + e.getSession().isValid() + " " + e.getSession().getId().length + " " + e.getHandshakeSession() + " " + e.getDelegatedTask(); });
    p("engine protocols", () -> { SSLContext c = SSLContext.getInstance("TLS"); c.init(null, null, null); SSLEngine e = c.createSSLEngine(); e.setUseClientMode(true); e.setEnabledProtocols(new String[]{"TLSv1.2"}); String r = Arrays.toString(e.getEnabledProtocols()); try { e.setEnabledProtocols(new String[]{"FOO"}); } catch (Exception x) { r += " " + x.getClass().getName() + ":" + x.getMessage(); } try { e.setEnabledProtocols(null); } catch (Exception x) { r += " " + x.getClass().getName() + ":" + x.getMessage(); } return r; });
    p("engine suites", () -> { SSLContext c = SSLContext.getInstance("TLS"); c.init(null, null, null); SSLEngine e = c.createSSLEngine(); e.setEnabledCipherSuites(new String[]{"TLS_AES_128_GCM_SHA256"}); String r = Arrays.toString(e.getEnabledCipherSuites()); try { e.setEnabledCipherSuites(new String[]{"FOO"}); } catch (Exception x) { r += " " + x.getClass().getName() + ":" + x.getMessage(); } try { e.setEnabledCipherSuites(new String[]{null}); } catch (Exception x) { r += " " + x.getClass().getName() + ":" + x.getMessage(); } try { e.setEnabledCipherSuites(null); } catch (Exception x) { r += " " + x.getClass().getName() + ":" + x.getMessage(); } return r; });
    p("engine sslparams", () -> { SSLContext c = SSLContext.getInstance("TLS"); c.init(null, null, null); SSLEngine e = c.createSSLEngine(); SSLParameters s = e.getSSLParameters(); s.setProtocols(new String[]{"TLSv1.3"}); s.setCipherSuites(new String[]{"TLS_AES_256_GCM_SHA384"}); s.setNeedClientAuth(true); s.setEndpointIdentificationAlgorithm("HTTPS"); s.setServerNames(List.of(new SNIHostName("a.b"))); s.setApplicationProtocols(new String[]{"h2"}); e.setSSLParameters(s); SSLParameters t = e.getSSLParameters(); return Arrays.toString(t.getProtocols()) + Arrays.toString(t.getCipherSuites()) + t.getNeedClientAuth() + t.getEndpointIdentificationAlgorithm() + t.getServerNames() + Arrays.toString(t.getApplicationProtocols()); });
    p("engine client auth", () -> { SSLContext c = SSLContext.getInstance("TLS"); c.init(null, null, null); SSLEngine e = c.createSSLEngine(); e.setWantClientAuth(true); boolean a1 = e.getWantClientAuth(); e.setNeedClientAuth(true); return a1 + " " + e.getWantClientAuth() + " " + e.getNeedClientAuth(); });
    p("engine app proto", () -> { SSLContext c = SSLContext.getInstance("TLS"); c.init(null, null, null); SSLEngine e = c.createSSLEngine(); e.setUseClientMode(true); return e.getApplicationProtocol() + " " + e.getHandshakeApplicationProtocol(); });
    p("engine supported", () -> { SSLContext c = SSLContext.getInstance("TLS"); c.init(null, null, null); SSLEngine e = c.createSSLEngine(); return e.getSupportedProtocols().length > 0 && e.getSupportedCipherSuites().length > 0 && Arrays.asList(e.getSupportedProtocols()).contains("TLSv1.3") && Arrays.asList(e.getSupportedCipherSuites()).contains("TLS_AES_256_GCM_SHA384"); });
    p("socket unconnected", () -> { SSLContext c = SSLContext.getInstance("TLS"); c.init(null, null, null); SSLSocket s = (SSLSocket) c.getSocketFactory().createSocket(); return s.isConnected() + " " + s.isClosed() + " " + s.getUseClientMode() + " " + s.getEnableSessionCreation() + " " + Arrays.toString(s.getEnabledProtocols()) + " " + s.getSession().getProtocol(); });
    p("socket shutdown", () -> { SSLContext c = SSLContext.getInstance("TLS"); c.init(null, null, null); SSLSocket s = (SSLSocket) c.getSocketFactory().createSocket(); s.shutdownOutput(); return "x"; });
    p("socket listener", () -> { SSLContext c = SSLContext.getInstance("TLS"); c.init(null, null, null); SSLSocket s = (SSLSocket) c.getSocketFactory().createSocket(); HandshakeCompletedListener l = e -> {}; try { s.addHandshakeCompletedListener(null); } catch (Exception x) { return x.getClass().getName() + ":" + x.getMessage(); } finally { } return "x"; });
    p("socket listener2", () -> { SSLContext c = SSLContext.getInstance("TLS"); c.init(null, null, null); SSLSocket s = (SSLSocket) c.getSocketFactory().createSocket(); s.removeHandshakeCompletedListener(e -> {}); return "x"; });
    p("socket sslparams", () -> { SSLContext c = SSLContext.getInstance("TLS"); c.init(null, null, null); SSLSocket s = (SSLSocket) c.getSocketFactory().createSocket(); SSLParameters ps = s.getSSLParameters(); ps.setWantClientAuth(true); ps.setProtocols(new String[]{"TLSv1.2"}); s.setSSLParameters(ps); return s.getWantClientAuth() + " " + s.getNeedClientAuth() + Arrays.toString(s.getEnabledProtocols()); });
    p("socket server mode", () -> { SSLContext c = SSLContext.getInstance("TLS"); c.init(null, null, null); SSLSocket s = (SSLSocket) c.getSocketFactory().createSocket(); s.setUseClientMode(false); return s.getUseClientMode(); });
    p("server socket", () -> { SSLContext c = SSLContext.getInstance("TLS"); c.init(null, null, null); SSLServerSocket s = (SSLServerSocket) c.getServerSocketFactory().createServerSocket(); s.setNeedClientAuth(true); boolean n = s.getNeedClientAuth(); s.setWantClientAuth(true); return s.getUseClientMode() + " " + n + s.getNeedClientAuth() + s.getWantClientAuth() + " " + s.getEnableSessionCreation() + " " + Arrays.toString(s.getEnabledProtocols()); });
    p("session names", () -> { SSLContext c = SSLContext.getInstance("TLS"); c.init(null, null, null); SSLSession s = c.createSSLEngine().getSession(); s.putValue("a", 1); s.putValue("b", "x"); String[] n = s.getValueNames(); Arrays.sort(n); s.removeValue("a"); return Arrays.toString(n) + s.getValue("a") + s.getValue("b"); });
    p("session null", () -> { SSLContext c = SSLContext.getInstance("TLS"); c.init(null, null, null); SSLSession s = c.createSSLEngine().getSession(); s.putValue(null, 1); return "x"; });
    p("session null2", () -> { SSLContext c = SSLContext.getInstance("TLS"); c.init(null, null, null); SSLSession s = c.createSSLEngine().getSession(); return s.getValue(null); });
    p("session peer", () -> { SSLContext c = SSLContext.getInstance("TLS"); c.init(null, null, null); SSLSession s = c.createSSLEngine().getSession(); return s.getPeerCertificates(); });
    p("session peer2", () -> { SSLContext c = SSLContext.getInstance("TLS"); c.init(null, null, null); SSLSession s = c.createSSLEngine().getSession(); return s.getPeerPrincipal(); });
    p("session local", () -> { SSLContext c = SSLContext.getInstance("TLS"); c.init(null, null, null); SSLSession s = c.createSSLEngine().getSession(); return s.getLocalCertificates() + " " + s.getLocalPrincipal(); });
    p("session ctx", () -> { SSLContext c = SSLContext.getInstance("TLS"); c.init(null, null, null); SSLSessionContext sc = c.getClientSessionContext(); sc.setSessionTimeout(10); sc.setSessionCacheSize(5); String r = sc.getSessionTimeout() + " " + sc.getSessionCacheSize() + " " + sc.getIds().hasMoreElements(); try { sc.setSessionTimeout(-1); } catch (Exception x) { r += " " + x.getClass().getName(); } try { sc.getSession(null); } catch (Exception x) { r += " " + x.getClass().getName(); } return r; });
    p("exceptions", () -> new SSLException("a", new RuntimeException("b")).getCause().getMessage() + new SSLHandshakeException("h") + new SSLKeyException("k") + new SSLPeerUnverifiedException("p") + new SSLProtocolException("q") + new SSLException(new RuntimeException("c")).getMessage());
    p("permission", () -> new SSLPermission("setHostnameVerifier").getName() + " " + new SSLPermission("getSSLSessionContext", "x").getActions());
    p("params constants", () -> new CertPathTrustManagerParameters(new java.security.cert.PKIXParameters(java.util.Set.of())).getClass());
    p("cert params", () -> { java.security.cert.PKIXBuilderParameters pp = new java.security.cert.PKIXBuilderParameters(KeyStoreUtil.empty(), null); CertPathTrustManagerParameters t = new CertPathTrustManagerParameters(pp); return t.getParameters().getClass().getSimpleName(); });
    p("keystore builder params", () -> { try { new KeyStoreBuilderParameters(new ArrayList<KeyStore.Builder>()); } catch (Exception e) { return e.getClass().getName(); } return "x"; });
  }
  static class SocketFactoryCheck { static Object get() throws Exception { javax.net.SocketFactory f = javax.net.SocketFactory.getDefault(); try { f.createSocket(); return "ok"; } catch (Exception e) { return e.getClass().getName(); } } }
  static class KeyStoreUtil { static KeyStore empty() throws Exception { KeyStore ks = KeyStore.getInstance("PKCS12"); ks.load(null, null); return ks; } }
}
