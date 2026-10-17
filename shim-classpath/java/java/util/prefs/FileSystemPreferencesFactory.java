/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.util.prefs;

final class FileSystemPreferencesFactory implements PreferencesFactory {
  public Preferences userRoot() {
    return FileSystemPreferences.getUserRoot();
  }

  public Preferences systemRoot() {
    return FileSystemPreferences.getSystemRoot();
  }
}
