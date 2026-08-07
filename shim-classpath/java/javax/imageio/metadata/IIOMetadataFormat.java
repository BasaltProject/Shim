/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.imageio.metadata;

public interface IIOMetadataFormat {
  int CHILD_POLICY_EMPTY = 0;
  int CHILD_POLICY_ALL = 1;
  int CHILD_POLICY_SOME = 2;
  int CHILD_POLICY_CHOICE = 3;
  int CHILD_POLICY_SEQUENCE = 4;
  int CHILD_POLICY_REPEAT = 5;
  int CHILD_POLICY_MAX = 5;
  int VALUE_NONE = 0;
  int VALUE_ARBITRARY = 1;
  int VALUE_RANGE = 2;
  int VALUE_RANGE_MIN_INCLUSIVE_MASK = 4;
  int VALUE_RANGE_MAX_INCLUSIVE_MASK = 8;
  int VALUE_RANGE_MIN_INCLUSIVE = 6;
  int VALUE_RANGE_MAX_INCLUSIVE = 10;
  int VALUE_RANGE_MIN_MAX_INCLUSIVE = 14;
  int VALUE_ENUMERATION = 16;
  int VALUE_LIST = 32;
  int DATATYPE_STRING = 0;
  int DATATYPE_BOOLEAN = 1;
  int DATATYPE_INTEGER = 2;
  int DATATYPE_FLOAT = 3;
  int DATATYPE_DOUBLE = 4;
  java.lang.String[] getAttributeNames(java.lang.String p0);
  boolean canNodeAppear(java.lang.String p0, javax.imageio.ImageTypeSpecifier p1);
  int getElementMinChildren(java.lang.String p0);
  int getElementMaxChildren(java.lang.String p0);
  java.lang.String getElementDescription(java.lang.String p0, java.util.Locale p1);
  int getChildPolicy(java.lang.String p0);
  java.lang.String[] getChildNames(java.lang.String p0);
  int getAttributeValueType(java.lang.String p0, java.lang.String p1);
  int getAttributeDataType(java.lang.String p0, java.lang.String p1);
  boolean isAttributeRequired(java.lang.String p0, java.lang.String p1);
  java.lang.String getAttributeDefaultValue(java.lang.String p0, java.lang.String p1);
  java.lang.String[] getAttributeEnumerations(java.lang.String p0, java.lang.String p1);
  java.lang.String getAttributeMinValue(java.lang.String p0, java.lang.String p1);
  java.lang.String getAttributeMaxValue(java.lang.String p0, java.lang.String p1);
  int getAttributeListMinLength(java.lang.String p0, java.lang.String p1);
  int getAttributeListMaxLength(java.lang.String p0, java.lang.String p1);
  java.lang.String getAttributeDescription(java.lang.String p0, java.lang.String p1, java.util.Locale p2);
  int getObjectValueType(java.lang.String p0);
  java.lang.Class<?> getObjectClass(java.lang.String p0);
  java.lang.Object getObjectDefaultValue(java.lang.String p0);
  java.lang.Object[] getObjectEnumerations(java.lang.String p0);
  java.lang.Comparable<?> getObjectMinValue(java.lang.String p0);
  java.lang.Comparable<?> getObjectMaxValue(java.lang.String p0);
  int getObjectArrayMinLength(java.lang.String p0);
  int getObjectArrayMaxLength(java.lang.String p0);
  java.lang.String getRootName();
}
