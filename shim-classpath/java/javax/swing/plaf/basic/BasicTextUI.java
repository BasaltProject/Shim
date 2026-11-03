/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */
package javax.swing.plaf.basic;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JTextArea;
import javax.swing.LookAndFeel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.ActionMapUIResource;
import javax.swing.plaf.InputMapUIResource;
import javax.swing.plaf.TextUI;
import javax.swing.plaf.UIResource;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.DefaultCaret;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Document;
import javax.swing.text.EditorKit;
import javax.swing.text.Element;
import javax.swing.text.Highlighter;
import javax.swing.text.JTextComponent;
import javax.swing.text.Keymap;
import javax.swing.text.Position;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;

public abstract class BasicTextUI extends TextUI implements ViewFactory {
  private static final EditorKit defaultKit = new DefaultEditorKit();
  private JTextComponent editor;
  private RootView rootView = new RootView();
  private boolean painted;
  private final UpdateHandler updateHandler = new UpdateHandler();
  private boolean i18nView;
  private int caretMargin = 1;

  public BasicTextUI() {
    painted = false;
  }

  protected Caret createCaret() { return new BasicCaret(); }

  protected Highlighter createHighlighter() { return new BasicHighlighter(); }

  protected String getKeymapName() {
    String nm = getClass().getName();
    int index = nm.lastIndexOf('.');
    if (index >= 0) {
      nm = nm.substring(index + 1);
    }
    return nm;
  }

  protected Keymap createKeymap() {
    String nm = getKeymapName();
    Keymap map = JTextComponent.getKeymap(nm);
    if (map == null) {
      Keymap parent = JTextComponent.getKeymap(JTextComponent.DEFAULT_KEYMAP);
      map = JTextComponent.addKeymap(nm, parent);
      String prefix = getPropertyPrefix();
      Object o = UIManager.get(prefix + ".keyBindings");
      if (o instanceof JTextComponent.KeyBinding[]) {
        JTextComponent.loadKeymap(map, (JTextComponent.KeyBinding[]) o, getComponent().getActions());
      }
    }
    return map;
  }

  protected void installDefaults() {
    String prefix = getPropertyPrefix();
    Font f = editor.getFont();
    if ((f == null) || (f instanceof UIResource)) {
      editor.setFont(UIManager.getFont(prefix + ".font"));
    }
    Color bg = editor.getBackground();
    if ((bg == null) || (bg instanceof UIResource)) {
      editor.setBackground(UIManager.getColor(prefix + ".background"));
    }
    Color fg = editor.getForeground();
    if ((fg == null) || (fg instanceof UIResource)) {
      editor.setForeground(UIManager.getColor(prefix + ".foreground"));
    }
    Color color = editor.getCaretColor();
    if ((color == null) || (color instanceof UIResource)) {
      editor.setCaretColor(UIManager.getColor(prefix + ".caretForeground"));
    }
    Color s = editor.getSelectionColor();
    if ((s == null) || (s instanceof UIResource)) {
      editor.setSelectionColor(UIManager.getColor(prefix + ".selectionBackground"));
    }
    s = editor.getSelectedTextColor();
    if ((s == null) || (s instanceof UIResource)) {
      editor.setSelectedTextColor(UIManager.getColor(prefix + ".selectionForeground"));
    }
    s = editor.getDisabledTextColor();
    if ((s == null) || (s instanceof UIResource)) {
      editor.setDisabledTextColor(UIManager.getColor(prefix + ".inactiveForeground"));
    }
    Border b = editor.getBorder();
    if ((b == null) || (b instanceof UIResource)) {
      editor.setBorder(UIManager.getBorder(prefix + ".border"));
    }
    java.awt.Insets margin = editor.getMargin();
    if (margin == null || margin instanceof UIResource) {
      margin = UIManager.getInsets(prefix + ".margin");
      editor.setMargin(margin);
    }
    Caret caret = editor.getCaret();
    if (caret == null || caret instanceof UIResource) {
      caret = createCaret();
      editor.setCaret(caret);
      int rate = UIManager.getInt(prefix + ".caretBlinkRate");
      caret.setBlinkRate(UIManager.get(prefix + ".caretBlinkRate") == null ? 500 : rate);
    }
    Highlighter highlighter = editor.getHighlighter();
    if (highlighter == null || highlighter instanceof UIResource) {
      editor.setHighlighter(createHighlighter());
    }
    LookAndFeel.installProperty(editor, "opaque", Boolean.TRUE);
    LookAndFeel.installProperty(editor, "autoscrolls", Boolean.TRUE);
  }

