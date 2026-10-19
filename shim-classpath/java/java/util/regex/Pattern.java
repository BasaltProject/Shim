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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.regex.Nodes.Node;

public final class Pattern implements Serializable {
  private static final long serialVersionUID = 5073258162644648461L;

  public static final int UNIX_LINES = 0x01;
  public static final int CASE_INSENSITIVE = 0x02;
  public static final int COMMENTS = 0x04;
  public static final int MULTILINE = 0x08;
  public static final int LITERAL = 0x10;
  public static final int DOTALL = 0x20;
  public static final int UNICODE_CASE = 0x40;
  public static final int CANON_EQ = 0x80;
  public static final int UNICODE_CHARACTER_CLASS = 0x100;

  private static final int kAllFlags = 0x1ff;
  private static final int kInf = Nodes.kInfinity;

  private final String pattern;
  private int flags;
  private final int flags0;
  transient Node root;
  transient int capturingGroupCount;
  transient int localCount;
  private transient Map<String, Integer> namedGroups;
  private transient int cursor;
  private transient int currentFlags;
  private transient boolean compiled;
  private transient boolean hasGroupRef;

  private static final class Frag {
    Node head;
    Node tail;
    int min;
    int max;
    CharPredicate single;

    Frag(Node head, Node tail, int min, int max) {
      this.head = head;
      this.tail = tail;
      this.min = min;
      this.max = max;
    }
  }

  public static Pattern compile(String regex) {
    return new Pattern(regex, 0);
  }

  public static Pattern compile(String regex, int flags) {
    return new Pattern(regex, flags);
  }

  public String pattern() {
    return pattern;
  }

  @Override
  public String toString() {
    return pattern;
  }

  public Matcher matcher(CharSequence input) {
    if (!compiled) {
      synchronized (this) {
        if (!compiled) {
          compile();
        }
      }
    }
    return new Matcher(this, input);
  }

  public int flags() {
    return flags0;
  }

  public static boolean matches(String regex, CharSequence input) {
    Pattern p = Pattern.compile(regex);
    Matcher m = p.matcher(input);
    return m.matches();
  }

  public String[] split(CharSequence input, int limit) {
    return split(input, limit, false);
  }

  public String[] splitWithDelimiters(CharSequence input, int limit) {
    return split(input, limit, true);
  }

  private String[] split(CharSequence input, int limit, boolean withDelimiters) {
    int matchCount = 0;
    int index = 0;
    boolean matchLimited = limit > 0;
    ArrayList<String> matchList = new ArrayList<String>();
    Matcher m = matcher(input);
    while (m.find()) {
      if (!matchLimited || matchCount < limit - 1) {
        if (index == 0 && index == m.start() && m.start() == m.end()) {
          continue;
        }
        String match = input.subSequence(index, m.start()).toString();
        matchList.add(match);
        index = m.end();
        if (withDelimiters) {
          matchList.add(input.subSequence(m.start(), index).toString());
        }
        ++matchCount;
      } else if (matchCount == limit - 1) {
        String match = input.subSequence(index, input.length()).toString();
        matchList.add(match);
        index = m.end();
        ++matchCount;
      }
    }
    if (index == 0) {
      return new String[] {input.toString()};
    }
    if (!matchLimited || matchCount < limit) {
      matchList.add(input.subSequence(index, input.length()).toString());
    }
    int resultSize = matchList.size();
    if (limit == 0) {
      while (resultSize > 0 && matchList.get(resultSize - 1).isEmpty()) {
        resultSize--;
      }
    }
    String[] result = new String[resultSize];
    return matchList.subList(0, resultSize).toArray(result);
  }

  public String[] split(CharSequence input) {
    return split(input, 0, false);
  }

  public static String quote(String s) {
    int slashEIndex = s.indexOf("\\E");
    if (slashEIndex == -1) {
      return "\\Q" + s + "\\E";
    }
    int lenHint = s.length();
    lenHint = (lenHint < Integer.MAX_VALUE - 8 - lenHint) ? (lenHint << 1) : (Integer.MAX_VALUE - 8);
    StringBuilder sb = new StringBuilder(lenHint);
    sb.append("\\Q");
    int current = 0;
    do {
      sb.append(s, current, slashEIndex).append("\\E\\\\E\\Q");
      current = slashEIndex + 2;
    } while ((slashEIndex = s.indexOf("\\E", current)) != -1);
    return sb.append(s, current, s.length()).append("\\E").toString();
  }

