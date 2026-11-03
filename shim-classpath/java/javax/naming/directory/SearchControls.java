/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.naming.directory;

public class SearchControls implements java.io.Serializable {
  private static final long serialVersionUID = -2480540967773454797L;

  public static final int OBJECT_SCOPE = 0;
  public static final int ONELEVEL_SCOPE = 1;
  public static final int SUBTREE_SCOPE = 2;

  private int searchScope;
  private int timeLimit;
  private boolean derefLink;
  private boolean returnObj;
  private long countLimit;
  private String[] attributesToReturn;

  public SearchControls() {
    searchScope = ONELEVEL_SCOPE;
    timeLimit = 0;
    countLimit = 0;
    derefLink = false;
    returnObj = false;
    attributesToReturn = null;
  }

  public SearchControls(int scope, long countlim, int timelim, String[] attrs, boolean retobj, boolean deref) {
    searchScope = scope;
    timeLimit = timelim;
    countLimit = countlim;
    derefLink = deref;
    returnObj = retobj;
    attributesToReturn = attrs;
  }

  public int getSearchScope() {
    return searchScope;
  }

  public int getTimeLimit() {
    return timeLimit;
  }

  public boolean getDerefLinkFlag() {
    return derefLink;
  }

  public boolean getReturningObjFlag() {
    return returnObj;
  }

  public long getCountLimit() {
    return countLimit;
  }

  public String[] getReturningAttributes() {
    return attributesToReturn;
  }

  public void setSearchScope(int scope) {
    searchScope = scope;
  }

  public void setTimeLimit(int ms) {
    timeLimit = ms;
  }

  public void setDerefLinkFlag(boolean on) {
    derefLink = on;
  }

  public void setReturningObjFlag(boolean on) {
    returnObj = on;
  }

  public void setCountLimit(long limit) {
    countLimit = limit;
  }

  public void setReturningAttributes(String[] attrs) {
    attributesToReturn = attrs;
  }
}
