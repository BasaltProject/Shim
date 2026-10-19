/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.script;

import java.util.List;

public interface ScriptEngineFactory {
  String getEngineName();

  String getEngineVersion();

  List<String> getExtensions();

  List<String> getMimeTypes();

  List<String> getNames();

  String getLanguageName();

  String getLanguageVersion();

  Object getParameter(String key);

  String getMethodCallSyntax(String obj, String m, String... args);

  String getOutputStatement(String toDisplay);

  String getProgram(String... statements);

  ScriptEngine getScriptEngine();
}
