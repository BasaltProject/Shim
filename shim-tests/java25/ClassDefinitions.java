/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.concurrent.CountDownLatch;

public class ClassDefinitions {
  private static void utf(DataOutputStream out, String text) throws Exception {
    ByteArrayOutputStream encoded = new ByteArrayOutputStream();
    for (int i = 0; i < text.length(); ++i) {
      int c = text.charAt(i);
      if (c != 0 && c < 128) encoded.write(c);
      else if (c < 2048) {
        encoded.write(0xc0 | (c >>> 6)); encoded.write(0x80 | (c & 63));
      } else {
        encoded.write(0xe0 | (c >>> 12)); encoded.write(0x80 | ((c >>> 6) & 63)); encoded.write(0x80 | (c & 63));
      }
    }
    out.writeShort(encoded.size()); out.write(encoded.toByteArray());
  }

  private static byte[] data(String name, String parent) throws Exception {
    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    DataOutputStream out = new DataOutputStream(buffer);
    out.writeInt(0xcafebabe); out.writeShort(0); out.writeShort(69); out.writeShort(10);
    out.writeByte(1); utf(out, name.replace('.', '/'));
    out.writeByte(7); out.writeShort(1);
    out.writeByte(1); utf(out, parent.replace('.', '/'));
    out.writeByte(7); out.writeShort(3);
    out.writeByte(1); utf(out, "<init>");
    out.writeByte(1); utf(out, "()V");
    out.writeByte(12); out.writeShort(5); out.writeShort(6);
    out.writeByte(10); out.writeShort(4); out.writeShort(7);
    out.writeByte(1); utf(out, "Code");
    out.writeShort(0x21); out.writeShort(2); out.writeShort(4); out.writeShort(0); out.writeShort(0);
    out.writeShort(1); out.writeShort(1); out.writeShort(5); out.writeShort(6); out.writeShort(1);
    out.writeShort(9); out.writeInt(17); out.writeShort(1); out.writeShort(1); out.writeInt(5);
    out.writeByte(42); out.writeByte(183); out.writeShort(8); out.writeByte(177);
    out.writeShort(0); out.writeShort(0); out.writeShort(0);
    return buffer.toByteArray();
  }

  private static class Loader extends ClassLoader {
    int missing;
    Loader() { super(ClassDefinitions.class.getClassLoader()); }
    Class<?> define(String name, byte[] bytes, int start, int length) {
      return defineClass(name, bytes, start, length);
    }
    Class<?> seen(String name) { return findLoadedClass(name); }
    Class<?> legacy(byte[] bytes, int start, int length) { return defineClass(bytes, start, length); }
    protected Class<?> findClass(String name) throws ClassNotFoundException {
      ++missing;
      throw new ClassNotFoundException(name);
    }
  }

  private static void attempt(String label, Loader loader, String name, byte[] bytes, int start, int length) {
    try {
      Class<?> type = loader.define(name, bytes, start, length);
      System.out.println(label + ":defined:" + type.getName());
    } catch (Throwable error) { System.out.println(label + ":" + error.getClass().getSimpleName()); }
  }

