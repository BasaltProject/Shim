/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.nio.cs;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.zip.GZIPInputStream;

// Decode graph and encode map unpacked from a gzip and base64 blob.
final class CodeTable {
  static final int kChar = 0;
  static final int kMalformed = 1;
  static final int kUnmappable = 2;
  static final int kChild = 3;
  static final int kSpecial = 4;
  static final int kFullFirst = 1 << 27;

  int[][] nodes;
  int[] nodeNext;
  int[] roots;
  String[] flush;
  int[] specialKind;
  int[] specialConsumed;
  int[] specialNext;
  String[] specialOut;
  private int[] codes;
  private byte[] lengths;
  private final Map<Integer, byte[]> supplementary = new HashMap<Integer, byte[]>();
  private final Set<Integer> canEncodeFlips = new HashSet<Integer>();
  private final Map<Long, byte[]> composites = new HashMap<Long, byte[]>();
  private final Set<Integer> composingBases = new HashSet<Integer>();

  private byte[] data;
  private int at;

  private CodeTable() { }

  static byte[] hexBytes(String hex) {
    byte[] b = new byte[hex.length() / 2];
    for (int i = 0; i < b.length; i++) {
      b[i] = (byte) Integer.parseInt(hex.substring(2 * i, 2 * i + 2), 16);
    }
    return b;
  }

  static byte[] unpack(String[] blob) {
    StringBuilder encoded = new StringBuilder();
    for (String part : blob) {
      encoded.append(part);
    }
    try {
      InputStream gz = new GZIPInputStream(new ByteArrayInputStream(Base64.getDecoder().decode(encoded.toString())));
      ByteArrayOutputStream bytes = new ByteArrayOutputStream(1 << 16);
      byte[] buffer = new byte[16384];
      int n;
      while ((n = gz.read(buffer)) > 0) {
        bytes.write(buffer, 0, n);
      }
      return bytes.toByteArray();
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
  }

  private int u8() {
    return data[at++] & 0xff;
  }

  private int var() {
    int v = 0;
    int shift = 0;
    while (true) {
      int b = u8();
      v |= (b & 0x7f) << shift;
      if (b < 0x80) {
        return v;
      }
      shift += 7;
    }
  }

  private String codePoints() {
    int n = var();
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < n; i++) {
      sb.appendCodePoint(var());
    }
    return sb.toString();
  }

  private static final Map<String[], CodeTable> kLoaded = new java.util.IdentityHashMap<String[], CodeTable>();

  static synchronized CodeTable named(String name) {
    for (int i = 0; i < ExtendedTables.kEntries.length; i++) {
      if (ExtendedTables.kEntries[i].startsWith(name + "|")) {
        return load(ExtendedTables.kBlobs[i]);
      }
    }
    throw new IllegalArgumentException(name);
  }

  static synchronized CodeTable load(String[] blob) {
    CodeTable cached = kLoaded.get(blob);
    if (cached != null) {
      return cached;
    }
    CodeTable t = new CodeTable();
    t.data = unpack(blob);
    t.parse();
    t.data = null;
    kLoaded.put(blob, t);
    return t;
  }

