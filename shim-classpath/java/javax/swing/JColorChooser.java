/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing;

import java.awt.Color;
import java.awt.Component;
import java.awt.HeadlessException;
import java.awt.event.ActionListener;
import javax.accessibility.Accessible;
import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleRole;
import javax.swing.colorchooser.AbstractColorChooserPanel;
import javax.swing.colorchooser.ColorChooserComponentFactory;
import javax.swing.colorchooser.ColorSelectionModel;
import javax.swing.colorchooser.DefaultColorSelectionModel;
import javax.swing.plaf.ColorChooserUI;

public class JColorChooser extends JComponent implements Accessible {
  public static final String SELECTION_MODEL_PROPERTY = "selectionModel";
  public static final String PREVIEW_PANEL_PROPERTY = "previewPanel";
  public static final String CHOOSER_PANELS_PROPERTY = "chooserPanels";

  private ColorSelectionModel selectionModel;
  private JComponent previewPanel;
  private AbstractColorChooserPanel[] chooserPanels = new AbstractColorChooserPanel[0];
  private boolean dragEnabled;
  protected AccessibleContext accessibleContext = null;

  public static Color showDialog(Component component, String title, Color initialColor) throws HeadlessException {
    return showDialog(component, title, initialColor, true);
  }

  public static Color showDialog(Component component, String title, Color initialColor, boolean colorTransparencySelectionEnabled)
      throws HeadlessException {
    throw new HeadlessException();
  }

  public static JDialog createDialog(Component c, String title, boolean modal, JColorChooser chooserPane,
      ActionListener okListener, ActionListener cancelListener) throws HeadlessException {
    throw new HeadlessException();
  }

  public JColorChooser() { this(Color.white); }

  public JColorChooser(Color initialColor) { this(new DefaultColorSelectionModel(initialColor)); }

  public JColorChooser(ColorSelectionModel model) {
    selectionModel = model;
    updateUI();
    dragEnabled = false;
  }

  public ColorChooserUI getUI() { return (ColorChooserUI) ui; }

  public void setUI(ColorChooserUI ui) { super.setUI(ui); }

  public void updateUI() {
    setUI((ColorChooserUI) UIManager.getUI(this));
    if (chooserPanels.length == 0) {
      setChooserPanels(ColorChooserComponentFactory.getDefaultChooserPanels());
    }
    if (previewPanel == null) {
      setPreviewPanel(ColorChooserComponentFactory.getPreviewPanel());
    }
  }

  public String getUIClassID() { return "ColorChooserUI"; }

  public Color getColor() { return selectionModel.getSelectedColor(); }

  public void setColor(Color color) { selectionModel.setSelectedColor(color); }

  public void setColor(int r, int g, int b) { setColor(new Color(r, g, b)); }

  public void setColor(int c) { setColor((c >> 16) & 0xFF, (c >> 8) & 0xFF, c & 0xFF); }

  public void setDragEnabled(boolean b) {
    if (b && java.awt.GraphicsEnvironment.isHeadless()) {
      throw new HeadlessException();
    }
    dragEnabled = b;
  }

  public boolean getDragEnabled() { return dragEnabled; }

  public void setPreviewPanel(JComponent preview) {
    if (previewPanel != preview) {
      JComponent oldPreview = previewPanel;
      previewPanel = preview;
      firePropertyChange(JColorChooser.PREVIEW_PANEL_PROPERTY, oldPreview, preview);
    }
  }

  public JComponent getPreviewPanel() { return previewPanel; }

  public void addChooserPanel(AbstractColorChooserPanel panel) {
    AbstractColorChooserPanel[] oldPanels = getChooserPanels();
    AbstractColorChooserPanel[] newPanels = new AbstractColorChooserPanel[oldPanels.length + 1];
    System.arraycopy(oldPanels, 0, newPanels, 0, oldPanels.length);
    newPanels[newPanels.length - 1] = panel;
    setChooserPanels(newPanels);
  }

  public AbstractColorChooserPanel removeChooserPanel(AbstractColorChooserPanel panel) {
    int containedAt = -1;
    for (int i = 0; i < chooserPanels.length; i++) {
      if (chooserPanels[i] == panel) {
        containedAt = i;
        break;
      }
    }
    if (containedAt == -1) {
      throw new IllegalArgumentException("chooser panel not in this chooser");
    }
    AbstractColorChooserPanel[] newArray = new AbstractColorChooserPanel[chooserPanels.length - 1];
    if (containedAt == chooserPanels.length - 1) {
      System.arraycopy(chooserPanels, 0, newArray, 0, newArray.length);
    } else if (containedAt == 0) {
      System.arraycopy(chooserPanels, 1, newArray, 0, newArray.length);
    } else {
      System.arraycopy(chooserPanels, 0, newArray, 0, containedAt);
      System.arraycopy(chooserPanels, containedAt + 1, newArray, containedAt, chooserPanels.length - containedAt - 1);
    }
    setChooserPanels(newArray);
    return panel;
  }

  public void setChooserPanels(AbstractColorChooserPanel[] panels) {
    AbstractColorChooserPanel[] oldValue = chooserPanels;
    chooserPanels = panels;
    firePropertyChange(CHOOSER_PANELS_PROPERTY, oldValue, panels);
  }

  public AbstractColorChooserPanel[] getChooserPanels() { return chooserPanels; }

  public ColorSelectionModel getSelectionModel() { return selectionModel; }

  public void setSelectionModel(ColorSelectionModel newModel) {
    ColorSelectionModel oldModel = selectionModel;
    selectionModel = newModel;
    firePropertyChange(JColorChooser.SELECTION_MODEL_PROPERTY, oldModel, newModel);
  }

  protected String paramString() {
    StringBuilder chooserPanelsString = new StringBuilder();
    for (int i = 0; i < chooserPanels.length; i++) {
      chooserPanelsString.append("[").append(chooserPanels[i].toString()).append("]");
    }
    String previewPanelString = (previewPanel != null ? previewPanel.toString() : "");
    return super.paramString() + ",chooserPanels=" + chooserPanelsString.toString() + ",previewPanel="
        + previewPanelString;
  }

  public AccessibleContext getAccessibleContext() {
    if (accessibleContext == null) {
      accessibleContext = new AccessibleJColorChooser();
    }
    return accessibleContext;
  }

  protected class AccessibleJColorChooser extends AccessibleJComponent {
    protected AccessibleJColorChooser() { }

    public AccessibleRole getAccessibleRole() { return AccessibleRole.COLOR_CHOOSER; }
  }
}
