/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.LayoutManager;
import java.awt.Rectangle;
import java.beans.PropertyVetoException;
import javax.accessibility.Accessible;
import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleRole;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;
import javax.swing.plaf.DesktopIconUI;
import javax.swing.plaf.InternalFrameUI;

public class JInternalFrame extends JComponent implements Accessible, WindowConstants, RootPaneContainer {
  public static final String CONTENT_PANE_PROPERTY = "contentPane";
  public static final String MENU_BAR_PROPERTY = "JMenuBar";
  public static final String TITLE_PROPERTY = "title";
  public static final String LAYERED_PANE_PROPERTY = "layeredPane";
  public static final String ROOT_PANE_PROPERTY = "rootPane";
  public static final String GLASS_PANE_PROPERTY = "glassPane";
  public static final String FRAME_ICON_PROPERTY = "frameIcon";
  public static final String IS_SELECTED_PROPERTY = "selected";
  public static final String IS_CLOSED_PROPERTY = "closed";
  public static final String IS_MAXIMUM_PROPERTY = "maximum";
  public static final String IS_ICON_PROPERTY = "icon";

  protected JRootPane rootPane;
  protected boolean rootPaneCheckingEnabled = false;
  protected boolean closable;
  protected boolean isClosed;
  protected boolean maximizable;
  protected boolean isMaximum;
  protected boolean iconable;
  protected boolean isIcon;
  protected boolean resizable;
  protected boolean isSelected;
  protected Icon frameIcon;
  protected String title;
  protected JDesktopIcon desktopIcon;

  private Cursor lastCursor = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
  private boolean opened;
  private Rectangle normalBounds = null;
  private int defaultCloseOperation = DISPOSE_ON_CLOSE;
  private Component lastFocusOwner;

  public JInternalFrame() { this(""); }

  public JInternalFrame(String title) { this(title, false); }

  public JInternalFrame(String title, boolean resizable) { this(title, resizable, false); }

  public JInternalFrame(String title, boolean resizable, boolean closable) { this(title, resizable, closable, false); }

  public JInternalFrame(String title, boolean resizable, boolean closable, boolean maximizable) {
    this(title, resizable, closable, maximizable, false);
  }

  public JInternalFrame(String title, boolean resizable, boolean closable, boolean maximizable, boolean iconifiable) {
    setRootPane(createRootPane());
    setLayout(new BorderLayout());
    this.title = title;
    this.resizable = resizable;
    this.closable = closable;
    this.maximizable = maximizable;
    isMaximum = false;
    this.iconable = iconifiable;
    isClosed = false;
    setVisible(false);
    setRootPaneCheckingEnabled(true);
    desktopIcon = new JDesktopIcon(this);
    setLayer(JLayeredPane.DEFAULT_LAYER);
    updateUI();
  }

  protected JRootPane createRootPane() { return new JRootPane(); }

  public InternalFrameUI getUI() { return (InternalFrameUI) ui; }

  public void setUI(InternalFrameUI ui) {
    boolean checkingEnabled = isRootPaneCheckingEnabled();
    try {
      setRootPaneCheckingEnabled(false);
      super.setUI(ui);
    } finally {
      setRootPaneCheckingEnabled(checkingEnabled);
    }
  }

  public void updateUI() { setUI((InternalFrameUI) UIManager.getUI(this)); }

  public String getUIClassID() { return "InternalFrameUI"; }

  protected boolean isRootPaneCheckingEnabled() { return rootPaneCheckingEnabled; }

  protected void setRootPaneCheckingEnabled(boolean enabled) { rootPaneCheckingEnabled = enabled; }

  protected void addImpl(Component comp, Object constraints, int index) {
    if (isRootPaneCheckingEnabled()) {
      getContentPane().add(comp, constraints, index);
    } else {
      super.addImpl(comp, constraints, index);
    }
  }

  public void remove(Component comp) {
    int oldCount = getComponentCount();
    super.remove(comp);
    if (oldCount == getComponentCount()) {
      getContentPane().remove(comp);
    }
  }

