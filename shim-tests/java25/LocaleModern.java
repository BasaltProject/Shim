/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 * Minimum release: 21
 */

import java.util.*;
import java.util.Locale.FilteringMode;
import java.util.Locale.LanguageRange;

public class LocaleModern {
  static String msg(Throwable t) {
    return t instanceof NullPointerException ? "npe" : t.getMessage();
  }

  public static void main(String[] args) {
    Locale.setDefault(Locale.US);
    Locale.setDefault(Locale.Category.DISPLAY, Locale.US);
    Locale.setDefault(Locale.Category.FORMAT, Locale.US);
    Lp.main(null);
    Lg.main(null);
    Lc.main(null);
    Le.main(null);
    LrangeProbe.main(null);
    Ld.main(null);
    Sweep.main(null);
  }

  static class Lp {
  static String d(Locale l) { return "[" + l + "|" + l.getLanguage() + "|" + l.getScript() + "|" + l.getCountry() + "|" + l.getVariant() + "|" + l.toLanguageTag() + "|" + l.getExtensionKeys() + "|" + l.getUnicodeLocaleKeys() + "|" + l.getUnicodeLocaleAttributes() + "|" + l.hashCode() + "]"; }
  public static void main(String[] a) {
    String[] tags = {"en", "en-US", "EN-us", "en_US", "und", "und-US", "zh-Hant-TW", "zh-yue", "zh-yue-HK", "sr-Latn-RS", "de-DE-1996", "de-DE-POSIX", "en-US-posix", "es-419", "en-001", "ja-JP-u-ca-japanese", "th-TH-u-nu-thai", "en-u-co-phonebk-ca-gregory", "en-u-attr1-attr2-ca-gregory", "en-u-kn-true", "en-u-kn", "en-u-ca-gregory-x-priv", "x-private", "en-x-foo-bar", "en-a-bbb-ccc", "en-b-ccc-a-bbb", "i-klingon", "i-navajo", "zh-min-nan", "zh-hakka", "art-lojban", "en-GB-oed", "no-NO-NY", "iw", "he", "ji-IL", "in-ID", "en-US-u-ca-buddhist-x-lvariant-foo", "en-US-x-lvariant-foo", "e", "en-", "-en", "en--US", "en-USA", "en-Latn-US-Latn", "toolongsubtag", "en-12345", "en-1234", "de-1901-1996", "en-abcde-fghij", "en-U-CA-Gregory", "ab-cd", "x", "en-x", "123", "en-US-u", "en-u-", "en-US-a", "aa-bb-cc-dd-ee", "sl-rozaj-biske", "ca-valencia", "en-Latn", "latn", "en-latn-us", "ar-u-nu-arab", "en-u-ca", "en-t-es", "en-US-t-m0-iso", "de-u-co-phonebk-x-a", "fr-FR-u-fw-mon"};
    for (String t : tags) System.out.println("tag " + t + " -> " + d(Locale.forLanguageTag(t)));
    String[][] ctors = {{"EN", "us", "posix"}, {"", "US", ""}, {"", "", "x"}, {"iw", "IL", ""}, {"ji", "", ""}, {"in", "ID", ""}, {"ja", "JP", "JP"}, {"th", "TH", "TH"}, {"no", "NO", "NY"}, {"en", "", "a_b"}, {"en", "usa", ""}, {"en", "", "x-y"}, {"sr", "RS", "Latn"}, {"123", "456", "789"}, {"en", "US", "u"}, {"", "", ""}, {null != null ? "" : "xx", "", "yyyyyyyyy"}};
    for (String[] c : ctors) System.out.println("ctor " + Arrays.toString(c) + " -> " + d(new Locale(c[0], c[1], c[2])));
    Locale.Builder b = new Locale.Builder();
    String[][] sets = {{"language", "en"}, {"language", "e"}, {"language", "english"}, {"language", "123"}, {"language", ""}, {"script", "latn"}, {"script", "la"}, {"region", "us"}, {"region", "840"}, {"region", "usa"}, {"variant", "POSIX"}, {"variant", "posix-1996"}, {"variant", "pos"}, {"variant", "1996"}, {"variant", "a_b"}};
    for (String[] s : sets) {
      try { Locale.Builder bb = new Locale.Builder(); switch (s[0]) { case "language": bb.setLanguage(s[1]); break; case "script": bb.setScript(s[1]); break; case "region": bb.setRegion(s[1]); break; default: bb.setVariant(s[1]); } System.out.println("builder " + s[0] + "=" + s[1] + " -> " + d(bb.build())); } catch (Exception e) { System.out.println("builder " + s[0] + "=" + s[1] + " -> !" + e.getClass().getName() + ":" + msg(e)); }
    }
    for (String t : new String[] {"en-US", "en-", "x-a", "en-US-u-ca-gregory", "i-klingon", "en-us-foo", "en-GB-oed", "ab-c", "123", "en-US-u"}) {
      try { System.out.println("setLanguageTag " + t + " -> " + d(new Locale.Builder().setLanguageTag(t).build())); } catch (Exception e) { System.out.println("setLanguageTag " + t + " -> !" + e.getClass().getName() + ":" + msg(e)); }
    }
    String[][] ext = {{"u", "ca-gregory"}, {"x", "private"}, {"a", "bbb"}, {"a", ""}, {"a", "b"}, {"u", "x"}, {"1", "ab"}, {"ab", "cd"}, {"x", "a-b-c"}};
    for (String[] s : ext) {
      try { System.out.println("ext " + s[0] + "=" + s[1] + " -> " + d(new Locale.Builder().setLanguage("en").setExtension(s[0].charAt(0), s[1]).build())); } catch (Exception e) { System.out.println("ext " + s[0] + "=" + s[1] + " -> !" + e.getClass().getName() + ":" + msg(e)); }
    }
    String[][] kw = {{"ca", "gregory"}, {"co", "phonebk"}, {"nu", null}, {"a", "b"}, {"cA", "GREGORY"}, {"ca", "x"}, {"kn", "true"}, {"kn", ""}, {"ca", "a-b"}};
    for (String[] s : kw) {
      try { System.out.println("kw " + s[0] + "=" + s[1] + " -> " + d(new Locale.Builder().setLanguage("en").setUnicodeLocaleKeyword(s[0], s[1]).build())); } catch (Exception e) { System.out.println("kw " + s[0] + "=" + s[1] + " -> !" + e.getClass().getName() + ":" + msg(e)); }
    }
    for (String at : new String[] {"abc", "ab", "toolongattr", "attr-1", "ABC"}) {
      try { System.out.println("attr " + at + " -> " + d(new Locale.Builder().setLanguage("en").addUnicodeLocaleAttribute(at).build())); } catch (Exception e) { System.out.println("attr " + at + " -> !" + e.getClass().getName() + ":" + msg(e)); }
    }
    Locale ja = new Locale.Builder().setLanguage("ja").setRegion("JP").setUnicodeLocaleKeyword("ca", "japanese").addUnicodeLocaleAttribute("foo").setExtension('x', "priv").build();
    System.out.println("complex " + d(ja) + " " + ja.stripExtensions() + " " + ja.hasExtensions() + " " + ja.getExtension('u') + " " + ja.getExtension('x') + " " + ja.getExtension('a') + " " + ja.getUnicodeLocaleType("ca") + " " + ja.getUnicodeLocaleType("zz"));
    System.out.println("eq " + Locale.forLanguageTag("en-US-u-ca-gregory-nu-latn").equals(Locale.forLanguageTag("en-US-u-nu-latn-ca-gregory")) + " " + Locale.forLanguageTag("en-US").equals(Locale.US));
    System.out.println("iso " + Locale.getISOCountries().length + " " + Locale.getISOLanguages().length + " " + Locale.getISOCountries(Locale.IsoCountryCode.PART1_ALPHA3).size() + " " + Locale.getISOCountries(Locale.IsoCountryCode.PART3).size() + " " + Locale.getISOCountries(Locale.IsoCountryCode.PART1_ALPHA2).size());
    for (Locale l : new Locale[] {Locale.US, Locale.FRANCE, new Locale("zh", "TW"), Locale.ROOT, new Locale("xx"), new Locale("yue"), new Locale("en", "US", "x")}) {
      try { System.out.println("iso3 " + l + " " + l.getISO3Language() + " " + l.getISO3Country()); } catch (Exception e) { System.out.println("iso3 " + l + " !" + e.getClass().getName() + ":" + msg(e)); }
    }
    System.out.println("tostring " + new Locale("", "US") + " " + new Locale("en", "", "POSIX") + " " + new Locale.Builder().setLanguage("sr").setScript("Latn").build() + " " + new Locale.Builder().setLanguage("sr").setScript("Latn").setRegion("RS").setVariant("a1234").build() + " " + new Locale.Builder().setScript("Latn").build() + " " + new Locale.Builder().setExtension('x', "foo").build() + " " + new Locale.Builder().setRegion("US").setExtension('x', "foo").build());
  }
}

