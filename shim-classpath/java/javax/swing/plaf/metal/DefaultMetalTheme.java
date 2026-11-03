/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing.plaf.metal;

import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.FontUIResource;

public class DefaultMetalTheme extends MetalTheme {
  private static final ColorUIResource kPrimary1 = new ColorUIResource(102, 102, 153);
  private static final ColorUIResource kPrimary2 = new ColorUIResource(153, 153, 204);
  private static final ColorUIResource kPrimary3 = new ColorUIResource(204, 204, 255);
  private static final ColorUIResource kSecondary1 = new ColorUIResource(102, 102, 102);
  private static final ColorUIResource kSecondary2 = new ColorUIResource(153, 153, 153);
  private static final ColorUIResource kSecondary3 = new ColorUIResource(204, 204, 204);

  public DefaultMetalTheme() { }

  public String getName() { return "Steel"; }

  protected ColorUIResource getPrimary1() { return kPrimary1; }

  protected ColorUIResource getPrimary2() { return kPrimary2; }

  protected ColorUIResource getPrimary3() { return kPrimary3; }

  protected ColorUIResource getSecondary1() { return kSecondary1; }

  protected ColorUIResource getSecondary2() { return kSecondary2; }

  protected ColorUIResource getSecondary3() { return kSecondary3; }

  public FontUIResource getControlTextFont() { return new FontUIResource("Dialog", java.awt.Font.BOLD, 12); }

  public FontUIResource getSystemTextFont() { return new FontUIResource("Dialog", java.awt.Font.PLAIN, 12); }

  public FontUIResource getUserTextFont() { return new FontUIResource("Dialog", java.awt.Font.PLAIN, 12); }

  public FontUIResource getMenuTextFont() { return new FontUIResource("Dialog", java.awt.Font.BOLD, 12); }

  public FontUIResource getWindowTitleFont() { return new FontUIResource("Dialog", java.awt.Font.BOLD, 12); }

  public FontUIResource getSubTextFont() { return new FontUIResource("Dialog", java.awt.Font.PLAIN, 10); }
}
