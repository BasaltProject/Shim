/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing.text;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.io.*;
import java.text.MessageFormat;
import java.awt.print.Printable;
import javax.print.PrintService;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.plaf.TextUI;
import javax.accessibility.*;

public abstract class JTextComponent extends javax.swing.JComponent implements javax.swing.Scrollable, javax.accessibility.Accessible {
  Dimension plainContentSize() {
    Insets in = getInsets();
    Font f = getFont();
    FontMetrics fm = f == null ? null : getFontMetrics(f);
    String text = getText();
    int lines = 1;
    int width = 0;
    if (fm != null) {
      int start = 0;
      if (text != null) {
        for (int i = 0; i <= text.length(); i++) {
          if (i == text.length() || text.charAt(i) == '\n') {
            width = Math.max(width, fm.stringWidth(text.substring(start, i)));
            if (i < text.length()) {
              lines++;
            }
            start = i + 1;
          }
        }
      }
    }
    int lineHeight = fm == null ? 0 : fm.getHeight();
    return new Dimension(width + in.left + in.right + 1, lines * lineHeight + in.top + in.bottom);
  }

  public Dimension getPreferredSize() {
    if (isPreferredSizeSet() || ui != null && ui.getPreferredSize(this) != null) {
      return super.getPreferredSize();
    }
    return plainContentSize();
  }

  public Dimension getMaximumSize() {
    return isMaximumSizeSet() || ui != null && ui.getMaximumSize(this) != null ? super.getMaximumSize()
        : new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
  }

  public Dimension getMinimumSize() {
    if (isMinimumSizeSet() || ui != null && ui.getMinimumSize(this) != null) {
      return super.getMinimumSize();
    }
    return plainContentSize();
  }

