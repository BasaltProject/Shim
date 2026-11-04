/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

import javax.tools.*; import java.io.*; import java.net.*; import java.nio.file.*; import java.util.*;
public class JavacCompileBasics { static class Src extends SimpleJavaFileObject { final String code; Src(String n, String c) { super(URI.create("string:///" + n.replace('.', '/') + ".java"), Kind.SOURCE); code = c; } public CharSequence getCharContent(boolean b) { return code; } }
  public static void main(String[] a) throws Exception { Path out = Files.createTempDirectory("jc2"); JavaCompiler jc = ToolProvider.getSystemJavaCompiler(); DiagnosticCollector<JavaFileObject> dc = new DiagnosticCollector<>();
    String code = "package q; public class Hi { static int sq(int x) { return x * x; } public static String hello(String n) { String s = \"Hello \"; for (int i = 0; i < 2; i++) s += n; return s + sq(7); } }";
    boolean ok = jc.getTask(null, null, dc, List.of("-d", out.toString()), null, List.of(new Src("q.Hi", code))).call(); System.out.println("ok=" + ok + " diags=" + dc.getDiagnostics());
    try (URLClassLoader cl = new URLClassLoader(new URL[] {out.toUri().toURL()})) { Class<?> k = cl.loadClass("q.Hi"); System.out.println(k.getMethod("hello", String.class).invoke(null, "Bob")); }
    DiagnosticCollector<JavaFileObject> d2 = new DiagnosticCollector<>(); boolean ok2 = jc.getTask(null, null, d2, List.of("-d", out.toString()), null, List.of(new Src("q.Bad", "package q; public class Bad { int f() { return y; } }"))).call(); System.out.println("ok2=" + ok2); for (var d : d2.getDiagnostics()) System.out.println(d.getKind() + " " + d.getLineNumber() + " " + d.getMessage(null).split("\n")[0]); } }
