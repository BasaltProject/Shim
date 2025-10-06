/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.security.auth.callback;

public class NameCallback implements Callback, java.io.Serializable {
  private static final long serialVersionUID = 3770938795909392253L;

  private String prompt;
  private String defaultName;
  private String inputName;

  public NameCallback(String prompt) {
    if (prompt == null || prompt.isEmpty()) {
      throw new IllegalArgumentException();
    }
    this.prompt = prompt;
  }

  public NameCallback(String prompt, String defaultName) {
    if (prompt == null || prompt.isEmpty() || defaultName == null || defaultName.isEmpty()) {
      throw new IllegalArgumentException();
    }
    this.prompt = prompt;
    this.defaultName = defaultName;
  }

  public String getPrompt() {
    return prompt;
  }

  public String getDefaultName() {
    return defaultName;
  }

  public void setName(String name) {
    this.inputName = name;
  }

  public String getName() {
    return inputName;
  }
}
