/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing.event;

import java.awt.event.InputEvent;
import java.net.URL;
import java.util.EventObject;
import javax.swing.text.Element;

public class HyperlinkEvent extends EventObject {
  private final EventType type;
  private final URL u;
  private final String desc;
  private final Element sourceElement;
  private final InputEvent inputEvent;

  public HyperlinkEvent(Object source, EventType type, URL u) { this(source, type, u, null); }

  public HyperlinkEvent(Object source, EventType type, URL u, String desc) { this(source, type, u, desc, null); }

  public HyperlinkEvent(Object source, EventType type, URL u, String desc, Element sourceElement) {
    this(source, type, u, desc, sourceElement, null);
  }

  public HyperlinkEvent(Object source, EventType type, URL u, String desc, Element sourceElement,
      InputEvent inputEvent) {
    super(source);
    this.type = type;
    this.u = u;
    this.desc = desc;
    this.sourceElement = sourceElement;
    this.inputEvent = inputEvent;
  }

  public EventType getEventType() { return type; }

  public String getDescription() { return desc; }

  public URL getURL() { return u; }

  public Element getSourceElement() { return sourceElement; }

  public InputEvent getInputEvent() { return inputEvent; }

  public static final class EventType {
    public static final EventType ENTERED = new EventType("ENTERED");
    public static final EventType EXITED = new EventType("EXITED");
    public static final EventType ACTIVATED = new EventType("ACTIVATED");

    private final String typeString;

    private EventType(String s) { typeString = s; }

    public String toString() { return typeString; }
  }
}
