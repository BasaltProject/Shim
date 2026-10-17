/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.xml.stream.events;

public interface StartDocument extends XMLEvent {
  String getSystemId();

  String getCharacterEncodingScheme();

  boolean encodingSet();

  boolean isStandalone();

  boolean standaloneSet();

  String getVersion();
}
