/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.security.auth.login;

public abstract class ConfigurationSpi {
  public ConfigurationSpi() {
  }

  protected abstract AppConfigurationEntry[] engineGetAppConfigurationEntry(String name);

  protected void engineRefresh() {
  }
}