  public Predicate<String> asPredicate() {
    return new Predicate<String>() {
      @Override
      public boolean test(String s) {
        return matcher(s).find();
      }
    };
  }

  public Predicate<String> asMatchPredicate() {
    return new Predicate<String>() {
      @Override
      public boolean test(String s) {
        return matcher(s).matches();
      }
    };
  }

  public Stream<String> splitAsStream(final CharSequence input) {
    return Arrays.stream(split(input));
  }

  private Stream<String> splitWithDelimitersAsStream(final CharSequence input) {
    return Arrays.stream(splitWithDelimiters(input, 0));
  }

  public Map<String, Integer> namedGroups() {
    return Collections.unmodifiableMap(new HashMap<String, Integer>(namedGroups));
  }

  private Pattern(String p, int f) {
    if ((f & ~kAllFlags) != 0) {
      throw new IllegalArgumentException("Unknown flag 0x" + Integer.toHexString(f));
    }
    pattern = Objects.requireNonNull(p, "regex");
    if ((f & UNICODE_CHARACTER_CLASS) != 0) {
      f |= UNICODE_CASE;
    }
    flags = f;
    flags0 = f;
    if (pattern.length() > 0) {
      compile();
    } else {
      root = new Nodes.Accept();
      capturingGroupCount = 1;
      localCount = 0;
      namedGroups = new HashMap<String, Integer>();
      compiled = true;
    }
  }

  private PatternSyntaxException error(String s) {
    return new PatternSyntaxException(s, pattern, cursor - 1);
  }

  private PatternSyntaxException errorAt(String s, int index) {
    return new PatternSyntaxException(s, pattern, index);
  }

  private void compile() {
    cursor = 0;
    currentFlags = flags;
    capturingGroupCount = 1;
    localCount = 0;
    namedGroups = new HashMap<String, Integer>();
    Frag f;
    if ((flags & LITERAL) != 0) {
      int[] cps = pattern.codePoints().toArray();
      f = cps.length == 0 ? empty() : literal(cps);
      cursor = pattern.length();
    } else {
      f = expr();
      if (cursor < pattern.length()) {
        if (pattern.charAt(cursor) == ')') {
          throw errorAt("Unmatched closing ')'", cursor - 1);
        }
        throw errorAt("Unexpected character '" + pattern.charAt(cursor) + "'", cursor);
      }
    }
    f.tail.next = new Nodes.Accept();
    root = f.head;
    compiled = true;
  }

  private Frag empty() {
    Node n = new Nodes.Pass();
    return new Frag(n, n, 0, 0);
  }

  private static int satAdd(int a, int b) {
    if (a == kInf || b == kInf) {
      return kInf;
    }
    long r = (long) a + b;
    return r >= kInf ? kInf : (int) r;
  }

  private static int satMul(int a, int b) {
    if (a == 0 || b == 0) {
      return 0;
    }
    if (a == kInf || b == kInf) {
      return kInf;
    }
    long r = (long) a * b;
    return r >= kInf ? kInf : (int) r;
  }

  private Frag concat(Frag a, Frag b) {
    a.tail.next = b.head;
    return new Frag(a.head, b.tail, satAdd(a.min, b.min), satAdd(a.max, b.max));
  }

  private int peek() {
    if (cursor >= pattern.length()) {
      return -1;
    }
    return pattern.charAt(cursor);
  }

  private void skipComments() {
    if ((currentFlags & COMMENTS) == 0) {
      return;
    }
    while (cursor < pattern.length()) {
      char c = pattern.charAt(cursor);
      if (c == ' ' || c == '\t' || c == '\n' || c == '\r' || c == 0x0b || c == '\f') {
        cursor++;
      } else if (c == '#') {
        while (cursor < pattern.length() && pattern.charAt(cursor) != '\n' && pattern.charAt(cursor) != '\r'
            && pattern.charAt(cursor) != 0x85 && pattern.charAt(cursor) != 0x2028
            && pattern.charAt(cursor) != 0x2029) {
          cursor++;
        }
      } else {
        break;
      }
    }
  }

  private Frag expr() {
    List<Frag> alts = new ArrayList<Frag>();
    alts.add(sequence());
    while (peek() == '|') {
      cursor++;
      alts.add(sequence());
    }
    if (alts.size() == 1) {
      return alts.get(0);
    }
    Nodes.BranchConn conn = new Nodes.BranchConn();
    Node[] heads = new Node[alts.size()];
    int min = kInf;
    int max = 0;
    for (int i = 0; i < heads.length; i++) {
      Frag a = alts.get(i);
      a.tail.next = conn;
      heads[i] = a.head;
      min = Math.min(min, a.min);
      max = Math.max(max, a.max);
    }
    return new Frag(new Nodes.Branch(heads, conn), conn, min, max);
  }

