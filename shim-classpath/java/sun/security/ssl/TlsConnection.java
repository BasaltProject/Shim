/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.security.ssl;

import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.ArrayDeque;
import java.util.Arrays;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLHandshakeException;

abstract class TlsConnection {
  static final int kTls13 = 0x0304;
  static final int kTls12 = 0x0303;

  static final int kClientHello = 1;
  static final int kServerHello = 2;
  static final int kNewSessionTicket = 4;
  static final int kEncryptedExtensions = 8;
  static final int kCertificate = 11;
  static final int kServerKeyExchange = 12;
  static final int kCertificateRequest = 13;
  static final int kServerHelloDone = 14;
  static final int kCertificateVerify = 15;
  static final int kClientKeyExchange = 16;
  static final int kFinished = 20;
  static final int kKeyUpdate = 24;
  static final int kMessageHash = 254;

  static final int kAlertCloseNotify = 0;
  static final int kAlertUnexpectedMessage = 10;
  static final int kAlertBadRecordMac = 20;
  static final int kAlertRecordOverflow = 22;
  static final int kAlertHandshakeFailure = 40;
  static final int kAlertBadCertificate = 42;
  static final int kAlertUnsupportedCertificate = 43;
  static final int kAlertCertificateExpired = 45;
  static final int kAlertCertificateUnknown = 46;
  static final int kAlertIllegalParameter = 47;
  static final int kAlertUnknownCa = 48;
  static final int kAlertDecodeError = 50;
  static final int kAlertDecryptError = 51;
  static final int kAlertProtocolVersion = 70;
  static final int kAlertInsufficientSecurity = 71;
  static final int kAlertInternalError = 80;
  static final int kAlertMissingExtension = 109;
  static final int kAlertUnsupportedExtension = 110;
  static final int kAlertNoApplicationProtocol = 120;

  static final int kMaxFragment = 16384;

  final SSLContextImpl context;
  final SslConfig cfg;
  final String peerHost;
  final int peerPort;
  final Object owner;
  final SecureRandom random = new SecureRandom();

  private RecordCipher readCipher;
  private RecordCipher writeCipher;
  private final ArrayDeque<byte[]> outRecords = new ArrayDeque<byte[]>();
  private final ArrayDeque<byte[]> appChunks = new ArrayDeque<byte[]>();
  private int appChunkPos;
  private byte[] hsPending = new byte[0];

  final ByteWriter transcript = new ByteWriter();
  int version;
  CipherSuite suite;
  SSLSessionImpl session;
  boolean handshakeStarted;
  boolean handshakeDone;
  boolean handshakeJustFinished;
  boolean inboundDone;
  boolean outboundDone;
  SSLException failure;
  String negotiatedApplicationProtocol;
  private boolean sentCompatCcs;

  TlsConnection(SSLContextImpl context, SslConfig cfg, String peerHost, int peerPort, Object owner) {
    this.context = context;
    this.cfg = cfg;
    this.peerHost = peerHost;
    this.peerPort = peerPort;
    this.owner = owner;
  }

  abstract void startHandshakeImpl() throws SSLException;

  abstract void handleHandshake(int type, byte[] body, byte[] message) throws SSLException;

  synchronized void begin() throws SSLException {
    if (handshakeStarted) {
      return;
    }
    handshakeStarted = true;
    try {
      startHandshakeImpl();
    } catch (SSLException e) {
      throw abort(e);
    }
  }

  synchronized boolean hasOutput() {
    return !outRecords.isEmpty();
  }

  synchronized byte[] peekOutput() {
    return outRecords.peek();
  }

  synchronized byte[] pollOutput() {
    return outRecords.poll();
  }

  synchronized int appAvailable() {
    int n = 0;
    for (byte[] c : appChunks) {
      n += c.length;
    }
    return n - appChunkPos;
  }

  synchronized int readApp(byte[] dst, int off, int len) {
    int total = 0;
    while (total < len && !appChunks.isEmpty()) {
      byte[] c = appChunks.peek();
      int n = Math.min(len - total, c.length - appChunkPos);
      System.arraycopy(c, appChunkPos, dst, off + total, n);
      total += n;
      appChunkPos += n;
      if (appChunkPos == c.length) {
        appChunks.poll();
        appChunkPos = 0;
      }
    }
    return total;
  }

  synchronized byte[] encryptApp(byte[] src, int off, int len) throws SSLException {
    if (failure != null) {
      throw failure;
    }
    if (outboundDone) {
      throw new SSLException("Cannot write application data after close_notify");
    }
    try {
      return writeCipher.encrypt(RecordCipher.kContentApplicationData, src, off, len);
    } catch (GeneralSecurityException e) {
      throw abort(new SSLException("Cannot encrypt record", e));
    }
  }

