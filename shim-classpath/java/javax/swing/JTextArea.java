/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing;

import java.awt.*;
import javax.swing.text.*;
import javax.accessibility.*;

public class JTextArea extends javax.swing.text.JTextComponent {
  private int rowHeight;
  private int columnWidth;
  private AccessibleContext accessibleContext;
  private int bp_TabSize;
  private int bp_LineCount;
  private boolean bp_LineWrap;
  private boolean bp_WrapStyleWord;
  private java.awt.Dimension bp_PreferredScrollableViewportSize;
  private boolean bp_ScrollableTracksViewportWidth;
  private int bp_Rows;
  private int bp_Columns;
  private javax.accessibility.AccessibleContext bp_AccessibleContext;
  private java.awt.Dimension bp_PreferredSize;
  public JTextArea(javax.swing.text.Document p0, java.lang.String p1, int p2, int p3) {
    super();
    bp_Rows = p2;
    bp_Columns = p3;
    if (p2 < 0 || p3 < 0) {
      throw new IllegalArgumentException("rows: " + p2 + " columns: " + p3);
    }
    bp_TabSize = 8;
    if (p0 == null) {
      p0 = createDefaultModel();
    }
    setDocument(p0);
    if (p1 != null) {
      setText(p1);
      select(0, 0);
    }
  }
  public JTextArea(javax.swing.text.Document p0) {
    this(p0, null, 0, 0);
  }
  public JTextArea(java.lang.String p0, int p1, int p2) {
    this(null, p0, p1, p2);
  }
  public JTextArea() {
    this(null, null, 0, 0);
  }
  public JTextArea(java.lang.String p0) {
    this(null, p0, 0, 0);
  }
  public JTextArea(int p0, int p1) {
    this(null, null, p0, p1);
  }
  public void append(java.lang.String p0) {
    Document doc = getDocument();
    if (doc != null) {
      try {
        doc.insertString(doc.getLength(), p0, null);
      } catch (BadLocationException e) {
        // ignored
      }
    }
  }
  public void insert(java.lang.String p0, int p1) {
    if (p1 < 0) {
      throw new IllegalArgumentException("posistion less than zero.");
    }
    Document doc = getDocument();
    if (doc != null) {
      try {
        doc.insertString(p1, p0, null);
      } catch (BadLocationException e) {
        throw new IllegalArgumentException(e.getMessage());
      }
    }
  }
  protected java.lang.String paramString() {
    String wrapString = (bp_LineWrap ? "true" : "false");
    String wordString = (bp_WrapStyleWord ? "true" : "false");
    return super.paramString() + ",colums=" + bp_Columns + ",columWidth=" + columnWidth + ",rows=" + bp_Rows + ",rowHeight="
        + rowHeight + ",word=" + wordString + ",wrap=" + wrapString;
  }
  public int getTabSize() {
    int size = 8;
    Document doc = getDocument();
    if (doc != null) {
      Integer i = (Integer) doc.getProperty(PlainDocument.tabSizeAttribute);
      if (i != null) {
        size = i.intValue();
      }
    }
    return size;
  }
  public int getLineCount() {
    Document doc = getDocument();
    return doc == null ? 0 : doc.getDefaultRootElement().getElementCount();
  }
  public void setTabSize(int p0) {
    Document doc = getDocument();
    if (doc != null) {
      int old = getTabSize();
      doc.putProperty(PlainDocument.tabSizeAttribute, Integer.valueOf(p0));
      firePropertyChange("tabSize", old, p0);
    }
  }
  public void setLineWrap(boolean p0) {
    boolean old = bp_LineWrap;
    bp_LineWrap = p0;
    firePropertyChange("lineWrap", old, p0);
  }
  public void setWrapStyleWord(boolean p0) {
    boolean old = bp_WrapStyleWord;
    bp_WrapStyleWord = p0;
    firePropertyChange("wrapStyleWord", old, p0);
  }
  public boolean getWrapStyleWord() { return bp_WrapStyleWord; }
  public int getLineOfOffset(int p0) throws javax.swing.text.BadLocationException {
    Document doc = getDocument();
    if (p0 < 0) {
      throw new BadLocationException("Can't translate offset to line", -1);
    } else if (p0 > doc.getLength()) {
      throw new BadLocationException("Can't translate offset to line", doc.getLength() + 1);
    }
    Element map = getDocument().getDefaultRootElement();
    return map.getElementIndex(p0);
  }
  public int getLineStartOffset(int p0) throws javax.swing.text.BadLocationException {
    int lineCount = getLineCount();
    if (p0 < 0) {
      throw new BadLocationException("Negative line", -1);
    } else if (p0 >= lineCount) {
      throw new BadLocationException("No such line", getDocument().getLength() + 1);
    }
    Element map = getDocument().getDefaultRootElement();
    Element line = map.getElement(p0);
    return line.getStartOffset();
  }
  public int getLineEndOffset(int p0) throws javax.swing.text.BadLocationException {
    int lineCount = getLineCount();
    if (p0 < 0) {
      throw new BadLocationException("Negative line", -1);
    } else if (p0 >= lineCount) {
      throw new BadLocationException("No such line", getDocument().getLength() + 1);
    }
    Element map = getDocument().getDefaultRootElement();
    Element line = map.getElement(p0);
    int end = line.getEndOffset();
    return (p0 == lineCount - 1) ? (end - 1) : end;
  }
  public java.awt.Dimension getPreferredScrollableViewportSize() {
    Dimension size = super.getPreferredScrollableViewportSize();
    size = (size == null) ? new Dimension(400, 400) : size;
    Insets insets = getInsets();
    size.width = (bp_Columns == 0) ? size.width : bp_Columns * getColumnWidth() + insets.left + insets.right;
    size.height = (bp_Rows == 0) ? size.height : bp_Rows * getRowHeight() + insets.top + insets.bottom;
    return size;
  }
  public int getScrollableUnitIncrement(java.awt.Rectangle p0, int p1, int p2) {
    switch (p1) {
      case SwingConstants.VERTICAL:
        return getRowHeight();
      case SwingConstants.HORIZONTAL:
        return getColumnWidth();
      default:
        throw new IllegalArgumentException("Invalid orientation: " + p1);
    }
  }
  public boolean getScrollableTracksViewportWidth() {
    return bp_LineWrap ? true : super.getScrollableTracksViewportWidth();
  }
  protected int getRowHeight() {
    if (rowHeight == 0) {
      FontMetrics metrics = getFontMetrics(getFont());
      rowHeight = metrics.getHeight();
    }
    return rowHeight;
  }
  protected javax.swing.text.Document createDefaultModel() {
    return new PlainDocument();
  }
  protected int getColumnWidth() {
    if (columnWidth == 0) {
      FontMetrics metrics = getFontMetrics(getFont());
      columnWidth = metrics.charWidth('m');
    }
    return columnWidth;
  }
  public int getRows() { return bp_Rows; }
  public void setRows(int p0) {
    int oldVal = bp_Rows;
    if (p0 < 0) {
      throw new IllegalArgumentException("rows less than zero.");
    }
    if (p0 != oldVal) {
      bp_Rows = p0;
      invalidate();
    }
  }
  public int getColumns() { return bp_Columns; }
  public void setColumns(int p0) {
    int oldVal = bp_Columns;
    if (p0 < 0) {
      throw new IllegalArgumentException("columns less than zero.");
    }
    if (p0 != oldVal) {
      bp_Columns = p0;
      invalidate();
    }
  }
  public boolean getLineWrap() { return bp_LineWrap; }
  public void replaceRange(java.lang.String p0, int p1, int p2) {
    if (p2 < p1) {
      throw new IllegalArgumentException("end before start");
    }
    Document doc = getDocument();
    if (doc != null) {
      try {
        if (doc instanceof AbstractDocument) {
          ((AbstractDocument) doc).replace(p1, p2 - p1, p0, null);
        } else {
          doc.remove(p1, p2 - p1);
          doc.insertString(p1, p0, null);
        }
      } catch (BadLocationException e) {
        throw new IllegalArgumentException(e.getMessage());
      }
    }
  }
  public javax.accessibility.AccessibleContext getAccessibleContext() {
    if (accessibleContext == null) {
      accessibleContext = new AccessibleJTextArea();
    }
    return accessibleContext;
  }
  public void setFont(java.awt.Font p0) {
    super.setFont(p0);
    rowHeight = 0;
    columnWidth = 0;
  }
  public java.awt.Dimension getPreferredSize() {
    Dimension d = super.getPreferredSize();
    d = (d == null) ? new Dimension(400, 400) : d;
    Insets insets = getInsets();
    if (bp_Columns != 0) {
      d.width = Math.max(d.width, bp_Columns * getColumnWidth() + insets.left + insets.right);
    }
    if (bp_Rows != 0) {
      d.height = Math.max(d.height, bp_Rows * getRowHeight() + insets.top + insets.bottom);
    }
    return d;
  }
  public java.lang.String getUIClassID() { return "TextAreaUI"; }

  protected class AccessibleJTextArea extends AccessibleJTextComponent {
    public AccessibleStateSet getAccessibleStateSet() {
      AccessibleStateSet states = super.getAccessibleStateSet();
      states.add(AccessibleState.MULTI_LINE);
      return states;
    }
  }

}
