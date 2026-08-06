/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.print;

import java.io.IOException;
import java.io.OutputStream;

public abstract class StreamPrintService implements PrintService {
  private OutputStream outStream;
  private boolean disposed = false;

  private StreamPrintService() { }

  protected StreamPrintService(OutputStream out) { this.outStream = out; }

  public OutputStream getOutputStream() { return outStream; }

  public abstract String getOutputFormat();

  public void dispose() { disposed = true; }

  public boolean isDisposed() { return disposed; }
}
