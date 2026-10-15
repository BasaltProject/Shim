/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.net;

public abstract class Authenticator {

  public static enum RequestorType {
    PROXY, SERVER
  }

  private static Authenticator theAuthenticator;

  public static synchronized void setDefault(Authenticator a) {
    theAuthenticator = a;
  }

  public static Authenticator getDefault() {
    return theAuthenticator;
  }

  protected PasswordAuthentication getPasswordAuthentication() {
    return null;
  }
}
