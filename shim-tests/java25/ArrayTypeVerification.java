/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;

public class ArrayTypeVerification {
  private static int sequence;
  private static final int INT = -1, LONG = -2, OBJECT = -3, INT2 = -4, OBJECT2 = -5,
      BOOLEAN = -6, BYTE = -7, CHAR = -8, SHORT = -9, FLOAT = -10, DOUBLE = -11,
      STRING_ARRAY = -12, STRING = -13, FIELD = -14, TAKE = -15, INIT = -16, OBJECT_ARRAY = -17, MAKE = -18, DYNAMIC = -19;

  private static class Pool {
    final ArrayList<byte[]> entries = new ArrayList<byte[]>();
    int utf(String value) throws Exception {
      ByteArrayOutputStream bytes = new ByteArrayOutputStream();
      DataOutputStream out = new DataOutputStream(bytes);
      out.writeByte(1); out.writeUTF(value); entries.add(bytes.toByteArray());
      return entries.size();
    }
    int entry(int tag, int first, int second) throws Exception {
      ByteArrayOutputStream bytes = new ByteArrayOutputStream();
      DataOutputStream out = new DataOutputStream(bytes);
      out.writeByte(tag); out.writeShort(first);
      if (tag != 7) out.writeShort(second);
      entries.add(bytes.toByteArray()); return entries.size();
    }
    int handle(int member) throws Exception {
      ByteArrayOutputStream bytes = new ByteArrayOutputStream();
      DataOutputStream out = new DataOutputStream(bytes);
      out.writeByte(15); out.writeByte(6); out.writeShort(member);
      entries.add(bytes.toByteArray()); return entries.size();
    }
    int type(String name) throws Exception { return entry(7, utf(name), 0); }
    int member(int tag, int owner, String name, String descriptor) throws Exception {
      return entry(tag, owner, entry(12, utf(name), utf(descriptor)));
    }
    void write(DataOutputStream out) throws Exception {
      out.writeShort(entries.size() + 1);
      for (byte[] entry : entries) out.write(entry);
    }
  }

  private static class Loader extends ClassLoader {
    Class<?> define(byte[] bytes) { return defineClass(null, bytes, 0, bytes.length); }
  }

  private static void bytes(DataOutputStream out, int[] values, int[] constants) throws Exception {
    for (int value : values) {
      value = value < 0 ? constants[-value] : value;
      if (value < 0 || value > 255) throw new AssertionError("fixture constant overflow");
      out.writeByte(value);
    }
  }

  private static void method(DataOutputStream out, int access, int name, int descriptor, int codeName,
      int mapName, int maxStack, int maxLocals, int[] code, int[] frames, int[] constants) throws Exception {
    out.writeShort(access); out.writeShort(name); out.writeShort(descriptor); out.writeShort(1);
    out.writeShort(codeName);
    out.writeInt(12 + code.length + (frames == null ? 0 : 6 + frames.length));
    out.writeShort(maxStack); out.writeShort(maxLocals); out.writeInt(code.length);
    bytes(out, code, constants); out.writeShort(0); out.writeShort(frames == null ? 0 : 1);
    if (frames != null) { out.writeShort(mapName); out.writeInt(frames.length); bytes(out, frames, constants); }
  }

