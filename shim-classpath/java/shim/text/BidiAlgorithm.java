/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.text;

import java.util.ArrayList;
import java.util.List;

public final class BidiAlgorithm {
  public static final int kMaxDepth = 125;

  private static final byte L = Character.DIRECTIONALITY_LEFT_TO_RIGHT;
  private static final byte R = Character.DIRECTIONALITY_RIGHT_TO_LEFT;
  private static final byte AL = Character.DIRECTIONALITY_RIGHT_TO_LEFT_ARABIC;
  private static final byte EN = Character.DIRECTIONALITY_EUROPEAN_NUMBER;
  private static final byte ES = Character.DIRECTIONALITY_EUROPEAN_NUMBER_SEPARATOR;
  private static final byte ET = Character.DIRECTIONALITY_EUROPEAN_NUMBER_TERMINATOR;
  private static final byte AN = Character.DIRECTIONALITY_ARABIC_NUMBER;
  private static final byte CS = Character.DIRECTIONALITY_COMMON_NUMBER_SEPARATOR;
  private static final byte NSM = Character.DIRECTIONALITY_NONSPACING_MARK;
  private static final byte BN = Character.DIRECTIONALITY_BOUNDARY_NEUTRAL;
  private static final byte B = Character.DIRECTIONALITY_PARAGRAPH_SEPARATOR;
  private static final byte S = Character.DIRECTIONALITY_SEGMENT_SEPARATOR;
  private static final byte WS = Character.DIRECTIONALITY_WHITESPACE;
  private static final byte ON = Character.DIRECTIONALITY_OTHER_NEUTRALS;
  private static final byte LRE = Character.DIRECTIONALITY_LEFT_TO_RIGHT_EMBEDDING;
  private static final byte LRO = Character.DIRECTIONALITY_LEFT_TO_RIGHT_OVERRIDE;
  private static final byte RLE = Character.DIRECTIONALITY_RIGHT_TO_LEFT_EMBEDDING;
  private static final byte RLO = Character.DIRECTIONALITY_RIGHT_TO_LEFT_OVERRIDE;
  private static final byte PDF = Character.DIRECTIONALITY_POP_DIRECTIONAL_FORMAT;
  private static final byte LRI = Character.DIRECTIONALITY_LEFT_TO_RIGHT_ISOLATE;
  private static final byte RLI = Character.DIRECTIONALITY_RIGHT_TO_LEFT_ISOLATE;
  private static final byte FSI = Character.DIRECTIONALITY_FIRST_STRONG_ISOLATE;
  private static final byte PDI = Character.DIRECTIONALITY_POP_DIRECTIONAL_ISOLATE;

  private final char[] text;
  private final int start;
  private final int end;
  private final byte[] types;
  private final byte[] levels;
  private final byte[] embeddings;
  private final boolean[] removed;
  private final int[] matchingPdi;
  private final int[] matchingIsolate;
  private byte paragraphLevel;
  private boolean[] flags = new boolean[4];
  private boolean[] fsiRtl;

  private BidiAlgorithm(char[] text, int start, int end, byte[] levels, byte[] embeddings) {
    this.text = text;
    this.start = start;
    this.end = end;
    int n = text.length;
    this.types = new byte[n];
    this.levels = levels;
    this.embeddings = embeddings;
    this.removed = new boolean[n];
    this.fsiRtl = new boolean[n];
    this.matchingPdi = new int[n];
    this.matchingIsolate = new int[n];
    for (int i = start; i < end; i++) {
      types[i] = Character.getDirectionality(text[i]);
    }
  }

