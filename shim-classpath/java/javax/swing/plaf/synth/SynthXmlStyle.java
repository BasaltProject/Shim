/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */
package javax.swing.plaf.synth;

import java.awt.Color;
import java.awt.Font;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.UIManager;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.FontUIResource;

final class SynthXmlStyle extends SynthStyle {
  static final class StateData {
    final int state;
    Font font;
    final Color[] colors = new Color[ColorType.MAX_COUNT];

    StateData(int state) {
      this.state = state;
    }
  }

  final List<StateData> states = new ArrayList<StateData>();
  final Map<Object, Object> properties = new HashMap<Object, Object>();
  Insets insets;
  Boolean opaque;
  SynthPainter painter;
  SynthGraphicsUtils graphicsUtils;

  StateData stateData(int state) {
    for (StateData d : states) {
      if (d.state == state) {
        return d;
      }
    }
    StateData d = new StateData(state);
    states.add(d);
    return d;
  }

  void mergeFrom(SynthXmlStyle other) {
    for (StateData od : other.states) {
      StateData d = stateData(od.state);
      if (od.font != null) {
        d.font = od.font;
      }
      for (int i = 0; i < od.colors.length; i++) {
        if (od.colors[i] != null) {
          d.colors[i] = od.colors[i];
        }
      }
    }
    properties.putAll(other.properties);
    if (other.insets != null) {
      insets = (Insets) other.insets.clone();
    }
    if (other.opaque != null) {
      opaque = other.opaque;
    }
    if (other.painter != null) {
      painter = other.painter;
    }
    if (other.graphicsUtils != null) {
      graphicsUtils = other.graphicsUtils;
    }
  }

  private List<StateData> matching(int componentState) {
    List<StateData> result = new ArrayList<StateData>();
    for (StateData d : states) {
      if ((d.state & componentState) == d.state) {
        result.add(d);
      }
    }
    java.util.Collections.sort(result, new java.util.Comparator<StateData>() {
      public int compare(StateData a, StateData b) {
        return Integer.bitCount(b.state) - Integer.bitCount(a.state);
      }
    });
    return result;
  }

  protected Color getColorForState(SynthContext ctx, ColorType type) {
    for (StateData d : matching(ctx.getComponentState())) {
      if (d.colors[type.getID()] != null) {
        return d.colors[type.getID()];
      }
    }
    String key = type == ColorType.BACKGROUND ? "control" : type == ColorType.TEXT_BACKGROUND ? "text"
        : type == ColorType.FOCUS ? "controlDkShadow" : "controlText";
    Color c = UIManager.getColor(key);
    return c == null ? new ColorUIResource(Color.black) : c;
  }

  protected Font getFontForState(SynthContext ctx) {
    for (StateData d : matching(ctx.getComponentState())) {
      if (d.font != null) {
        return d.font;
      }
    }
    return new FontUIResource("Dialog", Font.PLAIN, 12);
  }

  public Insets getInsets(SynthContext ctx, Insets modified) {
    if (modified == null) {
      modified = new Insets(0, 0, 0, 0);
    }
    if (insets != null) {
      modified.set(insets.top, insets.left, insets.bottom, insets.right);
    } else {
      modified.set(0, 0, 0, 0);
    }
    return modified;
  }

  public Object get(SynthContext ctx, Object key) {
    return properties.get(key);
  }

  public boolean isOpaque(SynthContext ctx) {
    return opaque != null && opaque.booleanValue();
  }

  public SynthPainter getPainter(SynthContext ctx) {
    return painter;
  }

  public SynthGraphicsUtils getGraphicsUtils(SynthContext ctx) {
    return graphicsUtils != null ? graphicsUtils : super.getGraphicsUtils(ctx);
  }
}
