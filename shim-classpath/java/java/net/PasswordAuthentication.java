/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.net;

import java.util.Objects;

public final class PasswordAuthentication {

  private final String userName;
  private final char[] password;

  public PasswordAuthentication(String userName, char[] password) {
    this.userName = Objects.requireNonNull(userName, "userName");
    this.password = password != null ? password.clone() : new char[0];
  }

  public String getUserName() {
    return userName;
  }

  public char[] getPassword() {
    return password.clone();
  }
}
