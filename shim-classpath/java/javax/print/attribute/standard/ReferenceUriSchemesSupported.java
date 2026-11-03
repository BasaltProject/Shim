/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.print.attribute.standard;

public class ReferenceUriSchemesSupported extends javax.print.attribute.EnumSyntax implements javax.print.attribute.Attribute {
  public static final javax.print.attribute.standard.ReferenceUriSchemesSupported FTP = new javax.print.attribute.standard.ReferenceUriSchemesSupported(0);
  public static final javax.print.attribute.standard.ReferenceUriSchemesSupported HTTP = new javax.print.attribute.standard.ReferenceUriSchemesSupported(1);
  public static final javax.print.attribute.standard.ReferenceUriSchemesSupported HTTPS = new javax.print.attribute.standard.ReferenceUriSchemesSupported(2);
  public static final javax.print.attribute.standard.ReferenceUriSchemesSupported GOPHER = new javax.print.attribute.standard.ReferenceUriSchemesSupported(3);
  public static final javax.print.attribute.standard.ReferenceUriSchemesSupported NEWS = new javax.print.attribute.standard.ReferenceUriSchemesSupported(4);
  public static final javax.print.attribute.standard.ReferenceUriSchemesSupported NNTP = new javax.print.attribute.standard.ReferenceUriSchemesSupported(5);
  public static final javax.print.attribute.standard.ReferenceUriSchemesSupported WAIS = new javax.print.attribute.standard.ReferenceUriSchemesSupported(6);
  public static final javax.print.attribute.standard.ReferenceUriSchemesSupported FILE = new javax.print.attribute.standard.ReferenceUriSchemesSupported(7);
  protected ReferenceUriSchemesSupported(int p0) {
    super(p0);
  }
  public final java.lang.String getName() { return "reference-uri-schemes-supported"; }
  public final java.lang.Class<? extends javax.print.attribute.Attribute> getCategory() { return javax.print.attribute.standard.ReferenceUriSchemesSupported.class; }
  protected java.lang.String[] getStringTable() { return myStringTable.clone(); }
  protected javax.print.attribute.EnumSyntax[] getEnumValueTable() { return (javax.print.attribute.EnumSyntax[]) myEnumValueTable.clone(); }

  private static final String[] myStringTable = {"ftp", "http", "https", "gopher", "news", "nntp", "wais", "file"};

  private static final javax.print.attribute.EnumSyntax[] myEnumValueTable = {FTP, HTTP, HTTPS, GOPHER, NEWS, NNTP, WAIS, FILE};
}
