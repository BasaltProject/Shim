/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

// Minimum release: 21

import java.lang.constant.ClassDesc;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class VarHandleDescriptorsAndViewsModern {

  public static class SampleTarget {
    public static int staticIntField = 42;
    public String instanceStrField = "initial";
  }

  public static void main(String[] args) throws Throwable {
    testVarHandleDescriptors();
    testByteArrayViewVarHandle();
    testByteBufferViewVarHandle();
    System.out.println("VarHandleDescriptorsAndViewsModern passed successfully");
  }

  private static void testVarHandleDescriptors() throws Throwable {
    MethodHandles.Lookup lookup = MethodHandles.lookup();

    VarHandle staticVh = lookup.findStaticVarHandle(SampleTarget.class, "staticIntField", int.class);
    if (!staticVh.describeConstable().isPresent()) {
      throw new AssertionError("staticVh describeConstable not present");
    }
    VarHandle.VarHandleDesc staticDesc = staticVh.describeConstable().get();
    String staticDescStr = staticDesc.toString();
    System.out.println("staticDesc: " + staticDescStr);
    if (!staticDescStr.contains("static") || !staticDescStr.contains("staticIntField")) {
      throw new AssertionError("unexpected static desc: " + staticDescStr);
    }
    VarHandle resolvedStaticVh = staticDesc.resolveConstantDesc(lookup);
    MethodHandle staticGet = resolvedStaticVh.toMethodHandle(VarHandle.AccessMode.GET);
    MethodHandle staticSet = resolvedStaticVh.toMethodHandle(VarHandle.AccessMode.SET);
    if ((int) staticGet.invokeWithArguments() != 42) {
      throw new AssertionError("resolved static get mismatch");
    }
    staticSet.invokeWithArguments(99);
    if ((int) staticGet.invokeWithArguments() != 99) {
      throw new AssertionError("resolved static set mismatch");
    }

    VarHandle instanceVh = lookup.findVarHandle(SampleTarget.class, "instanceStrField", String.class);
    if (!instanceVh.describeConstable().isPresent()) {
      throw new AssertionError("instanceVh describeConstable not present");
    }
    VarHandle.VarHandleDesc instanceDesc = instanceVh.describeConstable().get();
    String instanceDescStr = instanceDesc.toString();
    System.out.println("instanceDesc: " + instanceDescStr);
    if (!instanceDescStr.contains("instanceStrField")) {
      throw new AssertionError("unexpected instance desc: " + instanceDescStr);
    }
    VarHandle resolvedInstanceVh = instanceDesc.resolveConstantDesc(lookup);
    MethodHandle instanceGet = resolvedInstanceVh.toMethodHandle(VarHandle.AccessMode.GET);
    MethodHandle instanceSet = resolvedInstanceVh.toMethodHandle(VarHandle.AccessMode.SET);
    SampleTarget target = new SampleTarget();
    if (!"initial".equals(instanceGet.invokeWithArguments(target))) {
      throw new AssertionError("resolved instance get mismatch");
    }
    instanceSet.invokeWithArguments(target, "updated");
    if (!"updated".equals(target.instanceStrField)) {
      throw new AssertionError("resolved instance set mismatch");
    }

    VarHandle arrayVh = MethodHandles.arrayElementVarHandle(int[].class);
    if (!arrayVh.describeConstable().isPresent()) {
      throw new AssertionError("arrayVh describeConstable not present");
    }
    VarHandle.VarHandleDesc arrayDesc = arrayVh.describeConstable().get();
    String arrayDescStr = arrayDesc.toString();
    System.out.println("arrayDesc: " + arrayDescStr);
    VarHandle resolvedArrayVh = arrayDesc.resolveConstantDesc(lookup);
    MethodHandle arrayGet = resolvedArrayVh.toMethodHandle(VarHandle.AccessMode.GET);
    MethodHandle arraySet = resolvedArrayVh.toMethodHandle(VarHandle.AccessMode.SET);
    int[] array = new int[] { 10, 20, 30 };
    if ((int) arrayGet.invokeWithArguments(array, 1) != 20) {
      throw new AssertionError("resolved array get mismatch");
    }
    arraySet.invokeWithArguments(array, 1, 77);
    if (array[1] != 77) {
      throw new AssertionError("resolved array set mismatch");
    }

    VarHandle.VarHandleDesc customFieldDesc = VarHandle.VarHandleDesc.ofField(
        ClassDesc.of("VarHandleDescriptorsAndViewsModern$SampleTarget"),
        "instanceStrField",
        ClassDesc.of("java.lang.String"));
    if (!customFieldDesc.varType().displayName().equals("String")) {
      throw new AssertionError("customFieldDesc varType mismatch: " + customFieldDesc.varType());
    }
  }

  private static void testByteArrayViewVarHandle() throws Throwable {
    VarHandle vhIntBE = MethodHandles.byteArrayViewVarHandle(int[].class, ByteOrder.BIG_ENDIAN);
    VarHandle vhIntLE = MethodHandles.byteArrayViewVarHandle(int[].class, ByteOrder.LITTLE_ENDIAN);

    MethodHandle getIntBE = vhIntBE.toMethodHandle(VarHandle.AccessMode.GET);
    MethodHandle setIntBE = vhIntBE.toMethodHandle(VarHandle.AccessMode.SET);
    MethodHandle getIntLE = vhIntLE.toMethodHandle(VarHandle.AccessMode.GET);
    MethodHandle setIntLE = vhIntLE.toMethodHandle(VarHandle.AccessMode.SET);

    byte[] bytes = new byte[8];
    setIntBE.invokeWithArguments(bytes, 0, 0x12345678);
    if (bytes[0] != 0x12 || bytes[1] != 0x34 || bytes[2] != 0x56 || bytes[3] != 0x78) {
      throw new AssertionError("int BE bytes mismatch");
    }
    if ((int) getIntBE.invokeWithArguments(bytes, 0) != 0x12345678) {
      throw new AssertionError("int BE get mismatch");
    }
    if ((int) getIntLE.invokeWithArguments(bytes, 0) != 0x78563412) {
      throw new AssertionError("int LE read of BE bytes mismatch");
    }

    setIntLE.invokeWithArguments(bytes, 4, 0x01020304);
    if (bytes[4] != 0x04 || bytes[5] != 0x03 || bytes[6] != 0x02 || bytes[7] != 0x01) {
      throw new AssertionError("int LE bytes mismatch");
    }
    if ((int) getIntLE.invokeWithArguments(bytes, 4) != 0x01020304) {
      throw new AssertionError("int LE get mismatch");
    }

    if (!vhIntBE.isAccessModeSupported(VarHandle.AccessMode.GET)) {
      throw new AssertionError("int BE GET should be supported");
    }
    if (vhIntBE.isAccessModeSupported(VarHandle.AccessMode.COMPARE_AND_SET)) {
      throw new AssertionError("int BE CAS should not be supported on byte array view");
    }

    MethodHandle casIntBE = vhIntBE.toMethodHandle(VarHandle.AccessMode.COMPARE_AND_SET);
    boolean caughtCasUoe = false;
    try {
      casIntBE.invokeWithArguments(bytes, 0, 0x12345678, 0x11223344);
    } catch (UnsupportedOperationException e) {
      caughtCasUoe = true;
    }
    if (!caughtCasUoe) {
      throw new AssertionError("expected UnsupportedOperationException for byte array CAS");
    }

    VarHandle vhLongBE = MethodHandles.byteArrayViewVarHandle(long[].class, ByteOrder.BIG_ENDIAN);
    MethodHandle getLongBE = vhLongBE.toMethodHandle(VarHandle.AccessMode.GET);
    MethodHandle setLongBE = vhLongBE.toMethodHandle(VarHandle.AccessMode.SET);
    setLongBE.invokeWithArguments(bytes, 0, 0x0102030405060708L);
    if ((long) getLongBE.invokeWithArguments(bytes, 0) != 0x0102030405060708L) {
      throw new AssertionError("long BE get mismatch");
    }

    VarHandle vhShortLE = MethodHandles.byteArrayViewVarHandle(short[].class, ByteOrder.LITTLE_ENDIAN);
    MethodHandle getShortLE = vhShortLE.toMethodHandle(VarHandle.AccessMode.GET);
    MethodHandle setShortLE = vhShortLE.toMethodHandle(VarHandle.AccessMode.SET);
    setShortLE.invokeWithArguments(bytes, 0, (short) 0x1234);
    if (bytes[0] != 0x34 || bytes[1] != 0x12) {
      throw new AssertionError("short LE bytes mismatch");
    }
    if ((short) getShortLE.invokeWithArguments(bytes, 0) != (short) 0x1234) {
      throw new AssertionError("short LE get mismatch");
    }

    boolean caughtOob = false;
    try {
      getIntBE.invokeWithArguments(bytes, 5);
    } catch (IndexOutOfBoundsException e) {
      caughtOob = true;
    }
    if (!caughtOob) {
      throw new AssertionError("expected IndexOutOfBoundsException for byte array view");
    }
  }

  private static void testByteBufferViewVarHandle() throws Throwable {
    ByteBuffer buf = ByteBuffer.allocate(16);
    VarHandle vhIntBE = MethodHandles.byteBufferViewVarHandle(int[].class, ByteOrder.BIG_ENDIAN);
    VarHandle vhIntLE = MethodHandles.byteBufferViewVarHandle(int[].class, ByteOrder.LITTLE_ENDIAN);

    MethodHandle getIntBE = vhIntBE.toMethodHandle(VarHandle.AccessMode.GET);
    MethodHandle setIntBE = vhIntBE.toMethodHandle(VarHandle.AccessMode.SET);
    MethodHandle getIntLE = vhIntLE.toMethodHandle(VarHandle.AccessMode.GET);
    MethodHandle setIntLE = vhIntLE.toMethodHandle(VarHandle.AccessMode.SET);

    setIntBE.invokeWithArguments(buf, 0, 0xAABBCCDD);
    if ((int) getIntBE.invokeWithArguments(buf, 0) != 0xAABBCCDD) {
      throw new AssertionError("buf int BE mismatch");
    }
    if ((int) getIntLE.invokeWithArguments(buf, 0) != 0xDDCCBBAA) {
      throw new AssertionError("buf int LE read of BE mismatch");
    }

    setIntLE.invokeWithArguments(buf, 4, 0x10203040);
    if ((int) getIntLE.invokeWithArguments(buf, 4) != 0x10203040) {
      throw new AssertionError("buf int LE get mismatch");
    }

    MethodHandle casIntBE = vhIntBE.toMethodHandle(VarHandle.AccessMode.COMPARE_AND_SET);
    boolean caughtHeapIse = false;
    try {
      casIntBE.invokeWithArguments(buf, 0, 0xAABBCCDD, 0x12345678);
    } catch (IllegalStateException e) {
      caughtHeapIse = true;
    }
    if (!caughtHeapIse) {
      throw new AssertionError("expected IllegalStateException for atomic CAS on heap buffer");
    }

    ByteBuffer direct = ByteBuffer.allocateDirect(16);
    VarHandle vhDirect = MethodHandles.byteBufferViewVarHandle(int[].class, ByteOrder.nativeOrder());
    MethodHandle getDirect = vhDirect.toMethodHandle(VarHandle.AccessMode.GET);
    MethodHandle setDirect = vhDirect.toMethodHandle(VarHandle.AccessMode.SET);
    MethodHandle casDirect = vhDirect.toMethodHandle(VarHandle.AccessMode.COMPARE_AND_SET);
    MethodHandle gasDirect = vhDirect.toMethodHandle(VarHandle.AccessMode.GET_AND_SET);

    setDirect.invokeWithArguments(direct, 0, 0x12345678);
    if ((int) getDirect.invokeWithArguments(direct, 0) != 0x12345678) {
      throw new AssertionError("direct get mismatch");
    }
    boolean casOk = (boolean) casDirect.invokeWithArguments(direct, 0, 0x12345678, 0x55667788);
    if (!casOk || (int) getDirect.invokeWithArguments(direct, 0) != 0x55667788) {
      throw new AssertionError("direct compareAndSet mismatch");
    }
    int oldVal = (int) gasDirect.invokeWithArguments(direct, 0, 0x77777777);
    if (oldVal != 0x55667788 || (int) getDirect.invokeWithArguments(direct, 0) != 0x77777777) {
      throw new AssertionError("direct getAndSet mismatch");
    }

    VarHandle vhLongBE = MethodHandles.byteBufferViewVarHandle(long[].class, ByteOrder.BIG_ENDIAN);
    MethodHandle getLongBE = vhLongBE.toMethodHandle(VarHandle.AccessMode.GET);
    MethodHandle setLongBE = vhLongBE.toMethodHandle(VarHandle.AccessMode.SET);
    setLongBE.invokeWithArguments(buf, 8, 0x1122334455667788L);
    if ((long) getLongBE.invokeWithArguments(buf, 8) != 0x1122334455667788L) {
      throw new AssertionError("buf long BE mismatch");
    }

    boolean caughtOob = false;
    try {
      getIntBE.invokeWithArguments(buf, 13);
    } catch (IndexOutOfBoundsException e) {
      caughtOob = true;
    }
    if (!caughtOob) {
      throw new AssertionError("expected IndexOutOfBoundsException for byte buffer view");
    }
  }
}