  static class Lg {
  static String d(Locale l) { return "[" + l + "|" + l.toLanguageTag() + "]"; }
  public static void main(String[] a) {
    String[] tags = {"art-lojban", "cel-gaulish", "en-GB-oed", "i-ami", "i-bnn", "i-default", "i-enochian", "i-hak", "i-klingon", "i-lux", "i-mingo", "i-navajo", "i-pwn", "i-tao", "i-tay", "i-tsu", "no-bok", "no-nyn", "sgn-BE-FR", "sgn-BE-NL", "sgn-CH-DE", "zh-guoyu", "zh-hakka", "zh-min", "zh-min-nan", "zh-xiang", "I-KLINGON", "Zh-Min-Nan", "i-unknown", "sgn-be-fr-x-foo", "zh-hakka-u-ca-x",
      "en-x-lvariant-foo-bar", "en-x-lvariant-ab-cd", "en-x-lvariant-foo-priv", "en-x-priv-lvariant-foo", "en-x-lvariant", "en-x-lvariant-foo-lvariant-bar", "en-x-lvariant-toolongvariant", "en-US-x-lvariant-foo-bar", "en-a-bbb-a-ccc", "en-u-ca-gregory-u-nu-latn", "en-u-ca-a-ca-b", "en-u-ca-gregory-ca-buddhist", "en-u-nu-latn-ca-gregory-attr", "en-u-attr-ca", "en-u-AAA-bbb-ca-gregory", "en-u-ca-gregory-islamic-civil", "en-u-x-abc", "en-a-bbb-x-priv", "en-x-a-b", "en-a-b", "en-X-priv", "en-Latn-US-1996-u-ca-gregory", "en-u-1a-foo", "en-u-a1-foo", "en-u-ab-c", "en-u-ab-cde-fgh", "en-u-ab-cdefghijk", "und-u-ca-gregory", "und-x-foo", "und-Latn", "root", "en-US-POSIX-x-lvariant-POSIX", "ja-JP-x-lvariant-JP", "de-CH-1901-x-lvariant-a-b", "en-  ", " en", "en-US ", "en-0", "en-00", "en-000", "en-001-001", "en-1996-1996", "es-ES-x-lvariant-ab-12345"};
    for (String t : tags) System.out.println(t + " -> " + d(Locale.forLanguageTag(t)));
    for (String t : new String[] {"", "-", "en--US", "-en", "en-US--", "en-US-u-ca", "en-US-x", "en-a-b", "en-a-bbb-a-ccc", "en-US-US", "en-Latn-Latn", "en-x-", "en-x-toolonglonglong", "en-u-ca-gregory-u-nu-latn", "und", "UND-us", "xx-Latn-yy-zz", "e-US", "en-1", "en-US-1", "en-US-abcd", "en-abcd-US"}) {
      try { System.out.println("strict " + t + " -> " + d(new Locale.Builder().setLanguageTag(t).build())); } catch (Exception e) { System.out.println("strict " + t + " -> !" + msg(e)); }
    }
    for (String l : new String[] {"iw", "ji", "in", "IW", "EN", "english", "eng"}) { try { System.out.println("blang " + l + " -> " + d(new Locale.Builder().setLanguage(l).build()) + " " + new Locale(l)); } catch (Exception e) { System.out.println("blang " + l + " -> !" + msg(e)); } }
    try { System.out.println(d(new Locale.Builder().setLocale(new Locale("ja", "JP", "JP")).build())); } catch (Exception e) { System.out.println("setLocale JP !" + msg(e)); }
    try { System.out.println(d(new Locale.Builder().setLocale(new Locale("en", "US", "a_b")).build())); } catch (Exception e) { System.out.println("setLocale a_b !" + msg(e)); }
    try { System.out.println(d(new Locale.Builder().setLocale(new Locale("en", "usa")).build())); } catch (Exception e) { System.out.println("setLocale usa !" + msg(e)); }
    try { System.out.println(d(new Locale.Builder().setLocale(new Locale("123")).build())); } catch (Exception e) { System.out.println("setLocale 123 !" + msg(e)); }
    try { System.out.println(d(new Locale.Builder().setLocale(new Locale("en", "", "x")).build())); } catch (Exception e) { System.out.println("setLocale x !" + msg(e)); }
    try { System.out.println(d(new Locale.Builder().setLocale(new Locale("", "", "POSIX")).build())); } catch (Exception e) { System.out.println("setLocale POSIX !" + msg(e)); }
    try { System.out.println(d(new Locale.Builder().setLocale(null).setLanguage("fr").setLocale(Locale.forLanguageTag("en-u-ca-gregory")).setUnicodeLocaleKeyword("nu", "latn").addUnicodeLocaleAttribute("zzz").clearExtensions().build())); } catch (Exception e) { System.out.println("chain !" + msg(e)); }
    try { System.out.println(d(new Locale.Builder().setLocale(null).build())); } catch (Exception e) { System.out.println("setLocale null !" + e.getClass().getName()); }
    System.out.println(d(new Locale.Builder().setLanguage("en").setExtension('u', "").setExtension('x', "foo").setExtension('x', null).build()));
    System.out.println(d(new Locale.Builder().setExtension('u', "attr-ca-gregory").setUnicodeLocaleKeyword("ca", null).build()));
    System.out.println(d(new Locale.Builder().setExtension('u', "ca-gregory").addUnicodeLocaleAttribute("abc").build()));
    System.out.println(d(new Locale.Builder().setExtension('u', "abc-ca-gregory").removeUnicodeLocaleAttribute("abc").build()));
    try { System.out.println(d(new Locale.Builder().setExtension('u', "ca-gregory-ca-buddhist").build())); } catch (Exception e) { System.out.println("dupkw !" + msg(e)); }
    try { System.out.println(d(new Locale.Builder().setExtension('a', "bb-cc").setExtension('B', "dd").build())); } catch (Exception e) { System.out.println("upperkey !" + msg(e)); }
    try { new Locale.Builder().setExtension('x', "toolonglonglong"); } catch (Exception e) { System.out.println("x long !" + msg(e)); }
    try { new Locale.Builder().setExtension('u', "ca-gregory-x"); } catch (Exception e) { System.out.println("u x !" + msg(e)); }
    try { new Locale.Builder().addUnicodeLocaleAttribute(null); } catch (Exception e) { System.out.println("attr null !" + e.getClass().getName()); }
    try { new Locale.Builder().setUnicodeLocaleKeyword(null, "a"); } catch (Exception e) { System.out.println("kw null !" + e.getClass().getName()); }
    IllformedLocaleException ie = new IllformedLocaleException("msg", 3); System.out.println(ie.getMessage() + " " + ie.getErrorIndex() + " | " + new IllformedLocaleException("m").getErrorIndex() + " " + new IllformedLocaleException().getMessage() + " " + new IllformedLocaleException("m").getMessage());
    try { Locale.forLanguageTag(null); } catch (Exception e) { System.out.println("null tag " + e.getClass().getName()); }
  }
}

