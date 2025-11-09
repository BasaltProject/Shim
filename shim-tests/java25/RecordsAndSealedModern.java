/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */


import java.lang.reflect.Method;
import java.lang.reflect.RecordComponent;
import java.lang.reflect.Type;
import java.util.List;

public class RecordsAndSealedModern {

  public record Point(int x, int y) {}

  public record Empty() {}

  public record ItemHolder<T>(T item, List<T> list) {}

  public sealed interface Shape permits Circle, Rectangle {}

  public static final class Circle implements Shape {}

  public static non-sealed class Rectangle implements Shape {}

  private static void assertEquals(Object expected, Object actual, String message) {
    if (expected == null && actual == null) {
      return;
    }
    if (expected != null && expected.equals(actual)) {
      return;
    }
    throw new AssertionError(message + " expected: " + expected + ", actual: " + actual);
  }

  private static void assertTrue(boolean condition, String message) {
    if (!condition) {
      throw new AssertionError(message);
    }
  }

  private static void testRecords() throws Exception {
    assertTrue(Point.class.isRecord(), "Point should be a record");
    assertTrue(Empty.class.isRecord(), "Empty should be a record");
    assertTrue(ItemHolder.class.isRecord(), "ItemHolder should be a record");

    assertTrue(!Object.class.isRecord(), "Object is not a record");
    assertTrue(!String.class.isRecord(), "String is not a record");
    assertTrue(!Record.class.isRecord(), "Record itself is not a record");

    assertEquals(null, Object.class.getRecordComponents(), "Object components must be null");
    assertEquals(null, String.class.getRecordComponents(), "String components must be null");
    assertEquals(null, Record.class.getRecordComponents(), "Record components must be null");

    RecordComponent[] emptyComps = Empty.class.getRecordComponents();
    assertTrue(emptyComps != null, "Empty record components non-null");
    assertEquals(0, emptyComps.length, "Empty record component count");

    RecordComponent[] pointComps = Point.class.getRecordComponents();
    assertTrue(pointComps != null, "Point components non-null");
    assertEquals(2, pointComps.length, "Point component count");

    RecordComponent xComp = pointComps[0];
    assertEquals("x", xComp.getName(), "x component name");
    assertEquals(int.class, xComp.getType(), "x component type");
    assertEquals(Point.class, xComp.getDeclaringRecord(), "x declaring record");
    assertEquals("int x", xComp.toString(), "x toString");
    assertEquals(int.class, xComp.getGenericType(), "x generic type");
    assertEquals(int.class, xComp.getAnnotatedType().getType(), "x annotated type");

    Method xAccessor = xComp.getAccessor();
    assertTrue(xAccessor != null, "x accessor exists");
    assertEquals("x", xAccessor.getName(), "x accessor name");

    Point p = new Point(10, 20);
    assertEquals(10, xAccessor.invoke(p), "x accessor invoke");

    RecordComponent yComp = pointComps[1];
    assertEquals("y", yComp.getName(), "y component name");
    assertEquals(int.class, yComp.getType(), "y component type");
    Method yAccessor = yComp.getAccessor();
    assertEquals(20, yAccessor.invoke(p), "y accessor invoke");

    RecordComponent[] holderComps = ItemHolder.class.getRecordComponents();
    assertEquals(2, holderComps.length, "ItemHolder component count");
    assertEquals("item", holderComps[0].getName(), "item name");
    assertEquals("list", holderComps[1].getName(), "list name");
    assertEquals(List.class, holderComps[1].getType(), "list erased type");
    Type listGenType = holderComps[1].getGenericType();
    assertTrue(listGenType != null, "list generic type non-null");
  }

  private static void testSealed() {
    assertTrue(Shape.class.isSealed(), "Shape should be sealed");
    assertTrue(!Circle.class.isSealed(), "Circle is final, not sealed");
    assertTrue(!Rectangle.class.isSealed(), "Rectangle is non-sealed");
    assertTrue(!Object.class.isSealed(), "Object is not sealed");

    assertEquals(null, Circle.class.getPermittedSubclasses(), "Circle permitted must be null");
    assertEquals(null, Rectangle.class.getPermittedSubclasses(), "Rectangle permitted must be null");
    assertEquals(null, Object.class.getPermittedSubclasses(), "Object permitted must be null");

    Class<?>[] permitted = Shape.class.getPermittedSubclasses();
    assertTrue(permitted != null, "Shape permitted non-null");
    assertEquals(2, permitted.length, "Shape permitted length");

    boolean foundCircle = false;
    boolean foundRectangle = false;
    for (Class<?> c : permitted) {
      if (c == Circle.class) {
        foundCircle = true;
      }
      if (c == Rectangle.class) {
        foundRectangle = true;
      }
    }
    assertTrue(foundCircle, "Shape permits Circle");
    assertTrue(foundRectangle, "Shape permits Rectangle");
  }

  public static void main(String[] args) throws Exception {
    testRecords();
    testSealed();
    System.out.println("OK");
  }
}
