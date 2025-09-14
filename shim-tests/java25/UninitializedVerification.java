/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.lang.reflect.Method;

public class UninitializedVerification {
  private static int sequence;
  private static final int VERSION = 69;
  private static byte[] constructorMaps;
  private static int[] constructorCode = {42, 183, 0, 12, 177};
  private static int[][] constructorHandlers;
  private static final int CONSTRUCTOR_LOCALS = 2;
  private static final int CONSTRUCTOR_STACK = 3;

  private static class Loader extends ClassLoader {
    Class<?> define(byte[] bytes) { return defineClass(null, bytes, 0, bytes.length); }
  }

  private static void utf(DataOutputStream out, String text) throws Exception {
    out.writeByte(1);
    out.writeUTF(text);
  }

  private static void entry(DataOutputStream out, int tag, int first, int second) throws Exception {
    out.writeByte(tag);
    out.writeShort(first);
    if (second != 0) out.writeShort(second);
  }

  private static void method(DataOutputStream out, int access, int name, int descriptor,
      int maxStack, int maxLocals, int[] code, byte[] frames, int[][] handlers) throws Exception {
    int handlerCount = handlers == null ? 0 : handlers.length;
    int mapCount = frames == null ? 0 : 1;
    out.writeShort(access);
    out.writeShort(name);
    out.writeShort(descriptor);
    out.writeShort(1);
    out.writeShort(7);
    out.writeInt(12 + code.length + 8 * handlerCount + (mapCount == 0 ? 0 : mapCount * (6 + frames.length)));
    out.writeShort(maxStack);
    out.writeShort(maxLocals);
    out.writeInt(code.length);
    for (int value : code) out.writeByte(value);
    out.writeShort(handlerCount);
    if (handlers != null) for (int[] handler : handlers) for (int value : handler) out.writeShort(value);
    out.writeShort(mapCount);
    for (int i = 0; i < mapCount; ++i) {
      out.writeShort(8);
      out.writeInt(frames.length);
      out.write(frames);
    }
  }

  private static Method define(String descriptor, boolean instance, int maxStack, int maxLocals,
      int[] code, byte[] frames, int[][] handlers) throws Exception {
    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    DataOutputStream out = new DataOutputStream(buffer);
    out.writeInt(0xcafebabe);
    out.writeShort(0);
    out.writeShort(VERSION);
    out.writeShort(40);
    utf(out, "MapCase" + sequence++);
    entry(out, 7, 1, 0);
    utf(out, "java/lang/Object");
    entry(out, 7, 3, 0);
    utf(out, "value");
    utf(out, descriptor);
    utf(out, "Code");
    utf(out, "StackMapTable");
    utf(out, "<init>");
    utf(out, "()V");
    entry(out, 12, 9, 10);
    entry(out, 10, 4, 11);
    utf(out, "java/lang/Throwable");
    entry(out, 7, 13, 0);
    utf(out, "java/lang/System");
    entry(out, 7, 15, 0);
    utf(out, "gc");
    entry(out, 12, 17, 10);
    entry(out, 10, 16, 18);
    utf(out, "toString");
    utf(out, "()Ljava/lang/String;");
    entry(out, 12, 20, 21);
    entry(out, 10, 4, 22);
    utf(out, "java/lang/String");
    entry(out, 7, 24, 0);
    entry(out, 10, 25, 11);
    utf(out, "consume");
    utf(out, "(Ljava/lang/Object;)V");
    entry(out, 12, 27, 28);
    entry(out, 10, 16, 29);
    utf(out, "field");
    utf(out, "I");
    entry(out, 12, 31, 32);
    entry(out, 9, 2, 33);
    entry(out, 10, 2, 11);
    utf(out, "Ljava/lang/Object;");
    entry(out, 12, 31, 36);
    entry(out, 9, 2, 37);
    entry(out, 9, 4, 33);
    out.writeShort(0x21);
    out.writeShort(2);
    out.writeShort(4);
    out.writeShort(0);
    out.writeShort(1);
    out.writeShort(1); out.writeShort(31); out.writeShort(32); out.writeShort(0);
    out.writeShort(2);
    method(out, 1, 9, 10, CONSTRUCTOR_STACK, CONSTRUCTOR_LOCALS, constructorCode, constructorMaps, constructorHandlers);
    method(out, instance ? 1 : 9, 5, 6, maxStack, maxLocals, code, frames, handlers);
    out.writeShort(0);
    return new Loader().define(buffer.toByteArray()).getDeclaredMethods()[0];
  }

  private static void valid(String name, int expected, String descriptor, boolean instance,
      int maxStack, int maxLocals, int[] code, byte[] frames, int[][] handlers, Object... arguments) throws Exception {
    Method method = define(descriptor, instance, maxStack, maxLocals, code, frames, handlers);
    Object receiver = instance ? method.getDeclaringClass().getDeclaredConstructor().newInstance() : null;
    if (!Integer.valueOf(expected).equals(method.invoke(receiver, arguments))) throw new AssertionError(name);
    System.out.println(name + " accepted");
  }

