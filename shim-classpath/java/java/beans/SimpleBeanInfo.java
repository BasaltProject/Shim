/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package java.beans;

import java.awt.Image;

public class SimpleBeanInfo implements BeanInfo {
  @Override
  public BeanDescriptor getBeanDescriptor() {
    return null;
  }

  @Override
  public PropertyDescriptor[] getPropertyDescriptors() {
    return null;
  }

  @Override
  public int getDefaultPropertyIndex() {
    return -1;
  }

  @Override
  public EventSetDescriptor[] getEventSetDescriptors() {
    return null;
  }

  @Override
  public int getDefaultEventIndex() {
    return -1;
  }

  @Override
  public MethodDescriptor[] getMethodDescriptors() {
    return null;
  }

  @Override
  public BeanInfo[] getAdditionalBeanInfo() {
    return null;
  }

  @Override
  public Image getIcon(int iconKind) {
    return null;
  }

  public Image loadImage(final String resourceName) {
    return null;
  }
}
