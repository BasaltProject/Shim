/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing;

import java.awt.AWTEvent;
import java.awt.ItemSelectable;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Vector;
import javax.accessibility.Accessible;
import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleRole;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.plaf.ComboBoxUI;

public class JComboBox<E> extends JComponent implements ItemSelectable, ListDataListener, ActionListener,
    Accessible {
  protected ComboBoxModel<E> dataModel;
  protected ListCellRenderer<? super E> renderer;
  protected ComboBoxEditor editor;
  protected int maximumRowCount = 8;
  protected boolean isEditable = false;
  protected KeySelectionManager keySelectionManager = null;
  protected String actionCommand = "comboBoxChanged";
  protected boolean lightWeightPopupEnabled = JPopupMenu.getDefaultLightWeightPopupEnabled();
  protected Object selectedItemReminder = null;

  private E prototypeDisplayValue;
  private boolean firingActionEvent = false;
  private boolean selectingItem = false;
  private Action action;
  private PropertyChangeListener actionPropertyChangeListener;
  private boolean popupVisible;
  private AccessibleContext accessibleContext;

  public interface KeySelectionManager {
    int selectionForKey(char aKey, ComboBoxModel<?> aModel);
  }

  public JComboBox(ComboBoxModel<E> aModel) {
    super();
    setModel(aModel);
    init();
  }

  public JComboBox(final E[] items) {
    super();
    setModel(new DefaultComboBoxModel<E>(items));
    init();
  }

  public JComboBox(Vector<E> items) {
    super();
    setModel(new DefaultComboBoxModel<E>(items));
    init();
  }

  public JComboBox() {
    super();
    setModel(new DefaultComboBoxModel<E>());
    init();
  }

  private void init() {
    installAncestorListener();
    updateUI();
  }

  protected void installAncestorListener() { }

  public void setUI(ComboBoxUI ui) { super.setUI(ui); }

  public void updateUI() {
    setUI((javax.swing.plaf.ComponentUI) UIManager.getUI(this));
    ListCellRenderer<?> renderer = getRenderer();
    if (renderer instanceof java.awt.Component) {
      SwingUtilities.updateComponentTreeUI((java.awt.Component) renderer);
    }
  }

  public String getUIClassID() { return "ComboBoxUI"; }

  public ComboBoxUI getUI() { return (ComboBoxUI) ui; }

  public void setModel(ComboBoxModel<E> aModel) {
    ComboBoxModel<E> oldModel = dataModel;
    if (oldModel != null) {
      oldModel.removeListDataListener(this);
    }
    dataModel = aModel;
    dataModel.addListDataListener(this);
    selectedItemReminder = dataModel.getSelectedItem();
    firePropertyChange("model", oldModel, dataModel);
  }

  public ComboBoxModel<E> getModel() { return dataModel; }

  public void setLightWeightPopupEnabled(boolean aFlag) {
    boolean oldFlag = lightWeightPopupEnabled;
    lightWeightPopupEnabled = aFlag;
    firePropertyChange("lightWeightPopupEnabled", oldFlag, lightWeightPopupEnabled);
  }

  public boolean isLightWeightPopupEnabled() { return lightWeightPopupEnabled; }

  public void setEditable(boolean aFlag) {
    boolean oldFlag = isEditable;
    isEditable = aFlag;
    firePropertyChange("editable", oldFlag, isEditable);
  }

  public boolean isEditable() { return isEditable; }

  public void setMaximumRowCount(int count) {
    int oldCount = maximumRowCount;
    maximumRowCount = count;
    firePropertyChange("maximumRowCount", oldCount, maximumRowCount);
  }

  public int getMaximumRowCount() { return maximumRowCount; }

  public void setRenderer(ListCellRenderer<? super E> aRenderer) {
    ListCellRenderer<? super E> oldRenderer = renderer;
    renderer = aRenderer;
    firePropertyChange("renderer", oldRenderer, renderer);
    invalidate();
  }

  public ListCellRenderer<? super E> getRenderer() { return renderer; }

  public void setEditor(ComboBoxEditor anEditor) {
    ComboBoxEditor oldEditor = editor;
    if (editor != null) {
      editor.removeActionListener(this);
    }
    editor = anEditor;
    if (editor != null) {
      editor.addActionListener(this);
    }
    firePropertyChange("editor", oldEditor, editor);
  }

  public ComboBoxEditor getEditor() { return editor; }

  public void setSelectedItem(Object anObject) {
    Object oldSelection = selectedItemReminder;
    Object objectToSelect = anObject;
    if (oldSelection == null || !oldSelection.equals(anObject)) {
      if (anObject != null && !isEditable()) {
        boolean found = false;
        for (int i = 0; i < dataModel.getSize(); i++) {
          E element = dataModel.getElementAt(i);
          if (anObject.equals(element)) {
            found = true;
            objectToSelect = element;
            break;
          }
        }
        if (!found) {
          return;
        }
      }
      selectingItem = true;
      dataModel.setSelectedItem(objectToSelect);
      selectingItem = false;
      if (selectedItemReminder != dataModel.getSelectedItem()) {
        selectedItemChanged();
      }
    }
    fireActionEvent();
  }

  public Object getSelectedItem() { return dataModel.getSelectedItem(); }

  public void setSelectedIndex(int anIndex) {
    int size = dataModel.getSize();
    if (anIndex == -1) {
      setSelectedItem(null);
    } else if (anIndex < -1 || anIndex >= size) {
      throw new IllegalArgumentException("setSelectedIndex: " + anIndex + " out of bounds");
    } else {
      setSelectedItem(dataModel.getElementAt(anIndex));
    }
  }

  public int getSelectedIndex() {
    Object sObject = dataModel.getSelectedItem();
    int i;
    int c;
    E obj;
    for (i = 0, c = dataModel.getSize(); i < c; i++) {
      obj = dataModel.getElementAt(i);
      if (obj != null && obj.equals(sObject)) {
        return i;
      }
    }
    return -1;
  }

  public E getPrototypeDisplayValue() { return prototypeDisplayValue; }

  public void setPrototypeDisplayValue(E prototypeDisplayValue) {
    Object oldValue = this.prototypeDisplayValue;
    this.prototypeDisplayValue = prototypeDisplayValue;
    firePropertyChange("prototypeDisplayValue", oldValue, prototypeDisplayValue);
  }

  public void addItem(E item) {
    checkMutableComboBoxModel();
    ((MutableComboBoxModel<E>) dataModel).addElement(item);
  }

  public void insertItemAt(E item, int index) {
    checkMutableComboBoxModel();
    ((MutableComboBoxModel<E>) dataModel).insertElementAt(item, index);
  }

  public void removeItem(Object anObject) {
    checkMutableComboBoxModel();
    ((MutableComboBoxModel<?>) dataModel).removeElement(anObject);
  }

  public void removeItemAt(int anIndex) {
    checkMutableComboBoxModel();
    ((MutableComboBoxModel<?>) dataModel).removeElementAt(anIndex);
  }

  public void removeAllItems() {
    checkMutableComboBoxModel();
    MutableComboBoxModel<E> model = (MutableComboBoxModel<E>) dataModel;
    int size = model.getSize();
    if (model instanceof DefaultComboBoxModel) {
      ((DefaultComboBoxModel<?>) model).removeAllElements();
    } else {
      for (int i = 0; i < size; ++i) {
        E element = model.getElementAt(0);
        model.removeElement(element);
      }
    }
    selectedItemReminder = null;
    if (isEditable()) {
      editor.setItem(null);
    }
  }

  void checkMutableComboBoxModel() {
    if (!(dataModel instanceof MutableComboBoxModel)) {
      throw new RuntimeException("Cannot use this method with a non-Mutable data model.");
    }
  }

  public void showPopup() { setPopupVisible(true); }

  public void hidePopup() { setPopupVisible(false); }

  public void setPopupVisible(boolean v) {
    if (getUI() != null) {
      getUI().setPopupVisible(this, v);
      return;
    }
    popupVisible = v;
    if (v) {
      firePopupMenuWillBecomeVisible();
    } else {
      firePopupMenuWillBecomeInvisible();
    }
  }

  public boolean isPopupVisible() { return getUI() != null ? getUI().isPopupVisible(this) : popupVisible; }

  public void addItemListener(ItemListener aListener) { listenerList.add(ItemListener.class, aListener); }

  public void removeItemListener(ItemListener aListener) { listenerList.remove(ItemListener.class, aListener); }

  public ItemListener[] getItemListeners() { return listenerList.getListeners(ItemListener.class); }

  public void addActionListener(ActionListener l) { listenerList.add(ActionListener.class, l); }

  public void removeActionListener(ActionListener l) {
    if ((l != null) && (getAction() == l)) {
      setAction(null);
    } else {
      listenerList.remove(ActionListener.class, l);
    }
  }

  public ActionListener[] getActionListeners() { return listenerList.getListeners(ActionListener.class); }

  public void addPopupMenuListener(PopupMenuListener l) { listenerList.add(PopupMenuListener.class, l); }

  public void removePopupMenuListener(PopupMenuListener l) { listenerList.remove(PopupMenuListener.class, l); }

  public PopupMenuListener[] getPopupMenuListeners() { return listenerList.getListeners(PopupMenuListener.class); }

  public void firePopupMenuWillBecomeVisible() {
    Object[] listeners = listenerList.getListenerList();
    PopupMenuEvent e = null;
    for (int i = listeners.length - 2; i >= 0; i -= 2) {
      if (listeners[i] == PopupMenuListener.class) {
        if (e == null) {
          e = new PopupMenuEvent(this);
        }
        ((PopupMenuListener) listeners[i + 1]).popupMenuWillBecomeVisible(e);
      }
    }
  }

  public void firePopupMenuWillBecomeInvisible() {
    Object[] listeners = listenerList.getListenerList();
    PopupMenuEvent e = null;
    for (int i = listeners.length - 2; i >= 0; i -= 2) {
      if (listeners[i] == PopupMenuListener.class) {
        if (e == null) {
          e = new PopupMenuEvent(this);
        }
        ((PopupMenuListener) listeners[i + 1]).popupMenuWillBecomeInvisible(e);
      }
    }
  }

  public void firePopupMenuCanceled() {
    Object[] listeners = listenerList.getListenerList();
    PopupMenuEvent e = null;
    for (int i = listeners.length - 2; i >= 0; i -= 2) {
      if (listeners[i] == PopupMenuListener.class) {
        if (e == null) {
          e = new PopupMenuEvent(this);
        }
        ((PopupMenuListener) listeners[i + 1]).popupMenuCanceled(e);
      }
    }
  }

  public void setActionCommand(String aCommand) { actionCommand = aCommand; }

  public String getActionCommand() { return actionCommand; }

  public void setAction(Action a) {
    Action oldValue = getAction();
    if (action == null || !action.equals(a)) {
      action = a;
      if (oldValue != null) {
        removeActionListener(oldValue);
        oldValue.removePropertyChangeListener(actionPropertyChangeListener);
        actionPropertyChangeListener = null;
      }
      configurePropertiesFromAction(action);
      if (action != null) {
        if (!isListener(ActionListener.class, action)) {
          addActionListener(action);
        }
        actionPropertyChangeListener = createActionPropertyChangeListener(action);
        action.addPropertyChangeListener(actionPropertyChangeListener);
      }
      firePropertyChange("action", oldValue, action);
    }
  }

  private boolean isListener(Class<?> c, ActionListener a) {
    boolean isListener = false;
    Object[] listeners = listenerList.getListenerList();
    for (int i = listeners.length - 2; i >= 0; i -= 2) {
      if (listeners[i] == c && listeners[i + 1] == a) {
        isListener = true;
      }
    }
    return isListener;
  }

  public Action getAction() { return action; }

  protected void configurePropertiesFromAction(Action a) {
    setEnabled(a == null || a.isEnabled());
    setToolTipText(a == null ? null : (String) a.getValue(Action.SHORT_DESCRIPTION));
    setActionCommand(a == null ? null : (String) a.getValue(Action.ACTION_COMMAND_KEY));
  }

  protected PropertyChangeListener createActionPropertyChangeListener(final Action a) {
    return new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent e) {
        String propertyName = e.getPropertyName();
        if (getAction() == null) {
          return;
        }
        actionPropertyChanged(a, propertyName);
      }
    };
  }

  protected void actionPropertyChanged(Action action, String propertyName) {
    if (propertyName == Action.ACTION_COMMAND_KEY) {
      setActionCommand((String) action.getValue(Action.ACTION_COMMAND_KEY));
    } else if (propertyName == "enabled") {
      setEnabled(action.isEnabled());
    } else if (propertyName == Action.SHORT_DESCRIPTION) {
      setToolTipText((String) action.getValue(Action.SHORT_DESCRIPTION));
    }
  }

  protected void fireItemStateChanged(ItemEvent e) {
    Object[] listeners = listenerList.getListenerList();
    for (int i = listeners.length - 2; i >= 0; i -= 2) {
      if (listeners[i] == ItemListener.class) {
        ((ItemListener) listeners[i + 1]).itemStateChanged(e);
      }
    }
  }

  protected void fireActionEvent() {
    if (!firingActionEvent) {
      firingActionEvent = true;
      ActionEvent e = null;
      Object[] listeners = listenerList.getListenerList();
      long mostRecentEventTime = EventQueue.getMostRecentEventTime();
      int modifiers = 0;
      AWTEvent currentEvent = EventQueue.getCurrentEvent();
      if (currentEvent instanceof InputEvent) {
        modifiers = ((InputEvent) currentEvent).getModifiers();
      } else if (currentEvent instanceof ActionEvent) {
        modifiers = ((ActionEvent) currentEvent).getModifiers();
      }
      for (int i = listeners.length - 2; i >= 0; i -= 2) {
        if (listeners[i] == ActionListener.class) {
          if (e == null) {
            e = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, getActionCommand(), mostRecentEventTime,
                modifiers);
          }
          ((ActionListener) listeners[i + 1]).actionPerformed(e);
        }
      }
      firingActionEvent = false;
    }
  }

  protected void selectedItemChanged() {
    if (selectedItemReminder != null) {
      fireItemStateChanged(new ItemEvent(this, ItemEvent.ITEM_STATE_CHANGED, selectedItemReminder,
          ItemEvent.DESELECTED));
    }
    selectedItemReminder = dataModel.getSelectedItem();
    if (selectedItemReminder != null) {
      fireItemStateChanged(new ItemEvent(this, ItemEvent.ITEM_STATE_CHANGED, selectedItemReminder,
          ItemEvent.SELECTED));
    }
  }

  public Object[] getSelectedObjects() {
    Object selectedObject = getSelectedItem();
    if (selectedObject == null) {
      return new Object[0];
    } else {
      Object[] result = new Object[1];
      result[0] = selectedObject;
      return result;
    }
  }

  public void actionPerformed(ActionEvent e) {
    ComboBoxEditor editor = getEditor();
    if ((editor != null) && (e != null) && (editor == e.getSource() || editor.getEditorComponent() == e.getSource())) {
      setPopupVisible(false);
      getModel().setSelectedItem(editor.getItem());
      String oldCommand = getActionCommand();
      setActionCommand("comboBoxEdited");
      fireActionEvent();
      setActionCommand(oldCommand);
    }
  }

  public void contentsChanged(ListDataEvent e) {
    Object oldSelection = selectedItemReminder;
    Object newSelection = dataModel.getSelectedItem();
    if (oldSelection == null || !oldSelection.equals(newSelection)) {
      selectedItemChanged();
      if (!selectingItem) {
        fireActionEvent();
      }
    }
  }

  public void intervalAdded(ListDataEvent e) {
    if (selectedItemReminder != dataModel.getSelectedItem()) {
      selectedItemChanged();
    }
  }

  public void intervalRemoved(ListDataEvent e) { contentsChanged(e); }

  public E getItemAt(int index) { return dataModel.getElementAt(index); }

  public int getItemCount() { return dataModel.getSize(); }

  public void configureEditor(ComboBoxEditor anEditor, Object anItem) { anEditor.setItem(anItem); }

  public void processKeyEvent(KeyEvent e) {
    if (e.getKeyCode() == KeyEvent.VK_TAB) {
      hidePopup();
    }
    super.processKeyEvent(e);
  }

  protected boolean processKeyBinding(KeyStroke ks, KeyEvent e, int condition, boolean pressed) {
    if (super.processKeyBinding(ks, e, condition, pressed)) {
      return true;
    }
    if (!isEditable() || condition != WHEN_FOCUSED || getEditor() == null
        || !Boolean.TRUE.equals(getClientProperty("JComboBox.isTableCellEditor"))) {
      return false;
    }
    java.awt.Component editorComponent = getEditor().getEditorComponent();
    if (editorComponent instanceof JComponent) {
      JComponent component = (JComponent) editorComponent;
      return component.processKeyBinding(ks, e, WHEN_FOCUSED, pressed);
    }
    return false;
  }

  public void setKeySelectionManager(KeySelectionManager aManager) { keySelectionManager = aManager; }

  public KeySelectionManager getKeySelectionManager() { return keySelectionManager; }

  public boolean selectWithKeyChar(char keyChar) {
    int index;
    if (keySelectionManager == null) {
      keySelectionManager = createDefaultKeySelectionManager();
    }
    index = keySelectionManager.selectionForKey(keyChar, getModel());
    if (index != -1) {
      setSelectedIndex(index);
      return true;
    } else {
      return false;
    }
  }

  protected KeySelectionManager createDefaultKeySelectionManager() { return new DefaultKeySelectionManager(); }

  class DefaultKeySelectionManager implements KeySelectionManager, java.io.Serializable {
    public int selectionForKey(char aKey, ComboBoxModel<?> aModel) {
      int i;
      int c;
      int currentSelection = -1;
      Object selectedItem = aModel.getSelectedItem();
      String v;
      String pattern;
      if (selectedItem != null) {
        for (i = 0, c = aModel.getSize(); i < c; i++) {
          if (selectedItem == aModel.getElementAt(i)) {
            currentSelection = i;
            break;
          }
        }
      }
      pattern = ("" + aKey).toLowerCase();
      aKey = pattern.charAt(0);
      for (i = ++currentSelection, c = aModel.getSize(); i < c; i++) {
        Object elem = aModel.getElementAt(i);
        if (elem != null && elem.toString() != null) {
          v = elem.toString().toLowerCase();
          if (v.length() > 0 && v.charAt(0) == aKey) {
            return i;
          }
        }
      }
      for (i = 0; i < currentSelection; i++) {
        Object elem = aModel.getElementAt(i);
        if (elem != null && elem.toString() != null) {
          v = elem.toString().toLowerCase();
          if (v.length() > 0 && v.charAt(0) == aKey) {
            return i;
          }
        }
      }
      return -1;
    }
  }

  protected String paramString() {
    String selectedItemReminderString = (selectedItemReminder != null ? selectedItemReminder.toString() : "");
    String isEditableString = (isEditable ? "true" : "false");
    String lightWeightPopupEnabledString = (lightWeightPopupEnabled ? "true" : "false");
    return super.paramString() + ",isEditable=" + isEditableString + ",lightWeightPopupEnabled="
        + lightWeightPopupEnabledString + ",maximumRowCount=" + maximumRowCount + ",selectedItemReminder="
        + selectedItemReminderString;
  }

  public AccessibleContext getAccessibleContext() {
    if (accessibleContext == null) {
      accessibleContext = new AccessibleJComboBox();
    }
    return accessibleContext;
  }

  protected class AccessibleJComboBox extends AccessibleJComponent {
    public AccessibleRole getAccessibleRole() { return AccessibleRole.COMBO_BOX; }

    public int getAccessibleChildrenCount() { return 0; }

    public Accessible getAccessibleChild(int i) { return null; }
  }
}
