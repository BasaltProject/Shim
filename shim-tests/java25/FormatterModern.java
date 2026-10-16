/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 * Minimum release: 15
 */

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

public class FormatterModern {
  static int count;

  static void check(String format, Object... args) {
    String label = String.format("%03d [%s]", ++count, format);
    try {
      System.out.println(label + " -> |" + String.format(format, args) + "|");
    } catch (Throwable error) {
      System.out.println(label + " -> " + error.getClass().getName() + ": " + error.getMessage());
    }
  }

  static class Custom implements Formattable {
    public void formatTo(Formatter formatter, int flags, int width, int precision) {
      formatter.format("custom(%d,%d,%d)", flags, width, precision);
    }
    public String toString() { return "plain"; }
  }

  public static void main(String[] args) {
    Locale.setDefault(Locale.ROOT);
    check("plain text");
    check("%s", "abc");
    check("%S", "abc");
    check("%10s|", "abc");
    check("%-10s|", "abc");
    check("%.2s", "abcdef");
    check("%8.3s|", "abcdef");
    check("%s", (Object) null);
    check("%S", (Object) null);
    check("%.2s", (Object) null);
    check("%s %s", "a", 42);
    check("%b %b %b %B", true, null, "x", false);
    check("%h %H", "hello", "hello");
    check("%h", (Object) null);
    check("%c %c %C", 'a', 66, 'z');
    check("%c", 0x1F600);
    check("%c", 0x110000);
    check("%c", "s");
    check("%5c|%-5c|", 'x', 'y');
    check("%%|%5%|%-5%|");
    check("a%nb");
    check("%d", 42);
    check("%d", -42);
    check("%5d|%-5d|%05d", 42, 42, 42);
    check("%+d % d %+d", 5, 5, -5);
    check("%(d %(d", 5, -5);
    check("%,d %,d %,d", 1234567, -1234567, 12);
    check("%,10d|%-,10d|%,010d", 1234567, 1234567, 1234567);
    check("%d %d %d %d", (byte) -1, (short) -2, 7L, Long.MIN_VALUE);
    check("%d", new BigInteger("123456789012345678901234567890"));
    check("%,d", new BigInteger("-123456789012345678901234567890"));
    check("%x %X %o", 255, 255, 8);
    check("%x %x %x", (byte) -1, (short) -1, -1);
    check("%x %o", -1L, -1L);
    check("%#x %#X %#o", 255, 255, 8);
    check("%08x|%#08x|%-8x|", 255, 255, 255);
    check("%x", new BigInteger("-255"));
    check("%#x %o", new BigInteger("255"), new BigInteger("-8"));
    check("%+x", new BigInteger("255"));
    check("%+x", 255);
    check("%d", 1.5);
    check("%d", "s");
    check("%d", (Object) null);
    check("%5d|", (Object) null);
    check("%f", 3.14159);
    check("%f", 2.0);
    check("%.2f", 0.125);
    check("%.1f", 0.15);
    check("%.1f", 0.25);
    check("%.0f", 2.5);
    check("%.0f", 3.5);
    check("%.3f", 1.0005);
    check("%.2f", -1.005);
    check("%10.3f|%-10.3f|%010.3f", 3.14159, 3.14159, -3.14159);
    check("%+.2f % .2f", 3.14159, 3.14159);
    check("%(.2f", -3.14159);
    check("%,.2f", 1234567.891);
    check("%f", 1e20);
    check("%f", 1.0e-5);
    check("%f", 123456789.123456789);
    check("%.10f", 0.1);
    check("%f %f %f", Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY);
    check("%10f|%-10f|%010f", Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY);
    check("%f", -0.0);
    check("%f", 0.0f);
    check("%f", 1.1f);
    check("%.3f", new BigDecimal("2.0005"));
    check("%f", new BigDecimal("-12345.678901234"));
    check("%#.0f", 3.0);
    check("%e", 12345.6789);
    check("%E", 12345.6789);
    check("%.2e", 0.000123456);
    check("%.0e", 5.5);
    check("%e", 0.0);
    check("%e", 1.0e100);
    check("%15.3e|%-15.3e|%015.3e", 12345.6789, 12345.6789, -12345.6789);
    check("%e", new BigDecimal("12345.6789"));
    check("%#.0e", 5.0);
    check("%g", 12345.6789);
    check("%g", 0.0001234);
    check("%g", 0.00001234);
    check("%g", 123456789.0);
    check("%G", 0.00001234);
    check("%.3g", 1234.5678);
    check("%.0g", 1234.5678);
    check("%g", 0.0);
    check("%g", 100000.0);
    check("%g", 1000000.0);
    check("%10.3g|", 3.14159);
    check("%a", 1.0);
    check("%a", 0.5);
    check("%A", 255.5);
    check("%a", -2.0);
    check("%.2a", 1.0);
    check("%e", "s");
    check("%f", 5);
    check("%2$s %1$s", "a", "b");
    check("%1$s %1$s %2$s", "a", "b");
    check("%s %<s %s", "a", "b");
    check("%<s", "a");
    check("%s %s", "a");
    check("%3$s", "a");
    check("%s");
    check("%q", 1);
    check("%", 1);
    check("%.s", 1);
    check("%-s", "a");
    check("%-d", 1);
    check("%0d", 1);
    check("%+ d", 1);
    check("%-0d", 1);
    check("%#d", 1);
    check("%,x", 1);
    check("%.2d", 1);
    check("%.2c", 'a');
    check("%#s", "a");
    check("%+s", "a");
    check("%#b", true);
    check("%--5d", 1);
    check("%,e", 1.0);
    check("%#g", 1.0);
    check("%,a", 1.0);
    check("%.2%");
    check("%5n");
    check("%s", new Custom());
    check("%-8.2S|", new Custom());
    check("%#s", new Custom());
    check("%tY", 0L);
    check("%d%%", 50);
    check("%s=%d;%s=%.1f", "a", 1, "b", 2.55);
    check("%08.3f", -1.5);
    check("%+08d", 42);
    check("% 08d", 42);
    check("%(08d", -42);
    check("%s", new int[0].length);
    check("%s", List.of(1, 2));
    check("%10.4s|", "truncated");
    check("%-6b|", true);
    check("%d %s", Integer.MAX_VALUE, Integer.MIN_VALUE);
    check("%o", Long.MIN_VALUE);
    check("%.15f", 0.1 + 0.2);
    check("%.17g", 0.1);
    check("%s", 1.0f);
    check("%s", 100.0 / 3);

    Formatter formatter = new Formatter(new StringBuilder());
    formatter.format("%s-%d", "x", 1).format("|%s", "y");
    System.out.println(formatter);
    System.out.println(formatter.locale());
    formatter.close();
    try {
      formatter.format("%s", "z");
    } catch (Throwable error) {
      System.out.println(error.getClass().getName());
    }
    try {
      formatter.toString();
    } catch (Throwable error) {
      System.out.println(error.getClass().getName());
    }
    formatter.close();
    System.out.println(new Formatter(Locale.ROOT).format("%.1f", 2.25).toString());
    System.out.printf("%s %d%n", "printf", 7);
    System.out.println("x".formatted() + "%d-%d".formatted(1, 2));
  }
}