  static class Lc {
  static void p(String n, java.util.concurrent.Callable<Object> c) { try { System.out.println(n + "=" + c.call()); } catch (Throwable t) { System.out.println(n + "=!" + t.getClass().getName() + ":" + msg(t)); } }
  public static void main(String[] a) {
    for (String t : new String[] {"en-us", "EN-LATN-us-POSIX", "en-u-CA-GREGORY-x-PRIV", "zh-hant-tw", "iw-il", "x-FOO", "en", "und", "en-u-ca-japanese-attr", "in", "ji", "bad-tag!", "en_US", "", "e", "en-USA", "i-klingon", "zh-min-nan", "a-b", "en-t-ES-x-Foo", "tlh"}) p("fold " + t, () -> Locale.caseFoldLanguageTag(t));
    p("fold null", () -> Locale.caseFoldLanguageTag(null));
    p("of", () -> Locale.of("EN", "us", "Posix") + " " + Locale.of("iw") + " " + Locale.of("en").equals(Locale.ENGLISH));
    p("ctor-null", () -> new Locale(null));
    p("ctor-null2", () -> new Locale("en", null));
    p("of-null", () -> Locale.of(null));
    p("avail-stream", () -> Locale.availableLocales().filter(l -> l.getLanguage().equals("fr")).count());
    p("iso-langs", () -> Arrays.toString(Locale.getISOLanguages()).substring(0, 60));
    p("iso-countries", () -> Arrays.toString(Locale.getISOCountries()).substring(0, 60));
    p("iso-part3", () -> new TreeSet<>(Locale.getISOCountries(Locale.IsoCountryCode.PART3)).toString());
    p("iso-alpha3", () -> new TreeSet<>(Locale.getISOCountries(Locale.IsoCountryCode.PART1_ALPHA3)).first());
    p("iso-null", () -> Locale.getISOCountries(null));
    p("iso-set-immutable", () -> { try { Locale.getISOCountries(Locale.IsoCountryCode.PART3).add("XX"); return "mutable"; } catch (UnsupportedOperationException e) { return "immutable"; } });
    p("ext-sets", () -> { Locale l = Locale.forLanguageTag("en-u-ca-gregory-x-priv"); try { l.getExtensionKeys().add('z'); return "mutable"; } catch (UnsupportedOperationException e) { return "immutable " + l.getExtensionKeys().getClass().getSimpleName(); } });
    p("ext-char-bad", () -> Locale.forLanguageTag("en-u-ca-gregory").getExtension('1'));
    p("ext-char-bad2", () -> Locale.forLanguageTag("en-u-ca-gregory").getExtension('$'));
    p("ext-upper", () -> Locale.forLanguageTag("en-u-ca-gregory").getExtension('U'));
    p("type-null", () -> Locale.forLanguageTag("en-u-ca-gregory").getUnicodeLocaleType(null));
    p("type-bad", () -> Locale.forLanguageTag("en-u-ca-gregory").getUnicodeLocaleType("c"));
    p("type-upper", () -> Locale.forLanguageTag("en-u-ca-gregory").getUnicodeLocaleType("CA"));
    p("keys-class", () -> Locale.forLanguageTag("en-u-ca-gregory-co-phonebk").getUnicodeLocaleKeys().getClass().getSimpleName() + " " + Locale.forLanguageTag("en").getUnicodeLocaleKeys() + " " + Locale.forLanguageTag("en").getExtensionKeys());
    p("strip", () -> Locale.forLanguageTag("en-US-u-ca-gregory-x-priv").stripExtensions() + " " + (Locale.US.stripExtensions() == Locale.US));
    p("hasExt", () -> Locale.US.hasExtensions() + " " + Locale.forLanguageTag("en-x-a").hasExtensions());
    p("equals-var-case", () -> new Locale("en", "US", "POSIX").equals(new Locale("en", "US", "posix")));
    p("equals-case", () -> new Locale("EN", "us").equals(Locale.US));
    p("hash-ext", () -> Locale.forLanguageTag("en-US-u-ca-gregory").hashCode() + " " + new Locale("ja", "JP", "JP").hashCode());
    p("script-title", () -> { Locale l = new Locale.Builder().setLanguage("sr").setScript("lATN").build(); return l.getScript(); });
    p("variant-tostring", () -> new Locale("en", "US", "WIN") + " " + new Locale("en", "US", "WIN").toLanguageTag());
    p("tostring-more", () -> new Locale("", "", "x") + "|" + new Locale("", "US", "x") + "|" + new Locale("en", "", "x") + "|" + new Locale("en", "US", "x_y"));
    p("display-default-null", () -> Locale.US.getDisplayName(null));
    p("serial", () -> { var bo = new java.io.ByteArrayOutputStream(); new java.io.ObjectOutputStream(bo).writeObject(Locale.forLanguageTag("ja-JP-u-ca-japanese")); return new java.io.ObjectInputStream(new java.io.ByteArrayInputStream(bo.toByteArray())).readObject(); });
    p("setDefault-null", () -> { Locale.setDefault(null); return 1; });
    p("setDefault-cat-null", () -> { Locale.setDefault(Locale.Category.FORMAT, null); return 1; });
    p("setDefault-cat", () -> { Locale old = Locale.getDefault(); Locale.setDefault(Locale.Category.DISPLAY, Locale.FRANCE); String r = Locale.getDefault(Locale.Category.DISPLAY) + " " + Locale.getDefault() + " " + Locale.getDefault(Locale.Category.FORMAT); Locale.setDefault(old); return r + " " + Locale.getDefault(Locale.Category.DISPLAY); });
    p("cat-null", () -> Locale.getDefault(null));
    p("lookup", () -> Locale.lookupTag(Locale.LanguageRange.parse("en-US,fr;q=0.5"), List.of("fr", "en")));
  }
}

