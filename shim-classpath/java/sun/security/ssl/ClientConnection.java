/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.security.ssl;

import java.nio.charset.StandardCharsets;
import java.net.Socket;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.CertPathValidatorException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.net.ssl.SNIHostName;
import javax.net.ssl.SNIServerName;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.X509ExtendedKeyManager;
import sun.security.util.HostnameChecker;

final class ClientConnection extends TlsConnection {
  private static final int kExtServerName = 0;
  private static final int kExtSupportedGroups = 10;
  private static final int kExtEcPointFormats = 11;
  private static final int kExtSignatureAlgorithms = 13;
  private static final int kExtAlpn = 16;
  private static final int kExtExtendedMasterSecret = 23;
  private static final int kExtSupportedVersions = 43;
  private static final int kExtCookie = 44;
  private static final int kExtCertificateAuthorities = 47;
  private static final int kExtKeyShare = 51;
  private static final int kExtRenegotiationInfo = 0xff01;

  private static final byte[] kHelloRetryRequestRandom = {
    (byte) 0xCF, 0x21, (byte) 0xAD, 0x74, (byte) 0xE5, (byte) 0x9A, 0x61, 0x11, (byte) 0xBE, 0x1D, (byte) 0x8C, 0x02,
    0x1E, 0x65, (byte) 0xB8, (byte) 0x91, (byte) 0xC2, (byte) 0xA2, 0x11, 0x16, 0x7A, (byte) 0xBB, (byte) 0x8C, 0x5E,
    0x07, (byte) 0x9E, 0x09, (byte) 0xE2, (byte) 0xC8, (byte) 0xA8, 0x33, (byte) 0x9C,
  };

  private static final int kStateServerHello = 0;
  private static final int kStateEncryptedExtensions = 1;
  private static final int kStateCertificateOrRequest = 2;
  private static final int kStateCertificate = 3;
  private static final int kStateCertificateVerify = 4;
  private static final int kStateFinished = 5;
  private static final int kStateTls12Certificate = 6;
  private static final int kStateTls12KeyExchange = 7;
  private static final int kStateTls12RequestOrDone = 8;
  private static final int kStateTls12Done = 9;
  private static final int kStateTls12ChangeCipherSpec = 10;
  private static final int kStateTls12Finished = 11;
  private static final int kStateComplete = 12;

  private int state = kStateServerHello;
  private byte[] clientRandom;
  private byte[] serverRandom;
  private byte[] legacySessionId;
  private List<CipherSuite> offered;
  private List<Integer> offeredVersions;
  private List<NamedGroup> groups;
  private List<SignatureScheme> schemes;
  private boolean offered13;
  private boolean offered12;
  private boolean sawHelloRetry;
  private byte[] cookie;
  private NamedGroup.KeyShareKeys keys;
  private NamedGroup keyShareGroup;
  private Tls13Schedule schedule;
  private X509Certificate[] serverChain;
  private boolean certificateRequested;
  private List<SignatureScheme> serverSchemes;
  private List<Principal> authorities;
  private byte[] certificateRequestContext = new byte[0];
  private boolean useExtendedMasterSecret;
  private byte[] masterSecret;
  private String hmac12;
  private RecordCipher pendingReadCipher;
  private byte[] serverKeyExchangeEcdhe;
  private NamedGroup serverKeyExchangeGroup;
  private PrivateKey clientKey;
  private X509Certificate[] clientChain;

  ClientConnection(SSLContextImpl context, SslConfig cfg, String peerHost, int peerPort, Object owner) {
    super(context, cfg, peerHost, peerPort, owner);
  }

  private static void ext(ByteWriter w, int id, byte[] body) {
    w.u16(id).u16(body.length).bytes(body);
  }

  private List<SNIServerName> sniNames() {
    if (cfg.serverNames != null) {
      return cfg.serverNames;
    }
    if (peerHost != null && !peerHost.isEmpty() && !HostnameChecker.isIpAddress(peerHost)
        && peerHost.indexOf(':') < 0 && peerHost.indexOf('.') >= 0) {
      try {
        return Collections.<SNIServerName>singletonList(new SNIHostName(peerHost));
      } catch (IllegalArgumentException e) {
        return Collections.emptyList();
      }
    }
    return Collections.emptyList();
  }

