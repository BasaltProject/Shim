/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.awt.desktop;

public final class UserSessionEvent extends AppEvent {
  private static final long serialVersionUID = 6747138462796569055L;

  public static enum Reason {
    UNSPECIFIED, CONSOLE, REMOTE, LOCK
  }

  private final Reason reason;

  public UserSessionEvent(Reason reason) {
    super();
    if (reason == null) {
      throw new IllegalArgumentException("null reason");
    }
    this.reason = reason;
  }

  public Reason getReason() { return reason; }
}
