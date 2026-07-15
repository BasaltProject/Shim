/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.accessibility;

import java.util.Vector;

public class AccessibleRelationSet {
  protected Vector<AccessibleRelation> relations = null;

  public AccessibleRelationSet() { }

  public AccessibleRelationSet(AccessibleRelation[] relations) {
    if (relations.length != 0) {
      this.relations = new Vector<>(relations.length);
      for (AccessibleRelation r : relations) {
        add(r);
      }
    }
  }

  public boolean add(AccessibleRelation relation) {
    if (relations == null) {
      relations = new Vector<>();
    }
    AccessibleRelation existingRelation = get(relation.getKey());
    if (existingRelation == null) {
      relations.addElement(relation);
      return true;
    }
    Object[] existingTarget = existingRelation.getTarget();
    Object[] newTarget = relation.getTarget();
    int mergedLength = existingTarget.length + newTarget.length;
    Object[] mergedTarget = new Object[mergedLength];
    for (int i = 0; i < existingTarget.length; i++) {
      mergedTarget[i] = existingTarget[i];
    }
    for (int i = existingTarget.length, j = 0; i < mergedLength; i++, j++) {
      mergedTarget[i] = newTarget[j];
    }
    existingRelation.setTarget(mergedTarget);
    return true;
  }

  public void addAll(AccessibleRelation[] relations) {
    if (relations.length != 0) {
      if (this.relations == null) {
        this.relations = new Vector<>(relations.length);
      }
      for (AccessibleRelation r : relations) {
        add(r);
      }
    }
  }

  public boolean remove(AccessibleRelation relation) { return relations == null ? false : relations.removeElement(relation); }

  public void clear() {
    if (relations != null) {
      relations.removeAllElements();
    }
  }

  public int size() { return relations == null ? 0 : relations.size(); }

  public boolean contains(String key) { return get(key) != null; }

  public AccessibleRelation get(String key) {
    if (relations == null) {
      return null;
    }
    for (AccessibleRelation r : relations) {
      if (r != null && r.getKey().equals(key)) {
        return r;
      }
    }
    return null;
  }

  public AccessibleRelation[] toArray() {
    int n = relations == null ? 0 : relations.size();
    AccessibleRelation[] r = new AccessibleRelation[n];
    for (int i = 0; i < n; i++) {
      r[i] = relations.elementAt(i);
    }
    return r;
  }

  public String toString() {
    String ret = "";
    if (relations != null && !relations.isEmpty()) {
      ret = relations.elementAt(0).toDisplayString();
      for (int i = 1; i < relations.size(); i++) {
        ret = ret + "," + relations.elementAt(i).toDisplayString();
      }
    }
    return ret;
  }
}
