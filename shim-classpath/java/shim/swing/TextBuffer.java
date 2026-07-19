/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.swing;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.Position;
import javax.swing.text.Segment;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEdit;

public final class TextBuffer implements AbstractDocument.Content, java.io.Serializable {
  private final StringBuilder text = new StringBuilder("\n");
  private final transient ArrayList<WeakReference<Mark>> marks = new ArrayList<>();

  public static final class Mark implements Position {
    int offset;

    Mark(int offset) { this.offset = offset; }

    public int getOffset() { return offset; }
  }

  public int length() { return text.length(); }

  public UndoableEdit insertString(int where, String str) throws BadLocationException {
    if (where > text.length() || where < 0) {
      throw new BadLocationException("Invalid insert", text.length());
    }
    doInsert(where, str);
    return new InsertEdit(where, str);
  }

  public UndoableEdit remove(int where, int nitems) throws BadLocationException {
    if (where + nitems >= text.length()) {
      throw new BadLocationException("Invalid remove", text.length() + 1);
    }
    String removed = text.substring(where, where + nitems);
    doRemove(where, nitems);
    return new RemoveEdit(where, removed);
  }

  public String getString(int where, int len) throws BadLocationException {
    if (where < 0 || len < 0 || where + len > text.length()) {
      throw new BadLocationException("Invalid location", text.length());
    }
    return text.substring(where, where + len);
  }

  public void getChars(int where, int len, Segment chars) throws BadLocationException {
    if (where < 0 || len < 0 || where + len > text.length()) {
      throw new BadLocationException("Invalid location", text.length());
    }
    char[] arr = new char[len];
    text.getChars(where, where + len, arr, 0);
    chars.array = arr;
    chars.offset = 0;
    chars.count = len;
  }

  public Position createPosition(int offset) throws BadLocationException {
    if (offset < 0 || offset > text.length()) {
      throw new BadLocationException("Invalid position", offset);
    }
    Mark m = new Mark(offset);
    synchronized (marks) {
      if (marks.size() > 64) {
        marks.removeIf(w -> w.get() == null);
      }
      marks.add(new WeakReference<>(m));
    }
    return m;
  }

  public java.util.Vector<Position> positionsInRange(int start, int end) {
    java.util.Vector<Position> v = new java.util.Vector<>();
    synchronized (marks) {
      for (Iterator<WeakReference<Mark>> it = marks.iterator(); it.hasNext();) {
        Mark m = it.next().get();
        if (m == null) {
          it.remove();
        } else if (m.offset >= start && m.offset <= end) {
          v.add(m);
        }
      }
    }
    return v;
  }

  private void doInsert(int where, String str) {
    text.insert(where, str);
    int n = str.length();
    synchronized (marks) {
      for (WeakReference<Mark> w : marks) {
        Mark m = w.get();
        if (m != null && m.offset >= where && m.offset != 0) {
          m.offset += n;
        }
      }
    }
  }

  private void doRemove(int where, int n) {
    text.delete(where, where + n);
    synchronized (marks) {
      for (WeakReference<Mark> w : marks) {
        Mark m = w.get();
        if (m == null) {
          continue;
        }
        if (m.offset >= where + n) {
          m.offset -= n;
        } else if (m.offset > where) {
          m.offset = where;
        }
      }
    }
  }

  private final class InsertEdit extends AbstractUndoableEdit {
    private final int where;
    private final String str;

    InsertEdit(int where, String str) {
      this.where = where;
      this.str = str;
    }

    public void undo() throws CannotUndoException {
      super.undo();
      doRemove(where, str.length());
    }

    public void redo() throws CannotRedoException {
      super.redo();
      doInsert(where, str);
    }
  }

  private final class RemoveEdit extends AbstractUndoableEdit {
    private final int where;
    private final String str;

    RemoveEdit(int where, String str) {
      this.where = where;
      this.str = str;
    }

    public void undo() throws CannotUndoException {
      super.undo();
      doInsert(where, str);
    }

    public void redo() throws CannotRedoException {
      super.redo();
      doRemove(where, str.length());
    }
  }
}
