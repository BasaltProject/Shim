/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */
package sun.print;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Pageable;
import java.awt.print.Paper;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import javax.print.Doc;
import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintException;
import javax.print.PrintService;
import javax.print.attribute.Attribute;
import javax.print.attribute.HashPrintJobAttributeSet;
import javax.print.attribute.AttributeSetUtilities;
import javax.print.attribute.PrintJobAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.Copies;
import javax.print.attribute.standard.JobName;
import javax.print.attribute.standard.Media;
import javax.print.attribute.standard.MediaPrintableArea;
import javax.print.attribute.standard.MediaSize;
import javax.print.attribute.standard.MediaSizeName;
import javax.print.attribute.standard.OrientationRequested;
import javax.print.attribute.standard.PageRanges;
import javax.print.event.PrintJobAttributeListener;
import javax.print.event.PrintJobEvent;
import javax.print.event.PrintJobListener;
import shim.awt.SoftwareGraphics2D;

public class PSStreamPrintJob implements DocPrintJob {
  private static final double kDeviceDpi = 300.0;
  private static final double kScale = kDeviceDpi / 72.0;

  private final PSStreamPrintService service;
  private final List<PrintJobListener> listeners = new ArrayList<PrintJobListener>();
  private final PrintJobAttributeSet attributes = new HashPrintJobAttributeSet();
  private boolean printing;
  private boolean printed;

  PSStreamPrintJob(PSStreamPrintService service) {
    this.service = service;
  }

  public PrintService getPrintService() {
    return service;
  }

  public PrintJobAttributeSet getAttributes() {
    return AttributeSetUtilities.unmodifiableView(attributes);
  }

  public synchronized void addPrintJobListener(PrintJobListener listener) {
    if (listener != null && !listeners.contains(listener)) {
      listeners.add(listener);
    }
  }

  public synchronized void removePrintJobListener(PrintJobListener listener) {
    listeners.remove(listener);
  }

  public void addPrintJobAttributeListener(PrintJobAttributeListener listener, PrintJobAttributeSet set) { }

  public void removePrintJobAttributeListener(PrintJobAttributeListener listener) { }

  private void notifyEvent(int reason) {
    PrintJobListener[] copy;
    synchronized (this) {
      copy = listeners.toArray(new PrintJobListener[0]);
    }
    PrintJobEvent event = new PrintJobEvent(this, reason);
    for (PrintJobListener l : copy) {
      switch (reason) {
        case PrintJobEvent.JOB_COMPLETE:
          l.printJobCompleted(event);
          break;
        case PrintJobEvent.JOB_FAILED:
          l.printJobFailed(event);
          break;
        default:
          break;
      }
    }
  }

  public void print(Doc doc, PrintRequestAttributeSet attrs) throws PrintException {
    synchronized (this) {
      if (printing || printed) {
        throw new PrintException("already printing");
      }
      printing = true;
    }
    try {
      DocFlavor flavor = doc.getDocFlavor();
      if (flavor == null || !service.isDocFlavorSupported(flavor)) {
        notifyEvent(PrintJobEvent.JOB_FAILED);
        throw new PrintJobFlavorException("invalid flavor", flavor);
      }
      Object data;
      try {
        data = doc.getPrintData();
      } catch (IOException e) {
        notifyEvent(PrintJobEvent.JOB_FAILED);
        throw new PrintException(e);
      }
      String title = jobName(attrs);
      int copies = 1;
      if (attrs != null && attrs.get(Copies.class) != null) {
        copies = ((Copies) attrs.get(Copies.class)).getValue();
      }
      PrinterJob job = new shim.awt.HeadlessPrinterJob();
      try {
        PSDocument ps = new PSDocument(service.getOutputStream(), title, copies);
        PageFormat base = pageFormat(attrs);
        PageRanges ranges = attrs == null ? null : (PageRanges) attrs.get(PageRanges.class);
        ps.begin();
        if (data instanceof Pageable) {
          writePages((Pageable) data, null, base, ps, ranges, job);
        } else if (data instanceof Printable) {
          writePages(null, (Printable) data, base, ps, ranges, job);
        } else {
          writeImage(data, base, ps, job);
        }
        ps.end();
      } catch (PrinterException e) {
        notifyEvent(PrintJobEvent.JOB_FAILED);
        throw new PrintException(e);
      } catch (IOException | PSPageGraphics.PrintFailure e) {
        notifyEvent(PrintJobEvent.JOB_FAILED);
        throw new PrintException(e);
      }
      notifyEvent(PrintJobEvent.JOB_COMPLETE);
    } finally {
      synchronized (this) {
        printing = false;
        printed = true;
      }
    }
  }

  private static String jobName(PrintRequestAttributeSet attrs) {
    if (attrs != null && attrs.get(JobName.class) != null) {
      return ((JobName) attrs.get(JobName.class)).getValue();
    }
    return "Java Printing";
  }

