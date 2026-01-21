/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package java.beans;

import java.awt.Image;

public interface BeanInfo {
  BeanDescriptor getBeanDescriptor();

  EventSetDescriptor[] getEventSetDescriptors();

  int getDefaultEventIndex();

  PropertyDescriptor[] getPropertyDescriptors();

  int getDefaultPropertyIndex();

  MethodDescriptor[] getMethodDescriptors();

  BeanInfo[] getAdditionalBeanInfo();

  Image getIcon(int iconKind);

  int ICON_COLOR_16x16 = 1;

  int ICON_COLOR_32x32 = 2;

  int ICON_MONO_16x16 = 3;

  int ICON_MONO_32x32 = 4;
}
