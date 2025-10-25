/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.security.auth.callback;

public class TextInputCallback implements Callback, java.io.Serializable {
  private static final long serialVersionUID = -8064222478852811804L;

  private final String prompt;
  private String defaultText;
  private String inputText;

  public TextInputCallback(String prompt) {
    if (prompt == null || prompt.isEmpty()) {
      throw new IllegalArgumentException();
    }
    this.prompt = prompt;
  }

  public TextInputCallback(String prompt, String defaultText) {
    if (prompt == null || prompt.isEmpty() || defaultText == null || defaultText.isEmpty()) {
      throw new IllegalArgumentException();
    }
    this.prompt = prompt;
    this.defaultText = defaultText;
  }

  public String getPrompt() {
    return prompt;
  }

  public String getDefaultText() {
    return defaultText;
  }

  public void setText(String text) {
    this.inputText = text;
  }

  public String getText() {
    return inputText;
  }
}
