/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.net.http;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/** HPACK (RFC 7541) header compression: a decoder with a dynamic table and a literal-only encoder. */
final class Hpack {
  private Hpack() { }

  private static final String[][] kStatic = {
    {":authority", ""}, {":method", "GET"}, {":method", "POST"}, {":path", "/"}, {":path", "/index.html"},
    {":scheme", "http"}, {":scheme", "https"}, {":status", "200"}, {":status", "204"}, {":status", "206"},
    {":status", "304"}, {":status", "400"}, {":status", "404"}, {":status", "500"}, {"accept-charset", ""},
    {"accept-encoding", "gzip, deflate"}, {"accept-language", ""}, {"accept-ranges", ""}, {"accept", ""},
    {"access-control-allow-origin", ""}, {"age", ""}, {"allow", ""}, {"authorization", ""}, {"cache-control", ""},
    {"content-disposition", ""}, {"content-encoding", ""}, {"content-language", ""}, {"content-length", ""},
    {"content-location", ""}, {"content-range", ""}, {"content-type", ""}, {"cookie", ""}, {"date", ""},
    {"etag", ""}, {"expect", ""}, {"expires", ""}, {"from", ""}, {"host", ""}, {"if-match", ""},
    {"if-modified-since", ""}, {"if-none-match", ""}, {"if-range", ""}, {"if-unmodified-since", ""},
    {"last-modified", ""}, {"link", ""}, {"location", ""}, {"max-forwards", ""}, {"proxy-authenticate", ""},
    {"proxy-authorization", ""}, {"range", ""}, {"referer", ""}, {"refresh", ""}, {"retry-after", ""},
    {"server", ""}, {"set-cookie", ""}, {"strict-transport-security", ""}, {"transfer-encoding", ""},
    {"user-agent", ""}, {"vary", ""}, {"via", ""}, {"www-authenticate", ""}
  };

  private static final int[] kLength = new int[257];
  private static final int[] kCode = new int[257];
  private static final int[] kFirstCode = new int[33];
  private static final int[] kCount = new int[33];
  private static final int[] kFirstIndex = new int[33];
  private static final int[] kSorted;

  static {
    String[] groups = {
      "", "", "", "", "", "012aceiost", " %-./3456789=A_bdfghlmnpru", ":BCDEFGHIJKLMNOPQRSTUVWYjkqvwxyz",
      "&*,;XZ", "", "!\"()?", "'+|", "#>", "$@[]~", "^}", "<`{"
    };
    for (int len = 5; len < groups.length; len++) {
      for (int i = 0; i < groups[len].length(); i++) {
        kLength[groups[len].charAt(i)] = len;
      }
    }
    kLength[0] = 13;
    kLength['\\'] = 19;
    List<Integer> symbols = new ArrayList<Integer>();
    for (int len = 1; len <= 32; len++) {
      for (int s = 0; s < 257; s++) {
        if (kLength[s] == len) {
          symbols.add(Integer.valueOf(s));
        }
      }
    }
    kSorted = new int[symbols.size()];
    int code = 0;
    int previous = 0;
    int index = 0;
    for (int len = 1; len <= 32; len++) {
      code <<= 1;
      kFirstCode[len] = code;
      kFirstIndex[len] = index;
      for (int s = 0; s < 257; s++) {
        if (kLength[s] == len) {
          kCode[s] = code++;
          kSorted[index++] = s;
          kCount[len]++;
        }
      }
    }
  }

  static byte[] huffmanDecode(byte[] data, int offset, int length) throws IOException {
    ByteArrayOutputStream out = new ByteArrayOutputStream(length * 2);
    long bits = 0;
    int nbits = 0;
    int end = offset + length;
    int codeLength = 0;
    int code = 0;
    for (int i = offset; i < end; i++) {
      int b = data[i] & 0xff;
      for (int k = 7; k >= 0; k--) {
        code = (code << 1) | ((b >> k) & 1);
        codeLength++;
        if (codeLength > 30) {
          throw new IOException("Invalid Huffman code");
        }
        int count = kCount[codeLength];
        if (count > 0 && code - kFirstCode[codeLength] >= 0 && code - kFirstCode[codeLength] < count) {
          out.write(kSorted[kFirstIndex[codeLength] + code - kFirstCode[codeLength]]);
          code = 0;
          codeLength = 0;
        }
      }
    }
    if (codeLength > 7 || (code != (1 << codeLength) - 1)) {
      throw new IOException("Invalid Huffman padding");
    }
    return out.toByteArray();
  }

  static final class Decoder {
    private final LinkedList<String[]> dynamic = new LinkedList<String[]>();
    private int size;
    private int maxSize = 4096;
    private int limit = 4096;

