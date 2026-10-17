/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.security.auth.callback;

public class ChoiceCallback implements Callback, java.io.Serializable {
  private static final long serialVersionUID = -3975664071579892167L;

  private final String prompt;
  private final String[] choices;
  private final int defaultChoice;
  private final boolean multipleSelectionsAllowed;
  private int[] selections;

  public ChoiceCallback(String prompt, String[] choices, int defaultChoice, boolean multipleSelectionsAllowed) {
    if (prompt == null || prompt.isEmpty() || choices == null || choices.length == 0 || defaultChoice < 0
        || defaultChoice >= choices.length) {
      throw new IllegalArgumentException("Missing/invalid prompt/choices");
    }
    for (int i = 0; i < choices.length; i++) {
      if (choices[i] == null || choices[i].isEmpty()) {
        throw new IllegalArgumentException("Null/empty choices value");
      }
    }
    this.prompt = prompt;
    this.choices = choices.clone();
    this.defaultChoice = defaultChoice;
    this.multipleSelectionsAllowed = multipleSelectionsAllowed;
  }

  public String getPrompt() {
    return prompt;
  }

  public String[] getChoices() {
    return choices;
  }

  public int getDefaultChoice() {
    return defaultChoice;
  }

  public boolean allowMultipleSelections() {
    return multipleSelectionsAllowed;
  }

  public void setSelectedIndex(int selection) {
    this.selections = new int[] {selection};
  }

  public void setSelectedIndexes(int[] selections) {
    if (!multipleSelectionsAllowed) {
      throw new UnsupportedOperationException();
    }
    this.selections = selections == null ? null : selections.clone();
  }

  public int[] getSelectedIndexes() {
    return selections == null ? null : selections.clone();
  }
}
