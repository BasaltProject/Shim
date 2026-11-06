/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */
package sun.print;

import java.io.OutputStream;
import java.util.Locale;
import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.ServiceUIFactory;
import javax.print.StreamPrintService;
import javax.print.attribute.Attribute;
import javax.print.attribute.AttributeSet;
import javax.print.attribute.AttributeSetUtilities;
import javax.print.attribute.HashAttributeSet;
import javax.print.attribute.HashPrintServiceAttributeSet;
import javax.print.attribute.PrintServiceAttribute;
import javax.print.attribute.PrintServiceAttributeSet;
import javax.print.attribute.standard.Chromaticity;
import javax.print.attribute.standard.ColorSupported;
import javax.print.attribute.standard.Copies;
import javax.print.attribute.standard.Fidelity;
import javax.print.attribute.standard.JobName;
import javax.print.attribute.standard.Media;
import javax.print.attribute.standard.MediaPrintableArea;
import javax.print.attribute.standard.MediaSize;
import javax.print.attribute.standard.MediaSizeName;
import javax.print.attribute.standard.OrientationRequested;
import javax.print.attribute.standard.PageRanges;
import javax.print.attribute.standard.RequestingUserName;
import javax.print.attribute.standard.SheetCollate;
import javax.print.attribute.standard.Sides;
import javax.print.event.PrintServiceAttributeListener;

public class PSStreamPrintService extends StreamPrintService {
  private static final Class<?>[] kCategories = {Chromaticity.class, Copies.class, Fidelity.class, JobName.class,
      Media.class, MediaPrintableArea.class, OrientationRequested.class, PageRanges.class,
      RequestingUserName.class, SheetCollate.class, Sides.class};
  private static final MediaSizeName[] kMedia = {MediaSizeName.NA_LETTER, MediaSizeName.TABLOID,
      MediaSizeName.LEDGER, MediaSizeName.NA_LEGAL, MediaSizeName.EXECUTIVE, MediaSizeName.ISO_A3,
      MediaSizeName.ISO_A4, MediaSizeName.ISO_A5, MediaSizeName.ISO_B4, MediaSizeName.ISO_B5};

  private final PrintServiceAttributeSet attributes = new HashPrintServiceAttributeSet();

  PSStreamPrintService(OutputStream out) {
    super(out);
    attributes.add(ColorSupported.SUPPORTED);
  }

  public String getOutputFormat() {
    return PSStreamPrinterFactory.kFormat;
  }

  public String getName() {
    return "Postscript output";
  }

  public String toString() {
    return "PSStreamPrintService: " + getName();
  }

  public DocPrintJob createPrintJob() {
    return new PSStreamPrintJob(this);
  }

  public void addPrintServiceAttributeListener(PrintServiceAttributeListener listener) { }

  public void removePrintServiceAttributeListener(PrintServiceAttributeListener listener) { }

  public PrintServiceAttributeSet getAttributes() {
    return AttributeSetUtilities.unmodifiableView(attributes);
  }

  public <T extends PrintServiceAttribute> T getAttribute(Class<T> category) {
    if (category == null) {
      throw new NullPointerException("category");
    }
    if (!PrintServiceAttribute.class.isAssignableFrom(category)) {
      throw new IllegalArgumentException("Not a PrintServiceAttribute");
    }
    return category.cast(attributes.get(category));
  }

  public DocFlavor[] getSupportedDocFlavors() {
    return new PSStreamPrinterFactory().getSupportedDocFlavors();
  }

  public boolean isDocFlavorSupported(DocFlavor flavor) {
    for (DocFlavor f : getSupportedDocFlavors()) {
      if (f.equals(flavor)) {
        return true;
      }
    }
    return false;
  }

  public Class<?>[] getSupportedAttributeCategories() {
    return kCategories.clone();
  }

  public boolean isAttributeCategorySupported(Class<? extends Attribute> category) {
    if (category == null) {
      throw new NullPointerException("null category");
    }
    if (!Attribute.class.isAssignableFrom(category)) {
      throw new IllegalArgumentException(category + " is not an Attribute");
    }
    for (Class<?> c : kCategories) {
      if (c == category) {
        return true;
      }
    }
    return false;
  }

  public static MediaSizeName defaultMediaName() {
    return defaultMedia();
  }

  public void printPages(java.awt.print.Pageable pageable, java.awt.print.Printable printable,
      java.awt.print.PageFormat base, java.awt.print.PrinterJob job, int copies, String title)
      throws java.awt.print.PrinterException {
    new PSStreamPrintJob(this).printPages(pageable, printable, base, job, copies, title, null);
  }

  static MediaSizeName defaultMedia() {
    String country = Locale.getDefault().getCountry();
    return country.isEmpty() || "US".equals(country) || "CA".equals(country) ? MediaSizeName.NA_LETTER : MediaSizeName.ISO_A4;
  }

