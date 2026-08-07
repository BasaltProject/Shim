/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.imageio.plugins.jpeg;

import javax.imageio.ImageReadParam;

public class JPEGImageReadParam extends ImageReadParam {
  private JPEGQTable[] qTables = null;
  private JPEGHuffmanTable[] DCHuffmanTables = null;
  private JPEGHuffmanTable[] ACHuffmanTables = null;

  public JPEGImageReadParam() { }

  public boolean areTablesSet() { return (qTables != null); }

  public void setDecodeTables(JPEGQTable[] qTables, JPEGHuffmanTable[] DCHuffmanTables,
      JPEGHuffmanTable[] ACHuffmanTables) {
    if ((qTables == null) || (DCHuffmanTables == null) || (ACHuffmanTables == null) || (qTables.length > 4)
        || (DCHuffmanTables.length > 4) || (ACHuffmanTables.length > 4)
        || (DCHuffmanTables.length != ACHuffmanTables.length)) {
      throw new IllegalArgumentException("Invalid JPEG table arrays");
    }
    this.qTables = qTables.clone();
    this.DCHuffmanTables = DCHuffmanTables.clone();
    this.ACHuffmanTables = ACHuffmanTables.clone();
  }

  public void unsetDecodeTables() {
    this.qTables = null;
    this.DCHuffmanTables = null;
    this.ACHuffmanTables = null;
  }

  public JPEGQTable[] getQTables() { return (qTables != null) ? qTables.clone() : null; }

  public JPEGHuffmanTable[] getDCHuffmanTables() { return (DCHuffmanTables != null) ? DCHuffmanTables.clone() : null; }

  public JPEGHuffmanTable[] getACHuffmanTables() { return (ACHuffmanTables != null) ? ACHuffmanTables.clone() : null; }
}