  private Frag sequence() {
    Frag result = null;
    for (;;) {
      skipComments();
      int c = peek();
      if (c == -1 || c == '|' || c == ')') {
        break;
      }
      Frag atom = atomWithQuantifier();
      result = result == null ? atom : concat(result, atom);
    }
    return result == null ? empty() : result;
  }

  private boolean isQuantifierStart(int pos) {
    int p = pos;
    if ((currentFlags & COMMENTS) != 0) {
      while (p < pattern.length()) {
        char c = pattern.charAt(p);
        if (c == ' ' || c == '\t' || c == '\n' || c == '\r' || c == 0x0b || c == '\f') {
          p++;
        } else if (c == '#') {
          while (p < pattern.length() && pattern.charAt(p) != '\n' && pattern.charAt(p) != '\r') {
            p++;
          }
        } else {
          break;
        }
      }
    }
    if (p >= pattern.length()) {
      return false;
    }
    char c = pattern.charAt(p);
    return c == '*' || c == '+' || c == '?' || c == '{';
  }

  private Frag atomWithQuantifier() {
    Frag atom = atom();
    skipComments();
    int c = peek();
    if (c != '*' && c != '+' && c != '?' && c != '{') {
      return atom;
    }
    if (c == '{' && !looksLikeRepetition()) {
      throw errorAt("Illegal repetition", cursor + 1);
    }
    int min;
    int max;
    cursor++;
    if (c == '*') {
      min = 0;
      max = kInf;
    } else if (c == '+') {
      min = 1;
      max = kInf;
    } else if (c == '?') {
      min = 0;
      max = 1;
    } else {
      int start = cursor;
      min = readInt();
      if (min < 0) {
        throw errorAt("Illegal repetition", start);
      }
      max = min;
      if (peek() == ',') {
        cursor++;
        if (peek() == '}') {
          max = kInf;
        } else if (peek() == -1) {
          throw errorAt("Unclosed counted closure", cursor);
        } else {
          max = readInt();
          if (max < 0) {
            throw errorAt("Illegal repetition", start);
          }
        }
      }
      if (peek() != '}') {
        throw errorAt("Unclosed counted closure", cursor);
      }
      cursor++;
      if (max < min) {
        throw errorAt("Illegal repetition range", cursor - 1);
      }
    }
    int type = Nodes.kGreedy;
    if (peek() == '?') {
      cursor++;
      type = Nodes.kLazy;
    } else if (peek() == '+') {
      cursor++;
      type = Nodes.kPossessive;
    }
    return quantify(atom, min, max, type);
  }

  private boolean looksLikeRepetition() {
    int p = cursor + 1;
    int digits = 0;
    while (p < pattern.length() && Character.isDigit(pattern.charAt(p))) {
      p++;
      digits++;
    }
    return digits > 0;
  }

  private int readInt() {
    int start = cursor;
    long v = 0;
    while (cursor < pattern.length() && pattern.charAt(cursor) >= '0' && pattern.charAt(cursor) <= '9') {
      v = v * 10 + (pattern.charAt(cursor) - '0');
      if (v > Integer.MAX_VALUE) {
        throw errorAt("Illegal repetition range", cursor);
      }
      cursor++;
    }
    return cursor == start ? -1 : (int) v;
  }

  private Frag quantify(Frag atom, int min, int max, int type) {
    if (atom.single != null) {
      Node n = new Nodes.RepeatChar(atom.single, min, max, type);
      return new Frag(n, n, min, satMul(max, 2));
    }
    if (min == 1 && max == 1) {
      return atom;
    }
    int ci = localCount++;
    int bi = localCount++;
    Nodes.Loop loop = new Nodes.Loop(min, max, type == Nodes.kPossessive ? Nodes.kGreedy : type, ci, bi);
    atom.tail.next = loop;
    loop.body = atom.head;
    Nodes.LoopEntry entry = new Nodes.LoopEntry(loop);
    Frag result = new Frag(entry, loop, satMul(atom.min, min), satMul(atom.max, max));
    if (type == Nodes.kPossessive) {
      loop.next = new Nodes.SubEnd();
      Nodes.Atomic at = new Nodes.Atomic(entry);
      return new Frag(at, at, result.min, result.max);
    }
    return result;
  }

