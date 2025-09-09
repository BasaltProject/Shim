// Minimum release: 17
/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.constant.DynamicConstantDesc;
import java.util.Optional;

public class WrapperAndSystemModern {
  public static void main(String[] args) {
    testPrimitiveConstants();
    testHashCodes();
    testSurrogatesAndBits();
    testBooleanLogical();
    testDescribeConstable();
    testSystemProperties();
    testSystemRedirection();
    testClassLoaderModern();
    System.out.println("WrapperAndSystemModern OK");
  }

  private static void testPrimitiveConstants() {
    System.out.println("Byte.SIZE=" + Byte.SIZE + " BYTES=" + Byte.BYTES);
    System.out.println("Short.SIZE=" + Short.SIZE + " BYTES=" + Short.BYTES);
    System.out.println("Character.SIZE=" + Character.SIZE + " BYTES=" + Character.BYTES);
    System.out.println("Integer.SIZE=" + Integer.SIZE + " BYTES=" + Integer.BYTES);
    System.out.println("Long.SIZE=" + Long.SIZE + " BYTES=" + Long.BYTES);

    System.out.println("Char.MIN_VALUE=" + (int) Character.MIN_VALUE + " MAX_VALUE=" + (int) Character.MAX_VALUE);
    System.out.println("Char.MIN_HIGH=" + (int) Character.MIN_HIGH_SURROGATE + " MAX_HIGH=" + (int) Character.MAX_HIGH_SURROGATE);
    System.out.println("Char.MIN_LOW=" + (int) Character.MIN_LOW_SURROGATE + " MAX_LOW=" + (int) Character.MAX_LOW_SURROGATE);
    System.out.println("Char.MIN_SURROGATE=" + (int) Character.MIN_SURROGATE + " MAX_SURROGATE=" + (int) Character.MAX_SURROGATE);
    System.out.println("Char.MIN_SUPP=" + Character.MIN_SUPPLEMENTARY_CODE_POINT);
    System.out.println("Char.MIN_CP=" + Character.MIN_CODE_POINT + " MAX_CP=" + Character.MAX_CODE_POINT);
  }

  private static void testHashCodes() {
    System.out.println("Byte.hashCode=" + Byte.hashCode((byte) 42));
    System.out.println("Short.hashCode=" + Short.hashCode((short) 1000));
    System.out.println("Character.hashCode=" + Character.hashCode('X'));
  }

  private static void testSurrogatesAndBits() {
    System.out.println("isSurrogate high=" + Character.isSurrogate('\uD800'));
    System.out.println("isSurrogate low=" + Character.isSurrogate('\uDC00'));
    System.out.println("isSurrogate ordinary=" + Character.isSurrogate('A'));

    char high = Character.highSurrogate(0x1F600);
    char low = Character.lowSurrogate(0x1F600);
    System.out.println("highSurrogate=" + (int) high + " lowSurrogate=" + (int) low);

    System.out.println("Integer.highestOneBit=" + Integer.toHexString(Integer.highestOneBit(0x00FF0000)));
    System.out.println("Integer.lowestOneBit=" + Integer.toHexString(Integer.lowestOneBit(0x00FF0080)));
    System.out.println("Integer.reverse=" + Integer.toHexString(Integer.reverse(0x12345678)));

    System.out.println("Long.highestOneBit=" + Long.toHexString(Long.highestOneBit(0x0000000100000000L)));
    System.out.println("Long.lowestOneBit=" + Long.toHexString(Long.lowestOneBit(0x0000000100000080L)));
    System.out.println("Long.reverse=" + Long.toHexString(Long.reverse(0x1234567890ABCDEFL)));
  }

  private static void testBooleanLogical() {
    System.out.println("logicalAnd=" + Boolean.logicalAnd(true, false));
    System.out.println("logicalOr=" + Boolean.logicalOr(true, false));
    System.out.println("logicalXor1=" + Boolean.logicalXor(true, false));
    System.out.println("logicalXor2=" + Boolean.logicalXor(true, true));
  }

  private static void testDescribeConstable() {
    Optional<DynamicConstantDesc<Byte>> bDesc = Byte.valueOf((byte) 12).describeConstable();
    System.out.println("Byte.describeConstable present=" + bDesc.isPresent());
    if (bDesc.isPresent()) {
      System.out.println("Byte desc type=" + bDesc.get().constantType().descriptorString());
    }

    Optional<DynamicConstantDesc<Short>> sDesc = Short.valueOf((short) 34).describeConstable();
    System.out.println("Short.describeConstable present=" + sDesc.isPresent());
    if (sDesc.isPresent()) {
      System.out.println("Short desc type=" + sDesc.get().constantType().descriptorString());
    }

    Optional<DynamicConstantDesc<Character>> cDesc = Character.valueOf('K').describeConstable();
    System.out.println("Character.describeConstable present=" + cDesc.isPresent());
    if (cDesc.isPresent()) {
      System.out.println("Character desc type=" + cDesc.get().constantType().descriptorString());
    }
  }

  private static void testSystemProperties() {
    System.setProperty("basalt.modern.test", "hello-world");
    System.out.println("getProperty set=" + System.getProperty("basalt.modern.test"));
    String removed = System.clearProperty("basalt.modern.test");
    System.out.println("clearProperty removed=" + removed);
    System.out.println("getProperty after clear=" + System.getProperty("basalt.modern.test"));
    System.out.println("System.console=" + System.console());
  }

  private static void testSystemRedirection() {
    PrintStream originalOut = System.out;
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    PrintStream customOut = new PrintStream(baos);
    System.setOut(customOut);
    System.out.print("redirection-success");
    System.setOut(originalOut);
    System.out.println("captured=" + baos.toString());
  }

  static class TestLoader extends ClassLoader {
    static boolean register() {
      return registerAsParallelCapable();
    }
  }

  private static void testClassLoaderModern() {
    ClassLoader platform = ClassLoader.getPlatformClassLoader();
    System.out.println("platformLoader exists=" + (platform != null));

    ClassLoader system = ClassLoader.getSystemClassLoader();
    System.out.println("systemLoader exists=" + (system != null));
    System.out.println("registerAsParallelCapable=" + TestLoader.register());
    System.out.println("isRegisteredAsParallelCapable=" + system.isRegisteredAsParallelCapable());
    System.out.println("definedPackages exists=" + (system.getDefinedPackages() != null));

    system.setDefaultAssertionStatus(true);
    system.setPackageAssertionStatus("org.basaltmc", true);
    system.setClassAssertionStatus("org.basaltmc.Test", true);
    system.clearAssertionStatus();
    System.out.println("assertion status calls completed");
  }
}