  public void setLayout(LayoutManager manager) {
    if (isRootPaneCheckingEnabled()) {
      getContentPane().setLayout(manager);
    } else {
      super.setLayout(manager);
    }
  }

  public JMenuBar getMenuBar() { return getRootPane().getMenuBar(); }

  public JMenuBar getJMenuBar() { return getRootPane().getJMenuBar(); }

  public void setMenuBar(JMenuBar m) {
    JMenuBar oldValue = getMenuBar();
    getRootPane().setJMenuBar(m);
    firePropertyChange(MENU_BAR_PROPERTY, oldValue, m);
  }

  public void setJMenuBar(JMenuBar m) {
    JMenuBar oldValue = getMenuBar();
    getRootPane().setJMenuBar(m);
    firePropertyChange(MENU_BAR_PROPERTY, oldValue, m);
  }

  public Container getContentPane() { return getRootPane().getContentPane(); }

  public void setContentPane(Container c) {
    Container oldValue = getContentPane();
    getRootPane().setContentPane(c);
    firePropertyChange(CONTENT_PANE_PROPERTY, oldValue, c);
  }

  public JLayeredPane getLayeredPane() { return getRootPane().getLayeredPane(); }

  public void setLayeredPane(JLayeredPane layered) {
    JLayeredPane oldValue = getLayeredPane();
    getRootPane().setLayeredPane(layered);
    firePropertyChange(LAYERED_PANE_PROPERTY, oldValue, layered);
  }

  public Component getGlassPane() { return getRootPane().getGlassPane(); }

  public void setGlassPane(Component glass) {
    Component oldValue = getGlassPane();
    getRootPane().setGlassPane(glass);
    firePropertyChange(GLASS_PANE_PROPERTY, oldValue, glass);
  }

  public JRootPane getRootPane() { return rootPane; }

  protected void setRootPane(JRootPane root) {
    if (rootPane != null) {
      remove(rootPane);
    }
    JRootPane oldValue = getRootPane();
    rootPane = root;
    if (rootPane != null) {
      boolean checkingEnabled = isRootPaneCheckingEnabled();
      try {
        setRootPaneCheckingEnabled(false);
        add(rootPane, BorderLayout.CENTER);
      } finally {
        setRootPaneCheckingEnabled(checkingEnabled);
      }
    }
    firePropertyChange(ROOT_PANE_PROPERTY, oldValue, root);
  }

  public void setClosable(boolean b) {
    Boolean oldValue = closable ? Boolean.TRUE : Boolean.FALSE;
    Boolean newValue = b ? Boolean.TRUE : Boolean.FALSE;
    closable = b;
    firePropertyChange("closable", oldValue, newValue);
  }

  public boolean isClosable() { return closable; }

  public boolean isClosed() { return isClosed; }

  public void setClosed(boolean b) throws PropertyVetoException {
    if (isClosed == b) {
      return;
    }
    Boolean oldValue = isClosed ? Boolean.TRUE : Boolean.FALSE;
    Boolean newValue = b ? Boolean.TRUE : Boolean.FALSE;
    if (b) {
      fireInternalFrameEvent(InternalFrameEvent.INTERNAL_FRAME_CLOSING);
    }
    fireVetoableChange(IS_CLOSED_PROPERTY, oldValue, newValue);
    isClosed = b;
    if (isClosed) {
      setVisible(false);
    }
    firePropertyChange(IS_CLOSED_PROPERTY, oldValue, newValue);
    if (isClosed) {
      dispose();
    } else if (!opened) {
      // do nothing
    }
  }

  public void setResizable(boolean b) {
    Boolean oldValue = resizable ? Boolean.TRUE : Boolean.FALSE;
    Boolean newValue = b ? Boolean.TRUE : Boolean.FALSE;
    resizable = b;
    firePropertyChange("resizable", oldValue, newValue);
  }

  public boolean isResizable() { return isMaximum ? false : resizable; }

