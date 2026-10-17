/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.lang.module;

public class InvalidModuleDescriptorException extends RuntimeException {
  private static final long serialVersionUID = 4863390386809347380L;

  public InvalidModuleDescriptorException() {
  }

  public InvalidModuleDescriptorException(String msg) {
    super(msg);
  }
}