  public Object getDefaultAttributeValue(Class<? extends Attribute> category) {
    if (category == null) {
      throw new NullPointerException("null category");
    }
    if (!Attribute.class.isAssignableFrom(category)) {
      throw new IllegalArgumentException(category + " is not an Attribute");
    }
    if (!isAttributeCategorySupported(category)) {
      return null;
    }
    if (category == Copies.class) {
      return new Copies(1);
    } else if (category == Chromaticity.class) {
      return Chromaticity.COLOR;
    } else if (category == Fidelity.class) {
      return Fidelity.FIDELITY_FALSE;
    } else if (category == Media.class) {
      return defaultMedia();
    } else if (category == MediaPrintableArea.class) {
      MediaSize size = MediaSize.getMediaSizeForName(defaultMedia());
      float w = size.getX(MediaSize.MM);
      float h = size.getY(MediaSize.MM);
      return new MediaPrintableArea(12.7f, 12.7f, w - 25.4f, h - 25.4f, MediaSize.MM);
    } else if (category == OrientationRequested.class) {
      return OrientationRequested.PORTRAIT;
    } else if (category == PageRanges.class) {
      return new PageRanges(1, Integer.MAX_VALUE);
    } else if (category == SheetCollate.class) {
      return SheetCollate.UNCOLLATED;
    } else if (category == Sides.class) {
      return Sides.ONE_SIDED;
    }
    return null;
  }

  public Object getSupportedAttributeValues(Class<? extends Attribute> category, DocFlavor flavor,
      AttributeSet attributes) {
    if (category == null) {
      throw new NullPointerException("null category");
    }
    if (!Attribute.class.isAssignableFrom(category)) {
      throw new IllegalArgumentException(category + " does not implement Attribute");
    }
    if (flavor != null && !isDocFlavorSupported(flavor)) {
      throw new IllegalArgumentException(flavor + " is an unsupported flavor");
    }
    if (!isAttributeCategorySupported(category)) {
      return null;
    }
    if (category == Chromaticity.class) {
      return new Chromaticity[] {Chromaticity.COLOR};
    } else if (category == Copies.class) {
      return new javax.print.attribute.standard.CopiesSupported(1, 1000);
    } else if (category == Fidelity.class) {
      return new Fidelity[] {Fidelity.FIDELITY_FALSE, Fidelity.FIDELITY_TRUE};
    } else if (category == JobName.class || category == RequestingUserName.class) {
      return "";
    } else if (category == Media.class) {
      return kMedia.clone();
    } else if (category == MediaPrintableArea.class) {
      return null;
    } else if (category == OrientationRequested.class) {
      return new OrientationRequested[] {OrientationRequested.PORTRAIT, OrientationRequested.LANDSCAPE,
          OrientationRequested.REVERSE_LANDSCAPE};
    } else if (category == PageRanges.class) {
      return new PageRanges[] {new PageRanges(1, Integer.MAX_VALUE)};
    } else if (category == SheetCollate.class) {
      return new SheetCollate[] {SheetCollate.UNCOLLATED, SheetCollate.COLLATED};
    } else if (category == Sides.class) {
      return new Sides[] {Sides.ONE_SIDED, Sides.TWO_SIDED_LONG_EDGE, Sides.TWO_SIDED_SHORT_EDGE};
    }
    return null;
  }

  public boolean isAttributeValueSupported(Attribute attr, DocFlavor flavor, AttributeSet attributes) {
    if (attr == null) {
      throw new NullPointerException("null attribute");
    }
    if (flavor != null && !isDocFlavorSupported(flavor)) {
      throw new IllegalArgumentException(flavor + " is an unsupported flavor");
    }
    Class<? extends Attribute> category = attr.getCategory();
    if (!isAttributeCategorySupported(category)) {
      return false;
    }
    if (category == Copies.class) {
      int v = ((Copies) attr).getValue();
      return v >= 1 && v <= 1000;
    } else if (category == Media.class) {
      if (attr instanceof MediaSizeName) {
        for (MediaSizeName m : kMedia) {
          if (m.equals(attr)) {
            return true;
          }
        }
        return false;
      }
      return false;
    } else if (category == Chromaticity.class) {
      return attr == Chromaticity.COLOR;
    } else if (category == PageRanges.class) {
      return true;
    } else if (category == MediaPrintableArea.class) {
      return true;
    }
    Object values = getSupportedAttributeValues(category, flavor, attributes);
    if (values instanceof Object[]) {
      for (Object o : (Object[]) values) {
        if (o.equals(attr)) {
          return true;
        }
      }
      return false;
    }
    return true;
  }

  public AttributeSet getUnsupportedAttributes(DocFlavor flavor, AttributeSet attributes) {
    if (flavor != null && !isDocFlavorSupported(flavor)) {
      throw new IllegalArgumentException("flavor " + flavor + "is not supported");
    }
    if (attributes == null) {
      return null;
    }
    HashAttributeSet unsupported = new HashAttributeSet();
    for (Attribute a : attributes.toArray()) {
      if (!isAttributeCategorySupported(a.getCategory()) || !isAttributeValueSupported(a, flavor, attributes)) {
        unsupported.add(a);
      }
    }
    return unsupported.isEmpty() ? null : unsupported;
  }

  public ServiceUIFactory getServiceUIFactory() {
    return null;
  }

  public boolean equals(Object obj) {
    return obj == this || obj instanceof PSStreamPrintService && ((PSStreamPrintService) obj).getOutputStream() == getOutputStream();
  }

  public int hashCode() {
    return System.identityHashCode(getOutputStream());
  }
}
