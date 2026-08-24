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
import javax.management.MBeanOperationInfo;

public class ModelMBeanOperationInfo extends MBeanOperationInfo implements DescriptorAccess {
  private static final long serialVersionUID = 6532732096650090465L;

  private Descriptor descriptor;

  public ModelMBeanOperationInfo(String description, java.lang.reflect.Method operationMethod) {
    super(description, operationMethod);
    descriptor = ModelDescriptors.valid(null, operationMethod.getName(), "operation", "operation");
  }

  public ModelMBeanOperationInfo(String description, java.lang.reflect.Method operationMethod, Descriptor descriptor) {
    super(description, operationMethod);
    this.descriptor = ModelDescriptors.valid(descriptor, operationMethod.getName(), "operation", "operation");
  }

  public ModelMBeanOperationInfo(String name, String description, MBeanParameterInfo[] signature, String type,
      int impact) {
    super(name, description, signature, type, impact);
    descriptor = ModelDescriptors.valid(null, name, "operation", "operation");
  }

  public ModelMBeanOperationInfo(String name, String description, MBeanParameterInfo[] signature, String type,
      int impact, Descriptor descriptor) {
    super(name, description, signature, type, impact);
    this.descriptor = ModelDescriptors.valid(descriptor, name, "operation", "operation");
  }

  public ModelMBeanOperationInfo(ModelMBeanOperationInfo inInfo) {
    super(inInfo.getName(), inInfo.getDescription(), inInfo.getSignature(), inInfo.getReturnType(),
        inInfo.getImpact());
    descriptor = inInfo.getDescriptor();
  }

  public Descriptor getDescriptor() {
    return (Descriptor) (descriptor == null ? null : descriptor.clone());
  }

  public void setDescriptor(Descriptor inDescriptor) {
    descriptor = ModelDescriptors.valid(inDescriptor, getName(), "operation", "operation");
  }

  public Object clone() {
    return new ModelMBeanOperationInfo(this);
  }
}
