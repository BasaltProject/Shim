/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.script;

import java.io.Reader;

public interface Compilable {
  CompiledScript compile(String script) throws ScriptException;

  CompiledScript compile(Reader script) throws ScriptException;
}
