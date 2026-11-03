/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */
package javax.swing.plaf.basic;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.BoxLayout;
import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.LookAndFeel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.OptionPaneUI;
import javax.swing.plaf.UIResource;

public class BasicOptionPaneUI extends OptionPaneUI {
  public static final int MinimumWidth = 262;
  public static final int MinimumHeight = 90;
  private static final String kNewline = System.lineSeparator();
  protected JOptionPane optionPane;
  protected Dimension minimumSize;
  protected JComponent inputComponent;
  protected Component initialFocusComponent;
  protected boolean hasCustomComponents;
  protected PropertyChangeListener propertyChangeListener;

  public BasicOptionPaneUI() {
  }

  public static ComponentUI createUI(JComponent x) {
    return new BasicOptionPaneUI();
  }

  public void installUI(JComponent c) {
    optionPane = (JOptionPane) c;
    installDefaults();
    optionPane.setLayout(createLayoutManager());
    installComponents();
    installListeners();
    installKeyboardActions();
  }

  public void uninstallUI(JComponent c) {
    uninstallComponents();
    optionPane.setLayout(null);
    uninstallKeyboardActions();
    uninstallListeners();
    uninstallDefaults();
    optionPane = null;
  }

  protected void installDefaults() {
    LookAndFeel.installColorsAndFont(optionPane, "OptionPane.background", "OptionPane.foreground",
        "OptionPane.font");
    LookAndFeel.installBorder(optionPane, "OptionPane.border");
    minimumSize = UIManager.getDimension("OptionPane.minimumSize");
    LookAndFeel.installProperty(optionPane, "opaque", Boolean.TRUE);
  }

  protected void uninstallDefaults() {
    LookAndFeel.uninstallBorder(optionPane);
  }

  protected void installComponents() {
    optionPane.add(createMessageArea());
    Container separator = createSeparator();
    if (separator != null) {
      optionPane.add(separator);
    }
    optionPane.add(createButtonArea());
    optionPane.applyComponentOrientation(optionPane.getComponentOrientation());
  }

  protected void uninstallComponents() {
    hasCustomComponents = false;
    inputComponent = null;
    initialFocusComponent = null;
    optionPane.removeAll();
  }

  protected LayoutManager createLayoutManager() {
    return new BoxLayout(optionPane, BoxLayout.Y_AXIS);
  }

  protected void installListeners() {
    if ((propertyChangeListener = createPropertyChangeListener()) != null) {
      optionPane.addPropertyChangeListener(propertyChangeListener);
    }
  }

  protected void uninstallListeners() {
    if (propertyChangeListener != null) {
      optionPane.removePropertyChangeListener(propertyChangeListener);
      propertyChangeListener = null;
    }
  }

  protected PropertyChangeListener createPropertyChangeListener() {
    return new PropertyChangeHandler();
  }

  protected void installKeyboardActions() {
  }

  protected void uninstallKeyboardActions() {
  }

  protected Object getMessage() {
    inputComponent = null;
    if (optionPane != null) {
      if (optionPane.getWantsInput()) {
        Object message = optionPane.getMessage();
        Object[] sValues = optionPane.getSelectionValues();
        Object inputValue = optionPane.getInitialSelectionValue();
        JComponent toAdd;
        if (sValues != null) {
          if (sValues.length < 20) {
            JComboBox<Object> cb = new JComboBox<Object>();
            cb.setName("OptionPane.comboBox");
            for (int counter = 0; counter < sValues.length; counter++) {
              cb.addItem(sValues[counter]);
            }
            if (inputValue != null) {
              cb.setSelectedItem(inputValue);
            }
            inputComponent = cb;
            toAdd = cb;
          } else {
            JList<Object> list = new JList<Object>(sValues);
            JScrollPane sp = new JScrollPane(list);
            sp.setName("OptionPane.scrollPane");
            list.setName("OptionPane.list");
            list.setVisibleRowCount(10);
            list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            if (inputValue != null) {
              list.setSelectedValue(inputValue, true);
            }
            toAdd = sp;
            inputComponent = list;
          }
        } else {
          JTextField tf = new JTextField(20);
          tf.setName("OptionPane.textField");
          if (inputValue != null) {
            String inputString = inputValue.toString();
            tf.setText(inputString);
            tf.setSelectionStart(0);
            tf.setSelectionEnd(inputString.length());
          }
          toAdd = inputComponent = tf;
        }
        Object[] newMessage;
        if (message == null) {
          newMessage = new Object[1];
          newMessage[0] = toAdd;
        } else {
          newMessage = new Object[2];
          newMessage[0] = message;
          newMessage[1] = toAdd;
        }
        return newMessage;
      }
      return optionPane.getMessage();
    }
    return null;
  }

