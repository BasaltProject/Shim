/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.net.ssl;

import java.util.List;
import javax.crypto.SecretKey;

public abstract class ExtendedSSLSession implements SSLSession {
  public ExtendedSSLSession() {
  }

  public abstract String[] getLocalSupportedSignatureAlgorithms();

  public abstract String[] getPeerSupportedSignatureAlgorithms();

  public List<SNIServerName> getRequestedServerNames() {
    throw new UnsupportedOperationException();
  }

  public List<byte[]> getStatusResponses() {
    throw new UnsupportedOperationException();
  }

  public SecretKey exportKeyingMaterialKey(String keyAlg, String label, byte[] context, int length)
      throws SSLKeyException {
    throw new UnsupportedOperationException("Method not implemented");
  }

  public byte[] exportKeyingMaterialData(String label, byte[] context, int length) throws SSLKeyException {
    throw new UnsupportedOperationException("Method not implemented");
  }
}
