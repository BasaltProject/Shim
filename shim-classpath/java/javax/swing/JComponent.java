/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing;

import javax.accessibility.*;
import java.beans.PropertyChangeListener;

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.util.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.plaf.*;

public abstract class JComponent extends java.awt.Container implements java.io.Serializable, TransferHandler.HasGetTransferHandler {
  private javax.swing.JPopupMenu popupMenu;
  private InputMap focusInputMap;
  private InputMap ancestorInputMap;
  private ComponentInputMap windowInputMap;
  private ActionMap actionMap;
  private TransferHandler transferHandler;
  private boolean doubleBuffered;
  private java.beans.VetoableChangeSupport vetoable;
  private java.util.HashMap<Object, Object> clientProps;
  private Border border;
  private boolean opaqueFlag;
  private String toolTip;
  private float alignX = -1f;
  private float alignY = -1f;
  private boolean requestFocusEnabled = true;
  private boolean autoscrolls;
  private boolean verifyInput = true;
  private boolean inheritsPopup;
  private InputVerifier inputVerifier;
  private final java.util.concurrent.CopyOnWriteArrayList<AncestorListener> ancestorListeners = new java.util.concurrent.CopyOnWriteArrayList<>();
  private static Locale defaultLocale;

  protected javax.swing.plaf.ComponentUI ui;
  protected javax.swing.event.EventListenerList listenerList = new javax.swing.event.EventListenerList();
  public static final int WHEN_FOCUSED = 0;
  public static final int WHEN_ANCESTOR_OF_FOCUSED_COMPONENT = 1;
  public static final int WHEN_IN_FOCUSED_WINDOW = 2;
  public static final int UNDEFINED_CONDITION = -1;
  public static final java.lang.String TOOL_TIP_TEXT_KEY = "ToolTipText";
  public JComponent() {
  }
  public void update(java.awt.Graphics p0) {
    paint(p0);
  }
  public boolean contains(int p0, int p1) {
    return super.contains(p0, p1);
  }
  public java.awt.Rectangle getBounds(java.awt.Rectangle p0) {
    return super.getBounds(p0);
  }
  public java.awt.Point getLocation(java.awt.Point p0) {
    return super.getLocation(p0);
  }
  public void print(java.awt.Graphics p0) {
    paint(p0);
  }
  public java.awt.Dimension getSize(java.awt.Dimension p0) {
    return super.getSize(p0);
  }
  protected void setUIProperty(String propertyName, Object value) {
    if ("opaque".equals(propertyName)) {
      if (!opaqueExplicit) {
        boolean old = opaqueFlag;
        opaqueFlag = ((Boolean) value).booleanValue();
        firePropertyChange("opaque", old, opaqueFlag);
      }
    } else if ("autoscrolls".equals(propertyName)) {
      if (!autoscrollsExplicit) {
        autoscrolls = ((Boolean) value).booleanValue();
      }
    } else {
      throw new IllegalArgumentException("property \"" + propertyName + "\" cannot be set using this method");
    }
  }
  private boolean opaqueExplicit;
  private boolean autoscrollsExplicit;
  public void setOpaque(boolean p0) {
    opaqueExplicit = true;
    boolean old = opaqueFlag;
    opaqueFlag = p0;
    firePropertyChange("opaque", old, p0);
  }
  protected java.lang.String paramString() {
    return super.paramString();
  }
  public boolean isOpaque() {
    return opaqueFlag;
  }
  public void enable() {
    setEnabled(true);
  }
  public void updateUI() {
    setUI(UIManager.getUI(this));
  }
  protected void paintBorder(java.awt.Graphics p0) {
    if (border != null) {
      border.paintBorder(this, p0, 0, 0, getWidth(), getHeight());
    }
  }
  protected void setUI(javax.swing.plaf.ComponentUI p0) {
    if (ui != null) {
      ui.uninstallUI(this);
    }
    javax.swing.plaf.ComponentUI old = ui;
    ui = p0;
    if (ui != null) {
      ui.installUI(this);
    } else {
      ShimDefaults.install(this);
    }
    firePropertyChange("UI", old, p0);
    revalidate();
    repaint();
  }
  public javax.swing.plaf.ComponentUI getUI() {
    return ui;
  }
  protected boolean processKeyBinding(javax.swing.KeyStroke p0, java.awt.event.KeyEvent p1, int p2, boolean p3) {
    InputMap map = getInputMap(p2, false);
    ActionMap am = getActionMap(false);
    if (map != null && am != null && isEnabled()) {
      Object binding = map.get(p0);
      Action action = binding == null ? null : am.get(binding);
      if (action != null) {
        return shim.swing.Actions.notifyAction(action, p0, p1, this, p1.getModifiers());
      }
    }
    return false;
  }
  public java.lang.String getUIClassID() {
    return "ComponentUI";
  }
  public boolean getInheritsPopupMenu() {
    return inheritsPopup;
  }
  public javax.swing.JPopupMenu getComponentPopupMenu() {
    if (!getInheritsPopupMenu()) {
      return popupMenu;
    }
    if (popupMenu == null) {
      Container parent = getParent();
      while (parent != null) {
        if (parent instanceof JComponent) {
          return ((JComponent) parent).getComponentPopupMenu();
        }
        if (parent instanceof java.applet.Applet) {
          break;
        }
        parent = parent.getParent();
      }
      return null;
    }
    return popupMenu;
  }
  public boolean isManagingFocus() {
    return false;
  }
  public static java.util.Locale getDefaultLocale() {
    if (defaultLocale == null) {
      defaultLocale = Locale.getDefault();
    }
    return defaultLocale;
  }
  public final void putClientProperty(java.lang.Object p0, java.lang.Object p1) {
    Object old;
    synchronized (this) {
      if (clientProps == null) {
        clientProps = new java.util.HashMap<>();
      }
      old = p1 == null ? clientProps.remove(p0) : clientProps.put(p0, p1);
    }
    firePropertyChange(p0.toString(), old, p1);
  }
  public boolean isOptimizedDrawingEnabled() {
    return true;
  }
  public static boolean isLightweightComponent(java.awt.Component p0) {
    return p0.isLightweight();
  }
  public javax.swing.border.Border getBorder() {
    return border;
  }
  protected java.awt.Graphics getComponentGraphics(java.awt.Graphics p0) {
    p0.setColor(getForeground());
    p0.setFont(getFont());
    return p0;
  }
  protected void paintComponent(java.awt.Graphics p0) {
    if (ui != null) {
      Graphics scratch = p0.create();
      try {
        ui.update(scratch, this);
      } finally {
        scratch.dispose();
      }
    } else if (opaqueFlag) {
      p0.setColor(getBackground());
      p0.fillRect(0, 0, getWidth(), getHeight());
    }
  }
  protected void printComponent(java.awt.Graphics p0) {
    paintComponent(p0);
  }
  protected void printBorder(java.awt.Graphics p0) {
    paintBorder(p0);
  }
  protected void paintChildren(java.awt.Graphics p0) {
    for (int i = getComponentCount() - 1; i >= 0; i--) {
      Component c = getComponent(i);
      if (c.isVisible()) {
        Graphics cg = p0.create(c.getX(), c.getY(), c.getWidth(), c.getHeight());
        try {
          c.paint(cg);
        } finally {
          cg.dispose();
        }
      }
    }
  }
  protected void printChildren(java.awt.Graphics p0) {
    paintChildren(p0);
  }
  public java.awt.Component getNextFocusableComponent() {
    return (Component) getClientProperty("nextFocus");
  }
  public void setNextFocusableComponent(java.awt.Component p0) {
    putClientProperty("nextFocus", p0);
  }
  public final java.lang.Object getClientProperty(java.lang.Object p0) {
    synchronized (this) {
      return clientProps == null ? null : clientProps.get(p0);
    }
  }
  public final javax.swing.InputMap getInputMap() {
    return getInputMap(WHEN_FOCUSED, true);
  }
  public final javax.swing.InputMap getInputMap(int p0) {
    return getInputMap(p0, true);
  }
  public final javax.swing.ActionMap getActionMap() {
    return getActionMap(true);
  }
  public void registerKeyboardAction(java.awt.event.ActionListener p0, java.lang.String p1, javax.swing.KeyStroke p2, int p3) {
    InputMap km = getInputMap(p3, true);
    if (km != null) {
      km.put(p2, p1);
      ActionMap am = getActionMap(true);
      if (am != null) {
        am.put(p1, new shim.swing.Actions.ListenerAction(p0));
      }
    }
  }
  public void registerKeyboardAction(java.awt.event.ActionListener p0, javax.swing.KeyStroke p1, int p2) {
    registerKeyboardAction(p0, null, p1, p2);
  }
  public final void setInputMap(int p0, javax.swing.InputMap p1) {
    switch (p0) {
      case WHEN_IN_FOCUSED_WINDOW:
        if (p1 != null && !(p1 instanceof ComponentInputMap)) {
          throw new IllegalArgumentException("WHEN_IN_FOCUSED_WINDOW InputMaps must be of type ComponentInputMap");
        }
        windowInputMap = (ComponentInputMap) p1;
        break;
      case WHEN_ANCESTOR_OF_FOCUSED_COMPONENT:
        ancestorInputMap = p1;
        break;
      case WHEN_FOCUSED:
        focusInputMap = p1;
        break;
      default:
        throw new IllegalArgumentException("condition must be one of JComponent.WHEN_IN_FOCUSED_WINDOW, JComponent.WHEN_FOCUSED or JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT");
    }
  }
  public final void setActionMap(javax.swing.ActionMap p0) {
    actionMap = p0;
  }
  public static void setDefaultLocale(java.util.Locale p0) {
    defaultLocale = p0;
  }
  protected void processComponentKeyEvent(java.awt.event.KeyEvent p0) {
    
  }
  public java.lang.String getToolTipText(java.awt.event.MouseEvent p0) {
    return getToolTipText();
  }
  public java.lang.String getToolTipText() {
    return toolTip;
  }
  public void scrollRectToVisible(java.awt.Rectangle p0) {
    
  }
  public void setAutoscrolls(boolean p0) {
    autoscrolls = p0;
  }
  public void computeVisibleRect(java.awt.Rectangle p0) {
    p0.setBounds(0, 0, getWidth(), getHeight());
  }
  public void addAncestorListener(javax.swing.event.AncestorListener p0) {
    if (p0 != null) {
      ancestorListeners.add(p0);
    }
  }
  public void removeAncestorListener(javax.swing.event.AncestorListener p0) {
    ancestorListeners.remove(p0);
  }
  public javax.swing.event.AncestorListener[] getAncestorListeners() {
    return ancestorListeners.toArray(new AncestorListener[0]);
  }
  public void paintImmediately(java.awt.Rectangle p0) {
    
  }
  public void paintImmediately(int p0, int p1, int p2, int p3) {
    
  }
  protected boolean isPaintingOrigin() {
    return false;
  }
  public void setInheritsPopupMenu(boolean p0) {
    inheritsPopup = p0;
  }
  public void setComponentPopupMenu(javax.swing.JPopupMenu p0) {
    if (p0 != null) {
      enableEvents(AWTEvent.MOUSE_EVENT_MASK);
    }
    JPopupMenu old = popupMenu;
    popupMenu = p0;
    firePropertyChange("componentPopupMenu", old, p0);
  }
  public boolean isPaintingTile() {
    return false;
  }
  public final boolean isPaintingForPrint() {
    return false;
  }
  public void setRequestFocusEnabled(boolean p0) {
    requestFocusEnabled = p0;
  }
  public boolean isRequestFocusEnabled() {
    return requestFocusEnabled;
  }
  public void grabFocus() {
    
  }
  public void setVerifyInputWhenFocusTarget(boolean p0) {
    verifyInput = p0;
  }
  public boolean getVerifyInputWhenFocusTarget() {
    return verifyInput;
  }
  public void setBorder(javax.swing.border.Border p0) {
    Border old = border;
    border = p0;
    firePropertyChange("border", old, p0);
    if (p0 != old) {
      revalidate();
    }
  }
  public void setAlignmentY(float p0) {
    alignY = p0 > 1f ? 1f : p0 < 0f ? 0f : p0;
  }
  public void setAlignmentX(float p0) {
    alignX = p0 > 1f ? 1f : p0 < 0f ? 0f : p0;
  }
  public void setInputVerifier(javax.swing.InputVerifier p0) {
    inputVerifier = p0;
  }
  public javax.swing.InputVerifier getInputVerifier() {
    return inputVerifier;
  }
  public void setDebugGraphicsOptions(int p0) {
    
  }
  public int getDebugGraphicsOptions() {
    return 0;
  }
  public void unregisterKeyboardAction(javax.swing.KeyStroke p0) {
    ActionMap am = getActionMap(false);
    for (int counter = 0; counter < 3; counter++) {
      InputMap km = getInputMap(counter, false);
      if (km != null) {
        Object actionID = km.get(p0);
        if (am != null && actionID != null) {
          am.remove(actionID);
        }
        km.remove(p0);
      }
    }
  }
  public javax.swing.KeyStroke[] getRegisteredKeyStrokes() {
    java.util.LinkedHashSet<KeyStroke> all = new java.util.LinkedHashSet<>();
    for (int counter = 0; counter < 3; counter++) {
      InputMap km = getInputMap(counter, false);
      KeyStroke[] ks = km == null ? null : km.allKeys();
      if (ks != null) {
        for (KeyStroke k : ks) {
          all.add(k);
        }
      }
    }
    return all.toArray(new KeyStroke[0]);
  }
  public int getConditionForKeyStroke(javax.swing.KeyStroke p0) {
    for (int counter = 0; counter < 3; counter++) {
      InputMap inputMap = getInputMap(counter, false);
      if (inputMap != null && inputMap.get(p0) != null) {
        return counter;
      }
    }
    return UNDEFINED_CONDITION;
  }
  public java.awt.event.ActionListener getActionForKeyStroke(javax.swing.KeyStroke p0) {
    ActionMap am = getActionMap(false);
    for (int counter = 0; counter < 3; counter++) {
      InputMap inputMap = getInputMap(counter, false);
      if (inputMap != null && am != null) {
        Object actionBinding = inputMap.get(p0);
        if (actionBinding != null) {
          Action action = am.get(actionBinding);
          if (action instanceof shim.swing.Actions.ListenerAction) {
            return ((shim.swing.Actions.ListenerAction) action).listener();
          }
          return action;
        }
      }
    }
    return null;
  }
  public void resetKeyboardActions() {
    focusInputMap = null;
    ancestorInputMap = null;
    windowInputMap = null;
    actionMap = null;
  }
  public boolean requestDefaultFocus() {
    return false;
  }
  public void setToolTipText(java.lang.String p0) {
    String old = toolTip;
    toolTip = p0;
    firePropertyChange("ToolTipText", old, p0);
  }
  public java.awt.Point getToolTipLocation(java.awt.event.MouseEvent p0) {
    return null;
  }
  public java.awt.Point getPopupLocation(java.awt.event.MouseEvent p0) {
    return null;
  }
  public javax.swing.JToolTip createToolTip() {
    JToolTip tip = new JToolTip();
    tip.setComponent(this);
    return tip;
  }
  public boolean getAutoscrolls() {
    return autoscrolls;
  }
  public void setTransferHandler(javax.swing.TransferHandler p0) {
    TransferHandler old = transferHandler;
    transferHandler = p0;
    firePropertyChange("transferHandler", old, p0);
  }
  protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
    super.firePropertyChange(propertyName, oldValue, newValue);
  }
  public javax.swing.TransferHandler getTransferHandler() {
    return transferHandler;
  }
  public java.awt.Rectangle getVisibleRect() {
    Rectangle r = new Rectangle();
    computeVisibleRect(r);
    return r;
  }
  public java.awt.Container getTopLevelAncestor() {
    for (Container p = this; p != null; p = p.getParent()) {
      if (p instanceof Window || p instanceof java.applet.Applet) {
        return p;
      }
    }
    return null;
  }
  public void setDoubleBuffered(boolean p0) {
    doubleBuffered = p0;
  }
  public boolean isValidateRoot() { return super.isValidateRoot(); }
  public void addNotify() { super.addNotify(); }
  public java.awt.Insets getInsets(java.awt.Insets p0) {
    if (p0 == null) {
      p0 = new Insets(0, 0, 0, 0);
    }
    Insets i = getInsets();
    p0.set(i.top, i.left, i.bottom, i.right);
    return p0;
  }
  public java.awt.Insets getInsets() {
    return border == null ? super.getInsets() : border.getBorderInsets(this);
  }
  public void removeNotify() { super.removeNotify(); }
  public void setFont(java.awt.Font p0) {
    super.setFont(p0);
  }
  public java.awt.Dimension getMaximumSize() {
    if (isMaximumSizeSet()) {
      return super.getMaximumSize();
    }
    java.awt.Dimension size = ui != null ? ui.getMaximumSize(this) : null;
    return size != null ? size : super.getMaximumSize();
  }
  public float getAlignmentX() {
    return alignX >= 0 ? alignX : super.getAlignmentX();
  }
  public float getAlignmentY() {
    return alignY >= 0 ? alignY : super.getAlignmentY();
  }
  public void paint(java.awt.Graphics p0) {
    Graphics cg = getComponentGraphics(p0).create();
    try {
      if (cg.getClipBounds() == null) {
        cg.setClip(0, 0, getWidth(), getHeight());
      }
      paintComponent(cg);
      paintBorder(cg);
      paintChildren(cg);
    } finally {
      cg.dispose();
    }
  }
  public <T extends java.util.EventListener> T[] getListeners(java.lang.Class<T> p0) { return super.getListeners(p0); }
  public void firePropertyChange(java.lang.String p0, char p1, char p2) { super.firePropertyChange(p0, p1, p2); }
  public void firePropertyChange(java.lang.String p0, int p1, int p2) { super.firePropertyChange(p0, p1, p2); }
  public void firePropertyChange(java.lang.String p0, boolean p1, boolean p2) { super.firePropertyChange(p0, p1, p2); }
  public boolean requestFocus(boolean p0) { return super.requestFocus(p0); }
  public void requestFocus() {
    super.requestFocus();
  }
  public void setVisible(boolean p0) {
    super.setVisible(p0);
  }
  public int getX() { return super.getX(); }
  public int getY() { return super.getY(); }
  public int getWidth() { return super.getWidth(); }
  public int getHeight() { return super.getHeight(); }
  public java.awt.Dimension getPreferredSize() {
    if (isPreferredSizeSet()) {
      return super.getPreferredSize();
    }
    java.awt.Dimension size = ui != null ? ui.getPreferredSize(this) : null;
    return size != null ? size : super.getPreferredSize();
  }
  public java.awt.Dimension getMinimumSize() {
    if (isMinimumSizeSet()) {
      return super.getMinimumSize();
    }
    java.awt.Dimension size = ui != null ? ui.getMinimumSize(this) : null;
    return size != null ? size : super.getMinimumSize();
  }
  public void setFocusTraversalKeys(int p0, java.util.Set<? extends java.awt.AWTKeyStroke> p1) { super.setFocusTraversalKeys(p0, p1); }
  public void setEnabled(boolean p0) {
    super.setEnabled(p0);
  }
  public void disable() { super.disable(); }
  public void repaint(long p0, int p1, int p2, int p3, int p4) {
    
  }
  public void repaint(java.awt.Rectangle p0) {
    
  }
  public void hide() { super.hide(); }
  public void setForeground(java.awt.Color p0) {
    super.setForeground(p0);
  }
  public void setBackground(java.awt.Color p0) {
    super.setBackground(p0);
  }
  public void reshape(int p0, int p1, int p2, int p3) { super.reshape(p0, p1, p2, p3); }
  public java.awt.Graphics getGraphics() { return super.getGraphics(); }
  protected void processKeyEvent(java.awt.event.KeyEvent p0) { super.processKeyEvent(p0); }
  protected void processMouseEvent(java.awt.event.MouseEvent p0) { super.processMouseEvent(p0); }
  protected void processMouseMotionEvent(java.awt.event.MouseEvent p0) { super.processMouseMotionEvent(p0); }
  protected boolean requestFocusInWindow(boolean p0) { return super.requestFocusInWindow(p0); }
  public boolean requestFocusInWindow() { return super.requestFocusInWindow(); }
  public boolean isDoubleBuffered() { return super.isDoubleBuffered(); }
  public void setPreferredSize(java.awt.Dimension p0) { super.setPreferredSize(p0); }
  public void setMinimumSize(java.awt.Dimension p0) { super.setMinimumSize(p0); }
  public void setMaximumSize(java.awt.Dimension p0) { super.setMaximumSize(p0); }
  public int getBaseline(int width, int height) {
    super.getBaseline(width, height);
    if (ui != null) {
      return ui.getBaseline(this, width, height);
    }
    return -1;
  }
  public java.awt.Component.BaselineResizeBehavior getBaselineResizeBehavior() {
    if (ui != null) {
      return ui.getBaselineResizeBehavior(this);
    }
    return java.awt.Component.BaselineResizeBehavior.OTHER;
  }
  public void revalidate() {
    if (getParent() == null) {
      return;
    }
    invalidate();
    final java.awt.Component self = this;
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        java.awt.Container root = self instanceof java.awt.Container ? (java.awt.Container) self : self.getParent();
        java.awt.Component c = self;
        while (c.getParent() != null && !(c instanceof JComponent && ((JComponent) c).isValidateRoot())) {
          c = c.getParent();
        }
        if (c instanceof java.awt.Container) {
          ((java.awt.Container) c).validate();
        } else if (root != null) {
          root.validate();
        }
      }
    });
  }
  public java.awt.FontMetrics getFontMetrics(java.awt.Font p0) {
    return super.getFontMetrics(p0);
  }
  public void printAll(java.awt.Graphics p0) { super.printAll(p0); }
  public javax.swing.JRootPane getRootPane() {
    return SwingUtilities.getRootPane(this);
  }
  protected void fireVetoableChange(java.lang.String p0, java.lang.Object p1, java.lang.Object p2) throws java.beans.PropertyVetoException {
    if (vetoable == null) {
      return;
    }
    vetoable.fireVetoableChange(p0, p1, p2);
  }
  public void addVetoableChangeListener(java.beans.VetoableChangeListener p0) {
    if (vetoable == null) {
      vetoable = new java.beans.VetoableChangeSupport(this);
    }
    vetoable.addVetoableChangeListener(p0);
  }
  public void removeVetoableChangeListener(java.beans.VetoableChangeListener p0) {
    if (vetoable != null) {
      vetoable.removeVetoableChangeListener(p0);
    }
  }
  public java.beans.VetoableChangeListener[] getVetoableChangeListeners() {
    if (vetoable == null) {
      return new java.beans.VetoableChangeListener[0];
    }
    return vetoable.getVetoableChangeListeners();
  }

  public abstract class AccessibleJComponent extends AccessibleAWTContainer implements javax.accessibility.AccessibleExtendedComponent {
    protected java.awt.event.FocusListener accessibleFocusHandler;
    protected AccessibleJComponent() {
    }
    public boolean contains(java.awt.Point p0) {
    return JComponent.this.contains(p0);
  }
    public java.awt.Rectangle getBounds() {
    return JComponent.this.getBounds();
  }
    public java.awt.Point getLocation() {
    return JComponent.this.getLocation();
  }
    public boolean isEnabled() {
    return JComponent.this.isEnabled();
  }
    public java.awt.Dimension getSize() {
    return JComponent.this.getSize();
  }
    public java.util.Locale getLocale() {
    return JComponent.this.getLocale();
  }
    public void setSize(java.awt.Dimension p0) {
    JComponent.this.setSize(p0);
  }
    public java.lang.String getTitledBorderText() {
    Border border = JComponent.this.getBorder();
    if (border instanceof javax.swing.border.TitledBorder) {
      return ((javax.swing.border.TitledBorder) border).getTitle();
    }
    return null;
  }
    public javax.accessibility.AccessibleKeyBinding getAccessibleKeyBinding() {
    return null;
  }
    public java.lang.String getToolTipText() {
    return JComponent.this.getToolTipText();
  }
    protected java.lang.String getBorderTitle(javax.swing.border.Border p0) {
    if (p0 instanceof javax.swing.border.TitledBorder) {
      return ((javax.swing.border.TitledBorder) p0).getTitle();
    }
    return null;
  }
    public boolean isVisible() {
    return JComponent.this.isVisible();
  }
    public java.awt.Font getFont() {
    return JComponent.this.getFont();
  }
    public void setFont(java.awt.Font f) { JComponent.this.setFont(f); }
    public boolean isShowing() {
    return JComponent.this.isShowing();
  }
    public void requestFocus() { JComponent.this.requestFocus(); }
    public void addPropertyChangeListener(java.beans.PropertyChangeListener p0) {
    super.addPropertyChangeListener(p0);
  }
    public int getAccessibleChildrenCount() {
    return JComponent.this.getComponentCount();
  }
    public javax.accessibility.Accessible getAccessibleChild(int p0) {
    Component c = JComponent.this.getComponent(p0);
    return c instanceof Accessible ? (Accessible) c : null;
  }
    public javax.accessibility.AccessibleComponent getAccessibleComponent() {
    return this;
  }
    public void setVisible(boolean b) { JComponent.this.setVisible(b); }
    public void setBounds(java.awt.Rectangle p0) {
    JComponent.this.setBounds(p0);
  }
    public javax.accessibility.Accessible getAccessibleAt(java.awt.Point p0) {
    Component c = JComponent.this.getComponentAt(p0);
    return c instanceof Accessible && c != JComponent.this ? (Accessible) c : null;
  }
    public void setLocation(java.awt.Point p0) {
    JComponent.this.setLocation(p0);
  }
    public java.awt.Point getLocationOnScreen() {
    return JComponent.this.isShowing() ? JComponent.this.getLocationOnScreen() : null;
  }
    public void setEnabled(boolean b) { JComponent.this.setEnabled(b); }
    public java.awt.Color getForeground() {
    return JComponent.this.getForeground();
  }
    public void setForeground(java.awt.Color c) { JComponent.this.setForeground(c); }
    public java.awt.Color getBackground() {
    return JComponent.this.getBackground();
  }
    public void setBackground(java.awt.Color c) { JComponent.this.setBackground(c); }
    public boolean isFocusTraversable() {
    return JComponent.this.isFocusable();
  }
    public void removePropertyChangeListener(java.beans.PropertyChangeListener p0) {
    super.removePropertyChangeListener(p0);
  }
    public void addFocusListener(java.awt.event.FocusListener p0) {
    JComponent.this.addFocusListener(p0);
  }
    public javax.accessibility.Accessible getAccessibleParent() {
    Accessible a = super.getAccessibleParent();
    if (a != null) {
      return a;
    }
    Container p = JComponent.this.getParent();
    return p instanceof Accessible ? (Accessible) p : null;
  }
    public int getAccessibleIndexInParent() {
    Container p = JComponent.this.getParent();
    if (p == null) {
      return -1;
    }
    for (int i = 0; i < p.getComponentCount(); i++) {
      if (p.getComponent(i) == JComponent.this) {
        return i;
      }
    }
    return -1;
  }
    public java.awt.FontMetrics getFontMetrics(java.awt.Font f) { return JComponent.this.getFontMetrics(f); }
    public void setCursor(java.awt.Cursor p0) {
    JComponent.this.setCursor(p0);
  }
    public java.awt.Cursor getCursor() {
    return JComponent.this.getCursor();
  }
    public void removeFocusListener(java.awt.event.FocusListener p0) {
    JComponent.this.removeFocusListener(p0);
  }
    public javax.accessibility.AccessibleStateSet getAccessibleStateSet() {
    AccessibleStateSet states = new AccessibleStateSet();
    if (JComponent.this.isEnabled()) {
      states.add(AccessibleState.ENABLED);
    }
    if (JComponent.this.isFocusable()) {
      states.add(AccessibleState.FOCUSABLE);
    }
    if (JComponent.this.isVisible()) {
      states.add(AccessibleState.VISIBLE);
    }
    if (JComponent.this.isOpaque()) {
      states.add(AccessibleState.OPAQUE);
    }
    if (JComponent.this.isShowing()) {
      states.add(AccessibleState.SHOWING);
    }
    return states;
  }
    public java.lang.String getAccessibleName() {
    if (accessibleName != null) {
      return accessibleName;
    }
    Object name = JComponent.this.getClientProperty("AccessibleName");
    if (name instanceof String) {
      return (String) name;
    }
    return null;
  }
    public java.lang.String getAccessibleDescription() {
    if (accessibleDescription != null) {
      return accessibleDescription;
    }
    Object d = JComponent.this.getClientProperty("AccessibleDescription");
    if (d instanceof String) {
      return (String) d;
    }
    return JComponent.this.getToolTipText();
  }
    public javax.accessibility.AccessibleRole getAccessibleRole() {
    return AccessibleRole.SWING_COMPONENT;
  }
  }

  final InputMap getInputMap(int condition, boolean create) {
    switch (condition) {
      case WHEN_FOCUSED:
        if (focusInputMap == null && create) {
          setInputMap(condition, new javax.swing.plaf.InputMapUIResource());
        }
        return focusInputMap;
      case WHEN_ANCESTOR_OF_FOCUSED_COMPONENT:
        if (ancestorInputMap == null && create) {
          setInputMap(condition, new javax.swing.plaf.InputMapUIResource());
        }
        return ancestorInputMap;
      case WHEN_IN_FOCUSED_WINDOW:
        if (windowInputMap == null && create) {
          setInputMap(condition, new javax.swing.plaf.ComponentInputMapUIResource(this));
        }
        return windowInputMap;
      default:
        throw new IllegalArgumentException("condition must be one of JComponent.WHEN_IN_FOCUSED_WINDOW, JComponent.WHEN_FOCUSED or JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT");
    }
  }

  final ActionMap getActionMap(boolean create) {
    if (actionMap == null && create) {
      setActionMap(new javax.swing.plaf.ActionMapUIResource());
    }
    return actionMap;
  }

  void componentInputMapChanged(ComponentInputMap inputMap) { }

}
