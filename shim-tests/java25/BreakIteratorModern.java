/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 * Minimum release: 21
 */

import java.text.*;
import java.util.*;

public class BreakIteratorModern {
static class A {
  static String bounds(BreakIterator b, String s) {
    b.setText(s); StringBuilder sb = new StringBuilder();
    for (int i = b.first(); i != BreakIterator.DONE; i = b.next()) sb.append(i).append(' ');
    return sb.toString().trim();
  }
  public static void run() {
    String[] texts = {"", "a", "Hello, world! How are you? I'm fine.", "The quick  brown fox's tail, 3.14 and 1,000 items; e-mail: a.b@c.com... ok",
      "e\u0301a\u0300b", "\r\nab\n\rc", "\ud83d\ude00x\ud83d\ude00", "\uac00\u1100\u1161\u11a8x", "Mr. Smith went to Washington. He said \"Hi.\" Then left! Really? yes\u2026 Done.",
      "foo-bar baz_qux 123abc abc123 a.b a. b", "line one\nline two\r\nline three", "A  B\tC", "don't stop-believing (now) [ok] {x}", "\u3053\u3093\u306b\u3061\u306f\u4e16\u754c", "caf\u00e9 na\u00efve \u00fcber", "   leading and trailing   ", "1.5% $3,000.50 #tag @user", "end. Next sentence? Yes! lower case. 9 Dig.", "a\u200db \u0905\u0928\u0941 \u0e01\u0e32"};
    BreakIterator[] its = {BreakIterator.getCharacterInstance(Locale.ENGLISH), BreakIterator.getWordInstance(Locale.ENGLISH), BreakIterator.getSentenceInstance(Locale.ENGLISH), BreakIterator.getLineInstance(Locale.ENGLISH)};
    String[] names = {"char", "word", "sent", "line"};
    for (String t : texts) {
      System.out.println("TEXT " + t.replace("\n", "\\n").replace("\r", "\\r"));
      for (int i = 0; i < 4; i++) System.out.println("  " + names[i] + ": " + bounds(its[i], t));
    }
    BreakIterator w = BreakIterator.getWordInstance();
    String s = "Hello big world.";
    w.setText(s);
    System.out.println(w.first() + " " + w.last() + " " + w.current() + " " + w.previous() + " " + w.preceding(8) + " " + w.following(8) + " " + w.isBoundary(5) + " " + w.isBoundary(6) + " " + w.next(2) + " " + w.next(-1) + " " + w.following(16) + " " + w.preceding(0));
    try { w.following(99); } catch (Exception e) { System.out.println(e); }
    try { w.preceding(-1); } catch (Exception e) { System.out.println(e); }
    try { w.isBoundary(-3); } catch (Exception e) { System.out.println(e); }
    System.out.println(w.getText().getClass().getName() + " " + w.getText().getEndIndex() + " " + w.clone().getClass().getName());
    
  }
  
}

static class B {
  static String bounds(BreakIterator b, String s) {
    b.setText(s); StringBuilder sb = new StringBuilder();
    for (int i = b.first(); i != BreakIterator.DONE; i = b.next()) sb.append(i).append(' ');
    return sb.toString().trim();
  }
  static String esc(String s) { StringBuilder b = new StringBuilder(); for (char c : s.toCharArray()) { if (c < 32 || c > 126) b.append(String.format("\\u%04x", (int) c)); else b.append(c); } return b.toString(); }
  public static void run() throws Exception {
    java.util.List<String> cases = new java.util.ArrayList<>(java.util.List.of("a:b","a;b","a,b","a-b","a_b","a'b","a\u2019b","a.b","1.a","a.1","1,a","a,1","1,000,000","1..2","1.2.3","$5","5%","a  \t b","a\u00a0b","a\u3000b","\n\n","a\nb","a\u2028b","\u0301a","\u30ab\u30bf\u30ab\u30ca","\u6f22\u5b57\u304b\u306a","\ud55c\uae00","\u043f\u0440\u0438\u0432\u0435\u0442 \u043c\u0438\u0440","\uff41\uff42\uff43","\u0663\u0664","\"quoted\" 'single'","a\"b","a/b","a\\b","a&b","a+b","a=b","a@b","a#b","a*b","a~b","a`b","a^b","a|b","a<b>c","a!b","a?b","a(b)c","a\u00ad b","a\u2010b","a\u2013b","1-2","1:2","1;2","1/2","1 2","a\u00b7b","ab\ud800","\ud800ab","12ab34","1a1","a1a","x_1","_a","a_","__","a.","a..b","...", "a ... b", "a!!b", "\u00e9\u00e9", "\u0391\u0392", "\u05d0\u05d1", "\u0627\u0644", "\u0e01\u0e02 \u0e01", "fi\ufb01", "\u2160\u2161", "a\u00a0 \u00a0b", "\t\t", " \t ", "a\r\nb", "a\rb"));
    BreakIterator[] its = {BreakIterator.getCharacterInstance(Locale.ENGLISH), BreakIterator.getWordInstance(Locale.ENGLISH), BreakIterator.getSentenceInstance(Locale.ENGLISH), BreakIterator.getLineInstance(Locale.ENGLISH)};
    for (String t : cases) System.out.println(esc(t) + " | c:" + bounds(its[0], t) + " | w:" + bounds(its[1], t) + " | s:" + bounds(its[2], t) + " | l:" + bounds(its[3], t));
    // sentences
    String[] ss = {"A. B", "A.B", "a. b", "A. b", "A! b", "A? b", "A.  B", "A.\nB", "A.\n\nB", "A\nB", "A\n\nB", "A.\"B", "A.\" B", "A.\" b", "A.' B", "A.) B", "A.) b", "A?! B", "A... B", "A\u2026 B", "A\u2026b", "1. 2", "1. A", "A.1", "A. 1", "e.g. this", "U.S. Army", "A.B. C", "A. \u00c9", "A. \u00e9", "A\u3002B", "A\u3002 B", "A. \u0410", "A\u0964 B", "A! \"B\"", "\"A!\" B", "\"A.\" \"B\"", "A:\nB", "A.\tB", "A. B. C.", "   A. B", "A.   ", "A. \n", "A\u2029B", "A\u2028B", "A.\u2029B", "Hello.How are you", "Hi!Ok", "Wait?Ok", "x. Y z. W", "A.B? C", "3.5 or 4. Next"};
    for (String t : ss) System.out.println("S " + esc(t) + " | " + bounds(its[2], t));
    String[] ls = {"a b", "a  b", "a\tb", "a-b", "a--b", "a- b", "a -b", "a/b", "a\\b", "a.b", "a,b", "a, b", "a. b", "(a b)", "(a) b", "\"a b\"", "a \"b c\" d", "a\nb", "a\r\nb", "a\n\nb", "a\rb", "1,000 2.5", "$5 5%", "a\u00a0b", "a\u200bb", "a\u00adb", "a\u2010b", "a\u2013b", "http://a.b/c?d=e", "a_b", "\u3053\u3093", "\u3053\u3093 \u306b", "\u30ab\u30bf", "\u6f22\u5b57", "\u6f22 \u5b57", "a\u6f22b", "\u3042\u3063\u3044", "\u3042\u30fc\u3044", "\u3001\u3002", "a\u3001b", "\u300ca\u300d", "\uff08a\uff09", "a   ", "   a", "a \n b", "ab\u0301c", "a.\u0301", "x(y)z", "x[y]z", "x{y}z", "a;b", "a:b", "a!b", "a?b", "a%b", "a&b", "a+b", "a=b", "a@b", "a#b", "A1 B2", "12 34", "1-2", "1/2", "\u0e01\u0e32 \u0e01", "\ud83d\ude00 \ud83d\ude00", "\ud83d\ude00\ud83d\ude00", "a\ud83d\ude00b", "word,word", "word;word", "\u00e9 \u00e9", "..", "a ..", "a...", "a. . b", "- a", "a -", "-a", "a-", "--", "a - b"};
    for (String t : ls) System.out.println("L " + esc(t) + " | " + bounds(its[3], t));
  }
}

  public static void main(String[] args) throws Exception {
    A.run();
    B.run();
  }
}
