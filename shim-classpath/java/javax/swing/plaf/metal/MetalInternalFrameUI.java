/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */
package javax.swing.plaf.metal;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JComponent;
import javax.swing.JInternalFrame;
import javax.swing.border.Border;
import javax.swing.event.MouseInputAdapter;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicInternalFrameUI;

public class MetalInternalFrameUI extends BasicInternalFrameUI {
  protected static String IS_PALETTE = "JInternalFrame.isPalette";
  private static final String FRAME_TYPE = "JInternalFrame.frameType";
  private static final String PALETTE_FRAME = "palette";
  private static final String OPTION_DIALOG = "optionDialog";
  private PropertyChangeListener paletteListener;

  public MetalInternalFrameUI(JInternalFrame b) {
    super(b);
  }

  public static ComponentUI createUI(JComponent c) {
    return new MetalInternalFrameUI((JInternalFrame) c);
  }

  public void installUI(JComponent c) {
    super.installUI(c);
    Object paletteProp = c.getClientProperty(IS_PALETTE);
    if (paletteProp != null) {
      setPalette(((Boolean) paletteProp).booleanValue());
    }
  }

  public void uninstallUI(JComponent c) {
    super.uninstallUI(c);
  }

  protected void installKeyboardActions() {
    super.installKeyboardActions();
  }

  protected void uninstallKeyboardActions() {
    super.uninstallKeyboardActions();
  }

  protected void installListeners() {
    super.installListeners();
    paletteListener = new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent e) {
        if (IS_PALETTE.equals(e.getPropertyName()) && e.getNewValue() instanceof Boolean) {
          setPalette(((Boolean) e.getNewValue()).booleanValue());
        }
      }
    };
    frame.addPropertyChangeListener(paletteListener);
  }

  protected void uninstallListeners() {
    if (paletteListener != null) {
      frame.removePropertyChangeListener(paletteListener);
      paletteListener = null;
    }
    super.uninstallListeners();
  }

  protected void uninstallComponents() {
    titlePane = null;
    super.uninstallComponents();
  }

  protected JComponent createNorthPane(JInternalFrame w) {
    titlePane = new MetalInternalFrameTitlePane(w);
    return titlePane;
  }

  protected MouseInputAdapter createBorderListener(JInternalFrame w) {
    return super.createBorderListener(w);
  }

  public void setPalette(boolean isPalette) {
    if (isPalette) {
      frame.setBorder(javax.swing.UIManager.getBorder("InternalFrame.paletteBorder"));
    } else {
      frame.setBorder(javax.swing.UIManager.getBorder("InternalFrame.border"));
    }
    if (titlePane instanceof MetalInternalFrameTitlePane) {
      ((MetalInternalFrameTitlePane) titlePane).setPalette(isPalette);
    }
  }
}