  protected void addMessageComponents(Container container, GridBagConstraints cons, Object msg, int maxll,
      boolean internallyCreated) {
    if (msg == null) {
      return;
    }
    if (msg instanceof Object[]) {
      Object[] msgs = (Object[]) msg;
      for (Object o : msgs) {
        addMessageComponents(container, cons, o, maxll, false);
      }
    } else if (msg instanceof Component) {
      cons.weightx = 1;
      cons.weighty = 0;
      cons.fill = GridBagConstraints.HORIZONTAL;
      container.add((Component) msg, cons);
      cons.weightx = 0;
      cons.weighty = 0;
      cons.fill = GridBagConstraints.NONE;
      cons.gridy++;
      if (!internallyCreated) {
        hasCustomComponents = true;
      }
    } else if (msg instanceof Icon) {
      JLabel label = new JLabel((Icon) msg, SwingConstants.CENTER);
      configureMessageLabel(label);
      addMessageComponents(container, cons, label, maxll, true);
    } else {
      String s = msg.toString();
      int len = s.length();
      if (len <= 0) {
        return;
      }
      int nl;
      int nll = 0;
      if ((nl = s.indexOf(kNewline)) >= 0) {
        nll = kNewline.length();
      } else if ((nl = s.indexOf("\r\n")) >= 0) {
        nll = 2;
      } else if ((nl = s.indexOf('\n')) >= 0) {
        nll = 1;
      }
      if (nl >= 0) {
        if (nl == 0) {
          JPanel breakPanel = new JPanel() {
            public Dimension getPreferredSize() {
              Font f = getFont();
              if (f != null) {
                return new Dimension(1, f.getSize() + 2);
              }
              return new Dimension(0, 0);
            }
          };
          breakPanel.setName("OptionPane.break");
          addMessageComponents(container, cons, breakPanel, maxll, true);
        } else {
          addMessageComponents(container, cons, s.substring(0, nl), maxll, false);
        }
        addMessageComponents(container, cons, s.substring(nl + nll), maxll, false);
      } else if (len > maxll) {
        Container c = Box.createVerticalBox();
        c.setName("OptionPane.verticalBox");
        burstStringInto(c, s, maxll);
        addMessageComponents(container, cons, c, maxll, true);
      } else {
        JLabel label = new JLabel(s, SwingConstants.LEADING);
        label.setName("OptionPane.label");
        configureMessageLabel(label);
        addMessageComponents(container, cons, label, maxll, true);
      }
    }
  }

  private void configureMessageLabel(JLabel label) {
    Color color = UIManager.getColor("OptionPane.messageForeground");
    if (color != null) {
      label.setForeground(color);
    }
    Font messageFont = UIManager.getFont("OptionPane.messageFont");
    if (messageFont != null) {
      label.setFont(messageFont);
    }
  }

  protected void burstStringInto(Container c, String d, int maxll) {
    int len = d.length();
    if (len <= 0) {
      return;
    }
    if (len > maxll) {
      int p = d.lastIndexOf(' ', maxll);
      if (p <= 0) {
        p = d.indexOf(' ', maxll);
      }
      if (p > 0 && p < len) {
        burstStringInto(c, d.substring(0, p), maxll);
        burstStringInto(c, d.substring(p + 1), maxll);
        return;
      }
    }
    JLabel label = new JLabel(d);
    label.setName("OptionPane.label");
    configureMessageLabel(label);
    c.add(label);
  }