  static class Le {
  static String d(Locale l) { return "[" + l + "|" + l.toLanguageTag() + "|" + l.getExtensionKeys() + "]"; }
  public static void main(String[] a) {
    for (String t : new String[] {"en-1-abc", "en-9-ab-c", "en-US-w-abc", "en-y-abc", "en-z-ab", "en-A-ab", "en-q-aa-bb-x-priv", "en-us-X-a-1", "en-t-m0-ISO-x-lvariant-priv", "en-x-lvariant-ab-x-zzz", "en-latn-latn", "es-419-u-nu-latn", "de-DE-u-co-phonebk-ca-gregory-nu-latn-attr", "ar-SA-u-ca-islamic-civil-nu-arab", "he-IL-u-ca-hebrew"}) System.out.println(t + " -> " + d(Locale.forLanguageTag(t)));
    for (String[] c : new String[][] {{"no","NO","NY"},{"no","","NY"},{"nn","NO",""}}) { try { System.out.println("sl " + Arrays.toString(c) + " " + d(new Locale.Builder().setLocale(new Locale(c[0], c[1], c[2])).build())); } catch (Exception e) { System.out.println("sl " + Arrays.toString(c) + " !" + msg(e)); } }
    for (String t : new String[] {"en-u-nu-latn", "en", "und"}) System.out.println("display " + new Locale("en", "US", "posix").getDisplayVariant() + "|" + new Locale("en", "US", "Posix").getDisplayVariant());
    for (String t : new String[] {"en", "EN", "En", "e", "eng"}) System.out.println(t + " -> " + new Locale(t).getDisplayLanguage(Locale.ENGLISH));
    System.out.println(new Locale("", "us").getDisplayCountry(Locale.ENGLISH) + "|" + new Locale("", "US", "POSIX").getDisplayName(Locale.ENGLISH) + "|" + new Locale("", "", "POSIX").getDisplayName(Locale.ENGLISH));
    System.out.println(Locale.forLanguageTag("en-u-ca-japanese-nu-thai-attr").getDisplayName(Locale.ENGLISH) + "|" + Locale.forLanguageTag("und-u-ca-japanese").getDisplayName(Locale.ENGLISH) + "|" + Locale.forLanguageTag("en-u-kn").getDisplayName(Locale.ENGLISH) + "|" + Locale.forLanguageTag("en-u-attr-ca-japanese").getDisplayName(Locale.ENGLISH) + "|" + Locale.forLanguageTag("en-u-cu-usd").getDisplayName(Locale.ENGLISH));
    System.out.println(new Locale("zh", "TW").getDisplayName(new Locale("zh", "TW")) + "|" + new Locale("zh", "TW").getDisplayName(Locale.forLanguageTag("zh-Hant")) + "|" + new Locale("zh", "CN").getDisplayName(Locale.forLanguageTag("zh-Hans-CN")) + "|" + new Locale("en", "GB").getDisplayName(new Locale("en", "GB")) + "|" + new Locale("pt", "PT").getDisplayName(new Locale("pt", "PT")) + "|" + new Locale("es", "MX").getDisplayName(new Locale("es", "MX")));
    for (String tag : new String[] {"es-ES", "fr-FR", "de-DE", "it-IT", "pt-BR", "ja-JP", "ko-KR", "ru-RU", "nl-NL", "pl-PL", "tr-TR", "zh-CN", "zh-TW", "sv-SE", "ar-SA", "hi-IN"}) { Locale l = Locale.forLanguageTag(tag); System.out.println(tag + " -> " + l.getDisplayName(l) + " | " + l.getDisplayLanguage(l) + " | " + l.getDisplayCountry(Locale.ENGLISH) + " | " + Locale.US.getDisplayName(l) + " | " + Locale.FRANCE.getDisplayCountry(l)); }
  }
}

