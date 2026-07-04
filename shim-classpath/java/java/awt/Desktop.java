/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.awt;

public class Desktop {
  Desktop() {
  }
  public boolean isSupported(java.awt.Desktop.Action p0) { throw new UnsupportedOperationException(); }
  public void print(java.io.File p0) throws java.io.IOException { throw new UnsupportedOperationException(); }
  public void open(java.io.File p0) throws java.io.IOException { throw new UnsupportedOperationException(); }
  public static boolean isDesktopSupported() { return false; }
  public void edit(java.io.File p0) throws java.io.IOException { throw new UnsupportedOperationException(); }
  public void browse(java.net.URI p0) throws java.io.IOException { throw new UnsupportedOperationException(); }
  public void mail() throws java.io.IOException { throw new UnsupportedOperationException(); }
  public void mail(java.net.URI p0) throws java.io.IOException { throw new UnsupportedOperationException(); }
  public void addAppEventListener(java.awt.desktop.SystemEventListener p0) { throw new UnsupportedOperationException(); }
  public void removeAppEventListener(java.awt.desktop.SystemEventListener p0) { throw new UnsupportedOperationException(); }
  public void setAboutHandler(java.awt.desktop.AboutHandler p0) { throw new UnsupportedOperationException(); }
  public void setPreferencesHandler(java.awt.desktop.PreferencesHandler p0) { throw new UnsupportedOperationException(); }
  public void setOpenFileHandler(java.awt.desktop.OpenFilesHandler p0) { throw new UnsupportedOperationException(); }
  public void setPrintFileHandler(java.awt.desktop.PrintFilesHandler p0) { throw new UnsupportedOperationException(); }
  public void setOpenURIHandler(java.awt.desktop.OpenURIHandler p0) { throw new UnsupportedOperationException(); }
  public void setQuitHandler(java.awt.desktop.QuitHandler p0) { throw new UnsupportedOperationException(); }
  public void setQuitStrategy(java.awt.desktop.QuitStrategy p0) { throw new UnsupportedOperationException(); }
  public void enableSuddenTermination() { throw new UnsupportedOperationException(); }
  public void disableSuddenTermination() { throw new UnsupportedOperationException(); }
  public void requestForeground(boolean p0) { throw new UnsupportedOperationException(); }
  public void openHelpViewer() { throw new UnsupportedOperationException(); }
  public void setDefaultMenuBar(javax.swing.JMenuBar p0) { throw new UnsupportedOperationException(); }
  public void browseFileDirectory(java.io.File p0) { throw new UnsupportedOperationException(); }
  public boolean moveToTrash(java.io.File p0) { throw new UnsupportedOperationException(); }
  public static java.awt.Desktop getDesktop() { throw new java.awt.HeadlessException(); }

  public enum Action {
    OPEN, EDIT, PRINT, MAIL, BROWSE, APP_EVENT_FOREGROUND, APP_EVENT_HIDDEN, APP_EVENT_REOPENED, APP_EVENT_SCREEN_SLEEP, APP_EVENT_SYSTEM_SLEEP, APP_EVENT_USER_SESSION, APP_ABOUT, APP_PREFERENCES, APP_OPEN_FILE, APP_PRINT_FILE, APP_OPEN_URI, APP_QUIT_HANDLER, APP_QUIT_STRATEGY, APP_SUDDEN_TERMINATION, APP_REQUEST_FOREGROUND, APP_HELP_VIEWER, APP_MENU_BAR, BROWSE_FILE_DIR, MOVE_TO_TRASH;
  }
}
