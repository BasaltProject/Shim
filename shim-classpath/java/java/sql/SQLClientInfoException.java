/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.sql;

public class SQLClientInfoException extends SQLException {
  private static final long serialVersionUID = -4319604256824655880L;

  private java.util.Map<String, ClientInfoStatus> failedProperties;

  public SQLClientInfoException() {
    super();
    this.failedProperties = null;
  }

  public SQLClientInfoException(java.util.Map<String, ClientInfoStatus> failedProperties) {
    super();
    this.failedProperties = failedProperties;
  }

  public SQLClientInfoException(java.util.Map<String, ClientInfoStatus> failedProperties, Throwable cause) {
    super(cause);
    this.failedProperties = failedProperties;
  }

  public SQLClientInfoException(String reason, java.util.Map<String, ClientInfoStatus> failedProperties) {
    super(reason);
    this.failedProperties = failedProperties;
  }

  public SQLClientInfoException(String reason, java.util.Map<String, ClientInfoStatus> failedProperties,
      Throwable cause) {
    super(reason, cause);
    this.failedProperties = failedProperties;
  }

  public SQLClientInfoException(String reason, String SQLState, java.util.Map<String,
      ClientInfoStatus> failedProperties) {
    super(reason, SQLState);
    this.failedProperties = failedProperties;
  }

  public SQLClientInfoException(String reason, String SQLState, java.util.Map<String,
      ClientInfoStatus> failedProperties, Throwable cause) {
    super(reason, SQLState, cause);
    this.failedProperties = failedProperties;
  }

  public SQLClientInfoException(String reason, String SQLState, int vendorCode, java.util.Map<String,
      ClientInfoStatus> failedProperties) {
    super(reason, SQLState, vendorCode);
    this.failedProperties = failedProperties;
  }

  public SQLClientInfoException(String reason, String SQLState, int vendorCode, java.util.Map<String,
      ClientInfoStatus> failedProperties, Throwable cause) {
    super(reason, SQLState, vendorCode, cause);
    this.failedProperties = failedProperties;
  }

  public java.util.Map<String, ClientInfoStatus> getFailedProperties() {
    return this.failedProperties;
  }
}