  private void installDefaults2() { }

  protected void uninstallDefaults() {
    editor.putClientProperty("caretWidth", null);
    if (editor.getCaretColor() instanceof UIResource) {
      editor.setCaretColor(null);
    }
    if (editor.getSelectionColor() instanceof UIResource) {
      editor.setSelectionColor(null);
    }
    if (editor.getDisabledTextColor() instanceof UIResource) {
      editor.setDisabledTextColor(null);
    }
    if (editor.getSelectedTextColor() instanceof UIResource) {
      editor.setSelectedTextColor(null);
    }
    if (editor.getBorder() instanceof UIResource) {
      editor.setBorder(null);
    }
    if (editor.getMargin() instanceof UIResource) {
      editor.setMargin(null);
    }
    if (editor.getCaret() instanceof UIResource) {
      editor.setCaret(null);
    }
    if (editor.getHighlighter() instanceof UIResource) {
      editor.setHighlighter(null);
    }
    if (editor.getActionMap() != null) {
      editor.getActionMap().remove("requestFocus");
    }
  }

  protected void installListeners() { }

  protected void uninstallListeners() { }

  protected void installKeyboardActions() {
    editor.setKeymap(createKeymap());
    InputMap km = getInputMap();
    if (km != null) {
      SwingUtilities.replaceUIInputMap(editor, JComponent.WHEN_FOCUSED, km);
    }
    ActionMap map = getActionMap();
    if (map != null) {
      SwingUtilities.replaceUIActionMap(editor, map);
    }
  }

  InputMap getInputMap() {
    InputMap map = new InputMapUIResource();
    Object shared = UIManager.get(getPropertyPrefix() + ".focusInputMap");
    if (shared instanceof InputMap) {
      map.setParent((InputMap) shared);
    }
    return map;
  }

  ActionMap getActionMap() {
    String mapName = getPropertyPrefix() + ".actionMap";
    ActionMap map = (ActionMap) UIManager.get(mapName);
    if (map == null) {
      map = createActionMap();
      if (map != null) {
        UIManager.getLookAndFeelDefaults().put(mapName, map);
      }
    }
    ActionMap componentMap = new ActionMapUIResource();
    componentMap.put("requestFocus", new FocusAction());
    if (getEditorKit(editor) instanceof DefaultEditorKit) {
      if (map != null) {
        componentMap.setParent(map);
      }
    }
    return componentMap;
  }

  ActionMap createActionMap() {
    ActionMap map = new ActionMapUIResource();
    Action[] actions = editor.getActions();
    for (int i = 0; i < actions.length; i++) {
      Action a = actions[i];
      map.put(a.getValue(Action.NAME), a);
    }
    map.put(javax.swing.TransferHandler.getCutAction().getValue(Action.NAME), javax.swing.TransferHandler.getCutAction());
    map.put(javax.swing.TransferHandler.getCopyAction().getValue(Action.NAME),
        javax.swing.TransferHandler.getCopyAction());
    map.put(javax.swing.TransferHandler.getPasteAction().getValue(Action.NAME),
        javax.swing.TransferHandler.getPasteAction());
    return map;
  }

  class FocusAction extends javax.swing.AbstractAction {
    public void actionPerformed(java.awt.event.ActionEvent e) { editor.requestFocus(); }

    public boolean isEnabled() { return editor.isEditable(); }
  }

  protected void uninstallKeyboardActions() {
    editor.setKeymap(null);
    SwingUtilities.replaceUIInputMap(editor, JComponent.WHEN_IN_FOCUSED_WINDOW, null);
    SwingUtilities.replaceUIActionMap(editor, null);
  }

  protected abstract String getPropertyPrefix();

  protected void paintBackground(Graphics g) {
    g.setColor(editor.getBackground());
    g.fillRect(0, 0, editor.getWidth(), editor.getHeight());
  }

  protected final JTextComponent getComponent() { return editor; }

