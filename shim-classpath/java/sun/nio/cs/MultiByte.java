/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package sun.nio.cs;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;

public final class MultiByte extends Charset implements HistoricallyNamedCharset {
  private static final class Group {
    byte[] prefix;
    int[][] rows = new int[256][];
    int[] minTrail = new int[256];
    int[] maxTrail = new int[256];
  }

  private final String historical;
  private final boolean asciiCompatible;
  private final String[] blob;
  private boolean loaded;
  private boolean dbcsOnly;
  private boolean eucJp;
  private boolean pairUnmappable;
  private int[] single;
  private Group[] groups;
  private int[][] ranges;
  private int[][] rangesByCodePoint;
  private byte[][] encodeBmp;
  private Map<Integer, byte[]> encodeSupplementary;
  private float averageBytes;
  private float maxBytes;
  private float averageChars;
  private float maxChars;
  private byte[] replacement;

  MultiByte(String name, String[] aliases, String historical, boolean registered, boolean asciiCompatible,
      String[] blob) {
    super(name, aliases);
    this.historical = historical.isEmpty() ? name : historical;
    this.asciiCompatible = asciiCompatible;
    this.blob = blob;
  }

  public String historicalName() {
    return historical;
  }

  public boolean contains(Charset cs) {
    return cs == this || (asciiCompatible && cs.name().equals("US-ASCII"));
  }

  private static int readCp(DataInputStream in, int[] supplementary, int[] next) throws IOException {
    int v = in.readUnsignedShort();
    if (v == 0xFFFF) {
      return -1;
    }
    if (v == 0xFFFE) {
      return supplementary[next[0]++];
    }
    return v;
  }