  static class LrangeProbe {
  static void p(String n, java.util.concurrent.Callable<Object> c) { try { System.out.println(n + "=" + c.call()); } catch (Throwable t) { System.out.println(n + "=!" + t.getClass().getName() + ":" + msg(t)); } }
  public static void main(String[] a) {
    for (String s : new String[] {"en-US", "en-US,fr;q=0.5", "en;q=0.3, fr;q=0.8, de", "*", "zh-TW", "zh-Hant-TW", "zh-hk", "iw", "he", "ja-JP-JP", "no-nyn", "en-US;q=0", "en;q=1.0", "en;q=1", "en;q=0.5;x=1", "EN-us", "x-private", "en-*", "*-US", "*-Latn-*", "en-us ,  fr", "", " ", "en;q=abc", "en;q=1.5", "en;q=-1", "en;q=0.1234", "en,,fr", "en;", "en;q=", "123", "toolonglanguage", "en-toolongsubtagx", "en-US-x-priv", "art-lojban", "sgn-be-fr", "zh-min-nan", "i-klingon", "zh-guoyu", "cmn-hans", "sr-latn-rs", "sh", "mo", "ro-md", "tl", "fil", "nb", "no", "ar-SA, ar;q=0.9"}) p("parse " + s, () -> Locale.LanguageRange.parse(s));
    p("parse null", () -> LanguageRange.parse(null));
    p("ctor", () -> new LanguageRange("EN-us") + " " + new LanguageRange("en", 0.5) + " " + new LanguageRange("*") + " " + new LanguageRange("en;q=0.5"));
    p("ctor bad", () -> new LanguageRange("en_US"));
    p("ctor bad2", () -> new LanguageRange("e!"));
    p("ctor null", () -> new LanguageRange(null));
    p("ctor w", () -> new LanguageRange("en", 1.1));
    p("ctor w2", () -> new LanguageRange("en", -0.1));
    p("ctor x", () -> new LanguageRange("x-private").getRange());
    p("ctor ext", () -> new LanguageRange("en-*-US").getRange() + " " + new LanguageRange("*-US").getRange());
    p("ctor num", () -> new LanguageRange("123").getRange());
    p("eq", () -> new LanguageRange("en", 0.5).equals(new LanguageRange("EN", 0.5)) + " " + new LanguageRange("en").equals(new LanguageRange("en", 1.0)) + " " + (new LanguageRange("en", 0.5).hashCode() == new LanguageRange("en", 0.5).hashCode()) + " " + new LanguageRange("en").hashCode() + " " + new LanguageRange("en", 0.5).hashCode());
    p("tostring", () -> new LanguageRange("en", 0.5) + " " + new LanguageRange("en") + " " + new LanguageRange("en", 0.0) + " " + new LanguageRange("en", 1.0));
    Map<String, List<String>> map = new HashMap<>(); map.put("ja", List.of("ja-JP", "ja-x")); map.put("en-us", List.of("en-US-x")); map.put("zh", List.of());
    p("parse map", () -> LanguageRange.parse("ja, en-US;q=0.5, de, zh", map));
    p("parse map null", () -> LanguageRange.parse("ja", null));
    p("mapEq", () -> LanguageRange.mapEquivalents(LanguageRange.parse("ja,en"), map));
    p("mapEq null", () -> LanguageRange.mapEquivalents(null, map));
    p("mapEq nullmap", () -> LanguageRange.mapEquivalents(LanguageRange.parse("ja,en"), null));
    List<Locale> locs = List.of(Locale.forLanguageTag("en-US"), Locale.forLanguageTag("en-GB"), Locale.forLanguageTag("fr-FR"), Locale.forLanguageTag("fr"), Locale.forLanguageTag("zh-Hant-TW"), Locale.forLanguageTag("zh-TW"), Locale.forLanguageTag("de"), Locale.forLanguageTag("de-CH-1996"), Locale.ROOT, Locale.forLanguageTag("ja-JP-u-ca-japanese"));
    for (String r : new String[] {"en-US", "en", "fr-CA", "fr-CA,en", "zh-TW", "zh-Hant", "zh", "de-CH", "de-DE-1996", "*", "en-*", "*-US", "*-CH", "es", "ja-JP", "xx", "en-US;q=0.5,fr;q=0.9", "", "ja-*-JP"}) {
      List<LanguageRange> lr = r.isEmpty() ? List.of() : LanguageRange.parse(r);
      p("lookup " + r, () -> Locale.lookup(lr, locs));
      for (FilteringMode m : FilteringMode.values()) p("filter " + r + " " + m, () -> Locale.filter(lr, locs, m));
      p("filter def " + r, () -> Locale.filter(lr, locs));
      List<String> tags = new ArrayList<>(); for (Locale l : locs) tags.add(l.toLanguageTag());
      p("lookupTag " + r, () -> Locale.lookupTag(lr, tags));
      p("filterTags " + r, () -> Locale.filterTags(lr, tags));
    }
    p("filter null", () -> Locale.filter(null, locs));
    p("filter null2", () -> Locale.filter(LanguageRange.parse("en"), null));
    p("filter mode null", () -> Locale.filter(LanguageRange.parse("en"), locs, null));
    p("lookup null", () -> Locale.lookup(null, locs));
    p("filterTags order", () -> Locale.filterTags(LanguageRange.parse("fr;q=0.9,en-US;q=0.8,en;q=0.5"), List.of("en-US", "en-GB", "fr", "fr-FR", "EN-us")));
    p("lookupTag case", () -> Locale.lookupTag(LanguageRange.parse("EN-US"), List.of("en-us", "EN")));
    p("filterTags dup", () -> Locale.filterTags(LanguageRange.parse("en,en-US"), List.of("en-US", "en-GB", "en")));
    p("filter zero weight", () -> Locale.filterTags(LanguageRange.parse("en;q=0, fr"), List.of("en", "fr")));
    p("lookup weight0", () -> Locale.lookupTag(LanguageRange.parse("en;q=0"), List.of("en")));
    p("filter ext mode reject", () -> Locale.filterTags(LanguageRange.parse("en-*-US"), List.of("en-US", "en-Latn-US"), FilteringMode.REJECT_EXTENDED_RANGES));
    p("filter ext mode ignore", () -> Locale.filterTags(LanguageRange.parse("en-*-US"), List.of("en-US", "en-Latn-US", "en-GB"), FilteringMode.IGNORE_EXTENDED_RANGES));
    p("filter ext mode map", () -> Locale.filterTags(LanguageRange.parse("en-*-US"), List.of("en-US", "en-Latn-US", "en-GB"), FilteringMode.MAP_EXTENDED_RANGES));
    p("filter ext mode extended", () -> Locale.filterTags(LanguageRange.parse("en-*-US"), List.of("en-US", "en-Latn-US", "en-GB"), FilteringMode.EXTENDED_FILTERING));
    p("filter ext auto", () -> Locale.filterTags(LanguageRange.parse("*-US"), List.of("en-US", "fr-FR", "de-US")));
    p("filter wild", () -> Locale.filterTags(LanguageRange.parse("*"), List.of("en-US", "fr-FR")));
    p("filter wild reject", () -> Locale.filterTags(LanguageRange.parse("*"), List.of("en-US", "fr-FR"), FilteringMode.REJECT_EXTENDED_RANGES));
    p("filter wild ignore", () -> Locale.filterTags(LanguageRange.parse("*"), List.of("en-US", "fr-FR"), FilteringMode.IGNORE_EXTENDED_RANGES));
  }
}

