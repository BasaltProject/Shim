/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing.plaf.metal;

public class MetalLookAndFeel extends javax.swing.plaf.basic.BasicLookAndFeel {
  private java.lang.String bp_Name;
  private java.lang.String bp_ID;
  private javax.swing.LayoutStyle bp_LayoutStyle;
  private boolean bp_NativeLookAndFeel;
  private boolean bp_SupportedLookAndFeel;
  private java.lang.String bp_Description;
  private boolean bp_SupportsWindowDecorations;
  private javax.swing.UIDefaults bp_Defaults;
  public MetalLookAndFeel() {
  }
  public java.lang.String getName() { return "Metal"; }
  public java.lang.String getID() { return "Metal"; }
  public static javax.swing.plaf.ColorUIResource getControl() { return currentTheme.getControl(); }
  public javax.swing.Icon getDisabledIcon(javax.swing.JComponent p0, javax.swing.Icon p1) { return super.getDisabledIcon(p0, p1); }
  public javax.swing.Icon getDisabledSelectedIcon(javax.swing.JComponent p0, javax.swing.Icon p1) { return super.getDisabledSelectedIcon(p0, p1); }
  public javax.swing.LayoutStyle getLayoutStyle() { return bp_LayoutStyle; }
  public boolean isNativeLookAndFeel() { return false; }
  public boolean isSupportedLookAndFeel() { return true; }
  public static javax.swing.plaf.ColorUIResource getFocusColor() { return currentTheme.getFocusColor(); }
  public java.lang.String getDescription() { return "The Java(tm) Look and Feel"; }
  public void provideErrorFeedback(java.awt.Component p0) { super.provideErrorFeedback(p0); }
  private static javax.swing.plaf.metal.MetalTheme currentTheme = new OceanTheme();
  public static javax.swing.plaf.metal.MetalTheme getCurrentTheme() { return currentTheme; }
  protected void createDefaultTheme() { if (currentTheme == null) { currentTheme = new OceanTheme(); } }
  public static void setCurrentTheme(javax.swing.plaf.metal.MetalTheme p0) {
    if (p0 == null) {
      throw new NullPointerException("Can't have null theme");
    }
    currentTheme = p0;
  }
  public boolean getSupportsWindowDecorations() { return bp_SupportsWindowDecorations; }
  protected void initClassDefaults(javax.swing.UIDefaults table) { super.initClassDefaults(table); }
  protected void initSystemColorDefaults(javax.swing.UIDefaults table) { super.initSystemColorDefaults(table); }
  protected void initComponentDefaults(javax.swing.UIDefaults table) { super.initComponentDefaults(table); }
  public static javax.swing.plaf.FontUIResource getControlTextFont() { return currentTheme.getControlTextFont(); }
  public static javax.swing.plaf.FontUIResource getSystemTextFont() { return currentTheme.getSystemTextFont(); }
  public static javax.swing.plaf.FontUIResource getUserTextFont() { return currentTheme.getUserTextFont(); }
  public static javax.swing.plaf.FontUIResource getMenuTextFont() { return currentTheme.getMenuTextFont(); }
  public static javax.swing.plaf.FontUIResource getWindowTitleFont() { return currentTheme.getWindowTitleFont(); }
  public static javax.swing.plaf.FontUIResource getSubTextFont() { return currentTheme.getSubTextFont(); }
  public static javax.swing.plaf.ColorUIResource getBlack() { return currentTheme.getBlack(); }
  public static javax.swing.plaf.ColorUIResource getWhite() { return currentTheme.getWhite(); }
  public static javax.swing.plaf.ColorUIResource getControlInfo() { return currentTheme.getControlInfo(); }
  public static javax.swing.plaf.ColorUIResource getControlDisabled() { return currentTheme.getControlDisabled(); }
  public static javax.swing.plaf.ColorUIResource getControlTextColor() { return currentTheme.getControlTextColor(); }
  public static javax.swing.plaf.ColorUIResource getDesktopColor() { return currentTheme.getDesktopColor(); }
  public static javax.swing.plaf.ColorUIResource getControlShadow() { return currentTheme.getControlShadow(); }
  public static javax.swing.plaf.ColorUIResource getControlDarkShadow() { return currentTheme.getControlDarkShadow(); }
  public static javax.swing.plaf.ColorUIResource getControlHighlight() { return currentTheme.getControlHighlight(); }
  public static javax.swing.plaf.ColorUIResource getPrimaryControl() { return currentTheme.getPrimaryControl(); }
  public static javax.swing.plaf.ColorUIResource getPrimaryControlShadow() { return currentTheme.getPrimaryControlShadow(); }
  public static javax.swing.plaf.ColorUIResource getPrimaryControlDarkShadow() { return currentTheme.getPrimaryControlDarkShadow(); }
  public static javax.swing.plaf.ColorUIResource getPrimaryControlInfo() { return currentTheme.getPrimaryControlInfo(); }
  public static javax.swing.plaf.ColorUIResource getPrimaryControlHighlight() { return currentTheme.getPrimaryControlHighlight(); }
  public static javax.swing.plaf.ColorUIResource getSystemTextColor() { return currentTheme.getSystemTextColor(); }
  public static javax.swing.plaf.ColorUIResource getInactiveControlTextColor() { return currentTheme.getInactiveControlTextColor(); }
  public static javax.swing.plaf.ColorUIResource getInactiveSystemTextColor() { return currentTheme.getInactiveSystemTextColor(); }
  public static javax.swing.plaf.ColorUIResource getUserTextColor() { return currentTheme.getUserTextColor(); }
  public static javax.swing.plaf.ColorUIResource getTextHighlightColor() { return currentTheme.getTextHighlightColor(); }
  public static javax.swing.plaf.ColorUIResource getHighlightedTextColor() { return currentTheme.getHighlightedTextColor(); }
  public static javax.swing.plaf.ColorUIResource getWindowBackground() { return currentTheme.getWindowBackground(); }
  public static javax.swing.plaf.ColorUIResource getWindowTitleBackground() { return currentTheme.getWindowTitleBackground(); }
  public static javax.swing.plaf.ColorUIResource getWindowTitleForeground() { return currentTheme.getWindowTitleForeground(); }
  public static javax.swing.plaf.ColorUIResource getWindowTitleInactiveBackground() { return currentTheme.getWindowTitleInactiveBackground(); }
  public static javax.swing.plaf.ColorUIResource getWindowTitleInactiveForeground() { return currentTheme.getWindowTitleInactiveForeground(); }
  public static javax.swing.plaf.ColorUIResource getMenuBackground() { return currentTheme.getMenuBackground(); }
  public static javax.swing.plaf.ColorUIResource getMenuForeground() { return currentTheme.getMenuForeground(); }
  public static javax.swing.plaf.ColorUIResource getMenuSelectedBackground() { return currentTheme.getMenuSelectedBackground(); }
  public static javax.swing.plaf.ColorUIResource getMenuSelectedForeground() { return currentTheme.getMenuSelectedForeground(); }
  public static javax.swing.plaf.ColorUIResource getMenuDisabledForeground() { return currentTheme.getMenuDisabledForeground(); }
  public static javax.swing.plaf.ColorUIResource getSeparatorBackground() { return currentTheme.getSeparatorBackground(); }
  public static javax.swing.plaf.ColorUIResource getSeparatorForeground() { return currentTheme.getSeparatorForeground(); }
  public static javax.swing.plaf.ColorUIResource getAcceleratorForeground() { return currentTheme.getAcceleratorForeground(); }
  public static javax.swing.plaf.ColorUIResource getAcceleratorSelectedForeground() { return currentTheme.getAcceleratorSelectedForeground(); }
  static boolean usingOcean() { return getCurrentTheme() instanceof OceanTheme; }
  public javax.swing.UIDefaults getDefaults() { return super.getDefaults(); }
}
