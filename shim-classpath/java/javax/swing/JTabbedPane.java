/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing;

import java.awt.Color;
import java.awt.Component;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.accessibility.Accessible;
import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleRole;
import javax.accessibility.AccessibleSelection;
import javax.accessibility.AccessibleStateSet;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.TabbedPaneUI;
import javax.swing.plaf.UIResource;

public class JTabbedPane extends JComponent implements Serializable, Accessible, SwingConstants {
  public static final int WRAP_TAB_LAYOUT = 0;
  public static final int SCROLL_TAB_LAYOUT = 1;

  protected int tabPlacement = TOP;
  private int tabLayoutPolicy;
  protected SingleSelectionModel model;
  private boolean haveRegistered;
  protected ChangeListener changeListener = null;
  private final List<Page> pages = new ArrayList<Page>(1);
  private Component visComp = null;
  protected transient ChangeEvent changeEvent = null;

  public JTabbedPane() { this(TOP, WRAP_TAB_LAYOUT); }

  public JTabbedPane(int tabPlacement) { this(tabPlacement, WRAP_TAB_LAYOUT); }

  public JTabbedPane(int tabPlacement, int tabLayoutPolicy) {
    setTabPlacement(tabPlacement);
    setTabLayoutPolicy(tabLayoutPolicy);
    setModel(new DefaultSingleSelectionModel());
    updateUI();
  }

  public TabbedPaneUI getUI() { return (TabbedPaneUI) ui; }

  public void setUI(TabbedPaneUI ui) { super.setUI(ui); }

  public void updateUI() { setUI((TabbedPaneUI) UIManager.getUI(this)); }

  public String getUIClassID() { return "TabbedPaneUI"; }

  protected class ModelListener implements ChangeListener, Serializable {
    public void stateChanged(ChangeEvent e) { fireStateChanged(); }
  }

  protected ChangeListener createChangeListener() { return new ModelListener(); }

  public void addChangeListener(ChangeListener l) { listenerList.add(ChangeListener.class, l); }

  public void removeChangeListener(ChangeListener l) { listenerList.remove(ChangeListener.class, l); }

  public ChangeListener[] getChangeListeners() { return listenerList.getListeners(ChangeListener.class); }