  @Override
  void startHandshakeImpl() throws SSLException {
    offeredVersions = TlsParams.versions(cfg);
    offered13 = offeredVersions.contains(kTls13) && !TlsParams.suites(cfg, true, false).isEmpty();
    offered12 = offeredVersions.contains(kTls12) && !TlsParams.suites(cfg, false, true).isEmpty();
    if (!offered13 && !offered12) {
      throw new SSLHandshakeException("No appropriate protocol (protocol is disabled or cipher suites are "
          + "inappropriate)");
    }
    offered = TlsParams.suites(cfg, offered13, offered12);
    groups = TlsParams.groups(cfg);
    schemes = TlsParams.schemes(cfg);
    session = new SSLSessionImpl(context.clientSessionContext(), peerHost, peerPort);
    session.setRequestedServerNames(sniNames());
    String[] names = new String[schemes.size()];
    for (int i = 0; i < names.length; i++) {
      names[i] = schemes.get(i).name;
    }
    session.setSignatureAlgorithms(names, new String[0]);
    clientRandom = new byte[32];
    random.nextBytes(clientRandom);
    legacySessionId = new byte[0];
    if (offered13) {
      legacySessionId = new byte[32];
      random.nextBytes(legacySessionId);
    }
    try {
      if (offered13 && !groups.isEmpty()) {
        keyShareGroup = groups.get(0);
        keys = keyShareGroup.generate(random);
      }
    } catch (GeneralSecurityException e) {
      throw new SSLException("Cannot generate key share", e);
    }
    sendClientHello();
  }

  private void sendClientHello() throws SSLException {
    ByteWriter w = new ByteWriter();
    w.u16(0x0303).bytes(clientRandom).vec8(legacySessionId);
    w.begin(2);
    for (CipherSuite s : offered) {
      w.u16(s.id);
    }
    if (offered12) {
      w.u16(0x00ff);
    }
    w.end();
    w.u8(1).u8(0);
    w.begin(2);
    List<SNIServerName> sni = sniNames();
    if (!sni.isEmpty()) {
      ByteWriter b = new ByteWriter();
      b.begin(2);
      for (SNIServerName n : sni) {
        b.u8(n.getType()).vec16(n.getEncoded());
      }
      b.end();
      ext(w, kExtServerName, b.toByteArray());
    }
    ByteWriter g = new ByteWriter();
    g.begin(2);
    for (NamedGroup ng : groups) {
      g.u16(ng.id);
    }
    g.end();
    ext(w, kExtSupportedGroups, g.toByteArray());
    ext(w, kExtEcPointFormats, new byte[] {1, 0});
    ByteWriter sa = new ByteWriter();
    sa.begin(2);
    for (SignatureScheme s : schemes) {
      sa.u16(s.id);
    }
    sa.end();
    ext(w, kExtSignatureAlgorithms, sa.toByteArray());
    if (cfg.applicationProtocols.length > 0) {
      ByteWriter ap = new ByteWriter();
      ap.begin(2);
      for (String p : cfg.applicationProtocols) {
        ap.vec8(p.getBytes(StandardCharsets.UTF_8));
      }
      ap.end();
      ext(w, kExtAlpn, ap.toByteArray());
    }
    if (offered12) {
      ext(w, kExtExtendedMasterSecret, new byte[0]);
      ext(w, kExtRenegotiationInfo, new byte[] {0});
    }
    if (offered13) {
      ByteWriter sv = new ByteWriter();
      sv.begin(1);
      for (int v : offeredVersions) {
        sv.u16(v);
      }
      sv.end();
      ext(w, kExtSupportedVersions, sv.toByteArray());
      if (keys != null) {
        ByteWriter ks = new ByteWriter();
        ks.begin(2).u16(keyShareGroup.id).vec16(keys.publicBytes()).end();
        ext(w, kExtKeyShare, ks.toByteArray());
      }
      if (cookie != null) {
        ext(w, kExtCookie, new ByteWriter().vec16(cookie).toByteArray());
      }
    }
    w.end();
    byte[] msg = buildMessage(kClientHello, w.toByteArray());
    transcript.bytes(msg);
    sendHandshake(msg);
  }

  @Override
  void onChangeCipherSpec() throws SSLException {
    if (state != kStateTls12ChangeCipherSpec || pendingReadCipher == null) {
      throw fatal(kAlertUnexpectedMessage, "Unexpected change_cipher_spec");
    }
    installReadCipher(pendingReadCipher);
    state = kStateTls12Finished;
  }