  public static byte[] resolve(char[] text, int textStart, int length, byte[] embeddings, int embStart, int flags,
      byte[] baseLevelOut, int[] directionOut) {
    byte[] levels = new byte[length];
    char[] t = new char[length];
    System.arraycopy(text, textStart, t, 0, length);
    byte[] emb = null;
    if (embeddings != null) {
      emb = new byte[length];
      System.arraycopy(embeddings, embStart, emb, 0, length);
    }
    if (flags != 1 && flags != -1 && flags != -2) {
      flags = 0;
    }
    boolean[] total = new boolean[4];
    boolean anyMixed = false;
    List<Integer> starts = new ArrayList<Integer>();
    List<Integer> ends = new ArrayList<Integer>();
    List<Integer> plevels = new ArrayList<Integer>();
    int p = 0;
    int firstLevel = -1;
    while (p < length) {
      int q = p;
      while (q < length) {
        byte ty = Character.getDirectionality(t[q]);
        q++;
        if (ty == B) {
          break;
        }
      }
      BidiAlgorithm a = new BidiAlgorithm(t, p, q, levels, emb);
      a.run(flags);
      boolean individuallyMixed = emb != null || mixedFlags(a.flags, a.paragraphLevel);
      anyMixed |= individuallyMixed;
      for (int k = 0; k < 4; k++) {
        total[k] |= a.flags[k];
      }
      starts.add(p);
      ends.add(q);
      plevels.add((int) a.paragraphLevel);
      if (firstLevel < 0) {
        firstLevel = a.paragraphLevel;
      }
      p = q;
    }
    int direction;
    if (length == 0) {
      firstLevel = flags == 1 || flags == -1 ? 1 : 0;
      direction = firstLevel & 1;
    } else if (anyMixed || mixedFlags(total, (byte) firstLevel)) {
      direction = 2;
    } else {
      direction = firstLevel & 1;
      for (int k = 0; k < starts.size(); k++) {
        java.util.Arrays.fill(levels, starts.get(k), ends.get(k), (byte) (int) plevels.get(k));
      }
    }
    baseLevelOut[0] = (byte) firstLevel;
    directionOut[0] = direction;
    return levels;
  }

  private static boolean mixedFlags(boolean[] f, byte level) {
    boolean hasLtr = f[0];
    boolean hasRtl = f[1];
    boolean anAndNeutral = f[2] && f[3];
    if ((level & 1) == 0) {
      return hasRtl || anAndNeutral;
    }
    return hasLtr;
  }

  private boolean isIsolateInitiator(int i) {
    byte ty = types[i];
    return ty == LRI || ty == RLI || ty == FSI;
  }

  private void matchIsolates() {
    for (int i = start; i < end; i++) {
      matchingPdi[i] = end;
      matchingIsolate[i] = -1;
    }
    for (int i = start; i < end; i++) {
      if (!isIsolateInitiator(i)) {
        continue;
      }
      int depth = 1;
      for (int j = i + 1; j < end; j++) {
        if (isIsolateInitiator(j)) {
          depth++;
        } else if (types[j] == PDI) {
          depth--;
          if (depth == 0) {
            matchingPdi[i] = j;
            matchingIsolate[j] = i;
            break;
          }
        }
      }
    }
  }

  private int firstStrong(int from, int to, int fallback) {
    for (int i = from; i < to; i++) {
      byte ty = types[i];
      if (ty == L) {
        return 0;
      }
      if (ty == R || ty == AL) {
        return 1;
      }
      if (isIsolateInitiator(i)) {
        i = matchingPdi[i];
      }
    }
    return fallback;
  }

  private void run(int flags) {
    matchIsolates();
    if (flags == 0 || flags == 1) {
      paragraphLevel = (byte) flags;
    } else {
      paragraphLevel = (byte) firstStrong(start, end, flags == -1 ? 1 : 0);
    }
    explicitLevels();
    collectFlags();
    resolveSequences();
    removedLevels();
    lineEnd();
  }

