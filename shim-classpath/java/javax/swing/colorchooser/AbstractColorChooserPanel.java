/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing.colorchooser;

import java.awt.Color;
import java.awt.Graphics;
import javax.swing.Icon;
import javax.swing.JColorChooser;
import javax.swing.JPanel;

public abstract class AbstractColorChooserPanel extends JPanel {
  public static final String TRANSPARENCY_ENABLED_PROPERTY = "TransparencyEnabled";

  private JColorChooser chooser;
  private boolean colorTransparencySelectionEnabled = true;

  protected AbstractColorChooserPanel() { }

  public abstract void updateChooser();

  protected abstract void buildChooser();

  public abstract String getDisplayName();

  public int getMnemonic() { return 0; }

  public int getDisplayedMnemonicIndex() { return -1; }

  public abstract Icon getSmallDisplayIcon();

  public abstract Icon getLargeDisplayIcon();

  public void installChooserPanel(JColorChooser enclosingChooser) {
    if (chooser != null) {
      throw new RuntimeException("This chooser panel is already installed");
    }
    chooser = enclosingChooser;
    chooser.addPropertyChangeListener(TRANSPARENCY_ENABLED_PROPERTY, new java.beans.PropertyChangeListener() {
      public void propertyChange(java.beans.PropertyChangeEvent evt) { updateChooser(); }
    });
    buildChooser();
    updateChooser();
  }

  public void uninstallChooserPanel(JColorChooser enclosingChooser) {
    removeAll();
    chooser = null;
  }

  public ColorSelectionModel getColorSelectionModel() {
    return (this.chooser != null) ? this.chooser.getSelectionModel() : null;
  }

  protected Color getColorFromModel() {
    ColorSelectionModel model = getColorSelectionModel();
    return (model != null) ? model.getSelectedColor() : null;
  }

  void setSelectedColor(Color color) {
    ColorSelectionModel model = getColorSelectionModel();
    if (model != null) {
      model.setSelectedColor(color);
    }
  }

  public void setColorTransparencySelectionEnabled(boolean b) {
    boolean old = colorTransparencySelectionEnabled;
    colorTransparencySelectionEnabled = b;
    firePropertyChange(TRANSPARENCY_ENABLED_PROPERTY, old, b);
  }

  public boolean isColorTransparencySelectionEnabled() { return colorTransparencySelectionEnabled; }

  public void paint(Graphics g) { super.paint(g); }
}
