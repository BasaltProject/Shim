/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package java.lang;

public class EnumConstantNotPresentException extends RuntimeException {
  private static final long serialVersionUID = -6046998521960521108L;

  private Class<? extends Enum> enumType;
  private String constantName;

  public EnumConstantNotPresentException(Class<? extends Enum> enumType, String constantName) {
    super(enumType.getName() + "." + constantName);
    this.enumType = enumType;
    this.constantName = constantName;
  }

  public Class<? extends Enum> enumType() {
    return enumType;
  }

  public String constantName() {
    return constantName;
  }
}
