/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 * Minimum release: 21
 */

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import javax.management.openmbean.ArrayType;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;
import javax.management.openmbean.TabularData;
import javax.management.openmbean.TabularDataSupport;
import javax.management.openmbean.TabularType;

public class OpenMBeanTypesModern {
  static void t(String name, Callable<Object> c) {
    try {
      System.out.println(name + " = " + c.call());
    } catch (Throwable e) {
      System.out.println(name + " ! " + e.getClass().getName() + (e instanceof NullPointerException || e instanceof ClassCastException ? "" : ": "
          + e.getMessage()));
    }
  }

  static CompositeType rowType() throws OpenDataException {
    return new CompositeType("row", "a row", new String[] {"id", "name", "score", "tags"},
        new String[] {"the id", "the name", "the score", "the tags"},
        new OpenType<?>[] {SimpleType.INTEGER, SimpleType.STRING, SimpleType.DOUBLE,
            new ArrayType<String[]>(1, SimpleType.STRING)});
  }

  static CompositeData row(int id, String name, double score) throws OpenDataException {
    Map<String, Object> items = new HashMap<>();
    items.put("id", id);
    items.put("name", name);
    items.put("score", score);
    items.put("tags", new String[] {"a", "b"});
    return new CompositeDataSupport(rowType(), items);
  }