  private boolean ci() {
    return (currentFlags & CASE_INSENSITIVE) != 0;
  }

  private boolean ucase() {
    return (currentFlags & UNICODE_CASE) != 0;
  }

  private boolean uclass() {
    return (currentFlags & UNICODE_CHARACTER_CLASS) != 0;
  }

  private CharPredicate caseless(final CharPredicate p) {
    if (!ci()) {
      return p;
    }
    final boolean uni = ucase();
    return new CharPredicate() {
      @Override
      public boolean is(int ch) {
        if (p.is(ch)) {
          return true;
        }
        if (ch < 128 || uni) {
          int lo = ch < 128 ? (ch >= 'A' && ch <= 'Z' ? ch + 32 : ch) : Character.toLowerCase(ch);
          int up = ch < 128 ? (ch >= 'a' && ch <= 'z' ? ch - 32 : ch) : Character.toUpperCase(ch);
          if (lo != ch && p.is(lo)) {
            return true;
          }
          if (up != ch && p.is(up)) {
            return true;
          }
          if (uni) {
            int lu = Character.toLowerCase(up);
            if (lu != ch && p.is(lu)) {
              return true;
            }
          }
        }
        return false;
      }
    };
  }

  private Frag single(CharPredicate p) {
    Node n = new Nodes.CharNode(p);
    Frag f = new Frag(n, n, 1, 2);
    f.single = p;
    return f;
  }

  private Frag literal(int[] cps) {
    Node n = new Nodes.Slice(cps, ci(), ucase());
    int units = 0;
    for (int c : cps) {
      units += Character.charCount(c);
    }
    return new Frag(n, n, units, units);
  }

  private Frag literalChar(int cp) {
    final int c = cp;
    final boolean fold = ci();
    final boolean uni = ucase();
    CharPredicate p;
    if (fold && (c < 128 ? Character.isLetter(c) : (uni && (Character.toLowerCase(c) != c
        || Character.toUpperCase(c) != c)))) {
      final int folded = Nodes.fold(c, uni);
      p = new CharPredicate() {
        @Override
        public boolean is(int ch) {
          return ch == c || Nodes.fold(ch, uni) == folded;
        }
      };
    } else {
      p = RegexProperties.single(c);
    }
    return single(p);
  }

  private Frag atom() {
    int c = peek();
    switch (c) {
      case '[':
        cursor++;
        return single(parseClass());
      case '\\':
        cursor++;
        return escape();
      case '(':
        cursor++;
        return group();
      case '^':
        cursor++;
        return anchor(new Nodes.Caret((currentFlags & MULTILINE) != 0, (currentFlags & UNIX_LINES) != 0));
      case '$':
        cursor++;
        return anchor(new Nodes.Dollar((currentFlags & MULTILINE) != 0, (currentFlags & UNIX_LINES) != 0));
      case '.': {
        cursor++;
        CharPredicate dot;
        if ((currentFlags & DOTALL) != 0) {
          dot = RegexProperties.kAny;
        } else if ((currentFlags & UNIX_LINES) != 0) {
          dot = RegexProperties.single('\n').negate();
        } else {
          dot = new CharPredicate() {
            @Override
            public boolean is(int ch) {
              return !(ch == '\n' || ch == '\r' || ch == 0x85 || ch == 0x2028 || ch == 0x2029);
            }
          };
        }
        return single(dot);
      }
      case '*':
      case '+':
      case '?':
        cursor++;
        throw error("Dangling meta character '" + (char) c + "'");
      default:
        return literalRun();
    }
  }

  private Frag anchor(Node n) {
    return new Frag(n, n, 0, 0);
  }

  private Frag literalRun() {
    List<Integer> run = new ArrayList<Integer>();
    for (;;) {
      int c = peek();
      if (c == -1 || c == '|' || c == ')' || c == '[' || c == '\\' || c == '(' || c == '^' || c == '$' || c == '.'
          || c == '*' || c == '+' || c == '?') {
        break;
      }
      if ((currentFlags & COMMENTS) != 0 && (c == '#' || c == ' ' || c == '\t' || c == '\n' || c == '\r'
          || c == 0x0b || c == '\f')) {
        break;
      }
      int cp = Character.codePointAt(pattern, cursor);
      int next = cursor + Character.charCount(cp);
      if (isQuantifierStart(next)) {
        if (run.isEmpty()) {
          cursor = next;
          return quantifiable(cp);
        }
        break;
      }
      run.add(cp);
      cursor = next;
    }
    if (run.isEmpty()) {
      return empty();
    }
    if (run.size() == 1) {
      return literalChar(run.get(0));
    }
    int[] cps = new int[run.size()];
    for (int i = 0; i < cps.length; i++) {
      cps[i] = run.get(i);
    }
    return literal(cps);
  }

