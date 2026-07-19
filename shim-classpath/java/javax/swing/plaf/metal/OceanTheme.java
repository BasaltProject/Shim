/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing.plaf.metal;

import javax.swing.UIDefaults;
import javax.swing.plaf.ColorUIResource;

public class OceanTheme extends DefaultMetalTheme {
  private static final ColorUIResource kPrimary1 = new ColorUIResource(0x6382BF);
  private static final ColorUIResource kPrimary2 = new ColorUIResource(0xA3B8CC);
  private static final ColorUIResource kPrimary3 = new ColorUIResource(0xB8CFE5);
  private static final ColorUIResource kSecondary1 = new ColorUIResource(0x7A8A99);
  private static final ColorUIResource kSecondary2 = new ColorUIResource(0xB8CFE5);
  private static final ColorUIResource kSecondary3 = new ColorUIResource(0xEEEEEE);
  private static final ColorUIResource kControlText = new ColorUIResource(0x333333);
  private static final ColorUIResource kInactiveControlText = new ColorUIResource(0x999999);
  private static final ColorUIResource kMenuDisabled = new ColorUIResource(0x999999);

  public OceanTheme() { }

  public void addCustomEntriesToTable(UIDefaults table) { }

  public String getName() { return "Ocean"; }

  protected ColorUIResource getPrimary1() { return kPrimary1; }

  protected ColorUIResource getPrimary2() { return kPrimary2; }

  protected ColorUIResource getPrimary3() { return kPrimary3; }

  protected ColorUIResource getSecondary1() { return kSecondary1; }

  protected ColorUIResource getSecondary2() { return kSecondary2; }

  protected ColorUIResource getSecondary3() { return kSecondary3; }

  protected ColorUIResource getBlack() { return kControlText; }

  public ColorUIResource getDesktopColor() { return getWhite(); }

  public ColorUIResource getInactiveControlTextColor() { return kInactiveControlText; }

  public ColorUIResource getControlTextColor() { return kControlText; }

  public ColorUIResource getMenuDisabledForeground() { return kMenuDisabled; }
}