  private static Method define(String descriptor, int maxStack, int maxLocals, int[] code, int[] frames)
      throws Exception {
    Pool pool = new Pool();
    int self = pool.type("ArrayCase" + sequence++), object = pool.type("java/lang/Object");
    int valueName = pool.utf("value"), valueSpec = pool.utf(descriptor);
    int codeName = pool.utf("Code"), mapName = pool.utf("StackMapTable");
    int initName = pool.utf("<init>"), voidSpec = pool.utf("()V");
    int fieldName = pool.utf("array"), fieldSpec = pool.utf("[I");
    int takeName = pool.utf("take"), takeSpec = pool.utf("([I)I");
    String[] types = {"[I", "[J", "java/lang/Object", "[[I", "[[Ljava/lang/Object;", "[Z", "[B", "[C",
        "[S", "[F", "[D", "[Ljava/lang/String;", "java/lang/String"};
    int makeName = pool.utf("make"), makeSpec = pool.utf("()[I");
    int bootstrapName = pool.utf("BootstrapMethods");
    int bootstrap = pool.handle(pool.member(10, pool.type("ArrayTypeVerification"), "arrayConstant",
        "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/Class;)Ljava/lang/Object;"));
    int[] constants = new int[20];
    for (int i = 0; i < types.length; ++i) constants[i + 1] = pool.type(types[i]);
    constants[-FIELD] = pool.member(9, self, "array", "[I");
    constants[-TAKE] = pool.member(10, self, "take", "([I)I");
    constants[-INIT] = pool.member(10, object, "<init>", "()V");
    constants[-OBJECT_ARRAY] = pool.type("[Ljava/lang/Object;");
    constants[-MAKE] = pool.member(10, self, "make", "()[I");
    constants[-DYNAMIC] = pool.entry(17, 0, pool.entry(12, pool.utf("constant"), pool.utf("[I")));
    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    DataOutputStream out = new DataOutputStream(buffer);
    out.writeInt(0xcafebabe); out.writeShort(0); out.writeShort(69); pool.write(out);
    out.writeShort(0x21); out.writeShort(self); out.writeShort(object); out.writeShort(0);
    out.writeShort(1); out.writeShort(9); out.writeShort(fieldName); out.writeShort(fieldSpec); out.writeShort(0);
    out.writeShort(4);
    method(out, 1, initName, voidSpec, codeName, mapName, 1, 1,
        new int[] {42,183,0,INIT,177}, null, constants);
    method(out, 9, takeName, takeSpec, codeName, mapName, 1, 1, new int[] {42,190,172}, null, constants);
    method(out, 9, makeName, makeSpec, codeName, mapName, 1, 0, new int[] {4,188,10,176}, null, constants);
    method(out, 9, valueName, valueSpec, codeName, mapName, maxStack, maxLocals, code, frames, constants);
    out.writeShort(1); out.writeShort(bootstrapName); out.writeInt(6);
    out.writeShort(1); out.writeShort(bootstrap); out.writeShort(0);
    for (Method method : new Loader().define(buffer.toByteArray()).getDeclaredMethods()) {
      if (method.getName().equals("value")) return method;
    }
    throw new AssertionError("missing value method");
  }

  private static void valid(String name, int expected, String descriptor, int stack, int locals,
      int[] code, int[] frames, Object... arguments) throws Exception {
    Object result = define(descriptor, stack, locals, code, frames).invoke(null, arguments);
    if (!Integer.valueOf(expected).equals(result)) throw new AssertionError(name + " result=" + result);
    System.out.println(name + " accepted");
  }

  private static void definition(String name, String descriptor, int stack, int locals,
      int[] code, int[] frames) throws Exception {
    define(descriptor, stack, locals, code, frames);
    System.out.println(name + " accepted");
  }

  private static void bad(String name, String descriptor, int stack, int locals, int[] code, int[] frames)
      throws Exception {
    try {
      define(descriptor, stack, locals, code, frames);
      throw new AssertionError(name + " accepted");
    } catch (VerifyError expected) { System.out.println(name + ":VerifyError"); }
  }

  public static Object arrayConstant(java.lang.invoke.MethodHandles.Lookup lookup, String name, Class<?> type) {
    if (type != int[].class) throw new AssertionError("bootstrap type");
    return new int[] {7};
  }