  @Override
  void handleHandshake(int type, byte[] body, byte[] message) throws SSLException {
    if (handshakeDone) {
      handlePostHandshake(type, body);
      return;
    }
    try {
      dispatch(type, body, message);
    } catch (GeneralSecurityException e) {
      throw fatal(kAlertInternalError, "Handshake failure: " + e.getMessage(), e);
    }
  }

  private void dispatch(int type, byte[] body, byte[] message) throws SSLException, GeneralSecurityException {
    switch (state) {
    case kStateServerHello:
      expect(type == kServerHello, type);
      handleServerHello(body, message);
      break;
    case kStateEncryptedExtensions:
      expect(type == kEncryptedExtensions, type);
      handleEncryptedExtensions(body);
      transcript.bytes(message);
      state = kStateCertificateOrRequest;
      break;
    case kStateCertificateOrRequest:
      if (type == kCertificateRequest) {
        handleCertificateRequest13(body);
        transcript.bytes(message);
        state = kStateCertificate;
        break;
      }
      expect(type == kCertificate, type);
      handleCertificate13(body);
      transcript.bytes(message);
      state = kStateCertificateVerify;
      break;
    case kStateCertificate:
      expect(type == kCertificate, type);
      handleCertificate13(body);
      transcript.bytes(message);
      state = kStateCertificateVerify;
      break;
    case kStateCertificateVerify:
      expect(type == kCertificateVerify, type);
      handleCertificateVerify13(body);
      transcript.bytes(message);
      state = kStateFinished;
      break;
    case kStateFinished:
      expect(type == kFinished, type);
      handleFinished13(body, message);
      break;
    case kStateTls12Certificate:
      expect(type == kCertificate, type);
      handleCertificate12(body);
      transcript.bytes(message);
      state = kStateTls12KeyExchange;
      break;
    case kStateTls12KeyExchange:
      expect(type == kServerKeyExchange, type);
      handleServerKeyExchange(body);
      transcript.bytes(message);
      state = kStateTls12RequestOrDone;
      break;
    case kStateTls12RequestOrDone:
      if (type == kCertificateRequest) {
        handleCertificateRequest12(body);
        transcript.bytes(message);
        state = kStateTls12Done;
        break;
      }
      expect(type == kServerHelloDone, type);
      transcript.bytes(message);
      handleServerHelloDone();
      break;
    case kStateTls12Done:
      expect(type == kServerHelloDone, type);
      transcript.bytes(message);
      handleServerHelloDone();
      break;
    case kStateTls12Finished:
      expect(type == kFinished, type);
      handleFinished12(body, message);
      break;
    default:
      throw fatal(kAlertUnexpectedMessage, "Unexpected handshake message: " + type);
    }
  }

  private void expect(boolean ok, int type) throws SSLException {
    if (!ok) {
      throw fatal(kAlertUnexpectedMessage, "Unexpected handshake message: " + type);
    }
  }

  private void handlePostHandshake(int type, byte[] body) throws SSLException {
    if (version != kTls13) {
      throw fatal(kAlertUnexpectedMessage, "Unexpected handshake message after the handshake completed");
    }
    if (type == kNewSessionTicket) {
      return;
    }
    if (type == kKeyUpdate) {
      if (body.length != 1 || (body[0] != 0 && body[0] != 1)) {
        throw fatal(kAlertIllegalParameter, "Invalid KeyUpdate message");
      }
      schedule.serverApplication = schedule.nextTrafficSecret(schedule.serverApplication);
      installReadCipher(schedule.cipher(schedule.serverApplication, true));
      if (body[0] == 1) {
        sendHandshake(buildMessage(kKeyUpdate, new byte[] {0}));
        schedule.clientApplication = schedule.nextTrafficSecret(schedule.clientApplication);
        installWriteCipher(schedule.cipher(schedule.clientApplication, false));
      }
      return;
    }
    throw fatal(kAlertUnexpectedMessage, "Unexpected handshake message after the handshake completed: " + type);
  }

