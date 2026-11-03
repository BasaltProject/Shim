/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.management.modelmbean;

import javax.management.Descriptor;
import javax.management.RuntimeOperationsException;

final class ModelDescriptors {
  private ModelDescriptors() {
  }

  static Descriptor valid(Descriptor in, String name, String type, String role) {
    DescriptorSupport d;
    if (in == null) {
      d = new DescriptorSupport();
    } else if (in instanceof DescriptorSupport) {
      d = (DescriptorSupport) in.clone();
    } else {
      d = new DescriptorSupport(in.getFieldNames(), in.getFieldValues((String[]) null));
    }
    if (d.getFieldValue("name") == null) {
      d.setField("name", name);
    }
    if (d.getFieldValue("descriptorType") == null) {
      d.setField("descriptorType", type);
    }
    if (d.getFieldValue("displayName") == null) {
      d.setField("displayName", name);
    }
    if (role != null && d.getFieldValue("role") == null) {
      d.setField("role", role);
    }
    if (!d.isValid()) {
      throw new RuntimeOperationsException(new IllegalArgumentException("Invalid descriptor passed in parameter"),
          "Invalid descriptor passed in parameter");
    }
    if (!name.equals(d.getFieldValue("name"))) {
      String m = "The Descriptor \"name\" field does not match the object described.  Expected: " + name + " , was: "
          + d.getFieldValue("name");
      throw new RuntimeOperationsException(new IllegalArgumentException(m), m);
    }
    if (!type.equalsIgnoreCase((String) d.getFieldValue("descriptorType"))) {
      String m = "The Descriptor \"descriptorType\" field does not match the object described.  Expected: " + type
          + " , was: " + d.getFieldValue("descriptorType");
      throw new RuntimeOperationsException(new IllegalArgumentException(m), m);
    }
    return d;
  }
}
