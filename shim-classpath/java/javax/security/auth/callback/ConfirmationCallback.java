/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.security.auth.callback;

public class ConfirmationCallback implements Callback, java.io.Serializable {
  private static final long serialVersionUID = -9095656433782481624L;

  public static final int UNSPECIFIED_OPTION = -1;
  public static final int YES_NO_OPTION = 0;
  public static final int YES_NO_CANCEL_OPTION = 1;
  public static final int OK_CANCEL_OPTION = 2;
  public static final int YES = 0;
  public static final int NO = 1;
  public static final int CANCEL = 2;
  public static final int OK = 3;
  public static final int INFORMATION = 0;
  public static final int WARNING = 1;
  public static final int ERROR = 2;

  private String prompt;
  private final int messageType;
  private int optionType = -1;
  private int defaultOption;
  private String[] options;
  private int selection;

  public ConfirmationCallback(int messageType, int optionType, int defaultOption) {
    if (messageType < INFORMATION || messageType > ERROR) {
      throw new IllegalArgumentException("Invalid msgType");
    }
    if (optionType < YES_NO_OPTION || optionType > OK_CANCEL_OPTION) {
      throw new IllegalArgumentException("Invalid optionType");
    }
    boolean valid;
    switch (optionType) {
      case YES_NO_OPTION:
        valid = defaultOption == YES || defaultOption == NO;
        break;
      case YES_NO_CANCEL_OPTION:
        valid = defaultOption == YES || defaultOption == NO || defaultOption == CANCEL;
        break;
      default:
        valid = defaultOption == OK || defaultOption == CANCEL;
        break;
    }
    if (!valid) {
      throw new IllegalArgumentException("Invalid default option");
    }
    this.messageType = messageType;
    this.optionType = optionType;
    this.defaultOption = defaultOption;
  }

  public ConfirmationCallback(int messageType, String[] options, int defaultOption) {
    if (messageType < INFORMATION || messageType > ERROR) {
      throw new IllegalArgumentException("Invalid msgType");
    }
    if (options == null || options.length == 0 || defaultOption < 0 || defaultOption >= options.length) {
      throw new IllegalArgumentException("Invalid options and/or default option");
    }
    for (String o : options) {
      if (o == null || o.isEmpty()) {
        throw new IllegalArgumentException("Invalid option value");
      }
    }
    this.messageType = messageType;
    this.options = options.clone();
    this.defaultOption = defaultOption;
  }

  public ConfirmationCallback(String prompt, int messageType, int optionType, int defaultOption) {
    this(messageType, optionType, defaultOption);
    if (prompt == null || prompt.isEmpty()) {
      throw new IllegalArgumentException("Invalid prompt");
    }
    this.prompt = prompt;
  }

  public ConfirmationCallback(String prompt, int messageType, String[] options, int defaultOption) {
    this(messageType, options, defaultOption);
    if (prompt == null || prompt.isEmpty()) {
      throw new IllegalArgumentException("Invalid prompt");
    }
    this.prompt = prompt;
  }

  public String getPrompt() {
    return prompt;
  }

  public int getMessageType() {
    return messageType;
  }

  public int getOptionType() {
    return optionType;
  }

  public String[] getOptions() {
    return options;
  }

  public int getDefaultOption() {
    return defaultOption;
  }

  public void setSelectedIndex(int selection) {
    this.selection = selection;
  }

  public int getSelectedIndex() {
    return selection;
  }
}