  public static void main(String[] args) throws Exception {
    t("array-1", () -> new ArrayType<Integer[]>(1, SimpleType.INTEGER));
    t("array-3", () -> new ArrayType<Integer[][][]>(3, SimpleType.INTEGER));
    t("array-fields", () -> {
      ArrayType<?> a = new ArrayType<Object>(2, SimpleType.STRING);
      return a.getClassName() + "|" + a.getTypeName() + "|" + a.getDescription() + "|" + a.getDimension() + "|"
          + a.getElementOpenType() + "|" + a.isPrimitiveArray() + "|" + a.isArray();
    });
    t("array-zero", () -> new ArrayType<Object>(0, SimpleType.STRING));
    t("array-negative", () -> new ArrayType<Object>(-1, SimpleType.STRING));
    t("array-null-element", () -> new ArrayType<Object>(1, null));
    t("array-of-array", () -> {
      ArrayType<?> inner = new ArrayType<Object>(2, SimpleType.LONG);
      ArrayType<?> outer = new ArrayType<Object>(1, inner);
      return outer.getDimension() + " " + outer.getElementOpenType() + " " + outer.getClassName() + " "
          + outer.getTypeName() + " | " + outer.getDescription();
    });
    t("array-of-composite", () -> {
      ArrayType<?> a = new ArrayType<Object>(1, rowType());
      return a.getClassName() + " | " + a.getDescription();
    });
    t("array-primitive-int", () -> {
      ArrayType<int[]> a = new ArrayType<int[]>(SimpleType.INTEGER, true);
      return a + " " + a.getClassName() + " " + a.isPrimitiveArray() + " " + a.getDescription();
    });
    t("array-primitive-all", () -> {
      StringBuilder sb = new StringBuilder();
      for (SimpleType<?> st : new SimpleType<?>[] {SimpleType.BOOLEAN, SimpleType.CHARACTER, SimpleType.BYTE,
          SimpleType.SHORT, SimpleType.INTEGER, SimpleType.LONG, SimpleType.FLOAT, SimpleType.DOUBLE}) {
        sb.append(new ArrayType<Object>(st, true).getClassName()).append(' ');
      }
      return sb;
    });
    t("array-primitive-bad", () -> new ArrayType<Object>(SimpleType.STRING, true));
    t("array-primitive-void", () -> new ArrayType<Object>(SimpleType.VOID, true));
    t("array-nonprimitive-flag", () -> new ArrayType<Object>(SimpleType.STRING, false));
    t("array-static", () -> ArrayType.getArrayType(SimpleType.INTEGER));
    t("array-static-nested", () -> ArrayType.getArrayType(ArrayType.getArrayType(SimpleType.INTEGER)));
    t("array-static-primitive", () -> ArrayType.getPrimitiveArrayType(int[].class));
    t("array-static-primitive-bad", () -> ArrayType.getPrimitiveArrayType(Integer[].class));
    t("array-static-primitive-2d", () -> ArrayType.getPrimitiveArrayType(int[][].class));
    t("array-static-primitive-nonarray", () -> ArrayType.getPrimitiveArrayType(int.class));
    t("array-equals", () -> {
      ArrayType<?> a = new ArrayType<Object>(1, SimpleType.INTEGER);
      ArrayType<?> b = new ArrayType<Object>(1, SimpleType.INTEGER);
      ArrayType<?> c = new ArrayType<Object>(2, SimpleType.INTEGER);
      ArrayType<?> d = new ArrayType<Object>(SimpleType.INTEGER, true);
      return a.equals(b) + " " + (a.hashCode() == b.hashCode()) + " " + a.equals(c) + " " + a.equals(d) + " "
          + a.equals(null) + " " + a.equals("x");
    });
    t("array-isvalue", () -> {
      ArrayType<?> a = new ArrayType<Object>(1, SimpleType.INTEGER);
      return a.isValue(new Integer[] {1, 2}) + " " + a.isValue(new int[] {1}) + " " + a.isValue(new Long[] {1L}) + " "
          + a.isValue(null) + " " + a.isValue("x") + " " + a.isValue(new Integer[0]) + " "
          + a.isValue(new Integer[][] {{1}});
    });
    t("array-isvalue-primitive", () -> {
      ArrayType<?> a = new ArrayType<Object>(SimpleType.INTEGER, true);
      return a.isValue(new int[] {1}) + " " + a.isValue(new Integer[] {1}) + " " + a.isValue(new long[0]);
    });
    t("array-isvalue-2d", () -> {
      ArrayType<?> a = new ArrayType<Object>(2, SimpleType.STRING);
      return a.isValue(new String[][] {{"a"}}) + " " + a.isValue(new String[] {"a"}) + " "
          + a.isValue(new String[][][] {});
    });
    t("array-isvalue-composite", () -> {
      ArrayType<?> a = new ArrayType<Object>(1, rowType());
      return a.isValue(new CompositeData[] {row(1, "x", 1.0)}) + " " + a.isValue(new CompositeData[0]) + " "
          + a.isValue(new CompositeDataSupport[] {(CompositeDataSupport) row(2, "y", 2.0)});
    });
    t("array-tostring-composite", () -> new ArrayType<Object>(1, rowType()).toString().replaceAll("@[0-9a-f]+", "@X"));
    t("array-opentype-isarray", () -> new ArrayType<Object>(1, SimpleType.STRING).isArray() + " "
        + SimpleType.STRING.isArray());

    t("tabular-type", () -> new TabularType("table", "a table", rowType(), new String[] {"id"}));
    t("tabular-fields", () -> {
      TabularType tt = new TabularType("table", "a table", rowType(), new String[] {"name", "id"});
      return tt.getTypeName() + "|" + tt.getDescription() + "|" + tt.getClassName() + "|" + tt.getIndexNames() + "|"
          + tt.getRowType().getTypeName() + "|" + tt.isArray();
    });
    t("tabular-indexnames-immutable", () -> new TabularType("table", "a table", rowType(), new String[] {"id"})
        .getIndexNames().add("x"));
    t("tabular-null-name", () -> new TabularType(null, "d", rowType(), new String[] {"id"}));
    t("tabular-empty-name", () -> new TabularType("", "d", rowType(), new String[] {"id"}));
    t("tabular-null-desc", () -> new TabularType("n", null, rowType(), new String[] {"id"}));
    t("tabular-null-row", () -> new TabularType("n", "d", null, new String[] {"id"}));
    t("tabular-null-index", () -> new TabularType("n", "d", rowType(), null));
    t("tabular-empty-index", () -> new TabularType("n", "d", rowType(), new String[0]));
    t("tabular-null-index-element", () -> new TabularType("n", "d", rowType(), new String[] {null}));
    t("tabular-empty-index-element", () -> new TabularType("n", "d", rowType(), new String[] {""}));
    t("tabular-unknown-index", () -> new TabularType("n", "d", rowType(), new String[] {"nope"}));
    t("tabular-array-index", () -> new TabularType("n", "d", rowType(), new String[] {"tags"}));
    t("tabular-duplicate-index", () -> new TabularType("n", "d", rowType(), new String[] {"id", "id"}));
    t("tabular-equals", () -> {
      TabularType a = new TabularType("t", "d", rowType(), new String[] {"id"});
      TabularType b = new TabularType("t", "other", rowType(), new String[] {"id"});
      TabularType c = new TabularType("t", "d", rowType(), new String[] {"name"});
      TabularType d = new TabularType("u", "d", rowType(), new String[] {"id"});
      return a.equals(b) + " " + (a.hashCode() == b.hashCode()) + " " + a.equals(c) + " " + a.equals(d) + " "
          + a.equals(null);
    });
    t("tabular-tostring", () -> new TabularType("t", "d", rowType(), new String[] {"id", "name"}).toString()
        .replaceAll("@[0-9a-f]+", "@X"));
    t("tabular-isvalue", () -> {
      TabularType tt = new TabularType("t", "d", rowType(), new String[] {"id"});
      TabularDataSupport data = new TabularDataSupport(tt);
      TabularType other = new TabularType("t", "d", rowType(), new String[] {"name"});
      return tt.isValue(data) + " " + tt.isValue(new TabularDataSupport(other)) + " " + tt.isValue(null) + " "
          + tt.isValue("x");
    });

    TabularType tt = new TabularType("table", "a table", rowType(), new String[] {"id"});
    t("data-new", () -> {
      TabularDataSupport d = new TabularDataSupport(tt);
      return d.size() + " " + d.isEmpty() + " " + d.getTabularType().equals(tt);
    });
    t("data-null-type", () -> new TabularDataSupport(null));
    t("data-ctor-sizes", () -> new TabularDataSupport(tt, 4, 0.5f).size());
    t("data-ctor-bad-capacity", () -> new TabularDataSupport(tt, -1, 0.5f));
    t("data-ctor-bad-load", () -> new TabularDataSupport(tt, 4, -1f));
    t("data-put-get", () -> {
      TabularDataSupport d = new TabularDataSupport(tt);
      d.put(row(1, "one", 1.5));
      d.put(row(2, "two", 2.5));
      CompositeData found = d.get(new Object[] {2});
      return d.size() + " " + found.get("name") + " " + d.containsKey(new Object[] {1}) + " "
          + d.containsKey(new Object[] {3}) + " " + d.containsValue(row(1, "one", 1.5)) + " "
          + d.containsValue(row(9, "nine", 0)) + " " + d.get(new Object[] {3});
    });
    t("data-calculate-index", () -> {
      TabularType two = new TabularType("t", "d", rowType(), new String[] {"name", "id"});
      TabularDataSupport d = new TabularDataSupport(two);
      return Arrays.toString(d.calculateIndex(row(7, "seven", 1)));
    });
    t("data-calculate-index-bad", () -> new TabularDataSupport(tt).calculateIndex(null));
    t("data-put-twice", () -> {
      TabularDataSupport d = new TabularDataSupport(tt);
      d.put(row(1, "one", 1.5));
      d.put(row(1, "uno", 1.5));
      return "no error";
    });
    t("data-put-wrong-type", () -> {
      CompositeType other = new CompositeType("other", "o", new String[] {"id"}, new String[] {"id"},
          new OpenType<?>[] {SimpleType.INTEGER});
      Map<String, Object> items = new HashMap<>();
      items.put("id", 1);
      new TabularDataSupport(tt).put(new CompositeDataSupport(other, items));
      return "no error";
    });
    t("data-put-null", () -> {
      new TabularDataSupport(tt).put((CompositeData) null);
      return "no error";
    });
    t("data-remove", () -> {
      TabularDataSupport d = new TabularDataSupport(tt);
      d.put(row(1, "one", 1.5));
      CompositeData removed = d.remove(new Object[] {1});
      return removed.get("name") + " " + d.size() + " " + d.remove(new Object[] {1});
    });
    t("data-remove-bad-key", () -> new TabularDataSupport(tt).remove(new Object[] {"x"}));
    t("data-remove-wrong-length", () -> new TabularDataSupport(tt).remove(new Object[] {1, 2}));
    t("data-remove-null", () -> new TabularDataSupport(tt).remove((Object[]) null));
    t("data-get-bad-key", () -> new TabularDataSupport(tt).get(new Object[] {"x"}));
    t("data-get-null", () -> new TabularDataSupport(tt).get((Object[]) null));
    t("data-contains-bad", () -> new TabularDataSupport(tt).containsKey(new Object[] {"x"}));
    t("data-contains-null", () -> new TabularDataSupport(tt).containsKey((Object[]) null));
    t("data-contains-value-null", () -> new TabularDataSupport(tt).containsValue((CompositeData) null));
    t("data-putall-array", () -> {
      TabularDataSupport d = new TabularDataSupport(tt);
      d.putAll(new CompositeData[] {row(1, "a", 1), row(2, "b", 2), row(3, "c", 3)});
      return d.size();
    });
    t("data-putall-array-null", () -> {
      TabularDataSupport d = new TabularDataSupport(tt);
      d.putAll((CompositeData[]) null);
      return d.size();
    });
    t("data-putall-array-empty", () -> {
      TabularDataSupport d = new TabularDataSupport(tt);
      d.putAll(new CompositeData[0]);
      return d.size();
    });
    t("data-putall-array-dup", () -> {
      TabularDataSupport d = new TabularDataSupport(tt);
      d.putAll(new CompositeData[] {row(1, "a", 1), row(1, "b", 2)});
      return d.size();
    });
    t("data-putall-array-partial", () -> {
      TabularDataSupport d = new TabularDataSupport(tt);
      try {
        d.putAll(new CompositeData[] {row(1, "a", 1), null});
      } catch (RuntimeException e) {
        return d.size() + " " + e.getClass().getName();
      }
      return d.size();
    });
    t("data-putall-map", () -> {
      TabularDataSupport d = new TabularDataSupport(tt);
      Map<Object, Object> m = new HashMap<>();
      m.put(Arrays.asList(1), row(1, "a", 1));
      m.put(Arrays.asList(2), row(2, "b", 2));
      d.putAll(m);
      return d.size();
    });
    t("data-putall-map-null", () -> {
      TabularDataSupport d = new TabularDataSupport(tt);
      d.putAll((Map<?, ?>) null);
      return d.size();
    });
    t("data-putall-map-bad-value", () -> {
      TabularDataSupport d = new TabularDataSupport(tt);
      Map<Object, Object> m = new HashMap<>();
      m.put(Arrays.asList(1), "not composite");
      d.putAll(m);
      return d.size();
    });
    t("data-map-put", () -> {
      TabularDataSupport d = new TabularDataSupport(tt);
      d.put((Object) Arrays.asList(5), (Object) row(5, "five", 5));
      return d.size() + " " + d.keySet();
    });
    t("data-map-put-wrong-key", () -> {
      TabularDataSupport d = new TabularDataSupport(tt);
      d.put((Object) Arrays.asList(99), (Object) row(5, "five", 5));
      return d.size();
    });
    t("data-map-put-bad-value", () -> {
      TabularDataSupport d = new TabularDataSupport(tt);
      d.put((Object) Arrays.asList(5), (Object) "x");
      return d.size();
    });
    t("data-map-get", () -> {
      TabularDataSupport d = new TabularDataSupport(tt);
      d.put(row(5, "five", 5));
      Object byList = d.get((Object) Arrays.asList(5));
      Object byArray = d.get((Object) new Object[] {5});
      return ((CompositeData) byList).get("name") + " " + byArray;
    });
    t("data-map-get-bad", () -> new TabularDataSupport(tt).get((Object) "x"));
    t("data-map-contains", () -> {
      TabularDataSupport d = new TabularDataSupport(tt);
      d.put(row(5, "five", 5));
      return d.containsKey((Object) Arrays.asList(5)) + " " + d.containsKey((Object) Arrays.asList(6)) + " "
          + d.containsValue((Object) row(5, "five", 5)) + " " + d.containsValue((Object) "x");
    });
    t("data-map-contains-bad-key", () -> new TabularDataSupport(tt).containsKey((Object) "x"));
    t("data-map-remove", () -> {
      TabularDataSupport d = new TabularDataSupport(tt);
      d.put(row(5, "five", 5));
      Object removed = d.remove((Object) Arrays.asList(5));
      return (removed != null) + " " + d.size();
    });
    t("data-views", () -> {
      TabularDataSupport d = new TabularDataSupport(tt);
      d.put(row(1, "a", 1));
      d.put(row(2, "b", 2));
      Set<Object> keys = d.keySet();
      Collection<Object> values = d.values();
      Set<Map.Entry<Object, Object>> entries = d.entrySet();
      return keys + " " + values.size() + " " + entries.size() + " " + (keys.iterator().next() instanceof List)
          + " " + new TreeMap<>(Map.of("n", d.size()));
    });
    t("data-clone", () -> {
      TabularDataSupport d = new TabularDataSupport(tt);
      d.put(row(1, "a", 1));
      TabularDataSupport c = (TabularDataSupport) d.clone();
      c.put(row(2, "b", 2));
      return d.size() + " " + c.size() + " " + d.equals(c) + " " + d.equals(d.clone());
    });
    t("data-equals", () -> {
      TabularDataSupport a = new TabularDataSupport(tt);
      TabularDataSupport b = new TabularDataSupport(tt);
      a.put(row(1, "a", 1));
      b.put(row(1, "a", 1));
      TabularDataSupport c = new TabularDataSupport(tt);
      c.put(row(1, "z", 1));
      return a.equals(b) + " " + (a.hashCode() == b.hashCode()) + " " + a.equals(c) + " " + a.equals(null) + " "
          + a.equals("x") + " " + a.equals(new HashMap<Object, Object>());
    });
    t("data-clear", () -> {
      TabularDataSupport d = new TabularDataSupport(tt);
      d.put(row(1, "a", 1));
      d.clear();
      return d.size() + " " + d.isEmpty();
    });
    t("data-tostring", () -> {
      TabularDataSupport d = new TabularDataSupport(tt);
      d.put(row(1, "a", 1));
      return d.toString().replaceAll("@[0-9a-f]+", "@X");
    });
    t("data-keys-order", () -> {
      TabularDataSupport d = new TabularDataSupport(tt);
      for (int i = 5; i >= 1; i--) {
        d.put(row(i, "n" + i, i));
      }
      return d.keySet();
    });
    t("data-two-index", () -> {
      TabularType two = new TabularType("t", "d", rowType(), new String[] {"name", "id"});
      TabularDataSupport d = new TabularDataSupport(two);
      d.put(row(1, "a", 1));
      d.put(row(1, "b", 1));
      return d.size() + " " + d.containsKey(new Object[] {"a", 1}) + " " + d.get(new Object[] {"b", 1}).get("name")
          + " " + d.keySet();
    });
    t("data-key-too-short", () -> {
      TabularType two = new TabularType("t", "d", rowType(), new String[] {"name", "id"});
      return new TabularDataSupport(two).containsKey(new Object[] {"a"});
    });
    t("data-tabulardata-iface", () -> {
      TabularData d = new TabularDataSupport(tt);
      d.put(row(3, "c", 3));
      return d.size() + " " + d.getTabularType().getRowType().getTypeName() + " "
          + Arrays.toString(d.calculateIndex(row(3, "c", 3)));
    });
  }
}