  public void setIconifiable(boolean b) {
    Boolean oldValue = iconable ? Boolean.TRUE : Boolean.FALSE;
    Boolean newValue = b ? Boolean.TRUE : Boolean.FALSE;
    iconable = b;
    firePropertyChange("iconable", oldValue, newValue);
  }

  public boolean isIconifiable() { return iconable; }

  public boolean isIcon() { return isIcon; }

  public void setIcon(boolean b) throws PropertyVetoException {
    if (isIcon == b) {
      return;
    }
    firePropertyChange("ancestor", null, getParent());
    Boolean oldValue = isIcon ? Boolean.TRUE : Boolean.FALSE;
    Boolean newValue = b ? Boolean.TRUE : Boolean.FALSE;
    fireVetoableChange(IS_ICON_PROPERTY, oldValue, newValue);
    isIcon = b;
    firePropertyChange(IS_ICON_PROPERTY, oldValue, newValue);
    if (b) {
      fireInternalFrameEvent(InternalFrameEvent.INTERNAL_FRAME_ICONIFIED);
    } else {
      fireInternalFrameEvent(InternalFrameEvent.INTERNAL_FRAME_DEICONIFIED);
    }
  }

  public void setMaximizable(boolean b) {
    Boolean oldValue = maximizable ? Boolean.TRUE : Boolean.FALSE;
    Boolean newValue = b ? Boolean.TRUE : Boolean.FALSE;
    maximizable = b;
    firePropertyChange("maximizable", oldValue, newValue);
  }

  public boolean isMaximizable() { return maximizable; }

  public boolean isMaximum() { return isMaximum; }

  public void setMaximum(boolean b) throws PropertyVetoException {
    if (isMaximum == b) {
      return;
    }
    Boolean oldValue = isMaximum ? Boolean.TRUE : Boolean.FALSE;
    Boolean newValue = b ? Boolean.TRUE : Boolean.FALSE;
    fireVetoableChange(IS_MAXIMUM_PROPERTY, oldValue, newValue);
    isMaximum = b;
    firePropertyChange(IS_MAXIMUM_PROPERTY, oldValue, newValue);
  }

  public String getTitle() { return title; }

  public void setTitle(String title) {
    String oldValue = this.title;
    this.title = title;
    firePropertyChange(TITLE_PROPERTY, oldValue, title);
  }

  public void setSelected(boolean selected) throws PropertyVetoException {
    if ((isSelected == selected) || (selected && (isIcon || !isShowing()))) {
      return;
    }
    Boolean oldValue = isSelected ? Boolean.TRUE : Boolean.FALSE;
    Boolean newValue = selected ? Boolean.TRUE : Boolean.FALSE;
    fireVetoableChange(IS_SELECTED_PROPERTY, oldValue, newValue);
    if (selected) {
      restoreSubcomponentFocus();
    }
    isSelected = selected;
    firePropertyChange(IS_SELECTED_PROPERTY, oldValue, newValue);
    if (isSelected) {
      fireInternalFrameEvent(InternalFrameEvent.INTERNAL_FRAME_ACTIVATED);
    } else {
      fireInternalFrameEvent(InternalFrameEvent.INTERNAL_FRAME_DEACTIVATED);
    }
    repaint();
  }

  public boolean isSelected() { return isSelected; }

  public void setFrameIcon(Icon icon) {
    Icon oldIcon = frameIcon;
    frameIcon = icon;
    firePropertyChange(FRAME_ICON_PROPERTY, oldIcon, icon);
  }

  public Icon getFrameIcon() { return frameIcon; }

  public void moveToFront() {
    if (isIcon()) {
      if (getDesktopIcon().getParent() instanceof JLayeredPane) {
        ((JLayeredPane) getDesktopIcon().getParent()).moveToFront(getDesktopIcon());
      }
    } else if (getParent() instanceof JLayeredPane) {
      ((JLayeredPane) getParent()).moveToFront(this);
    }
  }

