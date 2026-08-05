/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing;

import java.awt.Color;
import java.awt.Font;
import java.awt.Insets;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.BorderUIResource;
import javax.swing.plaf.UIResource;
import javax.swing.plaf.basic.BasicBorders;
import javax.swing.plaf.metal.MetalBorders;
import javax.swing.text.JTextComponent;

final class ShimDefaults {
  private ShimDefaults() { }

  static void install(JComponent c) {
    String id = c.getUIClassID();
    String prefix = prefixFor(id);
    if (prefix == null) {
      return;
    }
    LookAndFeel.installColorsAndFont(c, prefix + ".background", prefix + ".foreground", prefix + ".font");
    switch (id) {
      case "PanelUI":
        setOpaqueUI(c, true);
        break;
      case "LabelUI":
        setOpaqueUI(c, false);
        break;
      case "ButtonUI":
      case "ToggleButtonUI":
      case "CheckBoxUI":
      case "RadioButtonUI":
        installButton((AbstractButton) c, prefix);
        break;
      case "ScrollPaneUI":
        setOpaqueUI(c, true);
        break;
      case "MenuItemUI":
      case "MenuUI":
      case "CheckBoxMenuItemUI":
      case "RadioButtonMenuItemUI":
        c.setUIProperty("borderPainted", Boolean.valueOf(UIManager.getBoolean(prefix + ".borderPainted")));
        break;
      case "ListUI":
      case "TableUI":
      case "TreeUI":
        setOpaqueUI(c, true);
        break;
      default:
        break;
    }
    installBorderFor(c, id);
    if (c instanceof JTextComponent) {
      installText((JTextComponent) c, prefix);
    }
  }

  private static void installBorderFor(JComponent c, String id) {
    Border cur = c.getBorder();
    if (cur != null && !(cur instanceof UIResource)) {
      return;
    }
    Border b = null;
    switch (id) {
      case "ButtonUI": b = MetalBorders.getButtonBorder(); break;
      case "ToggleButtonUI": b = MetalBorders.getToggleButtonBorder(); break;
      case "CheckBoxUI":
      case "RadioButtonUI":
        b = new BorderUIResource.CompoundBorderUIResource(
            new BasicBorders.RadioButtonBorder(null, UIManager.getColor("controlDarkShadow"), null, null),
            new BasicBorders.MarginBorder());
        break;
      case "TextFieldUI":
      case "PasswordFieldUI":
      case "FormattedTextFieldUI": b = MetalBorders.getTextFieldBorder(); break;
      case "TextAreaUI":
      case "TextPaneUI":
      case "EditorPaneUI": b = new BasicBorders.MarginBorder(); break;
      case "ScrollPaneUI": b = new MetalBorders.ScrollPaneBorder(); break;
      case "ProgressBarUI":
      case "ToolTipUI": b = new BorderUIResource.LineBorderUIResource(UIManager.getColor("controlDarkShadow")); break;
      case "ToolBarUI": b = new MetalBorders.ToolBarBorder(); break;
      case "MenuBarUI": b = new MetalBorders.MenuBarBorder(); break;
      case "PopupMenuUI": b = new MetalBorders.PopupMenuBorder(); break;
      case "MenuItemUI":
      case "MenuUI":
      case "CheckBoxMenuItemUI":
      case "RadioButtonMenuItemUI": b = new MetalBorders.MenuItemBorder(); break;
      case "SplitPaneUI": b = BasicBorders.getSplitPaneBorder(); break;
      case "SpinnerUI": b = new BorderUIResource.CompoundBorderUIResource(new MetalBorders.Flush3DBorder(),
          new javax.swing.border.EmptyBorder(0, 0, 0, 0)); break;
      default: return;
    }
    c.setBorder(b);
  }

  private static void setOpaqueUI(JComponent c, boolean v) { c.setUIProperty("opaque", Boolean.valueOf(v)); }

  private static String prefixFor(String id) {
    switch (id) {
      case "PanelUI": return "Panel";
      case "LabelUI": return "Label";
      case "ButtonUI": return "Button";
      case "ToggleButtonUI": return "ToggleButton";
      case "CheckBoxUI": return "CheckBox";
      case "RadioButtonUI": return "RadioButton";
      case "TextFieldUI": return "TextField";
      case "PasswordFieldUI": return "PasswordField";
      case "FormattedTextFieldUI": return "FormattedTextField";
      case "TextAreaUI": return "TextArea";
      case "TextPaneUI": return "TextPane";
      case "EditorPaneUI": return "EditorPane";
      case "ListUI": return "List";
      case "TableUI": return "Table";
      case "TreeUI": return "Tree";
      case "ComboBoxUI": return "ComboBox";
      case "SpinnerUI": return "Spinner";
      case "TabbedPaneUI": return "TabbedPane";
      case "ToolTipUI": return "ToolTip";
      case "ProgressBarUI": return "ProgressBar";
      case "SliderUI": return "Slider";
      case "ScrollPaneUI": return "ScrollPane";
      case "ViewportUI": return "Viewport";
      case "MenuUI": return "Menu";
      case "MenuItemUI": return "MenuItem";
      case "CheckBoxMenuItemUI": return "CheckBoxMenuItem";
      case "RadioButtonMenuItemUI": return "RadioButtonMenuItem";
      case "MenuBarUI": return "MenuBar";
      case "PopupMenuUI": return "PopupMenu";
      case "ToolBarUI": return "ToolBar";
      case "OptionPaneUI": return "OptionPane";
      case "TableHeaderUI": return "TableHeader";
      default: return null;
    }
  }

  private static void installButton(AbstractButton b, String prefix) {
    setOpaqueUI(b, true);
    Insets margin = b.getMargin();
    if (margin == null || margin instanceof UIResource) {
      Insets m = UIManager.getInsets(prefix + ".margin");
      if (m != null) {
        b.setMargin(m);
      }
    }
    Object gap = UIManager.get(prefix + ".textIconGap");
    if (gap instanceof Integer) {
      b.setIconTextGap(((Integer) gap).intValue());
    }
    Object rollover = UIManager.get(prefix + ".rollover");
    if (rollover instanceof Boolean) {
      b.setRolloverEnabled(((Boolean) rollover).booleanValue());
    }
  }

  private static void installText(JTextComponent t, String prefix) {
    Color caret = t.getCaretColor();
    if (caret == null || caret instanceof UIResource) {
      t.setCaretColor(UIManager.getColor(prefix + ".caretForeground"));
    }
    Color sel = t.getSelectionColor();
    if (sel == null || sel instanceof UIResource) {
      t.setSelectionColor(UIManager.getColor(prefix + ".selectionBackground"));
    }
    Color selText = t.getSelectedTextColor();
    if (selText == null || selText instanceof UIResource) {
      t.setSelectedTextColor(UIManager.getColor(prefix + ".selectionForeground"));
    }
    Color dis = t.getDisabledTextColor();
    if (dis == null || dis instanceof UIResource) {
      t.setDisabledTextColor(UIManager.getColor(prefix + ".inactiveForeground"));
    }
    Insets m = t.getMargin();
    if (m == null || m instanceof UIResource) {
      Insets d = UIManager.getInsets(prefix + ".margin");
      if (d != null) {
        t.setMargin(d);
      }
    }
    setOpaqueUI(t, true);
    if (t.getCaret() != null) {
      Object rate = UIManager.get(prefix + ".caretBlinkRate");
      if (rate instanceof Integer) {
        t.getCaret().setBlinkRate(((Integer) rate).intValue());
      }
    }
  }
}