  private static final java.util.Map<String, Keymap> keymapTable = new java.util.HashMap<>();
  static {
    Keymap binding = new shim.swing.DefaultKeymap("default", null);
    binding.setDefaultAction(new DefaultEditorKit.DefaultKeyTypedAction());
    keymapTable.put("default", binding);
  }
  private javax.accessibility.AccessibleContext accessibleContext;
  private final MutableCaretEvent cap = new MutableCaretEvent(this);
  private java.lang.String bp_Text;
  private java.awt.Color bp_CaretColor;
  private java.awt.Color bp_SelectionColor;
  private java.awt.Color bp_SelectedTextColor;
  private java.awt.Color bp_DisabledTextColor;
  private char bp_FocusAccelerator;
  private java.awt.Dimension bp_PreferredScrollableViewportSize;
  private boolean bp_ScrollableTracksViewportWidth;
  private boolean bp_ScrollableTracksViewportHeight;
  private javax.swing.text.Document bp_Document;
  private boolean bp_DragEnabled;
  private javax.swing.text.Keymap bp_Keymap;
  private javax.swing.text.Caret bp_Caret;
  private final java.util.concurrent.CopyOnWriteArrayList<javax.swing.event.CaretListener> bl_CaretListener = new java.util.concurrent.CopyOnWriteArrayList<>();
  private javax.swing.event.CaretListener[] bp_CaretListeners;
  private javax.swing.text.NavigationFilter bp_NavigationFilter;
  private javax.swing.text.Highlighter bp_Highlighter;
  private javax.swing.DropMode bp_DropMode;
  private javax.swing.text.JTextComponent.DropLocation bp_DropLocation;
  private int bp_SelectionStart;
  private int bp_SelectionEnd;
  private boolean bp_Editable = false;
  private int bp_CaretPosition;
  private javax.swing.Action[] bp_Actions;
  private javax.accessibility.AccessibleContext bp_AccessibleContext;
  private final java.util.concurrent.CopyOnWriteArrayList<java.awt.event.InputMethodListener> bl_InputMethodListener = new java.util.concurrent.CopyOnWriteArrayList<>();
  private java.awt.im.InputMethodRequests bp_InputMethodRequests;
  private java.lang.String bp_SelectedText;
  private java.awt.Insets bp_Margin;
  public static final java.lang.String FOCUS_ACCELERATOR_KEY = "focusAcceleratorKey";
  public static final java.lang.String DEFAULT_KEYMAP = "default";
  public JTextComponent() {
    super();
    setEditable(true);
    setLayout(null);
    setCaret(new DefaultCaret());
    setHighlighter(new DefaultHighlighter());
    setKeymap(addKeymap(null, getKeymap(DEFAULT_KEYMAP)));
    updateUI();
  }
  public void copy() {
    if (getSelectionStart() != getSelectionEnd() && getSelectedText() != null) {
      clipboard().setContents(new java.awt.datatransfer.StringSelection(getSelectedText()), null);
    }
  }
  public boolean print(java.text.MessageFormat p0, java.text.MessageFormat p1, boolean p2, javax.print.PrintService p3, javax.print.attribute.PrintRequestAttributeSet p4, boolean p5) throws java.awt.print.PrinterException {
    if (java.awt.GraphicsEnvironment.isHeadless() && p2) {
      throw new java.awt.HeadlessException();
    }
    return false;
  }
  public boolean print() throws java.awt.print.PrinterException {
    return print(null, null, true, null, null, true);
  }
  public boolean print(java.text.MessageFormat p0, java.text.MessageFormat p1) throws java.awt.print.PrinterException {
    return print(p0, p1, true, null, null, true);
  }
  public void write(java.io.Writer p0) throws java.io.IOException {
    Document doc = getDocument();
    try {
      p0.write(doc.getText(0, doc.getLength()));
      p0.flush();
    } catch (BadLocationException e) {
      throw new java.io.IOException(e.getMessage());
    }
  }
  protected java.lang.String paramString() {
    String editableString = (isEditable() ? "true" : "false");
    String caretColorString = (getCaretColor() != null ? getCaretColor().toString() : "");
    String selectionColorString = (getSelectionColor() != null ? getSelectionColor().toString() : "");
    String selectedTextColorString = (getSelectedTextColor() != null ? getSelectedTextColor().toString() : "");
    String disabledTextColorString = (getDisabledTextColor() != null ? getDisabledTextColor().toString() : "");
    String marginString = (getMargin() != null ? getMargin().toString() : "");
    return super.paramString() + ",caretColor=" + caretColorString + ",disabledTextColor=" + disabledTextColorString
        + ",editable=" + editableString + ",margin=" + marginString + ",selectedTextColor=" + selectedTextColorString
        + ",selectionColor=" + selectionColorString;
  }
  public void read(java.io.Reader p0, java.lang.Object p1) throws java.io.IOException {
    StringBuilder sb = new StringBuilder();
    char[] buf = new char[4096];
    int n;
    while ((n = p0.read(buf)) > 0) {
      sb.append(buf, 0, n);
    }
    String text = sb.toString();
    Document doc = new PlainDocument();
    if (text.indexOf('\r') >= 0) {
      doc.putProperty("__EndOfLine__", text.indexOf("\r\n") >= 0 ? "\r\n" : "\r");
      text = text.replace("\r\n", "\n").replace('\r', '\n');
    }
    try {
      doc.insertString(0, text, null);
    } catch (BadLocationException e) {
      throw new java.io.IOException(e.getMessage());
    }
    if (p1 != null) {
      doc.putProperty(Document.StreamDescriptionProperty, p1);
    }
    setDocument(doc);
  }
  public java.lang.String getText(int p0, int p1) throws javax.swing.text.BadLocationException {
    return getDocument().getText(p0, p1);
  }
  public java.lang.String getText() {
    Document doc = getDocument();
    try {
      return doc.getText(0, doc.getLength());
    } catch (BadLocationException e) {
      return null;
    }
  }
  public java.awt.print.Printable getPrintable(java.text.MessageFormat p0, java.text.MessageFormat p1) {
    return new java.awt.print.Printable() {
      public int print(java.awt.Graphics graphics, java.awt.print.PageFormat pageFormat, int pageIndex) {
        return pageIndex == 0 ? PAGE_EXISTS : NO_SUCH_PAGE;
      }
    };
  }
  public static void loadKeymap(javax.swing.text.Keymap p0, javax.swing.text.JTextComponent.KeyBinding[] p1, javax.swing.Action[] p2) {
    for (KeyBinding binding : p1) {
      for (Action a : p2) {
        String name = (String) a.getValue(Action.NAME);
        if (name != null && name.equals(binding.actionName)) {
          p0.addActionForKeyStroke(binding.key, a);
          break;
        }
      }
    }
  }
  public java.awt.Color getCaretColor() { return bp_CaretColor; }
  public void setCaretColor(java.awt.Color p0) { java.awt.Color old = bp_CaretColor; bp_CaretColor = p0; firePropertyChange("caretColor", old, p0); }
  public java.awt.Color getSelectionColor() { return bp_SelectionColor; }
  public void setSelectionColor(java.awt.Color p0) { java.awt.Color old = bp_SelectionColor; bp_SelectionColor = p0; firePropertyChange("selectionColor", old, p0); }
  public java.awt.Color getSelectedTextColor() { return bp_SelectedTextColor; }
  public void setSelectedTextColor(java.awt.Color p0) { java.awt.Color old = bp_SelectedTextColor; bp_SelectedTextColor = p0; firePropertyChange("selectedTextColor", old, p0); }
  public java.awt.Color getDisabledTextColor() { return bp_DisabledTextColor; }
  public void setDisabledTextColor(java.awt.Color p0) { java.awt.Color old = bp_DisabledTextColor; bp_DisabledTextColor = p0; firePropertyChange("disabledTextColor", old, p0); }
  public void setFocusAccelerator(char p0) { bp_FocusAccelerator = p0; }
  public char getFocusAccelerator() { return bp_FocusAccelerator; }
  public java.awt.Dimension getPreferredScrollableViewportSize() {
    return getPreferredSize();
  }
  public int getScrollableUnitIncrement(java.awt.Rectangle p0, int p1, int p2) {
    switch (p1) {
      case SwingConstants.VERTICAL:
        return p0.height / 10;
      case SwingConstants.HORIZONTAL:
        return p0.width / 10;
      default:
        throw new IllegalArgumentException("Invalid orientation: " + p1);
    }
  }
  public int getScrollableBlockIncrement(java.awt.Rectangle p0, int p1, int p2) {
    switch (p1) {
      case SwingConstants.VERTICAL:
        return p0.height;
      case SwingConstants.HORIZONTAL:
        return p0.width;
      default:
        throw new IllegalArgumentException("Invalid orientation: " + p1);
    }
  }
  public boolean getScrollableTracksViewportWidth() {
    return (getParent() instanceof JViewport) && getParent().getWidth() > getPreferredSize().width;
  }
  public boolean getScrollableTracksViewportHeight() {
    return (getParent() instanceof JViewport) && getParent().getHeight() > getPreferredSize().height;
  }
  public void setDocument(javax.swing.text.Document p0) {
    if (p0 == null) {
      throw new IllegalArgumentException("document must not be null");
    }
    Document old = bp_Document;
    if (old != null) {
      old.removeDocumentListener(cap);
    }
    bp_Document = p0;
    if (bp_Caret instanceof DefaultCaret) {
      ((DefaultCaret) bp_Caret).documentChanged(old, p0);
    }
    firePropertyChange("document", old, p0);
    revalidate();
    repaint();
  }
  public void setDragEnabled(boolean p0) {
    if (p0 && java.awt.GraphicsEnvironment.isHeadless()) {
      throw new java.awt.HeadlessException();
    }
    bp_DragEnabled = p0;
  }
  public javax.swing.text.Document getDocument() { return bp_Document; }
  public int viewToModel(java.awt.Point p0) {
    TextUI ui = getUI();
    return ui == null ? -1 : ui.viewToModel(this, p0);
  }
  public static javax.swing.text.Keymap addKeymap(java.lang.String p0, javax.swing.text.Keymap p1) {
    Keymap map = new shim.swing.DefaultKeymap(p0, p1);
    if (p0 != null) {
      keymapTable.put(p0, map);
    }
    return map;
  }
  protected boolean saveComposedText(int p0) {
    return false;
  }
  protected void restoreComposedText() {
    
  }
  public java.awt.Rectangle modelToView(int p0) throws javax.swing.text.BadLocationException {
    TextUI ui = getUI();
    return ui == null ? null : ui.modelToView(this, p0);
  }
  public java.awt.geom.Rectangle2D modelToView2D(int p0) throws javax.swing.text.BadLocationException {
    TextUI ui = getUI();
    return ui == null ? null : ui.modelToView2D(this, p0, Position.Bias.Forward);
  }
  public int viewToModel2D(java.awt.geom.Point2D p0) {
    TextUI ui = getUI();
    return ui == null ? -1 : ui.viewToModel2D(this, p0, new Position.Bias[1]);
  }
  public void moveCaretPosition(int p0) {
    Document doc = getDocument();
    if (doc != null) {
      if (p0 > doc.getLength() || p0 < 0) {
        throw new IllegalArgumentException("bad position: " + p0);
      }
      bp_Caret.moveDot(p0);
    }
  }
  public void replaceSelection(java.lang.String p0) {
    Document doc = getDocument();
    if (doc != null) {
      try {
        boolean composedTextSaved = saveComposedText(bp_Caret.getDot());
        int p00 = Math.min(bp_Caret.getDot(), bp_Caret.getMark());
        int p11 = Math.max(bp_Caret.getDot(), bp_Caret.getMark());
        if (doc instanceof AbstractDocument) {
          ((AbstractDocument) doc).replace(p00, p11 - p00, p0, null);
        } else {
          if (p00 != p11) {
            doc.remove(p00, p11 - p00);
          }
          if (p0 != null && p0.length() > 0) {
            doc.insertString(p00, p0, null);
          }
        }
        if (composedTextSaved) {
          restoreComposedText();
        }
      } catch (BadLocationException e) {
        UIManager.getLookAndFeel().provideErrorFeedback(JTextComponent.this);
      }
    }
  }
  public javax.swing.text.Keymap getKeymap() { return bp_Keymap; }
  public static javax.swing.text.Keymap getKeymap(java.lang.String p0) {
    return keymapTable.get(p0);
  }
  public void setCaret(javax.swing.text.Caret p0) {
    if (bp_Caret != null) {
      bp_Caret.removeChangeListener(cap);
      bp_Caret.deinstall(this);
    }
    Caret old = bp_Caret;
    bp_Caret = p0;
    if (bp_Caret != null) {
      bp_Caret.install(this);
      bp_Caret.addChangeListener(cap);
    }
    firePropertyChange("caret", old, bp_Caret);
  }
  public void addCaretListener(javax.swing.event.CaretListener p0) { if (p0 != null) { bl_CaretListener.add(p0); } }
  public void removeCaretListener(javax.swing.event.CaretListener p0) { bl_CaretListener.remove(p0); }
  public javax.swing.event.CaretListener[] getCaretListeners() { return bl_CaretListener.toArray(new javax.swing.event.CaretListener[0]); }
  protected void fireCaretUpdate(javax.swing.event.CaretEvent p0) {
    for (int i = bl_CaretListener.size() - 1; i >= 0; i--) {
      bl_CaretListener.get(i).caretUpdate(p0);
    }
  }
  public void setNavigationFilter(javax.swing.text.NavigationFilter p0) { javax.swing.text.NavigationFilter old = bp_NavigationFilter; bp_NavigationFilter = p0; firePropertyChange("navigationFilter", old, p0); }
  public javax.swing.text.NavigationFilter getNavigationFilter() { return bp_NavigationFilter; }
  public javax.swing.text.Highlighter getHighlighter() { return bp_Highlighter; }
  public void setHighlighter(javax.swing.text.Highlighter p0) {
    if (bp_Highlighter != null) {
      bp_Highlighter.deinstall(this);
    }
    Highlighter old = bp_Highlighter;
    bp_Highlighter = p0;
    if (bp_Highlighter != null) {
      bp_Highlighter.install(this);
    }
    firePropertyChange("highlighter", old, p0);
  }
  public void setKeymap(javax.swing.text.Keymap p0) {
    javax.swing.text.Keymap old = bp_Keymap;
    bp_Keymap = p0;
    updateInputMap(old, p0);
    firePropertyChange("keymap", old, p0);
  }