  private void collectFlags() {
    for (int i = start; i < end; i++) {
      if (removed[i]) {
        flags[3] = true;
        continue;
      }
      byte ty = types[i];
      byte orig = Character.getDirectionality(text[i]);
      if (orig == LRI || orig == RLI || orig == FSI) {
        boolean rtl = orig == RLI || (orig == FSI && fsiRtl[i]);
        if (ty == L || ty == R) {
          flags[ty == L ? 0 : 1] = true;
        } else {
          flags[rtl ? 1 : 0] = true;
        }
      } else if (ty == L || ty == EN || ty == AN) {
        flags[0] = true;
        if (ty == AN) {
          flags[2] = true;
        }
      } else if (ty == R || ty == AL) {
        flags[1] = true;
      } else {
        flags[3] = true;
      }
      if (levels[i] != paragraphLevel) {
        flags[(levels[i] & 1) == 1 ? 1 : 0] = true;
      }
    }
  }

  private static byte nextOdd(byte level) {
    return (byte) ((level + 1) | 1);
  }

  private static byte nextEven(byte level) {
    return (byte) ((level + 2) & ~1);
  }

  private void explicitLevels() {
    int[] stackLevel = new int[kMaxDepth + 3];
    int[] stackOverride = new int[kMaxDepth + 3];
    boolean[] stackIsolate = new boolean[kMaxDepth + 3];
    int sp = 0;
    stackLevel[0] = paragraphLevel;
    stackOverride[0] = -1;
    int overflowIsolate = 0;
    int overflowEmbedding = 0;
    int validIsolate = 0;
    for (int i = start; i < end; i++) {
      byte ty = types[i];
      if (embeddings != null && embeddings[i] != 0) {
        int v = embeddings[i];
        levels[i] = (byte) Math.abs(v);
        if (v < 0) {
          types[i] = (levels[i] & 1) == 0 ? L : R;
        }
        if (ty == LRE || ty == RLE || ty == LRO || ty == RLO || ty == PDF || ty == BN) {
          removed[i] = true;
        }
        continue;
      }
      switch (ty) {
      case RLE:
      case LRE:
      case RLO:
      case LRO: {
        removed[i] = true;
        levels[i] = (byte) stackLevel[sp];
        boolean rtl = ty == RLE || ty == RLO;
        byte nl = rtl ? nextOdd((byte) stackLevel[sp]) : nextEven((byte) stackLevel[sp]);
        if (nl <= kMaxDepth && overflowIsolate == 0 && overflowEmbedding == 0) {
          sp++;
          stackLevel[sp] = nl;
          stackOverride[sp] = ty == RLO ? R : ty == LRO ? L : -1;
          stackIsolate[sp] = false;
        } else if (overflowIsolate == 0) {
          overflowEmbedding++;
        }
        break;
      }
      case RLI:
      case LRI:
      case FSI: {
        levels[i] = (byte) stackLevel[sp];
        if (stackOverride[sp] != -1) {
          types[i] = (byte) stackOverride[sp];
        }
        boolean rtl = ty == RLI;
        if (ty == FSI) {
          rtl = firstStrong(i + 1, matchingPdi[i], 0) == 1;
          fsiRtl[i] = rtl;
        }
        byte nl = rtl ? nextOdd((byte) stackLevel[sp]) : nextEven((byte) stackLevel[sp]);
        if (nl <= kMaxDepth && overflowIsolate == 0 && overflowEmbedding == 0) {
          validIsolate++;
          sp++;
          stackLevel[sp] = nl;
          stackOverride[sp] = -1;
          stackIsolate[sp] = true;
        } else {
          overflowIsolate++;
        }
        break;
      }
      case PDI: {
        if (overflowIsolate > 0) {
          overflowIsolate--;
        } else if (validIsolate > 0) {
          overflowEmbedding = 0;
          while (!stackIsolate[sp]) {
            sp--;
          }
          sp--;
          validIsolate--;
        }
        levels[i] = (byte) stackLevel[sp];
        if (stackOverride[sp] != -1) {
          types[i] = (byte) stackOverride[sp];
        }
        break;
      }
      case PDF: {
        removed[i] = true;
        levels[i] = (byte) stackLevel[sp];
        if (overflowIsolate > 0) {
          break;
        }
        if (overflowEmbedding > 0) {
          overflowEmbedding--;
        } else if (!stackIsolate[sp] && sp >= 1) {
          sp--;
        }
        break;
      }
      case B:
        levels[i] = paragraphLevel;
        break;
      case BN:
        removed[i] = true;
        levels[i] = (byte) stackLevel[sp];
        break;
      default:
        levels[i] = (byte) stackLevel[sp];
        if (stackOverride[sp] != -1) {
          types[i] = (byte) stackOverride[sp];
        }
        break;
      }
    }
  }

