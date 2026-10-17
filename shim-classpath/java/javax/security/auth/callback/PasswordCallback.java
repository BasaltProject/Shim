/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.security.auth.callback;

public class PasswordCallback implements Callback, java.io.Serializable {
  private static final long serialVersionUID = 2267422647454909926L;

  private String prompt;
  private boolean echoOn;
  private char[] inputPassword;

  public PasswordCallback(String prompt, boolean echoOn) {
    if (prompt == null || prompt.isEmpty()) {
      throw new IllegalArgumentException();
    }
    this.prompt = prompt;
    this.echoOn = echoOn;
  }

  public String getPrompt() {
    return prompt;
  }

  public boolean isEchoOn() {
    return echoOn;
  }

  public void setPassword(char[] password) {
    this.inputPassword = (password == null ? null : password.clone());
  }

  public char[] getPassword() {
    return (inputPassword == null ? null : inputPassword.clone());
  }

  public void clearPassword() {
    if (inputPassword != null) {
      java.util.Arrays.fill(inputPassword, ' ');
    }
  }
}