  synchronized void closeOutbound() {
    if (outboundDone) {
      return;
    }
    outboundDone = true;
    if (handshakeStarted && failure == null && (writeCipher != null || !handshakeDone)) {
      try {
        sendAlertRecord(1, kAlertCloseNotify);
      } catch (SSLException e) {
        failure = e;
      }
    }
  }

  synchronized void markInboundClosed() {
    inboundDone = true;
  }

  static String alertName(int desc) {
    switch (desc) {
    case 0:
      return "close_notify";
    case 10:
      return "unexpected_message";
    case 20:
      return "bad_record_mac";
    case 22:
      return "record_overflow";
    case 40:
      return "handshake_failure";
    case 42:
      return "bad_certificate";
    case 43:
      return "unsupported_certificate";
    case 44:
      return "certificate_revoked";
    case 45:
      return "certificate_expired";
    case 46:
      return "certificate_unknown";
    case 47:
      return "illegal_parameter";
    case 48:
      return "unknown_ca";
    case 49:
      return "access_denied";
    case 50:
      return "decode_error";
    case 51:
      return "decrypt_error";
    case 70:
      return "protocol_version";
    case 71:
      return "insufficient_security";
    case 80:
      return "internal_error";
    case 86:
      return "inappropriate_fallback";
    case 90:
      return "user_canceled";
    case 109:
      return "missing_extension";
    case 110:
      return "unsupported_extension";
    case 112:
      return "unrecognized_name";
    case 116:
      return "certificate_required";
    case 120:
      return "no_application_protocol";
    default:
      return "unknown_" + desc;
    }
  }

  SSLException fatal(int alert, String message) {
    return fatal(alert, message, null);
  }

  static boolean handshakeOnly(int alert) {
    switch (alert) {
    case 40:
    case 41:
    case 42:
    case 43:
    case 44:
    case 45:
    case 46:
    case 47:
    case 48:
    case 49:
    case 50:
    case 51:
    case 60:
    case 70:
    case 71:
    case 100:
    case 109:
    case 110:
    case 111:
    case 112:
    case 113:
    case 114:
    case 115:
    case 116:
    case 120:
      return true;
    default:
      return false;
    }
  }

  static SSLException newException(int alert, String reason, Throwable cause) {
    String msg = "(" + alertName(alert) + ") " + reason;
    SSLException e = handshakeOnly(alert) ? new SSLHandshakeException(msg) : new SSLException(msg);
    if (cause != null) {
      e.initCause(cause);
    }
    return e;
  }

  SSLException fatal(int alert, String message, Throwable cause) {
    SSLException e = newException(alert, message, cause);
    try {
      sendAlertRecord(2, alert);
    } catch (SSLException ignored) {
      // the original failure takes precedence
    }
    return abort(e);
  }

  SSLException abort(SSLException e) {
    if (failure == null) {
      failure = e;
    }
    outboundDone = true;
    inboundDone = true;
    return failure;
  }

  private void sendAlertRecord(int level, int desc) throws SSLException {
    byte[] alert = {(byte) level, (byte) desc};
    queueRecord(RecordCipher.kContentAlert, alert, 0, 2);
  }

  void queueRecord(int type, byte[] data, int off, int len) throws SSLException {
    if (writeCipher == null) {
      ByteWriter w = new ByteWriter();
      int legacy = type == RecordCipher.kContentHandshake && version == 0 ? 0x0301 : 0x0303;
      w.u8(type).u16(legacy).u16(len).bytes(data, off, len);
      outRecords.add(w.toByteArray());
      return;
    }
    try {
      outRecords.add(writeCipher.encrypt(type, data, off, len));
    } catch (GeneralSecurityException e) {
      throw new SSLException("Cannot encrypt record", e);
    }
  }

  void sendHandshake(byte[] message) throws SSLException {
    int off = 0;
    while (off < message.length) {
      int n = Math.min(kMaxFragment, message.length - off);
      queueRecord(RecordCipher.kContentHandshake, message, off, n);
      off += n;
    }
  }

  byte[] buildMessage(int type, byte[] body) {
    ByteWriter w = new ByteWriter();
    w.u8(type).u24(body.length).bytes(body);
    return w.toByteArray();
  }

  void sendCompatChangeCipherSpec() throws SSLException {
    if (sentCompatCcs) {
      return;
    }
    sentCompatCcs = true;
    outRecords.add(new byte[] {20, 3, 3, 0, 1, 1});
  }

  void sendChangeCipherSpec() throws SSLException {
    outRecords.add(new byte[] {20, 3, 3, 0, 1, 1});
  }

