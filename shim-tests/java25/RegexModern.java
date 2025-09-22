/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 * Minimum release: 21
 */

import java.util.*;
import java.util.function.Supplier;
import java.util.regex.*;
import java.util.stream.*;

public class RegexModern {
  static void probe(String name, Supplier<Object> body) {
    String out;
    try {
      out = String.valueOf(body.get());
    } catch (Throwable t) {
      out = "!" + t.getClass().getName() + ":" + t.getMessage();
    }
    System.out.println(name + "=" + out);
  }

  static String all(String regex, int flags, String input) {
    Matcher m = Pattern.compile(regex, flags).matcher(input);
    StringBuilder sb = new StringBuilder();
    while (m.find()) {
      sb.append('[').append(m.start()).append(',').append(m.end()).append(':').append(m.group());
      for (int g = 1; g <= m.groupCount(); g++) {
        sb.append('|').append(m.group(g));
      }
      sb.append(']');
    }
    return sb.toString();
  }

  static String whole(String regex, int flags, String input) {
    Matcher m = Pattern.compile(regex, flags).matcher(input);
    StringBuilder sb = new StringBuilder();
    sb.append(m.matches());
    if (m.hasMatch()) {
      for (int g = 0; g <= m.groupCount(); g++) {
        sb.append('|').append(m.group(g));
      }
    }
    return sb.toString();
  }

