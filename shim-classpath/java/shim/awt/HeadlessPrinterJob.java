/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.awt;

import java.awt.HeadlessException;
import java.awt.print.PageFormat;
import java.awt.print.Pageable;
import java.awt.print.Printable;
import java.awt.print.PrinterAbortException;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;

public final class HeadlessPrinterJob extends PrinterJob {
  private int copies = 1;
  private String jobName = "Java Printing";
  private boolean cancelled;
  private Printable printable;
  private Pageable pageable;
  private PageFormat pageFormat = new PageFormat();
  private javax.print.PrintService service;

  public void setPrintable(Printable painter) { setPrintable(painter, new PageFormat()); }

  public void setPrintable(Printable painter, PageFormat format) {
    printable = painter;
    pageFormat = format;
    pageable = null;
  }

  public void setPageable(Pageable document) throws NullPointerException {
    if (document == null) {
      throw new NullPointerException();
    }
    pageable = document;
    printable = null;
  }

  public boolean printDialog() throws HeadlessException {
    if (java.awt.GraphicsEnvironment.isHeadless()) {
      throw new HeadlessException();
    }
    return false;
  }

  public PageFormat pageDialog(PageFormat page) throws HeadlessException {
    if (java.awt.GraphicsEnvironment.isHeadless()) {
      throw new HeadlessException();
    }
    return page;
  }

  public PageFormat defaultPage(PageFormat page) {
    PageFormat out = (PageFormat) page.clone();
    java.awt.print.Paper paper = new java.awt.print.Paper();
    javax.print.attribute.standard.MediaSize size = javax.print.attribute.standard.MediaSize
        .getMediaSizeForName(sun.print.PSStreamPrintService.defaultMediaName());
    double w = (float) size.getX(javax.print.attribute.standard.MediaSize.INCH) * 72.0;
    double h = (float) size.getY(javax.print.attribute.standard.MediaSize.INCH) * 72.0;
    paper.setSize(w, h);
    paper.setImageableArea(72, 72, w - 144, h - 144);
    out.setPaper(paper);
    return out;
  }

  public javax.print.PrintService getPrintService() { return service; }

  public void setPrintService(javax.print.PrintService service) throws PrinterException {
    if (service == null) {
      throw new PrinterException("Service cannot be null");
    }
    if (!(service instanceof javax.print.StreamPrintService)) {
      throw new PrinterException("Setting a service is not supported on this class");
    }
    this.service = service;
  }

  public PageFormat validatePage(PageFormat page) { return (PageFormat) page.clone(); }

  public void print() throws PrinterException {
    if (service == null) {
      throw new PrinterException("No printer services available.");
    }
    if (printable == null && pageable == null) {
      return;
    }
    if (cancelled) {
      throw new PrinterAbortException();
    }
    sun.print.PSStreamPrintService ps = (sun.print.PSStreamPrintService) service;
    ps.printPages(pageable, printable, pageFormat, this, copies, jobName);
  }

  public void setCopies(int copies) { this.copies = copies; }

  public int getCopies() { return copies; }

  public String getUserName() { return System.getProperty("user.name"); }

  public void setJobName(String jobName) {
    if (jobName == null) {
      throw new NullPointerException("job name is null");
    }
    this.jobName = jobName;
  }

  public String getJobName() { return jobName; }

  public void cancel() { cancelled = true; }

  public boolean isCancelled() { return cancelled; }
}
