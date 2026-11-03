/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.swing;

import java.awt.Color;
import javax.swing.UIDefaults;
import javax.swing.border.Border;
import javax.swing.plaf.BorderUIResource;

public final class MetalExtras {
  private MetalExtras() { }

  public static void fill(UIDefaults t) {
    classes(t);
    t.put("List.cellRenderer", new UIDefaults.ActiveValue() {
      public Object createValue(UIDefaults table) {
        return new javax.swing.DefaultListCellRenderer.UIResource();
      }
    });
    t.put("List.noFocusBorder", new UIDefaults.LazyValue() {
      public Object createValue(UIDefaults table) {
        return new BorderUIResource.EmptyBorderUIResource(1, 1, 1, 1);
      }
    });
    t.put("List.focusCellHighlightBorder", new UIDefaults.LazyValue() {
      public Object createValue(UIDefaults table) {
        return new BorderUIResource.LineBorderUIResource(new Color(0x6382BF));
      }
    });
  }

  private static void lazyBorder(UIDefaults t, String key, final java.util.concurrent.Callable<Border> make) {
    t.put(key, new UIDefaults.LazyValue() {
      public Object createValue(UIDefaults table) {
        try {
          return make.call();
        } catch (Exception e) {
          return null;
        }
      }
    });
  }

  private static void lazyIcon(UIDefaults t, String key, final java.util.concurrent.Callable<javax.swing.Icon> make) {
    t.put(key, new UIDefaults.LazyValue() {
      public Object createValue(UIDefaults table) {
        try {
          return new javax.swing.plaf.IconUIResource(make.call());
        } catch (Exception e) {
          return null;
        }
      }
    });
  }

  private static void tableAndTree(UIDefaults t) {
    t.put("Table.focusCellHighlightBorder", new UIDefaults.LazyValue() {
      public Object createValue(UIDefaults table) {
        return new BorderUIResource.LineBorderUIResource(new Color(0x6382BF));
      }
    });
    lazyBorder(t, "Table.scrollPaneBorder", new java.util.concurrent.Callable<Border>() {
      public Border call() { return new javax.swing.plaf.metal.MetalBorders.ScrollPaneBorder(); }
    });
    lazyBorder(t, "ScrollPane.border", new java.util.concurrent.Callable<Border>() {
      public Border call() { return new javax.swing.plaf.metal.MetalBorders.ScrollPaneBorder(); }
    });
    lazyBorder(t, "TableHeader.cellBorder", new java.util.concurrent.Callable<Border>() {
      public Border call() { return new javax.swing.plaf.metal.MetalBorders.TableHeaderBorder(); }
    });
    lazyBorder(t, "Tree.editorBorder", new java.util.concurrent.Callable<Border>() {
      public Border call() {
        return new BorderUIResource.LineBorderUIResource(javax.swing.UIManager.getColor("Tree.selectionBorderColor"));
      }
    });
    java.util.concurrent.Callable<javax.swing.Icon> folder = new java.util.concurrent.Callable<javax.swing.Icon>() {
      public javax.swing.Icon call() { return javax.swing.plaf.metal.MetalIconFactory.getTreeFolderIcon(); }
    };
    lazyIcon(t, "Tree.closedIcon", folder);
    lazyIcon(t, "Tree.openIcon", folder);
    lazyIcon(t, "Tree.leafIcon", new java.util.concurrent.Callable<javax.swing.Icon>() {
      public javax.swing.Icon call() { return javax.swing.plaf.metal.MetalIconFactory.getTreeLeafIcon(); }
    });
    lazyIcon(t, "Tree.expandedIcon", new java.util.concurrent.Callable<javax.swing.Icon>() {
      public javax.swing.Icon call() { return javax.swing.plaf.metal.MetalIconFactory.getTreeControlIcon(false); }
    });
    lazyIcon(t, "Tree.collapsedIcon", new java.util.concurrent.Callable<javax.swing.Icon>() {
      public javax.swing.Icon call() { return javax.swing.plaf.metal.MetalIconFactory.getTreeControlIcon(true); }
    });
    lazyIcon(t, "Table.ascendingSortIcon", new java.util.concurrent.Callable<javax.swing.Icon>() {
      public javax.swing.Icon call() { return new SortIcon(true); }
    });
    lazyIcon(t, "Table.descendingSortIcon", new java.util.concurrent.Callable<javax.swing.Icon>() {
      public javax.swing.Icon call() { return new SortIcon(false); }
    });
  }