  public void moveToBack() {
    if (isIcon()) {
      if (getDesktopIcon().getParent() instanceof JLayeredPane) {
        ((JLayeredPane) getDesktopIcon().getParent()).moveToBack(getDesktopIcon());
      }
    } else if (getParent() instanceof JLayeredPane) {
      ((JLayeredPane) getParent()).moveToBack(this);
    }
  }

  public Cursor getLastCursor() { return lastCursor; }

  public void setCursor(Cursor cursor) {
    if (cursor == null) {
      lastCursor = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
      super.setCursor(cursor);
      return;
    }
    int type = cursor.getType();
    if (!(type == Cursor.SW_RESIZE_CURSOR || type == Cursor.SE_RESIZE_CURSOR || type == Cursor.NW_RESIZE_CURSOR
        || type == Cursor.NE_RESIZE_CURSOR || type == Cursor.N_RESIZE_CURSOR || type == Cursor.S_RESIZE_CURSOR
        || type == Cursor.W_RESIZE_CURSOR || type == Cursor.E_RESIZE_CURSOR)) {
      lastCursor = cursor;
    }
    super.setCursor(cursor);
  }

  public void setLayer(Integer layer) {
    putClientProperty(JLayeredPane.LAYER_PROPERTY, layer);
    if (getParent() != null && getParent() instanceof JLayeredPane) {
      JLayeredPane p = (JLayeredPane) getParent();
      p.setLayer(this, layer.intValue(), p.getPosition(this));
    }
  }

  public void setLayer(int layer) { this.setLayer(Integer.valueOf(layer)); }

  public int getLayer() { return JLayeredPane.getLayer(this); }

  public JDesktopPane getDesktopPane() {
    Container p;
    if ((p = getParent()) == null) {
      p = getDesktopIcon().getParent();
    }
    while (p != null && !(p instanceof JDesktopPane)) {
      p = p.getParent();
    }
    return (JDesktopPane) p;
  }

  public void setDesktopIcon(JDesktopIcon d) {
    JDesktopIcon oldValue = getDesktopIcon();
    desktopIcon = d;
    firePropertyChange("desktopIcon", oldValue, d);
  }

  public JDesktopIcon getDesktopIcon() { return desktopIcon; }

  public Rectangle getNormalBounds() {
    if (normalBounds != null) {
      return normalBounds;
    } else {
      return getBounds();
    }
  }

  public void setNormalBounds(Rectangle r) { normalBounds = r; }

  public Component getFocusOwner() {
    if (isSelected()) {
      return lastFocusOwner;
    }
    return null;
  }

  public Component getMostRecentFocusOwner() {
    if (isSelected()) {
      return getFocusOwner();
    }
    if (lastFocusOwner != null) {
      return lastFocusOwner;
    }
    return getContentPane() == null ? null : getContentPane().getFocusTraversalPolicy() == null ? null
        : getContentPane().getFocusTraversalPolicy().getDefaultComponent(getContentPane());
  }

  public void restoreSubcomponentFocus() {
    if (isIcon()) {
      SwingUtilities2_.noop();
    }
  }

  private static final class SwingUtilities2_ {
    static void noop() { }
  }

  public void reshape(int x, int y, int width, int height) {
    super.reshape(x, y, width, height);
    validate();
    repaint();
  }

  public void addInternalFrameListener(InternalFrameListener l) { listenerList.add(InternalFrameListener.class, l); }

  public void removeInternalFrameListener(InternalFrameListener l) {
    listenerList.remove(InternalFrameListener.class, l);
  }

  public InternalFrameListener[] getInternalFrameListeners() {
    return listenerList.getListeners(InternalFrameListener.class);
  }

