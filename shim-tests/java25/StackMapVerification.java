/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.lang.reflect.Method;

public class StackMapVerification {
  private static int sequence;
  private static int version = 69;
  private static int copies = 1;
  private static int lengthDelta;
  private static byte[] constructorMaps;

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
    int mapCount = frames == null ? 0 : copies;
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
      out.writeInt(frames.length + lengthDelta);
      out.write(frames);
    }
  }

  private static Method define(String descriptor, boolean instance, int maxStack, int maxLocals,
      int[] code, byte[] frames, int[][] handlers) throws Exception {
    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    DataOutputStream out = new DataOutputStream(buffer);
    out.writeInt(0xcafebabe);
    out.writeShort(0);
    out.writeShort(version);
    out.writeShort(20);
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
    out.writeShort(0x21);
    out.writeShort(2);
    out.writeShort(4);
    out.writeShort(0);
    out.writeShort(0);
    out.writeShort(2);
    method(out, 1, 9, 10, 1, 1, new int[] {42, 183, 0, 12, 177}, constructorMaps, null);
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

  public static void main(String[] args) throws Exception {
    int[] simple = {3, 172};
    int[] branch = {3, 153, 0, 5, 3, 172, 3, 172};
    valid("empty-map", 0, "()I", false, 1, 0, simple, new byte[] {0, 0}, null);
    valid("same-frame", 0, "()I", false, 1, 0, branch, new byte[] {0, 1, 6}, null);
    valid("same-extended", 0, "()I", false, 1, 0, branch, new byte[] {0, 1, (byte) 251, 0, 6}, null);
    valid("one-stack", 0, "()I", false, 1, 0, simple, new byte[] {0, 1, 65, 1}, null);
    valid("one-stack-extended", 0, "()I", false, 1, 0, simple,
        new byte[] {0, 1, (byte) 247, 0, 1, 1}, null);
    int[] extended = new int[302];
    extended[300] = 3;
    extended[301] = 172;
    valid("large-offset", 0, "()I", false, 1, 0, extended, new byte[] {0, 1, (byte) 251, 1, 44}, null);
    valid("large-stack-offset", 0, "()I", false, 1, 0, extended,
        new byte[] {0, 1, (byte) 247, 1, 45, 1}, null);
    valid("append-chop-wide", 0, "()I", false, 2, 2,
        new int[] {9, 63, 167, 0, 3, 3, 87, 167, 0, 3, 3, 172},
        new byte[] {0, 2, (byte) 252, 0, 5, 4, (byte) 250, 0, 4}, null);
    valid("append-two-wide", 0, "()I", false, 2, 4,
        new int[] {9, 63, 14, 73, 167, 0, 3, 3, 172},
        new byte[] {0, 1, (byte) 253, 0, 7, 4, 3}, null);
    valid("append-three", 0, "()I", false, 1, 3,
        new int[] {3, 59, 3, 60, 3, 61, 167, 0, 3, 3, 172},
        new byte[] {0, 1, (byte) 254, 0, 9, 1, 1, 1}, null);
    valid("chop-two", 0, "(III)I", false, 1, 3,
        new int[] {167, 0, 3, 3, 172}, new byte[] {0, 1, (byte) 249, 0, 3}, null, 1, 2, 3);
    valid("chop-three", 0, "(III)I", false, 1, 3,
        new int[] {167, 0, 3, 3, 172}, new byte[] {0, 1, (byte) 248, 0, 3}, null, 1, 2, 3);
    valid("full-parameters", 7, "(IFJDLjava/lang/Object;)I", false, 1, 7, new int[] {26, 172},
        new byte[] {0, 1, (byte) 255, 0, 0, 0, 5, 1, 2, 4, 3, 7, 0, 4, 0, 0}, null, 7, 2f, 3L, 4d, new Object());
    valid("null-stack", 0, "()I", false, 1, 0, new int[] {1, 87, 3, 172}, new byte[] {0, 1, 65, 5}, null);
    valid("null-local", 0, "()I", false, 1, 1, new int[] {1, 75, 42, 87, 3, 172},
        new byte[] {0, 1, (byte) 255, 0, 2, 0, 1, 5, 0, 0}, null);
    valid("null-reference-join", 0, "(Ljava/lang/Object;)I", false, 1, 1,
        new int[] {3, 153, 0, 7, 1, 167, 0, 4, 42, 87, 3, 172},
        new byte[] {0, 2, 8, 64, 7, 0, 4}, null, new Object());
    int[] constructed = {187, 0, 4, 89, 183, 0, 12, 87, 3, 172};
    valid("object-stack", 0, "()I", false, 2, 0, constructed, new byte[] {0, 1, 71, 7, 0, 4}, null);
    valid("uninitialized-offset", 0, "()I", false, 2, 0, constructed, new byte[] {0, 1, 67, 8, 0, 0}, null);
    constructorMaps = new byte[] {0, 1, (byte) 255, 0, 0, 0, 1, 6, 0, 0};
    valid("constructor-frame", 0, "()I", true, 1, 1, simple, null, null);
    constructorMaps = null;
    checkDefinition("back-zero-map", 0, 0, new int[] {167, 0, 0}, new byte[] {0, 1, 0});
    valid("dead-map", 0, "()I", false, 1, 0, new int[] {3, 172, 3, 172}, new byte[] {0, 1, 2}, null);
    valid("dead-asserted-local", 0, "()I", false, 1, 1, new int[] {3, 172, 26, 172},
        new byte[] {0, 1, (byte) 255, 0, 2, 0, 1, 1, 0, 0}, null);
    valid("dead-asserted-stack", 0, "()I", false, 1, 0, new int[] {3, 172, 172}, new byte[] {0, 1, 66, 1}, null);
    for (int major : new int[] {51, 52, 61, 65, 69}) {
      version = major;
      valid("version-" + major, 0, "()I", false, 1, 0, branch, new byte[] {0, 1, 6}, null);
    }
    version = 50;
    checkDefinition("legacy-mapless", 0, 0, new int[] {167, 0, 0}, null);
    version = 69;
    bad("back-zero-missing", 0, 0, new int[] {167, 0, 0}, null);
    bad("branch-missing", 1, 0, branch, null);
    bad("dead-missing", 1, 0, new int[] {3, 172, 3, 172}, null);
    bad("dead-wrong-type", 1, 0, new int[] {3, 172, 1, 172}, new byte[] {0, 1, 2});
    bad("after-goto-missing", 1, 0, new int[] {167, 0, 4, 0, 3, 172}, new byte[] {0, 1, 4});
    bad("handler-missing", "()I", 1, 0, new int[] {1, 191, 87, 3, 172}, null, new int[][] {{0, 2, 2, 0}});
    bad("entry-erased", "(I)I", 1, 1, new int[] {26, 172}, new byte[] {0, 1, (byte) 250, 0, 0}, null);
    bad("wide-entry-erased", "(J)I", 2, 2, new int[] {30, 136, 172}, new byte[] {0, 1, (byte) 250, 0, 0}, null);
    bad("local-kind", "(I)I", 1, 1, new int[] {26, 172},
        new byte[] {0, 1, (byte) 255, 0, 0, 0, 1, 2, 0, 0}, null);
    bad("invented-local", 1, 1, simple, new byte[] {0, 1, (byte) 252, 0, 0, 1});
    bad("stack-erased", 1, 0, simple, new byte[] {0, 1, 65, 0});
    bad("stack-kind", 1, 0, simple, new byte[] {0, 1, 65, 2});
    bad("stack-height", 1, 0, simple, new byte[] {0, 1, 1});
    bad("null-not-object", 2, 0, constructed, new byte[] {0, 1, 71, 5});
    bad("null-not-argument", "(Ljava/lang/Object;)I", 1, 1, simple,
        new byte[] {0, 1, (byte) 255, 0, 0, 0, 1, 5, 0, 0}, null);
    valid("empty-body", 0, "()I", false, 1, 0, simple, new byte[] {}, null);
    bad("truncated-count", 1, 0, simple, new byte[] {0});
    bad("truncated-frame", 1, 0, simple, new byte[] {0, 1});
    bad("reserved-frame", 1, 0, simple, new byte[] {0, 1, (byte) 128});
    bad("reserved-frame-high", 1, 0, simple, new byte[] {0, 1, (byte) 246});
    bad("truncated-offset", 1, 0, simple, new byte[] {0, 1, (byte) 251, 0});
    bad("truncated-object", 1, 0, simple, new byte[] {0, 1, 65, 7, 0});
    valid("trailing-zero-table", 0, "()I", false, 1, 0, simple, new byte[] {0, 0, 0}, null);
    bad("trailing-nonempty-table", 1, 0, simple, new byte[] {0, 1, 0, 42});
    bad("empty-body-with-branch", 1, 0, branch, new byte[] {});
    bad("zero-table-with-branch", 1, 0, branch, new byte[] {0, 0, 42});
    bad("truncated-second-frame", 1, 0, simple, new byte[] {0, 2, 0});
    bad("unknown-type", 1, 0, simple, new byte[] {0, 1, 65, 9});
    bad("object-index-zero", 1, 0, simple, new byte[] {0, 1, 65, 7, 0, 0});
    bad("object-index-tag", 1, 0, simple, new byte[] {0, 1, 65, 7, 0, 1});
    bad("object-index-range", 1, 0, simple, new byte[] {0, 1, 65, 7, 0, 20});
    bad("object-type-mismatch", 1, 0, simple, new byte[] {0, 1, 65, 7, 0, 4});
    bad("offset-operand", 1, 0, new int[] {16, 0, 172}, new byte[] {0, 1, 1});
    bad("offset-end", 1, 0, simple, new byte[] {0, 1, 2});
    bad("offset-large", 1, 0, simple, new byte[] {0, 1, (byte) 251, (byte) 255, (byte) 255});
    bad("initial-stack", 1, 0, simple, new byte[] {0, 1, 64, 1});
    bad("stack-overflow", 1, 0, simple, new byte[] {0, 1, 65, 4});
    bad("locals-overflow", 1, 0, simple, new byte[] {0, 1, (byte) 252, 0, 0, 1});
    bad("chop-underflow", 1, 0, simple, new byte[] {0, 1, (byte) 250, 0, 0});
    bad("full-locals-overflow", 1, 0, simple, new byte[] {0, 1, (byte) 255, 0, 0, 0, 1, 1, 0, 0});
    bad("full-stack-overflow", 1, 0, simple, new byte[] {0, 1, (byte) 255, 0, 1, 0, 0, 0, 1, 4});
    bad("uninitialized-not-new", 1, 0, simple, new byte[] {0, 1, 65, 8, 0, 0});
    bad("uninitialized-operand", 2, 0, constructed, new byte[] {0, 1, 67, 8, 0, 1});
    bad("uninitialized-range", 2, 0, constructed, new byte[] {0, 1, 67, 8, (byte) 255, (byte) 255});
    bad("uninitialized-this-static", 1, 1, simple, new byte[] {0, 1, (byte) 252, 0, 0, 6});
    copies = 2;
    bad("duplicate-map", 1, 0, simple, new byte[] {0, 0});
    copies = 1;
    lengthDelta = 1;
    bad("attribute-length-long", 1, 0, simple, new byte[] {0, 0});
    lengthDelta = -1;
    bad("attribute-length-short", 1, 0, simple, new byte[] {0, 0});
    lengthDelta = 0;
  }
}
