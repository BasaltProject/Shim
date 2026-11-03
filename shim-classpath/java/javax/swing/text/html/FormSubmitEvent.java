/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */
package javax.swing.text.html;

import java.net.URL;
import javax.swing.event.HyperlinkEvent;
import javax.swing.text.Element;

public final class FormSubmitEvent extends HTMLFrameHyperlinkEvent {
  private final MethodType method;
  private final String data;

  FormSubmitEvent(Object source, HyperlinkEvent.EventType type, URL targetURL, Element sourceElement, String targetFrame,
      MethodType method, String data) {
    super(source, type, targetURL, sourceElement, targetFrame);
    this.method = method;
    this.data = data;
  }

  public MethodType getMethod() { return method; }

  public String getData() { return data; }

  public enum MethodType {
    GET, POST
  }
}
