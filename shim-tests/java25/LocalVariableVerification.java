/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.lang.reflect.Method;

public class LocalVariableVerification {
  private static int sequence;

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
    out.writeShort(access);
    out.writeShort(name);
    out.writeShort(descriptor);
    out.writeShort(1);
    out.writeShort(7);
    out.writeInt(12 + code.length + 8 * handlerCount + (frames == null ? 0 : 6 + frames.length));
    out.writeShort(maxStack);
    out.writeShort(maxLocals);
    out.writeInt(code.length);
    for (int value : code) out.writeByte(value);
    out.writeShort(handlerCount);
    if (handlers != null) for (int[] handler : handlers) for (int value : handler) out.writeShort(value);
    out.writeShort(frames == null ? 0 : 1);
    if (frames != null) {
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
    out.writeShort(52);
    out.writeShort(20);
    utf(out, "LocalCase" + sequence++);
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
    out.writeShort(0x21);
    out.writeShort(2);
    out.writeShort(4);
    out.writeShort(0);
    out.writeShort(0);
    out.writeShort(2);
    method(out, 1, 9, 10, 1, 1, new int[] {42, 183, 0, 12, 177}, null, null);
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

  private static void invalid(String name, String descriptor, int maxStack, int maxLocals,
      int[] code, byte[] frames, int[][] handlers) throws Exception {
    try {
      define(descriptor, false, maxStack, maxLocals, code, frames, handlers);
      throw new AssertionError(name + " accepted");
    } catch (VerifyError expected) {
      System.out.println(name + " rejected");
    }
  }

  private static void invalid(String name, int maxStack, int maxLocals, int[] code) throws Exception {
    invalid(name, "()I", maxStack, maxLocals, code, null, null);
  }

  public static void main(String[] args) throws Exception {
    valid("int-argument", 7, "(I)I", false, 1, 1, new int[] {26, 172}, null, null, 7);
    valid("long-argument", 7, "(J)I", false, 2, 2, new int[] {30, 136, 172}, null, null, 7L);
    valid("float-argument", 3, "(F)I", false, 1, 1, new int[] {34, 139, 172}, null, null, 3.5f);
    valid("double-argument", 4, "(D)I", false, 2, 2, new int[] {38, 142, 172}, null, null, 4.25d);
    valid("reference-argument", 0, "(Ljava/lang/Object;)I", false, 1, 1,
        new int[] {42, 87, 3, 172}, null, null, new Object());
    valid("array-argument", 3, "([I)I", false, 1, 1, new int[] {42, 190, 172}, null, null, new int[3]);
    valid("mixed-arguments", 5, "(JDI[Ljava/lang/Object;)I", false, 2, 6,
        new int[] {30, 88, 40, 88, 25, 5, 87, 21, 4, 172}, null, null, 1L, 2d, 5, new Object[0]);
    valid("instance-receiver", 0, "()I", true, 1, 1, new int[] {42, 87, 3, 172}, null, null);
    valid("increment", 7, 1, 1, new int[] {3, 59, 132, 0, 7, 26, 172});
    valid("wide-local", 7, 1, 257, new int[] {16, 7, 196, 54, 1, 0, 196, 21, 1, 0, 172});
    valid("wide-increment", 258, 1, 257, new int[] {3, 196, 54, 1, 0, 196, 132, 1, 0, 1, 2, 196, 21, 1, 0, 172});
    valid("tail-reuse", 1, 2, 2, new int[] {9, 63, 4, 60, 27, 172});
    valid("wide-restored", 1, 2, 2, new int[] {9, 63, 4, 60, 10, 63, 30, 136, 172});
    valid("narrow-reuse", 3, 2, 2, new int[] {9, 63, 4, 59, 5, 60, 26, 27, 96, 172});
    valid("overlap-reuse", 0, 2, 3, new int[] {9, 64, 14, 71, 38, 142, 172});
    byte[] merged = {0, 2, 9, 2};
    int[] branches = {3, 153, 0, 8, 4, 59, 167, 0, 6, 11, 67, 0, 3, 172};
    valid("unused-local-merge", 0, "()I", false, 1, 1, branches, merged, null);
    valid("argument-merge", 7, "(I)I", false, 1, 1,
        new int[] {3, 153, 0, 5, 4, 59, 26, 172}, new byte[] {0, 1, 6}, null, 7);
    valid("loop", 0, "(I)I", false, 1, 1,
        new int[] {26, 158, 0, 9, 132, 0, 255, 167, 255, 249, 26, 172}, new byte[] {0, 2, 0, 9}, null, 3);
    byte[] handlerInt = {0, 1, (byte) 255, 0, 4, 0, 1, 1, 0, 1, 7, 0, 14};
    valid("exception-local", 5, "()I", false, 1, 2,
        new int[] {8, 59, 1, 191, 76, 26, 172}, handlerInt, new int[][] {{2, 4, 4, 0}});
    byte[] handlerBefore = {0, 1, (byte) 255, 0, 6, 0, 1, 1, 0, 1, 7, 0, 14};
    valid("exception-before-store", 0, "()I", false, 1, 2,
        new int[] {8, 59, 1, 75, 3, 172, 76, 26, 172}, handlerBefore, new int[][] {{3, 4, 6, 0}});
    byte[] nestedHandlers = {0, 2, (byte) 255, 0, 4, 0, 1, 1, 0, 1, 7, 0, 14,
        (byte) 255, 0, 5, 0, 1, 1, 0, 1, 7, 0, 14};
    valid("nested-handler-snapshot", 5, "(Ljava/lang/Object;)I", false, 1, 2,
        new int[] {8, 59, 1, 191, 76, 184, 0, 19, 26, 172, 76, 184, 0, 19, 26, 172}, nestedHandlers,
        new int[][] {{2, 4, 4, 0}, {4, 5, 10, 0}}, new Object());
    byte[] handlerTop = {0, 1, (byte) 255, 0, 4, 0, 0, 0, 1, 7, 0, 14};
    valid("exception-reassigned", 1, "()I", false, 1, 2,
        new int[] {3, 59, 1, 191, 76, 4, 59, 26, 172}, handlerTop, new int[][] {{0, 4, 4, 0}});
    int[] sparse = new int[20002];
    sparse[20000] = 3;
    sparse[20001] = 172;
    define("()I", false, 1, 65535, sparse, null, null);
    System.out.println("unused-local-capacity accepted");
    define("()I", false, 1, 65535, new int[] {4, 196, 54, 255, 254, 196, 21, 255, 254, 172}, null, null);
    System.out.println("last-local accepted");
    define("()I", false, 2, 65535, new int[] {9, 196, 55, 255, 253, 196, 22, 255, 253, 136, 172}, null, null);
    System.out.println("last-wide-local accepted");
    invalid("uninitialized-int", 1, 1, new int[] {26, 172});
    invalid("uninitialized-reference", 1, 1, new int[] {42, 87, 3, 172});
    invalid("uninitialized-long", 2, 2, new int[] {30, 136, 172});
    invalid("float-as-int", 1, 1, new int[] {11, 67, 26, 172});
    invalid("reference-as-int", 1, 1, new int[] {1, 75, 26, 172});
    invalid("int-as-reference", 1, 1, new int[] {3, 59, 42, 87, 3, 172});
    invalid("long-as-double", 2, 2, new int[] {9, 63, 38, 142, 172});
    invalid("double-as-long", 2, 2, new int[] {14, 71, 30, 136, 172});
    invalid("second-word-as-int", 2, 2, new int[] {9, 63, 27, 172});
    invalid("overwritten-tail", 2, 2, new int[] {9, 63, 3, 60, 30, 136, 172});
    invalid("overwritten-head", 2, 2, new int[] {9, 63, 3, 59, 30, 136, 172});
    invalid("overlapping-wide", 2, 3, new int[] {9, 63, 14, 72, 30, 136, 172});
    invalid("overwritten-wide-owner", 2, 3, new int[] {9, 64, 14, 71, 31, 136, 172});
    invalid("uninitialized-increment", 1, 1, new int[] {132, 0, 1, 26, 172});
    invalid("reference-increment", 1, 1, new int[] {1, 75, 132, 0, 1, 3, 172});
    invalid("wide-uninitialized", 1, 257, new int[] {196, 21, 1, 0, 172});
    invalid("wide-uninitialized-increment", 1, 257, new int[] {196, 132, 1, 0, 0, 1, 3, 172});
    invalid("wide-float-as-int", 1, 257, new int[] {11, 196, 56, 1, 0, 196, 21, 1, 0, 172});
    invalid("bad-argument-kind", "(I)I", 1, 1, new int[] {34, 139, 172}, null, null);
    invalid("argument-second-word", "(J)I", 1, 2, new int[] {27, 172}, null, null);
    invalid("partly-initialized-merge", "()I", 1, 1,
        new int[] {3, 153, 0, 5, 4, 59, 26, 172}, new byte[] {0, 1, 6}, null);
    int[] badBranches = branches.clone();
    badBranches[12] = 26;
    invalid("incompatible-local-merge", "()I", 1, 1, badBranches, merged, null);
    invalid("loop-type-loss", "(I)I", 1, 1,
        new int[] {26, 87, 11, 67, 167, 255, 252}, new byte[] {0, 1, 0}, null);
    invalid("exception-before-assignment", "()I", 1, 2,
        new int[] {3, 59, 1, 191, 76, 26, 172}, handlerTop, new int[][] {{0, 4, 4, 0}});
    try {
      define("(I)I", false, 1, 0, new int[] {3, 172}, null, null);
      throw new AssertionError("short parameter locals accepted");
    } catch (ClassFormatError expected) {
      System.out.println("short parameter locals rejected");
    }
  }
}
