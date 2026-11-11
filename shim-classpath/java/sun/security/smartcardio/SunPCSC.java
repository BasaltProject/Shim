/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */
package sun.security.smartcardio;

import java.security.PrivilegedAction;
import java.security.Provider;
import javax.smartcardio.CardTerminals;
import javax.smartcardio.TerminalFactorySpi;

public final class SunPCSC extends Provider {
  private static final long serialVersionUID = 6168388284028876579L;

  public SunPCSC() {
    super("SunPCSC", "25", "Sun PC/SC provider");
    putService(new ProviderService(this, "TerminalFactory", "PC/SC", "sun.security.smartcardio.SunPCSC$Factory"));
  }

  private static final class ProviderService extends Provider.Service {
    ProviderService(Provider p, String type, String algo, String cn) {
      super(p, type, algo, cn, null, null);
    }

    public Object newInstance(Object ctrParamObj) throws java.security.NoSuchAlgorithmException {
      if (getType().equals("TerminalFactory") && getAlgorithm().equals("PC/SC")) {
        try {
          return new Factory(ctrParamObj);
        } catch (Exception e) {
          throw new java.security.NoSuchAlgorithmException("Error constructing TerminalFactory for PC/SC using SunPCSC",
              e);
        }
      }
      throw new java.security.ProviderException("No impl for " + getAlgorithm());
    }
  }

  public static final class Factory extends TerminalFactorySpi {
    public Factory(Object obj) throws PCSCException {
      if (obj != null) {
        throw new IllegalArgumentException("SunPCSC factory does not use parameters");
      }
      PCSC.checkAvailable();
      PCSCTerminals.initContext();
    }

    protected CardTerminals engineTerminals() {
      return new PCSCTerminals();
    }
  }
}
