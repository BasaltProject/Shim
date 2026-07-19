/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */
package javax.swing.plaf.nimbus;

import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.util.HashMap;
import java.util.Map;
import javax.swing.GrayFilter;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.synth.Region;
import javax.swing.plaf.synth.SynthLookAndFeel;
import javax.swing.plaf.synth.SynthStyle;
import javax.swing.plaf.synth.SynthStyleFactory;

public class NimbusLookAndFeel extends SynthLookAndFeel {
  private final Map<Region, String> prefixes = new HashMap<Region, String>();
  private final Map<String, NimbusStyle> styles = new HashMap<String, NimbusStyle>();
  private UIDefaults nimbusDefaults;
  private static volatile UIDefaults kOwn;

  static UIDefaults ownDefaults() {
    return kOwn;
  }

  public NimbusLookAndFeel() { }

  public void initialize() {
    super.initialize();
    setStyleFactory(new SynthStyleFactory() {
      public SynthStyle getStyle(JComponent c, Region r) {
        return NimbusLookAndFeel.getStyle(c, r);
      }
    });
  }

  public void uninitialize() {
    super.uninitialize();
    styles.clear();
  }

  public UIDefaults getDefaults() {
    if (nimbusDefaults == null) {
      nimbusDefaults = super.getDefaults();
      UIDefaults own = new UIDefaults();
      shim.swing.NimbusDefaults.fill(own);
      nimbusDefaults.putAll(own);
      kOwn = own;
    }
    return nimbusDefaults;
  }

  public static NimbusStyle getStyle(JComponent c, Region r) {
    String prefix = r.getName();
    return new NimbusStyle(prefix);
  }

  public void register(Region region, String prefix) {
    if (region == null || prefix == null) {
      throw new NullPointerException();
    }
    prefixes.put(region, prefix);
  }

  public Icon getDisabledIcon(JComponent component, Icon icon) {
    if (icon instanceof ImageIcon) {
      return new javax.swing.plaf.IconUIResource(
          new ImageIcon(GrayFilter.createDisabledImage(((ImageIcon) icon).getImage())));
    }
    return null;
  }

  public String getName() {
    return "Nimbus";
  }

  public String getID() {
    return "Nimbus";
  }

  public String getDescription() {
    return "Nimbus Look and Feel";
  }

  public boolean shouldUpdateStyleOnAncestorChanged() {
    return true;
  }

  protected boolean shouldUpdateStyleOnEvent(PropertyChangeEvent ev) {
    String eName = ev.getPropertyName();
    if ("Nimbus.Overrides" == eName || "Nimbus.Overrides.InheritDefaults" == eName || "JComponent.sizeVariant" == eName) {
      return true;
    }
    return super.shouldUpdateStyleOnEvent(ev);
  }

  public Color getDerivedColor(String uiDefaultParentName, float hOffset, float sOffset, float bOffset, int aOffset,
      boolean uiResource) {
    Color src = UIManager.getColor(uiDefaultParentName);
    float[] tmp = Color.RGBtoHSB(src.getRed(), src.getGreen(), src.getBlue(), null);
    tmp[0] = clamp(tmp[0] + hOffset);
    tmp[1] = clamp(tmp[1] + sOffset);
    tmp[2] = clamp(tmp[2] + bOffset);
    int alpha = Math.max(0, Math.min(255, src.getAlpha() + aOffset));
    Color c = new Color((Color.HSBtoRGB(tmp[0], tmp[1], tmp[2]) & 0xFFFFFF) | (alpha << 24), true);
    return uiResource ? new ColorUIResource(c) : c;
  }

  private static float clamp(float v) {
    return v < 0 ? 0 : v > 1 ? 1 : v;
  }

  protected final Color getDerivedColor(Color color1, Color color2, float midPoint) {
    return getDerivedColor(color1, color2, midPoint, true);
  }

  protected final Color getDerivedColor(Color color1, Color color2, float midPoint, boolean uiResource) {
    int argb = deriveARGB(color1, color2, midPoint);
    return uiResource ? new ColorUIResource(new Color(argb, true)) : new Color(argb, true);
  }

  static int deriveARGB(Color color1, Color color2, float midPoint) {
    int r = color1.getRed() + Math.round(midPoint * (color2.getRed() - color1.getRed()));
    int g = color1.getGreen() + Math.round(midPoint * (color2.getGreen() - color1.getGreen()));
    int b = color1.getBlue() + Math.round(midPoint * (color2.getBlue() - color1.getBlue()));
    int a = color1.getAlpha() + Math.round(midPoint * (color2.getAlpha() - color1.getAlpha()));
    return ((a & 0xFF) << 24) | ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | (b & 0xFF);
  }
}
