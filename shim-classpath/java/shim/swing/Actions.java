/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.swing;

import java.awt.AWTEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.KeyStroke;

public final class Actions {
  private Actions() { }

  public static boolean notifyAction(Action action, KeyStroke ks, AWTEvent event, Object sender, int modifiers) {
    if (action == null || !action.isEnabled()) {
      return false;
    }
    Object command = action.getValue(Action.ACTION_COMMAND_KEY);
    if (command == null && ks != null && ks.getKeyChar() != KeyEvent.CHAR_UNDEFINED) {
      command = String.valueOf(ks.getKeyChar());
    }
    if (command == null && event instanceof KeyEvent && ((KeyEvent) event).getKeyChar() != KeyEvent.CHAR_UNDEFINED) {
      command = String.valueOf(((KeyEvent) event).getKeyChar());
    }
    action.actionPerformed(new ActionEvent(sender, ActionEvent.ACTION_PERFORMED, (String) command, modifiers));
    return true;
  }

  public static final class ListenerAction extends AbstractAction {
    private final ActionListener listener;

    public ListenerAction(ActionListener listener) { this.listener = listener; }

    public ActionListener listener() { return listener; }

    public void actionPerformed(ActionEvent e) { listener.actionPerformed(e); }
  }
}
