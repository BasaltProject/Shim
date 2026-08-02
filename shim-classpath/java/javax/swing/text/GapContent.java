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

public class GapContent extends GapVector implements AbstractDocument.Content, Serializable {
  private final TextBuffer buffer = new TextBuffer();
  private int capacity;

  public GapContent() { this(10); }

  public GapContent(int initialLength) {
    super(Math.max(initialLength, 2));
    capacity = Math.max(initialLength, 2);
  }

  protected Object allocateArray(int len) { return new char[len]; }

  protected int getArrayLength() { return capacity; }

  public int length() { return buffer.length(); }

  public UndoableEdit insertString(int where, String str) throws BadLocationException {
    UndoableEdit e = buffer.insertString(where, str);
    capacity = Math.max(capacity, buffer.length());
    return e;
  }

  public UndoableEdit remove(int where, int nitems) throws BadLocationException { return buffer.remove(where, nitems); }

  public String getString(int where, int len) throws BadLocationException { return buffer.getString(where, len); }

  public void getChars(int where, int len, Segment chars) throws BadLocationException {
    buffer.getChars(where, len, chars);
  }

  public Position createPosition(int offset) throws BadLocationException { return buffer.createPosition(offset); }

  protected void shiftEnd(int newSize) { capacity = Math.max(capacity, newSize); }

  protected void shiftGap(int newGapStart) { }

  protected void shiftGapStartDown(int newGapStart) { }

  protected void shiftGapEndUp(int newGapEnd) { }

  protected void resetMarksAtZero() { }

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
  protected void updateUndoPositions(Vector positions, int offset, int length) { }
}