  private static final class SortIcon implements javax.swing.Icon {
    private final boolean up;

    SortIcon(boolean up) {
      this.up = up;
    }

    public int getIconWidth() { return 12; }

    public int getIconHeight() { return 12; }

    public void paintIcon(java.awt.Component c, java.awt.Graphics g, int x, int y) {
      g.setColor(javax.swing.UIManager.getColor("Table.sortIconColor"));
      int[] xs = {x + 2, x + 10, x + 6};
      int[] ys = up ? new int[] {y + 8, y + 8, y + 3} : new int[] {y + 3, y + 3, y + 8};
      g.fillPolygon(xs, ys, 3);
    }
  }

  private static void border(UIDefaults t, String key, final int kind) {
    lazyBorder(t, key, new java.util.concurrent.Callable<Border>() {
      public Border call() { return makeBorder(kind); }
    });
  }

  private static Border makeBorder(int kind) {
    javax.swing.UIManager.getColor("controlDarkShadow");
    switch (kind) {
      case 0: return javax.swing.plaf.metal.MetalBorders.getButtonBorder();
      case 1: return javax.swing.plaf.metal.MetalBorders.getToggleButtonBorder();
      case 2: return new BorderUIResource.CompoundBorderUIResource(
          new javax.swing.plaf.basic.BasicBorders.RadioButtonBorder(color("controlShadow"), color("controlDarkShadow"),
              color("controlLtHighlight"), color("controlHighlight")),
          new javax.swing.plaf.basic.BasicBorders.MarginBorder());
      case 3: return new javax.swing.plaf.metal.MetalBorders.MenuItemBorder();
      case 4: return javax.swing.plaf.metal.MetalBorders.getTextFieldBorder();
      case 5: return new javax.swing.plaf.basic.BasicBorders.MarginBorder();
      case 6: return new BorderUIResource.LineBorderUIResource(color("controlDarkShadow"));
      case 7: return new BorderUIResource.LineBorderUIResource(color("primaryControlDarkShadow"));
      case 8: return new javax.swing.plaf.metal.MetalBorders.MenuBarBorder();
      case 9: return new javax.swing.plaf.metal.MetalBorders.PopupMenuBorder();
      case 10: return new javax.swing.plaf.metal.MetalBorders.ToolBarBorder();
      case 11: return new BorderUIResource.EmptyBorderUIResource(10, 10, 12, 10);
      case 12: return new BorderUIResource.EmptyBorderUIResource(6, 0, 0, 0);
      case 13: return new BorderUIResource.EmptyBorderUIResource(0, 0, 0, 0);
      case 14: return new BorderUIResource.CompoundBorderUIResource(new javax.swing.plaf.metal.MetalBorders.TextFieldBorder(),
          new javax.swing.plaf.basic.BasicBorders.MarginBorder());
      case 15: return new BorderUIResource.CompoundBorderUIResource(new javax.swing.plaf.metal.MetalBorders.ButtonBorder(),
          new javax.swing.plaf.basic.BasicBorders.MarginBorder());
      case 16: return javax.swing.plaf.basic.BasicBorders.getSplitPaneBorder();
      case 17: return new javax.swing.plaf.metal.MetalBorders.InternalFrameBorder();
      case 18: return new javax.swing.plaf.metal.MetalBorders.PaletteBorder();
      case 19: return new javax.swing.plaf.metal.MetalBorders.OptionDialogBorder();
      case 20: return javax.swing.plaf.metal.MetalBorders.getDesktopIconBorder();
      case 21: return new javax.swing.border.CompoundBorder(new javax.swing.plaf.metal.MetalBorders.ButtonBorder(),
          new javax.swing.border.EmptyBorder(3, 3, 3, 3));
      default: return null;
    }
  }

