/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing.text;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.Serializable;
import java.util.EventListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.EventListenerList;

public class DefaultCaret extends Rectangle implements Caret, FocusListener, MouseListener, MouseMotionListener {
  public static final int UPDATE_WHEN_ON_EDT = 0;
  public static final int NEVER_UPDATE = 1;
  public static final int ALWAYS_UPDATE = 2;

  protected EventListenerList listenerList = new EventListenerList();
  protected transient ChangeEvent changeEvent = null;

  private JTextComponent component;
  private int dot;
  private int mark;
  private Position.Bias dotBias = Position.Bias.Forward;
  private Position.Bias markBias = Position.Bias.Forward;
  private boolean visible;
  private boolean active;
  private boolean selectionVisible;
  private int blinkRate;
  private int updatePolicy = UPDATE_WHEN_ON_EDT;
  private Point magicCaretPosition;
  private Object selectionTag;
  private final DocumentListener handler = new DocumentListener() {
    public void insertUpdate(DocumentEvent e) {
      int offs = e.getOffset();
      int len = e.getLength();
      int newDot = dot;
      int newMark = mark;
      if (updatePolicy == NEVER_UPDATE
          || (updatePolicy == UPDATE_WHEN_ON_EDT && !javax.swing.SwingUtilities.isEventDispatchThread())) {
        return;
      }
      if (newDot >= offs) {
        newDot += len;
      }
      if (newMark >= offs) {
        newMark += len;
      }
      if (newMark != mark || newDot != dot) {
        dot = newDot;
        mark = newMark;
        fireStateChanged();
      }
    }

    public void removeUpdate(DocumentEvent e) {
      if (updatePolicy == NEVER_UPDATE
          || (updatePolicy == UPDATE_WHEN_ON_EDT && !javax.swing.SwingUtilities.isEventDispatchThread())) {
        int len = component.getDocument().getLength();
        dot = Math.min(dot, len);
        mark = Math.min(mark, len);
        return;
      }
      int offs0 = e.getOffset();
      int offs1 = offs0 + e.getLength();
      int newDot = dot;
      int newMark = mark;
      if (newDot >= offs1) {
        newDot -= (offs1 - offs0);
      } else if (newDot >= offs0) {
        newDot = offs0;
      }
      if (newMark >= offs1) {
        newMark -= (offs1 - offs0);
      } else if (newMark >= offs0) {
        newMark = offs0;
      }
      if (newDot != dot || newMark != mark) {
        dot = newDot;
        mark = newMark;
        fireStateChanged();
      }
    }

    public void changedUpdate(DocumentEvent e) { }
  };

  public DefaultCaret() { }

  public void setUpdatePolicy(int policy) { updatePolicy = policy; }

  public int getUpdatePolicy() { return updatePolicy; }

  protected final JTextComponent getComponent() { return component; }

  protected final synchronized void repaint() { }

  protected void damage(Rectangle r) { }

  protected void adjustVisibility(Rectangle nloc) { }

  protected Highlighter.HighlightPainter getSelectionPainter() { return DefaultHighlighter.DefaultPainter; }

  protected void positionCaret(MouseEvent e) {
    Point pt = new Point(e.getX(), e.getY());
    int pos = component.viewToModel(pt);
    if (pos >= 0) {
      setDot(pos);
    }
  }

  protected void moveCaret(MouseEvent e) {
    Point pt = new Point(e.getX(), e.getY());
    int pos = component.viewToModel(pt);
    if (pos >= 0) {
      moveDot(pos);
    }
  }

  public void focusGained(FocusEvent e) {
    if (component.isEnabled()) {
      if (component.isEditable()) {
        setVisible(true);
      }
      setSelectionVisible(true);
    }
  }

  public void focusLost(FocusEvent e) {
    setVisible(false);
    setSelectionVisible(e.isTemporary());
  }

  public void mouseClicked(MouseEvent e) { }

  public void mousePressed(MouseEvent e) {
    if (!e.isConsumed() && javax.swing.SwingUtilities.isLeftMouseButton(e)) {
      positionCaret(e);
    }
  }

  public void mouseReleased(MouseEvent e) { }

  public void mouseEntered(MouseEvent e) { }

  public void mouseExited(MouseEvent e) { }

  public void mouseDragged(MouseEvent e) {
    if (!e.isConsumed() && javax.swing.SwingUtilities.isLeftMouseButton(e)) {
      moveCaret(e);
    }
  }

  public void mouseMoved(MouseEvent e) { }

  public void paint(Graphics g) {
    if (isVisible() && component != null) {
      try {
        Rectangle r = component.getUI().modelToView(component, dot, dotBias);
        if (r == null || (r.width == 0 && r.height == 0)) {
          return;
        }
        g.setColor(component.getCaretColor());
        int paintWidth = caretPaintWidth();
        r.x -= paintWidth >> 1;
        g.fillRect(r.x, r.y, paintWidth, r.height);
      } catch (BadLocationException e) {
        // a stale position paints nothing
      }
    }
  }

  private int caretPaintWidth() {
    Object property = component.getClientProperty("caretWidth");
    if (property instanceof Number) {
      return ((Number) property).intValue();
    }
    property = javax.swing.UIManager.get("Caret.width");
    if (property instanceof Number) {
      return ((Number) property).intValue();
    }
    return 1;
  }

  public void install(JTextComponent c) {
    component = c;
    Document doc = c.getDocument();
    dot = mark = 0;
    if (doc != null) {
      doc.addDocumentListener(handler);
    }
    c.addFocusListener(this);
    c.addMouseListener(this);
    c.addMouseMotionListener(this);
  }

