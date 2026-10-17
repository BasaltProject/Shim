/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 * Minimum release: 21
 */

import java.lang.invoke.*;
import java.util.concurrent.Callable;
public class VarHandleBitwiseModern {
  int i; long l; short s; byte b; char c; boolean z; float f; double d; String str;
  static int si;
  static void t(String n, Callable<Object> c) { try { System.out.println(n + ": " + c.call()); } catch (Throwable e) { System.out.println(n + ": !" + e.getClass().getName() + ":" + (e instanceof NullPointerException ? null : e.getMessage())); } }
  public static void main(String[] a) throws Throwable {
    MethodHandles.Lookup lk = MethodHandles.lookup();
    VarHandleBitwiseModern o = new VarHandleBitwiseModern();
    VarHandle vi = lk.findVarHandle(VarHandleBitwiseModern.class, "i", int.class), vl = lk.findVarHandle(VarHandleBitwiseModern.class, "l", long.class), vs = lk.findVarHandle(VarHandleBitwiseModern.class, "s", short.class), vb = lk.findVarHandle(VarHandleBitwiseModern.class, "b", byte.class), vc = lk.findVarHandle(VarHandleBitwiseModern.class, "c", char.class), vz = lk.findVarHandle(VarHandleBitwiseModern.class, "z", boolean.class), vf = lk.findVarHandle(VarHandleBitwiseModern.class, "f", float.class), vstr = lk.findVarHandle(VarHandleBitwiseModern.class, "str", String.class);
    VarHandle vsi = lk.findStaticVarHandle(VarHandleBitwiseModern.class, "si", int.class);
    VarHandle arr = MethodHandles.arrayElementVarHandle(int[].class);
    int[] ia = new int[3];
    t("or", () -> { o.i = 0b0101; int old = (int) vi.getAndBitwiseOr(o, 0b0011); return old + " " + o.i; });
    t("and", () -> { int old = (int) vi.getAndBitwiseAnd(o, 0b0110); return old + " " + o.i; });
    t("xor", () -> { int old = (int) vi.getAndBitwiseXor(o, 0b1111); return old + " " + o.i; });
    t("orAcq", () -> { int old = (int) vi.getAndBitwiseOrAcquire(o, 1); int o2 = (int) vi.getAndBitwiseOrRelease(o, 16); int o3 = (int) vi.getAndBitwiseAndAcquire(o, 0xff); int o4 = (int) vi.getAndBitwiseAndRelease(o, 0xff); int o5 = (int) vi.getAndBitwiseXorAcquire(o, 3); int o6 = (int) vi.getAndBitwiseXorRelease(o, 3); return old + " " + o2 + " " + o3 + " " + o4 + " " + o5 + " " + o6 + " " + o.i; });
    t("long", () -> { o.l = 1L << 40; long old = (long) vl.getAndBitwiseOr(o, 7L); return old + " " + o.l; });
    t("short", () -> { o.s = 12; short old = (short) vs.getAndBitwiseXor(o, (short) 5); return old + " " + o.s; });
    t("byte", () -> { o.b = 12; byte old = (byte) vb.getAndBitwiseAnd(o, (byte) 5); return old + " " + o.b; });
    t("char", () -> { o.c = 'a'; char old = (char) vc.getAndBitwiseOr(o, (char) 1); return old + " " + o.c; });
    t("bool", () -> { o.z = true; boolean old = (boolean) vz.getAndBitwiseXor(o, true); return old + " " + o.z; });
    t("float", () -> vf.getAndBitwiseOr(o, 1.0f));
    t("string", () -> vstr.getAndBitwiseOr(o, "x"));
    t("static", () -> { si = 6; int old = (int) vsi.getAndBitwiseOr(3); return old + " " + si; });
    t("array", () -> { ia[1] = 8; int old = (int) arr.getAndBitwiseOr(ia, 1, 1); return old + " " + ia[1]; });
    t("fences", () -> { VarHandle.fullFence(); VarHandle.acquireFence(); VarHandle.releaseFence(); VarHandle.loadLoadFence(); VarHandle.storeStoreFence(); return "ok"; });
    t("exact", () -> vi.hasInvokeExactBehavior() + " " + vi.withInvokeExactBehavior().hasInvokeExactBehavior() + " " + vi.withInvokeExactBehavior().withInvokeBehavior().hasInvokeExactBehavior());
    t("exactSame", () -> vi.withInvokeBehavior() == vi);
    t("exactUse", () -> { VarHandle e = vi.withInvokeExactBehavior(); o.i = 5; return (int) e.get(o); });
    t("types", () -> vi.varType() + " " + vi.coordinateTypes() + " " + vi.accessModeType(VarHandle.AccessMode.GET_AND_BITWISE_OR) + " " + vi.isAccessModeSupported(VarHandle.AccessMode.GET_AND_BITWISE_OR) + " " + vf.isAccessModeSupported(VarHandle.AccessMode.GET_AND_BITWISE_OR) + " " + vstr.isAccessModeSupported(VarHandle.AccessMode.GET_AND_ADD));
  }
}