  static PageFormat pageFormat(PrintRequestAttributeSet attrs) {
    if (attrs == null) {
      return new PageFormat();
    }
    Media media = (Media) attrs.get(Media.class);
    OrientationRequested orient = (OrientationRequested) attrs.get(OrientationRequested.class);
    MediaPrintableArea area = (MediaPrintableArea) attrs.get(MediaPrintableArea.class);
    if (media == null && orient == null && area == null) {
      return new PageFormat();
    }
    MediaSizeName name = media instanceof MediaSizeName ? (MediaSizeName) media : PSStreamPrintService.defaultMedia();
    MediaSize size = MediaSize.getMediaSizeForName(name);
    if (size == null) {
      size = MediaSize.getMediaSizeForName(PSStreamPrintService.defaultMedia());
    }
    double w = (float) size.getX(MediaSize.INCH) * 72.0;
    double h = (float) size.getY(MediaSize.INCH) * 72.0;
    Paper paper = new Paper();
    paper.setSize(w, h);
    if (area != null) {
      paper.setImageableArea(area.getX(MediaSize.INCH) * 72.0, area.getY(MediaSize.INCH) * 72.0,
          area.getWidth(MediaSize.INCH) * 72.0, area.getHeight(MediaSize.INCH) * 72.0);
    } else {
      paper.setImageableArea(36, 36, w - 72, h - 72);
    }
    PageFormat pf = new PageFormat();
    pf.setPaper(paper);
    if (orient == OrientationRequested.LANDSCAPE) {
      pf.setOrientation(PageFormat.LANDSCAPE);
    } else if (orient == OrientationRequested.REVERSE_LANDSCAPE) {
      pf.setOrientation(PageFormat.REVERSE_LANDSCAPE);
    }
    return pf;
  }

  private Graphics2D newState(PageFormat pf) {
    Paper p = pf.getPaper();
    int w = (int) Math.ceil(p.getWidth() * kScale);
    int h = (int) Math.ceil(p.getHeight() * kScale);
    BufferedImage image = new BufferedImage(Math.max(w, 1), Math.max(h, 1), BufferedImage.TYPE_BYTE_BINARY);
    Graphics2D g = new SoftwareGraphics2D(image);
    AffineTransform base = new AffineTransform();
    base.scale(kScale, kScale);
    base.concatenate(new AffineTransform(pf.getMatrix()));
    g.setColor(java.awt.Color.BLACK);
    g.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_OFF);
    g.setTransform(base);
    g.setClip(new Rectangle2D.Double(pf.getImageableX(), pf.getImageableY(), pf.getImageableWidth(),
        pf.getImageableHeight()));
    return g;
  }

  static boolean inRange(PageRanges ranges, int pageIndex) {
    return ranges == null || ranges.contains(pageIndex + 1);
  }

  private void writePages(Pageable pageable, Printable printable, PageFormat base, PSDocument ps, PageRanges ranges,
      PrinterJob job) throws PrinterException, IOException {
    int total = pageable == null ? Pageable.UNKNOWN_NUMBER_OF_PAGES : pageable.getNumberOfPages();
    int maxPage = Integer.MAX_VALUE;
    if (ranges != null) {
      int[][] members = ranges.getMembers();
      maxPage = members.length == 0 ? 0 : members[members.length - 1][1];
    }
    for (int i = 0; total == Pageable.UNKNOWN_NUMBER_OF_PAGES || i < total; i++) {
      if (i + 1 > maxPage) {
        break;
      }
      if (!inRange(ranges, i)) {
        continue;
      }
      PageFormat pf = pageable == null ? base : pageable.getPageFormat(i);
      Printable p = pageable == null ? printable : pageable.getPrintable(i);
      Graphics2D peek = newState(pf);
      int peekResult = p.print(new PSPageGraphics(peek, null, job), pf, i);
      peek.dispose();
      if (peekResult == Printable.NO_SUCH_PAGE) {
        break;
      }
      Graphics2D state = newState(pf);
      ps.beginPage(pf.getPaper().getWidth(), pf.getPaper().getHeight(), kScale);
      int result = p.print(new PSPageGraphics(state, ps, job), pf, i);
      ps.endPage();
      state.dispose();
      if (result == Printable.NO_SUCH_PAGE) {
        break;
      }
    }
  }

  private void writeImage(Object data, PageFormat base, PSDocument ps, PrinterJob job)
      throws PrinterException, IOException {
    BufferedImage image;
    if (data instanceof byte[]) {
      image = ImageIO.read(new java.io.ByteArrayInputStream((byte[]) data));
    } else if (data instanceof InputStream) {
      image = ImageIO.read((InputStream) data);
    } else if (data instanceof java.net.URL) {
      image = ImageIO.read((java.net.URL) data);
    } else {
      throw new PrinterException("unsupported print data");
    }
    if (image == null) {
      throw new PrinterException("unreadable image data");
    }
    Graphics2D state = newState(base);
    ps.beginPage(base.getPaper().getWidth(), base.getPaper().getHeight(), kScale);
    PSPageGraphics g = new PSPageGraphics(state, ps, job);
    double fit = Math.min(1.0, Math.min(base.getImageableWidth() / image.getWidth(),
        base.getImageableHeight() / image.getHeight()));
    g.drawImage(image, (int) base.getImageableX(), (int) base.getImageableY(),
        Math.max(1, (int) Math.round(image.getWidth() * fit)), Math.max(1, (int) Math.round(image.getHeight() * fit)),
        null);
    ps.endPage();
    state.dispose();
  }

  void printPages(Pageable pageable, Printable printable, PageFormat base, PrinterJob job, int copies, String title,
      PageRanges ranges) throws PrinterException {
    try {
      PSDocument ps = new PSDocument(service.getOutputStream(), title, copies);
      ps.begin();
      writePages(pageable, printable, base, ps, ranges, job);
      ps.end();
    } catch (IOException | PSPageGraphics.PrintFailure e) {
      throw new java.awt.print.PrinterIOException(e instanceof IOException ? (IOException) e : new IOException(e));
    }
  }
}
