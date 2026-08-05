/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */
package javax.swing.colorchooser;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;

class DefaultSwatchChooserPanel extends AbstractColorChooserPanel {
  private SwatchPanel mainSwatches;
  private RecentSwatchPanel recentSwatches;

  DefaultSwatchChooserPanel() {
    setInheritsPopupMenu(true);
  }

  public String getDisplayName() {
    return UIManager.getString("ColorChooser.swatchesNameText", getLocale());
  }

  public int getMnemonic() {
    return mnemonic("ColorChooser.swatchesMnemonic");
  }

  public int getDisplayedMnemonicIndex() {
    return mnemonic("ColorChooser.swatchesDisplayedMnemonicIndex");
  }

  private int mnemonic(String key) {
    Object v = UIManager.get(key, getLocale());
    return v instanceof Integer ? ((Integer) v).intValue() : -1;
  }

  public Icon getSmallDisplayIcon() {
    return null;
  }

  public Icon getLargeDisplayIcon() {
    return null;
  }

  public void installChooserPanel(javax.swing.JColorChooser enclosingChooser) {
    super.installChooserPanel(enclosingChooser);
  }

  protected void buildChooser() {
    String recentStr = UIManager.getString("ColorChooser.swatchesRecentText", getLocale());
    mainSwatches = new MainSwatchPanel();
    recentSwatches = new RecentSwatchPanel();
    MouseAdapter mainHandler = new MouseAdapter() {
      public void mousePressed(MouseEvent e) {
        Color c = mainSwatches.getColorForLocation(e.getX(), e.getY());
        if (c != null) {
          setSelectedColor(c);
          recentSwatches.setMostRecentColor(c);
        }
      }
    };
    mainSwatches.addMouseListener(mainHandler);
    recentSwatches.addMouseListener(new MouseAdapter() {
      public void mousePressed(MouseEvent e) {
        Color c = recentSwatches.getColorForLocation(e.getX(), e.getY());
        if (c != null) {
          setSelectedColor(c);
          recentSwatches.setMostRecentColor(c);
        }
      }
    });
    JPanel superHolder = new JPanel(new GridBagLayout());
    JPanel mainHolder = new JPanel(new BorderLayout());
    mainHolder.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.black),
        BorderFactory.createLineBorder(Color.white)));
    mainHolder.add(mainSwatches, BorderLayout.CENTER);
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.anchor = GridBagConstraints.LAST_LINE_START;
    gbc.gridheight = 2;
    gbc.insets = new Insets(0, 0, 0, 10);
    superHolder.add(mainHolder, gbc);
    JPanel recentHolder = new JPanel(new BorderLayout());
    recentHolder.setBorder(mainHolder.getBorder());
    recentHolder.add(recentSwatches, BorderLayout.CENTER);
    JLabel l = new JLabel(recentStr);
    l.setLabelFor(recentSwatches);
    gbc = new GridBagConstraints();
    gbc.anchor = GridBagConstraints.LINE_START;
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbc.insets = new Insets(7, 0, 0, 0);
    superHolder.add(l, gbc);
    gbc = new GridBagConstraints();
    gbc.anchor = GridBagConstraints.LINE_START;
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbc.insets = new Insets(0, 0, 0, 2);
    superHolder.add(recentHolder, gbc);
    add(superHolder);
  }

  public void uninstallChooserPanel(javax.swing.JColorChooser enclosingChooser) {
    super.uninstallChooserPanel(enclosingChooser);
    mainSwatches = null;
    recentSwatches = null;
  }

  public void updateChooser() { }
}