  public static void main(String[] args) throws Exception {
    int[][] types = {{4,51,84,4,0},{8,51,84,4,0},{5,52,85,4,0},{9,53,86,4,0},
        {10,46,79,4,0},{11,47,80,10,136},{6,48,81,12,139},{7,49,82,15,142}};
    for (int i = 0; i < types.length; ++i) {
      int[] t = types[i];
      int[] code = t[4] == 0 ? new int[] {4,188,t[0],89,3,t[3],t[2],3,t[1],172}
          : new int[] {4,188,t[0],89,3,t[3],t[2],3,t[1],t[4],172};
      valid("primitive-" + i, 1, "()I", 5, 0, code, null);
    }
    valid("boolean-byte-load", 1, "([Z)I", 2, 1, new int[] {42,3,51,172}, null, new boolean[] {true});
    valid("byte-boolean-store", 1, "([B)I", 3, 1, new int[] {42,3,4,84,42,3,51,172}, null, new byte[] {0});
    valid("reference-array", 1, "()I", 4, 0,
        new int[] {4,189,0,OBJECT,89,3,1,83,3,50,87,4,172}, null);
    valid("nested-primitive", 2, "()I", 4, 0,
        new int[] {4,189,0,INT,89,3,5,188,10,83,3,50,190,172}, null);
    valid("multianewarray", 2, "()I", 2, 0, new int[] {4,5,197,0,INT2,2,3,50,190,172}, null);
    valid("cast-array", 3, "(Ljava/lang/Object;)I", 1, 1, new int[] {42,192,0,INT,190,172}, null, new int[3]);
    valid("field-array", 1, "()I", 1, 0, new int[] {4,188,10,179,0,FIELD,178,0,FIELD,190,172}, null);
    valid("argument-array", 2, "()I", 1, 0, new int[] {5,188,10,184,0,TAKE,172}, null);
    valid("null-map-array", 1, "()I", 1, 1, new int[] {1,75,42,87,4,172},
        new int[] {0,1,255,0,2,0,1,7,0,INT,0,0});
    valid("array-map-object", 2, "([I)I", 1, 1, new int[] {42,87,5,172},
        new int[] {0,1,255,0,0,0,1,7,0,OBJECT,0,0}, new int[0]);
    definition("null-iaload", "()I", 2, 0, new int[] {1,3,46,172}, null);
    definition("null-aaload", "()[I", 2, 0, new int[] {1,3,50,176}, null);
    definition("null-arraylength", "()I", 1, 0, new int[] {1,190,172}, null);
    definition("array-covariance", "()[Ljava/lang/Object;", 1, 0, new int[] {4,189,0,INT,176}, null);
    definition("runtime-array-store-check", "([Ljava/lang/String;Ljava/lang/Object;)I", 3, 2,
        new int[] {42,3,43,83,3,172}, null);
    valid("nested-array-join", 1, "()I", 1, 0,
        new int[] {3,153,0,10,4,189,0,INT,167,0,7,4,189,0,LONG,190,172},
        new int[] {0,2,11,67,7,0,OBJECT_ARRAY});
    valid("null-array-join", 1, "()I", 1, 0,
        new int[] {4,153,0,9,4,188,10,167,0,4,1,190,172}, new int[] {0,2,10,64,7,0,INT});
    definition("nested-runtime-store-check", "([[ILjava/lang/Object;)I", 3, 2,
        new int[] {42,3,43,83,3,172}, null);
    bad("primitive-return-object-array", "()[Ljava/lang/Object;", 1, 0, new int[] {4,188,10,176}, null);
    bad("nested-return-object-matrix", "()[[Ljava/lang/Object;", 1, 0, new int[] {4,189,0,INT,176}, null);
    bad("array-erased-by-frame", "([I)I", 1, 1, new int[] {42,190,172},
        new int[] {0,1,255,0,0,0,1,7,0,OBJECT,0,0});
    bad("byte-is-not-boolean-array", "([B)I", 1, 1, new int[] {3,172},
        new int[] {0,1,255,0,0,0,1,7,0,BOOLEAN,0,0});
    valid("method-array-result", 0, "()I", 2, 0, new int[] {184,0,MAKE,3,46,172}, null);
    valid("dynamic-array-result", 7, "()I", 2, 0, new int[] {18,DYNAMIC,3,46,172}, null);
    bad("wrong-method-array-result", "()I", 2, 0, new int[] {184,0,MAKE,3,47,136,172}, null);
    bad("wrong-dynamic-array-result", "()I", 2, 0, new int[] {18,DYNAMIC,3,47,136,172}, null);
    bad("object-arraylength", "(Ljava/lang/Object;)I", 1, 1, new int[] {42,190,172}, null);
    bad("object-iaload", "(Ljava/lang/Object;)I", 2, 1, new int[] {42,3,46,172}, null);
    for (int i = 0; i < types.length; ++i) for (int j = 0; j < types.length; ++j) {
      if (types[i][1] == types[j][1]) continue;
      int[] t = types[j];
      int[] code = t[4] == 0 ? new int[] {4,188,types[i][0],3,t[1],172}
          : new int[] {4,188,types[i][0],3,t[1],t[4],172};
      bad("wrong-load-" + i + "-" + j, "()I", 3, 0, code, null);
      bad("wrong-store-" + i + "-" + j, "()I", 4, 0,
          new int[] {4,188,types[i][0],3,t[3],t[2],3,172}, null);
    }
    bad("primitive-aaload", "()I", 2, 0, new int[] {4,188,10,3,50,87,3,172}, null);
    bad("primitive-aastore", "()I", 3, 0, new int[] {4,188,10,3,1,83,3,172}, null);
    bad("reference-iaload", "()I", 2, 0, new int[] {4,189,0,OBJECT,3,46,172}, null);
    bad("nested-iaload", "()I", 2, 0, new int[] {4,189,0,INT,3,46,172}, null);
    bad("wrong-return-array", "()[I", 1, 0, new int[] {4,188,11,176}, null);
    bad("object-return-array", "(Ljava/lang/Object;)[I", 1, 1, new int[] {42,176}, null);
    bad("wrong-argument-array", "()I", 1, 0, new int[] {4,188,11,184,0,TAKE,172}, null);
    bad("wrong-field-array", "()I", 1, 0, new int[] {4,188,11,179,0,FIELD,3,172}, null);
    bad("wrong-frame-array", "([I)I", 1, 1, new int[] {3,172}, new int[] {0,1,255,0,0,0,1,7,0,LONG,0,0});
    bad("narrow-frame-array", "(Ljava/lang/Object;)I", 1, 1, new int[] {3,172},
        new int[] {0,1,255,0,0,0,1,7,0,INT,0,0});
  }
}
