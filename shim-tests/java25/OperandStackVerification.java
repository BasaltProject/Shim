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

  private static byte[] classBytes(int maxStack, int maxLocals, int[] code, byte[] frames) throws Exception {
    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    DataOutputStream out = new DataOutputStream(buffer);
    out.writeInt(0xcafebabe);
    out.writeShort(0);
    out.writeShort(52);
    out.writeShort(9);
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
    invalid("pop-underflow", 1, new int[] {87, 3, 172}, null);
    invalid("return-underflow", 1, new int[] {172}, null);
    invalid("stack-overflow", 1, new int[] {3, 4, 96, 172}, null);
    invalid("category-two-overflow", 1, new int[] {9, 88, 3, 172}, null);
    invalid("dup-underflow", 2, new int[] {89, 172}, null);
    invalid("merge-height", 1, new int[] {3, 153, 0, 7, 4, 167, 0, 4, 0, 172}, frames);
  }
}
