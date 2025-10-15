/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.util.prefs;

public class PreferenceChangeEvent extends java.util.EventObject {
  private static final long serialVersionUID = 793724513368024975L;

  private final String key;
  private final String newValue;

  public PreferenceChangeEvent(Preferences node, String key, String newValue) {
    super(node);
    this.key = key;
    this.newValue = newValue;
  }

  public Preferences getNode() {
    return (Preferences) getSource();
  }

  public String getKey() {
    return key;
  }

  public String getNewValue() {
    return newValue;
  }

  private void writeObject(java.io.ObjectOutputStream out) throws java.io.NotSerializableException {
    throw new java.io.NotSerializableException("Not serializable.");
  }

  private void readObject(java.io.ObjectInputStream in) throws java.io.NotSerializableException {
    throw new java.io.NotSerializableException("Not serializable.");
  }
}