  public static void main(String[] args) {
    String[][] finds = {
        {"a", "banana"}, {"an", "banana"}, {"a*", "baaac"}, {"a+?", "aaa"}, {"a{2}", "aaaaa"}, {"a{2,}", "aaaaa"}, {"a{1,2}?", "aaa"},
        {"(a)(b)?", "abaa"}, {"(a|ab)(c|bcd)(d*)", "abcd"}, {"(?:ab)+", "ababab"}, {"(ab)*", "ababab"}, {"(a*)*", "aaa"}, {"(a*)+", "aaa"},
        {"(a|b)*c", "ababc"}, {"x*", "abc"}, {"^a", "aaa"}, {"a$", "aaa"}, {"\\bfoo\\b", "foo food foo"}, {"\\Bo", "foo"},
        {"[a-c]+", "abcdabc"}, {"[^a-c]+", "abcdabc"}, {"[a-c&&[^b]]+", "abcdabc"}, {"[a-z&&[def]]+", "abcdefg"}, {"[\\d\\s]+", "ab12 3cd"},
        {"\\w+", "hello, world_1!"}, {"\\W+", "hello, world_1!"}, {"\\d{3}-\\d{4}", "555-1234 and 666-9999"}, {"(\\d+)\\.(\\d+)", "3.14 and 2.718"},
        {"(?<y>\\d{4})-(?<m>\\d{2})", "2024-02 and 2023-12"}, {"(a)\\1", "aa ab"}, {"(?<x>a)\\k<x>", "aa ab"}, {"(?i)abc", "ABC abc AbC"},
        {"(?i:a)b", "Ab AB"}, {"a(?=b)", "ab ac"}, {"a(?!b)", "ab ac"}, {"(?<=a)b", "ab cb"}, {"(?<!a)b", "ab cb"}, {"(?>a+)b", "aaab"},
        {"a++b", "aaab"}, {"a*+a", "aaa"}, {"(?m)^a", "a\na\nb"}, {"(?m)a$", "a\na\nb"}, {"(?s).+", "a\nb"}, {".+", "a\nb"}, {"\\Qa.b\\E", "a.b axb"},
        {"a|b|c", "xabcx"}, {"", "abc"}, {"\\p{Alpha}+", "ab12cd"}, {"\\p{Upper}\\p{Lower}+", "Hello World"}, {"\\p{L}+", "héllo wörld"}, {"\\p{Lu}", "aBcD"},
        {"[\\p{L}&&[^a-z]]+", "abcÉÀdef"}, {"\\p{IsAlphabetic}+", "ab12"}, {"\\p{javaLowerCase}+", "abCD"}, {"\\p{Punct}+", "a,b;c!"},
        {"\\h+", "a \tb"}, {"\\R", "a\r\nb\nc\rd"}, {"\\x41", "ABC"}, {"\\u0041", "ABC"}, {"\\x{1F600}", "a😀b"}, {"\\t", "a\tb"}, {"\\0101", "ABC"},
        {".", "a😀b"}, {"[😀]", "a😀b"}, {"\\p{L}", "a😀b"}, {"(?x) a b # comment\n c", "abc"}, {"[a-zA-Z_][a-zA-Z0-9_]*", "foo bar_1 9x"},
        {"(\\w+)@(\\w+)\\.com", "joe@example.com, ann@test.com"}, {"\\G\\d", "123a45"}, {"(a)|(b)", "ab"}, {"(a)?b", "b ab"}, {"(?:(a)|b)+", "ab"},
        {"((a)|(b))+", "ab"}, {"a{0}", "aaa"}, {"(?=a)", "aa"}, {"\\Z", "a\n"}, {"\\z", "a\n"}, {"a\\Z", "a\n"}, {"$", "a\n"}, {"(?m)$", "a\nb\n"},
        {"[\\w&&[^\\d]]+", "ab12cd"}, {"[a\\-z]+", "a-z"}, {"[-a]+", "a-b"}, {"[a-]+", "a-b"}, {"[]a]+", "]a"}, {"[\\]]+", "]]"}, {"[\\[]+", "[["},
        {"\\.\\*\\+", ".*+"}, {"[.]", "a.b"}, {"(?U)\\w+", "héllo"}, {"\\w+", "héllo"}, {"(?u)é", "É é"}, {"(?i)é", "É é"}, {"[^a]", "ab"},
        {"a.c", "abc a\nc"}, {"(?d)a.c", "abc a\rc"}, {"\\s+", " a \t b "}, {"(?i)[a-c]+", "ABCabc"}, {"(?i)\\p{Lower}+", "abcDEF"}};
    for (int i = 0; i < finds.length; i++) {
      final int k = i;
      probe("find[" + finds[i][0].replace("\n", "\\n") + "][" + finds[i][1].replace("\n", "\\n").replace("\r", "\\r") + "]", () -> all(finds[k][0], 0, finds[k][1]));
    }
    String[][] matchesData = {
        {"a*", "aaa"}, {"a*", "aab"}, {"a+", ""}, {"(a)(b)(c)", "abc"}, {"(a|ab)(c|bcd)(d*)", "abcd"}, {"(a*)(a*)", "aaa"}, {"(a*?)(a*)", "aaa"}, {"(a+)(a+)", "aaaa"},
        {"(\\d+)-(\\d+)", "10-20"}, {"[a-z]+\\d?", "abc1"}, {"(?<first>\\w+) (?<last>\\w+)", "John Smith"}, {"a|ab", "ab"}, {"(a|ab)", "ab"}, {"a.b", "a\nb"},
        {"(?s)a.b", "a\nb"}, {"\\d+", "０１２"}, {"(?U)\\d+", "０１２"}, {"x?", ""}, {"(x)?", ""}, {".*", "abc\n"}, {".*\\n", "abc\n"}, {"abc$", "abc\n"}, {"abc\\Z", "abc\n"},
        {"(a)|b", "b"}, {"(?:a|(b))*", "ab"}, {"^$", ""}, {"(?i)hello", "HeLLo"}, {"(a(b(c)))", "abc"}, {"((((((((((a))))))))))\\10", "aa"}, {"a{2,3}", "aaaa"}, {"[a-c]{2}", "ab"}};
    for (int i = 0; i < matchesData.length; i++) {
      final int k = i;
      probe("matches[" + matchesData[i][0].replace("\n", "\\n") + "][" + matchesData[i][1].replace("\n", "\\n") + "]", () -> whole(matchesData[k][0], 0, matchesData[k][1]));
    }
    String[] bad = {"(", ")", "[", "[a", "a**", "*", "+a", "?", "a{", "a{1", "a{1,", "a{2,1}", "{1}", "\\", "(?", "(?<", "(?<n", "(?<n>", "(?z)", "(?<1a>x)", "\\k<n>", "(?<n>a)(?<n>b)",
        "\\p{Foo}", "\\p{", "\\pX", "[z-a]", "[a-\\d]", "(?<=a*)b", "\\xZZ", "\\u12", "\\x{110000}", "\\c", "\\8", "\\Q", "(?i", "a)", "[\\p{L}", "\\N{x}", "(?-)", "(?i-i)", "\\b{g}", "a{99999999999}", "[[a]", "[a&&", "\\ ", "\\q", "[\\b]"};
    for (String b : bad) {
      probe("bad[" + b + "]", () -> { try { Pattern.compile(b); return "ok"; } catch (PatternSyntaxException e) { return e.getDescription() + "@" + e.getIndex(); } });
    }
    probe("bad-message", () -> { try { Pattern.compile("a(b"); return "ok"; } catch (PatternSyntaxException e) { return e.getMessage().replace("\n", "|"); } });
    probe("replace", () -> "hello world".replaceAll("o", "0") + " " + "hello world".replaceFirst("o", "0") + " " + "a1b22c333".replaceAll("\\d+", "<$0>") + " " + "john smith".replaceAll("(\\w+) (\\w+)", "$2, $1") + " " + "aaa".replaceAll("a*", "X") + " " + "abc".replaceAll("", "-") + " " + "a.b".replaceAll("\\.", "\\\\") + " " + "x".replaceAll("x", "\\$") + " " + "ab".replaceAll("(?<f>a)", "${f}${f}"));
    probe("replace-bad", () -> "a".replaceAll("a", "$1"));
    probe("replace-bad2", () -> "a".replaceAll("(a)", "$2"));
    probe("replace-bad3", () -> "a".replaceAll("a", "\\"));
    probe("replace-bad4", () -> "a".replaceAll("a", "$"));
    probe("replace-bad5", () -> "a".replaceAll("(?<n>a)", "${m}"));
    probe("replace-bad6", () -> "a".replaceAll("(?<n>a)", "${n"));
    probe("replace-fn", () -> Pattern.compile("\\d+").matcher("a1b22c333").replaceAll(r -> "<" + r.group().length() + ">") + " " + Pattern.compile("\\d+").matcher("a1b22c333").replaceFirst(r -> "#"));
    probe("quote", () -> Pattern.quote("a.b") + " " + Pattern.quote("a\\Eb") + " " + "a.b".matches(Pattern.quote("a.b")) + " " + "axb".matches(Pattern.quote("a.b")) + " " + Matcher.quoteReplacement("a$b\\c"));
    String[][] splits = {{",", "a,b,,c,,"}, {",", ""}, {",", ","}, {",", ",a"}, {"", "abc"}, {"\\s*", "a b  c"}, {"(?=b)", "abab"}, {"(,)", "a,b"}, {"b", "abc"}, {"x", "abc"}, {"\\d", "a1b2c3"}, {"a*", "baab"}, {"(?<=,)", "a,b,c"}};
    for (String[] sp : splits) {
      for (int limit : new int[] {0, -1, 2}) {
        probe("split[" + sp[0] + "][" + sp[1] + "][" + limit + "]", () -> Arrays.toString(Pattern.compile(sp[0]).split(sp[1], limit)) + Arrays.toString(sp[1].split(sp[0], limit)));
      }
    }
    probe("split-delim", () -> Arrays.toString(Pattern.compile("[,;]").splitWithDelimiters("a,b;c", 0)) + Arrays.toString("a,b;c".splitWithDelimiters("[,;]", 2)) + Pattern.compile(",").splitAsStream("a,b,c").collect(Collectors.toList()));
    probe("flags", () -> Pattern.compile("a", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE).flags() + " " + Pattern.compile("a", Pattern.UNICODE_CHARACTER_CLASS).flags() + " " + Pattern.compile("a").flags());
    probe("flags-literal", () -> Pattern.compile("a.b*", Pattern.LITERAL).matcher("a.b*").matches() + " " + Pattern.compile("A.B", Pattern.LITERAL | Pattern.CASE_INSENSITIVE).matcher("a.b").matches());
    probe("flags-bad", () -> Pattern.compile("a", 0x1000));
    probe("flags-ci-unicode", () -> Pattern.compile("é", Pattern.CASE_INSENSITIVE).matcher("É").matches() + " " + Pattern.compile("é", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE).matcher("É").matches() + " " + Pattern.compile("[à-ÿ]", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE).matcher("É").matches());
    probe("flags-comments", () -> Pattern.compile("a b # x\n c", Pattern.COMMENTS).matcher("abc").matches() + " " + Pattern.compile("[a b]+", Pattern.COMMENTS).matcher("ab").matches() + " " + Pattern.compile("a\\ b", Pattern.COMMENTS).matcher("a b").matches());
    probe("flags-dotall", () -> Pattern.compile("a.b", Pattern.DOTALL).matcher("a\nb").matches() + " " + Pattern.compile("a.b", Pattern.UNIX_LINES).matcher("a\rb").matches() + " " + Pattern.compile("a.b", Pattern.UNIX_LINES).matcher("a\nb").matches());
    probe("groups", () -> { Matcher m = Pattern.compile("(?<a>x)(y)?(?<b>z)").matcher("xz"); m.matches(); return m.groupCount() + " " + m.group("a") + " " + m.group(2) + " " + m.group("b") + " " + m.start("b") + " " + m.end("b") + " " + m.start(2) + " " + new TreeMap<>(m.namedGroups()) + " " + new TreeMap<>(m.pattern().namedGroups()); });
    probe("groups-bad", () -> { Matcher m = Pattern.compile("(a)").matcher("a"); m.matches(); return m.group(2); });
    probe("groups-bad2", () -> { Matcher m = Pattern.compile("(a)").matcher("a"); return m.group(); });
    probe("groups-bad3", () -> { Matcher m = Pattern.compile("(a)").matcher("a"); m.matches(); return m.group("nope"); });
    probe("groups-bad4", () -> { Matcher m = Pattern.compile("(a)").matcher("b"); m.matches(); return m.start(); });
    probe("groups-bad5", () -> { Matcher m = Pattern.compile("(a)").matcher("a"); m.matches(); return m.start(-1); });
    probe("groups-bad6", () -> { Matcher m = Pattern.compile("(a)").matcher("a"); return m.group(null); });
    probe("matcher-state", () -> { Matcher m = Pattern.compile("a").matcher("aXa"); StringBuilder sb = new StringBuilder(); sb.append(m.find()).append(m.start()).append(m.find()).append(m.start()).append(m.find()); sb.append(m.hasMatch()).append(m.reset().find()).append(m.start()); sb.append(m.find(2)).append(m.start()); return sb; });
    probe("find-bad", () -> Pattern.compile("a").matcher("a").find(5));
    probe("looking-at", () -> { Matcher m = Pattern.compile("ab").matcher("abc"); return m.lookingAt() + " " + m.matches() + " " + m.group() + " " + Pattern.compile("b").matcher("abc").lookingAt(); });
    probe("region", () -> { Matcher m = Pattern.compile("^b").matcher("abc"); m.region(1, 3); boolean a = m.find(); m.useAnchoringBounds(false); m.region(1, 3); boolean b = m.find(); return a + " " + b + " " + m.regionStart() + " " + m.regionEnd() + " " + m.hasAnchoringBounds() + " " + m.hasTransparentBounds(); });
    probe("region-transparent", () -> { Matcher m = Pattern.compile("(?<=a)b").matcher("ab"); m.region(1, 2); boolean a = m.find(); m.useTransparentBounds(true); m.region(1, 2); boolean b = m.find(); Matcher n = Pattern.compile("a(?=b)").matcher("ab"); n.region(0, 1); boolean c = n.find(); n.useTransparentBounds(true); n.region(0, 1); boolean d = n.find(); return a + " " + b + " " + c + " " + d; });
    probe("region-bad", () -> Pattern.compile("a").matcher("abc").region(2, 1));
    probe("hit-end", () -> { Matcher m = Pattern.compile("abc").matcher("ab"); m.matches(); boolean a = m.hitEnd(); m = Pattern.compile("abc").matcher("abd"); m.matches(); boolean b = m.hitEnd(); m = Pattern.compile("a+").matcher("aaa"); m.lookingAt(); boolean c = m.hitEnd(); m = Pattern.compile("a$").matcher("a"); m.find(); boolean d = m.requireEnd(); return a + " " + b + " " + c + " " + d; });
    probe("use-pattern", () -> { Matcher m = Pattern.compile("a").matcher("abab"); m.find(); m.usePattern(Pattern.compile("b")); return m.find() + " " + m.start() + " " + m.group(); });
    probe("results", () -> Pattern.compile("\\d+").matcher("a1b22c333").results().map(MatchResult::group).collect(Collectors.toList()) + " " + Pattern.compile("(\\d)(\\w)").matcher("1a2b").results().map(r -> r.group(2) + r.start()).collect(Collectors.joining(",")));
    probe("to-match-result", () -> { Matcher m = Pattern.compile("(a)(b)?").matcher("ac"); m.find(); MatchResult r = m.toMatchResult(); m.reset(); return r.group() + r.groupCount() + r.group(2) + r.start(1) + r.end() + r.hasMatch(); });
    probe("append", () -> { Matcher m = Pattern.compile("a(\\d)").matcher("a1-a2-a3"); StringBuilder sb = new StringBuilder(); while (m.find()) { m.appendReplacement(sb, "<$1>"); } m.appendTail(sb); StringBuffer sf = new StringBuffer(); m.reset(); while (m.find()) { m.appendReplacement(sf, "[" + m.group(1) + "]"); } m.appendTail(sf); return sb + " " + sf; });
    probe("append-bad", () -> Pattern.compile("a").matcher("a").appendReplacement(new StringBuilder(), "x"));
    probe("tostring", () -> Pattern.compile("a(b)").toString() + " " + Pattern.compile("a(b)").pattern() + " " + Pattern.compile("a").matcher("xa").find() + " " + Pattern.compile("a").matcher("a"));
    probe("tostring-found", () -> { Matcher m = Pattern.compile("a+").matcher("xaay"); m.find(); return m.toString(); });
    probe("predicates", () -> Stream.of("apple", "banana", "cherry").filter(Pattern.compile("an").asPredicate()).collect(Collectors.toList()) + " " + Stream.of("apple", "banana", "cherry").filter(Pattern.compile("a.*").asMatchPredicate()).collect(Collectors.toList()));
    probe("string-api", () -> "a1b2".matches("[a-z0-9]+") + " " + "abc".matches("ab") + " " + Arrays.toString("a:b:c".split(":", 2)) + " " + "a-b".replaceAll("-", "+") + " " + "x".repeat(3).replaceAll("x", "yy"));
    probe("long-input", () -> { StringBuilder sb = new StringBuilder(); for (int i = 0; i < 400; i++) sb.append('a'); return Pattern.compile("a*b").matcher(sb).find() + " " + Pattern.compile("(a|b)*").matcher(sb).matches() + " " + Pattern.compile(".*").matcher(sb).matches() + " " + Pattern.compile("(?:a)+").matcher(sb).matches(); });
    probe("catastrophic-safe", () -> Pattern.compile("(a+)+b").matcher("aaaaaaaaaaaa").find());
    probe("unicode-props", () -> all("\\p{Lu}\\p{Ll}+", 0, "Élan Über straße") + " " + all("\\p{Sc}", 0, "$5 €6 ¥7") + " " + all("\\p{Nd}+", 0, "a12b٣٤c") + " " + all("\\P{L}+", 0, "ab12cd") + " " + all("\\p{IsLetter}", 0, "a1") + " " + all("\\p{gc=Lu}", 0, "aB") + " " + all("\\p{general_category=Nd}", 0, "a1"));
    probe("lookbehind-var", () -> all("(?<=a|bc)d", 0, "ad bcd cd") + " " + all("(?<=\\d{2,3})x", 0, "1x 12x 123x") + " " + all("(?<![a-z])\\d", 0, "a1 2"));
    probe("backtracking", () -> all("(a+)(b+)?\\2", 0, "aabb aab") + " " + all("(\\w+)\\s\\1", 0, "the the cat cat dog") + " " + all("^(.*)(\\d+)$", 0, "abc123"));
    probe("empty-matches", () -> all("", 0, "ab") + all("\\b", 0, "ab cd") + all("(?m)^", 0, "a\nb") + all("a*?", 0, "aa"));
  }
}
