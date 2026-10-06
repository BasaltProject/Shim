/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.rowset.providers;

import javax.sql.RowSetReader;
import javax.sql.RowSetWriter;
import javax.sql.rowset.spi.SyncProvider;
import javax.sql.rowset.spi.SyncProviderException;

public final class RIXMLProvider extends SyncProvider {
  private String providerID = "com.sun.rowset.providers.RIXMLProvider";
  private String vendorName = "Oracle Corporation";
  private String versionNumber = "1.0";
  private RowSetReader rowSetReader;
  private RowSetWriter rowSetWriter;

  public RIXMLProvider() {
  }

  public String getProviderID() {
    return providerID;
  }

  public RowSetReader getRowSetReader() {
    if (rowSetReader == null) {
      rowSetReader = new com.sun.rowset.internal.WebRowSetXmlReader();
    }
    return rowSetReader;
  }

  public RowSetWriter getRowSetWriter() {
    if (rowSetWriter == null) {
      rowSetWriter = new com.sun.rowset.internal.WebRowSetXmlWriter();
    }
    return rowSetWriter;
  }

  public int getProviderGrade() {
    return SyncProvider.GRADE_NONE;
  }

  public void setDataSourceLock(int datasource_lock) throws SyncProviderException {
    if (datasource_lock != SyncProvider.DATASOURCE_NO_LOCK) {
      throw new UnsupportedOperationException("RIXMLProvider implementation does not support locking");
    }
  }

  public int getDataSourceLock() throws SyncProviderException {
    return SyncProvider.DATASOURCE_NO_LOCK;
  }

  public int supportsUpdatableView() {
    return SyncProvider.NONUPDATABLE_VIEW_SYNC;
  }

  public String getVersion() {
    return this.versionNumber;
  }

  public String getVendor() {
    return this.vendorName;
  }
}