  private void updateInputMap(Keymap oldKm, Keymap newKm) {
    javax.swing.InputMap km = getInputMap(WHEN_FOCUSED);
    javax.swing.InputMap last = km;
    while (km != null && !(km instanceof KeymapWrapper)) {
      last = km;
      km = km.getParent();
    }
    if (km != null) {
      if (newKm == null) {
        last.setParent(last != km ? km.getParent() : null);
      } else {
        javax.swing.InputMap newKM = new KeymapWrapper(newKm);
        last.setParent(newKM);
        if (last != km) {
          newKM.setParent(km.getParent());
        }
      }
    } else if (newKm != null) {
      km = getInputMap(WHEN_FOCUSED);
      if (km != null) {
        javax.swing.InputMap newKM = new KeymapWrapper(newKm);
        newKM.setParent(km.getParent());
        km.setParent(newKM);
      }
    }
    javax.swing.ActionMap am = getActionMap();
    javax.swing.ActionMap lastAM = am;
    while (am != null && !(am instanceof KeymapActionMap)) {
      lastAM = am;
      am = am.getParent();
    }
    if (am != null) {
      if (newKm == null) {
        lastAM.setParent(lastAM != am ? am.getParent() : null);
      } else {
        javax.swing.ActionMap newAM = new KeymapActionMap(newKm);
        lastAM.setParent(newAM);
        if (lastAM != am) {
          newAM.setParent(am.getParent());
        }
      }
    } else if (newKm != null) {
      am = getActionMap();
      if (am != null) {
        javax.swing.ActionMap newAM = new KeymapActionMap(newKm);
        newAM.setParent(am.getParent());
        am.setParent(newAM);
      }
    }
  }

