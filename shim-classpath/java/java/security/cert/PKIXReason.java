/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.security.cert;

public enum PKIXReason implements CertPathValidatorException.Reason {
  NAME_CHAINING,
  INVALID_KEY_USAGE,
  INVALID_POLICY,
  NO_TRUST_ANCHOR,
  UNRECOGNIZED_CRIT_EXT,
  NOT_CA_CERT,
  PATH_TOO_LONG,
  INVALID_NAME
}
