/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.print;

public interface PrintService {
  java.lang.String getName();
  boolean equals(java.lang.Object p0);
  int hashCode();
  javax.print.attribute.PrintServiceAttributeSet getAttributes();
  <T extends javax.print.attribute.PrintServiceAttribute> T getAttribute(java.lang.Class<T> p0);
  boolean isAttributeCategorySupported(java.lang.Class<? extends javax.print.attribute.Attribute> p0);
  java.lang.Object getSupportedAttributeValues(java.lang.Class<? extends javax.print.attribute.Attribute> p0, javax.print.DocFlavor p1, javax.print.attribute.AttributeSet p2);
  boolean isAttributeValueSupported(javax.print.attribute.Attribute p0, javax.print.DocFlavor p1, javax.print.attribute.AttributeSet p2);
  javax.print.DocPrintJob createPrintJob();
  void addPrintServiceAttributeListener(javax.print.event.PrintServiceAttributeListener p0);
  void removePrintServiceAttributeListener(javax.print.event.PrintServiceAttributeListener p0);
  javax.print.DocFlavor[] getSupportedDocFlavors();
  boolean isDocFlavorSupported(javax.print.DocFlavor p0);
  java.lang.Class<?>[] getSupportedAttributeCategories();
  java.lang.Object getDefaultAttributeValue(java.lang.Class<? extends javax.print.attribute.Attribute> p0);
  javax.print.attribute.AttributeSet getUnsupportedAttributes(javax.print.DocFlavor p0, javax.print.attribute.AttributeSet p1);
  javax.print.ServiceUIFactory getServiceUIFactory();
}