  private synchronized void load() {
    if (loaded) {
      return;
    }
    try {
      StringBuilder encoded = new StringBuilder();
      for (String part : blob) {
        encoded.append(part);
      }
      InputStream gz = new GZIPInputStream(new ByteArrayInputStream(Base64.getDecoder().decode(encoded.toString())));
      ByteArrayOutputStream bytes = new ByteArrayOutputStream(1 << 17);
      byte[] buffer = new byte[16384];
      int n;
      while ((n = gz.read(buffer)) > 0) {
        bytes.write(buffer, 0, n);
      }
      DataInputStream in = new DataInputStream(new ByteArrayInputStream(bytes.toByteArray()));
      int supplementaryCount = in.readInt();
      int[] supplementary = new int[supplementaryCount];
      for (int i = 0; i < supplementaryCount; i++) {
        supplementary[i] = in.readInt();
      }
      int[] next = new int[1];
      single = new int[256];
      for (int i = 0; i < 256; i++) {
        single[i] = readCp(in, supplementary, next);
      }
      int groupCount = in.readUnsignedByte();
      groups = new Group[groupCount];
      for (int g = 0; g < groupCount; g++) {
        Group group = new Group();
        int prefixLength = in.readUnsignedByte();
        group.prefix = new byte[prefixLength];
        in.readFully(group.prefix);
        int leads = in.readUnsignedShort();
        for (int l = 0; l < leads; l++) {
          int lead = in.readUnsignedByte();
          int min = in.readUnsignedByte();
          int max = in.readUnsignedByte();
          int[] row = new int[max - min + 1];
          for (int t = 0; t < row.length; t++) {
            row[t] = readCp(in, supplementary, next);
          }
          group.rows[lead] = row;
          group.minTrail[lead] = min;
          group.maxTrail[lead] = max;
        }
        groups[g] = group;
      }
      int rangeCount = in.readInt();
      ranges = new int[rangeCount][];
      for (int i = 0; i < rangeCount; i++) {
        ranges[i] = new int[] {in.readInt(), in.readInt(), in.readInt()};
      }
      int extraCount = in.readInt();
      int[] extraCps = new int[extraCount];
      byte[][] extraBytes = new byte[extraCount][];
      for (int i = 0; i < extraCount; i++) {
        extraCps[i] = in.readInt();
        extraBytes[i] = new byte[in.readUnsignedByte()];
        in.readFully(extraBytes[i]);
      }
      int unencodableCount = in.readInt();
      int[] unencodable = new int[unencodableCount];
      for (int i = 0; i < unencodableCount; i++) {
        unencodable[i] = in.readInt();
      }
      averageBytes = in.readFloat();
      maxBytes = in.readFloat();
      averageChars = in.readFloat();
      maxChars = in.readFloat();
      replacement = new byte[in.readUnsignedByte()];
      in.readFully(replacement);
      dbcsOnly = true;
      for (int i = 0; i < 256; i++) {
        if (single[i] >= 0) {
          dbcsOnly = false;
        }
      }
      eucJp = name().equals("EUC-JP");
      pairUnmappable = name().equals("Big5-HKSCS");
      buildEncoding(extraCps, extraBytes, unencodable);
      loaded = true;
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
  }

  private void putNatural(int cp, byte[] seq) {
    if (cp > 0xFFFF) {
      if (!encodeSupplementary.containsKey(cp)) {
        encodeSupplementary.put(cp, seq);
      }
    } else if (encodeBmp[cp] == null) {
      encodeBmp[cp] = seq;
    }
  }

  private void buildEncoding(int[] extraCps, byte[][] extraBytes, int[] unencodable) {
    encodeBmp = new byte[65536][];
    encodeSupplementary = new HashMap<Integer, byte[]>();
    for (int i = 0; i < 256; i++) {
      if (single[i] >= 0) {
        putNatural(single[i], new byte[] {(byte) i});
      }
    }
    for (Group group : groups) {
      for (int lead = 0x21; lead <= 0xFF; lead++) {
        int[] row = group.rows[lead];
        if (row == null) {
          continue;
        }
        for (int t = group.minTrail[lead]; t <= group.maxTrail[lead]; t++) {
          int cp = row[t - group.minTrail[lead]];
          if (cp >= 0) {
            byte[] seq = new byte[group.prefix.length + 2];
            System.arraycopy(group.prefix, 0, seq, 0, group.prefix.length);
            seq[group.prefix.length] = (byte) lead;
            seq[group.prefix.length + 1] = (byte) t;
            putNatural(cp, seq);
          }
        }
      }
    }
    for (int i = 0; i < extraCps.length; i++) {
      if (extraCps[i] > 0xFFFF) {
        encodeSupplementary.put(extraCps[i], extraBytes[i]);
      } else {
        encodeBmp[extraCps[i]] = extraBytes[i];
      }
    }
    for (int cp : unencodable) {
      if (cp > 0xFFFF) {
        encodeSupplementary.remove(cp);
      } else {
        encodeBmp[cp] = null;
      }
    }
    if (ranges.length > 0) {
      rangesByCodePoint = ranges.clone();
      Arrays.sort(rangesByCodePoint, new java.util.Comparator<int[]>() {
        public int compare(int[] a, int[] b) {
          return a[1] - b[1];
        }
      });
    }
  }

  private static byte[] fourBytes(int linear) {
    int b4 = linear % 10;
    linear /= 10;
    int b3 = linear % 126;
    linear /= 126;
    int b2 = linear % 10;
    int b1 = linear / 10;
    return new byte[] {(byte) (b1 + 0x81), (byte) (b2 + 0x30), (byte) (b3 + 0x81), (byte) (b4 + 0x30)};
  }

  private byte[] encodeSequence(int cp) {
    byte[] seq = cp > 0xFFFF ? encodeSupplementary.get(cp) : encodeBmp[cp];
    if (seq != null || rangesByCodePoint == null) {
      return seq;
    }
    int low = 0;
    int high = rangesByCodePoint.length - 1;
    while (low <= high) {
      int mid = (low + high) >>> 1;
      int[] r = rangesByCodePoint[mid];
      if (cp < r[1]) {
        high = mid - 1;
      } else if (cp >= r[1] + r[2]) {
        low = mid + 1;
      } else {
        return fourBytes(r[0] + (cp - r[1]));
      }
    }
    return null;
  }

  private int decodeFour(int b1, int b2, int b3, int b4) {
    int linear = (((b1 - 0x81) * 10 + (b2 - 0x30)) * 126 + (b3 - 0x81)) * 10 + (b4 - 0x30);
    int low = 0;
    int high = ranges.length - 1;
    while (low <= high) {
      int mid = (low + high) >>> 1;
      int[] r = ranges[mid];
      if (linear < r[0]) {
        high = mid - 1;
      } else if (linear >= r[0] + r[2]) {
        low = mid + 1;
      } else {
        return r[1] + (linear - r[0]);
      }
    }
    return -1;
  }

  private static boolean put(CharBuffer dst, int cp) {
    if (cp > 0xFFFF) {
      if (dst.remaining() < 2) {
        return false;
      }
      dst.put(Character.highSurrogate(cp));
      dst.put(Character.lowSurrogate(cp));
      return true;
    }
    if (!dst.hasRemaining()) {
      return false;
    }
    dst.put((char) cp);
    return true;
  }

  public CharsetDecoder newDecoder() {
    load();
    return new CharsetDecoder(this, averageChars, maxChars) {
      protected CoderResult decodeLoop(ByteBuffer src, CharBuffer dst) {
        while (src.hasRemaining()) {
          int pos = src.position();
          int b1 = src.get(pos) & 0xff;
          int cp = single[b1];
          if (cp >= 0) {
            if (!put(dst, cp)) {
              return CoderResult.OVERFLOW;
            }
            src.position(pos + 1);
            continue;
          }
          if (dbcsOnly) {
            if (src.remaining() < 2) {
              return CoderResult.UNDERFLOW;
            }
            int b2 = src.get(pos + 1) & 0xff;
            int[] row = groups[0].rows[b1];
            if (row == null || b2 < groups[0].minTrail[b1] || b2 > groups[0].maxTrail[b1]
                || (cp = row[b2 - groups[0].minTrail[b1]]) < 0) {
              return CoderResult.unmappableForLength(2);
            }
            if (!put(dst, cp)) {
              return CoderResult.OVERFLOW;
            }
            src.position(pos + 2);
            continue;
          }
          if (eucJp) {
            boolean three = b1 == 0x8F;
            int need = three ? 3 : 2;
            if (src.remaining() < need) {
              return CoderResult.UNDERFLOW;
            }
            int b2 = src.get(pos + 1) & 0xff;
            Group group = three ? groups[1] : groups[0];
            int lead = three ? b2 : b1;
            int trail = three ? src.get(pos + 2) & 0xff : b2;
            int[] row = group.rows[lead];
            if (row == null || trail < group.minTrail[lead] || trail > group.maxTrail[lead]
                || (cp = row[trail - group.minTrail[lead]]) < 0) {
              return CoderResult.unmappableForLength(need);
            }
            if (!put(dst, cp)) {
              return CoderResult.OVERFLOW;
            }
            src.position(pos + need);
            continue;
          }
          int consumed = 0;
          CoderResult error = null;
          boolean handled = false;
          for (int g = 1; g < groups.length && !handled; g++) {
            Group group = groups[g];
            if (group.prefix.length == 1 && (group.prefix[0] & 0xff) == b1) {
              handled = true;
              if (src.remaining() < 3) {
                return CoderResult.UNDERFLOW;
              }
              int b2 = src.get(pos + 1) & 0xff;
              int b3 = src.get(pos + 2) & 0xff;
              int[] row = group.rows[b2];
              if (row == null || b3 < group.minTrail[b2] || b3 > group.maxTrail[b2]
                  || (cp = row[b3 - group.minTrail[b2]]) < 0) {
                return row == null ? CoderResult.malformedForLength(1) : CoderResult.unmappableForLength(3);
              }
              consumed = 3;
            }
          }
          if (!handled && ranges.length > 0 && b1 >= 0x81 && b1 <= 0xFE) {
            if (src.remaining() < 2) {
              return CoderResult.UNDERFLOW;
            }
            int b2 = src.get(pos + 1) & 0xff;
            if (b2 < 0x30) {
              return CoderResult.malformedForLength(1);
            }
            if (b2 <= 0x39) {
              if (src.remaining() < 4) {
                return CoderResult.UNDERFLOW;
              }
              int b3 = src.get(pos + 2) & 0xff;
              int b4 = src.get(pos + 3) & 0xff;
              if (b3 < 0x81 || b3 > 0xFE) {
                return CoderResult.malformedForLength(3);
              }
              if (b4 < 0x30 || b4 > 0x39) {
                return CoderResult.malformedForLength(4);
              }
              cp = decodeFour(b1, b2, b3, b4);
              if (cp < 0) {
                return CoderResult.unmappableForLength(4);
              }
              consumed = 4;
              handled = true;
            } else if (b2 < 0x40 || b2 == 0x7F || b2 == 0xFF) {
              return CoderResult.malformedForLength(2);
            }
          }
          if (!handled) {
            Group plain = groups[0];
            int[] row = plain.rows[b1];
            if (row == null && !pairUnmappable) {
              return CoderResult.malformedForLength(1);
            }
            if (src.remaining() < 2) {
              return CoderResult.UNDERFLOW;
            }
            if (row == null) {
              return CoderResult.unmappableForLength(2);
            }
            int b2 = src.get(pos + 1) & 0xff;
            if (b2 < plain.minTrail[b1] || b2 > plain.maxTrail[b1] || (cp = row[b2 - plain.minTrail[b1]]) < 0) {
              return !pairUnmappable && (single[b2] >= 0 || plain.rows[b2] != null) ? CoderResult.malformedForLength(1)
                  : CoderResult.unmappableForLength(2);
            }
            consumed = 2;
          }
          if (!put(dst, cp)) {
            return CoderResult.OVERFLOW;
          }
          src.position(pos + consumed);
        }
        return CoderResult.UNDERFLOW;
      }
    };
  }

  public CharsetEncoder newEncoder() {
    load();
    return new CharsetEncoder(this, averageBytes, maxBytes, replacement.clone()) {
      private final Surrogate.Parser sgp = new Surrogate.Parser();

      public boolean canEncode(char c) {
        return !Character.isSurrogate(c) && encodeSequence(c) != null;
      }

      protected CoderResult encodeLoop(CharBuffer src, ByteBuffer dst) {
        int mark = src.position();
        try {
          while (src.hasRemaining()) {
            char c = src.get();
            int cp = c;
            int width = 1;
            if (Character.isSurrogate(c)) {
              if (sgp.parse(c, src) < 0) {
                return sgp.error();
              }
              cp = sgp.character();
              width = 2;
            }
            byte[] seq = encodeSequence(cp);
            if (seq == null) {
              return CoderResult.unmappableForLength(width);
            }
            if (dst.remaining() < seq.length) {
              return CoderResult.OVERFLOW;
            }
            dst.put(seq);
            mark += width;
          }
          return CoderResult.UNDERFLOW;
        } finally {
          src.position(mark);
        }
      }
    };
  }
}