  protected void modelChanged() {
    ViewFactory f = rootView.getViewFactory();
    Document doc = editor.getDocument();
    Element elem = doc.getDefaultRootElement();
    setView(f.create(elem));
  }

  protected final void setView(View v) {
    rootView.setView(v);
    painted = false;
    editor.revalidate();
    editor.repaint();
  }

  protected void paintSafely(Graphics g) {
    painted = true;
    Highlighter highlighter = editor.getHighlighter();
    Caret caret = editor.getCaret();
    if (editor.isOpaque()) {
      paintBackground(g);
    }
    if (highlighter != null) {
      highlighter.paint(g);
    }
    Rectangle alloc = getVisibleEditorRect();
    if (alloc != null) {
      rootView.paint(g, alloc);
    }
    if (caret != null) {
      caret.paint(g);
    }
  }

  public void installUI(JComponent c) {
    if (c instanceof JTextComponent) {
      editor = (JTextComponent) c;
      Object cw = UIManager.get("Caret.width");
      caretMargin = cw instanceof Number ? ((Number) cw).intValue() : 1;
      installDefaults();
      installDefaults2();
      editor.addPropertyChangeListener(updateHandler);
      installListeners();
      installKeyboardActions();
      Document doc = editor.getDocument();
      if (doc == null) {
        editor.setDocument(getEditorKit(editor).createDefaultDocument());
      } else {
        doc.addDocumentListener(updateHandler);
        modelChanged();
      }
      updateBackground(editor);
    } else {
      throw new Error("TextUI needs JTextComponent");
    }
  }

  public void uninstallUI(JComponent c) {
    editor.removePropertyChangeListener(updateHandler);
    editor.getDocument().removeDocumentListener(updateHandler);
    painted = false;
    uninstallDefaults();
    rootView.setView(null);
    c.removeAll();
    uninstallKeyboardActions();
    uninstallListeners();
    editor = null;
  }

  public void update(Graphics g, JComponent c) {
    paint(g, c);
  }

  public final void paint(Graphics g, JComponent c) {
    if ((rootView.getViewCount() > 0) && (rootView.getView(0) != null)) {
      Document doc = editor.getDocument();
      if (doc instanceof AbstractDocument) {
        ((AbstractDocument) doc).readLock();
      }
      try {
        paintSafely(g);
      } finally {
        if (doc instanceof AbstractDocument) {
          ((AbstractDocument) doc).readUnlock();
        }
      }
    }
  }

  public Dimension getPreferredSize(JComponent c) {
    Document doc = editor.getDocument();
    Insets i = c.getInsets();
    Dimension d = c.getSize();
    if (doc instanceof AbstractDocument) {
      ((AbstractDocument) doc).readLock();
    }
    try {
      if ((d.width > (i.left + i.right + caretMargin)) && (d.height > (i.top + i.bottom))) {
        rootView.setSize(d.width - i.left - i.right - caretMargin, d.height - i.top - i.bottom);
      } else if (d.width == 0 && d.height == 0) {
        rootView.setSize(Integer.MAX_VALUE, Integer.MAX_VALUE);
      }
      d.width = (int) Math.min((long) rootView.getPreferredSpan(View.X_AXIS) + (long) i.left + (long) i.right
          + caretMargin, Integer.MAX_VALUE);
      d.height = (int) Math.min((long) rootView.getPreferredSpan(View.Y_AXIS) + (long) i.top + (long) i.bottom,
          Integer.MAX_VALUE);
    } finally {
      if (doc instanceof AbstractDocument) {
        ((AbstractDocument) doc).readUnlock();
      }
    }
    return d;
  }


  public Dimension getMinimumSize(JComponent c) {
    Document doc = editor.getDocument();
    Insets i = c.getInsets();
    Dimension d = new Dimension();
    if (doc instanceof AbstractDocument) {
      ((AbstractDocument) doc).readLock();
    }
    try {
      d.width = (int) rootView.getMinimumSpan(View.X_AXIS) + i.left + i.right + caretMargin;
      d.height = (int) rootView.getMinimumSpan(View.Y_AXIS) + i.top + i.bottom;
    } finally {
      if (doc instanceof AbstractDocument) {
        ((AbstractDocument) doc).readUnlock();
      }
    }
    return d;
  }

  public Dimension getMaximumSize(JComponent c) {
    return new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
  }

