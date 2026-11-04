/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.javac;

import com.sun.source.tree.LineMap;
import java.util.ArrayList;
import java.util.List;

public final class LineMapImpl implements LineMap {
  private final int[] starts;
  private final String src;

  public LineMapImpl(String src) {
    this.src = src;
    List<Integer> l = new ArrayList<Integer>();
    l.add(0);
    int n = src.length();
    for (int i = 0; i < n; i++) {
      char c = src.charAt(i);
      if (c == '\n') {
        l.add(i + 1);
      } else if (c == '\r') {
        if (i + 1 < n && src.charAt(i + 1) == '\n') {
          i++;
        }
        l.add(i + 1);
      }
    }
    starts = new int[l.size()];
    for (int i = 0; i < starts.length; i++) {
      starts[i] = l.get(i);
    }
  }

  private int lineIndex(long pos) {
    int lo = 0;
    int hi = starts.length - 1;
    while (lo < hi) {
      int mid = (lo + hi + 1) >>> 1;
      if (starts[mid] <= pos) {
        lo = mid;
      } else {
        hi = mid - 1;
      }
    }
    return lo;
  }

  public long getStartPosition(long line) {
    return starts[(int) line - 1];
  }

  public long getPosition(long line, long column) {
    int start = starts[(int) line - 1];
    int col = 1;
    int i = start;
    while (col < column && i < src.length()) {
      if (src.charAt(i) == '\t') {
        col = ((col - 1) / 8 + 1) * 8 + 1;
      } else {
        col++;
      }
      i++;
    }
    return i;
  }

  public long getLineNumber(long pos) {
    return lineIndex(pos) + 1;
  }

  public long getColumnNumber(long pos) {
    int start = starts[lineIndex(pos)];
    int col = 0;
    for (int i = start; i < pos && i < src.length(); i++) {
      if (src.charAt(i) == '\t') {
        col = (col / 8 + 1) * 8;
      } else {
        col++;
      }
    }
    return col + 1;
  }

  public String lineText(long pos) {
    int idx = lineIndex(pos);
    int s = starts[idx];
    int e = s;
    while (e < src.length() && src.charAt(e) != '\n' && src.charAt(e) != '\r') {
      e++;
    }
    return src.substring(s, e);
  }
}
