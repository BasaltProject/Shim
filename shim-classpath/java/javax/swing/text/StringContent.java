/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing.text;

import java.io.Serializable;
import java.util.Vector;
import javax.swing.undo.UndoableEdit;
import shim.swing.TextBuffer;

public final class StringContent implements AbstractDocument.Content, Serializable {
  private final TextBuffer buffer = new TextBuffer();

  public StringContent() { }

  public StringContent(int initialLength) {
    if (initialLength < 1) {
      throw new NegativeArraySizeException(String.valueOf(initialLength));
    }
  }

  public int length() { return buffer.length(); }

  public UndoableEdit insertString(int where, String str) throws BadLocationException {
    return buffer.insertString(where, str);
  }

  public UndoableEdit remove(int where, int nitems) throws BadLocationException { return buffer.remove(where, nitems); }

  public String getString(int where, int len) throws BadLocationException { return buffer.getString(where, len); }

  public void getChars(int where, int len, Segment chars) throws BadLocationException {
    buffer.getChars(where, len, chars);
  }

  public Position createPosition(int offset) throws BadLocationException { return buffer.createPosition(offset); }

  @SuppressWarnings({"rawtypes", "unchecked"})
  protected Vector getPositionsInRange(Vector v, int offset, int length) {
    Vector found = buffer.positionsInRange(offset, offset + length);
    if (v == null) {
      return found;
    }
    v.addAll(found);
    return v;
  }

  @SuppressWarnings("rawtypes")
  protected void updateUndoPositions(Vector positions) { }
}
