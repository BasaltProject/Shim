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

import java.util.ConcurrentModificationException;
import java.util.Map;
import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public final class Matcher implements MatchResult {
  static final int kNoAnchor = 0;
  static final int kEndAnchor = 1;

  Pattern parentPattern;
  int[] groups;
  int from;
  int to;
  int hi;
  int lastAppendPosition = 0;
  int[] locals;
  int acceptMode = kNoAnchor;
  int first = -1;
  int last = 0;
  int oldLast = -1;
  CharSequence text;
  boolean hitEnd;
  boolean requireEnd;
  boolean transparentBounds = false;
  boolean anchoringBounds = true;
  int subEnd;
  int behindTarget;
  int modCount;

  Matcher(Pattern parent, CharSequence text) {
    this.parentPattern = parent;
    this.text = text;
    int parentGroupCount = Math.max(parent.capturingGroupCount, 10);
    groups = new int[parentGroupCount * 2];
    locals = new int[parent.localCount];
    reset();
  }

  int textLength() {
    return text.length();
  }

  public Pattern pattern() {
    return parentPattern;
  }

  public MatchResult toMatchResult() {
    return toMatchResult(text.toString());
  }

  private Immutable toMatchResult(String s) {
    return new Immutable(first, last, groupCount(), groups.clone(), s, parentPattern);
  }

  private static final class Immutable implements MatchResult {
    private final int first;
    private final int last;
    private final int[] groups;
    private final int groupCount;
    private final String text;
    private final Pattern pattern;

    Immutable(int first, int last, int groupCount, int[] groups, String text, Pattern pattern) {
      this.first = first;
      this.last = last;
      this.groupCount = groupCount;
      this.groups = groups;
      this.text = text;
      this.pattern = pattern;
    }

    @Override
    public int start() {
      checkMatch();
      return first;
    }

    @Override
    public int start(int group) {
      checkMatch();
      checkGroup(group);
      return groups[group * 2];
    }

    @Override
    public int end() {
      checkMatch();
      return last;
    }

    @Override
    public int end(int group) {
      checkMatch();
      checkGroup(group);
      return groups[group * 2 + 1];
    }

    @Override
    public int groupCount() {
      return groupCount;
    }

    @Override
    public String group() {
      checkMatch();
      return group(0);
    }

    @Override
    public String group(int group) {
      checkMatch();
      checkGroup(group);
      if ((groups[group * 2] == -1) || (groups[group * 2 + 1] == -1)) {
        return null;
      }
      return text.subSequence(groups[group * 2], groups[group * 2 + 1]).toString();
    }

    @Override
    public Map<String, Integer> namedGroups() {
      return pattern.namedGroups();
    }

    @Override
    public boolean hasMatch() {
      return first >= 0;
    }

    private void checkGroup(int group) {
      if (group < 0 || group > groupCount) {
        throw new IndexOutOfBoundsException("No group " + group);
      }
    }

    private void checkMatch() {
      if (first < 0) {
        throw new IllegalStateException("No match found");
      }
    }
  }

  public Matcher usePattern(Pattern newPattern) {
    if (newPattern == null) {
      throw new IllegalArgumentException("Pattern cannot be null");
    }
    parentPattern = newPattern;
    namedGroupsCache = null;
    int parentGroupCount = Math.max(newPattern.capturingGroupCount, 10);
    groups = new int[parentGroupCount * 2];
    locals = new int[newPattern.localCount];
    for (int i = 0; i < groups.length; i++) {
      groups[i] = -1;
    }
    for (int i = 0; i < locals.length; i++) {
      locals[i] = -1;
    }
    modCount++;
    return this;
  }

  private Map<String, Integer> namedGroupsCache;

  public Matcher reset() {
    first = -1;
    last = 0;
    oldLast = -1;
    for (int i = 0; i < groups.length; i++) {
      groups[i] = -1;
    }
    for (int i = 0; i < locals.length; i++) {
      locals[i] = -1;
    }
    lastAppendPosition = 0;
    from = 0;
    to = textLength();
    hi = to;
    modCount++;
    return this;
  }

  public Matcher reset(CharSequence input) {
    text = input;
    return reset();
  }

  public int start() {
    if (first < 0) {
      throw new IllegalStateException("No match found");
    }
    return first;
  }

  public int start(int group) {
    if (first < 0) {
      throw new IllegalStateException("No match found");
    }
    if (group < 0 || group > groupCount()) {
      throw new IndexOutOfBoundsException("No group " + group);
    }
    return groups[group * 2];
  }

  public int start(String name) {
    return groups[getMatchedGroupIndex(name) * 2];
  }

  public int end() {
    if (first < 0) {
      throw new IllegalStateException("No match found");
    }
    return last;
  }

  public int end(int group) {
    if (first < 0) {
      throw new IllegalStateException("No match found");
    }
    if (group < 0 || group > groupCount()) {
      throw new IndexOutOfBoundsException("No group " + group);
    }
    return groups[group * 2 + 1];
  }

  public int end(String name) {
    return groups[getMatchedGroupIndex(name) * 2 + 1];
  }

  public String group() {
    return group(0);
  }

  public String group(int group) {
    if (first < 0) {
      throw new IllegalStateException("No match found");
    }
    if (group < 0 || group > groupCount()) {
      throw new IndexOutOfBoundsException("No group " + group);
    }
    if ((groups[group * 2] == -1) || (groups[group * 2 + 1] == -1)) {
      return null;
    }
    return text.subSequence(groups[group * 2], groups[group * 2 + 1]).toString();
  }

  public String group(String name) {
    int group = getMatchedGroupIndex(name);
    if ((groups[group * 2] == -1) || (groups[group * 2 + 1] == -1)) {
      return null;
    }
    return text.subSequence(groups[group * 2], groups[group * 2 + 1]).toString();
  }

  public int groupCount() {
    return parentPattern.capturingGroupCount - 1;
  }

  public boolean hasMatch() {
    return first >= 0;
  }

  public Map<String, Integer> namedGroups() {
    return parentPattern.namedGroups();
  }

  public boolean matches() {
    return match(from, kEndAnchor);
  }

  public boolean find() {
    int nextSearchIndex = last;
    if (nextSearchIndex == first) {
      nextSearchIndex++;
    }
    if (nextSearchIndex < from) {
      nextSearchIndex = from;
    }
    if (nextSearchIndex > to) {
      for (int i = 0; i < groups.length; i++) {
        groups[i] = -1;
      }
      first = -1;
      return false;
    }
    return search(nextSearchIndex);
  }

  public boolean find(int start) {
    int limit = textLength();
    if ((start < 0) || (start > limit)) {
      throw new IndexOutOfBoundsException("Illegal start index");
    }
    reset();
    return search(start);
  }

  public boolean lookingAt() {
    return match(from, kNoAnchor);
  }

  public static String quoteReplacement(String s) {
    if ((s.indexOf('\\') == -1) && (s.indexOf('$') == -1)) {
      return s;
    }
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < s.length(); i++) {
      char c = s.charAt(i);
      if (c == '\\' || c == '$') {
        sb.append('\\');
      }
      sb.append(c);
    }
    return sb.toString();
  }

  public Matcher appendReplacement(StringBuffer sb, String replacement) {
    if (first < 0) {
      throw new IllegalStateException("No match found");
    }
    StringBuilder result = new StringBuilder();
    appendExpandedReplacement(replacement, result);
    sb.append(text, lastAppendPosition, first);
    sb.append(result);
    lastAppendPosition = last;
    modCount++;
    return this;
  }

  public Matcher appendReplacement(StringBuilder sb, String replacement) {
    if (first < 0) {
      throw new IllegalStateException("No match found");
    }
    StringBuilder result = new StringBuilder();
    appendExpandedReplacement(replacement, result);
    sb.append(text, lastAppendPosition, first);
    sb.append(result);
    lastAppendPosition = last;
    modCount++;
    return this;
  }

  private StringBuilder appendExpandedReplacement(String replacement, StringBuilder result) {
    int cursor = 0;
    while (cursor < replacement.length()) {
      char nextChar = replacement.charAt(cursor);
      if (nextChar == '\\') {
        cursor++;
        if (cursor == replacement.length()) {
          throw new IllegalArgumentException("character to be escaped is missing");
        }
        nextChar = replacement.charAt(cursor);
        result.append(nextChar);
        cursor++;
      } else if (nextChar == '$') {
        cursor++;
        if (cursor == replacement.length()) {
          throw new IllegalArgumentException("Illegal group reference: group index is missing");
        }
        nextChar = replacement.charAt(cursor);
        int refNum = -1;
        if (nextChar == '{') {
          cursor++;
          StringBuilder gsb = new StringBuilder();
          while (cursor < replacement.length()) {
            nextChar = replacement.charAt(cursor);
            if ((nextChar >= 'a' && nextChar <= 'z') || (nextChar >= 'A' && nextChar <= 'Z')
                || (nextChar >= '0' && nextChar <= '9')) {
              gsb.append(nextChar);
              cursor++;
            } else {
              break;
            }
          }
          if (gsb.length() == 0) {
            throw new IllegalArgumentException("named capturing group has 0 length name");
          }
          if (nextChar != '}') {
            throw new IllegalArgumentException("named capturing group is missing trailing '}'");
          }
          String gname = gsb.toString();
          if (gname.charAt(0) >= '0' && gname.charAt(0) <= '9') {
            throw new IllegalArgumentException("capturing group name {" + gname + "} starts with digit character");
          }
          if (!parentPattern.namedGroupsInternal().containsKey(gname)) {
            throw new IllegalArgumentException("No group with name {" + gname + "}");
          }
          refNum = parentPattern.namedGroupsInternal().get(gname);
          cursor++;
        } else {
          refNum = nextChar - '0';
          if ((refNum < 0) || (refNum > 9)) {
            throw new IllegalArgumentException("Illegal group reference");
          }
          cursor++;
          boolean done = false;
          while (!done) {
            if (cursor >= replacement.length()) {
              break;
            }
            int nextDigit = replacement.charAt(cursor) - '0';
            if ((nextDigit < 0) || (nextDigit > 9)) {
              break;
            }
            int newRefNum = (refNum * 10) + nextDigit;
            if (groupCount() < newRefNum) {
              done = true;
            } else {
              refNum = newRefNum;
              cursor++;
            }
          }
        }
        if (start(refNum) != -1 && end(refNum) != -1) {
          result.append(text, start(refNum), end(refNum));
        }
      } else {
        result.append(nextChar);
        cursor++;
      }
    }
    return result;
  }

  public StringBuffer appendTail(StringBuffer sb) {
    sb.append(text, lastAppendPosition, textLength());
    return sb;
  }

  public StringBuilder appendTail(StringBuilder sb) {
    sb.append(text, lastAppendPosition, textLength());
    return sb;
  }

  public String replaceAll(String replacement) {
    reset();
    boolean result = find();
    if (result) {
      StringBuilder sb = new StringBuilder();
      do {
        appendReplacement(sb, replacement);
        result = find();
      } while (result);
      appendTail(sb);
      return sb.toString();
    }
    return text.toString();
  }

  public String replaceAll(Function<MatchResult, String> replacer) {
    Objects.requireNonNull(replacer);
    reset();
    boolean result = find();
    if (result) {
      StringBuilder sb = new StringBuilder();
      do {
        int ec = modCount;
        String replacement = replacer.apply(this);
        if (ec != modCount) {
          throw new ConcurrentModificationException();
        }
        appendReplacement(sb, replacement);
        result = find();
      } while (result);
      appendTail(sb);
      return sb.toString();
    }
    return text.toString();
  }

  public Stream<MatchResult> results() {
    class MatchResultIterator extends Spliterators.AbstractSpliterator<MatchResult> {
      int expectedCount = -1;
      int state;
      int emitted;

      MatchResultIterator() {
        super(Long.MAX_VALUE, Spliterator.ORDERED | Spliterator.NONNULL);
      }

      @Override
      public boolean tryAdvance(Consumer<? super MatchResult> action) {
        if (action == null) {
          throw new NullPointerException();
        }
        if (state == 0) {
          reset();
          state = 1;
          expectedCount = modCount;
        }
        if (expectedCount >= 0 && expectedCount != modCount) {
          throw new ConcurrentModificationException();
        }
        if (find()) {
          expectedCount = modCount;
          action.accept(toMatchResult());
          if (expectedCount != modCount) {
            throw new ConcurrentModificationException();
          }
          return true;
        }
        expectedCount = -1;
        return false;
      }
    }
    return StreamSupport.stream(new MatchResultIterator(), false);
  }

  public String replaceFirst(String replacement) {
    if (replacement == null) {
      throw new NullPointerException("replacement");
    }
    reset();
    if (!find()) {
      return text.toString();
    }
    StringBuilder sb = new StringBuilder();
    appendReplacement(sb, replacement);
    appendTail(sb);
    return sb.toString();
  }

  public String replaceFirst(Function<MatchResult, String> replacer) {
    Objects.requireNonNull(replacer);
    reset();
    if (!find()) {
      return text.toString();
    }
    StringBuilder sb = new StringBuilder();
    int ec = modCount;
    String replacement = replacer.apply(this);
    if (ec != modCount) {
      throw new ConcurrentModificationException();
    }
    appendReplacement(sb, replacement);
    appendTail(sb);
    return sb.toString();
  }

  public Matcher region(int start, int end) {
    if ((start < 0) || (start > textLength())) {
      throw new IndexOutOfBoundsException("start");
    }
    if ((end < 0) || (end > textLength())) {
      throw new IndexOutOfBoundsException("end");
    }
    if (start > end) {
      throw new IndexOutOfBoundsException("start > end");
    }
    reset();
    from = start;
    to = end;
    hi = end;
    return this;
  }

  public int regionStart() {
    return from;
  }

  public int regionEnd() {
    return to;
  }

  public boolean hasTransparentBounds() {
    return transparentBounds;
  }

  public Matcher useTransparentBounds(boolean b) {
    transparentBounds = b;
    return this;
  }

  public boolean hasAnchoringBounds() {
    return anchoringBounds;
  }

  public Matcher useAnchoringBounds(boolean b) {
    anchoringBounds = b;
    return this;
  }

  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("java.util.regex.Matcher").append("[pattern=").append(pattern()).append(" region=");
    sb.append(regionStart()).append(',').append(regionEnd());
    sb.append(" lastmatch=");
    if ((first >= 0) && (group() != null)) {
      sb.append(group());
    }
    sb.append(']');
    return sb.toString();
  }

  public boolean hitEnd() {
    return hitEnd;
  }

  public boolean requireEnd() {
    return requireEnd;
  }

  boolean search(int from) {
    hitEnd = false;
    requireEnd = false;
    from = from < 0 ? 0 : from;
    this.oldLast = oldLast < 0 ? from : oldLast;
    int savedHi = hi;
    hi = to;
    boolean found = false;
    int i = from;
    Nodes.Node root = parentPattern.root;
    CharSequence seq = text;
    while (i <= to) {
      for (int k = 0; k < groups.length; k++) {
        groups[k] = -1;
      }
      for (int k = 0; k < locals.length; k++) {
        locals[k] = -1;
      }
      acceptMode = kNoAnchor;
      this.first = i;
      if (root.match(this, i, seq)) {
        found = true;
        break;
      }
      i++;
    }
    hi = savedHi;
    if (!found) {
      this.first = -1;
      this.oldLast = this.last;
      modCount++;
      return false;
    }
    this.oldLast = this.last;
    modCount++;
    return true;
  }

  boolean match(int from, int anchor) {
    hitEnd = false;
    requireEnd = false;
    from = from < 0 ? 0 : from;
    this.first = from;
    this.oldLast = oldLast < 0 ? from : oldLast;
    for (int k = 0; k < groups.length; k++) {
      groups[k] = -1;
    }
    for (int k = 0; k < locals.length; k++) {
      locals[k] = -1;
    }
    acceptMode = anchor;
    int savedHi = hi;
    hi = to;
    boolean result = parentPattern.root.match(this, from, text);
    hi = savedHi;
    if (!result) {
      this.first = -1;
    }
    this.oldLast = this.last;
    modCount++;
    return result;
  }

  static int groupNumber(MatchResult result, String name) {
    Objects.requireNonNull(name, "Group name");
    Integer number = result.namedGroups().get(name);
    if (number != null) {
      return number;
    }
    throw new IllegalArgumentException("No group with name <" + name + ">");
  }

  int getMatchedGroupIndex(String name) {
    Objects.requireNonNull(name, "Group name");
    if (first < 0) {
      throw new IllegalStateException("No match found");
    }
    Integer index = parentPattern.namedGroupsInternal().get(name);
    if (index == null) {
      throw new IllegalArgumentException("No group with name <" + name + ">");
    }
    return index;
  }
}
