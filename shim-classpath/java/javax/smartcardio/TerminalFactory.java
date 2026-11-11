/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.smartcardio;

import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.security.Security;
import java.util.List;

public final class TerminalFactory {
  private static final Provider NONE = new Provider("None", "1.0", "none") {
    private static final long serialVersionUID = 2745808869881593918L;
  };

  private static final String PROP_NAME = "javax.smartcardio.TerminalFactory.DefaultType";
  private static final String defaultType;
  private static final TerminalFactory defaultFactory;

  static {
    String type = System.getProperty(PROP_NAME, "PC/SC").trim();
    TerminalFactory factory = null;
    try {
      factory = TerminalFactory.getInstance(type, null);
    } catch (Exception e) {
    }
    if (factory == null) {
      type = "None";
      factory = new TerminalFactory(new NoneFactorySpi(), NONE, "None");
    }
    defaultType = type;
    defaultFactory = factory;
  }

  private static final class NoneFactorySpi extends TerminalFactorySpi {
    protected CardTerminals engineTerminals() {
      return new NoneCardTerminals();
    }
  }

  private static final class NoneCardTerminals extends CardTerminals {
    public List<CardTerminal> list(State state) throws CardException {
      if (state == null) {
        throw new NullPointerException();
      }
      return java.util.Collections.emptyList();
    }

    public boolean waitForChange(long timeout) throws CardException {
      throw new IllegalStateException("no terminals");
    }
  }

  private final TerminalFactorySpi spi;
  private final Provider provider;
  private final String type;

  private TerminalFactory(TerminalFactorySpi spi, Provider provider, String type) {
    this.spi = spi;
    this.provider = provider;
    this.type = type;
  }

  public static String getDefaultType() {
    return defaultType;
  }

  public static TerminalFactory getDefault() {
    return defaultFactory;
  }

  public static TerminalFactory getInstance(String type, Object params) throws NoSuchAlgorithmException {
    if (type == null) {
      throw new NullPointerException("type must not be null");
    }
    NoSuchAlgorithmException failure = null;
    for (Provider p : Security.getProviders()) {
      Provider.Service s = p.getService("TerminalFactory", type);
      if (s == null) {
        continue;
      }
      try {
        return new TerminalFactory((TerminalFactorySpi) s.newInstance(params), p, type);
      } catch (NoSuchAlgorithmException e) {
        if (failure == null) {
          failure = e;
        }
      }
    }
    if (failure != null) {
      throw failure;
    }
    throw new NoSuchAlgorithmException(type + " TerminalFactory not available");
  }

  public static TerminalFactory getInstance(String type, Object params, String provider)
      throws NoSuchAlgorithmException, NoSuchProviderException {
    if (provider == null || provider.length() == 0) {
      throw new IllegalArgumentException("missing provider");
    }
    Provider p = Security.getProvider(provider);
    if (p == null) {
      throw new NoSuchProviderException("no such provider: " + provider);
    }
    return getInstance(type, params, p);
  }

  public static TerminalFactory getInstance(String type, Object params, Provider provider)
      throws NoSuchAlgorithmException {
    if (provider == null) {
      throw new IllegalArgumentException("missing provider");
    }
    if (type == null) {
      throw new NullPointerException("type must not be null");
    }
    Provider.Service s = provider.getService("TerminalFactory", type);
    if (s == null) {
      throw new NoSuchAlgorithmException(type + " TerminalFactory not available");
    }
    return new TerminalFactory((TerminalFactorySpi) s.newInstance(params), provider, type);
  }

  public Provider getProvider() {
    return provider;
  }

  public String getType() {
    return type;
  }

  public CardTerminals terminals() {
    return spi.engineTerminals();
  }

  public String toString() {
    return "TerminalFactory for type " + type + " from provider " + provider.getName();
  }
}
