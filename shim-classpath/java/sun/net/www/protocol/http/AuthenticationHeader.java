/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.net.www.protocol.http;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import sun.net.www.MessageHeader;
import sun.net.www.http.HeaderParser;

final class AuthenticationHeader {
  static final class Challenge {
    final String scheme;
    final String params;
    final HeaderParser parser;

    Challenge(String scheme, String params) {
      this.scheme = scheme;
      this.params = params;
      this.parser = new HeaderParser(params);
    }
  }

  private final List<Challenge> challenges = new ArrayList<Challenge>();
  private final List<String> raws = new ArrayList<String>();

  AuthenticationHeader(String header, MessageHeader response) {
    Iterator<String> it = response.multiValueIterator(header);
    while (it.hasNext()) {
      String line = it.next();
      raws.add(line);
      parseLine(line);
    }
  }

  private void parseLine(String line) {
    List<String> pieces = new ArrayList<String>();
    StringBuilder cur = new StringBuilder();
    boolean quoted = false;
    for (int i = 0; i < line.length(); i++) {
      char c = line.charAt(i);
      if (c == '"') {
        quoted = !quoted;
      }
      if (c == ',' && !quoted) {
        pieces.add(cur.toString().trim());
        cur.setLength(0);
      } else {
        cur.append(c);
      }
    }
    pieces.add(cur.toString().trim());
    String scheme = null;
    StringBuilder params = new StringBuilder();
    for (String piece : pieces) {
      if (piece.isEmpty()) {
        continue;
      }
      int sp = piece.indexOf(' ');
      int eq = piece.indexOf('=');
      boolean startsScheme = sp > 0 && (eq < 0 || sp < eq);
      boolean bareScheme = sp < 0 && eq < 0;
      if (startsScheme || bareScheme) {
        if (scheme != null) {
          challenges.add(new Challenge(scheme, params.toString()));
        }
        scheme = bareScheme ? piece : piece.substring(0, sp);
        params.setLength(0);
        if (startsScheme) {
          params.append(piece.substring(sp + 1).trim());
        }
      } else {
        if (params.length() > 0) {
          params.append(", ");
        }
        params.append(piece);
      }
    }
    if (scheme != null) {
      challenges.add(new Challenge(scheme, params.toString()));
    }
  }

  List<Challenge> challenges() {
    return challenges;
  }

  Challenge preferred() {
    Challenge best = null;
    for (Challenge c : challenges) {
      if (c.scheme.equalsIgnoreCase("Digest")) {
        return c;
      }
      if (c.scheme.equalsIgnoreCase("Basic") && best == null) {
        best = c;
      }
    }
    return best;
  }

  String raw() {
    return raws.isEmpty() ? null : raws.get(0);
  }
}
