/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.sql;

public interface ParameterMetaData extends Wrapper {
  int parameterNoNulls = 0;
  int parameterNullable = 1;
  int parameterNullableUnknown = 2;
  int parameterModeUnknown = 0;
  int parameterModeIn = 1;
  int parameterModeInOut = 2;
  int parameterModeOut = 4;

  int getParameterCount() throws SQLException;

  int isNullable(int p0) throws SQLException;

  boolean isSigned(int p0) throws SQLException;

  int getPrecision(int p0) throws SQLException;

  int getScale(int p0) throws SQLException;

  int getParameterType(int p0) throws SQLException;

  String getParameterTypeName(int p0) throws SQLException;

  String getParameterClassName(int p0) throws SQLException;

  int getParameterMode(int p0) throws SQLException;
}
