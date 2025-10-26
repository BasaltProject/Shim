/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 * Minimum release: 21
 */

import java.text.*;
import java.util.*;

public class CollatorModern {
static class Default {
  public static void run() throws Exception {
    Random r = new Random(12345);
    String alpha = "aAbBcCeEéÉèêëzZ0159 -_.,'\"!?äöüßæøåñ́̀\t\n xyXYΑαЖж一";
    List<String[]> pairs = new ArrayList<>();
    for (int i = 0; i < 4000; i++) {
      String x = rnd(r, alpha), y = rnd(r, alpha);
      if (r.nextInt(4) == 0) y = x.substring(0, r.nextInt(x.length() + 1)) + rnd(r, alpha);
      pairs.add(new String[] {x, y});
    }
    for (int i = 0; i <= 0x30ff; i += 7) pairs.add(new String[] {String.valueOf((char) i), String.valueOf((char) (i + 5))});
    String[] words = {"cote", "côte", "coté", "côté", "Cote", "résumé", "resume", "Resume", "naïve", "naive", "Zebra", "apple", "Apple", "APPLE", "ápple", "a b", "ab", "a-b", "a_b", "ß", "ss", "straße", "strasse", "æther", "aether", "Ångström", "angstrom", "ñandú", "nandu", "Äpfel", "Apfel", "Öl", "Ol", "Ül", "10", "9", "a1", "a10", "a2", "", " ", "é", "é", "É", "Å", "Å", "Ａ", "A", "Ⅰ", "I"};
    for (String x : words) for (String y : words) pairs.add(new String[] {x, y});
    for (int st : new int[] {Collator.PRIMARY, Collator.SECONDARY, Collator.TERTIARY, Collator.IDENTICAL}) {
      for (int dec : new int[] {Collator.NO_DECOMPOSITION, Collator.CANONICAL_DECOMPOSITION}) {
        Collator c = Collator.getInstance(Locale.ENGLISH); c.setStrength(st); c.setDecomposition(dec);
        StringBuilder sb = new StringBuilder();
        int n = 0;
        System.out.println("== strength " + st + " decomp " + dec);
        for (String[] p : pairs) { int v = c.compare(p[0], p[1]); sb.append(v < 0 ? '-' : v > 0 ? '+' : '0'); if (++n % 100 == 0) { System.out.println(sb); sb.setLength(0); } }
        System.out.println(sb);
      }
    }
    Collator c = Collator.getInstance(Locale.ENGLISH);
    List<String> l = new ArrayList<>(Arrays.asList(words)); l.sort(c); System.out.println(l);
    System.out.println(c.getStrength() + " " + c.getDecomposition() + " " + c.equals("a", "a") + " " + c.equals("a", "A") + " " + c.equals(c.clone()) + " " + c.getClass().getName());
    for (String w : new String[] {"", "a", "ab", "AB", "é", "é", "a b", "ab-"}) System.out.println(Arrays.toString(c.getCollationKey(w).toByteArray()));
    c.setStrength(Collator.PRIMARY); System.out.println(Arrays.toString(c.getCollationKey("Ab").toByteArray()));
    c.setStrength(Collator.SECONDARY); System.out.println(Arrays.toString(c.getCollationKey("Ab").toByteArray()));
    try { c.setStrength(7); } catch (Exception e) { System.out.println(e); }
    try { c.setDecomposition(7); } catch (Exception e) { System.out.println(e); }
    try { c.compare(null, "a"); } catch (Exception e) { System.out.println(e.getClass().getName()); }
    try { c.compare((Object) 5, (Object) "a"); } catch (Exception e) { System.out.println(e.getClass().getName()); }
    RuleBasedCollator rc = (RuleBasedCollator) Collator.getInstance(Locale.ENGLISH);
    CollationElementIterator it = rc.getCollationElementIterator("aé");
    StringBuilder sb = new StringBuilder();
    for (int e = it.next(); e != CollationElementIterator.NULLORDER; e = it.next()) sb.append(CollationElementIterator.primaryOrder(e) > 0).append('/').append(CollationElementIterator.secondaryOrder(e) > 0).append('/').append(it.getOffset()).append(' ');
    System.out.println(sb);
  }
  static String rnd(Random r, String alpha) { int n = r.nextInt(6); StringBuilder b = new StringBuilder(); for (int i = 0; i < n; i++) b.append(alpha.charAt(r.nextInt(alpha.length()))); return b.toString(); }
}

static class Custom {
  static void t(String n, java.util.concurrent.Callable<Object> c) { try { System.out.println(n + ": " + c.call()); } catch (Throwable e) { System.out.println(n + ": !" + e.getClass().getName() + ":" + e.getMessage()); } }
  static String sorted(Collator c, String... xs) { List<String> l = new ArrayList<>(Arrays.asList(xs)); l.sort(c); return l.toString(); }
  public static void run() throws Exception {
    t("r1", () -> sorted(new RuleBasedCollator("< c < b < a"), "a", "b", "c", "d", "A", "e"));
    t("r2", () -> sorted(new RuleBasedCollator("< a, A < b, B"), "B", "b", "A", "a"));
    t("r3", () -> sorted(new RuleBasedCollator("< a ; A < b"), "b", "A", "a"));
    t("r3s", () -> { Collator c = new RuleBasedCollator("< a ; A < b"); c.setStrength(Collator.PRIMARY); return c.compare("a", "A"); });
    t("r4", () -> sorted(new RuleBasedCollator("< ch < a < b"), "b", "ch", "a", "c", "cz", "h"));
    t("r5", () -> sorted(new RuleBasedCollator("< a < b & a < x"), "b", "x", "a"));
    t("r6", () -> sorted(new RuleBasedCollator("< a < b = c"), "c", "b", "a"));
    t("r6e", () -> new RuleBasedCollator("< a < b = c").compare("b", "c"));
    t("r7", () -> sorted(new RuleBasedCollator("< a < b < c & a < æ / e"), "b", "æ", "ae", "af", "a"));
    t("empty", () -> sorted(new RuleBasedCollator(""), "b", "a", "C"));
    t("badrule", () -> new RuleBasedCollator("a < b"));
    t("badrule2", () -> new RuleBasedCollator("< "));
    t("badrule3", () -> new RuleBasedCollator("< a < < b"));
    t("getrules", () -> new RuleBasedCollator("< a < b").getRules());
    t("getrules2", () -> new RuleBasedCollator("< a<b   <c").getRules());
    t("quote", () -> sorted(new RuleBasedCollator("< '-' < a < '<'"), "<", "a", "-"));
    t("ign", () -> { Collator c = new RuleBasedCollator("= '-' < a < b"); return c.compare("a-b", "ab"); });
    t("unmapped", () -> sorted(new RuleBasedCollator("< a < b"), "z", "c", "a", "b", "1", "!"));
    t("space", () -> sorted(new RuleBasedCollator("< a < b"), " ", "a", "\t"));
    t("keys", () -> { Collator c = new RuleBasedCollator("< a < b"); return Arrays.toString(c.getCollationKey("ab").toByteArray()) + " " + c.getCollationKey("ab").getSourceString() + " " + c.getCollationKey("ab").compareTo(c.getCollationKey("ba")); });
    t("keyempty", () -> Arrays.toString(new RuleBasedCollator("< a < b").getCollationKey("").toByteArray()));
    t("keynull", () -> new RuleBasedCollator("< a < b").getCollationKey(null));
    t("cei", () -> { RuleBasedCollator c = new RuleBasedCollator("< a < b ; B < c"); CollationElementIterator i = c.getCollationElementIterator("abBc"); StringBuilder sb = new StringBuilder(); for (int e = i.next(); e != CollationElementIterator.NULLORDER; e = i.next()) sb.append(Integer.toHexString(e)).append(' ').append(CollationElementIterator.primaryOrder(e)).append('/').append(CollationElementIterator.secondaryOrder(e)).append('/').append(CollationElementIterator.tertiaryOrder(e)).append(' '); return sb; });
  }
}

  public static void main(String[] args) throws Exception {
    Default.run();
    Custom.run();
  }
}
