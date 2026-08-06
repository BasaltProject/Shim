/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.print;

import java.util.ArrayList;
import java.util.List;
import javax.print.attribute.AttributeSet;

public abstract class PrintServiceLookup {
  private static final List<PrintServiceLookup> providers = new ArrayList<PrintServiceLookup>();
  private static final List<PrintService> registered = new ArrayList<PrintService>();

  protected PrintServiceLookup() { }

  public static final PrintService[] lookupPrintServices(DocFlavor flavor, AttributeSet attributes) {
    List<PrintService> result = new ArrayList<PrintService>();
    synchronized (PrintServiceLookup.class) {
      for (PrintServiceLookup p : providers) {
        PrintService[] s = p.getPrintServices(flavor, attributes);
        for (PrintService ps : s) {
          result.add(ps);
        }
      }
      for (PrintService ps : registered) {
        if (flavor == null || ps.isDocFlavorSupported(flavor)) {
          result.add(ps);
        }
      }
    }
    return result.toArray(new PrintService[0]);
  }

  public static final MultiDocPrintService[] lookupMultiDocPrintServices(DocFlavor[] flavors,
      AttributeSet attributes) {
    List<MultiDocPrintService> result = new ArrayList<MultiDocPrintService>();
    synchronized (PrintServiceLookup.class) {
      for (PrintServiceLookup p : providers) {
        for (MultiDocPrintService s : p.getMultiDocPrintServices(flavors, attributes)) {
          result.add(s);
        }
      }
    }
    return result.toArray(new MultiDocPrintService[0]);
  }

  public static final PrintService lookupDefaultPrintService() {
    synchronized (PrintServiceLookup.class) {
      for (PrintServiceLookup p : providers) {
        PrintService s = p.getDefaultPrintService();
        if (s != null) {
          return s;
        }
      }
    }
    return null;
  }

  public static boolean registerServiceProvider(PrintServiceLookup sp) {
    synchronized (PrintServiceLookup.class) {
      for (PrintServiceLookup p : providers) {
        if (p.getClass() == sp.getClass()) {
          return false;
        }
      }
      providers.add(sp);
      return true;
    }
  }

  public static boolean registerService(PrintService service) {
    synchronized (PrintServiceLookup.class) {
      if (service instanceof StreamPrintService) {
        return false;
      }
      if (registered.contains(service)) {
        return false;
      }
      registered.add(service);
      return true;
    }
  }

  public abstract PrintService[] getPrintServices(DocFlavor flavor, AttributeSet attributes);

  public abstract PrintService[] getPrintServices();

  public abstract MultiDocPrintService[] getMultiDocPrintServices(DocFlavor[] flavors, AttributeSet attributes);

  public abstract PrintService getDefaultPrintService();
}
