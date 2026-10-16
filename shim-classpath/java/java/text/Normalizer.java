/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.text;

public final class Normalizer {
  private Normalizer() { }

  public enum Form {
    NFD,
    NFC,
    NFKD,
    NFKC
  }

  private static final int kSBase = 0xAC00;
  private static final int kLBase = 0x1100;
  private static final int kVBase = 0x1161;
  private static final int kTBase = 0x11A7;
  private static final int kLCount = 19;
  private static final int kVCount = 21;
  private static final int kTCount = 28;
  private static final int kNCount = kVCount * kTCount;
  private static final int kSCount = kLCount * kNCount;

  private static final class Tables {
    final int[] cccStart;
    final int[] cccEnd;
    final int[] cccValue;
    final int[] canonKeys;
    final int[] canonOffsets;
    final int[] canonData;
    final int[] compatKeys;
    final int[] compatOffsets;
    final int[] compatData;
    final int[] pairFirst;
    final int[] pairSecond;
    final int[] pairResult;

    Tables() {
      int[] c = decode(NormalizerData.kCombiningClasses);
      int n = c.length / 3;
      cccStart = new int[n];
      cccEnd = new int[n];
      cccValue = new int[n];
      for (int i = 0; i < n; i++) {
        cccStart[i] = c[3 * i];
        cccEnd[i] = c[3 * i + 1];
        cccValue[i] = c[3 * i + 2];
      }
      int[] canon = decode(NormalizerData.kCanonical);
      int count = countRecords(canon);
      canonKeys = new int[count];
      canonOffsets = new int[count];
      canonData = flatten(canon, canonKeys, canonOffsets);
      int[] compat = decode(NormalizerData.kCompatibility);
      count = countRecords(compat);
      compatKeys = new int[count];
      compatOffsets = new int[count];
      compatData = flatten(compat, compatKeys, compatOffsets);
      int[] p = decode(NormalizerData.kPairs);
      n = p.length / 3;
      pairFirst = new int[n];
      pairSecond = new int[n];
      pairResult = new int[n];
      for (int i = 0; i < n; i++) {
        pairFirst[i] = p[3 * i];
        pairSecond[i] = p[3 * i + 1];
        pairResult[i] = p[3 * i + 2];
      }
    }

    private static int countRecords(int[] raw) {
      int count = 0;
      for (int i = 0; i < raw.length; i += 2 + raw[i + 1]) {
        count++;
      }
      return count;
    }

    private static int[] flatten(int[] raw, int[] keys, int[] offsets) {
      int total = 0;
      for (int i = 0, r = 0; i < raw.length; i += 2 + raw[i + 1], r++) {
        total += raw[i + 1];
      }
      int[] data = new int[total];
      int at = 0;
      for (int i = 0, r = 0; i < raw.length; i += 2 + raw[i + 1], r++) {
        keys[r] = raw[i];
        offsets[r] = at;
        System.arraycopy(raw, i + 2, data, at, raw[i + 1]);
        at += raw[i + 1];
      }
      return data;
    }

    private static int[] decode(String[] parts) {
      StringBuilder joined = new StringBuilder();
      for (String part : parts) {
        joined.append(part);
      }
      char[] chars = joined.toString().toCharArray();
      int[] out = new int[chars.length / 6];
      for (int i = 0; i < out.length; i++) {
        int v = 0;
        for (int j = 0; j < 6; j++) {
          int ch = chars[6 * i + j];
          v = (v << 4) | (ch <= '9' ? ch - '0' : ch - 'a' + 10);
        }
        out[i] = v;
      }
      return out;
    }
  }

  private static final class Holder {
    static final Tables kTables = new Tables();
  }

  public static String normalize(CharSequence src, Form form) {
    if (form == null) {
      throw new NullPointerException();
    }
    String s = src.toString();
    boolean plain = true;
    for (int i = 0; i < s.length(); i++) {
      if (s.charAt(i) >= 0xA0) {
        plain = false;
        break;
      }
    }
    if (plain) {
      return s;
    }
    Tables t = Holder.kTables;
    boolean compat = form == Form.NFKD || form == Form.NFKC;
    int[] cps = new int[s.length() + 8];
    int[] len = {0};
    for (int i = 0; i < s.length();) {
      int cp = s.codePointAt(i);
      i += Character.charCount(cp);
      cps = decompose(t, cp, compat, cps, len);
    }
    int n = len[0];
    reorder(t, cps, n);
    if (form == Form.NFC || form == Form.NFKC) {
      n = compose(t, cps, n);
    }
    StringBuilder sb = new StringBuilder(n);
    for (int i = 0; i < n; i++) {
      sb.appendCodePoint(cps[i]);
    }
    return sb.toString();
  }

  public static boolean isNormalized(CharSequence src, Form form) {
    if (form == null) {
      throw new NullPointerException();
    }
    String s = src.toString();
    return normalize(s, form).equals(s);
  }