  protected void fireInternalFrameEvent(int id) {
    Object[] listeners = listenerList.getListenerList();
    InternalFrameEvent e = null;
    for (int i = listeners.length - 2; i >= 0; i -= 2) {
      if (listeners[i] == InternalFrameListener.class) {
        if (e == null) {
          e = new InternalFrameEvent(this, id);
        }
        switch (e.getID()) {
          case InternalFrameEvent.INTERNAL_FRAME_OPENED:
            ((InternalFrameListener) listeners[i + 1]).internalFrameOpened(e);
            break;
          case InternalFrameEvent.INTERNAL_FRAME_CLOSING:
            ((InternalFrameListener) listeners[i + 1]).internalFrameClosing(e);
            break;
          case InternalFrameEvent.INTERNAL_FRAME_CLOSED:
            ((InternalFrameListener) listeners[i + 1]).internalFrameClosed(e);
            break;
          case InternalFrameEvent.INTERNAL_FRAME_ICONIFIED:
            ((InternalFrameListener) listeners[i + 1]).internalFrameIconified(e);
            break;
          case InternalFrameEvent.INTERNAL_FRAME_DEICONIFIED:
            ((InternalFrameListener) listeners[i + 1]).internalFrameDeiconified(e);
            break;
          case InternalFrameEvent.INTERNAL_FRAME_ACTIVATED:
            ((InternalFrameListener) listeners[i + 1]).internalFrameActivated(e);
            break;
          case InternalFrameEvent.INTERNAL_FRAME_DEACTIVATED:
            ((InternalFrameListener) listeners[i + 1]).internalFrameDeactivated(e);
            break;
          default:
            break;
        }
      }
    }
  }

  public void doDefaultCloseAction() {
    fireInternalFrameEvent(InternalFrameEvent.INTERNAL_FRAME_CLOSING);
    switch (defaultCloseOperation) {
      case DO_NOTHING_ON_CLOSE:
        break;
      case HIDE_ON_CLOSE:
        setVisible(false);
        if (isSelected()) {
          try {
            setSelected(false);
          } catch (PropertyVetoException pve) {
            return;
          }
        }
        break;
      case DISPOSE_ON_CLOSE:
        try {
          fireVetoableChange(IS_CLOSED_PROPERTY, Boolean.FALSE, Boolean.TRUE);
          isClosed = true;
          setVisible(false);
          firePropertyChange(IS_CLOSED_PROPERTY, Boolean.FALSE, Boolean.TRUE);
          dispose();
        } catch (PropertyVetoException pve) {
          return;
        }
        break;
      default:
        break;
    }
  }

  public void setDefaultCloseOperation(int operation) { this.defaultCloseOperation = operation; }

  public int getDefaultCloseOperation() { return defaultCloseOperation; }

  public void pack() {
    try {
      if (isIcon()) {
        setIcon(false);
      } else if (isMaximum()) {
        setMaximum(false);
      }
    } catch (PropertyVetoException e) {
      return;
    }
    setSize(getPreferredSize());
    validate();
  }

  public void show() {
    if (isVisible()) {
      return;
    }
    if (!opened) {
      fireInternalFrameEvent(InternalFrameEvent.INTERNAL_FRAME_OPENED);
      opened = true;
    }
    toFront();
    super.show();
  }

  public void hide() {
    if (isIcon()) {
      getDesktopIcon().setVisible(false);
    }
    super.hide();
  }

  public void dispose() {
    if (isVisible()) {
      setVisible(false);
    }
    if (isSelected()) {
      try {
        setSelected(false);
      } catch (PropertyVetoException pve) {
        isSelected = false;
      }
    }
    if (!isClosed) {
      firePropertyChange(IS_CLOSED_PROPERTY, Boolean.FALSE, Boolean.TRUE);
      isClosed = true;
    }
    fireInternalFrameEvent(InternalFrameEvent.INTERNAL_FRAME_CLOSED);
  }

  public void toFront() { moveToFront(); }

  public void toBack() { moveToBack(); }

  public final void setFocusCycleRoot(boolean focusCycleRoot) { }

  public final boolean isFocusCycleRoot() { return true; }

  public final Container getFocusCycleRootAncestor() { return null; }

  public final String getWarningString() { return null; }

