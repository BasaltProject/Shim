/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */


import java.io.InputStream;
import java.lang.invoke.MethodHandles;

public class HiddenClassIsolation {
  public static class Payload {
    public static int count = 7;
    public static Object self() { return Payload.class; }
    public static Object make() { return new Payload(); }
    public static int increment() { return ++count; }
    public static java.util.function.IntSupplier supplier() { return Payload::increment; }
  }

  public static class DataPayload {
    public static Object value;
    static {
      try {
        value = MethodHandles.classData(MethodHandles.lookup(), "_", Object.class);
      } catch (IllegalAccessException e) {
        throw new ExceptionInInitializerError(e);
      }
    }
  }

  private static void require(boolean value) {
    if (!value) throw new AssertionError();
  }

  public static void main(String[] args) throws Throwable {
    Class<?> ordinary = Payload.class;
    byte[] bytes;
    try (InputStream in = HiddenClassIsolation.class.getResourceAsStream(
        "HiddenClassIsolation$Payload.class")) {
      bytes = in.readAllBytes();
    }
    MethodHandles.Lookup lookup = MethodHandles.lookup();
    Class<?> first = lookup.defineHiddenClass(bytes, true).lookupClass();
    Class<?> second = lookup.defineHiddenClass(bytes, true).lookupClass();
    require(first != second && first != ordinary);
    require(first.isHidden() && second.isHidden() && !ordinary.isHidden());
    require(!first.getName().equals(second.getName()));
    require(first.getName().contains("/"));
    require(first.getCanonicalName() == null);
    require(!first.describeConstable().isPresent());
    require(first.getNestHost() == first);
    require(first.descriptorString().equals("L" + first.getName().replace('.', '/').replace("/0x", ".0x") + ";"));
    Class<?> array = java.lang.reflect.Array.newInstance(first, 0).getClass();
    require(array.getComponentType() == first && !array.isHidden());
    require(array.getCanonicalName() == null && !array.describeConstable().isPresent());
    require(array.getName().equals("[L" + first.getName() + ";"));
    require(array.descriptorString().equals("[" + first.descriptorString()));
    require(java.lang.reflect.Array.newInstance(first, 0).getClass() == array);
    System.gc();
    require(first.getMethod("self").invoke(null) == first);
    require(first.getMethod("make").invoke(null).getClass() == first);
    require(first.getMethod("increment").invoke(null).equals(8));
    require(second.getMethod("increment").invoke(null).equals(8));
    java.util.function.IntSupplier supplier =
        (java.util.function.IntSupplier) first.getMethod("supplier").invoke(null);
    require(supplier.getAsInt() == 9);
    require(Payload.count == 7);
    require(Class.forName(ordinary.getName()) == ordinary);
    try {
      Class.forName(first.getName());
      throw new AssertionError("hidden class found by name");
    } catch (ClassNotFoundException expected) {
      System.out.println("hidden name rejected");
    }
    Class<?> nested = lookup.defineHiddenClass(bytes, false,
        MethodHandles.Lookup.ClassOption.NESTMATE).lookupClass();
    require(nested.getNestHost() == HiddenClassIsolation.class);
    try {
      lookup.defineHiddenClass(bytes, false, new MethodHandles.Lookup.ClassOption[] { null });
      throw new AssertionError("null option accepted");
    } catch (NullPointerException expected) {
      System.out.println("null option rejected");
    }
    Class<?> another = lookup.defineHiddenClass(bytes, false).defineHiddenClass(bytes, false,
        MethodHandles.Lookup.ClassOption.NESTMATE).lookupClass();
    require(another.getNestHost().isHidden());
    try {
      lookup.defineHiddenClassWithClassData(bytes, null, false);
      throw new AssertionError("null class data accepted");
    } catch (NullPointerException expected) {
      System.out.println("null data rejected");
    }
    byte[] dataBytes;
    try (InputStream in = HiddenClassIsolation.class.getResourceAsStream(
        "HiddenClassIsolation$DataPayload.class")) {
      dataBytes = in.readAllBytes();
    }
    Object token = new Object();
    MethodHandles.Lookup dataLookup = lookup.defineHiddenClassWithClassData(dataBytes, token, true);
    System.gc();
    require(dataLookup.lookupClass().getField("value").get(null) == token);
    try {
      MethodHandles.classData(dataLookup, "wrong", Object.class);
      throw new AssertionError("invalid data name accepted");
    } catch (IllegalArgumentException expected) {
      System.out.println("data name rejected");
    }
    try {
      MethodHandles.classData(dataLookup.dropLookupMode(MethodHandles.Lookup.ORIGINAL), "_", Object.class);
      throw new AssertionError("non-original lookup accepted");
    } catch (IllegalAccessException expected) {
      System.out.println("data access restricted");
    }
    try {
      MethodHandles.classData(MethodHandles.privateLookupIn(dataLookup.lookupClass(), lookup), "_", Object.class);
      throw new AssertionError("delegated class data access accepted");
    } catch (IllegalAccessException expected) {
      System.out.println("delegated data access rejected");
    }
    try {
      MethodHandles.privateLookupIn(Payload.class, MethodHandles.publicLookup());
      throw new AssertionError("public lookup gained private access");
    } catch (IllegalAccessException expected) {
      System.out.println("private access restricted");
    }
    require(lookup.dropLookupMode(MethodHandles.Lookup.PUBLIC).lookupModes() == 0);
    require((lookup.dropLookupMode(MethodHandles.Lookup.PROTECTED).lookupModes()
        & MethodHandles.Lookup.ORIGINAL) == 0);
    try {
      lookup.dropLookupMode(0);
      throw new AssertionError("invalid lookup mode accepted");
    } catch (IllegalArgumentException expected) {
      System.out.println("lookup mode validated");
    }
    System.out.println("hidden identity isolated");
  }
}
