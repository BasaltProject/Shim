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
import javax.management.MBeanConstructorInfo;

public class ModelMBeanConstructorInfo extends MBeanConstructorInfo implements DescriptorAccess {
  private static final long serialVersionUID = 3862947819818064362L;

  private Descriptor descriptor;

  public ModelMBeanConstructorInfo(String description, java.lang.reflect.Constructor<?> constructorMethod) {
    super(description, constructorMethod);
    descriptor = ModelDescriptors.valid(null, constructorMethod.getName(), "operation", "constructor");
  }

  public ModelMBeanConstructorInfo(String description, java.lang.reflect.Constructor<?> constructorMethod,
      Descriptor descriptor) {
    super(description, constructorMethod);
    this.descriptor = ModelDescriptors.valid(descriptor, constructorMethod.getName(), "operation", "constructor");
  }

  public ModelMBeanConstructorInfo(String name, String description, MBeanParameterInfo[] signature) {
    super(name, description, signature);
    descriptor = ModelDescriptors.valid(null, name, "operation", "constructor");
  }

  public ModelMBeanConstructorInfo(String name, String description, MBeanParameterInfo[] signature,
      Descriptor descriptor) {
    super(name, description, signature);
    this.descriptor = ModelDescriptors.valid(descriptor, name, "operation", "constructor");
  }

  public ModelMBeanConstructorInfo(ModelMBeanConstructorInfo inInfo) {
    super(inInfo.getName(), inInfo.getDescription(), inInfo.getSignature());
    descriptor = inInfo.getDescriptor();
  }

  public Descriptor getDescriptor() {
    return (Descriptor) (descriptor == null ? null : descriptor.clone());
  }

  public void setDescriptor(Descriptor inDescriptor) {
    descriptor = ModelDescriptors.valid(inDescriptor, getName(), "operation", "constructor");
  }

  public Object clone() {
    return new ModelMBeanConstructorInfo(this);
  }
}