  private void resolveSequences() {
    List<int[]> runs = new ArrayList<int[]>();
    int i = start;
    while (i < end) {
      if (removed[i]) {
        i++;
        continue;
      }
      int s = i;
      int lvl = levels[i];
      int j = i + 1;
      int last = i;
      while (j < end) {
        if (removed[j]) {
          j++;
          continue;
        }
        if (levels[j] != lvl) {
          break;
        }
        last = j;
        j++;
      }
      runs.add(new int[] {s, last, lvl});
      i = j;
    }
    boolean[] used = new boolean[runs.size()];
    for (int r = 0; r < runs.size(); r++) {
      if (used[r]) {
        continue;
      }
      int[] run = runs.get(r);
      if (types[run[0]] == PDI && matchingIsolate[run[0]] >= 0 && originallyPdi(run[0])) {
        continue;
      }
      List<Integer> seq = new ArrayList<Integer>();
      int cur = r;
      while (true) {
        used[cur] = true;
        int[] cr = runs.get(cur);
        for (int k = cr[0]; k <= cr[1]; k++) {
          if (!removed[k]) {
            seq.add(k);
          }
        }
        int lastIdx = cr[1];
        if (isIsolateInitiatorOriginal(lastIdx) && matchingPdi[lastIdx] < end) {
          int pdi = matchingPdi[lastIdx];
          int next = -1;
          for (int q = 0; q < runs.size(); q++) {
            if (runs.get(q)[0] == pdi) {
              next = q;
              break;
            }
          }
          if (next >= 0) {
            cur = next;
            continue;
          }
        }
        break;
      }
      resolveOne(seq);
    }
  }

  private byte[] originalTypes;

  private boolean originallyPdi(int i) {
    return Character.getDirectionality(text[i]) == PDI;
  }

  private boolean isIsolateInitiatorOriginal(int i) {
    byte ty = Character.getDirectionality(text[i]);
    return ty == LRI || ty == RLI || ty == FSI;
  }

