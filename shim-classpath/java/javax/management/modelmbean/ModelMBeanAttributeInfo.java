/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.management.modelmbean;

import javax.management.Descriptor;
import javax.management.DescriptorAccess;
import javax.management.MBeanParameterInfo;
import javax.management.RuntimeOperationsException;
import javax.management.MBeanAttributeInfo;

public class ModelMBeanAttributeInfo extends MBeanAttributeInfo implements DescriptorAccess {
  private static final long serialVersionUID = 6181543027787327345L;

  private Descriptor descriptor;

  public ModelMBeanAttributeInfo(String name, String description, java.lang.reflect.Method getter,
      java.lang.reflect.Method setter) throws javax.management.IntrospectionException {
    super(name, description, getter, setter);
    descriptor = ModelDescriptors.valid(null, name, "attribute", null);
  }

  public ModelMBeanAttributeInfo(String name, String description, java.lang.reflect.Method getter,
      java.lang.reflect.Method setter, Descriptor descriptor) throws javax.management.IntrospectionException {
    super(name, description, getter, setter);
    this.descriptor = ModelDescriptors.valid(descriptor, name, "attribute", null);
  }

  public ModelMBeanAttributeInfo(String name, String type, String description, boolean isReadable,
      boolean isWritable, boolean isIs) {
    super(name, type, description, isReadable, isWritable, isIs);
    descriptor = ModelDescriptors.valid(null, name, "attribute", null);
  }

  public ModelMBeanAttributeInfo(String name, String type, String description, boolean isReadable,
      boolean isWritable, boolean isIs, Descriptor descriptor) {
    super(name, type, description, isReadable, isWritable, isIs);
    this.descriptor = ModelDescriptors.valid(descriptor, name, "attribute", null);
  }

  public ModelMBeanAttributeInfo(ModelMBeanAttributeInfo inInfo) {
    super(inInfo.getName(), inInfo.getType(), inInfo.getDescription(), inInfo.isReadable(), inInfo.isWritable(),
        inInfo.isIs());
    descriptor = inInfo.getDescriptor();
  }

  public Descriptor getDescriptor() {
    return (Descriptor) (descriptor == null ? null : descriptor.clone());
  }

  public void setDescriptor(Descriptor inDescriptor) {
    descriptor = ModelDescriptors.valid(inDescriptor, getName(), "attribute", null);
  }

  public Object clone() {
    return new ModelMBeanAttributeInfo(this);
  }
}