  private void handleServerHello(byte[] body, byte[] message) throws SSLException, GeneralSecurityException {
    ByteReader r = new ByteReader(body);
    int legacyVersion = r.u16();
    serverRandom = r.bytes(32);
    byte[] sessionId = r.vec8();
    int suiteId = r.u16();
    int compression = r.u8();
    ByteReader exts = r.hasRemaining() ? r.sub16() : new ByteReader(new byte[0]);
    if (compression != 0) {
      throw fatal(kAlertIllegalParameter, "compression type not supported, " + compression);
    }
    boolean hello13 = false;
    int selectedVersion = 0;
    int keyShareGroupId = -1;
    byte[] keyShareData = null;
    byte[] newCookie = null;
    boolean ems = false;
    String alpn = null;
    boolean helloRetry = Arrays.equals(serverRandom, kHelloRetryRequestRandom);
    while (exts.hasRemaining()) {
      int id = exts.u16();
      ByteReader e = exts.sub16();
      switch (id) {
      case kExtSupportedVersions:
        selectedVersion = e.u16();
        hello13 = selectedVersion == kTls13;
        break;
      case kExtKeyShare:
        if (helloRetry) {
          keyShareGroupId = e.u16();
        } else {
          keyShareGroupId = e.u16();
          keyShareData = e.vec16();
        }
        break;
      case kExtCookie:
        newCookie = e.vec16();
        break;
      case kExtExtendedMasterSecret:
        ems = true;
        break;
      case kExtAlpn:
        ByteReader list = e.sub16();
        alpn = new String(list.vec8(), StandardCharsets.UTF_8);
        break;
      default:
        break;
      }
    }
    CipherSuite chosen = null;
    for (CipherSuite s : offered) {
      if (s.id == suiteId) {
        chosen = s;
      }
    }
    if (chosen == null) {
      throw fatal(kAlertIllegalParameter, "Server selected improper ciphersuite "
          + Integer.toHexString(suiteId));
    }
    if (hello13) {
      if (!offered13 || !chosen.tls13 || legacyVersion != kTls12) {
        throw fatal(kAlertIllegalParameter, "Unexpected TLS 1.3 ServerHello");
      }
      if (!Arrays.equals(sessionId, legacySessionId)) {
        throw fatal(kAlertIllegalParameter, "Illegal legacy_session_id in ServerHello");
      }
      suite = chosen;
      version = kTls13;
      if (helloRetry) {
        handleHelloRetry(keyShareGroupId, newCookie, message);
        return;
      }
      if (keyShareData == null || keyShareGroupId != keyShareGroup.id) {
        throw fatal(kAlertMissingExtension, "Missing key_share extension in ServerHello");
      }
      transcript.bytes(message);
      startTls13(keyShareData);
      return;
    }
    if (helloRetry) {
      throw fatal(kAlertIllegalParameter, "Unexpected HelloRetryRequest");
    }
    if (legacyVersion != kTls12 || !offered12 || chosen.tls13) {
      throw fatal(kAlertProtocolVersion, "The server selected protocol version "
          + (legacyVersion == 0x0302 ? "TLS11" : legacyVersion == 0x0301 ? "TLS10" : "0x"
              + Integer.toHexString(legacyVersion)) + " is not accepted by client preferences ["
          + offeredNames() + "]");
    }
    if (offered13) {
      byte[] tail = Arrays.copyOfRange(serverRandom, 24, 32);
      if (Arrays.equals(tail, new byte[] {0x44, 0x4F, 0x57, 0x4E, 0x47, 0x52, 0x44, 1})
          || Arrays.equals(tail, new byte[] {0x44, 0x4F, 0x57, 0x4E, 0x47, 0x52, 0x44, 0})) {
        throw fatal(kAlertIllegalParameter, "Unexpected ServerHello downgrade indication");
      }
    }
    suite = chosen;
    version = kTls12;
    useExtendedMasterSecret = ems;
    if (alpn != null) {
      acceptAlpn(alpn);
    }
    hmac12 = suite.hmac;
    transcript.bytes(message);
    session.setNegotiated(versionName(version), suite.name);
    state = kStateTls12Certificate;
  }

