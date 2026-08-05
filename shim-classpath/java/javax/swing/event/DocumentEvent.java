/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing.event;

public interface DocumentEvent {
  int getLength();
  javax.swing.event.DocumentEvent.EventType getType();
  int getOffset();
  javax.swing.text.Document getDocument();
  javax.swing.event.DocumentEvent.ElementChange getChange(javax.swing.text.Element p0);

  public interface ElementChange {
    int getIndex();
    javax.swing.text.Element getElement();
    javax.swing.text.Element[] getChildrenRemoved();
    javax.swing.text.Element[] getChildrenAdded();
  }

  public static final class EventType {
    public static final javax.swing.event.DocumentEvent.EventType INSERT = new javax.swing.event.DocumentEvent.EventType("INSERT");
    public static final javax.swing.event.DocumentEvent.EventType REMOVE = new javax.swing.event.DocumentEvent.EventType("REMOVE");
    public static final javax.swing.event.DocumentEvent.EventType CHANGE = new javax.swing.event.DocumentEvent.EventType("CHANGE");
    private final java.lang.String typeString;
    private EventType(java.lang.String s) { typeString = s; }
    public java.lang.String toString() { return typeString; }
  }
}