  static class Ld {
  static void p(String n, java.util.concurrent.Callable<Object> c) { try { System.out.println(n + "=" + c.call()); } catch (Throwable t) { System.out.println(n + "=!" + t.getClass().getName() + ":" + msg(t)); } }
  public static void main(String[] a) {
    Locale en = Locale.ENGLISH;
    String[][] combos = {{"en","","",""},{"en","US","",""},{"en","","POSIX",""},{"en","US","POSIX",""},{"","US","",""},{"","","POSIX",""},{"","","",""},{"zh","TW","","Hant"},{"","","","Latn"},{"zh","","","Hans"},{"sr","RS","","Latn"},{"en","US","a_b",""},{"en","","POSIX_REVISED",""},{"xx","YY","",""},{"xx","","qqq",""},{"","XX","",""},{"ja","JP","JP",""}};
    for (String[] c : combos) {
      Locale l = new Locale.Builder().build();
      try { l = new Locale(c[0], c[1], c[2]); if (!c[3].isEmpty()) l = new Locale.Builder().setLocale(l).setScript(c[3]).build(); } catch (Throwable t) { l = new Locale(c[0], c[1], c[2]); }
      System.out.println("name " + Arrays.toString(c) + " en=" + l.getDisplayName(en) + " | es=" + l.getDisplayName(Locale.forLanguageTag("es")) + " | ja=" + l.getDisplayName(Locale.JAPANESE) + " | zh=" + l.getDisplayName(Locale.CHINESE));
    }
    for (String tag : new String[] {"en-US-u-ca-japanese", "ja-JP-u-ca-japanese-nu-thai", "en-u-co-phonebk", "en-u-kn-true", "en-u-attr", "en-x-priv", "en-u-zz-foo", "en-u-ca-zzzz", "en-US-u-hc-h23", "de-u-fw-mon", "en-t-es", "en-u-nu-arab", "en-u-cu-eur", "en-u-ms-metric", "en-u-va-posix"}) {
      Locale l = Locale.forLanguageTag(tag);
      System.out.println("ext " + tag + " en=" + l.getDisplayName(en) + " | es=" + l.getDisplayName(Locale.forLanguageTag("es")) + " | de=" + l.getDisplayName(Locale.GERMAN));
    }
    System.out.println("script " + Locale.forLanguageTag("zh-Hant").getDisplayScript(en) + " | " + Locale.forLanguageTag("zh-Hant").getDisplayScript(Locale.CHINESE) + " | " + Locale.ROOT.getDisplayScript(en).isEmpty() + " | " + new Locale.Builder().setScript("Qaaa").build().getDisplayScript(en));
    System.out.println("lang-fallback " + new Locale("xx").getDisplayLanguage() + " | " + new Locale("xxx").getDisplayLanguage() + " | " + new Locale("yue").getDisplayLanguage(en) + " | " + new Locale("EN").getDisplayLanguage(new Locale("xx")));
    System.out.println("country-fallback " + new Locale("", "ZZ").getDisplayCountry(en) + " | " + new Locale("", "XK").getDisplayCountry(en) + " | " + new Locale("", "419").getDisplayCountry(en) + " | " + new Locale("", "001").getDisplayCountry(en));
    System.out.println("variant " + new Locale("en", "", "POSIX").getDisplayVariant(en) + " | " + new Locale("en", "", "foo_bar").getDisplayVariant(en) + " | " + new Locale("ca", "", "valencia").getDisplayVariant(en));
    Locale def = Locale.getDefault(); System.out.println("defaults " + def + " " + Locale.getDefault(Locale.Category.DISPLAY) + " " + Locale.getDefault(Locale.Category.FORMAT));
    System.out.println("avail " + Locale.getAvailableLocales().length + " " + Locale.availableLocales().count());
    List<String> av = new ArrayList<>(); for (Locale l : Locale.getAvailableLocales()) av.add(l.toString()); Collections.sort(av);
    System.out.println(av.subList(0, 12) + " ... " + av.stream().filter(s -> s.contains("#") || s.split("_").length > 2).limit(10).toList());
  }
}

