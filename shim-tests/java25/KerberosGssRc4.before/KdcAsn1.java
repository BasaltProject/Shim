/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public final class KdcAsn1 {
  public static final int kInteger = 0x02;
  public static final int kBitString = 0x03;
  public static final int kOctetString = 0x04;
  public static final int kOid = 0x06;
  public static final int kGeneralString = 0x1b;
  public static final int kGeneralizedTime = 0x18;
  public static final int kSequence = 0x30;

  private KdcAsn1() {
  }

  public static byte[] tlv(int tag, byte[] content) {
    ByteArrayOutputStream out = new ByteArrayOutputStream(content.length + 6);
    out.write(tag);
    int n = content.length;
    if (n < 128) {
      out.write(n);
    } else if (n < 256) {
      out.write(0x81);
      out.write(n);
    } else if (n < 65536) {
      out.write(0x82);
      out.write(n >> 8);
      out.write(n);
    } else {
      out.write(0x83);
      out.write(n >> 16);
      out.write(n >> 8);
      out.write(n);
    }
    out.write(content, 0, content.length);
    return out.toByteArray();
  }

  public static byte[] cat(byte[]... parts) {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    for (byte[] p : parts) {
      if (p != null) {
        out.write(p, 0, p.length);
      }
    }
    return out.toByteArray();
  }

  public static byte[] seq(byte[]... parts) {
    return tlv(kSequence, cat(parts));
  }

  public static byte[] ctx(int n, byte[] inner) {
    return inner == null ? null : tlv(0xa0 | n, inner);
  }

  public static byte[] app(int n, byte[] inner) {
    return tlv(0x60 | n, inner);
  }

  public static byte[] integer(long v) {
    return tlv(kInteger, BigInteger.valueOf(v).toByteArray());
  }

  public static byte[] octets(byte[] b) {
    return tlv(kOctetString, b);
  }

  public static byte[] general(String s) {
    return tlv(kGeneralString, s.getBytes(StandardCharsets.UTF_8));
  }

  public static byte[] bits32(int flags) {
    return tlv(kBitString, new byte[] {0, (byte) (flags >>> 24), (byte) (flags >>> 16), (byte) (flags >>> 8),
        (byte) flags});
  }

  public static byte[] time(long millis) {
    SimpleDateFormat f = new SimpleDateFormat("yyyyMMddHHmmss'Z'");
    f.setTimeZone(TimeZone.getTimeZone("UTC"));
    return tlv(kGeneralizedTime, f.format(new Date(millis)).getBytes(StandardCharsets.US_ASCII));
  }

  public static byte[] oid(String dotted) {
    String[] p = dotted.split("\\.");
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    out.write(Integer.parseInt(p[0]) * 40 + Integer.parseInt(p[1]));
    for (int i = 2; i < p.length; i++) {
      long v = Long.parseLong(p[i]);
      int shift = 0;
      while ((v >> (shift + 7)) != 0) {
        shift += 7;
      }
      for (; shift > 0; shift -= 7) {
        out.write((int) ((v >> shift) & 0x7f) | 0x80);
      }
      out.write((int) (v & 0x7f));
    }
    return tlv(kOid, out.toByteArray());
  }

  public static final class Node {
    public final int tag;
    public final byte[] buf;
    public final int off;
    public final int len;
    public final int total;

    Node(int tag, byte[] buf, int off, int len, int total) {
      this.tag = tag;
      this.buf = buf;
      this.off = off;
      this.len = len;
      this.total = total;
    }

    public byte[] content() {
      byte[] c = new byte[len];
      System.arraycopy(buf, off, c, 0, len);
      return c;
    }

    public byte[] encoded() {
      int headerLen = total - len;
      byte[] c = new byte[total];
      System.arraycopy(buf, off - headerLen, c, 0, total);
      return c;
    }

    public List<Node> children() throws IOException {
      List<Node> out = new ArrayList<Node>();
      int pos = off;
      int end = off + len;
      while (pos < end) {
        Node n = read(buf, pos, end);
        out.add(n);
        pos += n.total;
      }
      return out;
    }

    public Node inner() throws IOException {
      return read(buf, off, off + len);
    }

    public Node field(int n) throws IOException {
      for (Node c : children()) {
        if (c.tag == (0xa0 | n)) {
          return c.inner();
        }
      }
      return null;
    }

    public Node need(int n) throws IOException {
      Node f = field(n);
      if (f == null) {
        throw new IOException("Identifier doesn't match expected value (906)");
      }
      return f;
    }

    public long asLong() throws IOException {
      return new BigInteger(content()).longValue();
    }

    public int asInt() throws IOException {
      return (int) asLong();
    }

    public String asString() {
      return new String(buf, off, len, StandardCharsets.UTF_8);
    }

    public long asTime() throws IOException {
      String s = new String(buf, off, len, StandardCharsets.US_ASCII);
      try {
        SimpleDateFormat f = new SimpleDateFormat("yyyyMMddHHmmss'Z'");
        f.setTimeZone(TimeZone.getTimeZone("UTC"));
        return f.parse(s).getTime();
      } catch (java.text.ParseException e) {
        throw new IOException("bad time " + s);
      }
    }

    public int asBits32() {
      int v = 0;
      for (int i = 1; i < len && i <= 4; i++) {
        v |= (buf[off + i] & 0xff) << (32 - 8 * i);
      }
      return v;
    }
  }

  public static Node read(byte[] b, int off, int end) throws IOException {
    if (off + 2 > end) {
      throw new IOException("truncated DER");
    }
    int tag = b[off] & 0xff;
    int pos = off + 1;
    int len = b[pos++] & 0xff;
    if (len >= 0x80) {
      int n = len & 0x7f;
      if (n == 0 || n > 4 || pos + n > end) {
        throw new IOException("bad DER length");
      }
      len = 0;
      for (int i = 0; i < n; i++) {
        len = (len << 8) | (b[pos++] & 0xff);
      }
    }
    if (len < 0 || pos + len > end) {
      throw new IOException("truncated DER");
    }
    return new Node(tag, b, pos, len, pos + len - off);
  }

  public static Node parse(byte[] b) throws IOException {
    return read(b, 0, b.length);
  }
}
