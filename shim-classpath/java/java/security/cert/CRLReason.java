/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.security.cert;

public enum CRLReason {
  UNSPECIFIED,
  KEY_COMPROMISE,
  CA_COMPROMISE,
  AFFILIATION_CHANGED,
  SUPERSEDED,
  CESSATION_OF_OPERATION,
  CERTIFICATE_HOLD,
  UNUSED,
  REMOVE_FROM_CRL,
  PRIVILEGE_WITHDRAWN,
  AA_COMPROMISE
}
