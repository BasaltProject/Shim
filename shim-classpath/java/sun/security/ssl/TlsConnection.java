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

  boolean dtls;
  static final int kHelloVerifyRequest = 3;
  private static final int kDtlsVersion = 0xFEFD;
  private int sendMessageSeq;
  private int receiveMessageSeq;
  private int writeEpoch;
  private int readEpoch;
  private final long[] writeSeq = new long[2];
  private final long[] replayMax = {-1, -1};
  private final long[] replayMask = new long[2];
  private final java.util.Map<Integer, DtlsReassembly> reassembly = new java.util.HashMap<Integer, DtlsReassembly>();
  private final java.util.List<Object[]> flight = new java.util.ArrayList<Object[]>();
  private boolean newFlight = true;
  private boolean retransmitWanted;

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
    if (dtls) {
      return dtlsRecord(writeEpoch, RecordCipher.kContentApplicationData, Arrays.copyOfRange(src, off, off + len));
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
    if (dtls) {
      writeRecord(writeEpoch, type, Arrays.copyOfRange(data, off, off + len));
      return;
    }
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
    if (dtls) {
      trackFlight(0, message, writeEpoch);
      emitHandshake(message, writeEpoch);
      return;
    }
    int off = 0;
    while (off < message.length) {
      int n = Math.min(kMaxFragment, message.length - off);
      queueRecord(RecordCipher.kContentHandshake, message, off, n);
      off += n;
    }
  }

  byte[] buildMessage(int type, byte[] body) {
    ByteWriter w = new ByteWriter();
    if (dtls) {
      w.u8(type).u24(body.length).u16(sendMessageSeq++).u24(0).u24(body.length).bytes(body);
    } else {
      w.u8(type).u24(body.length).bytes(body);
    }
    return w.toByteArray();
  }

  String protocolName() {
    return dtls ? "DTLSv1.2" : versionName(version);
  }

  void sendCompatChangeCipherSpec() throws SSLException {
    if (sentCompatCcs) {
      return;
    }
    sentCompatCcs = true;
    outRecords.add(new byte[] {20, 3, 3, 0, 1, 1});
  }

  void sendChangeCipherSpec() throws SSLException {
    if (dtls) {
      trackFlight(1, new byte[] {1}, 0);
      writeRecord(0, RecordCipher.kContentChangeCipherSpec, new byte[] {1});
      return;
    }
    outRecords.add(new byte[] {20, 3, 3, 0, 1, 1});
  }

  void installReadCipher(RecordCipher c) {
    readCipher = c;
    if (dtls) {
      readEpoch = 1;
    }
  }

  void installWriteCipher(RecordCipher c) {
    writeCipher = c;
    if (dtls) {
      writeEpoch = 1;
    }
  }

  boolean hasReadCipher() {
    return readCipher != null;
  }

  boolean hasWriteCipher() {
    return writeCipher != null;
  }

  RecordCipher newCipher(byte[] key, byte[] iv, boolean decrypting) {
    return new RecordCipher(version == kTls13, suite.aead, key, iv, decrypting);
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

  void handleAlert(byte[] data) throws SSLException {
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


  private byte[] dtlsRecord(int epoch, int type, byte[] data) throws SSLException {
    long seq = writeSeq[epoch]++;
    byte[] fragment = data;
    if (epoch > 0) {
      try {
        fragment = writeCipher.encryptDtls(type, data, 0, data.length, ((long) epoch << 48) | seq);
      } catch (GeneralSecurityException e) {
        throw new SSLException("Cannot encrypt record", e);
      }
    }
    ByteWriter w = new ByteWriter();
    w.u8(type).u16(kDtlsVersion).u16(epoch).u16((int) (seq >>> 32)).u32(seq & 0xFFFFFFFFL).u16(fragment.length);
    w.bytes(fragment);
    return w.toByteArray();
  }

  private void writeRecord(int epoch, int type, byte[] data) throws SSLException {
    outRecords.add(dtlsRecord(epoch, type, data));
  }

  private void emitHandshake(byte[] message, int epoch) throws SSLException {
    int bodyLen = message.length - 12;
    int max = kMaxFragment - 12;
    if (cfg.maximumPacketSize > 0) {
      max = Math.max(1, Math.min(max, cfg.maximumPacketSize - 13 - 12 - (epoch > 0 ? 24 : 0)));
    }
    int off = 0;
    do {
      int n = Math.min(max, bodyLen - off);
      ByteWriter w = new ByteWriter();
      w.bytes(message, 0, 6).u24(off).u24(n).bytes(message, 12 + off, n);
      writeRecord(epoch, RecordCipher.kContentHandshake, w.toByteArray());
      off += n;
    } while (off < bodyLen);
  }

  private void trackFlight(int kind, byte[] data, int epoch) {
    if (newFlight) {
      flight.clear();
      newFlight = false;
    }
    flight.add(new Object[] {kind, data, epoch});
  }

  private void retransmitFlight() throws SSLException {
    for (Object[] item : flight) {
      if ((Integer) item[0] == 0) {
        emitHandshake((byte[]) item[1], (Integer) item[2]);
      } else {
        writeRecord(0, RecordCipher.kContentChangeCipherSpec, (byte[]) item[1]);
      }
    }
  }

  synchronized void retransmitNow() throws SSLException {
    if (dtls && !flight.isEmpty() && !handshakeDone && failure == null) {
      retransmitFlight();
    }
  }

  private boolean replayed(int epoch, long seq) {
    if (seq > replayMax[epoch]) {
      return false;
    }
    long diff = replayMax[epoch] - seq;
    return diff >= 64 || (replayMask[epoch] >>> diff & 1) != 0;
  }

  private void markSeen(int epoch, long seq) {
    if (seq > replayMax[epoch]) {
      long shift = replayMax[epoch] < 0 ? 64 : seq - replayMax[epoch];
      replayMask[epoch] = shift >= 64 ? 1 : (replayMask[epoch] << shift) | 1;
      replayMax[epoch] = seq;
    } else {
      replayMask[epoch] |= 1L << (replayMax[epoch] - seq);
    }
  }

  synchronized void processDatagram(byte[] d) throws SSLException {
    if (failure != null) {
      throw failure;
    }
    retransmitWanted = false;
    int off = 0;
    try {
      while (off + 13 <= d.length) {
        int type = d[off] & 0xff;
        int ver = (d[off + 1] & 0xff) << 8 | (d[off + 2] & 0xff);
        int epoch = (d[off + 3] & 0xff) << 8 | (d[off + 4] & 0xff);
        long seq = 0;
        for (int i = 0; i < 6; i++) {
          seq = seq << 8 | (d[off + 5 + i] & 0xff);
        }
        int len = (d[off + 11] & 0xff) << 8 | (d[off + 12] & 0xff);
        if (off + 13 + len > d.length || (ver != 0xFEFD && ver != 0xFEFF)) {
          break;
        }
        byte[] fragment = Arrays.copyOfRange(d, off + 13, off + 13 + len);
        off += 13 + len;
        if (epoch > 1 || type < 20 || type > 23 || replayed(epoch, seq)) {
          continue;
        }
        byte[] data = fragment;
        if (epoch == 1) {
          if (readCipher == null || readEpoch != 1) {
            continue;
          }
          try {
            data = readCipher.decryptDtls(type, fragment, ((long) epoch << 48) | seq);
          } catch (GeneralSecurityException e) {
            continue;
          }
        }
        markSeen(epoch, seq);
        switch (type) {
        case RecordCipher.kContentChangeCipherSpec:
          if (readEpoch == 0) {
            onChangeCipherSpec();
          }
          break;
        case RecordCipher.kContentAlert:
          handleAlert(data);
          break;
        case RecordCipher.kContentHandshake:
          handleDtlsHandshake(data);
          break;
        default:
          if (handshakeDone && data.length > 0) {
            appChunks.add(data);
          }
          break;
        }
        if (inboundDone) {
          break;
        }
      }
      if (retransmitWanted && failure == null) {
        retransmitFlight();
      }
    } catch (ByteReader.DecodeException e) {
      throw fatal(kAlertDecodeError, "Illegal handshake message");
    }
  }

  private void handleDtlsHandshake(byte[] data) throws SSLException {
    int off = 0;
    while (off + 12 <= data.length) {
      int type = data[off] & 0xff;
      int mlen = (data[off + 1] & 0xff) << 16 | (data[off + 2] & 0xff) << 8 | (data[off + 3] & 0xff);
      int mseq = (data[off + 4] & 0xff) << 8 | (data[off + 5] & 0xff);
      int foff = (data[off + 6] & 0xff) << 16 | (data[off + 7] & 0xff) << 8 | (data[off + 8] & 0xff);
      int flen = (data[off + 9] & 0xff) << 16 | (data[off + 10] & 0xff) << 8 | (data[off + 11] & 0xff);
      if (off + 12 + flen > data.length || foff + flen > mlen || mlen > (1 << 20)) {
        return;
      }
      byte[] piece = Arrays.copyOfRange(data, off + 12, off + 12 + flen);
      off += 12 + flen;
      if (mseq < receiveMessageSeq) {
        retransmitWanted = true;
        continue;
      }
      if (mseq > receiveMessageSeq + 16) {
        continue;
      }
      DtlsReassembly r = reassembly.get(mseq);
      if (r == null) {
        r = new DtlsReassembly(type, mlen);
        reassembly.put(mseq, r);
      }
      if (r.type != type || r.length != mlen) {
        continue;
      }
      r.add(foff, piece);
      while (true) {
        DtlsReassembly next = reassembly.get(receiveMessageSeq);
        if (next == null || !next.complete()) {
          break;
        }
        reassembly.remove(receiveMessageSeq);
        ByteWriter w = new ByteWriter();
        w.u8(next.type).u24(next.length).u16(receiveMessageSeq).u24(0).u24(next.length).bytes(next.data);
        receiveMessageSeq++;
        newFlight = true;
        handleHandshake(next.type, next.data, w.toByteArray());
      }
    }
  }

  static final class DtlsReassembly {
    final int type;
    final int length;
    final byte[] data;
    private final java.util.BitSet seen = new java.util.BitSet();

    DtlsReassembly(int type, int length) {
      this.type = type;
      this.length = length;
      this.data = new byte[length];
    }

    void add(int offset, byte[] piece) {
      System.arraycopy(piece, 0, data, offset, piece.length);
      seen.set(offset, offset + piece.length);
    }

    boolean complete() {
      return seen.nextClearBit(0) >= length;
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
