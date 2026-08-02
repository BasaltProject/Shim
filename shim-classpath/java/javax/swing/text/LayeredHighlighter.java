/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing.text;

public abstract class LayeredHighlighter implements javax.swing.text.Highlighter {
  protected LayeredHighlighter() {
  }
  public abstract void paintLayeredHighlights(java.awt.Graphics p0, int p1, int p2, java.awt.Shape p3, javax.swing.text.JTextComponent p4, javax.swing.text.View p5);

  public static abstract class LayerPainter implements javax.swing.text.Highlighter.HighlightPainter {
    protected LayerPainter() {
    }
    public abstract java.awt.Shape paintLayer(java.awt.Graphics p0, int p1, int p2, java.awt.Shape p3, javax.swing.text.JTextComponent p4, javax.swing.text.View p5);
  }
}
