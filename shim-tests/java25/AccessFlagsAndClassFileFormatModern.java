/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */


import java.lang.reflect.AccessFlag;
import java.lang.reflect.ClassFileFormatVersion;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Set;

public class AccessFlagsAndClassFileFormatModern {

  public static volatile int sampleField = 100;
  private static final String CONST_STR = "shim";

  public static final void sampleMethod(final int count, String label) {}
  private synchronized void privateMethod() {}

  public static void main(String[] args) throws Exception {
    testClassFileFormatVersion();
    testAccessFlagAttributes();
    testMaskToAccessFlags();
    testReflectiveAccessFlags();
    System.out.println("ALL ACCESS FLAGS AND CLASS FILE FORMAT TESTS COMPLETED");
  }

  private static void testClassFileFormatVersion() {
    if (ClassFileFormatVersion.fromMajor(65) != ClassFileFormatVersion.RELEASE_21) throw new AssertionError();
    if (ClassFileFormatVersion.fromMajor(52) != ClassFileFormatVersion.RELEASE_8) throw new AssertionError();
    if (ClassFileFormatVersion.fromMajor(45) != ClassFileFormatVersion.RELEASE_1) throw new AssertionError();

    if (ClassFileFormatVersion.RELEASE_21.major() != 65) throw new AssertionError();
    if (ClassFileFormatVersion.RELEASE_8.major() != 52) throw new AssertionError();
    if (ClassFileFormatVersion.RELEASE_1.major() != 45) throw new AssertionError();
    if (ClassFileFormatVersion.RELEASE_0.major() != 45) throw new AssertionError();

    if (ClassFileFormatVersion.RELEASE_0.runtimeVersion() != null) throw new AssertionError();
    if (ClassFileFormatVersion.RELEASE_5.runtimeVersion() != null) throw new AssertionError();
    if (ClassFileFormatVersion.RELEASE_6.runtimeVersion().feature() != 6) throw new AssertionError();
    if (ClassFileFormatVersion.RELEASE_21.runtimeVersion().feature() != 21) throw new AssertionError();

    try {
      ClassFileFormatVersion r25 = ClassFileFormatVersion.valueOf("RELEASE_25");
      if (r25.major() != 69 || r25.runtimeVersion().feature() != 25) throw new AssertionError();
      if (ClassFileFormatVersion.fromMajor(69) != r25) throw new AssertionError();
      if (ClassFileFormatVersion.latest() != r25) throw new AssertionError();
    } catch (IllegalArgumentException ignored) {
      if (ClassFileFormatVersion.latest() != ClassFileFormatVersion.RELEASE_21) throw new AssertionError();
    }

    try {
      ClassFileFormatVersion.fromMajor(44);
      throw new AssertionError();
    } catch (IllegalArgumentException expected) {}

    try {
      ClassFileFormatVersion.fromMajor(70);
      throw new AssertionError();
    } catch (IllegalArgumentException expected) {}

    System.out.println("ClassFileFormatVersion: OK");
  }

  private static void testAccessFlagAttributes() {
    if (AccessFlag.PUBLIC.mask() != 1) throw new AssertionError();
    if (!AccessFlag.PUBLIC.sourceModifier()) throw new AssertionError();
    if (AccessFlag.SUPER.sourceModifier()) throw new AssertionError();
    if (!AccessFlag.STATIC.sourceModifier()) throw new AssertionError();
    if (AccessFlag.SYNTHETIC.sourceModifier()) throw new AssertionError();

    Set<AccessFlag.Location> pubLocs = AccessFlag.PUBLIC.locations();
    if (!pubLocs.contains(AccessFlag.Location.CLASS)) throw new AssertionError();
    if (!pubLocs.contains(AccessFlag.Location.FIELD)) throw new AssertionError();
    if (!pubLocs.contains(AccessFlag.Location.METHOD)) throw new AssertionError();
    if (!pubLocs.contains(AccessFlag.Location.INNER_CLASS)) throw new AssertionError();
    if (pubLocs.contains(AccessFlag.Location.METHOD_PARAMETER)) throw new AssertionError();

    Set<AccessFlag.Location> superLocs = AccessFlag.SUPER.locations();
    if (!superLocs.contains(AccessFlag.Location.CLASS)) throw new AssertionError();
    if (superLocs.contains(AccessFlag.Location.METHOD)) throw new AssertionError();

    System.out.println("AccessFlag Attributes: OK");
  }

