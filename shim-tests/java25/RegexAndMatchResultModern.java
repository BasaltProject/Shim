/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

// Minimum release: 17

import java.util.List;
import java.util.Locale;
import java.util.function.Function;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;

public class RegexAndMatchResultModern {

  public static void main(String[] args) throws Exception {
    testPatternQuote();
    testPatternPredicates();
    testPatternSplitAsStream();
    testMatcherAppendReplacement();
    testMatcherFunctionalReplacements();
    testMatcherResultsStream();
    testPatternSyntaxException();
    System.out.println("RegexAndMatchResultModern passed successfully");
  }

  private static void testPatternQuote() {
    String literal = "special (test) [brackets] {braces} *+?^$";
    String quoted = Pattern.quote(literal);
    System.out.println("quoted starts with \\Q: " + quoted.startsWith("\\Q"));
    System.out.println("quoted ends with \\E: " + quoted.endsWith("\\E"));

    Pattern p = Pattern.compile(quoted);
    Matcher m = p.matcher(literal);
    System.out.println("matches literal: " + m.matches());

    String withEmbeddedE = "a\\Eb";
    String quotedE = Pattern.quote(withEmbeddedE);
    System.out.println("quoted with \\E matches: " + Pattern.compile(quotedE).matcher(withEmbeddedE).matches());
  }

  private static void testPatternPredicates() {
    Pattern digitPattern = Pattern.compile("\\d+");

    var findPred = digitPattern.asPredicate();
    System.out.println("findPred 'hello123world': " + findPred.test("hello123world"));
    System.out.println("findPred 'helloworld': " + findPred.test("helloworld"));

    var matchPred = digitPattern.asMatchPredicate();
    System.out.println("matchPred '12345': " + matchPred.test("12345"));
    System.out.println("matchPred '123abc456': " + matchPred.test("123abc456"));
  }

  private static void testPatternSplitAsStream() {
    Pattern splitPattern = Pattern.compile(",");
    List<String> items = splitPattern.splitAsStream("apple,banana,orange,grape")
        .collect(Collectors.toList());
    System.out.println("split items count: " + items.size());
    System.out.println("first item: " + items.get(0));
    System.out.println("last item: " + items.get(items.size() - 1));
  }

  private static void testMatcherAppendReplacement() {
    Pattern p = Pattern.compile("\\d+");
    Matcher m = p.matcher("cat: 12, dog: 34, bird: 56");

    StringBuilder sb = new StringBuilder();
    while (m.find()) {
      m.appendReplacement(sb, "#num");
    }
    m.appendTail(sb);
    System.out.println("appendReplacement result: " + sb.toString());
  }

  private static void testMatcherFunctionalReplacements() {
    Pattern p = Pattern.compile("\\b[a-z]+\\b");
    Matcher m = p.matcher("hello world from basalt");

    String replacedAll = m.replaceAll((Function<MatchResult, String>) mr -> mr.group().toUpperCase(Locale.ROOT));
    System.out.println("replaceAll function: " + replacedAll);

    m.reset("first second third");
    String replacedFirst = m.replaceFirst((Function<MatchResult, String>) mr -> "[" + mr.group() + "]");
    System.out.println("replaceFirst function: " + replacedFirst);
  }

  private static void testMatcherResultsStream() {
    Pattern p = Pattern.compile("\\d+");
    Matcher m = p.matcher("values: 10, 20, 30, 40");

    List<String> matchedGroups = m.results()
        .map(MatchResult::group)
        .collect(Collectors.toList());
    System.out.println("results count: " + matchedGroups.size());
    System.out.println("results joined: " + String.join("-", matchedGroups));

    m.reset("123");
    if (m.find()) {
      MatchResult snapshot = m.toMatchResult();
      System.out.println("snapshot start: " + snapshot.start());
      System.out.println("snapshot end: " + snapshot.end());
      System.out.println("snapshot group: " + snapshot.group());
    }
  }

  private static void testPatternSyntaxException() {
    PatternSyntaxException pse = new PatternSyntaxException("Unclosed group", "(abc", 4);
    System.out.println("pse desc: " + pse.getDescription());
    System.out.println("pse index: " + pse.getIndex());
    System.out.println("pse pattern: " + pse.getPattern());
    System.out.println("pse msg not null: " + (pse.getMessage() != null));
  }
}
