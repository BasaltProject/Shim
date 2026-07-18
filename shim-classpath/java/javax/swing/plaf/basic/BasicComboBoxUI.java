/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing.plaf.basic;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.Rectangle;
import java.awt.event.ItemEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import javax.swing.ComboBoxEditor;
import javax.swing.ComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.LookAndFeel;
import javax.swing.UIManager;
import javax.swing.CellRendererPane;
import javax.swing.plaf.UIResource;

public class BasicComboBoxUI extends javax.swing.plaf.ComboBoxUI {
  protected javax.swing.JComboBox<java.lang.Object> comboBox;
  protected boolean hasFocus;
  protected javax.swing.JList<java.lang.Object> listBox;
  protected javax.swing.CellRendererPane currentValuePane;
  protected javax.swing.plaf.basic.ComboPopup popup;
  protected java.awt.Component editor;
  protected javax.swing.JButton arrowButton;
  protected java.awt.event.KeyListener keyListener;
  protected java.awt.event.FocusListener focusListener;
  protected java.beans.PropertyChangeListener propertyChangeListener;
  protected java.awt.event.ItemListener itemListener;
  protected java.awt.event.MouseListener popupMouseListener;
  protected java.awt.event.MouseMotionListener popupMouseMotionListener;
  protected java.awt.event.KeyListener popupKeyListener;
  protected javax.swing.event.ListDataListener listDataListener;
  protected boolean isMinimumSizeDirty;
  protected java.awt.Dimension cachedMinimumSize;
  protected boolean squareButton;
  protected java.awt.Insets padding;
  public BasicComboBoxUI() {
  }
  protected java.awt.event.ItemListener createItemListener() {
    return new ItemHandler();
  }
  protected LayoutManager createLayoutManager() {
    return new ComboBoxLayoutManager();
  }
  protected ComboBoxEditor createEditor() {
    return new BasicComboBoxEditor.UIResource();
  }
  protected javax.swing.plaf.basic.ComboPopup createPopup() {
    return new BasicComboPopup(comboBox);
  }
  protected ListCellRenderer<Object> createRenderer() {
    return new BasicComboBoxRenderer.UIResource();
  }
  protected java.awt.event.FocusListener createFocusListener() {
    return new FocusHandler();
  }
  protected javax.swing.event.ListDataListener createListDataListener() {
    return new ListDataHandler();
  }
  protected JButton createArrowButton() {
    JButton button = new BasicArrowButton(javax.swing.SwingConstants.SOUTH, UIManager.getColor("ComboBox.buttonBackground"),
        UIManager.getColor("ComboBox.buttonShadow"), UIManager.getColor("ComboBox.buttonDarkShadow"),
        UIManager.getColor("ComboBox.buttonHighlight"));
    button.setName("ComboBox.arrowButton");
    return button;
  }
  public void configureArrowButton() {
    if (arrowButton != null) {
      arrowButton.setEnabled(comboBox.isEnabled());
      arrowButton.setFocusable(comboBox.isFocusable());
      arrowButton.setRequestFocusEnabled(false);
      arrowButton.addMouseListener(popup.getMouseListener());
      arrowButton.addMouseMotionListener(popup.getMouseMotionListener());
      arrowButton.resetKeyboardActions();
    }
  }
  public void addEditor() {
    removeEditor();
    editor = comboBox.getEditor().getEditorComponent();
    if (editor != null) {
      configureEditor();
      comboBox.add(editor);
      if (comboBox.isFocusOwner()) {
        editor.requestFocus();
      }
    }
  }
  public void unconfigureArrowButton() {
    if (arrowButton != null) {
      arrowButton.removeMouseListener(popup.getMouseListener());
      arrowButton.removeMouseMotionListener(popup.getMouseMotionListener());
    }
  }
  protected void unconfigureEditor() {
    if (focusListener != null) {
      editor.removeFocusListener(focusListener);
    }
  }
  protected Rectangle rectangleForCurrentValue() {
    int width = comboBox.getWidth();
    int height = comboBox.getHeight();
    Insets insets = getInsets();
    int buttonSize = height - (insets.top + insets.bottom);
    if (arrowButton != null) {
      buttonSize = arrowButton.getWidth();
    }
    if (comboBox.getComponentOrientation().isLeftToRight()) {
      return new Rectangle(insets.left, insets.top, width - (insets.left + insets.right + buttonSize),
          height - (insets.top + insets.bottom));
    }
    return new Rectangle(insets.left + buttonSize, insets.top, width - (insets.left + insets.right + buttonSize),
        height - (insets.top + insets.bottom));
  }
  public void paintCurrentValueBackground(Graphics g, Rectangle bounds, boolean hasFocus) {
    Color t = g.getColor();
    if (comboBox.isEnabled()) {
      g.setColor(comboBox.getBackground());
    } else {
      Color disabled = UIManager.getColor("ComboBox.disabledBackground");
      g.setColor(disabled == null ? comboBox.getBackground() : disabled);
    }
    g.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
    g.setColor(t);
  }
  public void paintCurrentValue(Graphics g, Rectangle bounds, boolean hasFocus) {
    ListCellRenderer<Object> renderer = comboBox.getRenderer();
    Component c;
    if (hasFocus && !isPopupVisible(comboBox)) {
      c = renderer.getListCellRendererComponent(listBox, comboBox.getSelectedItem(), -1, true, false);
    } else {
      c = renderer.getListCellRendererComponent(listBox, comboBox.getSelectedItem(), -1, false, false);
      c.setBackground(UIManager.getColor("ComboBox.background"));
    }
    c.setFont(comboBox.getFont());
    if (hasFocus && !isPopupVisible(comboBox)) {
      c.setForeground(listBox.getSelectionForeground());
      c.setBackground(listBox.getSelectionBackground());
    } else if (comboBox.isEnabled()) {
      c.setForeground(comboBox.getForeground());
      c.setBackground(comboBox.getBackground());
    } else {
      Color disabled = UIManager.getColor("ComboBox.disabledForeground");
      c.setForeground(disabled == null ? comboBox.getForeground() : disabled);
      Color disabledBg = UIManager.getColor("ComboBox.disabledBackground");
      c.setBackground(disabledBg == null ? comboBox.getBackground() : disabledBg);
    }
    boolean shouldValidate = c instanceof javax.swing.JPanel;
    int x = bounds.x;
    int y = bounds.y;
    int w = bounds.width;
    int h = bounds.height;
    if (padding != null) {
      x = bounds.x + padding.left;
      y = bounds.y + padding.top;
      w = bounds.width - (padding.left + padding.right);
      h = bounds.height - (padding.top + padding.bottom);
    }
    currentValuePane.paintComponent(g, c, comboBox, x, y, w, h, shouldValidate);
  }
  protected Dimension getDisplaySize() {
    if (!isDisplaySizeDirty()) {
      return new Dimension(displaySize);
    }
    Dimension result = new Dimension();
    ListCellRenderer<Object> renderer = comboBox.getRenderer();
    if (renderer == null) {
      renderer = getDefaultListCellRenderer();
    }
    sameBaseline = true;
    Object prototypeValue = comboBox.getPrototypeDisplayValue();
    if (prototypeValue != null) {
      result = getSizeForComponent(renderer.getListCellRendererComponent(listBox, prototypeValue, -1, false, false));
    } else {
      ComboBoxModel<Object> model = comboBox.getModel();
      int modelSize = model.getSize();
      if (modelSize > 0) {
        for (int i = 0; i < modelSize; i++) {
          Object value = model.getElementAt(i);
          Component c = renderer.getListCellRendererComponent(listBox, value, -1, false, false);
          Dimension d = getSizeForComponent(c);
          result.width = Math.max(result.width, d.width);
          result.height = Math.max(result.height, d.height);
        }
      } else {
        result = getDefaultSize();
        if (comboBox.isEditable()) {
          result.width = 100;
        }
      }
    }
    if (comboBox.isEditable()) {
      Dimension d = comboBox.getEditor().getEditorComponent().getPreferredSize();
      result.width = Math.max(result.width, d.width);
      result.height = Math.max(result.height, d.height);
    }
    if (padding != null) {
      result.width += padding.left + padding.right;
      result.height += padding.top + padding.bottom;
    }
    displaySize.setSize(result.width, result.height);
    return new Dimension(result);
  }
  protected Dimension getSizeForComponent(Component comp) {
    currentValuePane.add(comp);
    comp.setFont(comboBox.getFont());
    Dimension d = comp.getPreferredSize();
    currentValuePane.remove(comp);
    return d;
  }
  protected Dimension getDefaultSize() {
    Dimension d = getSizeForComponent(getDefaultListCellRenderer().getListCellRendererComponent(listBox, " ", -1, false,
        false));
    return new Dimension(d.width, d.height);
  }
  protected boolean isNavigationKey(int keyCode) {
    return keyCode == KeyEvent.VK_UP || keyCode == KeyEvent.VK_DOWN || keyCode == KeyEvent.VK_KP_UP
        || keyCode == KeyEvent.VK_KP_DOWN;
  }
  protected void selectNextPossibleValue() {
    int si;
    if (comboBox.isPopupVisible()) {
      si = listBox.getSelectedIndex();
    } else {
      si = comboBox.getSelectedIndex();
    }
    if (si < comboBox.getModel().getSize() - 1) {
      listBox.setSelectedIndex(si + 1);
      listBox.ensureIndexIsVisible(si + 1);
      if (!comboBox.isPopupVisible()) {
        comboBox.setSelectedIndex(si + 1);
      }
      comboBox.repaint();
    }
  }
  protected void selectPreviousPossibleValue() {
    int si;
    if (comboBox.isPopupVisible()) {
      si = listBox.getSelectedIndex();
    } else {
      si = comboBox.getSelectedIndex();
    }
    if (si > 0) {
      listBox.setSelectedIndex(si - 1);
      listBox.ensureIndexIsVisible(si - 1);
      if (!comboBox.isPopupVisible()) {
        comboBox.setSelectedIndex(si - 1);
      }
      comboBox.repaint();
    }
  }
  protected void toggleOpenClose() {
    setPopupVisible(comboBox, !isPopupVisible(comboBox));
  }
  protected void installKeyboardActions() {
  }
  protected void uninstallKeyboardActions() {
  }
  protected void installDefaults() {
    LookAndFeel.installColorsAndFont(comboBox, "ComboBox.background", "ComboBox.foreground", "ComboBox.font");
    LookAndFeel.installBorder(comboBox, "ComboBox.border");
    LookAndFeel.installProperty(comboBox, "opaque", Boolean.TRUE);
    Long l = (Long) UIManager.get("ComboBox.timeFactor");
    squareButton = UIManager.getBoolean("ComboBox.squareButton");
    padding = UIManager.getInsets("ComboBox.padding");
    if (padding == null) {
      padding = new Insets(0, 0, 0, 0);
    }
  }
  protected void installListeners() {
    if ((itemListener = createItemListener()) != null) {
      comboBox.addItemListener(itemListener);
    }
    if ((propertyChangeListener = createPropertyChangeListener()) != null) {
      comboBox.addPropertyChangeListener(propertyChangeListener);
    }
    if ((keyListener = createKeyListener()) != null) {
      comboBox.addKeyListener(keyListener);
    }
    if ((focusListener = createFocusListener()) != null) {
      comboBox.addFocusListener(focusListener);
    }
    if ((popupMouseListener = popup.getMouseListener()) != null) {
      comboBox.addMouseListener(popupMouseListener);
    }
    if ((popupMouseMotionListener = popup.getMouseMotionListener()) != null) {
      comboBox.addMouseMotionListener(popupMouseMotionListener);
    }
    if ((popupKeyListener = popup.getKeyListener()) != null) {
      comboBox.addKeyListener(popupKeyListener);
    }
    if (comboBox.getModel() != null) {
      if ((listDataListener = createListDataListener()) != null) {
        comboBox.getModel().addListDataListener(listDataListener);
      }
    }
  }
  protected java.awt.event.KeyListener createKeyListener() {
    return new KeyHandler();
  }
  protected void uninstallListeners() {
    comboBox.removeKeyListener(keyListener);
    comboBox.removeItemListener(itemListener);
    comboBox.removePropertyChangeListener(propertyChangeListener);
    comboBox.removeFocusListener(focusListener);
    comboBox.removeMouseListener(popupMouseListener);
    comboBox.removeMouseMotionListener(popupMouseMotionListener);
    comboBox.removeKeyListener(popupKeyListener);
    if (comboBox.getModel() != null && listDataListener != null) {
      comboBox.getModel().removeListDataListener(listDataListener);
    }
  }
  protected void uninstallDefaults() {
    LookAndFeel.uninstallBorder(comboBox);
  }
  protected void installComponents() {
    arrowButton = createArrowButton();
    if (arrowButton != null) {
      comboBox.add(arrowButton);
      configureArrowButton();
    }
    if (comboBox.isEditable()) {
      addEditor();
    }
    comboBox.add(currentValuePane);
  }
  protected java.beans.PropertyChangeListener createPropertyChangeListener() {
    return new PropertyChangeHandler();
  }
  protected void uninstallComponents() {
    if (arrowButton != null) {
      unconfigureArrowButton();
    }
    if (editor != null) {
      unconfigureEditor();
    }
    comboBox.removeAll();
    arrowButton = null;
  }
  public void removeEditor() {
    if (editor != null) {
      unconfigureEditor();
      comboBox.remove(editor);
      editor = null;
    }
  }
  public void setPopupVisible(JComboBox<?> c, boolean v) {
    if (v) {
      popup.show();
    } else {
      popup.hide();
    }
  }
  public boolean isPopupVisible(JComboBox<?> c) {
    return popup.isVisible();
  }
  protected void configureEditor() {
    editor.setEnabled(comboBox.isEnabled());
    editor.setFocusable(comboBox.isFocusable());
    editor.setFont(comboBox.getFont());
    if (focusListener != null) {
      editor.addFocusListener(focusListener);
    }
    comboBox.getEditor().addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent e) {
        Object item = comboBox.getEditor().getItem();
        if (item != null) {
          if (!comboBox.isPopupVisible() && !item.equals(comboBox.getSelectedItem())) {
            comboBox.setSelectedItem(comboBox.getEditor().getItem());
          }
        }
      }
    });
    comboBox.configureEditor(comboBox.getEditor(), comboBox.getSelectedItem());
  }
  public static javax.swing.plaf.ComponentUI createUI(JComponent c) {
    return new BasicComboBoxUI();
  }
  protected Insets getInsets() {
    return comboBox.getInsets();
  }
  public Dimension getMaximumSize(JComponent c) {
    return new Dimension(Short.MAX_VALUE, Short.MAX_VALUE);
  }
  public void paint(Graphics g, JComponent c) {
    hasFocus = comboBox.hasFocus();
    if (!comboBox.isEditable()) {
      Rectangle r = rectangleForCurrentValue();
      paintCurrentValueBackground(g, r, hasFocus);
      paintCurrentValue(g, r, hasFocus);
    }
  }
  public int getAccessibleChildrenCount(JComponent c) {
    if (comboBox.isEditable()) {
      return 2;
    }
    return 1;
  }
  public javax.accessibility.Accessible getAccessibleChild(JComponent c, int i) {
    switch (i) {
    case 0:
      if (popup instanceof javax.accessibility.Accessible) {
        return (javax.accessibility.Accessible) popup;
      }
      break;
    case 1:
      if (comboBox.isEditable() && (editor instanceof javax.accessibility.Accessible)) {
        return (javax.accessibility.Accessible) editor;
      }
      break;
    default:
      break;
    }
    return null;
  }
  public Dimension getPreferredSize(JComponent c) {
    return getMinimumSize(c);
  }
  public Dimension getMinimumSize(JComponent c) {
    if (!isMinimumSizeDirty) {
      return new Dimension(cachedMinimumSize);
    }
    Dimension size = getDisplaySize();
    Insets insets = getInsets();
    size.height += insets.top + insets.bottom;
    int buttonSize = size.height - (insets.top + insets.bottom);
    size.width += insets.left + insets.right + buttonSize;
    cachedMinimumSize = new Dimension(size.width, size.height);
    isMinimumSizeDirty = false;
    return new Dimension(size);
  }
  public boolean isFocusTraversable(JComboBox<?> c) {
    return !comboBox.isEditable();
  }
  public int getBaseline(JComponent c, int width, int height) {
    super.getBaseline(c, width, height);
    return -1;
  }
  public java.awt.Component.BaselineResizeBehavior getBaselineResizeBehavior(JComponent c) {
    super.getBaselineResizeBehavior(c);
    return java.awt.Component.BaselineResizeBehavior.CENTER_OFFSET;
  }
  public void installUI(JComponent c) {
    isMinimumSizeDirty = true;
    comboBox = (JComboBox<Object>) c;
    currentValuePane = new CellRendererPane();
    installDefaults();
    popup = createPopup();
    listBox = popup.getList();
    Object rend = comboBox.getRenderer();
    if (rend == null || rend instanceof UIResource) {
      comboBox.setRenderer(createRenderer());
    }
    Object ed = comboBox.getEditor();
    if (ed == null || ed instanceof UIResource) {
      comboBox.setEditor(createEditor());
    }
    installListeners();
    installComponents();
    comboBox.setLayout(createLayoutManager());
    comboBox.setRequestFocusEnabled(true);
    installKeyboardActions();
    comboBox.putClientProperty("doNotCancelPopup", null);
  }
  public void uninstallUI(JComponent c) {
    setPopupVisible(comboBox, false);
    popup.uninstallingUI();
    uninstallKeyboardActions();
    comboBox.setLayout(null);
    uninstallComponents();
    uninstallListeners();
    uninstallDefaults();
    if (comboBox.getRenderer() == null || comboBox.getRenderer() instanceof UIResource) {
      comboBox.setRenderer(null);
    }
    ComboBoxEditor comboBoxEditor = comboBox.getEditor();
    if (comboBoxEditor instanceof UIResource) {
      if (comboBoxEditor.getEditorComponent().hasFocus()) {
        comboBox.requestFocus();
      }
      comboBox.setEditor(null);
    }
    keyListener = null;
    focusListener = null;
    listDataListener = null;
    propertyChangeListener = null;
    popup = null;
    listBox = null;
    comboBox = null;
  }

  public class ComboBoxLayoutManager implements java.awt.LayoutManager {
    public ComboBoxLayoutManager() {
    }
    public void removeLayoutComponent(Component comp) {
    }
    public void addLayoutComponent(String name, Component comp) {
    }
    public void layoutContainer(Container parent) {
      JComboBox<?> cb = (JComboBox<?>) parent;
      int width = cb.getWidth();
      int height = cb.getHeight();
      Insets insets = getInsets();
      int buttonHeight = height - (insets.top + insets.bottom);
      int buttonWidth = buttonHeight;
      if (arrowButton != null) {
        Insets arrowInsets = arrowButton.getInsets();
        buttonWidth = squareButton ? buttonHeight : arrowButton.getPreferredSize().width + arrowInsets.left
            + arrowInsets.right;
      }
      Rectangle cvb;
      if (arrowButton != null) {
        if (cb.getComponentOrientation().isLeftToRight()) {
          arrowButton.setBounds(width - (insets.right + buttonWidth), insets.top, buttonWidth, buttonHeight);
        } else {
          arrowButton.setBounds(insets.left, insets.top, buttonWidth, buttonHeight);
        }
      }
      if (editor != null) {
        cvb = rectangleForCurrentValue();
        editor.setBounds(cvb);
      }
    }
    public Dimension preferredLayoutSize(Container parent) {
      return parent.getPreferredSize();
    }
    public Dimension minimumLayoutSize(Container parent) {
      return parent.getMinimumSize();
    }
  }

  public class PropertyChangeHandler implements java.beans.PropertyChangeListener {
    public PropertyChangeHandler() {
    }
    public void propertyChange(java.beans.PropertyChangeEvent e) {
      String propertyName = e.getPropertyName();
      if (e.getSource() == editor) {
        return;
      }
      if ("model".equals(propertyName)) {
        ComboBoxModel<?> newModel = (ComboBoxModel<?>) e.getNewValue();
        ComboBoxModel<?> oldModel = (ComboBoxModel<?>) e.getOldValue();
        if (oldModel != null && listDataListener != null) {
          oldModel.removeListDataListener(listDataListener);
        }
        if (newModel != null && listDataListener != null) {
          newModel.addListDataListener(listDataListener);
        }
        isMinimumSizeDirty = true;
        comboBox.revalidate();
        comboBox.repaint();
      } else if ("editor".equals(propertyName) && comboBox.isEditable()) {
        addEditor();
        comboBox.revalidate();
      } else if ("editable".equals(propertyName)) {
        if (comboBox.isEditable()) {
          comboBox.setRequestFocusEnabled(false);
          addEditor();
        } else {
          comboBox.setRequestFocusEnabled(true);
          removeEditor();
        }
        isMinimumSizeDirty = true;
        comboBox.revalidate();
        comboBox.repaint();
      } else if ("enabled".equals(propertyName)) {
        boolean enabled = comboBox.isEnabled();
        if (editor != null) {
          editor.setEnabled(enabled);
        }
        if (arrowButton != null) {
          arrowButton.setEnabled(enabled);
        }
        comboBox.repaint();
      } else if ("font".equals(propertyName)) {
        listBox.setFont(comboBox.getFont());
        if (editor != null) {
          editor.setFont(comboBox.getFont());
        }
        isMinimumSizeDirty = true;
        comboBox.validate();
      } else if ("renderer".equals(propertyName) || "prototypeDisplayValue".equals(propertyName)) {
        isMinimumSizeDirty = true;
        comboBox.revalidate();
      } else if ("componentOrientation".equals(propertyName)) {
        comboBox.revalidate();
      }
    }
  }

  public class ItemHandler implements java.awt.event.ItemListener {
    public ItemHandler() {
    }
    public void itemStateChanged(java.awt.event.ItemEvent e) {
      ItemEvent ie = e;
      if (ie.getStateChange() == ItemEvent.SELECTED && editor != null) {
        comboBox.configureEditor(comboBox.getEditor(), comboBox.getSelectedItem());
      }
      comboBox.repaint();
    }
  }

  public class ListDataHandler implements javax.swing.event.ListDataListener {
    public ListDataHandler() {
    }
    public void contentsChanged(javax.swing.event.ListDataEvent e) {
      if (!(e.getIndex0() == -1 && e.getIndex1() == -1)) {
        isMinimumSizeDirty = true;
        comboBox.revalidate();
      }
      if (comboBox.isEditable() && editor != null) {
        comboBox.configureEditor(comboBox.getEditor(), comboBox.getSelectedItem());
      }
      comboBox.repaint();
    }
    public void intervalAdded(javax.swing.event.ListDataEvent e) {
      if (comboBox.isEditable() && editor != null) {
        comboBox.configureEditor(comboBox.getEditor(), comboBox.getSelectedItem());
      }
      isMinimumSizeDirty = true;
      comboBox.revalidate();
      comboBox.repaint();
    }
    public void intervalRemoved(javax.swing.event.ListDataEvent e) {
      isMinimumSizeDirty = true;
      comboBox.revalidate();
      comboBox.repaint();
    }
  }

  public class FocusHandler implements java.awt.event.FocusListener {
    public FocusHandler() {
    }
    public void focusGained(java.awt.event.FocusEvent e) {
      hasFocus = true;
      comboBox.repaint();
    }
    public void focusLost(java.awt.event.FocusEvent e) {
      hasFocus = false;
      if (!e.isTemporary()) {
        setPopupVisible(comboBox, false);
      }
      comboBox.repaint();
    }
  }

  public class KeyHandler extends java.awt.event.KeyAdapter {
    public KeyHandler() {
    }
    public void keyPressed(java.awt.event.KeyEvent e) {
      if (isNavigationKey(e.getKeyCode())) {
        if (e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_KP_UP) {
          selectPreviousPossibleValue();
        } else {
          selectNextPossibleValue();
        }
        e.consume();
      }
    }
  }
  private boolean sameBaseline;
  private Dimension displaySize = new Dimension();

  private ListCellRenderer<Object> getDefaultListCellRenderer() {
    @SuppressWarnings("unchecked")
    ListCellRenderer<Object> r = (ListCellRenderer<Object>) (ListCellRenderer<?>) new javax.swing.DefaultListCellRenderer();
    return r;
  }

  private boolean isDisplaySizeDirty() {
    return true;
  }
}
