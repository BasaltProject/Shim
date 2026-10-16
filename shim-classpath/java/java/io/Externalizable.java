/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.io;

import java.io.ObjectOutput;

public interface Externalizable extends Serializable {
  void writeExternal(ObjectOutput out) throws IOException;

  void readExternal(ObjectInput in) throws IOException, ClassNotFoundException;
}
