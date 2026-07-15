/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.accessibility;

public class AccessibleRelation extends AccessibleBundle {
  public static final java.lang.String LABEL_FOR = "labelFor";
  public static final java.lang.String LABELED_BY = "labeledBy";
  public static final java.lang.String MEMBER_OF = "memberOf";
  public static final java.lang.String CONTROLLER_FOR = "controllerFor";
  public static final java.lang.String CONTROLLED_BY = "controlledBy";
  public static final java.lang.String FLOWS_TO = "flowsTo";
  public static final java.lang.String FLOWS_FROM = "flowsFrom";
  public static final java.lang.String SUBWINDOW_OF = "subwindowOf";
  public static final java.lang.String PARENT_WINDOW_OF = "parentWindowOf";
  public static final java.lang.String EMBEDS = "embeds";
  public static final java.lang.String EMBEDDED_BY = "embeddedBy";
  public static final java.lang.String CHILD_NODE_OF = "childNodeOf";
  public static final java.lang.String LABEL_FOR_PROPERTY = "labelForProperty";
  public static final java.lang.String LABELED_BY_PROPERTY = "labeledByProperty";
  public static final java.lang.String MEMBER_OF_PROPERTY = "memberOfProperty";
  public static final java.lang.String CONTROLLER_FOR_PROPERTY = "controllerForProperty";
  public static final java.lang.String CONTROLLED_BY_PROPERTY = "controlledByProperty";
  public static final java.lang.String FLOWS_TO_PROPERTY = "flowsToProperty";
  public static final java.lang.String FLOWS_FROM_PROPERTY = "flowsFromProperty";
  public static final java.lang.String SUBWINDOW_OF_PROPERTY = "subwindowOfProperty";
  public static final java.lang.String PARENT_WINDOW_OF_PROPERTY = "parentWindowOfProperty";
  public static final java.lang.String EMBEDS_PROPERTY = "embedsProperty";
  public static final java.lang.String EMBEDDED_BY_PROPERTY = "embeddedByProperty";
  public static final java.lang.String CHILD_NODE_OF_PROPERTY = "childNodeOfProperty";
  private Object[] target = new Object[0];

  public AccessibleRelation(String key) { this.key = key; this.target = null; }

  public AccessibleRelation(String key, Object target) {
    this.key = key;
    this.target = new Object[] {target};
  }

  public AccessibleRelation(String key, Object[] target) {
    this.key = key;
    this.target = target;
  }

  public String getKey() { return this.key; }

  public Object[] getTarget() {
    if (target == null) {
      target = new Object[0];
    }
    Object[] retval = new Object[target.length];
    for (int i = 0; i < target.length; i++) {
      retval[i] = target[i];
    }
    return retval;
  }

  public void setTarget(Object target) { this.target = new Object[] {target}; }

  public void setTarget(Object[] target) { this.target = target; }
}
