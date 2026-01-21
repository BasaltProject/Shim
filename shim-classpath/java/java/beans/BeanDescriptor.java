/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package java.beans;

public class BeanDescriptor extends FeatureDescriptor {
  private final Class<?> beanClass;
  private final Class<?> customizerClass;

  public BeanDescriptor(Class<?> beanClass) {
    this(beanClass, null);
  }

  public BeanDescriptor(Class<?> beanClass, Class<?> customizerClass) {
    this.beanClass = beanClass;
    this.customizerClass = customizerClass;
    String name = beanClass.getName();
    while (name.indexOf('.') >= 0) {
      name = name.substring(name.indexOf('.') + 1);
    }
    setName(name);
  }

  BeanDescriptor(BeanDescriptor old) {
    super(old);
    beanClass = old.beanClass;
    customizerClass = old.customizerClass;
  }

  public Class<?> getBeanClass() {
    return beanClass;
  }

  public Class<?> getCustomizerClass() {
    return customizerClass;
  }

  @Override
  void appendTo(StringBuilder sb) {
    appendTo(sb, "beanClass", beanClass);
    appendTo(sb, "customizerClass", customizerClass);
  }
}
