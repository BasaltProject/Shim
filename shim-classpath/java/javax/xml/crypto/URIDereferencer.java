/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.xml.crypto;

public interface URIDereferencer {
  javax.xml.crypto.Data dereference(javax.xml.crypto.URIReference uRIReference, javax.xml.crypto.XMLCryptoContext xMLCryptoContext) throws javax.xml.crypto.URIReferenceException;
}