  static class Sweep {
    static long hash(long h, String s) {
      return h * 31 + s.hashCode();
    }

    public static void main(String[] a) {
      Locale[] all = Locale.getAvailableLocales();
      Arrays.sort(all, Comparator.comparing(Locale::toLanguageTag).thenComparing(Locale::toString));
      System.out.println("available " + all.length);
      long h = 0;
      for (Locale l : all) {
        h = hash(h, l.toString());
        h = hash(h, l.toLanguageTag());
        h = h * 31 + l.hashCode();
        h = hash(h, l.getExtensionKeys().toString());
        try { h = hash(h, l.getISO3Language()); } catch (MissingResourceException e) { h = hash(h, "x3l"); }
        try { h = hash(h, l.getISO3Country()); } catch (MissingResourceException e) { h = hash(h, "x3c"); }
        h = h * 31 + (Locale.forLanguageTag(l.toLanguageTag()).equals(l) ? 1 : 0);
      }
      System.out.println("identity " + h);
      String[] displayTags = {"en", "es", "fr", "de", "it", "pt", "ja", "zh", "zh-Hant", "ko", "ru", "nl", "pl", "tr", "sv", "ar", "hi", "th", "vi", "id", "cs", "da", "fi", "nb", "he", "el", "hu", "uk", "ro", "bg", "ca", "hr", "sk", "sl"};
      for (String tag : displayTags) {
        Locale in = Locale.forLanguageTag(tag);
        long d = 0;
        for (String lang : Locale.getISOLanguages()) d = hash(d, new Locale(lang).getDisplayLanguage(in));
        for (String c : Locale.getISOCountries()) d = hash(d, new Locale("", c).getDisplayCountry(in));
        if (Arrays.asList("en", "es", "ja", "zh-Hant", "ar").contains(tag)) {
          for (Locale l : all) d = hash(d, l.getDisplayName(in));
        }
        System.out.println("display " + tag + " " + d);
      }
      long scripts = 0;
      for (String sc : new String[] {"Latn", "Cyrl", "Arab", "Hans", "Hant", "Deva", "Grek", "Hebr", "Thai", "Jpan", "Kore", "Armn", "Beng", "Ethi", "Geor", "Khmr", "Taml", "Telu", "Zzzz", "Qaaa"}) {
        scripts = hash(scripts, new Locale.Builder().setScript(sc).build().getDisplayScript(Locale.ENGLISH));
        scripts = hash(scripts, new Locale.Builder().setScript(sc).build().getDisplayScript(Locale.FRENCH));
      }
      System.out.println("scripts " + scripts);
      long currencies = 0;
      for (String tag : new String[] {"en", "es", "fr", "de", "ja", "ru"}) {
        for (String code : new String[] {"EUR", "USD", "JPY", "GBP", "CHF", "CAD", "AUD", "CNY"}) {
          try { currencies = hash(currencies, Currency.getInstance(code).getDisplayName(Locale.forLanguageTag(tag))); } catch (RuntimeException e) { currencies = hash(currencies, "x"); }
        }
      }
      System.out.println("currencies " + currencies);
      String[] pieces = {"en", "EN", "zh", "yue", "x", "", "und", "Latn", "latn", "Hant", "US", "us", "001", "419", "12", "1996", "posix", "POSIX", "u", "ca", "gregory", "a", "bb", "x", "priv", "lvariant", "foo", "toolongsubtag", "sr", "RS", "t", "m0"};
      long fuzz = 0;
      int count = 0;
      for (int i = 0; i < pieces.length; i++) {
        for (int j = 0; j < pieces.length; j++) {
          for (int k = 0; k < pieces.length; k += 7) {
            String tag = pieces[i] + "-" + pieces[j] + "-" + pieces[k];
            Locale l = Locale.forLanguageTag(tag);
            fuzz = hash(fuzz, l.toString() + "|" + l.toLanguageTag() + "|" + l.hashCode());
            try { fuzz = hash(fuzz, new Locale.Builder().setLanguageTag(tag).build().toString()); } catch (IllformedLocaleException e) { fuzz = hash(fuzz, e.getMessage()); }
            count++;
          }
        }
      }
      System.out.println("fuzz " + count + " " + fuzz);
    }
  }
}
