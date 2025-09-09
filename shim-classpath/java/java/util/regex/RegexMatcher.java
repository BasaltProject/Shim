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
 * A minimal implementation of a regular expression matcher.
 * 
 * @author Johannes Schindelin
 */
public class RegexMatcher extends Matcher {
  private final PikeVM vm;
  private char[] array;
  int[] groupStart, groupEnd;

  RegexMatcher(PikeVM vm, CharSequence string) {
    super(string);
    this.vm = vm;
  }

  private final PikeVM.Result adapter = new PikeVM.Result() {
    public void set(int[] start, int[] end) {
      RegexMatcher.this.start = start[0];
      RegexMatcher.this.end = end[0];
      RegexMatcher.this.groupStart = start;
      RegexMatcher.this.groupEnd = end;
    }
  };

  @Override
  public Matcher reset() {
    super.reset();
    start = end = -1;
    return this;
  }

  @Override
  public Matcher reset(CharSequence input) {
    super.reset(input);
    this.array = input != null ? input.toString().toCharArray() : null;
    start = end = -1;
    return this;
  }

  public boolean matches() {
    return vm.matches(array, 0, array.length, true, true, adapter);
  }

  public boolean find() {
    return find(end + (start == end ? 1 : 0));
  }

  public boolean find(int offset) {
    return vm.matches(array, offset, array.length, false, false, adapter);
  }

  public int start(int group) {
    return groupStart[group];
  }

  public int end(int group) {
    return groupEnd[group];
  }

  public String group(int group) {
    int offset = start(group);
    if (offset < 0) {
      return null;
    }
    int length = end(group) - offset;
    return new String(array, offset, length);
  }

  public int groupCount() {
    return groupStart.length - 1;
  }
}
