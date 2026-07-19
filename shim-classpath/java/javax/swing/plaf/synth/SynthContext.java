/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */
package javax.swing.plaf.synth;

import javax.swing.JComponent;

public class SynthContext {
  private JComponent component;
  private Region region;
  private SynthStyle style;
  private int state;

  public SynthContext(JComponent component, Region region, SynthStyle style, int state) {
    if (component == null || region == null || style == null) {
      throw new NullPointerException("You must supply a non-null component, region and style");
    }
    this.component = component;
    this.region = region;
    this.style = style;
    this.state = state;
  }

  public JComponent getComponent() {
    return component;
  }

  public Region getRegion() {
    return region;
  }

  void setComponentState(int state) {
    this.state = state;
  }

  public int getComponentState() {
    return state;
  }

  void setStyle(SynthStyle style) {
    this.style = style;
  }

  public SynthStyle getStyle() {
    return style;
  }
}