  void installReadCipher(RecordCipher c) {
    readCipher = c;
  }

  void installWriteCipher(RecordCipher c) {
    writeCipher = c;
  }

  boolean hasReadCipher() {
    return readCipher != null;
  }

  boolean hasWriteCipher() {
    return writeCipher != null;
  }

  RecordCipher newCipher(byte[] key, byte[] iv, boolean decrypting) {
    return new RecordCipher(version == kTls13, CipherSuite.kAeadGcm, key, iv, decrypting);
  }

  synchronized void processRecord(int type, byte[] fragment) throws SSLException {
    if (failure != null) {
      throw failure;
    }
    try {
      int contentType = type;
      byte[] data = fragment;
      if (fragment.length > kMaxFragment + 2048) {
        throw fatal(kAlertRecordOverflow, "Record overflow");
      }
      boolean compatCcs = version == kTls13 && type == RecordCipher.kContentChangeCipherSpec;
      if (readCipher != null && !compatCcs) {
        try {
          RecordCipher.Plain p = readCipher.decrypt(type, fragment);
          contentType = p.type;
          data = p.data;
        } catch (GeneralSecurityException e) {
          throw fatal(kAlertBadRecordMac, "Bad record MAC", e);
        }
      }
      switch (contentType) {
      case RecordCipher.kContentChangeCipherSpec:
        handleChangeCipherSpec(data);
        break;
      case RecordCipher.kContentAlert:
        handleAlert(data);
        break;
      case RecordCipher.kContentHandshake:
        handleHandshakeBytes(data);
        break;
      case RecordCipher.kContentApplicationData:
        if (!handshakeDone) {
          throw fatal(kAlertUnexpectedMessage, "Unexpected application data before the handshake completed");
        }
        if (data.length > 0) {
          appChunks.add(data);
        }
        break;
      default:
        throw fatal(kAlertUnexpectedMessage, "Unknown record content type: " + contentType);
      }
    } catch (ByteReader.DecodeException e) {
      throw fatal(kAlertDecodeError, "Illegal handshake message");
    }
  }

  void handleChangeCipherSpec(byte[] data) throws SSLException {
    if (version == kTls13) {
      if (data.length != 1 || data[0] != 1) {
        throw fatal(kAlertUnexpectedMessage, "Malformed change_cipher_spec");
      }
      return;
    }
    onChangeCipherSpec();
  }

  abstract void onChangeCipherSpec() throws SSLException;

  private void handleAlert(byte[] data) throws SSLException {
    if (data.length != 2) {
      throw fatal(kAlertDecodeError, "Invalid alert message");
    }
    int level = data[0] & 0xff;
    int desc = data[1] & 0xff;
    if (desc == kAlertCloseNotify) {
      inboundDone = true;
      if (!outboundDone && handshakeDone) {
        closeOutbound();
      }
      return;
    }
    if (level == 2 || version == kTls13 && desc != 90) {
      throw abort(newException(desc, "Received fatal alert: " + alertName(desc), null));
    }
  }

  private void handleHandshakeBytes(byte[] data) throws SSLException {
    hsPending = TlsPrf.concat(hsPending, data);
    while (hsPending.length >= 4) {
      int len = ((hsPending[1] & 0xff) << 16) | ((hsPending[2] & 0xff) << 8) | (hsPending[3] & 0xff);
      if (hsPending.length < 4 + len) {
        break;
      }
      byte[] msg = Arrays.copyOfRange(hsPending, 0, 4 + len);
      hsPending = Arrays.copyOfRange(hsPending, 4 + len, hsPending.length);
      int type = msg[0] & 0xff;
      byte[] body = Arrays.copyOfRange(msg, 4, msg.length);
      handleHandshake(type, body, msg);
    }
  }

  byte[] transcriptHash() throws SSLException {
    try {
      return MessageDigest.getInstance(suite.hash).digest(transcript.toByteArray());
    } catch (GeneralSecurityException e) {
      throw new SSLException(e);
    }
  }

  byte[] hashOf(byte[] data) throws SSLException {
    try {
      return MessageDigest.getInstance(suite.hash).digest(data);
    } catch (GeneralSecurityException e) {
      throw new SSLException(e);
    }
  }

  void finishHandshake() {
    handshakeDone = true;
    handshakeJustFinished = true;
    session.touch();
  }

  static String versionName(int version) {
    return version == kTls13 ? "TLSv1.3" : version == kTls12 ? "TLSv1.2" : "NONE";
  }

  static int enabledVersionCode(String name) {
    switch (name) {
    case "TLSv1.3":
      return kTls13;
    case "TLSv1.2":
      return kTls12;
    default:
      return 0;
    }
  }
}
