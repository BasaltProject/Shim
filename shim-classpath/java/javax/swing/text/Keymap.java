/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing.text;

public interface Keymap {
  java.lang.String getName();
  void setDefaultAction(javax.swing.Action p0);
  void addActionForKeyStroke(javax.swing.KeyStroke p0, javax.swing.Action p1);
  javax.swing.Action getDefaultAction();
  javax.swing.text.Keymap getResolveParent();
  javax.swing.Action getAction(javax.swing.KeyStroke p0);
  javax.swing.KeyStroke[] getBoundKeyStrokes();
  javax.swing.Action[] getBoundActions();
  javax.swing.KeyStroke[] getKeyStrokesForAction(javax.swing.Action p0);
  boolean isLocallyDefined(javax.swing.KeyStroke p0);
  void removeKeyStrokeBinding(javax.swing.KeyStroke p0);
  void removeBindings();
  void setResolveParent(javax.swing.text.Keymap p0);
}
