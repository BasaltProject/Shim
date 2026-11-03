/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */
package javax.swing.colorchooser;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JColorChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

final class ColorChooserPanel extends AbstractColorChooserPanel {
  private final ChannelModel model;
  private final String prefix;
  private JSlider[] sliders;
  private JSpinner[] spinners;
  private JRadioButton[] radios;
  private JFormattedTextField hexField;
  private DiagramComponent plane;
  private DiagramComponent strip;
  private int[] values;
  private int selected;
  private boolean adjusting;

  ColorChooserPanel(ChannelModel model) {
    this.model = model;
    this.prefix = "ColorChooser." + model.key;
    setLayout(new GridBagLayout());
    values = model.fromColor(Color.white);
  }

  public String getDisplayName() {
    return UIManager.getString(prefix + "NameText", getLocale());
  }

  public int getMnemonic() {
    return number(prefix + "Mnemonic");
  }

  public int getDisplayedMnemonicIndex() {
    return number(prefix + "DisplayedMnemonicIndex");
  }

  private int number(String key) {
    Object v = UIManager.get(key, getLocale());
    return v instanceof Integer ? ((Integer) v).intValue() : -1;
  }

  public Icon getSmallDisplayIcon() {
    return null;
  }

  public Icon getLargeDisplayIcon() {
    return null;
  }

  int selectedChannel() {
    return selected;
  }

  int[] values() {
    return values.clone();
  }

  void setValues(int[] v) {
    values = v.clone();
    pushToModel();
  }

  private void pushToModel() {
    if (adjusting) {
      return;
    }
    adjusting = true;
    try {
      getColorSelectionModel().setSelectedColor(model.toColor(values));
    } finally {
      adjusting = false;
    }
    syncControls();
  }

