/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.text;

import shim.text.CollationTable;

public class RuleBasedCollator extends Collator {
  private static RuleBasedCollator defaultInstance;

  private final String rules;
  private final transient CollationTable table;

  static synchronized RuleBasedCollator defaultCollator() {
    if (defaultInstance == null) {
      defaultInstance = new RuleBasedCollator(CollationTable.defaultTable(), "");
    }
    return defaultInstance;
  }

  private RuleBasedCollator(CollationTable table, String rules) {
    this.table = table;
    this.rules = rules;
  }

  public RuleBasedCollator(String rules) throws ParseException {
    this(CollationTable.parse(rules), rules);
  }

  public String getRules() {
    return rules;
  }

  public CollationElementIterator getCollationElementIterator(String source) {
    return new CollationElementIterator(source, this);
  }

  public CollationElementIterator getCollationElementIterator(CharacterIterator source) {
    StringBuilder sb = new StringBuilder();
    for (char c = source.first(); c != CharacterIterator.DONE; c = source.next()) {
      sb.append(c);
    }
    return new CollationElementIterator(sb.toString(), this);
  }

  CollationTable table() {
    return table;
  }

  String prepare(String s) {
    if (getDecomposition() == NO_DECOMPOSITION) {
      return s;
    }
    return Normalizer.normalize(s, Normalizer.Form.NFD);
  }

  @Override
  public synchronized int compare(String source, String target) {
    if (source == null || target == null) {
      throw new NullPointerException();
    }
    int[] a = table.elementsOf(prepare(source), null, null);
    int[] b = table.elementsOf(prepare(target), null, null);
    int result = compareLevel(a, b, 0);
    if (result == 0 && getStrength() >= SECONDARY) {
      result = compareLevel(a, b, 1);
    }
    if (result == 0 && getStrength() >= TERTIARY) {
      result = compareLevel(a, b, 2);
    }
    if (result == 0 && getStrength() == IDENTICAL) {
      int c = prepare(source).compareTo(prepare(target));
      result = c < 0 ? -1 : c > 0 ? 1 : 0;
    }
    return result;
  }

  private static int compareLevel(int[] a, int[] b, int level) {
    int i = 0;
    int j = 0;
    while (true) {
      while (i < a.length && level == 0 && a[i] == 0) {
        i += 3;
      }
      while (j < b.length && level == 0 && b[j] == 0) {
        j += 3;
      }
      if (i >= a.length || j >= b.length) {
        break;
      }
      int x = a[i + level];
      int y = b[j + level];
      if (x != y) {
        return x < y ? -1 : 1;
      }
      i += 3;
      j += 3;
    }
    if (level == 0) {
      boolean ra = i < a.length;
      boolean rb = j < b.length;
      return ra == rb ? 0 : ra ? 1 : -1;
    }
    int ra = (a.length - i) / 3;
    int rb = (b.length - j) / 3;
    return ra == rb ? 0 : ra > rb ? 1 : -1;
  }

  @Override
  public synchronized CollationKey getCollationKey(String source) {
    if (source == null) {
      return null;
    }
    int[] e = table.elementsOf(prepare(source), null, null);
    StringBuilder primary = new StringBuilder();
    StringBuilder secondary = new StringBuilder();
    StringBuilder tertiary = new StringBuilder();
    for (int i = 0; i < e.length; i += 3) {
      if (e[i] != 0) {
        primary.append((char) (e[i] + 1));
        secondary.append((char) (e[i + 1] + 1));
        tertiary.append((char) (e[i + 2] + 1));
      } else {
        secondary.append((char) (e[i + 1] + 118));
        if (e[i + 2] != 0) {
          tertiary.append((char) (e[i + 2] + 2));
        }
      }
    }
    StringBuilder key = new StringBuilder(primary);
    key.append((char) 0);
    if (getStrength() >= SECONDARY) {
      key.append(secondary);
    }
    key.append((char) 0);
    if (getStrength() >= TERTIARY) {
      key.append(tertiary);
    }
    if (getStrength() == IDENTICAL) {
      key.append((char) 0).append(prepare(source));
    }
    return new RuleBasedCollationKey(source, key.toString());
  }

  @Override
  public Object clone() {
    return super.clone();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (!super.equals(obj)) {
      return false;
    }
    return rules.equals(((RuleBasedCollator) obj).rules);
  }

  @Override
  public int hashCode() {
    return rules.hashCode();
  }
}