  protected Container createMessageArea() {
    JPanel top = new JPanel();
    Border topBorder = UIManager.getBorder("OptionPane.messageAreaBorder");
    if (topBorder != null) {
      top.setBorder(topBorder);
    }
    top.setLayout(new BorderLayout());
    Container body = new JPanel(new GridBagLayout());
    Container realBody = new JPanel(new BorderLayout());
    body.setName("OptionPane.body");
    realBody.setName("OptionPane.realBody");
    if (getIcon() != null) {
      JPanel sep = new JPanel();
      sep.setName("OptionPane.separator");
      sep.setPreferredSize(new Dimension(15, 1));
      realBody.add(sep, BorderLayout.BEFORE_LINE_BEGINS);
    }
    realBody.add(body, BorderLayout.CENTER);
    GridBagConstraints cons = new GridBagConstraints();
    cons.gridx = cons.gridy = 0;
    cons.gridwidth = GridBagConstraints.REMAINDER;
    cons.gridheight = 1;
    Object anchor = UIManager.get("OptionPane.messageAnchor");
    cons.anchor = anchor instanceof Integer ? ((Integer) anchor).intValue() : GridBagConstraints.CENTER;
    cons.insets = new Insets(0, 0, 3, 0);
    addMessageComponents(body, cons, getMessage(), getMaxCharactersPerLineCount(), false);
    top.add(realBody, BorderLayout.CENTER);
    addIcon(top);
    return top;
  }

  protected Container createSeparator() {
    return null;
  }

  protected int getMaxCharactersPerLineCount() {
    return optionPane.getMaxCharactersPerLineCount();
  }

  protected void addIcon(Container top) {
    Icon sideIcon = getIcon();
    if (sideIcon != null) {
      JLabel iconLabel = new JLabel(sideIcon);
      iconLabel.setName("OptionPane.iconLabel");
      iconLabel.setVerticalAlignment(SwingConstants.TOP);
      top.add(iconLabel, BorderLayout.BEFORE_LINE_BEGINS);
    }
  }

  protected Icon getIcon() {
    Icon mIcon = (optionPane == null ? null : optionPane.getIcon());
    if (mIcon == null && optionPane != null) {
      mIcon = getIconForType(optionPane.getMessageType());
    }
    return mIcon;
  }

  protected Icon getIconForType(int messageType) {
    if (messageType < 0 || messageType > 3) {
      return null;
    }
    String propertyName = null;
    switch (messageType) {
      case 0:
        propertyName = "OptionPane.errorIcon";
        break;
      case 1:
        propertyName = "OptionPane.informationIcon";
        break;
      case 2:
        propertyName = "OptionPane.warningIcon";
        break;
      case 3:
        propertyName = "OptionPane.questionIcon";
        break;
      default:
    }
    return propertyName == null ? null : UIManager.getIcon(propertyName);
  }

  protected Container createButtonArea() {
    JPanel bottom = new JPanel();
    Border border = UIManager.getBorder("OptionPane.buttonAreaBorder");
    bottom.setName("OptionPane.buttonArea");
    if (border != null) {
      bottom.setBorder(border);
    }
    bottom.setLayout(new ButtonAreaLayout(true, 6));
    addButtonComponents(bottom, getButtons(), getInitialValueIndex());
    return bottom;
  }

