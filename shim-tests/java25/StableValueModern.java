/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 * Minimum release: 25
 * javac-args: --enable-preview --release 25
 * jvm-args: --enable-preview
 */

import java.util.*;
import java.util.concurrent.Callable;
import java.util.function.*;

public class StableValueModern {
  static void t(String n, Callable<Object> c) {
    try {
      Object r = c.call();
      System.out.println(n + " = " + r);
    } catch (Throwable e) {
      System.out.println(n + " ! " + e.getClass().getName() + ": " + e.getMessage());
    }
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  public static void main(String[] a) throws Exception {
    first();
    second();
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  static void first() throws Exception {
  StableValue<String> v=StableValue.of(); t("unset-str",()->v); t("isSet",()->v.isSet()); t("orElse",()->v.orElse("d")); t("orElseThrow",()->v.orElseThrow());
  t("trySet",()->v.trySet("a")); t("trySet2",()->v.trySet("b")); t("setOrThrow",()->{ v.setOrThrow("c"); return 1; }); t("str",()->v); t("orElseSet",()->v.orElseSet(()->"z")); t("eq",()->v.equals(v)+" "+v.equals(StableValue.of("a"))); t("hash",()->v.hashCode()==System.identityHashCode(v));
  t("of-v",()->StableValue.of("x")); t("of-null",()->StableValue.of(null)); t("of-null-isSet",()->StableValue.of(null).isSet());
  StableValue<String> w=StableValue.of(); t("orElseSet-null",()->w.orElseSet(()->null)); t("trySet-after-null",()->w.trySet("q"));
  StableValue<String> r=StableValue.of(); t("recursive",()->r.orElseSet(()->r.orElseSet(()->"x")));
  t("recursive-set",()->{ StableValue<String> q=StableValue.of(); return q.orElseSet(()->{ q.setOrThrow("in"); return "out"; }); });
  StableValue<String> th=StableValue.of(); t("throwing",()->th.orElseSet(()->{ throw new IllegalArgumentException("boom"); })); t("after-throw",()->th.isSet());
  t("self-str",()->{ StableValue<Object> s=StableValue.of(); s.trySet(s); return s; });
  t("npe-supplier",()->StableValue.of().orElseSet(null));
  Supplier<String> sup=StableValue.supplier(()->"sv"); t("sup-str",()->sup); t("sup-get",()->sup.get()); t("sup-str2",()->sup); t("sup-null",()->StableValue.supplier(null));
  t("sup-null-val",()->{ Supplier<String> n=StableValue.supplier(()->null); return n.get()+" "+n; });
  IntFunction<String> inf=StableValue.intFunction(3,i->"i"+i); t("if-str",()->inf); t("if-1",()->inf.apply(1)); t("if-str2",()->inf); t("if-oob",()->inf.apply(3)); t("if-neg",()->StableValue.intFunction(-1,i->"x")); t("if-zero",()->StableValue.intFunction(0,i->"x").apply(0));
  Function<String,Integer> fn=StableValue.function(Set.of("a","bb"),String::length); t("fn-1",()->fn.apply("bb")); t("fn-bad",()->fn.apply("zz"));   t("fn-empty",()->StableValue.function(Set.of(),x->1).apply("a"));
  List<String> l=StableValue.list(3,i->"e"+i); t("list-str",()->l); t("list-get",()->l.get(2)); t("list-str2",()->l); t("list-size",()->l.size()); t("list-oob",()->l.get(3)); t("list-add",()->l.add("x")); t("list-set",()->l.set(0,"x")); t("list-sub",()->l.subList(1,3)); t("list-idx",()->l.indexOf("e1")); t("list-contains",()->l.contains("e0")); t("list-eq",()->l.equals(List.of("e0","e1","e2"))); t("list-hash",()->l.hashCode()==List.of("e0","e1","e2").hashCode()); t("list-iter",()->{ StringBuilder sb=new StringBuilder(); for(String x:l) sb.append(x); return sb; }); t("list-neg",()->StableValue.list(-1,i->"x")); t("list-stream",()->l.stream().toList()); t("list-toArray",()->Arrays.toString(l.toArray()));
  Map<String,Integer> m=StableValue.map(Set.of("a","bb"),String::length); t("map-get",()->m.get("bb")); t("map-size",()->m.size()); t("map-missing",()->m.get("zz")); t("map-contains",()->m.containsKey("a")); t("map-put",()->m.put("c",1)); t("map-eq",()->m.equals(Map.of("a",1,"bb",2))); t("map-keys",()->new TreeSet<>(m.keySet())); t("map-entries",()->new TreeSet<>(m.entrySet().stream().map(Object::toString).toList())); t("map-str",()->new TreeMap<>(m));  t("map-getOrDefault",()->m.getOrDefault("q",9)); t("map-values",()->new TreeSet<>(m.values())); t("map-dup",()->StableValue.map(new HashSet<>(Arrays.asList("a")),x->null).get("a"));
  
   }

  @SuppressWarnings({"unchecked", "rawtypes"})
  static void second() throws Exception {
  Map<String,Integer> m=StableValue.map(Set.of("a"),String::length); t("map-str0",()->m); t("map-get",()->m.get("a")); t("map-str1",()->m); t("map-entries-str",()->m.entrySet()); t("map-values-str",()->m.values()); t("map-keys-str",()->m.keySet());
  t("map-forEach",()->{ StringBuilder sb=new StringBuilder(); m.forEach((k,v)->sb.append(k).append(v)); return sb; });
  Function<String,Integer> fn=StableValue.function(Set.of("a"),String::length); t("fn-str0",()->fn); t("fn-apply",()->fn.apply("a")); t("fn-str1",()->fn);
  t("fn-null-set",()->StableValue.function(null,x->1)); t("fn-null-fn",()->StableValue.function(Set.of("a"),null));
  t("list-null-fn",()->StableValue.list(2,null)); t("list-zero",()->StableValue.list(0,i->"x")); 
  t("map-empty",()->StableValue.map(Set.of(),x->1)); 
  t("sup-throw",()->{ int[] c={0}; Supplier<String> s=StableValue.supplier(()->{ if(c[0]++==0) throw new IllegalStateException("first"); return "ok"; }); try{ s.get(); }catch(Exception e){} return s.get()+c[0]+" "+s; });
  t("sup-recursive",()->{ Supplier<String>[] h=new Supplier[1]; h[0]=StableValue.supplier(()->h[0].get()); return h[0].get(); });
  t("list-throw",()->{ List<String> l=StableValue.list(2,i->{ throw new RuntimeException("e"+i); }); try{ l.get(0);}catch(Exception e){} return l; });
  t("list-iter-order",()->{ StringBuilder sb=new StringBuilder(); List<String> l=StableValue.list(3,i->{ sb.append(i); return "v"+i; }); l.toString(); l.hashCode(); l.contains("v2"); return sb; });
  t("list-toStr-forces",()->{ StringBuilder sb=new StringBuilder(); List<String> l=StableValue.list(2,i->{ sb.append(i); return "v"+i; }); l.toString(); return sb+" "+l; });
  
  t("list-reversed",()->{ List<String> l=StableValue.list(3,i->"e"+i); return l.reversed(); });
  t("list-eq-other",()->{ List<String> l=StableValue.list(2,i->"e"+i); return List.of("e0","e1").equals(l); });
  t("val-concurrent",()->{ StableValue<Integer> v=StableValue.of(); int[] c={0}; Thread[] ts=new Thread[8]; for(int i=0;i<8;i++){ ts[i]=new Thread(()->v.orElseSet(()->{ c[0]++; try{ Thread.sleep(20);}catch(Exception e){} return 7; })); ts[i].start(); } for(Thread x:ts) x.join(); return v.orElseThrow()+" "+c[0]; });
   }
}
