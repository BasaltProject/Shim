/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing.plaf.multi;

import java.util.Vector;
import javax.swing.JComponent;
import javax.swing.LookAndFeel;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.plaf.ComponentUI;

public class MultiLookAndFeel extends LookAndFeel {
  public MultiLookAndFeel() { }

  public String getName() {
    return "Multiplexing Look and Feel";
  }

  public String getID() {
    return "Multiplex";
  }

  public String getDescription() {
    return "Allows multiple UI instances per component instance";
  }

  public boolean isNativeLookAndFeel() {
    return false;
  }

  public boolean isSupportedLookAndFeel() {
    return true;
  }

  public UIDefaults getDefaults() {
    String prefix = "javax.swing.plaf.multi.Multi";
    Object[] uiDefaults = {
        "ButtonUI", prefix + "ButtonUI",
        "CheckBoxMenuItemUI", prefix + "MenuItemUI",
        "CheckBoxUI", prefix + "ButtonUI",
        "ColorChooserUI", prefix + "ColorChooserUI",
        "ComboBoxUI", prefix + "ComboBoxUI",
        "DesktopIconUI", prefix + "DesktopIconUI",
        "DesktopPaneUI", prefix + "DesktopPaneUI",
        "EditorPaneUI", prefix + "TextUI",
        "FileChooserUI", prefix + "FileChooserUI",
        "FormattedTextFieldUI", prefix + "TextUI",
        "InternalFrameUI", prefix + "InternalFrameUI",
        "LabelUI", prefix + "LabelUI",
        "ListUI", prefix + "ListUI",
        "MenuBarUI", prefix + "MenuBarUI",
        "MenuItemUI", prefix + "MenuItemUI",
        "MenuUI", prefix + "MenuItemUI",
        "OptionPaneUI", prefix + "OptionPaneUI",
        "PanelUI", prefix + "PanelUI",
        "PasswordFieldUI", prefix + "TextUI",
        "PopupMenuSeparatorUI", prefix + "SeparatorUI",
        "PopupMenuUI", prefix + "PopupMenuUI",
        "ProgressBarUI", prefix + "ProgressBarUI",
        "RadioButtonMenuItemUI", prefix + "MenuItemUI",
        "RadioButtonUI", prefix + "ButtonUI",
        "RootPaneUI", prefix + "RootPaneUI",
        "ScrollBarUI", prefix + "ScrollBarUI",
        "ScrollPaneUI", prefix + "ScrollPaneUI",
        "SeparatorUI", prefix + "SeparatorUI",
        "SliderUI", prefix + "SliderUI",
        "SpinnerUI", prefix + "SpinnerUI",
        "SplitPaneUI", prefix + "SplitPaneUI",
        "TabbedPaneUI", prefix + "TabbedPaneUI",
        "TableHeaderUI", prefix + "TableHeaderUI",
        "TableUI", prefix + "TableUI",
        "TextAreaUI", prefix + "TextUI",
        "TextFieldUI", prefix + "TextUI",
        "TextPaneUI", prefix + "TextUI",
        "ToggleButtonUI", prefix + "ButtonUI",
        "ToolBarSeparatorUI", prefix + "SeparatorUI",
        "ToolBarUI", prefix + "ToolBarUI",
        "ToolTipUI", prefix + "ToolTipUI",
        "TreeUI", prefix + "TreeUI",
        "ViewportUI", prefix + "ViewportUI"
    };
    UIDefaults table = new UIDefaults();
    table.putDefaults(uiDefaults);
    return table;
  }

  public static ComponentUI createUIs(ComponentUI mui, Vector<ComponentUI> uis, JComponent target) {
    ComponentUI ui;
    UIDefaults defaults = UIManager.getLookAndFeelDefaults();
    ui = defaults.getUI(target);
    if (ui != null) {
      uis.addElement(ui);
    }
    javax.swing.LookAndFeel[] auxiliaryLookAndFeels = UIManager.getAuxiliaryLookAndFeels();
    if (auxiliaryLookAndFeels != null) {
      for (int i = 0; i < auxiliaryLookAndFeels.length; i++) {
        ui = auxiliaryLookAndFeels[i].getDefaults().getUI(target);
        if (ui != null) {
          uis.addElement(ui);
        }
      }
    }
    if (uis.size() == 0) {
      return null;
    }
    if (uis.size() == 1) {
      return uis.elementAt(0);
    }
    return mui;
  }

  protected static ComponentUI[] uisToArray(Vector<? extends ComponentUI> uis) {
    if (uis == null) {
      return new ComponentUI[0];
    }
    int count = uis.size();
    if (count > 0) {
      ComponentUI[] u = new ComponentUI[count];
      for (int i = 0; i < count; i++) {
        u[i] = uis.elementAt(i);
      }
      return u;
    }
    return null;
  }
}