  protected void addButtonComponents(Container container, Object[] buttons, int initialIndex) {
    if (buttons != null && buttons.length > 0) {
      boolean sizeButtonsToSame = getSizeButtonsToSameWidth();
      boolean createdAll = true;
      int numButtons = buttons.length;
      JButton[] createdButtons = null;
      if (sizeButtonsToSame) {
        createdButtons = new JButton[numButtons];
      }
      for (int counter = 0; counter < numButtons; counter++) {
        Object button = buttons[counter];
        Component newComponent;
        if (button instanceof Component) {
          createdAll = false;
          newComponent = (Component) button;
          container.add(newComponent);
          hasCustomComponents = true;
        } else {
          JButton aButton;
          if (button instanceof ButtonFactory) {
            aButton = ((ButtonFactory) button).createButton();
          } else if (button instanceof Icon) {
            aButton = new JButton((Icon) button);
          } else {
            aButton = new JButton(button.toString());
          }
          aButton.setName("OptionPane.button");
          aButton.setMultiClickThreshhold(UIManager.getInt("OptionPane.buttonClickThreshhold"));
          container.add(aButton);
          ActionListener buttonListener = createButtonActionListener(counter);
          if (buttonListener != null) {
            aButton.addActionListener(buttonListener);
          }
          newComponent = aButton;
        }
        if (sizeButtonsToSame && createdAll && (newComponent instanceof JButton)) {
          createdButtons[counter] = (JButton) newComponent;
        }
        if (counter == initialIndex) {
          initialFocusComponent = newComponent;
        }
      }
      ((ButtonAreaLayout) container.getLayout()).setSyncAllWidths(sizeButtonsToSame && createdAll);
      if (sizeButtonsToSame && createdAll) {
        int padSize = numButtons <= 2 ? 8 : 4;
        for (JButton createdButton : createdButtons) {
          createdButton.setMargin(new Insets(2, padSize, 2, padSize));
        }
      }
    }
  }

  protected ActionListener createButtonActionListener(int buttonIndex) {
    return new ButtonActionListener(buttonIndex);
  }

  protected Object[] getButtons() {
    if (optionPane != null) {
      Object[] suppliedOptions = optionPane.getOptions();
      if (suppliedOptions == null) {
        ButtonFactory[] defaultOptions;
        int type = optionPane.getOptionType();
        if (type == JOptionPane.YES_NO_OPTION) {
          defaultOptions = new ButtonFactory[] {factory("yes"), factory("no")};
        } else if (type == JOptionPane.YES_NO_CANCEL_OPTION) {
          defaultOptions = new ButtonFactory[] {factory("yes"), factory("no"), factory("cancel")};
        } else if (type == JOptionPane.OK_CANCEL_OPTION) {
          defaultOptions = new ButtonFactory[] {factory("ok"), factory("cancel")};
        } else {
          defaultOptions = new ButtonFactory[] {factory("ok")};
        }
        return defaultOptions;
      }
      return suppliedOptions;
    }
    return null;
  }

  private ButtonFactory factory(String name) {
    String text = UIManager.getString("OptionPane." + name + "ButtonText");
    Object mnemonic = UIManager.get("OptionPane." + name + "ButtonMnemonic");
    int m = 0;
    if (mnemonic instanceof Integer) {
      m = ((Integer) mnemonic).intValue();
    } else if (mnemonic instanceof String) {
      try {
        m = Integer.parseInt((String) mnemonic);
      } catch (NumberFormatException e) {
        m = 0;
      }
    }
    return new ButtonFactory(text, m, UIManager.getIcon("OptionPane." + name + "Icon"));
  }

  protected boolean getSizeButtonsToSameWidth() {
    return true;
  }

  protected int getInitialValueIndex() {
    if (optionPane != null) {
      Object iv = optionPane.getInitialValue();
      Object[] options = optionPane.getOptions();
      if (options == null) {
        return 0;
      } else if (iv != null) {
        for (int counter = options.length - 1; counter >= 0; counter--) {
          if (options[counter].equals(iv)) {
            return counter;
          }
        }
      }
    }
    return -1;
  }

  protected void resetInputValue() {
    if (inputComponent != null && (inputComponent instanceof JTextField)) {
      optionPane.setInputValue(((JTextField) inputComponent).getText());
    } else if (inputComponent != null && (inputComponent instanceof JComboBox)) {
      optionPane.setInputValue(((JComboBox<?>) inputComponent).getSelectedItem());
    } else if (inputComponent != null) {
      optionPane.setInputValue(((JList<?>) inputComponent).getSelectedValue());
    }
  }

