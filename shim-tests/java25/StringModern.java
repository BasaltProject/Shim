/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

// Minimum release: 17

import java.util.Arrays;
import java.util.List;
import java.util.StringJoiner;

public class StringModern {
    public static void main(String[] args) {
        testCharacterMethods();
        testCharSequenceMethods();
        testStripAndBlank();
        testRepeat();
        testTransformAndFormatted();
        testIndent();
        testStripIndent();
        testTranslateEscapes();
        testStringJoin();
        testStringJoiner();
        testStringBuilderAndBuffer();
        System.out.println("ALL STRING MODERN TESTS PASSED");
    }

    private static void testCharacterMethods() {
        System.out.println("char toString: " + Character.toString(65));
        System.out.println("supplementary toString: " + Character.toString(0x1F600));

        try {
            Character.toString(-1);
            throw new AssertionError();
        } catch (IllegalArgumentException expected) {
            System.out.println("char toString negative: iae");
        }

        try {
            Character.toString(0x110000);
            throw new AssertionError();
        } catch (IllegalArgumentException expected) {
            System.out.println("char toString overflow: iae");
        }

        System.out.println("isWhitespace space: " + Character.isWhitespace(' '));
        System.out.println("isWhitespace tab: " + Character.isWhitespace('\t'));
        System.out.println("isWhitespace newline: " + Character.isWhitespace('\n'));
        System.out.println("isWhitespace ogham: " + Character.isWhitespace(0x1680));
        System.out.println("isWhitespace ideographic: " + Character.isWhitespace(0x3000));
        System.out.println("isWhitespace nbsp: " + Character.isWhitespace(0x00A0));
        System.out.println("isWhitespace letter: " + Character.isWhitespace('A'));

        System.out.println("isValidCodePoint 0: " + Character.isValidCodePoint(0));
        System.out.println("isValidCodePoint max: " + Character.isValidCodePoint(0x10FFFF));
        System.out.println("isValidCodePoint invalid: " + Character.isValidCodePoint(0x110000));

        System.out.println("isBmpCodePoint bmp: " + Character.isBmpCodePoint(0xFFFF));
        System.out.println("isBmpCodePoint supp: " + Character.isBmpCodePoint(0x10000));

        System.out.println("charCount bmp: " + Character.charCount('x'));
        System.out.println("charCount supp: " + Character.charCount(0x1F600));

        System.out.println("char compare: " + (Character.compare('b', 'a') > 0));
    }

    private static void testCharSequenceMethods() {
        CharSequence cs1 = "abc";
        CharSequence cs2 = "abd";
        CharSequence csEmpty = "";

        System.out.println("cs isEmpty false: " + cs1.isEmpty());
        System.out.println("cs isEmpty true: " + csEmpty.isEmpty());
        System.out.println("cs compare lt: " + (CharSequence.compare(cs1, cs2) < 0));
        System.out.println("cs compare eq: " + CharSequence.compare(cs1, "abc"));
        System.out.println("cs compare len: " + (CharSequence.compare("ab", "abc") < 0));
    }

    private static void testStripAndBlank() {
        String s = " \t  hello world \u3000\n";
        System.out.println("strip: [" + s.strip() + "]");
        System.out.println("stripLeading: [" + s.stripLeading().replace("\u3000", "[IDEO]").replace("\n", "[LF]") + "]");
        System.out.println("stripTrailing: [" + s.stripTrailing().replace("\t", "[TAB]") + "]");

        System.out.println("isBlank spaces: " + "  \t \u3000 \n".isBlank());
        System.out.println("isBlank empty: " + "".isBlank());
        System.out.println("isBlank nonblank: " + "  a  ".isBlank());
    }

    private static void testRepeat() {
        System.out.println("repeat 0: [" + "abc".repeat(0) + "]");
        System.out.println("repeat 1: [" + "abc".repeat(1) + "]");
        System.out.println("repeat 3: [" + "abc".repeat(3) + "]");
        System.out.println("empty repeat: [" + "".repeat(5) + "]");

        try {
            "abc".repeat(-1);
            throw new AssertionError();
        } catch (IllegalArgumentException expected) {
            System.out.println("repeat negative: iae");
        }
    }

    private static void testTransformAndFormatted() {
        String res = "hello".transform(String::toUpperCase);
        System.out.println("transform: " + res);

        int len = "12345".transform(String::length);
        System.out.println("transform to int: " + len);

        String formatted = "Hello %s, number %d!".formatted("World", 42);
        System.out.println("formatted: " + formatted);
    }