  static class KeymapWrapper extends javax.swing.InputMap {
    static final Object DefaultActionKey = new Object();
    private final Keymap keymap;

    KeymapWrapper(Keymap keymap) { this.keymap = keymap; }

    public KeyStroke[] keys() {
      KeyStroke[] sKeys = super.keys();
      KeyStroke[] keymapKeys = keymap.getBoundKeyStrokes();
      int sCount = sKeys == null ? 0 : sKeys.length;
      int keymapCount = keymapKeys == null ? 0 : keymapKeys.length;
      if (sCount == 0) {
        return keymapKeys;
      }
      if (keymapCount == 0) {
        return sKeys;
      }
      KeyStroke[] all = new KeyStroke[sCount + keymapCount];
      System.arraycopy(sKeys, 0, all, 0, sCount);
      System.arraycopy(keymapKeys, 0, all, sCount, keymapCount);
      return all;
    }

    public int size() {
      KeyStroke[] keymapStrokes = keymap.getBoundKeyStrokes();
      return super.size() + (keymapStrokes == null ? 0 : keymapStrokes.length);
    }

    public Object get(KeyStroke keyStroke) {
      Object value = keymap.getAction(keyStroke);
      if (value == null) {
        value = super.get(keyStroke);
        if (value == null && keyStroke.getKeyChar() != java.awt.event.KeyEvent.CHAR_UNDEFINED
            && keymap.getDefaultAction() != null) {
          value = DefaultActionKey;
        }
      }
      return value;
    }
  }

  static class KeymapActionMap extends javax.swing.ActionMap {
    private final Keymap keymap;

    KeymapActionMap(Keymap keymap) { this.keymap = keymap; }

    public Object[] keys() {
      Object[] sKeys = super.keys();
      Object[] keymapKeys = keymap.getBoundActions();
      int sCount = sKeys == null ? 0 : sKeys.length;
      int keymapCount = keymapKeys == null ? 0 : keymapKeys.length;
      boolean hasDefault = keymap.getDefaultAction() != null;
      if (hasDefault) {
        keymapCount++;
      }
      if (sCount == 0) {
        if (hasDefault) {
          Object[] all = new Object[keymapCount];
          if (keymapCount > 1) {
            System.arraycopy(keymapKeys, 0, all, 0, keymapCount - 1);
          }
          all[keymapCount - 1] = KeymapWrapper.DefaultActionKey;
          return all;
        }
        return keymapKeys;
      }
      if (keymapCount == 0) {
        return sKeys;
      }
      Object[] all = new Object[sCount + keymapCount];
      System.arraycopy(sKeys, 0, all, 0, sCount);
      if (hasDefault) {
        if (keymapCount > 1) {
          System.arraycopy(keymapKeys, 0, all, sCount, keymapCount - 1);
        }
        all[sCount + keymapCount - 1] = KeymapWrapper.DefaultActionKey;
      } else {
        System.arraycopy(keymapKeys, 0, all, sCount, keymapCount);
      }
      return all;
    }

