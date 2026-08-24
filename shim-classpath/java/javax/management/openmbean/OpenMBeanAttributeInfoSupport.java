/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.management.openmbean;

import javax.management.Descriptor;
import javax.management.MBeanAttributeInfo;

public class OpenMBeanAttributeInfoSupport extends MBeanAttributeInfo implements OpenMBeanAttributeInfo {
  private static final long serialVersionUID = -4867215622149721849L;

  private final OpenInfoData data;

  public OpenMBeanAttributeInfoSupport(String name, String description, OpenType<?> openType, boolean isReadable,
      boolean isWritable, boolean isIs) {
    this(name, description, openType, isReadable, isWritable, isIs, OpenInfoData.simple(openType), (Descriptor) null);
  }

  public OpenMBeanAttributeInfoSupport(String name, String description, OpenType<?> openType, boolean isReadable,
      boolean isWritable, boolean isIs, Descriptor descriptor) {
    this(name, description, openType, isReadable, isWritable, isIs, OpenInfoData.simple(openType), descriptor);
  }

  public <T> OpenMBeanAttributeInfoSupport(String name, String description, OpenType<T> openType, boolean isReadable,
      boolean isWritable, boolean isIs, T defaultValue) throws OpenDataException {
    this(name, description, openType, isReadable, isWritable, isIs,
        OpenInfoData.create(openType, defaultValue, null, null, null), (Descriptor) null);
  }

  public <T> OpenMBeanAttributeInfoSupport(String name, String description, OpenType<T> openType, boolean isReadable,
      boolean isWritable, boolean isIs, T defaultValue, T[] legalValues) throws OpenDataException {
    this(name, description, openType, isReadable, isWritable, isIs,
        OpenInfoData.create(openType, defaultValue, legalValues, null, null), (Descriptor) null);
  }

  public <T> OpenMBeanAttributeInfoSupport(String name, String description, OpenType<T> openType, boolean isReadable,
      boolean isWritable, boolean isIs, T defaultValue, Comparable<T> minValue, Comparable<T> maxValue)
      throws OpenDataException {
    this(name, description, openType, isReadable, isWritable, isIs,
        OpenInfoData.create(openType, defaultValue, null, minValue, maxValue), (Descriptor) null);
  }

  private OpenMBeanAttributeInfoSupport(String name, String description, OpenType<?> openType, boolean isReadable,
      boolean isWritable, boolean isIs, OpenInfoData data, Descriptor descriptor) {
    super(name, openType == null ? null : openType.getClassName(), description, isReadable, isWritable, isIs,
        data.descriptor(descriptor));
    OpenInfoData.checkNameAndDescription(name, description);
    this.data = data;
  }

  public OpenType<?> getOpenType() {
    return data.openType;
  }

  public Object getDefaultValue() {
    return data.defaultValue;
  }

  public java.util.Set<?> getLegalValues() {
    return data.legalValues;
  }

  public Comparable<?> getMinValue() {
    return data.minValue;
  }

  public Comparable<?> getMaxValue() {
    return data.maxValue;
  }

  public boolean hasDefaultValue() {
    return data.defaultValue != null;
  }

  public boolean hasLegalValues() {
    return data.legalValues != null;
  }

  public boolean hasMinValue() {
    return data.minValue != null;
  }

  public boolean hasMaxValue() {
    return data.maxValue != null;
  }

  public boolean isValue(Object obj) {
    return data.isValueChecked(obj);
  }

  public boolean equals(Object obj) {
    if (!(obj instanceof OpenMBeanAttributeInfo)) {
      return false;
    }
    OpenMBeanAttributeInfo other = (OpenMBeanAttributeInfo) obj;
    if (!java.util.Objects.equals(getName(), other.getName())) {
      return false;
    }
    if (!java.util.Objects.equals(getOpenType(), other.getOpenType())) {
      return false;
    }
    return java.util.Objects.equals(getDefaultValue(), other.getDefaultValue())
        && java.util.Objects.equals(getLegalValues(), other.getLegalValues())
        && java.util.Objects.equals(getMinValue(), other.getMinValue())
        && java.util.Objects.equals(getMaxValue(), other.getMaxValue()) && isReadable() == other.isReadable() && isWritable() == other.isWritable() && isIs() == other.isIs();
  }

  public int hashCode() {
    return getName().hashCode() + data.hash() + getDescriptor().hashCode();
  }

  public String toString() {
    return getClass().getName() + "(name=" + getName() + data.describe() + ",descriptor=" + getDescriptor() + ")";
  }
}