  public void selectInitialValue(JOptionPane op) {
    if (inputComponent != null) {
      inputComponent.requestFocus();
    } else {
      if (initialFocusComponent != null) {
        initialFocusComponent.requestFocus();
      }
      if (initialFocusComponent instanceof JButton) {
        JRootPane root = SwingUtilities.getRootPane(initialFocusComponent);
        if (root != null) {
          root.setDefaultButton((JButton) initialFocusComponent);
        }
      }
    }
  }

  public boolean containsCustomComponents(JOptionPane op) {
    return hasCustomComponents;
  }

  public Dimension getPreferredSize(JComponent c) {
    if (c == optionPane) {
      Dimension ourMin = getMinimumOptionPaneSize();
      LayoutManager lm = c.getLayout();
      if (lm != null) {
        Dimension lmSize = lm.preferredLayoutSize(c);
        if (ourMin != null) {
          return new Dimension(Math.max(lmSize.width, ourMin.width), Math.max(lmSize.height, ourMin.height));
        }
        return lmSize;
      }
      return ourMin;
    }
    return null;
  }

  private Dimension getMinimumOptionPaneSize() {
    if (minimumSize == null) {
      return new Dimension(MinimumWidth, MinimumHeight);
    }
    return new Dimension(minimumSize.width, minimumSize.height);
  }

  public static class ButtonAreaLayout implements LayoutManager {
    protected boolean syncAllWidths;
    protected int padding;
    protected boolean centersChildren;
    private int orientation = SwingConstants.CENTER;
    private boolean useOrientation;

    public ButtonAreaLayout(boolean syncAllWidths, int padding) {
      this.syncAllWidths = syncAllWidths;
      this.padding = padding;
      centersChildren = true;
    }

    public void setSyncAllWidths(boolean newValue) {
      syncAllWidths = newValue;
    }

    public boolean getSyncAllWidths() {
      return syncAllWidths;
    }

    public void setPadding(int newPadding) {
      this.padding = newPadding;
    }

    public int getPadding() {
      return padding;
    }

    public void setCentersChildren(boolean newValue) {
      centersChildren = newValue;
      useOrientation = false;
    }

    public boolean getCentersChildren() {
      return centersChildren;
    }

    public void addLayoutComponent(String string, Component comp) {
    }

    public void layoutContainer(Container container) {
      Component[] children = container.getComponents();
      if (children != null && children.length > 0) {
        int numChildren = children.length;
        Insets insets = container.getInsets();
        int maxWidth = 0;
        int maxHeight = 0;
        int totalButtonWidth = 0;
        for (int counter = 0; counter < numChildren; counter++) {
          Dimension pref = children[counter].getPreferredSize();
          maxWidth = Math.max(maxWidth, pref.width);
          maxHeight = Math.max(maxHeight, pref.height);
          totalButtonWidth += pref.width;
        }
        if (getSyncAllWidths()) {
          totalButtonWidth = maxWidth * numChildren;
        }
        totalButtonWidth += (numChildren - 1) * padding;
        int x;
        if (getCentersChildren() || numChildren < 2) {
          x = (container.getWidth() - totalButtonWidth) / 2;
        } else {
          x = container.getWidth() - insets.right - totalButtonWidth;
        }
        boolean ltr = container.getComponentOrientation().isLeftToRight();
        for (int counter = 0; counter < numChildren; counter++) {
          Component child = children[ltr ? counter : numChildren - 1 - counter];
          Dimension pref = child.getPreferredSize();
          int w = getSyncAllWidths() ? maxWidth : pref.width;
          int h = getSyncAllWidths() ? maxHeight : pref.height;
          child.setBounds(x, insets.top, w, h);
          x += w + padding;
        }
      }
    }

