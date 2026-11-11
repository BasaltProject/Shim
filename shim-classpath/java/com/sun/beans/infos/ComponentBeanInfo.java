/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package com.sun.beans.infos;

import java.awt.Component;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

public class ComponentBeanInfo extends SimpleBeanInfo {
  private static final String[] kNames = {"name", "background", "foreground", "font", "enabled", "visible", "focusable"};

  @Override
  public PropertyDescriptor[] getPropertyDescriptors() {
    try {
      PropertyDescriptor[] result = new PropertyDescriptor[kNames.length];
      for (int i = 0; i < kNames.length; i++) {
        PropertyDescriptor pd = new PropertyDescriptor(kNames[i], Component.class);
        pd.setBound(true);
        if (i >= 1 && i <= 3 || kNames[i].equals("visible")) {
          pd.setValue("transient", Boolean.TRUE);
        }
        if (kNames[i].equals("enabled")) {
          pd.setExpert(true);
        }
        if (kNames[i].equals("visible")) {
          pd.setHidden(true);
        }
        result[i] = pd;
      }
      return result;
    } catch (IntrospectionException e) {
      return null;
    }
  }
}
