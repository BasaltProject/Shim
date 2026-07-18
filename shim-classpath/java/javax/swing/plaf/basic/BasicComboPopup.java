/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */


package javax.swing.plaf.basic;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseMotionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.accessibility.AccessibleContext;
import javax.swing.BorderFactory;
import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.MenuSelectionManager;
import javax.swing.MenuElement;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

public class BasicComboPopup extends JPopupMenu implements ComboPopup {
  protected JComboBox<Object> comboBox;
  protected JList<Object> list;
  protected JScrollPane scroller;
  protected boolean valueIsAdjusting = false;
  protected MouseMotionListener mouseMotionListener;
  protected MouseListener mouseListener;
  protected KeyListener keyListener;
  protected ListSelectionListener listSelectionListener;
  protected MouseListener listMouseListener;
  protected MouseMotionListener listMouseMotionListener;
  protected PropertyChangeListener propertyChangeListener;
  protected ListDataListener listDataListener;
  protected ItemListener itemListener;
  protected Timer autoscrollTimer;
  protected boolean hasEntered = false;
  protected boolean isAutoScrolling = false;
  protected int scrollDirection = SCROLL_UP;
  protected static final int SCROLL_UP = 0;
  protected static final int SCROLL_DOWN = 1;

  public BasicComboPopup(JComboBox<Object> combo) {
    super();
    setName("ComboPopup.popup");
    comboBox = combo;
    setLightWeightPopupEnabled(comboBox.isLightWeightPopupEnabled());
    list = createList();
    list.setName("ComboBox.list");
    configureList();
    scroller = createScroller();
    scroller.setName("ComboBox.scrollPane");
    configureScroller();
    configurePopup();
    installComboBoxListeners();
    installKeyboardActions();
  }

  protected void firePopupMenuWillBecomeVisible() {
    comboBox.firePopupMenuWillBecomeVisible();
  }

  protected void firePopupMenuWillBecomeInvisible() {
    comboBox.firePopupMenuWillBecomeInvisible();
  }

  protected void firePopupMenuCanceled() {
    comboBox.firePopupMenuCanceled();
  }

  public void show() {
    comboBox.firePopupMenuWillBecomeVisible();
    setListSelection(comboBox.getSelectedIndex());
    Point location = getPopupLocation();
    show(comboBox, location.x, location.y);
  }

  public void hide() {
    MenuSelectionManager msm = MenuSelectionManager.defaultManager();
    MenuElement[] selection = msm.getSelectedPath();
    for (int i = 0; i < selection.length; i++) {
      if (selection[i] == this) {
        msm.clearSelectedPath();
        break;
      }
    }
    if (selection.length > 0) {
      comboBox.repaint();
    }
  }

  public JList<Object> getList() {
    return list;
  }

  public MouseListener getMouseListener() {
    if (mouseListener == null) {
      mouseListener = createMouseListener();
    }
    return mouseListener;
  }

  public MouseMotionListener getMouseMotionListener() {
    if (mouseMotionListener == null) {
      mouseMotionListener = createMouseMotionListener();
    }
    return mouseMotionListener;
  }

  public KeyListener getKeyListener() {
    if (keyListener == null) {
      keyListener = createKeyListener();
    }
    return keyListener;
  }

  public void uninstallingUI() {
    if (propertyChangeListener != null) {
      comboBox.removePropertyChangeListener(propertyChangeListener);
    }
    if (itemListener != null) {
      comboBox.removeItemListener(itemListener);
    }
    uninstallComboBoxModelListeners(comboBox.getModel());
    uninstallKeyboardActions();
    uninstallListListeners();
    uninstallListSelectionModelListeners();
    mouseListener = null;
    mouseMotionListener = null;
    keyListener = null;
    listMouseListener = null;
    listMouseMotionListener = null;
    listSelectionListener = null;
    listDataListener = null;
    propertyChangeListener = null;
    itemListener = null;
  }

  protected void uninstallComboBoxModelListeners(ComboBoxModel<?> model) {
    if (model != null && listDataListener != null) {
      model.removeListDataListener(listDataListener);
    }
  }

  protected void uninstallKeyboardActions() {
  }

  private void uninstallListListeners() {
    if (listMouseListener != null) {
      list.removeMouseListener(listMouseListener);
    }
    if (listMouseMotionListener != null) {
      list.removeMouseMotionListener(listMouseMotionListener);
    }
  }

  private void uninstallListSelectionModelListeners() {
    if (listSelectionListener != null) {
      list.getSelectionModel().removeListSelectionListener(listSelectionListener);
    }
  }

  protected MouseListener createMouseListener() {
    return new InvocationMouseHandler();
  }

  protected MouseMotionListener createMouseMotionListener() {
    return new InvocationMouseMotionHandler();
  }

