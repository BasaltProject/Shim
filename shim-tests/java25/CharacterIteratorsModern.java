/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 * Minimum release: 21
 */

import java.text.*;
import java.text.AttributedCharacterIterator.Attribute;
import java.util.*;
import java.util.function.Supplier;

public class CharacterIteratorsModern {
  static void probe(String name, Supplier<Object> body) {
    String out;
    try {
      out = String.valueOf(body.get());
    } catch (Throwable t) {
      out = "!" + t.getClass().getName() + ":" + t.getMessage();
    }
    System.out.println(name + "=" + out.replace(' ', '~').replace(' ', '_'));
  }

  static String runs(AttributedCharacterIterator it) {
    StringBuilder sb = new StringBuilder();
    for (char c = it.first(); c != CharacterIterator.DONE; c = it.setIndex(it.getRunLimit())) {
      int s = it.getRunStart(), e = it.getRunLimit();
      StringBuilder text = new StringBuilder();
      for (int i = s; i < e; i++) { it.setIndex(i); text.append(it.current()); }
      it.setIndex(s);
      List<String> attrs = new ArrayList<>();
      for (Map.Entry<Attribute, Object> en : it.getAttributes().entrySet()) attrs.add(en.getKey() + "=" + en.getValue());
      Collections.sort(attrs);
      sb.append('[').append(s).append(',').append(e).append(')').append('"').append(text).append('"').append(attrs).append(' ');
    }
    return sb.toString();
  }

