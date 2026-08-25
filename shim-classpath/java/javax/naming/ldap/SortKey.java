/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.naming.ldap;

public class SortKey {
  private String attrID;
  private boolean reverseOrder = false;
  private String matchingRuleID = null;

  public SortKey(String attrID) {
    this.attrID = attrID;
  }

  public SortKey(String attrID, boolean ascendingOrder, String matchingRuleID) {
    this.attrID = attrID;
    reverseOrder = !ascendingOrder;
    this.matchingRuleID = matchingRuleID;
  }

  public String getAttributeID() {
    return attrID;
  }

  public boolean isAscending() {
    return !reverseOrder;
  }

  public String getMatchingRuleID() {
    return matchingRuleID;
  }
}
