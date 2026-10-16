/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;

public class VerificationOrder {
  private static int sequence;

  private static class Loader extends ClassLoader {
    Class<?> define(byte[] bytes) { return defineClass(null, bytes, 0, bytes.length); }
  }

  private static void utf(DataOutputStream out, String text) throws Exception {
    out.writeByte(1); out.writeUTF(text);
  }

  private static void code(DataOutputStream out, boolean valid) throws Exception {
    out.writeShort(7); out.writeInt(14); out.writeShort(1); out.writeShort(0);
    out.writeInt(2); out.writeByte(valid ? 3 : 1); out.writeByte(172);
    out.writeShort(0); out.writeShort(0);
  }

  private static Class<?> define(int mode, boolean valid) throws Exception {
    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    DataOutputStream out = new DataOutputStream(buffer);
    out.writeInt(0xcafebabe); out.writeShort(0); out.writeShort(69); out.writeShort(12);
    utf(out, "OrderCase" + sequence++); out.writeByte(7); out.writeShort(1);
    utf(out, "java/lang/Object"); out.writeByte(7); out.writeShort(3);
    utf(out, "value"); utf(out, "()I"); utf(out, "Code"); utf(out, "NestHost");
    utf(out, "FutureAttribute"); utf(out, "NestMembers"); utf(out, "BootstrapMethods");
    out.writeShort(0x21); out.writeShort(2); out.writeShort(4); out.writeShort(0); out.writeShort(0);
    out.writeShort(1); out.writeShort(9); out.writeShort(5); out.writeShort(6);
    out.writeShort(mode == 8 ? 2 : 1); code(out, valid);
    if (mode == 8) code(out, valid);
    if (mode == 0 || mode == 8) out.writeShort(0);
    else if (mode == 4) {
      out.writeShort(2);
      for (int i = 0; i < 2; ++i) { out.writeShort(8); out.writeInt(2); out.writeShort(4); }
    } else {
      out.writeShort(1);
      if (mode == 1) { out.writeShort(8); out.writeInt(1); out.writeByte(0); }
      else if (mode == 2) { out.writeShort(8); out.writeInt(3); out.writeShort(4); out.writeByte(0); }
      else if (mode == 3) { out.writeShort(8); out.writeInt(2); out.writeShort(0); }
      else if (mode == 5) { out.writeShort(9); out.writeInt(3); out.writeShort(255); out.writeByte(0); }
      else if (mode == 6) { out.writeShort(10); out.writeInt(2); out.writeShort(1); }
      else if (mode == 7) { out.writeShort(11); out.writeInt(1); out.writeByte(0); }
      else throw new AssertionError("fixture mode");
    }
    Class<?> type = new Loader().define(buffer.toByteArray());
    type.getDeclaredMethods();
    return type;
  }

  private static void valid(String name, int mode) throws Exception {
    Object result = define(mode, true).getDeclaredMethod("value").invoke(null);
    if (!Integer.valueOf(0).equals(result)) throw new AssertionError(name);
    System.out.println(name + " accepted");
  }

  private static void invalid(String name, int mode, boolean format) throws Exception {
    try {
      define(mode, false);
      throw new AssertionError(name + " accepted");
    } catch (ClassFormatError expected) {
      if (!format) throw new AssertionError(name + " unexpected format error");
      System.out.println(name + ":ClassFormatError");
    } catch (VerifyError expected) {
      if (format) throw new AssertionError(name + " format error masked by verification");
      System.out.println(name + ":VerifyError");
    }
  }

  public static void main(String[] args) throws Exception {
    valid("plain", 0);
    valid("unknown-attribute", 5);
    invalid("plain-bad-bytecode", 0, false);
    invalid("unknown-attribute-bad-bytecode", 5, false);
    invalid("short-nest-host", 1, true);
    invalid("long-nest-host", 2, true);
    invalid("bad-nest-host-index", 3, true);
    invalid("duplicate-nest-host", 4, true);
    invalid("truncated-nest-members", 6, true);
    invalid("truncated-bootstrap-methods", 7, true);
    invalid("duplicate-code", 8, true);
  }
}
