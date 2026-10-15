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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Stream;

public abstract class Pattern implements PikeVMOpcodes, Serializable {
  private static final long serialVersionUID = 5073258162644648461L;

  public static final int UNIX_LINES       = 1;
  public static final int CASE_INSENSITIVE = 2;
  public static final int COMMENTS         = 4;
  public static final int MULTILINE        = 8;
  public static final int LITERAL          = 16;
  public static final int DOTALL           = 32;
  public static final int UNICODE_CASE     = 64;
  public static final int CANON_EQ         = 128;

  private final int patternFlags;
  private final String pattern;

  protected Pattern(String pattern, int flags) {
    this.pattern = pattern;
    this.patternFlags = flags;
  }

  public static Pattern compile(String regex) {
    return compile(regex, 0);
  }

  public static Pattern compile(String regex, int flags) {
    Objects.requireNonNull(regex, "regex");
    if ((flags & LITERAL) != 0) {
      return new TrivialPattern(regex, regex, flags);
    }
    try {
      return new Compiler(flags).compile(regex);
    } catch (PatternSyntaxException e) {
      throw e;
    } catch (RuntimeException e) {
      throw new PatternSyntaxException(e.getMessage() != null ? e.getMessage() : "Syntax error", regex, -1);
    }
  }

  public int flags() {
    return patternFlags;
  }

  public abstract Matcher matcher(CharSequence input);

  public static boolean matches(String regex, CharSequence input) {
    return Pattern.compile(regex).matcher(input).matches();
  }

  public String pattern() {
    return pattern;
  }

  public static String quote(String s) {
    Objects.requireNonNull(s, "s");
    int slashEIndex = s.indexOf("\\E");
    if (slashEIndex == -1) {
      return "\\Q" + s + "\\E";
    }
    StringBuilder sb = new StringBuilder(s.length() * 2);
    sb.append("\\Q");
    int current = 0;
    while ((slashEIndex = s.indexOf("\\E", current)) != -1) {
      sb.append(s, current, slashEIndex);
      current = slashEIndex + 2;
      sb.append("\\E\\\\E\\Q");
    }
    sb.append(s, current, s.length());
    sb.append("\\E");
    return sb.toString();
  }

  public Predicate<String> asPredicate() {
    return s -> matcher(s).find();
  }

  public Predicate<String> asMatchPredicate() {
    return s -> matcher(s).matches();
  }

  public Stream<String> splitAsStream(CharSequence input) {
    return Arrays.stream(split(input));
  }

  public String[] split(CharSequence input) {
    return split(input, 0);
  }

  public String[] split(CharSequence input, int limit) {
    if (limit <= 0) {
      limit = Integer.MAX_VALUE;
    }
    Matcher matcher = matcher(input);
    List<String> result = new ArrayList<String>();
    int offset = 0;
    for (;;) {
      if (result.size() >= limit || !matcher.find()) {
        break;
      }
      result.add(input.subSequence(offset, matcher.start()).toString());
      offset = matcher.end();
    }
    if (offset == 0 || offset < input.length()) {
      result.add(input.subSequence(offset, input.length()).toString());
    }
    return result.toArray(new String[result.size()]);
  }

  @Override
  public String toString() {
    return pattern;
  }
}
