/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.javac;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.JavacTask;
import com.sun.source.util.TaskEvent;
import com.sun.source.util.TaskListener;
import com.sun.tools.javac.api.JavacTool;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import javax.lang.model.element.Element;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticListener;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;

public final class JavacTaskImpl extends JavacTask {
  private final JavacTool tool;
  private final Writer out;
  private final JavaFileManager fileManager;
  private final DiagnosticListener<? super JavaFileObject> listener;
  private final List<String> options;
  private final List<String> classes;
  private final List<JavaFileObject> units;
  private final List<TaskListener> taskListeners = new ArrayList<TaskListener>();
  private final List<N.CompilationUnit> parsed = new ArrayList<N.CompilationUnit>();
  private final java.util.Map<N.CompilationUnit, LineMapImpl> lineMaps =
      new java.util.IdentityHashMap<N.CompilationUnit, LineMapImpl>();
  private boolean parseDone;
  private boolean used;
  private int errorCount;
  private int warningCount;
  private Locale locale = Locale.getDefault();
  private Backend backend;
  private Pipeline pipeline;
  private ModelUtils.ElementsImpl elements;
  private ModelUtils.TypesImpl types;

  public JavacTaskImpl(JavacTool tool, Writer out, JavaFileManager fm,
      DiagnosticListener<? super JavaFileObject> listener, List<String> options, List<String> classes,
      List<JavaFileObject> units) {
    this.tool = tool;
    this.out = out;
    this.fileManager = fm;
    this.listener = listener;
    this.options = options;
    this.classes = classes;
    this.units = units;
  }

  public void validateOptions() {
    java.util.Iterator<String> it = options.iterator();
    while (it.hasNext()) {
      String o = it.next();
      int n = tool.isSupportedOption(o);
      if (n < 0) {
        if (fileManager instanceof FileManagerImpl && ((FileManagerImpl) fileManager).isSupportedOption(o) >= 0) {
          n = ((FileManagerImpl) fileManager).isSupportedOption(o);
        } else {
          throw new IllegalArgumentException("error: invalid flag: " + o);
        }
      }
      for (int k = 0; k < n; k++) {
        if (!it.hasNext()) {
          throw new IllegalArgumentException("error: " + o + " requires an argument");
        }
        it.next();
      }
      if (n >= 0 && (o.equals("-d") || o.equals("-cp") || o.equals("-classpath") || o.equals("--class-path")
          || o.equals("-sourcepath") || o.equals("--source-path") || o.equals("-s"))) {
        java.util.List<String> single = new ArrayList<String>();
        int idx = options.indexOf(o);
        if (idx >= 0 && idx + 1 < options.size()) {
          single.add(o);
          single.add(options.get(idx + 1));
          fileManager.handleOption(o, single.subList(1, 2).iterator());
        }
      }
    }
  }

  public List<String> options() {
    return options;
  }

  public List<JavaFileObject> sources() {
    return units;
  }

  public JavaFileManager fileManager() {
    return fileManager;
  }

  public String option(String name) {
    int i = options.indexOf(name);
    return i >= 0 && i + 1 < options.size() ? options.get(i + 1) : null;
  }

  public boolean hasOption(String name) {
    return options.contains(name);
  }

  public List<N.CompilationUnit> units() {
    return parsed;
  }

  public LineMapImpl lineMapOf(CompilationUnitTree cu) {
    return lineMaps.get(cu);
  }

  public void addModules(Iterable<String> moduleNames) {
    checkNotUsed();
  }

  public void setProcessors(Iterable<? extends javax.annotation.processing.Processor> processors) {
    checkNotUsed();
  }

  public void setLocale(Locale locale) {
    checkNotUsed();
    this.locale = locale;
  }

  private void checkNotUsed() {
    if (used) {
      throw new IllegalStateException();
    }
  }

  public void addTaskListener(TaskListener l) {
    taskListeners.add(l);
  }

  public void removeTaskListener(TaskListener l) {
    taskListeners.remove(l);
  }

  public void setTaskListener(TaskListener l) {
    taskListeners.clear();
    if (l != null) {
      taskListeners.add(l);
    }
  }

  void fire(boolean started, TaskEvent e) {
    for (TaskListener l : new ArrayList<TaskListener>(taskListeners)) {
      if (started) {
        l.started(e);
      } else {
        l.finished(e);
      }
    }
  }

  public Iterable<? extends CompilationUnitTree> parse() throws IOException {
    if (parseDone) {
      throw new IllegalStateException("multiple calls to method 'parse'");
    }
    parseDone = true;
    used = true;
    for (JavaFileObject f : units) {
      fire(true, new TaskEvent(TaskEvent.Kind.PARSE, f));
      CharSequence cs = f.getCharContent(true);
      String src = cs == null ? "" : cs.toString();
      Parser p = new Parser(src, f.getName());
      N.CompilationUnit cu = p.parseCompilationUnit();
      cu.sourceFile = f;
      LineMapImpl lm = new LineMapImpl(src);
      cu.lineMap = lm;
      lineMaps.put(cu, lm);
      for (Parser.Diag d : p.diags) {
        report(Diagnostic.Kind.ERROR, f, lm, d.pos, d.pos, d.pos, d.code, d.message);
      }
      parsed.add(cu);
      fire(false, new TaskEvent(TaskEvent.Kind.PARSE, cu));
    }
    return Collections.unmodifiableList(new ArrayList<N.CompilationUnit>(parsed));
  }

