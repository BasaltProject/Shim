/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.security.cert;

import java.util.Collection;
import java.util.Collections;

public class CollectionCertStoreParameters implements CertStoreParameters {
  private final Collection<?> coll;

  public CollectionCertStoreParameters(Collection<?> collection) {
    if (collection == null) {
      throw new NullPointerException();
    }
    coll = collection;
  }

  public CollectionCertStoreParameters() {
    coll = Collections.EMPTY_SET;
  }

  public Collection<?> getCollection() {
    return coll;
  }

  @Override
  public Object clone() {
    try {
      return super.clone();
    } catch (CloneNotSupportedException e) {
      throw new InternalError(e.toString(), e);
    }
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("CollectionCertStoreParameters: [\n");
    sb.append("  collection: " + coll + "\n");
    sb.append("]");
    return sb.toString();
  }
}