  private Frag quantifiable(int cp) {
    return literalChar(cp);
  }

  private Frag group() {
    int savedFlags = currentFlags;
    Frag result;
    if (peek() == '?') {
      cursor++;
      int c = peek();
      if (c == -1) {
        throw errorAt("Unknown inline modifier", cursor);
      }
      cursor++;
      switch (c) {
        case ':': {
          Frag inner = expr();
          close();
          result = inner;
          break;
        }
        case '=':
        case '!': {
          Frag inner = expr();
          close();
          inner.tail.next = new Nodes.SubEnd();
          Nodes.Look look = new Nodes.Look(inner.head, c == '=');
          result = new Frag(look, look, 0, 0);
          break;
        }
        case '>': {
          Frag inner = expr();
          close();
          inner.tail.next = new Nodes.SubEnd();
          Nodes.Atomic at = new Nodes.Atomic(inner.head);
          result = new Frag(at, at, inner.min, inner.max);
          break;
        }
        case '<': {
          int d = peek();
          if (d == '=' || d == '!') {
            cursor++;
            Frag inner = expr();
            close();
            inner.tail.next = new Nodes.BehindEnd();
            Nodes.Behind b = new Nodes.Behind(inner.head, d == '=', inner.min, inner.max);
            result = new Frag(b, b, 0, 0);
          } else {
            String name = groupName();
            int gi = capturingGroupCount++;
            if (namedGroups.containsKey(name)) {
              throw errorAt("Named capturing group <" + name + "> is already defined", cursor - 1);
            }
            namedGroups.put(name, gi);
            result = capture(gi);
          }
          break;
        }
        default: {
          cursor--;
          Frag r = flagGroup();
          if (r != null) {
            return r;
          }
          result = null;
          break;
        }
      }
    } else {
      int gi = capturingGroupCount++;
      result = capture(gi);
    }
    currentFlags = savedFlags;
    return result;
  }

  private Frag flagGroup() {
    int on = 0;
    int off = 0;
    boolean negate = false;
    for (;;) {
      int c = peek();
      if (c == -1) {
        throw errorAt("Unknown inline modifier", cursor);
      }
      cursor++;
      int flag;
      switch (c) {
        case 'i':
          flag = CASE_INSENSITIVE;
          break;
        case 'm':
          flag = MULTILINE;
          break;
        case 's':
          flag = DOTALL;
          break;
        case 'd':
          flag = UNIX_LINES;
          break;
        case 'u':
          flag = UNICODE_CASE;
          break;
        case 'c':
          flag = CANON_EQ;
          break;
        case 'x':
          flag = COMMENTS;
          break;
        case 'U':
          flag = UNICODE_CHARACTER_CLASS | UNICODE_CASE;
          break;
        case '-':
          if (negate) {
            throw errorAt("Unknown inline modifier", cursor - 1);
          }
          negate = true;
          continue;
        case ')':
          currentFlags = (currentFlags | on) & ~off;
          return empty();
        case ':': {
          int outer = currentFlags;
          currentFlags = (currentFlags | on) & ~off;
          Frag inner = expr();
          close();
          currentFlags = outer;
          return inner;
        }
        default:
          throw errorAt("Unknown inline modifier", cursor - 1);
      }
      if (negate) {
        off |= flag;
      } else {
        on |= flag;
      }
    }
  }

  private void close() {
    if (peek() != ')') {
      throw errorAt("Unclosed group", pattern.length());
    }
    cursor++;
  }

  private Frag capture(int gi) {
    int li = localCount++;
    Node head = new Nodes.GroupHead(li);
    Node tail = new Nodes.GroupTail(li, gi);
    Frag inner = expr();
    close();
    head.next = inner.head;
    inner.tail.next = tail;
    return new Frag(head, tail, inner.min, inner.max);
  }