  private String offeredNames() {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < offeredVersions.size(); i++) {
      if (i > 0) {
        sb.append(", ");
      }
      sb.append(offeredVersions.get(i) == kTls13 ? "TLS13" : "TLS12");
    }
    return sb.toString();
  }

  private void acceptAlpn(String proto) throws SSLException {
    for (String p : cfg.applicationProtocols) {
      if (p.equals(proto)) {
        negotiatedApplicationProtocol = proto;
        return;
      }
    }
    throw fatal(kAlertIllegalParameter, "Invalid application_layer_protocol_negotiation: server selected " + proto);
  }

  private void handleHelloRetry(int groupId, byte[] newCookie, byte[] message) throws SSLException,
      GeneralSecurityException {
    if (sawHelloRetry) {
      throw fatal(kAlertUnexpectedMessage, "Unexpected second HelloRetryRequest");
    }
    sawHelloRetry = true;
    byte[] ch1Hash = hashOf(transcript.toByteArray());
    transcript.reset();
    transcript.bytes(buildMessage(kMessageHash, ch1Hash));
    transcript.bytes(message);
    cookie = newCookie;
    if (groupId >= 0) {
      NamedGroup g = NamedGroup.byId(groupId);
      if (g == null || !groups.contains(g) || g == keyShareGroup) {
        throw fatal(kAlertIllegalParameter, "Unsupported or unexpected key_share group in HelloRetryRequest");
      }
      keyShareGroup = g;
      keys = g.generate(random);
    } else if (newCookie == null) {
      throw fatal(kAlertIllegalParameter, "Empty HelloRetryRequest");
    }
    sendCompatChangeCipherSpec();
    sendClientHello();
  }

  private void startTls13(byte[] serverShare) throws SSLException, GeneralSecurityException {
    byte[] shared;
    try {
      shared = keys.deriveSecret(serverShare);
    } catch (GeneralSecurityException e) {
      throw fatal(kAlertIllegalParameter, "Invalid key_share: " + e.getMessage(), e);
    }
    schedule = new Tls13Schedule(suite);
    schedule.deriveHandshake(shared, transcriptHash());
    sendCompatChangeCipherSpec();
    installReadCipher(schedule.cipher(schedule.serverHandshake, true));
    installWriteCipher(schedule.cipher(schedule.clientHandshake, false));
    session.setNegotiated(versionName(version), suite.name);
    state = kStateEncryptedExtensions;
  }

  private void handleEncryptedExtensions(byte[] body) throws SSLException {
    ByteReader r = new ByteReader(body);
    ByteReader exts = r.sub16();
    while (exts.hasRemaining()) {
      int id = exts.u16();
      ByteReader e = exts.sub16();
      if (id == kExtAlpn) {
        ByteReader list = e.sub16();
        acceptAlpn(new String(list.vec8(), StandardCharsets.UTF_8));
      }
    }
  }

  private void handleCertificateRequest13(byte[] body) throws SSLException {
    ByteReader r = new ByteReader(body);
    certificateRequestContext = r.vec8();
    ByteReader exts = r.sub16();
    serverSchemes = new ArrayList<SignatureScheme>();
    authorities = new ArrayList<Principal>();
    while (exts.hasRemaining()) {
      int id = exts.u16();
      ByteReader e = exts.sub16();
      if (id == kExtSignatureAlgorithms) {
        ByteReader list = e.sub16();
        while (list.hasRemaining()) {
          SignatureScheme s = TlsCrypto.findScheme(list.u16(), schemes);
          if (s != null) {
            serverSchemes.add(s);
          }
        }
      } else if (id == kExtCertificateAuthorities) {
        ByteReader list = e.sub16();
        while (list.hasRemaining()) {
          authorities.add(new javax.security.auth.x500.X500Principal(list.vec16()));
        }
      }
    }
    certificateRequested = true;
  }

  private void handleCertificate13(byte[] body) throws SSLException {
    ByteReader r = new ByteReader(body);
    byte[] ctx = r.vec8();
    if (ctx.length != 0) {
      throw fatal(kAlertIllegalParameter, "Unexpected certificate_request_context");
    }
    ByteReader list = r.sub24();
    List<byte[]> certs = new ArrayList<byte[]>();
    while (list.hasRemaining()) {
      certs.add(list.vec24());
      list.sub16();
    }
    verifyServerChain(certs);
  }

  private void verifyServerChain(List<byte[]> encoded) throws SSLException {
    if (encoded.isEmpty()) {
      throw fatal(kAlertBadCertificate, "Empty server certificate chain");
    }
    try {
      serverChain = TlsCrypto.parseChain(encoded);
    } catch (CertificateException e) {
      throw fatal(kAlertBadCertificate, "Failed to parse server certificates", e);
    }
    String authType = version == kTls13 ? serverChain[0].getPublicKey().getAlgorithm()
        : suite.keyExchange;
    if (authType.equals("RSASSA-PSS")) {
      authType = "RSA";
    }
    try {
      context.checkServerTrusted(serverChain, authType, owner);
    } catch (CertificateException e) {
      int alert = kAlertCertificateUnknown;
      Throwable c = e.getCause();
      if (c instanceof CertPathValidatorException) {
        CertPathValidatorException.Reason reason = ((CertPathValidatorException) c).getReason();
        if (reason == CertPathValidatorException.BasicReason.EXPIRED) {
          alert = kAlertCertificateExpired;
        } else if (reason == CertPathValidatorException.BasicReason.INVALID_SIGNATURE
            || reason == CertPathValidatorException.BasicReason.UNSPECIFIED) {
          alert = kAlertBadCertificate;
        }
      } else if (e.getMessage() != null && (e.getMessage().startsWith("No subject alternative")
          || e.getMessage().startsWith("No name matching") || e.getMessage().startsWith("Unknown identification"))) {
        alert = kAlertCertificateUnknown;
      }
      throw fatal(alert, e.getMessage(), e);
    } catch (RuntimeException e) {
      throw fatal(kAlertInternalError, "Certificate validation failed: " + e, e);
    }
    session.setPeerCertificates(serverChain);
  }

  private void handleCertificateVerify13(byte[] body) throws SSLException, GeneralSecurityException {
    ByteReader r = new ByteReader(body);
    int schemeId = r.u16();
    byte[] signature = r.vec16();
    SignatureScheme scheme = TlsCrypto.findScheme(schemeId, schemes);
    PublicKey pub = serverChain[0].getPublicKey();
    if (scheme == null || !scheme.usableWith(pub.getAlgorithm(), SignatureScheme.PublicKeyInfo.of(pub), true)) {
      throw fatal(kAlertIllegalParameter, "Unsupported signature scheme: 0x" + Integer.toHexString(schemeId));
    }
    byte[] content = TlsCrypto.certificateVerifyContent(true, transcriptHash());
    boolean ok;
    try {
      ok = TlsCrypto.verify(scheme, pub, content, signature);
    } catch (GeneralSecurityException e) {
      ok = false;
    }
    if (!ok) {
      throw fatal(kAlertDecryptError, "Invalid CertificateVerify signature");
    }
  }

  private void handleFinished13(byte[] body, byte[] message) throws SSLException, GeneralSecurityException {
    byte[] expected = schedule.verifyData(schedule.serverHandshake, transcriptHash());
    if (!MessageDigest.isEqual(expected, body)) {
      throw fatal(kAlertDecryptError, "Invalid Finished message");
    }
    transcript.bytes(message);
    byte[] serverFinishedHash = transcriptHash();
    schedule.deriveApplication(serverFinishedHash);
    if (certificateRequested) {
      sendClientCertificate13();
    }
    byte[] verify = schedule.verifyData(schedule.clientHandshake, transcriptHash());
    byte[] fin = buildMessage(kFinished, verify);
    transcript.bytes(fin);
    sendHandshake(fin);
    installWriteCipher(schedule.cipher(schedule.clientApplication, false));
    installReadCipher(schedule.cipher(schedule.serverApplication, true));
    state = kStateComplete;
    finishHandshake();
  }

  private void chooseClientCredentials(List<SignatureScheme> accepted) {
    X509ExtendedKeyManager km = context.keyManager();
    if (km == null) {
      return;
    }
    List<String> types = new ArrayList<String>();
    for (SignatureScheme s : accepted) {
      String t = TlsCrypto.keyTypeFor(s);
      if (!types.contains(t)) {
        types.add(t);
      }
    }
    Principal[] issuers = authorities == null || authorities.isEmpty() ? null : authorities.toArray(new Principal[0]);
    String alias;
    if (owner instanceof SSLEngine) {
      alias = km.chooseEngineClientAlias(types.toArray(new String[0]), issuers, (SSLEngine) owner);
    } else {
      alias = km.chooseClientAlias(types.toArray(new String[0]), issuers, owner instanceof Socket ? (Socket) owner
          : null);
    }
    if (alias == null) {
      return;
    }
    clientChain = km.getCertificateChain(alias);
    clientKey = km.getPrivateKey(alias);
    if (clientChain == null || clientKey == null) {
      clientChain = null;
      clientKey = null;
    }
  }

  private void sendClientCertificate13() throws SSLException, GeneralSecurityException {
    chooseClientCredentials(serverSchemes == null ? schemes : serverSchemes);
    ByteWriter w = new ByteWriter();
    w.vec8(certificateRequestContext);
    w.begin(3);
    SignatureScheme scheme = null;
    if (clientChain != null) {
      for (SignatureScheme s : serverSchemes == null ? schemes : serverSchemes) {
        if (s.usableWith(clientKey.getAlgorithm(), SignatureScheme.PublicKeyInfo.of(clientChain[0].getPublicKey()),
            true)) {
          scheme = s;
          break;
        }
      }
    }
    if (scheme == null) {
      clientChain = null;
    }
    if (clientChain != null) {
      try {
        for (X509Certificate c : clientChain) {
          w.vec24(c.getEncoded());
          w.u16(0);
        }
      } catch (CertificateException e) {
        throw fatal(kAlertInternalError, "Cannot encode client certificate", e);
      }
    }
    w.end();
    byte[] certMsg = buildMessage(kCertificate, w.toByteArray());
    transcript.bytes(certMsg);
    sendHandshake(certMsg);
    if (clientChain == null) {
      return;
    }
    session.setLocalCertificates(clientChain);
    byte[] content = TlsCrypto.certificateVerifyContent(false, transcriptHash());
    byte[] sig = TlsCrypto.sign(scheme, clientKey, content, random);
    byte[] cv = buildMessage(kCertificateVerify, new ByteWriter().u16(scheme.id).vec16(sig).toByteArray());
    transcript.bytes(cv);
    sendHandshake(cv);
  }

  private void handleCertificate12(byte[] body) throws SSLException {
    ByteReader r = new ByteReader(body);
    ByteReader list = r.sub24();
    List<byte[]> certs = new ArrayList<byte[]>();
    while (list.hasRemaining()) {
      certs.add(list.vec24());
    }
    verifyServerChain(certs);
  }

  private void handleServerKeyExchange(byte[] body) throws SSLException, GeneralSecurityException {
    ByteReader r = new ByteReader(body);
    int curveType = r.u8();
    int groupId = r.u16();
    byte[] point = r.vec8();
    int paramsLen = 1 + 2 + 1 + point.length;
    if (curveType != 3) {
      throw fatal(kAlertIllegalParameter, "Unsupported ECCurveType: " + curveType);
    }
    NamedGroup g = NamedGroup.byId(groupId);
    if (g == null || !groups.contains(g)) {
      throw fatal(kAlertIllegalParameter, "Unsupported named group: " + groupId);
    }
    int schemeId = r.u16();
    byte[] signature = r.vec16();
    SignatureScheme scheme = TlsCrypto.findScheme(schemeId, schemes);
    PublicKey pub = serverChain[0].getPublicKey();
    if (scheme == null || !scheme.usableWith(pub.getAlgorithm(), SignatureScheme.PublicKeyInfo.of(pub), false)) {
      throw fatal(kAlertIllegalParameter, "Unsupported signature scheme: 0x" + Integer.toHexString(schemeId));
    }
    byte[] signed = TlsPrf.concat(clientRandom, serverRandom, Arrays.copyOf(body, paramsLen));
    boolean ok;
    try {
      ok = TlsCrypto.verify(scheme, pub, signed, signature);
    } catch (GeneralSecurityException e) {
      ok = false;
    }
    if (!ok) {
      throw fatal(kAlertDecryptError, "Invalid ServerKeyExchange signature");
    }
    serverKeyExchangeGroup = g;
    serverKeyExchangeEcdhe = point;
  }

  private void handleCertificateRequest12(byte[] body) throws SSLException {
    ByteReader r = new ByteReader(body);
    r.vec8();
    ByteReader sa = r.sub16();
    serverSchemes = new ArrayList<SignatureScheme>();
    while (sa.hasRemaining()) {
      SignatureScheme s = TlsCrypto.findScheme(sa.u16(), schemes);
      if (s != null) {
        serverSchemes.add(s);
      }
    }
    ByteReader cas = r.sub16();
    authorities = new ArrayList<Principal>();
    while (cas.hasRemaining()) {
      authorities.add(new javax.security.auth.x500.X500Principal(cas.vec16()));
    }
    certificateRequested = true;
  }

  private void handleServerHelloDone() throws SSLException, GeneralSecurityException {
    NamedGroup.KeyShareKeys ephemeral = serverKeyExchangeGroup.generate(random);
    byte[] shared;
    try {
      shared = ephemeral.deriveSecret(serverKeyExchangeEcdhe);
    } catch (GeneralSecurityException e) {
      throw fatal(kAlertIllegalParameter, "Invalid server key exchange value: " + e.getMessage(), e);
    }
    SignatureScheme certScheme = null;
    if (certificateRequested) {
      chooseClientCredentials(serverSchemes);
      if (clientChain != null) {
        for (SignatureScheme s : serverSchemes) {
          if (s.usableWith(clientKey.getAlgorithm(), SignatureScheme.PublicKeyInfo.of(
              clientChain[0].getPublicKey()), false)) {
            certScheme = s;
            break;
          }
        }
      }
      ByteWriter cw = new ByteWriter();
      cw.begin(3);
      if (certScheme != null) {
        try {
          for (X509Certificate c : clientChain) {
            cw.vec24(c.getEncoded());
          }
        } catch (CertificateException e) {
          throw fatal(kAlertInternalError, "Cannot encode client certificate", e);
        }
        session.setLocalCertificates(clientChain);
      }
      cw.end();
      byte[] certMsg = buildMessage(kCertificate, cw.toByteArray());
      transcript.bytes(certMsg);
      sendHandshake(certMsg);
    }
    byte[] cke = buildMessage(kClientKeyExchange, new ByteWriter().vec8(ephemeral.publicBytes()).toByteArray());
    transcript.bytes(cke);
    byte[] sessionHash = hashOf(transcript.toByteArray());
    sendHandshake(cke);
    if (certScheme != null) {
      byte[] sig = TlsCrypto.sign(certScheme, clientKey, transcript.toByteArray(), random);
      byte[] cv = buildMessage(kCertificateVerify, new ByteWriter().u16(certScheme.id).vec16(sig).toByteArray());
      transcript.bytes(cv);
      sendHandshake(cv);
    }
    String label = useExtendedMasterSecret ? "extended master secret" : "master secret";
    byte[] seed = useExtendedMasterSecret ? sessionHash : TlsPrf.concat(clientRandom, serverRandom);
    masterSecret = TlsPrf.prf(hmac12, shared, label, seed, 48);
    int keyLen = suite.keyLength;
    byte[] block = TlsPrf.prf(hmac12, masterSecret, "key expansion", TlsPrf.concat(serverRandom, clientRandom),
        2 * keyLen + 8);
    byte[] cKey = Arrays.copyOfRange(block, 0, keyLen);
    byte[] sKey = Arrays.copyOfRange(block, keyLen, 2 * keyLen);
    byte[] cIv = Arrays.copyOfRange(block, 2 * keyLen, 2 * keyLen + 4);
    byte[] sIv = Arrays.copyOfRange(block, 2 * keyLen + 4, 2 * keyLen + 8);
    pendingReadCipher = new RecordCipher(false, CipherSuite.kAeadGcm, sKey, sIv, true);
    sendChangeCipherSpec();
    installWriteCipher(new RecordCipher(false, CipherSuite.kAeadGcm, cKey, cIv, false));
    byte[] verify = TlsPrf.prf(hmac12, masterSecret, "client finished", transcriptHash(), 12);
    byte[] fin = buildMessage(kFinished, verify);
    transcript.bytes(fin);
    sendHandshake(fin);
    state = kStateTls12ChangeCipherSpec;
  }

  private void handleFinished12(byte[] body, byte[] message) throws SSLException, GeneralSecurityException {
    byte[] expected = TlsPrf.prf(hmac12, masterSecret, "server finished", transcriptHash(), 12);
    if (!MessageDigest.isEqual(expected, body)) {
      throw fatal(kAlertDecryptError, "Invalid Finished message");
    }
    transcript.bytes(message);
    state = kStateComplete;
    finishHandshake();
  }
}
