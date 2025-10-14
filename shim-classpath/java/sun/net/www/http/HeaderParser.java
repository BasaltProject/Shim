/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.net.www.http;

public class HeaderParser {
  private final String raw;
  private final String[][] tab;
  private int nkeys;

  public HeaderParser(String raw) {
    this.raw = raw;
    tab = new String[10][2];
    if (raw != null) {
      parse();
    }
  }

  private void parse() {
    int i = 0;
    int n = raw.length();
    while (i < n) {
      while (i < n && (raw.charAt(i) == ' ' || raw.charAt(i) == ',' || raw.charAt(i) == '\t')) {
        i++;
      }
      int ks = i;
      while (i < n && raw.charAt(i) != '=' && raw.charAt(i) != ',') {
        i++;
      }
      String key = raw.substring(ks, i).trim();
      String value = null;
      if (i < n && raw.charAt(i) == '=') {
        i++;
        while (i < n && raw.charAt(i) == ' ') {
          i++;
        }
        if (i < n && raw.charAt(i) == '"') {
          i++;
          int vs = i;
          while (i < n && raw.charAt(i) != '"') {
            i++;
          }
          value = raw.substring(vs, Math.min(i, n));
          i++;
        } else {
          int vs = i;
          while (i < n && raw.charAt(i) != ',') {
            i++;
          }
          value = raw.substring(vs, i).trim();
        }
      }
      if (!key.isEmpty() && nkeys < tab.length) {
        tab[nkeys][0] = key;
        tab[nkeys][1] = value;
        nkeys++;
      }
    }
  }

  public String findValue(String key) {
    return findValue(key, null);
  }

  public String findValue(String key, String defaultValue) {
    for (int i = 0; i < nkeys; i++) {
      if (key.equalsIgnoreCase(tab[i][0])) {
        return tab[i][1];
      }
    }
    return defaultValue;
  }

  public int findInt(String key, int defaultValue) {
    try {
      return Integer.parseInt(findValue(key, String.valueOf(defaultValue)));
    } catch (Throwable t) {
      return defaultValue;
    }
  }
}