    private static void testIndent() {
        System.out.println("empty indent: [" + "".indent(4) + "]");

        String single = "hello";
        System.out.print("single indent 2:\n" + single.indent(2));

        String multi = "line1\nline2\n";
        System.out.print("multi indent 2:\n" + multi.indent(2));

        String unindent = "    indented";
        System.out.print("unindent -2:\n" + unindent.indent(-2));
        System.out.print("unindent -6:\n" + unindent.indent(-6));

        String zero = "hello\nworld";
        System.out.print("zero indent:\n" + zero.indent(0));
    }

    private static void testStripIndent() {
        String s1 = "    line1\n      line2\n    line3";
        System.out.println("stripIndent:\n" + s1.stripIndent());

        String s2 = "  line1\n";
        System.out.println("stripIndent with trailing newline:\n" + s2.stripIndent());

        String s3 = "    line1\n\n    line2";
        System.out.println("stripIndent middle blank:\n" + s3.stripIndent());
    }

    private static void testTranslateEscapes() {
        String s = "a\\tb\\nc\\\"d\\'e\\\\f";
        System.out.println("escapes: " + s.translateEscapes().replace("\t", "[TAB]").replace("\n", "[LF]"));

        String octal = "\\101\\102";
        System.out.println("octal escapes: " + octal.translateEscapes());

        String cont = "line1\\\nline2";
        System.out.println("line cont: " + cont.translateEscapes());

        try {
            "abc\\x".translateEscapes();
            throw new AssertionError();
        } catch (IllegalArgumentException expected) {
            System.out.println("invalid escape: iae");
        }

        try {
            "abc\\".translateEscapes();
            throw new AssertionError();
        } catch (IllegalArgumentException expected) {
            System.out.println("trailing escape: iae");
        }
    }

    private static void testStringJoin() {
        System.out.println("join varargs: " + String.join("-", "a", "b", "c"));
        System.out.println("join single: " + String.join("-", "alone"));
        System.out.println("join empty: " + String.join("-"));

        List<String> list = Arrays.asList("1", "2", "3");
        System.out.println("join iterable: " + String.join(":", list));
    }

    private static void testStringJoiner() {
        StringJoiner sj1 = new StringJoiner(", ");
        System.out.println("empty sj: [" + sj1.toString() + "]");
        sj1.add("first").add("second").add(null);
        System.out.println("sj elements: [" + sj1.toString() + "]");

        StringJoiner sj2 = new StringJoiner(", ", "[", "]");
        System.out.println("empty with prefix/suffix: [" + sj2.toString() + "]");
        sj2.add("x").add("y");
        System.out.println("sj2 elements: [" + sj2.toString() + "]");

        StringJoiner sj3 = new StringJoiner(" - ", "<", ">");
        sj3.setEmptyValue("<empty>");
        System.out.println("custom empty: [" + sj3.toString() + "]");

        StringJoiner sj4 = new StringJoiner(", ", "(", ")");
        sj4.add("alpha").add("beta");
        StringJoiner sj5 = new StringJoiner(", ", "(", ")");
        sj5.add("gamma").add("delta");
        sj4.merge(sj5);
        System.out.println("merged: [" + sj4.toString() + "]");
    }

    private static void testStringBuilderAndBuffer() {
        StringBuilder sb1 = new StringBuilder("abc");
        StringBuilder sb2 = new StringBuilder("abd");
        System.out.println("sb compareTo: " + (sb1.compareTo(sb2) < 0));
        System.out.println("sb codePointAt: " + sb1.codePointAt(0));
        System.out.println("sb codePointBefore: " + sb1.codePointBefore(2));
        System.out.println("sb codePointCount: " + sb1.codePointCount(0, 3));

        StringBuffer sbuf1 = new StringBuffer("abc");
        StringBuffer sbuf2 = new StringBuffer("abd");
        System.out.println("sbuf compareTo: " + (sbuf1.compareTo(sbuf2) < 0));
        System.out.println("sbuf codePointAt: " + sbuf1.codePointAt(0));
        System.out.println("sbuf codePointBefore: " + sbuf1.codePointBefore(2));
        System.out.println("sbuf codePointCount: " + sbuf1.codePointCount(0, 3));
    }
}
