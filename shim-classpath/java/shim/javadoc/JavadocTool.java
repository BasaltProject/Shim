/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.javadoc;

import com.sun.source.util.DocTrees;
import com.sun.tools.javac.api.JavacTool;
import java.io.PrintWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.*;
import jdk.javadoc.doclet.Doclet;
import jdk.javadoc.doclet.DocletEnvironment;
import jdk.javadoc.doclet.Reporter;

/** Runs a doclet over sources attributed by the in-process compiler. */
public final class JavadocTool implements DocumentationTool {
  public String name() {
    return "javadoc";
  }

  public Set<SourceVersion> getSourceVersions() {
    return java.util.EnumSet.range(SourceVersion.RELEASE_8, SourceVersion.latest());
  }

  public int isSupportedOption(String option) {
    switch (option) {
      case "-d":
      case "-doclet":
      case "-docletpath":
      case "-sourcepath":
      case "-classpath":
      case "-cp":
        return 1;
      case "-public":
      case "-protected":
      case "-package":
      case "-private":
      case "-quiet":
        return 0;
      default:
        return -1;
    }
  }

  public int run(java.io.InputStream in, java.io.OutputStream out, java.io.OutputStream err, String... args) {
    return 2;
  }

  public StandardJavaFileManager getStandardFileManager(DiagnosticListener<? super JavaFileObject> dl, Locale locale,
      Charset charset) {
    return new JavacTool().getStandardFileManager(dl, locale, charset);
  }

  public DocumentationTask getTask(final Writer out, final JavaFileManager fm,
      final DiagnosticListener<? super JavaFileObject> dl, final Class<?> docletClass,
      final Iterable<String> options, final Iterable<? extends JavaFileObject> units) {
    return new DocumentationTask() {
      public void addModules(Iterable<String> moduleNames) {
      }

      public void setLocale(Locale locale) {
      }

      public Boolean call() {
        return new Run(out, fm, dl, docletClass, options, units).run();
      }
    };
  }

  static final class Run {
    final Writer out;
    final JavaFileManager fm;
    final DiagnosticListener<? super JavaFileObject> dl;
    final Class<?> docletClass;
    final List<String> options = new ArrayList<String>();
    final List<JavaFileObject> units = new ArrayList<JavaFileObject>();
    String access = "protected";

    Run(Writer out, JavaFileManager fm, DiagnosticListener<? super JavaFileObject> dl, Class<?> docletClass,
        Iterable<String> options, Iterable<? extends JavaFileObject> units) {
      this.out = out;
      this.fm = fm;
      this.dl = dl;
      this.docletClass = docletClass;
      if (options != null) {
        for (String o : options) {
          this.options.add(o);
        }
      }
      if (units != null) {
        for (JavaFileObject u : units) {
          this.units.add(u);
        }
      }
    }

    Boolean run() {
      final PrintWriter pw = out == null ? new PrintWriter(System.err, true)
          : out instanceof PrintWriter ? (PrintWriter) out : new PrintWriter(out, true);
      try {
        Doclet doclet = docletClass == null ? new jdk.javadoc.doclet.StandardDoclet()
            : (Doclet) docletClass.getDeclaredConstructor().newInstance();
        List<String> compilerOpts = new ArrayList<String>();
        List<String[]> docletOpts = new ArrayList<String[]>();
        Set<? extends Doclet.Option> supported = doclet.getSupportedOptions();
        for (int i = 0; i < options.size(); i++) {
          String o = options.get(i);
          if (o.equals("-public") || o.equals("-protected") || o.equals("-package") || o.equals("-private")) {
            access = o.substring(1);
            continue;
          }
          Doclet.Option match = null;
          for (Doclet.Option opt : supported) {
            if (opt.getNames().contains(o)) {
              match = opt;
            }
          }
          if (match != null) {
            int n = match.getArgumentCount();
            List<String> args = new ArrayList<String>();
            for (int k = 0; k < n && i + 1 < options.size(); k++) {
              args.add(options.get(++i));
            }
            match.process(o, args);
            continue;
          }
          if (o.equals("-d") && i + 1 < options.size()) {
            docletOpts.add(new String[] {o, options.get(++i)});
            continue;
          }
          if (o.equals("-sourcepath") || o.equals("-classpath") || o.equals("-cp")) {
            compilerOpts.add(o);
            if (i + 1 < options.size()) {
              compilerOpts.add(options.get(++i));
            }
          }
        }
        PrintWriter std = out == null ? new PrintWriter(System.out, true) : pw;
        for (JavaFileObject u : units) {
          std.println("Loading source file " + u.getName() + "...");
        }
        std.println("Constructing Javadoc information...");
        std.flush();
        JavacTool tool = new JavacTool();
        com.sun.source.util.JavacTask task = tool.getTask(pw, fm, dl, compilerOpts, null, units);
        task.parse();
        task.analyze();
        final DiagnosticListener<? super JavaFileObject> d = dl;
        Reporter reporter = new Reporter() {
          public void print(Diagnostic.Kind kind, String msg) {
            pw.println(kind + ": " + msg);
          }

          public void print(Diagnostic.Kind kind, Element e, String msg) {
            pw.println(kind + ": " + e + ": " + msg);
          }

          public void print(Diagnostic.Kind kind, com.sun.source.util.DocTreePath p, String msg) {
            pw.println(kind + ": " + msg);
          }

          public PrintWriter getStandardWriter() {
            return pw;
          }

          public PrintWriter getDiagnosticWriter() {
            return pw;
          }
        };
        doclet.init(Locale.getDefault(), reporter);
        Env env = new Env(task, access);
        return Boolean.valueOf(doclet.run(env));
      } catch (Exception e) {
        pw.println("error: " + e);
        return Boolean.FALSE;
      }
    }
  }

