/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.awt.datatransfer;

import java.io.IOException;
import java.io.StringReader;

public class StringSelection implements Transferable, ClipboardOwner {
  private static final int STRING = 0;
  private static final int PLAIN_TEXT = 1;
  private static final DataFlavor[] flavors = {DataFlavor.stringFlavor, DataFlavor.plainTextFlavor};

  private String data;

  public StringSelection(String data) {
    this.data = data;
  }

  public DataFlavor[] getTransferDataFlavors() {
    return flavors.clone();
  }

  public boolean isDataFlavorSupported(DataFlavor flavor) {
    for (DataFlavor f : flavors) {
      if (flavor.equals(f)) {
        return true;
      }
    }
    return false;
  }

  public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
    if (flavor.equals(flavors[STRING])) {
      return (Object) data;
    } else if (flavor.equals(flavors[PLAIN_TEXT])) {
      return new StringReader(data == null ? "" : data);
    } else {
      throw new UnsupportedFlavorException(flavor);
    }
  }

  public void lostOwnership(Clipboard clipboard, Transferable contents) { }
}
