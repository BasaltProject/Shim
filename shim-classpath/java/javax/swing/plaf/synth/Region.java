/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */
package javax.swing.plaf.synth;

import java.util.HashMap;
import java.util.Map;
import javax.swing.UIDefaults;

public class Region {
  private static final Map<String, Region> uiToRegionMap = new HashMap<String, Region>();
  private static final Map<String, Region> lowerCaseNameMap = new HashMap<String, Region>();

  public static final Region ARROW_BUTTON = new Region("ArrowButton", false);
  public static final Region BUTTON = new Region("Button", "ButtonUI", false);
  public static final Region CHECK_BOX = new Region("CheckBox", "CheckBoxUI", false);
  public static final Region CHECK_BOX_MENU_ITEM = new Region("CheckBoxMenuItem", "CheckBoxMenuItemUI", false);
  public static final Region COLOR_CHOOSER = new Region("ColorChooser", "ColorChooserUI", false);
  public static final Region COMBO_BOX = new Region("ComboBox", "ComboBoxUI", false);
  public static final Region DESKTOP_PANE = new Region("DesktopPane", "DesktopPaneUI", false);
  public static final Region DESKTOP_ICON = new Region("DesktopIcon", "DesktopIconUI", false);
  public static final Region EDITOR_PANE = new Region("EditorPane", "EditorPaneUI", false);
  public static final Region FILE_CHOOSER = new Region("FileChooser", "FileChooserUI", false);
  public static final Region FORMATTED_TEXT_FIELD = new Region("FormattedTextField", "FormattedTextFieldUI", false);
  public static final Region INTERNAL_FRAME = new Region("InternalFrame", "InternalFrameUI", false);
  public static final Region INTERNAL_FRAME_TITLE_PANE = new Region("InternalFrameTitlePane", "InternalFrameTitlePaneUI", false);
  public static final Region LABEL = new Region("Label", "LabelUI", false);
  public static final Region LIST = new Region("List", "ListUI", false);
  public static final Region MENU = new Region("Menu", "MenuUI", false);
  public static final Region MENU_BAR = new Region("MenuBar", "MenuBarUI", false);
  public static final Region MENU_ITEM = new Region("MenuItem", "MenuItemUI", false);
  public static final Region MENU_ITEM_ACCELERATOR = new Region("MenuItemAccelerator", true);
  public static final Region OPTION_PANE = new Region("OptionPane", "OptionPaneUI", false);
  public static final Region PANEL = new Region("Panel", "PanelUI", false);
  public static final Region PASSWORD_FIELD = new Region("PasswordField", "PasswordFieldUI", false);
  public static final Region POPUP_MENU = new Region("PopupMenu", "PopupMenuUI", false);
  public static final Region POPUP_MENU_SEPARATOR = new Region("PopupMenuSeparator", "PopupMenuSeparatorUI", false);
  public static final Region PROGRESS_BAR = new Region("ProgressBar", "ProgressBarUI", false);
  public static final Region RADIO_BUTTON = new Region("RadioButton", "RadioButtonUI", false);
  public static final Region RADIO_BUTTON_MENU_ITEM = new Region("RadioButtonMenuItem", "RadioButtonMenuItemUI", false);
  public static final Region ROOT_PANE = new Region("RootPane", "RootPaneUI", false);
  public static final Region SCROLL_BAR = new Region("ScrollBar", "ScrollBarUI", false);
  public static final Region SCROLL_BAR_TRACK = new Region("ScrollBarTrack", true);
  public static final Region SCROLL_BAR_THUMB = new Region("ScrollBarThumb", true);
  public static final Region SCROLL_PANE = new Region("ScrollPane", "ScrollPaneUI", false);
  public static final Region SEPARATOR = new Region("Separator", "SeparatorUI", false);
  public static final Region SLIDER = new Region("Slider", "SliderUI", false);
  public static final Region SLIDER_TRACK = new Region("SliderTrack", true);
  public static final Region SLIDER_THUMB = new Region("SliderThumb", true);
  public static final Region SPINNER = new Region("Spinner", "SpinnerUI", false);
  public static final Region SPLIT_PANE = new Region("SplitPane", "SplitPaneUI", false);
  public static final Region SPLIT_PANE_DIVIDER = new Region("SplitPaneDivider", true);
  public static final Region TABBED_PANE = new Region("TabbedPane", "TabbedPaneUI", false);
  public static final Region TABBED_PANE_TAB = new Region("TabbedPaneTab", true);
  public static final Region TABBED_PANE_TAB_AREA = new Region("TabbedPaneTabArea", true);
  public static final Region TABBED_PANE_CONTENT = new Region("TabbedPaneContent", true);
  public static final Region TABLE = new Region("Table", "TableUI", false);
  public static final Region TABLE_HEADER = new Region("TableHeader", "TableHeaderUI", false);
  public static final Region TEXT_AREA = new Region("TextArea", "TextAreaUI", false);
  public static final Region TEXT_FIELD = new Region("TextField", "TextFieldUI", false);
  public static final Region TEXT_PANE = new Region("TextPane", "TextPaneUI", false);
  public static final Region TOGGLE_BUTTON = new Region("ToggleButton", "ToggleButtonUI", false);
  public static final Region TOOL_BAR = new Region("ToolBar", "ToolBarUI", false);
  public static final Region TOOL_BAR_CONTENT = new Region("ToolBarContent", true);
  public static final Region TOOL_BAR_DRAG_WINDOW = new Region("ToolBarDragWindow", false);
  public static final Region TOOL_TIP = new Region("ToolTip", "ToolTipUI", false);
  public static final Region TOOL_BAR_SEPARATOR = new Region("ToolBarSeparator", "ToolBarSeparatorUI", false);
  public static final Region TREE = new Region("Tree", "TreeUI", false);
  public static final Region TREE_CELL = new Region("TreeCell", true);
  public static final Region VIEWPORT = new Region("Viewport", "ViewportUI", false);

  private final String name;
  private final boolean subregion;

  static Region getRegion(javax.swing.JComponent c) {
    return uiToRegionMap.get(c.getUIClassID());
  }

  static void registerUI(String uiKey, Region region) {
    uiToRegionMap.put(uiKey, region);
  }

  Region(String name, boolean subregion) {
    this(name, null, subregion);
  }

  protected Region(String name, String ui, boolean subregion) {
    if (name == null) {
      throw new NullPointerException("You must specify a non-null name");
    }
    this.name = name;
    if (ui != null) {
      uiToRegionMap.put(ui, this);
    }
    this.subregion = subregion;
    lowerCaseNameMap.put(name, this);
  }

  public boolean isSubregion() {
    return subregion;
  }

  public String getName() {
    return name;
  }

  String getLowerCaseName() {
    return name.toLowerCase();
  }

  public String toString() {
    return name;
  }
}
