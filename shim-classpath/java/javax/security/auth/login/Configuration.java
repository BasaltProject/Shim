/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.security.auth.login;

import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Provider;

public abstract class Configuration {
  private static Configuration configuration;

  private final Provider provider;
  private final String type;
  private final Configuration.Parameters params;

  public interface Parameters {
  }

  protected Configuration() {
    this(null, null, null);
  }

  private Configuration(Provider provider, String type, Configuration.Parameters params) {
    this.provider = provider;
    this.type = type;
    this.params = params;
  }

  public static synchronized Configuration getConfiguration() {
    if (configuration == null) {
      configuration = new Configuration() {
        @Override
        public AppConfigurationEntry[] getAppConfigurationEntry(String name) {
          return null;
        }
      };
    }
    return configuration;
  }

  public static synchronized void setConfiguration(Configuration configuration) {
    Configuration.configuration = configuration;
  }

  public static Configuration getInstance(String type, Configuration.Parameters params)
      throws NoSuchAlgorithmException {
    if (type == null) {
      throw new NullPointerException("null type name");
    }
    throw new NoSuchAlgorithmException(type + " Configuration not available");
  }

  public static Configuration getInstance(String type, Configuration.Parameters params, String provider)
      throws NoSuchProviderException, NoSuchAlgorithmException {
    if (provider == null || provider.isEmpty()) {
      throw new IllegalArgumentException("missing provider");
    }
    throw new NoSuchProviderException("no such provider: " + provider);
  }

  public static Configuration getInstance(String type, Configuration.Parameters params, Provider provider)
      throws NoSuchAlgorithmException {
    if (provider == null) {
      throw new IllegalArgumentException("missing provider");
    }
    return getInstance(type, params);
  }

  public Provider getProvider() {
    return provider;
  }

  public String getType() {
    return type;
  }

  public Configuration.Parameters getParameters() {
    return params;
  }

  public abstract AppConfigurationEntry[] getAppConfigurationEntry(String name);

  public void refresh() {
  }
}