    void setLimit(int limit) {
      this.limit = limit;
    }

    private void evict() {
      while (size > maxSize && !dynamic.isEmpty()) {
        String[] last = dynamic.removeLast();
        size -= 32 + last[0].length() + last[1].length();
      }
    }

    private void add(String[] entry) {
      int entrySize = 32 + entry[0].getBytes(StandardCharsets.ISO_8859_1).length
          + entry[1].getBytes(StandardCharsets.ISO_8859_1).length;
      if (entrySize > maxSize) {
        dynamic.clear();
        size = 0;
        return;
      }
      dynamic.addFirst(entry);
      size += entrySize;
      evict();
    }

    private String[] lookup(int index) throws IOException {
      if (index <= 0) {
        throw new IOException("Invalid HPACK index " + index);
      }
      if (index <= kStatic.length) {
        return kStatic[index - 1];
      }
      int d = index - kStatic.length - 1;
      if (d >= dynamic.size()) {
        throw new IOException("Invalid HPACK index " + index);
      }
      return dynamic.get(d);
    }

    private int pos;

    private int integer(byte[] b, int prefix) throws IOException {
      int mask = (1 << prefix) - 1;
      int value = b[pos++] & mask;
      if (value < mask) {
        return value;
      }
      int shift = 0;
      while (true) {
        if (pos >= b.length || shift > 28) {
          throw new IOException("Invalid HPACK integer");
        }
        int x = b[pos++] & 0xff;
        value += (x & 0x7f) << shift;
        shift += 7;
        if ((x & 0x80) == 0) {
          return value;
        }
      }
    }

    private String string(byte[] b) throws IOException {
      boolean huffman = (b[pos] & 0x80) != 0;
      int length = integer(b, 7);
      if (pos + length > b.length) {
        throw new IOException("Truncated HPACK string");
      }
      byte[] raw = huffman ? huffmanDecode(b, pos, length) : null;
      String s = huffman ? new String(raw, StandardCharsets.ISO_8859_1)
          : new String(b, pos, length, StandardCharsets.ISO_8859_1);
      pos += length;
      return s;
    }

    List<String[]> decode(byte[] block) throws IOException {
      List<String[]> out = new ArrayList<String[]>();
      pos = 0;
      while (pos < block.length) {
        int b = block[pos] & 0xff;
        if ((b & 0x80) != 0) {
          out.add(lookup(integer(block, 7)));
        } else if ((b & 0xc0) == 0x40) {
          int index = integer(block, 6);
          String name = index == 0 ? string(block) : lookup(index)[0];
          String[] entry = {name, string(block)};
          add(entry);
          out.add(entry);
        } else if ((b & 0xe0) == 0x20) {
          int newSize = integer(block, 5);
          if (newSize > limit) {
            throw new IOException("HPACK table size update exceeds the limit");
          }
          maxSize = newSize;
          evict();
        } else {
          int index = integer(block, 4);
          String name = index == 0 ? string(block) : lookup(index)[0];
          out.add(new String[] {name, string(block)});
        }
      }
      return out;
    }
  }

  private static void integer(ByteArrayOutputStream out, int prefix, int flags, int value) {
    int mask = (1 << prefix) - 1;
    if (value < mask) {
      out.write(flags | value);
      return;
    }
    out.write(flags | mask);
    value -= mask;
    while (value >= 128) {
      out.write((value & 0x7f) | 0x80);
      value >>>= 7;
    }
    out.write(value);
  }

  private static void string(ByteArrayOutputStream out, String s) {
    byte[] b = s.getBytes(StandardCharsets.UTF_8);
    integer(out, 7, 0, b.length);
    out.write(b, 0, b.length);
  }

  static byte[] encode(List<String[]> headers) {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    for (String[] h : headers) {
      int full = 0;
      int nameIndex = 0;
      for (int i = 0; i < kStatic.length; i++) {
        if (kStatic[i][0].equals(h[0])) {
          if (nameIndex == 0) {
            nameIndex = i + 1;
          }
          if (kStatic[i][1].equals(h[1]) && !h[1].isEmpty()) {
            full = i + 1;
            break;
          }
        }
      }
      if (full != 0) {
        integer(out, 7, 0x80, full);
      } else {
        boolean sensitive = h[0].equals("authorization") || h[0].equals("cookie") || h[0].equals("set-cookie");
        integer(out, 4, sensitive ? 0x10 : 0, nameIndex);
        if (nameIndex == 0) {
          string(out, h[0]);
        }
        string(out, h[1]);
      }
    }
    return out.toByteArray();
  }
}
