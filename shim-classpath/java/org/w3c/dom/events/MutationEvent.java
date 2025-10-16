/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package org.w3c.dom.events;

public interface MutationEvent extends Event {
  short MODIFICATION = 1;
  short ADDITION = 2;
  short REMOVAL = 3;

  org.w3c.dom.Node getRelatedNode();

  String getPrevValue();

  String getNewValue();

  String getAttrName();

  short getAttrChange();

  void initMutationEvent(String p0, boolean p1, boolean p2, org.w3c.dom.Node p3, String p4, String p5, String p6, short p7);
}