  private static int[] grow(int[] a, int need) {
    if (need <= a.length) {
      return a;
    }
    int[] b = new int[Math.max(need, a.length * 2)];
    System.arraycopy(a, 0, b, 0, a.length);
    return b;
  }

  private static int[] decompose(Tables t, int cp, boolean compat, int[] out, int[] len) {
    if (cp >= kSBase && cp < kSBase + kSCount) {
      int index = cp - kSBase;
      int l = kLBase + index / kNCount;
      int v = kVBase + (index % kNCount) / kTCount;
      int tt = kTBase + index % kTCount;
      out = grow(out, len[0] + 3);
      out[len[0]++] = l;
      out[len[0]++] = v;
      if (tt != kTBase) {
        out[len[0]++] = tt;
      }
      return out;
    }
    int at = -1;
    int[] data = null;
    if (compat) {
      at = java.util.Arrays.binarySearch(t.compatKeys, cp);
      if (at >= 0) {
        data = t.compatData;
        int from = t.compatOffsets[at];
        int to = at + 1 < t.compatOffsets.length ? t.compatOffsets[at + 1] : data.length;
        out = grow(out, len[0] + (to - from));
        for (int i = from; i < to; i++) {
          out[len[0]++] = data[i];
        }
        return out;
      }
    }
    at = java.util.Arrays.binarySearch(t.canonKeys, cp);
    if (at >= 0) {
      data = t.canonData;
      int from = t.canonOffsets[at];
      int to = at + 1 < t.canonOffsets.length ? t.canonOffsets[at + 1] : data.length;
      out = grow(out, len[0] + (to - from));
      for (int i = from; i < to; i++) {
        out[len[0]++] = data[i];
      }
      return out;
    }
    out = grow(out, len[0] + 1);
    out[len[0]++] = cp;
    return out;
  }

  private static int combiningClass(Tables t, int cp) {
    if (cp < 0x300) {
      return 0;
    }
    int lo = 0;
    int hi = t.cccStart.length - 1;
    while (lo <= hi) {
      int mid = (lo + hi) >>> 1;
      if (cp < t.cccStart[mid]) {
        hi = mid - 1;
      } else if (cp > t.cccEnd[mid]) {
        lo = mid + 1;
      } else {
        return t.cccValue[mid];
      }
    }
    return 0;
  }

  private static void reorder(Tables t, int[] cps, int n) {
    int i = 0;
    while (i < n) {
      if (combiningClass(t, cps[i]) == 0) {
        i++;
        continue;
      }
      int start = i;
      while (i < n && combiningClass(t, cps[i]) != 0) {
        i++;
      }
      for (int a = start + 1; a < i; a++) {
        int cp = cps[a];
        int cls = combiningClass(t, cp);
        int b = a - 1;
        while (b >= start && combiningClass(t, cps[b]) > cls) {
          cps[b + 1] = cps[b];
          b--;
        }
        cps[b + 1] = cp;
      }
    }
  }

  private static int pairLookup(Tables t, int first, int second) {
    int lo = 0;
    int hi = t.pairFirst.length - 1;
    while (lo <= hi) {
      int mid = (lo + hi) >>> 1;
      int f = t.pairFirst[mid];
      int s = t.pairSecond[mid];
      if (first < f || (first == f && second < s)) {
        hi = mid - 1;
      } else if (first > f || second > s) {
        lo = mid + 1;
      } else {
        return t.pairResult[mid];
      }
    }
    return -1;
  }

  private static int composePair(Tables t, int first, int second) {
    if (first >= kLBase && first < kLBase + kLCount && second >= kVBase && second < kVBase + kVCount) {
      return kSBase + ((first - kLBase) * kVCount + (second - kVBase)) * kTCount;
    }
    if (first >= kSBase && first < kSBase + kSCount && (first - kSBase) % kTCount == 0 && second > kTBase
        && second < kTBase + kTCount) {
      return first + (second - kTBase);
    }
    return pairLookup(t, first, second);
  }

  private static int compose(Tables t, int[] cps, int n) {
    if (n == 0) {
      return 0;
    }
    int starterPos = 0;
    int starter = cps[0];
    int lastClass = combiningClass(t, starter);
    if (lastClass != 0) {
      lastClass = 256;
    }
    int out = 1;
    for (int i = 1; i < n; i++) {
      int ch = cps[i];
      int cls = combiningClass(t, ch);
      int composite = composePair(t, starter, ch);
      if (composite >= 0 && (lastClass < cls || lastClass == 0)) {
        cps[starterPos] = composite;
        starter = composite;
      } else {
        if (cls == 0) {
          starterPos = out;
          starter = ch;
        }
        lastClass = cls;
        cps[out++] = ch;
      }
    }
    return out;
  }
}
