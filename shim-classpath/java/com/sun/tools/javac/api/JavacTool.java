/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.tools.javac.api;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import javax.lang.model.SourceVersion;
import javax.tools.DiagnosticListener;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import shim.javac.FileManagerImpl;
import shim.javac.JavacTaskImpl;

public final class JavacTool implements JavaCompiler {
  public JavacTool() {
  }

  public static JavacTool create() {
    return new JavacTool();
  }

  public String name() {
    return "javac";
  }

  public JavacTaskImpl getTask(Writer out, JavaFileManager fileManager,
      DiagnosticListener<? super JavaFileObject> diagnosticListener, Iterable<String> options,
      Iterable<String> classes, Iterable<? extends JavaFileObject> compilationUnits) {
    List<String> opts = new ArrayList<String>();
    if (options != null) {
      for (String o : options) {
        opts.add(java.util.Objects.requireNonNull(o));
      }
    }
    List<String> cls = new ArrayList<String>();
    if (classes != null) {
      for (String c : classes) {
        if (!SourceVersion.isName(c)) {
          throw new IllegalArgumentException("Not a valid class name: " + c);
        }
        cls.add(c);
      }
    }
    List<JavaFileObject> units = new ArrayList<JavaFileObject>();
    if (compilationUnits != null) {
      for (JavaFileObject u : compilationUnits) {
        if (u.getKind() != JavaFileObject.Kind.SOURCE) {
          throw new IllegalArgumentException("Compilation unit is not of SOURCE kind: \"" + u.getName() + "\"");
        }
        units.add(u);
      }
    }
    JavaFileManager fm = fileManager != null ? fileManager : getStandardFileManager(diagnosticListener, null, null);
    JavacTaskImpl task = new JavacTaskImpl(this, out, fm, diagnosticListener, opts, cls, units);
    task.validateOptions();
    return task;
  }

  public StandardJavaFileManager getStandardFileManager(DiagnosticListener<? super JavaFileObject> dl,
      Locale locale, Charset charset) {
    return new FileManagerImpl(dl, locale, charset);
  }

  public int run(InputStream in, OutputStream out, OutputStream err, String... arguments) {
    PrintWriter ew = new PrintWriter(err == null ? System.err : err, true);
    if (arguments == null) {
      throw new NullPointerException();
    }
    List<String> options = new ArrayList<String>();
    List<String> files = new ArrayList<String>();
    Iterator<String> it = java.util.Arrays.asList(arguments).iterator();
    while (it.hasNext()) {
      String a = it.next();
      if (a.startsWith("-")) {
        options.add(a);
        int n = isSupportedOption(a);
        for (int k = 0; k < n && it.hasNext(); k++) {
          options.add(it.next());
        }
      } else {
        files.add(a);
      }
    }
    if (files.isEmpty()) {
      ew.println("error: no source files");
      ew.flush();
      return 2;
    }
    try {
      StandardJavaFileManager fm = getStandardFileManager(null, null, null);
      JavacTaskImpl task = getTask(ew, fm, null, options, null, fm.getJavaFileObjectsFromStrings(files));
      boolean ok = task.call();
      ew.flush();
      return ok ? 0 : 1;
    } catch (IllegalArgumentException e) {
      ew.println(e.getMessage());
      ew.flush();
      return 2;
    }
  }

  public Set<SourceVersion> getSourceVersions() {
    return EnumSet.range(SourceVersion.RELEASE_3, SourceVersion.latest());
  }

  public int isSupportedOption(String option) {
    switch (option) {
      case "-d":
      case "-cp":
      case "-classpath":
      case "--class-path":
      case "-sourcepath":
      case "--source-path":
      case "-s":
      case "-h":
      case "-encoding":
      case "-source":
      case "--source":
      case "-target":
      case "--target":
      case "--release":
      case "-processor":
      case "-processorpath":
      case "--processor-path":
      case "--module-path":
      case "-p":
      case "--add-modules":
      case "--module-source-path":
        return 1;
      case "-g":
      case "-nowarn":
      case "-verbose":
      case "-deprecation":
      case "-parameters":
      case "-proc:none":
      case "-proc:only":
      case "-proc:full":
      case "--enable-preview":
      case "-Werror":
      case "-implicit:none":
      case "-implicit:class":
      case "-g:none":
      case "-Xdiags:compact":
      case "-Xdiags:verbose":
        return 0;
      default:
        if (option.startsWith("-Xlint") || option.startsWith("-A") || option.startsWith("-XD")
            || option.startsWith("-g:") || option.startsWith("-Xmaxerrs") && false) {
          return 0;
        }
        return -1;
    }
  }
}