  private void resolveOne(List<Integer> seq) {
    int n = seq.size();
    if (n == 0) {
      return;
    }
    int[] idx = new int[n];
    byte[] t = new byte[n];
    for (int k = 0; k < n; k++) {
      idx[k] = seq.get(k);
      t[k] = types[idx[k]];
    }
    int level = levels[idx[0]];
    int prevLevel = paragraphLevel;
    for (int q = idx[0] - 1; q >= start; q--) {
      if (!removed[q]) {
        prevLevel = levels[q];
        break;
      }
    }
    int lastIdx = idx[n - 1];
    int nextLevel = paragraphLevel;
    boolean unmatchedInitiator = isIsolateInitiatorOriginal(lastIdx) && matchingPdi[lastIdx] >= end;
    if (!unmatchedInitiator) {
      for (int q = lastIdx + 1; q < end; q++) {
        if (!removed[q]) {
          nextLevel = levels[q];
          break;
        }
      }
    }
    byte sos = (Math.max(prevLevel, level) & 1) == 0 ? L : R;
    byte eos = (Math.max(nextLevel, level) & 1) == 0 ? L : R;
    boolean[] initiator = new boolean[n];
    boolean[] pdi = new boolean[n];
    for (int k = 0; k < n; k++) {
      byte o = Character.getDirectionality(text[idx[k]]);
      initiator[k] = o == LRI || o == RLI || o == FSI;
      pdi[k] = o == PDI;
    }
    for (int k = 0; k < n; k++) {
      if (t[k] == NSM) {
        if (k == 0) {
          t[k] = sos;
        } else if (initiator[k - 1] || pdi[k - 1]) {
          t[k] = ON;
        } else {
          t[k] = t[k - 1];
        }
      }
    }
    byte lastStrong = sos;
    for (int k = 0; k < n; k++) {
      if (t[k] == L || t[k] == R || t[k] == AL) {
        lastStrong = t[k];
      } else if (t[k] == EN && lastStrong == AL) {
        t[k] = AN;
      }
    }
    for (int k = 0; k < n; k++) {
      if (t[k] == AL) {
        t[k] = R;
      }
    }
    for (int k = 1; k < n - 1; k++) {
      if (t[k] == ES && t[k - 1] == EN && t[k + 1] == EN) {
        t[k] = EN;
      } else if (t[k] == CS && t[k - 1] == EN && t[k + 1] == EN) {
        t[k] = EN;
      } else if (t[k] == CS && t[k - 1] == AN && t[k + 1] == AN) {
        t[k] = AN;
      }
    }
    for (int k = 0; k < n; k++) {
      if (t[k] != ET) {
        continue;
      }
      int e = k;
      while (e < n && t[e] == ET) {
        e++;
      }
      boolean adj = (k > 0 && t[k - 1] == EN) || (e < n && t[e] == EN);
      for (int m = k; m < e; m++) {
        t[m] = adj ? EN : ON;
      }
      k = e - 1;
    }
    for (int k = 0; k < n; k++) {
      if (t[k] == ES || t[k] == ET || t[k] == CS) {
        t[k] = ON;
      }
    }
    lastStrong = sos;
    for (int k = 0; k < n; k++) {
      if (t[k] == L || t[k] == R) {
        lastStrong = t[k];
      } else if (t[k] == EN && lastStrong == L) {
        t[k] = L;
      }
    }
    byte embedding = (level & 1) == 0 ? L : R;
    bracketPairs(idx, t, sos, embedding);
    int k = 0;
    while (k < n) {
      if (!isNeutral(t[k])) {
        k++;
        continue;
      }
      int e = k;
      while (e < n && isNeutral(t[e])) {
        e++;
      }
      byte before = k == 0 ? sos : strongOf(t[k - 1]);
      byte after = e == n ? eos : strongOf(t[e]);
      byte res = before == after ? before : embedding;
      for (int m = k; m < e; m++) {
        t[m] = res;
      }
      k = e;
    }
    for (int m = 0; m < n; m++) {
      int lv = level;
      if ((level & 1) == 0) {
        if (t[m] == R) {
          lv += 1;
        } else if (t[m] == AN || t[m] == EN) {
          lv += 2;
        }
      } else if (t[m] == L || t[m] == EN || t[m] == AN) {
        lv += 1;
      }
      levels[idx[m]] = (byte) lv;
    }
  }

  private static boolean isNeutral(byte t) {
    return t == B || t == S || t == WS || t == ON || t == LRI || t == RLI || t == FSI || t == PDI;
  }

  private static byte strongOf(byte t) {
    return t == L ? L : R;
  }

  private static final String kOpen = "([{\u0f3a\u0f3c\u169b\u2045\u207d\u208d\u2308\u230a\u2329\u2768\u276a\u276c\u276e\u2770"
      + "\u2772\u2774\u27c5\u27e6\u27e8\u27ea\u27ec\u27ee\u2983\u2985\u2987\u2989\u298b\u298d\u298f"
      + "\u2991\u2993\u2995\u2997\u29d8\u29da\u29fc\u2e22\u2e24\u2e26\u2e28\u3008\u300a\u300c\u300e"
      + "\u3010\u3014\u3016\u3018\u301a\ufe59\ufe5b\ufe5d\uff08\uff3b\uff5b\uff5f\uff62";
  private static final String kClose = ")]}\u0f3b\u0f3d\u169c\u2046\u207e\u208e\u2309\u230b\u232a\u2769\u276b\u276d\u276f\u2771"
      + "\u2773\u2775\u27c6\u27e7\u27e9\u27eb\u27ed\u27ef\u2984\u2986\u2988\u298a\u298c\u298e\u2990"
      + "\u2992\u2994\u2996\u2998\u29d9\u29db\u29fd\u2e23\u2e25\u2e27\u2e29\u3009\u300b\u300d\u300f"
      + "\u3011\u3015\u3017\u3019\u301b\ufe5a\ufe5c\ufe5e\uff09\uff3d\uff5d\uff60\uff63";

