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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;

public abstract class Matcher implements MatchResult {
  protected CharSequence input;
  protected int start;
  protected int end;
  private int lastAppendPosition = 0;

  public Matcher(CharSequence input) {
    reset(input);
  }

  public abstract boolean matches();

  public boolean lookingAt() {
    return find(0) && start == 0;
  }

  public boolean find() {
    return find(end);
  }

  public abstract boolean find(int start);

  public Matcher reset() {
    return reset(input);
  }

  public Matcher reset(CharSequence input) {
    this.input = input;
    this.start = 0;
    this.end = 0;
    this.lastAppendPosition = 0;
    return this;
  }

  public String replaceAll(String replacement) {
    return replace(replacement, Integer.MAX_VALUE);
  }

  public String replaceFirst(String replacement) {
    return replace(replacement, 1);
  }

  public String replaceAll(Function<MatchResult, String> replacer) {
    Objects.requireNonNull(replacer, "replacer");
    reset();
    StringBuilder sb = new StringBuilder();
    while (find()) {
      String repl = replacer.apply(this);
      sb.append(input.subSequence(lastAppendPosition, start));
      sb.append(repl);
      lastAppendPosition = end;
    }
    appendTail(sb);
    return sb.toString();
  }

  public String replaceFirst(Function<MatchResult, String> replacer) {
    Objects.requireNonNull(replacer, "replacer");
    reset();
    if (!find()) {
      return input.toString();
    }
    StringBuilder sb = new StringBuilder();
    String repl = replacer.apply(this);
    sb.append(input.subSequence(lastAppendPosition, start));
    sb.append(repl);
    lastAppendPosition = end;
    appendTail(sb);
    return sb.toString();
  }

  public Stream<MatchResult> results() {
    List<MatchResult> list = new ArrayList<>();
    reset();
    while (find()) {
      list.add(toMatchResult());
    }
    return list.stream();
  }

  public MatchResult toMatchResult() {
    final int s = start();
    final int e = end();
    final String g = group();
    final int gc = groupCount();
    return new MatchResult() {
      @Override
      public int start() {
        return s;
      }

      @Override
      public int start(int group) {
        return group == 0 ? s : Matcher.this.start(group);
      }

      @Override
      public int end() {
        return e;
      }

      @Override
      public int end(int group) {
        return group == 0 ? e : Matcher.this.end(group);
      }

      @Override
      public String group() {
        return g;
      }

      @Override
      public String group(int group) {
        return group == 0 ? g : Matcher.this.group(group);
      }

      @Override
      public int groupCount() {
        return gc;
      }
    };
  }

  public Matcher appendReplacement(StringBuffer sb, String replacement) {
    sb.append(input.subSequence(lastAppendPosition, start));
    appendExpandedReplacement(sb, replacement);
    lastAppendPosition = end;
    return this;
  }

  public Matcher appendReplacement(StringBuilder sb, String replacement) {
    sb.append(input.subSequence(lastAppendPosition, start));
    appendExpandedReplacement(sb, replacement);
    lastAppendPosition = end;
    return this;
  }

  public StringBuffer appendTail(StringBuffer sb) {
    sb.append(input.subSequence(lastAppendPosition, input.length()));
    return sb;
  }

  public StringBuilder appendTail(StringBuilder sb) {
    sb.append(input.subSequence(lastAppendPosition, input.length()));
    return sb;
  }

  private void appendExpandedReplacement(Appendable sb, String replacement) {
    try {
      int cursor = 0;
      int len = replacement.length();
      while (cursor < len) {
        char c = replacement.charAt(cursor);
        if (c == '\\') {
          cursor++;
          if (cursor < len) {
            sb.append(replacement.charAt(cursor++));
          }
        } else if (c == '$') {
          cursor++;
          if (cursor < len && replacement.charAt(cursor) >= '0' && replacement.charAt(cursor) <= '9') {
            int g = replacement.charAt(cursor++) - '0';
            String grp = group(g);
            if (grp != null) {
              sb.append(grp);
            }
          } else {
            sb.append('$');
          }
        } else {
          sb.append(c);
          cursor++;
        }
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  protected String replace(String replacement, int limit) {
    reset();

    StringBuilder sb = null;
    int index = 0;
    int count = 0;
    while (count < limit && index < input.length()) {
      if (find(index)) {
        if (sb == null) {
          sb = new StringBuilder();
        }
        if (start > index) {
          sb.append(input.subSequence(index, start));
        }
        sb.append(replacement);
        index = end;
        ++count;
      } else if (index == 0) {
        return input.toString();
      } else {
        break;
      }
    }
    if (sb == null) {
      return input.toString();
    }
    if (index < input.length()) {
      sb.append(input.subSequence(index, input.length()));
    }
    return sb.toString();
  }

  @Override
  public int start() {
    return start;
  }

  @Override
  public int end() {
    return end;
  }

  @Override
  public String group() {
    return input.subSequence(start, end).toString();
  }

  @Override
  public int start(int group) {
    if (group == 0) {
      return start();
    }
    throw new UnsupportedOperationException();
  }

  @Override
  public int end(int group) {
    if (group == 0) {
      return end();
    }
    throw new UnsupportedOperationException();
  }

  @Override
  public String group(int group) {
    if (group == 0) {
      return group();
    }
    throw new UnsupportedOperationException();
  }

  @Override
  public int groupCount() {
    return 0;
  }
}
