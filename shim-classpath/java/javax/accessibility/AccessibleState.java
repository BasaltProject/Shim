/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.accessibility;

public class AccessibleState extends javax.accessibility.AccessibleBundle {
  public static final javax.accessibility.AccessibleState ACTIVE = new javax.accessibility.AccessibleState("active");
  public static final javax.accessibility.AccessibleState PRESSED = new javax.accessibility.AccessibleState("pressed");
  public static final javax.accessibility.AccessibleState ARMED = new javax.accessibility.AccessibleState("armed");
  public static final javax.accessibility.AccessibleState BUSY = new javax.accessibility.AccessibleState("busy");
  public static final javax.accessibility.AccessibleState CHECKED = new javax.accessibility.AccessibleState("checked");
  public static final javax.accessibility.AccessibleState EDITABLE = new javax.accessibility.AccessibleState("editable");
  public static final javax.accessibility.AccessibleState EXPANDABLE = new javax.accessibility.AccessibleState("expandable");
  public static final javax.accessibility.AccessibleState COLLAPSED = new javax.accessibility.AccessibleState("collapsed");
  public static final javax.accessibility.AccessibleState EXPANDED = new javax.accessibility.AccessibleState("expanded");
  public static final javax.accessibility.AccessibleState ENABLED = new javax.accessibility.AccessibleState("enabled");
  public static final javax.accessibility.AccessibleState FOCUSABLE = new javax.accessibility.AccessibleState("focusable");
  public static final javax.accessibility.AccessibleState FOCUSED = new javax.accessibility.AccessibleState("focused");
  public static final javax.accessibility.AccessibleState ICONIFIED = new javax.accessibility.AccessibleState("iconified");
  public static final javax.accessibility.AccessibleState MODAL = new javax.accessibility.AccessibleState("modal");
  public static final javax.accessibility.AccessibleState OPAQUE = new javax.accessibility.AccessibleState("opaque");
  public static final javax.accessibility.AccessibleState RESIZABLE = new javax.accessibility.AccessibleState("resizable");
  public static final javax.accessibility.AccessibleState MULTISELECTABLE = new javax.accessibility.AccessibleState("multiselectable");
  public static final javax.accessibility.AccessibleState SELECTABLE = new javax.accessibility.AccessibleState("selectable");
  public static final javax.accessibility.AccessibleState SELECTED = new javax.accessibility.AccessibleState("selected");
  public static final javax.accessibility.AccessibleState SHOWING = new javax.accessibility.AccessibleState("showing");
  public static final javax.accessibility.AccessibleState VISIBLE = new javax.accessibility.AccessibleState("visible");
  public static final javax.accessibility.AccessibleState VERTICAL = new javax.accessibility.AccessibleState("vertical");
  public static final javax.accessibility.AccessibleState HORIZONTAL = new javax.accessibility.AccessibleState("horizontal");
  public static final javax.accessibility.AccessibleState SINGLE_LINE = new javax.accessibility.AccessibleState("singleline");
  public static final javax.accessibility.AccessibleState MULTI_LINE = new javax.accessibility.AccessibleState("multiline");
  public static final javax.accessibility.AccessibleState TRANSIENT = new javax.accessibility.AccessibleState("transient");
  public static final javax.accessibility.AccessibleState MANAGES_DESCENDANTS = new javax.accessibility.AccessibleState("managesDescendants");
  public static final javax.accessibility.AccessibleState INDETERMINATE = new javax.accessibility.AccessibleState("indeterminate");
  public static final javax.accessibility.AccessibleState TRUNCATED = new javax.accessibility.AccessibleState("truncated");
  protected AccessibleState(java.lang.String key) {
    super();
    this.key = key;
  }
}
