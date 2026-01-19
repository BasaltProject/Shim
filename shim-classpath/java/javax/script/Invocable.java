/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.script;

public interface Invocable {
  Object invokeMethod(Object thiz, String name, Object... args) throws ScriptException, NoSuchMethodException;

  Object invokeFunction(String name, Object... args) throws ScriptException, NoSuchMethodException;

  <T> T getInterface(Class<T> clasz);

  <T> T getInterface(Object thiz, Class<T> clasz);
}
