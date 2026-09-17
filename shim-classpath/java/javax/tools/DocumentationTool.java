/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.tools;

import java.io.Writer;
import java.nio.charset.Charset;
import java.util.Locale;
import java.util.concurrent.Callable;

public interface DocumentationTool extends Tool, OptionChecker {
  DocumentationTask getTask(Writer out, JavaFileManager fileManager,
      DiagnosticListener<? super JavaFileObject> diagnosticListener, Class<?> docletClass, Iterable<String> options,
      Iterable<? extends JavaFileObject> compilationUnits);

  StandardJavaFileManager getStandardFileManager(DiagnosticListener<? super JavaFileObject> diagnosticListener,
      Locale locale, Charset charset);

  interface DocumentationTask extends Callable<Boolean> {
    void addModules(Iterable<String> moduleNames);

    void setLocale(Locale locale);

    Boolean call();
  }

  enum Location implements JavaFileManager.Location {
    DOCUMENTATION_OUTPUT,
    DOCLET_PATH,
    TAGLET_PATH,
    SNIPPET_PATH;

    public String getName() {
      return name();
    }

    public boolean isOutputLocation() {
      switch (this) {
        case DOCUMENTATION_OUTPUT:
          return true;
        default:
          return false;
      }
    }
  }
}
