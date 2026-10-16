/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

import java.lang.reflect.Field;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Arrays;

public class PublicFieldReflection {
  @Retention(RetentionPolicy.RUNTIME)
  public @interface Mark { int value(); }
  public interface AnnotatedRoot { @Mark(9) int annotated = 6; }
  public static class AnnotatedParent { @Mark(8) public int annotatedParent; }
  public static class AnnotatedChild extends AnnotatedParent implements AnnotatedRoot {
    @Mark(7) public int annotatedChild;
  }
  public static class DeclaredAnnotated { @Mark(5) public int field; }
  public interface Root { int root = 10; int shared = 11; }
  public interface Left extends Root { int order = 20; }
  public interface Right extends Root { int order = 30; }
  public interface Diamond extends Left, Right { }
  public interface Reverse extends Right, Left { }
  public static class Parent {
    public int inherited = 40;
    public int shared = 41;
    public static int order = 42;
    private int secret = 43;
    protected int protectedField = 44;
    int packageField = 45;
  }
  public static class Child extends Parent implements Diamond {
    public String shared = "child";
    private int inherited = 50;
    public long own = 51;
  }
  public static class ViaInterface extends Parent implements Reverse { }
  public static class Leaf extends Child { }
  public interface Empty { }
  private static int initializations;
  public static class Tracked {
    static { ++initializations; }
  }
  public static class Holder {
    public static Tracked[][] matrix;
    public static int number = 7;
    static { ++initializations; }
  }

  private static String identity(Field field) {
    return field.getDeclaringClass().getName() + ":" + field.getName() + ":"
      + field.getType().getName() + ":" + field.getModifiers();
  }

  private static void lookup(Class<?> type, String name, boolean declared) throws Exception {
    try {
      Field field = declared ? type.getDeclaredField(name) : type.getField(name);
      System.out.println(type.getName() + ":" + name + ":" + declared + ":" + identity(field));
    } catch (NoSuchFieldException expected) {
      System.out.println(type.getName() + ":" + name + ":" + declared + ":missing");
    }
  }

  public static void main(String[] args) throws Exception {
    for (Class<?> type : new Class<?>[] {Root.class, Diamond.class, Reverse.class, Parent.class,
        Child.class, ViaInterface.class, Leaf.class, Empty.class, Object.class, int.class,
        void.class, String[].class, int[][].class}) {
      for (boolean declared : new boolean[] {false, true}) {
        Field[] fields = declared ? type.getDeclaredFields() : type.getFields();
        String[] names = new String[fields.length];
        for (int i = 0; i < fields.length; ++i) names[i] = identity(fields[i]);
        Arrays.sort(names);
        System.out.println(type.getName() + ":" + declared + ":" + Arrays.toString(names));
        for (String name : new String[] {"root", "shared", "order", "inherited", "own", "secret",
            "protectedField", "packageField", "length", "missing"}) lookup(type, name, declared);
        try {
          if (declared) type.getDeclaredField(null); else type.getField(null);
          throw new AssertionError();
        } catch (NullPointerException expected) { System.out.println("null name rejected"); }
      }
    }
    Leaf leaf = new Leaf();
    Field inherited = Leaf.class.getField("inherited");
    System.out.println(inherited.getInt(leaf));
    inherited.setInt(leaf, 123);
    System.out.println(((Parent) leaf).inherited);
    System.out.println(Leaf.class.getField("shared").get(leaf));
    System.out.println(ViaInterface.class.getField("order").getInt(null));
    Field[] first = Leaf.class.getFields();
    first[0] = null;
    for (Field field : Leaf.class.getFields()) if (field == null) throw new AssertionError();
    System.out.println("independent arrays");
    System.out.println(DeclaredAnnotated.class.getDeclaredFields()[0].getAnnotation(Mark.class).value());
    Field[] annotated = AnnotatedChild.class.getFields();
    String[] annotations = new String[annotated.length];
    for (int i = 0; i < annotated.length; ++i) {
      annotations[i] = annotated[i].getName() + ":" + annotated[i].getAnnotation(Mark.class).value();
    }
    Arrays.sort(annotations);
    System.out.println(Arrays.toString(annotations));
    System.out.println(AnnotatedChild.class.getField("annotated").getAnnotation(Mark.class).value());
    System.out.println(Holder.class.getFields().length);
    System.out.println(Holder.class.getField("matrix").getType() == Tracked[][].class);
    System.out.println(initializations);
    System.out.println(Holder.class.getField("number").getInt(null));
    System.out.println(initializations);
  }
}