  public void deinstall(JTextComponent c) {
    c.removeMouseListener(this);
    c.removeMouseMotionListener(this);
    c.removeFocusListener(this);
    if (c.getDocument() != null) {
      c.getDocument().removeDocumentListener(handler);
    }
    component = null;
  }

  void documentChanged(Document oldDoc, Document newDoc) {
    if (oldDoc != null) {
      oldDoc.removeDocumentListener(handler);
    }
    if (newDoc != null) {
      newDoc.addDocumentListener(handler);
    }
    dot = mark = 0;
  }

  public void addChangeListener(ChangeListener l) { listenerList.add(ChangeListener.class, l); }

  public void removeChangeListener(ChangeListener l) { listenerList.remove(ChangeListener.class, l); }

  public ChangeListener[] getChangeListeners() { return listenerList.getListeners(ChangeListener.class); }

  protected void fireStateChanged() {
    Object[] listeners = listenerList.getListenerList();
    for (int i = listeners.length - 2; i >= 0; i -= 2) {
      if (listeners[i] == ChangeListener.class) {
        if (changeEvent == null) {
          changeEvent = new ChangeEvent(this);
        }
        ((ChangeListener) listeners[i + 1]).stateChanged(changeEvent);
      }
    }
  }

  public <T extends EventListener> T[] getListeners(Class<T> listenerType) {
    return listenerList.getListeners(listenerType);
  }

  public void setSelectionVisible(boolean vis) {
    if (vis != selectionVisible) {
      selectionVisible = vis;
      updateSelectionHighlight();
    }
  }

  public boolean isSelectionVisible() { return selectionVisible; }

  public boolean isActive() { return active; }

  public boolean isVisible() { return visible; }

  public void setVisible(boolean e) {
    active = e;
    visible = e;
  }

  public void setBlinkRate(int rate) {
    if (rate < 0) {
      throw new IllegalArgumentException("Invalid blink rate: " + rate);
    }
    blinkRate = rate;
  }

  public int getBlinkRate() { return blinkRate; }

  public int getDot() { return dot; }

  public int getMark() { return mark; }

  public Position.Bias getDotBias() { return dotBias; }

  public Position.Bias getMarkBias() { return markBias; }

  public void setDot(int dot) { setDot(dot, Position.Bias.Forward); }

  public void moveDot(int dot) { moveDot(dot, Position.Bias.Forward); }

  public void moveDot(int dot, Position.Bias dotBias) {
    if (dot >= 0) {
      Document doc = component.getDocument();
      if (doc != null) {
        dot = Math.min(dot, doc.getLength());
      }
      dot = Math.max(dot, 0);
      if (dot == 0) {
        dotBias = Position.Bias.Forward;
      }
      NavigationFilter filter = component.getNavigationFilter();
      if (filter != null) {
        filter.moveDot(new NavigationFilter.FilterBypass() {
          public Caret getCaret() { return DefaultCaret.this; }

          public void setDot(int d, Position.Bias b) { handleSetDot(d, b); }

          public void moveDot(int d, Position.Bias b) { handleMoveDot(d, b); }
        }, dot, dotBias);
      } else {
        handleMoveDot(dot, dotBias);
      }
    }
  }

  public void setDot(int dot, Position.Bias dotBias) {
    if (dot >= 0) {
      Document doc = component.getDocument();
      if (doc != null) {
        dot = Math.min(dot, doc.getLength());
      }
      dot = Math.max(dot, 0);
      if (dot == 0) {
        dotBias = Position.Bias.Forward;
      }
      NavigationFilter filter = component.getNavigationFilter();
      if (filter != null) {
        filter.setDot(new NavigationFilter.FilterBypass() {
          public Caret getCaret() { return DefaultCaret.this; }

          public void setDot(int d, Position.Bias b) { handleSetDot(d, b); }

          public void moveDot(int d, Position.Bias b) { handleMoveDot(d, b); }
        }, dot, dotBias);
      } else {
        handleSetDot(dot, dotBias);
      }
    }
  }

  void handleSetDot(int d, Position.Bias b) {
    Document doc = component.getDocument();
    if (doc != null) {
      d = Math.min(d, doc.getLength());
    }
    d = Math.max(d, 0);
    if (d == dot && d == mark) {
      return;
    }
    mark = dot = d;
    markBias = dotBias = b == null ? Position.Bias.Forward : b;
    fireStateChanged();
    updateSelectionHighlight();
  }

  void handleMoveDot(int d, Position.Bias b) {
    if (d != dot) {
      dot = d;
      dotBias = b == null ? Position.Bias.Forward : b;
      fireStateChanged();
      updateSelectionHighlight();
    }
  }

  private void updateSelectionHighlight() {
    Highlighter h = component == null ? null : component.getHighlighter();
    if (h == null) {
      return;
    }
    try {
      if (selectionTag != null) {
        h.removeHighlight(selectionTag);
        selectionTag = null;
      }
      if (selectionVisible && dot != mark) {
        selectionTag = h.addHighlight(Math.min(dot, mark), Math.max(dot, mark), getSelectionPainter());
      }
    } catch (BadLocationException e) {
      // ignored
    }
  }

  public void setMagicCaretPosition(Point p) { magicCaretPosition = p; }

  public Point getMagicCaretPosition() { return magicCaretPosition; }

  public boolean equals(Object obj) { return this == obj; }

  public String toString() {
    return "Dot=(" + dot + ", " + dotBias + ") Mark=(" + mark + ", " + markBias + ")";
  }
}