  private static void testMaskToAccessFlags() throws Exception {
    Set<AccessFlag> classFlags = AccessFlag.maskToAccessFlags(0x0001, AccessFlag.Location.CLASS);
    if (classFlags.size() != 1 || !classFlags.contains(AccessFlag.PUBLIC)) throw new AssertionError();

    Set<AccessFlag> multiClass = AccessFlag.maskToAccessFlags(0x0011, AccessFlag.Location.CLASS);
    if (multiClass.size() != 2 || !multiClass.contains(AccessFlag.PUBLIC) || !multiClass.contains(AccessFlag.FINAL)) {
      throw new AssertionError();
    }

    Set<AccessFlag> fieldFlags = AccessFlag.maskToAccessFlags(0x0008 | 0x0040, AccessFlag.Location.FIELD);
    if (fieldFlags.size() != 2 || !fieldFlags.contains(AccessFlag.STATIC) || !fieldFlags.contains(AccessFlag.VOLATILE)) {
      throw new AssertionError();
    }

    try {
      AccessFlag.maskToAccessFlags(0x0002, AccessFlag.Location.CLASS);
      throw new AssertionError();
    } catch (IllegalArgumentException expected) {}

    try {
      java.lang.reflect.Method m3 = AccessFlag.class.getMethod("maskToAccessFlags",
          int.class, AccessFlag.Location.class, ClassFileFormatVersion.class);
      @SuppressWarnings("unchecked")
      Set<AccessFlag> versionedClass = (Set<AccessFlag>) m3.invoke(null,
          0x0001, AccessFlag.Location.CLASS, ClassFileFormatVersion.RELEASE_21);
      if (!versionedClass.contains(AccessFlag.PUBLIC)) throw new AssertionError();
    } catch (NoSuchMethodException ignored) {
    }

    System.out.println("MaskToAccessFlags: OK");
  }

  private static void testReflectiveAccessFlags() throws Exception {
    Set<AccessFlag> strFlags = String.class.accessFlags();
    if (!strFlags.contains(AccessFlag.PUBLIC) || !strFlags.contains(AccessFlag.FINAL)) {
      throw new AssertionError("String flags: " + strFlags);
    }

    Set<AccessFlag> runFlags = Runnable.class.accessFlags();
    if (!runFlags.contains(AccessFlag.PUBLIC) || !runFlags.contains(AccessFlag.INTERFACE) || !runFlags.contains(AccessFlag.ABSTRACT)) {
      throw new AssertionError("Runnable flags: " + runFlags);
    }

    Set<AccessFlag> arrFlags = String[].class.accessFlags();
    if (!arrFlags.contains(AccessFlag.PUBLIC) || !arrFlags.contains(AccessFlag.FINAL) || !arrFlags.contains(AccessFlag.ABSTRACT)) {
      throw new AssertionError("Array flags: " + arrFlags);
    }

    Field sf = AccessFlagsAndClassFileFormatModern.class.getField("sampleField");
    Set<AccessFlag> sfFlags = sf.accessFlags();
    if (!sfFlags.contains(AccessFlag.PUBLIC) || !sfFlags.contains(AccessFlag.STATIC) || !sfFlags.contains(AccessFlag.VOLATILE)) {
      throw new AssertionError("Field flags: " + sfFlags);
    }

    Field cs = AccessFlagsAndClassFileFormatModern.class.getDeclaredField("CONST_STR");
    Set<AccessFlag> csFlags = cs.accessFlags();
    if (!csFlags.contains(AccessFlag.PRIVATE) || !csFlags.contains(AccessFlag.STATIC) || !csFlags.contains(AccessFlag.FINAL)) {
      throw new AssertionError("Const string flags: " + csFlags);
    }

    Method sm = AccessFlagsAndClassFileFormatModern.class.getMethod("sampleMethod", int.class, String.class);
    Set<AccessFlag> smFlags = sm.accessFlags();
    if (!smFlags.contains(AccessFlag.PUBLIC) || !smFlags.contains(AccessFlag.STATIC) || !smFlags.contains(AccessFlag.FINAL)) {
      throw new AssertionError("Method flags: " + smFlags);
    }

    Method pm = AccessFlagsAndClassFileFormatModern.class.getDeclaredMethod("privateMethod");
    Set<AccessFlag> pmFlags = pm.accessFlags();
    if (!pmFlags.contains(AccessFlag.PRIVATE) || !pmFlags.contains(AccessFlag.SYNCHRONIZED)) {
      throw new AssertionError("Private method flags: " + pmFlags);
    }

    Parameter[] params = sm.getParameters();
    if (params.length != 2) throw new AssertionError();
    Set<AccessFlag> p0Flags = params[0].accessFlags();
    Set<AccessFlag> p1Flags = params[1].accessFlags();
    if (p0Flags == null || p1Flags == null) throw new AssertionError();

    System.out.println("Reflective AccessFlags: OK");
  }
}
