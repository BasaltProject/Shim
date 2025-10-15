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

package java.util;

public class StringTokenizer implements Enumeration<Object> {
  private int currentPosition;
  private int newPosition;
  private final int maxPosition;
  private final String str;
  private String delimiters;
  private final boolean retDelims;
  private boolean delimsChanged;

  public StringTokenizer(String str, String delim, boolean returnDelims) {
    currentPosition = 0;
    newPosition = -1;
    delimsChanged = false;
    this.str = str;
    maxPosition = str.length();
    delimiters = delim;
    retDelims = returnDelims;
  }

  public StringTokenizer(String str, String delim) {
    this(str, delim, false);
  }

  public StringTokenizer(String str) {
    this(str, " \t\n\r\f", false);
  }

  private boolean isDelimiter(char c) {
    return delimiters.indexOf(c) >= 0;
  }

  private int skipDelimiters(int startPos) {
    if (delimiters == null) {
      throw new NullPointerException();
    }
    int position = startPos;
    while (!retDelims && position < maxPosition) {
      if (!isDelimiter(str.charAt(position))) {
        break;
      }
      position++;
    }
    return position;
  }

  private int scanToken(int startPos) {
    int position = startPos;
    while (position < maxPosition) {
      if (isDelimiter(str.charAt(position))) {
        break;
      }
      position++;
    }
    if (retDelims && startPos == position) {
      if (isDelimiter(str.charAt(position))) {
        position++;
      }
    }
    return position;
  }

  public boolean hasMoreTokens() {
    newPosition = skipDelimiters(currentPosition);
    return newPosition < maxPosition;
  }

  public String nextToken() {
    currentPosition = (newPosition >= 0 && !delimsChanged) ? newPosition : skipDelimiters(currentPosition);
    delimsChanged = false;
    newPosition = -1;
    if (currentPosition >= maxPosition) {
      throw new NoSuchElementException();
    }
    int start = currentPosition;
    currentPosition = scanToken(currentPosition);
    return str.substring(start, currentPosition);
  }

  public String nextToken(String delim) {
    delimiters = delim;
    delimsChanged = true;
    return nextToken();
  }

  public boolean hasMoreElements() {
    return hasMoreTokens();
  }

  public Object nextElement() {
    return nextToken();
  }

  public int countTokens() {
    int count = 0;
    int currpos = currentPosition;
    while (currpos < maxPosition) {
      currpos = skipDelimiters(currpos);
      if (currpos >= maxPosition) {
        break;
      }
      currpos = scanToken(currpos);
      count++;
    }
    return count;
  }
}
