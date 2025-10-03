/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 * Minimum release: 22
 */

import java.text.*;
import java.util.*;
import java.util.function.Supplier;

public class ListFormatModern {
  static void probe(String name, Supplier<Object> body) {
    String out;
    try {
      out = String.valueOf(body.get());
    } catch (Throwable t) {
      out = "!" + t.getClass().getName() + ":" + t.getMessage();
    }
    System.out.println(name + "=" + out);
  }

  public static void main(String[] args) {
    Locale.setDefault(Locale.US);
    ListFormat lf = ListFormat.getInstance();
    probe("default", () -> lf + " " + lf.getLocale());
    probe("fmt-obj-list", () -> lf.format((Object) List.of("a", "b", "c")));
    probe("fmt-obj-array", () -> lf.format((Object) new String[] {"a", "b"}));
    probe("fmt-obj-bad", () -> lf.format((Object) "x"));
    probe("fmt-obj-null", () -> { try { return lf.format((Object) null); } catch (NullPointerException e) { return "npe"; } });
    probe("fmt-list-null", () -> { try { return lf.format((List<String>) null); } catch (NullPointerException e) { return "npe"; } });
    probe("fmt-nonstring", () -> lf.format((List) List.of(1, 2)));
    probe("fmt-null-elem", () -> lf.format(Arrays.asList("a", null)));
    probe("fmt-sizes", () -> { StringBuilder sb = new StringBuilder(); for (int n = 1; n <= 6; n++) { List<String> l = new ArrayList<>(); for (int i = 0; i < n; i++) l.add("e" + i); sb.append(lf.format(l)).append(" | "); } return sb; });
    probe("fmt-empty", () -> lf.format(List.of()));
    probe("patterns", () -> Arrays.toString(lf.getPatterns()) + " " + lf.getPatterns().length);
    probe("patterns-copy", () -> { lf.getPatterns()[0] = "zz"; return lf.getPatterns()[0]; });
    probe("custom", () -> { ListFormat c = ListFormat.getInstance(new String[] {"{0}; {1}", "{0}| {1}", "{0} & {1}", "{0} + {1}", "{0},{1},{2}"}); return c.format(List.of("a", "b", "c", "d")) + " " + c.format(List.of("a", "b", "c")) + " " + c.format(List.of("a", "b")) + " [" + c.getLocale() + "] " + c; });
    probe("custom-bad-len", () -> ListFormat.getInstance(new String[] {"{0}{1}"}));
    probe("custom-bad-pat", () -> ListFormat.getInstance(new String[] {"x", "{0}{1}", "{0}{1}", "{0}{1}", "{0}{1}{2}"}));
    probe("custom-null", () -> { try { return ListFormat.getInstance((String[]) null); } catch (NullPointerException e) { return "npe"; } });
    probe("custom-null-elem", () -> ListFormat.getInstance(new String[] {null, "{0}{1}", "{0}{1}", "{0}{1}", "{0}{1}{2}"}));
    probe("custom-three-bad", () -> ListFormat.getInstance(new String[] {"{0}{1}", "{0}{1}", "{0}{1}", "{0}{1}", "{0}{1}"}));
    probe("custom-each-bad", () -> { StringBuilder sb = new StringBuilder(); for (int i = 0; i < 4; i++) { String[] p = {"{0}{1}", "{0}{1}", "{0}{1}", "{0}{1}", "{0}{1}{2}"}; p[i] = "{0}"; try { ListFormat.getInstance(p); } catch (IllegalArgumentException e) { sb.append(e.getMessage()).append(" / "); } } return sb; });
    probe("custom-sep", () -> { try { ListFormat f = ListFormat.getInstance(new String[] {"{0}|{1}", "{0}~{1}", "{0}!{1}", "{0}?{1}", "{0}.{1}.{2}"}); return f.format(List.of("a", "b", "c", "d")) + " " + f.format(List.of("a", "b", "c")) + " " + f.format(List.of("a", "b")) + " " + f.parse("a|b~c!d") + " " + f.parse("a?b"); } catch (ParseException e) { return e.toString(); } });
    probe("equals", () -> lf.equals(ListFormat.getInstance()) + " " + (lf.hashCode() == ListFormat.getInstance().hashCode()) + " " + lf.equals(ListFormat.getInstance(Locale.FRENCH, ListFormat.Type.STANDARD, ListFormat.Style.FULL)) + " " + lf.equals("x"));
    ListFormat en = ListFormat.getInstance(Locale.US, ListFormat.Type.STANDARD, ListFormat.Style.FULL);
    probe("parse", () -> { try { return en.parse("a, b, and c") + " " + en.parse("a and b") + " " + en.parse("a") + " " + en.parse("a, b, c, and d") + " " + en.parse("a, b, c"); } catch (ParseException e) { return e.toString(); } });
    probe("parse-empty", () -> { try { return en.parse(""); } catch (ParseException e) { return e.toString(); } });
    probe("parse-null", () -> { try { return en.parse(null); } catch (NullPointerException | ParseException e) { return "npe"; } });
    probe("parseObject", () -> { ParsePosition pp = new ParsePosition(0); Object o = en.parseObject("x, y, and z", pp); return o + " " + pp; });
    probe("parseObject-pp", () -> { ParsePosition pp = new ParsePosition(2); Object o = en.parseObject("..a and b", pp); return o + " " + pp; });
    probe("parseObject-null", () -> { try { return en.parseObject("a", null); } catch (NullPointerException e) { return "npe"; } });
    probe("parse-unit-or", () -> { try { return ListFormat.getInstance(Locale.US, ListFormat.Type.UNIT, ListFormat.Style.NARROW).parse("a b c") + " " + ListFormat.getInstance(Locale.US, ListFormat.Type.OR, ListFormat.Style.FULL).parse("a, b, or c"); } catch (ParseException e) { return e.toString(); } });
    probe("parse-edge", () -> { try { return en.parse("a, b, and c trailing") + " " + en.parse("a, b and c") + " " + en.parse("a, b, and c, and d"); } catch (ParseException e) { return e.toString(); } });
    probe("iter", () -> { var it = en.formatToCharacterIterator(List.of("a", "bb", "c")); StringBuilder sb = new StringBuilder(); for (char c = it.first(); c != CharacterIterator.DONE; c = it.next()) { Object v = it.getAttribute(MessageFormat.Field.ARGUMENT); sb.append(c).append(v == null ? "-" : v); } return sb + " " + it.getAllAttributeKeys(); });
    probe("iter-bad", () -> en.formatToCharacterIterator("x"));
    probe("avail", () -> { Locale[] l = ListFormat.getAvailableLocales(); return (l.length > 100) + " " + Arrays.asList(l).contains(Locale.FRENCH) + " " + Arrays.asList(l).contains(Locale.ROOT); });
    probe("inst-null", () -> { try { return ListFormat.getInstance(null, ListFormat.Type.OR, ListFormat.Style.FULL); } catch (NullPointerException e) { return "npe"; } });
    probe("inst-root", () -> ListFormat.getInstance(Locale.ROOT, ListFormat.Type.OR, ListFormat.Style.NARROW));
    probe("inst-unknown", () -> ListFormat.getInstance(Locale.forLanguageTag("xx-YY"), ListFormat.Type.STANDARD, ListFormat.Style.FULL).format(List.of("a", "b", "c")));
    probe("enums", () -> Arrays.toString(ListFormat.Type.values()) + Arrays.toString(ListFormat.Style.values()) + ListFormat.Style.valueOf("SHORT"));
    probe("is-format", () -> en instanceof Format);
    probe("serial", () -> { try { var bo = new java.io.ByteArrayOutputStream(); new java.io.ObjectOutputStream(bo).writeObject(en); return ((ListFormat) new java.io.ObjectInputStream(new java.io.ByteArrayInputStream(bo.toByteArray())).readObject()).equals(en); } catch (Exception e) { return e.toString(); } });
    String[] tags = {"en", "en-GB", "en-AU", "es", "es-MX", "fr", "fr-CA", "de", "it", "pt-BR", "pt-PT", "ja", "zh", "zh-Hant-TW", "ko", "ru", "ar", "hi", "tr", "pl", "nl", "sv", "fi", "sr-Latn", "he", "th", "el", "cs", "hu", "uk", "vi", "id", "ro", "da", "nb"};
    for (String tag : tags) {
      probe("sweep-" + tag, () -> {
        StringBuilder sb = new StringBuilder();
        for (ListFormat.Type t : ListFormat.Type.values()) for (ListFormat.Style s : ListFormat.Style.values()) {
          ListFormat f = ListFormat.getInstance(Locale.forLanguageTag(tag), t, s);
          sb.append(f.format(List.of("x", "y", "z", "w"))).append('/').append(f.format(List.of("x", "y", "z"))).append('/').append(f.format(List.of("x", "y"))).append(';');
        }
        return sb;
      });
    }
    probe("sweep-all", () -> {
      long h = 0;
      Locale[] all = ListFormat.getAvailableLocales(); Arrays.sort(all, Comparator.comparing(Locale::toLanguageTag)); for (Locale l : all) for (ListFormat.Type t : ListFormat.Type.values()) for (ListFormat.Style s : ListFormat.Style.values()) {
        ListFormat f = ListFormat.getInstance(l, t, s);
        h = h * 31 + Arrays.hashCode(f.getPatterns());
      }
      return h;
    });
  }
}
