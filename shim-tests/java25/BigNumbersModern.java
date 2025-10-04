/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 * Minimum release: 21
 */

import java.math.*;
import java.util.*;

public class BigNumbersModern {
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

  static BigInteger rnd(Random r, int bits) {
    BigInteger b = new BigInteger(bits, r);
    return r.nextBoolean() ? b.negate() : b;
  }

  public static void main(String[] args) throws Exception {
    Locale.setDefault(Locale.US);
    Random r = new Random(12345);
    List<BigInteger> vals = new ArrayList<>();
    for (int bits : new int[] {0, 1, 7, 31, 32, 33, 63, 64, 65, 100, 128, 200, 500, 1000, 2000, 5000}) {
      vals.add(rnd(r, bits));
      vals.add(rnd(r, bits));
    }
    vals.addAll(List.of(BigInteger.ZERO, BigInteger.ONE, BigInteger.TWO, BigInteger.TEN, BigInteger.valueOf(-1), BigInteger.valueOf(Long.MIN_VALUE), BigInteger.valueOf(Long.MAX_VALUE), BigInteger.ONE.shiftLeft(64), BigInteger.ONE.shiftLeft(64).negate(), BigInteger.ONE.shiftLeft(1000).subtract(BigInteger.ONE)));
    long h = 0;
    int idx = 0;
    for (BigInteger a : vals) {
      StringBuilder sb = new StringBuilder();
      sb.append(a.toString(16)).append('|').append(a.toString(7)).append('|').append(a.signum()).append('|').append(a.bitLength()).append('|').append(a.bitCount()).append('|').append(a.getLowestSetBit()).append('|').append(a.hashCode()).append('|').append(a.longValue()).append('|').append(a.intValue()).append('|').append(a.shortValue()).append('|').append(a.byteValue()).append('|').append(a.doubleValue()).append('|').append(a.floatValue()).append('|').append(Arrays.toString(a.toByteArray()).hashCode());
      probe("v" + idx + " basic", () -> sb);
      probe("v" + idx + " shifts", () -> a.shiftLeft(13).toString(16) + "|" + a.shiftRight(13).toString(16) + "|" + a.shiftLeft(-5).toString(16) + "|" + a.shiftRight(-70).toString(16) + "|" + a.shiftRight(10000).toString() + "|" + a.not().toString(16) + "|" + a.testBit(3) + a.testBit(70) + "|" + a.setBit(100).toString(16) + "|" + a.clearBit(2).toString(16) + "|" + a.flipBit(65).toString(16));
      probe("v" + idx + " valueExact", () -> { StringBuilder s = new StringBuilder(); try { s.append(a.longValueExact()); } catch (ArithmeticException e) { s.append(e.getMessage()); } try { s.append(a.intValueExact()); } catch (ArithmeticException e) { s.append(e.getMessage()); } try { s.append(a.shortValueExact()); } catch (ArithmeticException e) { s.append(e.getMessage()); } try { s.append(a.byteValueExact()); } catch (ArithmeticException e) { s.append(e.getMessage()); } return s; });
      probe("v" + idx + " sqrt", () -> a.signum() < 0 ? "neg" : a.sqrt() + "|" + Arrays.toString(a.sqrtAndRemainder()));
      probe("v" + idx + " pow", () -> a.pow(3).toString(16) + "|" + a.pow(0) + "|" + a.abs().pow(17).bitLength());
      probe("v" + idx + " prime", () -> a.isProbablePrime(50) + "|" + (a.bitLength() < 400 && a.signum() > 0 ? a.nextProbablePrime().toString(16) : "skip"));
      idx++;
    }
    for (int i = 0; i + 1 < vals.size(); i++) {
      BigInteger a = vals.get(i), b = vals.get(i + 1);
      final int k = i;
      probe("p" + k + " arith", () -> a.add(b).toString(16) + "|" + a.subtract(b).toString(16) + "|" + a.multiply(b).toString(16) + "|" + a.and(b).toString(16) + "|" + a.or(b).toString(16) + "|" + a.xor(b).toString(16) + "|" + a.andNot(b).toString(16) + "|" + a.min(b).toString(16) + "|" + a.max(b).toString(16) + "|" + a.compareTo(b) + "|" + a.equals(b) + "|" + a.gcd(b).toString(16));
      probe("p" + k + " div", () -> { if (b.signum() == 0) return a.divide(b); BigInteger[] qr = a.divideAndRemainder(b); return qr[0].toString(16) + "|" + qr[1].toString(16) + "|" + a.divide(b).toString(16) + "|" + a.remainder(b).toString(16) + "|" + (b.signum() > 0 ? a.mod(b).toString(16) : "nm"); });
      probe("p" + k + " modInverse", () -> b.signum() > 0 ? a.modInverse(b).toString(16) : "nm");
      probe("p" + k + " modPow", () -> { BigInteger m = b.abs().add(BigInteger.ONE); BigInteger e = a.abs().mod(BigInteger.valueOf(1000)); return b.modPow(e, m).toString(16) + "|" + (m.testBit(0) ? a.modPow(a.abs().mod(BigInteger.ONE.shiftLeft(160)), m).toString(16) : "even") + "|" + b.modPow(e.negate(), m.gcd(b).equals(BigInteger.ONE) ? m : BigInteger.valueOf(7)).toString(16); });
      probe("p" + k + " parallelMultiply", () -> a.parallelMultiply(b).equals(a.multiply(b)));
    }
    probe("sqrt neg", () -> BigInteger.valueOf(-4).sqrt());
    probe("modInverse not invertible", () -> BigInteger.valueOf(4).modInverse(BigInteger.valueOf(8)));
    probe("modInverse nonpositive", () -> BigInteger.valueOf(4).modInverse(BigInteger.ZERO));
    probe("mod neg", () -> BigInteger.valueOf(4).mod(BigInteger.valueOf(-3)));
    probe("pow neg", () -> BigInteger.TEN.pow(-1));
    probe("parse", () -> new BigInteger("-123456789012345678901234567890") + " " + new BigInteger("ff", 16) + " " + new BigInteger("+7") + " " + new BigInteger("0000"));
    probe("parse bad", () -> new BigInteger("12x"));
    probe("parse empty", () -> new BigInteger(""));
    probe("parse radix", () -> new BigInteger("1", 99));
    probe("bytes ctor", () -> new BigInteger(new byte[] {(byte) 0xff, 0}) + " " + new BigInteger(1, new byte[] {(byte) 0xff, 0}) + " " + new BigInteger(-1, new byte[] {1, 2, 3}) + " " + new BigInteger(0, new byte[0]));
    probe("bytes ctor bad", () -> new BigInteger(new byte[0]));
    probe("signum ctor bad", () -> new BigInteger(2, new byte[] {1}));
    probe("zero signum ctor bad", () -> new BigInteger(0, new byte[] {1}));
    probe("random ctor", () -> new BigInteger(300, new Random(5)).toString(16) + " " + BigInteger.probablePrime(64, new Random(7)) + " " + BigInteger.probablePrime(256, new Random(8)).bitLength());
    probe("probablePrime small", () -> BigInteger.probablePrime(1, new Random(1)));
    probe("probablePrime 2", () -> BigInteger.probablePrime(2, new Random(1)) + " " + BigInteger.probablePrime(3, new Random(2)));
    probe("known primes", () -> BigInteger.valueOf(2).isProbablePrime(10) + " " + BigInteger.valueOf(97).isProbablePrime(10) + " " + BigInteger.valueOf(561).isProbablePrime(100) + " " + BigInteger.ONE.shiftLeft(127).subtract(BigInteger.ONE).isProbablePrime(50) + " " + BigInteger.ONE.shiftLeft(128).add(BigInteger.ONE).isProbablePrime(50) + " " + BigInteger.ZERO.isProbablePrime(1) + " " + BigInteger.valueOf(-7).isProbablePrime(10) + " " + BigInteger.valueOf(15).isProbablePrime(0));
    probe("nextProbablePrime", () -> BigInteger.valueOf(1).nextProbablePrime() + " " + BigInteger.valueOf(100).nextProbablePrime() + " " + BigInteger.valueOf(Long.MAX_VALUE).nextProbablePrime() + " " + BigInteger.valueOf(0).nextProbablePrime());
    probe("nextProbablePrime neg", () -> BigInteger.valueOf(-1).nextProbablePrime());
    probe("equals/hash", () -> BigInteger.valueOf(5).equals(BigInteger.valueOf(5)) + " " + BigInteger.valueOf(5).equals(5) + " " + new BigInteger("123456789123456789123456789").hashCode());
    probe("doubles", () -> { StringBuilder s = new StringBuilder(); for (String t : new String[] {"9007199254740993", "9007199254740992", "18014398509481985", "340282366920938463463374607431768211455", "1" + "0".repeat(400), "-1" + "0".repeat(309), "179769313486231580793728971405303415079934132710037826936173778980444968292764750946649017977587207096330286416692887910946555547851940402630657488671505820681908902000708383676273854845817711531764475730270069855571366959622842914819860834936475292719074168444365510704342711559699508093042880177904174497791"}) { BigInteger b = new BigInteger(t); s.append(b.doubleValue()).append('/').append(b.floatValue()).append(' '); } return s; });
    probe("toString radix bad", () -> BigInteger.TEN.toString(1) + " " + BigInteger.TEN.toString(40));
    probe("constants", () -> BigInteger.ZERO + " " + BigInteger.ONE + " " + BigInteger.TWO + " " + BigInteger.TEN + " " + BigInteger.valueOf(-16).toString(2));
    probe("big mult", () -> { BigInteger x = BigInteger.ONE.shiftLeft(20000).subtract(BigInteger.ONE); BigInteger y = x.multiply(x).add(x); return y.bitLength() + " " + y.mod(BigInteger.valueOf(1000003)) + " " + y.toString().length() + " " + y.hashCode(); });
    probe("big div", () -> { BigInteger x = BigInteger.valueOf(3).pow(5000); BigInteger y = BigInteger.valueOf(7).pow(1500); BigInteger[] qr = x.divideAndRemainder(y); return qr[0].bitLength() + " " + qr[1].bitLength() + " " + qr[0].mod(BigInteger.valueOf(1000003)) + " " + qr[1].mod(BigInteger.valueOf(1000003)); });
    probe("factorial", () -> { BigInteger f = BigInteger.ONE; for (int i = 2; i <= 500; i++) f = f.multiply(BigInteger.valueOf(i)); return f.toString().hashCode() + " " + f.bitLength() + " " + f.getLowestSetBit(); });
  }
}
