/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing.plaf.basic;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JComponent;
import javax.swing.JSpinner;
import javax.swing.LookAndFeel;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.plaf.UIResource;

public class BasicSpinnerUI extends javax.swing.plaf.SpinnerUI {
  protected javax.swing.JSpinner spinner;
  public BasicSpinnerUI() {
  }
  protected JComponent createEditor() {
    JComponent editor = spinner.getEditor();
    maybeRemoveEditorBorder(editor);
    return editor;
  }
  protected LayoutManager createLayout() {
    return new SpinnerLayout();
  }
  protected void installKeyboardActions() {
    shim.swing.KeyActions.install(spinner, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT,
        "Spinner.ancestorInputMap", shim.swing.KeyActions.spinnerActions());
  }
  protected void installDefaults() {
    spinner.setLayout(createLayout());
    LookAndFeel.installBorder(spinner, "Spinner.border");
    LookAndFeel.installColorsAndFont(spinner, "Spinner.background", "Spinner.foreground", "Spinner.font");
    LookAndFeel.installProperty(spinner, "opaque", Boolean.TRUE);
  }
  protected void installListeners() {
    propertyChangeListener = createPropertyChangeListener();
    spinner.addPropertyChangeListener(propertyChangeListener);
  }
  protected void uninstallListeners() {
    spinner.removePropertyChangeListener(propertyChangeListener);
    propertyChangeListener = null;
  }
  protected void uninstallDefaults() {
    spinner.setLayout(null);
    LookAndFeel.uninstallBorder(spinner);
  }
  protected PropertyChangeListener createPropertyChangeListener() {
    return new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent e) {
        String propertyName = e.getPropertyName();
        if ("editor".equals(propertyName)) {
          JComponent oldEditor = (JComponent) e.getOldValue();
          JComponent newEditor = (JComponent) e.getNewValue();
          replaceEditor(oldEditor, newEditor);
          spinner.revalidate();
          spinner.repaint();
        } else if ("enabled".equals(propertyName) || "model".equals(propertyName)) {
          updateEnabledState();
        } else if ("componentOrientation".equals(propertyName)) {
          spinner.revalidate();
        }
      }
    };
  }
  protected Component createNextButton() {
    Component c = createArrowButton(SwingConstants.NORTH);
    c.setName("Spinner.nextButton");
    nextButton = c;
    installNextButtonListeners(c);
    return c;
  }
  protected Component createPreviousButton() {
    Component c = createArrowButton(SwingConstants.SOUTH);
    c.setName("Spinner.previousButton");
    previousButton = c;
    installPreviousButtonListeners(c);
    return c;
  }
  protected void installPreviousButtonListeners(Component c) {
    previousHandler = new ArrowButtonHandler(false);
    installArrowListeners(c, previousHandler);
  }
  protected void installNextButtonListeners(Component c) {
    nextHandler = new ArrowButtonHandler(true);
    installArrowListeners(c, nextHandler);
  }
  private void maybeRemoveEditorBorder(JComponent editor) {
    if (!UIManager.getBoolean("Spinner.editorBorderPainted")) {
      if (editor instanceof javax.swing.JPanel && editor.getBorder() == null && editor.getComponentCount() > 0) {
        editor = (JComponent) editor.getComponent(0);
      }
      if (editor != null && editor.getBorder() instanceof UIResource) {
        editor.setBorder(null);
      }
    }
  }

  protected void replaceEditor(JComponent oldEditor, JComponent newEditor) {
    spinner.remove(oldEditor);
    maybeRemoveEditorBorder(newEditor);
    spinner.add(newEditor, "Editor");
  }
  public static javax.swing.plaf.ComponentUI createUI(JComponent c) {
    return new BasicSpinnerUI();
  }
  public int getBaseline(JComponent c, int width, int height) {
    super.getBaseline(c, width, height);
    JComponent editor = spinner.getEditor();
    Insets insets = spinner.getInsets();
    width = width - insets.left - insets.right;
    height = height - insets.top - insets.bottom;
    if (width >= 0 && height >= 0) {
      int baseline = editor.getBaseline(width, height);
      if (baseline >= 0) {
        return insets.top + baseline;
      }
    }
    return -1;
  }
  public java.awt.Component.BaselineResizeBehavior getBaselineResizeBehavior(JComponent c) {
    super.getBaselineResizeBehavior(c);
    return spinner.getEditor().getBaselineResizeBehavior();
  }
  public void installUI(JComponent c) {
    this.spinner = (JSpinner) c;
    installDefaults();
    installListeners();
    maybeAdd(createNextButton(), "Next");
    maybeAdd(createPreviousButton(), "Previous");
    maybeAdd(createEditor(), "Editor");
    updateEnabledState();
    installKeyboardActions();
  }
  public void uninstallUI(JComponent c) {
    shim.swing.KeyActions.uninstall(c, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    uninstallDefaults();
    uninstallListeners();
    spinner = null;
    c.removeAll();
  }
  private PropertyChangeListener propertyChangeListener;
  private Component nextButton;
  private Component previousButton;
  private ArrowButtonHandler nextHandler;
  private ArrowButtonHandler previousHandler;

  private void maybeAdd(Component c, String s) {
    if (c != null) {
      if (c instanceof javax.swing.JSpinner.DefaultEditor) {
        javax.swing.JFormattedTextField ftf = ((javax.swing.JSpinner.DefaultEditor) c).getTextField();
        int alignment = UIManager.getInt("Spinner.editorAlignment");
        ftf.setHorizontalAlignment(alignment == 0 ? javax.swing.JTextField.TRAILING : alignment);
        if (ftf.getFont() instanceof UIResource && spinner.getFont() != null) {
          ftf.setFont(spinner.getFont());
        }
      }
      spinner.add(c, s);
    }
  }

  private void updateEnabledState() {
    boolean enabled = spinner.isEnabled();
    for (Component c : spinner.getComponents()) {
      if (c == nextButton || c == previousButton) {
        c.setEnabled(enabled);
      }
    }
  }

  private Component createArrowButton(int direction) {
    javax.swing.JButton b = new BasicArrowButton(direction);
    Border buttonBorder = UIManager.getBorder("Spinner.arrowButtonBorder");
    if (buttonBorder instanceof UIResource) {
      b.setBorder(new CompoundBorder(buttonBorder, null));
    } else {
      b.setBorder(buttonBorder);
    }
    b.setInheritsPopupMenu(true);
    return b;
  }

  private void installArrowListeners(Component c, ArrowButtonHandler handler) {
    if (c instanceof javax.swing.JButton) {
      ((javax.swing.JButton) c).addActionListener(handler);
    }
    c.addMouseListener(handler);
  }

  private static class ArrowButtonHandler extends MouseAdapter implements ActionListener {
    private final Timer autoRepeatTimer;
    private final boolean isNext;
    private JSpinner target;
    ArrowButtonHandler(boolean isNext) {
      this.isNext = isNext;
      autoRepeatTimer = new Timer(60, this);
      autoRepeatTimer.setInitialDelay(300);
    }
    public void actionPerformed(ActionEvent e) {
      Object src = e.getSource();
      if (src instanceof Component) {
        JSpinner s = (JSpinner) javax.swing.SwingUtilities.getAncestorOfClass(JSpinner.class, (Component) src);
        if (s != null) {
          target = s;
        }
      }
      if (target == null) {
        return;
      }
      try {
        Object value = isNext ? target.getNextValue() : target.getPreviousValue();
        if (value != null) {
          target.setValue(value);
        }
      } catch (IllegalArgumentException iae) {
        // ignored like the editor does when the value is out of range
      }
    }
    public void mousePressed(MouseEvent e) {
      if (javax.swing.SwingUtilities.isLeftMouseButton(e) && e.getComponent().isEnabled()) {
        target = (JSpinner) javax.swing.SwingUtilities.getAncestorOfClass(JSpinner.class, e.getComponent());
        autoRepeatTimer.start();
      }
    }
    public void mouseReleased(MouseEvent e) {
      autoRepeatTimer.stop();
      target = null;
    }
  }

  private static class SpinnerLayout implements LayoutManager {
    private Component nextButton;
    private Component previousButton;
    private Component editor;
    public void addLayoutComponent(String name, Component c) {
      if ("Next".equals(name)) {
        nextButton = c;
      } else if ("Previous".equals(name)) {
        previousButton = c;
      } else if ("Editor".equals(name)) {
        editor = c;
      }
    }
    public void removeLayoutComponent(Component c) {
      if (c == nextButton) {
        nextButton = null;
      } else if (c == previousButton) {
        previousButton = null;
      } else if (c == editor) {
        editor = null;
      }
    }
    private Dimension preferredSize(Component c) {
      return c == null ? new Dimension(0, 0) : c.getPreferredSize();
    }
    public Dimension preferredLayoutSize(Container parent) {
      Dimension nextD = preferredSize(nextButton);
      Dimension previousD = preferredSize(previousButton);
      Dimension editorD = preferredSize(editor);
      editorD.height = ((editorD.height + 1) / 2) * 2;
      Dimension size = new Dimension(editorD.width, editorD.height);
      size.width += Math.max(nextD.width, previousD.width);
      Insets insets = parent.getInsets();
      size.width += insets.left + insets.right;
      size.height += insets.top + insets.bottom;
      return size;
    }
    public Dimension minimumLayoutSize(Container parent) {
      return preferredLayoutSize(parent);
    }
    private void setBounds(Component c, int x, int y, int width, int height) {
      if (c != null) {
        c.setBounds(x, y, width, height);
      }
    }
    public void layoutContainer(Container parent) {
      int width = parent.getWidth();
      int height = parent.getHeight();
      Insets insets = parent.getInsets();
      if (nextButton == null && previousButton == null) {
        setBounds(editor, insets.left, insets.top, width - insets.left - insets.right, height - insets.top
            - insets.bottom);
        return;
      }
      Dimension nextD = preferredSize(nextButton);
      Dimension previousD = preferredSize(previousButton);
      int buttonsWidth = Math.max(nextD.width, previousD.width);
      int editorHeight = height - (insets.top + insets.bottom);
      Insets buttonInsets = UIManager.getInsets("Spinner.arrowButtonInsets");
      if (buttonInsets == null) {
        buttonInsets = insets;
      }
      int editorX;
      int editorWidth;
      int buttonsX;
      if (parent.getComponentOrientation().isLeftToRight()) {
        editorX = insets.left;
        editorWidth = width - insets.left - buttonsWidth;
        buttonsX = width - buttonsWidth - buttonInsets.right;
      } else {
        buttonsX = buttonInsets.left;
        editorX = buttonsX + buttonsWidth;
        editorWidth = width - buttonInsets.left - buttonsWidth - insets.right;
      }
      int nextY = buttonInsets.top;
      int nextHeight = (height / 2) + (height % 2) - nextY;
      int previousY = buttonInsets.top + nextHeight;
      int previousHeight = height - previousY - buttonInsets.bottom;
      setBounds(editor, editorX, insets.top, editorWidth, editorHeight);
      setBounds(nextButton, buttonsX, nextY, buttonsWidth, nextHeight);
      setBounds(previousButton, buttonsX, previousY, buttonsWidth, previousHeight);
    }
  }
}
