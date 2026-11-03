/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing.text.html;

public class HTMLFrameHyperlinkEvent extends javax.swing.event.HyperlinkEvent {
  private java.lang.String bp_Target;
  public HTMLFrameHyperlinkEvent(java.lang.Object p0, javax.swing.event.HyperlinkEvent.EventType p1, java.net.URL p2, java.lang.String p3, javax.swing.text.Element p4, java.awt.event.InputEvent p5, java.lang.String p6) {
    super(p0, p1, p2, p3, p4, p5);
    bp_Target = p6;
  }
  public HTMLFrameHyperlinkEvent(java.lang.Object p0, javax.swing.event.HyperlinkEvent.EventType p1, java.net.URL p2, java.lang.String p3, javax.swing.text.Element p4, java.lang.String p5) {
    super(p0, p1, p2, p3, p4);
    bp_Target = p5;
  }
  public HTMLFrameHyperlinkEvent(java.lang.Object p0, javax.swing.event.HyperlinkEvent.EventType p1, java.net.URL p2, javax.swing.text.Element p3, java.lang.String p4) {
    super(p0, p1, p2, null, p3);
    bp_Target = p4;
  }
  public HTMLFrameHyperlinkEvent(java.lang.Object p0, javax.swing.event.HyperlinkEvent.EventType p1, java.net.URL p2, java.lang.String p3, java.lang.String p4) {
    super(p0, p1, p2, p3);
    bp_Target = p4;
  }
  public HTMLFrameHyperlinkEvent(java.lang.Object p0, javax.swing.event.HyperlinkEvent.EventType p1, java.net.URL p2, java.lang.String p3) {
    super(p0, p1, p2);
    bp_Target = p3;
  }
  public java.lang.String getTarget() { return bp_Target; }
}
