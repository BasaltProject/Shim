/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing.event;

import java.util.EventObject;
import javax.swing.undo.UndoableEdit;

public class UndoableEditEvent extends EventObject {
  private UndoableEdit myEdit;

  public UndoableEditEvent(Object source, UndoableEdit edit) {
    super(source);
    myEdit = edit;
  }

  public UndoableEdit getEdit() { return myEdit; }
}
