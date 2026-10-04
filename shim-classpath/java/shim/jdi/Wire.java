/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.jdi;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public final class Wire {
  public static final class Out {
    private final ByteArrayOutputStream b = new ByteArrayOutputStream();

    public Out u8(int v) {
      b.write(v);
      return this;
    }

    public Out bool(boolean v) {
      return u8(v ? 1 : 0);
    }

    public Out i32(int v) {
      b.write(v >>> 24);
      b.write(v >>> 16);
      b.write(v >>> 8);
      b.write(v);
      return this;
    }

    public Out i64(long v) {
      i32((int) (v >>> 32));
      return i32((int) v);
    }

    public Out id(long v, int size) {
      for (int i = size - 1; i >= 0; i--) {
        b.write((int) (v >>> (8 * i)));
      }
      return this;
    }

    public Out str(String s) {
      byte[] d = s.getBytes(StandardCharsets.UTF_8);
      i32(d.length);
      b.write(d, 0, d.length);
      return this;
    }

    public byte[] bytes() {
      return b.toByteArray();
    }
  }

  public static final class In {
    private final byte[] d;
    private int p;

    public In(byte[] d) {
      this.d = d;
    }

    public int u8() {
      return d[p++] & 0xff;
    }

    public boolean bool() {
      return u8() != 0;
    }

    public int i16() {
      return (u8() << 8) | u8();
    }

    public int i32() {
      return (u8() << 24) | (u8() << 16) | (u8() << 8) | u8();
    }

    public long i64() {
      return ((long) i32() << 32) | (i32() & 0xffffffffL);
    }

    public long id(int size) {
      long v = 0;
      for (int i = 0; i < size; i++) {
        v = (v << 8) | u8();
      }
      return v;
    }

    public String str() {
      int n = i32();
      String s = new String(d, p, n, StandardCharsets.UTF_8);
      p += n;
      return s;
    }

    public boolean more() {
      return p < d.length;
    }
  }

  public static final class Reply {
    public int error;
    public In data;
  }

  public interface EventSink {
    void events(In in);

    void closed();
  }

  private final InputStream rawIn;
  private final DataInputStream in;
  private final OutputStream out;
  private final Map<Integer, Reply[]> pending = new HashMap<Integer, Reply[]>();
  private int nextId = 1;
  private volatile boolean closed;
  private EventSink sink;
  public int fieldIdSize = 8;
  public int methodIdSize = 8;
  public int objectIdSize = 8;
  public int typeIdSize = 8;
  public int frameIdSize = 8;

  public Wire(InputStream in, OutputStream out) throws IOException {
    this.rawIn = in;
    this.in = new DataInputStream(in);
    this.out = out;
    byte[] hs = "JDWP-Handshake".getBytes(StandardCharsets.US_ASCII);
    out.write(hs);
    out.flush();
    byte[] got = new byte[hs.length];
    this.in.readFully(got);
    if (!new String(got, StandardCharsets.US_ASCII).equals("JDWP-Handshake")) {
      throw new IOException("handshake failed - received >" + new String(got, StandardCharsets.US_ASCII)
          + "< - expected >JDWP-Handshake<");
    }
  }

  public void start(EventSink sink) {
    this.sink = sink;
    Thread t = new Thread("JDI Target Reader") {
      public void run() {
        readLoop();
      }
    };
    t.setDaemon(true);
    t.start();
  }

  private void readLoop() {
    try {
      while (true) {
        int len = in.readInt();
        int id = in.readInt();
        int flags = in.readUnsignedByte();
        byte[] data;
        if ((flags & 0x80) != 0) {
          int err = in.readUnsignedShort();
          data = new byte[len - 11];
          in.readFully(data);
          Reply[] slot;
          synchronized (pending) {
            slot = pending.get(id);
          }
          if (slot != null) {
            Reply r = new Reply();
            r.error = err;
            r.data = new In(data);
            synchronized (slot) {
              slot[0] = r;
              slot.notifyAll();
            }
          }
        } else {
          int set = in.readUnsignedByte();
          int cmd = in.readUnsignedByte();
          data = new byte[len - 11];
          in.readFully(data);
          if (set == 64 && cmd == 100 && sink != null) {
            sink.events(new In(data));
          }
        }
      }
    } catch (IOException e) {
      closed = true;
      synchronized (pending) {
        for (Reply[] s : pending.values()) {
          synchronized (s) {
            s.notifyAll();
          }
        }
      }
      if (sink != null) {
        sink.closed();
      }
    }
  }

  public boolean isClosed() {
    return closed;
  }

  public Reply send(int set, int cmd, Out body) throws IOException {
    byte[] data = body == null ? new byte[0] : body.bytes();
    Reply[] slot = new Reply[1];
    int id;
    synchronized (this) {
      if (closed) {
        throw new IOException("connection closed");
      }
      id = nextId++;
      synchronized (pending) {
        pending.put(id, slot);
      }
      Out h = new Out();
      h.i32(11 + data.length).i32(id).u8(0).u8(set).u8(cmd);
      out.write(h.bytes());
      out.write(data);
      out.flush();
    }
    synchronized (slot) {
      while (slot[0] == null) {
        if (closed) {
          throw new IOException("connection closed");
        }
        try {
          slot.wait();
        } catch (InterruptedException e) {
          throw new IOException("interrupted");
        }
      }
    }
    synchronized (pending) {
      pending.remove(id);
    }
    return slot[0];
  }

  public Reply sendOk(int set, int cmd, Out body) throws IOException {
    Reply r = send(set, cmd, body);
    if (r.error != 0) {
      throw new JdwpError(r.error);
    }
    return r;
  }

  public static final class JdwpError extends IOException {
    public final int code;

    public JdwpError(int code) {
      super("JDWP error " + code);
      this.code = code;
    }
  }

  public void close() {
    closed = true;
    try {
      out.close();
      rawIn.close();
    } catch (IOException e) {
    }
  }
}
