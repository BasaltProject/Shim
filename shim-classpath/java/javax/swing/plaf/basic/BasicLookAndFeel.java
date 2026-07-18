/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing.plaf.basic;

public abstract class BasicLookAndFeel extends javax.swing.LookAndFeel implements java.io.Serializable {
  private javax.swing.UIDefaults bp_Defaults;
  protected BasicLookAndFeel() {
  }
  public void initialize() {
    
  }
  public void uninitialize() {
    
  }
  protected void playSound(javax.swing.Action audioAction) { }
  protected void initClassDefaults(javax.swing.UIDefaults table) { }
  protected void initSystemColorDefaults(javax.swing.UIDefaults table) {
    String[] defaultSystemColors = {"desktop", "#005C5C", "activeCaption", "#000080", "activeCaptionText", "#FFFFFF",
        "activeCaptionBorder", "#C0C0C0", "inactiveCaption", "#808080", "inactiveCaptionText", "#C0C0C0",
        "inactiveCaptionBorder", "#C0C0C0", "window", "#FFFFFF", "windowBorder", "#000000", "windowText", "#000000",
        "menu", "#C0C0C0", "menuText", "#000000", "text", "#C0C0C0", "textText", "#000000", "textHighlight", "#000080",
        "textHighlightText", "#FFFFFF", "textInactiveText", "#808080", "control", "#C0C0C0", "controlText", "#000000",
        "controlHighlight", "#C0C0C0", "controlLtHighlight", "#FFFFFF", "controlShadow", "#808080",
        "controlDkShadow", "#000000", "scrollbar", "#E0E0E0", "info", "#FFFFE1", "infoText", "#000000"};
    loadSystemColors(table, defaultSystemColors, false);
  }
  protected void initComponentDefaults(javax.swing.UIDefaults table) {
    fillDefaults(table);
  }
  protected javax.swing.Action createAudioAction(java.lang.Object key) {
    final String name = (String) key;
    return new javax.swing.AbstractAction(name) {
      public void actionPerformed(java.awt.event.ActionEvent e) { }
    };
  }
  protected javax.swing.ActionMap getAudioActionMap() {
    javax.swing.ActionMap audioActionMap = (javax.swing.ActionMap) javax.swing.UIManager.get("AuditoryCues.actionMap");
    if (audioActionMap == null) {
      audioActionMap = new javax.swing.ActionMap();
      javax.swing.UIManager.getLookAndFeelDefaults().put("AuditoryCues.actionMap", audioActionMap);
    }
    return audioActionMap;
  }
  protected void loadSystemColors(javax.swing.UIDefaults table, java.lang.String[] systemColors, boolean useNative) {
    for (int i = 0; i < systemColors.length; i += 2) {
      java.awt.Color color = java.awt.Color.black;
      try {
        color = java.awt.Color.decode(systemColors[i + 1]);
      } catch (NumberFormatException e) {
        e.printStackTrace();
      }
      table.put(systemColors[i], new javax.swing.plaf.ColorUIResource(color));
    }
  }
  public javax.swing.UIDefaults getDefaults() {
    if (defaults == null) {
      defaults = new javax.swing.UIDefaults();
      initClassDefaults(defaults);
      initSystemColorDefaults(defaults);
      initComponentDefaults(defaults);
    }
    return defaults;
  }

  private javax.swing.UIDefaults defaults;

  protected void fillDefaults(javax.swing.UIDefaults t) {
    shim.swing.MetalDefaults.fill(t);
    shim.swing.MetalKeymaps.fill(t);
    shim.swing.MetalExtras.fill(t);
  }
}
