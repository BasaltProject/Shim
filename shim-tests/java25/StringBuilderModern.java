/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 * Minimum release: 21
 */

import java.util.*;
import java.util.concurrent.Callable;
import java.util.stream.*;
public class StringBuilderModern {
  static void t(String n, Callable<Object> c) { try { System.out.println(n + ": " + c.call()); } catch (Throwable e) { System.out.println(n + ": !" + e.getClass().getName() + ":" + (e instanceof NullPointerException ? null : e.getMessage())); } }
  static StringBuilder sb(String s) { return new StringBuilder(s); }
  public static void main(String[] a) throws Exception {
    t("ctor0", () -> new StringBuilder().capacity() + " " + new StringBuilder(5).capacity() + " " + new StringBuilder("abc").capacity() + " " + new StringBuilder((CharSequence) "abcd").capacity() + " " + new StringBuilder(new StringBuffer("xy")).capacity());
    t("ctorNeg", () -> new StringBuilder(-1));
    t("ctorNullS", () -> new StringBuilder((String) null));
    t("ctorNullCs", () -> new StringBuilder((CharSequence) null));
    t("growth", () -> { StringBuilder b = new StringBuilder(); StringBuilder caps = new StringBuilder(); int last = -1; for (int i = 0; i < 200; i++) { b.append('x'); if (b.capacity() != last) { last = b.capacity(); caps.append(last).append(','); } } return caps; });
    t("growthStr", () -> { StringBuilder b = new StringBuilder(); b.append("0123456789012345678901234567890123456789"); return b.capacity(); });
    t("ensure", () -> { StringBuilder b = new StringBuilder(); b.ensureCapacity(0); int c0 = b.capacity(); b.ensureCapacity(17); int c1 = b.capacity(); b.ensureCapacity(100); int c2 = b.capacity(); b.ensureCapacity(-5); return c0 + " " + c1 + " " + c2 + " " + b.capacity(); });
    t("trim", () -> { StringBuilder b = new StringBuilder(100); b.append("abc"); b.trimToSize(); int c = b.capacity(); b.append("d"); return c + " " + b.capacity(); });
    t("setLength", () -> { StringBuilder b = sb("abc"); b.setLength(5); String s = b.toString().replace('\0', '0'); b.setLength(1); return s + " " + b + " " + b.length(); });
    t("setLengthNeg", () -> { sb("abc").setLength(-1); return 1; });
    t("charAt", () -> sb("abc").charAt(1));
    t("charAtBad", () -> sb("abc").charAt(3));
    t("charAtNeg", () -> sb("abc").charAt(-1));
    t("setCharAt", () -> { StringBuilder b = sb("abc"); b.setCharAt(1, 'X'); return b; });
    t("setCharAtBad", () -> { sb("abc").setCharAt(3, 'X'); return 1; });
    t("appendNull", () -> sb("a").append((String) null).append((Object) null).append((StringBuffer) null).append((CharSequence) null).append((char[]) null == null ? "" : ""));
    t("appendNullArr", () -> sb("a").append((char[]) null));
    t("appendPrims", () -> sb("").append(true).append('c').append(12).append(34L).append(1.5f).append(2.25).append(new char[] {'x', 'y'}).append(new char[] {'a', 'b', 'c'}, 1, 2).append((Object) "o").append(new StringBuffer("sb")).append((CharSequence) "cs").append("0123456789", 2, 5));
    t("appendFloatSpecial", () -> sb("").append(Float.NaN).append(' ').append(Double.POSITIVE_INFINITY).append(' ').append(-0.0).append(' ').append(1e10).append(' ').append(1.0E-5f).append(' ').append(100.0).append(' ').append(Double.MIN_VALUE).append(' ').append(1234567.0f));
    t("appendCharsBad", () -> sb("").append(new char[] {'a'}, 1, 1) + "|" + sb("").append(new char[] {'a'}, 0, 2));
    t("appendCsRange", () -> sb("").append((CharSequence) "abc", 1, 4));
    t("appendCsRange2", () -> sb("").append((CharSequence) "abc", 2, 1));
    t("appendCsRangeNull", () -> sb("").append((CharSequence) null, 0, 2));
    t("appendCsRangeNull2", () -> sb("").append((CharSequence) null, 0, 5));
    t("appendCp", () -> sb("").appendCodePoint(0x1F600).appendCodePoint(65).length());
    t("appendCpBad", () -> sb("").appendCodePoint(0x110000));
    t("appendCpNeg", () -> sb("").appendCodePoint(-1));
    t("appendSelf", () -> { StringBuilder b = sb("ab"); b.append(b); b.append((CharSequence) b); return b; });
    t("insert", () -> sb("abc").insert(1, "XY").insert(0, 'z').insert(5, 42).insert(0, true).insert(2, 1.5).insert(1, 7L).insert(0, 2.5f).insert(0, new char[] {'p', 'q'}).insert(0, (Object) "o").insert(3, (CharSequence) "cs"));
    t("insertCharsRange", () -> sb("abc").insert(1, new char[] {'x', 'y', 'z'}, 1, 2));
    t("insertCharsRangeBad", () -> sb("abc").insert(1, new char[] {'x'}, 1, 2));
    t("insertCharsRangeBad2", () -> sb("abc").insert(5, new char[] {'x'}, 0, 1));
    t("insertCsRange", () -> sb("abc").insert(1, "012345", 2, 4));
    t("insertCsRangeBad", () -> sb("abc").insert(1, "012345", 4, 2));
    t("insertCsRangeBad2", () -> sb("abc").insert(9, "012345", 1, 2));
    t("insertBad", () -> sb("abc").insert(4, "x"));
    t("insertNeg", () -> sb("abc").insert(-1, "x"));
    t("insertNull", () -> sb("abc").insert(1, (String) null).insert(0, (Object) null).insert(0, (CharSequence) null));
    t("insertCharBad", () -> sb("abc").insert(4, 'c'));
    t("insertIntBad", () -> sb("abc").insert(4, 1));
    t("insertSelf", () -> { StringBuilder b = sb("abc"); b.insert(1, b); return b; });
    t("delete", () -> sb("abcdef").delete(1, 3) + " " + sb("abcdef").delete(2, 100) + " " + sb("abcdef").delete(0, 0) + " " + sb("abcdef").delete(3, 3));
    t("deleteBad", () -> sb("abcdef").delete(-1, 2));
    t("deleteBad2", () -> sb("abcdef").delete(3, 2));
    t("deleteBad3", () -> sb("abcdef").delete(7, 8));
    t("deleteCharAt", () -> sb("abc").deleteCharAt(1));
    t("deleteCharAtBad", () -> sb("abc").deleteCharAt(3));
    t("deleteCharAtNeg", () -> sb("abc").deleteCharAt(-1));
    t("replace", () -> sb("abcdef").replace(1, 3, "XYZ") + " " + sb("abcdef").replace(2, 100, "!") + " " + sb("abc").replace(3, 3, "d") + " " + sb("abc").replace(0, 1, ""));
    t("replaceBad", () -> sb("abcdef").replace(-1, 2, "x"));
    t("replaceBad2", () -> sb("abcdef").replace(3, 2, "x"));
    t("replaceBad3", () -> sb("abcdef").replace(7, 8, "x"));
    t("replaceNull", () -> sb("abcdef").replace(0, 1, null));
    t("indexOf", () -> sb("abcabc").indexOf("bc") + " " + sb("abcabc").indexOf("bc", 2) + " " + sb("abcabc").indexOf("") + " " + sb("abcabc").indexOf("", 10) + " " + sb("abcabc").indexOf("x") + " " + sb("abcabc").indexOf("bc", -5) + " " + sb("abcabc").lastIndexOf("bc") + " " + sb("abcabc").lastIndexOf("bc", 3) + " " + sb("abcabc").lastIndexOf("") + " " + sb("abcabc").lastIndexOf("", 2) + " " + sb("abcabc").lastIndexOf("bc", -1) + " " + sb("abc").lastIndexOf("abcd"));
    t("indexOfNull", () -> sb("abc").indexOf(null));
    t("lastIndexOfNull", () -> sb("abc").lastIndexOf(null));
    t("reverse", () -> sb("abc").reverse() + " " + sb("a😀b").reverse().codePointAt(1) + " " + sb("").reverse() + " " + sb("\uDE00\uD83D").reverse().length() + " " + sb("ab\uD83D").reverse().length());
    t("reverseSurr", () -> { StringBuilder b = sb("x😀y"); b.reverse(); return (int) b.charAt(1) + " " + (int) b.charAt(2); });
    t("substring", () -> sb("abcdef").substring(2) + " " + sb("abcdef").substring(1, 3) + " " + sb("abcdef").substring(6) + " " + sb("abcdef").subSequence(0, 2));
    t("substringBad", () -> sb("abcdef").substring(7));
    t("substringBad2", () -> sb("abcdef").substring(3, 2));
    t("substringBad3", () -> sb("abcdef").substring(-1, 2));
    t("substringBad4", () -> sb("abcdef").substring(1, 7));
    t("subSequenceBad", () -> sb("abcdef").subSequence(4, 1));
    t("getChars", () -> { char[] d = new char[6]; Arrays.fill(d, '-'); sb("abcdef").getChars(1, 4, d, 2); return new String(d); });
    t("getCharsBad", () -> { sb("abc").getChars(0, 4, new char[10], 0); return 1; });
    t("getCharsBad2", () -> { sb("abc").getChars(2, 1, new char[10], 0); return 1; });
    t("getCharsBad3", () -> { sb("abc").getChars(0, 3, new char[2], 0); return 1; });
    t("getCharsBad4", () -> { sb("abc").getChars(0, 3, new char[3], -1); return 1; });
    t("getCharsNull", () -> { sb("abc").getChars(0, 3, null, 0); return 1; });
    t("cp", () -> { StringBuilder b = sb("a😀b"); return b.codePointAt(1) + " " + b.codePointAt(2) + " " + b.codePointBefore(3) + " " + b.codePointBefore(2) + " " + b.codePointCount(0, 4) + " " + b.codePointCount(1, 2) + " " + b.offsetByCodePoints(0, 2) + " " + b.offsetByCodePoints(4, -2); });
    t("cpBad", () -> sb("abc").codePointAt(3));
    t("cpBad2", () -> sb("abc").codePointBefore(0));
    t("cpBad3", () -> sb("abc").codePointBefore(4));
    t("cpBad4", () -> sb("abc").codePointCount(2, 1));
    t("cpBad5", () -> sb("abc").codePointCount(0, 4));
    t("cpBad6", () -> sb("abc").offsetByCodePoints(4, 1));
    t("cpBad7", () -> sb("abc").offsetByCodePoints(0, 5));
    t("cpBad8", () -> sb("abc").offsetByCodePoints(0, -1));
    t("compare", () -> sb("abc").compareTo(sb("abd")) + " " + sb("abc").compareTo(sb("abc")) + " " + sb("ab").compareTo(sb("abc")) + " " + sb("b").compareTo(sb("abc")));
    t("compareNull", () -> sb("abc").compareTo(null));
    t("equals", () -> { StringBuilder x = sb("a"); return x.equals(sb("a")) + " " + x.equals(x) + " " + (x.hashCode() == sb("a").hashCode()); });
    t("chars", () -> sb("abc").chars().sum() + " " + sb("a😀").codePoints().boxed().collect(Collectors.toList()) + " " + sb("a\uD83D").codePoints().boxed().collect(Collectors.toList()));
    t("isEmpty", () -> sb("").isEmpty() + " " + sb("a").isEmpty());
    t("repeat", () -> sb("x").repeat('a', 3) + " " + sb("x").repeat("ab", 2) + " " + sb("x").repeat(0x1F600, 2).length() + " " + sb("x").repeat("ab", 0) + " " + sb("x").repeat((CharSequence) null, 2));
    t("repeatBad", () -> sb("x").repeat('a', -1));
    t("repeatBad2", () -> sb("x").repeat("a", -1));
    t("repeatBadCp", () -> sb("x").repeat(-1, 1));
    t("repeatBadCp2", () -> sb("x").repeat(0x110000, 1));
    t("toString", () -> { StringBuilder b = sb("abc"); String s = b.toString(); b.append("d"); return s + " " + b; });
    t("buffer", () -> { StringBuffer b = new StringBuffer("abc"); b.append(1).insert(0, 'x').replace(1, 2, "ZZ").reverse().deleteCharAt(0).append(new char[] {'q'}).appendCodePoint(0x41); return b + " " + b.capacity() + " " + b.length() + " " + b.indexOf("Z") + " " + b.compareTo(new StringBuffer("a")) + " " + b.substring(1, 3) + " " + b.charAt(0); });
    t("bufferCtor", () -> new StringBuffer().capacity() + " " + new StringBuffer(3).capacity() + " " + new StringBuffer("ab").capacity() + " " + new StringBuffer((CharSequence) "abc").capacity());
    t("bufferBad", () -> new StringBuffer("abc").charAt(5));
    t("bufferBad2", () -> new StringBuffer("abc").delete(5, 6));
    t("bufferBad3", () -> new StringBuffer("abc").insert(9, "x"));
    t("bufferRepeat", () -> new StringBuffer("x").repeat("ab", 2));
    t("bufferChars", () -> new StringBuffer("ab").chars().sum());
    t("concat", () -> { String x = "a"; int n = 5; char c = 'c'; Object o = null; return x + n + c + o + 1.5 + true + 'z' + 3L + 2f + new char[0].length; });
    t("big", () -> { StringBuilder b = new StringBuilder(); for (int i = 0; i < 20000; i++) b.append(i % 10); return b.length() + " " + b.charAt(19999) + " " + b.substring(10, 15) + " " + b.indexOf("90123"); });
    t("serial", () -> { java.io.ByteArrayOutputStream bo = new java.io.ByteArrayOutputStream(); StringBuilder b = sb("serial"); b.ensureCapacity(100); new java.io.ObjectOutputStream(bo).writeObject(b); StringBuilder r = (StringBuilder) new java.io.ObjectInputStream(new java.io.ByteArrayInputStream(bo.toByteArray())).readObject(); StringBuffer sf = new StringBuffer("buf"); java.io.ByteArrayOutputStream bo2 = new java.io.ByteArrayOutputStream(); new java.io.ObjectOutputStream(bo2).writeObject(sf); StringBuffer r2 = (StringBuffer) new java.io.ObjectInputStream(new java.io.ByteArrayInputStream(bo2.toByteArray())).readObject(); return r + " " + r.capacity() + " " + r2 + " " + r2.capacity() + " " + bo.size() + " " + bo2.size(); });
  }
}
