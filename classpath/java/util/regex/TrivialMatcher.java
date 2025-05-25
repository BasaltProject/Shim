/*
 * Based on Avian (https://github.com/ReadyTalk/avian).
 * Original work Copyright (c) 2008-2015, Avian Contributors.
 * Original work licensed under ISC; see LICENSE.txt:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE.txt
 *
 * Modifications Copyright (c) 2026 BasaltMC.
 * Modifications licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: ISC AND MIT
 */

/* Copyright (c) 2008-2015, Avian Contributors

   Permission to use, copy, modify, and/or distribute this software
   for any purpose with or without fee is hereby granted, provided
   that the above copyright notice and this permission notice appear
   in all copies.

   There is NO WARRANTY for this software.  See license.txt for
   details. */

package java.util.regex;

/**
 * This is a work in progress.
 * 
 * @author zsombor and others
 */
class TrivialMatcher extends Matcher {
  private final String pattern;

  TrivialMatcher(String pattern, CharSequence input) {
    super(input);
    this.pattern = pattern;
  }

  public boolean matches() {
    if (pattern.equals(input.toString())) {
      start = 0;
      end = input.length();
      return true;
    } else {
      return false;
    }
  }

  public boolean find(int start) {
    String p = pattern;
    int i = TrivialPattern.indexOf(input, p, start);
    if (i >= 0) {
      this.start = i;
      this.end = i + p.length();
      return true;
    } else {
      return false;
    }
  }
}

