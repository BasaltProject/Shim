/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */
package javax.swing.plaf.basic;

import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.BorderFactory;
import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.LookAndFeel;
import javax.swing.UIManager;
import javax.swing.colorchooser.AbstractColorChooserPanel;
import javax.swing.colorchooser.ColorChooserComponentFactory;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.ColorChooserUI;
import javax.swing.plaf.ComponentUI;

public class BasicColorChooserUI extends ColorChooserUI {
  protected JColorChooser chooser;
  protected AbstractColorChooserPanel[] defaultChoosers;
  protected ChangeListener previewListener;
  protected PropertyChangeListener propertyChangeListener;
  private JTabbedPane tabs;
  private JPanel previewHolder;
  private AbstractColorChooserPanel[] installed = new AbstractColorChooserPanel[0];

  public BasicColorChooserUI() {
  }

  public static ComponentUI createUI(JComponent c) {
    return new BasicColorChooserUI();
  }

  public void installUI(JComponent c) {
    chooser = (JColorChooser) c;
    super.installUI(c);
    installDefaults();
    chooser.setLayout(new BorderLayout());
    tabs = new JTabbedPane();
    chooser.add(tabs, BorderLayout.CENTER);
    defaultChoosers = createDefaultChoosers();
    chooser.setChooserPanels(defaultChoosers);
    installPreviewPanel();
    installListeners();
    rebuildTabs();
  }

  public void uninstallUI(JComponent c) {
    chooser.remove(tabs);
    uninstallListeners();
    uninstallPreviewPanel();
    uninstallDefaultChoosers();
    uninstallDefaults();
    super.uninstallUI(c);
    chooser = null;
  }

  protected AbstractColorChooserPanel[] createDefaultChoosers() {
    return ColorChooserComponentFactory.getDefaultChooserPanels();
  }

  protected void uninstallDefaultChoosers() {
    for (AbstractColorChooserPanel p : installed) {
      p.uninstallChooserPanel(chooser);
    }
    installed = new AbstractColorChooserPanel[0];
  }

  protected void installPreviewPanel() {
    JComponent preview = chooser.getPreviewPanel();
    if (preview == null) {
      preview = ColorChooserComponentFactory.getPreviewPanel();
      chooser.setPreviewPanel(preview);
    }
    previewHolder = new JPanel(new CenterLayout());
    previewHolder.setBorder(BorderFactory.createTitledBorder(
        UIManager.getString("ColorChooser.previewText") == null ? "Preview"
            : UIManager.getString("ColorChooser.previewText")));
    previewHolder.add(preview, BorderLayout.CENTER);
    chooser.add(previewHolder, BorderLayout.SOUTH);
  }

  protected void uninstallPreviewPanel() {
    if (previewHolder != null) {
      chooser.remove(previewHolder);
      previewHolder = null;
    }
  }

  protected void installDefaults() {
    LookAndFeel.installColorsAndFont(chooser, "ColorChooser.background", "ColorChooser.foreground",
        "ColorChooser.font");
  }

  protected void uninstallDefaults() {
  }

  protected void installListeners() {
    propertyChangeListener = createPropertyChangeListener();
    chooser.addPropertyChangeListener(propertyChangeListener);
    previewListener = new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        for (AbstractColorChooserPanel p : installed) {
          p.updateChooser();
        }
        if (previewHolder != null) {
          previewHolder.repaint();
        }
      }
    };
    chooser.getSelectionModel().addChangeListener(previewListener);
  }

  protected PropertyChangeListener createPropertyChangeListener() {
    return new PropertyHandler();
  }

  protected void uninstallListeners() {
    chooser.removePropertyChangeListener(propertyChangeListener);
    chooser.getSelectionModel().removeChangeListener(previewListener);
    previewListener = null;
    propertyChangeListener = null;
  }

  private void rebuildTabs() {
    for (AbstractColorChooserPanel p : installed) {
      p.uninstallChooserPanel(chooser);
    }
    tabs.removeAll();
    installed = chooser.getChooserPanels();
    for (AbstractColorChooserPanel p : installed) {
      p.installChooserPanel(chooser);
      JPanel holder = new JPanel(new CenterLayout());
      holder.add(p);
      tabs.addTab(p.getDisplayName(), p.getSmallDisplayIcon(), holder);
      int index = tabs.getTabCount() - 1;
      int mnemonic = p.getMnemonic();
      if (mnemonic > 0) {
        tabs.setMnemonicAt(index, mnemonic);
        int displayed = p.getDisplayedMnemonicIndex();
        if (displayed >= 0) {
          tabs.setDisplayedMnemonicIndexAt(index, displayed);
        }
      }
    }
    chooser.revalidate();
  }

  public class PropertyHandler implements PropertyChangeListener {
    public PropertyHandler() {
    }

    public void propertyChange(PropertyChangeEvent e) {
      String name = e.getPropertyName();
      if (JColorChooser.CHOOSER_PANELS_PROPERTY.equals(name)) {
        rebuildTabs();
      } else if (JColorChooser.PREVIEW_PANEL_PROPERTY.equals(name)) {
        uninstallPreviewPanel();
        installPreviewPanel();
        chooser.revalidate();
      } else if (JColorChooser.SELECTION_MODEL_PROPERTY.equals(name)) {
        ((javax.swing.colorchooser.ColorSelectionModel) e.getOldValue()).removeChangeListener(previewListener);
        ((javax.swing.colorchooser.ColorSelectionModel) e.getNewValue()).addChangeListener(previewListener);
      }
    }
  }
}
