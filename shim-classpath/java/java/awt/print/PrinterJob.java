/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.awt.print;

public abstract class PrinterJob {
  public PrinterJob() {
  }
  public abstract void print() throws java.awt.print.PrinterException;
  public void print(javax.print.attribute.PrintRequestAttributeSet p0) throws java.awt.print.PrinterException {
    print();
  }
  public abstract void cancel();
  public abstract boolean isCancelled();
  public java.awt.print.PageFormat getPageFormat(javax.print.attribute.PrintRequestAttributeSet p0) {
    PageFormat page = defaultPage();
    return page;
  }
  public static java.awt.print.PrinterJob getPrinterJob() {
    return new shim.awt.HeadlessPrinterJob();
  }
  public static javax.print.PrintService[] lookupPrintServices() {
    return javax.print.PrintServiceLookup.lookupPrintServices(javax.print.DocFlavor.SERVICE_FORMATTED.PRINTABLE, null);
  }
  public java.awt.print.PageFormat defaultPage() {
    return defaultPage(new PageFormat());
  }
  public abstract java.awt.print.PageFormat defaultPage(java.awt.print.PageFormat p0);
  public abstract java.awt.print.PageFormat pageDialog(java.awt.print.PageFormat p0) throws java.awt.HeadlessException;
  public java.awt.print.PageFormat pageDialog(javax.print.attribute.PrintRequestAttributeSet p0) throws java.awt.HeadlessException {
    if (java.awt.GraphicsEnvironment.isHeadless()) {
      throw new java.awt.HeadlessException();
    }
    return pageDialog(defaultPage());
  }
  public javax.print.PrintService getPrintService() {
    return null;
  }
  public abstract java.awt.print.PageFormat validatePage(java.awt.print.PageFormat p0);
  public static javax.print.StreamPrintServiceFactory[] lookupStreamPrintServices(java.lang.String p0) {
    return javax.print.StreamPrintServiceFactory.lookupStreamPrintServiceFactories(javax.print.DocFlavor.SERVICE_FORMATTED.PRINTABLE, p0);
  }
  public void setPrintService(javax.print.PrintService p0) throws java.awt.print.PrinterException {
    throw new PrinterException("Setting a service is not supported on this class");
  }
  public abstract void setPrintable(java.awt.print.Printable p0, java.awt.print.PageFormat p1);
  public abstract void setPrintable(java.awt.print.Printable p0);
  public abstract void setPageable(java.awt.print.Pageable p0) throws java.lang.NullPointerException;
  public abstract java.lang.String getUserName();
  public abstract void setJobName(java.lang.String p0);
  public abstract java.lang.String getJobName();
  public abstract void setCopies(int p0);
  public abstract int getCopies();
  public boolean printDialog(javax.print.attribute.PrintRequestAttributeSet p0) throws java.awt.HeadlessException {
    if (p0 == null) {
      throw new NullPointerException("attributes");
    }
    return printDialog();
  }
  public abstract boolean printDialog() throws java.awt.HeadlessException;
}
