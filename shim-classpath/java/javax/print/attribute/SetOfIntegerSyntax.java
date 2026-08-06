/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.print.attribute;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public abstract class SetOfIntegerSyntax implements Serializable, Cloneable {
  private int[][] members;

  protected SetOfIntegerSyntax(String members) { this.members = parse(members); }

  protected SetOfIntegerSyntax(int[][] members) { this.members = normalize(members); }

  protected SetOfIntegerSyntax(int member) {
    if (member < 0) {
      throw new IllegalArgumentException("Illegal member value");
    }
    members = new int[][] {{member, member}};
  }

  protected SetOfIntegerSyntax(int lowerBound, int upperBound) {
    if (lowerBound <= upperBound && lowerBound < 0) {
      throw new IllegalArgumentException("Illegal member value");
    }
    members = lowerBound <= upperBound ? new int[][] {{lowerBound, upperBound}} : new int[0][];
  }

  private static int[][] parse(String s) {
    if (s == null) {
      return new int[0][];
    }
    List<int[]> list = new ArrayList<int[]>();
    int len = s.length();
    int pos = 0;
    while (pos < len) {
      while (pos < len && Character.isWhitespace(s.charAt(pos))) {
        pos++;
      }
      if (pos >= len) {
        break;
      }
      int a = number(s, pos);
      pos = skipDigits(s, pos);
      int b = a;
      while (pos < len && Character.isWhitespace(s.charAt(pos))) {
        pos++;
      }
      if (pos < len && (s.charAt(pos) == ':' || s.charAt(pos) == '-')) {
        pos++;
        while (pos < len && Character.isWhitespace(s.charAt(pos))) {
          pos++;
        }
        b = number(s, pos);
        pos = skipDigits(s, pos);
        while (pos < len && Character.isWhitespace(s.charAt(pos))) {
          pos++;
        }
      }
      if (pos < len) {
        if (s.charAt(pos) != ',') {
          throw new IllegalArgumentException("Illegal character in set of integers");
        }
        pos++;
      }
      list.add(new int[] {a, b});
    }
    return normalize(list.toArray(new int[0][]));
  }

  private static int number(String s, int pos) {
    int start = pos;
    int n = 0;
    while (pos < s.length() && Character.isDigit(s.charAt(pos))) {
      n = n * 10 + (s.charAt(pos) - '0');
      pos++;
    }
    if (pos == start) {
      throw new IllegalArgumentException("Illegal character in set of integers");
    }
    return n;
  }

  private static int skipDigits(String s, int pos) {
    while (pos < s.length() && Character.isDigit(s.charAt(pos))) {
      pos++;
    }
    return pos;
  }

  private static int[][] normalize(int[][] input) {
    if (input == null) {
      return new int[0][];
    }
    List<int[]> ranges = new ArrayList<int[]>();
    for (int[] r : input) {
      if (r == null || r.length < 1 || r.length > 2) {
        throw new IllegalArgumentException("Array element is not a 1 or 2 element array");
      }
      int lo = r[0];
      int hi = r.length == 2 ? r[1] : r[0];
      if (lo <= hi) {
        if (lo < 0) {
          throw new IllegalArgumentException("Illegal member value");
        }
        ranges.add(new int[] {lo, hi});
      }
    }
    Collections.sort(ranges, new Comparator<int[]>() {
      public int compare(int[] a, int[] b) { return a[0] != b[0] ? Integer.compare(a[0], b[0]) : Integer.compare(a[1], b[1]); }
    });
    List<int[]> merged = new ArrayList<int[]>();
    for (int[] r : ranges) {
      if (!merged.isEmpty() && r[0] <= merged.get(merged.size() - 1)[1] + 1) {
        int[] last = merged.get(merged.size() - 1);
        last[1] = Math.max(last[1], r[1]);
      } else {
        merged.add(r);
      }
    }
    return merged.toArray(new int[0][]);
  }

  public int[][] getMembers() {
    int[][] copy = new int[members.length][];
    for (int i = 0; i < members.length; i++) {
      copy[i] = members[i].clone();
    }
    return copy;
  }

  public boolean contains(int x) {
    for (int[] r : members) {
      if (x < r[0]) {
        return false;
      } else if (x <= r[1]) {
        return true;
      }
    }
    return false;
  }

  public boolean contains(IntegerSyntax attribute) { return contains(attribute.getValue()); }

  public int next(int x) {
    for (int[] r : members) {
      if (x < r[0]) {
        return r[0];
      } else if (x < r[1]) {
        return x + 1;
      }
    }
    return -1;
  }

  public boolean equals(Object object) {
    if (object != null && object instanceof SetOfIntegerSyntax) {
      int[][] myMembers = this.members;
      int[][] otherMembers = ((SetOfIntegerSyntax) object).members;
      int m = myMembers.length;
      int n = otherMembers.length;
      if (m == n) {
        for (int i = 0; i < m; i++) {
          if (myMembers[i][0] != otherMembers[i][0] || myMembers[i][1] != otherMembers[i][1]) {
            return false;
          }
        }
        return true;
      }
    }
    return false;
  }

  public int hashCode() {
    int result = 0;
    for (int[] r : members) {
      result += r[0] + r[1];
    }
    return result;
  }

  public String toString() {
    StringBuilder result = new StringBuilder();
    for (int i = 0; i < members.length; i++) {
      if (i > 0) {
        result.append(',');
      }
      result.append(members[i][0]);
      if (members[i][0] != members[i][1]) {
        result.append('-');
        result.append(members[i][1]);
      }
    }
    return result.toString();
  }
}
