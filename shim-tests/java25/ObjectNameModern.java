/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 * Minimum release: 21
 */

import java.util.*;
import javax.management.*;

public class ObjectNameModern {

static class NamesA {
  static void p(String s) {
    try {
      ObjectName n = new ObjectName(s);
      System.out.println("[" + s.replace("\n", "\\n") + "] -> " + n + " | " + n.getCanonicalName() + " | " + n.getDomain() + " | " + new TreeMap<>(n.getKeyPropertyList()) + " | "
        + n.getKeyPropertyListString() + " | " + n.getCanonicalKeyPropertyListString() + " | " + n.isPattern() + n.isDomainPattern() + n.isPropertyPattern() + n.isPropertyListPattern() + n.isPropertyValuePattern() + " | hash " + n.hashCode());
    } catch (Throwable e) {
      System.out.println("[" + s.replace("\n", "\\n") + "] !" + e.getClass().getName() + ":" + (e instanceof NullPointerException ? null : e.getMessage()));
    }
  }
  public static void run() throws Exception {
    String[] names = {"", "a:b=c", "a:b=c,d=e", "a:d=e,b=c", "a:b=c,*", "a:*", "a:*,b=c", "*:*", "a*:b=c", "a?c:b=c", "a:b=*", "a:b=c*", "a:b=?", "a:b=\"x,y\"", "a:b=\"x\\\"y\"", "a:b=\"x*y\"", "a:b=\"x?y\"",
      "a:b=\"x\\*y\"", "a:b=\"unterminated", "a:b=\"x\"y", "a", "a:", ":b=c", ":", "a:b", "a:b=", "a:=c", "a:b=c,", "a:b=c,,d=e", "a:b=c,b=d", "a:b=c,*,*", "a:b=c,*,d=e", "a:b:c=d", "a:b=c:d", "a:b c=d", "a:b=c d", " a:b=c", "a\n:b=c", "a:b\n=c", "a:b=c\n",
      "a:b=\"c\nd\"", "a:b=\"c\\nd\"", "a:b=\"c\\d\"", "a:b=\"\"", "a:b=\"\",c=d", "a:b=\"x\",c=\"y\"", "a:b==c", "a:b,c=d", "a:b=c,d", "a:*=c", "a:b*=c", "a:b?=c", "a:b,=c", "a=b:c=d", "a:b=\"x\"*", "a:b=c\"d\"", "a:b=\"c\"\"d\"", "a:\"b\"=c", "a:b=c,\"d\"=e", "java.lang:type=Memory", "java.lang:type=GarbageCollector,name=G1 Young Generation", "a:b=c;d=e", "a:b=\\", "a:b=\"\\\"", "a:b=\"x\\\\y\"", "a:b=\"x\\", "a:b=\"x\\\"", "*", "?", "a:b=c,?", "a:b=*,c=d*", "a:*b=c", "a:b=c*,*"};
    for (String s : names) p(s);
    for (String s : new String[] {"a b", "a,b", "a=b", "a*b", "\"abc\"", "a\"b", "a\nb", "", "a\\b", "x\"y\"z", "\\"}) {
      try { System.out.println("quote[" + s.replace("\n","\\n") + "] -> " + ObjectName.quote(s)); } catch (Throwable e) { System.out.println("quote !" + e); }
    }
    for (String s : new String[] {"\"a b\"", "\"a\\\"b\"", "\"a\\nb\"", "\"a\\*b\"", "\"a\"b\"", "ab", "\"ab", "ab\"", "\"a\\\"", "\"\"", "\"a\\xb\"", "\"a\\\\b\""}) {
      try { System.out.println("unquote[" + s + "] -> " + ObjectName.unquote(s).replace("\n","\\n")); } catch (Throwable e) { System.out.println("unquote[" + s + "] !" + e.getClass().getName() + ":" + (e instanceof NullPointerException ? null : e.getMessage())); }
    }
    Hashtable<String,String> h = new Hashtable<>(); h.put("k","v"); h.put("a","b");
    System.out.println(new ObjectName("d", h) + " " + new ObjectName("d", "k", "v"));
    try { new ObjectName("d", new Hashtable<String,String>()); } catch (Throwable e) { System.out.println(e instanceof NullPointerException ? e.getClass().getName() : e.toString()); }
    try { new ObjectName(null, "k", "v"); } catch (Throwable e) { System.out.println(e instanceof NullPointerException ? e.getClass().getName() : e.toString()); }
    try { new ObjectName("d", null, "v"); } catch (Throwable e) { System.out.println(e instanceof NullPointerException ? e.getClass().getName() : e.toString()); }
    try { new ObjectName("d", "k", null); } catch (Throwable e) { System.out.println(e instanceof NullPointerException ? e.getClass().getName() : e.toString()); }
    try { new ObjectName((String) null); } catch (Throwable e) { System.out.println(e instanceof NullPointerException ? e.getClass().getName() : e.toString()); }
    try { new ObjectName("d", (Hashtable<String,String>) null); } catch (Throwable e) { System.out.println(e instanceof NullPointerException ? e.getClass().getName() : e.toString()); }
    try { Hashtable<String,String> hh = new Hashtable<>(); hh.put("a b", "v"); new ObjectName("d", hh); } catch (Throwable e) { System.out.println(e instanceof NullPointerException ? e.getClass().getName() : e.toString()); }
    try { Hashtable<String,String> hh = new Hashtable<>(); hh.put("k", "a,b"); new ObjectName("d", hh); } catch (Throwable e) { System.out.println(e instanceof NullPointerException ? e.getClass().getName() : e.toString()); }
    try { new ObjectName("d", "k", "a=b"); System.out.println("ok k=a=b"); } catch (Throwable e) { System.out.println(e instanceof NullPointerException ? e.getClass().getName() : e.toString()); }
    try { new ObjectName("d:x", "k", "v"); } catch (Throwable e) { System.out.println(e instanceof NullPointerException ? e.getClass().getName() : e.toString()); }
    try { new ObjectName("d*", "k", "v"); System.out.println("ok pattern domain"); } catch (Throwable e) { System.out.println(e instanceof NullPointerException ? e.getClass().getName() : e.toString()); }
    ObjectName n1 = new ObjectName("a:b=c,d=e"), n2 = new ObjectName("a:d=e,b=c"), n3 = new ObjectName("a:*"), n4 = new ObjectName("*:*"), n5 = new ObjectName("a:b=*,*"), n6 = new ObjectName("a:b=c,d=e,f=g");
    System.out.println(n1.equals(n2) + " " + (n1.hashCode() == n2.hashCode()) + " " + n1.compareTo(n2) + " " + n1.compareTo(n3) + " " + n3.compareTo(n1) + " " + n1.compareTo(n6) + " " + n6.compareTo(n1) + " " + new ObjectName("b:x=y").compareTo(n1));
    for (ObjectName pat : new ObjectName[] {n3, n4, n5, new ObjectName("a:b=c,*"), new ObjectName("a:d=e,*"), new ObjectName("a:b=c"), new ObjectName("?:*"), new ObjectName("a*:*"), new ObjectName("a:b=c,d=e,f=g,*"), new ObjectName("a:b=c,d=e,*")})
      System.out.println(pat + " applies to n1=" + pat.apply(n1) + " n6=" + pat.apply(n6) + " " + pat.apply(new ObjectName("x:b=c")));
    System.out.println(n1.getKeyProperty("b") + " " + n1.getKeyProperty("zz") + " " + new ObjectName("a:b=\"x y\"").getKeyProperty("b"));
    try { n1.getKeyProperty(null); } catch (Throwable e) { System.out.println(e instanceof NullPointerException ? e.getClass().getName() : e.toString()); }
    System.out.println(n5.isPropertyValuePattern("b") + " " + n5.isPropertyValuePattern("zz".toString().isEmpty() ? "b" : "b"));
    try { n5.isPropertyValuePattern("zz"); } catch (Throwable e) { System.out.println(e instanceof NullPointerException ? e.getClass().getName() : e.toString()); }
    System.out.println(ObjectName.WILDCARD + " " + ObjectName.getInstance("a:b=c") + " " + ObjectName.getInstance(n1).equals(n1) + " " + ObjectName.getInstance("d", "k", "v"));
    try { n1.apply(null); } catch (Throwable e) { System.out.println(e instanceof NullPointerException ? e.getClass().getName() : e.toString()); }
    try { n1.compareTo(null); } catch (Throwable e) { System.out.println(e instanceof NullPointerException ? e.getClass().getName() : e.toString()); }
    System.out.println(n1.equals("a:b=c,d=e") + " " + n1.equals(null));
  }
}

static class NamesB {
  public static void run() throws Exception {
    for (String s : new String[] {"a:b=\"c\"\"d\"=e", "a:b=\"c\"d=e", "a:b=\"c\"=e", "a:b=\"c\"\"d\"", "a:b=\"c\" ,d=e", "a:b=\"c\",*", "a:b=\"c\"*", "a:b=\"c\",", "a:b=\"c\",*,*", "a:*,*", "a:,*", "a:b=c,*,", "a:b=c*,d=\"e*\"", "a:b=\"\\n\"", "a:b=\"x\\", "a:b=c,*x", "a:b=c,*=d", "a:*\n", "a:b=c,\n=d", "a:\n=d", "::", "a::b=c", "a:b=:", "a:=", "a:,", "a:==", "a:b=c\"", "a:b=\"c\"x", "a:b=\"\"\"", "a:\"b=c", "a:\"b\"x=c", "a:b=c,\"d=e\"=f", "a:b=\"c\\\"=d", "*:b=c", "a:b=c,d=e,", "a:b=*c*,d=?", "a:b=\"*\"", "a:b=\"\\*\"", "a:?b=c", "a:b?c=d", "aa:*,b=c,*", "a*:*", "\n:a=b", "a:b=\"c\"\n", "a:b=\"c\"\",d=e", "a: b=c", "a:b =c", "é:é=é", "a:b=c\t", "a:b=\tc"}) {
      try { ObjectName n = new ObjectName(s); System.out.println("[" + s.replace("\n","\\n") + "] -> " + n + " | " + n.getCanonicalName() + " | " + n.isPattern() + n.isDomainPattern() + n.isPropertyPattern() + n.isPropertyListPattern() + n.isPropertyValuePattern()); }
      catch (Throwable e) { System.out.println("[" + s.replace("\n","\\n") + "] !" + e.getClass().getSimpleName() + ":" + (e instanceof NullPointerException ? null : e.getMessage())); }
    }
  }
}

static class NamesC {
  static void c(String d, String k, String v) {
    try { ObjectName n = new ObjectName(d, k, v); System.out.println("(" + d + "," + k + "," + v + ") -> " + n + " " + n.isPattern() + n.isPropertyValuePattern()); }
    catch (Throwable e) { System.out.println("(" + d + "," + k + "," + v + ") !" + e.getClass().getSimpleName() + ":" + (e instanceof NullPointerException ? null : e.getMessage())); }
  }
  static void s(String x) {
    try { new ObjectName(x); System.out.println("[" + x + "] ok"); } catch (Throwable e) { System.out.println("[" + x + "] !" + (e instanceof NullPointerException ? null : e.getMessage())); }
  }
  static void cmp(String x, String y) throws Exception { System.out.println(x + " vs " + y + " = " + new ObjectName(x).compareTo(new ObjectName(y))); }
  public static void run() throws Exception {
    s("a=b"); s("ab"); s("a=b,c=d"); s("a*"); s("=a"); s("a:b=c,*,"); s("a:*,");
    c("d","k,","v"); c("d","k=","v"); c("d","k:","v"); c("d","k*","v"); c("d","k?","v"); c("d","k\n","v"); c("d","","v"); c("d","k",""); c("d","k","a:b"); c("d","k","a\"b"); c("d","k","\"a\"b\""); c("d","k","\"a\\xb\""); c("d","k","a\nb"); c("d","k","*"); c("d","k","\"x"); c("d", "k", "a*b"); c("d\n", "k", "v"); c("", "k", "v"); c("d:", "k", "v"); c("d=", "k", "v"); c("d,", "k", "v"); c("d?", "k", "v"); c("d","k","a,b"); c("*","k","v");
    cmp("a:type=x,k=1","a:type=y,k=0"); cmp("a:type=y,k=1","a:type=x,k=0"); cmp("a:type=x,k=1","a:k=1"); cmp("a:k=1","a:type=x,k=1"); cmp("a:k=1","a:k=2"); cmp("a:k=1,*","a:k=1"); cmp("a:type=x","a:type=x,k=1"); cmp("a:type=x,k=1","a:type=x,j=2"); cmp("a:type=\"x\"","a:type=x"); cmp("a:type=x","a:type=x"); cmp("b:k=1","a:type=x"); cmp("a:t=1","a:u=0");
    Hashtable<String,String> h = new Hashtable<>(); h.put("k","v");
    for (String[] kv : new String[][] {{"a,b","v"},{"a=b","v"},{"a:b","v"},{"a*b","v"},{"","v"},{"a","\"x"},{"a","x\"y"},{"a","x:y"},{"a","x\ny"}}) {
      Hashtable<String,String> hh = new Hashtable<>(); hh.put(kv[0], kv[1]);
      try { System.out.println("h(" + kv[0] + "," + kv[1].replace("\n","\\n") + ") -> " + new ObjectName("d", hh)); } catch (Throwable e) { System.out.println("h(" + kv[0] + "," + kv[1].replace("\n","\\n") + ") !" + e.getClass().getSimpleName() + ":" + (e instanceof NullPointerException ? null : e.getMessage())); }
    }
    ObjectName n = new ObjectName("a:b=c");
    System.out.println(n.getKeyProperty("b") + " " + Objects.toString(new ObjectName("*:*").getKeyPropertyListString()));
    try { new ObjectName("a:b=c").isPropertyValuePattern(null); } catch (Throwable e) { System.out.println(e instanceof NullPointerException ? e.getClass().getName() : e.toString()); }
    try { ObjectName.getInstance((String) null); } catch (Throwable e) { System.out.println(e instanceof NullPointerException ? e.getClass().getName() : e.toString()); }
    try { ObjectName.getInstance((ObjectName) null); } catch (Throwable e) { System.out.println(e instanceof NullPointerException ? e.getClass().getName() : e.toString()); }
    try { ObjectName.quote(null); } catch (Throwable e) { System.out.println(e instanceof NullPointerException ? e.getClass().getName() : e.toString()); }
    try { ObjectName.unquote(null); } catch (Throwable e) { System.out.println(e instanceof NullPointerException ? e.getClass().getName() : e.toString()); }
    System.out.println(new ObjectName("a:b=\"x\\ny\"").getKeyProperty("b") + " " + new ObjectName("a:*").getKeyPropertyListString().isEmpty());
    ObjectName m = new ObjectName("a:b=\"x*\"");
    System.out.println(m.apply(new ObjectName("a:b=\"xyz\"")) + " " + m.apply(new ObjectName("a:b=xyz")) + " " + new ObjectName("a:b=x*").apply(new ObjectName("a:b=\"xyz\"")) + " " + new ObjectName("a:b=\"x\\*\"").apply(new ObjectName("a:b=\"x*\"")) + " " + new ObjectName("a:b=*").apply(new ObjectName("a:b=\"q\"")));
    System.out.println(new ObjectName("a:b=c,d=e,*").apply(new ObjectName("a:*")) + " " + new ObjectName("a:*").apply(new ObjectName("a:*")) + " " + new ObjectName("a:b=c").apply(new ObjectName("a:b=c")));
  }
}

  public static void main(String[] args) throws Exception {
    java.util.Locale.setDefault(java.util.Locale.ENGLISH);
    System.out.println("##### NamesA");
    NamesA.run();
    System.out.println("##### NamesB");
    NamesB.run();
    System.out.println("##### NamesC");
    NamesC.run();
  }
}