  static final class Env implements DocletEnvironment {
    final com.sun.source.util.JavacTask task;
    final String access;
    final Set<Element> specified = new LinkedHashSet<Element>();
    final Set<Element> included = new LinkedHashSet<Element>();

    Env(com.sun.source.util.JavacTask task, String access) throws java.io.IOException {
      this.task = task;
      this.access = access;
      for (Element e : task.analyze()) {
        addSpecified(e);
      }
      for (Element e : new java.util.ArrayList<Element>(specified)) {
        if (isIncluded(e)) {
          PackageElement pkg = task.getElements().getPackageOf(e);
          if (pkg != null) {
            included.add(pkg);
          }
        }
      }
      for (Element e : specified) {
        if (isIncluded(e)) {
          included.add(e);
        }
      }
    }

    private void addSpecified(Element e) {
      specified.add(e);
      for (Element m : e.getEnclosedElements()) {
        if (m instanceof TypeElement && isIncluded(m)) {
          addSpecified(m);
        }
      }
    }

    public Set<? extends Element> getSpecifiedElements() {
      return specified;
    }

    public Set<? extends Element> getIncludedElements() {
      return included;
    }

    public DocTrees getDocTrees() {
      return DocTrees.instance(task);
    }

    public Elements getElementUtils() {
      return task.getElements();
    }

    public Types getTypeUtils() {
      return task.getTypes();
    }

    public boolean isIncluded(Element e) {
      if (e instanceof PackageElement) {
        return true;
      }
      Set<Modifier> m = e.getModifiers();
      boolean ok;
      if (access.equals("private")) {
        ok = true;
      } else if (access.equals("package")) {
        ok = !m.contains(Modifier.PRIVATE);
      } else if (access.equals("public")) {
        ok = m.contains(Modifier.PUBLIC) || inInterface(e) && !m.contains(Modifier.PRIVATE);
      } else {
        ok = m.contains(Modifier.PUBLIC) || m.contains(Modifier.PROTECTED) || inInterface(e)
            && !m.contains(Modifier.PRIVATE);
      }
      Element enc = e.getEnclosingElement();
      if (ok && enc != null && !(enc instanceof PackageElement)) {
        return isIncluded(enc);
      }
      return ok;
    }

    private boolean inInterface(Element e) {
      Element enc = e.getEnclosingElement();
      return enc != null && (enc.getKind() == ElementKind.INTERFACE || enc.getKind() == ElementKind.ANNOTATION_TYPE);
    }

    public boolean isSelected(Element e) {
      return isIncluded(e);
    }

    public JavaFileManager getJavaFileManager() {
      return null;
    }

    public SourceVersion getSourceVersion() {
      return SourceVersion.latest();
    }

    public ModuleMode getModuleMode() {
      return ModuleMode.API;
    }

    public JavaFileObject.Kind getFileKind(TypeElement type) {
      return JavaFileObject.Kind.SOURCE;
    }
  }
}