  private static void borders(UIDefaults t) {
    border(t, "Button.border", 0);
    border(t, "ToggleButton.border", 1);
    border(t, "CheckBox.border", 2);
    border(t, "RadioButton.border", 2);
    border(t, "CheckBoxMenuItem.border", 3);
    border(t, "Menu.border", 3);
    border(t, "MenuItem.border", 3);
    border(t, "RadioButtonMenuItem.border", 3);
    border(t, "TextField.border", 4);
    border(t, "PasswordField.border", 4);
    border(t, "FormattedTextField.border", 4);
    border(t, "TextArea.border", 5);
    border(t, "TextPane.border", 5);
    border(t, "EditorPane.border", 5);
    border(t, "ProgressBar.border", 6);
    border(t, "TitledBorder.border", 6);
    border(t, "ToolTip.border", 7);
    border(t, "ToolTip.borderInactive", 6);
    border(t, "MenuBar.border", 8);
    border(t, "PopupMenu.border", 9);
    border(t, "ToolBar.border", 10);
    border(t, "OptionPane.border", 11);
    border(t, "OptionPane.buttonAreaBorder", 12);
    border(t, "OptionPane.messageAreaBorder", 13);
    border(t, "Spinner.border", 14);
    border(t, "Spinner.arrowButtonBorder", 15);
    border(t, "SplitPane.border", 16);
    border(t, "InternalFrame.border", 17);
    border(t, "InternalFrame.paletteBorder", 18);
    border(t, "InternalFrame.optionDialogBorder", 19);
    border(t, "DesktopIcon.border", 20);
    border(t, "ToolBar.rolloverBorder", 21);
    border(t, "ToolBar.nonrolloverBorder", 21);
  }

  private static Color color(String key) {
    return javax.swing.UIManager.getColor(key);
  }

  private static void sliders(UIDefaults t) {
    lazyIcon(t, "Slider.horizontalThumbIcon", new java.util.concurrent.Callable<javax.swing.Icon>() {
      public javax.swing.Icon call() { return javax.swing.plaf.metal.MetalIconFactory.getHorizontalSliderThumbIcon(); }
    });
    lazyIcon(t, "Slider.verticalThumbIcon", new java.util.concurrent.Callable<javax.swing.Icon>() {
      public javax.swing.Icon call() { return javax.swing.plaf.metal.MetalIconFactory.getVerticalSliderThumbIcon(); }
    });
    t.put("Slider.thumb", new javax.swing.plaf.ColorUIResource(new Color(0xA3B8CC)));
    t.put("Slider.darkShadow", new javax.swing.plaf.ColorUIResource(new Color(0x7A8A99)));
  }

