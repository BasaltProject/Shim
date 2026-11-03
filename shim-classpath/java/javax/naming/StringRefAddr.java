/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.naming;

public class StringRefAddr extends RefAddr {
  private static final long serialVersionUID = -8913762495138505527L;

  private String contents;

  public StringRefAddr(String addrType, String addr) {
    super(addrType);
    contents = addr;
  }

  public Object getContent() {
    return contents;
  }
}
