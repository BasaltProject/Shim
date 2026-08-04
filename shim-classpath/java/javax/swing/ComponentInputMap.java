/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing;

public class ComponentInputMap extends InputMap {
  private final JComponent component;

  public ComponentInputMap(JComponent component) {
    if (component == null) {
      throw new IllegalArgumentException("ComponentInputMaps must be associated with a non-null JComponent");
    }
    this.component = component;
  }

  public void setParent(InputMap map) {
    if (getParent() == map) {
      return;
    }
    if (map != null && (!(map instanceof ComponentInputMap) || ((ComponentInputMap) map).getComponent() != getComponent())) {
      throw new IllegalArgumentException("ComponentInputMap.setParent requires a ComponentInputMap with the same component");
    }
    super.setParent(map);
    component.componentInputMapChanged(this);
  }

  public JComponent getComponent() { return component; }

  public void put(KeyStroke keyStroke, Object actionMapKey) {
    super.put(keyStroke, actionMapKey);
    component.componentInputMapChanged(this);
  }

  public void remove(KeyStroke key) {
    super.remove(key);
    component.componentInputMapChanged(this);
  }

  public void clear() {
    int oldSize = size();
    super.clear();
    if (oldSize > 0) {
      component.componentInputMapChanged(this);
    }
  }
}