  protected Rectangle getVisibleEditorRect() {
    Rectangle alloc = editor.getBounds();
    if ((alloc.width > 0) && (alloc.height > 0)) {
      alloc.x = alloc.y = 0;
      Insets insets = editor.getInsets();
      alloc.x += insets.left;
      alloc.y += insets.top;
      alloc.width -= insets.left + insets.right + caretMargin;
      alloc.height -= insets.top + insets.bottom;
      return alloc;
    }
    return null;
  }

  public Rectangle modelToView(JTextComponent tc, int pos) throws BadLocationException {
    return modelToView(tc, pos, Position.Bias.Forward);
  }

  public Rectangle modelToView(JTextComponent tc, int pos, Position.Bias bias) throws BadLocationException {
    Rectangle2D r = modelToView2D(tc, pos, bias);
    return r == null ? null : r.getBounds();
  }

  public Rectangle2D modelToView2D(JTextComponent tc, int pos, Position.Bias bias) throws BadLocationException {
    Document doc = editor.getDocument();
    if (doc instanceof AbstractDocument) {
      ((AbstractDocument) doc).readLock();
    }
    try {
      Rectangle alloc = getVisibleEditorRect();
      if (alloc != null) {
        rootView.setSize(alloc.width, alloc.height);
        Shape s = rootView.modelToView(pos, alloc, bias);
        if (s != null) {
          return s.getBounds2D();
        }
      }
    } finally {
      if (doc instanceof AbstractDocument) {
        ((AbstractDocument) doc).readUnlock();
      }
    }
    return null;
  }

  public int viewToModel(JTextComponent tc, Point pt) {
    return viewToModel(tc, pt, discardBias);
  }

  private static final Position.Bias[] discardBias = new Position.Bias[1];

  public int viewToModel(JTextComponent tc, Point pt, Position.Bias[] biasReturn) {
    return viewToModel2D(tc, pt, biasReturn);
  }

  public int viewToModel2D(JTextComponent tc, Point2D pt, Position.Bias[] biasReturn) {
    int offs = -1;
    Document doc = editor.getDocument();
    if (doc instanceof AbstractDocument) {
      ((AbstractDocument) doc).readLock();
    }
    try {
      Rectangle alloc = getVisibleEditorRect();
      if (alloc != null) {
        rootView.setSize(alloc.width, alloc.height);
        offs = rootView.viewToModel((float) pt.getX(), (float) pt.getY(), alloc, biasReturn);
      }
    } finally {
      if (doc instanceof AbstractDocument) {
        ((AbstractDocument) doc).readUnlock();
      }
    }
    return offs;
  }

  public int getNextVisualPositionFrom(JTextComponent t, int pos, Position.Bias b, int direction,
      Position.Bias[] biasRet) throws BadLocationException {
    Document doc = editor.getDocument();
    if (doc instanceof AbstractDocument) {
      ((AbstractDocument) doc).readLock();
    }
    try {
      if (painted) {
        Rectangle alloc = getVisibleEditorRect();
        if (alloc != null) {
          rootView.setSize(alloc.width, alloc.height);
        }
        return rootView.getNextVisualPositionFrom(pos, b, alloc, direction, biasRet);
      }
    } finally {
      if (doc instanceof AbstractDocument) {
        ((AbstractDocument) doc).readUnlock();
      }
    }
    return -1;
  }

  public void damageRange(JTextComponent t, int p0, int p1) {
    damageRange(t, p0, p1, Position.Bias.Forward, Position.Bias.Backward);
  }

  public void damageRange(JTextComponent t, int p0, int p1, Position.Bias p0Bias, Position.Bias p1Bias) {
    if (painted) {
      Rectangle alloc = getVisibleEditorRect();
      if (alloc != null) {
        Document doc = t.getDocument();
        if (doc instanceof AbstractDocument) {
          ((AbstractDocument) doc).readLock();
        }
        try {
          rootView.setSize(alloc.width, alloc.height);
          Shape toDamage = rootView.modelToView(p0, p0Bias, p1, p1Bias, alloc);
          Rectangle rect = (toDamage instanceof Rectangle) ? (Rectangle) toDamage : toDamage.getBounds();
          editor.repaint(rect.x, rect.y, rect.width, rect.height);
        } catch (BadLocationException e) {
        } finally {
          if (doc instanceof AbstractDocument) {
            ((AbstractDocument) doc).readUnlock();
          }
        }
      }
    }
  }