  public void report(Diagnostic.Kind kind, JavaFileObject file, LineMapImpl lm, long pos, long start, long end,
      String code, String message) {
    DiagnosticImpl d = new DiagnosticImpl(kind, file, lm, pos, start, end, code, message);
    if (kind == Diagnostic.Kind.ERROR) {
      errorCount++;
    } else if (kind == Diagnostic.Kind.WARNING || kind == Diagnostic.Kind.MANDATORY_WARNING) {
      warningCount++;
    }
    if (listener != null) {
      listener.report(d);
    } else {
      PrintWriter w = writer();
      w.println(d.toString());
      w.flush();
    }
  }

  private PrintWriter writer() {
    if (out == null) {
      return new PrintWriter(System.err, true);
    }
    return out instanceof PrintWriter ? (PrintWriter) out : new PrintWriter(out, true);
  }

  public int errorCount() {
    return errorCount;
  }

  Pipeline pipeline() throws IOException {
    if (pipeline == null) {
      if (!parseDone) {
        parse();
      }
      used = true;
      ClassLoader cl;
      try {
        cl = fileManager.getClassLoader(javax.tools.StandardLocation.CLASS_PATH);
      } catch (RuntimeException e) {
        cl = null;
      }
      final ClassLoader parent = cl != null ? new java.net.URLClassLoader(new java.net.URL[0], cl)
          : ClassLoader.getSystemClassLoader();
      pipeline = new Pipeline(parsed, parent, new Log.Sink() {
        public void error(CompilationUnitTree cu, Tree at, String code, String message) {
          N.CompilationUnit n = (N.CompilationUnit) cu;
          long pos = at instanceof N.Base ? ((N.Base) at).pos : -1;
          long start = at instanceof N.Base ? ((N.Base) at).start : -1;
          long end = at instanceof N.Base ? ((N.Base) at).end : -1;
          report(Diagnostic.Kind.ERROR, n.sourceFile, lineMaps.get(n), pos, start, end, code, message);
        }
      });
    }
    return pipeline;
  }

  public Pipeline attributed() throws IOException {
    Pipeline p = pipeline();
    if (errorCount == 0 || true) {
      p.attribute();
    }
    return p;
  }

  public Iterable<? extends Element> analyze() throws IOException {
    Pipeline p = attributed();
    List<Element> out = new ArrayList<Element>();
    for (CSym c : p.enter.sourceClasses) {
      if (c.outerClass == null && !c.local && !c.anon) {
        out.add(c);
      }
    }
    return out;
  }

  public Iterable<? extends JavaFileObject> generate() throws IOException {
    if (!parseDone) {
      parse();
    }
    used = true;
    List<JavaFileObject> written = new ArrayList<JavaFileObject>();
    if (errorCount > 0 || parsed.isEmpty()) {
      return written;
    }
    java.util.Map<String, byte[]> out = pipeline().generate();
    if (errorCount > 0) {
      return written;
    }
    for (java.util.Map.Entry<String, byte[]> e : out.entrySet()) {
      JavaFileObject src = parsed.isEmpty() ? null : parsed.get(0).sourceFile;
      JavaFileObject fo = fileManager.getJavaFileForOutput(javax.tools.StandardLocation.CLASS_OUTPUT,
          e.getKey().replace('/', '.'), JavaFileObject.Kind.CLASS, src);
      java.io.OutputStream os = fo.openOutputStream();
      try {
        os.write(e.getValue());
      } finally {
        os.close();
      }
      written.add(fo);
    }
    return written;
  }

  public Elements getElements() {
    try {
      if (elements == null) {
        elements = new ModelUtils.ElementsImpl(attributed(), this);
      }
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
    return elements;
  }

  public Types getTypes() {
    try {
      if (types == null) {
        types = new ModelUtils.TypesImpl(attributed());
      }
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
    return types;
  }

  public TypeMirror getTypeMirror(Iterable<? extends Tree> path) {
    Tree last = null;
    for (Tree t : path) {
      last = t;
    }
    try {
      return TreesImpl.typeOf(attributed(), last);
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
  }

  public Boolean call() {
    if (used) {
      throw new IllegalStateException("multiple calls to method 'call'");
    }
    try {
      parse();
      if (errorCount == 0) {
        generate();
      }
    } catch (IOException e) {
      PrintWriter w = writer();
      w.println("error: " + e.getMessage());
    }
    if (listener == null && errorCount > 0) {
      PrintWriter w = writer();
      w.println(errorCount + (errorCount == 1 ? " error" : " errors"));
      w.flush();
    }
    return errorCount == 0;
  }

  public interface Backend {
    void compile(JavacTaskImpl task) throws IOException;
  }
}
