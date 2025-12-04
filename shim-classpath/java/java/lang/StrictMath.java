/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Modifications licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.lang;

public final class StrictMath {
  public static final double E = Math.E;
  public static final double PI = Math.PI;
  public static final double TAU = Math.TAU;

  private StrictMath() { }

  public static double sin(double a) { return shim.FdLibm.sin(a); }
  public static double cos(double a) { return shim.FdLibm.cos(a); }
  public static double tan(double a) { return Math.tan(a); }
  public static double cosh(double a) { return shim.FdLibm.cosh(a); }
  public static double sinh(double a) { return Math.sinh(a); }
  public static double tanh(double a) { return Math.tanh(a); }
  public static double asin(double a) { return Math.asin(a); }
  public static double acos(double a) { return Math.acos(a); }
  public static double atan(double a) { return shim.FdLibm.atan(a); }
  public static double atan2(double y, double x) { return shim.FdLibm.atan2(y, x); }
  public static double exp(double a) { return shim.FdLibm.exp(a); }
  public static double log(double a) { return Math.log(a); }
  public static double sqrt(double a) { return Math.sqrt(a); }
  public static double pow(double a, double b) { return shim.FdLibm.pow(a, b); }
  public static double ceil(double a) { return Math.ceil(a); }
  public static double floor(double a) { return Math.floor(a); }
  public static int round(float a) { return Math.round(a); }
  public static long round(double a) { return Math.round(a); }
  public static double random() { return Math.random(); }
  public static int addExact(int x, int y) { return Math.addExact(x, y); }
  public static long addExact(long x, long y) { return Math.addExact(x, y); }
  public static int subtractExact(int x, int y) { return Math.subtractExact(x, y); }
  public static long subtractExact(long x, long y) { return Math.subtractExact(x, y); }
  public static int multiplyExact(int x, int y) { return Math.multiplyExact(x, y); }
  public static long multiplyExact(long x, int y) { return Math.multiplyExact(x, y); }
  public static long multiplyExact(long x, long y) { return Math.multiplyExact(x, y); }
  public static int incrementExact(int a) { return Math.incrementExact(a); }
  public static long incrementExact(long a) { return Math.incrementExact(a); }
  public static int decrementExact(int a) { return Math.decrementExact(a); }
  public static long decrementExact(long a) { return Math.decrementExact(a); }
  public static int negateExact(int a) { return Math.negateExact(a); }
  public static long negateExact(long a) { return Math.negateExact(a); }
  public static int toIntExact(long value) { return Math.toIntExact(value); }
  public static long multiplyHigh(long x, long y) { return Math.multiplyHigh(x, y); }
  public static long unsignedMultiplyHigh(long x, long y) { return Math.unsignedMultiplyHigh(x, y); }
  public static int floorDiv(int x, int y) { return Math.floorDiv(x, y); }
  public static int floorDivExact(int x, int y) { return Math.floorDivExact(x, y); }
  public static long floorDiv(long x, int y) { return Math.floorDiv(x, y); }
  public static long floorDiv(long x, long y) { return Math.floorDiv(x, y); }
  public static long floorDivExact(long x, long y) { return Math.floorDivExact(x, y); }
  public static int floorMod(int x, int y) { return Math.floorMod(x, y); }
  public static int floorMod(long x, int y) { return Math.floorMod(x, y); }
  public static long floorMod(long x, long y) { return Math.floorMod(x, y); }
  public static int ceilDiv(int x, int y) { return Math.ceilDiv(x, y); }
  public static int ceilDivExact(int x, int y) { return Math.ceilDivExact(x, y); }
  public static long ceilDiv(long x, int y) { return Math.ceilDiv(x, y); }
  public static long ceilDiv(long x, long y) { return Math.ceilDiv(x, y); }
  public static long ceilDivExact(long x, long y) { return Math.ceilDivExact(x, y); }
  public static int ceilMod(int x, int y) { return Math.ceilMod(x, y); }
  public static int ceilMod(long x, int y) { return Math.ceilMod(x, y); }
  public static long ceilMod(long x, long y) { return Math.ceilMod(x, y); }
  public static int divideExact(int x, int y) { return Math.divideExact(x, y); }
  public static long divideExact(long x, long y) { return Math.divideExact(x, y); }
  public static int abs(int a) { return Math.abs(a); }
  public static long abs(long a) { return Math.abs(a); }
  public static float abs(float a) { return Math.abs(a); }
  public static double abs(double a) { return Math.abs(a); }
  public static int max(int a, int b) { return Math.max(a, b); }
  public static long max(long a, long b) { return Math.max(a, b); }
  public static float max(float a, float b) { return Math.max(a, b); }
  public static double max(double a, double b) { return Math.max(a, b); }
  public static int min(int a, int b) { return Math.min(a, b); }
  public static long min(long a, long b) { return Math.min(a, b); }
  public static float min(float a, float b) { return Math.min(a, b); }
  public static double min(double a, double b) { return Math.min(a, b); }
  public static double fma(double a, double b, double c) { return Math.fma(a, b, c); }
  public static float fma(float a, float b, float c) { return Math.fma(a, b, c); }
  public static int clamp(long value, int min, int max) { return Math.clamp(value, min, max); }
  public static long clamp(long value, long min, long max) { return Math.clamp(value, min, max); }
  public static float clamp(float value, float min, float max) { return Math.clamp(value, min, max); }
  public static double clamp(double value, double min, double max) { return Math.clamp(value, min, max); }
  public static double ulp(double d) { return Math.ulp(d); }
  public static float ulp(float f) { return Math.ulp(f); }
  public static double signum(double d) { return Math.signum(d); }
  public static float signum(float f) { return Math.signum(f); }
  public static double copySign(double magnitude, double sign) { return Math.copySign(magnitude, sign); }
  public static float copySign(float magnitude, float sign) { return Math.copySign(magnitude, sign); }
  public static double nextUp(double value) { return Math.nextUp(value); }
  public static double nextDown(double value) { return Math.nextDown(value); }
  public static double nextAfter(double start, double direction) { return Math.nextAfter(start, direction); }
  public static int getExponent(float f) { return Math.getExponent(f); }
  public static int getExponent(double d) { return Math.getExponent(d); }
  public static double log10(double a) { return Math.log10(a); }
  public static double rint(double a) { return Math.rint(a); }
  public static double cbrt(double a) { return shim.FdLibm.cbrt(a); }
  public static double scalb(double d, int scaleFactor) { return Math.scalb(d, scaleFactor); }
  public static float scalb(float f, int scaleFactor) { return Math.scalb(f, scaleFactor); }
  public static double IEEEremainder(double f1, double f2) { return Math.IEEEremainder(f1, f2); }
  public static double hypot(double x, double y) { return shim.FdLibm.hypot(x, y); }
  public static double expm1(double x) { return Math.expm1(x); }
  public static double log1p(double x) { return Math.log1p(x); }
  public static int unsignedMultiplyExact(int x, int y) { return Math.unsignedMultiplyExact(x, y); }
  public static long unsignedMultiplyExact(long x, int y) { return Math.unsignedMultiplyExact(x, y); }
  public static long unsignedMultiplyExact(long x, long y) { return Math.unsignedMultiplyExact(x, y); }
  public static double toRadians(double angdeg) { return Math.toRadians(angdeg); }
  public static double toDegrees(double angrad) { return Math.toDegrees(angrad); }
  public static long multiplyFull(int x, int y) { return Math.multiplyFull(x, y); }
  public static int absExact(int a) { return Math.absExact(a); }
  public static long absExact(long a) { return Math.absExact(a); }
  public static float nextAfter(float start, double direction) { return Math.nextAfter(start, direction); }
  public static float nextUp(float f) { return Math.nextUp(f); }
  public static float nextDown(float f) { return Math.nextDown(f); }
  public static int powExact(int x, int n) { return Math.powExact(x, n); }
  public static long powExact(long x, int n) { return Math.powExact(x, n); }
  public static int unsignedPowExact(int x, int n) { return Math.unsignedPowExact(x, n); }
  public static long unsignedPowExact(long x, int n) { return Math.unsignedPowExact(x, n); }
}