    public int size() {
      Object[] actions = keymap.getBoundActions();
      int keymapCount = actions == null ? 0 : actions.length;
      if (keymap.getDefaultAction() != null) {
        keymapCount++;
      }
      return super.size() + keymapCount;
    }

    public Action get(Object key) {
      Action value = super.get(key);
      if (value == null) {
        if (key == KeymapWrapper.DefaultActionKey) {
          value = keymap.getDefaultAction();
        } else if (key instanceof Action) {
          value = (Action) key;
        }
      }
      return value;
    }
  }
  public boolean getDragEnabled() { return bp_DragEnabled; }
  public final void setDropMode(javax.swing.DropMode p0) { javax.swing.DropMode old = bp_DropMode; bp_DropMode = p0; firePropertyChange("dropMode", old, p0); }
  public final javax.swing.DropMode getDropMode() { return bp_DropMode; }
  public final javax.swing.text.JTextComponent.DropLocation getDropLocation() { return bp_DropLocation; }
  public static javax.swing.text.Keymap removeKeymap(java.lang.String p0) {
    return keymapTable.remove(p0);
  }
  public void cut() {
    if (isEditable() && isEnabled() && getSelectedText() != null) {
      copy();
      replaceSelection(null);
    }
  }
  public void paste() {
    if (isEditable() && isEnabled()) {
      try {
        java.awt.datatransfer.Transferable t = clipboard().getContents(null);
        if (t != null && t.isDataFlavorSupported(java.awt.datatransfer.DataFlavor.stringFlavor)) {
          replaceSelection((String) t.getTransferData(java.awt.datatransfer.DataFlavor.stringFlavor));
        }
      } catch (Exception e) {
        UIManager.getLookAndFeel().provideErrorFeedback(this);
      }
    }
  }
  public void setText(java.lang.String p0) {
    try {
      Document doc = getDocument();
      if (doc instanceof AbstractDocument) {
        ((AbstractDocument) doc).replace(0, doc.getLength(), p0, null);
      } else {
        doc.remove(0, doc.getLength());
        doc.insertString(0, p0, null);
      }
    } catch (BadLocationException e) {
      UIManager.getLookAndFeel().provideErrorFeedback(JTextComponent.this);
    }
  }
  public int getSelectionStart() {
    return Math.min(bp_Caret.getDot(), bp_Caret.getMark());
  }
  public int getSelectionEnd() {
    return Math.max(bp_Caret.getDot(), bp_Caret.getMark());
  }
  public void setEditable(boolean p0) {
    if (p0 != bp_Editable) {
      boolean oldVal = bp_Editable;
      bp_Editable = p0;
      enableInputMethods(p0);
      firePropertyChange("editable", Boolean.valueOf(oldVal), Boolean.valueOf(bp_Editable));
      repaint();
    }
  }
  public void setCaretPosition(int p0) {
    Document doc = getDocument();
    if (doc != null) {
      if (p0 > doc.getLength() || p0 < 0) {
        throw new IllegalArgumentException("bad position: " + p0);
      }
      bp_Caret.setDot(p0);
    }
  }
  public int getCaretPosition() {
    return bp_Caret.getDot();
  }
  public boolean isEditable() { return bp_Editable; }
  public void setSelectionStart(int p0) {
    select(p0, getSelectionEnd());
  }
  public void setSelectionEnd(int p0) {
    select(getSelectionStart(), p0);
  }
  public void selectAll() {
    Document doc = getDocument();
    if (doc != null) {
      setCaretPosition(0);
      moveCaretPosition(doc.getLength());
    }
  }
  public javax.swing.Action[] getActions() {
    TextUI ui = getUI();
    if (ui != null) {
      return ui.getEditorKit(this).getActions();
    }
    return new DefaultEditorKit().getActions();
  }
  public javax.accessibility.AccessibleContext getAccessibleContext() {
    if (accessibleContext == null) {
      accessibleContext = new AccessibleJTextComponent();
    }
    return accessibleContext;
  }
  public void removeNotify() {
    super.removeNotify();
  }
  protected void processInputMethodEvent(java.awt.event.InputMethodEvent p0) {
    super.processInputMethodEvent(p0);
  }
  public void addInputMethodListener(java.awt.event.InputMethodListener p0) { if (p0 != null) { bl_InputMethodListener.add(p0); } }
  public void setComponentOrientation(java.awt.ComponentOrientation p0) {
    super.setComponentOrientation(p0);
  }
  public java.awt.im.InputMethodRequests getInputMethodRequests() { return bp_InputMethodRequests; }
  public java.lang.String getSelectedText() {
    String txt = null;
    int p0 = Math.min(bp_Caret.getDot(), bp_Caret.getMark());
    int p1 = Math.max(bp_Caret.getDot(), bp_Caret.getMark());
    if (p0 != p1) {
      try {
        Document doc = getDocument();
        txt = doc.getText(p0, p1 - p0);
      } catch (BadLocationException e) {
        throw new IllegalArgumentException(e.getMessage());
      }
    }
    return txt;
  }
  public javax.swing.text.Caret getCaret() { return bp_Caret; }
  public void select(int p0, int p1) {
    int docEnd = getDocument().getLength();
    if (p0 < 0) {
      p0 = 0;
    }
    if (p0 > docEnd) {
      p0 = docEnd;
    }
    if (p1 > docEnd) {
      p1 = docEnd;
    }
    if (p1 < p0) {
      p1 = p0;
    }
    setCaretPosition(p0);
    moveCaretPosition(p1);
  }
  public void updateUI() {
    setUI((TextUI) UIManager.getUI(this));
    invalidate();
  }
  public void setUI(javax.swing.plaf.TextUI p0) { super.setUI(p0); }
  public javax.swing.plaf.TextUI getUI() { return (javax.swing.plaf.TextUI) ui; }
  public void setMargin(java.awt.Insets p0) { java.awt.Insets old = bp_Margin; bp_Margin = p0; firePropertyChange("margin", old, p0); }
  public java.awt.Insets getMargin() { return bp_Margin; }
  public java.lang.String getToolTipText(java.awt.event.MouseEvent p0) {
    String retValue = super.getToolTipText(p0);
    if (retValue == null) {
      TextUI ui = getUI();
      if (ui != null) {
        retValue = ui.getToolTipText(this, new Point(p0.getX(), p0.getY()));
      }
    }
    return retValue;
  }