  protected void buildChooser() {
    int rows = model.max.length;
    sliders = new JSlider[rows];
    spinners = new JSpinner[rows];
    radios = new JRadioButton[3];
    ColorPanel panel = new ColorPanel();
    ButtonGroup group = new ButtonGroup();
    javax.swing.JComponent[] labelCells = new javax.swing.JComponent[rows];
    GridBagConstraints[] labelCons = new GridBagConstraints[rows];
    GridBagConstraints[] sliderCons = new GridBagConstraints[rows];
    GridBagConstraints[] spinnerCons = new GridBagConstraints[rows];
    for (int i = 0; i < rows; i++) {
      final int row = i;
      String text = UIManager.getString(prefix + model.rowKeys[i], getLocale());
      javax.swing.JComponent label;
      if (i < 3) {
        JRadioButton rb = new JRadioButton(text);
        rb.setSelected(i == 0);
        rb.addChangeListener(new ChangeListener() {
          public void stateChanged(ChangeEvent e) {
            if (radios[row].isSelected() && selected != row) {
              selected = row;
              repaintDiagrams();
            }
          }
        });
        group.add(rb);
        radios[i] = rb;
        label = rb;
      } else {
        JLabel l = new JLabel(text);
        l.setBorder(BorderFactory.createEmptyBorder(4, 21, 4, 4));
        label = l;
      }
      JSlider slider = new JSlider(0, model.max[i], values[i]);
      slider.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent e) {
          userChange(row, sliders[row].getValue());
        }
      });
      JSpinner spinner = new JSpinner(new SpinnerNumberModel(values[i], 0, model.max[i], 1));
      spinner.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent e) {
          userChange(row, ((Number) spinners[row].getValue()).intValue());
        }
      });
      sliders[i] = slider;
      spinners[i] = spinner;
      GridBagConstraints gbc = new GridBagConstraints();
      gbc.gridy = i;
      gbc.fill = GridBagConstraints.HORIZONTAL;
      gbc.insets = new Insets(5, 0, 0, 0);
      gbc.gridx = 0;
      labelCells[i] = label;
      labelCons[i] = gbc;
      gbc = new GridBagConstraints();
      gbc.gridx = 1;
      gbc.gridy = i;
      gbc.fill = GridBagConstraints.HORIZONTAL;
      gbc.weightx = 1.0;
      gbc.insets = new Insets(i == 0 ? 0 : 5, 5, 0, 0);
      sliderCons[i] = gbc;
      gbc = new GridBagConstraints();
      gbc.gridx = 2;
      gbc.gridy = i;
      gbc.fill = GridBagConstraints.HORIZONTAL;
      gbc.insets = new Insets(i == 0 ? 0 : 5, 5, 0, 0);
      spinnerCons[i] = gbc;
    }
    for (int i = 0; i < rows; i++) {
      panel.add(labelCells[i], labelCons[i]);
    }
    for (int i = 0; i < rows; i++) {
      panel.add(sliders[i], sliderCons[i]);
    }
    for (int i = 0; i < rows; i++) {
      panel.add(spinners[i], spinnerCons[i]);
    }
    if (rows < 5) {
      JLabel hiddenLabel = new JLabel();
      hiddenLabel.setBorder(BorderFactory.createEmptyBorder(4, 21, 4, 4));
      hiddenLabel.setVisible(false);
      panel.add(hiddenLabel, new GridBagConstraints());
      JSlider hiddenSlider = new JSlider(0, 100, 50);
      hiddenSlider.setVisible(false);
      panel.add(hiddenSlider, new GridBagConstraints());
      JSpinner hiddenSpinner = new JSpinner();
      hiddenSpinner.setVisible(false);
      panel.add(hiddenSpinner, new GridBagConstraints());
    }
    plane = new DiagramComponent(model, this, true);
    strip = new DiagramComponent(model, this, false);
    GridBagConstraints gbc;
    gbc = new GridBagConstraints();
    gbc.gridx = 2;
    gbc.gridy = 0;
    gbc.gridwidth = 2;
    gbc.weighty = 1.0;
    gbc.anchor = GridBagConstraints.NORTH;
    gbc.insets = new Insets(10, 0, 0, 10);
    add(panel, gbc);
    {
      JLabel code = new JLabel(model.hasHex() ? UIManager.getString("ColorChooser.rgbHexCodeText", getLocale()) : null);
      code.setVisible(model.hasHex());
      gbc = new GridBagConstraints();
      gbc.gridx = 2;
      gbc.gridy = 1;
      gbc.weightx = 1.0;
      gbc.insets = new Insets(10, 0, 10, 5);
      gbc.fill = GridBagConstraints.HORIZONTAL;
      add(code, gbc);
      hexField = new JFormattedTextField("FFFFFF");
      hexField.setColumns(6);
      hexField.setVisible(model.hasHex());
      code.setLabelFor(hexField);
      hexField.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent e) {
          commitHex();
        }
      });
      hexField.addFocusListener(new FocusAdapter() {
        public void focusLost(FocusEvent e) {
          commitHex();
        }
      });
      gbc = new GridBagConstraints();
      gbc.gridx = 3;
      gbc.gridy = 1;
      gbc.insets = new Insets(10, 0, 10, 10);
      gbc.fill = GridBagConstraints.HORIZONTAL;
      add(hexField, gbc);
    }
    gbc = new GridBagConstraints();
    gbc.gridx = 1;
    gbc.gridy = 0;
    gbc.gridheight = 2;
    gbc.anchor = GridBagConstraints.NORTH;
    gbc.insets = new Insets(10, 0, 10, 10);
    add(strip, gbc);
    gbc = new GridBagConstraints();
    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.gridheight = 2;
    gbc.anchor = GridBagConstraints.NORTH;
    gbc.insets = new Insets(10, 10, 10, 10);
    add(plane, gbc);
  }

  private void commitHex() {
    String t = hexField.getText().trim();
    try {
      int rgb = Integer.parseInt(t, 16) & 0xFFFFFF;
      Color old = getColorSelectionModel().getSelectedColor();
      int a = old == null ? 255 : old.getAlpha();
      getColorSelectionModel().setSelectedColor(new Color((a << 24) | rgb, true));
    } catch (NumberFormatException e) {
      syncControls();
    }
  }

  private void userChange(int row, int value) {
    if (adjusting || values[row] == value) {
      return;
    }
    values[row] = value;
    pushToModel();
  }

  private void repaintDiagrams() {
    if (plane != null) {
      plane.repaint();
      strip.repaint();
    }
  }

  private void syncControls() {
    boolean was = adjusting;
    adjusting = true;
    try {
      for (int i = 0; i < sliders.length; i++) {
        sliders[i].setValue(values[i]);
        spinners[i].setValue(Integer.valueOf(values[i]));
      }
      if (hexField != null) {
        Color c = getColorFromModel();
        hexField.setText(String.format("%06X", c.getRGB() & 0xFFFFFF));
      }
    } finally {
      adjusting = was;
    }
    repaintDiagrams();
  }

  public void updateChooser() {
    if (adjusting || sliders == null) {
      return;
    }
    Color c = getColorFromModel();
    if (c == null) {
      return;
    }
    values = model.fromColor(c);
    syncControls();
  }

  public void uninstallChooserPanel(JColorChooser enclosingChooser) {
    sliders = null;
    spinners = null;
    radios = null;
    hexField = null;
    plane = null;
    strip = null;
    super.uninstallChooserPanel(enclosingChooser);
  }
}