  private static byte strongForBracket(byte ty) {
    if (ty == L) {
      return L;
    }
    if (ty == R || ty == EN || ty == AN) {
      return R;
    }
    return ON;
  }

  private void bracketPairs(int[] idx, byte[] t, byte sos, byte embedding) {
    int n = idx.length;
    int[] stackPos = new int[64];
    char[] stackClose = new char[64];
    int sp = 0;
    List<int[]> pairs = new ArrayList<int[]>();
    for (int k = 0; k < n; k++) {
      if (t[k] != ON) {
        continue;
      }
      char c = text[idx[k]];
      int o = kOpen.indexOf(c);
      int cl = kClose.indexOf(c);
      if (o >= 0) {
        if (sp == 63) {
          break;
        }
        stackPos[sp] = k;
        stackClose[sp] = kClose.charAt(o);
        sp++;
      } else if (cl >= 0) {
        char want = c;
        for (int q = sp - 1; q >= 0; q--) {
          if (canonicalClose(stackClose[q]) == canonicalClose(want)) {
            pairs.add(new int[] {stackPos[q], k});
            sp = q;
            break;
          }
        }
      }
    }
    java.util.Collections.sort(pairs, new java.util.Comparator<int[]>() {
      @Override
      public int compare(int[] a, int[] b) {
        return a[0] - b[0];
      }
    });
    byte opposite = embedding == L ? R : L;
    for (int[] pair : pairs) {
      boolean foundEmbedding = false;
      boolean foundOpposite = false;
      for (int k = pair[0] + 1; k < pair[1]; k++) {
        byte st = strongForBracket(t[k]);
        if (st == embedding) {
          foundEmbedding = true;
          break;
        } else if (st == opposite) {
          foundOpposite = true;
        }
      }
      byte set;
      if (foundEmbedding) {
        set = embedding;
      } else if (foundOpposite) {
        byte before = sos;
        for (int k = pair[0] - 1; k >= 0; k--) {
          byte st = strongForBracket(t[k]);
          if (st != ON) {
            before = st;
            break;
          }
        }
        set = before == opposite ? opposite : embedding;
      } else {
        continue;
      }
      t[pair[0]] = set;
      t[pair[1]] = set;
      for (int e = 0; e < 2; e++) {
        int at = pair[e];
        for (int k = at + 1; k < n; k++) {
          if (Character.getDirectionality(text[idx[k]]) == NSM) {
            t[k] = set;
          } else {
            break;
          }
        }
      }
    }
  }

  private static char canonicalClose(char c) {
    if (c == '\u232a') {
      return '\u3009';
    }
    return c;
  }

  private void removedLevels() {
    for (int i = end - 1; i >= start; i--) {
      if (!removed[i]) {
        continue;
      }
      int next = -1;
      for (int q = i + 1; q < end; q++) {
        if (!removed[q]) {
          next = q;
          break;
        }
      }
      levels[i] = next < 0 ? paragraphLevel : levels[next];
    }
  }

  private void lineEnd() {
    boolean trailing = true;
    for (int i = end - 1; i >= start; i--) {
      byte o = Character.getDirectionality(text[i]);
      if (o == B || o == S) {
        levels[i] = paragraphLevel;
        trailing = true;
      } else if (trailing && (o == WS || o == LRI || o == RLI || o == FSI || o == PDI || o == LRE || o == RLE
          || o == LRO || o == RLO || o == PDF || o == BN)) {
        levels[i] = paragraphLevel;
      } else {
        trailing = false;
      }
    }
  }
}
