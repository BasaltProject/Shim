/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.net.spi;

public abstract class InetAddressResolverProvider {
  public interface Configuration {
    InetAddressResolver builtinResolver();

    String lookupLocalHostName() throws java.io.IOException;
  }

  public abstract InetAddressResolver get(Configuration configuration);

  public abstract String name();

  protected InetAddressResolverProvider() {
  }
}