  private static void menus(UIDefaults t) {
    lazyIcon(t, "MenuItem.arrowIcon", new java.util.concurrent.Callable<javax.swing.Icon>() {
      public javax.swing.Icon call() { return javax.swing.plaf.metal.MetalIconFactory.getMenuItemArrowIcon(); }
    });
    lazyIcon(t, "CheckBoxMenuItem.arrowIcon", new java.util.concurrent.Callable<javax.swing.Icon>() {
      public javax.swing.Icon call() { return javax.swing.plaf.metal.MetalIconFactory.getMenuItemArrowIcon(); }
    });
    lazyIcon(t, "RadioButtonMenuItem.arrowIcon", new java.util.concurrent.Callable<javax.swing.Icon>() {
      public javax.swing.Icon call() { return javax.swing.plaf.metal.MetalIconFactory.getMenuItemArrowIcon(); }
    });
    lazyIcon(t, "Menu.arrowIcon", new java.util.concurrent.Callable<javax.swing.Icon>() {
      public javax.swing.Icon call() { return javax.swing.plaf.metal.MetalIconFactory.getMenuArrowIcon(); }
    });
    lazyIcon(t, "CheckBoxMenuItem.checkIcon", new java.util.concurrent.Callable<javax.swing.Icon>() {
      public javax.swing.Icon call() { return javax.swing.plaf.metal.MetalIconFactory.getCheckBoxMenuItemIcon(); }
    });
    lazyIcon(t, "RadioButtonMenuItem.checkIcon", new java.util.concurrent.Callable<javax.swing.Icon>() {
      public javax.swing.Icon call() { return javax.swing.plaf.metal.MetalIconFactory.getRadioButtonMenuItemIcon(); }
    });
  }

  private static void buttons(UIDefaults t) {
    final java.util.List<Object> gradient = java.util.Arrays.<Object>asList(Float.valueOf(0.3f), Float.valueOf(0f),
        new javax.swing.plaf.ColorUIResource(221, 232, 243), new javax.swing.plaf.ColorUIResource(255, 255, 255),
        new javax.swing.plaf.ColorUIResource(184, 207, 229));
    for (String k : new String[] {"Button", "ToggleButton", "CheckBox", "RadioButton", "ScrollBar", "CheckBoxMenuItem",
        "RadioButtonMenuItem", "InternalFrame.activeTitle"}) {
      t.put(k.contains(".") ? k + "Gradient" : k + ".gradient", gradient);
    }
    t.put("MenuBar.gradient", java.util.Arrays.<Object>asList(Float.valueOf(1f), Float.valueOf(0f),
        new javax.swing.plaf.ColorUIResource(255, 255, 255), new javax.swing.plaf.ColorUIResource(218, 218, 218),
        new javax.swing.plaf.ColorUIResource(218, 218, 218)));
    for (String k : new String[] {"Button", "ToggleButton", "CheckBox", "RadioButton"}) {
      t.put(k + ".focusInputMap", new UIDefaults.LazyInputMap(new Object[] {"SPACE", "pressed", "released SPACE",
          "released"}));
    }
    t.put("CheckBox.icon", new UIDefaults.LazyValue() {
      public Object createValue(UIDefaults table) { return javax.swing.plaf.metal.MetalIconFactory.getCheckBoxIcon(); }
    });
    t.put("RadioButton.icon", new UIDefaults.LazyValue() {
      public Object createValue(UIDefaults table) { return javax.swing.plaf.metal.MetalIconFactory.getRadioButtonIcon(); }
    });
  }

  private static void optionIcons(UIDefaults t) {
    String[] names = {"errorIcon", "informationIcon", "warningIcon", "questionIcon"};
    for (int i = 0; i < names.length; i++) {
      final int kind = i;
      t.put("OptionPane." + names[i], new UIDefaults.LazyValue() {
        public Object createValue(UIDefaults table) { return new OptionIcons(kind); }
      });
    }
  }

  private static void fileIcon(UIDefaults t, String key, final int kind) {
    t.put(key, new UIDefaults.LazyValue() {
      public Object createValue(UIDefaults table) { return new FileIcons(kind); }
    });
  }