  public class AccessibleJTextComponent extends javax.swing.JComponent.AccessibleJComponent
      implements javax.accessibility.AccessibleText, javax.swing.event.CaretListener,
      javax.swing.event.DocumentListener, javax.accessibility.AccessibleAction,
      javax.accessibility.AccessibleEditableText, javax.accessibility.AccessibleExtendedText {
    int caretPos;
    Point oldLocationOnScreen;

    public AccessibleJTextComponent() {
      Document doc = JTextComponent.this.getDocument();
      if (doc != null) {
        doc.addDocumentListener(this);
      }
      JTextComponent.this.addCaretListener(this);
      caretPos = getCaretPosition();
    }

    public void caretUpdate(javax.swing.event.CaretEvent e) {
      int dot = e.getDot();
      int mark = e.getMark();
      if (caretPos != dot) {
        firePropertyChange(ACCESSIBLE_CARET_PROPERTY, Integer.valueOf(caretPos), Integer.valueOf(dot));
        caretPos = dot;
      }
      if (mark != dot) {
        firePropertyChange(ACCESSIBLE_SELECTION_PROPERTY, null, getSelectedText());
      }
    }

    public void insertUpdate(javax.swing.event.DocumentEvent e) {
      Integer pos = Integer.valueOf(e.getOffset());
      if (SwingUtilities.isEventDispatchThread()) {
        firePropertyChange(ACCESSIBLE_TEXT_PROPERTY, null, pos);
      } else {
        final Integer p = pos;
        SwingUtilities.invokeLater(new Runnable() {
          public void run() { firePropertyChange(ACCESSIBLE_TEXT_PROPERTY, null, p); }
        });
      }
    }

    public void removeUpdate(javax.swing.event.DocumentEvent e) {
      final Integer pos = Integer.valueOf(e.getOffset());
      if (SwingUtilities.isEventDispatchThread()) {
        firePropertyChange(ACCESSIBLE_TEXT_PROPERTY, null, pos);
      } else {
        SwingUtilities.invokeLater(new Runnable() {
          public void run() { firePropertyChange(ACCESSIBLE_TEXT_PROPERTY, null, pos); }
        });
      }
    }

    public void changedUpdate(javax.swing.event.DocumentEvent e) {
      final Integer pos = Integer.valueOf(e.getOffset());
      if (SwingUtilities.isEventDispatchThread()) {
        firePropertyChange(ACCESSIBLE_TEXT_PROPERTY, null, pos);
      } else {
        SwingUtilities.invokeLater(new Runnable() {
          public void run() { firePropertyChange(ACCESSIBLE_TEXT_PROPERTY, null, pos); }
        });
      }
    }

    public javax.accessibility.AccessibleStateSet getAccessibleStateSet() {
      javax.accessibility.AccessibleStateSet states = super.getAccessibleStateSet();
      if (JTextComponent.this.isEditable()) {
        states.add(javax.accessibility.AccessibleState.EDITABLE);
      }
      return states;
    }

    public javax.accessibility.AccessibleRole getAccessibleRole() { return javax.accessibility.AccessibleRole.TEXT; }

    public javax.accessibility.AccessibleAction getAccessibleAction() { return this; }

    public javax.accessibility.AccessibleText getAccessibleText() { return this; }

    public javax.accessibility.AccessibleEditableText getAccessibleEditableText() { return this; }

    public int getIndexAtPoint(Point p) {
      if (p == null) {
        return -1;
      }
      return JTextComponent.this.viewToModel(p);
    }

    Rectangle getRootEditorRect() {
      Rectangle alloc = JTextComponent.this.getBounds();
      if (alloc.width > 0 && alloc.height > 0) {
        alloc.x = alloc.y = 0;
        Insets insets = JTextComponent.this.getInsets();
        alloc.x += insets.left;
        alloc.y += insets.top;
        alloc.width -= insets.left + insets.right;
        alloc.height -= insets.top + insets.bottom;
        return alloc;
      }
      return null;
    }

    public Rectangle getCharacterBounds(int i) {
      if (i < 0 || i > getDocument().getLength() - 1) {
        return null;
      }
      javax.swing.plaf.TextUI ui = getUI();
      if (ui == null) {
        return null;
      }
      try {
        Rectangle rect = ui.modelToView(JTextComponent.this, i, Position.Bias.Forward);
        Rectangle next = ui.modelToView(JTextComponent.this, i + 1, Position.Bias.Forward);
        if (rect != null && next != null && next.y == rect.y && next.x > rect.x) {
          rect.width = next.x - rect.x;
        }
        return rect;
      } catch (BadLocationException e) {
        return null;
      }
    }

    public int getCharCount() { return getDocument().getLength(); }

    public int getCaretPosition() { return JTextComponent.this.getCaretPosition(); }

    public AttributeSet getCharacterAttribute(int i) {
      Element e = null;
      if (getDocument() instanceof AbstractDocument) {
        ((AbstractDocument) getDocument()).readLock();
      }
      try {
        for (e = getDocument().getDefaultRootElement(); !e.isLeaf();) {
          int index = e.getElementIndex(i);
          e = e.getElement(index);
        }
      } finally {
        if (getDocument() instanceof AbstractDocument) {
          ((AbstractDocument) getDocument()).readUnlock();
        }
      }
      return e.getAttributes();
    }

    public int getSelectionStart() { return JTextComponent.this.getSelectionStart(); }

    public int getSelectionEnd() { return JTextComponent.this.getSelectionEnd(); }

    public String getSelectedText() { return JTextComponent.this.getSelectedText(); }

    private static final boolean NEXT = true;
    private static final boolean PREVIOUS = false;

    private String segmentText(int p0, int p1) {
      try {
        return getDocument().getText(p0, p1 - p0);
      } catch (BadLocationException e) {
        return null;
      }
    }

    private int[] bounds(int part, int index, int dir) {
      int len = getDocument().getLength();
      if (index < 0 || index >= len && !(part == AccessibleText.CHARACTER && false)) {
        return null;
      }
      switch (part) {
        case AccessibleText.CHARACTER: {
          int i = index + dir;
          return i < 0 || i >= len ? null : new int[] {i, i + 1};
        }
        case AccessibleText.WORD:
        case AccessibleText.SENTENCE: {
          java.text.BreakIterator it = part == AccessibleText.WORD ? java.text.BreakIterator.getWordInstance(getLocale())
              : java.text.BreakIterator.getSentenceInstance(getLocale());
          String text = segmentText(0, len);
          if (text == null) {
            return null;
          }
          it.setText(text);
          int start = it.isBoundary(index) ? index : it.preceding(index);
          if (start == java.text.BreakIterator.DONE) {
            start = 0;
          }
          int end = it.following(start);
          if (end == java.text.BreakIterator.DONE) {
            end = len;
          }
          if (dir == 0) {
            return new int[] {start, end};
          }
          if (dir > 0) {
            int s = end;
            int e = it.following(s);
            return s >= len ? null : new int[] {s, e == java.text.BreakIterator.DONE ? len : e};
          }
          if (start == 0) {
            return null;
          }
          int e = start;
          int s = it.preceding(e);
          return new int[] {s == java.text.BreakIterator.DONE ? 0 : s, e};
        }
        default:
          return null;
      }
    }

    private String textAt(int part, int index, int dir) {
      int[] b = bounds(part, index, dir);
      return b == null ? null : segmentText(b[0], b[1]);
    }

    public String getAtIndex(int part, int index) { return textAt(part, index, 0); }

    public String getAfterIndex(int part, int index) { return textAt(part, index, 1); }

    public String getBeforeIndex(int part, int index) { return textAt(part, index, -1); }

    private javax.accessibility.AccessibleTextSequence sequence(int part, int index, int dir) {
      int[] b = bounds(part, index, dir);
      if (b == null) {
        return null;
      }
      return new javax.accessibility.AccessibleTextSequence(b[0], b[1], segmentText(b[0], b[1]));
    }

    public javax.accessibility.AccessibleTextSequence getTextSequenceAt(int part, int index) {
      return sequence(part, index, 0);
    }

    public javax.accessibility.AccessibleTextSequence getTextSequenceAfter(int part, int index) {
      return sequence(part, index, 1);
    }

    public javax.accessibility.AccessibleTextSequence getTextSequenceBefore(int part, int index) {
      return sequence(part, index, -1);
    }

    public Rectangle getTextBounds(int startIndex, int endIndex) {
      if (startIndex < 0 || startIndex > getDocument().getLength() - 1 || endIndex < 0 || endIndex > getDocument().getLength() - 1
          || startIndex > endIndex) {
        return null;
      }
      javax.swing.plaf.TextUI ui = getUI();
      if (ui == null) {
        return null;
      }
      try {
        Rectangle rect = ui.modelToView(JTextComponent.this, startIndex, Position.Bias.Forward);
        Rectangle end = ui.modelToView(JTextComponent.this, endIndex, Position.Bias.Forward);
        return rect == null || end == null ? null : rect.union(end);
      } catch (BadLocationException e) {
        return null;
      }
    }

    public void setTextContents(String s) { JTextComponent.this.setText(s); }

    public void insertTextAtIndex(int index, String s) {
      Document doc = JTextComponent.this.getDocument();
      if (doc != null) {
        try {
          if (s != null && s.length() > 0) {
            doc.insertString(index, s, null);
          }
        } catch (BadLocationException e) {
          UIManager.getLookAndFeel().provideErrorFeedback(JTextComponent.this);
        }
      }
    }

    public String getTextRange(int startIndex, int endIndex) {
      String txt = null;
      int p0 = Math.min(startIndex, endIndex);
      int p1 = Math.max(startIndex, endIndex);
      if (p0 != p1) {
        try {
          txt = JTextComponent.this.getDocument().getText(p0, p1 - p0);
        } catch (BadLocationException e) {
          throw new IllegalArgumentException(e.getMessage());
        }
      }
      return txt;
    }

    public void delete(int startIndex, int endIndex) {
      if (isEditable() && isEnabled()) {
        try {
          int p0 = Math.min(startIndex, endIndex);
          int p1 = Math.max(startIndex, endIndex);
          if (p0 != p1) {
            Document doc = getDocument();
            doc.remove(p0, p1 - p0);
          }
        } catch (BadLocationException e) {
          UIManager.getLookAndFeel().provideErrorFeedback(JTextComponent.this);
        }
      } else {
        UIManager.getLookAndFeel().provideErrorFeedback(JTextComponent.this);
      }
    }

    public void cut(int startIndex, int endIndex) {
      selectText(startIndex, endIndex);
      JTextComponent.this.cut();
    }

    public void paste(int startIndex) {
      setCaretPosition(startIndex);
      JTextComponent.this.paste();
    }

    public void replaceText(int startIndex, int endIndex, String s) {
      selectText(startIndex, endIndex);
      JTextComponent.this.replaceSelection(s);
    }

    public void selectText(int startIndex, int endIndex) {
      if (isEditable() && isEnabled()) {
        try {
          int length = getDocument().getLength();
          startIndex = Math.max(0, Math.min(startIndex, length));
          endIndex = Math.max(0, Math.min(endIndex, length));
          setCaretPosition(startIndex);
          moveCaretPosition(endIndex);
        } catch (Exception e) {
          UIManager.getLookAndFeel().provideErrorFeedback(JTextComponent.this);
        }
      }
    }

    public void setAttributes(int startIndex, int endIndex, AttributeSet as) {
      Document doc = JTextComponent.this.getDocument();
      if (doc != null && doc instanceof StyledDocument) {
        StyledDocument sDoc = (StyledDocument) doc;
        int offset = startIndex;
        int length = endIndex - startIndex;
        sDoc.setCharacterAttributes(offset, length, as, true);
      }
    }

    public int getAccessibleActionCount() { return JTextComponent.this.getActions().length; }

    public String getAccessibleActionDescription(int i) {
      Action[] actions = JTextComponent.this.getActions();
      if (i < 0 || i >= actions.length) {
        return null;
      }
      return (String) actions[i].getValue(Action.NAME);
    }

    public boolean doAccessibleAction(int i) {
      Action[] actions = JTextComponent.this.getActions();
      if (i < 0 || i >= actions.length) {
        return false;
      }
      ActionEvent ae = new ActionEvent(JTextComponent.this, ActionEvent.ACTION_PERFORMED, null,
          EventQueue.getMostRecentEventTime(), 0);
      actions[i].actionPerformed(ae);
      return true;
    }
  }