  public EditorKit getEditorKit(JTextComponent tc) { return defaultKit; }

  public View getRootView(JTextComponent tc) { return rootView; }

  public String getToolTipText(JTextComponent t, Point pt) {
    if (!painted) {
      return null;
    }
    String tt = null;
    Rectangle alloc = getVisibleEditorRect();
    if (alloc != null) {
      Document doc = editor.getDocument();
      if (doc instanceof AbstractDocument) {
        ((AbstractDocument) doc).readLock();
      }
      try {
        tt = rootView.getToolTipText(pt.x, pt.y, alloc);
      } finally {
        if (doc instanceof AbstractDocument) {
          ((AbstractDocument) doc).readUnlock();
        }
      }
    }
    return tt;
  }

  public View create(Element elem) { return null; }

  public View create(Element elem, int p0, int p1) { return null; }

  protected void propertyChange(PropertyChangeEvent evt) { }

  private void updateBackground(JTextComponent c) {
    Color background = c.getBackground();
    if (background instanceof UIResource) {
      String prefix = getPropertyPrefix();
      Color disabledBG = UIManager.getColor(prefix + ".disabledBackground");
      Color inactiveBG = UIManager.getColor(prefix + ".inactiveBackground");
      Color bg = UIManager.getColor(prefix + ".background");
      if ((c instanceof JTextArea || c instanceof JEditorPane) && background != inactiveBG
          && background != disabledBG && background != bg) {
        return;
      }
      Color newColor = null;
      if (!c.isEnabled()) {
        newColor = disabledBG;
      }
      if (newColor == null && !c.isEditable()) {
        newColor = inactiveBG;
      }
      if (newColor == null) {
        newColor = bg;
      }
      if (newColor != null && newColor != background) {
        c.setBackground(newColor);
      }
    }
  }

  class UpdateHandler implements PropertyChangeListener, DocumentListener {
    public final void propertyChange(PropertyChangeEvent evt) {
      Object oldValue = evt.getOldValue();
      Object newValue = evt.getNewValue();
      String propertyName = evt.getPropertyName();
      if ((oldValue instanceof Document) || (newValue instanceof Document)) {
        if (oldValue != null) {
          ((Document) oldValue).removeDocumentListener(this);
          i18nView = false;
        }
        if (newValue != null) {
          ((Document) newValue).addDocumentListener(this);
          if ("document".equals(propertyName)) {
            setView(null);
            BasicTextUI.this.propertyChange(evt);
            modelChanged();
            return;
          }
        }
        modelChanged();
      }
      if ("componentOrientation".equals(propertyName) || "font".equals(propertyName)) {
        modelChanged();
      } else if ("editable".equals(propertyName)) {
        modelChanged();
        updateBackground(editor);
      } else if ("enabled".equals(propertyName)) {
        updateBackground(editor);
      }
      BasicTextUI.this.propertyChange(evt);
    }

    public final void insertUpdate(DocumentEvent e) {
      Document doc = e.getDocument();
      Object o = doc.getProperty("i18n");
      if (o instanceof Boolean) {
        Boolean i18nFlag = (Boolean) o;
        if (i18nFlag.booleanValue() != i18nView) {
          i18nView = i18nFlag.booleanValue();
          modelChanged();
          return;
        }
      }
      Rectangle alloc = painted ? getVisibleEditorRect() : null;
      rootView.insertUpdate(e, alloc, rootView.getViewFactory());
    }

    public final void removeUpdate(DocumentEvent e) {
      Rectangle alloc = painted ? getVisibleEditorRect() : null;
      rootView.removeUpdate(e, alloc, rootView.getViewFactory());
    }

    public final void changedUpdate(DocumentEvent e) {
      Rectangle alloc = painted ? getVisibleEditorRect() : null;
      rootView.changedUpdate(e, alloc, rootView.getViewFactory());
    }
  }

  class RootView extends View {
    private View view;

    RootView() {
      super(null);
    }

    void setView(View v) {
      View oldView = view;
      view = null;
      if (oldView != null) {
        oldView.setParent(null);
      }
      if (v != null) {
        v.setParent(this);
      }
      view = v;
    }