  private void parse() {
    CodeTable base = null;
    if (u8() == 2) {
      StringBuilder name = new StringBuilder();
      for (int n = var(); n > 0; n--) {
        name.append((char) u8());
      }
      base = named(name.toString());
    }
    int states = var();
    roots = new int[states];
    flush = new String[states];
    for (int s = 0; s < states; s++) {
      roots[s] = var();
      int n = var();
      StringBuilder sb = new StringBuilder();
      for (int i = 0; i < n; i++) {
        sb.append((char) var());
      }
      flush[s] = sb.toString();
    }
    int specials = var();
    specialKind = new int[specials];
    specialConsumed = new int[specials];
    specialNext = new int[specials];
    specialOut = new String[specials];
    for (int i = 0; i < specials; i++) {
      specialKind[i] = var();
      specialConsumed[i] = var();
      specialNext[i] = var();
      specialOut[i] = codePoints();
    }
    nodes = new int[var()][];
    nodeNext = new int[nodes.length];
    int prev = 0;
    for (int n = 0; n < nodes.length; n++) {
      nodeNext[n] = var() - 1;
      if (base != null) {
        int ref = var();
        if (ref > 0) {
          nodes[n] = base.nodes[ref - 1];
          continue;
        }
      }
      int[] e = new int[256];
      for (int b = 0; b < 256; b++) {
        int kind = u8();
        if (kind == 0) {
          e[b] = ++prev;
        } else if (kind == 1) {
          int z = var();
          prev += (z >>> 1) ^ -(z & 1);
          e[b] = prev;
        } else {
          e[b] = (((kind & 0xf) - 1) << 28) | ((kind >> 4) << 27) | var();
        }
      }
      nodes[n] = e;
    }
    codes = new int[0x10000];
    lengths = new byte[0x10000];
    for (int s = 0; s < states; s++) {
      derive(roots[s], 0, 0);
    }
    int diffs = var();
    int cp = -1;
    for (int i = 0; i < diffs; i++) {
      cp += var();
      int len = u8();
      byte[] seq = new byte[len];
      for (int k = 0; k < len; k++) {
        seq[k] = (byte) u8();
      }
      if (cp > 0xffff) {
        if (len == 0) {
          supplementary.remove(cp);
        } else {
          supplementary.put(cp, seq);
        }
      } else {
        int code = 0;
        for (byte b : seq) {
          code = (code << 8) | (b & 0xff);
        }
        codes[cp] = code;
        lengths[cp] = (byte) len;
      }
    }
    int flips = var();
    cp = -1;
    for (int i = 0; i < flips; i++) {
      cp += var();
      canEncodeFlips.add(cp);
    }
  }

  private void derive(int node, int path, int depth) {
    int[] e = nodes[node];
    for (int b = 0; b < 256; b++) {
      int kind = e[b] >>> 28;
      int payload = e[b] & 0x07ffffff;
      int code = (path << 8) | b;
      if (kind == kChar) {
        if (payload > 0xffff) {
          if (!supplementary.containsKey(payload)) {
            byte[] seq = new byte[depth + 1];
            for (int k = depth; k >= 0; k--) {
              seq[k] = (byte) (code >>> (8 * (depth - k)));
            }
            supplementary.put(payload, seq);
          }
        } else if (lengths[payload] == 0) {
          codes[payload] = code;
          lengths[payload] = (byte) (depth + 1);
        }
      } else if (kind == kChild) {
        derive(payload, code, depth + 1);
      } else if (kind == kSpecial && depth == 1 && specialKind[payload] == kChar && specialConsumed[payload] == 2) {
        String out = specialOut[payload];
        if (out.length() == 2 && specialNext[payload] == 0) {
          long key = ((long) out.charAt(0) << 32) | out.charAt(1);
          if (!composites.containsKey(key)) {
            composites.put(key, new byte[] {(byte) (code >>> 8), (byte) code});
            composingBases.add((int) out.charAt(0));
          }
        }
      }
    }
  }

  int length(int cp) {
    if (cp > 0xffff) {
      byte[] seq = supplementary.get(cp);
      return seq == null ? 0 : seq.length;
    }
    return lengths[cp];
  }

  void put(int cp, ByteBuffer dst) {
    if (cp > 0xffff) {
      dst.put(supplementary.get(cp));
      return;
    }
    int len = lengths[cp];
    int code = codes[cp];
    for (int k = len - 1; k >= 0; k--) {
      dst.put((byte) (code >>> (8 * k)));
    }
  }

  boolean canEncode(char c) {
    boolean mapped = !Character.isSurrogate(c) && lengths[c] != 0;
    return mapped != canEncodeFlips.contains((int) c);
  }

  byte[] composite(int base, int next) {
    return composites.get(((long) base << 32) | next);
  }

  boolean isComposingBase(int cp) {
    return composingBases.contains(cp);
  }
}
