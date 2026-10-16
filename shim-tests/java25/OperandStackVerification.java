/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.lang.reflect.Method;

public class OperandStackVerification {
  private static int sequence;

  private static class Loader extends ClassLoader {
    Class<?> define(byte[] bytes) {
      return defineClass(null, bytes, 0, bytes.length);
    }
  }

  private static void utf(DataOutputStream out, String value) throws Exception {
    out.writeByte(1);
    out.writeUTF(value);
  }

  private static void entry(DataOutputStream out, int tag, int first, int second) throws Exception {
    out.writeByte(tag);
    out.writeShort(first);
    if (second != 0) out.writeShort(second);
  }

  private static byte[] classBytes(int maxStack, int maxLocals, int[] code, byte[] frames) throws Exception {
    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    DataOutputStream out = new DataOutputStream(buffer);
    out.writeInt(0xcafebabe);
    out.writeShort(0);
    out.writeShort(52);
    out.writeShort(38);
    utf(out, "StackCase" + sequence++);
    out.writeByte(7);
    out.writeShort(1);
    utf(out, "java/lang/Object");
    out.writeByte(7);
    out.writeShort(3);
    utf(out, "value");
    utf(out, "()I");
    utf(out, "Code");
    utf(out, "StackMapTable");
    utf(out, "java/lang/Math");
    entry(out, 7, 9, 0);
    utf(out, "abs");
    utf(out, "(I)I");
    entry(out, 12, 11, 12);
    entry(out, 10, 10, 13);
    utf(out, "java/lang/System");
    entry(out, 7, 15, 0);
    utf(out, "out");
    utf(out, "Ljava/io/PrintStream;");
    entry(out, 12, 17, 18);
    entry(out, 9, 16, 19);
    utf(out, "java/lang/String");
    entry(out, 7, 21, 0);
    utf(out, "length");
    utf(out, "()I");
    entry(out, 12, 23, 24);
    entry(out, 10, 22, 25);
    utf(out, "(D)D");
    entry(out, 12, 11, 27);
    entry(out, 10, 10, 28);
    utf(out, "java/lang/Long");
    entry(out, 7, 30, 0);
    utf(out, "MAX_VALUE");
    utf(out, "J");
    entry(out, 12, 32, 33);
    entry(out, 9, 31, 34);
    utf(out, "abc");
    entry(out, 8, 36, 0);
    out.writeShort(0x21);
    out.writeShort(2);
    out.writeShort(4);
    out.writeShort(0);
    out.writeShort(0);
    out.writeShort(1);
    out.writeShort(9);
    out.writeShort(5);
    out.writeShort(6);
    out.writeShort(1);
    out.writeShort(7);
    out.writeInt(12 + code.length + (frames == null ? 0 : 6 + frames.length));
    out.writeShort(maxStack);
    out.writeShort(maxLocals);
    out.writeInt(code.length);
    for (int opcode : code) out.writeByte(opcode);
    out.writeShort(0);
    out.writeShort(frames == null ? 0 : 1);
    if (frames != null) {
      out.writeShort(8);
      out.writeInt(frames.length);
      out.write(frames);
    }
    out.writeShort(0);
    return buffer.toByteArray();
  }

  private static Method method(int maxStack, int maxLocals, int[] code, byte[] frames) throws Exception {
    return new Loader().define(classBytes(maxStack, maxLocals, code, frames)).getDeclaredMethod("value");
  }

  private static void valid(String name, int expected, int maxStack, int maxLocals,
                            int[] code, byte[] frames) throws Exception {
    if (!Integer.valueOf(expected).equals(method(maxStack, maxLocals, code, frames).invoke(null))) {
      throw new AssertionError(name);
    }
    System.out.println(name + " accepted");
  }

  private static void invalid(String name, int maxStack, int[] code, byte[] frames) throws Exception {
    try {
      method(maxStack, 0, code, frames);
      throw new AssertionError(name + " accepted");
    } catch (VerifyError expected) {
      System.out.println(name + " rejected");
    }
  }