  protected KeyListener createKeyListener() {
    return new InvocationKeyHandler();
  }

  protected ListSelectionListener createListSelectionListener() {
    return null;
  }

  protected ListDataListener createListDataListener() {
    return null;
  }

  protected MouseListener createListMouseListener() {
    return new ListMouseHandler();
  }

  protected MouseMotionListener createListMouseMotionListener() {
    return new ListMouseMotionHandler();
  }

  protected PropertyChangeListener createPropertyChangeListener() {
    return new PropertyChangeHandler();
  }

  protected ItemListener createItemListener() {
    return new ItemHandler();
  }

  @SuppressWarnings("unchecked")
  protected JList<Object> createList() {
    return new JList<Object>((ComboBoxModel<Object>) comboBox.getModel()) {
      public void processMouseEvent(MouseEvent e) {
        super.processMouseEvent(e);
      }
    };
  }

  protected void configureList() {
    list.setFont(comboBox.getFont());
    list.setForeground(comboBox.getForeground());
    list.setBackground(comboBox.getBackground());
    list.setSelectionForeground(UIManager.getColor("ComboBox.selectionForeground"));
    list.setSelectionBackground(UIManager.getColor("ComboBox.selectionBackground"));
    list.setBorder(null);
    list.setCellRenderer(comboBox.getRenderer());
    list.setFocusable(false);
    list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    setListSelection(comboBox.getSelectedIndex());
    installListListeners();
  }

  protected void installListListeners() {
    if ((listMouseListener = createListMouseListener()) != null) {
      list.addMouseListener(listMouseListener);
    }
    if ((listMouseMotionListener = createListMouseMotionListener()) != null) {
      list.addMouseMotionListener(listMouseMotionListener);
    }
  }

  private void setListSelection(int selectedIndex) {
    if (selectedIndex == -1) {
      list.clearSelection();
    } else {
      list.setSelectedIndex(selectedIndex);
      list.ensureIndexIsVisible(selectedIndex);
    }
  }

