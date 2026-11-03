/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.print;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

public abstract class StreamPrintServiceFactory {
  protected StreamPrintServiceFactory() { }

  public static StreamPrintServiceFactory[] lookupStreamPrintServiceFactories(DocFlavor flavor,
      String outputMimeType) {
    List<StreamPrintServiceFactory> result = new ArrayList<StreamPrintServiceFactory>();
    List<StreamPrintServiceFactory> all = new ArrayList<StreamPrintServiceFactory>();
    all.add(new sun.print.PSStreamPrinterFactory());
    for (StreamPrintServiceFactory f : ServiceLoader.load(StreamPrintServiceFactory.class)) {
      all.add(f);
    }
    for (StreamPrintServiceFactory f : all) {
      if (outputMimeType == null || f.getOutputFormat().equalsIgnoreCase(outputMimeType)) {
        if (flavor == null) {
          result.add(f);
        } else {
          for (DocFlavor d : f.getSupportedDocFlavors()) {
            if (d.equals(flavor)) {
              result.add(f);
              break;
            }
          }
        }
      }
    }
    return result.toArray(new StreamPrintServiceFactory[0]);
  }

  public abstract String getOutputFormat();

  public abstract DocFlavor[] getSupportedDocFlavors();

  public abstract StreamPrintService getPrintService(OutputStream out);
}
