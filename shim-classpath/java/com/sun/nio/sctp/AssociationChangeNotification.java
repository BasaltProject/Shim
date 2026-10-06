/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.nio.sctp;

public abstract class AssociationChangeNotification implements com.sun.nio.sctp.Notification {
  protected AssociationChangeNotification() {
  }

  public abstract com.sun.nio.sctp.Association association();
  public abstract com.sun.nio.sctp.AssociationChangeNotification.AssocChangeEvent event();

  public enum AssocChangeEvent {
    COMM_UP, COMM_LOST, RESTART, SHUTDOWN, CANT_START;


  }
}
