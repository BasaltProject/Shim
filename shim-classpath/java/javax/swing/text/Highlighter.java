/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing.text;

public interface Highlighter {
  void deinstall(javax.swing.text.JTextComponent p0);
  void install(javax.swing.text.JTextComponent p0);
  java.lang.Object addHighlight(int p0, int p1, javax.swing.text.Highlighter.HighlightPainter p2) throws javax.swing.text.BadLocationException;
  void removeHighlight(java.lang.Object p0);
  void removeAllHighlights();
  void changeHighlight(java.lang.Object p0, int p1, int p2) throws javax.swing.text.BadLocationException;
  javax.swing.text.Highlighter.Highlight[] getHighlights();
  void paint(java.awt.Graphics p0);

  public interface Highlight {
    javax.swing.text.Highlighter.HighlightPainter getPainter();
    int getEndOffset();
    int getStartOffset();
  }

  public interface HighlightPainter {
    void paint(java.awt.Graphics p0, int p1, int p2, java.awt.Shape p3, javax.swing.text.JTextComponent p4);
  }
}