  private String groupName() {
    int start = cursor;
    int c = peek();
    if (!((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z'))) {
      throw errorAt("capturing group name does not start with a Latin letter", cursor);
    }
    while (cursor < pattern.length()) {
      char ch = pattern.charAt(cursor);
      if ((ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z') || (ch >= '0' && ch <= '9')) {
        cursor++;
      } else {
        break;
      }
    }
    String name = pattern.substring(start, cursor);
    if (peek() != '>') {
      throw errorAt("named capturing group is missing trailing '>'", cursor);
    }
    cursor++;
    return name;
  }

  private Frag escape() {
    int c = peek();
    if (c == -1) {
      throw errorAt("Unescaped trailing backslash", cursor);
    }
    int start = cursor;
    cursor++;
    switch (c) {
      case 'd':
      case 'D':
      case 'w':
      case 'W':
      case 's':
      case 'S':
      case 'h':
      case 'H':
      case 'v':
      case 'V': {
        CharPredicate p = shortcut((char) c);
        return single(p);
      }
      case 'p':
      case 'P':
        return single(property(c == 'P'));
      case 'b':
        if (peek() == '{') {
          int close = pattern.indexOf('}', cursor);
          if (close < 0) {
            throw errorAt("Unclosed boundary type", pattern.length());
          }
          String type = pattern.substring(cursor + 1, close);
          if (!type.equals("g")) {
            throw errorAt("Unknown boundary type near " + type, cursor);
          }
          cursor = close + 1;
          return anchor(new Nodes.GraphemeBound());
        }
        return anchor(new Nodes.Bound(false, uclass()));
      case 'B':
        return anchor(new Nodes.Bound(true, uclass()));
      case 'A':
        return anchor(new Nodes.Begin());
      case 'Z':
        return anchor(new Nodes.Dollar(false, (currentFlags & UNIX_LINES) != 0));
      case 'z':
        return anchor(new Nodes.AbsEnd());
      case 'G':
        return anchor(new Nodes.LastMatch());
      case 'R': {
        Node n = new Nodes.LineEnding();
        return new Frag(n, n, 1, 2);
      }
      case 'X': {
        Node n = new Nodes.Grapheme();
        return new Frag(n, n, 1, kInf);
      }
      case 'k': {
        if (peek() != '<') {
          throw errorAt("\\k is not followed by '<' for named capturing group", cursor);
        }
        cursor++;
        String name = groupName();
        Integer gi = namedGroups.get(name);
        if (gi == null) {
          throw errorAt("named capturing group <" + name + "> does not exist", cursor - 1);
        }
        return backRef(gi);
      }
      case '1':
      case '2':
      case '3':
      case '4':
      case '5':
      case '6':
      case '7':
      case '8':
      case '9': {
        int ref = c - '0';
        while (peek() >= '0' && peek() <= '9') {
          int next = ref * 10 + (peek() - '0');
          if (capturingGroupCount - 1 < next) {
            break;
          }
          ref = next;
          cursor++;
        }
        return backRef(ref);
      }
      case 'Q': {
        int end = pattern.indexOf("\\E", cursor);
        int stop = end < 0 ? pattern.length() : end;
        String quoted = pattern.substring(cursor, stop);
        cursor = end < 0 ? stop : end + 2;
        if (quoted.isEmpty()) {
          return empty();
        }
        int[] cps = quoted.codePoints().toArray();
        if (isQuantifierStart(cursor) && cps.length > 1) {
          int[] head = Arrays.copyOf(cps, cps.length - 1);
          Frag h = literal(head);
          Frag lastChar = literalChar(cps[cps.length - 1]);
          return concat(h, lastChar);
        }
        return cps.length == 1 ? literalChar(cps[0]) : literal(cps);
      }
      case 'E':
        return empty();
      default: {
        cursor = start;
        int cp = escapedChar(false);
        return literalChar(cp);
      }
    }
  }

  private Frag backRef(int ref) {
    hasGroupRef = true;
    Node n = new Nodes.BackRef(ref, ci(), ucase());
    return new Frag(n, n, 0, kInf);
  }

  private CharPredicate shortcut(char c) {
    boolean u = uclass();
    CharPredicate p;
    switch (Character.toLowerCase(c)) {
      case 'd':
        p = u ? RegexProperties.kUnicodeDigit : RegexProperties.kAsciiDigit;
        break;
      case 'w':
        p = u ? RegexProperties.kUnicodeWord : RegexProperties.kAsciiWord;
        break;
      case 's':
        p = u ? RegexProperties.kUnicodeSpace : RegexProperties.kAsciiSpace;
        break;
      case 'h':
        p = RegexProperties.kHorizontalSpace;
        break;
      default:
        p = RegexProperties.kVerticalSpace;
        break;
    }
    return Character.isUpperCase(c) ? p.negate() : p;
  }

  private CharPredicate property(boolean negate) {
    String name;
    int c = peek();
    if (c == -1) {
      throw errorAt("Illegal character family", cursor);
    }
    if (c == '{') {
      int close = pattern.indexOf('}', cursor + 1);
      if (close < 0) {
        throw errorAt("Unclosed character family", pattern.length());
      }
      name = pattern.substring(cursor + 1, close);
      cursor = close + 1;
      if (name.isEmpty()) {
        throw errorAt("Empty character family", cursor - 1);
      }
    } else {
      name = String.valueOf((char) c);
      cursor++;
    }
    CharPredicate p = RegexProperties.lookup(name, uclass(), pattern, cursor - 1);
    if (ci() && (name.equals("Lower") || name.equals("Upper") || name.equals("Lu") || name.equals("Ll")
        || name.equals("javaLowerCase") || name.equals("javaUpperCase"))) {
      p = caseless(p);
    }
    return negate ? p.negate() : p;
  }

  private int hexDigit(int c) {
    int d = Character.digit(c, 16);
    return d;
  }

  private int escapedChar(boolean inClass) {
    int c = peek();
    cursor++;
    switch (c) {
      case '0': {
        int n = 0;
        int count = 0;
        int max = 3;
        while (count < max && peek() >= '0' && peek() <= '7') {
          int d = peek() - '0';
          if (count == 2 && n > 31) {
            break;
          }
          n = n * 8 + d;
          cursor++;
          count++;
        }
        if (count == 0) {
          throw errorAt("Illegal octal escape sequence", cursor - 1);
        }
        return n;
      }
      case 't':
        return '\t';
      case 'n':
        return '\n';
      case 'r':
        return '\r';
      case 'f':
        return '\f';
      case 'a':
        return 7;
      case 'e':
        return 27;
      case 'c': {
        if (cursor >= pattern.length()) {
          throw errorAt("Illegal control escape sequence", cursor - 1);
        }
        int ch = pattern.charAt(cursor++);
        return ch ^ 64;
      }
      case 'x': {
        if (peek() == '{') {
          int close = pattern.indexOf('}', cursor);
          if (close < 0) {
            throw errorAt("Unclosed hexadecimal escape sequence", cursor);
          }
          String hex = pattern.substring(cursor + 1, close);
          if (hex.isEmpty()) {
            throw errorAt("Illegal hexadecimal escape sequence", cursor);
          }
          long v = 0;
          for (int i = 0; i < hex.length(); i++) {
            int d = hexDigit(hex.charAt(i));
            if (d < 0) {
              throw errorAt("Illegal hexadecimal escape sequence", cursor + 1 + i);
            }
            v = v * 16 + d;
            if (v > 0x10ffff) {
              throw errorAt("Hexadecimal codepoint is too big", cursor + 1 + i);
            }
          }
          cursor = close + 1;
          return (int) v;
        }
        if (cursor + 2 > pattern.length() || hexDigit(pattern.charAt(cursor)) < 0
            || hexDigit(pattern.charAt(cursor + 1)) < 0) {
          throw errorAt("Illegal hexadecimal escape sequence", cursor < pattern.length() ? cursor : cursor - 1);
        }
        int v = hexDigit(pattern.charAt(cursor)) * 16 + hexDigit(pattern.charAt(cursor + 1));
        cursor += 2;
        return v;
      }
      case 'u': {
        int v = fourHex();
        if (Character.isHighSurrogate((char) v) && pattern.startsWith("\\u", cursor)) {
          int save = cursor;
          cursor += 2;
          int lo = fourHex();
          if (Character.isLowSurrogate((char) lo)) {
            return Character.toCodePoint((char) v, (char) lo);
          }
          cursor = save;
        }
        return v;
      }
      case 'N': {
        if (peek() != '{') {
          throw errorAt("Illegal character name", cursor);
        }
        int close = pattern.indexOf('}', cursor);
        if (close < 0) {
          throw errorAt("Unmatched { in character name", pattern.length());
        }
        String name = pattern.substring(cursor + 1, close);
        throw errorAt("Unknown character name [" + name + "]", close);
      }
      default:
        if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')) {
          throw errorAt("Illegal/unsupported escape sequence", cursor - 1);
        }
        if (c >= '1' && c <= '9' && inClass) {
          throw errorAt("Illegal/unsupported escape sequence", cursor - 1);
        }
        cursor--;
        int cp = Character.codePointAt(pattern, cursor);
        cursor += Character.charCount(cp);
        return cp;
    }
  }

  private int fourHex() {
    if (cursor + 4 > pattern.length()) {
      throw errorAt("Illegal Unicode escape sequence", pattern.length());
    }
    int v = 0;
    for (int i = 0; i < 4; i++) {
      int d = hexDigit(pattern.charAt(cursor + i));
      if (d < 0) {
        throw errorAt("Illegal Unicode escape sequence", cursor + i);
      }
      v = v * 16 + d;
    }
    cursor += 4;
    return v;
  }

  private CharPredicate parseClass() {
    boolean negate = false;
    if (peek() == '^') {
      negate = true;
      cursor++;
    }
    CharPredicate union = null;
    CharPredicate intersected = null;
    boolean haveItem = false;
    for (;;) {
      skipComments();
      int c = peek();
      if (c == -1) {
        throw errorAt("Unclosed character class", pattern.length() - 1);
      }
      if (c == ']' && haveItem) {
        cursor++;
        break;
      }
      CharPredicate item = null;
      if (c == '[') {
        cursor++;
        item = parseClass();
        haveItem = true;
      } else if (c == '&' && cursor + 1 < pattern.length() && pattern.charAt(cursor + 1) == '&') {
        cursor += 2;
        CharPredicate left = union == null ? RegexProperties.kAny : union;
        intersected = intersected == null ? left : intersected.and(left);
        union = null;
        haveItem = true;
        continue;
      } else {
        item = classAtom();
        haveItem = true;
      }
      union = union == null ? item : union.union(item);
    }
    CharPredicate result;
    if (intersected != null) {
      result = intersected.and(union == null ? RegexProperties.kAny : union);
    } else {
      result = union == null ? RegexProperties.kAny.negate() : union;
    }
    result = caseless(result);
    return negate ? result.negate() : result;
  }

  private CharPredicate classAtom() {
    int c = peek();
    int first;
    if (c == '\\') {
      cursor++;
      int e = peek();
      if (e == -1) {
        throw errorAt("Unclosed character class", pattern.length() - 1);
      }
      switch (e) {
        case 'd':
        case 'D':
        case 'w':
        case 'W':
        case 's':
        case 'S':
        case 'h':
        case 'H':
        case 'v':
        case 'V':
          cursor++;
          return shortcut((char) e);
        case 'p':
        case 'P':
          cursor++;
          return property(e == 'P');
        case 'Q': {
          cursor++;
          int end = pattern.indexOf("\\E", cursor);
          int stop = end < 0 ? pattern.length() : end;
          String quoted = pattern.substring(cursor, stop);
          cursor = end < 0 ? stop : end + 2;
          CharPredicate p = null;
          for (int cp : quoted.codePoints().toArray()) {
            CharPredicate one = RegexProperties.single(cp);
            p = p == null ? one : p.union(one);
          }
          return p == null ? RegexProperties.kAny.negate() : p;
        }
        case 'E':
          cursor++;
          return RegexProperties.kAny.negate();
        case 'b':
        case 'B':
        case 'A':
        case 'Z':
        case 'z':
        case 'G':
        case 'R':
        case 'X':
        case 'k':
          throw errorAt("Illegal/unsupported escape sequence", cursor);
        default:
          first = escapedChar(true);
          break;
      }
    } else {
      first = Character.codePointAt(pattern, cursor);
      cursor += Character.charCount(first);
    }
    skipComments();
    if (peek() == '-' && cursor + 1 < pattern.length() && pattern.charAt(cursor + 1) != ']') {
      int save = cursor;
      cursor++;
      skipComments();
      int d = peek();
      if (d == '[') {
        cursor = save;
        return RegexProperties.single(first);
      }
      int second;
      if (d == '\\') {
        cursor++;
        int e = peek();
        if (e == 'd' || e == 'D' || e == 'w' || e == 'W' || e == 's' || e == 'S' || e == 'p' || e == 'P'
            || e == 'h' || e == 'H' || e == 'v' || e == 'V') {
          throw errorAt("Illegal character range", cursor);
        }
        second = escapedChar(true);
      } else if (d == -1) {
        throw errorAt("Unclosed character class", pattern.length() - 1);
      } else {
        second = Character.codePointAt(pattern, cursor);
        cursor += Character.charCount(second);
      }
      if (second < first) {
        throw errorAt("Illegal character range", cursor - 1);
      }
      return RegexProperties.range(first, second);
    }
    return RegexProperties.single(first);
  }

  int groupCountInternal() {
    return capturingGroupCount - 1;
  }

  Map<String, Integer> namedGroupsInternal() {
    return namedGroups;
  }
}
