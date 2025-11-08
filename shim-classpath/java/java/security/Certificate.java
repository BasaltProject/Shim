/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.security;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

@Deprecated
public interface Certificate {
  Principal getGuarantor();

  Principal getPrincipal();

  PublicKey getPublicKey();

  void encode(OutputStream stream) throws KeyException, IOException;

  void decode(InputStream stream) throws KeyException, IOException;

  String getFormat();

  String toString(boolean detailed);
}