  protected void fireStateChanged() {
    int selIndex = getSelectedIndex();
    Component selComp = selIndex < 0 ? null : getComponentAt(selIndex);
    if (visComp != null && visComp != selComp) {
      visComp.setVisible(false);
    }
    if (selComp != null) {
      selComp.setVisible(true);
    }
    visComp = selComp;
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

  public SingleSelectionModel getModel() { return model; }

  public void setModel(SingleSelectionModel model) {
    SingleSelectionModel oldModel = getModel();
    if (oldModel != null) {
      oldModel.removeChangeListener(changeListener);
      changeListener = null;
    }
    this.model = model;
    if (model != null) {
      changeListener = createChangeListener();
      model.addChangeListener(changeListener);
    }
    firePropertyChange("model", oldModel, model);
    repaint();
  }

  public int getTabPlacement() { return tabPlacement; }

  public void setTabPlacement(int tabPlacement) {
    if (tabPlacement != TOP && tabPlacement != LEFT && tabPlacement != BOTTOM && tabPlacement != RIGHT) {
      throw new IllegalArgumentException("illegal tab placement: must be TOP, BOTTOM, LEFT, or RIGHT");
    }
    if (this.tabPlacement != tabPlacement) {
      int oldValue = this.tabPlacement;
      this.tabPlacement = tabPlacement;
      firePropertyChange("tabPlacement", oldValue, tabPlacement);
      revalidate();
      repaint();
    }
  }

  public int getTabLayoutPolicy() { return tabLayoutPolicy; }

  public void setTabLayoutPolicy(int tabLayoutPolicy) {
    if (tabLayoutPolicy != WRAP_TAB_LAYOUT && tabLayoutPolicy != SCROLL_TAB_LAYOUT) {
      throw new IllegalArgumentException("illegal tab layout policy: must be WRAP_TAB_LAYOUT or SCROLL_TAB_LAYOUT");
    }
    if (this.tabLayoutPolicy != tabLayoutPolicy) {
      int oldValue = this.tabLayoutPolicy;
      this.tabLayoutPolicy = tabLayoutPolicy;
      firePropertyChange("tabLayoutPolicy", oldValue, tabLayoutPolicy);
      revalidate();
      repaint();
    }
  }

  public int getSelectedIndex() { return model.getSelectedIndex(); }

  public void setSelectedIndex(int index) {
    if (index != -1) {
      checkIndex(index);
    }
    setSelectedIndexImpl(index, true);
  }

  private void setSelectedIndexImpl(int index, boolean doAccessibleChanges) {
    int oldIndex = model.getSelectedIndex();
    Page oldPage = null;
    Page newPage = null;
    String oldName = null;
    doAccessibleChanges = doAccessibleChanges && (oldIndex != index);
    if (doAccessibleChanges) {
      if (accessibleContext != null) {
        oldName = accessibleContext.getAccessibleName();
      }
      if (oldIndex >= 0) {
        oldPage = pages.get(oldIndex);
      }
      if (index >= 0) {
        newPage = pages.get(index);
      }
    }
    model.setSelectedIndex(index);
    if (doAccessibleChanges) {
      changeAccessibleSelection(oldPage, oldName, newPage);
    }
  }

  private void changeAccessibleSelection(Page oldPage, String oldName, Page newPage) {
    if (accessibleContext == null) {
      return;
    }
    if (oldPage != null) {
      accessibleContext.firePropertyChange(AccessibleContext.ACCESSIBLE_STATE_PROPERTY,
          javax.accessibility.AccessibleState.SELECTED, null);
    }
    if (newPage != null) {
      accessibleContext.firePropertyChange(AccessibleContext.ACCESSIBLE_STATE_PROPERTY, null,
          javax.accessibility.AccessibleState.SELECTED);
    }
    accessibleContext.firePropertyChange(AccessibleContext.ACCESSIBLE_NAME_PROPERTY, oldName,
        accessibleContext.getAccessibleName());
  }

  public Component getSelectedComponent() {
    int index = getSelectedIndex();
    if (index == -1) {
      return null;
    }
    return getComponentAt(index);
  }

  public void setSelectedComponent(Component c) {
    int index = indexOfComponent(c);
    if (index != -1) {
      setSelectedIndex(index);
    } else {
      throw new IllegalArgumentException("component not found in tabbed pane");
    }
  }

  public void insertTab(String title, Icon icon, Component component, String tip, int index) {
    int newIndex = index;
    int removeIndex = indexOfComponent(component);
    if (component != null && removeIndex != -1) {
      removeTabAt(removeIndex);
      if (newIndex > removeIndex) {
        newIndex--;
      }
    }
    int selectedIndex = getSelectedIndex();
    pages.add(newIndex, new Page(this, title != null ? title : "", icon, null, component, tip));
    if (component != null) {
      addImpl(component, null, -1);
      component.setVisible(false);
    } else {
      firePropertyChange("indexForNullComponent", -1, index);
    }
    if (pages.size() == 1) {
      setSelectedIndex(0);
    }
    if (selectedIndex >= newIndex) {
      setSelectedIndexImpl(selectedIndex + 1, false);
    }
    if (!isValid()) {
      revalidate();
      repaint();
    }
  }

  public void addTab(String title, Icon icon, Component component, String tip) {
    insertTab(title, icon, component, tip, pages.size());
  }

  public void addTab(String title, Icon icon, Component component) { insertTab(title, icon, component, null, pages.size()); }

  public void addTab(String title, Component component) { insertTab(title, null, component, null, pages.size()); }

  public Component add(Component component) {
    if (!(component instanceof UIResource)) {
      addTab(component.getName(), component);
    } else {
      super.add(component);
    }
    return component;
  }

  public Component add(String title, Component component) {
    if (!(component instanceof UIResource)) {
      addTab(title, component);
    } else {
      super.add(title, component);
    }
    return component;
  }

  public Component add(Component component, int index) {
    if (!(component instanceof UIResource)) {
      insertTab(component.getName(), null, component, null, index == -1 ? getTabCount() : index);
    } else {
      super.add(component, index);
    }
    return component;
  }

  public void add(Component component, Object constraints) {
    if (!(component instanceof UIResource)) {
      if (constraints instanceof String) {
        addTab((String) constraints, component);
      } else if (constraints instanceof Icon) {
        addTab(null, (Icon) constraints, component);
      } else {
        add(component);
      }
    } else {
      super.add(component, constraints);
    }
  }

  public void add(Component component, Object constraints, int index) {
    if (!(component instanceof UIResource)) {
      Icon icon = constraints instanceof Icon ? (Icon) constraints : null;
      String title = constraints instanceof String ? (String) constraints : null;
      insertTab(title, icon, component, null, index == -1 ? getTabCount() : index);
    } else {
      super.add(component, constraints, index);
    }
  }

  public void removeTabAt(int index) {
    checkIndex(index);
    Page removed = pages.get(index);
    Component component = removed.component;
    int selected = getSelectedIndex();
    int newSelected = selected;
    if (selected > index || (selected == index && selected == pages.size() - 1)) {
      newSelected = selected - 1;
    }
    boolean selectionChanges = selected == index;
    if (selected == index && component != null && visComp == component) {
      visComp = null;
    }
    pages.remove(index);
    if (component != null) {
      removeChild(component);
    }
    if (removed.tabComponent != null) {
      removeChild(removed.tabComponent);
    }
    if (selectionChanges && newSelected == selected) {
      setSelectedIndexImpl(newSelected, false);
      fireStateChanged();
    } else if (selected != newSelected) {
      setSelectedIndexImpl(newSelected, false);
    }
    revalidate();
    repaint();
  }

  private void removeChild(Component c) {
    Component[] cs = getComponents();
    for (int i = 0; i < cs.length; i++) {
      if (cs[i] == c) {
        super.remove(i);
        return;
      }
    }
  }

  public void remove(Component component) {
    int index = indexOfComponent(component);
    if (index != -1) {
      removeTabAt(index);
    } else {
      Component[] children = getComponents();
      for (int i = 0; i < children.length; ++i) {
        if (component == children[i]) {
          super.remove(i);
          break;
        }
      }
    }
  }

  public void remove(int index) { removeTabAt(index); }

  public void removeAll() {
    setSelectedIndexImpl(-1, true);
    int tabCount = getTabCount();
    while (tabCount-- > 0) {
      removeTabAt(tabCount);
    }
  }

  public int getTabCount() { return pages.size(); }

  public int getTabRunCount() {
    if (ui != null) {
      return 1;
    }
    return 0;
  }

  public String getTitleAt(int index) { return pages.get(index).title; }

  public Icon getIconAt(int index) { return pages.get(index).icon; }

  public Icon getDisabledIconAt(int index) {
    Page page = pages.get(index);
    if (page.disabledIcon == null) {
      page.disabledIcon = UIManager.getLookAndFeel().getDisabledIcon(this, page.icon);
    }
    return page.disabledIcon;
  }

  public String getToolTipTextAt(int index) { return pages.get(index).tip; }

  public Color getBackgroundAt(int index) { return pages.get(index).getBackground(); }

  public Color getForegroundAt(int index) { return pages.get(index).getForeground(); }

  public boolean isEnabledAt(int index) { return pages.get(index).isEnabled(); }

  public Component getComponentAt(int index) { return pages.get(index).component; }

  public int getMnemonicAt(int tabIndex) {
    checkIndex(tabIndex);
    return pages.get(tabIndex).getMnemonic();
  }

  public int getDisplayedMnemonicIndexAt(int tabIndex) {
    checkIndex(tabIndex);
    return pages.get(tabIndex).getDisplayedMnemonicIndex();
  }

  public Rectangle getBoundsAt(int index) {
    checkIndex(index);
    TabbedPaneUI ui = getUI();
    if (ui != null) {
      return ui.getTabBounds(this, index);
    }
    return null;
  }

  public void setTitleAt(int index, String title) {
    Page page = pages.get(index);
    String oldTitle = page.title;
    page.title = title;
    if (oldTitle != title) {
      firePropertyChange("indexForTitle", -1, index);
    }
    page.updateDisplayedMnemonicIndex();
    if ((oldTitle != title) && (accessibleContext != null)) {
      accessibleContext.firePropertyChange(AccessibleContext.ACCESSIBLE_VISIBLE_DATA_PROPERTY, oldTitle, title);
    }
    if (title == null || oldTitle == null || !title.equals(oldTitle)) {
      revalidate();
      repaint();
    }
  }

  public void setIconAt(int index, Icon icon) {
    Page page = pages.get(index);
    Icon oldIcon = page.icon;
    if (icon != oldIcon) {
      page.icon = icon;
      if (page.disabledIcon instanceof UIResource) {
        page.disabledIcon = null;
      }
      if (accessibleContext != null) {
        accessibleContext.firePropertyChange(AccessibleContext.ACCESSIBLE_VISIBLE_DATA_PROPERTY, oldIcon, icon);
      }
      revalidate();
      repaint();
    }
  }

  public void setDisabledIconAt(int index, Icon disabledIcon) {
    Icon oldIcon = pages.get(index).disabledIcon;
    pages.get(index).disabledIcon = disabledIcon;
    if (disabledIcon != oldIcon && !isEnabledAt(index)) {
      revalidate();
      repaint();
    }
  }

  public void setToolTipTextAt(int index, String toolTipText) {
    String oldToolTipText = pages.get(index).tip;
    pages.get(index).tip = toolTipText;
    if ((oldToolTipText != toolTipText) && (accessibleContext != null)) {
      accessibleContext.firePropertyChange(AccessibleContext.ACCESSIBLE_TABLE_CAPTION_CHANGED, oldToolTipText,
          toolTipText);
    }
    if (!haveRegistered && toolTipText != null) {
      ToolTipManager.sharedInstance().registerComponent(this);
      haveRegistered = true;
    }
  }

  public void setBackgroundAt(int index, Color background) {
    Color oldBg = pages.get(index).background;
    pages.get(index).setBackground(background);
    if (background == null || oldBg == null || !background.equals(oldBg)) {
      Rectangle tabBounds = getBoundsAt(index);
      if (tabBounds != null) {
        repaint(tabBounds);
      }
    }
  }

  public void setForegroundAt(int index, Color foreground) {
    Color oldFg = pages.get(index).foreground;
    pages.get(index).setForeground(foreground);
    if (foreground == null || oldFg == null || !foreground.equals(oldFg)) {
      Rectangle tabBounds = getBoundsAt(index);
      if (tabBounds != null) {
        repaint(tabBounds);
      }
    }
  }

  public void setEnabledAt(int index, boolean enabled) {
    boolean oldEnabled = pages.get(index).isEnabled();
    pages.get(index).setEnabled(enabled);
    if (enabled != oldEnabled) {
      revalidate();
      repaint();
    }
  }

  public void setComponentAt(int index, Component component) {
    Page page = pages.get(index);
    if (component != page.component) {
      if (page.component != null) {
        removeChild(page.component);
      }
      page.component = component;
      boolean selectedPage = (getSelectedIndex() == index);
      if (selectedPage) {
        this.visComp = component;
      }
      if (component != null) {
        component.setVisible(selectedPage);
        addImpl(component, null, -1);
      } else {
        firePropertyChange("indexForNullComponent", -1, index);
      }
      revalidate();
      repaint();
    }
  }

  public void setDisplayedMnemonicIndexAt(int tabIndex, int mnemonicIndex) {
    checkIndex(tabIndex);
    pages.get(tabIndex).setDisplayedMnemonicIndex(mnemonicIndex);
    firePropertyChange("displayedMnemonicIndexAt", null, null);
  }

  public void setMnemonicAt(int tabIndex, int mnemonic) {
    checkIndex(tabIndex);
    pages.get(tabIndex).setMnemonic(mnemonic);
    firePropertyChange("mnemonicAt", null, null);
  }

  public int indexOfTab(String title) {
    for (int i = 0; i < getTabCount(); i++) {
      if (getTitleAt(i).equals(title == null ? "" : title)) {
        return i;
      }
    }
    return -1;
  }

  public int indexOfTab(Icon icon) {
    for (int i = 0; i < getTabCount(); i++) {
      Icon tabIcon = getIconAt(i);
      if ((tabIcon != null && tabIcon.equals(icon)) || (tabIcon == null && tabIcon == icon)) {
        return i;
      }
    }
    return -1;
  }

  public int indexOfComponent(Component component) {
    for (int i = 0; i < getTabCount(); i++) {
      Component c = getComponentAt(i);
      if ((c != null && c.equals(component)) || (c == null && c == component)) {
        return i;
      }
    }
    return -1;
  }

  public int indexAtLocation(int x, int y) {
    if (ui != null) {
      return getUI().tabForCoordinate(this, x, y);
    }
    return -1;
  }

  public String getToolTipText(MouseEvent event) {
    if (ui != null) {
      int index = getUI().tabForCoordinate(this, event.getX(), event.getY());
      if (index != -1) {
        return pages.get(index).tip;
      }
    }
    return super.getToolTipText(event);
  }

  private void checkIndex(int index) {
    if (index < 0 || index >= pages.size()) {
      throw new IndexOutOfBoundsException("Index: " + index + ", Tab count: " + pages.size());
    }
  }

  protected String paramString() {
    String tabPlacementString;
    if (tabPlacement == TOP) {
      tabPlacementString = "TOP";
    } else if (tabPlacement == BOTTOM) {
      tabPlacementString = "BOTTOM";
    } else if (tabPlacement == LEFT) {
      tabPlacementString = "LEFT";
    } else if (tabPlacement == RIGHT) {
      tabPlacementString = "RIGHT";
    } else {
      tabPlacementString = "";
    }
    String haveRegisteredString = haveRegistered ? "true" : "false";
    return super.paramString() + ",haveRegistered=" + haveRegisteredString + ",tabPlacement=" + tabPlacementString;
  }

  public AccessibleContext getAccessibleContext() {
    if (accessibleContext == null) {
      accessibleContext = new AccessibleJTabbedPane();
      for (int i = 0; i < getTabCount(); i++) {
        pages.get(i).initAccessibleContext();
      }
    }
    return accessibleContext;
  }

  public void setTabComponentAt(int index, Component component) {
    if (component != null && indexOfComponent(component) != -1) {
      throw new IllegalArgumentException("Component is already added to this JTabbedPane");
    }
    Component oldValue = getTabComponentAt(index);
    if (component != oldValue) {
      int tabComponentIndex = indexOfTabComponent(component);
      if (tabComponentIndex != -1) {
        setTabComponentAt(tabComponentIndex, null);
      }
      pages.get(index).tabComponent = component;
      firePropertyChange("indexForTabComponent", -1, index);
    }
  }

  public Component getTabComponentAt(int i) { return pages.get(i).tabComponent; }

  public int indexOfTabComponent(Component tabComponent) {
    for (int i = 0; i < getTabCount(); i++) {
      Component c = getTabComponentAt(i);
      if (c == tabComponent) {
        return i;
      }
    }
    return -1;
  }

  protected class AccessibleJTabbedPane extends AccessibleJComponent implements AccessibleSelection, ChangeListener {
    public String getAccessibleName() {
      if (accessibleName != null) {
        return accessibleName;
      }
      String cp = (String) getClientProperty(AccessibleContext.ACCESSIBLE_NAME_PROPERTY);
      if (cp != null) {
        return cp;
      }
      int index = getSelectedIndex();
      if (index >= 0) {
        return pages.get(index).getAccessibleName();
      }
      return super.getAccessibleName();
    }

    public AccessibleJTabbedPane() {
      super();
      JTabbedPane.this.model.addChangeListener(this);
    }

    public void stateChanged(ChangeEvent e) {
      Object o = e.getSource();
      firePropertyChange(AccessibleContext.ACCESSIBLE_SELECTION_PROPERTY, null, o);
    }

    public AccessibleRole getAccessibleRole() { return AccessibleRole.PAGE_TAB_LIST; }

    public int getAccessibleChildrenCount() { return getTabCount(); }

    public Accessible getAccessibleChild(int i) {
      if (i < 0 || i >= getTabCount()) {
        return null;
      }
      return pages.get(i);
    }

    public AccessibleSelection getAccessibleSelection() { return this; }

    public Accessible getAccessibleAt(java.awt.Point p) {
      int tab = ((TabbedPaneUI) ui).tabForCoordinate(JTabbedPane.this, p.x, p.y);
      if (tab == -1) {
        tab = getSelectedIndex();
      }
      return getAccessibleChild(tab);
    }

    public int getAccessibleSelectionCount() { return 1; }

    public Accessible getAccessibleSelection(int i) {
      int index = getSelectedIndex();
      if (index == -1) {
        return null;
      }
      return pages.get(index);
    }

    public boolean isAccessibleChildSelected(int i) { return (i == getSelectedIndex()); }

    public void addAccessibleSelection(int i) { setSelectedIndex(i); }

    public void removeAccessibleSelection(int i) { }

    public void clearAccessibleSelection() { }

    public void selectAllAccessibleSelection() { }
  }

  private class Page extends AccessibleContext implements Serializable, Accessible {
    String title;
    Color background;
    Color foreground;
    Icon icon;
    Icon disabledIcon;
    JTabbedPane parent;
    Component component;
    String tip;
    boolean enabled = true;
    boolean needsUIUpdate;
    int mnemonic = -1;
    int mnemonicIndex = -1;
    Component tabComponent;

    Page(JTabbedPane parent, String title, Icon icon, Icon disabledIcon, Component component, String tip) {
      this.title = title;
      this.icon = icon;
      this.disabledIcon = disabledIcon;
      this.parent = parent;
      this.setAccessibleParent(parent);
      this.component = component;
      this.tip = tip;
      initAccessibleContext();
    }

    void initAccessibleContext() {
      if (JTabbedPane.this.accessibleContext != null && component instanceof Accessible) {
        AccessibleContext ac = component.getAccessibleContext();
        if (ac != null) {
          ac.setAccessibleParent(this);
        }
      }
    }

    void setMnemonic(int mnemonic) {
      this.mnemonic = mnemonic;
      updateDisplayedMnemonicIndex();
    }

    int getMnemonic() { return mnemonic; }

    void setDisplayedMnemonicIndex(int mnemonicIndex) {
      if (this.mnemonicIndex != mnemonicIndex) {
        if (mnemonicIndex != -1 && title != null && (mnemonicIndex < 0 || mnemonicIndex >= title.length())) {
          throw new IllegalArgumentException("Invalid mnemonic index: " + mnemonicIndex);
        }
        this.mnemonicIndex = mnemonicIndex;
      }
    }

    int getDisplayedMnemonicIndex() { return this.mnemonicIndex; }

    void updateDisplayedMnemonicIndex() {
      setDisplayedMnemonicIndex(SwingUtilities.findDisplayedMnemonicIndex(title, mnemonic));
    }

    Color getBackground() { return background != null ? background : parent.getBackground(); }

    void setBackground(Color c) { background = c; }

    Color getForeground() { return foreground != null ? foreground : parent.getForeground(); }

    void setForeground(Color c) { foreground = c; }

    boolean isEnabled() { return enabled; }

    void setEnabled(boolean e) { enabled = e; }

    public AccessibleContext getAccessibleContext() { return this; }

    public String getAccessibleName() {
      if (accessibleName != null) {
        return accessibleName;
      } else if (parent.getTitleAt(parent.indexOfComponent(component)) != null) {
        return parent.getTitleAt(parent.indexOfComponent(component));
      }
      return null;
    }

    public String getAccessibleDescription() {
      if (accessibleDescription != null) {
        return accessibleDescription;
      } else if (tip != null) {
        return tip;
      }
      return null;
    }

    public AccessibleRole getAccessibleRole() { return AccessibleRole.PAGE_TAB; }

    public AccessibleStateSet getAccessibleStateSet() {
      AccessibleStateSet states = parent.getAccessibleContext().getAccessibleStateSet();
      states.add(javax.accessibility.AccessibleState.SELECTABLE);
      if (parent.indexOfComponent(component) == parent.getSelectedIndex()) {
        states.add(javax.accessibility.AccessibleState.SELECTED);
      }
      return states;
    }

    public int getAccessibleIndexInParent() { return parent.indexOfComponent(component); }

    public int getAccessibleChildrenCount() {
      return component instanceof Accessible ? 1 : 0;
    }

    public Accessible getAccessibleChild(int i) {
      return component instanceof Accessible && i == 0 ? (Accessible) component : null;
    }

    public java.util.Locale getLocale() { return parent.getLocale(); }
  }
}
