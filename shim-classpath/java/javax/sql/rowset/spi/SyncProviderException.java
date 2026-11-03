/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.sql.rowset.spi;

import java.sql.SQLException;

public class SyncProviderException extends SQLException {
  static final long serialVersionUID = -939908523620640692L;

  private SyncResolver syncResolver = null;

  public SyncProviderException() {
    super();
  }

  public SyncProviderException(String msg) {
    super(msg);
  }

  public SyncProviderException(SyncResolver syncResolver) {
    if (syncResolver == null) {
      throw new IllegalArgumentException("Cannot instantiate a SyncProviderException with a null SyncResolver object");
    } else {
      this.syncResolver = syncResolver;
    }
  }

  public SyncResolver getSyncResolver() {
    if (syncResolver != null) {
      return syncResolver;
    } else {
      try {
        syncResolver = SyncFactory.getInstance("com.sun.rowset.providers.RIOptimisticProvider").getSyncResolver();
      } catch (SyncFactoryException sfEx) {
      }
      return syncResolver;
    }
  }

  public void setSyncResolver(SyncResolver syncResolver) {
    if (syncResolver == null) {
      throw new IllegalArgumentException("Cannot set a null SyncResolver object");
    } else {
      this.syncResolver = syncResolver;
    }
  }
}