  public static final class DropLocation extends javax.swing.TransferHandler.DropLocation {
    DropLocation() {
      super((java.awt.Point) null);
    }
    public java.lang.String toString() { throw new UnsupportedOperationException(); }
    public int getIndex() { throw new UnsupportedOperationException(); }
    public javax.swing.text.Position.Bias getBias() { throw new UnsupportedOperationException(); }
  }

  public static class KeyBinding {
    public javax.swing.KeyStroke key;
    public java.lang.String actionName;
    public KeyBinding(javax.swing.KeyStroke p0, java.lang.String p1) {
    }
  }

  private static java.awt.datatransfer.Clipboard localClipboard;

  private java.awt.datatransfer.Clipboard clipboard() {
    try {
      return getToolkit().getSystemClipboard();
    } catch (java.awt.HeadlessException | SecurityException e) {
      synchronized (JTextComponent.class) {
        if (localClipboard == null) {
          localClipboard = new java.awt.datatransfer.Clipboard("Sandboxed Text Component Clipboard");
        }
        return localClipboard;
      }
    }
  }

  static JTextComponent getFocusedComponent() {
    java.awt.Component c = java.awt.KeyboardFocusManager.getCurrentKeyboardFocusManager().getPermanentFocusOwner();
    return c instanceof JTextComponent ? (JTextComponent) c : null;
  }

  private static class MutableCaretEvent extends CaretEvent implements javax.swing.event.ChangeListener,
      DocumentListener {
    MutableCaretEvent(JTextComponent c) { super(c); }

    public int getDot() { return ((JTextComponent) getSource()).getCaret().getDot(); }

    public int getMark() { return ((JTextComponent) getSource()).getCaret().getMark(); }

    public void stateChanged(javax.swing.event.ChangeEvent e) {
      JTextComponent c = (JTextComponent) getSource();
      c.fireCaretUpdate(this);
    }

    public void insertUpdate(DocumentEvent e) { }

    public void removeUpdate(DocumentEvent e) { }

    public void changedUpdate(DocumentEvent e) { }
  }

}
