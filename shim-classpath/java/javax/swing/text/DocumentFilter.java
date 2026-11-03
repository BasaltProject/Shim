/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing.text;

public class DocumentFilter {
  public abstract static class FilterBypass {
    protected FilterBypass() { }

    public abstract Document getDocument();

    public abstract void remove(int offset, int length) throws BadLocationException;

    public abstract void insertString(int offset, String string, AttributeSet attr) throws BadLocationException;

    public abstract void replace(int offset, int length, String text, AttributeSet attrs) throws BadLocationException;
  }

  public DocumentFilter() { }

  public void remove(FilterBypass fb, int offset, int length) throws BadLocationException {
    fb.remove(offset, length);
  }

  public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
    fb.insertString(offset, string, attr);
  }

  public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
      throws BadLocationException {
    fb.replace(offset, length, text, attrs);
  }
}
