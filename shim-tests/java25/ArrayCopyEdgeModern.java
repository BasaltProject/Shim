/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 * Minimum release: 21
 */

public class ArrayCopyEdgeModern {
  static void t(String n, Runnable r) {
    try {
      r.run();
      System.out.println(n + " ok");
    } catch (Throwable e) {
      System.out.println(n + " " + e.getClass().getName() + ": " + e.getMessage());
    }
  }
  public static void main(String[] args) {
    int[] a = new int[5];
    int[] b = new int[5];
    long[] l = new long[5];
    Object[] o = new Object[5];
    String[] s = new String[5];
    Integer[] I = new Integer[5];
    Object str = "s";
    Object[] mix = {"a", 1, "b"};
    t("srcneg",()->System.arraycopy(a,-1,b,0,1));
    t("dstneg",()->System.arraycopy(a,0,b,-2,1));
    t("lenneg",()->System.arraycopy(a,0,b,0,-3));
    t("srcend",()->System.arraycopy(a,3,b,0,4));
    t("dstend",()->System.arraycopy(a,0,b,3,4));
    t("srcpast",()->System.arraycopy(a,6,b,0,0));
    t("dstpast",()->System.arraycopy(a,0,b,6,0));
    t("srcneg0",()->System.arraycopy(a,-1,b,0,0));
    t("oneg",()->System.arraycopy(o,-1,o,0,1));
    t("oend",()->System.arraycopy(o,3,o,0,4));
    t("odst",()->System.arraycopy(o,0,o,3,4));
    t("prim",()->System.arraycopy(a,0,l,0,1));
    t("primobj",()->System.arraycopy(a,0,o,0,1));
    t("objprim",()->System.arraycopy(o,0,a,0,1));
    t("srcnotarr",()->System.arraycopy(str,0,o,0,1));
    t("dstnotarr",()->System.arraycopy(o,0,str,0,1));
    t("srcnull",()->System.arraycopy(null,0,o,0,1));
    t("dstnull",()->System.arraycopy(o,0,null,0,1));
    t("elem",()->System.arraycopy(mix,0,s,0,3));
    t("elemnull",()->System.arraycopy(new Object[] {null,null},0,s,0,2));
    t("elem2",()->System.arraycopy(new Object[] {"a",1},0,I,0,2));
    t("elem3",()->System.arraycopy(o,0,s,0,1));
    t("slon",()->System.arraycopy(s,0,I,0,1));
    t("slon0",()->System.arraycopy(s,0,I,0,0));
    t("srcobj",()->System.arraycopy(str,0,str,0,0));
    t("bool",()->System.arraycopy(new boolean[2],0,new byte[2],0,1));
    t("nested",()->System.arraycopy(new int[2][2],0,new Object[2],0,2));
    t("nestedbad",()->System.arraycopy(new Object[] {new int[1],"x"},0,new int[2][],0,2));
  }
}