  public static void main(String[] args) throws Exception {
    byte[] frames = {0, 2, 8, 64, 1};
    valid("constant", 0, 1, 0, new int[] {3, 172}, null);
    valid("category-two", 2, 2, 0, new int[] {9, 88, 5, 172}, null);
    valid("dup2-long", 0, 4, 0, new int[] {9, 92, 97, 136, 172}, null);
    valid("wide-local", 1, 1, 257, new int[] {4, 196, 54, 1, 0, 196, 21, 1, 0, 172}, null);
    valid("branch-merge", 2, 1, 0, new int[] {3, 153, 0, 7, 4, 167, 0, 4, 5, 172}, frames);
    valid("dup-float", 0, 2, 0, new int[] {11, 89, 88, 3, 172}, null);
    valid("dup-x1", 0, 3, 0, new int[] {3, 3, 90, 88, 87, 3, 172}, null);
    valid("dup-x2-single", 0, 4, 0, new int[] {3, 3, 3, 91, 88, 88, 3, 172}, null);
    valid("dup-x2-wide", 0, 4, 0, new int[] {9, 3, 91, 87, 88, 87, 3, 172}, null);
    valid("dup2-single", 0, 4, 0, new int[] {3, 3, 92, 88, 88, 3, 172}, null);
    valid("dup2-x1-single", 0, 5, 0, new int[] {3, 3, 3, 93, 88, 88, 87, 3, 172}, null);
    valid("dup2-x1-wide", 0, 5, 0, new int[] {3, 9, 93, 88, 87, 88, 3, 172}, null);
    valid("dup2-x2-single", 0, 6, 0, new int[] {3, 3, 3, 3, 94, 88, 88, 88, 3, 172}, null);
    valid("dup2-x2-lower-wide", 0, 6, 0, new int[] {9, 3, 3, 94, 88, 88, 88, 3, 172}, null);
    valid("dup2-x2-upper-wide", 0, 6, 0, new int[] {3, 3, 9, 94, 88, 88, 88, 3, 172}, null);
    valid("dup2-x2-both-wide", 0, 6, 0, new int[] {9, 9, 94, 88, 88, 88, 3, 172}, null);
    valid("swap-single", 0, 2, 0, new int[] {1, 3, 95, 87, 87, 3, 172}, null);
    valid("float-to-int", 0, 1, 0, new int[] {11, 139, 172}, null);
    valid("static-int-call", 3, 1, 0, new int[] {16, 253, 184, 0, 14, 172}, null);
    valid("static-double-call", 0, 2, 0, new int[] {14, 184, 0, 29, 142, 172}, null);
    valid("virtual-reference-call", 3, 1, 0, new int[] {18, 37, 182, 0, 26, 172}, null);
    valid("reference-merge", 0, 1, 0, new int[] {3, 153, 0, 7, 1, 167, 0, 5, 18, 37, 87, 3, 172},
        new byte[] {0, 2, 8, 65, 7, 0, 22});
    valid("reference-field", 0, 1, 0, new int[] {178, 0, 20, 87, 3, 172}, null);
    valid("wide-field", -1, 2, 0, new int[] {178, 0, 35, 136, 172}, null);
    invalid("float-int-argument", 1, new int[] {11, 184, 0, 14, 172}, null);
    invalid("reference-int-argument", 1, new int[] {1, 184, 0, 14, 172}, null);
    invalid("long-double-argument", 2, new int[] {9, 184, 0, 29, 142, 172}, null);
    invalid("int-virtual-receiver", 1, new int[] {3, 182, 0, 26, 172}, null);
    invalid("int-reference-field", 1, new int[] {3, 179, 0, 20, 3, 172}, null);
    valid("unused-top-merge", 0, 2, 0, new int[] {3, 153, 0, 7, 3, 167, 0, 4, 11, 3, 172},
        new byte[] {0, 2, 8, 64, 0});
    valid("unused-wide-top-merge", 0, 3, 0, new int[] {3, 153, 0, 7, 9, 167, 0, 4, 14, 3, 172},
        new byte[] {0, 2, 8, (byte) 255, 0, 0, 0, 0, 0, 2, 0, 0});
    invalid("reference-int-return", 1, new int[] {1, 172}, null);
    invalid("float-int-return", 1, new int[] {11, 172}, null);
    invalid("float-int-add", 2, new int[] {11, 3, 96, 172}, null);
    invalid("pop-half-long", 2, new int[] {9, 87, 87, 3, 172}, null);
    invalid("pop2-half-long", 3, new int[] {9, 11, 88, 87, 3, 172}, null);
    invalid("dup-long", 3, new int[] {9, 89, 88, 87, 3, 172}, null);
    invalid("swap-long", 3, new int[] {9, 3, 95, 88, 87, 3, 172}, null);
    invalid("int-array-receiver", 2, new int[] {3, 3, 46, 172}, null);
    invalid("float-array-index", 3, new int[] {1, 11, 3, 79, 3, 172}, null);
    invalid("int-throw", 1, new int[] {3, 191}, null);
    invalid("int-monitor", 1, new int[] {3, 194, 3, 172}, null);
    invalid("int-null-branch", 1, new int[] {3, 198, 0, 5, 3, 172, 3, 172}, new byte[] {0, 1, 6});
    invalid("reference-int-branch", 1, new int[] {1, 153, 0, 5, 3, 172, 3, 172}, new byte[] {0, 1, 6});
    invalid("primitive-merge", 1, new int[] {3, 153, 0, 7, 3, 167, 0, 4, 11, 172}, frames);
    invalid("pop-underflow", 1, new int[] {87, 3, 172}, null);
    invalid("return-underflow", 1, new int[] {172}, null);
    invalid("stack-overflow", 1, new int[] {3, 4, 96, 172}, null);
    invalid("category-two-overflow", 1, new int[] {9, 88, 3, 172}, null);
    invalid("dup-underflow", 2, new int[] {89, 172}, null);
    invalid("merge-height", 1, new int[] {3, 153, 0, 7, 4, 167, 0, 4, 0, 172}, frames);
  }
}