  private static void valid(String name, int expected, int maxStack, int maxLocals, int[] code) throws Exception {
    valid(name, expected, "()I", false, maxStack, maxLocals, code, null, null);
  }

  private static void bad(String name, String descriptor, int maxStack, int maxLocals,
      int[] code, byte[] frames, int[][] handlers) throws Exception {
    try {
      define(descriptor, false, maxStack, maxLocals, code, frames, handlers);
      throw new AssertionError(name + " accepted");
    } catch (ClassFormatError expected) {
      System.out.println(name + ":ClassFormatError");
    } catch (VerifyError expected) {
      System.out.println(name + ":VerifyError");
    }
  }

  private static void bad(String name, int maxStack, int maxLocals, int[] code, byte[] frames) throws Exception {
    bad(name, "()I", maxStack, maxLocals, code, frames, null);
  }

  private static void checkDefinition(String name, int maxStack, int maxLocals, int[] code, byte[] frames)
      throws Exception {
    define("()I", false, maxStack, maxLocals, code, frames, null);
    System.out.println(name + " accepted");
  }

  private static byte[] map(int... values) {
    byte[] result = new byte[values.length];
    for (int i = 0; i < values.length; ++i) result[i] = (byte) values[i];
    return result;
  }

  private static void badConstructor(String name, int... code) throws Exception {
    constructorCode = code;
    bad(name, 1, 0, new int[] {3, 172}, null);
    constructorCode = new int[] {42, 183, 0, 12, 177};
  }