  protected JScrollPane createScroller() {
    JScrollPane sp = new JScrollPane(list, javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
        javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    sp.setHorizontalScrollBar(null);
    return sp;
  }

  protected void configureScroller() {
    scroller.setFocusable(false);
    scroller.getVerticalScrollBar().setFocusable(false);
    scroller.setBorder(null);
  }

  protected void configurePopup() {
    setLayout(new javax.swing.BoxLayout(this, javax.swing.BoxLayout.Y_AXIS));
    setBorderPainted(true);
    setBorder(BorderFactory.createLineBorder(java.awt.Color.BLACK));
    setOpaque(false);
    add(scroller);
    setDoubleBuffered(true);
    setFocusable(false);
    addPopupMenuListener(new PopupMenuListener() {
      public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
      }

      public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
        comboBox.firePopupMenuWillBecomeInvisible();
      }

      public void popupMenuCanceled(PopupMenuEvent e) {
        comboBox.firePopupMenuCanceled();
      }
    });
  }

  protected void installComboBoxListeners() {
    if ((propertyChangeListener = createPropertyChangeListener()) != null) {
      comboBox.addPropertyChangeListener(propertyChangeListener);
    }
    if ((itemListener = createItemListener()) != null) {
      comboBox.addItemListener(itemListener);
    }
    installComboBoxModelListeners(comboBox.getModel());
  }

  protected void installComboBoxModelListeners(ComboBoxModel<?> model) {
    if (model != null && (listDataListener = createListDataListener()) != null) {
      model.addListDataListener(listDataListener);
    }
  }

  protected void installKeyboardActions() {
  }

  protected void togglePopup() {
    if (isVisible()) {
      hide();
    } else {
      show();
    }
  }

  protected void delegateFocus(MouseEvent e) {
    if (comboBox.isEditable()) {
      Component comp = comboBox.getEditor().getEditorComponent();
      if (!(comp instanceof javax.swing.JComponent) || ((javax.swing.JComponent) comp).isRequestFocusEnabled()) {
        comp.requestFocus();
      }
    } else if (comboBox.isRequestFocusEnabled()) {
      comboBox.requestFocus();
    }
  }

  protected MouseEvent convertMouseEvent(MouseEvent e) {
    Point convertedPoint = javax.swing.SwingUtilities.convertPoint((Component) e.getSource(), e.getPoint(), list);
    return new MouseEvent((Component) e.getSource(), e.getID(), e.getWhen(), e.getModifiers(), convertedPoint.x,
        convertedPoint.y, e.getXOnScreen(), e.getYOnScreen(), e.getClickCount(), e.isPopupTrigger(),
        MouseEvent.NOBUTTON);
  }

  protected int getPopupHeightForRowCount(int maxRowCount) {
    int minRowCount = Math.min(maxRowCount, comboBox.getItemCount());
    int height = 0;
    ListCellRenderer<Object> renderer = list.getCellRenderer();
    Object value = null;
    for (int i = 0; i < minRowCount; ++i) {
      value = list.getModel().getElementAt(i);
      Component c = renderer.getListCellRendererComponent(list, value, i, false, false);
      height += c.getPreferredSize().height;
    }
    if (height == 0) {
      height = comboBox.getHeight();
    }
    Insets insets = list.getInsets();
    return height + insets.top + insets.bottom;
  }

  protected Rectangle computePopupBounds(int px, int py, int pw, int ph) {
    return new Rectangle(px, py, pw, ph);
  }

  private Point getPopupLocation() {
    Dimension popupSize = comboBox.getSize();
    Insets insets = getInsets();
    popupSize.setSize(popupSize.width - (insets.right + insets.left), getPopupHeightForRowCount(
        comboBox.getMaximumRowCount()));
    Rectangle popupBounds = computePopupBounds(0, comboBox.getBounds().height, popupSize.width, popupSize.height);
    Dimension scrollSize = popupBounds.getSize();
    Point popupLocation = popupBounds.getLocation();
    scroller.setMaximumSize(scrollSize);
    scroller.setPreferredSize(scrollSize);
    scroller.setMinimumSize(scrollSize);
    list.revalidate();
    return popupLocation;
  }

  protected void updateListBoxSelectionForEvent(MouseEvent anEvent, boolean shouldScroll) {
    Point location = anEvent.getPoint();
    if (list == null) {
      return;
    }
    int index = list.locationToIndex(location);
    if (index == -1) {
      if (location.y < 0) {
        index = 0;
      } else {
        index = comboBox.getModel().getSize() - 1;
      }
    }
    if (list.getSelectedIndex() != index) {
      list.setSelectedIndex(index);
      if (shouldScroll) {
        list.ensureIndexIsVisible(index);
      }
    }
  }

  protected void startAutoScrolling(int direction) {
    if (isAutoScrolling) {
      autoscrollTimer.stop();
    }
    isAutoScrolling = true;
    if (direction == SCROLL_UP) {
      scrollDirection = SCROLL_UP;
      Point convertedPoint = javax.swing.SwingUtilities.convertPoint(scroller, new Point(1, 1), list);
      int top = list.locationToIndex(convertedPoint);
      list.setSelectedIndex(top);
      autoscrollTimer = new Timer(100, new AutoScrollActionHandler(SCROLL_UP));
    } else if (direction == SCROLL_DOWN) {
      scrollDirection = SCROLL_DOWN;
      Dimension size = scroller.getSize();
      Point convertedPoint = javax.swing.SwingUtilities.convertPoint(scroller, new Point(1, size.height - 1 - 2),
          list);
      int bottom = list.locationToIndex(convertedPoint);
      list.setSelectedIndex(bottom);
      autoscrollTimer = new Timer(100, new AutoScrollActionHandler(SCROLL_DOWN));
    }
    autoscrollTimer.start();
  }

  protected void stopAutoScrolling() {
    isAutoScrolling = false;
    if (autoscrollTimer != null) {
      autoscrollTimer.stop();
      autoscrollTimer = null;
    }
  }

  protected void autoScrollUp() {
    int index = list.getSelectedIndex();
    if (index > 0) {
      list.setSelectedIndex(index - 1);
      list.ensureIndexIsVisible(index - 1);
    }
  }

  protected void autoScrollDown() {
    int index = list.getSelectedIndex();
    int lastItem = list.getModel().getSize() - 1;
    if (index < lastItem) {
      list.setSelectedIndex(index + 1);
      list.ensureIndexIsVisible(index + 1);
    }
  }

  public AccessibleContext getAccessibleContext() {
    return super.getAccessibleContext();
  }

  public boolean isFocusTraversable() {
    return false;
  }

  protected class InvocationMouseHandler extends MouseAdapter {
    public void mousePressed(MouseEvent e) {
      if (!javax.swing.SwingUtilities.isLeftMouseButton(e) || !comboBox.isEnabled()) {
        return;
      }
      if (comboBox.isEditable()) {
        Component comp = comboBox.getEditor().getEditorComponent();
        if ((!(comp instanceof javax.swing.JComponent)) || ((javax.swing.JComponent) comp).isRequestFocusEnabled()) {
          comp.requestFocus();
        }
      } else if (comboBox.isRequestFocusEnabled()) {
        comboBox.requestFocus();
      }
      togglePopup();
    }

    public void mouseReleased(MouseEvent e) {
      Component source = (Component) e.getSource();
      Dimension size = source.getSize();
      Rectangle bounds = new Rectangle(0, 0, size.width - 1, size.height - 1);
      if (!bounds.contains(e.getPoint())) {
        MouseEvent newEvent = convertMouseEvent(e);
        Point location = newEvent.getPoint();
        Rectangle r = new Rectangle();
        list.computeVisibleRect(r);
        if (r.contains(location)) {
          updateListBoxSelectionForEvent(newEvent, false);
          comboBox.setSelectedIndex(list.getSelectedIndex());
          comboBox.setPopupVisible(false);
        }
        hasEntered = false;
        stopAutoScrolling();
      }
    }
  }

  protected class InvocationMouseMotionHandler extends MouseMotionAdapter {
    public void mouseDragged(MouseEvent e) {
      if (isVisible()) {
        MouseEvent newEvent = convertMouseEvent(e);
        Rectangle r = new Rectangle();
        list.computeVisibleRect(r);
        if (newEvent.getPoint().y >= r.y && newEvent.getPoint().y <= r.y + r.height - 1) {
          hasEntered = true;
          if (isAutoScrolling) {
            stopAutoScrolling();
          }
          Point location = newEvent.getPoint();
          if (r.contains(location)) {
            updateListBoxSelectionForEvent(newEvent, false);
          }
        } else if (hasEntered) {
          int directionToScroll = newEvent.getPoint().y < r.y ? SCROLL_UP : SCROLL_DOWN;
          if (isAutoScrolling && scrollDirection != directionToScroll) {
            stopAutoScrolling();
            startAutoScrolling(directionToScroll);
          } else if (!isAutoScrolling) {
            startAutoScrolling(directionToScroll);
          }
        } else if (e.getPoint().y < 0) {
          hasEntered = true;
          startAutoScrolling(SCROLL_UP);
        }
      }
    }
  }

  public class ListDataHandler implements ListDataListener {
    public ListDataHandler() {
    }

    public void contentsChanged(ListDataEvent e) {
    }

    public void intervalAdded(ListDataEvent e) {
    }

    public void intervalRemoved(ListDataEvent e) {
    }
  }

  public class InvocationKeyHandler extends KeyAdapter {
    public InvocationKeyHandler() {
    }

    public void keyReleased(KeyEvent e) {
    }
  }

  protected class ListMouseHandler extends MouseAdapter {
    public void mousePressed(MouseEvent e) {
    }

    public void mouseReleased(MouseEvent anEvent) {
      comboBox.setSelectedIndex(list.getSelectedIndex());
      comboBox.setPopupVisible(false);
      if (comboBox.isEditable() && comboBox.getEditor() != null) {
        comboBox.configureEditor(comboBox.getEditor(), comboBox.getSelectedItem());
      }
      java.awt.Rectangle ignore = null;
    }
  }

  protected class ListMouseMotionHandler extends MouseMotionAdapter {
    public void mouseMoved(MouseEvent anEvent) {
      updateListBoxSelectionForEvent(anEvent, false);
    }
  }

  protected class ItemHandler implements ItemListener {
    public void itemStateChanged(ItemEvent e) {
      if (e.getStateChange() == ItemEvent.SELECTED) {
        setListSelection(comboBox.getSelectedIndex());
      }
    }
  }

  protected class PropertyChangeHandler implements PropertyChangeListener {
    public void propertyChange(PropertyChangeEvent e) {
      String propertyName = e.getPropertyName();
      if ("renderer".equals(propertyName)) {
        list.setCellRenderer(comboBox.getRenderer());
        if (isVisible()) {
          hide();
        }
      } else if ("model".equals(propertyName)) {
        @SuppressWarnings("unchecked")
        ComboBoxModel<Object> oldModel = (ComboBoxModel<Object>) e.getOldValue();
        @SuppressWarnings("unchecked")
        ComboBoxModel<Object> newModel = (ComboBoxModel<Object>) e.getNewValue();
        uninstallComboBoxModelListeners(oldModel);
        installComboBoxModelListeners(newModel);
        list.setModel(newModel);
        if (isVisible()) {
          hide();
        }
      } else if ("font".equals(propertyName)) {
        list.setFont(comboBox.getFont());
      } else if ("componentOrientation".equals(propertyName)) {
        list.setComponentOrientation(comboBox.getComponentOrientation());
        scroller.setComponentOrientation(comboBox.getComponentOrientation());
        setComponentOrientation(comboBox.getComponentOrientation());
      }
    }
  }

  private class AutoScrollActionHandler implements java.awt.event.ActionListener {
    private int direction;

    AutoScrollActionHandler(int direction) {
      this.direction = direction;
    }

    public void actionPerformed(java.awt.event.ActionEvent e) {
      if (direction == SCROLL_UP) {
        autoScrollUp();
      } else {
        autoScrollDown();
      }
    }
  }
}
