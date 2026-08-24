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
import javax.management.MBeanNotificationInfo;

public class ModelMBeanNotificationInfo extends MBeanNotificationInfo implements DescriptorAccess {
  private static final long serialVersionUID = -7445681389570207141L;

  private Descriptor descriptor;

  public ModelMBeanNotificationInfo(String[] notifTypes, String name, String description) {
    super(notifTypes, name, description);
    descriptor = ModelDescriptors.valid(null, name, "notification", null);
  }

  public ModelMBeanNotificationInfo(String[] notifTypes, String name, String description, Descriptor descriptor) {
    super(notifTypes, name, description);
    this.descriptor = ModelDescriptors.valid(descriptor, name, "notification", null);
  }

  public ModelMBeanNotificationInfo(ModelMBeanNotificationInfo inInfo) {
    super(inInfo.getNotifTypes(), inInfo.getName(), inInfo.getDescription());
    descriptor = inInfo.getDescriptor();
  }

  public Descriptor getDescriptor() {
    return (Descriptor) (descriptor == null ? null : descriptor.clone());
  }

  public void setDescriptor(Descriptor inDescriptor) {
    descriptor = ModelDescriptors.valid(inDescriptor, getName(), "notification", null);
  }

  public Object clone() {
    return new ModelMBeanNotificationInfo(this);
  }
}
