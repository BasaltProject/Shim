/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.security.ssl;

import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import javax.net.ssl.SNIHostName;
import javax.net.ssl.SNIMatcher;
import javax.net.ssl.SNIServerName;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLException;
import javax.net.ssl.X509ExtendedKeyManager;

final class ServerConnection extends TlsConnection {
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

  private static final int kStateClientHello = 0;
  private static final int kStateClientHello2 = 1;
  private static final int kStateClientCertificate = 2;
  private static final int kStateClientCertificateVerify = 3;
  private static final int kStateClientFinished = 4;
  private static final int kStateTls12ClientCertificate = 5;
  private static final int kStateTls12ClientKeyExchange = 6;
  private static final int kStateTls12ClientCertificateVerify = 7;
  private static final int kStateTls12ChangeCipherSpec = 8;
  private static final int kStateTls12Finished = 9;
  private static final int kStateComplete = 10;

  private int state = kStateClientHello;
  private byte[] clientRandom;
  private byte[] serverRandom;
  private byte[] clientSessionId;
  private List<Integer> clientSuiteIds;
  private List<Integer> clientVersions;
  private List<NamedGroup> clientGroups;
  private List<SignatureScheme> clientSchemes;
  private final List<int[]> unused = new ArrayList<int[]>();
  private Map<Integer, byte[]> clientKeyShares;
  private List<String> clientAlpn;
  private boolean clientExtendedMasterSecret;
  private boolean hrrSent;
  private int clientLegacyVersion;
  private Tls13Schedule schedule;
  private X509Certificate[] serverChain;
  private PrivateKey serverKey;
  private SignatureScheme serverScheme;
  private boolean clientAuthRequested;
  private X509Certificate[] clientChain;
  private byte[] masterSecret;
  private String hmac12;
  private RecordCipher pendingReadCipher;
  private NamedGroup.KeyShareKeys ephemeral;
  private byte[] clientHelloHashForHrr;

  ServerConnection(SSLContextImpl context, SslConfig cfg, String peerHost, int peerPort, Object owner) {
    super(context, cfg, peerHost, peerPort, owner);
  }