  protected String paramString() {
    String rootPaneString = (rootPane != null ? rootPane.toString() : "");
    String rootPaneCheckingEnabledString = (rootPaneCheckingEnabled ? "true" : "false");
    String closableString = (closable ? "true" : "false");
    String isClosedString = (isClosed ? "true" : "false");
    String maximizableString = (maximizable ? "true" : "false");
    String isMaximumString = (isMaximum ? "true" : "false");
    String iconableString = (iconable ? "true" : "false");
    String isIconString = (isIcon ? "true" : "false");
    String resizableString = (resizable ? "true" : "false");
    String isSelectedString = (isSelected ? "true" : "false");
    String frameIconString = (frameIcon != null ? frameIcon.toString() : "");
    String titleString = (title != null ? title : "");
    String desktopIconString = (desktopIcon != null ? desktopIcon.toString() : "");
    return super.paramString() + ",closable=" + closableString + ",desktopIcon=" + desktopIconString
        + ",frameIcon=" + frameIconString + ",iconable=" + iconableString + ",isIcon=" + isIconString
        + ",isMaximum=" + isMaximumString + ",isSelected=" + isSelectedString + ",maximizable=" + maximizableString
        + ",resizable=" + resizableString + ",rootPane=" + rootPaneString + ",rootPaneCheckingEnabled="
        + rootPaneCheckingEnabledString + ",title=" + titleString;
  }

  protected void paintComponent(Graphics g) {
    super.paintComponent(g);
  }

  public AccessibleContext getAccessibleContext() {
    if (accessibleContext == null) {
      accessibleContext = new AccessibleJInternalFrame();
    }
    return accessibleContext;
  }

  protected class AccessibleJInternalFrame extends AccessibleJComponent implements javax.accessibility.AccessibleValue {
    public String getAccessibleName() {
      String name = accessibleName;
      if (name == null) {
        name = (String) getClientProperty(AccessibleContext.ACCESSIBLE_NAME_PROPERTY);
      }
      if (name == null) {
        name = getTitle();
      }
      return name;
    }

    public AccessibleRole getAccessibleRole() { return AccessibleRole.INTERNAL_FRAME; }

    public javax.accessibility.AccessibleValue getAccessibleValue() { return this; }

    public Number getCurrentAccessibleValue() { return Integer.valueOf(getLayer()); }

    public boolean setCurrentAccessibleValue(Number n) {
      if (n == null) {
        return false;
      }
      setLayer(new Integer(n.intValue()));
      return true;
    }

    public Number getMinimumAccessibleValue() { return Integer.MIN_VALUE; }

    public Number getMaximumAccessibleValue() { return Integer.MAX_VALUE; }
  }

  public static class JDesktopIcon extends JComponent implements Accessible {
    JInternalFrame internalFrame;

    public JDesktopIcon(JInternalFrame f) {
      setVisible(false);
      setInternalFrame(f);
      updateUI();
    }

    public DesktopIconUI getUI() { return (DesktopIconUI) ui; }

    public void setUI(DesktopIconUI ui) { super.setUI(ui); }

    public JInternalFrame getInternalFrame() { return internalFrame; }

    public void setInternalFrame(JInternalFrame f) { internalFrame = f; }

    public JDesktopPane getDesktopPane() {
      if (getInternalFrame() != null) {
        return getInternalFrame().getDesktopPane();
      }
      return null;
    }

    public void updateUI() {
      setUI((DesktopIconUI) UIManager.getUI(this));
      invalidate();
      java.awt.Dimension r = getPreferredSize();
      setSize(r.width, r.height);
    }

    public String getUIClassID() { return "DesktopIconUI"; }

    public AccessibleContext getAccessibleContext() {
      if (accessibleContext == null) {
        accessibleContext = new AccessibleJDesktopIcon();
      }
      return accessibleContext;
    }

    protected class AccessibleJDesktopIcon extends AccessibleJComponent {
      public AccessibleRole getAccessibleRole() { return AccessibleRole.DESKTOP_ICON; }
    }
  }
}
