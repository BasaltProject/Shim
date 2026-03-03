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

public interface JavaCompiler extends Tool, OptionChecker {
  CompilationTask getTask(Writer out, JavaFileManager fileManager,
      DiagnosticListener<? super JavaFileObject> diagnosticListener, Iterable<String> options,
      Iterable<String> classes, Iterable<? extends JavaFileObject> compilationUnits);

  StandardJavaFileManager getStandardFileManager(DiagnosticListener<? super JavaFileObject> diagnosticListener,
      Locale locale, Charset charset);

  interface CompilationTask extends java.util.concurrent.Callable<Boolean> {
    void addModules(Iterable<String> moduleNames);

    void setProcessors(Iterable<? extends Object> processors);

    void setLocale(Locale locale);

    @Override
    Boolean call();
  }
}