  @Override
  void startHandshakeImpl() throws SSLException {
    session = new SSLSessionImpl(context.serverSessionContext(), peerHost, peerPort);
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

  private void expect(boolean ok, int type) throws SSLException {
    if (!ok) {
      throw fatal(kAlertUnexpectedMessage, "Unexpected handshake message: " + type);
    }
  }

  private void dispatch(int type, byte[] body, byte[] message) throws SSLException, GeneralSecurityException {
    switch (state) {
    case kStateClientHello:
    case kStateClientHello2:
      expect(type == kClientHello, type);
      handleClientHello(body, message);
      break;
    case kStateClientCertificate:
      expect(type == kCertificate, type);
      handleClientCertificate13(body);
      transcript.bytes(message);
      state = clientChain == null ? kStateClientFinished : kStateClientCertificateVerify;
      break;
    case kStateClientCertificateVerify:
      expect(type == kCertificateVerify, type);
      handleClientCertificateVerify13(body);
      transcript.bytes(message);
      state = kStateClientFinished;
      break;
    case kStateClientFinished:
      expect(type == kFinished, type);
      handleClientFinished13(body, message);
      break;
    case kStateTls12ClientCertificate:
      expect(type == kCertificate, type);
      handleClientCertificate12(body);
      transcript.bytes(message);
      state = kStateTls12ClientKeyExchange;
      break;
    case kStateTls12ClientKeyExchange:
      expect(type == kClientKeyExchange, type);
      handleClientKeyExchange(body, message);
      break;
    case kStateTls12ClientCertificateVerify:
      expect(type == kCertificateVerify, type);
      handleClientCertificateVerify12(body);
      transcript.bytes(message);
      state = kStateTls12ChangeCipherSpec;
      break;
    case kStateTls12Finished:
      expect(type == kFinished, type);
      handleClientFinished12(body, message);
      break;
    default:
      throw fatal(kAlertUnexpectedMessage, "Unexpected handshake message: " + type);
    }
  }

  private void handlePostHandshake(int type, byte[] body) throws SSLException {
    if (version == kTls13 && type == kKeyUpdate && body.length == 1) {
      schedule.clientApplication = schedule.nextTrafficSecret(schedule.clientApplication);
      installReadCipher(schedule.cipher(schedule.clientApplication, true));
      if (body[0] == 1) {
        sendHandshake(buildMessage(kKeyUpdate, new byte[] {0}));
        schedule.serverApplication = schedule.nextTrafficSecret(schedule.serverApplication);
        installWriteCipher(schedule.cipher(schedule.serverApplication, false));
      }
      return;
    }
    throw fatal(kAlertUnexpectedMessage, "Unexpected handshake message after the handshake completed: " + type);
  }

  private void handleClientHello(byte[] body, byte[] message) throws SSLException, GeneralSecurityException {
    ByteReader r = new ByteReader(body);
    clientLegacyVersion = r.u16();
    byte[] newRandom = r.bytes(32);
    byte[] sessionId = r.vec8();
    ByteReader suites = r.sub16();
    List<Integer> ids = new ArrayList<Integer>();
    while (suites.hasRemaining()) {
      ids.add(suites.u16());
    }
    r.vec8();
    ByteReader exts = r.hasRemaining() ? r.sub16() : new ByteReader(new byte[0]);
    List<Integer> versions = new ArrayList<Integer>();
    List<NamedGroup> groups = new ArrayList<NamedGroup>();
    List<SignatureScheme> schemes = new ArrayList<SignatureScheme>();
    Map<Integer, byte[]> shares = new LinkedHashMap<Integer, byte[]>();
    List<String> alpn = new ArrayList<String>();
    List<SNIServerName> names = new ArrayList<SNIServerName>();
    boolean ems = false;
    boolean sawSigAlgs = false;
    while (exts.hasRemaining()) {
      int id = exts.u16();
      ByteReader e = exts.sub16();
      switch (id) {
      case kExtSupportedVersions:
        ByteReader vs = e.sub8();
        while (vs.hasRemaining()) {
          versions.add(vs.u16());
        }
        break;
      case kExtSupportedGroups:
        ByteReader gs = e.sub16();
        while (gs.hasRemaining()) {
          NamedGroup g = NamedGroup.byId(gs.u16());
          if (g != null) {
            groups.add(g);
          }
        }
        break;
      case kExtSignatureAlgorithms:
        sawSigAlgs = true;
        ByteReader ss = e.sub16();
        while (ss.hasRemaining()) {
          SignatureScheme s = SignatureScheme.byId(ss.u16());
          if (s != null) {
            schemes.add(s);
          }
        }
        break;
      case kExtKeyShare:
        ByteReader ks = e.sub16();
        while (ks.hasRemaining()) {
          int gid = ks.u16();
          shares.put(gid, ks.vec16());
        }
        break;
      case kExtAlpn:
        ByteReader al = e.sub16();
        while (al.hasRemaining()) {
          alpn.add(new String(al.vec8(), StandardCharsets.UTF_8));
        }
        break;
      case kExtServerName:
        ByteReader sn = e.sub16();
        while (sn.hasRemaining()) {
          int nameType = sn.u8();
          byte[] nameBytes = sn.vec16();
          if (nameType == 0) {
            try {
              names.add(new SNIHostName(nameBytes));
            } catch (IllegalArgumentException ex) {
              throw fatal(kAlertIllegalParameter, "Invalid server name indication");
            }
          }
        }
        break;
      case kExtExtendedMasterSecret:
        ems = true;
        break;
      case kExtCookie:
        break;
      default:
        break;
      }
    }
    if (state == kStateClientHello2) {
      if (!Arrays.equals(newRandom, clientRandom) || !Arrays.equals(sessionId, clientSessionId)) {
        throw fatal(kAlertIllegalParameter, "Inconsistent ClientHello after HelloRetryRequest");
      }
    }
    clientRandom = newRandom;
    clientSessionId = sessionId;
    clientSuiteIds = ids;
    clientVersions = versions;
    clientGroups = groups;
    clientSchemes = schemes;
    clientKeyShares = shares;
    clientAlpn = alpn;
    clientExtendedMasterSecret = ems;
    if (!sawSigAlgs && state == kStateClientHello) {
      clientSchemes = new ArrayList<SignatureScheme>();
    }
    session.setRequestedServerNames(names);
    List<Integer> enabled = TlsParams.versions(cfg);
    boolean use13 = enabled.contains(kTls13) && clientVersions.contains(kTls13);
    boolean use12 = enabled.contains(kTls12) && (clientVersions.isEmpty() ? clientLegacyVersion >= kTls12
        : clientVersions.contains(kTls12) || clientLegacyVersion >= kTls12 && !clientVersions.contains(kTls13));
    if (!use13 && !use12) {
      throw fatal(kAlertProtocolVersion, "The client supported protocol versions are not accepted by server "
          + "preferences");
    }
    checkSni(names);
    if (use13) {
      handleClientHello13(message);
    } else {
      version = kTls12;
      transcript.bytes(message);
      handleClientHello12();
    }
  }

  private void checkSni(List<SNIServerName> names) throws SSLException {
    Collection<SNIMatcher> matchers = cfg.sniMatchers;
    if (matchers == null || matchers.isEmpty()) {
      return;
    }
    for (SNIMatcher m : matchers) {
      boolean matched = false;
      for (SNIServerName n : names) {
        if (n.getType() == m.getType() && m.matches(n)) {
          matched = true;
        }
      }
      if (!matched && !names.isEmpty()) {
        throw fatal(112, "Unrecognized server name indication");
      }
    }
  }

  private String selectAlpn() throws SSLException {
    if (clientAlpn.isEmpty()) {
      return null;
    }
    if (cfg.alpnSelector != null) {
      @SuppressWarnings("unchecked")
      BiFunction<Object, List<String>, String> sel = (BiFunction<Object, List<String>, String>) cfg.alpnSelector;
      String chosen = sel.apply(owner, new ArrayList<String>(clientAlpn));
      if (chosen == null) {
        return null;
      }
      if (chosen.isEmpty() || !clientAlpn.contains(chosen)) {
        throw fatal(kAlertNoApplicationProtocol, "No matching application protocol");
      }
      return chosen;
    }
    if (cfg.applicationProtocols.length == 0) {
      return null;
    }
    for (String p : cfg.applicationProtocols) {
      if (clientAlpn.contains(p)) {
        return p;
      }
    }
    throw fatal(kAlertNoApplicationProtocol, "No matching application protocol");
  }

  private boolean chooseCredentials(List<SignatureScheme> candidates, boolean tls13) {
    X509ExtendedKeyManager km = context.keyManager();
    if (km == null) {
      return false;
    }
    for (SignatureScheme s : candidates) {
      String keyType = TlsCrypto.keyTypeFor(s);
      String alias;
      if (owner instanceof SSLEngine) {
        alias = km.chooseEngineServerAlias(keyType, null, (SSLEngine) owner);
      } else {
        alias = km.chooseServerAlias(keyType, null, owner instanceof Socket ? (Socket) owner : null);
      }
      if (alias == null) {
        continue;
      }
      X509Certificate[] chain = km.getCertificateChain(alias);
      PrivateKey key = km.getPrivateKey(alias);
      if (chain == null || key == null) {
        continue;
      }
      PublicKey pub = chain[0].getPublicKey();
      if (s.usableWith(pub.getAlgorithm(), SignatureScheme.PublicKeyInfo.of(pub), tls13)) {
        serverChain = chain;
        serverKey = key;
        serverScheme = s;
        return true;
      }
    }
    return false;
  }

  private List<SignatureScheme> usableSchemes() {
    List<SignatureScheme> ours = TlsParams.schemes(cfg);
    List<SignatureScheme> out = new ArrayList<SignatureScheme>();
    for (SignatureScheme s : clientSchemes) {
      if (ours.contains(s)) {
        out.add(s);
      }
    }
    return out;
  }

  private void handleClientHello13(byte[] message) throws SSLException, GeneralSecurityException {
    version = kTls13;
    List<CipherSuite> ours = TlsParams.suites(cfg, true, false);
    CipherSuite chosen = null;
    if (cfg.useCipherSuitesOrder) {
      for (CipherSuite s : ours) {
        if (clientSuiteIds.contains(s.id)) {
          chosen = s;
          break;
        }
      }
    } else {
      for (int id : clientSuiteIds) {
        for (CipherSuite s : ours) {
          if (s.id == id) {
            chosen = s;
            break;
          }
        }
        if (chosen != null) {
          break;
        }
      }
    }
    if (chosen == null) {
      throw fatal(kAlertHandshakeFailure, "No cipher suites in common");
    }
    suite = chosen;
    NamedGroup group = null;
    byte[] share = null;
    List<NamedGroup> ourGroups = TlsParams.groups(cfg);
    for (NamedGroup g : clientGroups) {
      if (ourGroups.contains(g) && clientKeyShares.containsKey(g.id)) {
        group = g;
        share = clientKeyShares.get(g.id);
        break;
      }
    }
    if (group == null) {
      NamedGroup retry = null;
      for (NamedGroup g : clientGroups) {
        if (ourGroups.contains(g)) {
          retry = g;
          break;
        }
      }
      if (retry == null || hrrSent) {
        throw fatal(kAlertHandshakeFailure, "No supported key share groups");
      }
      sendHelloRetryRequest(retry, message);
      return;
    }
    List<SignatureScheme> candidates = usableSchemes();
    if (!chooseCredentials(candidates, true)) {
      throw fatal(kAlertHandshakeFailure, "No available authentication scheme");
    }
    String alpn = selectAlpn();
    negotiatedApplicationProtocol = alpn;
    transcript.bytes(message);
    ephemeral = group.generate(random);
    byte[] shared;
    try {
      shared = ephemeral.deriveSecret(share);
    } catch (GeneralSecurityException e) {
      throw fatal(kAlertIllegalParameter, "Invalid key_share: " + e.getMessage(), e);
    }
    serverRandom = new byte[32];
    random.nextBytes(serverRandom);
    ByteWriter sh = new ByteWriter();
    sh.u16(0x0303).bytes(serverRandom).vec8(clientSessionId).u16(suite.id).u8(0);
    sh.begin(2);
    sh.u16(kExtSupportedVersions).u16(2).u16(kTls13);
    ByteWriter ks = new ByteWriter();
    ks.u16(group.id).vec16(ephemeral.publicBytes());
    sh.u16(kExtKeyShare).vec16(ks.toByteArray());
    sh.end();
    byte[] shMsg = buildMessage(kServerHello, sh.toByteArray());
    transcript.bytes(shMsg);
    sendHandshake(shMsg);
    schedule = new Tls13Schedule(suite);
    schedule.deriveHandshake(shared, transcriptHash());
    sendCompatChangeCipherSpec();
    installWriteCipher(schedule.cipher(schedule.serverHandshake, false));
    installReadCipher(schedule.cipher(schedule.clientHandshake, true));
    session.setNegotiated(versionName(version), suite.name);
    String[] names = new String[candidates.size()];
    for (int i = 0; i < names.length; i++) {
      names[i] = candidates.get(i).name;
    }
    session.setSignatureAlgorithms(new String[0], names);
    sendServerFlight13();
  }

  private void sendHelloRetryRequest(NamedGroup group, byte[] clientHello) throws SSLException,
      GeneralSecurityException {
    hrrSent = true;
    byte[] ch1Hash = hashOf(clientHello);
    transcript.reset();
    transcript.bytes(buildMessage(kMessageHash, ch1Hash));
    ByteWriter sh = new ByteWriter();
    sh.u16(0x0303).bytes(kHelloRetryRequestRandom).vec8(clientSessionId).u16(suite.id).u8(0);
    sh.begin(2);
    sh.u16(kExtSupportedVersions).u16(2).u16(kTls13);
    sh.u16(kExtKeyShare).u16(2).u16(group.id);
    sh.end();
    byte[] hrr = buildMessage(kServerHello, sh.toByteArray());
    transcript.bytes(hrr);
    sendHandshake(hrr);
    sendCompatChangeCipherSpec();
    state = kStateClientHello2;
  }

  private void sendServerFlight13() throws SSLException, GeneralSecurityException {
    ByteWriter ee = new ByteWriter();
    ee.begin(2);
    if (negotiatedApplicationProtocol != null) {
      ByteWriter ap = new ByteWriter();
      ap.begin(2).vec8(negotiatedApplicationProtocol.getBytes(StandardCharsets.UTF_8)).end();
      ee.u16(kExtAlpn).vec16(ap.toByteArray());
    }
    ee.end();
    byte[] eeMsg = buildMessage(kEncryptedExtensions, ee.toByteArray());
    transcript.bytes(eeMsg);
    sendHandshake(eeMsg);
    clientAuthRequested = cfg.needClientAuth || cfg.wantClientAuth;
    if (clientAuthRequested) {
      ByteWriter cr = new ByteWriter();
      cr.vec8(new byte[0]);
      cr.begin(2);
      ByteWriter sa = new ByteWriter();
      sa.begin(2);
      for (SignatureScheme s : TlsParams.schemes(cfg)) {
        sa.u16(s.id);
      }
      sa.end();
      cr.u16(kExtSignatureAlgorithms).vec16(sa.toByteArray());
      X509Certificate[] issuers = context.acceptedIssuers();
      if (issuers.length > 0) {
        ByteWriter ca = new ByteWriter();
        ca.begin(2);
        for (X509Certificate c : issuers) {
          ca.vec16(c.getSubjectX500Principal().getEncoded());
        }
        ca.end();
        cr.u16(kExtCertificateAuthorities).vec16(ca.toByteArray());
      }
      cr.end();
      byte[] crMsg = buildMessage(kCertificateRequest, cr.toByteArray());
      transcript.bytes(crMsg);
      sendHandshake(crMsg);
    }
    ByteWriter cw = new ByteWriter();
    cw.vec8(new byte[0]);
    cw.begin(3);
    try {
      for (X509Certificate c : serverChain) {
        cw.vec24(c.getEncoded());
        cw.u16(0);
      }
    } catch (CertificateException e) {
      throw fatal(kAlertInternalError, "Cannot encode certificate", e);
    }
    cw.end();
    byte[] certMsg = buildMessage(kCertificate, cw.toByteArray());
    transcript.bytes(certMsg);
    sendHandshake(certMsg);
    session.setLocalCertificates(serverChain);
    byte[] content = TlsCrypto.certificateVerifyContent(true, transcriptHash());
    byte[] sig = TlsCrypto.sign(serverScheme, serverKey, content, random);
    byte[] cv = buildMessage(kCertificateVerify, new ByteWriter().u16(serverScheme.id).vec16(sig).toByteArray());
    transcript.bytes(cv);
    sendHandshake(cv);
    byte[] verify = schedule.verifyData(schedule.serverHandshake, transcriptHash());
    byte[] fin = buildMessage(kFinished, verify);
    transcript.bytes(fin);
    sendHandshake(fin);
    schedule.deriveApplication(transcriptHash());
    installWriteCipher(schedule.cipher(schedule.serverApplication, false));
    state = clientAuthRequested ? kStateClientCertificate : kStateClientFinished;
  }

  private void handleClientCertificate13(byte[] body) throws SSLException {
    ByteReader r = new ByteReader(body);
    r.vec8();
    ByteReader list = r.sub24();
    List<byte[]> certs = new ArrayList<byte[]>();
    while (list.hasRemaining()) {
      certs.add(list.vec24());
      list.sub16();
    }
    acceptClientChain(certs);
  }

  private void acceptClientChain(List<byte[]> encoded) throws SSLException {
    if (encoded.isEmpty()) {
      if (cfg.needClientAuth) {
        throw fatal(version == kTls13 ? 116 : kAlertHandshakeFailure,
            "Empty client certificate chain");
      }
      clientChain = null;
      return;
    }
    try {
      clientChain = TlsCrypto.parseChain(encoded);
    } catch (CertificateException e) {
      throw fatal(kAlertBadCertificate, "Failed to parse client certificates", e);
    }
    String authType = clientChain[0].getPublicKey().getAlgorithm();
    if (authType.equals("RSASSA-PSS")) {
      authType = "RSA";
    }
    try {
      context.checkClientTrusted(clientChain, authType, owner);
    } catch (CertificateException e) {
      throw fatal(kAlertCertificateUnknown, e.getMessage(), e);
    }
    session.setPeerCertificates(clientChain);
  }

  private void handleClientCertificateVerify13(byte[] body) throws SSLException, GeneralSecurityException {
    ByteReader r = new ByteReader(body);
    int schemeId = r.u16();
    byte[] signature = r.vec16();
    SignatureScheme scheme = TlsCrypto.findScheme(schemeId, TlsParams.schemes(cfg));
    PublicKey pub = clientChain[0].getPublicKey();
    if (scheme == null || !scheme.usableWith(pub.getAlgorithm(), SignatureScheme.PublicKeyInfo.of(pub), true)) {
      throw fatal(kAlertIllegalParameter, "Unsupported signature scheme: 0x" + Integer.toHexString(schemeId));
    }
    byte[] content = TlsCrypto.certificateVerifyContent(false, transcriptHash());
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

  private void handleClientFinished13(byte[] body, byte[] message) throws SSLException {
    byte[] expected = schedule.verifyData(schedule.clientHandshake, transcriptHash());
    if (!MessageDigest.isEqual(expected, body)) {
      throw fatal(kAlertDecryptError, "Invalid Finished message");
    }
    transcript.bytes(message);
    installReadCipher(schedule.cipher(schedule.clientApplication, true));
    state = kStateComplete;
    finishHandshake();
  }

  private void handleClientHello12() throws SSLException, GeneralSecurityException {
    List<CipherSuite> ours = TlsParams.suites(cfg, false, true);
    List<SignatureScheme> candidates = usableSchemes();
    CipherSuite chosen = null;
    List<CipherSuite> order = new ArrayList<CipherSuite>();
    if (cfg.useCipherSuitesOrder) {
      order.addAll(ours);
    } else {
      for (int id : clientSuiteIds) {
        CipherSuite s = CipherSuite.byId(id);
        if (s != null && ours.contains(s)) {
          order.add(s);
        }
      }
    }
    for (CipherSuite s : order) {
      if (!clientSuiteIds.contains(s.id)) {
        continue;
      }
      List<SignatureScheme> forKey = new ArrayList<SignatureScheme>();
      for (SignatureScheme sc : candidates) {
        boolean ec = sc.kind == SignatureScheme.kEcdsa;
        if (ec == s.keyExchange.equals("ECDHE_ECDSA")) {
          forKey.add(sc);
        }
      }
      if (chooseCredentials(forKey, false)) {
        chosen = s;
        break;
      }
    }
    if (chosen == null) {
      throw fatal(kAlertHandshakeFailure, "No available authentication scheme");
    }
    suite = chosen;
    hmac12 = suite.hmac;
    NamedGroup group = null;
    List<NamedGroup> ourGroups = TlsParams.groups(cfg);
    for (NamedGroup g : clientGroups) {
      if (ourGroups.contains(g)) {
        group = g;
        break;
      }
    }
    if (group == null) {
      throw fatal(kAlertHandshakeFailure, "No supported named group");
    }
    negotiatedApplicationProtocol = selectAlpn();
    serverRandom = new byte[32];
    random.nextBytes(serverRandom);
    ByteWriter sh = new ByteWriter();
    sh.u16(kTls12).bytes(serverRandom).vec8(new byte[0]).u16(suite.id).u8(0);
    sh.begin(2);
    sh.u16(kExtRenegotiationInfo).u16(1).u8(0);
    if (clientExtendedMasterSecret) {
      sh.u16(kExtExtendedMasterSecret).u16(0);
    }
    sh.u16(kExtEcPointFormats).u16(2).u8(1).u8(0);
    if (negotiatedApplicationProtocol != null) {
      ByteWriter ap = new ByteWriter();
      ap.begin(2).vec8(negotiatedApplicationProtocol.getBytes(StandardCharsets.UTF_8)).end();
      sh.u16(kExtAlpn).vec16(ap.toByteArray());
    }
    sh.end();
    byte[] shMsg = buildMessage(kServerHello, sh.toByteArray());
    transcript.bytes(shMsg);
    sendHandshake(shMsg);
    session.setNegotiated(versionName(version), suite.name);
    ByteWriter cw = new ByteWriter();
    cw.begin(3);
    try {
      for (X509Certificate c : serverChain) {
        cw.vec24(c.getEncoded());
      }
    } catch (CertificateException e) {
      throw fatal(kAlertInternalError, "Cannot encode certificate", e);
    }
    cw.end();
    byte[] certMsg = buildMessage(kCertificate, cw.toByteArray());
    transcript.bytes(certMsg);
    sendHandshake(certMsg);
    session.setLocalCertificates(serverChain);
    ephemeral = group.generate(random);
    ByteWriter params = new ByteWriter();
    params.u8(3).u16(group.id).vec8(ephemeral.publicBytes());
    byte[] paramBytes = params.toByteArray();
    byte[] signed = TlsPrf.concat(clientRandom, serverRandom, paramBytes);
    byte[] sig = TlsCrypto.sign(serverScheme, serverKey, signed, random);
    ByteWriter ske = new ByteWriter();
    ske.bytes(paramBytes).u16(serverScheme.id).vec16(sig);
    byte[] skeMsg = buildMessage(kServerKeyExchange, ske.toByteArray());
    transcript.bytes(skeMsg);
    sendHandshake(skeMsg);
    clientAuthRequested = cfg.needClientAuth || cfg.wantClientAuth;
    if (clientAuthRequested) {
      ByteWriter cr = new ByteWriter();
      cr.vec8(new byte[] {64, 1});
      ByteWriter sa = new ByteWriter();
      for (SignatureScheme s : TlsParams.schemes(cfg)) {
        sa.u16(s.id);
      }
      cr.vec16(sa.toByteArray());
      ByteWriter ca = new ByteWriter();
      for (X509Certificate c : context.acceptedIssuers()) {
        ca.vec16(c.getSubjectX500Principal().getEncoded());
      }
      cr.vec16(ca.toByteArray());
      byte[] crMsg = buildMessage(kCertificateRequest, cr.toByteArray());
      transcript.bytes(crMsg);
      sendHandshake(crMsg);
    }
    byte[] done = buildMessage(kServerHelloDone, new byte[0]);
    transcript.bytes(done);
    sendHandshake(done);
    state = clientAuthRequested ? kStateTls12ClientCertificate : kStateTls12ClientKeyExchange;
  }

  private void handleClientCertificate12(byte[] body) throws SSLException {
    ByteReader r = new ByteReader(body);
    ByteReader list = r.sub24();
    List<byte[]> certs = new ArrayList<byte[]>();
    while (list.hasRemaining()) {
      certs.add(list.vec24());
    }
    acceptClientChain(certs);
  }

  private void handleClientKeyExchange(byte[] body, byte[] message) throws SSLException, GeneralSecurityException {
    byte[] point = new ByteReader(body).vec8();
    byte[] shared;
    try {
      shared = ephemeral.deriveSecret(point);
    } catch (GeneralSecurityException e) {
      throw fatal(kAlertIllegalParameter, "Invalid client key exchange value: " + e.getMessage(), e);
    }
    transcript.bytes(message);
    byte[] seed = clientExtendedMasterSecret ? hashOf(transcript.toByteArray())
        : TlsPrf.concat(clientRandom, serverRandom);
    String label = clientExtendedMasterSecret ? "extended master secret" : "master secret";
    masterSecret = TlsPrf.prf(hmac12, shared, label, seed, 48);
    int keyLen = suite.keyLength;
    byte[] block = TlsPrf.prf(hmac12, masterSecret, "key expansion", TlsPrf.concat(serverRandom, clientRandom),
        2 * keyLen + 8);
    byte[] cKey = Arrays.copyOfRange(block, 0, keyLen);
    byte[] sKey = Arrays.copyOfRange(block, keyLen, 2 * keyLen);
    byte[] cIv = Arrays.copyOfRange(block, 2 * keyLen, 2 * keyLen + 4);
    byte[] sIv = Arrays.copyOfRange(block, 2 * keyLen + 4, 2 * keyLen + 8);
    pendingReadCipher = new RecordCipher(false, CipherSuite.kAeadGcm, cKey, cIv, true);
    pendingWriteCipher12 = new RecordCipher(false, CipherSuite.kAeadGcm, sKey, sIv, false);
    state = clientChain != null ? kStateTls12ClientCertificateVerify : kStateTls12ChangeCipherSpec;
  }

  private RecordCipher pendingWriteCipher12;

  private void handleClientCertificateVerify12(byte[] body) throws SSLException, GeneralSecurityException {
    ByteReader r = new ByteReader(body);
    int schemeId = r.u16();
    byte[] signature = r.vec16();
    SignatureScheme scheme = TlsCrypto.findScheme(schemeId, TlsParams.schemes(cfg));
    PublicKey pub = clientChain[0].getPublicKey();
    if (scheme == null || !scheme.usableWith(pub.getAlgorithm(), SignatureScheme.PublicKeyInfo.of(pub), false)) {
      throw fatal(kAlertIllegalParameter, "Unsupported signature scheme: 0x" + Integer.toHexString(schemeId));
    }
    boolean ok;
    try {
      ok = TlsCrypto.verify(scheme, pub, transcript.toByteArray(), signature);
    } catch (GeneralSecurityException e) {
      ok = false;
    }
    if (!ok) {
      throw fatal(kAlertDecryptError, "Invalid CertificateVerify signature");
    }
  }

  private void handleClientFinished12(byte[] body, byte[] message) throws SSLException, GeneralSecurityException {
    byte[] expected = TlsPrf.prf(hmac12, masterSecret, "client finished", transcriptHash(), 12);
    if (!MessageDigest.isEqual(expected, body)) {
      throw fatal(kAlertDecryptError, "Invalid Finished message");
    }
    transcript.bytes(message);
    sendChangeCipherSpec();
    installWriteCipher(pendingWriteCipher12);
    byte[] verify = TlsPrf.prf(hmac12, masterSecret, "server finished", transcriptHash(), 12);
    byte[] fin = buildMessage(kFinished, verify);
    transcript.bytes(fin);
    sendHandshake(fin);
    state = kStateComplete;
    finishHandshake();
  }
}