    public AttributeSet getAttributes() { return null; }

    public float getPreferredSpan(int axis) {
      if (view != null) {
        return view.getPreferredSpan(axis);
      }
      return 10;
    }

    public float getMinimumSpan(int axis) {
      if (view != null) {
        return view.getMinimumSpan(axis);
      }
      return 10;
    }

    public float getMaximumSpan(int axis) {
      if (view != null) {
        return view.getMaximumSpan(axis);
      }
      return Integer.MAX_VALUE;
    }

    public void preferenceChanged(View child, boolean width, boolean height) {
      editor.revalidate();
    }

    public float getAlignment(int axis) {
      if (view != null) {
        return view.getAlignment(axis);
      }
      return 0;
    }

    public void paint(Graphics g, Shape allocation) {
      if (view != null) {
        Rectangle alloc = (allocation instanceof Rectangle) ? (Rectangle) allocation : allocation.getBounds();
        setSize(alloc.width, alloc.height);
        view.paint(g, allocation);
      }
    }

    public void setParent(View parent) { throw new Error("Can't set parent on root view"); }

    public int getViewCount() { return 1; }

    public View getView(int n) { return view; }

    public int getViewIndex(int pos, Position.Bias b) { return 0; }

    public Shape getChildAllocation(int index, Shape a) { return a; }

    public Shape modelToView(int pos, Shape a, Position.Bias b) throws BadLocationException {
      if (view != null) {
        return view.modelToView(pos, a, b);
      }
      return null;
    }

    public Shape modelToView(int p0, Position.Bias b0, int p1, Position.Bias b1, Shape a)
        throws BadLocationException {
      if (view != null) {
        return view.modelToView(p0, b0, p1, b1, a);
      }
      return null;
    }

    public int viewToModel(float x, float y, Shape a, Position.Bias[] bias) {
      if (view != null) {
        return view.viewToModel(x, y, a, bias);
      }
      return -1;
    }

    public int getNextVisualPositionFrom(int pos, Position.Bias b, Shape a, int direction,
        Position.Bias[] biasRet) throws BadLocationException {
      if (pos < -1) {
        throw new BadLocationException("invalid position", pos);
      }
      if (view != null) {
        int nextPos = view.getNextVisualPositionFrom(pos, b, a, direction, biasRet);
        if (nextPos != -1) {
          pos = nextPos;
        } else {
          biasRet[0] = b;
        }
      }
      return pos;
    }

    public void insertUpdate(DocumentEvent e, Shape a, ViewFactory f) {
      if (view != null) {
        view.insertUpdate(e, a, f);
      }
    }

    public void removeUpdate(DocumentEvent e, Shape a, ViewFactory f) {
      if (view != null) {
        view.removeUpdate(e, a, f);
      }
    }

    public void changedUpdate(DocumentEvent e, Shape a, ViewFactory f) {
      if (view != null) {
        view.changedUpdate(e, a, f);
      }
    }

    public Document getDocument() { return editor.getDocument(); }

    public int getStartOffset() {
      if (view != null) {
        return view.getStartOffset();
      }
      return getElement().getStartOffset();
    }

    public int getEndOffset() {
      if (view != null) {
        return view.getEndOffset();
      }
      return getElement().getEndOffset();
    }

    public Element getElement() {
      if (view != null) {
        return view.getElement();
      }
      return editor.getDocument().getDefaultRootElement();
    }

    public View breakView(int axis, float len, Shape a) { throw new Error("Can't break root view"); }

    public int getResizeWeight(int axis) {
      if (view != null) {
        return view.getResizeWeight(axis);
      }
      return 0;
    }

    public void setSize(float width, float height) {
      if (view != null) {
        view.setSize(width, height);
      }
    }

    public Container getContainer() { return editor; }

    public ViewFactory getViewFactory() {
      EditorKit kit = getEditorKit(editor);
      ViewFactory f = kit.getViewFactory();
      if (f != null) {
        return f;
      }
      return BasicTextUI.this;
    }
  }

  public static class BasicCaret extends DefaultCaret implements UIResource {
    public BasicCaret() { }
  }

  public static class BasicHighlighter extends DefaultHighlighter implements UIResource {
    public BasicHighlighter() { }
  }
}