  public static void main(String[] args) throws Exception {
    valid("discard-new", 0, "()I", false, 1, 0, new int[] {187, 0, 4, 87, 3, 172}, null, null);
    valid("initialize-alias", 0, "()I", false, 2, 1,
        new int[] {187, 0, 4, 89, 75, 183, 0, 12, 42, 182, 0, 23, 87, 3, 172}, null, null);
    valid("two-allocations", 0, "()I", false, 3, 1,
        new int[] {187, 0, 4, 75, 187, 0, 4, 89, 183, 0, 12, 87, 42, 183, 0, 12, 3, 172}, null, null);
    valid("allocation-frame", 0, "()I", false, 2, 0,
        new int[] {187, 0, 4, 89, 183, 0, 12, 87, 3, 172}, map(0,1,67,8,0,0), null);
    valid("uninitialized-null-test", 0, "()I", false, 1, 0,
        new int[] {187,0,4,198,0,5,3,172,3,172}, map(0,1,8), null);
    valid("uninitialized-compare", 0, "()I", false, 2, 0,
        new int[] {187,0,4,89,165,0,5,3,172,3,172}, map(0,1,9), null);
    valid("uninitialized-monitor", 0, "()I", false, 2, 0,
        new int[] {187,0,4,89,194,195,3,172}, null, null);
    bad("return-new", "()Ljava/lang/Object;", 1, 0, new int[] {187,0,4,176}, null, null);
    bad("invoke-new", 1, 0, new int[] {187,0,4,182,0,23,87,3,172}, null);
    bad("argument-new", 1, 0, new int[] {187,0,4,184,0,30,3,172}, null);
    bad("double-initialization", 2, 0, new int[] {187,0,4,89,183,0,12,183,0,12,3,172}, null);
    bad("null-initialization", 1, 0, new int[] {1,183,0,12,3,172}, null);
    bad("initialized-initialization", "(Ljava/lang/Object;)I", 1, 1, new int[] {42,183,0,12,3,172}, null, null);
    bad("wrong-constructor-owner", 1, 0, new int[] {187,0,4,183,0,26,3,172}, null);
    bad("uninitialized-cast", 1, 0, new int[] {187,0,4,192,0,4,87,3,172}, null);
    bad("uninitialized-instanceof", 1, 0, new int[] {187,0,4,193,0,4,172}, null);
    bad("uninitialized-throw", 1, 0, new int[] {187,0,14,191}, null);
    bad("uninitialized-arraylength", 1, 0, new int[] {187,0,4,190,172}, null);
    bad("map-prematurely-initialized", 2, 0, new int[] {187,0,4,89,183,0,12,87,3,172}, map(0,1,67,7,0,4));
    bad("map-after-initialization", 2, 0, new int[] {187,0,4,89,183,0,12,87,3,172}, map(0,1,71,8,0,0));
    bad("different-allocation-site", 2, 0, new int[] {187,0,4,87,187,0,4,89,183,0,12,87,3,172}, map(0,1,71,8,0,0));
    badConstructor("constructor-return", 177);
    badConstructor("constructor-erased-this", 1,75,177);
    badConstructor("constructor-read-field", 42,180,0,34,87,42,183,0,12,177);
    badConstructor("constructor-invoke", 42,182,0,23,87,42,183,0,12,177);
    badConstructor("constructor-wrong-owner", 42,183,0,26,177);
    badConstructor("constructor-put-parent-field", 42,3,181,0,39,42,183,0,12,177);
    constructorCode = new int[] {42,3,181,0,34,42,183,0,12,177};
    valid("constructor-put-own-field", 0, "()I", true, 1, 1, new int[] {3,172}, null, null);
    constructorCode = new int[] {42,76,1,75,43,183,0,12,177};
    valid("constructor-aliased-this", 0, "()I", true, 1, 1, new int[] {3,172}, null, null);
    constructorCode = new int[] {1,191};
    checkDefinition("constructor-throws", 1, 0, new int[] {3,172}, null);
    constructorCode = new int[] {42,183,0,12,177};
    constructorMaps = map(0,1,255,0,0,0,1,6,0,0);
    valid("constructor-this-frame", 0, "()I", true, 1, 1, new int[] {3,172}, null, null);
    constructorMaps = map(0,1,255,0,0,0,1,7,0,2,0,0);
    bad("constructor-frame-claims-initialized", 1, 0, new int[] {3,172}, null);
    constructorMaps = null;
    valid("constructor-handler-drops-receiver", 0, "()I", false, 2, 1,
        new int[] {187,0,4,89,75,183,0,12,3,172,87,3,172},
        map(0,1,255,0,10,0,0,0,1,7,0,14), new int[][] {{5,8,10,0}});
    bad("constructor-handler-retry", "()I", 2, 1,
        new int[] {187,0,4,89,75,183,0,12,3,172,87,42,183,0,12,3,172},
        map(0,1,255,0,10,0,1,8,0,0,0,1,7,0,14), new int[][] {{5,8,10,0}});
    valid("handler-preserves-other-uninitialized", 0, "()I", false, 1, 1,
        new int[] {187,0,4,75,3,87,42,183,0,12,3,172,87,42,183,0,12,3,172},
        map(0,1,255,0,12,0,1,8,0,0,0,1,7,0,14), new int[][] {{4,6,12,0}});
    constructorCode = new int[] {42,183,0,12,177,191};
    constructorMaps = map(0,1,255,0,5,0,0,0,1,7,0,14);
    // The constructor's handler is supplied separately from the value method.
    constructorHandlers = new int[][] {{1,4,5,0}};
    bad("super-handler-cannot-erase-flag", 1, 0, new int[] {3,172}, null);
    constructorMaps = map(0,1,255,0,5,0,1,6,0,1,7,0,14);
    bad("super-handler-cannot-retry-this", 1, 0, new int[] {3,172}, null);
    constructorHandlers = new int[][] {{0,1,5,0}};
    valid("constructor-handler-before-super", 0, "()I", true, 1, 1, new int[] {3,172}, null, null);
    constructorHandlers = new int[][] {{4,5,5,0}};
    constructorMaps = map(0,1,255,0,5,0,1,7,0,2,0,1,7,0,14);
    valid("constructor-handler-after-super", 0, "()I", true, 1, 1, new int[] {3,172}, null, null);
    constructorHandlers = null;
    constructorCode = new int[] {3,153,0,7,42,183,0,12,177};
    constructorMaps = map(0,1,8);
    bad("constructor-path-skips-super", 1, 0, new int[] {3,172}, null);
    constructorCode = new int[] {42,183,0,12,177};
    constructorMaps = null;
    bad("initialization-preserves-other-site", 1, 2,
        new int[] {187,0,4,75,187,0,4,76,42,183,0,12,43,182,0,23,87,3,172}, null);
    checkDefinition("dead-new-local-token", 1, 1, new int[] {3,172,187,0,4,87,42,87,3,172},
        map(0,1,255,0,2,0,1,8,0,2,0,0));
    checkDefinition("dead-new-stack-token", 2, 0, new int[] {3,172,187,0,4,87,87,3,172}, map(0,1,66,8,0,2));
    badConstructor("constructor-other-init-not-this", 187,0,4,89,183,0,12,87,177);
    badConstructor("constructor-erases-all-aliases", 42,76,1,75,1,76,177);
    constructorCode = new int[] {42,183,0,35,177};
    checkDefinition("constructor-this-delegation", 1, 0, new int[] {3,172}, null);
    constructorCode = new int[] {42,183,0,12,177};
    constructorCode = new int[] {1,191};
    valid("failed-constructor-is-caught", 1, "()I", false, 2, 1,
        new int[] {187,0,2,89,75,183,0,35,3,172,87,184,0,19,4,172},
        map(0,1,255,0,10,0,0,0,1,7,0,14), new int[][] {{5,8,10,0}});
    constructorCode = new int[] {42,183,0,12,177};
    int[] high = new int[65535];
    high[65530] = 187; high[65532] = 4; high[65533] = 87; high[65534] = 177;
    define("()V", false, 1, 0, high, map(0,1,247,255,253,8,255,250), null);
    System.out.println("high-allocation-offset accepted");
  }
}
