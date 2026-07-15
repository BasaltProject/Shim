/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.awt.datatransfer;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.CopyOnWriteArrayList;

public class Clipboard {
  String name;
  protected ClipboardOwner owner;
  protected Transferable contents;
  private final CopyOnWriteArrayList<FlavorListener> flavorListeners = new CopyOnWriteArrayList<>();
  private java.util.Set<DataFlavor> currentDataFlavors;

  public Clipboard(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public synchronized void setContents(Transferable contents, ClipboardOwner owner) {
    final ClipboardOwner oldOwner = this.owner;
    final Transferable oldContents = this.contents;
    this.owner = owner;
    this.contents = contents;
    if (oldOwner != null && oldOwner != owner) {
      final Clipboard self = this;
      java.awt.EventQueue.invokeLater(new Runnable() {
        public void run() {
          oldOwner.lostOwnership(self, oldContents);
        }
      });
    }
    fireFlavorsChanged();
  }

  public synchronized Transferable getContents(Object requestor) {
    return contents;
  }

  public DataFlavor[] getAvailableDataFlavors() {
    Transferable cntnts = getContents(null);
    if (cntnts == null) {
      return new DataFlavor[0];
    }
    return cntnts.getTransferDataFlavors();
  }

  public boolean isDataFlavorAvailable(DataFlavor flavor) {
    if (flavor == null) {
      throw new NullPointerException("flavor");
    }
    Transferable cntnts = getContents(null);
    if (cntnts == null) {
      return false;
    }
    return cntnts.isDataFlavorSupported(flavor);
  }

  public Object getData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
    if (flavor == null) {
      throw new NullPointerException("flavor");
    }
    Transferable cntnts = getContents(null);
    if (cntnts == null) {
      throw new UnsupportedFlavorException(flavor);
    }
    return cntnts.getTransferData(flavor);
  }

  public synchronized void addFlavorListener(FlavorListener listener) {
    if (listener == null) {
      return;
    }
    if (flavorListeners.isEmpty()) {
      currentDataFlavors = new java.util.HashSet<>(Arrays.asList(getAvailableDataFlavors()));
    }
    flavorListeners.add(listener);
  }

  public synchronized void removeFlavorListener(FlavorListener listener) {
    if (listener == null) {
      return;
    }
    flavorListeners.remove(listener);
  }

  public synchronized FlavorListener[] getFlavorListeners() {
    return flavorListeners.toArray(new FlavorListener[0]);
  }

  private void fireFlavorsChanged() {
    if (flavorListeners.isEmpty()) {
      return;
    }
    java.util.Set<DataFlavor> prev = currentDataFlavors;
    currentDataFlavors = new java.util.HashSet<>(Arrays.asList(getAvailableDataFlavors()));
    if (prev != null && prev.equals(currentDataFlavors)) {
      return;
    }
    FlavorEvent e = new FlavorEvent(this);
    for (FlavorListener l : flavorListeners) {
      l.flavorsChanged(e);
    }
  }
}
