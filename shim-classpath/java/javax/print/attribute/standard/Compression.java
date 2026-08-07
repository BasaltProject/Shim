/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.print.attribute.standard;

public class Compression extends javax.print.attribute.EnumSyntax implements javax.print.attribute.DocAttribute {
  public static final javax.print.attribute.standard.Compression NONE = new javax.print.attribute.standard.Compression(0);
  public static final javax.print.attribute.standard.Compression DEFLATE = new javax.print.attribute.standard.Compression(1);
  public static final javax.print.attribute.standard.Compression GZIP = new javax.print.attribute.standard.Compression(2);
  public static final javax.print.attribute.standard.Compression COMPRESS = new javax.print.attribute.standard.Compression(3);
  protected Compression(int p0) {
    super(p0);
  }
  public final java.lang.String getName() { return "compression"; }
  public final java.lang.Class<? extends javax.print.attribute.Attribute> getCategory() { return javax.print.attribute.standard.Compression.class; }
  protected java.lang.String[] getStringTable() { return myStringTable.clone(); }
  protected javax.print.attribute.EnumSyntax[] getEnumValueTable() { return (javax.print.attribute.EnumSyntax[]) myEnumValueTable.clone(); }

  private static final String[] myStringTable = {"none", "deflate", "gzip", "compress"};

  private static final javax.print.attribute.EnumSyntax[] myEnumValueTable = {NONE, DEFLATE, GZIP, COMPRESS};
}
