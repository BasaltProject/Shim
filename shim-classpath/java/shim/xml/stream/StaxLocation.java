/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.xml.stream;

import javax.xml.stream.Location;

final class StaxLocation implements Location {
  private final int line;
  private final int column;
  private final int offset;
  private final String systemId;
  private final String publicId;

  StaxLocation(int line, int column, int offset, String systemId, String publicId) {
    this.line = line;
    this.column = column;
    this.offset = offset;
    this.systemId = systemId;
    this.publicId = publicId;
  }

  public int getLineNumber() {
    return line;
  }

  public int getColumnNumber() {
    return column;
  }

  public int getCharacterOffset() {
    return offset;
  }

  public String getPublicId() {
    return publicId;
  }

  public String getSystemId() {
    return systemId;
  }

  @Override
  public String toString() {
    return "Line number = " + line + "\nColumn number = " + column + "\nSystem Id = " + systemId
        + "\nPublic Id = " + publicId + "\nLocation Uri= " + systemId + "\nCharacter offset = " + offset;
  }
}
