/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing;

import java.awt.Component;
import javax.accessibility.Accessible;
import javax.accessibility.AccessibleContext;

public class ProgressMonitor implements Accessible {
  private Component parentComponent;
  private String note;
  private Object message;
  private int min;
  private int max;
  private int v;
  private int millisToDecideToPopup = 500;
  private int millisToPopup = 2000;
  private boolean closed;
  protected AccessibleContext accessibleContext;

  public ProgressMonitor(Component parentComponent, Object message, String note, int min, int max) {
    this.min = min;
    this.max = max;
    this.parentComponent = parentComponent;
    this.note = note;
    this.message = message;
  }

  public void setProgress(int nv) {
    v = nv;
    if (nv >= max) {
      close();
    }
  }

  public void close() { closed = true; }

  public int getMinimum() { return min; }

  public void setMinimum(int m) { min = m; }

  public int getMaximum() { return max; }

  public void setMaximum(int m) { max = m; }

  public boolean isCanceled() { return false; }

  public void setMillisToDecideToPopup(int millisToDecideToPopup) { this.millisToDecideToPopup = millisToDecideToPopup; }

  public int getMillisToDecideToPopup() { return millisToDecideToPopup; }

  public void setMillisToPopup(int millisToPopup) { this.millisToPopup = millisToPopup; }

  public int getMillisToPopup() { return millisToPopup; }

  public void setNote(String note) { this.note = note; }

  public String getNote() { return note; }

  public AccessibleContext getAccessibleContext() { return accessibleContext; }
}
