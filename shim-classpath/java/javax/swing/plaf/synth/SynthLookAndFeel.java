/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */
package javax.swing.plaf.synth;

import java.awt.Component;
import java.awt.Container;
import java.awt.Window;
import java.beans.PropertyChangeEvent;
import java.io.InputStream;
import java.text.ParseException;
import javax.swing.JComponent;
import javax.swing.UIDefaults;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicLookAndFeel;

public class SynthLookAndFeel extends BasicLookAndFeel {
  private static SynthStyleFactory styleFactory;
  private static final SynthXmlStyle kDefaultStyle = new SynthXmlStyle();
  private SynthStyleFactory loaded;
  private UIDefaults synthDefaults;

  public SynthLookAndFeel() { }

  public static void setStyleFactory(SynthStyleFactory cache) {
    styleFactory = cache;
  }

  public static SynthStyleFactory getStyleFactory() {
    if (styleFactory == null) {
      styleFactory = new SynthStyleFactory() {
        public SynthStyle getStyle(JComponent c, Region id) {
          return kDefaultStyle;
        }
      };
    }
    return styleFactory;
  }

  public static SynthStyle getStyle(JComponent c, Region region) {
    return getStyleFactory().getStyle(c, region);
  }

  public static Region getRegion(JComponent c) {
    return Region.getRegion(c);
  }

  public static void updateStyles(Component c) {
    if (c instanceof JComponent) {
      String name = c.getName();
      c.setName(null);
      if (name != null) {
        c.setName(name);
      }
      ((JComponent) c).revalidate();
    }
    Component[] children = null;
    if (c instanceof Container) {
      children = ((Container) c).getComponents();
    }
    if (children != null) {
      for (Component child : children) {
        updateStyles(child);
      }
    }
  }

  public static ComponentUI createUI(JComponent c) {
    String key = c.getUIClassID();
    try {
      Class<?> k = Class.forName("javax.swing.plaf.synth.Synth" + key);
      return (ComponentUI) k.getMethod("createUI", JComponent.class).invoke(null, c);
    } catch (Exception e) {
      return null;
    }
  }

  public void load(InputStream input, Class<?> resourceBase) throws ParseException {
    if (resourceBase == null) {
      throw new IllegalArgumentException("You must supply a valid resource base Class");
    }
    SynthXmlParser parser = new SynthXmlParser(resourceBase, kDefaultStyle);
    parser.parse(input);
    loaded = parser;
    synthDefaults = null;
    for (java.util.Map.Entry<String, Object> e : parser.defaultsProperties().entrySet()) {
      getDefaults().put(e.getKey(), e.getValue());
    }
  }

  public void initialize() {
    super.initialize();
    if (loaded != null) {
      setStyleFactory(loaded);
    } else if (styleFactory == null) {
      getStyleFactory();
    }
    try {
      for (Window w : Window.getWindows()) {
        updateStyles(w);
      }
    } catch (UnsupportedOperationException ignored) {
      return;
    }
  }

  public void uninitialize() {
    super.uninitialize();
    setStyleFactory(null);
  }

  public UIDefaults getDefaults() {
    if (synthDefaults == null) {
      synthDefaults = super.getDefaults();
      String prefix = "javax.swing.plaf.synth.";
      Object[] uis = {
        "ButtonUI", prefix + "SynthButtonUI",
        "CheckBoxMenuItemUI", prefix + "SynthCheckBoxMenuItemUI",
        "CheckBoxUI", prefix + "SynthCheckBoxUI",
        "ColorChooserUI", prefix + "SynthColorChooserUI",
        "ComboBoxUI", prefix + "SynthComboBoxUI",
        "DesktopIconUI", prefix + "SynthDesktopIconUI",
        "DesktopPaneUI", prefix + "SynthDesktopPaneUI",
        "EditorPaneUI", prefix + "SynthEditorPaneUI",
        "FormattedTextFieldUI", prefix + "SynthFormattedTextFieldUI",
        "InternalFrameUI", prefix + "SynthInternalFrameUI",
        "LabelUI", prefix + "SynthLabelUI",
        "ListUI", prefix + "SynthListUI",
        "MenuBarUI", prefix + "SynthMenuBarUI",
        "MenuItemUI", prefix + "SynthMenuItemUI",
        "MenuUI", prefix + "SynthMenuUI",
        "OptionPaneUI", prefix + "SynthOptionPaneUI",
        "PanelUI", prefix + "SynthPanelUI",
        "PasswordFieldUI", prefix + "SynthPasswordFieldUI",
        "PopupMenuUI", prefix + "SynthPopupMenuUI",
        "ProgressBarUI", prefix + "SynthProgressBarUI",
        "RadioButtonMenuItemUI", prefix + "SynthRadioButtonMenuItemUI",
        "RadioButtonUI", prefix + "SynthRadioButtonUI",
        "RootPaneUI", prefix + "SynthRootPaneUI",
        "ScrollBarUI", prefix + "SynthScrollBarUI",
        "ScrollPaneUI", prefix + "SynthScrollPaneUI",
        "SeparatorUI", prefix + "SynthSeparatorUI",
        "SliderUI", prefix + "SynthSliderUI",
        "SpinnerUI", prefix + "SynthSpinnerUI",
        "SplitPaneUI", prefix + "SynthSplitPaneUI",
        "TabbedPaneUI", prefix + "SynthTabbedPaneUI",
        "TableHeaderUI", prefix + "SynthTableHeaderUI",
        "TableUI", prefix + "SynthTableUI",
        "TextAreaUI", prefix + "SynthTextAreaUI",
        "TextFieldUI", prefix + "SynthTextFieldUI",
        "TextPaneUI", prefix + "SynthTextPaneUI",
        "ToggleButtonUI", prefix + "SynthToggleButtonUI",
        "ToolBarUI", prefix + "SynthToolBarUI",
        "ToolTipUI", prefix + "SynthToolTipUI",
        "TreeUI", prefix + "SynthTreeUI",
        "ViewportUI", prefix + "SynthViewportUI",
        "PopupMenuSeparatorUI", prefix + "SynthSeparatorUI",
        "ToolBarSeparatorUI", prefix + "SynthSeparatorUI",
        "FileChooserUI", "javax.swing.plaf.metal.MetalFileChooserUI"
      };
      synthDefaults.putDefaults(uis);
    }
    return synthDefaults;
  }

  public boolean isSupportedLookAndFeel() {
    return true;
  }

  public boolean isNativeLookAndFeel() {
    return false;
  }

  public String getDescription() {
    return "Synth look and feel";
  }

  public String getName() {
    return "Synth look and feel";
  }

  public String getID() {
    return "Synth";
  }

  public boolean shouldUpdateStyleOnAncestorChanged() {
    return false;
  }

  protected boolean shouldUpdateStyleOnEvent(PropertyChangeEvent ev) {
    String eName = ev.getPropertyName();
    if ("name" == eName || "componentOrientation" == eName) {
      return true;
    }
    if ("ancestor" == eName && ev.getNewValue() != null) {
      return shouldUpdateStyleOnAncestorChanged();
    }
    return false;
  }
}
