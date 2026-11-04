/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package jdk.javadoc.doclet;

import java.util.Collections;
import java.util.Locale;
import java.util.Set;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;

/** Minimal standard doclet: writes an index page listing the documented types. */
public class StandardDoclet implements Doclet {
  private Locale locale = Locale.getDefault();
  private Reporter reporter;
  private String destination;

  public StandardDoclet() {
  }

  public Locale getLocale() {
    return locale;
  }

  public Reporter getReporter() {
    return reporter;
  }

  public String getName() {
    return "Standard";
  }

  public SourceVersion getSupportedSourceVersion() {
    return SourceVersion.latest();
  }

  public void init(Locale locale, Reporter reporter) {
    this.locale = locale;
    this.reporter = reporter;
  }

  public Set<? extends Option> getSupportedOptions() {
    return Collections.<Option>singleton(new Option() {
      public int getArgumentCount() {
        return 1;
      }

      public String getDescription() {
        return "Destination directory for output files";
      }

      public Kind getKind() {
        return Kind.STANDARD;
      }

      public java.util.List<String> getNames() {
        return java.util.Arrays.asList("-d");
      }

      public String getParameters() {
        return "<directory>";
      }

      public boolean process(String option, java.util.List<String> arguments) {
        destination = arguments.get(0);
        return true;
      }
    });
  }

  public boolean run(DocletEnvironment env) {
    if (destination == null) {
      return true;
    }
    try {
      java.nio.file.Path dir = java.nio.file.Paths.get(destination);
      java.nio.file.Files.createDirectories(dir);
      StringBuilder sb = new StringBuilder("<html><body><h1>Types</h1><ul>\n");
      for (Element e : env.getIncludedElements()) {
        if (e instanceof TypeElement && e.getEnclosingElement().getKind() == ElementKind.PACKAGE) {
          sb.append("<li>").append(((TypeElement) e).getQualifiedName()).append("</li>\n");
        }
      }
      sb.append("</ul></body></html>\n");
      java.nio.file.Files.write(dir.resolve("index.html"), sb.toString().getBytes("UTF-8"));
      return true;
    } catch (java.io.IOException ex) {
      return false;
    }
  }
}