  private static void fileChooser(UIDefaults t) {
    fileIcon(t, "InternalFrame.icon", FileIcons.kFrame);
    fileIcon(t, "FileView.directoryIcon", FileIcons.kDirectory);
    fileIcon(t, "FileView.fileIcon", FileIcons.kFile);
    fileIcon(t, "FileView.computerIcon", FileIcons.kComputer);
    fileIcon(t, "FileView.hardDriveIcon", FileIcons.kHardDrive);
    fileIcon(t, "FileView.floppyDriveIcon", FileIcons.kFloppy);
    fileIcon(t, "FileChooser.homeFolderIcon", FileIcons.kHome);
    fileIcon(t, "FileChooser.newFolderIcon", FileIcons.kNewFolder);
    fileIcon(t, "FileChooser.upFolderIcon", FileIcons.kUpFolder);
    fileIcon(t, "FileChooser.listViewIcon", FileIcons.kList);
    fileIcon(t, "FileChooser.detailsViewIcon", FileIcons.kDetails);
    t.put("FileChooser.ancestorInputMap", new UIDefaults.LazyInputMap(new Object[] {"ESCAPE", "cancelSelection",
        "F2", "editFileName", "F5", "refresh", "BACK_SPACE", "Go Up", "ENTER", "approveSelection",
        "ctrl ENTER", "approveSelection"}));
  }

  private static void classes(UIDefaults t) {
    optionIcons(t);
    fileChooser(t);
    buttons(t);
    TextBindings.fill(t);
    menus(t);
    sliders(t);
    borders(t);
    tableAndTree(t);
    String basic = "javax.swing.plaf.basic.Basic";
    String metal = "javax.swing.plaf.metal.Metal";
    String[] pairs = {
        "LabelUI", metal + "LabelUI",
        "ScrollBarUI", metal + "ScrollBarUI",
        "ColorChooserUI", basic + "ColorChooserUI",
        "FileChooserUI", metal + "FileChooserUI",
        "ScrollPaneUI", metal + "ScrollPaneUI",
        "ViewportUI", basic + "ViewportUI",
        "TextFieldUI", metal + "TextFieldUI",
        "PasswordFieldUI", basic + "PasswordFieldUI",
        "FormattedTextFieldUI", basic + "FormattedTextFieldUI",
        "TextAreaUI", basic + "TextAreaUI",
        "TextPaneUI", basic + "TextPaneUI",
        "EditorPaneUI", basic + "EditorPaneUI",
        "InternalFrameUI", metal + "InternalFrameUI",
        "OptionPaneUI", basic + "OptionPaneUI",
        "RootPaneUI", metal + "RootPaneUI",
        "DesktopPaneUI", basic + "DesktopPaneUI",
        "DesktopIconUI", metal + "DesktopIconUI",
        "TabbedPaneUI", metal + "TabbedPaneUI",
        "PanelUI", basic + "PanelUI",
        "ButtonUI", metal + "ButtonUI",
        "ToggleButtonUI", metal + "ToggleButtonUI",
        "CheckBoxUI", metal + "CheckBoxUI",
        "RadioButtonUI", metal + "RadioButtonUI",
        "ListUI", basic + "ListUI",
        "TableUI", basic + "TableUI",
        "TableHeaderUI", basic + "TableHeaderUI",
        "TreeUI", metal + "TreeUI",
        "ComboBoxUI", metal + "ComboBoxUI",
        "ProgressBarUI", metal + "ProgressBarUI",
        "SliderUI", metal + "SliderUI",
        "SpinnerUI", basic + "SpinnerUI",
        "MenuItemUI", basic + "MenuItemUI",
        "MenuUI", basic + "MenuUI",
        "CheckBoxMenuItemUI", basic + "CheckBoxMenuItemUI",
        "RadioButtonMenuItemUI", basic + "RadioButtonMenuItemUI",
        "MenuBarUI", metal + "MenuBarUI",
        "PopupMenuUI", basic + "PopupMenuUI",
        "ToolBarUI", metal + "ToolBarUI",
        "ToolBarSeparatorUI", basic + "ToolBarSeparatorUI",
        "SplitPaneUI", metal + "SplitPaneUI",
        "PopupMenuSeparatorUI", metal + "PopupMenuSeparatorUI",
        "SeparatorUI", metal + "SeparatorUI",
        "ToolTipUI", metal + "ToolTipUI",
    };
    for (int i = 0; i < pairs.length; i += 2) {
      t.put(pairs[i], pairs[i + 1]);
    }
  }
}
