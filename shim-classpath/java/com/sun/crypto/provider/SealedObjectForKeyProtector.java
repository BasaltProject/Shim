/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package com.sun.crypto.provider;

import java.io.IOException;
import java.io.Serializable;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.SealedObject;

final class SealedObjectForKeyProtector extends SealedObject {
  private static final long serialVersionUID = -3650226485480866989L;

  SealedObjectForKeyProtector(Serializable object, Cipher c) throws IOException, IllegalBlockSizeException {
    super(object, c);
  }

  SealedObjectForKeyProtector(SealedObject so) {
    super(so);
  }
}
