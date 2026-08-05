/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing.colorchooser;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JPanel;

public class ColorChooserComponentFactory {
  ColorChooserComponentFactory() { }

  public static AbstractColorChooserPanel[] getDefaultChooserPanels() {
    return new AbstractColorChooserPanel[] {new DefaultSwatchChooserPanel(), new ColorChooserPanel(ChannelModel.kHsv),
        new ColorChooserPanel(ChannelModel.kHsl), new ColorChooserPanel(ChannelModel.kRgb),
        new ColorChooserPanel(ChannelModel.kCmyk)};
  }

  public static JComponent getPreviewPanel() { return new DefaultPreviewPanel(); }
}
