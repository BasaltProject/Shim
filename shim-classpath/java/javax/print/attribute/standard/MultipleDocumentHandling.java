/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.print.attribute.standard;

public class MultipleDocumentHandling extends javax.print.attribute.EnumSyntax implements javax.print.attribute.PrintRequestAttribute, javax.print.attribute.PrintJobAttribute {
  public static final javax.print.attribute.standard.MultipleDocumentHandling SINGLE_DOCUMENT = new javax.print.attribute.standard.MultipleDocumentHandling(0);
  public static final javax.print.attribute.standard.MultipleDocumentHandling SEPARATE_DOCUMENTS_UNCOLLATED_COPIES = new javax.print.attribute.standard.MultipleDocumentHandling(1);
  public static final javax.print.attribute.standard.MultipleDocumentHandling SEPARATE_DOCUMENTS_COLLATED_COPIES = new javax.print.attribute.standard.MultipleDocumentHandling(2);
  public static final javax.print.attribute.standard.MultipleDocumentHandling SINGLE_DOCUMENT_NEW_SHEET = new javax.print.attribute.standard.MultipleDocumentHandling(3);
  protected MultipleDocumentHandling(int p0) {
    super(p0);
  }
  public final java.lang.String getName() { return "multiple-document-handling"; }
  public final java.lang.Class<? extends javax.print.attribute.Attribute> getCategory() { return javax.print.attribute.standard.MultipleDocumentHandling.class; }
  protected java.lang.String[] getStringTable() { return myStringTable.clone(); }
  protected javax.print.attribute.EnumSyntax[] getEnumValueTable() { return (javax.print.attribute.EnumSyntax[]) myEnumValueTable.clone(); }

  private static final String[] myStringTable = {"single-document", "separate-documents-uncollated-copies", "separate-documents-collated-copies", "single-document-new-sheet"};

  private static final javax.print.attribute.EnumSyntax[] myEnumValueTable = {SINGLE_DOCUMENT, SEPARATE_DOCUMENTS_UNCOLLATED_COPIES, SEPARATE_DOCUMENTS_COLLATED_COPIES, SINGLE_DOCUMENT_NEW_SHEET};
}
