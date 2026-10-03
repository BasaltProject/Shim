/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package jdk.dynalink.beans;

@FunctionalInterface
public interface MissingMemberHandlerFactory {
  java.lang.invoke.MethodHandle createMissingMemberHandler(jdk.dynalink.linker.LinkRequest linkRequest, jdk.dynalink.linker.LinkerServices linkerServices) throws java.lang.Exception;
}
