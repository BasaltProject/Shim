/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.net.http;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

/** Cleartext HTTP/2 (h2c upgrade) exchange for a single request. */
final class Http2Exchange {
  static final class Result {
    int status;
    Map<String, List<String>> headers = new LinkedHashMap<String, List<String>>();
    byte[] body;
    javax.net.ssl.SSLSession session;
  }

  private static final byte[] kPreface = "PRI * HTTP/2.0\r\n\r\nSM\r\n\r\n".getBytes(StandardCharsets.ISO_8859_1);
  private static final int kData = 0, kHeaders = 1, kRstStream = 3, kSettings = 4, kPing = 6, kGoaway = 7,
      kWindowUpdate = 8, kContinuation = 9;
  private static final int kEndStream = 1, kAck = 1, kEndHeaders = 4, kPadded = 8, kPriority = 0x20;

  private final DataInputStream in;
  private final OutputStream out;
  private final Hpack.Decoder decoder = new Hpack.Decoder();
  private long connectionWindow = 65535;
  private long streamWindow = 65535;
  private int initialWindow = 65535;
  private int maxFrame = 16384;

  private Http2Exchange(InputStream in, OutputStream out) {
    this.in = new DataInputStream(in);
    this.out = out;
  }

  private void frame(int type, int flags, int stream, byte[] payload, int off, int len) throws IOException {
    byte[] h = {(byte) (len >> 16), (byte) (len >> 8), (byte) len, (byte) type, (byte) flags,
        (byte) (stream >> 24), (byte) (stream >> 16), (byte) (stream >> 8), (byte) stream};
    out.write(h);
    out.write(payload, off, len);
    out.flush();
  }

  private static byte[] ints(int... values) {
    byte[] b = new byte[values.length * 4];
    for (int i = 0; i < values.length; i++) {
      b[i * 4] = (byte) (values[i] >> 24);
      b[i * 4 + 1] = (byte) (values[i] >> 16);
      b[i * 4 + 2] = (byte) (values[i] >> 8);
      b[i * 4 + 3] = (byte) values[i];
    }
    return b;
  }

  private int type, flags, stream;
  private byte[] payload;

  private void readFrame() throws IOException {
    byte[] h = new byte[9];
    in.readFully(h);
    int len = ((h[0] & 0xff) << 16) | ((h[1] & 0xff) << 8) | (h[2] & 0xff);
    type = h[3] & 0xff;
    flags = h[4] & 0xff;
    stream = ((h[5] & 0x7f) << 24) | ((h[6] & 0xff) << 16) | ((h[7] & 0xff) << 8) | (h[8] & 0xff);
    payload = new byte[len];
    in.readFully(payload);
  }

  private static int word(byte[] b, int o) {
    return ((b[o] & 0xff) << 24) | ((b[o + 1] & 0xff) << 16) | ((b[o + 2] & 0xff) << 8) | (b[o + 3] & 0xff);
  }

  private boolean connectionFrame() throws IOException {
    if (type == kSettings) {
      if ((flags & kAck) == 0) {
        for (int i = 0; i + 6 <= payload.length; i += 6) {
          int id = ((payload[i] & 0xff) << 8) | (payload[i + 1] & 0xff);
          int value = word(payload, i + 2);
          if (id == 4) {
            streamWindow += value - initialWindow;
            initialWindow = value;
          } else if (id == 5) {
            maxFrame = value;
          }
        }
        frame(kSettings, kAck, 0, new byte[0], 0, 0);
      }
      return true;
    }
    if (type == kPing) {
      if ((flags & kAck) == 0) {
        frame(kPing, kAck, 0, payload, 0, payload.length);
      }
      return true;
    }
    if (type == kGoaway) {
      throw new IOException("received GOAWAY");
    }
    if (type == kWindowUpdate) {
      long inc = word(payload, 0) & 0x7fffffffL;
      if (stream == 0) {
        connectionWindow += inc;
      } else if (stream == 1) {
        streamWindow += inc;
      }
      return true;
    }
    return false;
  }

  static Result upgraded(InputStream in, OutputStream out) throws IOException {
    Http2Exchange x = new Http2Exchange(in, out);
    out.write(kPreface);
    byte[] settings = {0, 2, 0, 0, 0, 0, 0, 4, 0, (byte) 0xff, (byte) 0xff, (byte) 0xff};
    x.frame(kSettings, 0, 0, settings, 0, settings.length);
    return x.readResponse();
  }

  private Result readResponse() throws IOException {
    Result result = new Result();
    ByteArrayOutputStream collected = new ByteArrayOutputStream();
    ByteArrayOutputStream headerBlock = null;
    boolean headersDone = false;
    boolean ended = false;
    while (!ended) {
      readFrame();
      if (stream == 0 || type == kSettings || type == kPing || type == kGoaway || type == kWindowUpdate) {
        if (connectionFrame()) {
          continue;
        }
      }
      if (stream != 1) {
        continue;
      }
      if (type == kHeaders || type == kContinuation) {
        int off = 0;
        int end = payload.length;
        if (type == kHeaders) {
          if ((flags & kPadded) != 0) {
            end -= payload[0] & 0xff;
            off = 1;
          }
          if ((flags & kPriority) != 0) {
            off += 5;
          }
          headerBlock = new ByteArrayOutputStream();
        }
        headerBlock.write(payload, off, end - off);
        if ((flags & kEndHeaders) != 0) {
          boolean informational = false;
          for (String[] h : decoder.decode(headerBlock.toByteArray())) {
            if (h[0].equals(":status")) {
              int status = Integer.parseInt(h[1]);
              informational = status >= 100 && status < 200;
              if (!informational) {
                result.status = status;
              }
            } else if (!informational) {
              List<String> list = result.headers.get(h[0]);
              if (list == null) {
                list = new ArrayList<String>();
                result.headers.put(h[0], list);
              }
              list.add(h[1]);
            }
          }
          headersDone = !informational;
          headerBlock = null;
        }
        if ((flags & kEndStream) != 0) {
          ended = true;
        }
      } else if (type == kData) {
        int off = 0;
        int end = payload.length;
        if ((flags & kPadded) != 0) {
          end -= payload[0] & 0xff;
          off = 1;
        }
        collected.write(payload, off, end - off);
        if (payload.length > 0) {
          frame(kWindowUpdate, 0, 0, ints(payload.length), 0, 4);
          if ((flags & kEndStream) == 0) {
            frame(kWindowUpdate, 0, 1, ints(payload.length), 0, 4);
          }
        }
        if ((flags & kEndStream) != 0) {
          ended = true;
        }
      } else if (type == kRstStream) {
        throw new IOException("stream reset: " + word(payload, 0));
      }
    }
    if (!headersDone) {
      throw new IOException("no response headers received");
    }
    result.body = collected.toByteArray();
    return result;
  }
}