  public static void main(String[] args) throws Exception {
    byte[] bytes = data("realm.Sample", "java.lang.Object");
    for (String name : new String[] {"realm.Sample", null, "other.Name", "realm/Sample", "", "[Lrealm.Sample;",
        "java.lang.Injected", "javafx.Sample"}) {
      attempt("name:" + name, new Loader(), name, bytes, 0, bytes.length);
    }
    Loader mismatch = new Loader();
    attempt("mismatch", mismatch, "other.Name", bytes, 0, bytes.length);
    System.out.println("mismatch-unpublished:" + (mismatch.seen("realm.Sample") == null));
    attempt("mismatch-recovery", mismatch, "realm.Sample", bytes, 0, bytes.length);
    Loader duplicate = new Loader();
    Class<?> first = duplicate.define("realm.Sample", bytes, 0, bytes.length);
    attempt("duplicate-named", duplicate, "realm.Sample", bytes, 0, bytes.length);
    attempt("duplicate-unnamed", duplicate, null, bytes, 0, bytes.length);
    System.out.println("duplicate-preserves-class:" + (duplicate.seen("realm.Sample") == first));
    byte[] malformed = new byte[] {0, 0, 0, 0};
    attempt("duplicate-malformed", duplicate, "realm.Sample", malformed, 0, malformed.length);
    System.out.println("legacy:" + new Loader().legacy(bytes, 0, bytes.length).getName());
    byte[] carrier = new byte[bytes.length * 2 + 34];
    int prefix = bytes.length + 17;
    System.arraycopy(bytes, 0, carrier, prefix, bytes.length);
    attempt("offset", new Loader(), "realm.Sample", carrier, prefix, bytes.length);
    attempt("offset-unnamed", new Loader(), null, carrier, prefix, bytes.length);
    for (int[] range : new int[][] {{-1, 1}, {0, -1}, {0, bytes.length + 1}, {bytes.length, 0}, {0, 0},
        {Integer.MAX_VALUE, Integer.MAX_VALUE}, {Integer.MAX_VALUE, 1}, {0, Integer.MAX_VALUE},
        {0, Integer.MIN_VALUE}, {bytes.length + 1, 0}}) {
      attempt("range:" + range[0] + ":" + range[1], new Loader(), null, bytes, range[0], range[1]);
    }
    attempt("empty-array", new Loader(), null, new byte[0], 0, 0);
    int classOffset = 10;
    for (int i = 1; i < 10; ++i) {
      int tag = bytes[classOffset++] & 255;
      if (tag == 1) {
        int length = ((bytes[classOffset] & 255) << 8) | (bytes[classOffset + 1] & 255);
        classOffset += 2 + length;
      } else classOffset += tag == 7 ? 2 : 4;
    }
    for (int index : new int[] {0, 1}) {
      byte[] invalidIndex = new byte[bytes.length];
      System.arraycopy(bytes, 0, invalidIndex, 0, bytes.length);
      invalidIndex[classOffset + 2] = 0;
      invalidIndex[classOffset + 3] = (byte) index;
      attempt("invalid-this:" + index, new Loader(), null, invalidIndex, 0, invalidIndex.length);
    }
    for (String actual : new String[] {"", "[I", "bad..Name", "bad;Name", "/bad", "bad/"}) {
      byte[] invalidName = data(actual, "java.lang.Object");
      attempt("invalid-actual:" + actual, new Loader(), null, invalidName, 0, invalidName.length);
    }
    for (String name : new String[] {null, "bad/name", "java.lang.Forbidden"}) {
      attempt("null-bytes:" + name, new Loader(), name, null, 0, 0);
      attempt("invalid-range:" + name, new Loader(), name, bytes, -1, 1);
    }
    byte[] forbidden = data("java.lang.Injected", "java.lang.Object");
    attempt("actual-forbidden", new Loader(), null, forbidden, 0, forbidden.length);
    attempt("forbidden-name-mismatch", new Loader(), "realm.Sample", forbidden, 0, forbidden.length);
    for (String name : new String[] {"javafx.Valid", "javax.Valid"}) {
      byte[] allowed = data(name, "java.lang.Object");
      attempt("allowed-prefix", new Loader(), name, allowed, 0, allowed.length);
    }
    Loader parent = new Loader();
    byte[] child = data("realm.Child", "realm.Missing");
    attempt("wrong-before-parent", parent, "other.Child", child, 0, child.length);
    System.out.println("parent-requests:" + parent.missing);
    for (String name : new String[] {"réalm.Δ", "realm.\ud801\udc00Token"}) {
      byte[] unicode = data(name, "java.lang.Object");
      Class<?> defined = new Loader().define(name, unicode, 0, unicode.length);
      System.out.println("unicode-match:" + defined.getName().equals(name));
      attempt("unicode-mismatch", new Loader(), name + "Other", unicode, 0, unicode.length);
    }
    for (int round = 0; round < 3; ++round) {
      final Loader loader = new Loader();
      final byte[] contents = bytes;
      final CountDownLatch gate = new CountDownLatch(1);
      final Object[] outcomes = new Object[2];
      Thread[] threads = new Thread[2];
      for (int i = 0; i < threads.length; ++i) {
        final int slot = i;
        threads[i] = new Thread(new Runnable() {
          public void run() {
            try {
              gate.await();
              outcomes[slot] = loader.define("realm.Sample", contents, 0, contents.length);
            } catch (Throwable error) { outcomes[slot] = error; }
          }
        });
        threads[i].start();
      }
      gate.countDown();
      for (Thread thread : threads) thread.join();
      int success = 0, rejected = 0;
      for (Object result : outcomes) {
        if (result instanceof Class) {
          ++success;
          if (loader.seen("realm.Sample") != result) throw new AssertionError();
        } else if (result instanceof LinkageError) ++rejected;
        else throw new AssertionError(String.valueOf(result));
      }
      System.out.println("race:" + success + ":" + rejected);
    }
  }
}
