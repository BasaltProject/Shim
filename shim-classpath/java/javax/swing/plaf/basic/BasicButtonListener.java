/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */
package javax.swing.plaf.basic;

import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.ActionMap;
import javax.swing.ButtonModel;
import javax.swing.ComponentInputMap;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.ActionMapUIResource;
import javax.swing.plaf.InputMapUIResource;
import javax.swing.plaf.UIResource;

public class BasicButtonListener implements MouseListener, MouseMotionListener, FocusListener, ChangeListener,
    PropertyChangeListener {
  private long lastPressedTimestamp = -1;
  private boolean shouldDiscardRelease = false;

  public BasicButtonListener(AbstractButton b) {
  }

  public void propertyChange(PropertyChangeEvent e) {
    String prop = e.getPropertyName();
    if (prop == AbstractButton.MNEMONIC_CHANGED_PROPERTY) {
      updateMnemonicBinding((AbstractButton) e.getSource());
    } else if (prop == AbstractButton.CONTENT_AREA_FILLED_CHANGED_PROPERTY) {
      checkOpacity((AbstractButton) e.getSource());
    } else if (prop == AbstractButton.TEXT_CHANGED_PROPERTY || "font".equals(prop) || "foreground".equals(prop)) {
      AbstractButton b = (AbstractButton) e.getSource();
      b.revalidate();
      b.repaint();
    }
  }

  protected void checkOpacity(AbstractButton b) {
    b.setOpaque(b.isContentAreaFilled());
  }

  public void installKeyboardActions(JComponent c) {
    AbstractButton b = (AbstractButton) c;
    updateMnemonicBinding(b);
    InputMap focus = (InputMap) UIManager.get(prefix(b) + "focusInputMap");
    if (focus != null) {
      SwingUtilities.replaceUIInputMap(c, JComponent.WHEN_FOCUSED, focus);
    }
    ActionMap map = new ActionMapUIResource();
    map.put("pressed", new PressAction(true));
    map.put("released", new PressAction(false));
    if (b instanceof javax.swing.JToggleButton) {
      map.put("Next", new GroupMoveAction(1));
      map.put("Previous", new GroupMoveAction(-1));
      InputMap arrows = new javax.swing.plaf.InputMapUIResource();
      arrows.put(javax.swing.KeyStroke.getKeyStroke("UP"), "Previous");
      arrows.put(javax.swing.KeyStroke.getKeyStroke("DOWN"), "Next");
      arrows.put(javax.swing.KeyStroke.getKeyStroke("LEFT"), "Previous");
      arrows.put(javax.swing.KeyStroke.getKeyStroke("RIGHT"), "Next");
      SwingUtilities.replaceUIInputMap(c, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, arrows);
    }
    SwingUtilities.replaceUIActionMap(c, map);
  }

  private static final class GroupMoveAction extends javax.swing.AbstractAction {
    private final int direction;

    GroupMoveAction(int direction) {
      super(direction > 0 ? "Next" : "Previous");
      this.direction = direction;
    }

    public void actionPerformed(java.awt.event.ActionEvent e) {
      Object src = e.getSource();
      if (!(src instanceof AbstractButton)) {
        return;
      }
      AbstractButton button = (AbstractButton) src;
      javax.swing.ButtonModel model = button.getModel();
      javax.swing.ButtonGroup group = model instanceof javax.swing.DefaultButtonModel
          ? ((javax.swing.DefaultButtonModel) model).getGroup() : null;
      if (group == null) {
        return;
      }
      java.util.List<AbstractButton> members = java.util.Collections.list(group.getElements());
      int start = members.indexOf(button);
      for (int i = 1; i <= members.size() && start >= 0; i++) {
        AbstractButton candidate = members.get(((start + direction * i) % members.size() + members.size())
            % members.size());
        if (candidate.isEnabled() && candidate.isVisible() || candidate == button) {
          if (candidate != button) {
            candidate.requestFocus();
            candidate.setSelected(true);
          }
          return;
        }
      }
    }
  }

  private static String prefix(AbstractButton b) {
    String id = b.getUIClassID();
    return id.substring(0, id.length() - 2) + ".";
  }

  void updateMnemonicBinding(AbstractButton b) {
    int m = b.getMnemonic();
    if (m != 0) {
      InputMap map = SwingUtilities.getUIInputMap(b, JComponent.WHEN_IN_FOCUSED_WINDOW);
      if (map == null) {
        map = new ComponentInputMap(b);
        SwingUtilities.replaceUIInputMap(b, JComponent.WHEN_IN_FOCUSED_WINDOW, map);
      }
      map.clear();
      map.put(KeyStroke.getKeyStroke(m, java.awt.event.InputEvent.ALT_MASK, false), "pressed");
      map.put(KeyStroke.getKeyStroke(m, java.awt.event.InputEvent.ALT_MASK, true), "released");
      map.put(KeyStroke.getKeyStroke(m, 0, true), "released");
    } else {
      InputMap map = SwingUtilities.getUIInputMap(b, JComponent.WHEN_IN_FOCUSED_WINDOW);
      if (map != null) {
        map.clear();
      }
    }
  }

  public void uninstallKeyboardActions(JComponent c) {
    SwingUtilities.replaceUIInputMap(c, JComponent.WHEN_IN_FOCUSED_WINDOW, null);
    SwingUtilities.replaceUIInputMap(c, JComponent.WHEN_FOCUSED, null);
    SwingUtilities.replaceUIInputMap(c, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, null);
    SwingUtilities.replaceUIActionMap(c, null);
  }

  public void stateChanged(ChangeEvent e) {
    AbstractButton b = (AbstractButton) e.getSource();
    b.repaint();
  }

  public void focusGained(FocusEvent e) {
    AbstractButton b = (AbstractButton) e.getSource();
    if (b instanceof JButton && ((JButton) b).isDefaultCapable()) {
      JRootPane root = b.getRootPane();
      if (root != null) {
        Object follows = UIManager.get("Button.defaultButtonFollowsFocus");
        if (follows == null || !Boolean.FALSE.equals(follows)) {
          root.putClientProperty("temporaryDefaultButton", b);
          root.setDefaultButton((JButton) b);
          root.putClientProperty("temporaryDefaultButton", b);
        }
      }
    }
    b.repaint();
  }

  public void focusLost(FocusEvent e) {
    AbstractButton b = (AbstractButton) e.getSource();
    JRootPane root = b.getRootPane();
    if (root != null) {
      JButton initial = (JButton) root.getClientProperty("initialDefaultButton");
      if (b != initial) {
        Object temp = root.getClientProperty("temporaryDefaultButton");
        if (temp == null || temp == b) {
          root.setDefaultButton(initial);
        }
      }
    }
    ButtonModel model = b.getModel();
    model.setPressed(false);
    model.setArmed(false);
    b.repaint();
  }

  public void mouseMoved(MouseEvent e) {
  }

  public void mouseDragged(MouseEvent e) {
  }

  public void mouseClicked(MouseEvent e) {
  }

  public void mousePressed(MouseEvent e) {
    if (SwingUtilities.isLeftMouseButton(e)) {
      AbstractButton b = (AbstractButton) e.getSource();
      if (b.contains(e.getX(), e.getY())) {
        long multiClickThreshhold = b.getMultiClickThreshhold();
        long lastTime = lastPressedTimestamp;
        long currentTime = lastPressedTimestamp = e.getWhen();
        if (lastTime != -1 && currentTime - lastTime < multiClickThreshhold) {
          shouldDiscardRelease = true;
          return;
        }
        ButtonModel model = b.getModel();
        if (!model.isEnabled()) {
          return;
        }
        if (!model.isArmed()) {
          model.setArmed(true);
        }
        model.setPressed(true);
        if (!b.hasFocus() && b.isRequestFocusEnabled()) {
          b.requestFocus();
        }
      }
    }
  }

  public void mouseReleased(MouseEvent e) {
    if (SwingUtilities.isLeftMouseButton(e)) {
      AbstractButton b = (AbstractButton) e.getSource();
      ButtonModel model = b.getModel();
      model.setPressed(false);
      model.setArmed(false);
      if (shouldDiscardRelease) {
        shouldDiscardRelease = false;
      }
    }
  }

  public void mouseEntered(MouseEvent e) {
    AbstractButton b = (AbstractButton) e.getSource();
    ButtonModel model = b.getModel();
    if (b.isRolloverEnabled() && !SwingUtilities.isLeftMouseButton(e)) {
      model.setRollover(true);
    }
    if (model.isPressed()) {
      model.setArmed(true);
    }
  }

  public void mouseExited(MouseEvent e) {
    AbstractButton b = (AbstractButton) e.getSource();
    ButtonModel model = b.getModel();
    if (b.isRolloverEnabled()) {
      model.setRollover(false);
    }
    model.setArmed(false);
  }

  private static final class PressAction extends AbstractAction implements UIResource {
    private final boolean press;

    PressAction(boolean press) {
      this.press = press;
    }

    public void actionPerformed(ActionEvent e) {
      AbstractButton b = (AbstractButton) e.getSource();
      ButtonModel model = b.getModel();
      if (press) {
        model.setArmed(true);
        model.setPressed(true);
        if (!b.hasFocus()) {
          b.requestFocus();
        }
      } else {
        model.setPressed(false);
        model.setArmed(false);
      }
    }

    public boolean isEnabled() {
      return true;
    }
  }
}
