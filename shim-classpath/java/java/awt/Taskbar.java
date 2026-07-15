/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.awt;

public class Taskbar {
  Taskbar() {
  }
  public boolean isSupported(java.awt.Taskbar.Feature p0) { throw new UnsupportedOperationException(); }
  public java.awt.Image getIconImage() { throw new UnsupportedOperationException(); }
  public static boolean isTaskbarSupported() { return false; }
  public void requestUserAttention(boolean p0, boolean p1) { throw new UnsupportedOperationException(); }
  public void requestWindowUserAttention(java.awt.Window p0) { throw new UnsupportedOperationException(); }
  public void setMenu(java.awt.PopupMenu p0) { throw new UnsupportedOperationException(); }
  public void setIconBadge(java.lang.String p0) { throw new UnsupportedOperationException(); }
  public void setWindowIconBadge(java.awt.Window p0, java.awt.Image p1) { throw new UnsupportedOperationException(); }
  public void setProgressValue(int p0) { throw new UnsupportedOperationException(); }
  public void setWindowProgressValue(java.awt.Window p0, int p1) { throw new UnsupportedOperationException(); }
  public void setWindowProgressState(java.awt.Window p0, java.awt.Taskbar.State p1) { throw new UnsupportedOperationException(); }
  public static java.awt.Taskbar getTaskbar() { throw new java.awt.HeadlessException(); }
  public void setIconImage(java.awt.Image p0) { throw new UnsupportedOperationException(); }
  public java.awt.PopupMenu getMenu() { throw new UnsupportedOperationException(); }

  public enum Feature {
    ICON_BADGE_TEXT, ICON_BADGE_NUMBER, ICON_BADGE_IMAGE_WINDOW, ICON_IMAGE, MENU, PROGRESS_STATE_WINDOW, PROGRESS_VALUE, PROGRESS_VALUE_WINDOW, USER_ATTENTION, USER_ATTENTION_WINDOW;
  }

  public enum State {
    OFF, NORMAL, PAUSED, INDETERMINATE, ERROR;
  }
}