    public Dimension minimumLayoutSize(Container c) {
      if (c != null) {
        Component[] children = c.getComponents();
        if (children != null && children.length > 0) {
          int numChildren = children.length;
          int height = 0;
          Insets cInsets = c.getInsets();
          int extraHeight = cInsets.top + cInsets.bottom;
          int extraWidth = cInsets.left + cInsets.right;
          if (syncAllWidths) {
            int maxWidth = 0;
            for (int counter = 0; counter < numChildren; counter++) {
              Dimension aSize = children[counter].getPreferredSize();
              height = Math.max(height, aSize.height);
              maxWidth = Math.max(maxWidth, aSize.width);
            }
            return new Dimension(extraWidth + (maxWidth * numChildren + (numChildren - 1) * padding),
                extraHeight + height);
          } else {
            int totalWidth = 0;
            for (int counter = 0; counter < numChildren; counter++) {
              Dimension aSize = children[counter].getPreferredSize();
              height = Math.max(height, aSize.height);
              totalWidth += aSize.width;
            }
            totalWidth += ((numChildren - 1) * padding);
            return new Dimension(extraWidth + totalWidth, extraHeight + height);
          }
        }
      }
      return new Dimension(0, 0);
    }

    public Dimension preferredLayoutSize(Container c) {
      return minimumLayoutSize(c);
    }

    public void removeLayoutComponent(Component c) {
    }
  }

  public class PropertyChangeHandler implements PropertyChangeListener {
    public PropertyChangeHandler() {
    }

    public void propertyChange(PropertyChangeEvent e) {
      if (e.getSource() == optionPane) {
        String changeName = e.getPropertyName();
        if (changeName == JOptionPane.OPTIONS_PROPERTY || changeName == JOptionPane.INITIAL_VALUE_PROPERTY
            || changeName == JOptionPane.ICON_PROPERTY || changeName == JOptionPane.MESSAGE_TYPE_PROPERTY
            || changeName == JOptionPane.OPTION_TYPE_PROPERTY || changeName == JOptionPane.MESSAGE_PROPERTY
            || changeName == JOptionPane.SELECTION_VALUES_PROPERTY
            || changeName == JOptionPane.INITIAL_SELECTION_VALUE_PROPERTY
            || changeName == JOptionPane.WANTS_INPUT_PROPERTY) {
          uninstallComponents();
          installComponents();
          optionPane.validate();
        } else if (changeName == "componentOrientation") {
          optionPane.applyComponentOrientation((java.awt.ComponentOrientation) e.getNewValue());
        }
      }
    }
  }

  public class ButtonActionListener implements ActionListener {
    protected int buttonIndex;

    public ButtonActionListener(int buttonIndex) {
      this.buttonIndex = buttonIndex;
    }

    public void actionPerformed(ActionEvent e) {
      if (optionPane != null) {
        int optionType = optionPane.getOptionType();
        Object[] options = optionPane.getOptions();
        if (inputComponent != null && (options != null || optionType != JOptionPane.DEFAULT_OPTION)
            && (options == null ? buttonIndex != (optionType == JOptionPane.OK_CANCEL_OPTION ? 1 : 2)
                : true)) {
          resetInputValue();
        }
        if (options == null) {
          int index = buttonIndex;
          if (optionType == JOptionPane.OK_CANCEL_OPTION && index == 1) {
            index = 2;
          }
          optionPane.setValue(Integer.valueOf(index));
        } else {
          optionPane.setValue(options[buttonIndex]);
        }
      }
    }
  }

  private static final class ButtonFactory {
    private final String text;
    private final int mnemonic;
    private final Icon icon;

    ButtonFactory(String text, int mnemonic, Icon icon) {
      this.text = text;
      this.mnemonic = mnemonic;
      this.icon = icon;
    }

    JButton createButton() {
      JButton button = new JButton(text);
      if (icon != null) {
        button.setIcon(icon);
      }
      if (mnemonic != 0) {
        button.setMnemonic(mnemonic);
      }
      return button;
    }
  }
}
