/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */
package sun.print;

import java.io.OutputStream;
import javax.print.DocFlavor;
import javax.print.StreamPrintService;
import javax.print.StreamPrintServiceFactory;

public class PSStreamPrinterFactory extends StreamPrintServiceFactory {
  static final String kFormat = "application/postscript";

  private static final DocFlavor[] kFlavors = {DocFlavor.SERVICE_FORMATTED.PAGEABLE,
      DocFlavor.SERVICE_FORMATTED.PRINTABLE, DocFlavor.BYTE_ARRAY.GIF, DocFlavor.INPUT_STREAM.GIF,
      DocFlavor.URL.GIF, DocFlavor.BYTE_ARRAY.JPEG, DocFlavor.INPUT_STREAM.JPEG, DocFlavor.URL.JPEG,
      DocFlavor.BYTE_ARRAY.PNG, DocFlavor.INPUT_STREAM.PNG, DocFlavor.URL.PNG};

  public String getOutputFormat() {
    return kFormat;
  }

  public DocFlavor[] getSupportedDocFlavors() {
    return kFlavors.clone();
  }

  public StreamPrintService getPrintService(OutputStream out) {
    return new PSStreamPrintService(out);
  }
}