  public static void main(String[] args) {
    Locale.setDefault(Locale.US);
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    probe("sci-basic", () -> { StringCharacterIterator it = new StringCharacterIterator("hello"); StringBuilder sb = new StringBuilder(); for (char c = it.first(); c != CharacterIterator.DONE; c = it.next()) sb.append(c).append(it.getIndex()); sb.append(it.current() == CharacterIterator.DONE).append(it.getIndex()); for (char c = it.last(); c != CharacterIterator.DONE; c = it.previous()) sb.append(c); return sb + " " + it.getBeginIndex() + " " + it.getEndIndex() + " " + it.previous() + " " + it.getIndex(); });
    probe("sci-range", () -> { StringCharacterIterator it = new StringCharacterIterator("0123456789", 2, 6, 3); return it.current() + " " + it.first() + " " + it.last() + " " + it.next() + " " + it.getIndex() + " " + it.setIndex(6) + " " + it.setIndex(2) + " " + it.getBeginIndex() + it.getEndIndex(); });
    probe("sci-bad-index", () -> new StringCharacterIterator("abc").setIndex(4));
    probe("sci-bad-range", () -> new StringCharacterIterator("abc", 2, 1, 2));
    probe("sci-bad-pos", () -> new StringCharacterIterator("abc", 5));
    probe("sci-empty", () -> { StringCharacterIterator it = new StringCharacterIterator(""); return (it.first() == CharacterIterator.DONE) + " " + (it.last() == CharacterIterator.DONE) + " " + it.getIndex(); });
    probe("sci-equals", () -> { StringCharacterIterator a = new StringCharacterIterator("abc"), b = new StringCharacterIterator("abc"); boolean e1 = a.equals(b); b.next(); return e1 + " " + a.equals(b) + " " + a.equals(a.clone()) + " " + (a.hashCode() == new StringCharacterIterator("abc").hashCode()); });
    probe("sci-settext", () -> { StringCharacterIterator it = new StringCharacterIterator("abc", 2); it.setText("xyz1"); return it.getIndex() + " " + it.getEndIndex() + " " + it.current(); });
    probe("annotation", () -> new Annotation("v").getValue() + " " + new Annotation(null).toString() + " " + new Annotation(5));
    probe("attribute-names", () -> Attribute.LANGUAGE + " " + Attribute.READING + " " + Attribute.INPUT_METHOD_SEGMENT + " " + NumberFormat.Field.INTEGER + " " + DateFormat.Field.MONTH + " " + MessageFormat.Field.ARGUMENT);
    probe("attribute-equals", () -> Attribute.LANGUAGE.equals(Attribute.LANGUAGE) + " " + Attribute.LANGUAGE.equals(Attribute.READING));
    probe("as-basic", () -> { AttributedString as = new AttributedString("hello world"); as.addAttribute(Attribute.LANGUAGE, "en", 0, 5); as.addAttribute(Attribute.READING, "r", 3, 11); return runs(as.getIterator()); });
    probe("as-map", () -> { Map<Attribute, Object> m = new HashMap<>(); m.put(Attribute.LANGUAGE, "fr"); AttributedString as = new AttributedString("abc", m); return runs(as.getIterator()); });
    probe("as-all-keys", () -> { AttributedString as = new AttributedString("abcdef"); as.addAttribute(Attribute.LANGUAGE, "x", 1, 3); as.addAttribute(Attribute.READING, "y", 2, 5); AttributedCharacterIterator it = as.getIterator(); List<String> ks = new ArrayList<>(); for (Attribute a : it.getAllAttributeKeys()) ks.add(a.toString()); Collections.sort(ks); return ks; });
    probe("as-filter", () -> { AttributedString as = new AttributedString("abcdef"); as.addAttribute(Attribute.LANGUAGE, "x", 1, 3); as.addAttribute(Attribute.READING, "y", 2, 5); return runs(as.getIterator(new Attribute[] {Attribute.READING})); });
    probe("as-sub-range", () -> { AttributedString as = new AttributedString("abcdef"); as.addAttribute(Attribute.LANGUAGE, "x", 1, 3); AttributedCharacterIterator it = as.getIterator(null, 1, 5); StringBuilder sb = new StringBuilder(); for (char c = it.first(); c != CharacterIterator.DONE; c = it.next()) sb.append(c); return sb + " " + it.getBeginIndex() + " " + it.getEndIndex() + " " + runs(it); });
    probe("as-run-queries", () -> { AttributedString as = new AttributedString("0123456789"); as.addAttribute(Attribute.LANGUAGE, "a", 2, 6); as.addAttribute(Attribute.READING, "b", 4, 8); AttributedCharacterIterator it = as.getIterator(); StringBuilder sb = new StringBuilder(); for (int i = 0; i < 10; i++) { it.setIndex(i); sb.append(it.getRunStart()).append('-').append(it.getRunLimit()).append('/').append(it.getRunStart(Attribute.LANGUAGE)).append('-').append(it.getRunLimit(Attribute.LANGUAGE)).append('/').append(it.getAttribute(Attribute.READING)).append(' '); } return sb; });
    probe("as-run-set", () -> { AttributedString as = new AttributedString("0123456789"); as.addAttribute(Attribute.LANGUAGE, "a", 2, 6); as.addAttribute(Attribute.READING, "b", 4, 8); AttributedCharacterIterator it = as.getIterator(); it.setIndex(5); Set<Attribute> both = new HashSet<>(Arrays.asList(Attribute.LANGUAGE, Attribute.READING)); return it.getRunStart(both) + " " + it.getRunLimit(both); });
    probe("as-copy", () -> { AttributedString as = new AttributedString("abcdef"); as.addAttribute(Attribute.LANGUAGE, "x", 1, 4); AttributedString copy = new AttributedString(as.getIterator(), 2, 5); return runs(copy.getIterator()); });
    probe("as-copy-filter", () -> { AttributedString as = new AttributedString("abcdef"); as.addAttribute(Attribute.LANGUAGE, "x", 1, 4); as.addAttribute(Attribute.READING, "y", 0, 6); AttributedString copy = new AttributedString(as.getIterator(), 0, 6, new Attribute[] {Attribute.READING}); return runs(copy.getIterator()); });
    probe("as-null-value", () -> { AttributedString as = new AttributedString("abc"); as.addAttribute(Attribute.LANGUAGE, null, 0, 2); AttributedCharacterIterator it = as.getIterator(); return runs(it) + " " + it.getAllAttributeKeys().size(); });
    probe("as-empty", () -> { AttributedString as = new AttributedString(""); AttributedCharacterIterator it = as.getIterator(); return (it.first() == CharacterIterator.DONE) + " " + it.getRunStart() + " " + it.getRunLimit() + " " + it.getAttributes(); });
    probe("as-bad-add", () -> { new AttributedString("abc").addAttribute(Attribute.LANGUAGE, "x", 2, 2); return "no"; });
    probe("as-bad-add2", () -> { new AttributedString("").addAttribute(Attribute.LANGUAGE, "x"); return "no"; });
    probe("as-bad-add3", () -> { new AttributedString("abc").addAttribute(null, "x"); return "no"; });
    probe("as-bad-range", () -> new AttributedString("abc").getIterator(null, 2, 9));
    probe("as-bad-null", () -> new AttributedString((String) null));
    probe("as-iter-nav", () -> { AttributedString as = new AttributedString("abc"); AttributedCharacterIterator it = as.getIterator(); StringBuilder sb = new StringBuilder(); sb.append(it.first()).append(it.next()).append(it.next()).append(it.next() == CharacterIterator.DONE).append(it.getIndex()).append(it.previous()).append(it.last()).append(it.getIndex()); try { it.setIndex(9); } catch (IllegalArgumentException e) { sb.append(e.getMessage()); } return sb; });
    probe("date-fmt-iter", () -> runs(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS a EEE zzz", Locale.US).formatToCharacterIterator(new Date(1700000000123L))));
    probe("date-fmt-iter-misc", () -> runs(new SimpleDateFormat("G yy 'lit' D F w W k K h Z X", Locale.US).formatToCharacterIterator(new Date(1700000000123L))));
    probe("date-fmt-iter-bad", () -> new SimpleDateFormat("yyyy").formatToCharacterIterator("x"));
    probe("date-fmt-iter-null", () -> new SimpleDateFormat("yyyy").formatToCharacterIterator(null));
    probe("date-field-calendar", () -> DateFormat.Field.ofCalendarField(Calendar.MONTH) + " " + DateFormat.Field.MONTH.getCalendarField() + " " + DateFormat.Field.HOUR1.getCalendarField() + " " + DateFormat.Field.TIME_ZONE.getCalendarField() + " " + DateFormat.Field.ofCalendarField(Calendar.HOUR_OF_DAY));
    probe("date-field-bad", () -> DateFormat.Field.ofCalendarField(99));
    probe("num-fmt-iter", () -> runs(new DecimalFormat("#,##0.00").formatToCharacterIterator(-1234567.891)));
    probe("num-fmt-iter-pct", () -> runs(NumberFormat.getPercentInstance(Locale.US).formatToCharacterIterator(0.256)));
    probe("num-fmt-iter-cur", () -> runs(NumberFormat.getCurrencyInstance(Locale.US).formatToCharacterIterator(-1234.5)));
    probe("num-fmt-iter-sci", () -> runs(new DecimalFormat("0.###E0").formatToCharacterIterator(-0.000123456)));
    probe("num-fmt-iter-special", () -> runs(new DecimalFormat("#,##0.#").formatToCharacterIterator(Double.NaN)) + " " + runs(new DecimalFormat("#,##0.#").formatToCharacterIterator(Double.NEGATIVE_INFINITY)));
    probe("num-fmt-iter-long", () -> runs(new DecimalFormat("#,##0").formatToCharacterIterator(1234567L)) + " " + runs(new DecimalFormat("‰ #").formatToCharacterIterator(5)));
    probe("num-fmt-iter-bad", () -> new DecimalFormat("0").formatToCharacterIterator("x"));
    probe("num-field-names", () -> NumberFormat.Field.FRACTION + " " + NumberFormat.Field.EXPONENT + " " + NumberFormat.Field.DECIMAL_SEPARATOR + " " + NumberFormat.Field.SIGN + " " + NumberFormat.Field.GROUPING_SEPARATOR + " " + NumberFormat.Field.EXPONENT_SYMBOL + " " + NumberFormat.Field.PERCENT + " " + NumberFormat.Field.PERMILLE + " " + NumberFormat.Field.CURRENCY + " " + NumberFormat.Field.EXPONENT_SIGN);
    probe("msg-fmt-iter", () -> runs(new MessageFormat("Hello {0}, you have {1,number,integer} items on {2,date,yyyy-MM-dd}.", Locale.US).formatToCharacterIterator(new Object[] {"Bob", 1234, new Date(1700000000123L)})));
    probe("msg-fmt-iter-missing", () -> runs(new MessageFormat("a {0} b {1}", Locale.US).formatToCharacterIterator(new Object[] {"x"})));
    probe("msg-fmt-iter-null", () -> runs(new MessageFormat("a {0}", Locale.US).formatToCharacterIterator(new Object[] {null})));
    probe("plain-format-iter", () -> runs(new ChoiceFormat("0#none|1#one|2#many").formatToCharacterIterator(1)));
  }
}
