/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 * Minimum release: 21
 */

import java.io.*;
import java.math.*;
import java.util.*;

public class BigDecimalModern {
  interface Body {
    Object get() throws Exception;
  }

  static void probe(String name, Body body) {
    String out;
    try {
      out = String.valueOf(body.get());
    } catch (Throwable t) {
      out = "!" + t.getClass().getName() + ":" + (t instanceof NullPointerException ? "npe" : t.getMessage());
    }
    System.out.println(name + "=" + out);
  }

  static String desc(BigDecimal d) {
    return d + "/" + d.scale() + "/" + d.precision() + "/" + d.signum() + "/" + d.unscaledValue();
  }

  public static void main(String[] args) throws Exception {
    Locale.setDefault(Locale.US);
    String[] strs = {"0", "0.0", "0.00", "-0", "1", "-1", "10", "100", "1E+3", "1E3", "1.5E-3", "123.456", "-123.456", "0.000001", "0.0000001", "1E-7", "123456789012345678901234567890.123456789", "-9.99E+20", "1e5", "+5", "5.", ".5", "0E+10", "0E-10", "1234567890123456789012345678901234567890", "3.14159265358979323846264338327950288", "2.718281828459045235360287471352662497757", "99999999999999999999.99999999999999999999", "12345.6789e-2", "-.5e1"};
    List<BigDecimal> vals = new ArrayList<>();
    for (String s : strs) {
      final String fs = s;
      probe("parse " + fs, () -> { BigDecimal d = new BigDecimal(fs); vals.add(d); return desc(d) + "|" + d.toPlainString() + "|" + d.toEngineeringString() + "|" + d.hashCode() + "|" + d.stripTrailingZeros().toString(); });
    }
    probe("extreme exp", () -> { BigDecimal a = new BigDecimal("1E+2147483647"); BigDecimal b = new BigDecimal("1E-2147483648"); return a + " " + a.scale() + " " + a.precision() + " " + b + " " + b.scale() + " " + a.negate() + " " + a.compareTo(b) + " " + a.hashCode() + " " + a.signum(); });
    probe("extreme exp ops", () -> { BigDecimal a = new BigDecimal("1E+2147483647"); try { return a.multiply(a); } catch (ArithmeticException e) { return e.getMessage(); } });
    probe("extreme exp div", () -> { BigDecimal a = new BigDecimal("1E+2147483647"); return a.divide(new BigDecimal("1E-5"), MathContext.DECIMAL32); });
    probe("extreme exp add", () -> new BigDecimal("1E+2147483647").add(new BigDecimal("1E-2147483648"), MathContext.DECIMAL32) + " " + new BigDecimal("1E+2147483647").add(BigDecimal.ONE, MathContext.DECIMAL32));
    for (String s : new String[] {"", " 1", "1 ", "1e", "e1", "1.2.3", "--1", "1e1e1", "0x10", "NaN", "Infinity", "1_000", "١٢٣", "+-1", "1E99999999999", "."}) {
      final String fs = s;
      probe("parse bad [" + fs + "]", () -> new BigDecimal(fs));
    }
    for (double dv : new double[] {0.0, -0.0, 1.0, 0.1, 0.5, 100.0, 1e22, 1e-5, 123456.789, Double.MIN_VALUE, Double.MAX_VALUE, 1.0 / 3, Math.PI, 2.5e-300, (double) Long.MAX_VALUE}) {
      final double fd = dv;
      probe("double " + fd, () -> desc(new BigDecimal(fd)) + "|" + BigDecimal.valueOf(fd) + "|" + new BigDecimal(fd, new MathContext(10)) + "|" + new BigDecimal(fd, MathContext.DECIMAL32));
    }
    probe("double nan", () -> new BigDecimal(Double.NaN));
    probe("double inf", () -> new BigDecimal(Double.POSITIVE_INFINITY));
    probe("valueOf double nan", () -> BigDecimal.valueOf(Double.NaN));
    probe("int long ctor", () -> desc(new BigDecimal(42)) + " " + desc(new BigDecimal(-42L)) + " " + desc(new BigDecimal(Long.MIN_VALUE)) + " " + desc(new BigDecimal(123456789, new MathContext(4))) + " " + desc(new BigDecimal(12345L, new MathContext(3, RoundingMode.DOWN))));
    probe("valueOf", () -> desc(BigDecimal.valueOf(123L)) + " " + desc(BigDecimal.valueOf(123L, 5)) + " " + desc(BigDecimal.valueOf(-123L, -2)) + " " + desc(BigDecimal.valueOf(0L, 3)) + " " + desc(BigDecimal.valueOf(Long.MIN_VALUE, 1)));
    probe("bigint ctor", () -> desc(new BigDecimal(new BigInteger("123456789012345678901234567890"))) + " " + desc(new BigDecimal(new BigInteger("123456789012345678901234567890"), 10)) + " " + desc(new BigDecimal(new BigInteger("123456789012345678901234567890"), -3)) + " " + desc(new BigDecimal(new BigInteger("123456789012345678901234567890"), new MathContext(5))) + " " + desc(new BigDecimal(new BigInteger("-5"), 2, new MathContext(1))));
    probe("char[] ctor", () -> desc(new BigDecimal("xx12.50yy".toCharArray(), 2, 5)) + " " + desc(new BigDecimal("1.23456E3".toCharArray(), new MathContext(3))) + " " + desc(new BigDecimal("1.23456E3".toCharArray())));
    probe("constants", () -> desc(BigDecimal.ZERO) + desc(BigDecimal.ONE) + desc(BigDecimal.TWO) + desc(BigDecimal.TEN) + " " + MathContext.UNLIMITED + MathContext.DECIMAL32 + MathContext.DECIMAL64 + MathContext.DECIMAL128);
    probe("mathcontext", () -> new MathContext(5) + " " + new MathContext(7, RoundingMode.UP) + " " + new MathContext("precision=9 roundingMode=FLOOR") + " " + new MathContext(5).equals(new MathContext(5)) + " " + MathContext.DECIMAL64.getPrecision() + MathContext.DECIMAL64.getRoundingMode());
    probe("mathcontext bad", () -> new MathContext(-1));
    probe("mathcontext bad string", () -> new MathContext("precision=x roundingMode=UP"));
    probe("mathcontext bad string2", () -> new MathContext("foo"));
    probe("rounding modes", () -> Arrays.toString(RoundingMode.values()) + " " + RoundingMode.valueOf(3) + " " + RoundingMode.valueOf(RoundingMode.HALF_EVEN.ordinal()) + " " + BigDecimal.ROUND_HALF_UP);
    probe("rounding bad", () -> RoundingMode.valueOf(99));
    int n = vals.size();
    for (int i = 0; i < n; i++) {
      BigDecimal a = vals.get(i);
      final int k = i;
      probe("u" + k + " unary", () -> desc(a.negate()) + "|" + desc(a.abs()) + "|" + desc(a.plus()) + "|" + desc(a.plus(new MathContext(3))) + "|" + desc(a.negate(new MathContext(4, RoundingMode.CEILING))) + "|" + desc(a.abs(new MathContext(2))) + "|" + a.ulp() + "|" + desc(a.round(new MathContext(5))) + "|" + desc(a.round(new MathContext(1, RoundingMode.UP))) + "|" + desc(a.round(MathContext.UNLIMITED)));
      probe("u" + k + " scale", () -> { StringBuilder sb = new StringBuilder(); for (int sc : new int[] {-3, 0, 2, 7}) for (RoundingMode rm : RoundingMode.values()) { if (rm == RoundingMode.UNNECESSARY) { try { sb.append(a.setScale(sc, rm)); } catch (ArithmeticException e) { sb.append(e.getMessage()); } } else sb.append(desc(a.setScale(sc, rm))); sb.append(';'); } return sb; });
      probe("u" + k + " move", () -> desc(a.movePointLeft(3)) + "|" + desc(a.movePointRight(3)) + "|" + desc(a.movePointLeft(-2)) + "|" + desc(a.scaleByPowerOfTen(5)) + "|" + desc(a.scaleByPowerOfTen(-5)) + "|" + desc(a.movePointRight(0)) + "|" + desc(a.stripTrailingZeros()));
      probe("u" + k + " conv", () -> { StringBuilder sb = new StringBuilder(); sb.append(a.intValue()).append('|').append(a.longValue()).append('|').append(a.doubleValue()).append('|').append(a.floatValue()).append('|').append(a.toBigInteger()).append('|').append(a.shortValue()).append('|').append(a.byteValue()); try { sb.append('|').append(a.toBigIntegerExact()); } catch (ArithmeticException e) { sb.append('|').append(e.getMessage()); } try { sb.append('|').append(a.intValueExact()); } catch (ArithmeticException e) { sb.append('|').append(e.getMessage()); } try { sb.append('|').append(a.longValueExact()); } catch (ArithmeticException e) { sb.append('|').append(e.getMessage()); } try { sb.append('|').append(a.shortValueExact()); } catch (ArithmeticException e) { sb.append('|').append(e.getMessage()); } try { sb.append('|').append(a.byteValueExact()); } catch (ArithmeticException e) { sb.append('|').append(e.getMessage()); } return sb; });
      probe("u" + k + " pow", () -> { StringBuilder sb = new StringBuilder(); for (int e : new int[] {0, 1, 2, 5, 20}) { sb.append(desc(a.pow(e))).append('|'); } sb.append(desc(a.pow(3, new MathContext(7)))).append('|').append(desc(a.pow(-2, new MathContext(10)))).append('|').append(desc(a.pow(0, new MathContext(10)))); return sb; });
      probe("u" + k + " pow neg", () -> a.pow(-1));
      probe("u" + k + " sqrt", () -> a.signum() < 0 ? "neg" : desc(a.sqrt(new MathContext(20))) + "|" + desc(a.sqrt(new MathContext(5, RoundingMode.DOWN))) + "|" + desc(a.sqrt(new MathContext(3, RoundingMode.UP))));
      probe("u" + k + " sqrt exact", () -> a.signum() < 0 ? "neg" : desc(a.sqrt(MathContext.UNLIMITED)));
    }
    for (int i = 0; i < n; i++) {
      for (int j = 0; j < n; j += 3) {
        BigDecimal a = vals.get(i);
        BigDecimal b = vals.get((j + i) % n);
        final int fi = i, fj = j;
        probe("b" + fi + "_" + fj + " arith", () -> desc(a.add(b)) + "|" + desc(a.subtract(b)) + "|" + desc(a.multiply(b)) + "|" + desc(a.add(b, new MathContext(5))) + "|" + desc(a.subtract(b, new MathContext(3, RoundingMode.FLOOR))) + "|" + desc(a.multiply(b, new MathContext(6, RoundingMode.HALF_DOWN))) + "|" + a.compareTo(b) + "|" + a.equals(b) + "|" + desc(a.min(b)) + "|" + desc(a.max(b)));
        probe("b" + fi + "_" + fj + " div", () -> { if (b.signum() == 0) return a.divide(b, MathContext.DECIMAL64); StringBuilder sb = new StringBuilder(); sb.append(desc(a.divide(b, MathContext.DECIMAL64))).append('|').append(desc(a.divide(b, 5, RoundingMode.HALF_UP))).append('|').append(desc(a.divide(b, RoundingMode.HALF_EVEN))).append('|').append(desc(a.divide(b, 3, RoundingMode.DOWN))).append('|').append(desc(a.divide(b, new MathContext(12, RoundingMode.CEILING)))); try { sb.append('|').append(desc(a.divide(b))); } catch (ArithmeticException e) { sb.append('|').append(e.getMessage()); } sb.append('|').append(desc(a.divideToIntegralValue(b))).append('|').append(desc(a.remainder(b))).append('|').append(desc(a.divideToIntegralValue(b, new MathContext(5)))).append('|').append(desc(a.remainder(b, new MathContext(5)))); BigDecimal[] qr = a.divideAndRemainder(b); sb.append('|').append(desc(qr[0])).append('|').append(desc(qr[1])); return sb; });
      }
    }
    probe("div zero", () -> BigDecimal.ONE.divide(BigDecimal.ZERO));
    probe("div zero0", () -> BigDecimal.ZERO.divide(BigDecimal.ZERO));
    probe("div zero mc", () -> BigDecimal.ONE.divide(BigDecimal.ZERO, MathContext.DECIMAL32));
    probe("div zero0 mc", () -> BigDecimal.ZERO.divide(BigDecimal.ZERO, MathContext.DECIMAL32));
    probe("div nonterminating", () -> BigDecimal.ONE.divide(new BigDecimal(3)));
    probe("div nonterminating unlimited", () -> BigDecimal.ONE.divide(new BigDecimal(3), MathContext.UNLIMITED));
    probe("div exact", () -> desc(BigDecimal.ONE.divide(new BigDecimal(8))) + " " + desc(new BigDecimal("100").divide(new BigDecimal("4"))) + " " + desc(new BigDecimal("1.00").divide(new BigDecimal("0.5"))) + " " + desc(BigDecimal.ZERO.divide(new BigDecimal("0.001"))));
    probe("div unnecessary", () -> new BigDecimal("1").divide(new BigDecimal("3"), 2, RoundingMode.UNNECESSARY));
    probe("setScale unnecessary", () -> new BigDecimal("1.25").setScale(1));
    probe("setScale old", () -> new BigDecimal("1.25").setScale(1, BigDecimal.ROUND_HALF_UP));
    probe("setScale old bad", () -> new BigDecimal("1.25").setScale(1, 99));
    probe("divide old", () -> new BigDecimal("1.25").divide(new BigDecimal("3"), 4, BigDecimal.ROUND_HALF_DOWN) + " " + new BigDecimal("1.25").divide(new BigDecimal("3"), BigDecimal.ROUND_UP));
    probe("equals scale", () -> new BigDecimal("2.0").equals(new BigDecimal("2.00")) + " " + (new BigDecimal("2.0").compareTo(new BigDecimal("2.00")) == 0) + " " + new BigDecimal("2.0").hashCode() + " " + new BigDecimal("2.00").hashCode());
    probe("sqrt special", () -> desc(BigDecimal.ZERO.sqrt(new MathContext(5))) + " " + desc(new BigDecimal("0.00").sqrt(MathContext.DECIMAL32)) + " " + desc(new BigDecimal("4").sqrt(MathContext.UNLIMITED)) + " " + desc(new BigDecimal("2").sqrt(MathContext.DECIMAL128)) + " " + desc(new BigDecimal("1E+10").sqrt(MathContext.DECIMAL64)) + " " + desc(new BigDecimal("0.0001").sqrt(MathContext.DECIMAL64)) + " " + desc(new BigDecimal("144").sqrt(new MathContext(1))));
    probe("sqrt neg", () -> new BigDecimal("-1").sqrt(MathContext.DECIMAL32));
    probe("sqrt inexact unlimited", () -> new BigDecimal("2").sqrt(MathContext.UNLIMITED));
    probe("tostring forms", () -> { StringBuilder sb = new StringBuilder(); for (String s : new String[] {"1E+3", "1.23E+10", "1.23E-10", "0.00000123", "0.0000001", "123E5", "12.3E-2", "-1E-7", "0E+5", "100", "1000000000000000000000"}) { BigDecimal d = new BigDecimal(s); sb.append(d).append('/').append(d.toPlainString()).append('/').append(d.toEngineeringString()).append(' '); } return sb; });
    probe("serialization", () -> { ByteArrayOutputStream bo = new ByteArrayOutputStream(); ObjectOutputStream os = new ObjectOutputStream(bo); os.writeObject(new BigDecimal("-123.4500")); os.writeObject(new BigInteger("-123456789012345678901234567890")); os.writeObject(BigInteger.ZERO); os.writeObject(new MathContext(7, RoundingMode.FLOOR)); os.close(); byte[] b = bo.toByteArray(); ObjectInputStream is = new ObjectInputStream(new ByteArrayInputStream(b)); return HexFormat.of().formatHex(b) + " " + is.readObject() + " " + is.readObject() + " " + is.readObject() + " " + is.readObject(); });
    probe("rounding table", () -> { StringBuilder sb = new StringBuilder(); for (String s : new String[] {"5.5", "2.5", "1.6", "1.1", "1.0", "-1.0", "-1.1", "-1.6", "-2.5", "-5.5", "0.5", "-0.5"}) { for (RoundingMode rm : RoundingMode.values()) { try { sb.append(new BigDecimal(s).setScale(0, rm)); } catch (ArithmeticException e) { sb.append('X'); } sb.append(','); } sb.append(' '); } return sb; });
    probe("precision big", () -> { BigDecimal d = new BigDecimal(BigInteger.TEN.pow(200)); return d.precision() + " " + d.add(BigDecimal.ONE).precision() + " " + d.subtract(BigDecimal.ONE).precision() + " " + new BigDecimal("0.000").precision() + " " + BigDecimal.valueOf(Long.MIN_VALUE).precision(); });
    probe("big ops", () -> { BigDecimal x = new BigDecimal("1.000000000000000000000000000001"); BigDecimal y = x.pow(300); return y.precision() + " " + y.scale() + " " + y.round(new MathContext(50)) + " " + BigDecimal.ONE.divide(x, 100, RoundingMode.HALF_EVEN); });
    probe("sum", () -> { BigDecimal s = BigDecimal.ZERO; for (int i = 1; i <= 200; i++) s = s.add(BigDecimal.ONE.divide(BigDecimal.valueOf(i), 30, RoundingMode.HALF_UP)); return s; });
    probe("double exact", () -> new BigDecimal("1.23456789012345678901234567890").doubleValue() + " " + new BigDecimal("1E400").doubleValue() + " " + new BigDecimal("-1E400").doubleValue() + " " + new BigDecimal("1E-400").doubleValue() + " " + new BigDecimal("4.9E-324").doubleValue() + " " + new BigDecimal("2.4703282292062327E-324").doubleValue() + " " + new BigDecimal("2.4703282292062328E-324").doubleValue() + " " + new BigDecimal("9007199254740993").doubleValue() + " " + new BigDecimal("1E39").floatValue() + " " + new BigDecimal("0.1").floatValue());
  }
}
